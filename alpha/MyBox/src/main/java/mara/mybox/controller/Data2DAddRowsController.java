package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-1
 * @License Apache License Version 2.0
 */
public class Data2DAddRowsController extends TableAddRowsController {

    protected ControlData2DEditTable editController;

    @FXML
    protected VBox valuesBox;

    public void setParameters(ControlData2DEditTable editController) {
        try {
            super.setParameters(editController);
            this.editController = editController;

            for (Data2DColumn column : editController.data2D.getColumns()) {
                HBox line = new HBox();
                line.setAlignment(Pos.TOP_CENTER);
                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                line.setSpacing(5);
                HBox.setHgrow(line, Priority.ALWAYS);
                valuesBox.getChildren().add(line);

                line.getChildren().add(new Label(column.getColumnName()));
                TextField input = new TextField();
                input.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(input, Priority.ALWAYS);
                line.getChildren().add(input);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (number < 1) {
                popError(message("InvalidParameters") + ": " + message("Number"));
                return;
            }
            int index = rowSelector.getSelectionModel().getSelectedIndex();
            if (frontRadio.isSelected()) {
                index = 0;
            } else if (index < 0 || endRadio.isSelected()) {
                index = tableViewController.tableData.size();
            } else if (belowRadio.isSelected()) {
                index++;
            }

            if (number < 1) {
                return;
            }
            if (index < 0) {
                index = tableViewController.tableData.size();
            }

            List<String> row = new ArrayList<>();
            row.add("-1");
            for (Node node : valuesBox.getChildren()) {
                HBox line = (HBox) node;
                TextField input = (TextField) (line.getChildren().get(1));
                row.add(input.getText());
            }

            isSettingValues = true;
            List<List<String>> list = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                list.add(row);
            }
            tableViewController.tableData.addAll(index, list);
            tableViewController.tableView.scrollTo(index - 5);
            isSettingValues = false;
            tableViewController.tableChanged(true);

            popSuccessful();

            setSelector();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        static
     */
    public static Data2DAddRowsController open(ControlData2DEditTable tableViewController) {
        try {
            Data2DAddRowsController controller = (Data2DAddRowsController) WindowTools.openChildStage(
                    tableViewController.getMyWindow(), Fxmls.Data2DAddRowsFxml, false);
            controller.setParameters(tableViewController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
