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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
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
public class HtmlEditorController extends BaseHtmlController {

    protected boolean pageLoaded, codesChanged, heChanged, mdChanged, fileChanged;
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
    protected TextArea tocArea, markdownArea, textArea;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editorTab, codesTab, tocTab, markdownTab, textTab, linksTab, backupTab;
    @FXML
    protected Label editorLabel, mdLabel, codesLabel;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected ControlHtmlCodes codesController;

    public HtmlEditorController() {
        baseTitle = AppVariables.message("HtmlEditor");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            needSnap = true;
            needEdit = false;
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
            initBackupsTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initWebView() {
        try {
            super.initWebView();
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                    switch (newState) {
                        case RUNNING:
                            pageIsLoading();
                            break;
                        case SUCCEEDED:
                        case CANCELLED:
                        case FAILED:
                            afterPageLoaded();
                            break;
                    }
                }
            });

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
            if (!AppVariables.getUserConfigBoolean(baseName + "ShowBackup", true)) {
                tabPane.getTabs().remove(backupTab);
            }

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    synchronizePairButton.setVisible(
                            newValue == editorTab || newValue == codesTab || newValue == markdownTab);
                    synchronizeTab(oldValue);
                }
            });

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
            if (htmlEditor.isDisabled()) {
                return;
            }
            String c = htmlEditor.getHtmlText();
            int len = 0;
            if (c != null && !c.isEmpty()) {
                len = htmlEditor.getHtmlText().length();
            }
            editorLabel.setText(AppVariables.message("Total") + ": " + len);
            heChanged = true;
            updateTitle(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initCodesTab() {
        try {
            codesController.setValues(this);
            codesController.codesArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!codesController.codesArea.isEditable()) {
                        return;
                    }
                    codesLabel.setText(AppVariables.message("Total") + ": " + codesController.codesArea.getLength());
                    codesChanged = true;
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
                    if (!markdownArea.isEditable()) {
                        return;
                    }
                    mdLabel.setText(AppVariables.message("Total") + ": " + markdownArea.getLength());
                    mdChanged = true;
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

    protected void initBackupsTab() {
        try {
            backupController.setControls(this, baseName);

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

    private void loadEditor(String html) {
        if (StringTools.include(html, "<FRAMESET>", true)) {
            htmlEditor.setHtmlText("<p>" + message("NotSupportFrameSet") + "</p>");
        } else {
            htmlEditor.setHtmlText(html);
        }
    }

    private void pageIsLoading() {
        pageLoaded = false;
        codesChanged = heChanged = mdChanged = false;
        saveButton.setDisable(true);
        saveAsButton.setDisable(true);
        synchronizePairButton.setDisable(true);
        htmlEditor.setDisable(true);
        markdownArea.setEditable(false);
        codesController.codesArea.setEditable(false);
        String info = webviewController.address + "\n" + message("Loading...");
        codesController.load(info);
        tocArea.setText(info);
        textArea.setText(info);
        linksWebview.getEngine().getLoadWorker().cancel();
        linksWebview.getEngine().loadContent(info);
        markdownArea.setText(info);
        htmlEditor.setHtmlText(info);
    }

    private void afterPageLoaded() {
        pageLoaded = true;
        synchronizeMain();
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        synchronizePairButton.setDisable(false);
        backupController.loadBackups(sourceFile);
    }

    @Override
    protected void updateTitle(boolean changed) {
        String t = getBaseTitle();
        if (webviewController.address != null) {
            t += "  " + webviewController.address;
        } else if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (changed) {
            t += "*";
        }
        getMyStage().setTitle(t);
        fileChanged = changed;
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
            String html = FxmlControl.getHtml(webEngine);
            if (tabPane.getTabs().contains(editorTab)) {
                Platform.runLater(() -> {
                    htmlEditor.setDisable(true);
                    loadEditor(html);
                    if (pageLoaded) {
                        htmlEditor.setDisable(false);
                    }
                    heChanged = false;
                });
            }
            if (tabPane.getTabs().contains(codesTab)) {
                Platform.runLater(() -> {
                    codesController.codesArea.setEditable(false);
                    codesController.load(html);
                    if (pageLoaded) {
                        codesController.codesArea.setEditable(true);
                    }
                    codesChanged = false;
                });
            }
            if (tabPane.getTabs().contains(markdownTab)
                    || tabPane.getTabs().contains(textTab)
                    || tabPane.getTabs().contains(tocTab)
                    || tabPane.getTabs().contains(linksTab)) {
                Platform.runLater(() -> {
                    markdownArea.setEditable(false);
                    html2markdown(html);

                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void refreshPair() {
        synchronizeMain();
    }

    @FXML
    public void synchronizePair() {
        synchronizeTab(tabPane.getSelectionModel().getSelectedItem());
    }

    public void synchronizeTab(Tab tab) {
        try {
            if (tab == null) {
                return;
            }
            String html = null;
            if (tab == codesTab && codesChanged) {
                html = codesController.codes();

            } else if (tab == editorTab && heChanged) {
                html = htmlEditor.getHtmlText();
                if (StringTools.include(html, "<FRAMESET>", true)) {
                    return;
                }

            } else if (tab == markdownTab && mdChanged) {
                Node document = htmlParser.parse(markdownArea.getText());
                html = htmlRender.render(document);
            }

            if (html != null) {
                String web = html;
                Platform.runLater(() -> {
                    webEngine.loadContent(web);
                });
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
                        "myWeb", targetExtensionFilter);
                if (file == null) {
                    return;
                }
                sourceFile = file;
            }
            saveFile(sourceFile);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        String name = "myWeb";
        if (sourceFile != null) {
            name = FileTools.appendName(sourceFile.getName(), "m");
        }
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        saveFile(file);
    }

    public void saveFile(File file) {
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String html, fhtml;
            if (codesChanged) {
                html = codesController.codes();
            } else if (heChanged) {
                html = htmlEditor.getHtmlText();
                if (StringTools.include(html, "<FRAMESET>", true)) {
                    html = FxmlControl.getHtml(webEngine);
                }
            } else if (mdChanged) {
                Node document = htmlParser.parse(markdownArea.getText());
                html = htmlRender.render(document);
            } else {
                html = FxmlControl.getHtml(webEngine);
            }
            if (html == null) {
                return;
            }
            fhtml = html;
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try {
                        Charset charset = HtmlTools.htmlCharset(fhtml);
                        File tmpFile = FileTools.getTempFile();
                        try ( BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile, charset, false))) {
                            out.write(fhtml);
                            out.flush();
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                        if (sourceFile != null && file.getAbsolutePath().equals(sourceFile.getAbsolutePath())) {
                            if (backupController.backupCheck.isSelected()) {
                                backupController.addBackup(sourceFile);
                            }
                        }
                        if (FileTools.rename(tmpFile, file)) {
                            recordFileWritten(file);
                            return true;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    if (sourceFile != null && file.getAbsolutePath().equals(sourceFile.getAbsolutePath())) {
                        loadFile(sourceFile);
                    } else {
                        webEngine.loadContent(fhtml);
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

    @FXML
    @Override
    public void createAction() {
        try {
            sourceFile = null;
            webviewController.loadContents(HtmlTools.emptyHmtl());
            getMyStage().setTitle(getBaseTitle());
            fileChanged = codesChanged = heChanged = mdChanged = false;
            updateTitle(false);
            saveButton.setDisable(false);
            saveAsButton.setDisable(false);
            synchronizePairButton.setDisable(false);
            backupController.loadBackups(null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                        String html = FxmlControl.getHtml(webEngine);
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
                        String html = FxmlControl.getHtml(webEngine);
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
                        String html = FxmlControl.getHtml(webEngine);
                        codesController.load(html);
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
                        String html = FxmlControl.getHtml(webEngine);
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
                        String html = FxmlControl.getHtml(webEngine);
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
                        String html = FxmlControl.getHtml(webEngine);
                        html2markdown(html);
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowLinks",
                        tabPane.getTabs().contains(linksTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(linksTab));
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Backup"));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(backupTab)) {
                    tabPane.getTabs().remove(backupTab);
                } else {
                    tabPane.getTabs().add(backupTab);
                }
                AppVariables.setUserConfigValue(baseName + "ShowBackup",
                        tabPane.getTabs().contains(backupTab));
            });
            checkMenu.setSelected(tabPane.getTabs().contains(backupTab));
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
            if (pageLoaded) {
                markdownArea.setEditable(true);
            }
            mdChanged = false;
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
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (tabPane.getTabs().contains(markdownTab)) {
                        Platform.runLater(() -> {
                            markdownArea.setText(md);
                            if (pageLoaded) {
                                markdownArea.setEditable(true);
                            }
                            mdChanged = false;
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
