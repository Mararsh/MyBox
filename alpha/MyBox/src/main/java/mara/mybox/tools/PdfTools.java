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
import mara.mybox.image.tools.AlphaTools;
import mara.mybox.image.data.ImageBinary;
import mara.mybox.controller.ControlPdfWriteOptions;
import mara.mybox.data.WeiboSnapParameters;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.FxImageTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppValues;
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
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

/**
 * @Author Mara
 * @CreateDate 2018-6-17 9:13:00
 * @License Apache License Version 2.0
 */
public class PdfTools {

    public static int PDF_dpi = 72; // pixels per inch
    public static int DefaultMargin = 20;

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
        String suffix = FileNameTools.ext(filename);
        if (suffix == null) {
            return false;
        }
        return "PDF".equals(suffix.toUpperCase());
    }

    public static PDDocument createPDF(File file) {
        return createPDF(file, UserConfig.getString("AuthorKey", System.getProperty("user.name")));
    }

    public static PDDocument createPDF(File file, String author) {
        PDDocument targetDoc = null;
        try {
            PDDocument document = new PDDocument();
            setAttributes(document, author, -1);
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

    public static PDImageXObject imageObject(FxTask task, PDDocument document, String format, BufferedImage bufferedImage) {
        try {
            PDImageXObject imageObject;
            bufferedImage = AlphaTools.checkAlpha(task, bufferedImage, format);
            if (bufferedImage == null || (task != null && !task.isWorking())) {
                return null;
            }
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

    public static boolean writePage(FxTask task, PDDocument document,
            String sourceFormat, BufferedImage bufferedImage, int pageNumber, int total,
            ControlPdfWriteOptions options) {
        try {
            PDFont font = PdfTools.getFont(document, options.getTtfFile());
            return writePage(task, document, font, options.getFontSize(),
                    sourceFormat, bufferedImage,
                    pageNumber, total, options.getImageFormat(),
                    options.getThreshold(), options.getJpegQuality(),
                    options.isIsImageSize(), options.isShowPageNumber(),
                    options.getPageWidth(), options.getPageHeight(),
                    options.getMarginSize(), options.getHeader(), options.getFooter(),
                    options.isDithering());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean writePage(FxTask task, PDDocument document, PDFont font, int fontSize,
            String sourceFormat, BufferedImage bufferedImage,
            int pageNumber, int total, PdfImageFormat targetFormat,
            int threshold, int jpegQuality, boolean isImageSize,
            boolean showPageNumber, int pageWidth, int pageHeight, int marginSize,
            String header, String footer, boolean dithering) {
        try {
            PDImageXObject imageObject;
            switch (targetFormat) {
                case Tiff:
                    ImageBinary imageBinary = new ImageBinary();
                    imageBinary.setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                            .setImage(bufferedImage)
                            .setIntPara1(threshold)
                            .setIsDithering(dithering)
                            .setTask(task);
                    bufferedImage = imageBinary.start();
                    if (bufferedImage == null || (task != null && !task.isWorking())) {
                        return false;
                    }
                    bufferedImage = ImageBinary.byteBinary(task, bufferedImage);
                    if (bufferedImage == null || (task != null && !task.isWorking())) {
                        return false;
                    }
                    imageObject = CCITTFactory.createFromImage(document, bufferedImage);
                    break;
                case Jpeg:
                    bufferedImage = AlphaTools.checkAlpha(task, bufferedImage, "jpg");
                    if (bufferedImage == null || (task != null && !task.isWorking())) {
                        return false;
                    }
                    imageObject = JPEGFactory.createFromImage(document, bufferedImage, jpegQuality / 100f);
                    break;
                default:
                    if (sourceFormat != null) {
                        bufferedImage = AlphaTools.checkAlpha(task, bufferedImage, sourceFormat);
                    }
                    if (bufferedImage == null || (task != null && !task.isWorking())) {
                        return false;
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
                if (footer != null && !footer.trim().isEmpty()) {
                    try {
                        content.beginText();
                        if (font != null) {
                            content.setFont(font, fontSize);
                        }
                        content.newLineAtOffset(marginSize, marginSize + 2);
                        content.showText(footer.trim());
                        content.endText();
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
                PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                gs.setNonStrokingAlphaConstant(0.2f);
                gs.setAlphaSourceFlag(true);
                gs.setBlendMode(BlendMode.NORMAL);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static List<BlendMode> pdfBlendModes() {
        try {

            List<BlendMode> modes = new ArrayList<>();
            modes.add(BlendMode.NORMAL);
//            modes.add(BlendMode.COMPATIBLE);
            modes.add(BlendMode.MULTIPLY);
            modes.add(BlendMode.SCREEN);
            modes.add(BlendMode.OVERLAY);
            modes.add(BlendMode.DARKEN);
            modes.add(BlendMode.LIGHTEN);
            modes.add(BlendMode.COLOR_DODGE);
            modes.add(BlendMode.COLOR_BURN);
            modes.add(BlendMode.HARD_LIGHT);
            modes.add(BlendMode.SOFT_LIGHT);
            modes.add(BlendMode.DIFFERENCE);
            modes.add(BlendMode.EXCLUSION);
            modes.add(BlendMode.HUE);
            modes.add(BlendMode.SATURATION);
            modes.add(BlendMode.LUMINOSITY);
            modes.add(BlendMode.COLOR);
            return modes;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
//        BlendMode m = BlendMode.getInstance(COSName.A);
//        Field xbar = m.getClass().getDeclaredField("BLEND_MODE_NAMES");
//        xbar.setAccessible(true);
//        Map<BlendMode, COSName> maps = (Map<BlendMode, COSName>) xbar.get(reader);
//        return maps.keySet();
    }

    public static boolean imageInPdf(FxTask task, PDDocument document,
            BufferedImage bufferedImage, WeiboSnapParameters p,
            int showPageNumber, int totalPage, PDFont font) {
        return writePage(task, document, font, p.getFontSize(),
                "png", bufferedImage, showPageNumber, totalPage, p.getFormat(),
                p.getThreshold(), p.getJpegQuality(), p.isIsImageSize(), p.isAddPageNumber(),
                p.getPageWidth(), p.getPageHeight(), p.getMarginSize(),
                p.getTitle(), null, p.isDithering());
    }

    public static boolean images2Pdf(FxTask task, List<Image> images, File targetFile, WeiboSnapParameters p) {
        try {
            if (images == null || images.isEmpty()) {
                return false;
            }
            int count = 0, total = images.size();
            try (PDDocument document = new PDDocument()) {

                PDFont font = getFont(document, p.getFontFile());

                BufferedImage bufferedImage;
                for (Image image : images) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    if (null == p.getFormat()) {
                        bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    } else {
                        switch (p.getFormat()) {
                            case Tiff:
                                bufferedImage = SwingFXUtils.fromFXImage(image, null);
                                break;
                            case Jpeg:
                                bufferedImage = FxImageTools.checkAlpha(task, image, "jpg");
                                break;
                            default:
                                bufferedImage = SwingFXUtils.fromFXImage(image, null);
                                break;
                        }
                    }
                    imageInPdf(task, document, bufferedImage, p, ++count, total, font);
                }

                setAttributes(document, p.getAuthor(), p.getPdfScale());

                document.save(targetFile);
                document.close();
                return true;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

    }

    public static boolean imagesFiles2Pdf(FxTask task, List<String> files, File targetFile,
            WeiboSnapParameters p, boolean deleteFiles) {
        try {
            if (files == null || files.isEmpty()) {
                return false;
            }
            int count = 0, total = files.size();
            try (PDDocument document = new PDDocument()) {

                PDFont font = getFont(document, p.getFontFile());

                BufferedImage bufferedImage;
                File file;
                for (String filename : files) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    file = new File(filename);
                    bufferedImage = ImageFileReaders.readImage(task, file);
                    if (bufferedImage == null || (task != null && !task.isWorking())) {
                        return false;
                    }
                    imageInPdf(task, document, bufferedImage, p, ++count, total, font);
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    if (deleteFiles) {
                        FileDeleteTools.delete(file);
                    }
                }

                setAttributes(document, p.getAuthor(), p.getPdfScale());

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

    public static PDFont HELVETICA() {
        return new PDType1Font(Standard14Fonts.getMappedFontName(Standard14Fonts.FontName.HELVETICA.getName()));
    }

    public static PDFont getFont(PDDocument document, String fontFile) {
        PDFont font = HELVETICA();
        try {
            if (fontFile != null) {
                font = PDType0Font.load(document, new File(TTFTools.ttf(fontFile)));
            }
        } catch (Exception e) {
        }
        return font;
    }

    public static PDFont defaultFont(PDDocument document) {
        PDFont font = HELVETICA();
        try {
            List<String> ttfList = TTFTools.ttfList();
            if (ttfList != null && !ttfList.isEmpty()) {
                font = getFont(document, ttfList.get(0));
            }
        } catch (Exception e) {
        }
        return font;
    }

    public static float fontWidth(PDFont font, String text, int fontSize) {
        try {
            if (font == null || text == null || fontSize <= 0) {
                return -1;
            }
            return fontSize * font.getStringWidth(text) / 1000;
        } catch (Exception e) {
            return -2;
        }
    }

    public static float fontHeight(PDFont font, int fontSize) {
        try {
            if (font == null || fontSize <= 0) {
                return -1;
            }
            return fontSize * font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000;
        } catch (Exception e) {
            return -2;
        }
    }

    public static List<PDImageXObject> getImageListFromPDF(FxTask task, PDDocument document,
            Integer startPage) throws Exception {
        List<PDImageXObject> imageList = new ArrayList<>();
        if (null != document) {
            PDPageTree pages = document.getPages();
            startPage = startPage == null ? 0 : startPage;
            int len = pages.getCount();
            if (startPage < len) {
                for (int i = startPage; i < len; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    PDPage page = pages.get(i);
                    Iterable<COSName> objectNames = page.getResources().getXObjectNames();
                    for (COSName imageObjectName : objectNames) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        if (page.getResources().isImageXObject(imageObjectName)) {
                            imageList.add((PDImageXObject) page.getResources().getXObject(imageObjectName));
                        }
                    }
                }
            }
        }
        return imageList;
    }

    public static void setAttributes(PDDocument doc, String author, int defaultZoom) {
        setAttributes(doc, author, "MyBox v" + AppValues.AppVersion, defaultZoom, 1.0f);
    }

    public static void setAttributes(PDDocument doc, String author, String producer,
            int defaultZoom, float version) {
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

            if (defaultZoom > 0 && doc.getNumberOfPages() > 0) {
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

    public static String html2pdf(FxTask task, File target, String html,
            String css, boolean ignoreHead, DataHolder pdfOptions) {
        try {
            if (html == null || html.isBlank()) {
                return null;
            }
            if (ignoreHead) {
                html = HtmlWriteTools.ignoreHead(task, html);
                if (html == null || (task != null && !task.isWorking())) {
                    return message("Canceled");
                }
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

    public static File html2pdf(FxTask task, String html) {
        try {
            if (html == null || html.isBlank()) {
                return null;
            }
            File pdfFile = FileTmpTools.generateFile("pdf");
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
            html2pdf(task, pdfFile, html, css, true, pdfOptions);
            return pdfFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File text2pdf(FxTask task, String text) {
        try {
            if (text == null || text.isBlank()) {
                return null;
            }
            return html2pdf(task, HtmlWriteTools.textToHtml(text));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File md2pdf(FxTask task, String md) {
        try {
            if (md == null || md.isBlank()) {
                return null;
            }
            return html2pdf(task, HtmlWriteTools.md2html(md));
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
