package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.dev.MyBoxLog.LogType;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class MyBoxLogController extends BaseSysTable2Controller<MyBoxLog> {

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
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton errorRadio, debugRadio, infoRadio, allRadio;
    @FXML
    protected ControlWebView viewController;

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
            super.initColumns();
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
            MyBoxLog.error(e);
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
                setStyle(NodeStyleTools.selectedDataStyle());
            } else if (item.getLogType() == LogType.Error) {
                setStyle(NodeStyleTools.errorDataStyle());
            } else {
                setStyle(null);
            }
        }
    };

    @Override
    public void initControls() {
        try {
            super.initControls();

            viewController.setParent(this);
            viewController.initStyle = HtmlStyles.styleValue("Table");

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    loadLogs();
                }
            });

            loadLogs();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadLogs() {
        try {
            if (errorRadio.isSelected()) {
                queryConditions = " log_type=" + LogType.Error.ordinal();
            } else if (debugRadio.isSelected()) {
                queryConditions = " log_type=" + LogType.Debug.ordinal();
            } else if (infoRadio.isSelected()) {
                queryConditions = " log_type=" + LogType.Info.ordinal();
            } else {
                queryConditions = null;
            }

            loadTableData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void itemClicked() {
        loadItem();
    }

    @Override
    public void itemDoubleClicked() {
        popAction();
    }

    @FXML
    @Override
    public void viewAction() {
        List<MyBoxLog> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        MyBoxLogViewerController controller = (MyBoxLogViewerController) WindowTools.openStage(Fxmls.MyBoxLogViewerFxml);
        controller.setLogs(selected);
    }

    public void loadItem() {
        MyBoxLog selected = selectedItem();
        if (selected == null) {
            return;
        }
        viewController.loadContent(tableDefinition.htmlTable(selected).html());
    }

    @FXML
    @Override
    public boolean popAction() {
        HtmlPopController.openWebView(this, viewController.webView);
        return true;
    }

    @FXML
    public void popOptionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "OptionsPopWhenMouseHovering", true)) {
            showOptionsMenu(event);
        }
    }

    @FXML
    public void showOptionsMenu(Event mevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            CheckMenuItem popErrorLogsItem = new CheckMenuItem(message("PopErrorLogs"));
            popErrorLogsItem.setSelected(AppVariables.popErrorLogs);
            popErrorLogsItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.popErrorLogs = popErrorLogsItem.isSelected();
                    UserConfig.setBoolean("PopErrorLogs", AppVariables.popErrorLogs);
                }
            });
            items.add(popErrorLogsItem);

            CheckMenuItem saveDebugLogsItem = new CheckMenuItem(message("SaveDebugLogs"));
            saveDebugLogsItem.setSelected(AppVariables.saveDebugLogs);
            saveDebugLogsItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.saveDebugLogs = saveDebugLogsItem.isSelected();
                    UserConfig.setBoolean("SaveDebugLogs", AppVariables.saveDebugLogs);
                }
            });
            items.add(saveDebugLogsItem);

            CheckMenuItem detailedDebugLogsItem = new CheckMenuItem(message("DetailedDebugLogs"));
            detailedDebugLogsItem.setSelected(AppVariables.detailedDebugLogs);
            detailedDebugLogsItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.detailedDebugLogs = detailedDebugLogsItem.isSelected();
                    UserConfig.setBoolean("DetailedDebugLogs", AppVariables.detailedDebugLogs);
                }
            });
            items.add(detailedDebugLogsItem);

            items.add(new SeparatorMenuItem());

            CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                    StyleTools.getIconImageView("iconPop.png"));
            popItem.setSelected(UserConfig.getBoolean(baseName + "OptionsPopWhenMouseHovering", true));
            popItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "OptionsPopWhenMouseHovering", popItem.isSelected());
                }
            });
            items.add(popItem);

            popEventMenu(mevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void dataManufacture() {
        DataTable dataTable = new DataInternalTable();
        dataTable.setDataName("MyBox_Log").setSheet("MyBox_Log");
        Data2DManufactureController.openDef(dataTable);
    }

    /*
        static
     */
    public static MyBoxLogController oneOpen() {
        MyBoxLogController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MyBoxLogController) {
                try {
                    controller = (MyBoxLogController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MyBoxLogController) WindowTools.openStage(Fxmls.MyBoxLogsFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
