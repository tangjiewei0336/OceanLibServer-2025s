package com.oriole.ocean.controller;

import com.aliyun.oss.model.CannedAccessControlList;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.service.FileSearchService;
import com.oriole.ocean.common.service.FileService;
import com.oriole.ocean.common.service.NotifyService;
import com.oriole.ocean.common.service.NotifySubscriptionService;
import com.oriole.ocean.common.tools.JwtUtils;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.config.OSSConfig;
import com.oriole.ocean.common.enumerate.*;
import com.oriole.ocean.common.po.mysql.*;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.*;
import com.oriole.ocean.utils.OpenOfficeUtils;
import com.oriole.ocean.utils.PdfUtils;
import com.oriole.ocean.utils.objectStorage.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/docFileService")
public class DocIOController {

    @Autowired
    FileUploadTempService fileUploadTempService;

    @DubboReference
    FileService fileService;

    @DubboReference
    FileSearchService fileSearchService;

    @DubboReference
    NotifySubscriptionService notifySubscriptionService;

    @Autowired
    OSSConfig ossConfig;

    @Autowired
    AliOSSUtils aliOSSUtils;

    @Autowired
    OpenOfficeUtils openOfficeUtils;

    private final static String[] allowFileType = new String[]{".PPT", ".PPTX", ".DOC", ".DOCX", ".XLS", ".XLSX", ".PDF"};
    private final static String[] allowPicFileType = new String[]{".PNG", ".JPG"};
    private final static String previewFileSuffix = "_preview.pdf";
    private final static String previewPicSuffix = "_preview_img.jpg";

    @Value("${temp.path}")
    public String TEMP_SAVE_PATH;

    @Value("${auth.download.token.secretkey}")
    public String DOWNLOAD_TOKEN_ENCODED_SECRET_KEY;

    @RequestMapping(value = "/getIncompleteInfoFileList", method = RequestMethod.GET)
    public MsgEntity<PageInfo<FileUploadTempEntity>> getIncompleteInfoFileList(@AuthUser AuthUserEntity authUser,
                                                                               @RequestParam(required = false) String username,
                                                                               @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        username = authUser.getAllowOperationUsername(username);

        PageHelper.startPage(pageNum, pageSize, true);
        PageInfo<FileUploadTempEntity> fileUploadTempEntityListPageInfo = new PageInfo<>(fileUploadTempService.getAllUploadTempFileInfoByUsername(username));
        return new MsgEntity<>("SUCCESS", "1", fileUploadTempEntityListPageInfo);
    }
    @RequestMapping(value = "/getUploadFileTempInfo", method = RequestMethod.GET)
    public MsgEntity<FileUploadTempEntity> getUploadFileTempInfo(@AuthUser String username, @RequestParam Integer uploadID) throws Exception {
        return new MsgEntity<>("SUCCESS", "1", fileUploadTempService.getUploadTempFileInfo(uploadID, username));
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public MsgEntity<FileUploadTempEntity> uploadFile(@AuthUser String username,
                                                      @RequestParam(value = "uploadFile") MultipartFile uploadFile, @RequestParam String uploadApp) throws Exception {
        String fileName = RandomStringUtils.randomAlphanumeric(32).toUpperCase();
        if (uploadFile.isEmpty()) {
            throw new BusinessException("-2", "文件未正常上传，没有获取到MultipartFile");
        } else {
            if (!Objects.requireNonNull(uploadFile.getOriginalFilename()).contains(".")) {
                throw new BusinessException("-4", "文件名解析异常");
            }
            String suffix = uploadFile.getOriginalFilename().substring(uploadFile.getOriginalFilename().lastIndexOf("."));
            if (suffix.isEmpty() || !Arrays.asList(allowFileType).contains(suffix.toUpperCase())) {
                throw new BusinessException("-3", "非法的文件后缀格式");
            }

            //直接上传至OSS
            aliOSSUtils.inputToOSS(uploadFile.getInputStream(), ossConfig.TEMP_DOCUMENT_PATH + fileName + suffix, CannedAccessControlList.Private);

            FileUploadTempEntity fileUploadTempEntity = new FileUploadTempEntity();
            fileUploadTempEntity.setFileName(fileName);
            fileUploadTempEntity.setFileSuffix(suffix);

            fileUploadTempEntity.setTitle(uploadFile.getOriginalFilename().substring(0, uploadFile.getOriginalFilename().indexOf(suffix)));
            fileUploadTempEntity.setSize((int) uploadFile.getSize());

            fileUploadTempEntity.setUploadUsername(username);
            fileUploadTempEntity.setUploadApp(uploadApp);

            fileUploadTempService.saveOrUpdateUploadTempFileInfo(fileUploadTempEntity);
            return new MsgEntity<>("SUCCESS", "1", fileUploadTempEntity);
        }
    }

    @RequestMapping(value = "/uploadPicFile", method = RequestMethod.POST)
    public MsgEntity<FileUploadTempEntity> uploadFile(@AuthUser String username,
                                                      @RequestParam(value = "uploadPicFiles") MultipartFile[] uploadFiles, @RequestParam String uploadApp) throws Exception {
        String fileName = RandomStringUtils.randomAlphanumeric(32).toUpperCase();
        if (uploadFiles.length == 0) {
            throw new BusinessException("-2", "文件未正常上传，没有获取到任何MultipartFile列表");
        }
        List<File> files = new ArrayList<>();
        for (int index = 0; index < uploadFiles.length; index++) {
            MultipartFile uploadFile = uploadFiles[index];
            if (!Objects.requireNonNull(uploadFile.getOriginalFilename()).contains(".")) {
                throw new BusinessException("-4", "文件名解析异常");
            }
            String suffix = uploadFile.getOriginalFilename().substring(uploadFile.getOriginalFilename().lastIndexOf("."));
            if (!Arrays.asList(allowPicFileType).contains(suffix.toUpperCase())) {
                throw new BusinessException("-3", "非法的文件后缀格式");
            }
            File inputTempFile = new File(TEMP_SAVE_PATH + fileName + "_" + index + suffix);
            try {
                java.nio.file.Files.copy(
                        uploadFile.getInputStream(),
                        inputTempFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                files.add(inputTempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PdfUtils pdfUtils = new PdfUtils(new File(TEMP_SAVE_PATH + fileName + ".pdf"));
        pdfUtils.pic2Pdf(files);

        File pdfFile = new File(TEMP_SAVE_PATH + fileName + ".pdf");

        //直接上传至OSS
        aliOSSUtils.inputToOSS(new FileInputStream(pdfFile), ossConfig.TEMP_DOCUMENT_PATH + fileName + ".pdf", CannedAccessControlList.Private);

        FileUploadTempEntity fileUploadTempEntity = new FileUploadTempEntity();
        fileUploadTempEntity.setFileName(fileName);
        fileUploadTempEntity.setFileSuffix(".pdf");

        fileUploadTempEntity.setTitle("用户笔记文档");
        fileUploadTempEntity.setSize((int) pdfFile.length());

        fileUploadTempEntity.setUploadUsername(username);
        fileUploadTempEntity.setUploadApp(uploadApp);

        fileUploadTempService.saveOrUpdateUploadTempFileInfo(fileUploadTempEntity);
        return new MsgEntity<>("SUCCESS", "1", fileUploadTempEntity);
    }

    @RequestMapping(value = "/doTempFilePreview", method = RequestMethod.GET)
    public MsgEntity<String> doTempFilePreview(@AuthUser String username, @RequestParam Integer uploadID) throws Exception {
        // 获取临时文件信息
        FileUploadTempEntity fileUploadTempEntity = fileUploadTempService.getUploadTempFileInfo(uploadID, username);

        if (fileUploadTempEntity.getPreviewPdfName() != null && !fileUploadTempEntity.getPreviewPdfName().isEmpty()) {
            return new MsgEntity<>("SUCCESS", "2", ossConfig.TEMP_DOCUMENT_PATH + fileUploadTempEntity.getPreviewPdfName() + previewFileSuffix);
        }

        String suffix = fileUploadTempEntity.getFileSuffix();
        String fileName = fileUploadTempEntity.getFileName();
        // 拉取原始文件处理为PDF预览文件
        aliOSSUtils.downloadToTempFromOSS(ossConfig.TEMP_DOCUMENT_PATH, TEMP_SAVE_PATH, fileName + suffix);
        if (suffix.equalsIgnoreCase(".doc") || suffix.equalsIgnoreCase(".docx")
                || suffix.equalsIgnoreCase(".xls") || suffix.equalsIgnoreCase(".xlsx")
                || suffix.equalsIgnoreCase(".ppt") || suffix.equalsIgnoreCase(".pptx")) {
            //文档文件处理政策是OpenOffice转PDF
            Boolean result = openOfficeUtils.openOfficeExperience(TEMP_SAVE_PATH, fileName, suffix, previewFileSuffix);

            if (result) {
                //直接上传至OSS
                aliOSSUtils.inputToOSS(new FileInputStream(TEMP_SAVE_PATH + fileName + previewFileSuffix), ossConfig.TEMP_DOCUMENT_PATH + fileName + previewFileSuffix, CannedAccessControlList.PublicRead);

                fileUploadTempEntity.setPreviewPdfName(fileName);
                fileUploadTempService.saveOrUpdateUploadTempFileInfo(fileUploadTempEntity);
                return new MsgEntity<>("SUCCESS", "1", ossConfig.TEMP_DOCUMENT_PATH + fileName + previewFileSuffix);
            } else {
                throw new BusinessException("-2", "Document conversion PDF exception!");
            }

        } else if (suffix.equalsIgnoreCase(".pdf")) {
            //PDF文件处理政策是复制(已经废弃！)
            //Files.copy(new File(Constant.TEMP_SAVE_PATH + fileName + suffix).toPath(), new File(Constant.TEMP_SAVE_PATH + fileName + previewFileSuffix).toPath());

            aliOSSUtils.inputToOSS(new FileInputStream(TEMP_SAVE_PATH + fileName + suffix), ossConfig.TEMP_DOCUMENT_PATH + fileName + previewFileSuffix, CannedAccessControlList.PublicRead);
            fileUploadTempEntity.setPreviewPdfName(fileName);
            fileUploadTempService.saveOrUpdateUploadTempFileInfo(fileUploadTempEntity);
            return new MsgEntity<>("SUCCESS", "1", ossConfig.TEMP_DOCUMENT_PATH + fileName + previewFileSuffix);
        } else {
            throw new BusinessException("-3", "Other files that cannot be processed!");
        }
    }

    @RequestMapping(value = "/submitDocumentInfo", method = RequestMethod.POST)
    public MsgEntity<String> submitDocumentInfo(@AuthUser String username, @RequestParam Integer uploadID, @RequestParam String title, @RequestParam String abstractContent,
                                                @RequestParam Byte paymentMethod, @RequestParam Integer paymentAmount,
                                                @RequestParam Byte isAllowAnon,
                                                @RequestParam Byte isAllowVipfree,
                                                @RequestParam Byte isAllowComment,
                                                @RequestParam(required = false) Integer folderID,
                                                @RequestParam(required = false) String copyrightNotice,
                                                @RequestParam(required = false) Byte isOriginal,
                                                @RequestParam(required = false) String originalAuthor
    ) throws Exception {
        // 获取临时文件信息
        FileUploadTempEntity fileUploadTempEntity = fileUploadTempService.getUploadTempFileInfo(uploadID, username);
        if (fileUploadTempEntity.getPreviewPdfName() == null || fileUploadTempEntity.getPreviewPdfName().isEmpty()) {
            throw new BusinessException("-2", "请等待文件预览文件转换结束");
        }
        String fileName = fileUploadTempEntity.getFileName();
        String suffix = fileUploadTempEntity.getFileSuffix();

        // 拉取原始文件处理为PDF预览文件
        aliOSSUtils.downloadToTempFromOSS(ossConfig.TEMP_DOCUMENT_PATH, TEMP_SAVE_PATH, fileName + previewFileSuffix);
        PdfUtils pdfUtils = new PdfUtils(new File(TEMP_SAVE_PATH + fileName + previewFileSuffix));

        pdfUtils.doWaterMark("Ocean文库 盗版必究 Only For Preview");//加水印
        pdfUtils.getPreviewPic(TEMP_SAVE_PATH, fileName, previewPicSuffix);//取预览

        String searchText = pdfUtils.getTextString();
        //移动原始文件至正式区
        aliOSSUtils.moveFileInOSS(ossConfig.TEMP_DOCUMENT_PATH + fileName + suffix, ossConfig.ORIGINAL_DOCUMENT_PATH + fileName + suffix, CannedAccessControlList.Private);
        //上传预览文件（PDF和封面图片）至正式区
        aliOSSUtils.inputToOSS(new FileInputStream(TEMP_SAVE_PATH + fileName + previewFileSuffix), ossConfig.PREVIEW_DOCUMENT_PATH + fileName + previewFileSuffix, CannedAccessControlList.PublicRead);
        aliOSSUtils.inputToOSS(new FileInputStream(TEMP_SAVE_PATH + fileName + previewPicSuffix), ossConfig.PREVIEW_DOCUMENT_PATH + fileName + previewPicSuffix, CannedAccessControlList.PublicRead);

        FileEntity newFileEntity = new FileEntity();
        newFileEntity.setTitle(title);
        //如果用户未填写abstractContent(为“”)，则从搜索摘要中选取最长128个字符填入
        newFileEntity.setAbstractContent(abstractContent.equals("") ? searchText.substring(0, searchText.length() < 128 ? searchText.length() : 127) : abstractContent);
        newFileEntity.setSize(fileUploadTempEntity.getSize());

        newFileEntity.setRealObjectName(ossConfig.ORIGINAL_DOCUMENT_PATH + fileName + suffix);
        newFileEntity.setPreviewPictureObjectName(ossConfig.PREVIEW_DOCUMENT_PATH + fileName + previewPicSuffix);
        newFileEntity.setPreviewPdfObjectName(ossConfig.PREVIEW_DOCUMENT_PATH + fileName + previewFileSuffix);

        newFileEntity.setFileType(suffix.replace(".", ""));
        newFileEntity.setUploadUsername(username);
        newFileEntity.setUploadDate(new Date());

        newFileEntity.setPaymentMethod(paymentMethod);
        newFileEntity.setPaymentAmount(paymentAmount);

        newFileEntity.setIsAllowAnon(isAllowAnon);
        newFileEntity.setIsAllowComment(isAllowComment);
        newFileEntity.setIsAllowVipfree(isAllowVipfree);

        newFileEntity.setFolderID(folderID);

        FileExtraEntity newFileExtraEntity = new FileExtraEntity(newFileEntity.getFileID(), isOriginal, originalAuthor, copyrightNotice);
        newFileEntity.setFileExtraEntity(newFileExtraEntity);

        // 写数据库
        fileService.saveOrUpdateFileInfo(newFileEntity);
        fileSearchService.saveOrUpdateFileSearchInfo(new FileSearchEntity(newFileEntity.getFileID(), searchText));

        // 产生用户消息订阅
        List<NotifyAction> notifyActionList = new ArrayList<>();
        notifyActionList.add(NotifyAction.DOWNLOAD);
        notifyActionList.add(NotifyAction.LIKE);
        notifyActionList.add(NotifyAction.SCORED);
        notifyActionList.add(NotifyAction.NEW_COMMENT);
        notifySubscriptionService.setNotifySubscription(username, notifyActionList,
                String.valueOf(newFileEntity.getFileID()), NotifySubscriptionTargetType.DOCUMENT);
        return new MsgEntity<>("SUCCESS", "1", "文档上传成功，请耐心等待管理员审核");

    }

    @RequestMapping(value = "/downloadFile", method = RequestMethod.GET)
    //下载原始文件
    public void downloadFile(HttpServletResponse response, @RequestParam String token) throws Exception {
        JwtUtils jwtUtils = new JwtUtils(DOWNLOAD_TOKEN_ENCODED_SECRET_KEY);
        if (!jwtUtils.isVerify(token)) {
            throw new BusinessException("-1", "Illegal credentials!");
        }
        String realObjectName = (String) jwtUtils.decode(token).get("realObjectName");
        String title = (String) jwtUtils.decode(token).get("title");

        aliOSSUtils.downloadToUserFromOSS(response, realObjectName, title);
    }
}
