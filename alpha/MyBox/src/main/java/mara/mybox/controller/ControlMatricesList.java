package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
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
    protected ControlMatrixEdit editController;
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
    protected int deleteData(List<Matrix> data) {
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
        for (Matrix m : data) {
            if (m.getId() == currentid) {
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
