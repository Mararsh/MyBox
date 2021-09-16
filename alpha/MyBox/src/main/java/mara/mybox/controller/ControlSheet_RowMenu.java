package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
public abstract class ControlSheet_RowMenu extends ControlSheet_Operations {

    @Override
    public void popRowLabelMenu(Label label) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.setStyle("-fx-font-weight: normal;");

            MenuItem menu = new MenuItem(Languages.message("EnlargerColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = label.getWidth() + widthChange;
                label.setPrefWidth(width);
                if (rowsCheck == null) {
                    return;
                }
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setPrefWidth(width);
                }
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("ReduceColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                double width = label.getWidth() - widthChange;
                label.setPrefWidth(width);
                if (rowsCheck == null) {
                    return;
                }
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setPrefWidth(width);
                }
            });
            menu.setDisable(label.getWidth() <= widthChange * 1.5);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SetColWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", Languages.message("SetColWidth"), (int) (label.getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    label.setPrefWidth(width);
                    if (rowsCheck == null) {
                        return;
                    }
                    for (int j = 0; j < rowsCheck.length; ++j) {
                        rowsCheck[j].setPrefWidth(width);
                    }
                } catch (Exception e) {
                    popError(Languages.message("InvalidData"));
                }
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SelectAllCols"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    colsCheck[i].setSelected(true);
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SelectNoCol"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    colsCheck[i].setSelected(false);
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SelectAllRows"));
            menu.setOnAction((ActionEvent event) -> {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setSelected(true);
                }
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SelectNoRow"));
            menu.setOnAction((ActionEvent event) -> {
                for (int j = 0; j < rowsCheck.length; ++j) {
                    rowsCheck[j].setSelected(false);
                }
            });
            menu.setDisable(rowsCheck == null || rowsCheck.length == 0);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("DeleteAllCols"));
            menu.setOnAction((ActionEvent event) -> {
                if (!PopTools.askSure(Languages.message("DeleteAllCols"), Languages.message("SureDeleteAll"))) {
                    return;
                }
                deleteAllCols();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("DeleteAllRows"));
            menu.setOnAction((ActionEvent event) -> {
                if (!PopTools.askSure(Languages.message("DeleteAllRows"), Languages.message("SureDeleteAll"))) {
                    return;
                }
                deleteAllRows();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("PopupClose"));
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

            menu = new MenuItem(Languages.message("InsertRowAbove"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(row, true, 1);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("InsertRowBelow"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(row, false, 1);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("DeleteRow"));
            menu.setOnAction((ActionEvent event) -> {
                List<Integer> rows = new ArrayList<>();
                rows.add(row);
                deleteRows(rows);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SetValues") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataEqualController controller = (DataEqualController) openChildStage(Fxmls.DataEqualFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("CopyToSystemClipboard") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataCopyToSystemClipboardController controller = (DataCopyToSystemClipboardController) openChildStage(Fxmls.DataCopyToSystemClipboardFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
            });
            items.add(menu);

            menu = new MenuItem(message("CopyToMyBoxClipboard") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataCopyToMyBoxClipboardController controller = (DataCopyToMyBoxClipboardController) openChildStage(Fxmls.DataCopyToMyBoxClipboardFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
            });
            items.add(menu);

            menu = new MenuItem(message("Paste") + "...");
            menu.setOnAction((ActionEvent event) -> {
                myBoxClipBoard();
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("Add") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataRowsAddController controller = (DataRowsAddController) openChildStage(Fxmls.DataRowsAddFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("Delete") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataRowsDeleteController controller = (DataRowsDeleteController) openChildStage(Fxmls.DataRowsDeleteFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

}
