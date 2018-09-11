package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import mara.mybox.objects.CommonValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
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

    public int PDF_dpi = 72; // pixels per inch

    private static final Logger logger = LogManager.getLogger();

    public static float pixels2mm(float pixels) {
        return ValueTools.roundFloat2(pixels * 25.4f / 72f);
    }

    public static float pixels2inch(float pixels) {
        return ValueTools.roundFloat2(pixels / 72f);
    }

    public static int mm2pixels(float mm) {
        return Math.round(mm * 72f / 25.4f);
    }

    public static int inch2pixels(float inch) {
        return Math.round(inch * 72f);
    }

    public static boolean htmlIntoPdf(List<Image> images, File targetFile, boolean isImageSize) {

        try {
            int count = 0;
            try (PDDocument document = new PDDocument()) {
                PDPageContentStream content;
                PDFont font = PDType1Font.HELVETICA;
                PDDocumentInformation info = new PDDocumentInformation();
                info.setCreationDate(Calendar.getInstance());
                info.setModificationDate(Calendar.getInstance());
                info.setProducer("MyBox v" + CommonValues.AppVersion);
                document.setDocumentInformation(info);
                BufferedImage bufferedImage;
                PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
                int marginSize = 20, total = images.size();
                for (Image image : images) {
                    PDImageXObject imageObject;
                    bufferedImage = FxmlImageTools.getWritableData(image, "png");
                    imageObject = LosslessFactory.createFromImage(document, bufferedImage);
                    if (isImageSize) {
                        pageSize = new PDRectangle(imageObject.getWidth() + marginSize * 2, imageObject.getHeight() + marginSize * 2);
                    }
                    PDPage page = new PDPage(pageSize);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    float w, h;
                    if (isImageSize) {
                        w = imageObject.getWidth();
                        h = imageObject.getHeight();
                    } else {
                        if (imageObject.getWidth() > imageObject.getHeight()) {
                            w = page.getTrimBox().getWidth() - marginSize * 2;
                            h = imageObject.getHeight() * w / imageObject.getWidth();
                        } else {
                            h = page.getTrimBox().getHeight() - marginSize * 2;
                            w = imageObject.getWidth() * h / imageObject.getHeight();
                        }
                    }
                    content.drawImage(imageObject, marginSize, page.getTrimBox().getHeight() - marginSize - h, w, h);

                    content.beginText();
                    content.setFont(font, 12);
                    content.newLineAtOffset(w + marginSize - 80, 5);
                    content.showText((++count) + " / " + total);
                    content.endText();

                    content.close();
                }

                document.save(targetFile);
                return true;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }

    }

    public static void setPageSize(PDPage page, PDRectangle pageSize) {

        page.setTrimBox(pageSize);
        page.setCropBox(pageSize);
        page.setArtBox(pageSize);
        page.setBleedBox(new PDRectangle(pageSize.getWidth() + mm2pixels(5), pageSize.getHeight() + mm2pixels(5)));
        page.setMediaBox(page.getBleedBox());

//        pageSize.setLowerLeftX(20);
//        pageSize.setLowerLeftY(20);
//        pageSize.setUpperRightX(page.getTrimBox().getWidth() - 20);
//        pageSize.setUpperRightY(page.getTrimBox().getHeight() - 20);
    }

    public static PDFont getFont(PDDocument document, String name) {
        PDFont font = PDType1Font.HELVETICA;
        try {
            String fontFile = null;
            switch (name) {
                case "宋体":
                    fontFile = FileTools.getFontFile("simsun");
                    break;
                case "幼圆":
                    fontFile = FileTools.getFontFile("SIMYOU");
                    break;
                case "仿宋":
                    fontFile = FileTools.getFontFile("simfang");
                    break;
                case "隶书":
                    fontFile = FileTools.getFontFile("SIMLI");
                    break;
                case "Helvetica":
                    return PDType1Font.HELVETICA;
                case "Courier":
                    return PDType1Font.COURIER;
                case "Times New Roman":
                    return PDType1Font.TIMES_ROMAN;
            }
            if (fontFile != null) {
                logger.debug(fontFile);
                font = PDType0Font.load(document, new File(fontFile));
            }
        } catch (Exception e) {
        }
        logger.debug(font.getName());
        return font;
    }

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
        List<PDImageXObject> imageList = new ArrayList<>();
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
