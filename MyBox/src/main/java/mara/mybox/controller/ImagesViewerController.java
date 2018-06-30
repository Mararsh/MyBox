/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesViewerController extends ImageViewerController {

    private List<File> imageFileList;
    private int rowsNum, colsNum;
    private List<Pane> imagePaneList;
    private List<ImageViewerIController> imageControllerList;

    @FXML
    protected VBox imagesPane;
    @FXML
    protected Button selectButton;

    @Override
    protected void initializeNext2() {
        try {
            fileExtensionFilter = CommonValues.ImageExtensionFilter;

            makeImagesPane();

            FxmlTools.quickTooltip(selectButton, new Tooltip(AppVaribles.getMessage("ImagesMostTips")));
            setTips();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectImages(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            String defaultPath = AppVaribles.getConfigValue("LastPath", System.getProperty("user.home"));
            fileChooser.setInitialDirectory(new File(AppVaribles.getConfigValue("imageSourcePath", defaultPath)));
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            imageFileList = fileChooser.showOpenMultipleDialog(getMyStage());
            makeImagesPane();
            if (imageFileList == null || imageFileList.isEmpty()) {
                return;
            }
            String path = imageFileList.get(0).getParent();
            AppVaribles.setConfigValue("LastPath", path);
            AppVaribles.setConfigValue("imageSourcePath", path);

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void windowSize(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.windowSize(event);
        }
    }

    @FXML
    @Override
    public void originalSize(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.originalSize(event);
        }
    }

    @FXML
    @Override
    public void zoomIn(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.zoomIn(event);
        }

    }

    @FXML
    @Override
    public void zoomOut(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.zoomOut(event);
        }

    }

    @FXML
    @Override
    public void rotateLeft(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.rotateLeft(event);
        }

    }

    @FXML
    @Override
    public void rotateRight(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.rotateRight(event);
        }

    }

    @FXML
    @Override
    public void turnOver(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.turnOver(event);
        }

    }

    @FXML
    @Override
    public void back(ActionEvent event) {
        if (imageControllerList == null) {
            return;
        }
        for (ImageViewerIController c : imageControllerList) {
            c.back(event);
        }

    }

    private void makeImagesPane() {
        imagesPane.getChildren().clear();
        imagePaneList = new ArrayList();
        imageControllerList = new ArrayList();
        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        try {
            int num = imageFileList.size();
            if (num > 10) {
                num = 10;
            }
            int cols = num % 2 == 0 && num != 2 ? num / 2 : num / 2 + 1;
            int rows = num > 2 ? 2 : 1;

            HBox line = new HBox();
            for (int i = 0; i < num; i++) {
                if (i % cols == 0) {
                    line = new HBox();
                    line.setAlignment(Pos.CENTER);
                    line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    line.setSpacing(10);
                    imagesPane.getChildren().add(line);
                    VBox.setVgrow(line, Priority.ALWAYS);
                    HBox.setHgrow(line, Priority.ALWAYS);
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageViewerIFxml), AppVaribles.CurrentBundle);
                Pane imagePane = fxmlLoader.load();
                ImageViewerIController imageController = fxmlLoader.getController();
                imageController.loadImage(imageFileList.get(i), false);

                imagePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(imagePane, Priority.ALWAYS);
                HBox.setHgrow(imagePane, Priority.ALWAYS);
                line.getChildren().add(imagePane);
                imagePane.setPrefWidth(line.getWidth() / cols - 10);
                imagePane.setPrefHeight(imagesPane.getHeight() / 2 - 10);

                imagePaneList.add(imagePane);
                imageControllerList.add(imageController);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
