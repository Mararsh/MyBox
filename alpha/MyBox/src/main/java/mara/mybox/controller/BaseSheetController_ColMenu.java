package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController_ColMenu extends BaseSheetController_RowMenu {

    // col: 0-based
    @Override
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

            if (dataType != DataDefinition.DataType.Matrix) {
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

            List<MenuItem> orderItems = colOrderMenu(col);
            if (orderItems != null && !orderItems.isEmpty()) {
                items.add(new SeparatorMenuItem());
                items.addAll(orderItems);
            }

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

    public List<MenuItem> colOrderMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(Languages.message("OrderPageRowsAscByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderPageRows(col, true);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("OrderPageRowsDescByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderPageRows(col, false);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            rowsSelected = false;
            int total = rowsCheck == null ? 0 : rowsCheck.length;
            for (int r = 0; r < total; ++r) {
                if (rowsCheck[r].isSelected()) {
                    rowsSelected = true;
                    break;
                }
            }

            menu = new MenuItem(Languages.message("OrderSelectedRowsAscByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderSelectedRows(col, true);
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("OrderSelectedRowsDescByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderSelectedRows(col, false);
            });
            menu.setDisable(!rowsSelected);
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

    @Override
    protected void deleteAllCols() {
        if (columns != null) {
            columns.clear();
        }
        makeSheet(null);
    }

    protected void orderPageRows(int col, boolean asc) {
        if (inputs == null || columns == null || col < 0 || col >= columns.size()) {
            return;
        }
        int rNumber = inputs.length;
        int cNumber = inputs[0].length;
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < rNumber; i++) {
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
        String[][] data = new String[rNumber][cNumber];
        for (int row = 0; row < rows.size(); row++) {
            int drow = rows.get(row);
            for (int c = 0; c < cNumber; c++) {
                data[row][c] = value(drow, c);
            }
        }
        makeSheet(data);
    }

    protected void orderSelectedRows(int col, boolean asc) {
        if (inputs == null || columns == null || colsCheck == null || col < 0 || col >= columns.size()) {
            return;
        }
        int rowsTotal = inputs.length;
        int colsTotal = inputs[0].length;
        List<Integer> selected = new ArrayList<>();
        for (int r = 0; r < rowsTotal; r++) {
            if (rowsCheck[r].isSelected()) {
                selected.add(r);
            }
        }
        ColumnDefinition column = columns.get(col);
        Collections.sort(selected, new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                int v = column.compare(value(row1, col), value(row2, col));
                return asc ? v : -v;
            }
        });
        int selectedTotal = selected.size();
        String[][] data = new String[rowsTotal][colsTotal];
        for (int r = 0; r < selectedTotal; r++) {
            int rowIndex = selected.get(r);
            for (int c = 0; c < colsTotal; c++) {
                data[r][c] = value(rowIndex, c);
            }
        }
        int index = selectedTotal;
        for (int r = 0; r < rowsTotal; r++) {
            if (rowsCheck[r].isSelected()) {
                continue;
            }
            for (int c = 0; c < colsTotal; c++) {
                data[index][c] = value(r, c);
            }
            index++;
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

}
