package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Delete extends ControlSheet_Equal {

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
            int num = 0;
            int total = rowsCheck == null ? 0 : rowsCheck.length;
            for (int r = 0; r < total; ++r) {
                if (rowsCheck[r].isSelected()) {
                    num++;
                }
            }
            final int selectedRows = num;
            MenuItem menu = new MenuItem(Languages.message("DeleteSelectedRows"));
            menu.setOnAction((ActionEvent event) -> {
                if (total == selectedRows) {
                    if (!PopTools.askSure(Languages.message("DeleteSelectedRows"), Languages.message("SureDeleteAll"))) {
                        return;
                    }
                    deletePageAllRows();
                } else {
                    String[][] current = pickData();
                    int cols = current[0].length;
                    String[][] values = new String[total - selectedRows][cols];
                    int rowIndex = 0;
                    for (int r = 0; r < total; ++r) {
                        if (rowsCheck[r].isSelected()) {
                            continue;
                        }
                        for (int c = 0; c < cols; ++c) {
                            values[rowIndex][c] = current[r][c];
                        }
                        rowIndex++;
                    }
                    makeSheet(values);
                }
            });
            menu.setDisable(selectedRows == 0);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> makeSheetDeleteColsMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            int num = 0;
            int total = colsCheck == null ? 0 : colsCheck.length;
            for (int c = 0; c < total; ++c) {
                if (colsCheck[c].isSelected()) {
                    num++;
                }
            }
            final int selectedcols = num;
            MenuItem menu = new MenuItem(Languages.message("DeleteSelectedCols"));
            menu.setOnAction((ActionEvent event) -> {
                List<ColumnDefinition> unselectedColumns = new ArrayList<>();
                for (int c = 0; c < total; ++c) {
                    if (!colsCheck[c].isSelected()) {
                        unselectedColumns.add(columns.get(c));
                    }
                }
                int unselectedNumber = unselectedColumns.size();
                if (total == unselectedColumns.size()) {
                    return;
                }
                if (unselectedNumber == 0) {
                    if (!PopTools.askSure(Languages.message("DeleteSelectedCols"), Languages.message("SureDeleteAll"))) {
                        return;
                    }
                    deleteAllCols();
                } else {
                    columns = unselectedColumns;
                    deleteSelectedCols();
                }
            });
            menu.setDisable(selectedcols == 0);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
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
            int rowsLen = current.length;
            String[][] values = new String[rowsLen][columns.size()];
            for (int j = 0; j < rowsLen; ++j) {
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

    protected void deletePageAllRows() {
        makeSheet(null);
    }

    protected void deleteAllCols() {
        if (columns != null) {
            columns.clear();
        }
        makeSheet(null);
    }

}
