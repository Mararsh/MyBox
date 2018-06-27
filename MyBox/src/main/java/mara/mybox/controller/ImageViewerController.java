/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageBaseController {

    private double mouseX, mouseY;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ImageView imageView;
    @FXML
    private HBox toolBar;

    @Override
    protected void initializeNext2() {
        try {

            toolBar.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void sourceFileChanged(final File file) {
        loadImage(file, false);
    }

    @Override
    protected void afterImageLoaded() {
        if (scrollPane.getHeight() < imageInformation.getyPixels()) {
            imageView.setFitHeight(scrollPane.getHeight() - 5);
            imageView.setFitWidth(scrollPane.getWidth() - 1);
        } else {
            imageView.setFitHeight(imageInformation.getyPixels());
            imageView.setFitWidth(imageInformation.getxPixels());
        }
        try {
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
//                        imageView.setImage(new Image("file:" + fileName, true));
        } catch (Exception e) {
            imageView.setImage(null);
            popInformation(AppVaribles.getMessage("NotSupported"));
        }
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
        imageView.setFitHeight(imageInformation.getyPixels());
        imageView.setFitWidth(imageInformation.getxPixels());
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
            if (imageInformation == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageInformationController controller = fxmlLoader.getController();
            controller.loadInformation(imageInformation);

            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setTitle(AppVaribles.getMessage("AppTitle"));
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void showImageMetaData() {
        try {
            if (imageInformation == null) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageMetaDataFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageMetaDataController controller = fxmlLoader.getController();
            controller.loadData(imageInformation);

            Stage imageInformationStage = new Stage();
            controller.setMyStage(imageInformationStage);
            imageInformationStage.setTitle(AppVaribles.getMessage("AppTitle"));
            imageInformationStage.initModality(Modality.NONE);
            imageInformationStage.initStyle(StageStyle.DECORATED);
            imageInformationStage.initOwner(null);
            imageInformationStage.getIcons().add(CommonValues.AppIcon);
            imageInformationStage.setScene(new Scene(root));
            imageInformationStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
