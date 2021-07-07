package mara.mybox.controller;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
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
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class MarkdownEditerController extends TextEditerController {

    protected WebEngine webEngine;
    protected MutableDataSet htmlOptions;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRenderer;
    protected int indentSize = 4;

    @FXML
    protected TextArea htmlArea;
    @FXML
    protected ComboBox<String> emulationSelector, indentSelector, styleSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck, wrapCheck, updateCheck;
    @FXML
    protected TextField titleInput;
    @FXML
    protected BaseWebViewController webviewController;
    @FXML
    protected Button editHtmlButton;

    public MarkdownEditerController() {
        baseTitle = AppVariables.message("MarkdownEditer");
        TipsLabelKey = "MarkdownEditerTips";
    }

    @Override
    public void setFileType() {
        setMarkdownType();
    }

    @Override
    protected void initPairBox() {
        try {
            webviewController.setParameters(this);
            webEngine = webviewController.webView.getEngine();

            wrapCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Wrap", wrapCheck.isSelected());
                        htmlArea.setWrapText(wrapCheck.isSelected());
                    });
            htmlArea.setWrapText(wrapCheck.isSelected());

            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Emulation", "GITHUB"));
            emulationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "Emulation", newValue);
                    updateHtmlConverter();
                }
            });

            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Indent", "4"));
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                            AppVariables.setUserConfigValue(baseName + "Indent", newValue);
                            updateHtmlConverter();
                        }
                    } catch (Exception e) {
                    }
                }
            });

            trimCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Trim", false));
            trimCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Indent", trimCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            appendCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Append", false));
            appendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Append", appendCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            discardCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Discard", false));
            discardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Discard", discardCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            linesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Trim", false));
            linesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Trim", linesCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            List<String> styles = new ArrayList<>();
            for (HtmlTools.HtmlStyle style : HtmlTools.HtmlStyle.values()) {
                styles.add(message(style.name()));
            }
            styleSelector.getItems().addAll(styles);
            styleSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "HtmlStyle", message("Default")));
            styleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "HtmlStyle", newValue);
                    updateHtmlConverter();
                }
            });

            updateCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "UpdateSynchronously", false));
            updateCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "UpdateSynchronously", updateCheck.isSelected());
                        if (updateCheck.isSelected()) {
                            refreshPairAction();
                        }
                    });

            editHtmlButton.disableProperty().bind(Bindings.isEmpty(htmlArea.textProperty()));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void refreshPairAction() {
        if (isSettingValues || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        markdown2html();
    }

    @Override
    protected void setPairAreaSelection() {
    }

    @Override
    protected void scrollTopPairArea(double value) {
    }

    @Override
    protected void scrollLeftPairArea(double value) {
    }

    @Override
    protected void clearPairArea() {
        htmlArea.setText("");
        if (webEngine != null) {
            webEngine.loadContent("");
        }
    }

    // https://github.com/vsch/flexmark-java/wiki/Usage
    protected void makeHtmlConverter() {
        try {
            htmlOptions = new MutableDataSet();
            htmlOptions.setFrom(ParserEmulationProfile.valueOf(emulationSelector.getValue()));
            htmlOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    AbbreviationExtension.create(),
                    DefinitionExtension.create(),
                    FootnoteExtension.create(),
                    TablesExtension.create(),
                    TypographicExtension.create()
            ));

            htmlOptions.set(HtmlRenderer.INDENT_SIZE, indentSize)
                    //                    .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
                    //                    .set(TablesExtension.COLUMN_SPANS, false)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, trimCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected())
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, discardCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected());

            htmlParser = Parser.builder(htmlOptions).build();
            htmlRenderer = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void updateHtmlConverter() {
        makeHtmlConverter();
        markdown2html();
    }

    public void loadMarkdown(String md) {
        mainArea.setText(md);
        markdown2html();
    }

    protected void markdown2html() {
        if (webEngine == null) {
            return;
        }
        webEngine.getLoadWorker().cancel();
        webEngine.loadContent("");
        htmlArea.clear();
        if (mainArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            double htmlScrollLeft = htmlArea.getScrollLeft();
            double htmlScrollTop = htmlArea.getScrollTop();
            int htmlAnchor = htmlArea.getAnchor();
            int htmlCaretPosition = htmlArea.getCaretPosition();
            double htmlWidth = (Integer) webEngine.executeScript("document.documentElement.scrollWidth || document.body.scrollWidth;");
            double htmlHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");

            task = new SingletonTask<Void>() {

                private String html;

                @Override
                protected boolean handle() {
                    try {
                        if (htmlOptions == null || htmlParser == null || htmlRenderer == null) {
                            makeHtmlConverter();
                        }
                        Node document = htmlParser.parse(mainArea.getText());
                        html = htmlRenderer.render(document);

                        String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", message("Default"));
                        html = HtmlTools.html(titleInput.getText(), style, html);
                        return html != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        Platform.runLater(() -> {
                            htmlArea.setText(html);
                            htmlArea.setScrollLeft(htmlScrollLeft);
                            htmlArea.setScrollTop(htmlScrollTop);
                            htmlArea.selectRange(htmlAnchor, htmlCaretPosition);
                        });
                        Platform.runLater(() -> {
                            webEngine.loadContent(html);
                            webEngine.executeScript("window.scrollTo(" + htmlWidth + "," + htmlHeight + ");");
                        });

                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        webEngine.getLoadWorker().cancel();
                    }

                }

            };
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        Common methods
     */
    protected void insertText(String string) {
        IndexRange range = mainArea.getSelection();
        mainArea.insertText(range.getStart(), string);
        mainArea.requestFocus();
    }

    protected void addTextInFrontOfCurrentLine(String string) {
        IndexRange range = mainArea.getSelection();
        int first = range.getStart();
        while (first > 0) {
            if ("\n".equals(mainArea.getText(first - 1, first))) {
                break;
            }
            first--;
        }
        mainArea.requestFocus();
        mainArea.insertText(first, string);
    }

    @FXML
    protected void addTextInFrontOfEachLine(String prefix) {
        IndexRange range = mainArea.getSelection();
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
            pos = mainArea.getText(start, end).indexOf('\n');
            if (pos < 0) {
                break;
            }
            mainArea.insertText(start + pos + 1, prefix);
            start += pos + prefixLen + 1;
            end += prefixLen;
            int len = mainArea.getLength();
            if (start >= end || start >= len || end >= len) {
                break;
            }
        }
        mainArea.requestFocus();
    }

    protected void addTextAround(String string) {
        addTextAround(string, string);
    }

    protected void addTextAround(String prefix, String suffix) {
        IndexRange range = mainArea.getSelection();
        if (range.getLength() == 0) {
            String s = prefix + message("Text") + suffix;
            mainArea.insertText(range.getStart(), s);
            mainArea.selectRange(range.getStart() + prefix.length(),
                    range.getStart() + prefix.length() + message("Text").length());
        } else {
            mainArea.insertText(range.getStart(), prefix);
            mainArea.insertText(range.getEnd() + prefix.length(), suffix);
        }
        mainArea.requestFocus();
    }

    /*
        Input formats
     */
    @Override
    public void makeMainAreaContextMenu(PopNodesController controller) {
        try {
            super.makeMainAreaContextMenu(controller);
            controller.addNode(new Separator());

            List<javafx.scene.Node> aNodes = new ArrayList<>();

            Button br = new Button(message("BreakLine"));
            br.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("    \n");
                }
            });
            aNodes.add(br);

            Button p = new Button(message("Paragraph"));
            p.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("    \n" + message("Paragraph") + "    \n");
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

            Button image = new Button(message("Image"));
            image.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("![" + message("Name") + "](http://" + message("Address") + ")");
                }
            });
            aNodes.add(image);

            Button link = new Button(message("Link"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertText("[" + message("Name") + "](http://" + message("Address") + ")");
                }
            });
            aNodes.add(link);

            controller.addFlowPane(aNodes);
            controller.addNode(new Separator());

            List<javafx.scene.Node> headNodes = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                String name = message("Headings") + " " + i;
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

            controller.addFlowPane(headNodes);
            controller.addNode(new Separator());

            List<javafx.scene.Node> listNodes = new ArrayList<>();
            Button numberedList = new Button(message("NumberedList"));
            numberedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    IndexRange range = mainArea.getSelection();
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
                        pos = mainArea.getText(start, end).indexOf('\n');
                        if (pos < 0) {
                            break;
                        }
                        count++;
                        mainArea.insertText(start + pos + 1, count + ". ");
                        int nlen = 2 + (count + "").length();
                        start += pos + 1 + nlen;
                        end += nlen;
                        int len = mainArea.getLength();
                        if (start >= end || start >= len || end >= len) {
                            break;
                        }
                    }
                    mainArea.requestFocus();
                }
            });
            listNodes.add(numberedList);

            Button bulletedList = new Button(message("BulletedList"));
            bulletedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addTextInFrontOfEachLine("- ");
                }
            });
            listNodes.add(bulletedList);

            controller.addFlowPane(listNodes);
            controller.addNode(new Separator());

            List<javafx.scene.Node> otherNodes = new ArrayList<>();

            Button Bold = new Button(message("Bold"));
            Bold.setOnAction((ActionEvent event) -> {
                addTextAround("**");
            });
            otherNodes.add(Bold);

            Button Italic = new Button(message("Italic"));
            Italic.setOnAction((ActionEvent event) -> {
                addTextAround("*");
            });
            otherNodes.add(Italic);

            Button BoldItalic = new Button(message("BoldItalic"));
            BoldItalic.setOnAction((ActionEvent event) -> {
                addTextAround("***");
            });
            otherNodes.add(BoldItalic);

            Button Quote = new Button(message("Quote"));
            Quote.setOnAction((ActionEvent event) -> {
                insertText("\n\n>");
            });
            otherNodes.add(Quote);

            Button Codes = new Button(message("Codes"));
            Codes.setOnAction((ActionEvent event) -> {
                addTextAround("`");
            });
            otherNodes.add(Codes);

            Button CodesBlock = new Button(message("CodesBlock"));
            CodesBlock.setOnAction((ActionEvent event) -> {
                addTextAround("\n```\n", "\n```\n");
            });
            otherNodes.add(CodesBlock);

            Button ReferLocalFile = new Button(message("ReferLocalFile"));
            ReferLocalFile.setOnAction((ActionEvent event) -> {
                File file = FxmlControl.selectFile(this, VisitHistory.FileType.All);
                if (file == null) {
                    return;
                }
                insertText(HtmlTools.decodeURL(file, Charset.defaultCharset()));
            });
            otherNodes.add(ReferLocalFile);

            Button SeparatorLine = new Button(message("SeparatorLine"));
            SeparatorLine.setOnAction((ActionEvent event) -> {
                insertText("\n---\n");
            });
            otherNodes.add(SeparatorLine);

            controller.addFlowPane(otherNodes);
            controller.addNode(new Separator());

            Hyperlink about = new Hyperlink(message("AboutMarkdown"));
            about.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (AppVariables.isChinese()) {
                        openLink("https://baike.baidu.com/item/markdown");
                    } else {
                        openLink("https://daringfireball.net/projects/markdown/");
                    }
                }
            });
            controller.addNode(about);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addTable(int rowsNumber, int colsNumber) {
        String s = "\n|";
        for (int j = 1; j <= colsNumber; j++) {
            s += " col" + j + " |";
        }
        s += "    \n";
        for (int i = 1; i <= rowsNumber; i++) {
            s += "|";
            for (int j = 1; j <= colsNumber; j++) {
                s += " v" + i + "-" + j + " |";
            }
            s += "    \n";
        }
        insertText(s);
    }

    @FXML
    public void popValues(MouseEvent mouseEvent) {
        try {
            popup = FxmlWindow.popWindow(myController, mouseEvent);
            if (popup == null) {
                return;
            }
            Object object = popup.getUserData();
            if (object != null && object instanceof PopNodesController) {
                PopNodesController controller = (PopNodesController) object;
                makeMainAreaContextMenu(controller);
                controller.setParameters(myController);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        Buttons Action
     */
    @Override
    public MarkdownEditerController openNewStage() {
        return (MarkdownEditerController) openStage(CommonValues.MarkdownEditorFxml);
    }

    @FXML
    protected void editHtml() {
        String text = htmlArea.getText();
        if (text.isEmpty()) {
            return;
        }
        HtmlEditorController controller
                = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
        controller.loadContents(text);
    }

    @FXML
    @Override
    public void createAction() {
        super.createAction();
        clearPairArea();
    }

}
