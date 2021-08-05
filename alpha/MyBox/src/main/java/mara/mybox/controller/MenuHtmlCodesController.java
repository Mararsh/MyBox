package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Separator;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-4
 * @License Apache License Version 2.0
 */
public class MenuHtmlCodesController extends MenuTextEditController {

    public MenuHtmlCodesController() {
        baseTitle = "HtmlCodes";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            super.setParameters(parent, node, x, y);
            if (textInput != null && textInput.isEditable()) {
                addHtmlButtons();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addHtmlButtons() {
        try {
            if (textInput == null) {
                return;
            }
            addNode(new Separator());

            List<Node> aNodes = new ArrayList<>();

            Button br = new Button(Languages.message("BreakLine"));
            br.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<br>\n");
                }
            });
            aNodes.add(br);

            Button p = new Button(Languages.message("Paragraph"));
            p.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<p>" + Languages.message("Paragraph") + "</p>\n");
                }
            });
            aNodes.add(p);

            Button table = new Button(Languages.message("Table"));
            table.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TableSizeController controller = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
                    controller.setParameters(parentController);
                    controller.notify.addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            addTable(controller.rowsNumber, controller.colsNumber);
                            closeStage();
                        }
                    });
                }
            });
            aNodes.add(table);

            Button tableRow = new Button(Languages.message("TableRow"));
            tableRow.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String value = PopTools.askValue(baseTitle, "", Languages.message("ColumnsNumber"), "3");
                    if (value == null) {
                        return;
                    }
                    try {
                        int colsNumber = Integer.valueOf(value);
                        if (colsNumber > 0) {
                            String s = "    <tr>";
                            for (int j = 1; j <= colsNumber; j++) {
                                s += "<td> v" + j + " </td>";
                            }
                            s += "</tr>\n";
                            insertText(s);
                        } else {
                            popError(Languages.message("InvalidData"));
                        }
                    } catch (Exception e) {
                        popError(Languages.message("InvalidData"));
                    }
                }
            });
            aNodes.add(tableRow);

            Button image = new Button(Languages.message("Image"));
            image.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<img src=\"https://mararsh.github.io/MyBox/iconGo.png\" alt=\"ReadMe\" />");
                }
            });
            aNodes.add(image);

            Button link = new Button(Languages.message("Link"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<a href=\"https://github.com/Mararsh/MyBox\">MyBox</a>");
                }
            });
            aNodes.add(link);

            addFlowPane(aNodes);

            List<Node> headNodes = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                String name = Languages.message("Headings") + " " + i;
                int level = i;
                Button head = new Button(name);
                head.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        insertText("<H" + level + ">" + name + "</H" + level + ">\n");
                    }
                });
                headNodes.add(head);
            }

            addFlowPane(headNodes);

            List<Node> listNodes = new ArrayList<>();
            Button numberedList = new Button(Languages.message("NumberedList"));
            numberedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<ol>\n"
                            + "    <li>Item 1\n"
                            + "    </li>\n"
                            + "    <li>Item 2\n"
                            + "    </li>\n"
                            + "    <li>Item 3\n"
                            + "    </li>\n"
                            + "</ol>\n");
                }
            });
            listNodes.add(numberedList);

            Button bulletedList = new Button(Languages.message("BulletedList"));
            bulletedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<ul>\n"
                            + "    <li>Item 1\n"
                            + "    </li>\n"
                            + "    <li>Item 2\n"
                            + "    </li>\n"
                            + "    <li>Item 3\n"
                            + "    </li>\n"
                            + "</ul>\n");
                }
            });
            listNodes.add(bulletedList);

            addFlowPane(listNodes);

            List<Node> codeNodes = new ArrayList<>();
            Button block = new Button(Languages.message("Block"));
            block.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<div>\n" + Languages.message("Block") + "\n</div>\n");
                }
            });
            codeNodes.add(block);

            Button codes = new Button(Languages.message("Codes"));
            codes.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<PRE><CODE> \n" + Languages.message("Codes") + "\n</CODE></PRE>\n");
                }
            });
            codeNodes.add(codes);

            Button local = new Button(Languages.message("ReferLocalFile"));
            local.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    File file = FxFileTools.selectFile(myController, VisitHistory.FileType.All);
                    if (file != null) {
                        insertText(UrlTools.decodeURL(file, Charset.defaultCharset()));
                    }
                }
            });
            codeNodes.add(local);

            Button style = new Button(Languages.message("Style"));
            style.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<style type=\"text/css\">\n"
                            + "    table { max-width:95%; margin : 10px;  border-style: solid; border-width:2px; border-collapse: collapse;}\n"
                            + "    th, td { border-style: solid; border-width:1px; padding: 8px; border-collapse: collapse;}\n"
                            + "    th { font-weight:bold;  text-align:center;}\n"
                            + "</style>\n"
                    );
                }
            });
            codeNodes.add(style);

            Button separatorLine = new Button(Languages.message("SeparateLine"));
            separatorLine.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<hr>\n");
                }
            });
            codeNodes.add(separatorLine);

            Button font = new Button(Languages.message("Font"));
            font.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<font size=\"3\" color=\"red\">" + Languages.message("Font") + "</font>");
                }
            });
            codeNodes.add(font);

            Button bold = new Button(Languages.message("Bold"));
            bold.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<b>" + Languages.message("Bold") + "</b>");
                }
            });
            codeNodes.add(bold);

            addFlowPane(codeNodes);

            List<Node> charNodes = new ArrayList<>();

            Button blank = new Button(Languages.message("Blank"));
            blank.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&nbsp;");
                }
            });
            charNodes.add(blank);

            Button lt = new Button(Languages.message("<"));
            lt.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&lt;");
                }
            });
            charNodes.add(lt);

            Button gt = new Button(Languages.message(">"));
            gt.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&gt;");
                }
            });
            charNodes.add(gt);

            Button and = new Button(Languages.message("&"));
            and.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&amp;");
                }
            });
            charNodes.add(and);

            Button quot = new Button(Languages.message("\""));
            quot.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&quot;");
                }
            });
            charNodes.add(quot);

            Button registered = new Button(Languages.message("Registered"));
            registered.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&reg;");
                }
            });
            charNodes.add(registered);

            Button copyright = new Button(Languages.message("Copyright"));
            copyright.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&copy;");
                }
            });
            charNodes.add(copyright);

            Button trademark = new Button(Languages.message("Trademark"));
            trademark.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&trade;");
                }
            });
            charNodes.add(trademark);

            addFlowPane(charNodes);

            Hyperlink about = new Hyperlink(Languages.message("AboutHtml"));
            about.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (Languages.isChinese()) {
                        openLink("https://baike.baidu.com/item/html");
                    } else {
                        openLink("https://developer.mozilla.org/en-US/docs/Web/HTML");
                    }
                }
            });
            addNode(about);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void insertText(String string) {
        IndexRange range = textInput.getSelection();
        textInput.insertText(range.getStart(), string);
        textInput.requestFocus();
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
    @Override
    public void editAction() {
        HtmlEditorController controller = (HtmlEditorController) openStage(Fxmls.HtmlEditorFxml);
        controller.loadContents(textInput.getText());
    }

    @FXML
    @Override
    public void popAction() {
        if (textInput == null) {
            return;
        }
        HtmlEditorController controller = (HtmlEditorController) openStage(Fxmls.HtmlEditorFxml);
        controller.setAsPopup(baseName + "Pop");
        controller.loadContents(textInput.getText());
    }

    /*
        static methods
     */
    public static MenuHtmlCodesController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(parent, Fxmls.MenuHtmlCodesFxml, node, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuHtmlCodesController)) {
                return null;
            }
            MenuHtmlCodesController controller = (MenuHtmlCodesController) object;
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MenuHtmlCodesController open(BaseController parent, Node node, MouseEvent event) {
        return open(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public static MenuHtmlCodesController open(BaseController parent, Node node, ContextMenuEvent event) {
        return open(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }

}
