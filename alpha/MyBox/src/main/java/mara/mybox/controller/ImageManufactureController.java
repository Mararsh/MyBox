package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-12
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    protected SimpleBooleanProperty imageLoaded;
    protected String imageHistoriesPath;
    protected int newWidth, newHeight, maxEditHistories, historyIndex;
    protected ImageOperation operation;

    public static enum ImageOperation {
        Load, History, Saved, Recover, Clipboard, Paste, Arc, Color, Crop, Copy,
        Text, RichText, Mosaic, Convolution,
        Effects, Enhancement, Shadow, Scale2, Picture, Transform, Pen, Margins

    }

    @FXML
    protected TitledPane createPane, historiesPane, backupPane;
    @FXML
    protected VBox mainBox, historiesBox, historiesListBox;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab imageTab, scopeTab;
    @FXML
    protected Label scopeLabel;
    @FXML
    protected ImageView maskView;
    @FXML
    protected TextField newWidthInput, newHeightInput, maxHistoriesInput;
    @FXML
    protected ImageManufactureOperationsController operationsController;
    @FXML
    protected ImageManufactureScopeController scopeController;
    @FXML
    protected Button clearHistoriesButton, deleteHistoriesButton, useHistoryButton, okHistoriesSizeButton;
    @FXML
    protected ListView<ImageEditHistory> historiesList;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected CheckBox recordHistoriesCheck;

    public ImageManufactureController() {
        baseTitle = Languages.message("ImageManufacture");
        TipsLabelKey = "ImageManufactureTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            operationsController.imageController = this;
            operationsController.imageView = imageView;

            imageLoaded = new SimpleBooleanProperty(false);
            historyIndex = -1;
            imageHistoriesPath = AppVariables.getImageHisPath();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initCreatePane();
            initHistoriesTab();
            initBackupsTab();
            initEditBar();

            mainBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            rightPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            scopeController.imageView.fitWidthProperty().bind(imageView.fitWidthProperty());
            scopeController.imageView.fitHeightProperty().bind(imageView.fitHeightProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                                newWidthInput.setStyle(NodeStyleTools.badStyle);
                            }
                        } catch (Exception e) {
                            newWidthInput.setStyle(NodeStyleTools.badStyle);
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
                                newHeightInput.setStyle(NodeStyleTools.badStyle);
                            }
                        } catch (Exception e) {
                            newHeightInput.setStyle(NodeStyleTools.badStyle);
                        }
                    });
            colorSetController.init(this, baseName + "NewBackgroundColor");

            newWidthInput.setText("500");
            newHeightInput.setText("500");

            createButton.disableProperty().bind(
                    newWidthInput.styleProperty().isEqualTo(NodeStyleTools.badStyle)
                            .or(newHeightInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initHistoriesTab() {
        try {
            historiesPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            historiesPane.setExpanded(UserConfig.getBoolean("ImageManufactureHistoriesPane", false));
            historiesPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean("ImageManufactureHistoriesPane", historiesPane.isExpanded());
            });

            recordHistoriesCheck.setSelected(UserConfig.getBoolean(baseName + "RecordHistories", true));
            checkRecordHistoriesStatus();
            recordHistoriesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    checkRecordHistoriesStatus();
                }
            });

            maxEditHistories = UserConfig.getInt("MaxImageHistories", TableImageEditHistory.Default_Max_Histories);
            if (maxEditHistories <= 0) {
                maxEditHistories = TableImageEditHistory.Default_Max_Histories;
            }
            maxHistoriesInput.setText(maxEditHistories + "");
            maxHistoriesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(maxHistoriesInput.getText());
                        if (v >= 0) {
                            maxEditHistories = v;
                            UserConfig.setInt("MaxImageHistories", v);
                            maxHistoriesInput.setStyle(null);
                            okHistoriesSizeButton.setDisable(false);
                        } else {
                            maxHistoriesInput.setStyle(NodeStyleTools.badStyle);
                            okHistoriesSizeButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        maxHistoriesInput.setStyle(NodeStyleTools.badStyle);
                        okHistoriesSizeButton.setDisable(true);
                    }
                }
            });

            historiesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            historiesList.setCellFactory(new Callback<ListView<ImageEditHistory>, ListCell<ImageEditHistory>>() {
                @Override
                public ListCell<ImageEditHistory> call(ListView<ImageEditHistory> param) {
                    ListCell<ImageEditHistory> cell = new ListCell<ImageEditHistory>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        protected void updateItem(ImageEditHistory item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            String s = historyDescription(item);
                            if (getIndex() == historyIndex) {
                                setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                                s = "** " + Languages.message("CurrentImage") + " " + s;
                            } else {
                                setStyle("");
                            }
                            view.setFitWidth(UserConfig.getInt("ThumbnailWidth", 100));
                            view.setImage(item.getThumbnail());
                            setGraphic(view);
                            setText(s);
                        }
                    };
                    return cell;
                }
            });
            historiesList.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    okHistory();
                }
            });

            deleteHistoriesButton.disableProperty().bind(historiesList.getSelectionModel().selectedItemProperty().isNull());
            useHistoryButton.disableProperty().bind(deleteHistoriesButton.disableProperty());

            setHistoryIndex(-1);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBackupsTab() {
        try {
            backupPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            backupPane.setExpanded(UserConfig.getBoolean("ImageManufactureBackupPane", false));
            backupPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                UserConfig.setBoolean("ImageManufactureBackupPane", backupPane.isExpanded());
            });

            backupController.setControls(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEditBar() {
        try {
            redoButton.setDisable(true);
            undoButton.setDisable(true);
            recoverButton.disableProperty().bind(undoButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            imageLoaded.set(true);
            imageChanged = false;
            scopeController.initController(this);
            operationsController.resetOperationPanes();
            resetImagePane();

            recordImageHistory(ImageOperation.Load, image);
            updateBottom(Languages.message("Loaded"));

            autoSize();

            backupController.loadBackups(sourceFile);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public void autoSize() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        fitSize();
                    }
                });
            }
        }, 200);
    }

    @FXML
    public void editFrames() {
        loadMultipleFramesImage(sourceFile);
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

    @Override
    protected void checkRulerX() {
        super.checkRulerX();
        scopeController.checkRulerX();
    }

    @Override
    protected void checkRulerY() {
        super.checkRulerY();
        scopeController.checkRulerY();
    }

    @Override
    protected void checkCoordinate() {
        super.checkCoordinate();
        scopeController.checkCoordinate();
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
    public void refinePane() {
        super.refinePane();
        maskView.setFitWidth(imageView.getFitWidth());
        maskView.setFitHeight(imageView.getFitHeight());
        maskView.setLayoutX(imageView.getLayoutX());
        maskView.setLayoutY(imageView.getLayoutY());
    }

    /*
        Histories
     */
    protected void checkRecordHistoriesStatus() {
        if (recordHistoriesCheck.isSelected()) {
            if (!historiesBox.getChildren().contains(historiesListBox)) {
                historiesBox.getChildren().add(historiesListBox);
            }
            loadImageHistories();
        } else {
            if (historiesBox.getChildren().contains(historiesListBox)) {
                historiesBox.getChildren().remove(historiesListBox);
            }
            historiesList.getItems().clear();
            setHistoryIndex(-1);
        }
        historiesBox.applyCss();
        UserConfig.setBoolean(baseName + "RecordHistories", recordHistoriesCheck.isSelected());
    }

    protected void setHistoryIndex(int historyIndex) {
        this.historyIndex = historyIndex;
        undoButton.setDisable(historyIndex < 0 || historyIndex >= historiesList.getItems().size() - 1);
        redoButton.setDisable(historyIndex <= 0);
        historiesList.getSelectionModel().clearSelection();
        if (historyIndex >= 0 && historyIndex < historiesList.getItems().size()) {
            historiesList.getSelectionModel().select(historyIndex);
            // Force listView to refresh
            // https://stackoverflow.com/questions/13906139/javafx-update-of-listview-if-an-element-of-observablelist-changes?r=SearchResults
            for (int i = 0; i < historiesList.getItems().size(); ++i) {
                historiesList.getItems().set(i, historiesList.getItems().get(i));
            }
        }
    }

    protected void loadImageHistories() {
        historiesList.getItems().clear();
        setHistoryIndex(-1);
        if (sourceFile == null || !recordHistoriesCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                loadTask.cancel();
            }
            loadTask = new SingletonTask<Void>() {
                private List<ImageEditHistory> list;
                private File currentFile;

                @Override
                protected boolean handle() {
                    try {
                        currentFile = sourceFile;
                        String key = currentFile.getAbsolutePath();
                        if (framesNumber > 1) {
                            key += "-frame" + frameIndex;
                        }
                        list = TableImageEditHistory.read(key);
                        if (list != null) {
                            for (ImageEditHistory his : list) {
                                if (loadTask == null || loadTask.isCancelled() || !currentFile.equals(sourceFile)) {
                                    return false;
                                }
                                loadThumbnail(his);
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                    }
                    return list != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (currentFile.equals(sourceFile)) {
                        historiesList.getItems().addAll(list);
                        setHistoryIndex(0);
                    }
                }

            };
            Thread thread = new Thread(loadTask);
//        handling(loadTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void recordImageHistory(final ImageOperation operation, final Image newImage) {
        recordImageHistory(operation, null, null, newImage);
    }

    protected void recordImageHistory(final ImageOperation operation,
            String objectType, String opType, final Image newImage) {
        try {
            historiesList.getItems().clear();
            redoButton.setDisable(true);
            undoButton.setDisable(true);
            if (sourceFile == null || !recordHistoriesCheck.isSelected()
                    || operation == null || newImage == null) {
                return;
            }
            if (imageHistoriesPath == null) {
                imageHistoriesPath = AppVariables.getImageHisPath();
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private File currentFile;
                    private String finalname;
                    private BufferedImage thumbnail;
                    private List<ImageEditHistory> list;

                    @Override
                    protected boolean handle() {
                        try {
                            currentFile = sourceFile;
                            BufferedImage bufferedImage = FxImageTools.toBufferedImage(newImage);
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            String filename = getFilename();
                            while (new File(filename).exists()) {
                                filename = getFilename();
                            }
                            filename = new File(filename).getAbsolutePath();
                            finalname = new File(filename + ".png").getAbsolutePath();
                            ImageFileWriters.writeImageFile(bufferedImage, "png", finalname);
                            thumbnail = ScaleTools.scaleImageWidthKeep(bufferedImage,
                                    UserConfig.getInt("ThumbnailWidth", 100));
                            String thumbname = new File(filename + "_thumbnail.png").getAbsolutePath();
                            if (!ImageFileWriters.writeImageFile(thumbnail, "png", thumbname)) {
                                return false;
                            }
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            String key = currentFile.getAbsolutePath();
                            if (framesNumber > 1) {
                                key += "-frame" + frameIndex;
                            }
                            TableImageEditHistory.add(key, finalname, operation.name(),
                                    objectType, opType, scopeController.scope);
                            list = TableImageEditHistory.read(key);
                            if (list != null) {
                                for (ImageEditHistory his : list) {
                                    if (task == null || task.isCancelled() || !currentFile.equals(sourceFile)) {
                                        return false;
                                    }
                                    loadThumbnail(his);
                                }
                            }
                            return true;
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                    }

                    private String getFilename() {
                        String prefix = FileNameTools.getFilePrefix(currentFile.getName());
                        if (framesNumber > 1) {
                            prefix += "-frame" + frameIndex;
                        }
                        File path = new File(imageHistoriesPath + File.separator + prefix + File.separator);
                        path.mkdirs();
                        String name = path.getAbsolutePath() + File.separator + prefix
                                + "_" + (new Date().getTime()) + "_" + operation;
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
                        if (currentFile.equals(sourceFile)) {
                            historiesList.getItems().setAll(list);
                            setHistoryIndex(0);
                        }
                    }
                };
                task.setSelf(task);
                Thread thread = new Thread(task);
//            handling(task);
                thread.setDaemon(false);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadThumbnail(ImageEditHistory his) {
        try {
            if (his == null) {
                return;
            }
            String fname = his.getHistoryLocation();
            int width = UserConfig.getInt("ThumbnailWidth", 100);
            String thumbname = FileNameTools.appendName(fname, "_thumbnail");
            File thumbfile = new File(thumbname);
            BufferedImage bufferedImage;
            if (thumbfile.exists()) {
                bufferedImage = ImageFileReaders.readImage(thumbfile);
            } else {
                bufferedImage = ImageFileReaders.readImageByWidth("png", fname, width);
            }
            if (bufferedImage != null) {
                his.setThumbnail(SwingFXUtils.toFXImage(bufferedImage, null));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadImageHistory(int index) {
        if (sourceFile == null || !recordHistoriesCheck.isSelected()
                || index < 0 || index > historiesList.getItems().size() - 1) {
            return;
        }
        synchronized (this) {
            if (loadTask != null && !loadTask.isQuit()) {
                return;
            }
            loadTask = new SingletonTask<Void>() {
                private Image hisImage;
                private String hisDesc;
                private ImageEditHistory newHis;

                @Override
                protected boolean handle() {
                    try {
                        ImageEditHistory his = historiesList.getItems().get(index);
                        File file = new File(his.getHistoryLocation());
                        if (!file.exists()) {
                            TableImageEditHistory.deleteHistory(his.getImage(), his.getHistoryLocation());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    historiesList.getItems().remove(his);
                                }
                            });
                            return false;
                        }
                        BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                        if (bufferedImage != null) {
                            hisImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        }
                        hisDesc = DateTools.datetimeToString(his.getOperationTime()) + " " + Languages.message(his.getUpdateType());
                        newHis = (ImageEditHistory) (his.clone());
                        return TableImageEditHistory.add(newHis);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    String info = MessageFormat.format(Languages.message("CurrentImageSetAs"), hisDesc);
                    popInformation(info);
                    updateImage(hisImage, Languages.message("History"));
                    historiesList.getItems().add(0, newHis);
                    setHistoryIndex(index + 1);
                }

            };
            Thread thread = new Thread(loadTask);
            handling(loadTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected String historyDescription(ImageEditHistory his) {
        String s = DateTools.datetimeToString(his.getOperationTime())
                + " " + Languages.message(his.getUpdateType());
        if (his.getObjectType() != null && !his.getObjectType().isEmpty()) {
            s += " " + Languages.message(his.getObjectType());
        }
        if (his.getOpType() != null && !his.getOpType().isEmpty()) {
            s += " " + Languages.message(his.getOpType());
        }
        if (his.getScopeType() != null && !his.getScopeType().isEmpty()) {
            s += " " + Languages.message(his.getScopeType());
        }
        if (his.getScopeName() != null && !his.getScopeName().isEmpty()) {
            s += " " + Languages.message(his.getScopeName());
        }
        return s;
    }

    @FXML
    public void refreshHistories() {
        loadImageHistories();
    }

    @FXML
    public void clearHistories() {
        if (sourceFile == null) {
            return;
        }
        if (!PopTools.askSure(getBaseTitle(), Languages.message("SureClear"))) {
            return;
        }
        historiesList.getItems().clear();
        setHistoryIndex(-1);
        String key = sourceFile.getAbsolutePath();
        if (framesNumber > 1) {
            key += "-frame" + frameIndex;
        }
        TableImageEditHistory.clearImage(key);
    }

    @FXML
    public void deleteHistories() {
        ImageEditHistory selected = historiesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        TableImageEditHistory.deleteHistory(selected.getImage(), selected.getHistoryLocation());
        historiesList.getItems().remove(selected);
    }

    @FXML
    public void okHistory() {
        loadImageHistory(historiesList.getSelectionModel().getSelectedIndex());
    }

    @FXML
    protected void okHistoriesSize(ActionEvent event) {
        try {
            UserConfig.setInt("MaxImageHistories", maxEditHistories);
            popSuccessful();
            loadImageHistories();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void hisPath() {
        if (sourceFile == null) {
            return;
        }
        File path = new File(imageHistoriesPath + File.separator
                + FileNameTools.getFilePrefix(sourceFile.getName()) + File.separator);
        browseURI(path.toURI());
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

    @Override
    public Image imageToSave() {
        return imageView.getImage();
    }

    public void updateImage(ImageOperation operation, Image newImage) {
        updateImage(operation, null, null, newImage, -1);
    }

    public void updateImage(ImageOperation operation, Image newImage, long cost) {
        updateImage(operation, null, null, newImage, cost);
    }

    public void updateImage(ImageOperation operation, String objectType, String opType, Image newImage, long cost) {
        try {
            recordImageHistory(operation, objectType, opType, newImage);
            String info = operation == null ? "" : Languages.message(operation.name());
            if (objectType != null) {
                info += "  " + Languages.message(objectType);
            }
            if (opType != null) {
                info += "  " + Languages.message(opType);
            }
            if (cost > 0) {
                info += "  " + Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
            }
            updateImage(newImage, info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateImage(Image newImage, String info) {
        try {
            updateImage(newImage);
            scopeController.updateImage(newImage);
            resetImagePane();
            operationsController.resetOperationPanes();
            popInformation(info);
            updateBottom(info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    // Only update image and not reset image pane
    public void setImage(ImageOperation operation, Image newImage) {
        try {
            updateImage(newImage);
            scopeController.updateImage(newImage);
            recordImageHistory(operation, null, null, newImage);
            updateLabelsTitle();
            updateBottom(operation);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateBottom(ImageOperation operation) {
        updateBottom(operation != null ? Languages.message(operation.name()) : null);
    }

    public void updateBottom(String info) {
        try {
            if (imageLabel == null) {
                return;
            }
            String bottom = info != null ? info + "  " : "";
            if (imageInformation != null) {
                bottom += Languages.message("Format") + ":" + imageInformation.getImageFormat() + "  ";
                bottom += Languages.message("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight() + "  ";
            }
            if (imageView != null && imageView.getImage() != null) {
                bottom += Languages.message("LoadedSize") + ":"
                        + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "  "
                        + Languages.message("DisplayedSize") + ":"
                        + (int) imageView.getFitWidth() + "x" + (int) imageView.getFitHeight();
            }
            if (sourceFile != null) {
                bottom += "  " + Languages.message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "  "
                        + Languages.message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified()) + "  ";
            }
            imageLabel.setText(bottom);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
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

    public void applyKernel(ConvolutionKernel kernel) {
        operationsController.enhancementPane.setExpanded(true);
        operationsController.enhancementController.optionsController.applyKernel(kernel);
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

    @FXML
    @Override
    public void okAction() {
        operationsController.okAction();
    }

    protected boolean isUsingScope() {
        if (scopeController.scope == null
                || scopeController.scope.getScopeType() == ImageScope.ScopeType.All) {
            return false;
        }
        ImageManufactureOperationController c = operationsController.currentController;
        if (c == null) {
            return false;
        }
        return operationsController.cropController == c
                || (operationsController.colorController == c && !operationsController.colorController.colorReplaceRadio.isSelected())
                || operationsController.enhancementController == c
                || operationsController.effectController == c
                || (operationsController.copyController == c && !operationsController.copyController.wholeRadio.isSelected());
    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (!isPickingColor && isUsingScope()) {
            scopeController.imageClicked(event, p);
        }
        super.imageClicked(event, p);
        operationsController.imageClicked(event, p);
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        operationsController.mousePressed(event);
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        operationsController.mouseDragged(event);
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        operationsController.mouseReleased(event);
    }

    @FXML
    @Override
    public DoublePoint showXY(MouseEvent event) {
        if (xyText == null || !xyText.isVisible()) {
            return null;
        }
        if (isPickingColor
                || (scopeController.isPickingColor && isUsingScope())
                || UserConfig.getBoolean(baseName + "PopCooridnate", false)) {
            DoublePoint p = ImageViewTools.getImageXY(event, imageView);
            showXY(event, p);
            return p;
        } else {
            xyText.setText("");
            return null;
        }
    }

    // should make sure no event conflicts in these panes
    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (!scopeController.keyEventsFilter(event)) {
                return operationsController.keyEventsFilter(event);
            } else {
                return true;
            }
        }
        return true;
    }

    public void resetImagePane() {
        operation = null;
        scope = null;

        imageView.setRotate(0);
        imageView.setVisible(true);
        maskView.setImage(null);
        maskView.setVisible(false);
        maskView.toBack();
        initMaskControls(false);
    }

    public void imageTab() {
        tabPane.getSelectionModel().select(imageTab);
    }

    public void scopeTab() {
        tabPane.getSelectionModel().select(scopeTab);
    }

    /*
        get/set
     */
    public ImageManufactureOperationsController getOperationsController() {
        return operationsController;
    }

    public void setOperationsController(ImageManufactureOperationsController operationsController) {
        this.operationsController = operationsController;
    }

}
