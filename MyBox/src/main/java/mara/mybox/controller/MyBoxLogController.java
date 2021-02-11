package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.dev.MyBoxLog.LogType;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class MyBoxLogController extends BaseDataManageController<MyBoxLog> {

    @FXML
    protected MyBoxLogTypeController typeController;
    @FXML
    protected TableColumn<MyBoxLog, String> typeColumn, logColumn, fileColumn,
            classColumn, methodColumn, callersColumn, commentsColumn;
    @FXML
    protected TableColumn<MyBoxLog, Integer> lineColumn;
    @FXML
    protected TableColumn<MyBoxLog, Long> mblidColumn;
    @FXML
    protected TableColumn<MyBoxLog, Date> timeColumn;
    @FXML
    protected CheckBox popCheck;

    public MyBoxLogController() {
        baseTitle = message("MyBoxLogs");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableMyBoxLog();
    }

    @Override
    protected void initColumns() {
        try {
            mblidColumn.setCellValueFactory(new PropertyValueFactory<>("mblid"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            timeColumn.setCellFactory(new TableDateCell());
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
            logColumn.setCellValueFactory(new PropertyValueFactory<>("log"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            classColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
            methodColumn.setCellValueFactory(new PropertyValueFactory<>("methodName"));
            lineColumn.setCellValueFactory(new PropertyValueFactory<>("line"));
            callersColumn.setCellValueFactory(new PropertyValueFactory<>("callers"));
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

            tableView.setRowFactory((TableView<MyBoxLog> param) -> {
                return new SourceRow();
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected class SourceRow extends TableRow<MyBoxLog> {

        @Override
        protected void updateItem(MyBoxLog item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setTextFill(null);
                return;
            }
            if (this.isSelected()) {
                setStyle(FxmlControl.selectedData);
            } else if (item.getLogType() == LogType.Error) {
                setStyle(FxmlControl.errorData);
            } else {
                setStyle(null);
            }
        }
    };

    @Override
    public void initControls() {
        try {
            super.initControls();

            popCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.popErrorLogs = popCheck.isSelected();
                }
            });

            typeController.loadTree();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            errors();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    protected String checkWhere() {
        if (typeController == null) {
            return null;
        }
        String where = typeController.check();
        if (where == null) {
            popError(message("SetConditionsComments"));
            return null;
        }
        return where;
    }

    @Override
    protected String checkTitle() {
        if (typeController == null) {
            return null;
        }
        String title = typeController.getFinalTitle();
        if (title == null) {
            popError(message("SetConditionsComments"));
            return null;
        }
        return title;
    }

    @Override
    public void itemDoubleClicked() {
        viewAction();
    }

    @FXML
    @Override
    public void viewAction() {
        List<MyBoxLog> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        MyBoxLogViewerController controller = (MyBoxLogViewerController) FxmlStage.openStage(CommonValues.MyBoxLogViewerFxml);
        controller.setLogs(selected);
    }

    public void errors() {
        typeController.allItem.setSelected(false);
        typeController.errorItem.setSelected(true);
        orderByList.getSelectionModel().clearSelection();
        orderByList.getSelectionModel().select(message("Time") + " " + message("Descending"));
        tabsPane.getSelectionModel().select(dataTab);
        queryData();
    }

    public static MyBoxLogController oneOpen() {
        MyBoxLogController controller = null;
        Stage stage = FxmlStage.findStage(message("MyBoxLogs"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (MyBoxLogController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (MyBoxLogController) FxmlStage.openStage(CommonValues.MyBoxLogsFxml);
        }
        if (controller != null) {
            controller.getMyStage().requestFocus();
        }
        return controller;
    }

}
