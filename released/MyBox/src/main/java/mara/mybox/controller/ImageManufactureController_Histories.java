package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController_Histories extends ImageManufactureController_Image {

    protected String imageHistoriesPath;
    protected int maxEditHistories, historyIndex;

    @FXML
    protected VBox historiesBox, historiesListBox;
    @FXML
    protected Button clearHistoriesButton, deleteHistoriesButton, useHistoryButton, okHistoriesSizeButton, viewHisButton;
    @FXML
    protected ListView<ImageEditHistory> historiesList;
    @FXML
    protected TextField maxHistoriesInput;
    @FXML
    protected CheckBox recordHistoriesCheck;

    protected void initHistoriesTab() {
        try {
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
                            maxHistoriesInput.setStyle(UserConfig.badStyle());
                            okHistoriesSizeButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        maxHistoriesInput.setStyle(UserConfig.badStyle());
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
                            view.setFitWidth(AppVariables.thumbnailWidth);
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
            viewHisButton.disableProperty().bind(deleteHistoriesButton.disableProperty());

            setHistoryIndex(-1);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

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
            start(loadTask, false, null);
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
                imageHistoriesPath = AppPaths.getImageHisPath();
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
                            thumbnail = ScaleTools.scaleImageWidthKeep(bufferedImage, AppVariables.thumbnailWidth);
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
                start(task, false, null);
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
            int width = AppVariables.thumbnailWidth;
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
            start(loadTask);
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
    public void viewHistory() {
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
                    ImageViewerController controller = (ImageViewerController) openStage(Fxmls.ImageViewerFxml);
                    controller.loadImage(hisImage);
                }

            };
            start(viewTask);
        }
    }

    protected Image hisImage(ImageEditHistory his) {
        try {
            File file = new File(his.getHistoryLocation());
            if (!file.exists()) {
                TableImageEditHistory.deleteHistory(his.getImage(), his.getHistoryLocation());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        historiesList.getItems().remove(his);
                    }
                });
                return null;
            }
            BufferedImage bufferedImage = ImageFileReaders.readImage(file);
            if (bufferedImage != null) {
                return SwingFXUtils.toFXImage(bufferedImage, null);
            }
            return null;
        } catch (Exception e) {
            return null;
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
            updateLabel(info);
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
            updateLabel(operation);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateLabel(ImageOperation operation) {
        updateLabel(operation != null ? Languages.message(operation.name()) : null);
    }

    public void updateLabel(String info) {
        try {
            if (imageLabel == null) {
                return;
            }
            imageLabel.setText(info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
