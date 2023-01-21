package mara.mybox.fxml;

import java.io.File;
import java.util.Arrays;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BytesEditorController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.FileDecompressUnarchiveController;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.controller.HtmlTableController;
import mara.mybox.controller.ImageInformationController;
import mara.mybox.controller.ImageManufactureController;
import mara.mybox.controller.ImageMetaDataController;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.controller.MarkdownEditorController;
import mara.mybox.controller.MediaPlayerController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.PptViewController;
import mara.mybox.controller.TextEditorController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.controller.WordViewController;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.WindowTools.openScene;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.FileExtensions;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class ControllerTools {

    public static BaseController openMyBox(Stage stage) {
        return openScene(stage, Fxmls.MyboxFxml);
    }

    public static BaseController openTarget(Stage stage, String filename) {
        return openTarget(stage, filename, true);
    }

    public static BaseController openTarget(Stage stage, String filename, boolean mustOpen) {
        BaseController controller = null;
        if (filename == null) {
            return controller;
        }
        if (filename.startsWith("http") || filename.startsWith("ftp")) {
            return openHtmlEditor(null, filename);
        }
        File file = new File(filename);
        if (!file.exists()) {
            return controller;
        }
        if (file.isDirectory()) {
            PopTools.browseURI(controller, file.toURI());
            return controller;
        }
        String suffix = FileNameTools.suffix(file.getName()).toLowerCase();
        if (FileExtensions.SupportedImages.contains(suffix)) {
            controller = openImageViewer(stage, file);
        } else if ("html".equals(suffix) || "htm".equals(suffix)) {
            controller = openHtmlEditor(stage, file);
        } else if ("md".equals(suffix)) {
            controller = openMarkdownEditer(stage, file);
        } else if ("pdf".equals(suffix)) {
            controller = openPdfViewer(stage, file);
        } else if ("csv".equals(suffix)) {
            controller = openCsvEditor(stage, file);
        } else if ("xlsx".equals(suffix) || "xls".equals(suffix)) {
            controller = openExcelEditor(stage, file);
        } else if ("ppt".equals(suffix) || "pptx".equals(suffix)) {
            controller = viewPPT(stage, file);
        } else if ("doc".equals(suffix) || "docx".equals(suffix)) {
            controller = viewWord(stage, file);
        } else if (Arrays.asList(FileExtensions.TextFileSuffix).contains(suffix)) {
            controller = openTextEditer(stage, file);
        } else if (CompressTools.compressFormats().contains(suffix) || CompressTools.archiveFormats().contains(suffix)) {
            controller = openDecompressUnarchive(stage, file);
        } else if (Arrays.asList(FileExtensions.MediaPlayerSupports).contains(suffix)) {
            controller = openMediaPlayer(stage, file);
        } else if (mustOpen) {
            controller = openBytesEditer(stage, file);
        }
        return controller;
    }

    public static ImageManufactureController openImageManufacture(Stage stage, File file) {
        try {
            final ImageManufactureController controller = (ImageManufactureController) openScene(stage, Fxmls.ImageManufactureFxml);
            controller.loadImageFile(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage, File file, ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller = (ImageManufactureController) openScene(stage, Fxmls.ImageManufactureFxml);
            controller.loadImage(file, imageInfo);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage, ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller = (ImageManufactureController) openScene(stage, Fxmls.ImageManufactureFxml);
            controller.loadImageInfo(imageInfo);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage, Image image) {
        try {
            final ImageManufactureController controller = (ImageManufactureController) openScene(stage, Fxmls.ImageManufactureFxml);
            controller.loadImage(image);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(File file) {
        return openImageManufacture(null, file);
    }

    public static ImageViewerController openImageViewer(Stage stage) {
        try {
            final ImageViewerController controller = (ImageViewerController) openScene(stage, Fxmls.ImageViewerFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Stage stage, File file) {
        try {
            final ImageViewerController controller = openImageViewer(stage);
            if (controller != null && file != null) {
                controller.loadImageFile(file);
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(File file) {
        return openImageViewer(null, file);
    }

    public static ImageViewerController openImageViewer(String file) {
        return ControllerTools.openImageViewer(null, new File(file));
    }

    public static ImageViewerController openImageViewer(Image image) {
        try {
            final ImageViewerController controller = ControllerTools.openImageViewer(null, null);
            if (controller != null) {
                controller.loadImage(image);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(ImageInformation info) {
        try {
            ImageViewerController controller = ControllerTools.openImageViewer(null, null);
            controller.loadImageInfo(info);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImagesBrowserController openImagesBrowser(Stage stage) {
        try {
            final ImagesBrowserController controller = (ImagesBrowserController) openScene(stage, Fxmls.ImagesBrowserFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageMetaDataController openImageMetaData(Stage stage, ImageInformation info) {
        try {
            final ImageMetaDataController controller = (ImageMetaDataController) openScene(stage, Fxmls.ImageMetaDataFxml);
            controller.loadImageFileMeta(info);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void showImageMetaData(ImageInformation info) {
        if (info == null) {
            return;
        }
        ControllerTools.openImageMetaData(null, info);
    }

    public static void showImageInformation(ImageInformation info) {
        if (info == null) {
            return;
        }
        ControllerTools.openImageInformation(null, info);
    }

    public static ImageInformationController openImageInformation(Stage stage, ImageInformation info) {
        try {
            if (info == null) {
                return null;
            }
            final ImageInformationController controller = (ImageInformationController) openScene(stage, Fxmls.ImageInformationFxml);
            controller.loadImageFileInformation(info);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static TextEditorController openTextEditer(Stage stage, File file) {
        try {
            final TextEditorController controller = (TextEditorController) openScene(stage, Fxmls.TextEditorFxml);
            controller.openFile(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MarkdownEditorController openMarkdownEditer(Stage stage, File file) {
        try {
            final MarkdownEditorController controller = (MarkdownEditorController) openScene(stage, Fxmls.MarkdownEditorFxml);
            controller.openFile(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MediaPlayerController openMediaPlayer(Stage stage, File file) {
        try {
            final MediaPlayerController controller = (MediaPlayerController) openScene(stage, Fxmls.MediaPlayerFxml);
            controller.loadFile(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BytesEditorController openBytesEditer(Stage stage, File file) {
        try {
            final BytesEditorController controller = (BytesEditorController) openScene(stage, Fxmls.BytesEditorFxml);
            controller.openFile(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlTableController openHtmlTable(Stage stage, String body) {
        try {
            if (body == null) {
                return null;
            }
            final HtmlTableController controller = (HtmlTableController) openScene(stage, Fxmls.HtmlTableFxml);
            controller.loadBody(body);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, String link) {
        try {
            final HtmlEditorController controller = (HtmlEditorController) openScene(stage, Fxmls.HtmlEditorFxml);
            controller.loadAddress(link);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, File file) {
        try {
            final HtmlEditorController controller = (HtmlEditorController) openScene(stage, Fxmls.HtmlEditorFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static WebBrowserController openWebBrowser(Stage stage, File file) {
        try {
            WebBrowserController controller = WebBrowserController.oneOpen(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static PdfViewController openPdfViewer(Stage stage, File file) {
        try {
            final PdfViewController controller = (PdfViewController) openScene(stage, Fxmls.PdfViewFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static DataFileCSVController openCsvEditor(Stage stage, File file) {
        try {
            DataFileCSVController controller = (DataFileCSVController) openScene(stage, Fxmls.DataFileCSVFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static PptViewController viewPPT(Stage stage, File file) {
        try {
            final PptViewController controller = (PptViewController) openScene(stage, Fxmls.PptViewFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static WordViewController viewWord(Stage stage, File file) {
        try {
            final WordViewController controller = (WordViewController) openScene(stage, Fxmls.WordViewFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static DataFileExcelController openExcelEditor(Stage stage, File file) {
        try {
            final DataFileExcelController controller = (DataFileExcelController) openScene(stage, Fxmls.DataFileExcelFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static FileDecompressUnarchiveController openDecompressUnarchive(Stage stage, File file) {
        try {
            final FileDecompressUnarchiveController controller = (FileDecompressUnarchiveController) openScene(stage, Fxmls.FileDecompressUnarchiveFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void about() {
        try {
            StringTable table = new StringTable(null, "MyBox");
            table.newNameValueRow("Author", "Mara");
            table.newNameValueRow("Version", AppValues.AppVersion);
            table.newNameValueRow("Date", AppValues.AppVersionDate);
            table.newNameValueRow("License", Languages.message("FreeOpenSource"));
            table.newLinkRow("", "https://www.apache.org/licenses/LICENSE-2.0");
            table.newLinkRow("MainPage", "https://github.com/Mararsh/MyBox");
            table.newLinkRow("Mirror", "https://sourceforge.net/projects/mara-mybox/files/");
            table.newLinkRow("LatestRelease", "https://github.com/Mararsh/MyBox/releases");
            table.newLinkRow("KnownIssues", "https://github.com/Mararsh/MyBox/issues");
            table.newNameValueRow("", Languages.message("WelcomePR"));
            table.newLinkRow("CloudStorage", "https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F");
            table.newLinkRow("MyBoxInternetDataPath", "https://github.com/Mararsh/MyBox_data");
            File htmFile = HtmlWriteTools.writeHtml(table.html());
            if (htmFile == null || !htmFile.exists()) {
                return;
            }
            SoundTools.miao5();
            WebBrowserController.oneOpen(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
