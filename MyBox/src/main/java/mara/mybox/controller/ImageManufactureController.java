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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.db.table.TableImageHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageHistory;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageScope;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-8-12
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    protected SimpleBooleanProperty imageLoaded;
    protected String imageHistoriesPath;
    protected int newWidth, newHeight, maxEditHistories, historyIndex;
    protected ChangeListener<Number> mainDividerListener;
    protected ImageOperation operation;

    public static enum ImageOperation {
        Load, History, Saved, Recover, Clipboard, Paste, Arc, Color, Crop, Copy,
        Text, RichText, Mosaic, Convolution,
        Effects, Enhancement, Shadow, Scale2, Picture, Transform, Pen, Margins

    }

    @FXML
    protected TitledPane createPane, historiesPane;
    @FXML
    protected VBox mainBox, historiesBox, historiesListBox;
    @FXML
    protected SplitPane mainSplitPane;
    @FXML
    protected ScrollPane imagePane, scopePane;
    @FXML
    protected ImageView maskView, imagePaneControl, scopePaneControl;
    @FXML
    protected TextField newWidthInput, newHeightInput, maxHistoriesInput;
    @FXML
    protected ImageManufactureOperationsController operationsController;
    @FXML
    protected ImageManufactureScopeController scopeController;
    @FXML
    protected Button clearHistoriesButton, deleteHistoriesButton, useHistoryButton, okHistoriesSizeButton;
    @FXML
    protected ListView<ImageHistory> historiesList;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected Label scopeLabel;
    @FXML
    protected CheckBox recordHistoriesCheck;

    public ImageManufactureController() {
        baseTitle = AppVariables.message("ImageManufacture");
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
            initEditBar();
            initMainSplitPane();

            mainBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            rightPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            imageLabel.setText(message("ImagePaneTitle"));
            scopeLabel.setText(message("ScopePaneTitle"));
            scopeController.imageView.fitWidthProperty().bind(imageView.fitWidthProperty());
            scopeController.imageView.fitHeightProperty().bind(imageView.fitHeightProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initCreatePane() {
        try {
            createPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue("ImageManufactureNewPane", createPane.isExpanded());
                    });
            createPane.setExpanded(AppVariables.getUserConfigBoolean("ImageManufactureNewPane", true));

            newWidthInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                newWidth = v;
                                newWidthInput.setStyle(null);
                            } else {
                                newWidthInput.setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            newWidthInput.setStyle(badStyle);
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
                                newHeightInput.setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            newHeightInput.setStyle(badStyle);
                        }
                    });
            colorSetController.init(this, baseName + "NewBackgroundColor");

            newWidthInput.setText("500");
            newHeightInput.setText("500");

            createButton.disableProperty().bind(
                    newWidthInput.styleProperty().isEqualTo(badStyle)
                            .or(newHeightInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initHistoriesTab() {
        try {
            historiesPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            recordHistoriesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "RecordHistories", true));
            checkRecordHistoriesStatus();
            recordHistoriesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    checkRecordHistoriesStatus();
                }
            });

            maxEditHistories = AppVariables.getUserConfigInt("MaxImageHistories", TableImageHistory.Default_Max_Histories);
            if (maxEditHistories <= 0) {
                maxEditHistories = TableImageHistory.Default_Max_Histories;
            }
            maxHistoriesInput.setText(maxEditHistories + "");
            maxHistoriesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(maxHistoriesInput.getText());
                        if (v >= 0) {
                            maxEditHistories = v;
                            AppVariables.setUserConfigInt("MaxImageHistories", v);
                            maxHistoriesInput.setStyle(null);
                            okHistoriesSizeButton.setDisable(false);
                        } else {
                            maxHistoriesInput.setStyle(badStyle);
                            okHistoriesSizeButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        maxHistoriesInput.setStyle(badStyle);
                        okHistoriesSizeButton.setDisable(true);
                    }
                }
            });

            historiesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            historiesList.setCellFactory(new Callback<ListView<ImageHistory>, ListCell<ImageHistory>>() {
                @Override
                public ListCell<ImageHistory> call(ListView<ImageHistory> param) {
                    ListCell<ImageHistory> cell = new ListCell<ImageHistory>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        protected void updateItem(ImageHistory item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            String s = historyDescription(item);
                            if (getIndex() == historyIndex) {
                                setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                                s = "** " + message("CurrentImage") + " " + s;
                            } else {
                                setStyle("");
                            }
                            view.setFitWidth(AppVariables.getUserConfigInt("ThumbnailWidth", 100));
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

    protected void initEditBar() {
        try {
            redoButton.setDisable(true);
            undoButton.setDisable(true);
            recoverButton.disableProperty().bind(undoButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initMainSplitPane() {
        try {
            imagePaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlScopePaneOnMouseEnter();
                }
            });
            imagePaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlScopePane();
                }
            });
            imagePaneControl.setPickOnBounds(getUserConfigBoolean("ControlSplitPanesSensitive", false));

            scopePaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlImagePaneOnMouseEnter();
                }
            });
            scopePaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlImagePane();
                }
            });
            scopePaneControl.setPickOnBounds(getUserConfigBoolean("ControlSplitPanesSensitive", false));

            try {
                String mv = AppVariables.getUserConfigValue(baseName + "MainPanePosition", "0.7");
                mainSplitPane.setDividerPositions(Double.parseDouble(mv));
            } catch (Exception e) {
                mainSplitPane.setDividerPositions(0.7);
            }
            mainDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                if (!isSettingValues) {
                    if (mainSplitPane.getItems().size() > 1) {
                        AppVariables.setUserConfigValue(baseName + "MainPanePosition", newValue.doubleValue() + "");
                    }
                }
            };
            mainSplitPane.getDividers().get(0).positionProperty().addListener(mainDividerListener);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FxmlControl.setTooltip(imagePaneControl, new Tooltip("F7"));
            FxmlControl.setTooltip(scopePaneControl, new Tooltip("F8"));

            hideScopePane();
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
            updateBottom(message("Loaded"));

            autoSize();

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
        if (sourceFile == null) {
            return;
        }
        String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
        if (format.contains("gif")) {
            ImageGifEditerController controller
                    = (ImageGifEditerController) openStage(CommonValues.ImageGifEditerFxml);
            controller.selectSourceFile(sourceFile);

        } else {
            ImageTiffEditerController controller
                    = (ImageTiffEditerController) openStage(CommonValues.ImageTiffEditerFxml);
            controller.selectSourceFile(sourceFile);
        }
    }

    @FXML
    @Override
    public void createAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        Image newImage = FxmlImageManufacture.createImage(newWidth, newHeight, (Color) colorSetController.rect.getFill());
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

    @FXML
    @Override
    public void popAction() {
        ImageViewerController controller
                = (ImageViewerController) openStage(CommonValues.ImagePopupFxml);
        controller.loadImage(imageView.getImage());
        controller.paneSize();
    }

    public void controlScopePaneOnMouseEnter() {
        if (getUserConfigBoolean("MousePassControlPanes", true)) {
            controlScopePane();
        }
    }

    public void controlScopePane() {
        if (isSettingValues || !imagePaneControl.isVisible()) {
            return;
        }
        if (mainSplitPane.getItems().contains(scopePane)) {
            hideScopePane();
        } else {
            showScopePane();
        }
    }

    public void showScopePane() {
        if (isSettingValues || !imagePaneControl.isVisible()) {
            return;
        }
        if (mainSplitPane.getItems().contains(scopePane)) {
            if (AppVariables.getUserConfigBoolean(baseName + "FitSize", false)) {
                autoSize();
            }
            return;
        }
        isSettingValues = true;
        mainSplitPane.getItems().add(0, scopePane);
        try {
            String v = AppVariables.getUserConfigValue(baseName + "MainPanePosition", "0.7");
            mainSplitPane.setDividerPosition(0, Double.parseDouble(v));
        } catch (Exception e) {
            mainSplitPane.setDividerPosition(0, 0.7);
        }
        ControlStyle.setIconName(imagePaneControl, "iconDoubleLeft.png");
        mainSplitPane.getDividers().get(0).positionProperty().addListener(mainDividerListener);
//        if (scopeController.scopeAllRadio.isSelected()) {
//            scopeController.scopeRectangleRadio.fire();
//        }
        if (AppVariables.getUserConfigBoolean(baseName + "FitSize", false)) {
            autoSize();
        }
        mainSplitPane.applyCss();
        isSettingValues = false;
    }

    public void hideScopePane() {
        if (isSettingValues || !imagePaneControl.isVisible()
                || !mainSplitPane.getItems().contains(scopePane)
                || mainSplitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        mainSplitPane.getDividers().get(0).positionProperty().removeListener(mainDividerListener);
        mainSplitPane.getItems().remove(scopePane);
        ControlStyle.setIconName(imagePaneControl, "iconDoubleRight.png");
        if (AppVariables.getUserConfigBoolean(baseName + "FitSize", false)) {
            autoSize();
        }
        mainSplitPane.applyCss();
        isSettingValues = false;
    }

    public void controlImagePaneOnMouseEnter() {
        if (getUserConfigBoolean("MousePassControlPanes", true)) {
            controlImagePane();
        }
    }

    public void controlImagePane() {
        if (isSettingValues || !scopePaneControl.isVisible()) {
            return;
        }
        if (mainSplitPane.getItems().contains(imagePane)) {
            hideImagePane();
        } else {
            showImagePane();
        }
    }

    public void showImagePane() {
        if (isSettingValues || !scopePaneControl.isVisible()) {
            return;
        }
        if (mainSplitPane.getItems().contains(imagePane)) {
            if (AppVariables.getUserConfigBoolean(baseName + "FitSize", false)) {
                autoSize();
            }
            return;
        }
        isSettingValues = true;
        mainSplitPane.getItems().add(imagePane);
        try {
            String v = AppVariables.getUserConfigValue(baseName + "MainPanePosition", "0.7");
            mainSplitPane.setDividerPosition(0, Double.parseDouble(v));
        } catch (Exception e) {
            mainSplitPane.setDividerPosition(0, 0.7);
        }
        ControlStyle.setIconName(scopePaneControl, "iconDoubleRight.png");
        mainSplitPane.getDividers().get(0).positionProperty().addListener(mainDividerListener);
        if (AppVariables.getUserConfigBoolean(baseName + "FitSize", false)) {
            autoSize();
        }
        mainSplitPane.applyCss();
        isSettingValues = false;
    }

    public void hideImagePane() {
        if (isSettingValues || !scopePaneControl.isVisible()
                || !mainSplitPane.getItems().contains(imagePane)
                || mainSplitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        mainSplitPane.getDividers().get(0).positionProperty().removeListener(mainDividerListener);
        mainSplitPane.getItems().remove(imagePane);
        ControlStyle.setIconName(scopePaneControl, "iconDoubleLeft.png");
        mainSplitPane.applyCss();
        isSettingValues = false;
    }

    @FXML
    public void scopeAction() {
        controlScopePane();
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
        AppVariables.setUserConfigValue(baseName + "RecordHistories", recordHistoriesCheck.isSelected());
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
                private List<ImageHistory> list;
                private File currentFile;

                @Override
                protected boolean handle() {
                    try {
                        currentFile = sourceFile;
                        String key = currentFile.getAbsolutePath();
                        if (framesNumber > 1) {
                            key += "-frame" + frameIndex;
                        }
                        list = TableImageHistory.read(key);
                        if (list != null) {
                            for (ImageHistory his : list) {
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
//        openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            thread.setDaemon(true);
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
                    private List<ImageHistory> list;

                    @Override
                    protected boolean handle() {
                        try {
                            currentFile = sourceFile;
                            BufferedImage bufferedImage = FxmlImageManufacture.bufferedImage(newImage);
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
                            thumbnail = ImageManufacture.scaleImageWidthKeep(bufferedImage,
                                    AppVariables.getUserConfigInt("ThumbnailWidth", 100));
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
                            TableImageHistory.add(key, finalname, operation.name(),
                                    objectType, opType, scopeController.scope);
                            list = TableImageHistory.read(key);
                            if (list != null) {
                                for (ImageHistory his : list) {
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
                        String prefix = FileTools.getFilePrefix(currentFile.getName());
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
//            openHandlingStage(task, Modality.WINDOW_MODAL);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadThumbnail(ImageHistory his) {
        try {
            if (his == null) {
                return;
            }
            String fname = his.getHistoryLocation();
            int width = AppVariables.getUserConfigInt("ThumbnailWidth", 100);
            String thumbname = FileTools.appendName(fname, "_thumbnail");
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

                @Override
                protected boolean handle() {
                    try {
                        ImageHistory his = historiesList.getItems().get(index);
                        File file = new File(his.getHistoryLocation());
                        if (!file.exists()) {
                            TableImageHistory.deleteHistory(his.getImage(), his.getHistoryLocation());
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
                        hisDesc = DateTools.datetimeToString(his.getOperationTime()) + " " + message(his.getUpdateType());
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    String info = MessageFormat.format(message("CurrentImageSetAs"), hisDesc);
                    popInformation(info);
                    updateImage(hisImage, message("History"));
                    setHistoryIndex(index);
                }

            };
            Thread thread = new Thread(loadTask);
            openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected String historyDescription(ImageHistory his) {
        String s = DateTools.datetimeToString(his.getOperationTime())
                + " " + message(his.getUpdateType());
        if (his.getObjectType() != null && !his.getObjectType().isEmpty()) {
            s += " " + message(his.getObjectType());
        }
        if (his.getOpType() != null && !his.getOpType().isEmpty()) {
            s += " " + message(his.getOpType());
        }
        if (his.getScopeType() != null && !his.getScopeType().isEmpty()) {
            s += " " + message(his.getScopeType());
        }
        if (his.getScopeName() != null && !his.getScopeName().isEmpty()) {
            s += " " + message(his.getScopeName());
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
        if (!FxmlControl.askSure(getBaseTitle(), message("SureClear"))) {
            return;
        }
        historiesList.getItems().clear();
        setHistoryIndex(-1);
        String key = sourceFile.getAbsolutePath();
        if (framesNumber > 1) {
            key += "-frame" + frameIndex;
        }
        TableImageHistory.clearImage(key);
    }

    @FXML
    public void deleteHistories() {
        ImageHistory selected = historiesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        TableImageHistory.deleteHistory(selected.getImage(), selected.getHistoryLocation());
        historiesList.getItems().remove(selected);
    }

    @FXML
    public void okHistory() {
        loadImageHistory(historiesList.getSelectionModel().getSelectedIndex());
    }

    @FXML
    protected void okHistoriesSize(ActionEvent event) {
        try {
            AppVariables.setUserConfigInt("MaxImageHistories", maxEditHistories);
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
                + FileTools.getFilePrefix(sourceFile.getName()) + File.separator);
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
        popInformation(message("Recovered"));
    }

    @FXML
    @Override
    public void saveAction() {
        if (saveButton.isDisabled()) {
            return;
        }
        if (sourceFile == null) {
            saveAsAction();
            return;
        }
        if (saveConfirmCheck.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("SureOverrideFile"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVariables.message("SaveAs"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAsAction();
                return;
            }
        }

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    String format = "png";
                    if (imageInformation != null) {
                        format = imageInformation.getImageFormat();
                    }
                    final BufferedImage bufferedImage
                            = FxmlImageManufacture.bufferedImage(imageView.getImage());
                    if (bufferedImage == null || task == null || isCancelled()) {
                        return false;
                    }
                    if (framesNumber > 1) {
                        error = ImageFileWriters.writeFrame(sourceFile, frameIndex, bufferedImage);
                        ok = error == null;
                    } else {
                        ok = ImageFileWriters.writeImageFile(bufferedImage, format, sourceFile.getAbsolutePath());
                    }
                    if (!ok || task == null || isCancelled()) {
                        return false;
                    }
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(sourceFile.getAbsolutePath());
                    if (finfo == null || finfo.getImageInformation() == null) {
                        return false;
                    }
                    imageInformation = finfo.getImageInformation();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    setImageChanged(false);
                    updateBottom(message("Saved"));
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (saveAsButton.isDisabled()) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    saveAsPrefix(), targetExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file);
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        String format = FileTools.getFileSuffix(file.getName());
                        final BufferedImage bufferedImage
                                = FxmlImageManufacture.bufferedImage(imageView.getImage());
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        if (framesNumber > 1 && saveAllFramesRadio.isSelected()) {
                            error = ImageFileWriters.writeFrame(sourceFile, frameIndex, bufferedImage, file, format);
                            return error == null;
                        } else {
                            return ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (sourceFile == null
                                || saveAsType == SaveAsType.Load) {
                            sourceFileChanged(file);

                        } else if (saveAsType == SaveAsType.Open) {
                            FxmlStage.openImageManufacture(file.getAbsolutePath());
                        }
                        popSuccessful();
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

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
            String info = operation == null ? "" : AppVariables.message(operation.name());
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
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateImage(Image newImage, String info) {
        try {
            updateImage(newImage);
//            showImagePane();
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
        updateBottom(operation != null ? message(operation.name()) : null);
    }

    public void updateBottom(String info) {
        try {
            if (bottomLabel == null) {
                return;
            }
            String bottom = info != null ? info + "  " : "";
            if (imageInformation != null) {
                bottom += message("Format") + ":" + imageInformation.getImageFormat() + "  ";
                bottom += message("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight() + "  ";
            }
            if (imageView != null && imageView.getImage() != null) {
                bottom += message("LoadedSize") + ":"
                        + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "  "
                        + message("DisplayedSize") + ":"
                        + (int) imageView.getFitWidth() + "x" + (int) imageView.getFitHeight();
            }
            if (sourceFile != null) {
                bottom += "  " + message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "  "
                        + message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified()) + "  ";
            }
            bottomLabel.setText(bottom);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void cropAction() {
        operationsController.cropPane.setExpanded(true);
        operationsController.cropController.okAction();
    }

    @FXML
    @Override
    public void copyAction() {
        operationsController.copyPane.setExpanded(true);
        operationsController.copyController.okAction();
    }

    @FXML
    @Override
    public void pasteAction() {
        operationsController.clipboardPane.setExpanded(true);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();   // Waiting for thumbs list loaded
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (operationsController.clipboardController != null
                                && operationsController.clipboardController.loaded) {
                            operationsController.clipboardController.selectClip();
                            timer.cancel();
                            timer = null;
                        }
                    }
                });
            }
        }, 0, 100);
    }

    public void applyKernel(ConvolutionKernel kernel) {
        operationsController.enhancementPane.setExpanded(true);
        operationsController.enhancementController.optionsController.applyKernel(kernel);
    }

    /*
        Browse
     */
    @FXML
    @Override
    public void nextAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (previousFile != null) {
            loadImage(previousFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!imageLoaded.get() || !imageChanged) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVariables.message("ImageChanged"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
        ButtonType buttonSaveAs = new ButtonType(AppVariables.message("SaveAs"));
        ButtonType buttonNotSave = new ButtonType(AppVariables.message("NotSave"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
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
        operationsController.imageClicked(event, p);
        super.imageClicked(event, p);
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
                || (!needNotCoordinates && AppVariables.getUserConfigBoolean(baseName + "PopCooridnate", false))) {
            DoublePoint p = FxmlControl.getImageXY(event, imageView);
            showXY(event, p);
            return p;
        } else {
            xyText.setText("");
            return null;
        }
    }

    // should make sure no event conflicts in these panes
    @Override
    public void keyEventsHandler(KeyEvent event) {
        if (event.getCode() != null && imageView.getImage() != null) {
            switch (event.getCode()) {
                case F7:
                    controlScopePane();
                    return;
                case F8:
                    controlImagePane();
                    return;
            }
        }
        super.keyEventsHandler(event);
        operationsController.keyEventsHandler(event);
        scopeController.keyEventsHandler(event);
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
        imageLabel.setText(message("ImagePaneTitle"));
        scopeLabel.setText(message("ScopePaneTitle"));

    }

}
