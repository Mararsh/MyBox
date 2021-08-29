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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-19
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Size extends ControlSheet_Sheet {

    @FXML
    public void sheetSizeMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeSheetSizeMenu();
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

    public List<MenuItem> makeSheetSizeMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(Languages.message("EnlargerAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    double width = colsCheck[i].getWidth() + widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (sheetInputs != null) {
                        for (int j = 0; j < sheetInputs.length; ++j) {
                            sheetInputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDefintionPane();
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("ReduceAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (colsCheck[i].getWidth() < widthChange * 1.5) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() - widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (sheetInputs != null) {
                        for (int j = 0; j < sheetInputs.length; ++j) {
                            sheetInputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDefintionPane();
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetAllColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", Languages.message("SetAllColsWidth"), (int) (colsCheck[0].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    for (int i = 0; i < colsCheck.length; ++i) {
                        colsCheck[i].setPrefWidth(width);
                        if (sheetInputs != null) {
                            for (int j = 0; j < sheetInputs.length; ++j) {
                                sheetInputs[j][i].setPrefWidth(width);
                            }
                        }
                    }
                    makeDefintionPane();
                } catch (Exception e) {
                    popError(Languages.message("InvalidData"));
                }
            });
            menu.setDisable(colsCheck == null || colsCheck.length == 0);
            items.add(menu);

            boolean colsSelected = false;
            if (colsCheck != null) {
                for (int j = 0; j < colsCheck.length; ++j) {
                    if (colsCheck[j].isSelected()) {
                        colsSelected = true;
                        break;
                    }
                }
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("EnlargerSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() + widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (sheetInputs != null) {
                        for (int j = 0; j < sheetInputs.length; ++j) {
                            sheetInputs[j][i].setPrefWidth(width);
                        }
                    }
                }
                makeDefintionPane();
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("ReduceSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                for (int i = 0; i < colsCheck.length; ++i) {
                    if (!colsCheck[i].isSelected()) {
                        continue;
                    }
                    if (colsCheck[i].getWidth() < widthChange * 1.5) {
                        continue;
                    }
                    double width = colsCheck[i].getWidth() - widthChange;
                    colsCheck[i].setPrefWidth(width);
                    if (sheetInputs != null) {
                        for (int j = 0; j < sheetInputs.length; ++j) {
                            sheetInputs[j][i].setPrefWidth(width);
                        }
                    }
                    makeDefintionPane();
                }
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            menu = new MenuItem(Languages.message("SetSelectedColsWidth"));
            menu.setOnAction((ActionEvent event) -> {
                String value = askValue("", Languages.message("SetSelectedColsWidth"), (int) (colsCheck[0].getWidth()) + "");
                if (value == null) {
                    return;
                }
                try {
                    double width = Double.parseDouble(value);
                    for (int i = 0; i < colsCheck.length; ++i) {
                        if (!colsCheck[i].isSelected()) {
                            continue;
                        }
                        colsCheck[i].setPrefWidth(width);
                        if (sheetInputs != null) {
                            for (int j = 0; j < sheetInputs.length; ++j) {
                                sheetInputs[j][i].setPrefWidth(width);
                            }
                        }
                    }
                    makeDefintionPane();
                } catch (Exception e) {
                    popError(Languages.message("InvalidData"));
                }
            });
            menu.setDisable(!colsSelected);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("AddRowsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                addRowsNumber();
            });
            menu.setDisable(colsCheck == null || colsCheck.length < 1);
            items.add(menu);

            menu = new MenuItem(Languages.message("AddColsNumber"));
            menu.setOnAction((ActionEvent event) -> {
                addColsNumber();
            });
            items.add(menu);

            List<MenuItem> more = sheetSizeMoreMenu();
            if (more != null) {
                items.addAll(more);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    public List<MenuItem> sheetSizeMoreMenu() {
        return null;
    }

    protected void addRowsNumber() {
        if (colsCheck == null || colsCheck.length == 0) {
            return;
        }
        String value = askValue("", Languages.message("AddRowsNumber"), "1");
        if (value == null) {
            return;
        }
        try {
            int number = Integer.parseInt(value);
            if (sheetInputs == null || sheetInputs.length == 0) {
                resizeSheet(number, colsCheck.length);
            } else {
                resizeSheet(sheetInputs.length + number, colsCheck.length);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    protected void addColsNumber() {
        String value = askValue("", Languages.message("AddColsNumber"), "1");
        if (value == null) {
            return;
        }
        try {
            int number = Integer.parseInt(value);
            if (colsCheck == null || colsCheck.length == 0) {
                insertPageCol(0, true, number);
            } else {
                insertPageCol(colsCheck.length - 1, false, number);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    // Notice: this does not concern columns names
    public void resizeSheet(int rowsNumber, int colsNumber) {
        if (rowsNumber <= 0 || colsNumber <= 0) {
            makeSheet(null);
            return;
        }
        String[][] values = new String[rowsNumber][colsNumber];
        if (sheetInputs != null && sheetInputs.length > 0) {
            int drow = Math.min(sheetInputs.length, rowsNumber);
            int dcol = Math.min(sheetInputs[0].length, colsNumber);
            for (int j = 0; j < drow; ++j) {
                for (int i = 0; i < dcol; ++i) {
                    values[j][i] = cellString(j, i);
                }
            }
        }
        makeSheet(values);
    }

    protected void insertPageCol(int col, boolean left, int number) {
        if (number < 1) {
            return;
        }
        if (columns == null) {
            columns = new ArrayList<>();
        }
        int base = col + (left ? 0 : 1);
        makeColumns(base, number);
        String[][] current = pickData();
        if (current == null) {
            makeSheet(null);
        } else {
            int rNumber = current.length;
            int cNumber = current[0].length + number;
            String[][] values = new String[rNumber][cNumber];
            for (int j = 0; j < rNumber; ++j) {
                for (int i = 0; i < base; ++i) {
                    values[j][i] = current[j][i];
                }
                for (int i = base + number; i < cNumber; ++i) {
                    values[j][i] = current[j][i - 1];
                }
                for (int i = base; i < base + number; ++i) {
                    values[j][i] = defaultColValue;
                }
            }
            makeSheet(values);
        }
    }

}
