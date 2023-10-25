package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController_Image extends ImageViewerController {

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
    protected Tab imageTab, scopeTab;
    @FXML
    protected ImageView maskView;
    @FXML
    protected ImageManufactureOperationsController operationsController;
    @FXML
    protected ImageManufactureScopeController scopeController;
    @FXML
    protected Button historyButton, viewImageButton;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableImageEditHistory = new TableImageEditHistory();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void refinePane() {
        super.refinePane();
        maskView.setFitWidth(imageView.getFitWidth());
        maskView.setFitHeight(imageView.getFitHeight());
        maskView.setLayoutX(imageView.getLayoutX());
        maskView.setLayoutY(imageView.getLayoutY());
    }

    public void resetImagePane() {
        operation = null;
        scope = null;
        infoLabel.setText("");

        imageView.setRotate(0);
        imageView.setVisible(true);
        maskView.setImage(null);
        maskView.setVisible(false);
        maskView.toBack();

        resetShapeOptions();

        clearMask();
    }

    public void imageTab() {
        tabPane.getSelectionModel().select(imageTab);
    }

    public void scopeTab() {
        tabPane.getSelectionModel().select(scopeTab);
    }

    public boolean isImageTabSelected() {
        return tabPane.getSelectionModel().getSelectedItem() == imageTab;
    }

    public boolean isScopeTabSelected() {
        return tabPane.getSelectionModel().getSelectedItem() == scopeTab;
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

    @Override
    protected void zoomStepChanged() {
        xZoomStep = zoomStep;
        yZoomStep = zoomStep;
        scopeController.zoomStep = zoomStep;
        scopeController.xZoomStep = zoomStep;
        scopeController.yZoomStep = zoomStep;
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!operationsController.keyEventsFilter(event)) {  // handle operation pane at first
            if (!super.keyEventsFilter(event)) {
                return scopeController.keyEventsFilter(event);
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean controlAltK() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                return super.controlAltK();

            } else if (tab == scopeTab) {
                return scopeController.controlAltK();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @Override
    public boolean controlAlt1() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                return super.controlAlt1();

            } else if (tab == scopeTab) {
                return scopeController.controlAlt1();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @Override
    public boolean controlAlt2() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                return super.controlAlt2();

            } else if (tab == scopeTab) {
                return scopeController.controlAlt2();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @Override
    public boolean controlAlt3() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                return super.controlAlt3();

            } else if (tab == scopeTab) {
                return scopeController.controlAlt3();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @Override
    public boolean controlAlt4() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                return super.controlAlt4();

            } else if (tab == scopeTab) {
                return scopeController.controlAlt4();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        super.paneClicked(event, p);
        operationsController.paneClicked(event, p);
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        super.mousePressed(event);
        operationsController.mousePressed(event);
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        super.mouseDragged(event);
        operationsController.mouseDragged(event);
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        super.mouseReleased(event);
        operationsController.mouseReleased(event);
    }

    public void updateImage(ImageManufactureController_Image.ImageOperation operation, Image newImage) {
        updateImage(operation, null, null, newImage, -1);
    }

    public void updateImage(ImageManufactureController_Image.ImageOperation operation, Image newImage, long cost) {
        updateImage(operation, null, null, newImage, cost);
    }

    public void updateImage(ImageManufactureController_Image.ImageOperation operation, String objectType, String opType, Image newImage, long cost) {
        try {
            recordImageHistory(operation, objectType, opType, newImage);
            String info = operation == null ? "" : message(operation.name());
            if (objectType != null) {
                info += "  " + message(objectType);
            }
            if (opType != null) {
                info += "  " + message(opType);
            }
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
            scopeController.updateImage(newImage);
            resetImagePane();
            operationsController.resetOperationPanes();
            popInformation(info);
            updateLabelString(info);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    // Only update image and not reset image pane
    public void setImage(ImageManufactureController_Image.ImageOperation operation, Image newImage) {
        try {
            updateImage(newImage);
            scopeController.updateImage(newImage);
            recordImageHistory(operation, null, null, newImage);
            updateLabelsTitle();
            updateLabel(operation);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateLabel(ImageManufactureController_Image.ImageOperation operation) {
        updateLabelString(operation != null ? message(operation.name()) : null);
    }

    public void updateLabelString(String info) {
        infoLabel.setText(info);
    }

    protected void setHistoryIndex(int index) {
        historyIndex = index;
        undoButton.setDisable(historyIndex < 0 || historyIndex >= hisSize - 1);
        redoButton.setDisable(historyIndex <= 0);
    }

    protected void recordImageHistory(ImageOperation operation,
            String objectType, String opType, Image hisImage) {
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
                            .setObjectType(objectType)
                            .setOpType(opType)
                            .setOperationTime(new Date());
                    ImageScope scope = scopeController.scope;
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
                if (objectType != null && !objectType.trim().isEmpty()) {
                    name += "_" + objectType
                            + "_" + new Random().nextInt(1000);
                }
                if (opType != null && !opType.trim().isEmpty()) {
                    name += "_" + opType
                            + "_" + new Random().nextInt(1000);
                }
                name += "_" + new Random().nextInt(1000);
                return name;
            }

            @Override
            protected void whenSucceeded() {
                if (his != null) {
                    if (currentFile.equals(sourceFile)) {
                        setHistoryIndex(0);
                    }
                    ImageManufactureHistoriesController.updateList(currentFile);
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

}
