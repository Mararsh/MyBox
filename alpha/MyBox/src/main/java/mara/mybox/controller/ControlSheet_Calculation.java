package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Calculation extends ControlSheet_TextsDisplay {

    protected List<ColumnDefinition> numberColumns;

    public void initCalculationControls() {
        try {
            calGroup.selectedToggleProperty().addListener((ChangeListener<Toggle>) (observable, ov, nv) -> {
                checkCalculation();
            });

            calculatorButton.disableProperty().bind(calculationColumnsArea.textProperty().isEmpty());

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void checkCalculation() {
        try {
            if (isSettingValues) {
                return;
            }
            calculationColumnsArea.clear();
            displayColumnsArea.clear();

            if (ascendingRadio.isSelected() || descendingRadio.isSelected()) {
                calColumnsLabel.setText(message("OrderColumn"));
            } else {
                calColumnsLabel.setText(message("ColumnsCalculationSeparateByComma"));
            }

            calColumnsBox.setDisable(copyRadio.isSelected());

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void updateCalculation() {
        try {
            if (rowFromSelector == null) {
                return;
            }
            rowFromSelector.getItems().clear();
            rowToSelector.getItems().clear();
            long total = rowsTotal();
            List<String> indices = new ArrayList<>();
            long step = total > 1000 ? total / 1000 : 1;
            for (long i = 1; i <= total; i += step) {
                indices.add(i + "");
            }
            rowFromSelector.getItems().addAll(indices);
            rowToSelector.getItems().addAll(indices);
            selectAllRows();

            numberColumns = new ArrayList<>();
            if (columns != null) {
                for (ColumnDefinition column : columns) {
                    if (column.isNumberType()) {
                        numberColumns.add(column);
                    }
                }
            }
            isSettingValues = true;
            if (numberColumns.isEmpty()) {
                sumRadio.setDisable(true);
                addRadio.setDisable(true);
                subRadio.setDisable(true);
                multiplyRadio.setDisable(true);
                ascendingRadio.fire();
            } else {
                sumRadio.setDisable(false);
                addRadio.setDisable(false);
                subRadio.setDisable(false);
                multiplyRadio.setDisable(false);
                sumRadio.fire();
            }
            isSettingValues = false;
            checkCalculation();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void calculationAction() {
        try {
            int from = Integer.valueOf(rowFromSelector.getValue());
            int to = Integer.valueOf(rowToSelector.getValue());
            if (from > to) {
                popError(message("InvalidParameters"));
                return;
            }

            String[] calCols = calculationColumnsArea.getText().split(",");
            List<Integer> calculationColumns = new ArrayList<>();
            for (String v : calCols) {
                for (int c = 0; c < columns.size(); c++) {
                    ColumnDefinition col = columns.get(c);
                    if (col.getName().equals(v.trim())) {
                        calculationColumns.add(c);
                    }
                }
            }
            if (calculationColumns.isEmpty()) {
                popError(message("InvalidParameters"));
                return;
            }

            String[] disCols = displayColumnsArea.getText().split(",");
            List<Integer> displayColumns = new ArrayList<>();
            for (String v : disCols) {
                for (int c = 0; c < columns.size(); c++) {
                    ColumnDefinition col = columns.get(c);
                    if (col.getName().equals(v.trim())) {
                        displayColumns.add(c);
                    }
                }
            }

            if (sumRadio.isSelected()) {
                sum(calculationColumns, displayColumns, from, to);
            } else if (addRadio.isSelected()) {

            }

        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void selectAllRows() {
        rowFromSelector.getSelectionModel().select("1");
        rowToSelector.getSelectionModel().select(rowsTotal() + "");
    }

    @FXML
    public void popCalculationColumn(MouseEvent mouseEvent) {
        try {
            List<Node> columButtons = new ArrayList<>();
            List<ColumnDefinition> popColumns = columns;
            if (sumRadio.isSelected() || addRadio.isSelected() || subRadio.isSelected() || multiplyRadio.isSelected()) {
                popColumns = numberColumns;
            }
            if (popColumns == null || popColumns.isEmpty()) {
                return;
            }
            for (ColumnDefinition column : popColumns) {
                String name = column.getName();
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (ascendingRadio.isSelected() || descendingRadio.isSelected()) {
                            calculationColumnsArea.setText(name);
                        } else {
                            calculationColumnsArea.insertText(calculationColumnsArea.getAnchor(), name);
                        }
                    }
                });
                columButtons.add(button);
            }

            List<Node> otherButtons = new ArrayList<>();
            if (!ascendingRadio.isSelected() && !descendingRadio.isSelected()) {
                Button commaButton = new Button(message("Comma"));
                commaButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        calculationColumnsArea.insertText(calculationColumnsArea.getAnchor(), ",");
                    }
                });
                otherButtons.add(commaButton);
            }

            Button clearTButton = new Button(message("Clear"));
            clearTButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    calculationColumnsArea.clear();
                }
            });
            otherButtons.add(clearTButton);

            MenuController controller = MenuController.open(this, calculationColumnsArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addFlowPane(columButtons);
            controller.addFlowPane(otherButtons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popDisplayColumn(MouseEvent mouseEvent) {
        try {
            List<Node> columButtons = new ArrayList<>();
            for (ColumnDefinition column : columns) {
                String name = column.getName();
                Button button = new Button(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        displayColumnsArea.insertText(displayColumnsArea.getAnchor(), name);
                    }
                });
                columButtons.add(button);
            }

            List<Node> otherButtons = new ArrayList<>();
            Button commaButton = new Button(message("Comma"));
            commaButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    displayColumnsArea.insertText(displayColumnsArea.getAnchor(), ",");
                }
            });
            otherButtons.add(commaButton);

            Button clearTButton = new Button(message("Clear"));
            clearTButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    displayColumnsArea.clear();
                }
            });
            otherButtons.add(clearTButton);

            MenuController controller = MenuController.open(this, displayColumnsArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addFlowPane(columButtons);
            controller.addFlowPane(otherButtons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void copyCalculationResults() {

    }

    @FXML
    public void editCalulationResults() {

    }

    // 1-based, include
    public void sum(List<Integer> calculationColumns, List<Integer> displayColumns, int intFrom, int inTo) {
        try {
            if (calculationColumns == null || calculationColumns.isEmpty()
                    || pageData == null || intFrom > inTo) {
                popError(message("InvalidParameters"));
                return;
            }
            int from = Math.min(pageData.length, Math.max(1, intFrom));
            int to = Math.min(pageData.length, Math.max(1, inTo));
            if (from > to) {
                popError(message("InvalidParameters"));
                return;
            }
            MyBoxLog.console(from + " " + to);
            int calSize = calculationColumns.size();
            int displaySize = displayColumns.size();
            List<ColumnDefinition> dataColumns = new ArrayList<>();
            dataColumns.add(new ColumnDefinition(message("Calculation"), ColumnType.String));
            for (int c : calculationColumns) {
                ColumnDefinition def = columns.get(c);
                if (!def.isNumberType()) {
                    popError(message("InvalidParameters"));
                    return;
                }
                dataColumns.add(new ColumnDefinition(def.getName(), ColumnType.Double));
            }
            for (int c : displayColumns) {
                dataColumns.add(columns.get(c));
            }

            String[][] data = new String[to - from + 2][calSize + displaySize + 1];
            data[0][0] = message("Total");
            for (int c = 0; c < calSize; ++c) {
                double sum = 0;
                int colIndex = calculationColumns.get(c);
                for (int r = from - 1; r <= to - 1; ++r) {
                    try {
                        sum += Double.valueOf(pageData[r][colIndex]);
                    } catch (Exception e) {
                    }
                }
                data[0][c + 1] = DoubleTools.format(sum, 2);
            }
            for (int c = 0; c < displaySize; ++c) {
                data[0][c + calSize + 1] = "";
            }
            for (int r = 1; r <= to - from + 1; ++r) {
                data[r][0] = null;
            }
            for (int c = 0; c < calSize; ++c) {
                int colIndex = calculationColumns.get(c);
                for (int r = 1; r <= to - from + 1; ++r) {
                    data[r][c + 1] = pageData[r + from - 2][colIndex];
                }
            }
            for (int c = 0; c < displaySize; ++c) {
                int colIndex = displayColumns.get(c);
                for (int r = 1; r <= to - from + 1; ++r) {
                    data[r][c + calSize + 1] = pageData[r + from - 2][colIndex];
                }
            }
            DataClipboardController.open(data, dataColumns);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
