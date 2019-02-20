package mara.mybox.fxml;

import java.awt.Desktop;
import java.io.File;
import java.util.Arrays;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BytesEditerController;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.controller.ImageInformationController;
import mara.mybox.controller.ImageManufactureController;
import mara.mybox.controller.ImageMetaDataController;
import mara.mybox.controller.ImageStatisticController;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.TextEditerController;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageInformation;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2019-1-27 21:48:55
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlStage {

    public static BaseController openNewStage(Class theClass, Stage myStage,
            String newFxml, String title, boolean isOwned, boolean monitorClosing) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            pane.getStylesheets().add(theClass.getResource(AppVaribles.getStyle()).toExternalForm());
            final BaseController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            Scene scene = new Scene(pane);
            stage.initModality(Modality.NONE);
            if (isOwned) {
                stage.initOwner(myStage);
            } else {
                stage.initOwner(null);
            }
            if (title == null) {
                stage.setTitle(AppVaribles.getMessage("AppTitle"));
            } else {
                stage.setTitle(title);
            }
            controller.setBaseTitle(stage.getTitle());
            controller.setMyStage(stage);
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            if (monitorClosing) {
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        if (!controller.stageClosing()) {
                            event.consume();
                        }
                    }
                });
            }
            stage.show();
            pane.requestFocus();
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(Class theClass, Stage stage,
            String newFxml, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            pane.getStylesheets().add(theClass.getResource(AppVaribles.getStyle()).toExternalForm());
            final BaseController controller = (BaseController) fxmlLoader.getController();
            if (stage == null) {
                stage = new Stage();
                stage.initModality(Modality.NONE);
                stage.initStyle(StageStyle.DECORATED);
                stage.initOwner(null);
            }
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });
            controller.setMyStage(stage);
            if (title == null) {
                controller.setBaseTitle(AppVaribles.getMessage("AppTitle"));
            } else {
                controller.setBaseTitle(title);
            }

            stage.setTitle(controller.getBaseTitle());
            stage.setScene(new Scene(pane));
            stage.show();
            pane.requestFocus();

            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openMyBox(Class theClass, Stage stage) {
        return openStage(theClass, stage, CommonValues.MyboxFxml, AppVaribles.getMessage("AppTitle"));
    }

    public static PdfViewController openPdfViewer(Class theClass, Stage stage, File file) {
        try {
            final PdfViewController controller
                    = (PdfViewController) openStage(theClass, stage,
                            CommonValues.PdfViewFxml, AppVaribles.getMessage("PdfView"));
            controller.sourceFileChanged(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Class theClass, Stage stage, File file) {
        try {
            final HtmlEditorController controller
                    = (HtmlEditorController) openStage(theClass, stage,
                            CommonValues.HtmlEditorFxml, AppVaribles.getMessage("HtmlEditor"));
            controller.switchBroswerTab();
            controller.loadHtml(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Class theClass, Stage stage, File file) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openStage(theClass, stage,
                            CommonValues.ImageManufactureFileFxml, AppVaribles.getMessage("ImageManufacture"));
            controller.loadImage(file.getAbsolutePath());
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Class theClass, Stage stage,
            File file, Image image, ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openStage(theClass, stage,
                            CommonValues.ImageManufactureFileFxml, AppVaribles.getMessage("ImageManufacture"));
            controller.loadImage(file, image, imageInfo);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Class theClass, Stage stage,
            ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openStage(theClass, stage,
                            CommonValues.ImageManufactureFileFxml, AppVaribles.getMessage("ImageManufacture"));
            controller.loadImage(imageInfo);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Class theClass, Stage stage,
            Image image) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openStage(theClass, stage,
                            CommonValues.ImageManufactureFileFxml, AppVaribles.getMessage("ImageManufacture"));
            controller.loadImage(image);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Class theClass, Stage stage) {
        try {
            final ImageViewerController controller
                    = (ImageViewerController) openStage(theClass, stage,
                            CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Class theClass, Stage stage, File file) {
        try {
            final ImageViewerController controller = openImageViewer(theClass, stage);
            if (controller != null) {
                controller.loadImage(file.getAbsolutePath());
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImagesBrowserController openImagesBrowser(Class theClass, Stage stage) {
        try {
            final ImagesBrowserController controller = (ImagesBrowserController) openStage(theClass, stage,
                    CommonValues.ImagesBrowserFxml, AppVaribles.getMessage("ImagesBrowser"));
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static TextEditerController openTextEditer(Class theClass, Stage stage, File file) {
        try {
            final TextEditerController controller
                    = (TextEditerController) openStage(theClass, stage,
                            CommonValues.TextEditerFxml, AppVaribles.getMessage("TextEditer"));
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BytesEditerController openBytesEditer(Class theClass, Stage stage, File file) {
        try {
            final BytesEditerController controller
                    = (BytesEditerController) openStage(theClass, stage,
                            CommonValues.BytesEditerFxml, AppVaribles.getMessage("BytesEditer"));
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageInformationController openImageInformation(Class theClass, Stage stage, ImageInformation info) {
        try {
            final ImageInformationController controller = (ImageInformationController) openStage(theClass, stage,
                    CommonValues.ImageInformationFxml, AppVaribles.getMessage("ImageInformation"));
            controller.loadInformation(info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageMetaDataController openImageMetaData(Class theClass, Stage stage, ImageInformation info) {
        try {
            final ImageMetaDataController controller = (ImageMetaDataController) openStage(theClass, stage,
                    CommonValues.ImageMetaDataFxml, AppVaribles.getMessage("ImageInformation"));
            controller.loadData(info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageStatisticController openImageStatistic(Class theClass, Stage stage, ImageInformation info) {
        try {
            final ImageStatisticController controller = (ImageStatisticController) openStage(theClass, stage,
                    CommonValues.ImageStatisticFxml, AppVaribles.getMessage("ImageStatistic"));
            controller.loadImage(info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageStatisticController openImageStatistic(Class theClass, Stage stage, Image image) {
        try {
            final ImageStatisticController controller = (ImageStatisticController) openStage(theClass, stage,
                    CommonValues.ImageStatisticFxml, AppVaribles.getMessage("ImageStatistic"));
            controller.loadImage(image);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openTarget(Class theClass, Stage stage,
            String filename) {
        return openTarget(theClass, stage, filename, true);
    }

    public static BaseController openTarget(Class theClass, Stage stage,
            String filename, boolean mustOpen) {
        BaseController controller = null;
        if (filename == null) {
            return controller;
        }
        File file = new File(filename);
        if (!file.exists()) {
            return controller;
        }
        if (file.isDirectory()) {
            try {
                Desktop.getDesktop().browse(file.toURI());
            } catch (Exception e) {

            }
            return controller;
        }

        String suffix = FileTools.getFileSuffix(file.getAbsolutePath()).toLowerCase();
        if (CommonValues.SupportedImages.contains(suffix)) {
            controller = openImageViewer(theClass, stage, file);

        } else if ("html".equals(suffix) || "htm".equals(suffix)) {
            controller = openHtmlEditor(theClass, stage, file);

        } else if (Arrays.asList(CommonValues.TextFileSuffix).contains(suffix)) {
            controller = openTextEditer(theClass, stage, file);

        } else if ("pdf".equals(suffix)) {
            controller = openPdfViewer(theClass, stage, file);

        } else if (mustOpen) {
            try {
                Desktop.getDesktop().browse(file.toURI());
            } catch (Exception e) {
            }
        }
        return controller;
    }

    public static void alertError(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(myStage.getTitle());
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void alertWarning(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(myStage.getTitle());
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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
            alert.setContentText(information);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
