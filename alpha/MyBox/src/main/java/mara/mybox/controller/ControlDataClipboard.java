package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataClipboard;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class ControlDataClipboard extends BaseDataTableController<DataDefinition> {

    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected boolean checkedInvalid;
    protected DataDefinition currentData;

    @FXML
    protected TableColumn<DataDefinition, Long> dfidColumn;
    @FXML
    protected TableColumn<DataDefinition, String> nameColumn;
    @FXML
    protected Label nameLabel;
    @FXML
    protected ControlSheetCSV sheetController;

    public ControlDataClipboard() {
        baseTitle = message("DataClipboard");
        TipsLabelKey = "DataClipboardTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            sheetController.setParent(this);
            sheetController.targetCharset = Charset.forName("UTF-8");
            sheetController.targetCsvDelimiter = ',';
            sheetController.targetWithNames = false;
            sheetController.saveAsType = saveAsType;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setTableDefinition() {
        tableDataDefinition = sheetController.tableDataDefinition;
        tableDataColumn = sheetController.tableDataColumn;
        tableDefinition = tableDataDefinition;

        tableDataDefinition.setOrderColumns("dfid");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            sheetController.fileLoadedNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        updateStatus();
                    });
            sheetController.sheetChangedNotify.addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        updateStatus();
                    });

            loadTableData();
            loadNull();
            sheetController.newSheet(3, 3);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            dfidColumn.setCellValueFactory(new PropertyValueFactory<>("dfid"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("dataName"));
            nameColumn.setCellFactory(new Callback<TableColumn<DataDefinition, String>, TableCell<DataDefinition, String>>() {
                @Override
                public TableCell<DataDefinition, String> call(TableColumn<DataDefinition, String> param) {
                    TableCell<DataDefinition, String> cell = new TableCell<DataDefinition, String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(null);
                            setText(null);
                            if (empty || item == null) {
                                return;
                            }
                            try {
                                File file = new File(item);
                                if (file.exists()) {
                                    setText(FileNameTools.namePrefix(file.getName()));
                                }
                            } catch (Exception e) {
                            }
                        }
                    };
                    return cell;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(Languages.message("Rename"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameAction();
            });
            menu.setDisable(renameButton.isDisable());
            items.add(menu);

            menu = new MenuItem(Languages.message("OpenPath"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                openPath();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            items.addAll(super.makeTableContextMenu());

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
//            if (isMatrix) {
//                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("MatrixInputComments")));
//            } else {
//                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("DataInputComments")));
//            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public int readDataSize() {
        if (checkedInvalid) {
            return DataClipboard.size(tableDataDefinition);
        } else {
            int size = DataClipboard.checkValid(tableDataDefinition);
            checkedInvalid = true;
            return size;
        }
    }

    @Override
    public List<DataDefinition> readPageData() {
        return DataClipboard.queryPage(tableDataDefinition, currentPageStart - 1, currentPageSize);
    }

    @Override
    protected int deleteData(List<DataDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        for (DataDefinition d : data) {
            FileDeleteTools.delete(d.getDataName());
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected void afterClear() {
        FileDeleteTools.clearDir(new File(AppPaths.getDataClipboardPath()));
        refreshAction();
    }

    @Override
    public void itemClicked() {
        loadData(tableView.getSelectionModel().getSelectedItem());
    }

    @Override
    protected int checkSelected() {
        int selection = super.checkSelected();
        renameButton.setDisable(selection == 0);
        return selection;
    }

    public void loadData(DataDefinition data) {
        if (data == null || !sheetController.checkBeforeNextAction()) {
            return;
        }
        File file = new File(data.getDataName());
        if (!file.exists()) {
            tableDefinition.deleteData(data);
            refreshAction();
            return;
        }
        currentData = data;
        sheetController.sourceFile = file;
        sheetController.userSavedDataDefinition = true;
        sheetController.sourceCharset = Charset.forName(currentData.getCharset());
        sheetController.sourceCsvDelimiter = currentData.getDelimiter().charAt(0);
        sheetController.autoDetermineSourceCharset = false;
        sheetController.sourceWithNames = currentData.isHasHeader();
        sheetController.initCurrentPage();
        sheetController.loadFile();
    }

    public void loadNull() {
        currentData = null;
        sheetController.sourceFile = null;
        sheetController.sourceCharset = Charset.forName("UTF-8");
        sheetController.sourceCsvDelimiter = ',';
        sheetController.autoDetermineSourceCharset = false;
        sheetController.sourceWithNames = false;
        sheetController.initCurrentPage();
        sheetController.initFile();
    }

    public void load(String[][] data, List<ColumnDefinition> columns) {
        loadNull();
        sheetController.makeSheet(data, columns);
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!sheetController.checkBeforeNextAction()) {
                return;
            }
            loadNull();
            sheetController.createAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            if (!sheetController.checkBeforeNextAction()) {
                return;
            }
            if (!TextClipboardTools.systemClipboardHasString()) {
                popError(message("NoTextInClipboard"));
                return;
            }
            loadNull();
            sheetController.loadText(TextClipboardTools.getSystemClipboardString());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void openPath() {
        try {
            browseURI(new File(AppPaths.getDataClipboardPath() + File.separator).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        sheetController.recover();
    }

    @FXML
    @Override
    public void saveAction() {
        if (currentData != null) {
            saveCurrent();
        } else {
            saveNew();
        }
    }

    public void saveCurrent() {
        if (currentData == null) {
            saveNew();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    sheetController.backup();
                    error = sheetController.save(sheetController.sourceFile, sheetController.sourceCharset,
                            sheetController.sourceCsvFormat, false);
                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    sheetController.dataChangedNotify.set(false);
                    sheetController.loadFile();
                }

            };
            start(task);
        }
    }

    public void saveNew() {
        if (currentData != null) {
            saveCurrent();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private DataDefinition df;

                @Override
                protected boolean handle() {
                    File file = TmpFileTools.getTempFile();
                    CSVFormat csvFormat = CSVFormat.DEFAULT
                            .withDelimiter(',').withIgnoreEmptyLines().withTrim()
                            .withNullString("");
                    error = sheetController.save(file, Charset.forName("UTF-8"), csvFormat, false);
                    if (error != null) {
                        return false;
                    }
                    df = DataClipboard.createFile(tableDataDefinition, file, false);
                    return df != null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    sheetController.dataChangedNotify.set(false);
                    loadTableData();
                    loadData(df);
                }

            };
            start(task);
        }
    }

    protected void updateStatus() {
        String title = baseTitle;
        nameLabel.setText("");
        if (sheetController.sourceFile != null) {
            title += " " + sheetController.sourceFile.getAbsolutePath();
            nameLabel.setText(FileNameTools.namePrefix(sheetController.sourceFile.getName()));
        }
        if (sheetController.dataChangedNotify.get()) {
            title += " *";
        }
        getMyStage().setTitle(title);
    }

    @FXML
    public void renameAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        DataDefinition selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        File file = new File(selected.getDataName());
        if (!file.exists()) {
            tableDefinition.deleteData(selected);
            refreshAction();
            return;
        }
        FileRenameController controller = (FileRenameController) openStage(Fxmls.FileRenameFxml);
        controller.getMyStage().setOnHiding((WindowEvent event) -> {
            File newFile = controller.getNewFile();
            Platform.runLater(() -> {
                fileRenamed(index, selected, newFile);
            });
        });
        controller.set(file);
    }

    public void fileRenamed(int index, DataDefinition selected, File newFile) {
        if (selected == null || newFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private DataDefinition df;

                @Override
                protected boolean handle() {
                    selected.setDataName(newFile.getAbsolutePath());
                    df = tableDataDefinition.updateData(selected);
                    return df != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    tableData.set(index, df);
                    if (currentData == df) {
                        sheetController.sourceFile = newFile;
                        updateStatus();
                    }
                }

            };
            start(task);
        }
    }

    @Override
    protected void afterDeletion() {
        refreshAction();
        if (sheetController.sourceFile != null && !sheetController.sourceFile.exists()) {
            loadNull();
            sheetController.makeSheet(null, false);
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        return sheetController.popAction();
    }

    @FXML
    @Override
    public boolean menuAction() {
        return sheetController.menuAction();
    }

    /*
        static
     */
    public static ControlDataClipboard open(String[][] data, List<ColumnDefinition> columns) {
        ControlDataClipboard controller = (ControlDataClipboard) WindowTools.openStage(Fxmls.DataClipboardFxml);
        controller.load(data, columns);
        controller.toFront();
        return controller;
    }

//    public static DataClipboardController open(BaseSheetController sheetController) {
//        DataClipboardController controller = (DataClipboardController) WindowTools.openStage(Fxmls.DataClipboardFxml);
////        controller.setSourceController(sheetController);
//        controller.toFront();
//        return controller;
//    }
}
