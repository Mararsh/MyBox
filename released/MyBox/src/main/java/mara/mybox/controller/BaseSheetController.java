package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController extends ControlSheetDisplay {

    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected DataDefinition dataDefinition;
    protected DataType dataType;

    protected TextField[][] inputs;
    protected CheckBox[] colsCheck, rowsCheck;
    protected List<String> copiedRow, copiedCol;
    protected SimpleBooleanProperty notify;
    protected int widthChange;
    protected boolean rowsSelected, colsSelected, dataChanged;
    protected Label noDataLabel;

    @FXML
    protected VBox sheetBox;
    @FXML
    protected Button sizeSheetButton, deleteSheetButton, copySheetButton, equalSheetButton;
    @FXML
    protected Tab sheetTab;
    @FXML
    protected ControlSheetDisplay sheetDisplayController;

    public BaseSheetController() {
        baseTitle = Languages.message("DataEdit");
        dataType = DataType.DataFile;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            widthChange = 10;
            dataChanged = false;
            columns = new ArrayList<>();
            tableDataDefinition = new TableDataDefinition();
            tableDataColumn = new TableDataColumn();
            notify = new SimpleBooleanProperty(false);
            noDataLabel = new Label(Languages.message("NoData"));
            noDataLabel.setStyle("-fx-text-fill: gray;");
            inputStyle = "-fx-border-radius: 10; -fx-background-radius: 0;";

            sheetController = this;
            if (sheetDisplayController == null) {
                sheetDisplayController = this;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.removeTooltip(sizeSheetButton);
            NodeStyleTools.removeTooltip(copySheetButton);
            NodeStyleTools.removeTooltip(equalSheetButton);
            NodeStyleTools.removeTooltip(deleteSheetButton);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        sheetDisplayController.initDisplay(this);
    }

    @FXML
    @Override
    public boolean popAction() {
        return sheetDisplayController.popAction();
    }

    @FXML
    @Override
    public boolean menuAction() {
        return sheetDisplayController.menuAction();
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
        for (int j = 0; j < rowsNumber; j++) {
            for (int i = 0; i < colsNumber; i++) {
                data[j][i] = value(j, i);
            }
        }
        sheet = data;
        return data;
    }

    protected String value(int row, int col) {
        String value = null;
        try {
            value = inputs[row][col].getText();
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

    // Notice: this does not concern columns names
    public void resizeSheet(int rowsNumber, int colsNumber) {
        if (rowsNumber <= 0 || colsNumber <= 0) {
            makeSheet(null);
            return;
        }
        String[][] values = new String[rowsNumber][colsNumber];
        if (inputs != null && inputs.length > 0) {
            int drow = Math.min(inputs.length, rowsNumber);
            int dcol = Math.min(inputs[0].length, colsNumber);
            for (int j = 0; j < drow; ++j) {
                for (int i = 0; i < dcol; ++i) {
                    values[j][i] = value(j, i);
                }
            }
        }
        makeSheet(values);
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

    @FXML
    public void clipboard() {
        DataClipboardController controller = (DataClipboardController) WindowTools.openStage(Fxmls.DataClipboardFxml);
        controller.setSourceController(this);
        controller.toFront();
    }

    @FXML
    @Override
    public void copyText() {
        sheetDisplayController.copyText();
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

    // col: 0-based
    public void popColMenu(CheckBox label, int col) {
        if (label == null || colsCheck == null || colsCheck.length <= col) {
            return;
        }
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeColMenu(col);
            if (items == null || items.isEmpty()) {
                return;
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.setStyle("-fx-font-weight: normal;");

            popMenu.getItems().addAll(items);
            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
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

    public List<MenuItem> makeColMenu(int col) {
        if (colsCheck == null || colsCheck.length <= col) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(Languages.message("Column") + (col + 1) + ": " + colsCheck[col].getText());
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SelectCol"));
            menu.setOnAction((ActionEvent event) -> {
                colsCheck[col].setSelected(true);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("UnselectCol"));
            menu.setOnAction((ActionEvent event) -> {
                colsCheck[col].setSelected(false);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("EnlargerColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = colsCheck[col].getWidth() + widthChange;
                colsCheck[col].setPrefWidth(width);
                if (inputs != null) {
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][col].setPrefWidth(width);
                    }
                }
                makeDefintion();
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("ReduceColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = colsCheck[col].getWidth() - widthChange;
                colsCheck[col].setPrefWidth(width);
                if (inputs != null) {
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][col].setPrefWidth(width);
                    }
                }
                makeDefintion();
            });
            menu.setDisable(colsCheck[col].getWidth() <= widthChange * 1.5);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue(colsCheck[col].getText(), Languages.message("SetColWidth"), (int) (colsCheck[col].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    colsCheck[col].setPrefWidth(width);
                    if (inputs != null) {
                        for (int j = 0; j < inputs.length; ++j) {
                            inputs[j][col].setPrefWidth(width);
                        }
                    }
                    makeDefintion();
                } catch (Exception e) {
                    popError(Languages.message("InvalidData"));
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            items.addAll(colModifyMenu(col));

            if (dataType != DataType.Matrix) {
                items.add(new SeparatorMenuItem());
                menu = new MenuItem(Languages.message("Rename"));
                menu.setOnAction((ActionEvent event) -> {
                    String value = PopTools.askValue(baseTitle, colsCheck[col].getText(),
                            Languages.message("Rename"), colsCheck[col].getText());
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    colsCheck[col].setText(value);
                    columns.get(col).setName(value);
                    dataChanged(true);
                    makeDefintion();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("Ascending"));
            menu.setOnAction((ActionEvent event) -> {
                orderCol(col, true);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("Descending"));
            menu.setOnAction((ActionEvent event) -> {
                orderCol(col, false);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            List<MenuItem> moreItems = colMoreMenu(col);
            if (moreItems != null && !moreItems.isEmpty()) {
                items.add(new SeparatorMenuItem());
                items.addAll(moreItems);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colModifyMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            List<MenuItem> items1 = colModifyValuesMenu(col);
            if (items1 != null && !items1.isEmpty()) {
                items.addAll(items1);
            }
            List<MenuItem> items2 = colModifyDefMenu(col);
            if (items2 != null && !items2.isEmpty()) {
                if (!items.isEmpty()) {
                    items.add(new SeparatorMenuItem());
                }
                items.addAll(items2);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colModifyValuesMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(Languages.message("SetColValues"));
            menu.setOnAction((ActionEvent event) -> {
                SetPageColValues(col);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("CopyCol"));
            menu.setOnAction((ActionEvent event) -> {
                copyPageColValues(col);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("Paste"));
            menu.setOnAction((ActionEvent event) -> {
                pastePageColValues(col);
            });
            menu.setDisable(copiedCol == null || copiedCol.isEmpty());
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colModifyDefMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(Languages.message("InsertColLeft"));
            menu.setOnAction((ActionEvent event) -> {
                insertPageCol(col, true, 1);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("InsertColRight"));
            menu.setOnAction((ActionEvent event) -> {
                insertPageCol(col, false, 1);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("DeleteCol"));
            menu.setOnAction((ActionEvent event) -> {
                deletePageCol(col);
            });
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    protected void insertPageCol(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (columns == null) {
            columns = new ArrayList<>();
        }
        int base = col + (left ? 0 : 1);
        makeColumns(base, number);
        String[][] current = pickData();
        if (current == null) {
            makeSheet(null);
        } else {
            int rNumber = current.length;
            int cNumber = current[0].length + number;
            String[][] values = new String[rNumber][cNumber];
            for (int j = 0; j < rNumber; ++j) {
                for (int i = 0; i < base; ++i) {
                    values[j][i] = current[j][i];
                }
                for (int i = base + number; i < cNumber; ++i) {
                    values[j][i] = current[j][i - 1];
                }
                for (int i = base; i < base + number; ++i) {
                    values[j][i] = defaultColValue;
                }
            }
            makeSheet(values);
        }
    }

    protected void deletePageCol(int col) {
        if (columns.size() <= 1) {
            if (!PopTools.askSure(Languages.message("DeleteSelectedCols"), Languages.message("SureDeleteAll"))) {
                return;
            }
            deleteAllCols();
            return;
        }
        columns.remove(col);
        String[][] current = pickData();
        if (current == null) {
            makeSheet(null);
        } else {
            int rNumber = current.length;
            int cNumber = current[0].length;
            String[][] values = new String[rNumber][cNumber - 1];
            for (int j = 0; j < rNumber; ++j) {
                int index = 0;
                for (int i = 0; i < cNumber; ++i) {
                    if (i == col) {
                        continue;
                    }
                    values[j][index++] = current[j][i];
                }
            }
            makeSheet(values);
        }
    }

    protected void deleteAllCols() {
        if (columns != null) {
            columns.clear();
        }
        makeSheet(null);
    }

    protected void orderCol(int col, boolean asc) {
        if (inputs == null || columns == null || col < 0 || col >= columns.size()) {
            return;
        }
        int rowsNumber = inputs.length;
        int colsNumber = inputs[0].length;
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < rowsNumber; i++) {
            rows.add(i);
        }
        Collections.sort(rows, new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                ColumnDefinition column = columns.get(col);
                int v = column.compare(value(row1, col), value(row2, col));
                return asc ? v : -v;
            }
        });
        String[][] data = new String[rowsNumber][colsNumber];
        for (int row = 0; row < rows.size(); row++) {
            int drow = rows.get(row);
            for (int c = 0; c < colsNumber; c++) {
                data[row][c] = value(drow, c);
            }
        }
        makeSheet(data);
    }

    public void SetPageColValues(int col) {
        if (colsCheck == null || inputs == null) {
            return;
        }
        String value = askValue(colsCheck[col].getText(), Languages.message("SetPageColValues"), "");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < inputs.length; ++j) {
            inputs[j][col].setText(value);
        }
        isSettingValues = false;
        sheetChanged();
    }

    public void copyPageColValues(int col) {
        if (inputs == null) {
            return;
        }
        String s = "";
        int rowsNumber = inputs.length;
        copiedCol = new ArrayList<>();
        for (int j = 0; j < rowsNumber; ++j) {
            String v = value(j, col);
            s += v + "\n";
            copiedCol.add(v);
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
    }

    public void pastePageColValues(int col) {
        if (inputs == null || copiedCol == null || copiedCol.isEmpty()) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < Math.min(inputs.length, copiedCol.size()); ++j) {
            inputs[j][col].setText(copiedCol.get(j));
        }
        isSettingValues = false;
        sheetChanged();
    }

    protected List<MenuItem> colMoreMenu(int col) {
        return null;
    }

    // row: 0-based
    public void popRowMenu(CheckBox label, int row) {
        if (label == null || rowsCheck == null || rowsCheck.length <= row) {
            return;
        }
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeRowMenu(row);
            if (items == null || items.isEmpty()) {
                return;
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.setStyle("-fx-font-weight: normal;");

            popMenu.getItems().addAll(items);
            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
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

    public List<MenuItem> makeRowMenu(int row) {
        if (rowsCheck == null || rowsCheck.length <= row) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(rowsCheck[row].getText());
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SelectRow"));
            menu.setOnAction((ActionEvent event) -> {
                rowsCheck[row].setSelected(true);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("UnselectRow"));
            menu.setOnAction((ActionEvent event) -> {
                rowsCheck[row].setSelected(false);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("CopyRow"));
            menu.setOnAction((ActionEvent event) -> {
                String s = "";
                String p = TextTools.delimiterText(sheetDisplayController.textDelimiter);
                copiedRow = new ArrayList<>();
                for (int i = 0; i < colsCheck.length; ++i) {
                    String v = value(row, i);
                    s += v + p;
                    copiedRow.add(v);
                }
                TextClipboardTools.copyToSystemClipboard(myController, s);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("Paste"));
            menu.setOnAction((ActionEvent event) -> {
                isSettingValues = true;
                for (int i = 0; i < Math.min(colsCheck.length, copiedRow.size()); ++i) {
                    inputs[row][i].setText(copiedRow.get(i));
                }
                isSettingValues = false;
                sheetChanged();
            });
            menu.setDisable(copiedRow == null || copiedRow.isEmpty());
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("InsertRowAbove"));
            menu.setOnAction((ActionEvent event) -> {
                String[][] current = pickData();
                int rNumber = current.length;
                int cNumber = current[0].length;
                String[][] values = new String[++rNumber][cNumber];
                for (int j = 0; j < row; ++j) {
                    for (int i = 0; i < cNumber; ++i) {
                        values[j][i] = current[j][i];
                    }
                }
                for (int i = 0; i < cNumber; ++i) {
                    values[row][i] = defaultColValue;
                }
                for (int j = row + 1; j < rNumber; ++j) {
                    for (int i = 0; i < cNumber; ++i) {
                        values[j][i] = current[j - 1][i];
                    }
                }
                makeSheet(values);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("InsertRowBelow"));
            menu.setOnAction((ActionEvent event) -> {
                String[][] current = pickData();
                int rNumber = current.length;
                int cNumber = current[0].length;
                String[][] values = new String[++rNumber][cNumber];
                for (int j = 0; j <= row; ++j) {
                    for (int i = 0; i < cNumber; ++i) {
                        values[j][i] = current[j][i];
                    }
                }
                for (int i = 0; i < cNumber; ++i) {
                    values[row + 1][i] = defaultColValue;
                }
                for (int j = row + 2; j < rNumber; ++j) {
                    for (int i = 0; i < cNumber; ++i) {
                        values[j][i] = current[j - 1][i];
                    }
                }
                makeSheet(values);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("DeleteRow"));
            menu.setOnAction((ActionEvent event) -> {
                if (inputs.length <= 1) {
                    if (!PopTools.askSure(Languages.message("DeleteRow"), Languages.message("SureDeleteAll"))) {
                        return;
                    }
                    deletePageAllRows();
                    return;
                }
                String[][] current = pickData();
                int rNumber = current.length;
                int cNumber = current[0].length;
                String[][] values = new String[rNumber - 1][cNumber];
                int index = 0;
                for (int j = 0; j < rNumber; ++j) {
                    if (j == row) {
                        continue;
                    }
                    for (int i = 0; i < cNumber; ++i) {
                        values[index][i] = current[j][i];
                    }
                    index++;
                }
                makeSheet(values);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SetRowValues"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue(rowsCheck[row].getText(), Languages.message("SetRowValues"), "");
                if (value == null) {
                    return;
                }
                isSettingValues = true;
                for (int i = 0; i < colsCheck.length; ++i) {
                    inputs[row][i].setText(value);
                }
                isSettingValues = false;
                sheetChanged();
            });
            items.add(menu);

            List<MenuItem> moreItems = rowMoreMenu(row);
            if (moreItems != null && !moreItems.isEmpty()) {
                items.add(new SeparatorMenuItem());
                items.addAll(moreItems);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> rowMoreMenu(int row) {
        return null;
    }

    @FXML
    public void sheetCopyMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeSheetCopyMenu();
            if (items == null || items.isEmpty()) {
                return;
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().addAll(items);
            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetCopyMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu;

            rowsSelected = false;
            if (rowsCheck != null) {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        rowsSelected = true;
                        break;
                    }
                }
            }
            colsSelected = false;
            if (colsCheck != null) {
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }
            }

            menu = new MenuItem(Languages.message("CopyAll"));
            menu.setOnAction((ActionEvent event) -> {
                copyText();
            });
            menu.setDisable(inputs == null);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("CopySelectedRows"));
            menu.setOnAction((ActionEvent event) -> {
                if (!rowsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                copySelectedRows();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                copySelectedCols();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedRowsCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected || !rowsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                copySelectedRowsCols();
            });
            menu.setDisable(!colsSelected || !rowsSelected);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public void copySelectedRows() {
        if (inputs == null || rowsCheck == null) {
            return;
        }
        String s = null;
        String p = TextTools.delimiterText(sheetDisplayController.textDelimiter);
        rowsNumber = inputs.length;
        colsNumber = inputs[0].length;
        int lines = 0;
        for (int j = 0; j < rowsNumber; ++j) {
            if (!rowsCheck[j].isSelected()) {
                continue;
            }
            String row = null;
            for (int i = 0; i < colsNumber; ++i) {
                if (row == null) {
                    row = value(j, i);
                } else {
                    row += p + value(j, i);
                }
            }
            if (s == null) {
                s = row;
            } else {
                s += "\n" + row;
            }
            lines++;
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
    }

    public void copySelectedCols() {
        if (inputs == null || colsCheck == null) {
            return;
        }
        int lines = 0, cols = 0;
        for (CheckBox c : colsCheck) {
            if (c.isSelected()) {
                cols++;
            }
        }
        if (cols < 1) {
            popError(Languages.message("NoData"));
            return;
        }
        String s = null;
        String p = TextTools.delimiterText(sheetDisplayController.textDelimiter);
        rowsNumber = inputs.length;
        colsNumber = inputs[0].length;
        for (int j = 0; j < rowsNumber; ++j) {
            String row = null;
            for (int i = 0; i < colsNumber; ++i) {
                if (!colsCheck[i].isSelected()) {
                    continue;
                }
                if (row == null) {
                    row = value(j, i);
                } else {
                    row += p + value(j, i);
                }
            }
            if (row == null) {
                break;
            }
            if (s == null) {
                s = row;
            } else {
                s += "\n" + row;
            }
            lines++;
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
    }

    public void copySelectedRowsCols() {
        if (inputs == null || colsCheck == null) {
            return;
        }
        int lines = 0, cols = 0;
        for (CheckBox c : colsCheck) {
            if (c.isSelected()) {
                cols++;
            }
        }
        if (cols < 1) {
            popError(Languages.message("NoData"));
            return;
        }
        String s = null;
        String p = TextTools.delimiterText(sheetDisplayController.textDelimiter);
        rowsNumber = inputs.length;
        colsNumber = inputs[0].length;
        for (int j = 0; j < rowsNumber; ++j) {
            if (!rowsCheck[j].isSelected()) {
                continue;
            }
            String row = null;
            for (int i = 0; i < colsNumber; ++i) {
                if (!colsCheck[i].isSelected()) {
                    continue;
                }
                if (row == null) {
                    row = value(j, i);
                } else {
                    row += p + value(j, i);
                }
            }
            if (row == null) {
                break;
            }
            if (s == null) {
                s = row;
            } else {
                s += "\n" + row;
            }
            lines++;
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
    }

    @FXML
    public void sheetDeleteMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeSheetDeleteMenu();
            if (items == null || items.isEmpty()) {
                return;
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().addAll(items);
            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetDeleteMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            List<MenuItem> items1 = makeSheetDeleteRowsMenu();
            if (items1 != null && !items1.isEmpty()) {
                items.addAll(items1);
            }
            List<MenuItem> items2 = makeSheetDeleteColsMenu();
            if (items2 != null && !items2.isEmpty()) {
                items.addAll(items2);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> makeSheetDeleteRowsMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            rowsSelected = false;
            if (rowsCheck != null) {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        rowsSelected = true;
                        break;
                    }
                }
            }
            MenuItem menu = new MenuItem(Languages.message("DeleteSelectedRows"));
            menu.setOnAction((ActionEvent event) -> {
                int newNumber = 0;
                int rowsNumber = inputs.length;
                for (int j = 0; j < rowsNumber; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        continue;
                    }
                    newNumber++;
                }
                if (rowsNumber == newNumber) {
                    return;
                }
                String[][] current = pickData();
                int colsNumber = current[0].length;
                if (newNumber == 0) {
                    if (!PopTools.askSure(Languages.message("DeleteSelectedRows"), Languages.message("SureDeleteAll"))) {
                        return;
                    }
                    deletePageAllRows();
                } else {
                    String[][] values = new String[newNumber][colsNumber];
                    newNumber = 0;
                    for (int j = 0; j < rowsNumber; ++j) {
                        if (rowsCheck[j].isSelected()) {
                            continue;
                        }
                        for (int i = 0; i < colsNumber; ++i) {
                            values[newNumber][i] = current[j][i];
                        }
                        newNumber++;
                    }
                    makeSheet(values);
                }
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> makeSheetDeleteColsMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            colsSelected = false;
            if (colsCheck != null) {
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }
            }
            MenuItem menu = new MenuItem(Languages.message("DeleteSelectedCols"));
            menu.setOnAction((ActionEvent event) -> {
                int colsNumber = colsCheck.length;
                List<ColumnDefinition> newColumns = new ArrayList<>();
                for (int i = 0; i < colsNumber; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        newColumns.add(columns.get(i));
                    }
                }
                int newNumber = newColumns.size();
                if (colsNumber == newColumns.size()) {
                    return;
                }
                if (newNumber == 0) {
                    if (!PopTools.askSure(Languages.message("DeleteSelectedCols"), Languages.message("SureDeleteAll"))) {
                        return;
                    }
                    deleteAllCols();
                } else {
                    columns = newColumns;
                    deleteSelectedCols();
                }
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    protected void deletePageAllRows() {
        makeSheet(null);
    }

    // columns have been changed before call this
    protected void deleteSelectedCols() {
        if (columns == null || columns.isEmpty()) {
            deleteAllCols();
            return;
        }
        String[][] current = pickData();
        if (current == null) {
            makeSheet(null);
        } else {
            int rowsNumber = current.length;
            String[][] values = new String[rowsNumber][columns.size()];
            for (int j = 0; j < rowsNumber; ++j) {
                int index = 0;
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        values[j][index++] = current[j][i];
                    }
                }
            }
            makeSheet(values);
        }
    }

    @FXML
    public void sheetEqualMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeSheetEqualMenu();
            if (items == null || items.isEmpty()) {
                return;
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().addAll(items);
            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetEqualMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu;

            rowsSelected = false;
            if (rowsCheck != null) {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        rowsSelected = true;
                        break;
                    }
                }
            }
            colsSelected = false;
            if (colsCheck != null) {
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }
            }

            menu = new MenuItem(Languages.message("SetAllValues"));
            menu.setOnAction((ActionEvent event) -> {
                setAllValues();
            });
            menu.setDisable(inputs == null);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetSelectedRowsValues"));
            menu.setOnAction((ActionEvent event) -> {
                if (!rowsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                setSelectedRows();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetSelectedColsValues"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                setSelectedCols();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetSelectedRowsColsValues"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected || !rowsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                setSelectedRowsCols();
            });
            menu.setDisable(!colsSelected || !rowsSelected);
            items.add(menu);

            List<MenuItem> moreItems = equalMoreMenu();
            if (moreItems != null && !moreItems.isEmpty()) {
                items.add(new SeparatorMenuItem());
                items.addAll(moreItems);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public void setAllValues() {
        if (inputs == null) {
            return;
        }
        String value = askValue(Languages.message("All"), Languages.message("SetValue"), defaultColumnType == ColumnType.String ? "v" : "0");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < inputs.length; ++j) {
            for (int i = 0; i < inputs[0].length; ++i) {
                inputs[j][i].setText(value);
            }
        }
        isSettingValues = false;
        sheetChanged();
    }

    public void setSelectedRows() {
        if (inputs == null || rowsCheck == null) {
            return;
        }
        String value = askValue(Languages.message("SelectedRows"), Languages.message("SetValue"), "");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < inputs.length; ++j) {
            if (rowsCheck[j].isSelected()) {
                for (int i = 0; i < inputs[0].length; ++i) {
                    inputs[j][i].setText(value);
                }
            }
        }
        isSettingValues = false;
        sheetChanged();

    }

    public void setSelectedCols() {
        if (inputs == null || colsCheck == null) {
            return;
        }
        String value = askValue(Languages.message("SelectedCols"), Languages.message("SetValue"), "");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int i = 0; i < inputs[0].length; ++i) {
            if (colsCheck[i].isSelected()) {
                for (int j = 0; j < inputs.length; ++j) {
                    inputs[j][i].setText(value);
                }
            }
        }
        isSettingValues = false;
        sheetChanged();
    }

    public void setSelectedRowsCols() {
        if (inputs == null || rowsCheck == null || colsCheck == null) {
            return;
        }
        String value = askValue(Languages.message("SetSelectedRowsColsValues"), Languages.message("SetValue"), "");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < inputs.length; ++j) {
            if (rowsCheck[j].isSelected()) {
                for (int i = 0; i < inputs[0].length; ++i) {
                    if (colsCheck[i].isSelected()) {
                        inputs[j][i].setText(value);
                    }
                }
            }
        }
        isSettingValues = false;
        sheetChanged();
    }

    public List<MenuItem> equalMoreMenu() {
        return null;
    }

    @FXML
    public void sheetSizeMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeSheetSizeMenu();
            if (items == null || items.isEmpty()) {
                return;
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().addAll(items);
            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetSizeMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(Languages.message("EnlargerAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    double width = colsCheck[i].getWidth() + widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (inputs != null) {
                        for (int j = 0; j < inputs.length; ++j) {
                            inputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDefintion();
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("ReduceAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (colsCheck[i].getWidth() < widthChange * 1.5) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() - widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (inputs != null) {
                        for (int j = 0; j < inputs.length; ++j) {
                            inputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDefintion();
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", Languages.message("SetAllColsWidth"), (int) (colsCheck[0].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    for (int i = 0; i < colsCheck.length; ++i) {
                        colsCheck[i].setPrefWidth(width);
                        if (inputs != null) {
                            for (int j = 0; j < inputs.length; ++j) {
                                inputs[j][i].setPrefWidth(width);
                            }
                        }
                    }
                    makeDefintion();
                } catch (Exception e) {
                    popError(Languages.message("InvalidData"));
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            items.add(menu);

            colsSelected = false;
            if (colsCheck != null) {
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("EnlargerSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() + widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (inputs != null) {
                        for (int j = 0; j < inputs.length; ++j) {
                            inputs[j][i].setPrefWidth(width);
                        }
                    }
                }
                makeDefintion();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("ReduceSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        continue;
                    }
                    if (colsCheck[i].getWidth() < widthChange * 1.5) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() - widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (inputs != null) {
                        for (int j = 0; j < inputs.length; ++j) {
                            inputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDefintion();
                }
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", Languages.message("SetSelectedColsWidth"), (int) (colsCheck[0].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    for (int i = 0; i < colsCheck.length; ++i) {
                        if (!colsCheck[i].isSelected()) {
                            continue;
                        }
                        colsCheck[i].setPrefWidth(width);
                        if (inputs != null) {
                            for (int j = 0; j < inputs.length; ++j) {
                                inputs[j][i].setPrefWidth(width);
                            }
                        }
                    }
                    makeDefintion();
                } catch (Exception e) {
                    popError(Languages.message("InvalidData"));
                }
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("AddRowsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                addRowsNumber();
            });
            menu.setDisable(colsCheck == null || colsCheck.length < 1);
            items.add(menu);

            menu = new MenuItem(Languages.message("AddColsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                addColsNumber();
            });
            items.add(menu);

            List<MenuItem> more = sheetSizeMoreMenu();
            if (more != null) {
                items.addAll(more);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    protected void addRowsNumber() {
        if (colsCheck == null || colsCheck.length == 0) {
            return;
        }
        String value = askValue("", Languages.message("AddRowsNumber"), "1");
        if (value == null) {
            return;
        }
        try {
            int number = Integer.parseInt(value);
            if (inputs == null || inputs.length == 0) {
                resizeSheet(number, colsCheck.length);
            } else {
                resizeSheet(inputs.length + number, colsCheck.length);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    protected void addColsNumber() {
        String value = askValue("", Languages.message("AddColsNumber"), "1");
        if (value == null) {
            return;
        }
        try {
            int number = Integer.parseInt(value);
            if (colsCheck == null || colsCheck.length == 0) {
                insertPageCol(0, true, number);
            } else {
                insertPageCol(colsCheck.length - 1, false, number);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    public List<MenuItem> sheetSizeMoreMenu() {
        return null;
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!dataChanged) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            if (task != null) {
                task.cancel();
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            dataChanged = false;
        }
        return goOn;
    }

    @Override
    public void cleanPane() {
        try {
            tableDataDefinition = null;
            tableDataColumn = null;
            dataDefinition = null;
            inputs = null;
            colsCheck = null;
            rowsCheck = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get/set
     */
    public SimpleBooleanProperty getNotify() {
        return notify;
    }

    public void setNotify(SimpleBooleanProperty notify) {
        this.notify = notify;
    }

}
