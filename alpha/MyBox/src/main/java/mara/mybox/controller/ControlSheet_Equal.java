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
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Equal extends ControlSheet_Size {

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

            MenuItem menu = new MenuItem(Languages.message("SetPageAll"));
            menu.setOnAction((ActionEvent event) -> {
                setPageAllValues();
            });
            menu.setDisable(sheetInputs == null);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetSelectedRowsValues"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedRows();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetPageSelectedColsValues"));
            menu.setOnAction((ActionEvent event) -> {
                setPageSelectedCols();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            if (sourceFile != null && pagesNumber > 1) {
                menu = new MenuItem(Languages.message("SetFileSelectedColsValues"));
                menu.setOnAction((ActionEvent event) -> {
                    setFileSelectedCols();
                });
                menu.setDisable(!colsSelected || dataChangedNotify.get());
                items.add(menu);
            }

            menu = new MenuItem(Languages.message("SetSelectedRowsColsValues"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedRowsCols();
            });
            menu.setDisable(!colsSelected || !rowsSelected);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> equalMoreMenu() {
        return null;
    }

    public void setPageAllValues() {
        if (sheetInputs == null) {
            return;
        }
        String value = askValue(Languages.message("All"), Languages.message("SetValue"), defaultColumnType == ColumnDefinition.ColumnType.String ? "v" : "0");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < sheetInputs.length; ++j) {
            for (int i = 0; i < sheetInputs[0].length; ++i) {
                sheetInputs[j][i].setText(value);
            }
        }
        isSettingValues = false;
        sheetChanged();
    }

    public void setSelectedRows() {
        if (sheetInputs == null || rowsCheck == null) {
            return;
        }
        String value = askValue(Languages.message("SelectedRows"), Languages.message("SetValue"), "");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < sheetInputs.length; ++j) {
            if (rowsCheck[j].isSelected()) {
                for (int i = 0; i < sheetInputs[0].length; ++i) {
                    sheetInputs[j][i].setText(value);
                }
            }
        }
        isSettingValues = false;
        sheetChanged();

    }

    public void setPageSelectedCols() {
        if (sheetInputs == null || colsCheck == null) {
            return;
        }
        String value = askValue(Languages.message("SelectedCols"), Languages.message("SetValue"), "");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int i = 0; i < sheetInputs[0].length; ++i) {
            if (colsCheck[i].isSelected()) {
                for (int j = 0; j < sheetInputs.length; ++j) {
                    sheetInputs[j][i].setText(value);
                }
            }
        }
        isSettingValues = false;
        sheetChanged();
    }

    public void setSelectedRowsCols() {
        if (sheetInputs == null || rowsCheck == null || colsCheck == null) {
            return;
        }
        String value = askValue(Languages.message("SetSelectedRowsColsValues"), Languages.message("SetValue"), "");
        if (value == null) {
            return;
        }
        isSettingValues = true;
        for (int j = 0; j < sheetInputs.length; ++j) {
            if (rowsCheck[j].isSelected()) {
                for (int i = 0; i < sheetInputs[0].length; ++i) {
                    if (colsCheck[i].isSelected()) {
                        sheetInputs[j][i].setText(value);
                    }
                }
            }
        }
        isSettingValues = false;
        sheetChanged();
    }

    protected void setFileSelectedCols() {
        setPageSelectedCols();
    }

}
