package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController_Copy extends BaseSheetController_ColMenu {

    @FXML
    @Override
    public void copyText() {
        if (sheetDisplayController == this) {
            super.copyText();
        } else {
            sheetDisplayController.copyText();
        }
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

            menu = new MenuItem(Languages.message("CopyAll"));
            menu.setOnAction((ActionEvent event) -> {
                copyText();
            });
            menu.setDisable(inputs == null);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("CopySelectedRows"));
            menu.setOnAction((ActionEvent event) -> {
                if (!rowsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                copySelectedRows();
            });
            menu.setDisable(!rowsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                copySelectedCols();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("CopySelectedRowsCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!colsSelected || !rowsSelected) {
                    popError(Languages.message("NoData"));
                    return;
                }
                copySelectedRowsCols();
            });
            menu.setDisable(!colsSelected || !rowsSelected);
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public void copySelectedRows() {
        if (inputs == null || rowsCheck == null) {
            return;
        }
        String s = null;
        String p = TextTools.delimiterText(sheetDisplayController.textDelimiter);
        rowsNumber = inputs.length;
        colsNumber = inputs[0].length;
        int lines = 0;
        for (int j = 0; j < rowsNumber; ++j) {
            if (!rowsCheck[j].isSelected()) {
                continue;
            }
            String row = null;
            for (int i = 0; i < colsNumber; ++i) {
                if (row == null) {
                    row = value(j, i);
                } else {
                    row += p + value(j, i);
                }
            }
            if (s == null) {
                s = row;
            } else {
                s += "\n" + row;
            }
            lines++;
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
    }

    public void copySelectedCols() {
        if (inputs == null || colsCheck == null) {
            return;
        }
        int lines = 0, cols = 0;
        for (CheckBox c : colsCheck) {
            if (c.isSelected()) {
                cols++;
            }
        }
        if (cols < 1) {
            popError(Languages.message("NoData"));
            return;
        }
        String s = null;
        String p = TextTools.delimiterText(sheetDisplayController.textDelimiter);
        rowsNumber = inputs.length;
        colsNumber = inputs[0].length;
        for (int j = 0; j < rowsNumber; ++j) {
            String row = null;
            for (int i = 0; i < colsNumber; ++i) {
                if (!colsCheck[i].isSelected()) {
                    continue;
                }
                if (row == null) {
                    row = value(j, i);
                } else {
                    row += p + value(j, i);
                }
            }
            if (row == null) {
                break;
            }
            if (s == null) {
                s = row;
            } else {
                s += "\n" + row;
            }
            lines++;
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
    }

    public void copySelectedRowsCols() {
        if (inputs == null || colsCheck == null) {
            return;
        }
        int lines = 0, cols = 0;
        for (CheckBox c : colsCheck) {
            if (c.isSelected()) {
                cols++;
            }
        }
        if (cols < 1) {
            popError(Languages.message("NoData"));
            return;
        }
        String s = null;
        String p = TextTools.delimiterText(sheetDisplayController.textDelimiter);
        rowsNumber = inputs.length;
        colsNumber = inputs[0].length;
        for (int j = 0; j < rowsNumber; ++j) {
            if (!rowsCheck[j].isSelected()) {
                continue;
            }
            String row = null;
            for (int i = 0; i < colsNumber; ++i) {
                if (!colsCheck[i].isSelected()) {
                    continue;
                }
                if (row == null) {
                    row = value(j, i);
                } else {
                    row += p + value(j, i);
                }
            }
            if (row == null) {
                break;
            }
            if (s == null) {
                s = row;
            } else {
                s += "\n" + row;
            }
            lines++;
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
    }

}
