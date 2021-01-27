package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController extends BaseController {

    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected DataDefinition dataDefinition;
    protected DataType dataType;
    protected String dataName;
    protected TextField[][] inputs;
    protected CheckBox[] colsCheck, rowsCheck;
    protected String delimiter, defaultValue;
    protected List<String> copiedRow, copiedCol;
    protected SimpleBooleanProperty notify;
    protected int widthChange;
    protected boolean rowsSelected, colsSelected, dataChanged;
    protected List<ColumnDefinition> columns;

    @FXML
    protected VBox sheetBox, defBox;
    @FXML
    protected TextArea textArea;
    @FXML
    protected WebView webView;
    @FXML
    protected Button sizeSheetButton, deleteSheetButton, copySheetButton, equalSheetButton;
    @FXML
    protected CheckBox htmlColumnCheck, htmlRowCheck;

    public BaseSheetController() {
        baseTitle = message("DataEdit");
        dataType = DataType.DataFile;
        dataName = "sheet";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            defaultValue = "";
            widthChange = 10;
            columns = null;
            dataChanged = false;
            tableDataDefinition = new TableDataDefinition();
            tableDataColumn = new TableDataColumn();
            notify = new SimpleBooleanProperty(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // Should always run this after scene loaded and before input data
    public void setControls(String baseName) {
        try {
            this.baseName = baseName;
            delimiter = AppVariables.getUserConfigValue(baseName + "Delimiter", ",");

            htmlColumnCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "HtmlColumn", true));
            htmlColumnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                webView.getEngine().loadContent(html());
                AppVariables.setUserConfigValue(baseName + "HtmlColumn", newValue);
            });
            htmlRowCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "HtmlRow", true));
            htmlRowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                webView.getEngine().loadContent(html());
                AppVariables.setUserConfigValue(baseName + "HtmlRow", newValue);
            });

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        FxmlControl.removeTooltip(sizeSheetButton);
                        FxmlControl.removeTooltip(copySheetButton);
                        FxmlControl.removeTooltip(equalSheetButton);
                        FxmlControl.removeTooltip(deleteSheetButton);
                    });
                }
            }, 1000);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean dataValid(int col, String value) {
        try {
            ColumnDefinition column = columns.get(col);
            return column.valid(value);
        } catch (Exception e) {
        }
        return false;
    }

    protected void dataChanged(boolean dataChanged) {
        this.dataChanged = dataChanged;
        if (myStage != null) {
            String title = baseTitle;
            if (sourceFile != null) {
                title += " " + sourceFile.getAbsolutePath();
            }
            if (dataChanged) {
                title += " *";
            }
            myStage.setTitle(title);
        }
    }

    protected String value(int row, int col) {
        String value = null;
        try {
            value = inputs[row][col].getText();
//            if (dataValid(value)) {
//                return value;
//            }
        } catch (Exception e) {
        }
        return value == null ? defaultValue : value;
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

    protected String rowName(int row) {
        return message("Row") + (row + 1);
    }

    protected String colName(int col) {
        if (columns == null) {
            makeColumns(colsCheck.length);
        }
        return columns.get(col).getName();
    }

    protected List<String> columnNames() {
        if (columns == null) {
            makeColumns(colsCheck.length);
        }
        List<String> names = new ArrayList<>();
        for (ColumnDefinition column : columns) {
            names.add(column.getName());
        }
        return names;
    }

    public void makeColumns(int number) {
        if (dataName == null) {
            return;
        }
        columns = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            ColumnDefinition column = new ColumnDefinition(message("Field") + (i + 1), ColumnType.String);
            columns.add(column);
        }
    }

    // Sheet itself can be resued and need not clear
    public void clearSheet() {
        inputs = null;
        colsCheck = null;
        rowsCheck = null;
        rowsSelected = colsSelected = false;
        textArea.setText("");
        webView.getEngine().loadContent("");
        notify.set(!notify.get());
    }

    public void makeSheet(String[][] data) {
        makeSheet(data, true);
    }

    public void makeSheet(String[][] data, boolean changed) {
        if (isSettingValues) {
            return;
        }
        LoadingController loading = openHandlingStage(Modality.WINDOW_MODAL);
        Platform.runLater(() -> {
            try {
                clearSheet();
                if (data == null) {
                    return;
                }
                int rowsSize = data.length;
                if (rowsSize == 0) {
                    return;
                }
                int colsSize = data[0].length;
                if (colsSize == 0) {
                    return;
                }
                if (columns == null || dataType == DataType.Matrix) {
                    makeColumns(colsSize);
                } else if (columns.size() < colsSize) {
                    for (int col = columns.size() + 1; col <= colsSize; col++) {
                        ColumnDefinition column = new ColumnDefinition(message("Field") + col, ColumnType.String);
                        columns.add(column);
                    }
                } else if (columns.size() > colsSize) {
                    for (int col = columns.size() - 1; col > colsSize; col--) {
                        columns.remove(col);
                    }
                }
                double space = 0.0;
                double rowCheckWidth = 80 + (data.length + "").length() * AppVariables.sceneFontSize;
                HBox header;
                Label label;
                if (!sheetBox.getChildren().isEmpty()) {
                    header = (HBox) (sheetBox.getChildren().get(0));
                    label = (Label) (header.getChildren().get(0));
                } else {
                    header = new HBox();
                    sheetBox.setSpacing(space);
                    header.setAlignment(Pos.CENTER);
                    header.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    header.setSpacing(space);
                    label = new Label(message("Col") + "0");
                    label.setPrefWidth(rowCheckWidth);
                    label.setPrefHeight(header.getHeight());
                    label.setAlignment(Pos.CENTER);
                    label.hoverProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                        if (newValue) {
                            label.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;-fx-text-fill: blue;");
                            popRowLabelMenu(label);
                        } else {
                            label.setStyle("-fx-background-color: #E8E8E8;-fx-font-weight: bolder;");
                        }
                    });
                    header.getChildren().add(label);
                    sheetBox.getChildren().add(header);
                }
                label.setPrefWidth(rowCheckWidth);
                int currentRowsSize = sheetBox.getChildren().size() - 1;
                int currentColsSize = header.getChildren().size() - 1;
                if (currentColsSize > 0) {
                    if (currentColsSize > colsSize) {
                        header.getChildren().remove(colsSize + 1, currentColsSize + 1);
                    }
                }
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
                        v = v == null ? defaultValue : v;
                        isSettingValues = true;
                        valueInput.setText(v);
                        isSettingValues = false;
                        valueInput.setStyle("-fx-border-radius: 10; -fx-background-radius: 0;" + (dataValid(col, v) ? "" : badStyle));
                        valueInput.setPrefWidth(columns.get(col).getWidth());
                        inputs[row][col] = valueInput;
                    }
                }
                if (currentRowsSize > rowsSize) {
                    sheetBox.getChildren().remove(rowsSize + 1, currentRowsSize + 1);
                }
                makeDataDefintion();
                FxmlControl.refreshStyle(sheetBox);
                sheetChanged();
                dataChanged(changed);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
            if (loading != null) {
                loading.closeStage();
            }
        });

    }

    public void makeDataDefintion() {
        if (defBox == null) {
            return;
        }
        defBox.getChildren().clear();
        int index = 1;
        for (ColumnDefinition column : columns) {
            HBox line = new HBox();
            line.setAlignment(Pos.CENTER_LEFT);
            line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            line.setSpacing(5);

            Label indexLabel = new Label("" + index++);
            indexLabel.setPrefWidth(50);
            indexLabel.setMinWidth(Region.USE_PREF_SIZE);
            indexLabel.setAlignment(Pos.CENTER_RIGHT);

            TextField nameInput = new TextField(column.getName());
            nameInput.setPrefWidth(200);
            nameInput.setMinWidth(Region.USE_PREF_SIZE);

            TextField widthInput = new TextField(column.getWidth() + "");
            widthInput.setPrefWidth(80);
            widthInput.setMinWidth(Region.USE_PREF_SIZE);

            CheckBox notNull = new CheckBox(message("Yes"));
            notNull.setPrefWidth(80);
            notNull.setMinWidth(Region.USE_PREF_SIZE);
            notNull.setSelected(column.isNotNull());

            ComboBox<String> typeSelector = new ComboBox<>();
            for (ColumnType type : ColumnDefinition.editTypes()) {
                typeSelector.getItems().add(message(type.name()));
            }
            typeSelector.setValue(message(column.getType().name()));

            line.getChildren().addAll(indexLabel, nameInput, widthInput, notNull, typeSelector);
            defBox.getChildren().add(line);

        }
        FxmlControl.refreshStyle(defBox);
    }

    public void sheetChanged() {
        if (isSettingValues) {
            return;
        }
        updatePanes();
        notify.set(!notify.get());
        dataChanged(true);
    }

    public void updatePanes() {
        if (isSettingValues) {
            return;
        }
        textArea.setText(TextTools.dataText(data(), delimiter));
        webView.getEngine().loadContent(html());
    }

    public void resizeSheet(int rowsNumber, int colsNumber) {
        if (rowsNumber <= 0 || colsNumber <= 0) {
            makeSheet(null);
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
            if (dataValid(col, value(row, col))) {
                input.setStyle(null);
                sheetChanged();
            } else {
                input.setStyle(badStyle);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected String[][] data() {
        if (inputs == null) {
            return null;
        }
        int rowsNumber = inputs.length;
        int colsNumber = inputs[0].length;
        String[][] data = new String[inputs.length][inputs[0].length];
        for (int j = 0; j < rowsNumber; j++) {
            for (int i = 0; i < colsNumber; i++) {
                data[j][i] = value(j, i);
            }
        }
        return data;
    }

    @FXML
    public void editHtmlAction() {
        if (inputs == null) {
            return;
        }
        HtmlEditorController controller = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
        controller.loadContents(html());
    }

    public String html() {
        if (inputs == null) {
            return null;
        }
        try {
            int rowsNumber = inputs.length;
            int colsNumber = inputs[0].length;
            List<String> names;
            if (htmlColumnCheck.isSelected()) {
                names = new ArrayList<>();
                if (htmlRowCheck.isSelected()) {
                    names.add("");
                }
                for (int i = 0; i < columns.size(); i++) {
                    names.add(columns.get(i).getName());
                }
            } else {
                names = null;
            }
            StringTable table = new StringTable(names, baseTitle + (sourceFile == null ? "" : sourceFile.getAbsolutePath()));
            for (int i = 0; i < rowsNumber; i++) {
                List<String> row = new ArrayList<>();
                if (htmlRowCheck.isSelected()) {
                    row.add(rowName(i));
                }
                for (int j = 0; j < colsNumber; j++) {
                    row.add(value(i, j));
                }
                table.add(row);
            }
            return table.html();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return "";
        }
    }

    @FXML
    public void copyTextAction() {
        if (FxmlControl.copyToSystemClipboard(textArea.getText())) {
            popInformation(message("CopiedToSystemClipboard"));
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        editTextAction();
    }

    @FXML
    public void editTextAction() {
        SheetPasteController controller = (SheetPasteController) FxmlStage.openStage(CommonValues.SheetPasteFxml);
        controller.setSheet(this);
        controller.toFront();
    }

    @FXML
    public void okDefAction() {
        if (defBox == null) {
            return;
        }
        List<ColumnDefinition> newValues = new ArrayList<>();
        boolean ok = true;
        for (Node node : defBox.getChildren()) {
            ColumnDefinition column = new ColumnDefinition();
            HBox line = (HBox) node;

            TextField nameInput = (TextField) (line.getChildren().get(1));
            if (nameInput.getText().isBlank()) {
                nameInput.setStyle(badStyle);
                ok = false;
            } else {
                column.setName(nameInput.getText().trim());
                nameInput.setStyle(null);
            }

            TextField widthInput = (TextField) (line.getChildren().get(2));
            try {
                double v = Double.parseDouble(widthInput.getText());
                if (v > 10) {
                    column.setWidth((int) v);
                    widthInput.setStyle(null);
                } else {
                    ok = false;
                    widthInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                ok = false;
                widthInput.setStyle(badStyle);
            }

            if (ok) {
                CheckBox notNull = (CheckBox) (line.getChildren().get(3));
                column.setNotNull(notNull.isSelected());
            }

            ComboBox<String> typeSelector = (ComboBox) (line.getChildren().get(4));
            String ctype = typeSelector.getValue();
            if (ctype == null) {
                ok = false;
            } else if (ok) {
                for (ColumnType type : ColumnType.values()) {
                    if (ctype.equals(type.name()) || ctype.equals(message(type.name()))) {
                        column.setType(type);
                        break;
                    }
                }
            }

            if (ok) {
                newValues.add(column);
                if (!ColumnDefinition.valid(this, newValues)) {
                    return;
                }
            }
        }
        if (ok) {
            columns = newValues;
            makeSheet(data());
            popSuccessful();
        } else {
            popError(message("InvalidData"));
        }
    }

    @FXML
    public void recoverDefAction() {
        makeDataDefintion();
    }

    @FXML
    public void clearDefAction() {
        makeColumns(columns.size());
        dataChanged(true);
        makeDataDefintion();
    }

    public String askValue(String header, String name, String initValue) {
        String value = FxmlControl.askValue(baseTitle, header, name, initValue);
        if (value == null) {
            return null;
        }
//        if (!dataValid(value)) {
//            popError(message("InvalidData"));
//            return null;
//        }
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

            MenuItem menu = new MenuItem(message("EnlargerColWidth"));
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

            if (label.getWidth() > widthChange * 1.5) {
                menu = new MenuItem(message("ReduceColWidth"));
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
                popMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("SetColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", message("SetColWidth"), (int) (label.getWidth()) + "");
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
                    popError(message("InvalidData"));
                }
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("SelectAllCols"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    colsCheck[i].setSelected(true);
                }
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SelectNoCol"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    colsCheck[i].setSelected(false);
                }
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SelectAllRows"));
            menu.setOnAction((ActionEvent event) -> {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setSelected(true);
                }
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SelectNoRow"));
            menu.setOnAction((ActionEvent event) -> {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setSelected(false);
                }
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow(label, popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // col: 0-based
    public void popColMenu(CheckBox label, int col) {
        if (inputs == null) {
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

            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow(label, popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeColMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("Column") + (col + 1) + ": " + colsCheck[col].getText());
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("SelectCol"));
            menu.setOnAction((ActionEvent event) -> {
                colsCheck[col].setSelected(true);
            });
            items.add(menu);

            menu = new MenuItem(message("UnselectCol"));
            menu.setOnAction((ActionEvent event) -> {
                colsCheck[col].setSelected(false);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("EnlargerColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = colsCheck[col].getWidth() + widthChange;
                colsCheck[col].setPrefWidth(width);
                for (int j = 0; j < inputs.length; ++j) {
                    inputs[j][col].setPrefWidth(width);
                }
                makeDataDefintion();
            });
            items.add(menu);

            if (colsCheck[col].getWidth() > widthChange * 1.5) {
                menu = new MenuItem(message("ReduceColWidth"));
                menu.setOnAction((ActionEvent event) -> {
                    double width = colsCheck[col].getWidth() - widthChange;
                    colsCheck[col].setPrefWidth(width);
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][col].setPrefWidth(width);
                    }
                    makeDataDefintion();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("SetColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue(colsCheck[col].getText(), message("SetColWidth"), (int) (colsCheck[col].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    colsCheck[col].setPrefWidth(width);
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][col].setPrefWidth(width);
                    }
                    makeDataDefintion();
                } catch (Exception e) {
                    popError(message("InvalidData"));
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            items.addAll(colModifyMenu(col));

            if (dataType != DataType.Matrix) {
                items.add(new SeparatorMenuItem());
                menu = new MenuItem(message("Rename"));
                menu.setOnAction((ActionEvent event) -> {
                    String value = FxmlControl.askValue(baseTitle, colsCheck[col].getText(),
                            message("Rename"), colsCheck[col].getText());
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    colsCheck[col].setText(value);
                    columns.get(col).setName(value);
                    dataChanged(true);
                    makeDataDefintion();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Ascending"));
            menu.setOnAction((ActionEvent event) -> {
                orderCol(col, true);
            });
            items.add(menu);

            menu = new MenuItem(message("Descending"));
            menu.setOnAction((ActionEvent event) -> {
                orderCol(col, false);
            });
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
            MenuItem menu = new MenuItem(message("SetColValues"));
            menu.setOnAction((ActionEvent event) -> {
                SetPageColValues(col);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("CopyCol"));
            menu.setOnAction((ActionEvent event) -> {
                copyPageColValues(col);
            });
            items.add(menu);

            if (copiedCol != null && !copiedCol.isEmpty()) {
                menu = new MenuItem(message("Paste"));
                menu.setOnAction((ActionEvent event) -> {
                    pastePageColValues(col);
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("InsertColLeft"));
            menu.setOnAction((ActionEvent event) -> {
                insertPageCol(col, true);
            });
            items.add(menu);

            menu = new MenuItem(message("InsertColRight"));
            menu.setOnAction((ActionEvent event) -> {
                insertPageCol(col, false);
            });
            items.add(menu);

            if (inputs[0].length > 1) {
                menu = new MenuItem(message("DeleteCol"));
                menu.setOnAction((ActionEvent event) -> {
                    deletePageCol(col);
                });
                items.add(menu);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    protected void insertPageCol(int col, boolean left) {
        int offset = left ? 0 : 1;
        if (dataType != DataType.Matrix) {
            String name = message("Field") + (col + offset + 1);
            name = FxmlControl.askValue(baseTitle, message("InsertColLeft"), message("Name"), name);
            if (name == null) {
                return;
            }
            ColumnDefinition column = new ColumnDefinition(name, ColumnType.String);
            columns.add(col, column);
        }
        String[][] current = data();
        int rowsNumber = current.length;
        int colsNumber = current[0].length;
        String[][] values = new String[rowsNumber][++colsNumber];
        for (int j = 0; j < rowsNumber; ++j) {
            for (int i = 0; i < col + offset; ++i) {
                values[j][i] = current[j][i];
            }
            values[j][col + offset] = defaultValue;
            for (int i = col + 1 + offset; i < colsNumber; ++i) {
                values[j][i] = current[j][i - 1];
            }
        }
        makeSheet(values);
    }

    protected void deletePageCol(int col) {
        columns.remove(col);
        String[][] current = data();
        int rowsNumber = current.length;
        int colsNumber = current[0].length;
        String[][] values = new String[rowsNumber][colsNumber - 1];
        for (int j = 0; j < rowsNumber; ++j) {
            int index = 0;
            for (int i = 0; i < colsNumber; ++i) {
                if (i == col) {
                    continue;
                }
                values[j][index++] = current[j][i];
            }
        }
        makeSheet(values);
    }

    protected void orderCol(int col, boolean asc) {
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
        String value = askValue(colsCheck[col].getText(), message("SetPageColValues"), "");
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
        String s = "";
        int rowsNumber = inputs.length;
        copiedCol = new ArrayList<>();
        for (int j = 0; j < rowsNumber; ++j) {
            String v = value(j, col);
            s += v + "\n";
            copiedCol.add(v);
        }
        if (FxmlControl.copyToSystemClipboard(s)) {
            popInformation(message("CopiedInSheet"));
        }
    }

    public void pastePageColValues(int col) {
        if (copiedCol == null || copiedCol.isEmpty()) {
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
        if (inputs == null) {
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

            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow(label, popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeRowMenu(int row) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(rowsCheck[row].getText());
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("SelectRow"));
            menu.setOnAction((ActionEvent event) -> {
                rowsCheck[row].setSelected(true);
            });
            items.add(menu);

            menu = new MenuItem(message("UnselectRow"));
            menu.setOnAction((ActionEvent event) -> {
                rowsCheck[row].setSelected(false);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("CopyRow"));
            menu.setOnAction((ActionEvent event) -> {
                String s = "";
                String p = TextTools.delimiterText(delimiter);
                int colsNumber = inputs[0].length;
                copiedRow = new ArrayList<>();
                for (int i = 0; i < colsNumber; ++i) {
                    String v = value(row, i);
                    s += v + p;
                    copiedRow.add(v);
                }
                if (FxmlControl.copyToSystemClipboard(s)) {
                    popInformation(message("CopiedInSheet"));
                }
            });
            items.add(menu);

            if (copiedRow != null && !copiedRow.isEmpty()) {
                menu = new MenuItem(message("Paste"));
                menu.setOnAction((ActionEvent event) -> {
                    isSettingValues = true;
                    for (int i = 0; i < Math.min(inputs[0].length, copiedRow.size()); ++i) {
                        inputs[row][i].setText(copiedRow.get(i));
                    }
                    isSettingValues = false;
                    sheetChanged();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("InsertRowAbove"));
            menu.setOnAction((ActionEvent event) -> {
                String[][] current = data();
                int rowsNumber = current.length;
                int colsNumber = current[0].length;
                String[][] values = new String[++rowsNumber][colsNumber];
                for (int j = 0; j < row; ++j) {
                    for (int i = 0; i < colsNumber; ++i) {
                        values[j][i] = current[j][i];
                    }
                }
                for (int i = 0; i < colsNumber; ++i) {
                    values[row][i] = defaultValue;
                }
                for (int j = row + 1; j < rowsNumber; ++j) {
                    for (int i = 0; i < colsNumber; ++i) {
                        values[j][i] = current[j - 1][i];
                    }
                }
                makeSheet(values);
            });
            items.add(menu);

            menu = new MenuItem(message("InsertRowBelow"));
            menu.setOnAction((ActionEvent event) -> {
                String[][] current = data();
                int rowsNumber = current.length;
                int colsNumber = current[0].length;
                String[][] values = new String[++rowsNumber][colsNumber];
                for (int j = 0; j <= row; ++j) {
                    for (int i = 0; i < colsNumber; ++i) {
                        values[j][i] = current[j][i];
                    }
                }
                for (int i = 0; i < colsNumber; ++i) {
                    values[row + 1][i] = defaultValue;
                }
                for (int j = row + 2; j < rowsNumber; ++j) {
                    for (int i = 0; i < colsNumber; ++i) {
                        values[j][i] = current[j - 1][i];
                    }
                }
                makeSheet(values);
            });
            items.add(menu);

            if (inputs.length > 1) {
                menu = new MenuItem(message("DeleteRow"));
                menu.setOnAction((ActionEvent event) -> {
                    String[][] current = data();
                    int rowsNumber = current.length;
                    int colsNumber = current[0].length;
                    String[][] values = new String[rowsNumber - 1][colsNumber];
                    int index = 0;
                    for (int j = 0; j < rowsNumber; ++j) {
                        if (j == row) {
                            continue;
                        }
                        for (int i = 0; i < colsNumber; ++i) {
                            values[index][i] = current[j][i];
                        }
                        index++;
                    }
                    makeSheet(values);
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetRowValues"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue(rowsCheck[row].getText(), message("SetRowValues"), "");
                if (value == null) {
                    return;
                }
                isSettingValues = true;
                for (int i = 0; i < inputs[0].length; ++i) {
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
        if (inputs == null) {
            return;
        }
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

            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetCopyMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("CopyAll"));
            menu.setOnAction((ActionEvent event) -> {
                copyTextAction();
            });
            items.add(menu);
            items.add(new SeparatorMenuItem());

            rowsSelected = false;
            for (int j = 0; j < rowsCheck.length; ++j) {
                if (rowsCheck[j].isSelected()) {
                    rowsSelected = true;
                    break;
                }
            }
            colsSelected = false;
            for (int j = 0; j < colsCheck.length; ++j) {
                if (colsCheck[j].isSelected()) {
                    colsSelected = true;
                    break;
                }
            }

            menu = new MenuItem(message("CopySelectedRows"));
            menu.setOnAction((ActionEvent event) -> {
                if (!rowsSelected) {
                    popError(message("NoData"));
                    return;
                }
                copySelectedRows();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(message("CopySelectedCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected) {
                    popError(message("NoData"));
                    return;
                }
                copySelectedCols();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(message("CopySelectedRowsCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected || !rowsSelected) {
                    popError(message("NoData"));
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
        String s = "";
        String p = TextTools.delimiterText(delimiter);
        int rowsNumber = inputs.length;
        int colsNumber = inputs[0].length;
        for (int j = 0; j < rowsNumber; ++j) {
            if (!rowsCheck[j].isSelected()) {
                continue;
            }
            for (int i = 0; i < colsNumber; ++i) {
                s += value(j, i) + p;
            }
            s += "\n";
        }
        if (s.isBlank()) {
            popError(message("NoData"));
        } else if (FxmlControl.copyToSystemClipboard(s)) {
            popInformation(message("CopiedToSystemClipboard"));
        }
    }

    public void copySelectedCols() {
        String s = "";
        String p = TextTools.delimiterText(delimiter);
        int rowsNumber = inputs.length;
        int colsNumber = inputs[0].length;
        for (int j = 0; j < rowsNumber; ++j) {
            boolean has = false;
            for (int i = 0; i < colsNumber; ++i) {
                if (!colsCheck[i].isSelected()) {
                    continue;
                }
                has = true;
                s += value(j, i) + p;
            }
            if (!has) {
                break;
            }
            s += "\n";
        }
        if (s.isBlank()) {
            popError(message("NoData"));
        } else if (FxmlControl.copyToSystemClipboard(s)) {
            popInformation(message("CopiedToSystemClipboard"));
        }
    }

    public void copySelectedRowsCols() {
        String s = "";
        String p = TextTools.delimiterText(delimiter);
        int rowsNumber = inputs.length;
        int colsNumber = inputs[0].length;
        for (int j = 0; j < rowsNumber; ++j) {
            if (!rowsCheck[j].isSelected()) {
                continue;
            }
            boolean has = false;
            for (int i = 0; i < colsNumber; ++i) {
                if (!colsCheck[i].isSelected()) {
                    continue;
                }
                has = true;
                s += value(j, i) + p;
            }
            if (!has) {
                break;
            }
            s += "\n";
        }
        if (s.isBlank()) {
            popError(message("NoData"));
        } else if (FxmlControl.copyToSystemClipboard(s)) {
            popInformation(message("CopiedToSystemClipboard"));
        }
    }

    @FXML
    public void sheetDeleteMenu(MouseEvent mouseEvent) {
        if (inputs == null) {
            return;
        }
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

            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetDeleteMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu;

            rowsSelected = false;
            for (int j = 0; j < rowsCheck.length; ++j) {
                if (rowsCheck[j].isSelected()) {
                    rowsSelected = true;
                    break;
                }
            }
            colsSelected = false;
            for (int j = 0; j < colsCheck.length; ++j) {
                if (colsCheck[j].isSelected()) {
                    colsSelected = true;
                    break;
                }
            }

            menu = new MenuItem(message("DeleteSelectedRows"));
            menu.setOnAction((ActionEvent event) -> {
                int number = 0;
                int rowsNumber = inputs.length;
                for (int j = 0; j < rowsNumber; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        continue;
                    }
                    number++;
                }
                if (rowsNumber == number || number == 0) {
                    popError(message("NoData"));
                    return;
                }
                String[][] current = data();
                int colsNumber = current[0].length;
                String[][] values = new String[number][colsNumber];
                number = 0;
                for (int j = 0; j < rowsNumber; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        continue;
                    }
                    for (int i = 0; i < colsNumber; ++i) {
                        values[number][i] = current[j][i];
                    }
                    number++;
                }
                makeSheet(values);
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(message("DeleteSelectedCols"));
            menu.setOnAction((ActionEvent event) -> {
                int colsNumber = inputs[0].length;
                List<ColumnDefinition> newColumns = new ArrayList<>();
                for (int i = 0; i < colsNumber; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        newColumns.add(columns.get(i));
                    }
                }
                int newNumber = newColumns.size();
                if (colsNumber == newColumns.size() || newNumber == 0) {
                    popError(message("NoData"));
                    return;
                }
                columns = newColumns;
                deleteSelectedCols();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public void deleteSelectedCols() {
        String[][] current = data();
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

    @FXML
    public void sheetEqualMenu(MouseEvent mouseEvent) {
        if (inputs == null) {
            return;
        }
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

            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetEqualMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("SetAllValues"));
            menu.setOnAction((ActionEvent event) -> {
                setAllValues();
            });
            items.add(menu);

            rowsSelected = false;
            for (int j = 0; j < rowsCheck.length; ++j) {
                if (rowsCheck[j].isSelected()) {
                    rowsSelected = true;
                    break;
                }
            }
            colsSelected = false;
            for (int j = 0; j < colsCheck.length; ++j) {
                if (colsCheck[j].isSelected()) {
                    colsSelected = true;
                    break;
                }
            }

            menu = new MenuItem(message("SetSelectedRowsValues"));
            menu.setOnAction((ActionEvent event) -> {
                if (!rowsSelected) {
                    popError(message("NoData"));
                    return;
                }
                setSelectedRows();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(message("SetSelectedColsValues"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected) {
                    popError(message("NoData"));
                    return;
                }
                setSelectedCols();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(message("SetSelectedRowsColsValues"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected || !rowsSelected) {
                    popError(message("NoData"));
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
        String value = askValue(message("All"), message("SetValue"), "");
        if (value == null) {
            return;
        }
        for (int j = 0; j < inputs.length; ++j) {
            for (int i = 0; i < inputs[0].length; ++i) {
                inputs[j][i].setText(value);
            }
        }
        sheetChanged();
    }

    public void setSelectedRows() {
        String value = askValue(message("SelectedRows"), message("SetValue"), "");
        if (value == null) {
            return;
        }
        for (int j = 0; j < inputs.length; ++j) {
            if (rowsCheck[j].isSelected()) {
                for (int i = 0; i < inputs[0].length; ++i) {
                    inputs[j][i].setText(value);
                }
            }
        }
        sheetChanged();

    }

    public void setSelectedCols() {
        String value = askValue(message("SelectedCols"), message("SetValue"), "");
        if (value == null) {
            return;
        }
        for (int i = 0; i < inputs[0].length; ++i) {
            if (colsCheck[i].isSelected()) {
                for (int j = 0; j < inputs.length; ++j) {
                    inputs[j][i].setText(value);
                }
            }
        }
        sheetChanged();
    }

    public void setSelectedRowsCols() {
        String value = askValue(message("SetSelectedRowsColsValues"), message("SetValue"), "");
        if (value == null) {
            return;
        }
        for (int j = 0; j < inputs.length; ++j) {
            if (rowsCheck[j].isSelected()) {
                for (int i = 0; i < inputs[0].length; ++i) {
                    if (colsCheck[i].isSelected()) {
                        inputs[j][i].setText(value);
                    }
                }
            }
        }
    }

    public List<MenuItem> equalMoreMenu() {
        return null;
    }

    @FXML
    public void sheetSizeMenu(MouseEvent mouseEvent) {
        if (inputs == null) {
            return;
        }
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

            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetSizeMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("EnlargerAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < inputs[0].length; ++i) {
                    double width = colsCheck[i].getWidth() + widthChange;
                    colsCheck[i].setPrefWidth(width);
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][i].setPrefWidth(width);
                    }
                    makeDataDefintion();
                }
            });
            items.add(menu);

            menu = new MenuItem(message("ReduceAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < inputs[0].length; ++i) {
                    if (colsCheck[i].getWidth() < widthChange * 1.5) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() - widthChange;
                    colsCheck[i].setPrefWidth(width);
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][i].setPrefWidth(width);
                    }
                    makeDataDefintion();
                }
            });
            items.add(menu);

            menu = new MenuItem(message("SetAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", message("SetAllColsWidth"), (int) (colsCheck[0].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    for (int i = 0; i < inputs[0].length; ++i) {
                        colsCheck[i].setPrefWidth(width);
                        for (int j = 0; j < inputs.length; ++j) {
                            inputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDataDefintion();
                } catch (Exception e) {
                    popError(message("InvalidData"));
                }
            });
            items.add(menu);

            colsSelected = false;
            for (int j = 0; j < colsCheck.length; ++j) {
                if (colsCheck[j].isSelected()) {
                    colsSelected = true;
                    break;
                }
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("EnlargerSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < inputs[0].length; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() + widthChange;
                    colsCheck[i].setPrefWidth(width);
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][i].setPrefWidth(width);
                    }
                }
                makeDataDefintion();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(message("ReduceSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < inputs[0].length; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        continue;
                    }
                    if (colsCheck[i].getWidth() < widthChange * 1.5) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() - widthChange;
                    colsCheck[i].setPrefWidth(width);
                    for (int j = 0; j < inputs.length; ++j) {
                        inputs[j][i].setPrefWidth(width);
                    }
                    makeDataDefintion();
                }
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(message("SetSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", message("SetSelectedColsWidth"), (int) (colsCheck[0].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    for (int i = 0; i < inputs[0].length; ++i) {
                        if (!colsCheck[i].isSelected()) {
                            continue;
                        }
                        colsCheck[i].setPrefWidth(width);
                        for (int j = 0; j < inputs.length; ++j) {
                            inputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDataDefintion();
                } catch (Exception e) {
                    popError(message("InvalidData"));
                }
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("AddRowsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", message("AddRowsNumber"), "1");
                if (value == null) {
                    return;
                }
                try {
                    int number = Integer.parseInt(value);
                    resizeSheet(inputs.length + number, inputs[0].length);
                } catch (Exception e) {
                    popError(message("InvalidData"));
                }
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

    public List<MenuItem> sheetSizeMoreMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("AddColsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", message("AddColsNumber"), "1");
                if (value == null) {
                    return;
                }
                try {
                    int number = Integer.parseInt(value);
                    if (dataType != DataType.Matrix) {
                        for (int i = 0; i < number; i++) {
                            ColumnDefinition column = new ColumnDefinition(message("Field") + (columns.size() + i), ColumnType.String);
                            columns.add(column);
                        }
                    }
                    resizeSheet(inputs.length, inputs[0].length + number);
                } catch (Exception e) {
                    popError(message("InvalidData"));
                }
            });
            items.add(menu);

            menu = new MenuItem(message("SetRowsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", message("SetRowsNumber"), inputs.length + "");
                if (value == null) {
                    return;
                }
                try {
                    int number = Integer.parseInt(value);
                    resizeSheet(number, inputs[0].length);
                } catch (Exception e) {
                    popError(message("InvalidData"));
                }
            });
            items.add(menu);

            menu = new MenuItem(message("SetColumnsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", message("SetColumnsNumber"), inputs[0].length + "");
                if (value == null) {
                    return;
                }
                try {
                    int number = Integer.parseInt(value);
                    if (dataType != DataType.Matrix) {
                        for (int i = columns.size(); i < number; i++) {
                            ColumnDefinition column = new ColumnDefinition(message("Field") + (i + 1), ColumnType.String);
                            columns.add(column);
                        }
                    }
                    resizeSheet(inputs.length, number);
                } catch (Exception e) {
                    popError(message("InvalidData"));
                }
            });
            items.add(menu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    @FXML
    public void textDelimiterMenu(MouseEvent mouseEvent) {
        if (inputs == null) {
            return;
        }
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            List<String> dlist = Arrays.asList(
                    message("Blank"), message("Tab"), ",", message("Blank4"), message("Blank8"),
                    "|", "#", ";", "@"
            );
            MenuItem menu;
            for (String d : dlist) {
                menu = new MenuItem(d);
                menu.setOnAction((ActionEvent event) -> {
                    if (d.equals(message("Blank"))) {
                        delimiter = "blank";
                    } else if (d.equals(message("Blank4"))) {
                        delimiter = "blank4";
                    } else if (d.equals(message("Blank8"))) {
                        delimiter = "blank8";
                    } else if (message("Tab").equals(d)) {
                        delimiter = "tab";
                    } else {
                        delimiter = d;
                    }
                    AppVariables.setUserConfigValue(baseName + "Delimiter", delimiter);
                    textArea.setText(TextTools.dataText(data(), delimiter));
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!dataChanged) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVariables.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
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
