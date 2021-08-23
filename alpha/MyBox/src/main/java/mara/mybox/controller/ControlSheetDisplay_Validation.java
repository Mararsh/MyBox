package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetDisplay_Validation extends ControlSheetDisplay_Base {

    protected String titleName() {
        if (sourceFile == null) {
            return "";
        }
        return sourceFile.getAbsolutePath();
    }

    protected List<ColumnDefinition> checkColumns() {
        if (columns == null && sheet != null && sheet.length > 0) {
            makeColumns(sheet[0].length);
        }
        return columns;
    }

    protected List<String> columnNames() {
        try {
            if (checkColumns() == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                names.add(colName(i));
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    protected List<String> rowNames(int rows) {
        try {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                names.add(sheetController.rowName(i));
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    public void makeColumns() {
        columns = null;
        checkColumns();
    }

    public void makeColumns(int number) {
        columns = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            ColumnDefinition column = new ColumnDefinition(message(colPrefix) + i, defaultColumnType, defaultColNotNull);
            columns.add(column);
        }
    }

    // start: 0-based
    public void makeColumns(int start, int number) {
        if (columns == null) {
            makeColumns(start);
        }
        List<String> columnNames = columnNames();
        List<ColumnDefinition> newColumns = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            String name = message(colPrefix) + (start + i);
            while (columnNames.contains(name)) {
                name += "m";
            }
            ColumnDefinition column = new ColumnDefinition(name, defaultColumnType, defaultColNotNull);
            newColumns.add(column);
            columnNames.add(name);
        }
        columns.addAll(start, newColumns);
    }

    protected String rowName(int row) {
        return message("Row") + (row + 1);
    }

    protected String colName(int col) {
        try {
            if (checkColumns() == null || columns.size() <= col) {
                return null;
            }
            return columns.get(col).getName();
        } catch (Exception e) {
            return null;
        }

    }

    protected boolean dataValid(int col, String value) {
        try {
            if (checkColumns() == null) {
                return false;
            }
            ColumnDefinition column = columns.get(col);
            return column.valid(value);
        } catch (Exception e) {
        }
        return false;
    }

    protected String[][] pickData() {
        try {
            if (sheet == null || sheet.length == 0) {
                rowsNumber = colsNumber = 0;
            } else {
                rowsNumber = sheet.length;
                colsNumber = sheet[0].length;
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
        return sheet;
    }

    public void makeDefintion() {
        if (defBox == null) {
            return;
        }
        defBox.getChildren().clear();
        if (columns == null && sheet != null && sheet.length > 0) {
            makeColumns(sheet[0].length);
        }
        if (columns == null) {
            return;
        }
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
            for (ColumnDefinition.ColumnType type : ColumnDefinition.editTypes()) {
                typeSelector.getItems().add(message(type.name()));
            }
            typeSelector.setValue(message(column.getType().name()));

            line.getChildren().addAll(indexLabel, nameInput, typeSelector, notNull, widthInput);
            defBox.getChildren().add(line);

        }
        refreshStyle(defBox);
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
                nameInput.setStyle(NodeStyleTools.badStyle);
                ok = false;
            } else {
                column.setName(nameInput.getText().trim());
                nameInput.setStyle(null);
            }

            TextField widthInput = (TextField) (line.getChildren().get(4));
            try {
                double v = Double.parseDouble(widthInput.getText());
                if (v > 10) {
                    column.setWidth((int) v);
                    widthInput.setStyle(null);
                } else {
                    ok = false;
                    widthInput.setStyle(NodeStyleTools.badStyle);
                }
            } catch (Exception e) {
                ok = false;
                widthInput.setStyle(NodeStyleTools.badStyle);
            }

            if (ok) {
                CheckBox notNull = (CheckBox) (line.getChildren().get(3));
                column.setNotNull(notNull.isSelected());
            }

            ComboBox<String> typeSelector = (ComboBox) (line.getChildren().get(2));
            String ctype = typeSelector.getValue();
            if (ctype == null) {
                ok = false;
            } else if (ok) {
                for (ColumnDefinition.ColumnType type : ColumnDefinition.ColumnType.values()) {
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
            popSaved();
            sheetController.makeSheet(sheet, columns);
        } else {
            popError(message("InvalidData"));
        }
    }

    @FXML
    public void recoverDefAction() {
        makeDefintion();
    }

    @FXML
    public void clearDefAction() {
        makeColumns();
        makeDefintion();
    }

    protected void validate() {
        try {
            dataInvalid = false;
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Reason")));
            StringTable table = new StringTable(names, message("InvalidData"));
            if (sheetController != null && sheetController.inputs != null) {
                for (int i = 0; i < sheetController.inputs.length; i++) {
                    for (int j = 0; j < sheetController.inputs[i].length; j++) {
                        String value = sheetController.value(i, j);
                        if (!dataValid(j, value)) {
                            sheetController.inputs[i][j].setStyle(NodeStyleTools.badStyle);
                            List<String> row = new ArrayList<>();
                            row.addAll(Arrays.asList((i + 1) + "", (j + 1) + "",
                                    (value == null || value.isBlank() ? message("Null") : message("InvalidValue"))));
                            table.add(row);
                        } else {
                            sheetController.inputs[i][j].setStyle(inputStyle);
                        }
                    }
                }
            }
            dataInvalid = !table.isEmpty();
            if (saveButton != null) {
                saveButton.setDisable(dataInvalid);
            }
            if (dataInvalid) {
                sheetController.dataIsInvalid();
            }
            if (reportViewController == null) {
                return;
            }
            reportViewController.webEngine.getLoadWorker().cancel();
            if (dataInvalid) {
                reportViewController.webEngine.loadContent(table.html());
                tabPane.getSelectionModel().select(reportTab);
            } else {
                reportViewController.webEngine.loadContent("<H2 align=\"center\">" + message("DataAreValid") + "</H2>");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
