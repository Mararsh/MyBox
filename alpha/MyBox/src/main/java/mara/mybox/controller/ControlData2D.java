package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import mara.mybox.data.Data2D;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 *
 */
public class ControlData2D extends BaseController {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editTab, viewTab, defineTab;
    @FXML
    protected ControlData2DEdit editController;
    @FXML
    protected ControlData2DView viewController;
    @FXML
    protected ControlData2DDefine defineController;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableController = editController.tableController;

            tableData2DDefinition = new TableData2DDefinition();
            tableData2DColumn = new TableData2DColumn();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // parent should call this before initControls()
    public void setDataType(Data2D.Type type) {
        try {
            data2D = Data2D.create(type);
            data2D.setTableData2DDefinition(tableData2DDefinition);
            data2D.setTableData2DColumn(tableData2DColumn);
            data2D.setTableController(tableController);
            data2D.setTableData(tableController.tableData);

            editController.setParameters(this);
            viewController.setParameters(this);
            defineController.setParameters(this);

            data2D.getTableChangedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        viewController.loadData();
                    });

            data2D.getPageLoadedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        dataChanged();
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void dataChanged() {

    }

    public void loadData() {
        tableController.loadData();
        defineController.loadTableData();
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            data2D.resetData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        paigination
     */
    @FXML
    public void goPage() {
        tableController.goPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        tableController.pageNextAction();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        tableController.pagePreviousAction();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        tableController.pageFirstAction();
    }

    @FXML
    @Override
    public void pageLastAction() {
        tableController.pageLastAction();
    }

    protected void updateLabel() {
        tableController.updateSizeLabel();
    }

    /*
        interface
     */
    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            try {
                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                if (tab == editTab) {
                    return editController.keyEventsFilter(event);

                } else if (tab == viewTab) {
                    return viewController.keyEventsFilter(event);

                } else if (tab == defineTab) {
                    return defineController.keyEventsFilter(event);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return false;
        } else {
            return true;
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
//                HtmlPopController.openWebView(this, webView);
                return true;

            } else if (tab == editTab) {
//                MarkdownPopController.open(this, markdownArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {

                return true;

            } else if (tab == editTab) {

                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == editTab) {
//                Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
//                MenuWebviewController.pop(webViewController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == viewTab) {
//                Point2D localToScreen = codesArea.localToScreen(codesArea.getWidth() - 80, 80);
//                MenuHtmlCodesController.open(this, codesArea, localToScreen.getX(), localToScreen.getY());
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            tableController = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
