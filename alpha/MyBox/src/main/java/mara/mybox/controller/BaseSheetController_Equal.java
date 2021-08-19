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
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController_Equal extends BaseSheetController_Delete {

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
        String value = askValue(Languages.message("All"), Languages.message("SetValue"), defaultColumnType == ColumnDefinition.ColumnType.String ? "v" : "0");
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

}
