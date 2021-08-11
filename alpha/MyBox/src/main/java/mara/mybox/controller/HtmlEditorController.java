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
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
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
            if (backupController != null) {
                backupController.setControls(this, baseName);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                            if (backupController != null && backupController.isBack()) {
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

                if (tab == viewTab) {
                    String html = htmlInWebview();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadRichEditor(html, false);
                    }
                    return html;

                } else if (tab == editorTab) {
                    String html = htmlByEditor();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadView(html, false);
                    }
                    return html;

                } else if (tab == markdownTab) {
                    String html = htmlByMarkdown();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadRichEditor(html, false);
                        loadText(html, false);
                        loadView(html, false);
                    }
                    return html;

                } else if (tab == textsTab) {
                    String html = htmlByText();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadRichEditor(html, false);
                        loadView(html, false);
                    }
                    return html;

                }
            }
            String html = htmlByCodes();
            if (synchronize) {
                loadRichEditor(html, false);
                loadMarkdown(html, false);
                loadText(html, false);
                loadView(html, false);
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
            if (backupController != null) {
                backupController.loadBackups(null);
            }
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
            viewChanged(false);
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
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
        if (saveAsButton != null) {
            saveAsButton.setDisable(true);
        }
        if (synchronizeButton != null) {
            synchronizeButton.setDisable(true);
        }
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
                    if (backupController != null) {
                        viewChanged(false);
                        backupController.loadBackups(sourceFile);
                    }
                });
            }
            if (saveButton != null) {
                saveButton.setDisable(false);
            }
            if (saveAsButton != null) {
                saveAsButton.setDisable(false);
            }
            if (synchronizeButton != null) {
                synchronizeButton.setDisable(false);
            }
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

    public void loadView(String html, boolean updated) {
        Platform.runLater(() -> {
            try {
                webEngine.getLoadWorker().cancel();
                webEngine.loadContent(html);
                viewChanged(updated);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    protected void viewChanged(boolean changed) {
        codesChanged = changed;
        viewTab.setText(message("View") + (fileChanged ? " *" : ""));
        if (changed) {
            updateFileStatus(true);
        }
    }

    public String htmlInWebview() {
        return WebViewTools.getHtml(webEngine);
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
    public void clearCodes() {
        codesArea.clear();
    }

    @FXML
    public void editTextFile() {
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(codesArea.getText());
        controller.toFront();
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


    /*
        buttons
     */
    @Override
    public boolean keyF9() {
        synchronizeAction();
        return true;
    }

    @Override
    public boolean keyF12() {
        menuAction();
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
                MarkdownPopController.open(this, markdownArea.getText());

            } else if (tab == codesTab) {
                HtmlCodesPopController.open(this, codesArea.getText());

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
    public void synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
                Platform.runLater(() -> {
                    String html = htmlInWebview();
                    loadHtmlCodes(html, true);
                    loadRichEditor(html, true);
                    loadMarkdown(html, true);
                    loadText(html, true);
                });

            } else if (tab == codesTab) {
                Platform.runLater(() -> {
                    String html = htmlByCodes();
                    loadRichEditor(html, true);
                    loadMarkdown(html, true);
                    loadText(html, true);
                    loadView(html, true);
                });

            } else if (tab == editorTab) {
                Platform.runLater(() -> {
                    String html = htmlByEditor();
                    loadHtmlCodes(html, true);
                    loadMarkdown(html, true);
                    loadText(html, true);
                    loadView(html, true);
                });

            } else if (tab == markdownTab) {
                Platform.runLater(() -> {
                    String html = htmlByMarkdown();
                    loadRichEditor(html, true);
                    loadHtmlCodes(html, true);
                    loadText(html, true);
                    loadView(html, true);
                });

            } else if (tab == textsTab) {
                Platform.runLater(() -> {
                    String html = htmlByText();
                    loadRichEditor(html, true);
                    loadHtmlCodes(html, true);
                    loadMarkdown(html, true);
                    loadView(html, true);
                });

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
                Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
                MenuWebviewController.pop(this, webView, null, localToScreen.getX(), localToScreen.getY());

            } else if (tab == codesTab) {
                Point2D localToScreen = codesArea.localToScreen(codesArea.getWidth() - 80, 80);
                MenuHtmlCodesController.open(this, codesArea, localToScreen.getX(), localToScreen.getY());

            } else if (tab == editorTab) {
                Point2D localToScreen = htmlEditor.localToScreen(htmlEditor.getWidth() - 80, 80);
                MenuWebviewController.pop((BaseWebViewController) (htmlEditor.getUserData()), null, localToScreen.getX(), localToScreen.getY());

            } else if (tab == markdownTab) {
                Point2D localToScreen = markdownArea.localToScreen(markdownArea.getWidth() - 80, 80);
                MenuMarkdownEditController.open(this, markdownArea, localToScreen.getX(), localToScreen.getY());

            } else if (tab == textsTab) {
                Point2D localToScreen = textsArea.localToScreen(textsArea.getWidth() - 80, 80);
                MenuTextEditController.open(this, textsArea, localToScreen.getX(), localToScreen.getY());

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
        try {
            htmlEditor.setUserData(null);
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
