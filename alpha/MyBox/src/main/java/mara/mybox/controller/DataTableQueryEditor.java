package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-1
 * @License Apache License Version 2.0
 */
public class DataTableQueryEditor extends BaseInfoTreeNodeEditor {

    protected DataTable dataTable;

    @FXML
    protected ListView namesList;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck;
    @FXML
    protected Label dataLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            namesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (newV == null || newV.isBlank()) {
                        return;
                    }
                    valueInput.replaceText(valueInput.getSelection(), newV);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlData2DLoad tableController) {
        try {
            setDataTable(tableController.data2D);

            dataLabel.setText(dataTable.displayName());
            targetController.setParameters(this, tableController);

            rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
            rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setDataTable(Data2D data2D) {
        try {
            this.dataTable = (DataTable) data2D;
            namesList.getItems().clear();
            if (dataTable == null || !dataTable.isValid()) {
                return;
            }
            String name = dataTable.getSheet();
            if (name != null && !name.isBlank()) {
                namesList.getItems().add(name);
            }
            namesList.getItems().addAll(dataTable.columnNames());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (targetController != null && targetController.checkTarget() == null) {
            popError(message("SelectToHandle"));
            return;
        }
        String s = valueInput.getText();
        if (s == null || s.isBlank()) {
            popError(message("InvalidParameters") + ": SQL");
            return;
        }
        String query = StringTools.replaceLineBreak(s);
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private DataFileCSV dataCSV;

            @Override
            protected boolean handle() {
                TableStringValues.add("DataTableQueryHistories", query);
                dataTable.setTask(this);
                dataCSV = dataTable.query(targetController.name(), task, query,
                        rowNumberCheck.isSelected() ? message("Row") : null);
                return dataCSV != null;
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                DataFileCSV.openCSV(myController, dataCSV, targetController.target);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataTable.stopTask();
            }

        };
        start(task);
    }

    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("SqlExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        PopTools.popSqlExamples(this, valueInput,
                dataTable != null ? dataTable.getSheet() : null,
                true, event);
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean("DataTableQueryHistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popStringValues(this, valueInput, event, "DataTableQueryHistories", false, true);
    }

    @FXML
    protected void tableDefinition() {
        if (dataTable == null || dataTable.getSheet() == null) {
            popError(message("NotFound"));
            return;
        }
        String html = TableData2D.tableDefinition(dataTable.getSheet());
        if (html != null) {
            HtmlPopController.openHtml(this, html);
        } else {
            popError(message("NotFound"));
        }
    }

}
