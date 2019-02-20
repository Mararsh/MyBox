package mara.mybox.tools;

import mara.mybox.fxml.image.ImageTools;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageAttributes;
import mara.mybox.data.ImageInformation;
import mara.mybox.data.WeiboSnapParameters;
import static mara.mybox.value.AppVaribles.logger;

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
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-17 9:13:00
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfTools {

    public static int PDF_dpi = 72; // pixels per inch

    public static enum PdfImageFormat {
        Original, Tiff, Jpeg
    }

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

    public static boolean writePage(PDDocument document, PDFont font,
            String sourceFormat, BufferedImage bufferedImage,
            int index, int total, PdfImageFormat targetFormat,
            int threshold, int jpegQuality, boolean isImageSize, boolean pageNumber,
            int pageWidth, int pageHeight, int marginSize, String header, boolean dithering) {
        try {
            PDImageXObject imageObject;
            switch (targetFormat) {
                case Tiff:
                    ImageBinary imageBinary = new ImageBinary(bufferedImage, threshold);
                    imageBinary.setIsDithering(dithering);
                    bufferedImage = imageBinary.operate();
                    bufferedImage = ImageBinary.byteBinary(bufferedImage);
                    imageObject = CCITTFactory.createFromImage(document, bufferedImage);
                    break;
                case Jpeg:
                    bufferedImage = ImageConvert.checkAlpha(bufferedImage, "jpg");
                    imageObject = JPEGFactory.createFromImage(document, bufferedImage, jpegQuality / 100f);
                    break;
                default:
                    if (sourceFormat != null) {
                        bufferedImage = ImageConvert.checkAlpha(bufferedImage, sourceFormat);
                    }
                    imageObject = LosslessFactory.createFromImage(document, bufferedImage);
                    break;
            }
            PDRectangle pageSize;
            if (isImageSize) {
                pageSize = new PDRectangle(imageObject.getWidth() + marginSize * 2, imageObject.getHeight() + marginSize * 2);
            } else {
                pageSize = new PDRectangle(pageWidth, pageHeight);
            }
            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
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
                if (pageNumber) {
                    content.beginText();
                    content.setFont(font, 12);
                    content.newLineAtOffset(w + marginSize - 80, 5);
                    content.showText(index + " / " + total);
                    content.endText();
                }
                if (header != null && !header.trim().isEmpty()) {
                    try {
                        content.beginText();
                        content.setFont(font, 16);
                        content.newLineAtOffset(marginSize, page.getTrimBox().getHeight() - marginSize + 2);
                        content.showText(header.trim());
                        content.endText();
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean htmlIntoPdf(List<Image> images, File targetFile, boolean isImageSize) {

        try {
            int count = 0;
            try (PDDocument document = new PDDocument(AppVaribles.PdfMemUsage)) {
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
                    bufferedImage = ImageTools.getBufferedImage(image);
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

                PDPage page = document.getPage(0);
                PDPageXYZDestination dest = new PDPageXYZDestination();
                dest.setPage(page);
                dest.setZoom(1f);
                dest.setTop((int) page.getCropBox().getHeight());
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                document.getDocumentCatalog().setOpenAction(action);

                document.save(targetFile);
                return true;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }

    }

    public static boolean imageInPdf(PDDocument document, BufferedImage bufferedImage,
            WeiboSnapParameters p, int pageNumber, int totalPage, PDFont font) {
        return writePage(document, font, "png", bufferedImage,
                pageNumber, totalPage, p.getFormat(),
                p.getThreshold(), p.getJpegQuality(), p.isIsImageSize(), p.isAddPageNumber(),
                p.getPageWidth(), p.getPageHeight(), p.getMarginSize(), p.getTitle(), p.isDithering());
    }

    public static boolean images2Pdf(List<Image> images, File targetFile,
            WeiboSnapParameters p) {
        try {
            if (images == null || images.isEmpty()) {
                return false;
            }
            int count = 0, total = images.size();
            try (PDDocument document = new PDDocument(AppVaribles.PdfMemUsage)) {
                PDDocumentInformation info = new PDDocumentInformation();
                info.setCreationDate(Calendar.getInstance());
                info.setModificationDate(Calendar.getInstance());
                info.setProducer("MyBox v" + CommonValues.AppVersion);
                info.setAuthor(p.getAuthor());
                document.setDocumentInformation(info);
                PDFont font = getFont(document, p.getFontName());

                BufferedImage bufferedImage;
                for (Image image : images) {
                    if (null == p.getFormat()) {
                        bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    } else {
                        switch (p.getFormat()) {
                            case Tiff:
                                bufferedImage = SwingFXUtils.fromFXImage(image, null);
                                break;
                            case Jpeg:
                                bufferedImage = ImageTools.checkAlpha(image, "jpg");
                                break;
                            default:
                                bufferedImage = SwingFXUtils.fromFXImage(image, null);
                                break;
                        }
                    }
                    imageInPdf(document, bufferedImage, p, ++count, total, font);
                }

                PDPage page = document.getPage(0);
                PDPageXYZDestination dest = new PDPageXYZDestination();
                dest.setPage(page);
                dest.setZoom(p.getPdfScale() / 100.0f);
                dest.setTop((int) page.getCropBox().getHeight());
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                document.getDocumentCatalog().setOpenAction(action);

                document.save(targetFile);
                return true;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }

    }

    public static boolean imagesFiles2Pdf(List<String> files, File targetFile,
            WeiboSnapParameters p, boolean deleteFiles) {
        try {
            if (files == null || files.isEmpty()) {
                return false;
            }
            int count = 0, total = files.size();
            try (PDDocument document = new PDDocument(AppVaribles.PdfMemUsage)) {
                PDDocumentInformation info = new PDDocumentInformation();
                info.setCreationDate(Calendar.getInstance());
                info.setModificationDate(Calendar.getInstance());
                info.setProducer("MyBox v" + CommonValues.AppVersion);
                info.setAuthor(p.getAuthor());
                document.setDocumentInformation(info);
                PDFont font = getFont(document, p.getFontName());

                BufferedImage bufferedImage;
                File file;
                for (String filename : files) {
                    file = new File(filename);
                    bufferedImage = ImageIO.read(file);
                    imageInPdf(document, bufferedImage, p, ++count, total, font);
                    if (deleteFiles) {
                        file.delete();
                    }
                }

                PDPage page = document.getPage(0);
                PDPageXYZDestination dest = new PDPageXYZDestination();
                dest.setPage(page);
                dest.setZoom(p.getPdfScale() / 100.0f);
                dest.setTop((int) page.getCropBox().getHeight());
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                document.getDocumentCatalog().setOpenAction(action);

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

    public static String getFontFile(String name) {
        String fontFile = null;
        try {

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
            }
        } catch (Exception e) {
        }
        return fontFile;
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
//                logger.debug(fontFile);
                font = PDType0Font.load(document, new File(fontFile));
            }
        } catch (Exception e) {
        }
//        logger.debug(font.getName());
        return font;
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

    public static boolean writeSplitImages(String sourceFormat, String sourceFile,
            ImageInformation imageInformation, List<Integer> rows, List<Integer> cols,
            ImageAttributes attributes, File targetFile,
            PdfImageFormat pdfFormat, String fontName, String author,
            int threshold, int jpegQuality, boolean isImageSize, boolean pageNumber,
            int pageWidth, int pageHeight, int marginSize, String header) {
        try {
            if (sourceFormat == null || sourceFile == null || imageInformation == null
                    || rows == null || rows.isEmpty()
                    || cols == null || cols.isEmpty() || targetFile == null) {
                return false;
            }
            try {
                if (targetFile.exists()) {
                    targetFile.delete();
                }
            } catch (Exception e) {
                return false;
            }

            try (PDDocument document = new PDDocument(AppVaribles.PdfMemUsage)) {
                PDFont font = PdfTools.getFont(document, fontName);
                PDDocumentInformation info = new PDDocumentInformation();
                info.setCreationDate(Calendar.getInstance());
                info.setModificationDate(Calendar.getInstance());
                info.setProducer("MyBox v" + CommonValues.AppVersion);
                info.setAuthor(author);
                document.setDocumentInformation(info);
                int x1, y1, x2, y2;
                BufferedImage wholeSource = null;
                if (!imageInformation.isIsSampled()) {
                    wholeSource = ImageTools.getBufferedImage(imageInformation.getImage());
                }
                int count = 0;
                int total = (rows.size() - 1) * (cols.size() - 1);
                for (int i = 0; i < rows.size() - 1; i++) {
                    y1 = rows.get(i);
                    y2 = rows.get(i + 1);
                    for (int j = 0; j < cols.size() - 1; j++) {
                        x1 = cols.get(j);
                        x2 = cols.get(j + 1);
                        BufferedImage target;
                        if (imageInformation.isIsSampled()) {
                            target = ImageFileReaders.readRectangle(sourceFormat, sourceFile, x1, y1, x2, y2);
                        } else {
                            target = ImageConvert.cropImage(wholeSource, x1, y1, x2, y2);
                        }
                        PdfTools.writePage(document, font, sourceFormat, target,
                                ++count, total, pdfFormat,
                                threshold, jpegQuality, isImageSize, pageNumber,
                                pageWidth, pageHeight, marginSize, header, attributes.isIsDithering());
                    }
                }
                document.save(targetFile);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }

    }

    public static BufferedImage page2image(File file, int page) {
        try {
            try (PDDocument doc = PDDocument.load(file, null, AppVaribles.PdfMemUsage)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                BufferedImage image = renderer.renderImage(page, 1, ImageType.ARGB);
                return image;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage page2image(File file, String password, int page,
            float scale, ImageType imageType) {
        try {
            try (PDDocument doc = PDDocument.load(file, password, AppVaribles.PdfMemUsage)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                BufferedImage image = renderer.renderImage(page, scale, imageType);
                return image;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage page2image(File file, String password, int page,
            int dpi, ImageType imageType) {
        try {
            try (PDDocument doc = PDDocument.load(file, password, AppVaribles.PdfMemUsage)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                BufferedImage image = renderer.renderImageWithDPI(page, dpi, imageType);
                return image;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

}
