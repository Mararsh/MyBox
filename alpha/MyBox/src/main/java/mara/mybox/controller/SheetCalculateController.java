package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-7
 * @License Apache License Version 2.0
 */
public class SheetCalculateController extends BaseDataOperationController {

    protected String value;

    @FXML
    protected ToggleGroup calGroup;
    @FXML
    protected RadioButton transposeRadio, statisticRadio, concatRadio, sortRadio, formularRadio;
    @FXML
    protected ControlListCheckBox calColsListController;
    @FXML
    protected Button calculatorButton;
    @FXML
    protected VBox optionsBox, formularBox, statisticBox;
    @FXML
    protected HBox rowsBox, calColumnBox, calColumnsBox, columnsBox;
    @FXML
    protected Label colsLabel;
    @FXML
    protected CheckBox percentageCheck, medianCheck, modeCheck;
    @FXML
    protected TextArea formularArea;

    public SheetCalculateController() {
        baseTitle = message("Calculation");
        TipsLabelKey = "SheetCalculateTips";
    }

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            calColsListController.setParent(sheetController);

            calGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkOperation();
                }
            });
            checkOperation();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void updateControls() {
        try {
            super.updateControls();

            List<String> cols = new ArrayList<>();
            if (sheetController.columns != null) {
                for (ColumnDefinition c : sheetController.columns) {
                    if (c.isNumberType()) {
                        cols.add(c.getName());
                    }
                }
            }
            calColsListController.setValues(cols);
            statisticRadio.setDisable(cols.isEmpty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkOperation() {
        try {
            optionsBox.getChildren().clear();
            if (transposeRadio.isSelected()) {
                optionsBox.getChildren().addAll(rowsBox, columnsBox);
                colsLabel.setText(message("Columns"));

            } else if (statisticRadio.isSelected()) {
                if (calColsListController.getValues().isEmpty()) {
                    popError(message("NoNumberColumns"));
                    return false;
                }
                optionsBox.getChildren().addAll(rowsBox, statisticBox, columnsBox);
                colsLabel.setText(message("ColumnsDisplay"));
                percentageCheck.setVisible(true);

                colSelectRadio.fire();

            } else if (formularRadio.isSelected()) {
                optionsBox.getChildren().addAll(rowsBox, formularBox, columnsBox);
                colsLabel.setText(message("ColumnsDisplay"));
                percentageCheck.setVisible(true);

            }
            return true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @FXML
    public void selectAllCalCols() {
        calColsListController.checkAll();
    }

    @FXML
    public void selectNoneCalCols() {
        calColsListController.checkNone();
    }

    @FXML
    public void clearFormular() {
        formularArea.clear();
    }

    @FXML
    public void popColumns(MouseEvent mouseEvent) {
        try {
            List<Node> buttons = new ArrayList<>();
            List<ColumnDefinition> columns = sheetController.columns;
            for (ColumnDefinition column : columns) {
                String name = column.getName();
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        formularArea.insertText(formularArea.getAnchor(), " " + name + " ");
                    }
                });
                buttons.add(button);
            }
            MenuController controller = MenuController.open(this, formularArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addFlowPane(buttons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // http://commons.apache.org/proper/commons-jexl/reference/syntax.html
    @FXML
    public void popOperators(MouseEvent mouseEvent) {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (transposeRadio.isSelected()) {
            if (rowAllRadio.isSelected()) {
                sheetController.transpose(cols());
            } else {
                sheetController.transpose(rows(), cols());
            }

        } else if (statisticRadio.isSelected()) {
            List<Integer> calCols = selectedCols(calColsListController);
            if (calCols == null || calCols.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            if (rowAllRadio.isSelected()) {
                sheetController.statistic(calCols, cols(), modeCheck.isSelected(), medianCheck.isSelected(), percentageCheck.isSelected());
            } else {
                sheetController.statistic(rows(), calCols, cols(), modeCheck.isSelected(), medianCheck.isSelected(), percentageCheck.isSelected());
            }

        }

    }

}
