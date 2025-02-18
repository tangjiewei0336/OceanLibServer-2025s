package com.oriole.ocean.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfUtils {

    private File file;

    public PdfUtils(File file) {
        this.file = file;
    }

    public String getTextString() throws Exception {
        PDDocument doc = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        String result = stripper.getText(doc);
        if (result != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(result);
            result = m.replaceAll("");
        }

        return result;
    }

    public void getPreviewPic(String path, String fileName, String previewPicSuffix) throws Exception {
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        //截取第一张作为封面预览图
        int pageCount = doc.getNumberOfPages();
        if (pageCount > 0) {
            BufferedImage image = renderer.renderImage(0, 1.9f);
            ImageIO.write(image, "JPEG", new File(path + fileName + previewPicSuffix));
        }
    }

    public void doWaterMark(String waterMarkString) throws Exception {
        // 待加水印的文件
        PdfReader reader = new PdfReader(new FileInputStream(file));
        // 加完水印的文件
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(file));

        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;

        // 设置透明度
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.1f);
        // 设置字体
        ClassPathResource classPathResource = new ClassPathResource("fonts/font.ttf");
        BaseFont base = BaseFont.createFont(classPathResource.getPath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        // 循环对每页插入水印
        for (int i = 1; i < total; i++) {
            // 水印的起始
            content = stamper.getOverContent(i);
            content.setGState(gs);
            content.setFontAndSize(base, 32);
            // 开始
            content.beginText();
            // 设置颜色 默认为黑色
            content.setColorFill(BaseColor.BLACK);
            // 开始写入水印
            if(waterMarkString.isEmpty()){
                waterMarkString = "盗版必究 Only For Preview";
            }
            for (int j = 0; j <= 20; j++)
                content.showTextAligned(Element.ALIGN_MIDDLE, waterMarkString, -400 + 80 * j,
                        0, 45);
            content.endText();
        }
        stamper.close();
    }

    public static final int width_A4 = 595;
    public static final int height_A4 = 842;

    public void pic2Pdf(List<File> files) throws Exception {
        FileOutputStream out = null;

        Rectangle rect = new Rectangle(width_A4, height_A4);
        Document document = new Document(rect);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
        document.open();

        for (int index = 0; index < files.size(); index++) {
            document.newPage();
            Image image = Image.getInstance(files.get(index).getPath());
            if (image.getWidth() > width_A4 || image.getHeight() > height_A4) {
                float imgAspectRatio = image.getWidth() / image.getHeight();
                if (width_A4 / height_A4 < imgAspectRatio) {
                    image.scaleAbsolute(width_A4, width_A4 / imgAspectRatio);
                } else {
                    image.scaleAbsolute(height_A4 * imgAspectRatio, height_A4);
                }

            }
            image.setAbsolutePosition(0, 0);
            document.add(image);
        }
        document.close();
    }
}
