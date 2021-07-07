package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BytesEditerController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.FileDecompressUnarchiveController;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.controller.HtmlViewerController;
import mara.mybox.controller.ImageInformationController;
import mara.mybox.controller.ImageManufactureController;
import mara.mybox.controller.ImageMetaDataController;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.controller.LoadingController;
import mara.mybox.controller.MarkdownEditerController;
import mara.mybox.controller.MediaPlayerController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.PptViewController;
import mara.mybox.controller.TextEditerController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.controller.WordViewController;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.DerbyBase.DerbyStatus;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-1-27 21:48:55
 * @License Apache License Version 2.0
 */
public class FxmlWindow {

    public static BaseController initScene(Stage stage, String newFxml, StageStyle stageStyle) {
        return initScene(stage, newFxml, AppVariables.currentBundle, stageStyle);
    }

    public static BaseController initScene(Stage stage, String newFxml,
            ResourceBundle bundle, StageStyle stageStyle) {
        try {
            if (stage == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlWindow.class.getResource(newFxml), bundle);
            return initController(fxmlLoader, stage, stageStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController initController(FXMLLoader fxmlLoader,
            Stage stage, StageStyle stageStyle) {
        try {
            if (fxmlLoader == null) {
                return null;
            }
            Scene scene = new Scene(fxmlLoader.load());
            BaseController controller = (BaseController) fxmlLoader.getController();
            return initController(controller, scene, stage, stageStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController initController(BaseController controller, Scene scene,
            Stage stage, StageStyle stageStyle) {
        try {
            if (controller == null) {
                return null;
            }
            controller.setMyScene(scene);
            controller.setMyStage(stage);
            scene.getStylesheets().add(FxmlWindow.class.getResource(AppVariables.getStyle()).toExternalForm());

            stage.setUserData(controller);
            stage.getIcons().add(CommonFxValues.AppIcon);
            stage.setTitle(controller.getBaseTitle());
            if (stageStyle != null) {
                stage.initStyle(stageStyle);
            }
            stage.setOnShown((WindowEvent event) -> {
                controller.afterStageShown();
            });
            // External request to close
            stage.setOnCloseRequest((WindowEvent event) -> {
                if (!controller.leavingScene()) {
                    event.consume();
                } else {
                    FxmlWindow.closeWindow(stage);
                }
            });
            // Close anyway
            stage.setOnHiding((WindowEvent event) -> {
                FxmlWindow.closeWindow(stage);
            });

            stage.setScene(scene);
            stage.show();

            controller.afterSceneLoaded();

            String fxml = controller.getMyFxml();
            if (controller.getMainMenuController() != null && !fxml.contains(CommonValues.LoadingFxml)) {
                VisitHistoryTools.visitMenu(controller.getBaseTitle(), fxml);
            }
            Platform.setImplicitExit(AppVariables.scheduledTasks == null || AppVariables.scheduledTasks.isEmpty());

            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController initController(FXMLLoader fxmlLoader) {
        return initController(fxmlLoader, newStage(), null);
    }

    public static BaseController initController(BaseController controller, Scene scene) {
        return initController(controller, scene, newStage(), null);
    }

    public static BaseController setScene(final String fxml) {
        try {
            if (fxml == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlWindow.class.getResource(fxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            try {
                pane.getStylesheets().add(FxmlWindow.class.getResource(AppVariables.getStyle()).toExternalForm());
            } catch (Exception e) {
            }
            Scene scene = new Scene(pane);

            BaseController controller = (BaseController) fxmlLoader.getController();
            controller.setMyScene(scene);
            controller.initSplitPanes();
            controller.refreshStyle();

            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static FXMLLoader newFxml(String fxml) {
        try {
            return new FXMLLoader(FxmlWindow.class.getResource(fxml), AppVariables.currentBundle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Stage newStage() {
        try {
            Stage newStage = new Stage();
            newStage.initModality(Modality.NONE);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.initOwner(null);
            return newStage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(String fxml) {
        return initController(newFxml(fxml), newStage(), null);
    }

    public static BaseController openStage(Stage parent, String fxml, ResourceBundle bundle,
            boolean isOwned, Modality modality, StageStyle stageStyle) {
        try {
            Stage stage = new Stage();
            stage.initModality(modality);
            if (isOwned && parent != null) {
                stage.initOwner(parent);
            } else {
                stage.initOwner(null);
            }
            return initScene(stage, fxml, bundle, stageStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController openTableStage(Stage parent, String newFxml, boolean isOwned, Modality modality,
            StageStyle stageStyle) {
        return openStage(parent, newFxml, AppVariables.getTableBundle(), isOwned, modality, stageStyle);
    }

    public static BaseController openTableStage(String fxml) {
        return openTableStage(null, fxml, false, Modality.NONE, null);
    }

    public static BaseController openStage(Stage parent, String newFxml, boolean isOwned, Modality modality,
            StageStyle stageStyle) {
        return openStage(parent, newFxml, AppVariables.currentBundle, isOwned, modality, stageStyle);
    }

    public static BaseController openStage(Stage parent, String newFxml, boolean isOwned, Modality modality) {
        return openStage(parent, newFxml, isOwned, modality, null);

    }

    public static BaseController openStage(Stage parent, String newFxml, boolean isOwned) {
        return openStage(parent, newFxml, isOwned, Modality.NONE);
    }

    public static BaseController openStage(Stage parent, String newFxml) {
        return openStage(parent, newFxml, false, Modality.NONE);
    }

    public static BaseController openScene(Stage parent, String newFxml, StageStyle stageStyle) {
        try {
            Stage newStage = new Stage();  // new stage should be opened instead of keeping old stage, to clean resources
            newStage.initModality(Modality.NONE);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.initOwner(null);
            BaseController controller = initScene(newStage, newFxml, stageStyle);
            if (parent != null) {
                closeWindow(parent);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BaseController openScene(Stage parent, String fxml) {
        return openScene(parent, fxml, null);
    }

    public static Popup makePopWindow(String fxml) {
        return makePopWindow(null, fxml);
    }

    public static Popup makePopWindow(BaseController parent) {
        return makePopWindow(parent, CommonValues.PopNodesFxml);
    }

    public static Popup makePopWindow(BaseController parent, String fxml) {
        try {
            BaseController controller = setScene(fxml);
            if (controller == null) {
                return null;
            }
            controller.setParentController(parent);
            Popup popup = new Popup();
            popup.setAutoHide(true);

            popup.getContent().add(controller.getMyScene().getRoot());
            popup.setUserData(controller);

            if (parent != null) {
                parent.closePopup();
                parent.setPopup(popup);
            }
            return popup;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Popup popWindow(BaseController parent, MouseEvent event) {
        return popWindow(parent, CommonValues.PopNodesFxml, event);
    }

    public static Popup popWindow(BaseController parent, String fxml, MouseEvent event) {
        return popWindow(parent, fxml, (Node) event.getSource(), event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public static Popup popWindow(BaseController parent, ContextMenuEvent event) {
        return popWindow(parent, CommonValues.PopNodesFxml, event);
    }

    public static Popup popWindow(BaseController parent, String fxml, ContextMenuEvent event) {
        return popWindow(parent, fxml, (Node) event.getSource(), event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public static Popup popWindow(BaseController parent, Node owner, double x, double y) {
        return popWindow(parent, CommonValues.PopNodesFxml, owner, x, y);
    }

    public static Popup popWindow(BaseController parent, String fxml, Node owner, double x, double y) {
        try {
            Popup popup = makePopWindow(parent, fxml);
            if (popup == null) {
                return null;
            }
            popup.show(owner, x, y);
            return popup;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void closeWindow(Window window) {
        try {
            if (window == null) {
                return;
            }
            Object object = window.getUserData();
            if (object != null && object instanceof BaseController) {
                try {
                    ((BaseController) object).leaveScene();
                } catch (Exception e) {
                }
            }
            window.hide();
            if (Window.getWindows().isEmpty()) {
                appExit();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void appExit() {
        try {
            if (Window.getWindows() != null) {
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    window.hide();
                }
            }
            AppVariables.stopTextClipboardMonitor();

            if (AppVariables.scheduledTasks != null && !AppVariables.scheduledTasks.isEmpty()) {
                if (AppVariables.getUserConfigBoolean("StopAlarmsWhenExit")) {
                    for (Long key : AppVariables.scheduledTasks.keySet()) {
                        ScheduledFuture future = AppVariables.scheduledTasks.get(key);
                        future.cancel(true);
                    }
                    AppVariables.scheduledTasks = null;
                    if (AppVariables.executorService != null) {
                        AppVariables.executorService.shutdownNow();
                        AppVariables.executorService = null;
                    }
                }
            } else {
                if (AppVariables.scheduledTasks != null) {
                    AppVariables.scheduledTasks = null;
                }
                if (AppVariables.executorService != null) {
                    AppVariables.executorService.shutdownNow();
                    AppVariables.executorService = null;
                }
            }

            if (AppVariables.scheduledTasks == null || AppVariables.scheduledTasks.isEmpty()) {
                MyBoxLog.info("Exit now. Bye!");
                if (DerbyBase.status == DerbyStatus.Embedded) {
                    MyBoxLog.debug("Shut down Derby...");
                    DerbyBase.shutdownEmbeddedDerby();
                }
                Platform.exit(); // Some thread may still be alive after this
                System.exit(0);  // Go
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public static List<Stage> findStages(String title) {
        try {
            if (title == null) {
                return null;
            }
            List<Stage> stages = new ArrayList<>();
            for (Window window : Window.getWindows()) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                if (stage.getTitle().startsWith(title)) {
                    stages.add(stage);
                }
            }
            return stages;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Stage findStage(String title) {
        try {
            if (title == null) {
                return null;
            }
            for (Window window : Window.getWindows()) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                if (stage.getTitle().startsWith(title)) {
                    return stage;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static Stage currentStage() {
        try {
            for (Window window : Window.getWindows()) {
                Stage stage = (Stage) window;
                if (stage.isShowing() && stage.isFocused()) {
                    return stage;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static BaseController openMyBox(Stage stage) {
        return openScene(stage, CommonValues.MyboxFxml);
    }

    public static PdfViewController openPdfViewer(Stage stage, File file) {
        try {
            final PdfViewController controller
                    = (PdfViewController) openScene(stage, CommonValues.PdfViewFxml);
            controller.sourceFileChanged(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static DataFileCSVController openCsvEditor(Stage stage, File file) {
        try {
            DataFileCSVController controller
                    = (DataFileCSVController) openScene(stage, CommonValues.DataFileCSVFxml);
            controller.sourceFileChanged(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static DataFileExcelController openExcelEditor(Stage stage, File file) {
        try {
            final DataFileExcelController controller
                    = (DataFileExcelController) openScene(stage, CommonValues.DataFileExcelFxml);
            controller.sourceFileChanged(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static PptViewController viewPPT(Stage stage, File file) {
        try {
            final PptViewController controller
                    = (PptViewController) openScene(stage, CommonValues.PptViewFxml);
            controller.sourceFileChanged(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static WordViewController viewWord(Stage stage, File file) {
        try {
            final WordViewController controller
                    = (WordViewController) openScene(stage, CommonValues.WordViewFxml);
            controller.sourceFileChanged(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, String link) {
        try {
            final HtmlEditorController controller
                    = (HtmlEditorController) openScene(stage, CommonValues.HtmlEditorFxml);
            controller.loadAddress(link);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, File file) {
        try {
            final HtmlEditorController controller
                    = (HtmlEditorController) openScene(stage, CommonValues.HtmlEditorFxml);
            controller.sourceFileChanged(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlViewerController openHtmlViewer(Stage stage, String body) {
        try {
            final HtmlViewerController controller
                    = (HtmlViewerController) openScene(stage, CommonValues.HtmlViewerFxml);
            controller.loadBody(body);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static WebBrowserController openWebBrowser(Stage stage, File file) {
        try {
            WebBrowserController controller = WebBrowserController.oneOpen(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage, File file) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImageFile(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage, File file, ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(file, imageInfo);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage, ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImageInfo(imageInfo);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            Image image) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(image);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Stage stage) {
        try {
            final ImageViewerController controller
                    = (ImageViewerController) openScene(stage, CommonValues.ImageViewerFxml);
            controller.toFront();
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
                controller.toFront();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImagesBrowserController openImagesBrowser(Stage stage) {
        try {
            final ImagesBrowserController controller = (ImagesBrowserController) openScene(stage,
                    CommonValues.ImagesBrowserFxml);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static TextEditerController openTextEditer(Stage stage, File file) {
        try {
            final TextEditerController controller
                    = (TextEditerController) openScene(stage, CommonValues.TextEditerFxml);
            controller.openFile(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BytesEditerController openBytesEditer(Stage stage, File file) {
        try {
            final BytesEditerController controller
                    = (BytesEditerController) openScene(stage, CommonValues.BytesEditerFxml);
            controller.openFile(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MarkdownEditerController openMarkdownEditer(Stage stage,
            File file) {
        try {
            final MarkdownEditerController controller
                    = (MarkdownEditerController) openScene(stage, CommonValues.MarkdownEditorFxml);
            controller.openFile(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageInformationController openImageInformation(Stage stage,
            ImageInformation info) {
        try {
            if (info == null) {
                return null;
            }
            final ImageInformationController controller = (ImageInformationController) openScene(stage,
                    CommonValues.ImageInformationFxml);
            controller.loadImageFileInformation(info);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageMetaDataController openImageMetaData(Stage stage,
            ImageInformation info) {
        try {
            final ImageMetaDataController controller = (ImageMetaDataController) openScene(stage,
                    CommonValues.ImageMetaDataFxml);
            controller.loadImageFileMeta(info);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static FileDecompressUnarchiveController openDecompressUnarchive(
            Stage stage, File file) {
        try {
            final FileDecompressUnarchiveController controller = (FileDecompressUnarchiveController) openScene(stage,
                    CommonValues.FileDecompressUnarchiveFxml);
            controller.sourceFileChanged(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MediaPlayerController openMediaPlayer(Stage stage, File file) {
        try {
            final MediaPlayerController controller
                    = (MediaPlayerController) openScene(stage, CommonValues.MediaPlayerFxml);
            controller.loadFile(file);
            controller.toFront();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
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
            browseURI(stage, file.toURI());
            return controller;
        }

        String suffix = FileTools.getFileSuffix(file.getAbsolutePath()).toLowerCase();
        if (CommonValues.SupportedImages.contains(suffix)) {
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

        } else if (Arrays.asList(CommonValues.TextFileSuffix).contains(suffix)) {
            controller = openTextEditer(stage, file);

        } else if (CompressTools.compressFormats().contains(suffix)
                || CompressTools.archiveFormats().contains(suffix)) {
            controller = openDecompressUnarchive(stage, file);

        } else if (Arrays.asList(CommonValues.MediaPlayerSupports).contains(suffix)) {
            controller = openMediaPlayer(stage, file);

        } else if (mustOpen) {
            controller = openBytesEditer(stage, file);
            //            try {
//               browseURI(file.toURI());
//            } catch (Exception e) {
//            }
        }
        if (controller != null) {
            if (controller.getMyStage() != null) {
                controller.getMyStage().requestFocus();
                controller.getMyStage().toFront();
            }
        }
        return controller;
    }

    public static void browseURI(Stage myStage, URI uri) {
        if (uri == null) {
            return;
        }
        if (SystemTools.isLinux()) {
            // On my CentOS 7, system hangs when both Desktop.isDesktopSupported() and
            // desktop.isSupported(Desktop.Action.BROWSE) are true.
            // https://stackoverflow.com/questions/27879854/desktop-getdesktop-browse-hangs
            // Below workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
            try {
                if (Runtime.getRuntime().exec(new String[]{"which", "xdg-open"}).getInputStream().read() > 0) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", uri.toString()});
                    return;
                } else {
                }
            } catch (Exception e) {
            }

        } else if (SystemTools.isMac()) {
            // https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java/28807079#28807079
            try {
                Runtime rt = Runtime.getRuntime();
                rt.exec("open " + uri.toString());
                return;
            } catch (Exception e) {
            }

        } else if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                    // https://stackoverflow.com/questions/23176624/javafx-freeze-on-desktop-openfile-desktop-browseuri?r=SearchResults
                    // Menus are blocked after system explorer is opened
//                    if (myStage != null) {
//                        new Timer().schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                Platform.runLater(() -> {
//                                    myStage.requestFocus();
//                                });
//                            }
//                        }, 1000);
//                    }
                    return;
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            }
        }

        if (!uri.getScheme().equals("file") || new File(uri.getPath()).isFile()) {
            openTarget(null, uri.toString());
        } else {
            alertError(myStage, message("DesktopNotSupportBrowse"));
        }

    }

    public static Alert alertError(String information) {
        return alertError(null, information);
    }

    public static Alert alertError(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            if (myStage != null) {
                alert.setTitle(myStage.getTitle());
            }
            alert.setHeaderText(null);
            alert.setContentText(information);
//            alert.getDialogPane().applyCss();
            // https://stackoverflow.com/questions/38799220/javafx-how-to-bring-dialog-alert-to-the-front-of-the-screen?r=SearchResults
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            alert.showAndWait();
            return alert;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void alertWarning(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(myStage.getTitle());
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            alert.showAndWait();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void alertInformation(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            if (myStage != null) {
                alert.setTitle(myStage.getTitle());
            }
            alert.setHeaderText(null);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setContent(new Label(information));
//            alert.setContentText(information);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            alert.showAndWait();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static ImageViewerController openImageViewer(File file) {
        return FxmlWindow.openImageViewer(null, file);
    }

    public static ImageViewerController openImageViewer(String file) {
        return FxmlWindow.openImageViewer(null, new File(file));
    }

    public static ImageViewerController openImageViewer(Image image) {
        try {
            final ImageViewerController controller = FxmlWindow.openImageViewer(null, null);
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
            ImageViewerController controller = FxmlWindow.openImageViewer(null, null);
            controller.loadImageInfo(info);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void openImageManufacture(String filename) {
        FxmlWindow.openImageManufacture(null, new File(filename));
    }

    public static void showImageInformation(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlWindow.openImageInformation(null, info);
    }

    public static void showImageMetaData(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlWindow.openImageMetaData(null, info);
    }

    public static LoadingController openLoadingStage(Modality block) {
        return openLoadingStage(null, block, null, null);
    }

    public static LoadingController openLoadingStage(Modality block, String info) {
        return openLoadingStage(null, block, null, info);
    }

    public static LoadingController openLoadingStage(Stage stage, Modality block, String info) {
        return openLoadingStage(stage, block, null, info);
    }

    public static LoadingController openLoadingStage(Stage stage, Modality block, Task task, String info) {
        try {
            final LoadingController controller
                    = (LoadingController) FxmlWindow.openStage(stage, CommonValues.LoadingFxml,
                            true, block, StageStyle.TRANSPARENT);
            controller.init(task);
            if (info != null) {
                controller.setInfo(info);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

    public static boolean mapFirstRun(BaseController c) {
        if (AppVariables.getSystemConfigBoolean("MapRunFirstTime" + CommonValues.AppVersion, true)) {
            WebBrowserController.mapFirstRun();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        AppVariables.setSystemConfigValue("MapRunFirstTime" + CommonValues.AppVersion, false);
                        c.reload();
                    });
                }
            }, 2000);
            return true;
        }
        return false;
    }

    public static void about() {
        try {
            StringTable table = new StringTable(null, "MyBox");
            table.newNameValueRow("Author", "Mara");
            table.newNameValueRow("Version", CommonValues.AppVersion);
            table.newNameValueRow("Date", CommonValues.AppVersionDate);
            table.newNameValueRow("License", message("FreeOpenSource"));
            table.newLinkRow("", "http://www.apache.org/licenses/LICENSE-2.0");
            table.newLinkRow("MainPage", "https://github.com/Mararsh/MyBox");
            table.newLinkRow("Mirror", "https://sourceforge.net/projects/mara-mybox/files/");
            table.newLinkRow("LatestRelease", "https://github.com/Mararsh/MyBox/releases");
            table.newLinkRow("KnownIssues", "https://github.com/Mararsh/MyBox/issues");
            table.newNameValueRow("", message("WelcomePR"));
            table.newLinkRow("UserGuide", "https://mararsh.github.io/MyBox/MyBox-UserGuide.pdf");
            table.newLinkRow("CloudStorage", "https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F");
            table.newLinkRow("MyBoxInternetDataPath", "https://github.com/Mararsh/MyBox_data");

            File htmFile = HtmlTools.writeHtml(table.html());
            if (htmFile == null || !htmFile.exists()) {
                return;
            }
            FxmlControl.miao5();
            WebBrowserController.oneOpen(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
