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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileInformation.FileSelectorType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonBackgroundTask;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.fxml.cell.TableNumberCell;
import mara.mybox.fxml.cell.TableTimeCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @param <P> T must be subClass of FileInformation
 * @Author Mara
 * @CreateDate 2018-11-28
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchTableController<P> extends BaseTablePagesController<P> {

    protected long totalFilesNumber, totalFilesSize, fileSelectorSize, fileSelectorTime, currentIndex;
    protected FileSelectorType fileSelectorType;
    protected boolean countSubdir;

    @FXML
    protected Button addFilesButton, insertFilesButton, addDirectoryButton, insertDirectoryButton,
            listButton, exampleRegexButton;
    @FXML
    protected TableColumn<P, String> handledColumn, fileColumn, typeColumn;
    @FXML
    protected TableColumn<P, Long> currentIndexColumn, numberColumn, sizeColumn, modifyTimeColumn, createTimeColumn;
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
                        tableFiltersInput.setPromptText(message("FileSizeComments"));
                        NodeStyleTools.setTooltip(tableFiltersInput, new Tooltip(message("FileSizeComments")));
                        break;
                    case ModifiedTimeEarlierThan:
                    case ModifiedTimeLaterThan:
                        tableFiltersInput.setText("2019-10-24 10:10:10");
                        NodeStyleTools.setTooltip(tableFiltersInput, new Tooltip("2019-10-24 10:10:10"));
                        break;
                    default:
                        tableFiltersInput.setPromptText(message("SeparateBySpace"));
                        NodeStyleTools.setTooltip(tableFiltersInput, new Tooltip(message("SeparateBySpace")));
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
                menu = new MenuItem(message("AddFiles"), StyleTools.getIconImageView("iconFileAdd.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addFilesAction();
                });
                items.add(menu);
            }

            if (addDirectoryButton != null && addDirectoryButton.isVisible() && !addDirectoryButton.isDisabled()) {
                menu = new MenuItem(message("AddDirectory"), StyleTools.getIconImageView("iconFolderAdd.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addDirectoryAction();
                });
                items.add(menu);
            }
            if (insertFilesButton != null && insertFilesButton.isVisible() && !insertFilesButton.isDisabled()) {
                menu = new MenuItem(message("InsertFiles"), StyleTools.getIconImageView("iconFileInsert.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    insertFilesAction();
                });
                items.add(menu);
            }

            if (insertDirectoryButton != null && insertDirectoryButton.isVisible() && !insertDirectoryButton.isDisabled()) {
                menu = new MenuItem(message("InsertDirectory"), StyleTools.getIconImageView("iconFolderInsert.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    insertDirectoryAction();
                });
                items.add(menu);
            }

            if (moveUpButton != null && moveUpButton.isVisible() && !moveUpButton.isDisabled()) {
                menu = new MenuItem(message("MoveUp"), StyleTools.getIconImageView("iconUp.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    upFilesAction(null);
                });
                items.add(menu);
            }

            if (moveDownButton != null && moveDownButton.isVisible() && !moveDownButton.isDisabled()) {
                menu = new MenuItem(message("MoveDown"), StyleTools.getIconImageView("iconDown.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    downFilesAction(null);
                });
                items.add(menu);
            }
            if (viewButton != null && viewButton.isVisible() && !viewButton.isDisabled()) {
                menu = new MenuItem(message("View"), StyleTools.getIconImageView("iconView.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewAction();
                });
                items.add(menu);
            }
            if (editButton != null && editButton.isVisible() && !editButton.isDisabled()) {
                menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editAction();
                });
                items.add(menu);
            }

            if (deleteButton != null && deleteButton.isVisible() && !deleteButton.isDisabled()) {
                menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                items.add(menu);
            }
            menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearFilesAction();
            });
            items.add(menu);

            if (infoButton != null && infoButton.isVisible() && !infoButton.isDisabled()) {
                menu = new MenuItem(message("Information") + "  CTRL+i", StyleTools.getIconImageView("iconInfo.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);
            }
            if (metaButton != null && metaButton.isVisible() && !metaButton.isDisabled()) {
                menu = new MenuItem(message("MetaData"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    metaAction();
                });
                items.add(menu);
            }

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                        final ImageView imageview = StyleTools.getIconImageView("iconStar.png");
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
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("suffix"));
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
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void tableChanged() {
        if (isSettingValues) {
            return;
        }
        super.tableChanged();
        countSize(false);
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        checkButtons();
        countSize(false);
    }

    @Override
    public void itemDoubleClicked() {
        viewAction();
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
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<File> valids;

            @Override
            protected boolean handle() {
                try {
                    valids = new ArrayList<>();
                    for (int i = 0; i < tableData.size(); i++) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        File file = file(i);
                        if (file.isDirectory()) {
                            handleDir(file);
                        } else {
                            if (isValidFile(file)) {
                                valids.add(file);
                            }
                        }
                    }
                    return task != null && !isCancelled();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            private void handleDir(File dir) {
                if (task == null || isCancelled() || dir == null) {
                    return;
                }
                File[] list = dir.listFiles();
                if (list != null) {
                    for (File file : list) {
                        if (task == null || isCancelled()) {
                            return;
                        }
                        if (file.isDirectory()) {
                            handleDir(file);
                        } else {
                            if (isValidFile(file)) {
                                valids.add(file);
                            }
                        }
                    }
                }
            }

            @Override
            protected void whenSucceeded() {
                tableData.clear();
                if (valids.isEmpty()) {
                    popInformation(message("NotFound"));
                } else {
                    addFiles(0, valids);
                }
            }

        };
        start(task);
    }

    protected boolean isValidFile(File file) {
        return FileFilters.accept(sourceExtensionFilter, file);
    }

    protected boolean isSelected(int index) {
        List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
        return selected == null || selected.isEmpty() || selected.contains(index);
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
        markFileHandled(index, message("Yes"));
    }

    public void stopCountSize() {
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
            tableLabel.setText("");
        }
    }

    public boolean countDirectories() {
        return UserConfig.getBoolean("FilesTableCountSubdir", true);
    }

    public void countSize(boolean reset) {
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        tableLabel.setText(message("CountingFilesSize"));
        totalFilesNumber = totalFilesSize = 0;
        if (tableData == null || tableData.isEmpty()) {
            updateLabel();
            return;
        }
        backgroundTask = new SingletonBackgroundTask<Void>(this) {

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
                        info.countDirectorySize(backgroundTask, countDirectories(), reset);
                        if (backgroundTask == null || isCancelled()) {
                            canceled = true;
                            return false;
                        }
                    }
                    totalFilesNumber += info.getFilesNumber();
                    totalFilesSize += info.getFileSize();
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


    /*
        buttons
     */
    @Override
    public void initMore() {
        try {
            super.initMore();

            if (tableLabel != null) {
                if (nameFiltersSelector != null) {
                    tableLabel.setText(message("FilesSelectBasedTable"));
                } else {
                    tableLabel.setText("");
                }
            }

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
                            popError(message("FileSizeComments"));
                        }

                    } else if (fileSelectorType == FileSelectorType.ModifiedTimeEarlierThan
                            || fileSelectorType == FileSelectorType.ModifiedTimeLaterThan) {
                        Date d = DateTools.encodeDate(newv, -1);
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
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean none = isNoneSelected();
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
        if (infoButton != null) {
            infoButton.setDisable(none);
        }
        if (metaButton != null) {
            metaButton.setDisable(none);
        }

    }

    public void updateLabel() {
        if (tableLabel != null) {
            String s = MessageFormat.format(message("TotalFilesNumberSize"),
                    totalFilesNumber, FileTools.showFileSize(totalFilesSize));
            if (UserConfig.getBoolean("FilesTableCountSubdir", true)) {
                s += "    (" + message("IncludeFolders") + ")";
            } else {
                s += "    (" + message("NotIncludeSubFolders") + ")";
            }
            if (viewButton != null) {
                s += "    " + message("DoubleClickToOpen");
            }
            tableLabel.setText(s);
        }
    }

    @FXML
    public void metaAction() {

    }

    @FXML
    @Override
    public void viewAction() {
        int index = selectedIndix();
        if (index < 0 || index > tableData.size() - 1) {
            return;
        }
        FileInformation info = fileInformation(index);
        view(info.getFile());
    }

    @FXML
    @Override
    public void editAction() {

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
            MyBoxLog.error(e);
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
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            private List<P> infos;

            @Override
            protected boolean handle() {
                infos = createFiles(files);
                if (infos == null) {
                    return false;
                }
                recordFileAdded(files.get(0));
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (infos == null || infos.isEmpty()) {
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
        start(task);
    }

    public List<P> createFiles(List<File> files) {
        try {
            if (files == null || files.isEmpty()) {
                return null;
            }
            List<P> infos = new ArrayList<>();
            for (File file : files) {
                if (task == null || task.isCancelled()) {
                    return infos;
                }
                task.setInfo(file.getAbsolutePath());
                P t = create(file);
                if (t != null) {
                    infos.add(t);
                }
//                recordFileAdded(file);
            }
            return infos;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
        }

    }

    @Override
    public void addDirectory(File directory) {
        addDirectory(tableData.size(), directory);
    }

    public void addDirectory(int index, File directory) {
        try {

            isSettingValues = true;
            P d = create(directory);
            if (index < 0 || index >= tableData.size()) {
                tableData.add(d);
            } else {
                tableData.add(index, d);
            }
            tableView.refresh();
            isSettingValues = false;
            if (UserConfig.getBoolean("FilesTableExpandDirectories", false)) {
                expandDirectories();
            } else {
                tableChanged();
            }
            recordFileOpened(directory);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @FXML
    @Override
    public void insertFilesAction() {
        int index = selectedIndix();
        if (index >= 0) {
            addFiles(index);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @Override
    public void insertFile(File file) {
        int index = selectedIndix();
        if (index >= 0) {
            addFile(index, file);
        } else {
            insertFilesButton.setDisable(true);
        }
    }

    @FXML
    @Override
    public void insertDirectoryAction() {
        int index = selectedIndix();
        if (index >= 0) {
            addDirectory(index);
        } else {
            insertDirectoryButton.setDisable(true);
        }
    }

    @Override
    public void insertDirectory(File directory) {
        int index = selectedIndix();
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
    @Override
    public void deleteAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            clearFilesAction();
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
    protected void showRegexExample(Event event) {
        PopTools.popRegexExamples(this, tableFiltersInput, event);
    }

    @FXML
    protected void popRegexExample(Event event) {
        if (UserConfig.getBoolean("RegexExamplesPopWhenMouseHovering", false)) {
            showRegexExample(event);
        }
    }

    @FXML
    protected void showOptionsMenu(Event event) {
        List<MenuItem> items = new ArrayList<>();

        CheckMenuItem handleSubdirMenu = new CheckMenuItem(message("HandleSubDirectories"));
        handleSubdirMenu.setSelected(UserConfig.getBoolean("FilesTableHandleSubdir", true));
        handleSubdirMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("FilesTableHandleSubdir", handleSubdirMenu.isSelected());
                countSize(true);
            }
        });
        items.add(handleSubdirMenu);

        CheckMenuItem createSubdirMenu = new CheckMenuItem(message("TableCreateDirectories"));
        createSubdirMenu.setSelected(UserConfig.getBoolean("FilesTableCreateSubdir", true));
        createSubdirMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("FilesTableCreateSubdir", createSubdirMenu.isSelected());
            }
        });
        items.add(createSubdirMenu);

        CheckMenuItem countSubdirMenu = new CheckMenuItem(message("CountFilesUnderFolders"));
        countSubdirMenu.setSelected(UserConfig.getBoolean("FilesTableCountSubdir", true));
        countSubdirMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("FilesTableCountSubdir", countSubdirMenu.isSelected());
                countSize(true);
            }
        });
        items.add(countSubdirMenu);

        CheckMenuItem expandSubdirMenu = new CheckMenuItem(message("ExpandDirectories"));
        expandSubdirMenu.setSelected(UserConfig.getBoolean("FilesTableExpandDirectories", false));
        expandSubdirMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("FilesTableExpandDirectories", expandSubdirMenu.isSelected());
                if (expandSubdirMenu.isSelected()) {
                    expandDirectories();
                }
            }
        });
        items.add(expandSubdirMenu);

        items.add(new SeparatorMenuItem());

        CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        hoverMenu.setSelected(UserConfig.getBoolean("FilesTablePopWhenMouseHovering", true));
        hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("FilesTablePopWhenMouseHovering", hoverMenu.isSelected());
            }
        });
        items.add(hoverMenu);

        popEventMenu(event, items);
    }

    @FXML
    protected void popOptionsMenu(Event event) {
        if (UserConfig.getBoolean("FilesTablePopWhenMouseHovering", true)) {
            showOptionsMenu(event);
        }
    }

}
