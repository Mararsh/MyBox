package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.Arrays;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2019-1-27 21:48:55
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class OpenFile {

    public static BaseController openNewStage(Class theClass, Stage myStage,
            String newFxml, String title, boolean isOwned, boolean monitorClosing) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
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
            controller.setMyStage(stage);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(Class theClass, Stage stage,
            String newFxml, String title) {
        try {
            if (stage == null) {
                stage = new Stage();
                stage.initModality(Modality.NONE);
                stage.initStyle(StageStyle.DECORATED);
                stage.initOwner(null);
            }
            FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final BaseController controller = (BaseController) fxmlLoader.getController();
            controller.setMyStage(stage);
            if (title == null) {
                controller.setBaseTitle(AppVaribles.getMessage("AppTitle"));
            } else {
                controller.setBaseTitle(title);
            }
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            stage.getIcons().add(CommonValues.AppIcon);
            stage.setTitle(controller.getBaseTitle());
            stage.setScene(new Scene(pane));
            stage.show();

            pane.getStylesheets().add(theClass.getResource(AppVaribles.getStyle()).toExternalForm());

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

    public static ImagesViewerController openImagesViewer(Class theClass, Stage stage) {
        try {
            if (stage == null) {
                stage = new Stage();
                stage.initModality(Modality.NONE);
                stage.initStyle(StageStyle.DECORATED);
                stage.initOwner(null);
            }
            FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource(CommonValues.ImagesViewerFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final ImagesViewerController controller = (ImagesViewerController) fxmlLoader.getController();
            controller.setMyStage(stage);
            controller.setBaseTitle(AppVaribles.getMessage("MultipleImagesViewer"));
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            stage.getIcons().add(CommonValues.AppIcon);
            stage.setTitle(controller.getBaseTitle());
            stage.setScene(new Scene(pane));
            stage.show();

            pane.getStylesheets().add(theClass.getResource(AppVaribles.getStyle()).toExternalForm());

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

    public static Stage openBytesEditer(Class theClass, Stage stage, File file) {
        try {
            final BytesEditerController controller
                    = (BytesEditerController) openStage(theClass, stage,
                            CommonValues.BytesEditerFxml, AppVaribles.getMessage("BytesEditer"));
            controller.openFile(file);
            return stage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean openTarget(Class theClass, Stage stage, String filename) {
        if (filename == null) {
            return false;
        }
        File file = new File(filename);
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            try {
                Desktop.getDesktop().browse(file.toURI());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        String suffix = FileTools.getFileSuffix(file.getAbsolutePath()).toLowerCase();
        if (CommonValues.SupportedImages.contains(suffix)) {
            openImageManufacture(theClass, stage, file);

        } else if ("html".equals(suffix) || "htm".equals(suffix)) {
            OpenFile.openHtmlEditor(theClass, stage, file);

        } else if (Arrays.asList(CommonValues.TextFileSuffix).contains(suffix)) {
            OpenFile.openTextEditer(theClass, stage, file);

        } else if ("pdf".equals(suffix)) {
            OpenFile.openPdfViewer(theClass, stage, file);

        } else {
            try {
                Desktop.getDesktop().browse(file.toURI());
            } catch (Exception e) {
                return false;
            }
        }
        return true;

    }

}
