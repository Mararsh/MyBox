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
 * @CreateDate 2021-7-24
 * @License Apache License Version 2.0
 */
public class MenuMarkdownEditController extends MenuTextEditController {

    public MenuMarkdownEditController() {
        baseTitle = "Markdown";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Markdown);
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            super.setParameters(parent, node, x, y);
            if (textInput != null && textInput.isEditable()) {
                addMarkdownButtons();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addMarkdownButtons() {
        try {
            if (textInput == null) {
                return;
            }
            addNode(new Separator());

            List<javafx.scene.Node> aNodes = new ArrayList<>();

            Button blank4 = new Button(Languages.message("Blank4"));
            blank4.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("    ");
                }
            });
            aNodes.add(blank4);

            Button br = new Button(Languages.message("BreakLine"));
            br.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("    \n");
                }
            });
            aNodes.add(br);

            Button p = new Button(Languages.message("Paragraph"));
            p.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("    \n" + Languages.message("Paragraph") + "    \n");
                }
            });
            aNodes.add(p);

            Button table = new Button(Languages.message("Table"));
            table.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TableSizeController sizeController = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
                    sizeController.setParameters(parentController);
                    sizeController.notify.addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            String s = "\n|";
                            for (int j = 1; j <= sizeController.colsNumber; j++) {
                                s += " col" + j + " |";
                            }
                            s += "    \n|";
                            for (int j = 1; j <= sizeController.colsNumber; j++) {
                                s += " --- |";
                            }
                            s += "    \n";
                            for (int i = 1; i <= sizeController.rowsNumber; i++) {
                                s += "|";
                                for (int j = 1; j <= sizeController.colsNumber; j++) {
                                    s += " v" + i + "-" + j + " |";
                                }
                                s += "    \n";
                            }
                            insertText(s);
                            sizeController.closeStage();
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
                            String s = "| ";
                            for (int j = 1; j <= colsNumber; j++) {
                                s += " v" + j + " |";
                            }
                            s += "\n";
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
                    insertText("![" + Languages.message("Name") + "](http://" + Languages.message("Address") + ")");
                }
            });
            aNodes.add(image);

            Button link = new Button(Languages.message("Link"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("[" + Languages.message("Name") + "](http://" + Languages.message("Address") + ")");
                }
            });
            aNodes.add(link);

            addFlowPane(aNodes);

            List<javafx.scene.Node> headNodes = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                String name = Languages.message("Headings") + " " + i;
                String value = "";
                for (int h = 0; h < i; h++) {
                    value += "#";
                }
                String h = value + " ";
                Button head = new Button(name);
                head.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        addTextInFrontOfCurrentLine(h);
                    }
                });
                headNodes.add(head);
            }

            addFlowPane(headNodes);

            List<javafx.scene.Node> listNodes = new ArrayList<>();
            Button numberedList = new Button(Languages.message("NumberedList"));
            numberedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    IndexRange range = textInput.getSelection();
                    int start = range.getStart();
                    int end = range.getEnd();
                    addTextInFrontOfCurrentLine("1. ");
                    if (start == end) {
                        return;
                    }
                    start += 3;
                    end += 3;
                    int pos;
                    int count = 1;
                    while (true) {
                        pos = textInput.getText(start, end).indexOf('\n');
                        if (pos < 0) {
                            break;
                        }
                        count++;
                        textInput.insertText(start + pos + 1, count + ". ");
                        int nlen = 2 + (count + "").length();
                        start += pos + 1 + nlen;
                        end += nlen;
                        int len = textInput.getLength();
                        if (start >= end || start >= len || end >= len) {
                            break;
                        }
                    }
                    textInput.requestFocus();
                }
            });
            listNodes.add(numberedList);

            Button bulletedList = new Button(Languages.message("BulletedList"));
            bulletedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addTextInFrontOfEachLine("- ");
                }
            });
            listNodes.add(bulletedList);

            addFlowPane(listNodes);

            List<Node> codeNodes = new ArrayList<>();

            Button SeparatorLine = new Button(Languages.message("SeparateLine"));
            SeparatorLine.setOnAction((ActionEvent event) -> {
                insertText("\n---\n");
            });
            codeNodes.add(SeparatorLine);

            Button Quote = new Button(Languages.message("Quote"));
            Quote.setOnAction((ActionEvent event) -> {
                insertText("\n\n>");
            });
            codeNodes.add(Quote);

            Button Codes = new Button(Languages.message("Codes"));
            Codes.setOnAction((ActionEvent event) -> {
                addTextAround("`");
            });
            codeNodes.add(Codes);

            Button CodesBlock = new Button(Languages.message("CodesBlock"));
            CodesBlock.setOnAction((ActionEvent event) -> {
                addTextAround("\n```\n", "\n```\n");
            });
            codeNodes.add(CodesBlock);

            Button ReferLocalFile = new Button(Languages.message("ReferLocalFile"));
            ReferLocalFile.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.selectFile(myController, VisitHistory.FileType.All);
                if (file == null) {
                    return;
                }
                insertText(UrlTools.decodeURL(file, Charset.defaultCharset()));
            });
            codeNodes.add(ReferLocalFile);

            addFlowPane(codeNodes);

            List<javafx.scene.Node> otherNodes = new ArrayList<>();

            Button Bold = new Button(Languages.message("Bold"));
            Bold.setOnAction((ActionEvent event) -> {
                addTextAround("**");
            });
            otherNodes.add(Bold);

            Button Italic = new Button(Languages.message("Italic"));
            Italic.setOnAction((ActionEvent event) -> {
                addTextAround("*");
            });
            otherNodes.add(Italic);

            Button BoldItalic = new Button(Languages.message("BoldItalic"));
            BoldItalic.setOnAction((ActionEvent event) -> {
                addTextAround("***");
            });
            otherNodes.add(BoldItalic);

            addFlowPane(otherNodes);

            Hyperlink about = new Hyperlink(Languages.message("AboutMarkdown"));
            about.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (Languages.isChinese()) {
                        openLink("https://baike.baidu.com/item/markdown");
                    } else {
                        openLink("https://daringfireball.net/projects/markdown/");
                    }
                }
            });
            addNode(about);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void insertText(String string) {
        if (textInput == null) {
            return;
        }
        IndexRange range = textInput.getSelection();
        textInput.insertText(range.getStart(), string);
        textInput.requestFocus();
    }

    public void addTextInFrontOfCurrentLine(String string) {
        if (textInput == null) {
            return;
        }
        IndexRange range = textInput.getSelection();
        int first = range.getStart();
        while (first > 0) {
            if ("\n".equals(textInput.getText(first - 1, first))) {
                break;
            }
            first--;
        }
        textInput.requestFocus();
        textInput.insertText(first, string);
    }

    public void addTextInFrontOfEachLine(String prefix) {
        if (textInput == null) {
            return;
        }
        IndexRange range = textInput.getSelection();
        int start = range.getStart();
        int end = range.getEnd();
        addTextInFrontOfCurrentLine(prefix);

        if (start == end) {
            return;
        }
        int prefixLen = prefix.length();
        start += prefixLen;
        end += prefixLen;
        int pos;
        while (true) {
            pos = textInput.getText(start, end).indexOf('\n');
            if (pos < 0) {
                break;
            }
            textInput.insertText(start + pos + 1, prefix);
            start += pos + prefixLen + 1;
            end += prefixLen;
            int len = textInput.getLength();
            if (start >= end || start >= len || end >= len) {
                break;
            }
        }
        textInput.requestFocus();
    }

    public void addTextAround(String string) {
        addTextAround(string, string);
    }

    public void addTextAround(String prefix, String suffix) {
        if (textInput == null) {
            return;
        }
        IndexRange range = textInput.getSelection();
        if (range.getLength() == 0) {
            String s = prefix + Languages.message("Text") + suffix;
            textInput.insertText(range.getStart(), s);
            textInput.selectRange(range.getStart() + prefix.length(),
                    range.getStart() + prefix.length() + Languages.message("Text").length());
        } else {
            textInput.insertText(range.getStart(), prefix);
            textInput.insertText(range.getEnd() + prefix.length(), suffix);
        }
        textInput.requestFocus();
    }

    @FXML
    @Override
    public void editAction() {
        MarkdownEditorController controller = (MarkdownEditorController) openStage(Fxmls.MarkdownEditorFxml);
        controller.loadContents(textInput.getText());
    }

    @FXML
    @Override
    public void popAction() {
        if (textInput == null) {
            return;
        }
        if (parentController instanceof BaseFileEditorController) {
            BaseFileEditorController e = (BaseFileEditorController) parentController;
            if (textInput != null && textInput == e.mainArea) {
                e.popAction();
                return;
            }
        }
        MarkdownEditorController controller = (MarkdownEditorController) openStage(Fxmls.MarkdownEditorFxml);
        controller.setAsPopup(baseName + "Pop");
        controller.autoSaveCheck.setSelected(false);
        controller.loadContents(textInput.getText());
    }

    /*
        static methods
     */
    public static MenuMarkdownEditController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null || node == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(parent, Fxmls.MenuMarkdownEditFxml, node, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuMarkdownEditController)) {
                return null;
            }
            MenuMarkdownEditController controller = (MenuMarkdownEditController) object;
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MenuMarkdownEditController open(BaseController parent, Node node, MouseEvent event) {
        return open(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public static MenuMarkdownEditController open(BaseController parent, Node node, ContextMenuEvent event) {
        return open(parent, node, event.getScreenX() + 40, event.getScreenY() + 40);
    }

}
