package com.grand.mysql_handler.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
 
import javax.imageio.ImageIO;
 
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


public class PDF2IMGTestByPdfbox {
    public static void main(String[] args) {
        File file = new File("C:/Users/Administrator/Desktop/Mini/ID1/卷一.pdf");
        try {
            PDDocument doc = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                // 方式1,第二个参数是设置缩放比(即像素)，DPI是每英寸的像素点，长度单位，一般WEB为72即可，照片打印用300，在这里使用72放大后比较模糊，选用144比较好
                BufferedImage image = renderer.renderImageWithDPI(i, 144);// 方式2,第二个参数是设置缩放比(即像素)
                // BufferedImage image = renderer.renderImage(i, 2.5f);
                String filename = "pdfbox_image_"+i+".png";
                ImageIO.write(image, "PNG", new File("C:/Users/Administrator/Desktop/Mini/ID1/img3/" + filename));
                System.out.println(filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
