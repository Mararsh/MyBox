package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.BrowserHistory;
import mara.mybox.db.table.TableBrowserHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TableImageFileCell;
import mara.mybox.fxml.TableTimeCell;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class WebBrowserHistoryController extends BaseDataTableController<BrowserHistory> {

    protected WebBrowserController browserConroller;

    @FXML
    protected TableColumn<BrowserHistory, String> iconColumn, titleColumn, addressColumn;
    @FXML
    protected TableColumn<BrowserHistory, Long> timeColumn;
    @FXML
    protected Button openButton;

    public WebBrowserHistoryController() {
    }

    @Override
    protected void initColumns() {
        try {

            iconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
            iconColumn.setCellFactory(new TableImageFileCell(20));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
            timeColumn.setCellFactory(new TableTimeCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        loadTableData();
    }

    @Override
    public void loadTableData() {
        tableData.clear();
        tableData.addAll(TableBrowserHistory.read());
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        BrowserHistory selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deleteButton.setDisable(true);
            openButton.setDisable(true);
        } else {
            deleteButton.setDisable(false);
            openButton.setDisable(false);
        }
    }

    @Override
    public void itemDoubleClicked() {
        openAction();
    }

    @Override
    protected int deleteData(List<BrowserHistory> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        return TableBrowserHistory.delete(data);
    }

    @Override
    protected int clearData() {
        return new TableBrowserHistory().clear();
    }

    @FXML
    protected void openAction() {
        BrowserHistory selected = tableView.getSelectionModel().getSelectedItem();
        if (parentController == null || selected == null) {
            return;
        }
        browserConroller.newTabAction(selected.getAddress(), true);
    }

}
