package com.oriole.ocean.qaObjectStorage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.oriole.ocean.config.QaOSSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class AliOSSUtils implements ObjectStorageUtils{

    @Autowired
    QaOSSConfig ossConfig;

    public void downloadToTempFromOSS(String objectPath, String filePath, String fileName) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(ossConfig.ENDPOINT, ossConfig.ACCESS_KEY_ID, ossConfig.ACCESS_KEY_SECRET);
        // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建
        ossClient.getObject(new GetObjectRequest(ossConfig.BUCKET, objectPath + fileName), new File(filePath + fileName));

        // 关闭OSSClient
        ossClient.shutdown();
    }

    public void downloadToUserFromOSS(HttpServletResponse response, String objectName, String showName) throws Exception {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(ossConfig.ENDPOINT, ossConfig.ACCESS_KEY_ID, ossConfig.ACCESS_KEY_SECRET);
        OSSObject ossObject = ossClient.getObject(new GetObjectRequest(ossConfig.BUCKET, objectName));

        BufferedInputStream reader = new BufferedInputStream(ossObject.getObjectContent());

        response.setContentType(ossObject.getObjectMetadata().getContentType());
        String suffix = objectName.substring(objectName.lastIndexOf("."));
        response.setHeader("Content-disposition", "attachment; filename=" + java.net.URLEncoder.encode(showName, "UTF-8") + suffix);
        OutputStream out = response.getOutputStream();
        // 将要下载的文件内容通过输出流写到浏览器
        int len;
        byte[] buffer = new byte[1024];
        while ((len = reader.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }

        // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
        reader.close();
        ossClient.shutdown();
    }

    public void inputToOSS(InputStream inputStream, String objectName, Object acl) throws Exception {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(ossConfig.ENDPOINT, ossConfig.ACCESS_KEY_ID, ossConfig.ACCESS_KEY_SECRET);
        // 上传网络流
        ossClient.putObject(ossConfig.BUCKET, objectName, inputStream);
        // 设文件ACL权限
        ossClient.setObjectAcl(ossConfig.BUCKET, objectName, (CannedAccessControlList)acl);
        // 关闭OSSClient。
        ossClient.shutdown();
    }

    public void moveFileInOSS(String sourceObjectName, String destinationObjectName, Object acl) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(ossConfig.ENDPOINT, ossConfig.ACCESS_KEY_ID, ossConfig.ACCESS_KEY_SECRET);
        // 拷贝
        ossClient.copyObject(ossConfig.BUCKET, sourceObjectName, ossConfig.BUCKET, destinationObjectName);
        // 设文件ACL权限
        ossClient.setObjectAcl(ossConfig.BUCKET, destinationObjectName, (CannedAccessControlList)acl);
        // 关闭OSSClient。
        ossClient.shutdown();
    }
}
