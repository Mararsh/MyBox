package mara.mybox.fxml;

import java.io.File;
import java.util.Arrays;
import javafx.stage.Stage;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.FileDecompressUnarchiveController;
import mara.mybox.controller.HtmlPopController;
import mara.mybox.controller.ImageEditorController;
import mara.mybox.controller.ImagePopController;
import mara.mybox.controller.JavaScriptController;
import mara.mybox.controller.JsonEditorController;
import mara.mybox.controller.MarkdownEditorController;
import mara.mybox.controller.MarkdownPopController;
import mara.mybox.controller.MediaPlayerController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.PptViewController;
import mara.mybox.controller.SvgEditorController;
import mara.mybox.controller.TextEditorController;
import mara.mybox.controller.TextPopController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.controller.WordViewController;
import mara.mybox.controller.XmlEditorController;
import static mara.mybox.fxml.WindowTools.replaceStage;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.FileExtensions;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class ControllerTools {

    public static BaseController openMyBox(Stage stage) {
        return replaceStage(stage, Fxmls.MyboxFxml);
    }

    public static BaseController openTarget(String filename) {
        return openTarget(filename, true);
    }

    public static BaseController openTarget(String filename, boolean mustOpen) {
        if (filename == null) {
            return null;
        }
        if (filename.startsWith("http") || filename.startsWith("ftp")) {
            return WebBrowserController.openAddress(filename, true);
        }
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        if (file.isDirectory()) {
            PopTools.browseURI(null, file.toURI());
            return null;
        }
        String suffix = FileNameTools.suffix(file.getName()).toLowerCase();
        if (FileExtensions.SupportedImages.contains(suffix)) {
            return ImageEditorController.openFile(file);
        } else if ("html".equals(suffix) || "htm".equals(suffix)) {
            return WebBrowserController.openFile(file);
        } else if ("md".equals(suffix)) {
            return MarkdownEditorController.open(file);
        } else if ("pdf".equals(suffix)) {
            return PdfViewController.open(file);
        } else if ("csv".equals(suffix)) {
            return DataFileCSVController.openFile(file);
        } else if ("xlsx".equals(suffix) || "xls".equals(suffix)) {
            return DataFileExcelController.openFile(file);
        } else if ("ppt".equals(suffix) || "pptx".equals(suffix)) {
            return PptViewController.openFile(file);
        } else if ("doc".equals(suffix) || "docx".equals(suffix)) {
            return WordViewController.openFile(file);
        } else if ("json".equals(suffix)) {
            return JsonEditorController.open(file);
        } else if ("xml".equals(suffix)) {
            return XmlEditorController.open(file);
        } else if ("svg".equals(suffix)) {
            return SvgEditorController.open(file);
        } else if ("js".equals(suffix)) {
            return JavaScriptController.openFile(file);
        } else if (Arrays.asList(FileExtensions.TextFileSuffix).contains(suffix)) {
            return TextEditorController.open(file);
        } else if (CompressTools.compressFormats().contains(suffix) || CompressTools.archiveFormats().contains(suffix)) {
            return FileDecompressUnarchiveController.open(file);
        } else if (Arrays.asList(FileExtensions.MediaPlayerSupports).contains(suffix)) {
            return MediaPlayerController.open(file);
        } else if (mustOpen) {
            PopTools.browseURI(null, file.toURI());
        }
        return null;
    }

    public static BaseController popTarget(BaseController parent, String filename, boolean mustOpen) {
        if (filename == null) {
            return null;
        }
        if (filename.startsWith("http") || filename.startsWith("ftp")) {
            return HtmlPopController.openHtml(parent, filename);
        }
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        if (file.isDirectory()) {
            PopTools.browseURI(parent, file.toURI());
            return null;
        }
        if (file.length() > 1024 * 1024) {
            return openTarget(filename, true);
        }
        String suffix = FileNameTools.suffix(file.getName()).toLowerCase();
        if (FileExtensions.SupportedImages.contains(suffix)) {
            return ImagePopController.openFile(parent, filename);

        } else if ("html".equals(suffix) || "htm".equals(suffix)) {
            return HtmlPopController.openHtml(parent, filename);

        } else if ("md".equals(suffix)) {
            return MarkdownPopController.openFile(parent, filename);

        } else if (Arrays.asList(FileExtensions.TextFileSuffix).contains(suffix)) {
            return TextPopController.openFile(parent, filename);

        } else if (mustOpen) {
            return openTarget(filename, mustOpen);

        } else {
            return null;
        }
    }

}
