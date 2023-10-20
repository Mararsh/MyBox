package mara.mybox.controller;

import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController_Actions extends ImageManufactureController_Image {

    protected void initEditBar() {
        try {
            redoButton.setDisable(true);
            undoButton.setDisable(true);
            recoverButton.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initCreatePane() {
        try {
            createPane.setExpanded(UserConfig.getBoolean("ImageManufactureNewPane", true));
            createPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean("ImageManufactureNewPane", createPane.isExpanded());
            });

            newWidthInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                newWidth = v;
                                newWidthInput.setStyle(null);
                            } else {
                                newWidthInput.setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            newWidthInput.setStyle(UserConfig.badStyle());
                        }
                    });
            newHeightInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                newHeight = v;
                                newHeightInput.setStyle(null);
                            } else {
                                newHeightInput.setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            newHeightInput.setStyle(UserConfig.badStyle());
                        }
                    });
            colorSetController.init(this, baseName + "NewBackgroundColor");

            newWidthInput.setText("500");
            newHeightInput.setText("500");

            createButton.disableProperty().bind(
                    newWidthInput.styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(newHeightInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        Image newImage = FxImageTools.createImage(newWidth, newHeight, (Color) colorSetController.rect.getFill());
        loadImage(newImage);

        operationsController.marginsPane.setExpanded(true);
    }

    @FXML
    @Override
    public void undoAction() {
        if (undoButton.isDisabled()) {
            return;
        }
        loadImageHistory(historyIndex + 1);
    }

    @FXML
    @Override
    public void redoAction() {
        if (redoButton.isDisabled()) {
            return;
        }
        loadImageHistory(historyIndex - 1);
    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        updateImage(ImageOperation.Recover, image);
        setImageChanged(false);
        popInformation(Languages.message("Recovered"));
    }

    @FXML
    @Override
    public void cropAction() {
        if (operationsController.cropPane.isExpanded()) {
            operationsController.cropController.okAction();
        } else {
            operationsController.cropPane.setExpanded(true);
        }
    }

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        if (operationsController.copyPane.isExpanded()) {
            operationsController.copyController.okAction();
        } else {
            operationsController.copyPane.setExpanded(true);
        }
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        operationsController.clipboardPane.setExpanded(true);
        operationsController.clipboardController.pasteImageInSystemClipboard();
    }

    @FXML
    public void popImage() {
        ImagePopController.openView(this, imageView);
    }

    public void applyKernel(ConvolutionKernel kernel) {
        operationsController.enhancementPane.setExpanded(true);
        operationsController.enhancementController.optionsController.applyKernel(kernel);
    }

    @FXML
    @Override
    public void okAction() {
        operationsController.okAction();
    }

    @Override
    protected void popImageMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageManufactureController.manufactureMenu((ImageManufactureController) this, x, y);
    }

    @FXML
    @Override
    public void zoomOut() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == imageTab) {
            super.zoomOut();

        } else if (tab == scopeTab) {
            scopeController.zoomOut();

        }
    }

    @FXML
    @Override
    public void zoomIn() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == imageTab) {
            super.zoomIn();

        } else if (tab == scopeTab) {
            scopeController.zoomIn();

        }
    }

    @FXML
    @Override
    public void paneSize() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == imageTab) {
            super.paneSize();

        } else if (tab == scopeTab) {
            scopeController.paneSize();

        }
    }

    @FXML
    @Override
    public void loadedSize() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == imageTab) {
            super.loadedSize();

        } else if (tab == scopeTab) {
            scopeController.loadedSize();

        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
                MenuImageManufactureController.manufactureMenu((ImageManufactureController) this, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == scopeTab) {
                scopeController.menuAction();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                popImage();
                return true;

            } else if (tab == scopeTab) {
                scopeController.popAction();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!imageLoaded.get() || !imageChanged) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(Languages.message("ImageChanged"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSave = new ButtonType(Languages.message("Save"));
        ButtonType buttonSaveAs = new ButtonType(Languages.message("SaveAs"));
        ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
        ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonNotSave, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result == null || !result.isPresent()) {
            return false;
        }
        if (result.get() == buttonSave) {
            saveAction();
            return true;
        } else if (result.get() == buttonNotSave) {
            imageChanged = false;
            return true;
        } else if (result.get() == buttonSaveAs) {
            saveAsAction();
            return true;
        } else {
            return false;
        }

    }

}
