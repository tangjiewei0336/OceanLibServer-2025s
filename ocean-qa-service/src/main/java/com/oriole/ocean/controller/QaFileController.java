package com.oriole.ocean.controller;

import com.aliyun.oss.model.CannedAccessControlList;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.po.mysql.FileUploadTempEntity;
import com.oriole.ocean.qaObjectStorage.AliOSSUtils;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.ImageFileEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.config.QaOSSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/qaService/qaFile")
public class QaFileController {

    @Autowired
    QaOSSConfig ossConfig;

    @Autowired
    AliOSSUtils aliOSSUtils;

    private final static String[] allowPicFileType = new String[]{".PNG", ".JPG", ".JPEG", ".GIF"};

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public MsgEntity<ImageFileEntity> uploadFile(@AuthUser String username,
                                                 @RequestParam(value = "uploadFile") MultipartFile uploadFile) throws Exception {
        String fileName = UUID.randomUUID().toString();
        if (uploadFile.isEmpty()) {
            throw new BusinessException("-2", "文件未正常上传，没有获取到MultipartFile");
        } else {
            if (!Objects.requireNonNull(uploadFile.getOriginalFilename()).contains(".")) {
                throw new BusinessException("-4", "文件名解析异常");
            }
            String suffix = uploadFile.getOriginalFilename().substring(uploadFile.getOriginalFilename().lastIndexOf("."));
            if (suffix.isEmpty() || !Arrays.asList(allowPicFileType).contains(suffix.toUpperCase())) {
                throw new BusinessException("-3", "非法的文件后缀格式");
            }

            //直接上传至OSS
            aliOSSUtils.inputToOSS(uploadFile.getInputStream(), ossConfig.TEMP_DOCUMENT_PATH + fileName + suffix, CannedAccessControlList.Private);

            ImageFileEntity imageFileEntity = new ImageFileEntity();
            imageFileEntity.setFileName(fileName);
            imageFileEntity.setFileSuffix(suffix);

            imageFileEntity.setSize((int) uploadFile.getSize());
            imageFileEntity.setUploadUsername(username);

            return new MsgEntity<>("SUCCESS", "1", imageFileEntity);
        }
    }
}
