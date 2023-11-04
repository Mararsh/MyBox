package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.db.data.FileBackup;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.FileExtensions;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @License Apache License Version 2.0
 *
 * BaseImageController < BaseImageController_Actions < BaseImageController_Image
 * < BaseImageController_MouseEvents < BaseImageController_Shapes <
 * BaseImageController_Mask < BaseImageController_ImageView
 */
public class BaseImageController extends BaseImageController_Actions {

    @FXML
    protected FlowPane buttonsPane;

    public BaseImageController() {
        baseTitle = message("ImageViewer");
        TipsLabelKey = "ImageViewerTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            isPickingColor = imageChanged = false;
            loadWidth = defaultLoadWidth = -1;
            frameIndex = framesNumber = 0;
            sizeChangeAware = 1;
            zoomStep = xZoomStep = yZoomStep = 40;
            if (maskPane != null) {
                if (borderLine == null) {
                    borderLine = new Rectangle();
                    borderLine.setFill(Color.web("#ffffff00"));
                    borderLine.setStroke(Color.web("#cccccc"));
                    borderLine.setArcWidth(5);
                    borderLine.setArcHeight(5);
                    maskPane.getChildren().add(borderLine);
                }
                if (sizeText == null) {
                    sizeText = new Text();
                    sizeText.setFill(Color.web("#cccccc"));
                    sizeText.setStrokeWidth(0);
                    maskPane.getChildren().add(sizeText);
                }
                if (xyText == null) {
                    xyText = new Text();
                    maskPane.getChildren().add(xyText);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            if (zoomStepSelector != null) {
                NodeStyleTools.setTooltip(zoomStepSelector, new Tooltip(message("ZoomStep")));
            }
            if (pickColorCheck != null) {
                NodeStyleTools.setTooltip(pickColorCheck, new Tooltip(message("PickColor") + "\nCTRL+k"));
            }
            if (loadWidthSelector != null) {
                NodeStyleTools.setTooltip(loadWidthSelector, new Tooltip(message("ImageLoadWidthCommnets")));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageView();
            initMaskPane();
            initCheckboxs();

            if (imageBox != null && imageView != null) {
                imageBox.disableProperty().bind(imageView.imageProperty().isNull());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initImageView() {
        if (imageView == null) {
            return;
        }
        try {
            imageView.setPreserveRatio(true);

            imageView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                }
            });

            imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));

                }
            });

            if (scrollPane != null) {
                scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        paneSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                    }
                });
                scrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        paneSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                    }
                });

            }

            zoomStep = UserConfig.getInt(baseName + "ZoomStep", 40);
            zoomStep = zoomStep <= 0 ? 40 : zoomStep;
            xZoomStep = zoomStep;
            yZoomStep = zoomStep;
            if (zoomStepSelector != null) {
                zoomStepSelector.getItems().addAll(
                        Arrays.asList("40", "20", "5", "1", "3", "15", "30", "50", "80", "100", "150", "200", "300", "500")
                );
                zoomStepSelector.setValue(zoomStep + "");
                zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                        try {
                            int v = Integer.parseInt(newVal);
                            if (v > 0) {
                                zoomStep = v;
                                UserConfig.setInt(baseName + "ZoomStep", zoomStep);
                                zoomStepSelector.getEditor().setStyle(null);
                                xZoomStep = zoomStep;
                                yZoomStep = zoomStep;
                                zoomStepChanged();
                            } else {
                                zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

            loadWidth = defaultLoadWidth;
            if (loadWidthSelector != null) {
                List<String> values = Arrays.asList(message("OriginalSize"),
                        "512", "1024", "256", "128", "2048", "100", "80", "4096");
                loadWidthSelector.getItems().addAll(values);
                int v = UserConfig.getInt(baseName + "LoadWidth", defaultLoadWidth);
                if (v <= 0) {
                    loadWidth = -1;
                    loadWidthSelector.getSelectionModel().select(0);
                } else {
                    loadWidth = v;
                    loadWidthSelector.setValue(v + "");
                }
                loadWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (message("OriginalSize").equals(newValue)) {
                            loadWidth = -1;
                        } else {
                            try {
                                loadWidth = Integer.parseInt(newValue);
                            } catch (Exception e) {
                                ValidationTools.setEditorBadStyle(loadWidthSelector);
                                return;
                            }
                        }
                        ValidationTools.setEditorNormal(loadWidthSelector);
                        setLoadWidth();
                    }
                });
            }

            if (imageView == null) {
                return;
            }
            if (rightPane != null) {
                rightPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (buttonsPane != null) {
                buttonsPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (tabPane != null) {
                tabPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (scrollPane != null) {
                scrollPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

            if (saveButton != null) {
                saveButton.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cropAction() {

    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        imageView.setImage(image);
        setImageChanged(false);
        popInformation(message("Recovered"));
    }

    @FXML
    @Override
    public void saveAction() {
        if (imageView == null || imageView.getImage() == null
                || (saveButton != null && saveButton.isDisabled())) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        File srcFile = imageFile();
        if (srcFile == null) {
            targetFile = chooseSaveFile();
            if (targetFile == null) {
                return;
            }
        } else {
            targetFile = srcFile;
        }
        if (imageInformation != null && imageInformation.isIsScaled()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("SureSaveScaled"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAsAction();
                return;
            }
        }

        task = new SingletonCurrentTask<Void>(this) {

            private Image savedImage;
            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                savedImage = imageView.getImage();
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(savedImage, null);
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                needBackup = srcFile != null && UserConfig.getBoolean(baseName + "BackupWhenSave", false);
                if (needBackup) {
                    backup = addBackup(task, srcFile);
                }
                String format = FileNameTools.suffix(targetFile.getName());
                if (framesNumber > 1) {
                    error = ImageFileWriters.writeFrame(targetFile, frameIndex, bufferedImage, targetFile, null);
                    ok = error == null;
                } else {
                    ok = ImageFileWriters.writeImageFile(bufferedImage, format, targetFile.getAbsolutePath());
                }
                if (!ok || task == null || isCancelled()) {
                    return false;
                }
                ImageFileInformation finfo = ImageFileInformation.create(targetFile);
                if (finfo == null || finfo.getImageInformation() == null) {
                    return false;
                }
                imageInformation = finfo.getImageInformation();
                return true;
            }

            @Override
            protected void whenSucceeded() {
                sourceFile = targetFile;
                recordFileWritten(sourceFile);
                if (srcFile == null) {
                    if (savedImage != imageView.getImage()) {
                        ImageViewerController.openFile(sourceFile);
                    } else {
                        sourceFileChanged(sourceFile);
                    }
                } else {
                    image = savedImage;
                    imageView.setImage(image);
                    setImageChanged(false);
                    if (needBackup) {
                        if (backup != null && backup.getBackup() != null) {
                            popInformation(message("SavedAndBacked"));
                            FileBackupController.updateList(sourceFile);
                        } else {
                            popError(message("FailBackup"));
                        }
                    } else {
                        popInformation(sourceFile + "   " + message("Saved"));
                    }
                }
            }

        };
        start(task);
    }

    @Override
    protected void popImageMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageViewController.imageViewMenu(this, x, y);
    }

    @FXML
    @Override
    public boolean menuAction() {
        Point2D localToScreen = scrollPane.localToScreen(scrollPane.getWidth() - 80, 80);
        MenuImageViewController.imageViewMenu(this, localToScreen.getX(), localToScreen.getY());
        return true;
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (sourceFile != null) {
                menu = new MenuItem(message("Save"), StyleTools.getIconImageView("iconSave.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    saveAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Recover"), StyleTools.getIconImageView("iconRecover.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    recoverAction();
                });
                items.add(menu);

            }

            menu = new MenuItem(message("SaveAs"), StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageConverterController.open(this);
            });
            items.add(menu);

            if (sourceFile != null) {
                CheckMenuItem backItem = new CheckMenuItem(message("BackupWhenSave"));
                backItem.setSelected(UserConfig.getBoolean(baseName + "BackupWhenSave", false));
                backItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean(baseName + "BackupWhenSave", backItem.isSelected());
                    }
                });
                items.add(backItem);

                menu = new MenuItem(message("FileBackups"), StyleTools.getIconImageView("iconBackup.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    FileBackupController.load(this);
                });
                items.add(menu);
            } else {
                return items;
            }

            String fileFormat = FileNameTools.suffix(sourceFile.getName()).toLowerCase();
            if (FileExtensions.MultiFramesImages.contains(fileFormat)) {
                menu = new MenuItem(message("Frames"), StyleTools.getIconImageView("iconFrame.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    ImageFramesController.open(this);
                });
                items.add(menu);
            }
            items.add(new SeparatorMenuItem());

            if (imageInformation != null) {
                menu = new MenuItem(message("Information"), StyleTools.getIconImageView("iconInfo.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);

                menu = new MenuItem(message("MetaData"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    metaAction();
                });
                items.add(menu);

            }

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("ImagesBrowser"), StyleTools.getIconImageView("iconBrowse.png"));
            menu.setOnAction((ActionEvent event) -> {
                browseAction();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent event) -> {
                renameAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent event) -> {
                deleteAction();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("SelectScope"), StyleTools.getIconImageView("iconTarget.png"));
            menu.setOnAction((ActionEvent event) -> {
                selectScope();
            });
            items.add(menu);

            menu = new MenuItem(message("RotateRight"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateRight();
            });
            items.add(menu);

            menu = new MenuItem(message("RotateLeft"), StyleTools.getIconImageView("iconRotateLeft.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateLeft();
            });
            items.add(menu);

            menu = new MenuItem(message("TurnOver"), StyleTools.getIconImageView("iconTurnOver.png"));
            menu.setOnAction((ActionEvent event) -> {
                turnOver();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("RotateRight"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateRight();
            });
            items.add(menu);

            menu = new MenuItem(message("RotateRight"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateRight();
            });
            items.add(menu);

            menu = new MenuItem(message("RotateLeft"), StyleTools.getIconImageView("iconRotateLeft.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateLeft();
            });
            items.add(menu);

            menu = new MenuItem(message("TurnOver"), StyleTools.getIconImageView("iconTurnOver.png"));
            menu.setOnAction((ActionEvent event) -> {
                turnOver();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void selectScope() {
        ImageSelectScopeController.open(this);
    }

    @FXML
    public void renameAction() {
        try {
            if (imageChanged) {
                saveAction();
            }
            if (sourceFile == null) {
                return;
            }
            FileRenameController controller = (FileRenameController) openStage(Fxmls.FileRenameFxml);
            controller.set(sourceFile);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(newFile);
                });
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    public void fileRenamed(File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            popSuccessful();
            sourceFile = newFile;
            recordFileOpened(sourceFile);
            if (imageInformation != null) {
                imageInformation.setFile(sourceFile);
            }
            updateLabelsTitle();
            if (browseController != null) {
                browseController.setCurrentFile(sourceFile);
            }
            notifyLoad();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        if (sourceFile == null) {
            return;
        }
        if (!PopTools.askSure(getTitle(), message("Delete"), sourceFile.getAbsolutePath())) {
            return;
        }
        File focusFile = nextFile();
        if (focusFile == null) {
            focusFile = previousFile();
        }
        if (FileDeleteTools.delete(sourceFile)) {
            popSuccessful();
            sourceFile = null;
            image = null;
            imageView.setImage(null);
            if (focusFile != null) {
                sourceFileChanged(focusFile);
            }
        } else {
            popFailed();
        }
    }

    @Override
    public boolean controlAltK() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        if (pickColorCheck != null) {
            pickColorCheck.setSelected(!pickColorCheck.isSelected());
            return true;
        } else if (imageView != null && imageView.getImage() != null) {
            isPickingColor = !isPickingColor;
            checkPickingColor();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAltT() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        selectScope();
        return true;
    }

    @Override
    public boolean controlAlt1() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        loadedSize();
        return true;
    }

    @Override
    public boolean controlAlt2() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        paneSize();
        return true;
    }

    @Override
    public boolean controlAlt3() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        zoomIn();
        return true;
    }

    @Override
    public boolean controlAlt4() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        zoomOut();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            if (loadTask != null) {
                loadTask.cancel();
                loadTask = null;
            }
            if (paletteController != null) {
                paletteController.closeStage();
                paletteController = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
