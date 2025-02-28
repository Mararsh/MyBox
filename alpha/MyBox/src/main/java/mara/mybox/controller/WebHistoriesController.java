package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.WebHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableFileNameCell;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class WebHistoriesController extends BaseSysTableController<WebHistory> {

    protected TableWebHistory tableWebHistory;

    @FXML
    protected ControlTimesTree timesController;
    @FXML
    protected TextField findInput;
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
            iconColumn.setCellFactory(new TableFileNameCell(20));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
            timeColumn.setCellFactory(new TableDateCell());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            timesController.setParent(this, null, "Web_History", "visit_time");
            timesController.queryNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    queryTimes();
                }
            });
            timesController.refreshNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    refreshTimes();
                }
            });

            paginationController.setRightOrientation(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean none = isNoneSelected();
        goButton.setDisable(none);
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            refreshTimes();
            loadTableData();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void clearQuery() {
        queryConditions = null;
        queryConditionsString = null;
        tableData.clear();
        pagination.startRowOfCurrentPage = 0;
    }

    /*
        times
     */
    @FXML
    protected void refreshTimes() {
        timesController.loadTree();
    }

    @FXML
    protected void queryTimes() {
        String c = timesController.check();
        if (c == null) {
            popError(Languages.message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = c;
        queryConditionsString = timesController.getFinalTitle();
        loadTableData();
    }

    /*
        Find
     */
    @FXML
    protected void find() {
        String s = findInput.getText();
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Find"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(message("InvalidParameters") + ": " + message("Find"));
            return;
        }
        TableStringValues.add("WebHistoriesFindHistories", s);
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

    @FXML
    protected void showFindHistories(Event event) {
        PopTools.popSavedValues(this, findInput, event, "WebHistoriesFindHistories");
    }

    @FXML
    public void popFindHistories(Event event) {
        if (UserConfig.getBoolean("WebHistoriesFindHistoriesPopWhenMouseHovering", false)) {
            showFindHistories(event);
        }
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
        WebHistory selected = selectedItem();
        if (selected == null) {
            return;
        }
        WebBrowserController.openAddress(selected.getAddress(), true);
    }

    /*
        static methods
     */
    public static WebHistoriesController open() {
        WebHistoriesController controller = (WebHistoriesController) WindowTools.openStage(Fxmls.WebHistoriesFxml);
        if (controller != null) {
            controller.requestMouse();
        }
        return controller;
    }

    public static WebHistoriesController oneOpen() {
        WebHistoriesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebHistoriesController) {
                try {
                    controller = (WebHistoriesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebHistoriesController) WindowTools.openStage(Fxmls.WebHistoriesFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
