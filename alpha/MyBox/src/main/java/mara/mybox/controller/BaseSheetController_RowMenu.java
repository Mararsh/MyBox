package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController_RowMenu extends BaseSheetController_Input {

    // row: 0-based
    @Override
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

    protected abstract void deletePageAllRows();

}
