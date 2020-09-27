package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BytesEditerController;
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
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.TextEditerController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-1-27 21:48:55
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlStage {

    public static BaseController initScene(final Stage stage, final String newFxml, StageStyle stageStyle) {
        return initScene(stage, newFxml, AppVariables.currentBundle, stageStyle);
    }

    public static BaseController initScene(final Stage stage, final String newFxml,
            ResourceBundle bundle, StageStyle stageStyle) {
        try {
            if (stage == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlStage.class.getResource(newFxml), bundle);
            Pane pane = fxmlLoader.load();
            try {
                pane.getStylesheets().add(FxmlStage.class.getResource(AppVariables.getStyle()).toExternalForm());
            } catch (Exception e) {
            }
            Scene scene = new Scene(pane);

            final BaseController controller = (BaseController) fxmlLoader.getController();
            controller.setMyStage(stage);
            controller.setMyScene(scene);
            controller.setLoadFxml(newFxml);

//            stage.setUserData(controller);
            stage.getIcons().add(CommonFxValues.AppIcon);
            stage.setTitle(controller.getBaseTitle());
            if (stageStyle != null) {
                stage.initStyle(stageStyle);
            }
            stage.setOnShown((WindowEvent event) -> {
                controller.afterStageShown();
            });
            stage.setOnCloseRequest((WindowEvent event) -> {
                if (!controller.leavingScene()) {
                    event.consume();
                } else {
                    FxmlStage.closeStage(stage);
                }
            });

            stage.setScene(scene);
            stage.show();
            controller.afterSceneLoaded();

            if (controller.getMainMenuController() != null && !newFxml.equals(CommonValues.LoadingFxml)) {
                VisitHistoryTools.visitMenu(controller.getBaseTitle(), newFxml);
            }

            Platform.setImplicitExit(AppVariables.scheduledTasks == null || AppVariables.scheduledTasks.isEmpty());

            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController initScene(final String newFxml) {
        try {
            if (newFxml == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlStage.class.getResource(newFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            try {
                pane.getStylesheets().add(FxmlStage.class.getResource(AppVariables.getStyle()).toExternalForm());
            } catch (Exception e) {
            }
            Scene scene = new Scene(pane);

            BaseController controller = (BaseController) fxmlLoader.getController();
            controller.setMyScene(scene);
            controller.setLoadFxml(newFxml);
            controller.initSplitPanes();
            controller.refreshStyle();
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(Stage myStage, String newFxml, ResourceBundle bundle,
            boolean isOwned, Modality modality, StageStyle stageStyle) {
        try {
            Stage stage = new Stage();
            stage.initModality(modality);
            if (isOwned && myStage != null) {
                stage.initOwner(myStage);
            } else {
                stage.initOwner(null);
            }
            return initScene(stage, newFxml, bundle, stageStyle);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openTableStage(Stage myStage, String newFxml, boolean isOwned, Modality modality,
            StageStyle stageStyle) {
        return openStage(myStage, newFxml, AppVariables.getTableBundle(), isOwned, modality, stageStyle);
    }

    public static BaseController openTableStage(String newFxml) {
        return openTableStage(null, newFxml, false, Modality.NONE, null);
    }

    public static BaseController openStage(Stage myStage, String newFxml, boolean isOwned, Modality modality,
            StageStyle stageStyle) {
        return openStage(myStage, newFxml, AppVariables.currentBundle, isOwned, modality, stageStyle);
    }

    public static BaseController openStage(Stage myStage, String newFxml, boolean isOwned, Modality modality) {
        return openStage(myStage, newFxml, isOwned, modality, null);

    }

    public static BaseController openStage(Stage myStage, String newFxml, boolean isOwned) {
        return openStage(myStage, newFxml, isOwned, Modality.NONE);
    }

    public static BaseController openStage(Stage myStage, String newFxml) {
        return openStage(myStage, newFxml, false, Modality.NONE);
    }

    public static BaseController openStage(String newFxml) {
        return openStage(null, newFxml, false, Modality.NONE);
    }

    public static BaseController openScene(Stage stage, String newFxml,
            StageStyle stageStyle) {
        try {
            Stage newStage = new Stage();  // new stage should be opened instead of keeping old stage, to clean resources
            newStage.initModality(Modality.NONE);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.initOwner(null);
            BaseController controller = initScene(newStage, newFxml, stageStyle);

            if (stage != null) {
                closeStage(stage);
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openScene(Stage stage, String newFxml) {
        return openScene(stage, newFxml, null);
    }

    public static void closeStage(Stage stage) {
        try {
            stage.close();
            if (Window.getWindows().isEmpty()) {
                appExit();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void appExit() {
        try {
//            if (AppVariables.backgroundTasks != null && !AppVariables.backgroundTasks.isEmpty()) {
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.setContentText(MessageFormat.format(message("BackgroundTasksRunning"), AppVariables.backgroundTasks.size()));
//                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
//                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//                stage.setAlwaysOnTop(true);
//                stage.toFront();
//                Optional<ButtonType> result = alert.showAndWait();
//                if (result.get() == null || result.get() != ButtonType.OK) {
//                    return;
//                }
//            }

            if (Window.getWindows() != null) {
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    window.hide();
                }
            }

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

//                logger.debug("Shut down Derby server...");
//                DerbyBase.shutdownDerbyServer();
                logger.debug("Exit now. Bye!");
                Platform.exit(); // Some thread may still be alive after this
                System.exit(0);  // Go
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public static BaseController openMyBox(Stage stage) {
        return openScene(stage, CommonValues.MyboxFxml);
    }

    public static PdfViewController openPdfViewer(Stage stage, File file) {
        try {
            final PdfViewController controller
                    = (PdfViewController) openScene(stage, CommonValues.PdfViewFxml);
            controller.sourceFileChanged(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, String link) {
        try {
            final HtmlEditorController controller
                    = (HtmlEditorController) openScene(stage, CommonValues.HtmlEditorFxml);
            controller.switchBroswerTab();
            controller.loadLink(link);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, File file) {
        try {
            final HtmlEditorController controller
                    = (HtmlEditorController) openScene(stage, CommonValues.HtmlEditorFxml);
            controller.switchBroswerTab();
            controller.setNotChangedAfterLoad(true);
            controller.sourceFileChanged(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static HtmlViewerController openHtmlViewer(Stage stage, String body) {
        try {
            final HtmlViewerController controller
                    = (HtmlViewerController) openScene(stage, CommonValues.HtmlViewerFxml);
            controller.loadBody(body);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static WebBrowserController openWebBrowser(Stage stage, File file) {
        try {
            final WebBrowserController controller
                    = (WebBrowserController) openScene(stage, CommonValues.WebBrowserFxml);
            controller.loadFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            File file) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(file.getAbsolutePath());
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            File file, Image image, ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(file, image, imageInfo);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(imageInfo);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            Image image) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(image);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Stage stage) {
        try {
            final ImageViewerController controller
                    = (ImageViewerController) openScene(stage, CommonValues.ImageViewerFxml);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Stage stage, File file) {
        try {
            final ImageViewerController controller = openImageViewer(stage);
            if (controller != null && file != null) {
                controller.loadImage(file.getAbsolutePath());
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImagesBrowserController openImagesBrowser(Stage stage) {
        try {
            final ImagesBrowserController controller = (ImagesBrowserController) openScene(stage,
                    CommonValues.ImagesBrowserFxml);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static TextEditerController openTextEditer(Stage stage, File file) {
        try {
            final TextEditerController controller
                    = (TextEditerController) openScene(stage, CommonValues.TextEditerFxml);
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BytesEditerController openBytesEditer(Stage stage, File file) {
        try {
            final BytesEditerController controller
                    = (BytesEditerController) openScene(stage, CommonValues.BytesEditerFxml);
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static MarkdownEditerController openMarkdownEditer(Stage stage,
            File file) {
        try {
            final MarkdownEditerController controller
                    = (MarkdownEditerController) openScene(stage, CommonValues.MarkdownEditorFxml);
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
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
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageMetaDataController openImageMetaData(Stage stage,
            ImageInformation info) {
        try {
            final ImageMetaDataController controller = (ImageMetaDataController) openScene(stage,
                    CommonValues.ImageMetaDataFxml);
            controller.loadImageFileMeta(info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static FileDecompressUnarchiveController openDecompressUnarchive(
            Stage stage, File file) {
        try {
            final FileDecompressUnarchiveController controller = (FileDecompressUnarchiveController) openScene(stage,
                    CommonValues.FileDecompressUnarchiveFxml);
            controller.sourceFileChanged(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static MediaPlayerController openMediaPlayer(Stage stage, File file) {
        try {
            final MediaPlayerController controller
                    = (MediaPlayerController) openScene(stage, CommonValues.MediaPlayerFxml);
            controller.loadFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openTarget(Stage stage, String filename) {
        return openTarget(stage, filename, true);
    }

    public static BaseController openTarget(Stage stage,
            String filename, boolean mustOpen) {
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

        } else if (Arrays.asList(CommonValues.TextFileSuffix).contains(suffix)) {
            controller = openTextEditer(stage, file);

        } else if ("pdf".equals(suffix)) {
            controller = openPdfViewer(stage, file);

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
                controller.getMyStage().toFront();
            }
        }
        return controller;
    }

    public static boolean browseURI(Stage myStage, URI uri) {
        if (uri == null) {
            return false;
        }

        if (SystemTools.isLinux()) {
            // On my CentOS 7, system hangs when both Desktop.isDesktopSupported() and
            // desktop.isSupported(Desktop.Action.BROWSE) are true.
            // https://stackoverflow.com/questions/27879854/desktop-getdesktop-browse-hangs
            // Below workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
            try {
                if (Runtime.getRuntime().exec(new String[]{"which", "xdg-open"}).getInputStream().read() != -1) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open",
                        uri.toString()});
                    return true;
                } else {
                }
            } catch (Exception e) {
            }
            if (myStage != null) {
                alertError(myStage, message("DesktopNotSupportBrowse"));
            }

        } else if (SystemTools.isMac()) {
            // https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java/28807079#28807079
            try {
                Runtime rt = Runtime.getRuntime();
                rt.exec("open " + uri.toString());
            } catch (Exception e) {
            }
            if (myStage != null) {
                alertError(myStage, message("DesktopNotSupportBrowse"));
            }

        } else if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                    return true;
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            } else {
                if (myStage != null) {
                    alertError(myStage, message("DesktopNotSupportBrowse"));
                }
            }

        } else {
            if (myStage != null) {
                alertError(myStage, message("DesktopNotSupportBrowse"));
            }
        }

        if (!uri.getScheme().equals("file") || new File(uri.getPath()).isFile()) {
            openTarget(null, uri.toString());
        }
        return true;
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
            logger.error(e.toString());
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
            logger.error(e.toString());
        }
    }

    public static void alertInformation(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(myStage.getTitle());
            alert.setHeaderText(null);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setContent(new Label(information));
//            alert.setContentText(information);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static MyBoxLoadingController showInformation(String information) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initStyle(StageStyle.DECORATED);
            stage.getIcons().add(CommonFxValues.AppIcon);
            FXMLLoader fxmlLoader = new FXMLLoader(
                    FxmlStage.class.getResource(CommonValues.MyBoxLoadingFxml));
            Pane pane = fxmlLoader.load();
            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.show();

            MyBoxLoadingController c = (MyBoxLoadingController) fxmlLoader.getController();
            c.setInfo(information);
            c.setStage(stage);
            return c;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(File file) {
        return FxmlStage.openImageViewer(null, file);
    }

    public static ImageViewerController openImageViewer(String file) {
        return FxmlStage.openImageViewer(null, new File(file));
    }

    public static ImageViewerController openImageViewer(Image image) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null, null);
            if (controller != null) {
                controller.loadImage(image);
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(ImageInformation info) {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(null, null);
            if (controller != null) {
                controller.loadImage(info);
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void openImageManufacture(String filename) {
        FxmlStage.openImageManufacture(null, new File(filename));
    }

    public static void showImageInformation(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageInformation(null, info);
    }

    public static void showImageMetaData(ImageInformation info) {
        if (info == null) {
            return;
        }
        FxmlStage.openImageMetaData(null, info);
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
                    = (LoadingController) FxmlStage.openStage(stage, CommonValues.LoadingFxml,
                            true, block, StageStyle.TRANSPARENT);
            controller.init(task);
            if (info != null) {
                controller.setInfo(info);
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
