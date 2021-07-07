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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlWindow;
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
    @FXML
    protected CheckBox wrapCheck;

    public ControlHtmlCodes() {
        baseTitle = AppVariables.message("Html");
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
            FxmlControl.setTooltip(pasteTxtButton, new Tooltip(message("PasteTexts")));

            wrapCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Wrap", wrapCheck.isSelected());
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
            popup = FxmlWindow.makePopWindow(myController, CommonValues.PopNodesFxml);
            if (popup == null) {
                return;
            }
            Object object = popup.getUserData();
            if (object == null || !(object instanceof PopNodesController)) {
                return;
            }
            PopNodesController controller = (PopNodesController) object;

            controller.addEditPane(codesArea);
            controller.addNode(new Separator());

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
                    TableSizeController controller = (TableSizeController) openStage(CommonValues.TableSizeFxml, true);
                    controller.setValues(myController);
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

            Button tableRow = new Button(message("TableRow"));
            tableRow.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String value = FxmlControl.askValue(baseTitle, "", message("ColumnsNumber"), "3");
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
                            popError(message("InvalidData"));
                        }
                    } catch (Exception e) {
                        popError(message("InvalidData"));
                    }
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

            controller.addFlowPane(aNodes);
            controller.addNode(new Separator());

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

            controller.addFlowPane(headNodes);
            controller.addNode(new Separator());

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

            controller.addFlowPane(listNodes);
            controller.addNode(new Separator());

            List<Node> codeNodes = new ArrayList<>();
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
                    File file = FxmlControl.selectFile(myController, VisitHistory.FileType.All);
                    if (file != null) {
                        insertText(HtmlTools.decodeURL(file, Charset.defaultCharset()));
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

            Button separatorLine = new Button(message("SeparatorLine"));
            separatorLine.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("\n<hr>\n");
                }
            });
            codeNodes.add(separatorLine);

            Button font = new Button(message("Font"));
            font.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<font size=\"3\" color=\"red\">" + message("Font") + "</font>");
                }
            });
            codeNodes.add(font);

            Button bold = new Button(message("Bold"));
            bold.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("<b>" + message("Bold") + "</b>");
                }
            });
            codeNodes.add(bold);

            controller.addFlowPane(codeNodes);
            controller.addNode(new Separator());

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

            controller.addFlowPane(charNodes);
            controller.addNode(new Separator());

            Hyperlink about = new Hyperlink(message("AboutHtml"));
            about.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (AppVariables.isChinese()) {
                        openLink("https://baike.baidu.com/item/html");
                    } else {
                        openLink("https://developer.mozilla.org/en-US/docs/Web/HTML");
                    }
                }
            });
            controller.addNode(about);

            controller.setParameters(myController);
            popup.show(owner, x, y);

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
        String string = Clipboard.getSystemClipboard().getString();
        if (string == null || string.isBlank()) {
            popError(message("NoData"));
            return;
        }
        insertText(string.replaceAll("\n", "<BR>\n") + "<BR>\n");
    }

    @FXML
    @Override
    public void clearAction() {
        codesArea.clear();
    }

    @FXML
    public void editAction() {
        TextEditerController controller = (TextEditerController) FxmlWindow.openStage(CommonValues.TextEditerFxml);
        controller.loadContexts(codesArea.getText());
        controller.toFront();
    }

}
