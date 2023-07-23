package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
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
public class ImageManufactureHistory extends BaseTablePagesController<ImageEditHistory> {

    protected ImageManufactureController imageController;
    protected TableImageEditHistory tableImageEditHistory;
    protected String imageHistoriesRootPath;
    protected File imageHistoriesPath;
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
    protected CheckBox recordHistoriesCheck, loadCheck;
    @FXML
    protected Label infoLabel;

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
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ImageManufactureController imageController) {
        try {
            this.imageController = imageController;

            tableImageEditHistory = new TableImageEditHistory();
            imageHistoriesRootPath = AppPaths.getImageHisPath();

            recordHistoriesCheck.setSelected(UserConfig.getBoolean(baseName + "RecordHistories", true));
            checkRecordHistoriesStatus();
            recordHistoriesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    checkRecordHistoriesStatus();
                }
            });

            loadCheck.setSelected(UserConfig.getBoolean(baseName + "RecordLoading", true));
            loadCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "RecordLoading", loadCheck.isSelected());
                }
            });

            maxEditHistories = UserConfig.getInt("MaxImageHistories", ImageEditHistory.Default_Max_Histories);
            if (maxEditHistories <= 0) {
                maxEditHistories = ImageEditHistory.Default_Max_Histories;
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
            MyBoxLog.error(e);
        }

    }

    protected void checkRecordHistoriesStatus() {
        if (recordHistoriesCheck.isSelected()) {
            historiesListBox.setDisable(false);
            refreshHistories();
        } else {
            historiesListBox.setDisable(true);
            tableData.clear();
            setHistoryIndex(-1);
        }
        UserConfig.setBoolean(baseName + "RecordHistories", recordHistoriesCheck.isSelected());
    }

    protected boolean checkValid(ImageEditHistory his) {
        try {
            if (his.valid()) {
                return true;
            }
            tableImageEditHistory.deleteData(his);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    tableData.remove(his);
                }
            });
        } catch (Exception e) {
        }
        return false;
    }

    protected void loadHistories() {
        imageHistoriesPath = null;
        recordImageHistory(ImageOperation.Load, null, null, imageController.image);
    }

    protected void recordImageHistory(ImageOperation operation,
            String objectType, String opType, Image hisImage) {
        imageController.redoButton.setDisable(true);
        imageController.undoButton.setDisable(true);
        if (imageController.sourceFile == null || !recordHistoriesCheck.isSelected()) {
            tableData.clear();
            setHistoryIndex(-1);
            return;
        }
        if (operation == null || hisImage == null) {
            return;
        }
        if (operation == ImageOperation.Load) {
            tableData.clear();
        }
        infoLabel.setText(message("Handling..."));
        SingletonTask recordTask = new SingletonTask<Void>(this) {
            private File currentFile;
            private List<ImageEditHistory> list;
            private ImageEditHistory his;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    currentFile = imageController.sourceFile;
                    if (imageHistoriesPath == null) {
                        imageHistoriesPath = tableImageEditHistory.path(conn, currentFile);
                        if (imageHistoriesPath == null) {
                            String fname = currentFile.getName();
                            String subPath = FileNameTools.prefix(fname) + FileNameTools.suffix(fname);
                            imageHistoriesPath = new File(imageHistoriesRootPath + File.separator
                                    + subPath + (new Date()).getTime());
                        }
                    }
                    if (operation == ImageOperation.Load && !loadCheck.isSelected()) {
                        list = tableImageEditHistory.read(conn, currentFile);
                    } else if (!writeRecord(conn)) {
                        return false;
                    }
                    if (list != null) {
                        for (ImageEditHistory item : list) {
                            if (isCancelled() || !currentFile.equals(imageController.sourceFile)) {
                                return false;
                            }
                            for (ImageEditHistory row : tableData) {
                                if (row.getIehid() == item.getIehid()) {
                                    item.setThumbnail(row.getThumbnail());
                                }
                            }
                            if (item.getThumbnail() == null) {
                                loadThumbnail(conn, item);
                            }
                        }
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
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
                    ImageScope scope = imageController.scopeController.scope;
                    if (scope != null) {
                        if (scope.getScopeType() != null) {
                            his.setScopeType(scope.getScopeType().name());
                        }
                        if (scope.getName() != null) {
                            his.setScopeName(scope.getName());
                        }
                    }
                    list = tableImageEditHistory.addHistory(conn, his);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            private String makeHisName() {
                String prefix = FileNameTools.prefix(currentFile.getName());
                if (imageController.framesNumber > 1) {
                    prefix += "-frame" + imageController.frameIndex;
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
                if (currentFile.equals(imageController.sourceFile)) {
                    tableData.setAll(list);
                    setHistoryIndex(0);
                    tableView.scrollTo(his);
                }
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                infoLabel.setText("");
            }
        };
        start(recordTask, historiesListBox);
    }

    protected void loadThumbnail(Connection conn, ImageEditHistory his) {
        try {
            if (his == null || his.getThumbnail() != null) {
                return;
            }
            BufferedImage thumb;
            File thumbfile = his.getThumbnailFile();
            if (thumbfile == null || !thumbfile.exists()) {
                String thumbname = FileNameTools.append(his.getHistoryFile().getAbsolutePath(), "_thumbnail");
                thumb = ImageFileReaders.readImage(his.getHistoryFile(), AppVariables.thumbnailWidth);
                if (thumb != null) {
                    his.setThumbnailFile(new File(thumbname));
                    tableImageEditHistory.updateData(conn, his);
                }
            } else {
                thumb = ImageFileReaders.readImage(thumbfile);
            }
            if (thumb != null) {
                his.setThumbnail(SwingFXUtils.toFXImage(thumb, null));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
        infoLabel.setText("");
    }

    protected void loadImageHistory(int index) {
        if (imageController.sourceFile == null || !recordHistoriesCheck.isSelected()
                || index < 0 || index > tableData.size() - 1) {
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
                try {
                    his = tableData.get(index);
                    if (!checkValid(his)) {
                        return false;
                    }
                    hisImage = his.historyImage();
                    if (hisImage == null) {
                        return false;
                    }
                    his.setOperationTime(new Date());
                    his = tableImageEditHistory.updateData(his);
                    return his != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                String info = MessageFormat.format(message("CurrentImageSetAs"),
                        DateTools.datetimeToString(his.getOperationTime()) + " " + his.getDesc());
                popInformation(info);
                imageController.updateImage(hisImage, message("History"));
                tableData.remove(index);
                tableData.add(0, his);
                setHistoryIndex(0);
                tableView.scrollTo(his);
            }

        };
        start(task, message("loadImageHistory"));
    }

    @FXML
    public void refreshHistories() {
        if (task != null) {
            task.cancel();
        }
        tableData.clear();
        setHistoryIndex(-1);
        if (imageController.sourceFile == null || !recordHistoriesCheck.isSelected()) {
            return;
        }
        infoLabel.setText(message("Loading..."));
        task = new SingletonCurrentTask<Void>(this) {
            private List<ImageEditHistory> list;
            private File currentFile;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    currentFile = imageController.sourceFile;
                    list = tableImageEditHistory.read(conn, currentFile);
                    if (list != null) {
                        for (ImageEditHistory his : list) {
                            if (task == null || isCancelled()
                                    || !currentFile.equals(imageController.sourceFile)) {
                                return false;
                            }
                            loadThumbnail(conn, his);
                        }
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (currentFile.equals(imageController.sourceFile) && list != null) {
                    tableData.addAll(list);
                    setHistoryIndex(0);
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                infoLabel.setText("");
            }

        };
        start(task, historiesListBox);
    }

    @FXML
    public void clearHistories() {
        if (imageController.sourceFile == null) {
            return;
        }
        if (!PopTools.askSure(getTitle(), message("SureClear"))) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                tableImageEditHistory.clearHistories(task, imageController.sourceFile);
                return true;
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
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tableImageEditHistory.deleteData(selected) > 0;
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
            refreshHistories();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public synchronized void viewHistory() {
        ImageEditHistory selected = selectedItem();
        if (selected == null) {
            return;
        }
        SingletonTask viewTask = new SingletonTask<Void>(this) {
            private Image hisImage;

            @Override
            protected boolean handle() {
                hisImage = selected.historyImage();
                return hisImage != null;
            }

            @Override
            protected void whenSucceeded() {
                ImageViewerController viewController = (ImageViewerController) openStage(Fxmls.ImageViewerFxml);
                viewController.loadImage(hisImage);
            }

            @Override
            protected void whenFailed() {
                super.whenFailed();
                checkValid(selected);
            }

        };
        start(viewTask);
    }

    @FXML
    public void hisPath() {
        if (imageHistoriesPath == null) {
            return;
        }
        browseURI(imageHistoriesPath.toURI());
    }

    public void popHistory() {
        ImageEditHistory selected = selectedItem();
        if (selected == null) {
            return;
        }
        SingletonTask bgTask = new SingletonTask<Void>(this) {
            private Image hisImage;

            @Override
            protected boolean handle() {
                hisImage = selected.historyImage();
                return hisImage != null;
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, hisImage);
            }

            @Override
            protected void whenFailed() {
                super.whenFailed();
                checkValid(selected);
            }

        };
        start(bgTask, false);
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
