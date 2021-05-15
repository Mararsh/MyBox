package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.HtmlTools;
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
    @FXML
    protected Button pasteTxtButton;

    public ControlHtmlCodes() {
        baseTitle = AppVariables.message("Html");
    }

    public void setValues(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
            FxmlControl.setTooltip(pasteTxtButton, new Tooltip(message("PasteTexts")));
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
                int level = i;
                menu = new MenuItem(name);
                menu.setOnAction((ActionEvent event) -> {
                    String value = FxmlControl.askValue(baseTitle, message("Image"), null,
                            "<H" + level + ">" + name + "</H" + level + ">\n");
                    if (value == null) {
                        return;
                    }
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
                File file = FxmlControl.selectFile(this, VisitHistory.FileType.All);
                if (file == null) {
                    return;
                }
                insertText(HtmlTools.decodeURL(file));
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
                String value = FxmlControl.askValue(baseTitle, message("Font"), null,
                        "<font size=\"3\" color=\"red\">" + message("Font") + "</font>");
                if (value == null) {
                    return;
                }
                insertText(value);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Bold"));
            menu.setOnAction((ActionEvent event) -> {
                String value = FxmlControl.askValue(baseTitle, message("Bold"), null,
                        "<b>" + message("Bold") + "</b>");
                if (value == null) {
                    return;
                }
                insertText(value);
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
    public void popCharMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Blank"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("&nbsp;");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("<");
            menu.setOnAction((ActionEvent event) -> {
                insertText("&lt;");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(">");
            menu.setOnAction((ActionEvent event) -> {
                insertText("&gt;");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("&");
            menu.setOnAction((ActionEvent event) -> {
                insertText("&amp;");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("\"");
            menu.setOnAction((ActionEvent event) -> {
                insertText("&quot;");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Registered"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("&reg;");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Copyright"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("&copy;");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Trademark"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("&trade;");
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
        String value = FxmlControl.askValue(baseTitle, message("Image"), null,
                "<img src=\"https://mararsh.github.io/MyBox/iconGo.png\" alt=\"ReadMe\" />");
        if (value == null) {
            return;
        }
        insertText(value);
    }

    @FXML
    public void addlink() {
        String value = FxmlControl.askValue(baseTitle, message("Link"), null,
                "<a href=\"https://github.com/Mararsh/MyBox\">MyBox</a>");
        if (value == null) {
            return;
        }
        insertText(value);
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
    public void pasteTxt() {
        String string = Clipboard.getSystemClipboard().getString();
        if (string == null || string.isBlank()) {
            popError(message("NoData"));
            return;
        }
        insertText(string.replaceAll("\n", "<BR>\n"));
    }

    @FXML
    @Override
    public void clearAction() {
        codesArea.clear();
    }

    @FXML
    public void editAction() {
        TextEditerController controller = (TextEditerController) FxmlStage.openStage(CommonValues.TextEditerFxml);
        controller.loadContexts(codesArea.getText());
        controller.toFront();
    }

}
