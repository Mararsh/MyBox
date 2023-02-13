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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class ControlHtmlEditor extends WebAddressController {

    protected HTMLEditor richEditor;
    protected boolean addressChanged, pageLoaded, codesChanged, domChanged, richChanged,
            mdChanged, textsChanged, fileChanged;
    protected MutableDataSet htmlOptions;
    protected FlexmarkHtmlConverter htmlConverter;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected String title;
    protected TreeItem<HtmlNode> treeRoot;

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
    protected ControlFileBackup backupController;
    @FXML
    protected CheckBox wrapCodesCheck, wrapMarkdownCheck, wrapTextsCheck;
    @FXML
    protected Button menuViewButton, synchronizeViewButton, popViewButton,
            menuTextsButton, menuMDButton, menuRichButton, menuCodesButton;
    @FXML
    protected TreeTableView<HtmlNode> domTree;
    @FXML
    protected TreeTableColumn<HtmlNode, String> tagColumn, idColumn, classnameColumn;

    public ControlHtmlEditor() {
        TipsLabelKey = "HtmlEditorTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initTabPane();
            initCodesTab();
            initDomTab();
            initEdtiorTab();
            initMarkdownTab();
            initTextsTab();
            initBackupsTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTabPane() {
        try {
            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    TextClipboardPopController.closeAll();
                }
            });

            showTabs();

            NodeStyleTools.refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initDomTab() {
        try {
            tagColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("tag"));
            idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
            classnameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("classname"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEdtiorTab() {
        try {
            richEditor = richEditorController.htmlEditor;

            // https://stackoverflow.com/questions/31894239/javafx-htmleditor-how-to-implement-a-changelistener
            // As my testing, only DragEvent.DRAG_EXITED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED working for HtmlEdior
            richEditor.setOnDragExited(new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
//                    MyBoxLog.debug("setOnDragExited");
                    if (!richEditor.isDisabled()) {
                        richEditorChanged(true);
                    }
                }
            });
            richEditor.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
//                    MyBoxLog.debug("setOnKeyReleased");
                    if (!richEditor.isDisabled()) {
                        richEditorChanged(true);
                    }
                }
            });

            richEditorController.pageLoadingNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    richEditorPageLoading();
                }
            });

            richEditorController.pageLoadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    richEditorPageLoaded();
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

            wrapMarkdownCheck.setSelected(UserConfig.getBoolean(baseName + "WrapMarkdown", true));
            markdownArea.setWrapText(wrapMarkdownCheck.isSelected());
            wrapMarkdownCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapMarkdown", wrapMarkdownCheck.isSelected());
                    markdownArea.setWrapText(wrapMarkdownCheck.isSelected());
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

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            if (!tabPane.getTabs().contains(viewTab)) {
                NodeStyleTools.setTooltip(menuViewButton, message("ContextMenu"));
                NodeStyleTools.setTooltip(synchronizeViewButton, message("SynchronizeChangesToOtherPanes"));
                NodeStyleTools.setTooltip(popViewButton, message("Pop"));
            } else {
                NodeStyleTools.setTooltip(menuViewButton, message("ContextMenu") + "\nF12");
            }
            NodeStyleTools.setTooltip(menuTextsButton, message("ContextMenu") + "\nF12");
            NodeStyleTools.setTooltip(menuMDButton, message("ContextMenu") + "\nF12");
            NodeStyleTools.setTooltip(menuRichButton, message("ContextMenu") + "\nF12");
            NodeStyleTools.setTooltip(menuCodesButton, message("ContextMenu") + "\nF12");
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
            task = new SingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    try {
                        File tmpFile = HtmlWriteTools.writeHtml(html);
                        if (tmpFile == null || !tmpFile.exists()) {
                            return false;
                        }
                        if (sourceFile != null && backupController != null && backupController.needBackup()) {
                            backupController.addBackup(task, sourceFile);
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
                    recordFileWritten(targetFile);
                    setSourceFile(targetFile);
                    addressChanged();
                    updateFileStatus(false);
                }
            };
            start(task);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        webViewController.saveAs(currentHtml(true));
    }

    public String currentHtml() {
        return currentHtml(false);
    }

    public String currentHtml(boolean synchronize) {
        try {
            if (webViewController.framesDoc.isEmpty()) {
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

                if (currentTab == viewTab) {
                    String html = htmlInWebview();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadRichEditor(html, false);
                    }
                    return html;

                } else if (currentTab == richEditorTab) {
                    String html = htmlByRichEditor();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadMarkdown(html, false);
                        loadText(html, false);
                        loadView(html, false);
                    }
                    return html;

                } else if (currentTab == markdownTab) {
                    String html = htmlByMarkdown();
                    if (synchronize) {
                        loadHtmlCodes(html, false);
                        loadRichEditor(html, false);
                        loadText(html, false);
                        loadView(html, false);
                    }
                    return html;

                } else if (currentTab == textsTab) {
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
            addressChanged = false;
            addressInput.setText("");
            loadContents(HtmlWriteTools.emptyHmtl(null));
//            loadRichEditor("", false);
//            loadHtmlCodes("", false);
//            loadMarkdown("", false);
//            loadText("", false);
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
    protected void updateStageTitle() {
        if (myStage == null) {
            return;
        }
        super.updateStageTitle();
        if (fileChanged) {
            myStage.setTitle(myStage.getTitle() + " *");
        }
    }

    protected void updateFileStatus(boolean changed) {
        fileChanged = changed;
        updateStageTitle();
        if (!changed) {
            viewChanged(false);
            codesChanged(false);
            richEditorChanged(false);
            markdownChanged(false);
            textsChanged(false);
            addressChanged = false;
        }
    }


    /*
        webview
     */
    @Override
    public void addressChanged() {
        addressChanged = true;
        super.addressChanged();
    }

    @Override
    public void pageLoading() {
        super.pageLoading();
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
        domTree.setEditable(false);
        richEditor.setDisable(true);
        markdownArea.setEditable(false);
        textsArea.setEditable(false);
        if (addressChanged) {
            String info = webViewController.address;
            info += (info == null ? "" : info + "\n") + message("Loading...");
            codesArea.setText(info);
            markdownArea.setText(info);
            richEditorController.writeContents(info);
            textsArea.setText(info);
        }
    }

    @Override
    public void pageLoaded() {
        try {
            pageLoaded = true;
            if (addressChanged) {
                fileChanged = false;
                String html = htmlInWebview();
                if (webViewController.address != null) {
                    loadRichEditor(webViewController.address);
                } else {
                    loadRichEditor(html, false);
                }
                loadHtmlCodes(html, false);
                loadDom(html, false);
                loadMarkdown(html, false);
                loadText(html, false);
                Platform.runLater(() -> {
                    if (backupController != null) {
                        viewChanged(false);
                        backupController.loadBackups(sourceFile);
                    }
                });
            }
            addressChanged = false;
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
            domTree.setEditable(true);
            richEditor.setDisable(false);
            markdownArea.setEditable(true);
            textsArea.setEditable(true);
            title = webEngine.getTitle();
            super.pageLoaded();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadView(String html, boolean updated) {
        Platform.runLater(() -> {
            try {
                webViewController.writeContents(html);
                viewChanged(updated);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    protected void viewChanged(boolean changed) {
        viewTab.setText(message("View") + (fileChanged ? " *" : ""));
        if (changed) {
            updateFileStatus(true);
        }
    }

    public String htmlInWebview() {
        return webViewController.loadedHtml();
    }

    @FXML
    @Override
    public void refreshAction() {
        webViewController.refresh();
    }

    /*
        codes
     */
    public void loadHtmlCodes(String html, boolean updated) {
        if (!tabPane.getTabs().contains(codesTab)) {
            return;
        }
        Platform.runLater(() -> {
            try {
                codesArea.setEditable(false);
                codesArea.setText(htmlCodes(html));
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
        codesLabel.setText(message("CharactersNumber") + ": " + StringTools.format(codesArea.getLength()));
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
        controller.requestMouse();
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
        Platform.runLater(() -> {
            try {
                treeRoot = new TreeItem(new HtmlNode("DOM"));
                treeRoot.setExpanded(true);
                domTree.setRoot(treeRoot);

                Document doc = Jsoup.parse(html);
                makeNodes(treeRoot, doc);
                domChanged(updated);
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    public String htmlByDom() {
        return htmlByCodes();
    }

    protected void domChanged(boolean changed) {
        domChanged = changed;
        domTab.setText("dom" + (changed ? " *" : ""));
        if (changed) {
            updateFileStatus(true);
        }
    }

    public void makeNodes(TreeItem parent, Element element) {
        try {
            if (parent == null || element == null) {
                return;
            }
            TreeItem<HtmlNode> node = new TreeItem(new HtmlNode(element));
            node.setExpanded(false);
            parent.getChildren().add(node);

            Elements children = element.children();
            if (children == null || children.isEmpty()) {
                return;
            }
            for (Element child : children) {
                makeNodes(node, child);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void foldAction() {
        unfold(domTree.getRoot(), false);
    }

    @FXML
    public void unfoldAction() {
        unfold(domTree.getRoot(), true);
    }

    public void unfold(TreeItem item, boolean unfold) {
        if (item == null) {
            return;
        }
        item.setExpanded(unfold);
        List<TreeItem> children = item.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (TreeItem child : children) {
            unfold(child, unfold);
        }
    }

    /*
        rich editor
     */
    protected void richEditorPageLoading() {
        richEditor.setDisable(true);
    }

    protected void richEditorPageLoaded() {
        richEditor.setDisable(false);
        richEditorChanged(richChanged);
    }

    public void loadRichEditor(String address) {
        if (!tabPane.getTabs().contains(richEditorTab)) {
            return;
        }
        Platform.runLater(() -> {
            richEditorController.loadAddress(address);
            richChanged = false;
        });
    }

    public void loadRichEditor(String html, boolean updated) {
        if (!tabPane.getTabs().contains(richEditorTab)) {
            return;
        }
        Platform.runLater(() -> {
            try {
                String contents = html;
                if (StringTools.include(html, "<FRAMESET ", true)) {
                    contents = "<p>" + message("FrameSetAndSelectFrame") + "</p>";
                }
                richEditorController.writeContents(contents);
                richChanged = updated;
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        });
    }

    public String htmlByRichEditor() {
        return richEditorController.loadedHtml();
    }

    protected void richEditorChanged(boolean changed) {
        richChanged = changed;
        richEditorTab.setText(message("RichText") + (changed ? " *" : ""));
        String c = htmlByRichEditor();
        int len = 0;
        if (c != null && !c.isEmpty()) {
            len = c.length();
        }
        richEditorController.setWebViewLabel(message("CharactersNumber") + ": " + StringTools.format(len));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void popRichEditorStyle(MouseEvent mouseEvent) {
        PopTools.popHtmlStyle(mouseEvent, richEditorController);
    }

    @FXML
    public void clearRichEditor() {
        richEditorController.loadContents(null);
    }

    /*
        Markdown
     */
    public void loadMarkdown(String html, boolean changed) {
        if (!tabPane.getTabs().contains(markdownTab)) {
            return;
        }
        Platform.runLater(() -> {
            try {
                String md;
                if (html == null || html.isEmpty()) {
                    md = html;
                } else if (StringTools.include(html, "<FRAMESET ", true)) {
                    md = message("FrameSetAndSelectFrame");
                } else {
                    md = htmlConverter.convert(html);
                }
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
        markdownLabel.setText(message("CharactersNumber") + ": " + StringTools.format(markdownArea.getLength()));
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
//        MyBoxLog.error(updated);
        if (!tabPane.getTabs().contains(textsTab)) {
            return;
        }
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
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textsArea.getText());
        controller.requestMouse();
    }

    @FXML
    protected void clearTexts() {
        textsArea.clear();
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
                HtmlPopController.openWebView(this, WebViewTools.webview(richEditor));
                return true;

            } else if (tab == textsTab) {
                TextPopController.openInput(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    public boolean synchronizeViewAction() {
        synchronizeWebview();
        return true;
    }

    public void synchronizeWebview() {
        Platform.runLater(() -> {
            String html = htmlInWebview();
            loadHtmlCodes(html, true);
            loadDom(html, true);
            loadRichEditor(html, true);
            loadMarkdown(html, true);
            loadText(html, true);
        });
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
                Point2D localToScreen = codesArea.localToScreen(codesArea.getWidth() - 80, 80);
                MenuHtmlCodesController.open(this, codesArea, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == richEditorTab) {
                Point2D localToScreen = richEditor.localToScreen(richEditor.getWidth() - 80, 80);
                MenuWebviewController.pop(richEditorController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == markdownTab) {
                Point2D localToScreen = markdownArea.localToScreen(markdownArea.getWidth() - 80, 80);
                MenuMarkdownEditController.open(this, markdownArea, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == textsTab) {
                Point2D localToScreen = textsArea.localToScreen(textsArea.getWidth() - 80, 80);
                MenuTextEditController.open(this, textsArea, localToScreen.getX(), localToScreen.getY());
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    public boolean menuViewAction() {
        Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
        MenuWebviewController.pop(webViewController, null, localToScreen.getX(), localToScreen.getY());
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
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        panes
     */
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
                            synchronizeCodes();
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
            MyBoxLog.error(e.toString());
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
    public void popPanesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            List<MenuItem> items = makePanesMenu(mouseEvent);
            popMenu.getItems().addAll(items);

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makePanesMenu(MouseEvent mouseEvent) {
        List<MenuItem> items = new ArrayList<>();
        try {

            CheckMenuItem domMenu = new CheckMenuItem("DOM");
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
        return items;
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
            if (result == null || !result.isPresent()) {
                return false;
            }
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
            richEditor.setUserData(null);
        } catch (Exception e) {
        }
        super.cleanPane();
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
