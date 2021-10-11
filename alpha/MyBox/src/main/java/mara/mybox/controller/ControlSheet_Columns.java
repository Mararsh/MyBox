package mara.mybox.controller;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
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
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Columns extends ControlSheet_Base {

    protected List<ColumnDefinition> checkColumns() {
        if (columns == null) {
            if (colsCheck != null && colsCheck.length > 0) {
                makeColumns(colsCheck.length);
            } else if (pageData != null && pageData.length > 0) {
                makeColumns(pageData[0].length);
            }
        }
        return columns;
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

    protected List<String> rowNames(int end) {
        try {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < end; i++) {
                names.add(rowName(i));
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    protected String rowName(int row) {
        return message("Row") + (currentPageStart + row + 1);
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

    protected int colIndex(String name) {
        try {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getName().equals(name)) {
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    protected ColumnDefinition col(String name) {
        try {
            for (ColumnDefinition col : columns) {
                if (col.getName().equals(name)) {
                    return col;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    // 0=based
    public List<Integer> colsIndex(boolean isAll) {
        if (colsCheck == null) {
            return null;
        }
        List<Integer> cols = new ArrayList<>();
        for (int c = 0; c < colsCheck.length; c++) {
            if (isAll || colsCheck[c].isSelected()) {
                cols.add(c);
            }
        }
        return cols;
    }

    public List<Integer> rowsIndex(boolean isAll) {
        if (rowsCheck == null) {
            return null;
        }
        List<Integer> rows = new ArrayList<>();
        for (int r = 0; r < rowsCheck.length; ++r) {
            if (isAll || rowsCheck[r].isSelected()) {
                rows.add(r);
            }
        }
        return rows;
    }

    protected String titleName() {
        if (sourceFile == null) {
            return "";
        }
        return sourceFile.getAbsolutePath();
    }

    protected boolean cellValid(int col, String value) {
        try {
            if (checkColumns() == null) {
                return false;
            }
            ColumnDefinition column = columns.get(col);
            return column.validValue(value);
        } catch (Exception e) {
        }
        return false;
    }

    public void makeDefintionPane() {
        if (defBox == null) {
            return;
        }
        defBox.getChildren().clear();
        if (!tabPane.getTabs().contains(defTab)) {
            tabPane.getTabs().add(4, defTab);
        }
        if (columns == null && pageData != null && pageData.length > 0) {
            makeColumns(pageData[0].length);
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

            ComboBox<String> typeSelector = new ComboBox<>();
            typeSelector.setPrefWidth(150);
            for (ColumnDefinition.ColumnType type : ColumnDefinition.editTypes()) {
                typeSelector.getItems().add(message(type.name()));
            }
            typeSelector.setValue(message(column.getType().name()));

            CheckBox notNull = new CheckBox(message("Yes"));
            notNull.setPrefWidth(80);
            notNull.setMinWidth(Region.USE_PREF_SIZE);
            notNull.setSelected(column.isNotNull());

            ComboBox<String> lenSelector = new ComboBox<>();
            lenSelector.setEditable(true);
            List<String> lens = Arrays.asList("128", "512", "1028", "2048", "4096", "40960",
                    message("Maximum") + " " + BaseTable.StringMaxLength);
            lenSelector.getItems().setAll(lens);
            lenSelector.setValue(column.getLength() + "");
            lenSelector.setPrefWidth(120);

            TextField widthInput = new TextField(column.getWidth() + "");
            widthInput.setPrefWidth(80);
            widthInput.setMinWidth(Region.USE_PREF_SIZE);

            line.getChildren().addAll(indexLabel, nameInput, typeSelector, notNull, lenSelector, widthInput);
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
                nameInput.setStyle(UserConfig.badStyle());
                ok = false;
            } else {
                column.setName(nameInput.getText().trim());
                nameInput.setStyle(null);
            }

            ComboBox<String> lenSelector = (ComboBox) (line.getChildren().get(4));
            try {
                String v = lenSelector.getValue();
                if (v.startsWith(message("Maximum"))) {
                    column.setLength(BaseTable.StringMaxLength);
                    lenSelector.getEditor().setStyle(null);
                } else {
                    int len = Integer.valueOf(v);
                    if (len > 0 && len <= BaseTable.StringMaxLength) {
                        column.setLength(len);
                        lenSelector.getEditor().setStyle(null);
                    } else {
                        ok = false;
                        lenSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            } catch (Exception e) {
                ok = false;
                lenSelector.getEditor().setStyle(UserConfig.badStyle());
            }

            TextField widthInput = (TextField) (line.getChildren().get(5));
            try {
                double v = Double.parseDouble(widthInput.getText());
                if (v > 10) {
                    column.setWidth((int) v);
                    widthInput.setStyle(null);
                } else {
                    ok = false;
                    widthInput.setStyle(UserConfig.badStyle());
                }
            } catch (Exception e) {
                ok = false;
                widthInput.setStyle(UserConfig.badStyle());
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

                if (!validateColumns(newValues)) {
                    return;
                }
            }
        }
        if (ok) {
            popSuccessful();
            makeSheet(pickData(), newValues);
        } else {
            popError(message("InvalidData"));
        }
    }

    @FXML
    public void recoverDefAction() {
        makeDefintionPane();
    }

    @FXML
    public void renameDefAction() {
        if (defBox == null) {
            return;
        }
        List<Node> nodes = defBox.getChildren();
        for (int i = 0; i < nodes.size(); i++) {
            HBox line = (HBox) nodes.get(i);
            TextField nameInput = (TextField) (line.getChildren().get(1));
            nameInput.setText(message(colPrefix) + (i + 1));
        }
    }

    public boolean validateColumns(List<ColumnDefinition> columns) {
        if (columns == null || columns.isEmpty()) {
            return false;
        }
        try {
            StringTable validateTable = ColumnDefinition.validate(columns);
            if (validateTable != null) {
                if (validateTable.isEmpty()) {
                    return true;
                } else {
                    Platform.runLater(() -> {
                        validateTable.htmlTable();
                    });
                }
            }
            if (task != null) {
                task.setError(message("InvalidColumns"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @FXML
    public void validateData() {
        if (validateChange()) {
            popInformation(message("DataAreValid"));
        }
    }

    public boolean validateChange() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Reason")));
            StringTable table = new StringTable(names, message("InvalidData"));
            if (sheetInputs != null) {
                for (int i = 0; i < sheetInputs.length; i++) {
                    for (int j = 0; j < sheetInputs[i].length; j++) {
                        String value = cellString(i, j);
                        if (!cellValid(j, value)) {
                            sheetInputs[i][j].setStyle(UserConfig.badStyle());
                            List<String> row = new ArrayList<>();
                            row.addAll(Arrays.asList((currentPageStart + i + 1) + "", (j + 1) + "",
                                    (value == null || value.isBlank() ? message("Null") : message("InvalidValue"))));
                            table.add(row);
                        } else {
                            sheetInputs[i][j].setStyle(inputStyle);
                        }
                    }
                }
            }
            if (!table.isEmpty()) {
                table.htmlTable();
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean saveDefinition(String dataName, DataDefinition.DataType dataType,
            Charset charset, String delimiterName, boolean withName, List<ColumnDefinition> columns) {
        if (dataName == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveDefinition(conn, dataName, dataType, charset, delimiterName, withName, columns);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean saveDefinition(Connection conn, String dataName, DataDefinition.DataType dataType,
            Charset charset, String delimiterName, boolean withName, List<ColumnDefinition> columns) {
        if (conn == null || dataName == null) {
            return false;
        }
        StringTable validReport = DataDefinition.saveDefinition(tableDataDefinition, tableDataColumn, conn,
                dataName, dataType, charset, delimiterName, withName, columns);
        if (validReport != null && !validReport.isEmpty()) {
            Platform.runLater(() -> {
                validReport.htmlTable();
            });
            if (task != null) {
                task.setError(message("InvalidColumns"));
            }
            return false;
        } else {
            return true;
        }
    }

    /*
        abstract
     */
    protected boolean saveDefinition() {
        return true;
    }

    protected abstract String[][] pickData();

    public abstract void makeSheet(String[][] data, List<ColumnDefinition> columns);

    protected abstract String cellString(int row, int col);

}
