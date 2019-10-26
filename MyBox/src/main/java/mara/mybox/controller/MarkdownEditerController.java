package mara.mybox.controller;

import com.vladsch.flexmark.ast.util.TextCollectingVisitor;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class MarkdownEditerController extends TextEditerController {

    protected WebEngine webEngine;
    protected Parser parser;
    protected HtmlRenderer renderer;
    protected MutableDataHolder parserOptions;
    protected int indentSize = 4;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab markdownTab, htmlTab, codesTab, textTab;
    @FXML
    protected WebView webView;
    @FXML
    protected TextArea htmlArea, textArea;
    @FXML
    protected ComboBox<String> emulationSelector, indentSelector, styleSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck;
    @FXML
    protected TextField titleInput;
    @FXML
    protected HBox mdBox, htmlBox, textBox;

    public MarkdownEditerController() {
        baseTitle = AppVariables.message("MarkdownEditer");

        editType = Edit_Type.Text;

        SourceFileType = VisitHistory.FileType.Markdown;
        SourcePathType = VisitHistory.FileType.Markdown;
        TargetPathType = VisitHistory.FileType.Markdown;
        TargetFileType = VisitHistory.FileType.Markdown;
        AddFileType = VisitHistory.FileType.Markdown;
        AddPathType = VisitHistory.FileType.Markdown;

        sourcePathKey = "MarkdownFilePath";
        PageSizeKey = "MarkdownPageSize";

        sourceExtensionFilter = CommonImageValues.MarkdownExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

        AppVariables.setUserConfigInt(PageSizeKey, Integer.MAX_VALUE); // All in one page
    }

    @Override
    public void initializeNext() {
        try {
            initPage(null);
            initFileTab();
            initConversionOptions();
            initLocateTab();
            initReplaceTab();

            initMainArea();
            initHtmlTab();
            initTextTab();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    if (markdownTab.equals(oldValue)) {
                        markdown2all();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    protected void initMainArea() {
        try {
            super.initMainArea();
            mdBox.disableProperty().bind(mainArea.textProperty().isEmpty());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initHtmlTab() {
        try {
            webView.setContextMenuEnabled(true);
            webEngine = webView.getEngine();

            htmlBox.disableProperty().bind(mdBox.disableProperty());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initTextTab() {
        try {
            textBox.disableProperty().bind(mdBox.disableProperty());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initConversionOptions() {
        try {
            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    makeConverter();
                }
            });
            emulationSelector.getSelectionModel().select(0);

            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                            makeConverter();
                        }
                    } catch (Exception e) {
                    }
                }
            });
            indentSelector.getSelectionModel().select(0);

            trimCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeConverter();
                }
            });
            appendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeConverter();
                }
            });
            discardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeConverter();
                }
            });
            linesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeConverter();
                }
            });

            styleSelector.getItems().addAll(Arrays.asList(
                    message("DefaultStyle"), message("ConsoleStyle"), message("None")
            ));
            styleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    makeConverter();
                }
            });
            styleSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void makeConverter() {
        try {
            parserOptions = new MutableDataSet();
            parserOptions.setFrom(ParserEmulationProfile.valueOf(emulationSelector.getValue()));
            parserOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    //                    AbbreviationExtension.create(),
                    //                    DefinitionExtension.create(),
                    //                    FootnoteExtension.create(),
                    //                    TypographicExtension.create(),
                    TablesExtension.create()
            ));
            parserOptions.set(HtmlRenderer.INDENT_SIZE, indentSize)
                    //                    .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
                    //                    .set(TablesExtension.COLUMN_SPANS, false)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, trimCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected())
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, discardCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected());

            parser = Parser.builder(parserOptions).build();
            renderer = HtmlRenderer.builder(parserOptions).build();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void markdown2html() {
        if (mainArea.getText().isEmpty()) {
            htmlArea.setText("");
            webEngine.loadContent("");
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String html;

                @Override
                protected boolean handle() {
                    html = convert2html();
                    return html != null;
                }

                @Override
                protected void whenSucceeded() {
                    htmlArea.setText(html);
                    webEngine.loadContent(html);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void markdown2text() {
        if (mainArea.getText().isEmpty()) {
            textArea.setText("");
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String text;

                @Override
                protected boolean handle() {
                    text = convert2text();
                    return text != null;
                }

                @Override
                protected void whenSucceeded() {
                    textArea.setText(text);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void markdown2all() {
        if (mainArea.getText().isEmpty()) {
            htmlArea.setText("");
            webEngine.loadContent("");
            textArea.setText("");
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String html, text;

                @Override
                protected boolean handle() {
                    try {
                        html = convert2html();
                        text = convert2text();
                        return html != null || text != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    htmlArea.setText(html);
                    webEngine.loadContent(html);
                    textArea.setText(text);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public String convert2html() {
        try {
            if (parserOptions == null || parser == null || renderer == null) {
                makeConverter();
            }
            Node document = parser.parse(mainArea.getText());
            String html = renderer.render(document);
            String style;
            if (message("ConsoleStyle").equals(styleSelector.getValue())) {
                style = HtmlTools.ConsoleStyle;
            } else if (message("DefaultStyle").equals(styleSelector.getValue())) {
                style = HtmlTools.DefaultStyle;
            } else {
                style = null;
            }
            html = HtmlTools.html(titleInput.getText(), style, html);
            return html;
        } catch (Exception e) {
            return e.toString();
        }
    }

    public String convert2text() {
        try {
            // https://github.com/vsch/flexmark-java/blob/master/flexmark-java-samples/src/com/vladsch/flexmark/samples/MarkdownToText.java
            DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
            MutableDataSet FORMAT_OPTIONS = new MutableDataSet();
            FORMAT_OPTIONS.set(Parser.EXTENSIONS, OPTIONS.get(Parser.EXTENSIONS));
            Parser PARSER = Parser.builder(OPTIONS).build();

            Node document = PARSER.parse(mainArea.getText());
            TextCollectingVisitor textCollectingVisitor = new TextCollectingVisitor();
            String text = textCollectingVisitor.collectAndGetText(document);
            return text;
        } catch (Exception e) {
            return e.toString();
        }
    }

    public void loadMarkdown(String md) {
        mainArea.setText(md);
        markdown2all();
    }

    /*
        Common methods
     */
    protected void insertText(String string) {
        IndexRange range = mainArea.getSelection();
        mainArea.insertText(range.getStart(), string);
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
    }

    protected void addTextAround(String string) {
        addTextAround(string, string);
    }

    protected void addTextAround(String prefix, String suffix) {
        IndexRange range = mainArea.getSelection();
        mainArea.insertText(range.getStart(), prefix);
        mainArea.insertText(range.getEnd() + prefix.length(), suffix);
    }

    /*
        Input formats
     */
    @FXML
    protected void header1() {
        addTextInFrontOfCurrentLine("# ");
    }

    @FXML
    protected void header2() {
        addTextInFrontOfCurrentLine("## ");
    }

    @FXML
    protected void header3() {
        addTextInFrontOfCurrentLine("### ");
    }

    @FXML
    protected void header4() {
        addTextInFrontOfCurrentLine("#### ");
    }

    @FXML
    protected void header5() {
        addTextInFrontOfCurrentLine("##### ");
    }

    @FXML
    protected void header6() {
        addTextInFrontOfCurrentLine("###### ");
    }

    @FXML
    protected void bold() {
        addTextAround("**");
    }

    @FXML
    protected void italic() {
        addTextAround("*");
    }

    @FXML
    protected void boldItalic() {
        addTextAround("***");
    }

    @FXML
    protected void code() {
        addTextAround("`");
    }

    @FXML
    protected void codesBlock() {
        addTextAround("\n```\n", "\n```\n");
    }

    @FXML
    protected void numberedList() {
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
    }

    @FXML
    protected void bulletedList() {
        addTextInFrontOfEachLine("- ");
    }

    @FXML
    protected void quote() {
        insertText("\n\n>");
    }

    @FXML
    protected void separatorLine() {
        insertText("\n---\n");
    }

    @FXML
    protected void image() {
        insertText("![" + message("Name") + "](http://" + message("Address") + ")");
    }

    @FXML
    protected void makeLink() {
        insertText("[" + message("Name") + "](http://" + message("Address") + ")");
    }

    @FXML
    protected void table() {
        insertText("\n\n| h1 | h2 | h3 |  \n"
                + "| --- | --- | --- |  \n"
                + "| d1 | d2 | d3 |  \n");
    }

    @FXML
    protected void newline() {
        insertText("  \n");
    }

    /*
        Buttons Action
     */
    @Override
    public MarkdownEditerController openNewStage() {
        return (MarkdownEditerController) openStage(CommonValues.MarkdownEditorFxml);
    }

    @FXML
    protected void refreshHtml() {
        markdown2html();
    }

    @FXML
    protected void saveHtml() {
        if (htmlArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }

            String name = "";
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath("HtmlFilePath"),
                    name, CommonImageValues.HtmlExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file, "HtmlFilePath",
                    VisitHistory.FileType.Html, VisitHistory.FileType.Html);

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return FileTools.writeFile(file, htmlArea.getText()) != null;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void popSaveHtml(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Html);
            }

            @Override
            public void handleSelect() {
                saveHtml();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                AppVariables.setUserConfigValue("HtmlFilePath", fname);
                handleSelect();
            }

        }.pop();
    }

    @FXML
    protected void editHtml() {
        if (htmlArea.getText().isEmpty()) {
            return;
        }
        HtmlTools.displayHtml(htmlArea.getText());
    }

    @FXML
    protected void refreshText() {
        markdown2text();
    }

    @FXML
    protected void saveText() {
        if (textArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }

            String name = "";
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath("TextFilePath"),
                    name, CommonImageValues.TextExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file, "TextFilePath",
                    VisitHistory.FileType.Text, VisitHistory.FileType.Text);

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return FileTools.writeFile(file, textArea.getText()) != null;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void popSaveText(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                saveText();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                AppVariables.setUserConfigValue("TextFilePath", fname);
                handleSelect();
            }

        }.pop();
    }

    @FXML
    @Override
    public void createAction() {
        super.createAction();
        htmlArea.setText("");
        webEngine.loadContent("");
        textArea.setText("");
    }

}
