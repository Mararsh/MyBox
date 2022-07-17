package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import static mara.mybox.fxml.PopTools.addButtonsPane;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
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
                DataFileCSV.openCSV(myController, dataCSV, targetController.target);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataTable.stopTask();
                task = null;
            }

        };
        start(task);
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean("SqlExamplesPopWhenMouseHovering", true)) {
            examplesMenu(mouseEvent);
        }
    }

    @FXML
    protected void showExamplesMenu(ActionEvent event) {
        examplesMenu(event);
    }

    protected void examplesMenu(Event event) {
        PopTools.popSqlExamples(this, valueInput,
                dataTable != null ? dataTable.getSheet() : null,
                true, event);
    }

    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean("DataTableQueryHistoriesPopWhenMouseHovering", true)) {
            PopTools.popStringValues(this, valueInput, mouseEvent, "DataTableQueryHistories", false, true);
        }
    }

    @FXML
    protected void showHistories(ActionEvent event) {
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

    @FXML
    protected void popColumnNames(MouseEvent event) {
        if (UserConfig.getBoolean("DataTableQueryPopWhenMouseHovering", true)) {
            columnNames(event);
        }
    }

    @FXML
    protected void showColumnNames(ActionEvent event) {
        columnNames(event);
    }

    protected void columnNames(Event event) {
        try {
            if (dataTable == null) {
                return;
            }
            List<String> values = dataTable.columnNames();
            if (values == null || values.isEmpty()) {
                return;
            }
            Point2D everntCoord = LocateTools.getScreenCoordinate(event);
            MenuController controller = MenuController.open(this, valueInput, everntCoord.getX(), everntCoord.getY());

            boolean isTextArea = valueInput instanceof TextArea;

            List<Node> topButtons = new ArrayList<>();
            if (isTextArea) {
                Button newLineButton = new Button();
                newLineButton.setGraphic(StyleTools.getIconImage("iconTurnOver.png"));
                NodeStyleTools.setTooltip(newLineButton, new Tooltip(message("Newline")));
                newLineButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        valueInput.replaceText(valueInput.getSelection(), "\n");
                        controller.getThisPane().requestFocus();
                        valueInput.requestFocus();
                    }
                });
                topButtons.add(newLineButton);
            }
            Button cButton = new Button();
            cButton.setGraphic(StyleTools.getIconImage("iconClear.png"));
            NodeStyleTools.setTooltip(cButton, new Tooltip(message("ClearInputArea")));
            cButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    valueInput.clear();
                    controller.getThisPane().requestFocus();
                    valueInput.requestFocus();
                }
            });
            topButtons.add(cButton);

            CheckBox popCheck = new CheckBox();
            popCheck.setGraphic(StyleTools.getIconImage("iconPop.png"));
            NodeStyleTools.setTooltip(popCheck, new Tooltip(message("PopWhenMouseHovering")));
            popCheck.setSelected(UserConfig.getBoolean("DataTableQueryPopWhenMouseHovering", true));
            popCheck.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("DataTableQueryPopWhenMouseHovering", popCheck.isSelected());
                }
            });
            topButtons.add(popCheck);

            controller.addFlowPane(topButtons);
            controller.addNode(new Separator());

            addButtonsPane(controller, valueInput, values, true);

            Hyperlink link = new Hyperlink("Derby Reference Manual");
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://db.apache.org/derby/docs/10.15/ref/index.html");
                }
            });
            controller.addNode(link);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
