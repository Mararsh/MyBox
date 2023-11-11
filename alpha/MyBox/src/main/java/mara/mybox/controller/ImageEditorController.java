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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
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
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class ImageEditorController extends BaseImageController {

    protected TableImageEditHistory tableImageEditHistory;
    protected String imageHistoriesRootPath;
    protected File imageHistoriesPath;
    protected int historyIndex, hisSize;

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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(historyButton, new Tooltip(message("Histories") + "\nCTRL+H / ALT+H"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            imageChanged = false;
            historyButton.setDisable(sourceFile == null);
            updateLabel(message("Loaded"));

            recordImageHistory("Load", null, null, image);

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

    public void updateImage(String operation, Image newImage, long cost) {
        updateImage(operation, null, null, newImage, cost);
    }

    public void updateImage(String operation, String value, ImageScope opScope, Image newImage, long cost) {
        try {
            scope = opScope;
            recordImageHistory(operation, value, opScope, newImage);
            String info = operation == null ? "" : message(operation);
            if (value != null && !value.isBlank()) {
                info += ": " + value;
            }
            if (cost > 0) {
                info += "  " + message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
            }
            updateImage(newImage);
            popInformation(info);
            updateLabel(info);
            notifyLoad();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateLabel(String info) {
        imageLabel.setText(info);
    }

    protected void setHistoryIndex(int index) {
        historyIndex = index;
        undoButton.setDisable(historyIndex < 0 || historyIndex >= hisSize - 1);
        redoButton.setDisable(historyIndex <= 0);
    }

    protected void recordImageHistory(String op, String info, ImageScope scope, Image hisImage) {
        if (sourceFile == null || !UserConfig.getBoolean("ImageHistoriesRecord", true)) {
            hisSize = 0;
            setHistoryIndex(-1);
            return;
        }
        if (hisImage == null || op == null) {
            return;
        }
        if (Languages.matchIgnoreCase("Load", op)
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
                            .setOpType(message(op))
                            .setObjectType(info)
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
                        + prefix + "_" + (new Date().getTime()) + "_" + op;
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
                    updateImage("History", hisImage, cost);
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
    public List<MenuItem> operationsMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (sourceFile != null) {
                menu = new MenuItem(message("Undo") + "    Ctrl+Z " + message("Or") + " Alt+Z",
                        StyleTools.getIconImageView("iconUndo.png"));
                menu.setOnAction((ActionEvent event) -> {
                    undoAction();
                });
                menu.setDisable(undoButton.isDisabled());
                items.add(menu);

                menu = new MenuItem(message("Redo") + "    Ctrl+Y " + message("Or") + " Alt+Y",
                        StyleTools.getIconImageView("iconRedo.png"));
                menu.setOnAction((ActionEvent event) -> {
                    redoAction();
                });
                menu.setDisable(redoButton.isDisabled());
                items.add(menu);

                menu = new MenuItem(message("EditHistories"), StyleTools.getIconImageView("iconHistory.png"));
                menu.setOnAction((ActionEvent event) -> {
                    showHistories();
                });
                menu.setDisable(recoverButton.isDisabled());
                items.add(menu);

                items.add(new SeparatorMenuItem());
            }

            menu = new MenuItem(message("SelectScope") + "    Ctrl+T " + message("Or") + " Alt+T",
                    StyleTools.getIconImageView("iconTarget.png"));
            menu.setOnAction((ActionEvent event) -> {
                selectScope();
            });
            items.add(menu);

            items.add(copyMenu(fevent));

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

            menu = new MenuItem(message("Scale"), StyleTools.getIconImageView("iconExpand.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageScaleController.open(this);
            });
            items.add(menu);

            items.add(colorMenu(fevent));

            items.add(pixelsMenu(fevent));

            items.add(tranformMenu(fevent));

            menu = new MenuItem(message("Eraser"), StyleTools.getIconImageView("iconEraser.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageEraseController.open(this);
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Menu colorMenu(Event fevent) {
        try {
            Menu colorNenu = new Menu(message("Color"), StyleTools.getIconImageView("iconColor.png"));

            MenuItem menu = new MenuItem(message("ReplaceColor"), StyleTools.getIconImageView("iconReplace.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageReplaceColorController.open(this);
            });
            colorNenu.getItems().add(menu);

            menu = new MenuItem(message("BlendColor"), StyleTools.getIconImageView("iconCross.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageBlendColorController.open(this);
            });
            colorNenu.getItems().add(menu);

            menu = new MenuItem(message("AdjustColor"), StyleTools.getIconImageView("iconColorWheel.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageAdjustColorController.open(this);
            });
            colorNenu.getItems().add(menu);

            menu = new MenuItem(message("BlackOrWhite"), StyleTools.getIconImageView("iconBlackWhite.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageBlackWhiteController.open(this);
            });
            colorNenu.getItems().add(menu);

            menu = new MenuItem(message("Grey"), StyleTools.getIconImageView("iconGrey.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageGreyController.open(this);
            });
            colorNenu.getItems().add(menu);

            menu = new MenuItem(message("Sepia"), StyleTools.getIconImageView("iconSepia.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageSepiaController.open(this);
            });
            colorNenu.getItems().add(menu);

            menu = new MenuItem(message("ReduceColors"), StyleTools.getIconImageView("iconReduceColors.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageReduceColorsController.open(this);
            });
            colorNenu.getItems().add(menu);

            menu = new MenuItem(message("Thresholding"), StyleTools.getIconImageView("iconThresholding.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageThresholdingController.open(this);
            });
            colorNenu.getItems().add(menu);

            return colorNenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Menu pixelsMenu(Event fevent) {
        try {
            Menu pixelsNenu = new Menu(message("Pixels"), StyleTools.getIconImageView("iconMatrix.png"));

            MenuItem menu = new MenuItem(message("Mosaic"), StyleTools.getIconImageView("iconMosaic.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageMosaicController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            menu = new MenuItem(message("FrostedGlass"), StyleTools.getIconImageView("iconFrosted.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageGlassController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            menu = new MenuItem(message("Smooth"), StyleTools.getIconImageView("iconSmooth.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageSmoothController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            menu = new MenuItem(message("Sharpen"), StyleTools.getIconImageView("iconSharpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageSharpenController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            menu = new MenuItem(message("Contrast"), StyleTools.getIconImageView("iconGrey.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageBlackWhiteController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            menu = new MenuItem(message("EdgeDetection"), StyleTools.getIconImageView("iconEdgeDetection.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageEdgeController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            menu = new MenuItem(message("Emboss"), StyleTools.getIconImageView("iconEmboss.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageEmbossController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            menu = new MenuItem(message("Convolution"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageBlackWhiteController.open(this);
            });
            pixelsNenu.getItems().add(menu);

            return pixelsNenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Menu tranformMenu(Event fevent) {
        try {
            Menu tranformNenu = new Menu(message("Transform"), StyleTools.getIconImageView("iconRotateRight.png"));

            MenuItem menu = new MenuItem(message("RotateRight"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateRight();
            });
            tranformNenu.getItems().add(menu);

            menu = new MenuItem(message("RotateLeft"), StyleTools.getIconImageView("iconRotateLeft.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateLeft();
            });
            tranformNenu.getItems().add(menu);

            menu = new MenuItem(message("TurnOver"), StyleTools.getIconImageView("iconTurnOver.png"));
            menu.setOnAction((ActionEvent event) -> {
                turnOver();
            });
            tranformNenu.getItems().add(menu);

            menu = new MenuItem(message("Rotate"), StyleTools.getIconImageView("iconReplace.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageRotateController.open(this);
            });
            tranformNenu.getItems().add(menu);
            tranformNenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Shear"), StyleTools.getIconImageView("iconShear.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageShearController.open(this);
            });
            tranformNenu.getItems().add(menu);

            menu = new MenuItem(message("MirrorHorizontal"), StyleTools.getIconImageView("iconHorizontal.png"));
            menu.setOnAction((ActionEvent event) -> {
                horizontalAction();
            });
            tranformNenu.getItems().add(menu);

            menu = new MenuItem(message("MirrorVertical"), StyleTools.getIconImageView("iconVertical.png"));
            menu.setOnAction((ActionEvent event) -> {
                verticalAction();
            });
            tranformNenu.getItems().add(menu);

            return tranformNenu;
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
        updateImage("Recover", image, -1);
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
        if (imageView.getImage() == null) {
            loadContentInSystemClipboard();
        } else {
            ImagePasteController.open(this);
        }
    }

    @FXML
    public void horizontalAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.horizontalImage(imageView.getImage());
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                updateImage("MirrorHorizontal", newImage, cost);
            }

        };
        start(task);
    }

    @FXML
    public void verticalAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = TransformTools.verticalImage(imageView.getImage());
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                updateImage("MirrorVertical", newImage, cost);
            }

        };
        start(task);
    }

    public void applyKernel(ConvolutionKernel kernel) {
        // #####
    }

    @Override
    protected void popContextMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageEditController.editMenu(this, x, y);
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (imageView.getImage() == null || !imageChanged) {
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

    @Override
    public boolean controlAltH() {
        showHistories();
        return true;
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
