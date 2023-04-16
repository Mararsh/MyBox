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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class ImageManufactureHistory extends BaseTableViewController<ImageEditHistory> {

    protected ImageManufactureController imageController;
    protected String imageHistoriesPath;
    protected int maxEditHistories, historyIndex;

    @FXML
    protected TableColumn<ImageEditHistory, String> fileColumn;
    @FXML
    protected TableColumn<ImageEditHistory, Image> imageColumn;
    @FXML
    protected TableColumn<ImageEditHistory, Long> sizeColumn;
    @FXML
    protected TableColumn<ImageEditHistory, Date> timeColumn;
    @FXML
    protected TableColumn<ImageEditHistory, String> descColumn;
    @FXML
    protected VBox historiesListBox;
    @FXML
    protected Button clearHistoriesButton, deleteHistoriesButton, useHistoryButton, okHistoriesSizeButton, viewHisButton;
    @FXML
    protected TextField maxHistoriesInput;
    @FXML
    protected CheckBox recordHistoriesCheck;

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            sizeColumn.setCellFactory(new TableFileSizeCell());

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("operationTime"));
            timeColumn.setCellFactory(new TableDateCell());

            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

            descColumn.setCellValueFactory(new PropertyValueFactory<>("desc"));

            imageColumn.setCellValueFactory(new PropertyValueFactory<>("thumbnail"));
            imageColumn.setCellFactory(new Callback<TableColumn<ImageEditHistory, Image>, TableCell<ImageEditHistory, Image>>() {
                @Override
                public TableCell<ImageEditHistory, Image> call(TableColumn<ImageEditHistory, Image> param) {

                    TableCell<ImageEditHistory, Image> cell = new TableCell<ImageEditHistory, Image>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        public void updateItem(Image item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            view.setFitWidth(AppVariables.thumbnailWidth);
                            view.setImage(item);
                            setGraphic(view);
                        }
                    };
                    return cell;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(ImageManufactureController imageController) {
        try {
            this.imageController = imageController;

            imageHistoriesPath = AppPaths.getImageHisPath();

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
                        int v = Integer.parseInt(maxHistoriesInput.getText());
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

            setHistoryIndex(-1);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkRecordHistoriesStatus() {
        if (recordHistoriesCheck.isSelected()) {
            historiesListBox.setDisable(false);
            loadImageHistories();
        } else {
            historiesListBox.setDisable(true);
            tableData.clear();
            setHistoryIndex(-1);
        }
        UserConfig.setBoolean(baseName + "RecordHistories", recordHistoriesCheck.isSelected());
    }

    protected void loadImageHistories() {
        tableData.clear();
        setHistoryIndex(-1);
        if (imageController.sourceFile == null || !recordHistoriesCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private List<ImageEditHistory> list;
                private File currentFile;

                @Override
                protected boolean handle() {
                    try {
                        currentFile = imageController.sourceFile;
                        String key = makeHisKey(currentFile);
                        list = TableImageEditHistory.read(key);
                        if (list != null) {
                            for (ImageEditHistory his : list) {
                                if (task == null || task.isCancelled() || !currentFile.equals(imageController.sourceFile)) {
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
                    if (currentFile.equals(imageController.sourceFile)) {
                        tableData.addAll(list);
                        setHistoryIndex(0);
                    }
                }

                @Override
                protected void whenFailed() {
                    MyBoxLog.error("here");
                }

            };
            start(task, false, null);
        }
    }

    protected void setHistoryIndex(int historyIndex) {
        this.historyIndex = historyIndex;
        imageController.undoButton.setDisable(historyIndex < 0 || historyIndex >= tableData.size() - 1);
        imageController.redoButton.setDisable(historyIndex <= 0);
        tableView.getSelectionModel().clearSelection();
        if (historyIndex >= 0 && historyIndex < tableData.size()) {
            tableView.getSelectionModel().select(historyIndex);
            // Force listView to refresh
            // https://stackoverflow.com/questions/13906139/javafx-update-of-listview-if-an-element-of-observablelist-changes?r=SearchResults
            for (int i = 0; i < tableData.size(); ++i) {
                tableData.set(i, tableData.get(i));
            }
        }
    }

    protected void loadThumbnail(ImageEditHistory his) {
        try {
            if (his == null) {
                return;
            }
            String fname = his.getHistoryLocation();
            int width = AppVariables.thumbnailWidth;
            String thumbname = FileNameTools.append(fname, "_thumbnail");
            File thumbfile = new File(thumbname);
            BufferedImage bufferedImage;
            if (thumbfile.exists()) {
                bufferedImage = ImageFileReaders.readImage(thumbfile);
            } else {
                bufferedImage = ImageFileReaders.readImage(new File(fname), width);
            }
            if (bufferedImage != null) {
                his.setThumbnail(SwingFXUtils.toFXImage(bufferedImage, null));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadImageHistory(int index) {
        if (imageController.sourceFile == null || !recordHistoriesCheck.isSelected()
                || index < 0 || index > tableData.size() - 1) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private Image hisImage;
                private String hisDesc;
                private ImageEditHistory newHis;

                @Override
                protected boolean handle() {
                    try {
                        ImageEditHistory his = tableData.get(index);
                        File file = new File(his.getHistoryLocation());
                        if (!file.exists()) {
                            TableImageEditHistory.deleteHistory(his.getImage(), his.getHistoryLocation());
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    tableData.remove(his);
                                }
                            });
                            return false;
                        }
                        BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                        if (bufferedImage != null) {
                            hisImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        }
                        hisDesc = DateTools.datetimeToString(his.getOperationTime()) + " " + his.getDesc();
                        newHis = (ImageEditHistory) (his.clone());
                        return TableImageEditHistory.add(newHis);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    String info = MessageFormat.format(message("CurrentImageSetAs"), hisDesc);
                    popInformation(info);
                    imageController.updateImage(hisImage, message("History"));
                    tableData.add(0, newHis);
                    setHistoryIndex(index + 1);
                }

            };
            start(task);
        }
    }

    protected void recordImageHistory(final ImageManufactureController_Image.ImageOperation operation, final Image newImage) {
        recordImageHistory(operation, null, null, newImage);
    }

    protected void recordImageHistory(final ImageManufactureController_Image.ImageOperation operation,
            String objectType, String opType, final Image newImage) {
        tableData.clear();
        imageController.redoButton.setDisable(true);
        imageController.undoButton.setDisable(true);
        if (imageController.sourceFile == null || !recordHistoriesCheck.isSelected()
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
            task = new SingletonTask<Void>(this) {
                private File currentFile;
                private String finalname;
                private List<ImageEditHistory> list;

                @Override
                protected boolean handle() {
                    try {
                        currentFile = imageController.sourceFile;
                        BufferedImage bufferedImage = FxImageTools.toBufferedImage(newImage);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        String filename = makeHisName();
                        while (new File(filename).exists()) {
                            filename = makeHisName();
                        }
                        filename = new File(filename).getAbsolutePath();
                        finalname = new File(filename + ".png").getAbsolutePath();
                        if (!ImageFileWriters.writeImageFile(bufferedImage, "png", finalname)) {
                            return false;
                        }
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        String key = makeHisKey(currentFile);
                        TableImageEditHistory.add(key, finalname, operation.name(),
                                objectType, opType, imageController.scopeController.scope);
                        list = TableImageEditHistory.read(key);
                        if (list != null) {
                            for (ImageEditHistory his : list) {
                                if (task == null || task.isCancelled() || !currentFile.equals(imageController.sourceFile)) {
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

                private String makeHisName() {
                    String name = makeHisPath(currentFile) + File.separator
                            + FileNameTools.prefix(currentFile.getName())
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
                    if (currentFile.equals(imageController.sourceFile)) {
                        tableData.setAll(list);
                        setHistoryIndex(0);
                    }
                }

                @Override
                protected void whenFailed() {
                }
            };
            start(task, false, null);
        }
    }

    protected String makeHisKey(File file) {
        try {
            String key = file.getAbsolutePath();
            if (imageController.framesNumber > 1) {
                key += "-frame" + imageController.frameIndex;
            }
            return key;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    protected String makeHisPath(File file) {
        try {
            return AppPaths.getImageHisPath(file);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    protected String makeHisPrefix(File file) {
        try {
            String prefix = FileNameTools.prefix(file.getName());
            if (imageController.framesNumber > 1) {
                prefix += "-frame" + imageController.frameIndex;
            }
            return prefix;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @FXML
    public void refreshHistories() {
        loadImageHistories();
    }

    @FXML
    public void clearHistories() {
        if (imageController.sourceFile == null) {
            return;
        }
        if (!PopTools.askSure(getTitle(), message("SureClear"))) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return TableImageEditHistory.clearImage(makeHisKey(imageController.sourceFile));
            }

            @Override
            protected void whenSucceeded() {
                tableData.clear();
                setHistoryIndex(-1);
            }

        };
        start(task);
    }

    @FXML
    public void deleteHistories() {
        ImageEditHistory selected = selectedItem();
        if (selected == null) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return TableImageEditHistory.deleteHistory(selected.getImage(), selected.getHistoryLocation());
            }

            @Override
            protected void whenSucceeded() {
                tableData.remove(selected);
            }

        };
        start(task);
    }

    @FXML
    public void okHistory() {
        loadImageHistory(selectedIndix());
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
            ImageEditHistory selected = selectedItem();
            if (selected == null) {
                return;
            }
            SingletonTask viewTask = new SingletonTask<Void>(this) {
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
                        tableData.remove(his);
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
        if (imageController.sourceFile == null) {
            return;
        }
        File path = new File(makeHisPath(imageController.sourceFile));
        browseURI(path.toURI());
    }

    public void popHistory() {
        synchronized (this) {
            ImageEditHistory selected = selectedItem();
            if (selected == null) {
                return;
            }
            SingletonTask bgTask = new SingletonTask<Void>(this) {
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
            start(bgTask, false);
        }
    }

    @Override
    public void itemDoubleClicked() {
        okHistory();
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean none = isNoneSelected();

        if (deleteHistoriesButton != null) {
            deleteHistoriesButton.setDisable(none);
        }
        if (viewHisButton != null) {
            viewHisButton.setDisable(none);
        }
        if (useHistoryButton != null) {
            useHistoryButton.setDisable(none);
        }
    }

}
