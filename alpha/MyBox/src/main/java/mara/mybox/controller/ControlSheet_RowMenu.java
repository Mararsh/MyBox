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
public abstract class ControlSheet_RowMenu extends ControlSheet_Paste {

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

            menu = new MenuItem(Languages.message("Size") + "...");
            menu.setOnAction((ActionEvent event) -> {
                sizeDataAction();
            });
            menu.setDisable(colsCheck == null || colsCheck.length < 1);
            popMenu.getItems().add(menu);

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

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Copy") + "...");
            menu.setOnAction((ActionEvent event) -> {
                DataCopyController controller = (DataCopyController) openChildStage(Fxmls.DataCopyFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
            });
            items.add(menu);

//
//            menu = new MenuItem(Languages.message("Paste"));
//            menu.setOnAction((ActionEvent event) -> {
//                isSettingValues = true;
//                for (int i = 0; i < Math.min(colsCheck.length, copiedRow.size()); ++i) {
//                    inputs[row][i].setText(copiedRow.get(i));
//                }
//                isSettingValues = false;
//                sheetChanged();
//            });
//            menu.setDisable(copiedRow == null || copiedRow.isEmpty());
//            items.add(menu);
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
                DataDeleteController controller = (DataDeleteController) openChildStage(Fxmls.DataDeleteFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SetValues"));
            menu.setOnAction((ActionEvent event) -> {
                DataEqualController controller = (DataEqualController) openChildStage(Fxmls.DataEqualFxml, false);
                controller.setParameters((ControlSheet) this, row, -1);
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

}
