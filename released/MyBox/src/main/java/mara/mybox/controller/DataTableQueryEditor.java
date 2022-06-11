package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-1
 * @License Apache License Version 2.0
 */
public class DataTableQueryEditor extends TreeNodeEditor {

    protected ControlData2DEditTable tableController;
    protected DataTable dataTable;

    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected CheckBox rowNumberCheck;
    @FXML
    protected Label dataLabel;

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            dataTable = (DataTable) tableController.data2D;

            dataLabel.setText(dataTable.displayName());
            targetController.setParameters(this, null);

            rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "CopyRowNumber", false));
            rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyRowNumber", rowNumberCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        String query = s.replaceAll("\n", " ");
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV dataCSV;

            @Override
            protected boolean handle() {
                TableStringValues.add("DataTableQueryHistories", query);
                dataTable.setTask(task);
                dataCSV = dataTable.query(query, rowNumberCheck.isSelected());
                return dataCSV != null;
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                DataFileCSV.open(myController, dataCSV, targetController.target);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataTable.setTask(null);
                task = null;
            }

        };
        start(task);
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        PopTools.popSqlExamples(this, valueInput,
                dataTable != null ? dataTable.getSheet() : null,
                true, mouseEvent);
    }

    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, valueInput, mouseEvent, "DataTableQueryHistories");
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

    @FXML
    protected void popColumnNames(MouseEvent mouseEvent) {
        if (dataTable == null) {
            return;
        }
        PopTools.popStringValues(this, valueInput, mouseEvent, dataTable.columnNames());
    }

}
