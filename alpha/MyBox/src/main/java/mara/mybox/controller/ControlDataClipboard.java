package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableFileNameCell;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
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

    @FXML
    protected TableColumn<Data2DDefinition, Long> d2dColumn;
    @FXML
    protected TableColumn<Data2DDefinition, String> nameColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Integer> rowsColumn, colsColumn;
    @FXML
    protected TableColumn<Data2DDefinition, File> fileColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Date> timeColumn;
    @FXML
    protected Label nameLabel;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Button clearClipsButton, deleteClipsButton, editClipButton, renameClipButton;

    public ControlDataClipboard() {
        baseTitle = message("DataClipboard");
        TipsLabelKey = "Data2DTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataController.setDataType(this, Data2D.Type.MyBoxClipboard);
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            dataClipboard = (DataClipboard) dataController.data2D;
            dataController.tableController.dataLabel = nameLabel;
            dataController.tableController.baseTitle = baseTitle;

            tableDefinition = tableData2DDefinition;
            queryConditions = "data_type=" + dataClipboard.type();

            clearButton = clearClipsButton;
            deleteButton = deleteClipsButton;
            renameButton = renameClipButton;
            editButton = editClipButton;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
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

            dataController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    refreshAction();
                }
            });

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
            colsColumn.setCellValueFactory(new PropertyValueFactory<>("colsNumber"));
            rowsColumn.setCellValueFactory(new PropertyValueFactory<>("rowsNumber"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
            fileColumn.setCellFactory(new TableFileNameCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            timeColumn.setCellFactory(new TableDateCell());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Rename"), StyleTools.getIconImage("iconRename.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameAction();
            });
            menu.setDisable(renameButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("OpenPath"), StyleTools.getIconImage("iconOpen.png"));
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
        if (dataClipboard.getFile() != null && !dataClipboard.getFile().exists()) {
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
        editAction();
    }

    @Override
    public void itemDoubleClicked() {
    }

    @FXML
    @Override
    public void editAction() {
        loadClipboard(tableView.getSelectionModel().getSelectedItem());
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        renameButton.setDisable(none);
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
    public void loadClipboard(Data2DDefinition def) {
        if (def == null || !checkBeforeNextAction()) {
            return;
        }
        dataClipboard.initFile(def.getFile());
        dataClipboard.cloneAll(def);
        dataController.readDefinition();
    }

    @FXML
    @Override
    public void createAction() {
        dataController.create();
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recoverFile();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void saveAction() {
        dataController.save();
    }

    @FXML
    public void renameAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        dataController.renameAction(this, index, selected);
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
    public void myBoxClipBoard() {
        dataController.myBoxClipBoard();
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

}
