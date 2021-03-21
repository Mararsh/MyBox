package mara.mybox.controller;

import java.io.File;
import java.net.URLDecoder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-3-5
 * @License Apache License Version 2.0
 */
public class ControlHtmlCodes extends BaseController {

    @FXML
    protected TextArea codesArea;

    public ControlHtmlCodes() {
        baseTitle = AppVariables.message("Html");

    }

    public void setValues(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(String codes) {
        codesArea.setText(codes);
    }

    public String codes() {
        return codesArea.getText();
    }

    protected void insertText(String string) {
        IndexRange range = codesArea.getSelection();
        codesArea.insertText(range.getStart(), string);
        codesArea.requestFocus();
    }

    @FXML
    public void popListMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("NumberedList"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("\n<ol>\n"
                        + "    <li>Item 1\n"
                        + "    </li>\n"
                        + "    <li>Item 2\n"
                        + "    </li>\n"
                        + "    <li>Item 3\n"
                        + "    </li>\n"
                        + "</ol>\n");
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("BulletedList"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("\n<ul>\n"
                        + "    <li>Item 1\n"
                        + "    </li>\n"
                        + "    <li>Item 2\n"
                        + "    </li>\n"
                        + "    <li>Item 3\n"
                        + "    </li>\n"
                        + "</ul>\n");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popHeaderMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            for (int i = 1; i <= 6; i++) {
                String name = message("Headings") + " " + i;
                String value = "<h" + i + ">" + name + "</h" + i + ">\n";
                menu = new MenuItem(name);
                menu.setOnAction((ActionEvent event) -> {
                    insertText(value);
                });
                popMenu.getItems().add(menu);

            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popCodesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Block"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("\n<div>\n"
                        + message("Block")
                        + "\n</div>\n");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Codes"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("\n<PRE><CODE> \n"
                        + message("Codes")
                        + "\n</CODE></PRE>\n");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ReferLocalFile"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxmlControl.selectFile(this);
                if (file == null) {
                    return;
                }
                insertText(URLDecoder.decode(file.toURI().toString()));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Style"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("<style type=\"text/css\">\n"
                        + "    table { max-width:95%; margin : 10px;  border-style: solid; border-width:2px; border-collapse: collapse;}\n"
                        + "    th, td { border-style: solid; border-width:1px; padding: 8px; border-collapse: collapse;}\n"
                        + "    th { font-weight:bold;  text-align:center;}\n"
                        + "</style>\n"
                );
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SeparatorLine"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("\n<hr>\n");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Font"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("<font size=\"3\" color=\"red\">" + message("Font") + "</font>");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Bold"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("<b>" + message("Bold") + "</b>");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void addImage() {
        insertText("<img src=\"https://mararsh.github.io/MyBox/iconGo.png\" alt=\"ReadMe\" />\n");
    }

    @FXML
    public void addlink() {
        insertText("<a href=\"https://github.com/Mararsh/MyBox\">MyBox</a>\n");
    }

    @FXML
    public void addTable() {
        TableSizeController controller = (TableSizeController) openStage(CommonValues.TableSizeFxml, true);
        controller.setValues(this);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                addTable(controller.rowsNumber, controller.colsNumber);
                controller.closeStage();
            }
        });
    }

    public void addTable(int rowsNumber, int colsNumber) {
        String s = "<table>\n    <tr>";
        for (int j = 1; j <= colsNumber; j++) {
            s += "<th> col" + j + " </th>";
        }
        s += "</tr>\n";
        for (int i = 1; i <= rowsNumber; i++) {
            s += "    <tr>";
            for (int j = 1; j <= colsNumber; j++) {
                s += "<td> v" + i + "-" + j + " </td>";
            }
            s += "</tr>\n";
        }
        s += "</table>\n";
        insertText(s);
    }

    @FXML
    public void addP() {
        insertText("\n<p>" + message("Paragraph") + "</p>\n");
    }

    @FXML
    public void addBr() {
        insertText("<br>\n");
    }

    @FXML
    @Override
    public void clearAction() {
        codesArea.clear();
    }

}
