package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController_Input extends BaseSheetController_Base {

    protected long rowsTotal() {
        return sheet == null ? 0 : sheet.length;
    }

    @Override
    protected String[][] pickData() {
        sheet = null;
        rowsNumber = colsNumber = 0;
        if (inputs == null || inputs.length == 0) {
            return null;
        }
        rowsNumber = inputs.length;
        colsNumber = inputs[0].length;
        if (colsNumber == 0) {
            return null;
        }
        String[][] data = new String[inputs.length][inputs[0].length];
        for (int r = 0; r < rowsNumber; r++) {
            for (int c = 0; c < colsNumber; c++) {
                data[r][c] = value(r, c);
            }
        }
        sheet = data;
        return data;
    }

    protected String value(int row, int col) {
        String value = null;
        try {
            value = inputs[row][col].getText();
            if (value != null && columns.get(col).isNumberType()) {
                value = value.replaceAll(",", "");
            }
        } catch (Exception e) {
        }
        return value == null ? defaultColValue : value;
    }

    protected List<String> row(int row) {
        List<String> values = new ArrayList<>();
        try {
            for (TextField input : inputs[row]) {
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
            for (TextField[] row : inputs) {
                values.add(row[col].getText());
            }
        } catch (Exception e) {
        }
        return values;
    }

    @Override
    protected String colName(int col) {
        try {
            if (columns == null && colsCheck != null) {
                makeColumns(colsCheck.length);
            }
            if (columns == null || columns.size() <= col) {
                return null;
            }
            return columns.get(col).getName();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected List<String> columnNames() {
        try {
            if (columns == null && colsCheck != null) {
                makeColumns(colsCheck.length);
            }
            return super.columnNames();
        } catch (Exception e) {
            return null;
        }
    }

    // Sheet itself can be resued and need not clear
    public void clearSheet() {
        inputs = null;
        colsCheck = null;
        rowsCheck = null;
        rowsSelected = colsSelected = false;
        if (sheetBox.getChildren().contains(noDataLabel)) {
            sheetBox.getChildren().remove(noDataLabel);
        }
        sheetDisplayController.setDisplayData(null, columns);
        notify.set(!notify.get());
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
                            popRowLabelMenu(col0Label);
                        } else {
                            col0Label.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
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
                    inputs = null;
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
                                    popColMenu(colCheck, colf);
                                } else {
                                    colCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
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
                        inputs = null;
                    } else {
                        inputs = new TextField[rowsSize][colsSize];
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
                                        popRowMenu(rowCheck, rowf);
                                    } else {
                                        rowCheck.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
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
                                            valueInputted(rowf, colf);
                                        }
                                    });
                                    line.getChildren().add(valueInput);
                                }
                                String v = data[row][col];
                                v = v == null ? defaultColValue : v;
                                isSettingValues = true;
                                valueInput.setText(v);
                                valueInput.setStyle(inputStyle + (dataValid(col, v) ? "" : NodeStyleTools.badStyle));
                                valueInput.setPrefWidth(columns.get(col).getWidth());
                                isSettingValues = false;
                                inputs[row][col] = valueInput;
                            }
                        }

                    }
                }
                if (inputs == null) {
                    noDataLabel.setPrefWidth(rowCheckWidth);
                    noDataLabel.setPrefHeight(header.getHeight());
                    noDataLabel.setAlignment(Pos.CENTER);
                    sheetBox.getChildren().add(noDataLabel);
                }
                sheet = pickData();
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

    protected void valueInputted(int row, int col) {
        try {
            if (isSettingValues) {
                return;
            }
            TextField input = inputs[row][col];
            String value = value(row, col);
            if (dataValid(col, value)) {
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

    protected void dataIsInvalid() {

    }

    public void sheetChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        sheetDisplayController.setDisplayData(pickData(), columns);
        dataChanged(changed);
        notify.set(!notify.get());
    }

    public void sheetChanged() {
        sheetChanged(true);
    }

    protected void dataChanged(boolean dataChanged) {
        try {
            this.dataChanged = dataChanged;
            if (getMyStage() != null) {
                String title = baseTitle + " " + titleName();
                if (dataChanged) {
                    title += " *";
                }
                myStage.setTitle(title);
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public String askValue(String header, String name, String initValue) {
        String value = PopTools.askValue(baseTitle, header, name, initValue);
        if (value == null) {
            return null;
        }
        return value;
    }

    public void popRowLabelMenu(Label label) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.setStyle("-fx-font-weight: normal;");

            MenuItem menu = new MenuItem(Languages.message("EnlargerColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = label.getWidth() + widthChange;
                label.setPrefWidth(width);
                if (rowsCheck == null) {
                    return;
                }
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setPrefWidth(width);
                }
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("ReduceColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = label.getWidth() - widthChange;
                label.setPrefWidth(width);
                if (rowsCheck == null) {
                    return;
                }
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setPrefWidth(width);
                }
            });
            menu.setDisable(label.getWidth() <= widthChange * 1.5);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SetColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", Languages.message("SetColWidth"), (int) (label.getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    label.setPrefWidth(width);
                    if (rowsCheck == null) {
                        return;
                    }
                    for (int j = 0; j < rowsCheck.length; ++j) {
                        rowsCheck[j].setPrefWidth(width);
                    }
                } catch (Exception e) {
                    popError(Languages.message("InvalidData"));
                }
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SelectAllCols"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    colsCheck[i].setSelected(true);
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SelectNoCol"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    colsCheck[i].setSelected(false);
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SelectAllRows"));
            menu.setOnAction((ActionEvent event) -> {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setSelected(true);
                }
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SelectNoRow"));
            menu.setOnAction((ActionEvent event) -> {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setSelected(false);
                }
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("AddRowsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                addRowsNumber();
            });
            menu.setDisable(colsCheck == null || colsCheck.length < 1);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("AddColsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                addColsNumber();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("DeleteAllCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!PopTools.askSure(Languages.message("DeleteAllCols"), Languages.message("SureDeleteAll"))) {
                    return;
                }
                deleteAllCols();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateCenter(label, popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String delimiter() {
        return sheetDisplayController.textDelimiter;
    }

    /*
        abstract
     */
    public abstract void popColMenu(CheckBox label, int col);

    public abstract void popRowMenu(CheckBox label, int row);

    protected abstract void addRowsNumber();

    protected abstract void addColsNumber();

    protected abstract void deleteAllCols();

}
