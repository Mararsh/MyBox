package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.fxml.TableImageFileCell;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class WebHistoriesController extends BaseDataTableController<WebHistory> {

    protected TableWebHistory tableWebHistory;

    @FXML
    protected ControlTimeTree timeController;
    @FXML
    protected ControlStringSelector searchController;
    @FXML
    protected RadioButton titleRadio, addressRadio;
    @FXML
    protected TableColumn<WebHistory, String> iconColumn, titleColumn, addressColumn;
    @FXML
    protected TableColumn<WebHistory, Date> timeColumn;

    public WebHistoriesController() {
        baseTitle = AppVariables.message("WebHistories");
    }

    @Override
    public void setTableDefinition() {
        tableWebHistory = new TableWebHistory();
        tableDefinition = tableWebHistory;
    }

    @Override
    protected void initColumns() {
        try {
            iconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
            iconColumn.setCellFactory(new TableImageFileCell(20));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
            timeColumn.setCellFactory(new TableDateCell());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            timeController.setParent(this, false);
            timeController.queryNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    queryTimes();
                }
            });
            timeController.refreshNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    refreshTimes();
                }
            });

            searchController.init(this, baseName + "Saved", null, 20);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void checkSelected() {
        super.checkSelected();
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        goButton.setDisable(selection == 0);
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            refreshTimes();
            loadTableData();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void clearQuery() {
        queryConditions = null;
        queryConditionsString = null;
        tableData.clear();
        currentPageStart = 1;
    }

    /*
        times
     */
    @FXML
    protected void refreshTimes() {
        synchronized (this) {
            timeController.clearTree();
            SingletonTask timesTask = new SingletonTask<Void>() {
                private List<Date> times;

                @Override
                protected boolean handle() {
                    times = TableWebHistory.times();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    timeController.loadTree("visit_time", times, false);
                }

            };
            timesTask.setSelf(timesTask);
            Thread thread = new Thread(timesTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void queryTimes() {
        String c = timeController.check();
        if (c == null) {
            popError(message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = c;
        queryConditionsString = timeController.getFinalTitle();
        loadTableData();
    }

    /*
        Search
     */
    @FXML
    protected void search() {
        String s = searchController.value();
        if (s == null || s.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(message("InvalidData"));
            return;
        }
        searchController.refreshList();
        clearQuery();
        if (titleRadio.isSelected()) {
            queryConditions = null;
            queryConditionsString = message("Title") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( title like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }

        } else {
            queryConditions = null;
            queryConditionsString = message("Address") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( address like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }
        }
        loadTableData();
    }

    /*
        table
     */
    @Override
    public void itemDoubleClicked() {
        goAction();
    }

    @FXML
    @Override
    public void goAction() {
        WebHistory selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        WebBrowserController.oneOpen(selected.getAddress());
    }

    /*
        static methods
     */
    public static WebHistoriesController oneOpen() {
        WebHistoriesController controller = null;
        Stage stage = FxmlStage.findStage(message("WebHistories"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (WebHistoriesController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (WebHistoriesController) FxmlStage.openStage(CommonValues.WebHistoriesFxml);
        }
        controller.getMyStage().toFront();
        controller.getMyStage().requestFocus();
        return controller;
    }

}
