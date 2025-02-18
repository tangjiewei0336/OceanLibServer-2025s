package com.oriole.ocean.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OSSConfig {
    @Value("${oss.access.key.id}")
    public String ACCESS_KEY_ID;
    @Value("${oss.access.key.secret}")
    public String ACCESS_KEY_SECRET;
    @Value("${oss.endpoint}")
    public String ENDPOINT;
    @Value("${oss.object.path.temp}")
    public String TEMP_DOCUMENT_PATH;
    @Value("${oss.object.path.save}")
    public String ORIGINAL_DOCUMENT_PATH;
    @Value("${oss.object.path.preview}")
    public String PREVIEW_DOCUMENT_PATH;
    @Value("${oss.object.bucket}")
    public String BUCKET;
}
