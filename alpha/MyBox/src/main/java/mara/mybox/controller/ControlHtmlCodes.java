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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    @FXML
    protected CheckBox wrapCheck;

    public ControlHtmlCodes() {
        baseTitle = Languages.message("Html");
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(pasteTxtButton, new Tooltip(Languages.message("PasteTexts")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                UserConfig.setBoolean(baseName + "Wrap", wrapCheck.isSelected());
                codesArea.setWrapText(wrapCheck.isSelected());
            });
            codesArea.setWrapText(wrapCheck.isSelected());
            codesArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    popCodesMenu((Node) event.getSource(), event.getScreenX() + 40, event.getScreenY() + 40);
                }
            });

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
    public void popCodesMenu(MouseEvent event) {
        popCodesMenu((Node) event.getSource(), event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public void popCodesMenu(Node owner, double x, double y) {
        try {
            MenuTextEditController controller = MenuTextEditController.open(myController, codesArea, x, y);
            controller.setWidth(500);

            controller.addNode(new Separator());

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
                            controller.closeStage();
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

            controller.addFlowPane(aNodes);
            controller.addNode(new Separator());

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

            controller.addFlowPane(headNodes);
            controller.addNode(new Separator());

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

            controller.addFlowPane(listNodes);
            controller.addNode(new Separator());

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

            controller.addFlowPane(codeNodes);
            controller.addNode(new Separator());

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

            controller.addFlowPane(charNodes);
            controller.addNode(new Separator());

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
            controller.addNode(about);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
    public void pasteTxt() {
        String string = TextClipboardTools.getSystemClipboardString();
        if (string == null || string.isBlank()) {
            popError(Languages.message("NoData"));
            return;
        }
        string = HtmlWriteTools.stringToHtml(string);
        insertText(string);
    }

    @FXML
    @Override
    public void clearAction() {
        codesArea.clear();
    }

    @FXML
    public void editAction() {
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(codesArea.getText());
        controller.toFront();
    }

}
