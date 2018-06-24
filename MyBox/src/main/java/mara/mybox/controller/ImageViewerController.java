/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.ImageReaders;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class ImageViewerController extends BaseController {

    private ImageFileInformation info;
    private double mouseX, mouseY;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView imageView;
    @FXML
    private HBox toolBar;
    @FXML
    protected TextField sourceFileInput;

    @Override
    protected void initializeNext() {
        try {
            fileExtensionFilter = new ArrayList();
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tif", "*.tiff"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("png", "*.png"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));

            toolBar.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
            );

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        final File file = new File(newValue);
                        if (!file.exists() || file.isDirectory()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        sourceFileInput.setStyle(null);
                        sourceFileChanged(file);
                        AppVaribles.setConfigValue("imageSourcePath", file.getParent());
                    }
                });
            }

//            imageView.setCursor(Cursor.HAND);
//            imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    mouseX = event.getX();
//                    mouseY = event.getY();
//                }
//            });
//            imageView.setOnMouseReleased(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    FxmlTools.setScrollPane(scrollPane, mouseX - event.getX(), mouseY - event.getY());
//                }
//            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectSourceFile() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue("imageSourcePath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(myStage);
            if (file != null) {
                sourceFileInput.setText(file.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    protected void sourceFileChanged(final File file) {
        final String fileName = file.getPath();
        task = new Task<Void>() {
            private BufferedImage image;

            @Override
            protected Void call() throws Exception {
                info = ImageReaders.readImageInformation(fileName);
                String format = FileTools.getFileSuffix(fileName);
                if ("raw".equals(format.toLowerCase())) {
                    image = null;
                } else {
                    image = ImageIO.read(file);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (scrollPane.getHeight() < info.getyPixels()) {
                            imageView.setFitHeight(scrollPane.getHeight() - 5);
                            imageView.setFitWidth(scrollPane.getWidth() - 1);
                        } else {
                            imageView.setFitHeight(info.getyPixels());
                            imageView.setFitWidth(info.getxPixels());
                        }
                        try {
                            imageView.setImage(SwingFXUtils.toFXImage(image, null));
//                        imageView.setImage(new Image("file:" + fileName, true));
                        } catch (Exception e) {
                            imageView.setImage(null);
                            popInformation(AppVaribles.getMessage("NotSupported"));
                        }
                    }
                });
                return null;
            }
        };
        openLoadingStage(task);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    void imageMousePressed(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }

    @FXML
    void imageMouseReleased(MouseEvent event) {
        FxmlTools.setScrollPane(scrollPane, mouseX - event.getX(), mouseY - event.getY());
    }

    @FXML
    void popImageInformation(ActionEvent event) {
        showImageInformation();
    }

    @FXML
    void popImageInformation2(MouseEvent event) {
        showImageInformation();
    }

    @FXML
    void popMetaData(ActionEvent event) {
        showImageMetaData();
    }

    @FXML
    void popMetaData2(MouseEvent event) {
        showImageMetaData();
    }

    @FXML
    void zoomIn(ActionEvent event) {
        imageView.setFitHeight(imageView.getFitHeight() * 1.2);
        imageView.setFitWidth(imageView.getFitWidth() * 1.2);
    }

    @FXML
    void zoomOut(ActionEvent event) {
        imageView.setFitHeight(imageView.getFitHeight() * 0.8);
        imageView.setFitWidth(imageView.getFitWidth() * 0.8);

    }

    @FXML
    void originalSize(ActionEvent event) {
        imageView.setFitHeight(info.getyPixels());
        imageView.setFitWidth(info.getxPixels());
    }

    @FXML
    void windowSize(ActionEvent event) {
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
    }

    public void loadImage(final String fileName) {
        try {
            sourceFileInput.setText(new File(fileName).getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageInformation() {
        try {
            if (info == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageInformationController controller = fxmlLoader.getController();
            controller.loadInformation(info);

            Stage infoStage = new Stage();
            controller.setMyStage(infoStage);
            infoStage.setTitle(AppVaribles.getMessage("AppTitle"));
            infoStage.initModality(Modality.NONE);
            infoStage.initStyle(StageStyle.DECORATED);
            infoStage.initOwner(null);
            infoStage.getIcons().add(CommonValues.AppIcon);
            infoStage.setScene(new Scene(root));
            infoStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageMetaData() {
        try {
            if (info == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageMetaDataFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageMetaDataController controller = fxmlLoader.getController();
            controller.loadData(info);

            Stage infoStage = new Stage();
            controller.setMyStage(infoStage);
            infoStage.setTitle(AppVaribles.getMessage("AppTitle"));
            infoStage.initModality(Modality.NONE);
            infoStage.initStyle(StageStyle.DECORATED);
            infoStage.initOwner(null);
            infoStage.getIcons().add(CommonValues.AppIcon);
            infoStage.setScene(new Scene(root));
            infoStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
