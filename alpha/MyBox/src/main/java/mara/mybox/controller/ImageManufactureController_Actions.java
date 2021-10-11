package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
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
import mara.mybox.db.data.FileBackup;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController_Actions extends ImageManufactureController_Histories {

    protected void initEditBar() {
        try {
            redoButton.setDisable(true);
            undoButton.setDisable(true);
            recoverButton.disableProperty().bind(undoButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
    public void editFrames() {
        loadMultipleFramesImage(sourceFile);
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
    protected void popImage() {
        if (imagePopController != null && imagePopController.getMyWindow().isShowing()) {
            return;
        }
        imagePopController = ImagePopController.openView(this, imageView);
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
        if (!UserConfig.getBoolean(baseName + "ContextMenu", true)
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageManufactureController.open((ImageManufactureController) this, x, y);
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
                MenuImageManufactureController.open((ImageManufactureController) this, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == scopeTab) {
                scopeController.popMenu();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public void popHistory() {
        synchronized (this) {
            ImageEditHistory selected = historiesList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            SingletonTask viewTask = new SingletonTask<Void>() {
                private Image hisImage;

                @Override
                protected boolean handle() {
                    hisImage = hisImage(selected);
                    return hisImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    ImagePopController.openImage(myController, hisImage);
                }

            };
            start(viewTask);
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                ImagePopController.openView(myController, imageView);
                return true;

            } else if (tab == scopeTab) {
                ImageScopePopController.open(scopeController);
                return true;

            } else if (tab == hisTab) {
                popHistory();
                return true;

            } else if (tab == backupTab) {
                popBackup();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public void popBackup() {
        synchronized (this) {
            FileBackup selected = backupController.selectedBackup();
            if (selected == null) {
                return;
            }
            File file = selected.getBackup();
            if (file == null) {
                return;
            }
            SingletonTask viewTask = new SingletonTask<Void>() {
                private Image backImage;

                @Override
                protected boolean handle() {
                    try {
                        if (!file.exists()) {
                            TableFileBackup.deleteBackup(selected);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    backupController.backupsList.getItems().remove(selected);
                                }
                            });
                            return false;
                        }
                        BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                        if (bufferedImage != null) {
                            backImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        }
                        return backImage != null;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    ImagePopController.openImage(myController, backImage);
                }

            };
            start(viewTask);
        }
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
        if (result.get() == buttonSave) {
            saveAction();
            return true;
        } else if (result.get() == buttonNotSave) {
            return true;
        } else if (result.get() == buttonSaveAs) {
            saveAsAction();
            return true;
        } else {
            return false;
        }

    }

}
