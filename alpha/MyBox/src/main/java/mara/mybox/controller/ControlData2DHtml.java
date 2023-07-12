package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-8
 * @License Apache License Version 2.0
 */
public class ControlData2DHtml extends BaseController {

    protected List<List<String>> data;
    protected List<Data2DColumn> columns;

    @FXML
    protected ControlWebView webViewController;

    public void setParameters(BaseController parent) {
        try {
            webViewController.setParent(parent);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadData(List<Data2DColumn> columns, List<List<String>> data) {
        try {
            this.columns = columns;
            this.data = data;
            if (data == null || data.isEmpty()) {
                webViewController.clear();
                return;
            }
            List<String> names = new ArrayList<>();
            if (columns != null) {
                for (Data2DColumn c : columns) {
                    names.add(c.getColumnName());
                }
            }
            StringTable table = new StringTable(names);
            for (List<String> row : data) {
                table.add(row);
            }
            webViewController.loadContents(table.html());
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void dataAction() {
        if (data == null || data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        DataManufactureController.open(columns, data);
    }

    @FXML
    public void editAction() {
        webViewController.editAction();
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("WebviewFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.showFunctionsMenu(event);
    }

}
