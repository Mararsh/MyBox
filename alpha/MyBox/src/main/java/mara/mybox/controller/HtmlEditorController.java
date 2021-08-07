package mara.mybox.controller;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends BaseWebViewController {

    protected boolean pageLoaded, codesChanged, heChanged, mdChanged, textsChanged, fileChanged;
    protected MutableDataSet htmlOptions;
    protected FlexmarkHtmlConverter htmlConverter;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected String title;

    @FXML
    protected HBox addressBox;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab viewTab, codesTab, editorTab, markdownTab, textsTab, backupTab;
    @FXML
    protected Button synchronizeCodesButton, synchronizeEditorButton, synchronizeMarkdownButton, synchronizeTextsButton,
            popCodesButton, popEditorButton, popMarkdownButton, popTextsButton, popViewButton,
            myBoxClipboardCodesButton, myBoxClipboardMarkdownButton, myBoxClipboardTextButton;
    @FXML
    protected HTMLEditor htmlEditor;
    @FXML
    protected TextArea codesArea, markdownArea, textsArea;
    @FXML
    protected Label codesLabel, editorLabel, markdownLabel, textsLabel;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected CheckBox wrapCodesCheck, wrapMarkdownCheck, wrapTextsCheck;

    public HtmlEditorController() {
        baseTitle = message("HtmlEditor");
        TipsLabelKey = "HtmlEditorTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initTabPane();
            initCodesTab();
            initEdtiorTab();
            initMarkdownTab();
            initTextsTab();
            initBackupsTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initTabPane() {
        try {
            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    TextClipboardPopController.closeAll();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEdtiorTab() {
        try {
            // https://stackoverflow.com/questions/31894239/javafx-htmleditor-how-to-implement-a-changelistener
            // As my testing, only DragEvent.DRAG_EXITED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED working for HtmlEdior
            htmlEditor.setOnDragExited(new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
//                    MyBoxLog.debug("setOnDragExited");
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
            richTextChanged(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initCodesTab() {
        try {
            codesArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!codesArea.isEditable()) {
                        return;
                    }
                    codesChanged(true);
                }
            });

            codesArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuHtmlCodesController.open(myController, codesArea, event);
                }
            });

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
                    markdownChanged(true);
                }
            });

            markdownArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuMarkdownEditController.open(myController, markdownArea, event);
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
            htmlConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initTextsTab() {
        try {
            textsArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!textsArea.isEditable()) {
                        return;
                    }
                    textsChanged(true);
                }
            });

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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            String tips = message("SynchronizeChangesToOtherPanes") + "\nF1";
            NodeStyleTools.setTooltip(synchronizeCodesButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(synchronizeEditorButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(synchronizeMarkdownButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(synchronizeTextsButton, new Tooltip(tips));

            tips = message("Pop") + "\nCTRL+p / ALT+p";
            NodeStyleTools.setTooltip(popCodesButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(popEditorButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(popMarkdownButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(popTextsButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(popViewButton, new Tooltip(tips));

            tips = message("MyBoxClipboard") + "\nCTRL+m / ALT+m";
            NodeStyleTools.setTooltip(myBoxClipboardCodesButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(myBoxClipboardMarkdownButton, new Tooltip(tips));
            NodeStyleTools.setTooltip(myBoxClipboardTextButton, new Tooltip(tips));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        file
     */
    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (sourceFile == null) {
                targetFile = chooseSaveFile();
                if (targetFile == null) {
                    return;
                }
            } else {
                targetFile = sourceFile;
            }
            String html = currentHtml(true);
            if (html == null || html.isBlank()) {
                popError(message("NoData"));
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try {
                        File tmpFile = HtmlWriteTools.writeHtml(html);
                        if (tmpFile == null || !tmpFile.exists()) {
                            return false;
                        }
                        if (sourceFile != null) {
                            if (backupController.isBack()) {
                                backupController.addBackup(sourceFile);
                            }
                        }
                        return FileTools.rename(tmpFile, targetFile);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    if (sourceFile == null) {
                        sourceFile = targetFile;
                        setAddress(sourceFile.toURI().toString());
                    }
                    recordFileWritten(sourceFile);
                    updateFileStatus(false);
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public String currentHtml() {
        return currentHtml(false);
    }

    public String currentHtml(boolean synchronize) {
        try {
            if (framesDoc.isEmpty()) {
                Tab tab = tabPane.getSelectionModel().getSelectedItem();

                if (tab == editorTab) {
                    String html = htmlByEditor();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadWebview(html);
                    }
                    return html;

                } else if (tab == markdownTab) {
                    String html = htmlByMarkdown();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadRichEditor(html, false);
                        loadText(html, false);
                        loadWebview(html);
                    }
                    return html;

                } else if (tab == textsTab) {
                    String html = htmlByText();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadRichEditor(html, false);
                        loadWebview(html);
                    }
                    return html;

                }
            }
            String html = htmlByCodes();
            if (synchronize) {
                loadRichEditor(html, false);
                loadMarkdown(html, false);
                loadText(html, false);
                loadWebview(html);
            }
            return html;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            address = null;
            sourceFile = null;
            addressChanged = false;
            urlSelector.getEditor().setText("");
            webEngine.loadContent(HtmlWriteTools.emptyHmtl());
            loadRichEditor("", false);
            loadHtmlCodes("", false);
            loadMarkdown("", false);
            loadText("", false);
            getMyStage().setTitle(getBaseTitle());
            fileChanged = false;
            backupController.loadBackups(null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void updateTitle() {
        if (myStage == null) {
            return;
        }
        String t = getBaseTitle();
        if (address != null) {
            t += "  " + address;
        } else if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (fileChanged) {
            t += "*";
        }
        myStage.setTitle(t);
    }

    protected void updateFileStatus(boolean changed) {
        fileChanged = changed;
        updateTitle();
        if (!changed) {
            codesChanged(false);
            richTextChanged(false);
            markdownChanged(false);
            textsChanged(false);
            addressChanged = false;
        }
    }


    /*
        webview
     */
    @Override
    protected void pageIsLoading() {
        super.pageIsLoading();
        pageLoaded = false;
        saveButton.setDisable(true);
        saveAsButton.setDisable(true);
        synchronizeCodesButton.setDisable(true);
        synchronizeEditorButton.setDisable(true);
        synchronizeMarkdownButton.setDisable(true);
        synchronizeTextsButton.setDisable(true);
        codesArea.setEditable(false);
        htmlEditor.setDisable(true);
        markdownArea.setEditable(false);
        textsArea.setEditable(false);
        if (addressChanged) {
            String info = address + "\n" + message("Loading...");
            codesArea.setText(info);
            markdownArea.setText(info);
            htmlEditor.setHtmlText(info);
            textsArea.setText(info);
        }
    }

    @Override
    protected void afterPageLoaded() {
        try {
            pageLoaded = true;
            if (addressChanged) {
                fileChanged = false;
                String html = htmlInWebview();
                loadRichEditor(html, false);
                loadHtmlCodes(html, false);
                loadMarkdown(html, false);
                loadText(html, false);
                Platform.runLater(() -> {
                    backupController.loadBackups(sourceFile);
                });
            }
            saveButton.setDisable(false);
            saveAsButton.setDisable(false);
            synchronizeCodesButton.setDisable(false);
            synchronizeEditorButton.setDisable(false);
            synchronizeMarkdownButton.setDisable(false);
            synchronizeTextsButton.setDisable(false);
            codesArea.setEditable(true);
            htmlEditor.setDisable(false);
            markdownArea.setEditable(true);
            textsArea.setEditable(true);
            title = webEngine.getTitle();
            super.afterPageLoaded();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadWebview(String html) {
        Platform.runLater(() -> {
            try {
                webEngine.getLoadWorker().cancel();
                webEngine.loadContent(html);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    public String htmlInWebview() {
        return WebViewTools.getHtml(webEngine);
    }

    @FXML
    public void popViewMenu(MouseEvent mouseEvent) {
        MenuWebviewController.open(myController, null, mouseEvent.getScreenX() + 40, mouseEvent.getScreenY() + 40);
    }

    /*
        codes
     */
    public void loadHtmlCodes(String html, boolean updated) {
        Platform.runLater(() -> {
            try {
                codesArea.setEditable(false);
                codesArea.setText(html);
                if (pageLoaded) {
                    codesArea.setEditable(true);
                }
                codesChanged(updated);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    public String htmlByCodes() {
        return codesArea.getText();
    }

    protected void codesChanged(boolean changed) {
        codesChanged = changed;
        codesTab.setText(message("HtmlCodes") + (changed ? " *" : ""));
        codesLabel.setText(message("Total") + ": " + StringTools.format(codesArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void popCodesMenu(MouseEvent event) {
        MenuHtmlCodesController.open(myController, codesArea, event);
    }

    @FXML
    public void clearCodes() {
        codesArea.clear();
    }

    @FXML
    public void editTextFile() {
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(codesArea.getText());
        controller.toFront();
    }

    @FXML
    public void synchronizeCodes() {
        String html = htmlByCodes();
        loadRichEditor(html, true);
        loadMarkdown(html, true);
        loadText(html, true);
        loadWebview(html);
    }

    /*
        richText
     */
    public void loadRichEditor(String html, boolean updated) {
        Platform.runLater(() -> {
            try {
                htmlEditor.setDisable(true);
                if (StringTools.include(html, "<FRAMESET ", true)) {
                    htmlEditor.setHtmlText("<p>" + message("FrameSetAndSelectFrame") + "</p>");
                } else {
                    htmlEditor.setHtmlText(html);
                }
                if (pageLoaded) {
                    htmlEditor.setDisable(false);
                }
                richTextChanged(updated);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    public String htmlByEditor() {
        return htmlEditor.getHtmlText();
    }

    protected void richTextChanged(boolean changed) {
        heChanged = changed;
        editorTab.setText(message("RichText") + (changed ? " *" : ""));
        String c = htmlByEditor();
        int len = 0;
        if (c != null && !c.isEmpty()) {
            len = c.length();
        }
        editorLabel.setText(message("Total") + ": " + StringTools.format(len));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void popEditorMenu(MouseEvent mouseEvent) {
        MenuWebviewController.pop((BaseWebViewController) (htmlEditor.getUserData()), null,
                mouseEvent.getScreenX() + 40, mouseEvent.getScreenY() + 40);
    }

    @FXML
    public void synchronizeEditor() {
        String html = htmlByEditor();
        loadHtmlCodes(html, true);
        loadMarkdown(html, true);
        loadText(html, true);
        loadWebview(html);
    }

    @FXML
    public void clearEditor() {
        htmlEditor.setHtmlText("");
    }

    /*
        Markdown
     */
    public void loadMarkdown(String html, boolean changed) {
        Platform.runLater(() -> {
            try {
                if (html == null || html.isEmpty()) {
                    markdownArea.setEditable(false);
                    markdownArea.setText("");
                    if (pageLoaded) {
                        markdownArea.setEditable(true);
                    }
                    markdownChanged(changed);
                    return;
                }
                if (StringTools.include(html, "<FRAMESET ", true)) {
                    markdownArea.setEditable(false);
                    markdownArea.setText(message("FrameSetAndSelectFrame"));
                    if (pageLoaded) {
                        markdownArea.setEditable(true);
                    }
                    markdownChanged(changed);
                    return;
                }
                String md = htmlConverter.convert(html);
                markdownArea.setEditable(false);
                markdownArea.setText(md);
                if (pageLoaded) {
                    markdownArea.setEditable(true);
                }
                markdownChanged(changed);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    public String htmlByMarkdown() {
        Node document = htmlParser.parse(markdownArea.getText());
        return HtmlWriteTools.html(title, htmlRender.render(document));
    }

    protected void markdownChanged(boolean changed) {
        mdChanged = changed;
        markdownTab.setText("Markdown" + (changed ? " *" : ""));
        markdownLabel.setText(message("Total") + ": " + StringTools.format(markdownArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    protected void editMarkdown() {
        MarkdownEditorController controller = (MarkdownEditorController) openStage(Fxmls.MarkdownEditorFxml);
        controller.loadMarkdown(markdownArea.getText());
    }

    @FXML
    protected void clearMarkdown() {
        markdownArea.clear();
    }

    @FXML
    public void popMarkdownMenu(MouseEvent mouseEvent) {
        MenuMarkdownEditController.open(myController, markdownArea, mouseEvent);
    }

    @FXML
    public void synchronizeMarkdown() {
        String html = htmlByMarkdown();
        loadRichEditor(html, true);
        loadHtmlCodes(html, true);
        loadText(html, true);
        loadWebview(html);
    }

    /*
        texts
     */
    public void loadText(String html, boolean updated) {
        Platform.runLater(() -> {
            try {
                textsArea.setEditable(false);
                textsArea.setText(HtmlWriteTools.htmlToText(html));
                if (pageLoaded) {
                    textsArea.setEditable(true);
                }
                textsChanged(updated);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    public String htmlByText() {
        String body = HtmlWriteTools.stringToHtml(textsArea.getText());
        return HtmlWriteTools.html(title, body);
    }

    protected void textsChanged(boolean changed) {
        textsChanged = changed;
        textsTab.setText(message("Texts") + (changed ? " *" : ""));
        textsLabel.setText(message("Total") + ": " + StringTools.format(textsArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    protected void editTexts() {
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textsArea.getText());
        controller.toFront();
    }

    @FXML
    protected void clearTexts() {
        textsArea.clear();
    }

    @FXML
    public void popTextsMenu(MouseEvent mouseEvent) {
        MenuTextEditController.open(myController, textsArea, mouseEvent);
    }

    @FXML
    public void synchronizeTexts() {
        String html = htmlByText();
        loadRichEditor(html, true);
        loadHtmlCodes(html, true);
        loadMarkdown(html, true);
        loadWebview(html);
    }

    /*
        buttons
     */
    @Override
    public boolean controlAltP() {
        popAction();
        return true;
    }

    @FXML
    @Override
    public void popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
                HtmlPopController.open(this, htmlInWebview());

            } else if (tab == markdownTab) {
                TextPopController.open(this, markdownArea.getText());

            } else if (tab == codesTab) {
                TextPopController.open(this, codesArea.getText());

            } else if (tab == editorTab) {
                HtmlPopController.open(this, htmlByEditor());

            } else if (tab == textsTab) {
                TextPopController.open(this, textsArea.getText());

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == markdownTab) {
                TextClipboardPopController.open(this, markdownArea);

            } else if (tab == codesTab) {
                TextClipboardPopController.open(this, codesArea);

            } else if (tab == textsTab) {
                TextClipboardPopController.open(this, textsArea);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean keyF1() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == markdownTab) {
                Platform.runLater(() -> {
                    synchronizeMarkdown();
                });
                return true;

            } else if (tab == codesTab) {
                Platform.runLater(() -> {
                    synchronizeCodes();
                });
                return true;

            } else if (tab == editorTab) {
                Platform.runLater(() -> {
                    synchronizeEditor();
                });
                return true;

            } else if (tab == textsTab) {
                Platform.runLater(() -> {
                    synchronizeTexts();
                });
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (isPop || !fileChanged) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("FileChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    @Override
    public void cleanPane() {
        htmlEditor.setUserData(null);
        super.cleanPane();
    }
}
