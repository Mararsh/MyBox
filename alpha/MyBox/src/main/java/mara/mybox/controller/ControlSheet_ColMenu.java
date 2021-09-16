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
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
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

            menu = new MenuItem(message("InsertColLeft"));
            menu.setOnAction((ActionEvent event) -> {
                addCols(col, true, 1);
            });
            menu.setDisable(dataChangedNotify.get() && sourceFile != null && pagesNumber > 1);
            items.add(menu);

            menu = new MenuItem(message("InsertColRight"));
            menu.setOnAction((ActionEvent event) -> {
                addCols(col, false, 1);
            });
            menu.setDisable(dataChangedNotify.get() && sourceFile != null && pagesNumber > 1);
            items.add(menu);

            menu = new MenuItem(message("DeleteCol"));
            menu.setOnAction((ActionEvent event) -> {
                List<Integer> cols = new ArrayList<>();
                cols.add(col);
                deleteCols(cols);
            });
            menu.setDisable(dataChangedNotify.get() && sourceFile != null && pagesNumber > 1);
            items.add(menu);

            if (dataType != DataDefinition.DataType.Matrix) {
                menu = new MenuItem(message("Rename"));
                menu.setOnAction((ActionEvent event) -> {
                    String value = PopTools.askValue(baseTitle, colsCheck[col].getText(),
                            message("Rename"), colsCheck[col].getText());
                    if (value == null || value.isBlank()) {
                        return;
                    }
                    if (columnNames().contains(value)) {
                        popError(message("Duplicated"));
                        return;
                    }
                    colsCheck[col].setText(value);
                    columns.get(col).setName(value);
                    makeDefintionPane();
                    sheetChanged(true);
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetValues") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataEqualController controller = (DataEqualController) openChildStage(Fxmls.DataEqualFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            menu = new MenuItem(message("CopyToSystemClipboard") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataCopyToSystemClipboardController controller = (DataCopyToSystemClipboardController) openChildStage(Fxmls.DataCopyToSystemClipboardFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            menu = new MenuItem(message("CopyToMyBoxClipboard") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataCopyToMyBoxClipboardController controller = (DataCopyToMyBoxClipboardController) openChildStage(Fxmls.DataCopyToMyBoxClipboardFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            menu = new MenuItem(message("Paste") + "...");
            menu.setOnAction((ActionEvent event) -> {
                myBoxClipBoard();
            });
            items.add(menu);

            menu = new MenuItem(message("Width") + " ...");
            menu.setOnAction((ActionEvent event) -> {
                DataWidthController controller = (DataWidthController) openChildStage(Fxmls.DataWidthFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            menu = new MenuItem(message("Order") + " ...");
            menu.setOnAction((ActionEvent event) -> {
                DataSortController controller = (DataSortController) openChildStage(Fxmls.DataSortFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("Add") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataColumnsAddController controller = (DataColumnsAddController) openChildStage(Fxmls.DataColumnsAddFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("Delete") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataColumnsDeleteController controller = (DataColumnsDeleteController) openChildStage(Fxmls.DataColumnsDeleteFxml, false);
                controller.setParameters((ControlSheet) this, -1, col);
            });
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
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

}
