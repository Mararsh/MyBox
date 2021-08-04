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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlWriteTools;

import mara.mybox.value.Fxmls;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    protected TabPane tabPane;
    @FXML
    protected Tab markdownTab, htmlTab, codesTab;
    @FXML
    protected TextArea htmlArea;
    @FXML
    protected ComboBox<String> emulationSelector, indentSelector, styleSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck, wrapCheck;
    @FXML
    protected TextField titleInput;
    @FXML
    protected WebView webView;
    @FXML
    protected Button editHtmlButton;

    public MarkdownEditerController() {
        baseTitle = Languages.message("MarkdownEditer");
        TipsLabelKey = "MarkdownEditerTips";
    }

    @Override
    public void setFileType() {
        setMarkdownType();
    }

    @Override
    protected void initPairBox() {
        try {
            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue Tab, Tab oldValue, Tab newValue) {
                    if (isSettingValues || oldValue != markdownTab || !fileChanged.get()) {
                        return;
                    }
                    markdown2html();
                }
            });

            webEngine = webView.getEngine();
            editHtmlButton.disableProperty().bind(Bindings.isEmpty(htmlArea.textProperty()));

            wrapCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                UserConfig.setUserConfigBoolean(baseName + "Wrap", wrapCheck.isSelected());
                htmlArea.setWrapText(wrapCheck.isSelected());
            });
            htmlArea.setWrapText(wrapCheck.isSelected());

            initConversionOptionsPane();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initConversionOptionsPane() {
        try {
            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().select(UserConfig.getUserConfigString(baseName + "Emulation", "GITHUB"));
            emulationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    UserConfig.setUserConfigString(baseName + "Emulation", newValue);
                    updateHtmlConverter();
                }
            });

            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().select(UserConfig.getUserConfigString(baseName + "Indent", "4"));
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                            UserConfig.setUserConfigString(baseName + "Indent", newValue);
                            updateHtmlConverter();
                        }
                    } catch (Exception e) {
                    }
                }
            });

            trimCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Trim", false));
            trimCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "Indent", trimCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            appendCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Append", false));
            appendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "Append", appendCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            discardCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Discard", false));
            discardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "Discard", discardCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            linesCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Trim", false));
            linesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "Trim", linesCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            List<String> styles = new ArrayList<>();
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                styles.add(Languages.message(style.name()));
            }
            styleSelector.getItems().addAll(styles);
            styleSelector.getSelectionModel().select(UserConfig.getUserConfigString(baseName + "HtmlStyle", Languages.message("Default")));
            styleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    UserConfig.setUserConfigString(baseName + "HtmlStyle", newValue);
                    updateHtmlConverter();
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
        if (isSettingValues) {
            return;
        }
        markdown2html();
    }

    @Override
    protected void updatePairArea() {
        refreshPairAction();
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

                        String style = UserConfig.getUserConfigString(baseName + "HtmlStyle", Languages.message("Default"));
                        html = HtmlWriteTools.html(titleInput.getText(), style, html);
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

    @Override
    public void makeEditContextMenu(javafx.scene.Node node) {
        try {
            if (node == mainArea) {
                node.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                    @Override
                    public void handle(ContextMenuEvent event) {
                        MenuMarkdownEditController.open(myController, node, event);
                    }
                });
            } else {
                super.makeEditContextMenu(node);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void popButtons(MouseEvent mouseEvent) {
        MenuMarkdownEditController.open(myController, mainArea, mouseEvent);
    }

    @FXML
    protected void editHtml() {
        String text = htmlArea.getText();
        if (text.isEmpty()) {
            return;
        }
        HtmlEditorController controller = (HtmlEditorController) openStage(Fxmls.HtmlEditorFxml);
        controller.loadContents(text);
    }

    @FXML
    @Override
    public void createAction() {
        super.createAction();
        clearPairArea();
    }

}
