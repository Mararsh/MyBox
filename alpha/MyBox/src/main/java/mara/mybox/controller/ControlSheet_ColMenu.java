package mara.mybox.controller;

import java.text.MessageFormat;
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
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataClipboard;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_ColMenu extends ControlSheet_RowMenu {

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

            MenuItem menu = new MenuItem(message("PopupClose"));
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
            MenuItem menu = new MenuItem(message("Column") + (col + 1) + ": " + colsCheck[col].getText());
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);

            items.add(new SeparatorMenuItem());
            items.addAll(colSelectMenu(col));

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("SetValues"));
            menu.setOnAction((ActionEvent event) -> {
                DataEqualController controller = (DataEqualController) openChildStage(Fxmls.DataEqualFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            items.addAll(colCopyPasteMenu(col));

            items.add(new SeparatorMenuItem());
            items.addAll(colDefMenu(col));

            items.add(new SeparatorMenuItem());
            items.addAll(colOrderMenu(col));

            items.add(new SeparatorMenuItem());
            items.addAll(colSizeMenu(col));

            if (dataType != DataDefinition.DataType.Matrix) {
                items.add(new SeparatorMenuItem());
                items.addAll(colRenameMenu(col));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colSelectMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("SelectCol"));
            menu.setOnAction((ActionEvent event) -> {
                colsCheck[col].setSelected(true);
            });
            items.add(menu);

            menu = new MenuItem(message("UnselectCol"));
            menu.setOnAction((ActionEvent event) -> {
                colsCheck[col].setSelected(false);
            });
            items.add(menu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colCopyPasteMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("Copy") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataCopyController controller = (DataCopyController) openChildStage(Fxmls.DataCopyFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("PastePageColFromSystemClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                pastePageColFromSystemClipboard(col);
            });
            menu.setDisable(rowsTotal() <= 0);
            items.add(menu);

            menu = new MenuItem(message("PastePageColFromDataClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                pastePageColFromDataClipboard(col);
            });
            menu.setDisable(rowsTotal() <= 0);
            items.add(menu);

            if (sourceFile != null && pagesNumber > 1) {
                menu = new MenuItem(message("PastePagesColFromSystemClipboard"));
                menu.setOnAction((ActionEvent event) -> {
                    pastePagesColFromSystemClipboard(col);
                });
                menu.setDisable(rowsTotal() <= 0);
                items.add(menu);

                menu = new MenuItem(message("PastePagesColFromDataClipboard"));
                menu.setOnAction((ActionEvent event) -> {
                    pastePagesColFromDataClipboard(col);
                });
                menu.setDisable(rowsTotal() <= 0);
                items.add(menu);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colDefMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("InsertColLeft"));
            menu.setOnAction((ActionEvent event) -> {
                if (sourceFile != null && pagesNumber > 1) {
                    insertPagesCol(col, true, 1);
                } else {
                    insertPageCol(col, true, 1);
                }
            });
            menu.setDisable(dataChangedNotify.get() && sourceFile != null && pagesNumber > 1);
            items.add(menu);

            menu = new MenuItem(message("InsertColRight"));
            menu.setOnAction((ActionEvent event) -> {
                if (sourceFile != null && pagesNumber > 1) {
                    insertPagesCol(col, false, 1);
                } else {
                    insertPageCol(col, false, 1);
                }
            });
            menu.setDisable(dataChangedNotify.get() && sourceFile != null && pagesNumber > 1);
            items.add(menu);

            menu = new MenuItem(message("DeleteCol"));
            menu.setOnAction((ActionEvent event) -> {
                DataDeleteController controller = (DataDeleteController) openChildStage(Fxmls.DataDeleteFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            menu.setDisable(dataChangedNotify.get() && sourceFile != null && pagesNumber > 1);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colOrderMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("OrderPageRowsAscByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderPageCol(col, true);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(message("OrderPageRowsDescByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderPageCol(col, false);
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            items.add(menu);

            boolean rowsSelected = false;
            int total = rowsCheck == null ? 0 : rowsCheck.length;
            for (int r = 0; r < total; ++r) {
                if (rowsCheck[r].isSelected()) {
                    rowsSelected = true;
                    break;
                }
            }

            menu = new MenuItem(message("OrderSelectedRowsAscByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderSelectedRows(col, true);
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(message("OrderSelectedRowsDescByThisCol"));
            menu.setOnAction((ActionEvent event) -> {
                orderSelectedRows(col, false);
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            if (sourceFile != null && pagesNumber > 1) {
                menu = new MenuItem(message("OrderPagesRowsAscByThisCol"));
                menu.setOnAction((ActionEvent event) -> {
                    orderPagesCol(col, true);
                });
                menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
                items.add(menu);

                menu = new MenuItem(message("OrderPagesRowsDescByThisCol"));
                menu.setOnAction((ActionEvent event) -> {
                    orderPagesCol(col, false);
                });
                menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
                items.add(menu);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> colSizeMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("EnlargerColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = colsCheck[col].getWidth() + widthChange;
                colsCheck[col].setPrefWidth(width);
                if (sheetInputs != null) {
                    for (int j = 0; j < sheetInputs.length; ++j) {
                        sheetInputs[j][col].setPrefWidth(width);
                    }
                }
                makeDefintionPane();
            });
            items.add(menu);

            menu = new MenuItem(message("ReduceColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = colsCheck[col].getWidth() - widthChange;
                colsCheck[col].setPrefWidth(width);
                if (sheetInputs != null) {
                    for (int j = 0; j < sheetInputs.length; ++j) {
                        sheetInputs[j][col].setPrefWidth(width);
                    }
                }
                makeDefintionPane();
            });
            menu.setDisable(colsCheck[col].getWidth() <= widthChange * 1.5);
            items.add(menu);

            menu = new MenuItem(message("SetColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue(colsCheck[col].getText(), message("SetColWidth"), (int) (colsCheck[col].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    colsCheck[col].setPrefWidth(width);
                    if (sheetInputs != null) {
                        for (int j = 0; j < sheetInputs.length; ++j) {
                            sheetInputs[j][col].setPrefWidth(width);
                        }
                    }
                    makeDefintionPane();
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

    public List<MenuItem> colRenameMenu(int col) {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(message("Rename"));
            menu.setOnAction((ActionEvent event) -> {
                String value = PopTools.askValue(baseTitle, colsCheck[col].getText(),
                        message("Rename"), colsCheck[col].getText());
                if (value == null || value.isBlank()) {
                    return;
                }
                colsCheck[col].setText(value);
                columns.get(col).setName(value);
                makeDefintionPane();
            });
            items.add(menu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    protected void orderPageCol(int col, boolean asc) {
        if (sheetInputs == null || columns == null || col < 0 || col >= columns.size()) {
            return;
        }
        int rNumber = sheetInputs.length;
        int cNumber = sheetInputs[0].length;
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < rNumber; i++) {
            rows.add(i);
        }
        Collections.sort(rows, new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                ColumnDefinition column = columns.get(col);
                int v = column.compare(cellString(row1, col), cellString(row2, col));
                return asc ? v : -v;
            }
        });
        String[][] data = new String[rNumber][cNumber];
        for (int row = 0; row < rows.size(); row++) {
            int drow = rows.get(row);
            for (int c = 0; c < cNumber; c++) {
                data[row][c] = cellString(drow, c);
            }
        }
        makeSheet(data);
    }

    protected void orderSelectedRows(int col, boolean asc) {
        if (sheetInputs == null || columns == null || colsCheck == null || col < 0 || col >= columns.size()) {
            return;
        }
        int rowsTotal = sheetInputs.length;
        int colsTotal = sheetInputs[0].length;
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
                int v = column.compare(cellString(row1, col), cellString(row2, col));
                return asc ? v : -v;
            }
        });
        int selectedTotal = selected.size();
        String[][] data = new String[rowsTotal][colsTotal];
        for (int r = 0; r < selectedTotal; r++) {
            int rowIndex = selected.get(r);
            for (int c = 0; c < colsTotal; c++) {
                data[r][c] = cellString(rowIndex, c);
            }
        }
        int index = selectedTotal;
        for (int r = 0; r < rowsTotal; r++) {
            if (rowsCheck[r].isSelected()) {
                continue;
            }
            for (int c = 0; c < colsTotal; c++) {
                data[index][c] = cellString(r, c);
            }
            index++;
        }
        makeSheet(data);
    }

    protected void pastePageColFromSystemClipboard(int col) {
        try {
            if (sheetInputs == null) {
                popError(message("NoData"));
                return;
            }
            String s = TextClipboardTools.getSystemClipboardString();
            if (s == null || s.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String[] values = s.split("\n");
            if (values.length > sheetInputs.length) {
                if (!PopTools.askSure(message("PastePageColFromSystemClipboard"),
                        MessageFormat.format(message("DataInClipboardMoreThanPage"), values.length, sheetInputs.length))) {
                    return;
                }
            } else if (values.length < sheetInputs.length) {
                if (!PopTools.askSure(message("PastePageColFromSystemClipboard"),
                        MessageFormat.format(message("DataInClipboardLessThanPage"), values.length, sheetInputs.length))) {
                    return;
                }
            }
            isSettingValues = true;
            for (int r = 0; r < Math.min(sheetInputs.length, values.length); ++r) {
                sheetInputs[r][col].setText(values[r]);
            }
            isSettingValues = false;
            sheetChanged();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void pastePageColFromDataClipboard(int col) {
        if (sheetInputs == null) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                List<List<String>> data;

                @Override
                protected boolean handle() {
                    data = DataClipboard.lastData(tableDataDefinition, sheetInputs.length, 1);
                    return data != null && !data.isEmpty();
                }

                @Override
                protected void whenSucceeded() {
                    if (data.size() > sheetInputs.length) {
                        if (!PopTools.askSure(message("PastePageColFromDataClipboard"),
                                MessageFormat.format(message("DataInClipboardMoreThanPage"), data.size(), sheetInputs.length))) {
                            return;
                        }
                    } else if (data.size() < sheetInputs.length) {
                        if (!PopTools.askSure(message("PastePageColFromDataClipboard"),
                                MessageFormat.format(message("DataInClipboardLessThanPage"), data.size(), sheetInputs.length))) {
                            return;
                        }
                    }
                    isSettingValues = true;
                    for (int r = 0; r < Math.min(sheetInputs.length, data.size()); ++r) {
                        List<String> row = data.get(r);
                        sheetInputs[r][col].setText(row.isEmpty() ? "" : row.get(0));
                    }
                    isSettingValues = false;
                    sheetChanged();
                }

            };
            start(task);
        }

    }

    /*
        Implemented in BaseDataFileController_ColMenu
     */
    protected void pastePagesColFromSystemClipboard(int col) {
        pastePageColFromSystemClipboard(col);
    }

    protected void pastePagesColFromDataClipboard(int col) {
        pastePagesColFromDataClipboard(col);
    }

    protected void insertPagesCol(int col, boolean left, int number) {
        insertPageCol(col, left, number);
    }

    protected void orderPagesCol(int col, boolean asc) {
        orderPageCol(col, asc);
    }

}
