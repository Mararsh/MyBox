package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.WindowEvent;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class ControlDataClipboard extends BaseSysTableController<Data2DDefinition> {

    protected DataClipboard dataClipboard;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected boolean invalidChecked;

    @FXML
    protected TableColumn<Data2DDefinition, Long> d2dColumn;
    @FXML
    protected TableColumn<Data2DDefinition, String> nameColumn;
    @FXML
    protected Label nameLabel;
    @FXML
    protected ControlData2D dataController;

    public ControlDataClipboard() {
        baseTitle = message("DataClipboard");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataController.setDataType(this, Data2D.Type.DataClipboard);
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            dataClipboard = (DataClipboard) dataController.data2D;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = tableData2DDefinition;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    updateStatus();
                }
            });

            dataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    updateStatus();
                }
            });

            loadTableData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        table
     */
    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            d2dColumn.setCellValueFactory(new PropertyValueFactory<>("d2did"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("dataName"));

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
    public long readDataSize() {
        if (invalidChecked) {
            String condition = "data_type=" + dataClipboard.type();
            return tableData2DDefinition.conditionSize(condition);
        } else {
            int count = dataClipboard.checkValid();
            invalidChecked = true;
            return count;
        }
    }

    @Override
    public List<Data2DDefinition> readPageData() {
        String condition = "data_type=" + dataClipboard.type() + " ORDER BY d2did DESC ";
        return tableData2DDefinition.queryConditions(condition, startRowOfCurrentPage, pageSize);
    }

    @Override
    protected int deleteData(List<Data2DDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        for (Data2DDefinition d : data) {
            FileDeleteTools.delete(d.getFile());
        }
        return tableData2DDefinition.deleteData(data);
    }

    @Override
    protected void afterDeletion() {
        refreshAction();
        if (dataController.sourceFile != null && !dataController.sourceFile.exists()) {
            dataController.loadNull();
        }
    }

    @Override
    protected void afterClear() {
        FileDeleteTools.clearDir(new File(AppPaths.getDataClipboardPath()));
        refreshAction();
        dataController.loadNull();
    }

    @Override
    public void itemClicked() {
        loadData(tableView.getSelectionModel().getSelectedItem());
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        renameButton.setDisable(tableView.getSelectionModel().getSelectedIndex() < 0);
    }

    @FXML
    public void openPath() {
        try {
            browseURI(new File(AppPaths.getDataClipboardPath() + File.separator).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        clipboard
     */
    public void loadData(Data2DDefinition data) {
        if (data == null || !checkBeforeNextAction()) {
            return;
        }
        sourceFile = data.getFile();
        dataClipboard.initFile(sourceFile);
        dataClipboard.load(data);
        dataClipboard.setUserSavedDataDefinition(true);
        dataController.loadDefinition();
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFile = null;
            dataClipboard.initFile(dataClipboard.tmpFile());
            dataController.loadDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.resetStatus();
        if (dataClipboard.isTmpFile()) {
            dataClipboard.initFile(dataClipboard.tmpFile());
            dataController.loadDefinition();
        } else {
            dataClipboard.initFile(sourceFile);
            dataClipboard.setUserSavedDataDefinition(true);
            dataController.loadDefinition();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (dataClipboard.isTmpFile()) {
            File file = dataClipboard.newFile();
            if (file == null) {
                return;
            }
            DataClipboard targetData = (DataClipboard) dataClipboard.cloneAll();
            targetData.setFile(file);
            targetData.setD2did(-1);
            targetData.setCharset(Charset.forName("UTF-8"));
            targetData.setDelimiter(",");
            targetData.setHasHeader(true);
            dataController.saveAs(targetData, true);
        } else {
            dataController.save();
        }
    }

    protected void updateStatus() {
        String title = baseTitle + " - " + dataClipboard.getFile().getAbsolutePath();
        nameLabel.setText(dataClipboard.getDataName());
        if (dataController.isChanged()) {
            title += " *";
        }
        getMyStage().setTitle(title);
    }

    @FXML
    public void renameAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        int index = tableView.getSelectionModel().getSelectedIndex();
        Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        File file = selected.getFile();
        if (!file.exists()) {
            tableData2DDefinition.deleteData(selected);
            dataController.loadNull();
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

    public void fileRenamed(int index, Data2DDefinition selected, File newFile) {
        if (selected == null || newFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private Data2DDefinition df;

                @Override
                protected boolean handle() {
                    selected.setFile(newFile);
                    selected.setDataName(newFile.getName());
                    df = tableData2DDefinition.updateData(selected);
                    return df != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    tableData.set(index, df);
                    loadData(df);
                }

            };
            start(task);
        }
    }

    /*
        interface
     */
    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return dataController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        return dataController.checkBeforeNextAction();
    }

    @Override
    public void cleanPane() {
        try {
            dataClipboard = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static ControlDataClipboard open(String[][] data, List<ColumnDefinition> columns) {
        ControlDataClipboard controller = (ControlDataClipboard) WindowTools.openStage(Fxmls.DataClipboardFxml);
//        controller.load(data, columns);
        controller.toFront();
        return controller;
    }

}
