package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Sheet extends ControlSheet_Columns {

    // Sheet itself can be resued and need not clear
    public void clearSheet() {
        sheetInputs = null;
        colsCheck = null;
        rowsCheck = null;
        if (sheetBox.getChildren().contains(noDataLabel)) {
            sheetBox.getChildren().remove(noDataLabel);
        }
        sheetChanged(true);
    }

    public void makeSheet(String[][] data) {
        makeSheet(data, true);
    }

    public void makeSheetWithName(String[][] data) {
        if (data != null && data.length > 0) {
            columns = new ArrayList<>();
            for (String name : data[0]) {
                ColumnDefinition column = new ColumnDefinition(name, defaultColumnType, defaultColNotNull);
                columns.add(column);
            }
            if (data.length > 1) {
                String[][] vdata = new String[data.length - 1][columns.size()];
                for (int i = 0; i < data.length - 1; i++) {
                    vdata[i] = data[i + 1];
                }
                makeSheet(vdata, columns);
            } else {
                makeSheet(null, columns);
            }
        } else {
            makeSheet(data, null);
        }
    }

    @Override
    public void makeSheet(String[][] data, List<ColumnDefinition> columns) {
        this.columns = columns;
        makeSheet(data, true);
    }

    public synchronized void makeSheet(String[][] data, boolean changed) {
        if (isSettingValues) {
            return;
        }
        LoadingController loading;
        if (sheetTab != null && !sheetTab.isSelected()) {
            loading = null;
        } else {
            loading = handling();
        }
        Platform.runLater(() -> {
            try {
                clearSheet();
                int rowsSize = data == null ? 0 : data.length;
                int colsSize = data == null || rowsSize == 0 ? 0 : data[0].length;
                if (colsSize > 0) {
                    if (dataType != DataType.Matrix) {
                        if (columns == null) {
                            columns = new ArrayList<>();
                        }
                        if (columns.size() < colsSize) {
                            makeColumns(columns.size(), colsSize - columns.size());
                        } else if (columns.size() > colsSize) {
                            for (int col = columns.size() - 1; col > colsSize; col--) {
                                columns.remove(col);
                            }
                        }
                    }
                } else if (columns != null) {
                    colsSize = columns.size();
                }
                if (dataType == DataType.Matrix) {
                    makeColumns(colsSize);
                }
                double space = 0.0;
                double rowCheckWidth = 80 + (rowsSize + "").length() * AppVariables.sceneFontSize;
                sheetBox.setAlignment(Pos.TOP_CENTER);
                sheetBox.setSpacing(space);
                HBox header;
                Label col0Label;
                if (!sheetBox.getChildren().isEmpty()) {
                    header = (HBox) (sheetBox.getChildren().get(0));
                    col0Label = (Label) (header.getChildren().get(0));
                } else {
                    header = new HBox();
                    header.setAlignment(Pos.CENTER);
                    header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    header.setSpacing(space);
                    col0Label = new Label(Languages.message("Col") + "0");
                    col0Label.setPrefHeight(header.getHeight());
                    col0Label.setAlignment(Pos.CENTER);
                    col0Label.hoverProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                        if (newValue) {
                            col0Label.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;-fx-text-fill: blue;");
                            if (overPopMenuCheck.isSelected()) {
                                popRowLabelMenu(col0Label);
                            }
                        } else {
                            col0Label.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
                        }
                    });
                    col0Label.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (event.getButton() == MouseButton.SECONDARY && rightClickPopMenuCheck.isSelected()) {
                                popRowLabelMenu(col0Label);
                            }
                        }
                    });
                    header.getChildren().add(col0Label);
                    sheetBox.getChildren().add(header);
                }
                col0Label.setPrefWidth(rowCheckWidth);
                int currentRowsSize = sheetBox.getChildren().size() - 1;
                int currentColsSize = header.getChildren().size() - 1;
                if (currentColsSize > colsSize) {
                    header.getChildren().remove(colsSize + 1, currentColsSize + 1);
                }
                if (currentRowsSize > rowsSize) {
                    sheetBox.getChildren().remove(rowsSize + 1, currentRowsSize + 1);
                }
                if (colsSize <= 0) {
                    colsCheck = null;
                    rowsCheck = null;
                    sheetInputs = null;
                } else {
                    colsCheck = new CheckBox[colsSize];
                    for (int col = 0; col < colsSize; ++col) {
                        CheckBox colCheck;
                        if (col < currentColsSize) {
                            colCheck = (CheckBox) (header.getChildren().get(col + 1));
                        } else {
                            colCheck = new CheckBox();
                            colCheck.setAlignment(Pos.CENTER);
                            colCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
                            int colf = col;
                            colCheck.hoverProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                                if (newValue) {
                                    colCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;-fx-text-fill: blue;");
                                    if (overPopMenuCheck.isSelected()) {
                                        popColMenu(colCheck, colf);
                                    }
                                } else {
                                    colCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
                                }
                            });
                            colCheck.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    if (event.getButton() == MouseButton.SECONDARY && rightClickPopMenuCheck.isSelected()) {
                                        popColMenu(colCheck, colf);
                                    }
                                }
                            });
                            header.getChildren().add(colCheck);
                        }
                        colCheck.setPrefWidth(columns.get(col).getWidth());
                        colCheck.setText(colName(col));
                        colCheck.setSelected(false);
                        colsCheck[col] = colCheck;
                    }
                    if (rowsSize <= 0) {
                        rowsCheck = null;
                        sheetInputs = null;
                    } else {
                        sheetInputs = new TextField[rowsSize][colsSize];
                        rowsCheck = new CheckBox[rowsSize];
                        for (int row = 0; row < rowsSize; ++row) {
                            HBox line;
                            CheckBox rowCheck;
                            int rowf = row;
                            if (row < currentRowsSize) {
                                line = (HBox) (sheetBox.getChildren().get(row + 1));
                                rowCheck = (CheckBox) (line.getChildren().get(0));
                                if (currentColsSize > colsSize) {
                                    line.getChildren().remove(colsSize + 1, currentColsSize + 1);
                                }
                            } else {
                                line = new HBox();
                                line.setAlignment(Pos.CENTER);
                                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                                line.setSpacing(space);
                                VBox.setVgrow(line, Priority.NEVER);
                                HBox.setHgrow(line, Priority.NEVER);
                                rowCheck = new CheckBox();
                                rowCheck.setAlignment(Pos.CENTER_LEFT);
                                rowCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
                                rowCheck.hoverProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                                    if (newValue) {
                                        rowCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;-fx-text-fill: blue;");
                                        if (overPopMenuCheck.isSelected()) {
                                            popRowMenu(rowCheck, rowf);
                                        }
                                    } else {
                                        rowCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
                                    }
                                });
                                rowCheck.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent event) {
                                        if (event.getButton() == MouseButton.SECONDARY && rightClickPopMenuCheck.isSelected()) {
                                            popRowMenu(rowCheck, rowf);
                                        }
                                    }
                                });
                                line.getChildren().add(rowCheck);
                                sheetBox.getChildren().add(line);
                            }
                            rowCheck.setText(rowName(row));
                            rowCheck.setPrefWidth(rowCheckWidth);
                            rowCheck.setSelected(false);
                            rowsCheck[row] = rowCheck;
                            for (int col = 0; col < colsSize; ++col) {
                                TextField valueInput;
                                if (row < currentRowsSize && col < currentColsSize) {
                                    valueInput = (TextField) (line.getChildren().get(col + 1));
                                } else {
                                    valueInput = new TextField();
                                    int colf = col;
                                    valueInput.textProperty().addListener(new ChangeListener<String>() {
                                        @Override
                                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                                            cellInputted(rowf, colf);
                                        }
                                    });
                                    valueInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                                        @Override
                                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                                            cellFocused(rowf, colf);
                                        }
                                    });
                                    line.getChildren().add(valueInput);
                                }
                                String v = data[row][col];
                                v = v == null ? defaultColValue : v;
                                isSettingValues = true;
                                valueInput.setText(v);
                                valueInput.setStyle(inputStyle + (cellValid(col, v) ? "" : NodeStyleTools.badStyle));
                                valueInput.setPrefWidth(columns.get(col).getWidth());
                                isSettingValues = false;
                                sheetInputs[row][col] = valueInput;
                            }
                        }

                    }
                }
                if (sheetInputs == null) {
                    noDataLabel.setPrefWidth(rowCheckWidth);
                    noDataLabel.setPrefHeight(header.getHeight());
                    noDataLabel.setAlignment(Pos.CENTER);
                    sheetBox.getChildren().add(noDataLabel);
                }
                refreshStyle(sheetBox);
                sheetChanged(changed);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
            if (loading != null) {
                loading.closeStage();
            }
        });

    }

    public void newSheet(int rows, int cols) {
        try {
            sourceFile = null;
            columns = null;
            sheetBox.getChildren().clear();
            makeSheet(new String[rows][cols], false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void cellInputted(int row, int col) {
        try {
            if (isSettingValues || sheetInputs == null) {
                return;
            }
            TextField input = sheetInputs[row][col];
            String value = cellString(row, col);
            if (cellValid(col, value)) {
                input.setStyle(inputStyle);
            } else {
                input.setStyle(inputStyle + NodeStyleTools.badStyle);
                popError(Languages.message("Row") + " " + (row + 1) + " " + Languages.message("Column") + " " + (col + 1)
                        + " " + (value == null || value.isBlank() ? Languages.message("Null") : Languages.message("InvalidValue")));
            }
            sheetChanged();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void cellFocused(int row, int col) {
        try {
            if (isSettingValues) {
                return;
            }
            currentRow = row;
            currentCol = col;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized void sheetChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        pickData();
        dataChangedNotify.set(changed);
        sheetChangedNotify.set(!sheetChangedNotify.get());
        setDisplayData();
    }

    public void sheetChanged() {
        sheetChanged(true);
    }

    public String askValue(String header, String name, String initValue) {
        String value = PopTools.askValue(baseTitle, header, name, initValue);
        if (value == null) {
            return null;
        }
        return value;
    }

    protected long rowsTotal() {
        if (pagesNumber <= 1) {
            return pageData == null ? 0 : pageData.length;
        } else {
            return totalSize;
        }
    }

    protected long pageStart() {
        return currentPageStart;
    }

    protected long pageEnd() {
        return pageStart() + (pageData == null ? 0 : pageData.length);
    }

    protected List<String> row(int row) {
        List<String> values = new ArrayList<>();
        try {
            for (TextField input : sheetInputs[row]) {
                values.add(input.getText());
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
        return values;
    }

    protected List<String> col(int col) {
        List<String> values = new ArrayList<>();
        try {
            for (TextField[] row : sheetInputs) {
                values.add(row[col].getText());
            }
        } catch (Exception e) {
        }
        return values;
    }

    protected String[][] pickData() {
        pageData = null;
        rowsNumber = colsNumber = 0;
        if (sheetInputs == null || sheetInputs.length == 0) {
            return null;
        }
        rowsNumber = sheetInputs.length;
        colsNumber = sheetInputs[0].length;
        if (colsNumber == 0) {
            return null;
        }
        String[][] data = new String[sheetInputs.length][sheetInputs[0].length];
        for (int r = 0; r < rowsNumber; r++) {
            for (int c = 0; c < colsNumber; c++) {
                data[r][c] = cellString(r, c);
            }
        }
        pageData = data;
        return data;
    }

    @Override
    protected String cellString(int row, int col) {
        String value = null;
        try {
            value = sheetInputs[row][col].getText();
            if (value != null && columns.get(col).isNumberType()) {
                value = value.replaceAll(",", "");
            }
        } catch (Exception e) {
        }
        return value == null ? defaultColValue : value;
    }

    public boolean colsSelected() {
        if (colsCheck != null) {
            for (int j = 0; j < colsCheck.length; ++j) {
                if (colsCheck[j].isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean rowsSelected() {
        if (rowsCheck != null) {
            for (int j = 0; j < rowsCheck.length; ++j) {
                if (rowsCheck[j].isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
        abstract
     */
    protected abstract void setDisplayData();

    public abstract void popRowLabelMenu(Label label);

    public abstract void popColMenu(CheckBox label, int col);

    public abstract void popRowMenu(CheckBox label, int row);

}
