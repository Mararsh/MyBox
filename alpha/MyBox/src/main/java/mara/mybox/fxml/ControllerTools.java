package mara.mybox.fxml;

import java.io.File;
import java.util.Arrays;
import javafx.stage.Stage;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BytesEditorController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.FileDecompressUnarchiveController;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.controller.MarkdownEditorController;
import mara.mybox.controller.MediaPlayerController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.PptViewController;
import mara.mybox.controller.TextEditorController;
import mara.mybox.controller.WordViewController;
import static mara.mybox.fxml.WindowTools.openScene;
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
            return HtmlEditorController.openAddress(filename);
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
            controller = ImageViewerController.openFile(stage, file);
        } else if ("html".equals(suffix) || "htm".equals(suffix)) {
            controller = HtmlEditorController.openFile(file);
        } else if ("md".equals(suffix)) {
            controller = MarkdownEditorController.open(file);
        } else if ("pdf".equals(suffix)) {
            controller = PdfViewController.open(file);
        } else if ("csv".equals(suffix)) {
            controller = DataFileCSVController.openFile(file);
        } else if ("xlsx".equals(suffix) || "xls".equals(suffix)) {
            controller = DataFileExcelController.openFile(file);
        } else if ("ppt".equals(suffix) || "pptx".equals(suffix)) {
            controller = PptViewController.openFile(file);
        } else if ("doc".equals(suffix) || "docx".equals(suffix)) {
            controller = WordViewController.openFile(file);
        } else if (Arrays.asList(FileExtensions.TextFileSuffix).contains(suffix)) {
            controller = TextEditorController.open(file);
        } else if (CompressTools.compressFormats().contains(suffix) || CompressTools.archiveFormats().contains(suffix)) {
            controller = FileDecompressUnarchiveController.open(file);
        } else if (Arrays.asList(FileExtensions.MediaPlayerSupports).contains(suffix)) {
            controller = MediaPlayerController.open(file);
        } else if (mustOpen) {
            controller = BytesEditorController.open(file);
        }
        return controller;
    }

}
