package mara.mybox.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class ControlHtmlEditor extends BaseWebViewController {

    protected boolean codesChanged, domChanged, richChanged,
            mdChanged, textsChanged, fileChanged;
    protected MutableDataHolder htmlOptions;
    protected FlexmarkHtmlConverter htmlConverter;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected String title;
    protected final SimpleBooleanProperty loadNotify;

    protected final ButtonType buttonClose = new ButtonType(message("Close"));
    protected final ButtonType buttonSynchronize = new ButtonType(message("SynchronizeAndClose"));
    protected final ButtonType buttonCancel = new ButtonType(message("Cancel"));

    @FXML
    protected Tab viewTab, domTab, codesTab, richEditorTab, markdownTab, textsTab, backupTab;
    @FXML
    protected ControlHtmlRichEditor richEditorController;
    @FXML
    protected TextArea codesArea, markdownArea, textsArea;
    @FXML
    protected Label codesLabel, markdownLabel, textsLabel;
    @FXML
    protected ControlHtmlDomManage domController;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected CheckBox wrapCodesCheck, wrapMarkdownCheck, wrapTextsCheck;
    @FXML
    protected Button menuViewButton, synchronizeViewButton, popViewButton;

    public ControlHtmlEditor() {
        baseTitle = message("HtmlEditor");
        TipsLabelKey = "HtmlEditorTips";
        loadNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            domController.setEditor(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initTabPane();
            initCodesTab();
            initRichEdtiorTab();
            initMarkdownTab();
            initTextsTab();
            initBackupsTab();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initTabPane() {
        try {
            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    tabChanged();
                }
            });
            tabChanged();
            showTabs();

            NodeStyleTools.refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initRichEdtiorTab() {
        try {
            richEditorController.textChanged.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    richEditorChanged(true);
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void initCodesTab() {
        try {
            codesArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    codesChanged(true);
                }
            });

            codesArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuHtmlCodesController.htmlMenu(myController, codesArea, event);
                }
            });

            wrapCodesCheck.setSelected(UserConfig.getBoolean(baseName + "WrapCodes", true));
            codesArea.setWrapText(wrapCodesCheck.isSelected());
            wrapCodesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapCodes", wrapCodesCheck.isSelected());
                    codesArea.setWrapText(wrapCodesCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initMarkdownTab() {
        try {
            markdownArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    markdownChanged(true);
                }
            });

            markdownArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuMarkdownEditController.mdMenu(myController, markdownArea, event);
                }
            });

            wrapMarkdownCheck.setSelected(UserConfig.getBoolean(baseName + "WrapMarkdown", true));
            markdownArea.setWrapText(wrapMarkdownCheck.isSelected());
            wrapMarkdownCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapMarkdown", wrapMarkdownCheck.isSelected());
                    markdownArea.setWrapText(wrapMarkdownCheck.isSelected());
                }
            });

            htmlOptions = MarkdownTools.htmlOptions();
            htmlConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initTextsTab() {
        try {
            textsArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    textsChanged(true);
                }
            });

            wrapTextsCheck.setSelected(UserConfig.getBoolean(baseName + "WrapText", true));
            textsArea.setWrapText(wrapTextsCheck.isSelected());
            wrapTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapText", wrapTextsCheck.isSelected());
                    textsArea.setWrapText(wrapTextsCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initBackupsTab() {
        try {
            if (backupController != null) {
                backupController.setParameters(this, baseName);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            if (!tabPane.getTabs().contains(viewTab)) {
                NodeStyleTools.setTooltip(menuViewButton, message("ContextMenu"));
                NodeStyleTools.setTooltip(synchronizeViewButton, message("SynchronizeChangesToOtherPanes"));
                NodeStyleTools.setTooltip(popViewButton, message("Pop"));
            } else {
                NodeStyleTools.setTooltip(menuViewButton, message("ContextMenuTips"));
            }
            NodeStyleTools.setTooltip(menuButton, message("ContextMenuTips"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }


    /*
        source
     */
    @Override
    public boolean loadFile(File file) {
        if (!super.loadFile(file)) {
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    html = TextFileTools.readTexts(this, file);
                    return html != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                writePanes(html);
            }

        };
        start(task);
        return true;
    }

    @Override
    public boolean loadAddress(String address) {
        if (!super.loadAddress(address)) {
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    html = HtmlReadTools.url2html(this, address);
                    return html != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                writePanes(html);
            }

        };
        start(task);
        return true;
    }

    @Override
    public boolean loadContents(String contents) {
        if (!super.loadContents(contents)) {
            return false;
        }
        return writePanes(contents);
    }

    @Override
    public boolean loadContents(String address, String contents) {
        if (!super.loadContents(address, contents)) {
            return false;
        }
        return writePanes(contents);
    }

    public boolean writePanes(String html) {
        fileChanged = false;
        sourceFile = webViewController.sourceFile;

        isSettingValues = true;
        loadRichEditor(html, false);
        loadHtmlCodes(html, false);
        loadDom(html, false);
        loadMarkdown(html, false);
        loadText(html, false);
        isSettingValues = false;
        viewChanged(false);
        if (backupController != null) {
            backupController.loadBackups(sourceFile);
        }
        if (browseController != null) {
            browseController.setCurrentFile(sourceFile);
        }
        loadNotify.set(!loadNotify.get());
        return true;
    }

    @FXML
    @Override
    public void refreshAction() {
        fileChanged = false;
        if (webViewController.address != null) {
            loadAddress(webViewController.address);
        } else if (webViewController.contents != null) {
            loadContents(webViewController.contents);
        }
    }

    @Override
    public void selectSourceFile(File file) {
        loadFile(file);
    }

    /*
        file
     */
    @FXML
    @Override
    public void saveAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (sourceFile == null) {
            targetFile = chooseSaveFile();
        } else {
            targetFile = sourceFile;
        }
        if (targetFile == null) {
            return;
        }
        String html = currentHtml(true);
        if (html == null || html.isBlank()) {
            popError(message("NoData"));
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    File tmpFile = HtmlWriteTools.writeHtml(html);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    if (sourceFile != null && backupController != null && backupController.needBackup()) {
                        backupController.addBackup(this, sourceFile);
                    }
                    return FileTools.override(tmpFile, targetFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(targetFile);
                fileChanged = false;
                loadFile(targetFile);
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void saveAsAction() {
        webViewController.saveAs(currentHtml(true));
    }

    public String currentHtml(boolean synchronize) {
        try {
            if (webViewController.framesDoc.isEmpty()) {
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

                if (currentTab == viewTab) {
                    String html = htmlInWebview();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadDom(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadRichEditor(html, false);

                    }
                    return html;

                } else if (currentTab == domTab) {
                    String html = htmlByDom();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadView(html, false);
                        loadRichEditor(html, false);
                    }
                    return html;

                } else if (currentTab == richEditorTab) {
                    String html = htmlByRichEditor();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadDom(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadView(html, false);
                    }
                    return html;

                } else if (currentTab == markdownTab) {
                    String html = htmlByMarkdown();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadDom(html, false);
                        loadRichEditor(html, false);
                        loadText(html, false);
                        loadView(html, false);
                    }
                    return html;

                } else if (currentTab == textsTab) {
                    String html = htmlByText();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadDom(html, false);
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
                loadDom(html, false);
                loadMarkdown(html, false);
                loadText(html, false);
                loadView(html, false);
            }
            return html;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return null;
    }

    public boolean create() {
        try {
            if (!checkBeforeNextAction()) {
                return false;
            }
            loadContents(HtmlWriteTools.emptyHmtl(null));
            getMyStage().setTitle(getBaseTitle());
            fileChanged = false;
            if (backupController != null) {
                backupController.loadBackups(null);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void updateStageTitle() {
        if (getMyStage() == null) {
            return;
        }
        String t = title();
        if (fileChanged) {
            t += " *";
        }
        myStage.setTitle(t);
    }

    protected void updateFileStatus(boolean changed) {
        fileChanged = changed;
        updateStageTitle();
        if (!changed) {
            viewChanged(false);
            domChanged(false);
            codesChanged(false);
            richEditorChanged(false);
            markdownChanged(false);
            textsChanged(false);
        }
    }

    /*
        webview
     */
    public void loadView(String html, boolean updated) {
        try {
            webViewController.writeContents(html);
            viewChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void viewChanged(boolean changed) {
        viewTab.setText(message("View") + (fileChanged ? " *" : ""));
        if (changed) {
            updateFileStatus(true);
        } else {
            updateStageTitle();
        }
    }

    public String htmlInWebview() {
        return webViewController.currentHtml();
    }

    /*
        codes
     */
    public void loadHtmlCodes(String html, boolean updated) {
        if (!tabPane.getTabs().contains(codesTab)) {
            return;
        }
        try {
            codesArea.setText(htmlCodes(html));
            codesChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlByCodes() {
        return codesArea.getText();
    }

    protected void codesChanged(boolean changed) {
        codesChanged = changed;
        codesTab.setText(message("HtmlCodes") + (changed ? " *" : ""));
        codesLabel.setText(message("CharactersNumber") + ": " + StringTools.format(codesArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void clearCodes() {
        codesArea.clear();
        codesChanged(true);
    }

    @FXML
    public void editTextFile() {
        TextEditorController.edit(codesArea.getText());
    }

    public String htmlCodes(String html) {
        return html;
    }

    public void pasteText(String text) {
        if (!tabPane.getTabs().contains(codesTab) || text == null || text.isEmpty()) {
            return;
        }
        tabPane.getSelectionModel().select(codesTab);
        codesArea.replaceText(codesArea.getSelection(), text);
        codesArea.requestFocus();
        codesChanged(true);
    }

    /*
        dom
     */
    public void loadDom(String html, boolean updated) {
        if (!tabPane.getTabs().contains(domTab)) {
            return;
        }
        domController.loadHtml(html);
        domChanged(updated);
    }

    public String htmlByDom() {
        return domController.html();
    }

    public void domChanged(boolean changed) {
        domChanged = changed;
        domTab.setText(message("Tree") + (changed ? " *" : ""));
        if (changed) {
            updateFileStatus(true);
        }
    }

    public void updateNode(TreeItem<HtmlNode> item) {
        domChanged(true);
    }

    public void clearDom() {
        domController.clearTree();
        domChanged(true);
    }

    /*
        rich editor
     */
    public void loadRichEditor(String html, boolean updated) {
        if (!tabPane.getTabs().contains(richEditorTab)) {
            return;
        }
        try {
            String contents = html;
            if (StringTools.include(html, "<FRAMESET ", true)) {
                contents = "<p>" + message("FrameSetAndSelectFrame") + "</p>";
            }
            richEditorController.loadContents(contents);
            richEditorChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlByRichEditor() {
        return richEditorController.getContents();
    }

    protected void richEditorChanged(boolean changed) {
        richChanged = changed;
        richEditorTab.setText(message("RichText") + (changed ? " *" : ""));
        String c = htmlByRichEditor();
        int len = 0;
        if (c != null && !c.isEmpty()) {
            len = c.length();
        }
        richEditorController.setLabel(message("CharactersNumber") + ": " + StringTools.format(len));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void clearRichEditor() {
        richEditorController.loadContents(null);
        richEditorChanged(true);
    }

    /*
        Markdown
     */
    public void loadMarkdown(String html, boolean changed) {
        if (!tabPane.getTabs().contains(markdownTab)) {
            return;
        }
        try {
            String md;
            if (html == null || html.isEmpty()) {
                md = html;
            } else if (StringTools.include(html, "<FRAMESET ", true)) {
                md = message("FrameSetAndSelectFrame");
            } else {
                md = htmlConverter.convert(html);
            }
            markdownArea.setText(md);
            markdownChanged(changed);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlByMarkdown() {
        Node document = htmlParser.parse(markdownArea.getText());
        return HtmlWriteTools.html(title, htmlRender.render(document));
    }

    protected void markdownChanged(boolean changed) {
        mdChanged = changed;
        markdownTab.setText("Markdown" + (changed ? " *" : ""));
        markdownLabel.setText(message("CharactersNumber") + ": " + StringTools.format(markdownArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    protected void editMarkdown() {
        MarkdownEditorController.edit(markdownArea.getText());
    }

    @FXML
    protected void clearMarkdown() {
        markdownArea.clear();
        markdownChanged(true);
    }

    /*
        texts
     */
    public void loadText(String html, boolean updated) {
        if (!tabPane.getTabs().contains(textsTab)) {
            return;
        }
        try {
            textsArea.setText(HtmlWriteTools.htmlToText(html));
            textsChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlByText() {
        String body = HtmlWriteTools.stringToHtml(textsArea.getText());
        return HtmlWriteTools.html(title, body, HtmlStyles.DefaultStyle);
    }

    protected void textsChanged(boolean changed) {
//        MyBoxLog.debug(changed);
        textsChanged = changed;
        textsTab.setText(message("Texts") + (changed ? " *" : ""));
        textsLabel.setText(message("CharactersNumber") + ": " + StringTools.format(textsArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    protected void editTexts() {
        TextEditorController.edit(textsArea.getText());
    }

    @FXML
    protected void clearTexts() {
        textsArea.clear();
        textsChanged(true);
    }


    /*
        buttons
     */
    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
                return popViewAction();

            } else if (tab == markdownTab) {
                MarkdownPopController.open(this, markdownArea);
                return true;

            } else if (tab == codesTab) {
                HtmlCodesPopController.openInput(this, codesArea);
                return true;

            } else if (tab == richEditorTab) {
                HtmlPopController.openHtml(this, richEditorController.getContents());
                return true;

            } else if (tab == textsTab) {
                TextPopController.openInput(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    public boolean popViewAction() {
        return webViewController.popAction();
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
                return synchronizeViewAction();

            } else if (tab == codesTab) {
                synchronizeCodes();
                return true;

            } else if (tab == domTab) {
                synchronizeDom();
                return true;

            } else if (tab == richEditorTab) {
                synchronizeRichEditor();
                return true;

            } else if (tab == markdownTab) {
                synchronizeMarkdown();
                return true;

            } else if (tab == textsTab) {
                synchronizeTexts();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    public boolean synchronizeViewAction() {
        Platform.runLater(() -> {
            String html = htmlInWebview();
            loadHtmlCodes(html, true);
            loadDom(html, true);
            loadRichEditor(html, true);
            loadMarkdown(html, true);
            loadText(html, true);
        });
        return true;
    }

    public void synchronizeCodes() {
        Platform.runLater(() -> {
            String html = htmlByCodes();
            loadDom(html, true);
            loadRichEditor(html, true);
            loadMarkdown(html, true);
            loadText(html, true);
            loadView(html, true);
        });
    }

    public void synchronizeDom() {
        Platform.runLater(() -> {
            String html = htmlByDom();
            loadHtmlCodes(html, true);
            loadRichEditor(html, true);
            loadMarkdown(html, true);
            loadText(html, true);
            loadView(html, true);
        });
    }

    public void synchronizeRichEditor() {
        Platform.runLater(() -> {
            String html = htmlByRichEditor();
            loadHtmlCodes(html, true);
            loadDom(html, true);
            loadMarkdown(html, true);
            loadText(html, true);
            loadView(html, true);
        });
    }

    public void synchronizeMarkdown() {
        Platform.runLater(() -> {
            String html = htmlByMarkdown();
            loadDom(html, true);
            loadRichEditor(html, true);
            loadHtmlCodes(html, true);
            loadText(html, true);
            loadView(html, true);
        });
    }

    public void synchronizeTexts() {
        Platform.runLater(() -> {
            String html = htmlByText();
            loadDom(html, true);
            loadRichEditor(html, true);
            loadHtmlCodes(html, true);
            loadMarkdown(html, true);
            loadView(html, true);
        });
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
                return menuViewAction();

            } else if (tab == codesTab) {
                MenuHtmlCodesController.htmlMenu(this, codesArea);
                return true;

            } else if (tab == domTab) {
                domController.popFunctionsMenu(null);
                return true;

            } else if (tab == richEditorTab) {
//                MenuHtmlCodesController.openHtml(this, richEditorController.htmlEditor);
                return true;

            } else if (tab == markdownTab) {
                MenuMarkdownEditController.mdMenu(this, markdownArea);
                return true;

            } else if (tab == textsTab) {
                MenuTextEditController.textMenu(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    public boolean menuViewAction() {
        Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
        MenuWebviewController.webviewMenu(webViewController, null, localToScreen.getX(), localToScreen.getY());
        return true;
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
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == codesTab) {
                clearCodes();

            } else if (tab == domTab) {
                clearDom();

            } else if (tab == richEditorTab) {
                clearRichEditor();

            } else if (tab == markdownTab) {
                clearMarkdown();

            } else if (tab == textsTab) {
                clearTexts();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        panes
     */
    public void tabChanged() {
        try {
            TextClipboardPopController.closeAll();
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            clearButton.setDisable(tab == viewTab);
            popButton.setDisable(tab == domTab);
            menuButton.setDisable(tab == richEditorTab);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void showTabs() {
        try {
            if (!UserConfig.getBoolean(baseName + "ShowDomTab", true)) {
                tabPane.getTabs().remove(domTab);
            }
            domTab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (domChanged) {
                        Optional<ButtonType> result = alertClosingTab();
                        if (result == null || !result.isPresent()) {
                            return;
                        }
                        if (result.get() == buttonSynchronize) {
                            synchronizeDom();
                        } else if (result.get() != buttonClose) {
                            event.consume();
                            return;
                        }
                    }
                    UserConfig.setBoolean(baseName + "ShowDomTab", false);
                }
            });

            if (!UserConfig.getBoolean(baseName + "ShowCodesTab", true)) {
                tabPane.getTabs().remove(codesTab);
            }
            codesTab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (codesChanged) {
                        Optional<ButtonType> result = alertClosingTab();
                        if (result == null || !result.isPresent()) {
                            return;
                        }
                        if (result.get() == buttonSynchronize) {
                            synchronizeCodes();
                        } else if (result.get() != buttonClose) {
                            event.consume();
                            return;
                        }
                    }
                    UserConfig.setBoolean(baseName + "ShowCodesTab", false);
                }
            });

            if (!UserConfig.getBoolean(baseName + "ShowEditorTab", true)) {
                tabPane.getTabs().remove(richEditorTab);
            }
            richEditorTab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (richChanged) {
                        Optional<ButtonType> result = alertClosingTab();
                        if (result == null || !result.isPresent()) {
                            return;
                        }
                        if (result.get() == buttonSynchronize) {
                            synchronizeRichEditor();
                        } else if (result.get() != buttonClose) {
                            event.consume();
                            return;
                        }
                    }
                    UserConfig.setBoolean(baseName + "ShowEditorTab", false);
                }
            });

            if (!UserConfig.getBoolean(baseName + "ShowMarkdownTab", true)) {
                tabPane.getTabs().remove(markdownTab);
            }
            markdownTab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (mdChanged) {
                        Optional<ButtonType> result = alertClosingTab();
                        if (result == null || !result.isPresent()) {
                            return;
                        }
                        if (result.get() == buttonSynchronize) {
                            synchronizeMarkdown();
                        } else if (result.get() != buttonClose) {
                            event.consume();
                            return;
                        }
                    }
                    UserConfig.setBoolean(baseName + "ShowMarkdownTab", false);
                }
            });

            if (!UserConfig.getBoolean(baseName + "ShowTextsTab", true)) {
                tabPane.getTabs().remove(textsTab);
            }
            textsTab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (textsChanged) {
                        Optional<ButtonType> result = alertClosingTab();
                        if (result == null || !result.isPresent()) {
                            return;
                        }
                        if (result.get() == buttonSynchronize) {
                            synchronizeTexts();
                        } else if (result.get() != buttonClose) {
                            event.consume();
                            return;
                        }
                    }
                    UserConfig.setBoolean(baseName + "ShowTextsTab", false);
                }
            });

            NodeStyleTools.refreshStyle(tabPane);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public Optional<ButtonType> alertClosingTab() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setHeaderText(getMyStage().getTitle());
        alert.setContentText(message("ClosingEditorTabConfirm"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getButtonTypes().setAll(buttonSynchronize, buttonClose, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        return alert.showAndWait();
    }

    @FXML
    public void popPanesMenu(Event event) {
        if (UserConfig.getBoolean("HtmlPanesPopWhenMouseHovering", false)) {
            showPanesMenu(event);
        }
    }

    @FXML
    public void showPanesMenu(Event event) {
        List<MenuItem> items = new ArrayList<>();

        CheckMenuItem domMenu = new CheckMenuItem(message("Tree"));
        domMenu.setSelected(tabPane.getTabs().contains(domTab));
        domMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "ShowDomTab", domMenu.isSelected());
                if (domMenu.isSelected()) {
                    if (!tabPane.getTabs().contains(domTab)) {
                        tabPane.getTabs().add(tabPane.getTabs().size(), domTab);
                        loadHtmlCodes(htmlInWebview(), false);
                    }
                } else {
                    if (tabPane.getTabs().contains(domTab)) {
                        tabPane.getTabs().remove(domTab);
                    }
                }
                NodeStyleTools.refreshStyle(tabPane);
            }
        });
        items.add(domMenu);

        CheckMenuItem codesMenu = new CheckMenuItem(message("HtmlCodes"));
        codesMenu.setSelected(tabPane.getTabs().contains(codesTab));
        codesMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "ShowCodesTab", codesMenu.isSelected());
                if (codesMenu.isSelected()) {
                    if (!tabPane.getTabs().contains(codesTab)) {
                        tabPane.getTabs().add(tabPane.getTabs().size(), codesTab);
                        loadHtmlCodes(htmlInWebview(), false);
                    }
                } else {
                    if (tabPane.getTabs().contains(codesTab)) {
                        tabPane.getTabs().remove(codesTab);
                    }
                }
                NodeStyleTools.refreshStyle(tabPane);
            }
        });
        items.add(codesMenu);

        CheckMenuItem editorMenu = new CheckMenuItem(message("RichText"));
        editorMenu.setSelected(tabPane.getTabs().contains(richEditorTab));
        editorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "ShowEditorTab", editorMenu.isSelected());
                if (editorMenu.isSelected()) {
                    if (!tabPane.getTabs().contains(richEditorTab)) {
                        tabPane.getTabs().add(tabPane.getTabs().size(), richEditorTab);
                        loadRichEditor(htmlInWebview(), false);
                    }
                } else {
                    if (tabPane.getTabs().contains(richEditorTab)) {
                        tabPane.getTabs().remove(richEditorTab);
                    }
                }
                NodeStyleTools.refreshStyle(tabPane);
            }
        });
        items.add(editorMenu);

        CheckMenuItem mdMenu = new CheckMenuItem("Markdown");
        mdMenu.setSelected(tabPane.getTabs().contains(markdownTab));
        mdMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "ShowMarkdownTab", mdMenu.isSelected());
                if (mdMenu.isSelected()) {
                    if (!tabPane.getTabs().contains(markdownTab)) {
                        tabPane.getTabs().add(tabPane.getTabs().size(), markdownTab);
                        loadMarkdown(htmlInWebview(), false);
                    }
                } else {
                    if (tabPane.getTabs().contains(markdownTab)) {
                        tabPane.getTabs().remove(markdownTab);
                    }
                }
                NodeStyleTools.refreshStyle(tabPane);
            }
        });
        items.add(mdMenu);

        CheckMenuItem textsMenu = new CheckMenuItem(message("Texts"));
        textsMenu.setSelected(tabPane.getTabs().contains(textsTab));
        textsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(baseName + "ShowTextsTab", textsMenu.isSelected());
                if (textsMenu.isSelected()) {
                    if (!tabPane.getTabs().contains(textsTab)) {
                        tabPane.getTabs().add(tabPane.getTabs().size(), textsTab);
                        loadText(htmlInWebview(), false);
                    }
                } else {
                    if (tabPane.getTabs().contains(textsTab)) {
                        tabPane.getTabs().remove(textsTab);
                    }
                }
                NodeStyleTools.refreshStyle(thisPane);
            }
        });
        items.add(textsMenu);

        items.add(new SeparatorMenuItem());

        CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        hoverMenu.setSelected(UserConfig.getBoolean("HtmlPanesPopWhenMouseHovering", false));
        hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("HtmlPanesPopWhenMouseHovering", hoverMenu.isSelected());
            }
        });
        items.add(hoverMenu);

        popEventMenu(event, items);

    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!isIndependantStage() || !fileChanged) {
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
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else if (result.get() == buttonNotSave) {
                fileChanged = false;
                return true;
            } else {
                return false;
            }
        }
    }

    /*
        static
     */
    public static ControlHtmlEditor load(String html) {
        try {
            ControlHtmlEditor controller = (ControlHtmlEditor) WindowTools.openStage(Fxmls.HtmlEditorFxml);
            controller.loadContents(html);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
