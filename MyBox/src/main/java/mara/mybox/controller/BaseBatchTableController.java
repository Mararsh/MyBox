package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.TableFileSizeCell;
import mara.mybox.fxml.TableNumberCell;
import mara.mybox.fxml.TableTimeCell;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @param <P> T must be subClass of FileInformation
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchTableController<P> extends BaseController {

    protected ObservableList<P> tableData;
    protected long totalFilesNumber, totalFilesSize, fileSelectorSize, fileSelectorTime, currentIndex;
    protected FileSelectorType fileSelectorType;

    @FXML
    protected Button addFilesButton, insertFilesButton, addDirectoryButton, insertDirectoryButton,
            deleteFilesButton, clearFilesButton, upFilesButton, downFilesButton, viewFileButton, editFileButton,
            unselectAllFilesButton, selectAllFilesButton, listButton, exampleRegexButton;
    @FXML
    protected TableView<P> tableView;
    @FXML
    protected TableColumn<P, String> handledColumn, fileColumn, typeColumn;
    @FXML
    protected TableColumn<P, Long> tableIndexColumn, numberColumn, sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    protected CheckBox tableSubdirCheck, tableCreateDirCheck, tableExpandDirCheck, countDirCheck;
    @FXML
    protected ComboBox<String> nameFiltersSelector;
    @FXML
    protected TextField tableFiltersInput;
    @FXML
    protected Label tableLabel;

    public BaseBatchTableController() {
        TipsLabelKey = "TableTips";
        sourceExtensionFilter = CommonFxValues.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    /*
        Abstract methods
     */
    protected abstract P create(File file);

    /*
        Pretected methods
     */
    protected void initTable() {
        try {
            if (tableView == null) {
                return;
            }
            tableData = FXCollections.observableArrayList();
            tableData.addListener(new ListChangeListener<P>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends P> change) {
                    if (!isSettingValues) {
                        tableChanged();
                    }
                }
            });

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        tableSelected();
                    }
                }
            });
            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popTableMenu(event);
                    } else if (event.getClickCount() == 1) {
                        itemClicked();
                    } else if (event.getClickCount() > 1) {
                        itemDoubleClicked();
                    }
                }
            });

            initColumns();
            tableView.setItems(tableData);

            checkButtons();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void popTableMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = makeTableContextMenu();
        if (items == null || items.isEmpty()) {
            return;
        }
        items.add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (addFilesButton != null && addFilesButton.isVisible() && !addFilesButton.isDisabled()) {
                menu = new MenuItem(message("AddFiles"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addFilesAction();
                });
                items.add(menu);
            }

            if (addDirectoryButton != null && addDirectoryButton.isVisible() && !addDirectoryButton.isDisabled()) {
                menu = new MenuItem(message("AddDirectory"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addDirectoryAction();
                });
                items.add(menu);
            }
            if (insertFilesButton != null && insertFilesButton.isVisible() && !insertFilesButton.isDisabled()) {
                menu = new MenuItem(message("InsertFiles"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    insertFilesAction();
                });
                items.add(menu);
            }

            if (insertDirectoryButton != null && insertDirectoryButton.isVisible() && !insertDirectoryButton.isDisabled()) {
                menu = new MenuItem(message("InsertDirectory"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    insertDirectoryAction();
                });
                items.add(menu);
            }

            if (upFilesButton != null && upFilesButton.isVisible() && !upFilesButton.isDisabled()) {
                menu = new MenuItem(message("MoveUp"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    upFilesAction(null);
                });
                items.add(menu);
            }

            if (downFilesButton != null && downFilesButton.isVisible() && !downFilesButton.isDisabled()) {
                menu = new MenuItem(message("MoveDown"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    downFilesAction(null);
                });
                items.add(menu);
            }
            if (viewFileButton != null && viewFileButton.isVisible() && !viewFileButton.isDisabled()) {
                menu = new MenuItem(message("View"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewFileAction();
                });
                items.add(menu);
            }
            if (editFileButton != null && editFileButton.isVisible() && !editFileButton.isDisabled()) {
                menu = new MenuItem(message("Edit"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editFileAction();
                });
                items.add(menu);
            }
            if (selectAllFilesButton != null && selectAllFilesButton.isVisible() && !selectAllFilesButton.isDisabled()) {
                menu = new MenuItem(message("SelectAll") + "  CTRL+a");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    selectAllFilesAction();
                });
                items.add(menu);
            }
            if (unselectAllFilesButton != null && unselectAllFilesButton.isVisible() && !unselectAllFilesButton.isDisabled()) {
                menu = new MenuItem(message("UnselectAll"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    unselectAllFilesAction();
                });
                items.add(menu);
            }
            if (deleteFilesButton != null && deleteFilesButton.isVisible() && !deleteFilesButton.isDisabled()) {
                menu = new MenuItem(message("Delete"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteFilesAction();
                });
                items.add(menu);
            }
            if (clearFilesButton != null && clearFilesButton.isVisible() && !clearFilesButton.isDisabled()) {
                menu = new MenuItem(message("Clear"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    clearFilesAction();
                });
                items.add(menu);
            }

            if (infoButton != null && infoButton.isVisible() && !infoButton.isDisabled()) {
                menu = new MenuItem(message("Information") + "  CTRL+i");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);
            }
            if (metaButton != null && metaButton.isVisible() && !metaButton.isDisabled()) {
                menu = new MenuItem(message("MetaData"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    metaAction();
                });
                items.add(menu);
            }

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected void initColumns() {
        try {

            if (handledColumn != null) {
                handledColumn.setCellValueFactory(new PropertyValueFactory<>("handled"));
            }
            if (tableIndexColumn != null) {
                tableIndexColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));  // not care column value
                tableIndexColumn.setCellFactory(new Callback<TableColumn<P, Long>, TableCell<P, Long>>() {
                    @Override
                    public TableCell<P, Long> call(TableColumn<P, Long> param) {
                        final ImageView imageview = new ImageView(ControlStyle.getIcon("iconStar.png"));
                        imageview.setPreserveRatio(true);
                        imageview.setFitWidth(15);
                        imageview.setFitHeight(15);
                        TableCell<P, Long> cell = new TableCell<P, Long>() {
                            @Override
                            public void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setGraphic(null);
                                    setText(null);
                                    return;
                                }
                                int rowIndex = getIndex() + 1;
                                if (rowIndex == currentIndex) {
                                    setGraphic(imageview);
                                    setText(null);
                                } else {
                                    setGraphic(null);
                                    setText(String.valueOf(rowIndex));
                                }
                            }
                        };
                        return cell;
                    }
                });
            }
            if (fileColumn != null) {
                fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
                fileColumn.setPrefWidth(320);
            }
            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSuffix"));
            }
            if (numberColumn != null) {
                numberColumn.setCellValueFactory(new PropertyValueFactory<>("filesNumber"));
                numberColumn.setCellFactory(new TableNumberCell());
            }
            if (sizeColumn != null) {
                sizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
                sizeColumn.setCellFactory(new TableFileSizeCell());
            }

            if (modifyTimeColumn != null) {
                modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
                modifyTimeColumn.setCellFactory(new TableTimeCell());
            }
            if (createTimeColumn != null) {
                createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
                createTimeColumn.setCellFactory(new TableTimeCell());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void tableChanged() {
        checkButtons();
        countSize();
        if (parentController != null) {
            parentController.dataChanged();
        }
    }

    protected void tableSelected() {
        checkButtons();
    }

    protected void checkButtons() {
        boolean isEmpty = tableData.isEmpty();
        boolean none = isEmpty;
        if (!isEmpty) {
            P selected = tableView.getSelectionModel().getSelectedItem();
            none = (selected == null);
        }
        if (insertFilesButton != null) {
            insertFilesButton.setDisable(none);
        }
        if (insertDirectoryButton != null) {
            insertDirectoryButton.setDisable(none);
        }
        if (upFilesButton != null) {
            upFilesButton.setDisable(none);
        }
        if (downFilesButton != null) {
            downFilesButton.setDisable(none);
        }
        if (viewFileButton != null) {
            viewFileButton.setDisable(none);
        }
        if (editFileButton != null) {
            editFileButton.setDisable(none);
        }
        if (selectAllFilesButton != null) {
            selectAllFilesButton.setDisable(isEmpty);
        }
        if (unselectAllFilesButton != null) {
            unselectAllFilesButton.setDisable(none);
        }
        if (deleteFilesButton != null) {
            deleteFilesButton.setDisable(none);
        }
        if (clearFilesButton != null) {
            clearFilesButton.setDisable(isEmpty);
        }
        if (infoButton != null) {
            infoButton.setDisable(none);
        }
        if (metaButton != null) {
            metaButton.setDisable(none);
        }

    }

    protected void expandDirectories() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private boolean changed = false;

                @Override
                protected boolean handle() {
                    int index = 0;
                    isSettingValues = true;
                    while (index < tableData.size()) {
                        File file = file(index);
                        if (file.isFile()) {
                            if (isValidFile(file)) {
                                index++;
                            } else {
                                tableData.remove(index);
                                changed = true;
                            }
                            continue;
                        }
                        tableData.remove(index);
                        changed = true;
                        List<File> files = FileTools.allFiles(file);
                        if (files == null || files.isEmpty()) {
                            continue;
                        }
                        List<File> valids = new ArrayList<>();
                        for (File afile : files) {
                            if (isValidFile(afile)) {
                                valids.add(afile);
                            }
                        }
                        if (valids.isEmpty()) {
                            continue;
                        }
                        addFiles(index, valids);
                        index += valids.size();
                    }
                    isSettingValues = false;

                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (changed) {
                        tableView.refresh();
                        tableChanged();
                    }
                }

            };
            super.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    protected boolean isValidFile(File file) {
        return true;
    }

    protected void initSelector() {
        try {
            fileSelectorType = FileSelectorType.All;
            if (nameFiltersSelector == null) {
                return;
            }
            for (FileSelectorType type : FileSelectorType.values()) {
                nameFiltersSelector.getItems().add(message(type.name()));
            }
            nameFiltersSelector.setVisibleRowCount(FileSelectorType.values().length);
            nameFiltersSelector.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    String selected = nameFiltersSelector.getSelectionModel().getSelectedItem();
                    for (FileSelectorType type : FileSelectorType.values()) {
                        if (message(type.name()).equals(selected)) {
                            fileSelectorType = type;
                            break;
                        }
                    }
                    if (exampleRegexButton != null) {
                        exampleRegexButton.setVisible(
                                fileSelectorType == FileSelectorType.NameMatchRegularExpression
                                || fileSelectorType == FileSelectorType.NameNotMatchRegularExpression
                                || fileSelectorType == FileSelectorType.NameIncludeRegularExpression
                                || fileSelectorType == FileSelectorType.NameNotIncludeRegularExpression
                        );
                    }

                    tableFiltersInput.setText("");
                    switch (fileSelectorType) {
                        case FileSizeLargerThan:
                        case FileSizeSmallerThan:
                            tableFiltersInput.setPromptText(message("FileSizeComments"));
                            FxmlControl.setTooltip(tableFiltersInput, new Tooltip(message("FileSizeComments")));
                            break;
                        case ModifiedTimeEarlierThan:
                        case ModifiedTimeLaterThan:
                            tableFiltersInput.setText("2019-10-24 10:10:10");
                            FxmlControl.setTooltip(tableFiltersInput, new Tooltip("2019-10-24 10:10:10"));
                            break;
                        default:
                            tableFiltersInput.setPromptText(message("SeparateBySpace"));
                            FxmlControl.setTooltip(tableFiltersInput, new Tooltip(message("SeparateBySpace")));
                            break;
                    }
                }
            });
            nameFiltersSelector.getSelectionModel().select(0);

            tableFiltersInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    if (newv == null || newv.trim().isEmpty()) {
                        tableFiltersInput.setStyle(null);
                        fileSelectorSize = -1;
                        fileSelectorTime = -1;
                        return;
                    }
                    if (fileSelectorType == FileSelectorType.FileSizeLargerThan
                            || fileSelectorType == FileSelectorType.FileSizeSmallerThan) {
                        long v = ByteTools.checkBytesValue(newv);
                        if (v >= 0) {
                            fileSelectorSize = v;
                            tableFiltersInput.setStyle(null);
                        } else {
                            tableFiltersInput.setStyle(badStyle);
                            popError(message("FileSizeComments"));
                        }

                    } else if (fileSelectorType == FileSelectorType.ModifiedTimeEarlierThan
                            || fileSelectorType == FileSelectorType.ModifiedTimeLaterThan) {
                        Date d = DateTools.stringToDatetime(newv);
                        if (d != null) {
                            fileSelectorTime = d.getTime();
                        } else {
                            fileSelectorTime = -1;
                        }

                    }
                }
            });

            if (previewButton != null && tableView != null) {
                previewButton.disableProperty().bind(tableView.itemsProperty().isNull());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initOthers() {
        try {

            if (tableSubdirCheck != null) {
                tableSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        AppVariables.setUserConfigValue("TableSubDirctories", newv);
                        countSize();
                    }
                });
                tableSubdirCheck.setSelected(AppVariables.getUserConfigBoolean("TableSubDirctories", true));
            }

            if (tableExpandDirCheck != null) {
                tableExpandDirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        if (tableExpandDirCheck.isSelected()) {
                            expandDirectories();
                        }
                    }
                });
            }

            if (tableCreateDirCheck != null) {
                tableCreateDirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        AppVariables.setUserConfigValue("TableCreateDirctories", newv);
                    }
                });
                tableCreateDirCheck.setSelected(AppVariables.getUserConfigBoolean("TableCreateDirctories", true));
            }

            if (countDirCheck != null) {
                countDirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        AppVariables.setUserConfigValue("TableCountDirctories", newv);
                        countSize();
                    }
                });
                countDirCheck.setSelected(AppVariables.getUserConfigBoolean("TableCountDirctories", true));
            }

            if (tableLabel != null) {
                if (nameFiltersSelector != null) {
                    tableLabel.setText(message("FilesSelectBasedTable"));
                } else {
                    tableLabel.setText("");
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void updateLabel() {
        if (tableLabel != null) {
            String s = MessageFormat.format(message("TotalFilesNumberSize"),
                    totalFilesNumber, FileTools.showFileSize(totalFilesSize));
            if (countDirCheck != null && tableSubdirCheck != null) {
                if (!countDirCheck.isSelected()) {
                    s += "    (" + message("NotIncludeFolders") + ")";
                } else if (!tableSubdirCheck.isSelected()) {
                    s += "    (" + message("NotIncludeSubFolders") + ")";
                } else {
                    s += "    (" + message("IncludeFolders") + ")";
                }
            }
            if (viewFileButton != null) {
                s += "    " + message("DoubleClickToView");
            }
            tableLabel.setText(s);
        }
    }

    @FXML
    public void metaAction() {

    }

    /*
        Public methods
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initTable();
            initOthers();
            initSelector();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public P data(int index) {
        try {
            return tableData.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public FileInformation fileInformation(int index) {
        try {
            return (FileInformation) tableData.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public File file(int index) {
        try {
            return fileInformation(index).getFile();
        } catch (Exception e) {
            return null;
        }
    }

    public void itemClicked() {

    }

    public void itemDoubleClicked() {
        viewFileAction();
    }

    @FXML
    public void viewFileAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index < 0 || index > tableData.size() - 1) {
            return;
        }
        FileInformation info = fileInformation(index);
        if (info.getData() != null && !info.getData().isEmpty()) {
            view(info.getData());
        } else {
            view(info.getFile());
        }
    }

    @FXML
    public void editFileAction() {

    }

    public void stopCountSize() {
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
            tableLabel.setText("");
        }
    }

    public void countSize() {
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        synchronized (this) {
            tableLabel.setText(message("CountingFilesSize"));
            totalFilesNumber = totalFilesSize = 0;
            if (tableData == null || tableData.isEmpty()) {
                updateLabel();
                return;
            }
            backgroundTask = new SingletonTask<Void>() {

                private boolean canceled;

                @Override
                protected boolean handle() {
                    for (int i = 0; i < tableData.size(); ++i) {
                        if (backgroundTask == null || isCancelled()) {
                            canceled = true;
                            return false;
                        }
                        FileInformation info = fileInformation(i);
                        if (info == null || info.getFile() == null) {
                            continue;
                        }
                        if (info.getFile().isDirectory()) {
                            boolean sub = tableSubdirCheck == null || tableSubdirCheck.isSelected();
                            info.setDirectorySize(sub);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    tableView.refresh();
                                }
                            });
                            if (countDirCheck != null && countDirCheck.isSelected()) {
                                info.countDirectorySize(backgroundTask, sub);
                                if (backgroundTask == null || isCancelled()) {
                                    canceled = true;
                                    return false;
                                }
                                totalFilesNumber += info.getFilesNumber();
                                totalFilesSize += info.getFileSize();
                            }
                        } else {
                            totalFilesNumber += info.getFilesNumber();
                            totalFilesSize += info.getFileSize();
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tableView.refresh();
                    if (tableLabel != null) {
                        if (canceled) {
                            tableLabel.setText("");
                        } else {
                            updateLabel();
                        }
                    }
                }

                @Override
                protected void whenFailed() {
                    if (tableLabel != null) {
                        tableLabel.setText("");
                    }
                }

            };
            backgroundTask.setSelf(backgroundTask);
            Thread thread = new Thread(backgroundTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void addFilesAction() {
        addFiles(tableData.size());
    }

    public void addFiles(int index) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = AppVariables.getUserConfigPath(sourcePathKey);
            if (defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            addFiles(index, files);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void addFile(File file) {
        addFile(tableData.size(), file);
    }

    public void addFile(int index, File file) {
        if (file == null) {
            return;
        }
        List<File> files = new ArrayList<>();
        files.add(file);
        addFiles(index, files);
    }

    public void addFiles(int index, List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        recordFileAdded(files.get(0));
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<P> infos;

                @Override
                protected boolean handle() {
                    infos = createFiles(files);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (infos.isEmpty()) {
                        return;
                    }
                    isSettingValues = true;
                    if (index < 0 || index >= tableData.size()) {
                        tableData.addAll(infos);
                    } else {
                        tableData.addAll(index, infos);
                    }
                    isSettingValues = false;
                    tableChanged();
                    tableView.refresh();
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    public List<P> createFiles(List<File> files) {
        try {
            if (files == null || files.isEmpty()) {
                return null;
            }
            List<P> infos = new ArrayList<>();
            for (File file : files) {
                P t = create(file);
                if (t != null) {
                    infos.add(t);
                }
                recordFileAdded(file);
            }
            return infos;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void addDirectoryAction() {
        addDirectory(tableData.size());
    }

    public void addDirectory(int index) {
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            File defaultPath = AppVariables.getUserConfigPath(sourcePathKey);
            if (defaultPath != null) {
                dirChooser.setInitialDirectory(defaultPath);
            }
            File directory = dirChooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            addDirectory(index, directory);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void addDirectory(File directory) {
        addDirectory(tableData.size(), directory);
    }

    public void addDirectory(int index, File directory) {
        try {
            recordFileOpened(directory);

            isSettingValues = true;
            P d = create(directory);
            if (index < 0 || index >= tableData.size()) {
                tableData.add(d);
            } else {
                tableData.add(index, d);
            }
            tableView.refresh();
            isSettingValues = false;
            if (tableExpandDirCheck != null && tableExpandDirCheck.isSelected()) {
                expandDirectories();
            } else {
                tableChanged();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void insertFilesAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFiles(index);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @Override
    public void insertFile(File file) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addFile(index, file);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    @Override
    public void insertDirectoryAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectory(index);
        } else {
            insertDirectoryButton.setDisable(true);
        }
    }

    @Override
    public void insertDirectory(File directory) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addDirectory(index, directory);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    public void upFilesAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            P info = tableData.get(index);
            tableData.set(index, tableView.getItems().get(index - 1));
            tableData.set(index - 1, info);
            newselected.add(index - 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        tableView.refresh();
    }

    @FXML
    public void downFilesAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == tableData.size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            P info = tableData.get(index);
            tableData.set(index, tableData.get(index + 1));
            tableData.set(index + 1, info);
            newselected.add(index + 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        tableView.refresh();
    }

    @FXML
    public void selectAllFilesAction() {
        isSettingValues = true;
        tableView.getSelectionModel().selectAll();
        isSettingValues = false;
        tableSelected();
    }

    @FXML
    public void unselectAllFilesAction() {
        isSettingValues = true;
        tableView.getSelectionModel().clearSelection();
        isSettingValues = false;
        tableSelected();
    }

    @FXML
    public void deleteFilesAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index < 0 || index > tableData.size() - 1) {
                continue;
            }
            tableData.remove(index);
        }
        tableView.refresh();
        isSettingValues = false;
        tableChanged();
    }

    @FXML
    public void clearFilesAction() {
        isSettingValues = true;
        tableData.clear();
        tableView.refresh();
        isSettingValues = false;
        tableChanged();
    }

    public void markFileHandling(int index) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (index < 0) {
                    currentIndex = -1;
                    tableView.refresh();
                    return;
                }
                FileInformation d = fileInformation(index);
                if (d == null) {
                    return;
                }
                currentIndex = index + 1;
                tableData.set(index, tableData.get(index));
            }
        });

    }

    public void markFileHandled(int index, String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FileInformation d = fileInformation(index);
                if (d == null) {
                    return;
                }
                d.setHandled(message);
                currentIndex = -1;
                tableView.refresh();
            }
        });

    }

    public void markFileHandled(int index) {
        markFileHandled(index, message("Yes"));
    }

    public void listAction() {
        try {
            if (tableData.isEmpty()) {
                return;
            }
            FilesFindController controller
                    = (FilesFindController) openStage(CommonValues.FilesFindFxml);
            controller.tableData.clear();
            for (int i = 0; i < tableData.size(); ++i) {
                FileInformation finfo = fileInformation(i);
                controller.tableData.add(finfo);
            }
            controller.tableView.refresh();
            if (nameFiltersSelector != null) {
                controller.tableController.nameFiltersSelector.getSelectionModel().
                        select(nameFiltersSelector.getValue());
                controller.tableController.tableFiltersInput.
                        setText(tableFiltersInput.getText());
            }
            controller.startAction();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void popRegexExample(MouseEvent mouseEvent) {
        FxmlControl.popRegexExample(this, tableFiltersInput, mouseEvent);
    }


    /*
        get/set
     */
    public ObservableList<P> getTableData() {
        return tableData;
    }

    public void setTableData(ObservableList<P> tableData) {
        this.tableData = tableData;
    }

    public long getTotalFilesNumber() {
        return totalFilesNumber;
    }

    public void setTotalFilesNumber(long totalFilesNumber) {
        this.totalFilesNumber = totalFilesNumber;
    }

    public long getTotalFilesSize() {
        return totalFilesSize;
    }

    public void setTotalFilesSize(long totalFilesSize) {
        this.totalFilesSize = totalFilesSize;
    }

    public Button getAddFilesButton() {
        return addFilesButton;
    }

    public void setAddFilesButton(Button addFilesButton) {
        this.addFilesButton = addFilesButton;
    }

    public Button getInsertFilesButton() {
        return insertFilesButton;
    }

    public void setInsertFilesButton(Button insertFilesButton) {
        this.insertFilesButton = insertFilesButton;
    }

    public Button getAddDirectoryButton() {
        return addDirectoryButton;
    }

    public void setAddDirectoryButton(Button addDirectoryButton) {
        this.addDirectoryButton = addDirectoryButton;
    }

    public Button getInsertDirectoryButton() {
        return insertDirectoryButton;
    }

    public void setInsertDirectoryButton(Button insertDirectoryButton) {
        this.insertDirectoryButton = insertDirectoryButton;
    }

    public Button getDeleteFilesButton() {
        return deleteFilesButton;
    }

    public void setDeleteFilesButton(Button deleteFilesButton) {
        this.deleteFilesButton = deleteFilesButton;
    }

    public Button getClearFilesButton() {
        return clearFilesButton;
    }

    public void setClearFilesButton(Button clearFilesButton) {
        this.clearFilesButton = clearFilesButton;
    }

    public Button getUpFilesButton() {
        return upFilesButton;
    }

    public void setUpFilesButton(Button upFilesButton) {
        this.upFilesButton = upFilesButton;
    }

    public Button getDownFilesButton() {
        return downFilesButton;
    }

    public void setDownFilesButton(Button downFilesButton) {
        this.downFilesButton = downFilesButton;
    }

    public Button getViewFileButton() {
        return viewFileButton;
    }

    public void setViewFileButton(Button viewFileButton) {
        this.viewFileButton = viewFileButton;
    }

    public Button getUnselectAllFilesButton() {
        return unselectAllFilesButton;
    }

    public void setUnselectAllFilesButton(Button unselectAllFilesButton) {
        this.unselectAllFilesButton = unselectAllFilesButton;
    }

    public Button getSelectAllFilesButton() {
        return selectAllFilesButton;
    }

    public void setSelectAllFilesButton(Button selectAllFilesButton) {
        this.selectAllFilesButton = selectAllFilesButton;
    }

    public TableView<P> getTableView() {
        return tableView;
    }

    public void setTableView(TableView<P> tableView) {
        this.tableView = tableView;
    }

    public TableColumn<P, String> getHandledColumn() {
        return handledColumn;
    }

    public void setHandledColumn(TableColumn<P, String> handledColumn) {
        this.handledColumn = handledColumn;
    }

    public TableColumn<P, String> getFileColumn() {
        return fileColumn;
    }

    public void setFileColumn(TableColumn<P, String> fileColumn) {
        this.fileColumn = fileColumn;
    }

    public TableColumn<P, Long> getNumberColumn() {
        return numberColumn;
    }

    public void setNumberColumn(TableColumn<P, Long> numberColumn) {
        this.numberColumn = numberColumn;
    }

    public TableColumn<P, Long> getSizeColumn() {
        return sizeColumn;
    }

    public void setSizeColumn(TableColumn<P, Long> sizeColumn) {
        this.sizeColumn = sizeColumn;
    }

    public TableColumn<P, Long> getModifyTimeColumn() {
        return modifyTimeColumn;
    }

    public void setModifyTimeColumn(TableColumn<P, Long> modifyTimeColumn) {
        this.modifyTimeColumn = modifyTimeColumn;
    }

    public TableColumn<P, Long> getCreateTimeColumn() {
        return createTimeColumn;
    }

    public void setCreateTimeColumn(TableColumn<P, Long> createTimeColumn) {
        this.createTimeColumn = createTimeColumn;
    }

    public TableColumn<P, String> getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(TableColumn<P, String> typeColumn) {
        this.typeColumn = typeColumn;
    }

    public CheckBox getTableSubdirCheck() {
        return tableSubdirCheck;
    }

    public void setTableSubdirCheck(CheckBox tableSubdirCheck) {
        this.tableSubdirCheck = tableSubdirCheck;
    }

    public ComboBox<String> getNameFiltersSelector() {
        return nameFiltersSelector;
    }

    public void setNameFiltersSelector(ComboBox<String> nameFiltersSelector) {
        this.nameFiltersSelector = nameFiltersSelector;
    }

    public CheckBox getTableExpandDirCheck() {
        return tableExpandDirCheck;
    }

    public void setTableExpandDirCheck(CheckBox tableExpandDirCheck) {
        this.tableExpandDirCheck = tableExpandDirCheck;
    }

    public TextField getTableFiltersInput() {
        return tableFiltersInput;
    }

    public void setTableFiltersInput(TextField tableFiltersInput) {
        this.tableFiltersInput = tableFiltersInput;
    }

    public Label getTableLabel() {
        return tableLabel;
    }

    public void setTableLabel(Label tableLabel) {
        this.tableLabel = tableLabel;
    }

    public FileSelectorType getFileSelectorType() {
        return fileSelectorType;
    }

    public void setFileSelectorType(FileSelectorType fileSelectorType) {
        this.fileSelectorType = fileSelectorType;
    }

    public long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(long currentIndex) {
        this.currentIndex = currentIndex;
    }

}
