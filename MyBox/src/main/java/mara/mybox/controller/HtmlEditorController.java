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
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
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
    protected MutableDataSet htmlOptions;
    protected FlexmarkHtmlConverter htmlConverter;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;

    @FXML
    protected Button synchronizeMainButton, synchronizePairButton, editMarkdownButton, styleLinksButton;
    @FXML
    protected HTMLEditor htmlEditor;
    @FXML
    protected TextArea markdownArea;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editorTab, codesTab, markdownTab, backupTab;
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
    public void initControls() {
        try {
            super.initControls();
            initEdtior();
            initTabPane();
            initCodesTab();
            initMarkdownTab();
            initBackupsTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initWebView() {
        try {

            super.initWebView();
            webviewController.stateNotify.addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    int intValue = (int) newValue;
                    if (intValue == ControlWebview.TmpState) {
                        return;
                    }
                    if (intValue == ControlWebview.DocLoading) {
                        pageIsLoading();
                    } else if (intValue == ControlWebview.DocLoaded) {
                        afterPageLoaded();
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
            editorTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowEditor", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowCodes", true)) {
                tabPane.getTabs().remove(codesTab);
            }
            codesTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowCodes", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowMarkdown", true)) {
                tabPane.getTabs().remove(markdownTab);
            }
            markdownTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowMarkdown", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowBackup", true)) {
                tabPane.getTabs().remove(backupTab);
            }
            backupTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowBackup", false);
                }
            });

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    synchronizePairButton.setVisible(
                            newValue == editorTab || newValue == codesTab || newValue == markdownTab);
//                    Platform.runLater(() -> {
//                        if (oldValue == codesTab) {
//                            String html = codesController.codes();
//                            htmlEditor.setHtmlText(html);
//                            markdown(html);
//
//                        } else if (oldValue == editorTab) {
//                            String html = htmlEditor.getHtmlText();
//                            codes(html);
//                            markdown(html);
//
//                        } else if (oldValue == markdownTab) {
//                            Node document = htmlParser.parse(markdownArea.getText());
//                            String html = htmlRender.render(document);
//                            htmlEditor.setHtmlText(html);
//                            codes(html);
//                        }
//                    });

                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEdtior() {
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
//            Node[] nodes = htmlEditor.lookupAll(".tool-bar").toArray(new Node[0]);
//            for (Node node : nodes) {
//                node.setVisible(false);
//                node.setManaged(false);
//            }
//
//            WebView webv = (WebView) htmlEditor.lookup("WebView");
//            webv.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
//                @Override
//                public void changed(ObservableValue ov, Document oldValue, Document newValue) {
//                    MyBoxLog.debug("documentProperty");
//                    checkEditorChanged();
//                }
//            });
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
            codesController.setParameters(this);
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
            htmlConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

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
        markdownArea.setText(info);
        htmlEditor.setHtmlText(info);

    }

    private void afterPageLoaded() {
        pageLoaded = true;
        synchronizeMain();
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        synchronizePairButton.setDisable(false);
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
        try {

            if (tabPane.getTabs().contains(editorTab)) {
                Platform.runLater(() -> {
                    richText(FxmlControl.getHtml(webEngine));
                });
            }
            if (tabPane.getTabs().contains(codesTab)) {
                Platform.runLater(() -> {
                    codes(FxmlControl.getHtml(webEngine));
                });
            }
            if (tabPane.getTabs().contains(markdownTab)) {
                Platform.runLater(() -> {
                    markdown(FxmlControl.getHtml(webEngine));
                });
            }
            if (tabPane.getTabs().contains(backupTab)) {
                Platform.runLater(() -> {
                    backupController.loadBackups(sourceFile);
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

    public String currentUpdate() {
        try {
            if (webviewController.frameDoc.isEmpty()) {
                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                if (tab == editorTab) {
                    return htmlEditor.getHtmlText();

                } else if (tab == markdownTab) {
                    Node document = htmlParser.parse(markdownArea.getText());
                    return HtmlTools.html(null, htmlRender.render(document));
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return codesController.codes();
        }
        return codesController.codes();
    }

    @FXML
    public void synchronizePair() {
        String html = currentUpdate();
        if (html != null) {
            String web = html;
            Platform.runLater(() -> {
                webEngine.loadContent(web);
            });
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
            String html = currentUpdate();
            if (html == null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try {
                        Charset charset = HtmlTools.htmlCharset(html);
                        File tmpFile = FileTools.getTempFile();
                        try ( BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile, charset, false))) {
                            out.write(html);
                            out.flush();
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                        if (sourceFile != null && file.getAbsolutePath().equals(sourceFile.getAbsolutePath())) {
                            if (backupController.isBack()) {
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
                        webEngine.loadContent(html);
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
            checkMenu.setSelected(tabPane.getTabs().contains(editorTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(editorTab);
                if (c) {
                    tabPane.getTabs().remove(editorTab);
                } else {
                    tabPane.getTabs().add(editorTab);
                    Platform.runLater(() -> {
                        richText(FxmlControl.getHtml(webEngine));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowEditor", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem("Markdown");
            checkMenu.setSelected(tabPane.getTabs().contains(markdownTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(markdownTab);
                if (c) {
                    tabPane.getTabs().remove(markdownTab);
                } else {
                    tabPane.getTabs().add(markdownTab);
                    Platform.runLater(() -> {
                        markdown(FxmlControl.getHtml(webEngine));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowMarkdown", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("HtmlCodes"));
            checkMenu.setSelected(tabPane.getTabs().contains(codesTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(codesTab);
                if (c) {
                    tabPane.getTabs().remove(codesTab);
                } else {
                    tabPane.getTabs().add(codesTab);
                    Platform.runLater(() -> {
                        codes(FxmlControl.getHtml(webEngine));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowCodes", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Backup"));
            checkMenu.setSelected(tabPane.getTabs().contains(backupTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(backupTab);
                if (c) {
                    tabPane.getTabs().remove(backupTab);
                } else {
                    tabPane.getTabs().add(backupTab);
                    Platform.runLater(() -> {
                        markdown(FxmlControl.getHtml(webEngine));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowBackup", !c);
            });
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
        codes
     */
    protected void codes(String html) {
        codesChanged = false;
        if (!tabPane.getTabs().contains(codesTab)) {
            return;
        }
        codesController.codesArea.setEditable(false);
        codesController.load(html);
        if (pageLoaded) {
            codesController.codesArea.setEditable(true);
        }
    }

    /*
        richText
     */
    protected void richText(String html) {
        heChanged = false;
        if (!tabPane.getTabs().contains(editorTab)) {
            return;
        }
        htmlEditor.setDisable(true);
        if (StringTools.include(html, "<FRAMESET ", true)) {
            htmlEditor.setHtmlText("<p>" + message("FrameSetAndSelectFrame") + "</p>");
        } else {
            htmlEditor.setHtmlText(html);
            if (pageLoaded) {
                htmlEditor.setDisable(false);
            }
        }
    }

    /*
        Markdown
     */
    protected void markdown(String html) {
        mdChanged = false;
        if (!tabPane.getTabs().contains(markdownTab)) {
            return;
        }
        if (html == null || html.isEmpty()) {
            markdownArea.setText("");
            if (pageLoaded) {
                markdownArea.setEditable(true);
            }
            return;
        }
        if (StringTools.include(html, "<FRAMESET ", true)) {
            markdownArea.setText(message("FrameSetAndSelectFrame"));
            mdChanged = false;
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String md;

                @Override
                protected boolean handle() {
                    try {
                        md = htmlConverter.convert(html);
                        return md != null;
                    } catch (Exception e) {
                        MyBoxLog.error(e);
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
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
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
