package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-4
 * @License Apache License Version 2.0
 */
public class ControlData2DRowEdit extends BaseController {

    protected ControlData2DEditTable dataEditController;
    protected List<TextInputControl> inputs;

    @FXML
    protected VBox valuesBox;

    public void setParameters(ControlData2DEditTable editController) {
        try {
            this.dataEditController = editController;

            inputs = new ArrayList<>();
            addInput(message("DataRowNumber"), ColumnType.Long, false);
            for (Data2DColumn column : editController.data2D.getColumns()) {
                addInput(column.getColumnName(), column.getType(), column.isEditable());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public TextInputControl addInput(String name, ColumnType type, boolean editable) {
        try {
            if (type == ColumnType.String || type == ColumnType.String) {
                Label label = new Label(name);
                label.setWrapText(true);
                valuesBox.getChildren().add(label);

                TextArea input = new TextArea();
                input.setPrefHeight(100);
                input.setMinHeight(80);
                input.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(input, Priority.ALWAYS);
                input.setDisable(!editable);
                valuesBox.getChildren().add(input);
                inputs.add(input);

                return input;

            } else {
                HBox line = new HBox();
                line.setAlignment(Pos.CENTER_LEFT);
                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                line.setSpacing(5);
                HBox.setHgrow(line, Priority.ALWAYS);
                valuesBox.getChildren().add(line);

                Label label = new Label(name);
                label.setWrapText(true);
                TextField input = new TextField();
                input.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(input, Priority.ALWAYS);
                input.setDisable(!editable);
                line.getChildren().addAll(label, input);
                inputs.add(input);

                if (editable && (type == ColumnType.Datetime || type == ColumnType.Date)) {
                    Button dateButton = new Button(message("Now"));
                    dateButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            input.setText(DateTools.nowString());
                        }
                    });
                    line.getChildren().add(dateButton);
                }
                return input;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public void setParameters(ControlData2DEditTable editController, int index) {
        setParameters(editController);
        loadRow(index);
    }

    public void loadRow(int index) {
        try {
            List<String> row = dataEditController.tableData.get(index);
            if (row == null) {
                return;
            }
            for (int i = 0; i < inputs.size(); i++) {
                TextInputControl input = (TextInputControl) (inputs.get(i));
                input.setText(row.get(i));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<String> pickValues() {
        try {
            List<String> row = new ArrayList<>();
            for (TextInputControl input : inputs) {
                row.add(input.getText());
            }
            return row;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
