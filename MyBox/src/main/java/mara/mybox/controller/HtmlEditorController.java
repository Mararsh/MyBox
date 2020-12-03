package mara.mybox.controller;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.Link;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends HtmlBaseController {

    protected boolean isFrameSet;
    protected Parser htmlParser, textParser;
    protected HtmlRenderer htmlRender;
    protected MutableDataSet htmlOptions, textOptions;
    protected FlexmarkHtmlConverter htmlConverter;
    protected TextCollectingVisitor textCollectingVisitor;

    @FXML
    protected Button synchronizeMainButton, synchronizePairButton, editMarkdownButton, styleLinksButton;
    @FXML
    protected HTMLEditor htmlEditor;
    @FXML
    protected WebView linksWebview;
    @FXML
    protected TextArea codesArea, tocArea, markdownArea, textArea;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editorTab, codesTab, tocTab, markdownTab, textTab, linksTab;
    @FXML
    protected Label editorLabel, codesLabel, mdLabel;

    public HtmlEditorController() {
        baseTitle = AppVariables.message("HtmlEditor");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            isFrameSet = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initEdtior();
            initTabPane();
            initCodesTab();
            initTextTab();
            initTocTab();
            initMarkdownTab();
            initLinksTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEdtior() {
        try {
            // As my testing, only DragEvent.DRAG_EXITED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED working for HtmlEdior
            htmlEditor.addEventHandler(DragEvent.DRAG_EXITED, new EventHandler<InputEvent>() { // work
                @Override
                public void handle(InputEvent event) {
                    checkEditorChanged();
                }
            });
            htmlEditor.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
//                    MyBoxLog.debug("setOnKeyReleased");
                    checkEditorChanged();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void checkEditorChanged() {
        try {
            String c = htmlEditor.getHtmlText();
            int len = 0;
            if (c != null && !c.isEmpty()) {
                len = htmlEditor.getHtmlText().length();
            }
            editorLabel.setText(AppVariables.message("Total") + ": " + len);
            updateTitle(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initTabPane() {
        try {
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowEditor", true)) {
                tabPane.getTabs().remove(editorTab);
            }
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowCodes", true)) {
                tabPane.getTabs().remove(codesTab);
            }
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowMarkdown", true)) {
                tabPane.getTabs().remove(markdownTab);
            }
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowToc", true)) {
                tabPane.getTabs().remove(tocTab);
            }
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowLinks", true)) {
                tabPane.getTabs().remove(linksTab);
            }
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowText", true)) {
                tabPane.getTabs().remove(textTab);
            }

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    synchronizePairButton.setVisible(
                            newValue == editorTab || newValue == codesTab || newValue == markdownTab);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initCodesTab() {
        try {
            codesArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    int len = codesArea.getText().length();
                    codesLabel.setText(AppVariables.message("Total") + ": " + len);
                    updateTitle(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initTextTab() {
        try {
            DataHolder textHolder = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
            textOptions = new MutableDataSet();
            textOptions.set(Parser.EXTENSIONS, textHolder.get(Parser.EXTENSIONS));
            textParser = Parser.builder(textOptions).build();
            textCollectingVisitor = new TextCollectingVisitor();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initTocTab() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initLinksTab() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initMarkdownTab() {
        try {
            markdownArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    int len = markdownArea.getText().length();
                    mdLabel.setText(AppVariables.message("Total") + ": " + len);
                    updateTitle(true);
                }
            });

            htmlOptions = new MutableDataSet();
            htmlOptions.setFrom(ParserEmulationProfile.valueOf("PEGDOWN"));
            htmlOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    AbbreviationExtension.create(),
                    DefinitionExtension.create(),
                    FootnoteExtension.create(),
                    TypographicExtension.create(),
                    TablesExtension.create()
            ));
            htmlOptions.set(HtmlRenderer.INDENT_SIZE, 4)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, false)
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, true);

            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

            htmlConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FxmlControl.setTooltip(synchronizeMainButton, message("SynchronizeChangesToOtherPanes"));
            FxmlControl.setTooltip(synchronizePairButton, message("SynchronizeChangesToOtherPanes"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void loadContents(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        loadEditor(text);
        webEngine.loadContent(text);
        codesArea.setText(text);
        html2markdown(text);
        sourceFile = null;
        updateTitle(true);
    }

    public void loadEditor(String html) {
        isFrameSet = StringTools.include(html, "<FRAMESET>", true);
        if (isFrameSet) {
            htmlEditor.setHtmlText("<p>" + message("NotSupportFrameSet") + "</p>");
        } else {
            htmlEditor.setHtmlText(html);
        }
    }

    @Override
    public void loadLink(URI uri) {
        try {
            if (webEngine == null || uri == null) {
                popError(message("InvalidData"));
                return;
            }
            this.uri = uri;
            urlBox.setValue(uri.toString());
            isFrameSet = false;
            webEngine.getLoadWorker().cancel();
            webEngine.loadContent("");
            codesArea.setText(uri + "\n" + message("Loading..."));
            tocArea.setText(uri + "\n" + message("Loading..."));
            textArea.setText(uri + "\n" + message("Loading..."));
            linksWebview.getEngine().getLoadWorker().cancel();
            linksWebview.getEngine().loadContent("");
            markdownArea.setText(uri + "\n" + message("Loading..."));
            htmlEditor.setHtmlText(uri + "<br>" + message("Loading..."));
            editorLabel.setText(uri + "  " + message("Loading..."));
            webLabel.setText(uri + "  " + message("Loading..."));
            codesLabel.setText(uri + "  " + message("Loading..."));
            mdLabel.setText(uri + "  " + message("Loading..."));
            htmlEditor.setDisable(true);
//            saveButton.setDisable(true);
//            saveAsButton.setDisable(true);
//            synchronizeMainButton.setDisable(true);
            synchronizePairButton.setDisable(true);
            markdownTab.setDisable(true);
            codesTab.setDisable(true);
            tocTab.setDisable(true);
            textTab.setDisable(true);
            linksTab.setDisable(true);
            webLabel.setText(uri + "  " + message("Loading..."));
            webEngine.load(uri.toString());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void reset() {
        super.reset();
        htmlEditor.setDisable(false);
//        saveButton.setDisable(false);
//        saveAsButton.setDisable(false);
//        synchronizeMainButton.setDisable(false);
        synchronizePairButton.setDisable(false);
        markdownTab.setDisable(false);
        codesTab.setDisable(false);
        textTab.setDisable(false);
        tocTab.setDisable(false);
        linksTab.setDisable(false);
        editorLabel.setText("");
        webLabel.setText("");
        codesLabel.setText("");
        mdLabel.setText("");
    }

    @Override
    protected void afterPageLoaded() {
        super.afterPageLoaded();
        synchronizeMain();
    }

    @FXML
    public void synchronizeMain() {
        if (!tabPane.getTabs().contains(markdownTab)
                && !tabPane.getTabs().contains(codesTab)
                && !tabPane.getTabs().contains(editorTab)
                && !tabPane.getTabs().contains(textTab)
                && !tabPane.getTabs().contains(tocTab)
                && !tabPane.getTabs().contains(linksTab)) {
            return;
        }
        try {
            String html = getHtml(webEngine);
            if (tabPane.getTabs().contains(editorTab)) {
                Platform.runLater(() -> {
                    loadEditor(html);
                });
            }
            if (tabPane.getTabs().contains(codesTab)) {
                Platform.runLater(() -> {
                    codesArea.setText(html);
                });
            }
            if (tabPane.getTabs().contains(markdownTab)
                    || tabPane.getTabs().contains(textTab)
                    || tabPane.getTabs().contains(tocTab)
                    || tabPane.getTabs().contains(linksTab)) {
                Platform.runLater(() -> {
                    html2markdown(html);
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void synchronizePair() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == editorTab) {
                if (isFrameSet) {
                    popError(message("NotSupportFrameSet"));
                    return;
                }
                String text = htmlEditor.getHtmlText();
                if (text.isBlank()) {
                    popError(message("NoData"));
                    return;
                }
                Platform.runLater(() -> {
                    webEngine.loadContent(text);
                });

                if (tabPane.getTabs().contains(codesTab)) {
                    Platform.runLater(() -> {
                        codesArea.setText(text);
                    });
                }
                if (tabPane.getTabs().contains(markdownTab)
                        || tabPane.getTabs().contains(textTab)
                        || tabPane.getTabs().contains(tocTab)
                        || tabPane.getTabs().contains(linksTab)) {
                    Platform.runLater(() -> {
                        html2markdown(text);
                    });
                }

            } else if (tab == codesTab) {
                String text = codesArea.getText();
                if (text.isBlank()) {
                    popError(message("NoData"));
                    return;
                }
                Platform.runLater(() -> {
                    webEngine.loadContent(text);
                });

                if (tabPane.getTabs().contains(editorTab)) {
                    Platform.runLater(() -> {
                        loadEditor(text);
                    });
                }
                if (tabPane.getTabs().contains(markdownTab)
                        || tabPane.getTabs().contains(textTab)
                        || tabPane.getTabs().contains(tocTab)
                        || tabPane.getTabs().contains(linksTab)) {
                    Platform.runLater(() -> {
                        html2markdown(text);
                    });
                }

            } else if (tab == markdownTab) {
                markdown2all();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            if (sourceFile == null) {
                final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                        null, targetExtensionFilter);
                if (file == null) {
                    return;
                }
                recordFileWritten(file);
                sourceFile = file;
            }
            isSettingValues = true;
            String contents;
            if (tabPane.getSelectionModel().getSelectedItem().equals(codesTab)) {
                contents = codesArea.getText();
            } else {
                contents = htmlEditor.getHtmlText();
            }
            try ( BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, Charset.forName("utf-8"), false))) {
                out.write(contents);
                out.flush();
            }

            updateTitle(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        isSettingValues = false;
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            String name = null;
            if (sourceFile != null) {
                name = FileTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, targetExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file);
            sourceFile = file;

            isSettingValues = true;
            String contents;
            if (AppVariables.message("Editor").equals(tabPane.getSelectionModel().getSelectedItem().getText())) {
                contents = htmlEditor.getHtmlText();
            } else {
                contents = codesArea.getText();
            }
            try ( BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, Charset.forName("utf-8"), false))) {
                out.write(contents);
                out.flush();
            }
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        isSettingValues = false;
    }

    @FXML
    @Override
    public void createAction() {
        try {
            isSettingValues = true;
            sourceFile = null;
            uri = null;
            webEngine.getLoadWorker().cancel();
            webEngine.loadContent("");
            htmlEditor.setHtmlText("");
            codesArea.setText("");
            markdownArea.setText("");
            editorLabel.setText("");
            webLabel.setText("");
            codesLabel.setText("");
            mdLabel.setText("");
            isFrameSet = false;
            getMyStage().setTitle(getBaseTitle());
            htmlEditor.setDisable(false);
            saveButton.setDisable(false);
            saveAsButton.setDisable(false);
            synchronizeMainButton.setDisable(false);
            synchronizePairButton.setDisable(false);
            codesArea.setEditable(true);
            markdownArea.setEditable(true);
            updateTitle(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        isSettingValues = false;
    }

    protected String getHtml(WebEngine engine) {
        try {
            if (engine == null) {
                return "";
            }
            Object c = engine.executeScript("document.documentElement.outerHTML");
            if (c == null) {
                return "";
            }
            return (String) c;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    @FXML
    public void popPanesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            CheckMenuItem checkMenu;
            checkMenu = new CheckMenuItem(message("RichText"));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(editorTab)) {
                    tabPane.getTabs().remove(editorTab);
                } else {
                    tabPane.getTabs().add(editorTab);
                    Platform.runLater(() -> {
                        String html = getHtml(webEngine);
                        loadEditor(html);
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowEditor",
                        tabPane.getTabs().contains(editorTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(editorTab));
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem("Markdown");
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(markdownTab)) {
                    tabPane.getTabs().remove(markdownTab);
                } else {
                    tabPane.getTabs().add(markdownTab);
                    Platform.runLater(() -> {
                        String html = getHtml(webEngine);
                        html2markdown(html);
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowMarkdown",
                        tabPane.getTabs().contains(markdownTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(markdownTab));
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("HtmlCodes"));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(codesTab)) {
                    tabPane.getTabs().remove(codesTab);
                } else {
                    tabPane.getTabs().add(codesTab);
                    Platform.runLater(() -> {
                        String html = getHtml(webEngine);
                        codesArea.setText(html);
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowCodes",
                        tabPane.getTabs().contains(codesTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(codesTab));
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Text"));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(textTab)) {
                    tabPane.getTabs().remove(textTab);
                } else {
                    tabPane.getTabs().add(textTab);
                    Platform.runLater(() -> {
                        String html = getHtml(webEngine);
                        html2markdown(html);
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowText",
                        tabPane.getTabs().contains(textTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(textTab));
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Headings"));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(tocTab)) {
                    tabPane.getTabs().remove(tocTab);
                } else {
                    tabPane.getTabs().add(tocTab);
                    Platform.runLater(() -> {
                        String html = getHtml(webEngine);
                        html2markdown(html);
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowToc",
                        tabPane.getTabs().contains(tocTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(tocTab));
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Links"));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(linksTab)) {
                    tabPane.getTabs().remove(linksTab);
                } else {
                    tabPane.getTabs().add(linksTab);
                    Platform.runLater(() -> {
                        String html = getHtml(webEngine);
                        html2markdown(html);
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowLinks",
                        tabPane.getTabs().contains(linksTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(linksTab));
            popMenu.getItems().add(checkMenu);

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"));
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

    /*
        Markdown
     */
    protected void html2markdown(String contents) {
        if (!tabPane.getTabs().contains(markdownTab)
                && !tabPane.getTabs().contains(textTab)
                && !tabPane.getTabs().contains(tocTab)
                && !tabPane.getTabs().contains(linksTab)) {
            return;
        }
        if (contents == null || contents.isEmpty()) {
            markdownArea.setText("");
            tocArea.setText("");
            textArea.setText("");
            linksWebview.getEngine().loadContent("");
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String md, toc, text;
                private Node document;
                private List<Link> links;

                @Override
                protected boolean handle() {
                    try {
                        md = htmlConverter.convert(contents);
                        document = htmlParser.parse(md);
                        if (tabPane.getTabs().contains(tocTab)) {
                            toc = MarkdownTools.toc(document, 8);
                        }
                        if (tabPane.getTabs().contains(linksTab)) {
                            links = new ArrayList<>();
                            MarkdownTools.links(document, links);
                        }
                        if (tabPane.getTabs().contains(textTab)) {
                            // https://github.com/vsch/flexmark-java/blob/master/flexmark-java-samples/src/com/vladsch/flexmark/java/samples/MarkdownToText.java
                            document = textParser.parse(md);
                            text = textCollectingVisitor.collectAndGetText(document);
                        }
                        return md != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(error);
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (tabPane.getTabs().contains(markdownTab)) {
                        Platform.runLater(() -> {
                            markdownArea.setText(md);
                        });
                    }
                    if (tabPane.getTabs().contains(tocTab)) {
                        Platform.runLater(() -> {
                            tocArea.setText(toc);
                        });
                    }
                    if (tabPane.getTabs().contains(linksTab)) {
                        Platform.runLater(() -> {
                            displayLinks(links);
                        });
                    }
                    if (tabPane.getTabs().contains(textTab)) {
                        Platform.runLater(() -> {
                            textArea.setText(text);
                        });
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void markdown2all() {
        String md = markdownArea.getText();
        if (md == null || md.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String html, toc, text;
                private List<Link> links;

                @Override
                protected boolean handle() {
                    try {
                        Node document = htmlParser.parse(md);
                        html = htmlRender.render(document);

                        if (tabPane.getTabs().contains(tocTab)) {
                            toc = MarkdownTools.toc(document, 8);
                        }
                        if (tabPane.getTabs().contains(linksTab)) {
                            links = new ArrayList<>();
                            MarkdownTools.links(document, links);
                        }
                        if (tabPane.getTabs().contains(textTab)) {
                            document = textParser.parse(md);
                            text = textCollectingVisitor.collectAndGetText(document);
                        }
                        return html != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(error);
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    Platform.runLater(() -> {
                        webEngine.loadContent(html);
                    });
                    if (tabPane.getTabs().contains(tocTab)) {
                        Platform.runLater(() -> {
                            tocArea.setText(toc);
                        });
                    }
                    if (tabPane.getTabs().contains(linksTab)) {
                        Platform.runLater(() -> {
                            displayLinks(links);
                        });
                    }
                    if (tabPane.getTabs().contains(textTab)) {
                        Platform.runLater(() -> {
                            textArea.setText(text);
                        });
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void displayLinks(List<Link> links) {
        if (links == null) {
            linksWebview.getEngine().loadContent("");
            return;
        }
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(
                    message("Index"), message("Name"), message("Title"), message("Address")
            ));
            StringTable table = new StringTable(names);
            for (Link link : links) {
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        link.getIndex() >= 0 ? link.getIndex() + "" : "",
                        link.getName() == null ? "" : link.getName(),
                        link.getTitle() == null ? "" : link.getTitle(),
                        link.getAddress() == null ? "" : link.getAddress()
                ));
                table.add(row);
            }
            String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
            String html = HtmlTools.html(null, style, StringTable.tableDiv(table));
            linksWebview.getEngine().loadContent(html);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void editMarkdown() {
        String text = markdownArea.getText();
        if (text.isEmpty()) {
            return;
        }
        MarkdownEditerController controller
                = (MarkdownEditerController) openStage(CommonValues.MarkdownEditorFxml);
        controller.loadMarkdown(text);
    }

    @FXML
    protected void editText() {
        String text = textArea.getText();
        if (text.isEmpty()) {
            return;
        }
        TextEditerController controller
                = (TextEditerController) openStage(CommonValues.TextEditerFxml);
        controller.mainArea.setText(text);
    }

    @FXML
    public void popLinksStyle(MouseEvent mouseEvent) {
        popMenu = FxmlControl.popHtmlStyle(mouseEvent, this, popMenu, linksWebview.getEngine());
    }

    @Override
    public boolean checkBeforeNextAction() {
        String title = getMyStage().getTitle();
        if (!title.endsWith("*")) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setContentText(AppVariables.message("FileChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVariables.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return true;
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

}
