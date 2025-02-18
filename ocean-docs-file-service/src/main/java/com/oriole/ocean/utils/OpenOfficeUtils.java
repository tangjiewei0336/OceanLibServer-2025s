package com.oriole.ocean.utils;

import org.jodconverter.OfficeDocumentConverter;
import org.jodconverter.office.DefaultOfficeManagerBuilder;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class OpenOfficeUtils {

    private Boolean useOpenOffice = false;

    @Value("${openoffice.path}")
    private String OPENOFFICE_PATH;
    @Value("${libreoffice.path}")
    private String LIBREOFFICE_PATH;

    public OpenOfficeUtils() {
    }

    public OpenOfficeUtils(Boolean useOpenOffice) {
        if(useOpenOffice!=null) {
            this.useOpenOffice = useOpenOffice;
        }
    }

    /**
     * @return 返回一个OfficeManager实例，用于处理转换业务
     * @throws OfficeException
     */
    private OfficeManager getOfficeManager() throws OfficeException {
        DefaultOfficeManagerBuilder builder = new DefaultOfficeManagerBuilder();
        //此处填写OpenOffice安装路径
        if (useOpenOffice) {
            builder.setOfficeHome(OPENOFFICE_PATH);
        } else {
            builder.setOfficeHome(LIBREOFFICE_PATH);
        }
        OfficeManager officeManager = builder.build();
        //officeManager提供了开启OpenOffice的API服务
        officeManager.start();
        return officeManager;
    }

    public Boolean openOfficeExperience(String path, String fileName, String suffix, String previewFileSuffix) {
        File inputTempFile = new File(path + fileName + suffix);
        File outputTempFile = new File(path + fileName + previewFileSuffix);
        OfficeManager manage = null;

        try {
            //开启OpenOffice服务
            manage = getOfficeManager();
            //设置转换后的文件存储路径，文件名

            //使用OfficeDocumentConverter类转换文件，其实核心就这一句
            OfficeDocumentConverter converter = new OfficeDocumentConverter(manage);
            converter.convert(inputTempFile, outputTempFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            //关闭资源占用
            if (null != manage) {
                try {
                    manage.stop();
                } catch (OfficeException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}