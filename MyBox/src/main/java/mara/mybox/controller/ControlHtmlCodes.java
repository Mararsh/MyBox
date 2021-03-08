package mara.mybox.controller;

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
    public void addImage() {
        insertText("<img src=\"https://mararsh.github.io/MyBox/iconGo.png\" alt=\"ReadMe\" />\n");
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
    public void addlink() {
        insertText("<a href=\"https://github.com/Mararsh/MyBox\">MyBox</a>\n");
    }

    @FXML
    @Override
    public void clearAction() {
        codesArea.clear();
    }

    @FXML
    public void addTable() {
        insertText("\n<style type=\"text/css\">\n"
                + "table { max-width:95%; margin : 10px;  border-style: solid; border-width:2px; border-collapse: collapse;}\n"
                + "th, td { border-style: solid; border-width:1px; padding: 8px; border-collapse: collapse;}\n"
                + "th { font-weight:bold;  text-align:center;}\n"
                + "tr { height: 1.2em;  }\n"
                + "</style>\n"
                + "<table>\n"
                + "    <tr><th> col1 </th><th> col2 </th><th> col3 </th></tr>\n"
                + "    <tr><td> v11 </td><td> v12 </td><td> v13 </td></tr>\n"
                + "    <tr><td> v21 </td><td> v22 </td><td> v23 </td></tr>\n"
                + "    <tr><td> v31 </td><td> v32 </td><td> v33 </td></tr>\n"
                + "</table>\n");
    }

    @FXML
    public void addP() {
        insertText("\n<p>" + message("Paragraph") + "</p>\n");
    }

    @FXML
    public void addBr() {
        insertText("<br>\n");
    }

}
