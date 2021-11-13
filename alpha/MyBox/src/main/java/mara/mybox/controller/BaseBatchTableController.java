package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.fxml.cell.TableNumberCell;
import mara.mybox.fxml.cell.TableTimeCell;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @param <P> T must be subClass of FileInformation
 * @Author Mara
 * @CreateDate 2018-11-28
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchTableController<P> extends BaseTableViewController<P> {

    protected long totalFilesNumber, totalFilesSize, fileSelectorSize, fileSelectorTime, currentIndex;
    protected FileSelectorType fileSelectorType;

    @FXML
    protected Button addFilesButton, insertFilesButton, addDirectoryButton, insertDirectoryButton,
            deleteFilesButton, clearFilesButton, viewFileButton, editFileButton,
            listButton, exampleRegexButton;
    @FXML
    protected TableColumn<P, String> handledColumn, fileColumn, typeColumn;
    @FXML
    protected TableColumn<P, Long> currentIndexColumn, numberColumn, sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    protected CheckBox tableSubdirCheck, tableCreateDirCheck, tableExpandDirCheck, countDirCheck;
    @FXML
    protected ComboBox<String> nameFiltersSelector;
    @FXML
    protected TextField tableFiltersInput;
    @FXML
    protected Label tableLabel;

    public BaseBatchTableController() {
        sourceExtensionFilter = FileFilters.AllExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    protected abstract P create(File file);

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (fileSelectorType != null && tableFiltersInput != null) {
                switch (fileSelectorType) {
                    case FileSizeLargerThan:
                    case FileSizeSmallerThan:
                        tableFiltersInput.setPromptText(Languages.message("FileSizeComments"));
                        NodeStyleTools.setTooltip(tableFiltersInput, new Tooltip(Languages.message("FileSizeComments")));
                        break;
                    case ModifiedTimeEarlierThan:
                    case ModifiedTimeLaterThan:
                        tableFiltersInput.setText("2019-10-24 10:10:10");
                        NodeStyleTools.setTooltip(tableFiltersInput, new Tooltip("2019-10-24 10:10:10"));
                        break;
                    default:
                        tableFiltersInput.setPromptText(Languages.message("SeparateBySpace"));
                        NodeStyleTools.setTooltip(tableFiltersInput, new Tooltip(Languages.message("SeparateBySpace")));
                        break;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        table
     */
    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (addFilesButton != null && addFilesButton.isVisible() && !addFilesButton.isDisabled()) {
                menu = new MenuItem(Languages.message("AddFiles"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addFilesAction();
                });
                items.add(menu);
            }

            if (addDirectoryButton != null && addDirectoryButton.isVisible() && !addDirectoryButton.isDisabled()) {
                menu = new MenuItem(Languages.message("AddDirectory"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addDirectoryAction();
                });
                items.add(menu);
            }
            if (insertFilesButton != null && insertFilesButton.isVisible() && !insertFilesButton.isDisabled()) {
                menu = new MenuItem(Languages.message("InsertFiles"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    insertFilesAction();
                });
                items.add(menu);
            }

            if (insertDirectoryButton != null && insertDirectoryButton.isVisible() && !insertDirectoryButton.isDisabled()) {
                menu = new MenuItem(Languages.message("InsertDirectory"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    insertDirectoryAction();
                });
                items.add(menu);
            }

            if (moveUpButton != null && moveUpButton.isVisible() && !moveUpButton.isDisabled()) {
                menu = new MenuItem(Languages.message("MoveUp"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    upFilesAction(null);
                });
                items.add(menu);
            }

            if (moveDownButton != null && moveDownButton.isVisible() && !moveDownButton.isDisabled()) {
                menu = new MenuItem(Languages.message("MoveDown"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    downFilesAction(null);
                });
                items.add(menu);
            }
            if (viewFileButton != null && viewFileButton.isVisible() && !viewFileButton.isDisabled()) {
                menu = new MenuItem(Languages.message("View"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewFileAction();
                });
                items.add(menu);
            }
            if (editFileButton != null && editFileButton.isVisible() && !editFileButton.isDisabled()) {
                menu = new MenuItem(Languages.message("Edit"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editFileAction();
                });
                items.add(menu);
            }

            if (deleteFilesButton != null && deleteFilesButton.isVisible() && !deleteFilesButton.isDisabled()) {
                menu = new MenuItem(Languages.message("Delete"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteFilesAction();
                });
                items.add(menu);
            }
            menu = new MenuItem(Languages.message("Clear"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearFilesAction();
            });
            items.add(menu);

            if (infoButton != null && infoButton.isVisible() && !infoButton.isDisabled()) {
                menu = new MenuItem(Languages.message("Information") + "  CTRL+i");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);
            }
            if (metaButton != null && metaButton.isVisible() && !metaButton.isDisabled()) {
                menu = new MenuItem(Languages.message("MetaData"));
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

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            if (handledColumn != null) {
                handledColumn.setCellValueFactory(new PropertyValueFactory<>("handled"));
            }
            currentIndex = -1;
            if (currentIndexColumn != null) {
                currentIndexColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));  // not care column value
                currentIndexColumn.setCellFactory(new Callback<TableColumn<P, Long>, TableCell<P, Long>>() {
                    @Override
                    public TableCell<P, Long> call(TableColumn<P, Long> param) {
                        final ImageView imageview = new ImageView(StyleTools.getIcon("iconStar.png"));
                        imageview.setPreserveRatio(true);
                        imageview.setFitWidth(15);
                        imageview.setFitHeight(15);
                        TableCell<P, Long> cell = new TableCell<P, Long>() {
                            @Override
                            public void updateItem(Long item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(null);
                                setGraphic(null);
                                if (empty || item == null) {
                                    return;
                                }
                                if (getIndex() == currentIndex) {
                                    setGraphic(imageview);
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

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        checkButtons();
        countSize();
        if (parentController != null) {
            parentController.dataChanged();
        }
    }

    @Override
    public void itemDoubleClicked() {
        viewFileAction();
    }

    public P row(int index) {
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

    protected void expandDirectories() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
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
            super.start(task);
        }

    }

    protected boolean isValidFile(File file) {
        return true;
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
                currentIndex = index;
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
        markFileHandled(index, Languages.message("Yes"));
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
            tableLabel.setText(Languages.message("CountingFilesSize"));
            totalFilesNumber = totalFilesSize = 0;
            if (tableData == null || tableData.isEmpty()) {
                updateLabel();
                return;
            }
            backgroundTask = new SingletonTask<Void>(this) {

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
            start(backgroundTask, false, null);
        }
    }


    /*
        buttons
     */
    @Override
    public void initMore() {
        try {
            super.initMore();

            if (tableSubdirCheck != null) {
                tableSubdirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        UserConfig.setBoolean("TableSubDirctories", newv);
                        countSize();
                    }
                });
                tableSubdirCheck.setSelected(UserConfig.getBoolean("TableSubDirctories", true));
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
                        UserConfig.setBoolean("TableCreateDirctories", newv);
                    }
                });
                tableCreateDirCheck.setSelected(UserConfig.getBoolean("TableCreateDirctories", true));
            }

            if (countDirCheck != null) {
                countDirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                        UserConfig.setBoolean("TableCountDirctories", newv);
                        countSize();
                    }
                });
                countDirCheck.setSelected(UserConfig.getBoolean("TableCountDirctories", true));
            }

            if (tableLabel != null) {
                if (nameFiltersSelector != null) {
                    tableLabel.setText(Languages.message("FilesSelectBasedTable"));
                } else {
                    tableLabel.setText("");
                }
            }

            fileSelectorType = FileSelectorType.All;
            if (nameFiltersSelector == null) {
                return;
            }
            for (FileSelectorType type : FileSelectorType.values()) {
                nameFiltersSelector.getItems().add(Languages.message(type.name()));
            }
            nameFiltersSelector.setVisibleRowCount(FileSelectorType.values().length);
            nameFiltersSelector.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    String selected = nameFiltersSelector.getSelectionModel().getSelectedItem();
                    for (FileSelectorType type : FileSelectorType.values()) {
                        if (Languages.message(type.name()).equals(selected)) {
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
                    setControlsStyle();
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
                            tableFiltersInput.setStyle(UserConfig.badStyle());
                            popError(Languages.message("FileSizeComments"));
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

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        if (insertFilesButton != null) {
            insertFilesButton.setDisable(none);
        }
        if (insertDirectoryButton != null) {
            insertDirectoryButton.setDisable(none);
        }
        if (moveUpButton != null) {
            moveUpButton.setDisable(none);
        }
        if (moveDownButton != null) {
            moveDownButton.setDisable(none);
        }
        if (viewFileButton != null) {
            viewFileButton.setDisable(none);
        }
        if (editFileButton != null) {
            editFileButton.setDisable(none);
        }
        if (deleteFilesButton != null) {
            deleteFilesButton.setDisable(none);
        }
        if (infoButton != null) {
            infoButton.setDisable(none);
        }
        if (metaButton != null) {
            metaButton.setDisable(none);
        }

    }

    public void updateLabel() {
        if (tableLabel != null) {
            String s = MessageFormat.format(Languages.message("TotalFilesNumberSize"),
                    totalFilesNumber, FileTools.showFileSize(totalFilesSize));
            if (countDirCheck != null && tableSubdirCheck != null) {
                if (!countDirCheck.isSelected()) {
                    s += "    (" + Languages.message("NotIncludeFolders") + ")";
                } else if (!tableSubdirCheck.isSelected()) {
                    s += "    (" + Languages.message("NotIncludeSubFolders") + ")";
                } else {
                    s += "    (" + Languages.message("IncludeFolders") + ")";
                }
            }
            if (viewFileButton != null) {
                s += "    " + Languages.message("DoubleClickToView");
            }
            tableLabel.setText(s);
        }
    }

    @FXML
    public void metaAction() {

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

    @FXML
    @Override
    public void addFilesAction() {
        addFiles(tableData.size());
    }

    public void addFiles(int index) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = UserConfig.getPath(baseName + "SourcePath");
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

    public void addFilenames(List<String> fileNames) {
        if (fileNames == null) {
            return;
        }
        List<File> files = new ArrayList<>();
        for (String name : fileNames) {
            File file = new File(name);
            if (file.exists()) {
                files.add(file);
            }
        }
        addFiles(0, files);
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
            task = new SingletonTask<Void>(this) {

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
                parentController.handling(task);
            }
            start(task, false, null);
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
            File defaultPath = UserConfig.getPath(baseName + "SourcePath");
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

    public void listAction() {
        try {
            if (tableData.isEmpty()) {
                return;
            }
            FilesFindController controller
                    = (FilesFindController) openStage(Fxmls.FilesFindFxml);
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
        PopTools.popRegexExample(this, tableFiltersInput, mouseEvent);
    }

}
