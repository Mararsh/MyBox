package mara.mybox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Copy extends ControlSheet_Delete {

    public String[][] selectedRows() {
        if (sheetInputs == null || rowsCheck == null) {
            return null;
        }
        int selectedRowsCount = 0;
        for (CheckBox c : rowsCheck) {
            if (c.isSelected()) {
                selectedRowsCount++;
            }
        }
        if (selectedRowsCount == 0) {
            return null;
        }
        rowsNumber = sheetInputs.length;
        colsNumber = sheetInputs[0].length;
        int rowIndex = 0;
        String[][] data = new String[selectedRowsCount][colsNumber];
        for (int r = 0; r < rowsNumber; ++r) {
            if (!rowsCheck[r].isSelected()) {
                continue;
            }
            for (int c = 0; c < colsNumber; ++c) {
                data[rowIndex][c] = cellString(r, c);
            }
            rowIndex++;
        }
        return data;
    }

    public Map<String, Object> pageSelectedCols() {
        if (sheetInputs == null || colsCheck == null) {
            return null;
        }
        rowsNumber = sheetInputs.length;
        if (rowsNumber == 0) {
            return null;
        }
        List<ColumnDefinition> selectedColumns = new ArrayList<>();
        for (int c = 0; c < colsCheck.length; c++) {
            if (colsCheck[c].isSelected()) {
                selectedColumns.add(columns.get(c));
            }
        }
        if (selectedColumns.isEmpty()) {
            return null;
        }
        String[][] data = new String[rowsNumber][selectedColumns.size()];
        for (int r = 0; r < rowsNumber; ++r) {
            int colIndex = 0;
            for (int c = 0; c < colsCheck.length; c++) {
                if (!colsCheck[c].isSelected()) {
                    continue;
                }
                data[r][colIndex] = cellString(r, c);
                colIndex++;
            }
        }
        Map<String, Object> selected = new HashMap<>();
        selected.put("columns", selectedColumns);
        selected.put("data", data);
        return selected;
    }

    public Map<String, Object> selectedRowsCols() {
        if (sheetInputs == null || colsCheck == null || rowsCheck == null) {
            return null;
        }
        int selectedRowsCount = 0;
        for (CheckBox c : rowsCheck) {
            if (c.isSelected()) {
                selectedRowsCount++;
            }
        }
        if (selectedRowsCount == 0) {
            return null;
        }
        List<ColumnDefinition> selectedColumns = new ArrayList<>();
        for (int c = 0; c < colsCheck.length; c++) {
            if (colsCheck[c].isSelected()) {
                selectedColumns.add(columns.get(c));
            }
        }
        if (selectedColumns.isEmpty()) {
            return null;
        }
        rowsNumber = sheetInputs.length;
        colsNumber = sheetInputs[0].length;
        int rowIndex = 0;
        String[][] data = new String[selectedRowsCount][selectedColumns.size()];
        for (int r = 0; r < rowsNumber; ++r) {
            if (!rowsCheck[r].isSelected()) {
                continue;
            }
            int colIndex = 0;
            for (int c = 0; c < colsNumber; ++c) {
                if (!colsCheck[c].isSelected()) {
                    continue;
                }
                data[rowIndex][colIndex] = cellString(r, c);
                colIndex++;
            }
            rowIndex++;
        }
        Map<String, Object> selected = new HashMap<>();
        selected.put("columns", selectedColumns);
        selected.put("data", data);
        return selected;
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

            boolean rowsSelected = false;
            if (rowsCheck != null) {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    if (rowsCheck[j].isSelected()) {
                        rowsSelected = true;
                        break;
                    }
                }
            }
            boolean colsSelected = false;
            if (colsCheck != null) {
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }
            }

            menu = new MenuItem(Languages.message("CopyPageRowsToSystemClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                CopyPageRowsToSystemClipboard();
            });
            menu.setDisable(sheetInputs == null);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedRowsToSystmClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copySelectedRowsToSystemClipboard();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopyPageSelectedColsToSystmClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copyPageSelectedColsToSystemClipboard();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedRowsColsToSystmClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copySelectedRowsColsToSystemClipboard();
            });
            menu.setDisable(!colsSelected || !rowsSelected);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("CopyPageRowsToDataClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copyPageRowsToDataClipboard();
            });
            menu.setDisable(sheetInputs == null);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedRowsToDataClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copySelectedRowsToDataClipboard();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopyPageSelectedColsToDataClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copyPageSelectedColsToDataClipboard();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedRowsColsToDataClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                copySelectedRowsColsToDataClipboard();
            });
            menu.setDisable(!colsSelected || !rowsSelected);
            items.add(menu);

            if (sourceFile != null && pagesNumber > 1) {
                items.add(new SeparatorMenuItem());

                menu = new MenuItem(Languages.message("CopyFileSelectedCols"));
                menu.setOnAction((ActionEvent event) -> {
                    copyFileSelectedCols();
                });
                menu.setDisable(!colsSelected);
                items.add(menu);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public void CopyPageRowsToSystemClipboard() {
        if (pageData == null) {
            popError(message("NoData"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController,
                TextTools.dataText(pageData, displayDelimiter, columnNames(), null));
    }

    public void copySelectedRowsToSystemClipboard() {
        String[][] data = selectedRows();
        if (data == null) {
            popError(message("NoData"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController,
                TextTools.dataText(data, displayDelimiter, columnNames(), null));
    }

    public void copyPageSelectedColsToSystemClipboard() {
        Map<String, Object> selected = pageSelectedCols();
        if (selected == null) {
            popError(message("NoData"));
            return;
        }
        String[][] data = (String[][]) selected.get("data");
        List<ColumnDefinition> selectedColumns = (List<ColumnDefinition>) selected.get("columns");
        List<String> colsNames = new ArrayList<>();
        for (ColumnDefinition c : selectedColumns) {
            colsNames.add(c.getName());
        }
        TextClipboardTools.copyToSystemClipboard(myController,
                TextTools.dataText(data, displayDelimiter, colsNames, null));
    }

    public void copySelectedRowsColsToSystemClipboard() {
        Map<String, Object> selected = selectedRowsCols();
        if (selected == null) {
            popError(message("NoData"));
            return;
        }
        String[][] data = (String[][]) selected.get("data");
        List<ColumnDefinition> selectedColumns = (List<ColumnDefinition>) selected.get("columns");
        List<String> colsNames = new ArrayList<>();
        for (ColumnDefinition c : selectedColumns) {
            colsNames.add(c.getName());
        }
        TextClipboardTools.copyToSystemClipboard(myController,
                TextTools.dataText(data, displayDelimiter, colsNames, null));
    }

    public void copyPageRowsToDataClipboard() {
        if (pageData == null || columns == null) {
            popError(message("NoData"));
            return;
        }
        DataClipboardController.open(pageData, columns);
    }

    public void copySelectedRowsToDataClipboard() {
        String[][] data = selectedRows();
        if (data == null) {
            popError(message("NoData"));
            return;
        }
        DataClipboardController.open(data, columns);
    }

    public void copyPageSelectedColsToDataClipboard() {
        Map<String, Object> selected = pageSelectedCols();
        if (selected == null) {
            popError(message("NoData"));
            return;
        }
        String[][] data = (String[][]) selected.get("data");
        List<ColumnDefinition> selectedColumns = (List<ColumnDefinition>) selected.get("columns");
        DataClipboardController.open(data, selectedColumns);
    }

    public void copyFileSelectedCols() {
        copyPageSelectedColsToDataClipboard();
    }

    public void copySelectedRowsColsToDataClipboard() {
        Map<String, Object> selected = selectedRowsCols();
        if (selected == null) {
            popError(message("NoData"));
            return;
        }
        String[][] data = (String[][]) selected.get("data");
        List<ColumnDefinition> selectedColumns = (List<ColumnDefinition>) selected.get("columns");
        DataClipboardController.open(data, selectedColumns);
    }

}
