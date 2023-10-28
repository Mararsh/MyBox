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
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
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
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends BaseImageController {

    protected ImageScope scope;

    @FXML
    protected TitledPane viewPane, browsePane;
    @FXML
    protected VBox panesBox, contentBox;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected ControlFileBackup backupController;

    public ImageViewerController() {
        baseTitle = message("ImageViewer");
        TipsLabelKey = "ImageViewerTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initFilePane();
            initViewPane();
            initEditPane();
            initBrowsePane();

            if (imageView != null && rightPane != null) {
                rightPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initFilePane() {
        try {
            if (saveButton != null && imageView != null) {
                saveButton.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initViewPane() {
        try {
            if (viewPane != null) {
                if (imageView != null) {
                    viewPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                viewPane.setExpanded(UserConfig.getBoolean(baseName + "ViewPane", false));
                viewPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "ViewPane", viewPane.isExpanded());
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initBrowsePane() {
        try {
            if (browsePane != null) {
                if (imageView != null) {
                    browsePane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                browsePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "BrowsePane", browsePane.isExpanded());
                });
                browsePane.setExpanded(UserConfig.getBoolean(baseName + "BrowsePane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initEditPane() {
        try {
            if (imageView == null) {
                return;
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cropAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image areaImage;

            @Override
            protected boolean handle() {
                areaImage = imageToHandle();
                return areaImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageView.setImage(areaImage);
                setImageChanged(true);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        boolean sizeChanged = imageWidth() != image.getWidth()
                || imageHeight() != image.getHeight();
        imageView.setImage(image);
        if (sizeChanged) {
            redrawMaskShape();
        }
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

            @Override
            protected boolean handle() {
                savedImage = imageToHandle();
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(savedImage, null);
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                if (backupController != null && backupController.needBackup() && srcFile != null) {
                    backupController.addBackup(task, srcFile);
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
                        openFile(sourceFile);
                    } else {
                        sourceFileChanged(sourceFile);
                    }
                } else {
                    image = savedImage;
                    imageView.setImage(image);
                    popInformation(sourceFile + "   " + message("Saved"));
                    setImageChanged(false);
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

    public boolean scopeWhole() {
        return scope == null || scope.getScopeType() == null;
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
                ImageSelectScopeController.open(this);
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

    /*
        static methods
     */
    public static ImageViewerController open() {
        try {
            ImageViewerController controller = (ImageViewerController) WindowTools.openStage(Fxmls.ImageViewerFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageViewerController openFile(File file) {
        try {
            ImageViewerController controller = open();
            if (controller != null && file != null) {
                controller.loadImageFile(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageViewerController openImage(Image image) {
        ImageViewerController controller = open();
        if (controller != null) {
            controller.loadImage(image);
        }
        return controller;
    }

    public static ImageViewerController openImageInfo(ImageInformation info) {
        ImageViewerController controller = open();
        if (controller != null) {
            controller.loadImageInfo(info);
        }
        return controller;
    }

}
