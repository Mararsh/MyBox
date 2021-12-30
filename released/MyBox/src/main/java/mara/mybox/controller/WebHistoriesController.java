package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableImageFileCell;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class WebHistoriesController extends BaseSysTableController<WebHistory> {

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
        baseTitle = Languages.message("WebHistories");
    }

    @Override
    public void setTableDefinition() {
        tableWebHistory = new TableWebHistory();
        tableDefinition = tableWebHistory;
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
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
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        goButton.setDisable(none);
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
        startRowOfCurrentPage = 0;
    }

    /*
        times
     */
    @FXML
    protected void refreshTimes() {
        synchronized (this) {
            timeController.clearTree();
            SingletonTask timesTask = new SingletonTask<Void>(this) {
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
            start(timesTask, false);
        }
    }

    @FXML
    protected void queryTimes() {
        String c = timeController.check();
        if (c == null) {
            popError(Languages.message("MissTime"));
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
            popError(Languages.message("InvalidData"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(Languages.message("InvalidData"));
            return;
        }
        searchController.refreshList();
        clearQuery();
        if (titleRadio.isSelected()) {
            queryConditions = null;
            queryConditionsString = Languages.message("Title") + ":";
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
            queryConditionsString = Languages.message("Address") + ":";
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
        WebBrowserController.oneOpen(selected.getAddress(), true);
    }

    /*
        static methods
     */
    public static WebHistoriesController oneOpen() {
        WebHistoriesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebHistoriesController) {
                try {
                    controller = (WebHistoriesController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebHistoriesController) WindowTools.openStage(Fxmls.WebHistoriesFxml);
        }
        return controller;
    }

}
