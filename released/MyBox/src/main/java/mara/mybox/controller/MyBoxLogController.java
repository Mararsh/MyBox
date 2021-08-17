package mara.mybox.controller;

import java.util.ArrayList;
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
import javafx.stage.Window;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.dev.MyBoxLog.LogType;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    protected CheckBox popCheck, debugSaveCheck, debugDetailedCheck;

    public MyBoxLogController() {
        baseTitle = Languages.message("MyBoxLogs");
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
                setStyle(NodeStyleTools.selectedData);
            } else if (item.getLogType() == LogType.Error) {
                setStyle(NodeStyleTools.errorData);
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

            debugSaveCheck.setSelected(AppVariables.saveDebugLogs);
            debugSaveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.saveDebugLogs = debugSaveCheck.isSelected();
                    UserConfig.setBoolean("SaveDebugLogs", AppVariables.saveDebugLogs);
                }
            });

            debugDetailedCheck.setSelected(AppVariables.detailedDebugLogs);
            debugDetailedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.detailedDebugLogs = debugDetailedCheck.isSelected();
                    UserConfig.setBoolean("DetailedDebugLogs", AppVariables.detailedDebugLogs);
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
            popError(Languages.message("SetConditionsComments"));
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
            popError(Languages.message("SetConditionsComments"));
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
        MyBoxLogViewerController controller = (MyBoxLogViewerController) WindowTools.openStage(Fxmls.MyBoxLogViewerFxml);
        controller.setLogs(selected);
    }

    @FXML
    public void messageAction() {
        List<MyBoxLog> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        MyBoxLogViewerController controller = (MyBoxLogViewerController) WindowTools.openStage(Fxmls.MyBoxLogViewerFxml);
        controller.setLogs(selected);
    }

    public void errors() {
        typeController.allItem.setSelected(false);
        typeController.errorItem.setSelected(true);
        orderByList.getSelectionModel().clearSelection();
        orderByList.getSelectionModel().select(Languages.message("Time") + " " + Languages.message("Descending"));
        tabsPane.getSelectionModel().select(dataTab);
        queryData();
    }

    public static MyBoxLogController oneOpen() {
        MyBoxLogController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MyBoxLogController) {
                try {
                    controller = (MyBoxLogController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MyBoxLogController) WindowTools.openStage(Fxmls.MyBoxLogsFxml);
        }
        return controller;
    }

}
