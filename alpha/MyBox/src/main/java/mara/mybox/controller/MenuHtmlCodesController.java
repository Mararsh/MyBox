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
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

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

            Button br = new Button(message("BreakLine"));
            br.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<br>\n");
                }
            });
            aNodes.add(br);

            Button p = new Button(message("Paragraph"));
            p.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<p>" + message("Paragraph") + "</p>\n");
                }
            });
            aNodes.add(p);

            Button table = new Button(message("Table"));
            table.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TableSizeController controller = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
                    controller.setParameters(parentController, message("Table"));
                    controller.notify.addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            addTable(controller.rowsNumber, controller.colsNumber, true);
                            controller.closeStage();
                        }
                    });
                }
            });
            aNodes.add(table);

            Button tableRow = new Button(message("TableRow"));
            tableRow.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TableSizeController controller = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
                    controller.setParameters(parentController, message("TableRow"));
                    controller.notify.addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            addTable(controller.rowsNumber, controller.colsNumber, false);
                            controller.closeStage();
                        }
                    });
                }
            });
            aNodes.add(tableRow);

            Button image = new Button(message("Image"));
            image.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<img src=\"https://mararsh.github.io/MyBox/iconGo.png\" alt=\"ReadMe\" />");
                }
            });
            aNodes.add(image);

            Button link = new Button(message("Link"));
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
                String name = message("Headings") + " " + i;
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
            Button numberedList = new Button(message("NumberedList"));
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

            Button bulletedList = new Button(message("BulletedList"));
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

            Button separatorLine = new Button(message("SeparateLine"));
            separatorLine.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<hr>\n");
                }
            });
            codeNodes.add(separatorLine);

            Button block = new Button(message("Block"));
            block.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<div>\n" + message("Block") + "\n</div>\n");
                }
            });
            codeNodes.add(block);

            Button codes = new Button(message("Codes"));
            codes.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<PRE><CODE> \n" + message("Codes") + "\n</CODE></PRE>\n");
                }
            });
            codeNodes.add(codes);

            Button local = new Button(message("ReferLocalFile"));
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

            Button style = new Button(message("Style"));
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

            Button text = new Button(message("Texts"));
            text.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String string = TextClipboardTools.getSystemClipboardString();
                    if (string == null || string.isBlank()) {
                        popError(Languages.message("NoData"));
                        return;
                    }
                    insertText(HtmlWriteTools.stringToHtml(string));
                }
            });
            NodeStyleTools.setTooltip(text, new Tooltip(message("PasteTextAsHtml")));
            codeNodes.add(text);

            addFlowPane(codeNodes);

            List<Node> othersNodes = new ArrayList<>();

            Button font = new Button(message("Font"));
            font.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<font size=\"3\" color=\"red\">" + message("Font") + "</font>");
                }
            });
            othersNodes.add(font);

            Button bold = new Button(message("Bold"));
            bold.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<b>" + message("Bold") + "</b>");
                }
            });
            othersNodes.add(bold);

            addFlowPane(othersNodes);

            List<Node> charNodes = new ArrayList<>();

            Button blank = new Button(message("Blank"));
            blank.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&nbsp;");
                }
            });
            charNodes.add(blank);

            Button lt = new Button(message("<"));
            lt.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&lt;");
                }
            });
            charNodes.add(lt);

            Button gt = new Button(message(">"));
            gt.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&gt;");
                }
            });
            charNodes.add(gt);

            Button and = new Button(message("&"));
            and.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&amp;");
                }
            });
            charNodes.add(and);

            Button quot = new Button(message("\""));
            quot.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&quot;");
                }
            });
            charNodes.add(quot);

            Button registered = new Button(message("Registered"));
            registered.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&reg;");
                }
            });
            charNodes.add(registered);

            Button copyright = new Button(message("Copyright"));
            copyright.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&copy;");
                }
            });
            charNodes.add(copyright);

            Button trademark = new Button(message("Trademark"));
            trademark.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("&trade;");
                }
            });
            charNodes.add(trademark);

            addFlowPane(charNodes);

            Hyperlink about = new Hyperlink(message("AboutHtml"));
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
        if (string == null) {
            return;
        }
        IndexRange range = textInput.getSelection();
        textInput.insertText(range.getStart(), string);
        parentController.getMyWindow().requestFocus();
        textInput.requestFocus();
    }

    public void addTable(int rowsNumber, int colsNumber, boolean withHeader) {
        String s = "\n";
        if (withHeader) {
            s += "<table>\n    <tr>";
            for (int j = 1; j <= colsNumber; j++) {
                s += "<th> col" + j + " </th>";
            }
            s += "</tr>\n";
        }
        for (int i = 1; i <= rowsNumber; i++) {
            s += "    <tr>";
            for (int j = 1; j <= colsNumber; j++) {
                s += "<td> v" + i + "-" + j + " </td>";
            }
            s += "</tr>\n";
        }
        if (withHeader) {
            s += "</table>\n";
        }
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
    public boolean popAction() {
        if (textInput == null) {
            return false;
        }
        HtmlCodesPopController.openInput(parentController, textInput);
        return true;
    }

    /*
        static methods
     */
    public static MenuHtmlCodesController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof MenuHtmlCodesController) {
                    try {
                        MenuHtmlCodesController controller = (MenuHtmlCodesController) object;
                        if (controller.textInput != null && controller.textInput.equals(node)) {
                            controller.close();
                        }
                    } catch (Exception e) {
                    }
                }
            }
            MenuHtmlCodesController controller = (MenuHtmlCodesController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.MenuHtmlCodesFxml, false);
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
