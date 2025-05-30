package com.oriole.ocean.qaObjectStorage;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public interface ObjectStorageUtils {
    public void downloadToTempFromOSS(String objectPath, String filePath, String fileName)throws Exception;
    void downloadToUserFromOSS(HttpServletResponse response, String objectName, String showName)throws Exception;
    void inputToOSS(InputStream inputStream, String objectName, Object acl)throws Exception;
    void moveFileInOSS(String sourceObjectName, String destinationObjectName, Object acl)throws Exception;
}
