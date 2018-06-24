package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @Author Mara
 * @CreateDate 2018-6-17 9:13:00
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfTools {

    private static final Logger logger = LogManager.getLogger();

    public static String getTextFromPdf(InputStream fileStream, boolean sort) {
        // 开始提取页数
        int startPage = 1;
        // 结束提取页数
        String content = null;
        PDDocument document = null;
        try {
            // 加载 pdf 文档
            document = PDDocument.load(fileStream);
            int endPage = null == document ? Integer.MAX_VALUE : document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(sort);
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            content = stripper.getText(document);
//            log.info("pdf 文件解析，内容为：" + content);
        } catch (Exception e) {
//            log.error("文件解析异常，信息为： " + e.getMessage());
        }
        return content;

    }

    public static List<PDImageXObject> getImageListFromPDF(PDDocument document, Integer startPage) throws Exception {
        List<PDImageXObject> imageList = new ArrayList<PDImageXObject>();
        if (null != document) {
            PDPageTree pages = document.getPages();
            startPage = startPage == null ? 0 : startPage;
            int len = pages.getCount();
            if (startPage < len) {
                for (int i = startPage; i < len; i++) {
                    PDPage page = pages.get(i);
                    Iterable<COSName> objectNames = page.getResources().getXObjectNames();
                    for (COSName imageObjectName : objectNames) {
                        if (page.getResources().isImageXObject(imageObjectName)) {
                            imageList.add((PDImageXObject) page.getResources().getXObject(imageObjectName));
                        }
                    }
                }
            }
        }
        return imageList;
    }

    public static InputStream getImageInputStream(PDImageXObject iamge) throws Exception {

        if (null != iamge && null != iamge.getImage()) {
            BufferedImage bufferImage = iamge.getImage();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferImage, iamge.getSuffix(), os);
            return new ByteArrayInputStream(os.toByteArray());
        }
        return null;

    }

    public static void writeInputStream(InputStream imageb, PDImageXObject image) throws Exception {

//        File imgFile = new File("e:\\" + name + "." + image.getSuffix());
//        FileOutputStream fout = new FileOutputStream(imgFile);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageIO.write(imageb, image.getSuffix(), os);
//        InputStream is = new ByteArrayInputStream(os.toByteArray());
//        int byteCount = 0;
//        byte[] bytes = new byte[1024];
//
//        while ((byteCount = is.read(bytes)) > 0) {
//            fout.write(bytes, 0, byteCount);
//        }
//
//        fout.close();
//
//        is.close();
    }
}
