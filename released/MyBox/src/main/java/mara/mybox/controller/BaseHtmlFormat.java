package mara.mybox.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.data.HtmlNode;
import mara.mybox.db.data.FileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public abstract class BaseHtmlFormat extends BaseWebViewController {

    protected String srcHtml, address;
    protected SimpleBooleanProperty htmChanged, loadNotify;
    protected MutableDataHolder htmlOptions;
    protected FlexmarkHtmlConverter htmlConverter;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRender;
    protected String title;

    protected final ButtonType buttonClose = new ButtonType(message("Close"));
    protected final ButtonType buttonSynchronize = new ButtonType(message("SynchronizeAndClose"));
    protected final ButtonType buttonCancel = new ButtonType(message("Cancel"));

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected RadioButton codesRaido, treeRadio, richRadio, mdRadio, textsRadio, htmlRadio;
    @FXML
    protected VBox formatBox, codesBox, treeBox, richBox, mdBox, textsBox, htmlBox;
    @FXML
    protected ControlHtmlRichEditor richEditorController;
    @FXML
    protected TextArea codesArea, markdownArea, textsArea;
    @FXML
    protected Label codesLabel, markdownLabel, textsLabel;
    @FXML
    protected ControlHtmlDomManage domController;
    @FXML
    protected CheckBox wrapCheck, editableCheck, synchronizeSwitchCheck;
    @FXML
    protected Button txtButton, styleViewButton, functionsButton;
    @FXML
    protected FlowPane opPane;

    public BaseHtmlFormat() {
        TipsLabelKey = "HtmlFormatTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            loadNotify = new SimpleBooleanProperty(false);
            htmChanged = new SimpleBooleanProperty(false);
            domController.setEditor(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initWebview();
            initRichEdtior();
            initCodes();
            initTexts();
            initMarkdown();

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    if (codesRaido.isSelected()) {
                        codesArea.setWrapText(newValue);
                    } else if (mdRadio.isSelected()) {
                        markdownArea.setWrapText(newValue);
                    } else if (textsRadio.isSelected()) {
                        textsArea.setWrapText(newValue);
                    }
                }
            });

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                    checkFormat(ov);
                }
            });

            checkFormat(null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initWebview() {
        try {
            webViewController.baseName = baseName;

            editableCheck.setSelected(UserConfig.getBoolean(baseName + "Editable", true));
            editableCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    webViewController.setEditable(newValue);
                }
            });

            webViewController.pageLoadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    pageLoaded();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initRichEdtior() {
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

    protected void initCodes() {
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initMarkdown() {
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

            htmlOptions = MarkdownTools.htmlOptions();
            htmlConverter = FlexmarkHtmlConverter.builder(htmlOptions).build();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRender = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initTexts() {
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkFormat(Toggle toggle) {
        try {
            if (isSettingValues) {
                return;
            }
            closePopup();
            formatBox.getChildren().clear();
            opPane.getChildren().clear();

            if (saveButton != null) {
                opPane.getChildren().add(saveButton);
            }
            opPane.getChildren().addAll(synchronizeButton, recoverButton);
            VBox editBox;
            if (codesRaido.isSelected()) {
                opPane.getChildren().addAll(clearButton, menuButton, txtButton, wrapCheck);
                editBox = codesBox;
                codesArea.setWrapText(wrapCheck.isSelected());

            } else if (treeRadio.isSelected()) {
                opPane.getChildren().addAll(clearButton, menuButton);
                editBox = treeBox;

            } else if (richRadio.isSelected()) {
                opPane.getChildren().addAll(clearButton);
                editBox = richBox;

            } else if (mdRadio.isSelected()) {
                opPane.getChildren().addAll(clearButton, menuButton, txtButton, wrapCheck);
                editBox = mdBox;
                markdownArea.setWrapText(wrapCheck.isSelected());

            } else if (textsRadio.isSelected()) {
                opPane.getChildren().addAll(clearButton, menuButton, txtButton, wrapCheck);
                editBox = textsBox;
                textsArea.setWrapText(wrapCheck.isSelected());

            } else {
                opPane.getChildren().addAll(menuButton, styleViewButton, functionsButton, editableCheck);
                editBox = htmlBox;

            }
            opPane.getChildren().addAll(synchronizeSwitchCheck);

            formatBox.getChildren().add(editBox);
            VBox.setVgrow(editBox, Priority.ALWAYS);
            VBox.setVgrow(editBox, Priority.ALWAYS);

            NodeStyleTools.refreshStyle(opPane);
            NodeStyleTools.refreshStyle(formatBox);

            if (synchronizeSwitchCheck.isSelected()) {
                synchronizeFrom(toggle);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean synchronizeFrom(Toggle toggle) {
        try {
            if (isSettingValues) {
                return true;
            }
            isSettingValues = true;
            String currentHtml;

            RadioButton radio = (RadioButton) toggle;
            if (codesRaido == radio) {
                currentHtml = htmlByCodes();
                loadView(currentHtml, true);
                loadDom(currentHtml, true);
                loadRichEditor(currentHtml, true);
                loadMarkdown(currentHtml, true);
                loadText(currentHtml, true);

            } else if (treeRadio == radio) {
                currentHtml = htmlByDom();
                loadView(currentHtml, true);
                loadHtmlCodes(currentHtml, true);
                loadRichEditor(currentHtml, true);
                loadMarkdown(currentHtml, true);
                loadText(currentHtml, true);

            } else if (richRadio == radio) {
                currentHtml = htmlByRichEditor();
                loadView(currentHtml, true);
                loadHtmlCodes(currentHtml, true);
                loadDom(currentHtml, true);
                loadMarkdown(currentHtml, true);
                loadText(currentHtml, true);

            } else if (mdRadio == radio) {
                currentHtml = htmlByMarkdown();
                loadView(currentHtml, true);
                loadHtmlCodes(currentHtml, true);
                loadDom(currentHtml, true);
                loadRichEditor(currentHtml, true);
                loadText(currentHtml, true);

            } else if (textsRadio == radio) {
                currentHtml = htmlByText();
                loadView(currentHtml, true);
                loadHtmlCodes(currentHtml, true);
                loadDom(currentHtml, true);
                loadRichEditor(currentHtml, true);
                loadMarkdown(currentHtml, true);

            } else {
                currentHtml = htmlInWebview();
                loadHtmlCodes(currentHtml, true);
                loadDom(currentHtml, true);
                loadRichEditor(currentHtml, true);
                loadMarkdown(currentHtml, true);
                loadText(currentHtml, true);

            }
            isSettingValues = false;

            updateStatus(true);
            popInformation(message("SynchronizedChanges"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return true;
    }

    /*
        source
     */
    @Override
    public void selectSourceFile(File file) {
        loadFile(file);
    }

    @Override
    public boolean loadFile(File file) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        if (file == null || !file.exists()) {
            popError(message("InvalidParameter") + ": " + message("File"));
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
                setAddress(file.toURI().toString());
                sourceFile = file;
                writePanes(html);
            }

        };
        start(task);
        return true;
    }

    @Override
    public boolean loadAddress(String inAddress) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        String checkedAddress = UrlTools.checkURL(inAddress, Charset.forName("UTF-8"));
        if (checkedAddress == null) {
            popError(message("InvalidParameter") + ": " + message("Address"));
            return false;
        }
        String netpath = checkedAddress;
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    html = HtmlReadTools.url2html(this, netpath);
                    return html != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                setAddress(netpath);
                writePanes(html);
            }

        };
        start(task);
        return true;
    }

    @Override
    public boolean loadContents(String contents) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        setAddress(null);
        return writePanes(contents);
    }

    @Override
    public boolean loadContents(String inAddress, String contents) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        setAddress(inAddress);
        return writePanes(contents);
    }

    private boolean setAddress(String inAddress) {
        try {
            sourceFile = null;
            if (inAddress == null) {
                address = null;
            } else {
                address = UrlTools.checkURL(inAddress, Charset.forName("UTF-8"));
                if (address != null && address.startsWith("file:/")) {
                    File file = new File(address.substring(6));
                    if (file.exists()) {
                        sourceFile = file;
                    }
                }
            }
            webViewController.sourceFile = sourceFile;
            webViewController.address = address;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean writePanes(String html) {
        srcHtml = html;

        isSettingValues = true;
        loadHtmlCodes(html, false);
        loadDom(html, false);
        loadRichEditor(html, false);
        loadMarkdown(html, false);
        loadText(html, false);
        loadView(html, false);
        isSettingValues = false;

        updateStatus(false);

        loadNotify.set(!loadNotify.get());
        return true;
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
            targetFile = saveCurrentFile(title());
        } else {
            targetFile = sourceFile;
        }
        if (targetFile == null) {
            return;
        }
        String html = currentHtml();
        if (html == null || html.isBlank()) {
            popError(message("NoData"));
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                try {
                    File tmpFile = HtmlWriteTools.writeHtml(html);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    needBackup = sourceFile != null && parentController != null
                            && UserConfig.getBoolean(parentController.baseName + "BackupWhenSave", true);
                    if (needBackup) {
                        backup = addBackup(this, sourceFile);
                    }
                    return FileTools.override(tmpFile, targetFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (needBackup) {
                    if (backup != null && backup.getBackup() != null) {
                        popInformation(message("SavedAndBacked"));
                        FileBackupController.updateList(sourceFile);
                    } else {
                        popError(message("FailBackup"));
                    }
                } else {
                    popInformation(targetFile + "   " + message("Saved"));
                }
                recordFileWritten(targetFile);
                updateStatus(false);
                loadFile(targetFile);
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void saveAsAction() {
        webViewController.saveAs(currentHtml());
    }

    public String currentHtml() {
        Toggle t = formatGroup.getSelectedToggle();
        return currentHtml(t != null ? (RadioButton) t : null);
    }

    public String currentHtml(RadioButton radio) {
        try {
            if (codesRaido == radio) {
                return htmlByCodes();

            } else if (treeRadio == radio) {
                return htmlByDom();

            } else if (richRadio == radio) {
                return htmlByRichEditor();

            } else if (mdRadio == radio) {
                return htmlByMarkdown();

            } else if (textsRadio == radio) {
                return htmlByText();

            } else {
                return htmlInWebview();

            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public boolean create() {
        try {
            if (!checkBeforeNextAction()) {
                return false;
            }
            loadContents(HtmlWriteTools.emptyHmtl(null));
            updateStatus(false);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected void updateStatus(boolean changed) {
        htmChanged.set(changed);
    }

    /*
        webview
     */
    public void loadView(String html, boolean updated) {
        try {
            isSettingValues = true;
            webViewController.writeContent(html);
            isSettingValues = false;
            htmlChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlInWebview() {
        webViewController.executeScript("document.body.contentEditable=false");
        String html = webViewController.currentHtml();
        webViewController.executeScript("document.body.contentEditable=true");
        return html;
    }

    protected void htmlChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        if (changed) {
            updateStatus(true);
        }
    }

    @Override
    public void pageLoaded() {
        webViewController.executeScript("document.body.contentEditable=" + editableCheck.isSelected());
    }

    @Override
    public void updateStageTitle() {
    }

    /*
        codes
     */
    public void loadHtmlCodes(String html, boolean updated) {
        try {
            isSettingValues = true;
            codesArea.setText(htmlCodes(html));
            isSettingValues = false;
            codesChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlByCodes() {
        return codesArea.getText();
    }

    protected void codesChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        codesLabel.setText(message("CharactersNumber") + ": " + StringTools.format(codesArea.getLength()));
        codesRaido.setText(changed ? "*" : "");
        if (changed) {
            updateStatus(true);
        }
    }

    @FXML
    public void clearCodes() {
        codesArea.clear();
    }

    public String htmlCodes(String html) {
        return html;
    }

    public void pasteText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        codesArea.replaceText(codesArea.getSelection(), text);
        codesArea.requestFocus();
        codesChanged(true);
    }

    /*
        dom
     */
    public void loadDom(String html, boolean updated) {
        isSettingValues = true;
        domController.loadHtml(html);
        isSettingValues = false;
        domChanged(updated);
    }

    public String htmlByDom() {
        return domController.html();
    }

    public void domChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        treeRadio.setText(changed ? "*" : "");
        if (changed) {
            updateStatus(true);
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
        try {
            String contents = html;
            if (StringTools.include(html, "<FRAMESET ", true)) {
                contents = "<p>" + message("FrameSetAndSelectFrame") + "</p>";
            }
            isSettingValues = true;
            richEditorController.loadContents(contents);
            isSettingValues = false;
            richEditorChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlByRichEditor() {
        return richEditorController.getContents();
    }

    protected void richEditorChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        String c = htmlByRichEditor();
        int len = 0;
        if (c != null && !c.isEmpty()) {
            len = c.length();
        }
        richEditorController.setLabel(message("CharactersNumber") + ": " + StringTools.format(len));
        richRadio.setText(changed ? "*" : "");
        if (changed) {
            updateStatus(true);
        }
    }

    @FXML
    public void clearRichEditor() {
        richEditorController.loadContents(null);
    }

    /*
        Markdown
     */
    public void loadMarkdown(String html, boolean changed) {
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
        if (isSettingValues) {
            return;
        }
        markdownLabel.setText(message("CharactersNumber") + ": " + StringTools.format(markdownArea.getLength()));
        mdRadio.setText(changed ? "*" : "");
        if (changed) {
            updateStatus(true);
        }
    }

    @FXML
    protected void clearMarkdown() {
        markdownArea.clear();
    }

    /*
        texts
     */
    public void loadText(String html, boolean updated) {
        try {
            textsArea.setText(HtmlWriteTools.htmlToText(html));
            textsChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String htmlByText() {
        return HtmlWriteTools.textToHtml(textsArea.getText());
    }

    protected void textsChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        textsLabel.setText(message("CharactersNumber") + ": " + StringTools.format(textsArea.getLength()));
        textsRadio.setText(changed ? "*" : "");
        if (changed) {
            updateStatus(true);
        }
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
    public boolean synchronizeAction() {
        synchronizeFrom(formatGroup.getSelectedToggle());
        return true;
    }

    @FXML
    @Override
    public void recoverAction() {
        writePanes(srcHtml);
    }

    @FXML
    @Override
    public void refreshAction() {
        if (webViewController.address != null) {
            loadAddress(webViewController.address);
        } else if (webViewController.content != null) {
            loadContents(webViewController.content);
        }
        updateStatus(false);
    }

    @FXML
    protected void editText() {
        if (codesRaido.isSelected()) {
            TextEditorController.edit(codesArea.getText());
        } else if (mdRadio.isSelected()) {
            MarkdownEditorController.edit(markdownArea.getText());
        } else if (textsRadio.isSelected()) {
            TextEditorController.edit(textsArea.getText());
        }
    }

    @FXML
    @Override
    public void popViewMenu(Event event) {
        if (UserConfig.getBoolean("HtmlFormatViewMenuPopWhenMouseHovering", true)) {
            showViewMenu(event);
        }
    }

    @FXML
    @Override
    public void showViewMenu(Event event) {
        List<MenuItem> items = new ArrayList<>();
        String html = currentHtml();
        if (html == null || html.isBlank()) {
            popError(message("NoData"));
            return;
        }

        MenuItem menu = new MenuItem(message("CurrentFormat") + " - " + message("Html"), StyleTools.getIconImageView("iconHtml.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HtmlPopController.showHtml(myController, html);
            }
        });
        items.add(menu);

        menu = new MenuItem(message("CurrentFormat") + " - " + message("HtmlCodes"), StyleTools.getIconImageView("iconMeta.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TextPopController.loadText(html);
            }
        });
        items.add(menu);

        menu = new MenuItem(message("CurrentFormat") + " - " + "Markdown", StyleTools.getIconImageView("iconMarkdown.png"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (StringTools.include(html, "<FRAMESET ", true)) {
                    popError(message("FrameSetAndSelectFrame"));
                    return;
                }
                MarkdownPopController.show(myController, htmlConverter.convert(html));
            }
        });
        items.add(menu);

        if (htmChanged.get() && srcHtml != null) {
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Source") + " - " + message("Html"), StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    HtmlPopController.showHtml(myController, srcHtml);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("Source") + " - " + message("HtmlCodes"), StyleTools.getIconImageView("iconMeta.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TextPopController.loadText(srcHtml);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("Source") + " - " + "Markdown", StyleTools.getIconImageView("iconMarkdown.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (StringTools.include(html, "<FRAMESET ", true)) {
                        popError(message("FrameSetAndSelectFrame"));
                        return;
                    }
                    MarkdownPopController.show(myController, htmlConverter.convert(srcHtml));
                }
            });
            items.add(menu);
        }

        items.add(new SeparatorMenuItem());

        CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        hoverMenu.setSelected(UserConfig.getBoolean("HtmlFormatViewMenuPopWhenMouseHovering", true));
        hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("HtmlFormatViewMenuPopWhenMouseHovering", hoverMenu.isSelected());
            }
        });
        items.add(hoverMenu);

        popEventMenu(event, items);
    }

    @FXML
    @Override
    public boolean menuAction(Event event) {
        try {
            closePopup();

            if (codesRaido.isSelected()) {
                MenuHtmlCodesController.htmlMenu(this, codesArea);
                return true;

            } else if (treeRadio.isSelected()) {
                domController.popFunctionsMenu(event);
                return true;

            } else if (mdRadio.isSelected()) {
                MenuMarkdownEditController.mdMenu(this, markdownArea);
                return true;

            } else if (textsRadio.isSelected()) {
                MenuTextEditController.textMenu(this, textsArea);
                return true;

            } else if (htmlRadio.isSelected()) {
                webViewController.menuAction(event);
                return true;

            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            if (codesRaido.isSelected()) {
                clearCodes();

            } else if (treeRadio.isSelected()) {
                clearDom();

            } else if (richRadio.isSelected()) {
                clearRichEditor();

            } else if (mdRadio.isSelected()) {
                clearMarkdown();

            } else if (textsRadio.isSelected()) {
                clearTexts();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        try {
            if (mdRadio.isSelected()) {
                TextClipboardPopController.open(this, markdownArea);

            } else if (codesRaido.isSelected()) {
                TextClipboardPopController.open(this, codesArea);

            } else if (textsRadio.isSelected()) {
                TextClipboardPopController.open(this, textsArea);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        panes
     */
    @Override
    public boolean checkBeforeNextAction() {
        if (!isIndependantStage() || !htmChanged.get()) {
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
                updateStatus(false);
                return true;
            } else {
                return false;
            }
        }
    }

    /*
        static
     */
    public static BaseHtmlFormat load(String html) {
        try {
            BaseHtmlFormat controller = (BaseHtmlFormat) WindowTools.openStage(Fxmls.HtmlEditorFxml);
            controller.loadContents(html);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
