package mara.mybox.tools;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.CropTools;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.controller.ControlPdfWriteOptions;
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
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
        return FloatTools.roundFloat2(pixels * 25.4f / 72f);
    }

    public static float pixels2inch(float pixels) {
        return FloatTools.roundFloat2(pixels / 72f);
    }

    public static int mm2pixels(float mm) {
        return Math.round(mm * 72f / 25.4f);
    }

    public static int inch2pixels(float inch) {
        return Math.round(inch * 72f);
    }

    public static boolean isPDF(String filename) {
        String suffix = FileNameTools.suffix(filename);
        if (suffix == null) {
            return false;
        }
        return "PDF".equals(suffix.toUpperCase());
    }

    public static boolean isPDF(File file) {
        return isPDF(file.getAbsolutePath());
    }

    public static PDDocument createPDF(File file) {
        return createPDF(file, UserConfig.getString("AuthorKey", System.getProperty("user.name")));
    }

    public static PDDocument createPDF(File file, String author) {
        PDDocument targetDoc = null;
        try {
            PDDocument document = new PDDocument(AppVariables.pdfMemUsage);
            PDDocumentInformation info = new PDDocumentInformation();
            info.setCreationDate(Calendar.getInstance());
            info.setModificationDate(Calendar.getInstance());
            info.setProducer("MyBox v" + AppValues.AppVersion);
            info.setAuthor(author);
            document.setDocumentInformation(info);
            document.setVersion(1.0f);
            document.save(file);
            targetDoc = document;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return targetDoc;
    }

    public static boolean createPdfFile(File file, String author) {
        try {
            PDDocument targetDoc = createPDF(file, author);
            if (targetDoc != null) {
                targetDoc.close();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static PDImageXObject imageObject(PDDocument document, String format, BufferedImage bufferedImage) {
        try {
            PDImageXObject imageObject;
            bufferedImage = AlphaTools.checkAlpha(bufferedImage, format);
            switch (format) {
                case "tif":
                case "tiff":
                    imageObject = CCITTFactory.createFromImage(document, bufferedImage);
                    break;
                case "jpg":
                case "jpeg":
                    imageObject = JPEGFactory.createFromImage(document, bufferedImage, 1f);
                    break;
                default:
                    imageObject = LosslessFactory.createFromImage(document, bufferedImage);
                    break;
            }
            return imageObject;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean writePage(PDDocument document,
            String sourceFormat, BufferedImage bufferedImage, int pageNumber, int total,
            ControlPdfWriteOptions options) {
        try {
            PDFont font = PdfTools.getFont(document, options.getTtfFile());
            return writePage(document, font, options.getFontSize(), sourceFormat, bufferedImage,
                    pageNumber, total, options.getImageFormat(),
                    options.getThreshold(), options.getJpegQuality(),
                    options.isIsImageSize(), options.isShowPageNumber(),
                    options.getPageWidth(), options.getPageHeight(),
                    options.getMarginSize(), options.getHeader(),
                    options.isDithering());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean writePage(PDDocument document, PDFont font, int fontSize,
            String sourceFormat, BufferedImage bufferedImage,
            int pageNumber, int total, PdfImageFormat targetFormat,
            int threshold, int jpegQuality, boolean isImageSize,
            boolean showPageNumber, int pageWidth, int pageHeight, int marginSize,
            String header, boolean dithering) {
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
                    bufferedImage = AlphaTools.checkAlpha(bufferedImage, "jpg");
                    imageObject = JPEGFactory.createFromImage(document, bufferedImage, jpegQuality / 100f);
                    break;
                default:
                    if (sourceFormat != null) {
                        bufferedImage = AlphaTools.checkAlpha(bufferedImage, sourceFormat);
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
                if (showPageNumber) {
                    content.beginText();
                    if (font != null) {
                        content.setFont(font, fontSize);
                    }
                    content.newLineAtOffset(w + marginSize - 80, 5);
                    content.showText(pageNumber + " / " + total);
                    content.endText();
                }
                if (header != null && !header.trim().isEmpty()) {
                    try {
                        content.beginText();
                        if (font != null) {
                            content.setFont(font, fontSize);
                        }
                        content.newLineAtOffset(marginSize, page.getTrimBox().getHeight() - marginSize + 2);
                        content.showText(header.trim());
                        content.endText();
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean imageInPdf(PDDocument document,
            BufferedImage bufferedImage, WeiboSnapParameters p,
            int showPageNumber, int totalPage, PDFont font) {
        return writePage(document, font, p.getFontSize(),
                "png", bufferedImage, showPageNumber, totalPage, p.getFormat(),
                p.getThreshold(), p.getJpegQuality(), p.isIsImageSize(), p.isAddPageNumber(),
                p.getPageWidth(), p.getPageHeight(), p.getMarginSize(), p.getTitle(), p.isDithering());
    }

    public static boolean images2Pdf(List<Image> images, File targetFile, WeiboSnapParameters p) {
        try {
            if (images == null || images.isEmpty()) {
                return false;
            }
            int count = 0, total = images.size();
            try (PDDocument document = new PDDocument(AppVariables.pdfMemUsage)) {
                PDDocumentInformation info = new PDDocumentInformation();
                info.setCreationDate(Calendar.getInstance());
                info.setModificationDate(Calendar.getInstance());
                info.setProducer("MyBox v" + AppValues.AppVersion);
                info.setAuthor(p.getAuthor());
                document.setDocumentInformation(info);
                document.setVersion(1.0f);
                PDFont font = getFont(document, p.getFontFile());

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
                                bufferedImage = FxImageTools.checkAlpha(image, "jpg");
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
                document.close();
                return true;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            try (PDDocument document = new PDDocument(AppVariables.pdfMemUsage)) {
                PDDocumentInformation info = new PDDocumentInformation();
                info.setCreationDate(Calendar.getInstance());
                info.setModificationDate(Calendar.getInstance());
                info.setProducer("MyBox v" + AppValues.AppVersion);
                info.setAuthor(p.getAuthor());
                document.setDocumentInformation(info);
                document.setVersion(1.0f);
                PDFont font = getFont(document, p.getFontFile());

                BufferedImage bufferedImage;
                File file;
                for (String filename : files) {
                    file = new File(filename);
                    bufferedImage = ImageFileReaders.readImage(file);
                    imageInPdf(document, bufferedImage, p, ++count, total, font);
                    if (deleteFiles) {
                        FileDeleteTools.delete(file);
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
                document.close();
                return true;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    public static PDFont getFont(PDDocument document, String fontFile) {
        PDFont font = PDType1Font.HELVETICA;
        try {
            if (fontFile != null) {
                font = PDType0Font.load(document, new File(TTFTools.ttf(fontFile)));
            }
        } catch (Exception e) {
        }
        return font;
    }

    public static PDFont defaultFont(PDDocument document) {
        PDFont font = PDType1Font.HELVETICA;
        try {
            List<String> ttfList = TTFTools.ttfList();
            if (ttfList != null && !ttfList.isEmpty()) {
                font = getFont(document, ttfList.get(0));
            }
        } catch (Exception e) {
        }
        return font;
    }

    public static List<PDImageXObject> getImageListFromPDF(PDDocument document,
            Integer startPage) throws Exception {
        List<PDImageXObject> imageList = new ArrayList<>();
        if (null != document) {
            PDPageTree pages = document.getPages();
            startPage = startPage == null ? 0 : startPage;
            int len = pages.getCount();
            if (startPage < len) {
                for (int i = startPage; i < len; ++i) {
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

    public static boolean writeSplitImages(String sourceFormat, String sourceFile,
            ImageInformation imageInformation, List<Integer> rows, List<Integer> cols,
            ImageAttributes attributes, File targetFile,
            PdfImageFormat pdfFormat, String fontFile, int fontSize,
            String author, int threshold, int jpegQuality, boolean isImageSize,
            boolean pageNumber, int pageWidth, int pageHeight, int marginSize, String header) {
        try {
            if (sourceFormat == null || sourceFile == null || imageInformation == null
                    || rows == null || rows.isEmpty()
                    || cols == null || cols.isEmpty() || targetFile == null) {
                return false;
            }
            File tmpFile = TmpFileTools.getTempFile();
            try (PDDocument document = new PDDocument(AppVariables.pdfMemUsage)) {
                int x1, y1, x2, y2;
                BufferedImage wholeSource = null;
                if (imageInformation.getImage() != null && !imageInformation.isIsScaled()) {
                    wholeSource = FxImageTools.toBufferedImage(imageInformation.getImage());
                }
                int count = 0;
                int total = (rows.size() - 1) * (cols.size() - 1);
                PDFont font = PdfTools.getFont(document, fontFile);
                ImageInformation info = new ImageInformation(new File(sourceFile));
                info.setImageFormat(sourceFormat);
                for (int i = 0; i < rows.size() - 1; ++i) {
                    y1 = rows.get(i);
                    y2 = rows.get(i + 1);
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        x1 = cols.get(j);
                        x2 = cols.get(j + 1);
                        BufferedImage target;
                        if (wholeSource == null) {
                            info.setRegion(x1, y1, x2, y2);
                            target = ImageFileReaders.readFrame(info);
                        } else {
                            target = CropTools.cropOutside(wholeSource, x1, y1, x2, y2);
                        }
                        PdfTools.writePage(document, font, fontSize, sourceFormat, target,
                                ++count, total, pdfFormat, threshold, jpegQuality, isImageSize, pageNumber,
                                pageWidth, pageHeight, marginSize, header, attributes.isIsDithering());
                    }
                }
                setValues(document, author, "MyBox v" + AppValues.AppVersion, 100, 1.0f);
                document.save(tmpFile);
                document.close();
            }
            return FileTools.rename(tmpFile, targetFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

    }

    // page is 0-based
    public static BufferedImage page2image(File file, String password, int page,
            float scale, ImageType imageType) {
        try {
            try (PDDocument doc = PDDocument.load(file, password, AppVariables.pdfMemUsage)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                BufferedImage image = renderer.renderImage(page, scale, imageType);
                doc.close();
                return image;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // page is 0-based
    public static BufferedImage page2image(File file, String password, int page, int dpi, ImageType imageType) {
        try {
            try (PDDocument doc = PDDocument.load(file, password, AppVariables.pdfMemUsage)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                BufferedImage image = renderer.renderImageWithDPI(page, dpi, imageType);
                doc.close();
                return image;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static void setValues(PDDocument doc, String author, String producer, int defaultZoom, float version) {
        try {
            if (doc == null) {
                return;
            }
            PDDocumentInformation info = new PDDocumentInformation();
            info.setCreationDate(Calendar.getInstance());
            info.setModificationDate(Calendar.getInstance());
            info.setProducer(producer);
            info.setAuthor(author);
            doc.setDocumentInformation(info);
            doc.setVersion(version);

            if (doc.getNumberOfPages() > 0) {
                PDPage page = doc.getPage(0);
                PDPageXYZDestination dest = new PDPageXYZDestination();
                dest.setPage(page);
                dest.setZoom(defaultZoom / 100.0f);
                dest.setTop((int) page.getCropBox().getHeight());
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                doc.getDocumentCatalog().setOpenAction(action);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static String html2pdf(File target, String html, String css, boolean ignoreHead, DataHolder pdfOptions) {
        try {
            if (html == null || html.isBlank()) {
                return null;
            }
            if (ignoreHead) {
                html = HtmlWriteTools.ignoreHead(html);
            }
            if (!css.isBlank()) {
                try {
                    html = PdfConverterExtension.embedCss(html, css);
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            }
            try {
                PdfConverterExtension.exportToPdf(target.getAbsolutePath(), html, "", pdfOptions);
                if (!target.exists()) {
                    return message("Failed");
                } else if (target.length() == 0) {
                    FileDeleteTools.delete(target);
                    return message("Failed");
                }
                return message("Successful");
            } catch (Exception e) {
                return e.toString();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return e.toString();
        }
    }

    public static File html2pdf(String html) {
        try {
            if (html == null || html.isBlank()) {
                return null;
            }
            File pdfFile = TmpFileTools.pdfFile();
            File wqy_microhei = FxFileTools.getInternalFile("/data/wqy-microhei.ttf", "data", "wqy-microhei.ttf");
            String css = "@font-face {\n"
                    + "  font-family: 'myFont';\n"
                    + "  src: url('file:///" + wqy_microhei.getAbsolutePath().replaceAll("\\\\", "/") + "');\n"
                    + "  font-weight: normal;\n"
                    + "  font-style: normal;\n"
                    + "}\n"
                    + " body { font-family:  'myFont';}";
            DataHolder pdfOptions = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL
                    & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP), TocExtension.create())
                    .toMutable()
                    .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
                    .toImmutable();
            html2pdf(pdfFile, html, css, true, pdfOptions);
            return pdfFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File text2pdf(String text) {
        try {
            if (text == null || text.isBlank()) {
                return null;
            }
            return html2pdf(HtmlWriteTools.textToHtml(text));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File md2pdf(String md) {
        try {
            if (md == null || md.isBlank()) {
                return null;
            }
            return html2pdf(HtmlWriteTools.md2html(md));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
