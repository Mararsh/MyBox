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
import mara.mybox.data.DataMatrix;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class ControlMatricesList extends BaseSysTableController<Data2DDefinition> {

    protected DataMatrix dataMatrix;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;

    @FXML
    protected TableColumn<Data2DDefinition, Long> mxidColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Integer> widthColumn, heightColumn;
    @FXML
    protected TableColumn<Data2DDefinition, String> nameColumn, commentsColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Short> scaleColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Date> modifyColumn;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Label matrixLabel;
    @FXML
    protected Button clearMatricesButton, deleteMatricesButton, editClipsButton, renameClipButton;

    public ControlMatricesList() {
        baseTitle = Languages.message("MatricesManage");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataController.setDataType(this, Data2D.Type.Matrix);
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            dataMatrix = (DataMatrix) dataController.data2D;

            tableDefinition = tableData2DDefinition;
            queryConditions = "data_type=" + dataMatrix.type();

            clearButton = clearMatricesButton;
            deleteButton = deleteMatricesButton;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkStatus();
                }
            });

            dataController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    refreshAction();
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
            mxidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            widthColumn.setCellValueFactory(new PropertyValueFactory<>("colsNumber"));
            heightColumn.setCellValueFactory(new PropertyValueFactory<>("rowsNumber"));
            scaleColumn.setCellValueFactory(new PropertyValueFactory<>("scale"));
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
            modifyColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifyColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Edit"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                editAction();
            });
            menu.setDisable(renameButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("Rename"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameAction();
            });
            menu.setDisable(renameButton.isDisable());
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
    public void postLoadedTableData() {
        super.postLoadedTableData();
        if (!tableData.isEmpty()) {
            dataController.loadMatrix(tableData.get(0));
        }
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
        try {
            dataController.loadMatrix(tableView.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
        String newName = PopTools.askValue(getBaseTitle(), message("CurrentName") + ":" + selected.getDataName(),
                message("NewName"), selected.getDataName() + "m");
        if (newName == null || newName.isBlank()) {
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
                    selected.setDataName(newName);
                    df = tableData2DDefinition.updateData(selected);
                    return df != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    tableData.set(index, df);
                    if (dataMatrix != null && df.getD2did() == dataMatrix.getD2did()) {
                        dataMatrix.setDataName(newName);
                        dataController.attributesController.updateDataName();
                        checkStatus();
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recover();
    }

    /*
        clipboard
     */
    protected void checkStatus() {
        if (getMyStage() != null) {
            String title = baseTitle;
            if (!dataMatrix.isTmpData()) {
                title += " " + dataMatrix.getDataName();
            }
            if (dataController.isChanged()) {
                title += " *";
            }
            myStage.setTitle(title);
        }
        if (!dataMatrix.isTmpData()) {
            matrixLabel.setText(dataMatrix.getDataName());
        } else {
            matrixLabel.setText("");
        }
    }

    @FXML
    public void createMatrix() {
        dataController.create();
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

}
