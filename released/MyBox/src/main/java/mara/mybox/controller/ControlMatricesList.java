package mara.mybox.controller;

import java.util.Date;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.Matrix;
import mara.mybox.db.table.TableMatrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class ControlMatricesList extends BaseDataTableController<Matrix> {

    @FXML
    protected ControlMatrix editController;
    @FXML
    protected TableColumn<Matrix, Long> mxidColumn;
    @FXML
    protected TableColumn<Matrix, Integer> widthColumn, heightColumn;
    @FXML
    protected TableColumn<Matrix, String> nameColumn, commentsColumn;
    @FXML
    protected TableColumn<Matrix, Short> scaleColumn;
    @FXML
    protected TableColumn<Matrix, Date> modifyColumn;
    @FXML
    protected Label matrixLabel;

    public ControlMatricesList() {
        baseTitle = Languages.message("MatricesManage");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableMatrix();
    }

    @Override
    protected void initColumns() {
        try {
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
    public void initControls() {
        try {
            super.initControls();
            editController.initManager(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void itemClicked() {
        editAction(null);
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        try {
            Matrix selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            editController.loadMatrix(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void afterDeletion() {
        loadTableData();
        editController.createAction();
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTableData();
    }

}
