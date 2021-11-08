package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class ControlMatricesList extends BaseSysTableController<DataDefinition> {

    @FXML
    protected ControlMatrixEdit editController;
    @FXML
    protected TableColumn<DataDefinition, Long> mxidColumn;
    @FXML
    protected TableColumn<DataDefinition, Integer> widthColumn, heightColumn;
    @FXML
    protected TableColumn<DataDefinition, String> nameColumn, commentsColumn;
    @FXML
    protected TableColumn<DataDefinition, Short> scaleColumn;
    @FXML
    protected TableColumn<DataDefinition, Date> modifyColumn;
    @FXML
    protected Label matrixLabel;

    public ControlMatricesList() {
        baseTitle = Languages.message("MatricesManage");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableDataDefinition();
        queryConditions = "data_type=" + DataDefinition.dataType(DataDefinition.DataType.Matrix);
    }

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
    public void itemClicked() {
        editAction();
    }

    @FXML
    @Override
    public void editAction() {
        try {
            DataDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            editController.loadMatrix(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected int deleteData(List<DataDefinition> data) {
        int ret = super.deleteData(data);
        if (ret <= 0) {
            return ret;
        }
        long currentid = -1;
        try {
            currentid = Long.parseLong(editController.idInput.getText());
        } catch (Exception e) {
        }
        if (currentid < 0) {
            return ret;
        }
        for (DataDefinition m : data) {
            if (m.getDfid() == currentid) {
                Platform.runLater(() -> {
                    editController.loadNull();
                });
                break;
            }
        }
        return ret;
    }

    @Override
    protected void afterClear() {
        editController.loadNull();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return editController.keyEventsFilter(event);
        }
        return true;
    }

}
