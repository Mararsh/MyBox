package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileExtensions;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class ImageEditorController extends BaseShapeController {

    protected SimpleBooleanProperty imageLoaded;
    protected ImageOperation operation;
    protected TableImageEditHistory tableImageEditHistory;
    protected String imageHistoriesRootPath;
    protected File imageHistoriesPath;
    protected int historyIndex, hisSize;

    public static enum ImageOperation {
        Load, History, Saved, Recover, Clipboard, Paste, Arc, Color, Crop, Copy,
        Text, RichText, Convolution,
        Effects, Enhancement, Shadow, ScaleImage, Picture, Transform, Shape, Eliminate, Margins
    }

    @FXML
    protected Button historyButton, viewImageButton;

    public ImageEditorController() {
        baseTitle = message("EditImage");
        TipsLabelKey = "ImageManufactureTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            imageLoaded = new SimpleBooleanProperty(false);
            tableImageEditHistory = new TableImageEditHistory();
            imageHistoriesRootPath = AppPaths.getImageHisPath();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            redoButton.setDisable(true);
            undoButton.setDisable(true);
            recoverButton.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {

            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            if (sourceFile == null) {
                saveAsTmp();
                return true;
            }
            imageLoaded.set(true);
            imageChanged = false;
            resetImagePane();

            historyButton.setDisable(sourceFile == null);

            finalRefineView();

            updateLabelString(message("Loaded"));

            recordImageHistory(ImageOperation.Load, null, image);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean saveAsTmp() {
        if (image == null) {
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        File tmpFile = FileTmpTools.generateFile("png");
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage((Image) image, null);
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                return ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile.getAbsolutePath());
            }

            @Override
            protected void whenSucceeded() {
                sourceFileChanged(tmpFile);
            }
        };
        start(task);
        return true;
    }

    public void resetImagePane() {
        operation = null;
        scope = null;
        imageLabel.setText("");

        imageView.setRotate(0);
        imageView.setVisible(true);

        resetShapeOptions();

        clearMask();
    }

    public void adjustRightPane() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    rightPane.setHvalue(0);
                    rightPane.setVvalue(0);
                });
            }
        }, 500);
    }

    public void updateImage(ImageOperation operation, Image newImage) {
        updateImage(operation, null, newImage, -1);
    }

    public void updateImage(ImageOperation operation, Image newImage, long cost) {
        updateImage(operation, null, newImage, cost);
    }

    public void updateImage(ImageOperation operation, ImageScope scope, Image newImage, long cost) {
        try {
            recordImageHistory(operation, scope, newImage);
            String info = operation == null ? "" : message(operation.name());
            if (cost > 0) {
                info += "  " + message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
            }
            updateImage(newImage, info);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateImage(Image newImage, String info) {
        try {
            updateImage(newImage);
            resetImagePane();
            popInformation(info);
            updateLabelString(info);
            notifyLoad();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    // Only update image and not reset image pane
    public void setImage(ImageOperation operation, Image newImage) {
        try {
            updateImage(newImage);
            recordImageHistory(operation, null, newImage);
            updateLabelsTitle();
            updateLabel(operation);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateLabel(ImageOperation operation) {
        updateLabelString(operation != null ? message(operation.name()) : null);
    }

    public void updateLabelString(String info) {
        imageLabel.setText(info);
    }

    protected void setHistoryIndex(int index) {
        historyIndex = index;
        undoButton.setDisable(historyIndex < 0 || historyIndex >= hisSize - 1);
        redoButton.setDisable(historyIndex <= 0);
    }

    protected void recordImageHistory(ImageOperation operation, ImageScope scope, Image hisImage) {
        if (sourceFile == null || !UserConfig.getBoolean("ImageHistoriesRecord", true)) {
            hisSize = 0;
            setHistoryIndex(-1);
            return;
        }
        if (operation == null || hisImage == null) {
            return;
        }
        if (operation == ImageOperation.Load
                && !UserConfig.getBoolean("ImageHistoriesRecordLoading", true)) {
            return;
        }
        SingletonTask recordTask = new SingletonTask<Void>(this) {
            private File currentFile;
            private ImageEditHistory his;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    currentFile = sourceFile;
                    if (imageHistoriesPath == null) {
                        imageHistoriesPath = tableImageEditHistory.path(conn, currentFile);
                        if (imageHistoriesPath == null) {
                            String fname = currentFile.getName();
                            String subPath = FileNameTools.prefix(fname) + FileNameTools.suffix(fname);
                            imageHistoriesPath = new File(imageHistoriesRootPath + File.separator
                                    + subPath + (new Date()).getTime());
                        }
                    }
                    writeRecord(conn);
                    hisSize = tableImageEditHistory.count(conn, currentFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            private boolean writeRecord(Connection conn) {
                try {
                    BufferedImage bufferedImage = FxImageTools.toBufferedImage(hisImage);
                    if (isCancelled()) {
                        return false;
                    }
                    String hisname = makeHisName();
                    while (new File(hisname).exists()) {
                        hisname = makeHisName();
                    }
                    File hisFile = new File(hisname + ".png");
                    if (!ImageFileWriters.writeImageFile(bufferedImage, "png", hisFile.getAbsolutePath())) {
                        return false;
                    }
                    if (isCancelled()) {
                        return false;
                    }
                    File thumbFile = new File(hisname + "_thumbnail.png");
                    BufferedImage thumb = ScaleTools.scaleImageWidthKeep(bufferedImage, AppVariables.thumbnailWidth);
                    if (!ImageFileWriters.writeImageFile(thumb, "png", thumbFile.getAbsolutePath())) {
                        return false;
                    }
                    his = ImageEditHistory.create()
                            .setImageFile(currentFile)
                            .setHistoryFile(hisFile)
                            .setThumbnailFile(thumbFile)
                            .setThumbnail(SwingFXUtils.toFXImage(thumb, null))
                            .setUpdateType(operation.name())
                            .setOperationTime(new Date());
                    if (scope != null) {
                        if (scope.getScopeType() != null) {
                            his.setScopeType(scope.getScopeType().name());
                        }
                        if (scope.getName() != null) {
                            his.setScopeName(scope.getName());
                        }
                    }
                    his = tableImageEditHistory.insertData(conn, his);
                    return his != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            private String makeHisName() {
                String prefix = FileNameTools.prefix(currentFile.getName());
                if (framesNumber > 1) {
                    prefix += "-frame" + frameIndex;
                }
                String name = imageHistoriesPath.getAbsolutePath() + File.separator
                        + prefix + "_" + (new Date().getTime()) + "_" + operation;
                name += "_" + new Random().nextInt(1000);
                return name;
            }

            @Override
            protected void whenSucceeded() {
                if (his != null) {
                    if (currentFile.equals(sourceFile)) {
                        setHistoryIndex(0);
                    }
                    ImageHistoriesController.updateList(currentFile);
                }
            }

            @Override
            protected void whenFailed() {
                MyBoxLog.console(error);
            }

        };
        start(recordTask, false);
    }

    protected void loadImageHistory(int index) {
        if (sourceFile == null) {
            popError(message("InvalidData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image hisImage;
            private ImageEditHistory his;

            @Override

            protected boolean handle() {
                hisSize = 0;
                his = null;
                hisImage = null;
                try (Connection conn = DerbyBase.getConnection()) {
                    List<ImageEditHistory> records = tableImageEditHistory.read(conn, sourceFile);
                    if (records != null) {
                        hisSize = records.size();
                        if (hisSize > 0) {
                            int vindex = index;
                            if (vindex < 0) {
                                vindex = 0;
                            }
                            if (vindex >= hisSize) {
                                vindex = hisSize - 1;
                            }
                            his = records.get(vindex);
                            hisImage = his.historyImage();
                        }
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (hisImage == null) {
                    setHistoryIndex(-1);
                } else {
                    String info = MessageFormat.format(message("CurrentImageSetAs"),
                            DateTools.datetimeToString(his.getOperationTime()) + " " + his.getDesc());
                    popInformation(info);
                    updateImage(hisImage, message("History"));
                    setHistoryIndex(index);
                }
            }

        };
        start(task, message("loadImageHistory"));
    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        super.setImageChanged(imageChanged);
        recoverButton.setDisable(!imageChanged);
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Save"), StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                saveAction();
            });
            items.add(menu);

            if (sourceFile != null) {
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

            menu = new MenuItem(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Crop"), StyleTools.getIconImageView("iconCrop.png"));
            menu.setOnAction((ActionEvent event) -> {
                cropAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Paste"), StyleTools.getIconImageView("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                pasteAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Margins"), StyleTools.getIconImageView("iconRectangle.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageMarginsController.open(this);
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
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        ImageCanvasInputController controller = ImageCanvasInputController.open(this, baseTitle);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                Image canvas = controller.getCanvas();
                if (canvas != null) {
                    loadImage(canvas);
                }
                controller.close();
            }
        });
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
        popInformation(message("Recovered"));
    }

    @FXML
    public void showHistories() {
        ImageHistoriesController.open(this);
    }

    @FXML
    @Override
    public void cropAction() {
        ImageCropController.open(this);
    }

    @FXML
    @Override
    public void pasteAction() {
        ImagePasteController.open(this);
    }

    public void applyKernel(ConvolutionKernel kernel) {
        // #####
    }

    @Override
    protected void popImageMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
//        MenuImageManufactureController.manufactureMenu((ImageManufactureController) this, x, y);
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!imageLoaded.get() || !imageChanged) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(message("ImageChanged"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSave = new ButtonType(message("Save"));
        ButtonType buttonSaveAs = new ButtonType(message("SaveAs"));
        ButtonType buttonNotSave = new ButtonType(message("NotSave"));
        ButtonType buttonCancel = new ButtonType(message("Cancel"));
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

    /*
        static methods
     */
    public static ImageEditorController open() {
        try {
            ImageEditorController controller = (ImageEditorController) WindowTools.openStage(Fxmls.ImageEditorFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageEditorController openImage(Image image) {
        ImageEditorController controller = open();
        if (controller != null) {
            controller.loadImage(image);
        }
        return controller;
    }

    public static ImageEditorController openFile(File file) {
        ImageEditorController controller = open();
        if (controller != null) {
            controller.loadImageFile(file);
        }
        return controller;
    }

    public static ImageEditorController openImageInfo(ImageInformation imageInfo) {
        ImageEditorController controller = open();
        if (controller != null) {
            controller.loadImageInfo(imageInfo);
        }
        return controller;
    }

    public static ImageEditorController open(File file, ImageInformation imageInfo) {
        ImageEditorController controller = open();
        if (controller != null) {
            controller.loadImage(file, imageInfo);
        }
        return controller;
    }

}
