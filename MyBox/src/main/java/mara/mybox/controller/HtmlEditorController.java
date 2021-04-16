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
import java.net.URL;
import java.net.URLDecoder;
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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;

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
    protected int foundCount;

    @FXML
    protected Button synchronizeMainButton, synchronizePairButton, editMarkdownButton, styleLinksButton;
    @FXML
    protected HTMLEditor htmlEditor;
    @FXML
    protected TextArea tocArea, markdownArea, textArea;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editorTab, codesTab, tocTab, markdownTab, textTab, linksTab, imagesTab,
            elementsTab, findTab, backupTab;
    @FXML
    protected Label editorLabel, mdLabel, codesLabel;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected ControlHtmlCodes codesController;
    @FXML
    protected ControlWebview linksController, imagesController, elementController, findController;
    @FXML
    protected ComboBox<String> frameSelector;
    @FXML
    protected RadioButton tagRadio, idRadio, nameRadio;
    @FXML
    protected ControlStringSelector elementInputController, findInputController;
    @FXML
    protected ToggleGroup elementGroup;
    @FXML
    protected ColorSet findColorController, findBgColorController;

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
            initImagesTab();
            initBackupsTab();
            initElementsTab();
            initFindTab();

            frameSelector.setVisible(false);
            frameSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    synchronizeMain();
                }
            });

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
                    if (intValue >= 0) {
                        if (intValue == checkFrame()) {
                            synchronizeMain();
                        }
                    } else if (intValue == ControlWebview.DocLoading) {
                        pageIsLoading();
                    } else {
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

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowToc", true)) {
                tabPane.getTabs().remove(tocTab);
            }
            tocTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowToc", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowLinks", true)) {
                tabPane.getTabs().remove(linksTab);
            }
            linksTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowLinks", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowText", true)) {
                tabPane.getTabs().remove(textTab);
            }
            textTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowText", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowImages", true)) {
                tabPane.getTabs().remove(imagesTab);
            }
            imagesTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowImages", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowElements", true)) {
                tabPane.getTabs().remove(elementsTab);
            }
            elementsTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowElements", false);
                }
            });

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowFind", true)) {
                tabPane.getTabs().remove(findTab);
            }
            findTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowFind", false);
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
                    Platform.runLater(() -> {
                        if (oldValue == codesTab) {
                            String html = codesController.codes();
                            htmlEditor.setHtmlText(html);
                            markdown(html);

                        } else if (oldValue == editorTab) {
                            String html = htmlEditor.getHtmlText();
                            codes(html);
                            markdown(html);

                        } else if (oldValue == markdownTab) {
                            Node document = htmlParser.parse(markdownArea.getText());
                            String html = htmlRender.render(document);
                            htmlEditor.setHtmlText(html);
                            codes(html);
                        }
                    });

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
            linksController.setValues(this, false, true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initImagesTab() {
        try {
            imagesController.setValues(this, false, true);
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

    protected void initElementsTab() {
        try {
            elementController.setValues(this, false, true);

            elementInputController.init(this, baseName + "ElementTag", "table", 20);
            elementGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (tagRadio.isSelected()) {
                        elementInputController.refreshList(baseName + "ElementTag", "table");
                    } else if (idRadio.isSelected()) {
                        elementInputController.refreshList(baseName + "ElementID", "id");
                    } else if (nameRadio.isSelected()) {
                        elementInputController.refreshList(baseName + "ElementName", "name");
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initFindTab() {
        try {
            findController.setValues(this, false, true);

            findInputController.init(this, baseName + "Find", "find", 20);

            findColorController.init(this, baseName + "FindColor", Color.YELLOW);
            findBgColorController.init(this, baseName + "FindBgColor", Color.BLACK);

            findController.webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
                        findController.bottomLabel.setText(message("Found") + ": " + foundCount);
                    }
                }
            });

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
        frameSelector.getItems().clear();
        frameSelector.setVisible(false);
        String info = webviewController.address + "\n" + message("Loading...");
        codesController.load(info);
        tocArea.setText(info);
        textArea.setText(info);
        linksController.webEngine.getLoadWorker().cancel();
        linksController.webEngine.loadContent(info);
        imagesController.webEngine.getLoadWorker().cancel();
        imagesController.webEngine.loadContent(info);
        elementController.webEngine.getLoadWorker().cancel();
        elementController.webEngine.loadContent(info);
        findController.webEngine.getLoadWorker().cancel();
        findController.webEngine.loadContent(info);
        markdownArea.setText(info);
        htmlEditor.setHtmlText(info);
        foundCount = 0;
    }

    private void afterPageLoaded() {
        pageLoaded = true;
        initFrames();
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

    public Document getHtmlDoc(int frameIndex) {
        if (frameIndex >= 0) {
            return webviewController.frameDoc.get(frameIndex);
        } else {
            return webviewController.doc;
        }
    }

    public String getHtmlText(int frameIndex) {
        if (frameIndex >= 0) {
            return FxmlControl.getFrame(webEngine, frameIndex);
        } else {
            return FxmlControl.getHtml(webEngine);
        }
    }

    @FXML
    public void synchronizeMain() {
        try {
            int frameIndex = checkFrame();

            if (tabPane.getTabs().contains(editorTab)) {
                Platform.runLater(() -> {
                    richText(getHtmlText(frameIndex));
                });
            }
            if (tabPane.getTabs().contains(codesTab)) {
                Platform.runLater(() -> {
                    codes(getHtmlText(frameIndex));
                });
            }
            if (tabPane.getTabs().contains(tocTab)) {
                Platform.runLater(() -> {
                    toc(getHtmlText(frameIndex));
                });
            }
            if (tabPane.getTabs().contains(linksTab)) {
                Platform.runLater(() -> {
                    links(getHtmlDoc(frameIndex));
                });
            }
            if (tabPane.getTabs().contains(imagesTab)) {
                Platform.runLater(() -> {
                    images(getHtmlDoc(frameIndex));
                });
            }
            if (tabPane.getTabs().contains(markdownTab)
                    || tabPane.getTabs().contains(textTab)) {
                Platform.runLater(() -> {
                    markdown(getHtmlText(frameIndex));
                });
            }
            if (tabPane.getTabs().contains(backupTab)) {
                Platform.runLater(() -> {
                    backupController.loadBackups(sourceFile);
                });
            }
            if (tabPane.getTabs().contains(elementsTab)) {
                elementController.webEngine.loadContent("");
            }
            if (tabPane.getTabs().contains(findTab)) {
                findController.webEngine.loadContent("");
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
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            String html = null;
            if (tab == codesTab) {
                html = codesController.codes();

            } else if (tab == editorTab) {
                html = htmlEditor.getHtmlText();

            } else if (tab == markdownTab) {
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

    public void initFrames() {
        frameSelector.getItems().clear();
        NodeList frameList = webEngine.getDocument().getElementsByTagName("frame");
        if (frameList == null || frameList.getLength() < 1) {
            frameSelector.setVisible(false);
            return;
        }
        List<String> frames = new ArrayList<>();
        frames.add(message("Frameset"));
        for (int i = 0; i < frameList.getLength(); i++) {
            org.w3c.dom.Node node = frameList.item(i);
            if (node == null) {
                continue;
            }
            Element element = (Element) node;
            String src = element.getAttribute("src");
            String name = element.getAttribute("name");
            String frame = message("Frame") + i;
            if (name != null && !name.isBlank()) {
                frame += " :   " + name;
            } else if (src != null && !src.isBlank()) {
                frame += " :   " + src;
            }
            frames.add(frame);
        }
        isSettingValues = true;
        frameSelector.getItems().setAll(frames);
        frameSelector.setValue(message("Frameset"));
        isSettingValues = false;
        frameSelector.setVisible(true);
    }

    public int checkFrame() {
        if (!frameSelector.isVisible()) {
            return -1;
        }
        try {
            String frame = frameSelector.getValue();
            if (frame == null || frame.isBlank()
                    || frame.startsWith(message("Frameset"))
                    || !frame.startsWith(message("Frame"))) {
                return -1;
            }
            String indexStr = frame.substring(message("Frame").length());
            int pos = indexStr.indexOf(" :   ");
            if (pos >= 0) {
                indexStr = indexStr.substring(0, pos);
            }
            return Integer.parseInt(indexStr);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return -1;
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
                if (StringTools.include(html, "<FRAMESET ", true)) {
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

            int frameIndex = checkFrame();
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
                        richText(getHtmlText(frameIndex));
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
                        markdown(getHtmlText(frameIndex));
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
                        codes(getHtmlText(frameIndex));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowCodes", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Text"));
            checkMenu.setSelected(tabPane.getTabs().contains(textTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(textTab);
                if (c) {
                    tabPane.getTabs().remove(textTab);
                } else {
                    tabPane.getTabs().add(textTab);
                    Platform.runLater(() -> {
                        markdown(getHtmlText(frameIndex));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowText", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Headings"));
            checkMenu.setSelected(tabPane.getTabs().contains(tocTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(tocTab);
                if (c) {
                    tabPane.getTabs().remove(tocTab);
                } else {
                    tabPane.getTabs().add(tocTab);
                    Platform.runLater(() -> {
                        toc(getHtmlText(frameIndex));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowToc", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Links"));
            checkMenu.setSelected(tabPane.getTabs().contains(linksTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(linksTab);
                if (c) {
                    tabPane.getTabs().remove(linksTab);
                } else {
                    tabPane.getTabs().add(linksTab);
                    Platform.runLater(() -> {
                        links(getHtmlDoc(frameIndex));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowLinks", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Images"));
            checkMenu.setSelected(tabPane.getTabs().contains(imagesTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(imagesTab);
                if (c) {
                    tabPane.getTabs().remove(imagesTab);
                } else {
                    tabPane.getTabs().add(imagesTab);
                    Platform.runLater(() -> {
                        images(getHtmlDoc(frameIndex));
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowImages", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Elements"));
            checkMenu.setSelected(tabPane.getTabs().contains(elementsTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(elementsTab);
                if (c) {
                    tabPane.getTabs().remove(elementsTab);
                } else {
                    tabPane.getTabs().add(elementsTab);
                    elementController.webEngine.loadContent("");
                }
                AppVariables.setUserConfigValue(baseName + "ShowElements", !c);
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("Find"));
            checkMenu.setSelected(tabPane.getTabs().contains(findTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                boolean c = tabPane.getTabs().contains(findTab);
                if (c) {
                    tabPane.getTabs().remove(findTab);
                } else {
                    tabPane.getTabs().add(findTab);
                    findController.webEngine.loadContent("");
                }
                AppVariables.setUserConfigValue(baseName + "ShowFind", !c);
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
                        markdown(getHtmlText(frameIndex));
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
        if (!tabPane.getTabs().contains(markdownTab)
                && !tabPane.getTabs().contains(textTab)) {
            return;
        }
        if (html == null || html.isEmpty()) {
            markdownArea.setText("");
            textArea.setText("");
            if (pageLoaded) {
                markdownArea.setEditable(true);
            }
            return;
        }
        if (StringTools.include(html, "<FRAMESET ", true)) {
            markdownArea.setText(message("FrameSetAndSelectFrame"));
            textArea.setText(message("FrameSetAndSelectFrame"));
            mdChanged = false;
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String md, text;
                private Node document;

                @Override
                protected boolean handle() {
                    try {
                        md = htmlConverter.convert(html);
                        document = htmlParser.parse(md);
                        if (tabPane.getTabs().contains(textTab)) {
                            // https://github.com/vsch/flexmark-java/blob/master/flexmark-java-samples/src/com/vladsch/flexmark/java/samples/MarkdownToText.java
                            document = textParser.parse(md);
                            text = textCollectingVisitor.collectAndGetText(document);
                        }
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

    /*
        toc
     */
    protected void toc(String html) {
        tocArea.setText("");
        if (html == null || !tabPane.getTabs().contains(tocTab)) {
            return;
        }
        synchronized (this) {
            SingletonTask tocTask = new SingletonTask<Void>() {

                private String toc;

                @Override
                protected boolean handle() {
                    toc = HtmlTools.toc(html, 8);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tocArea.setText(toc);
                }

            };
            tocTask.setSelf(tocTask);
            Thread thread = new Thread(tocTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        links
     */
    protected void links(Document doc) {
        try {
            linksController.webEngine.loadContent("");
            if (doc == null || !tabPane.getTabs().contains(linksTab)) {
                return;
            }
            NodeList aList = doc.getElementsByTagName("a");
            if (aList == null || aList.getLength() < 1) {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(
                    message("Index"), message("Link"), message("Name"), message("Title"),
                    message("Address"), message("FullAddress")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (int i = 0; i < aList.getLength(); i++) {
                org.w3c.dom.Node node = aList.item(i);
                if (node == null) {
                    continue;
                }
                Element element = (Element) node;
                String href = element.getAttribute("href");
                if (href == null || href.isBlank()) {
                    continue;
                }
                String linkAddress = href;
                try {
                    URL url = new URL(new URL(element.getBaseURI()), href);
                    linkAddress = url.toString();
                } catch (Exception e) {
                }
                String name = element.getTextContent();
                String title = element.getAttribute("title");
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        index + "",
                        "<a href=\"" + linkAddress + "\">" + (name == null ? title : name) + "</a>",
                        name == null ? "" : name,
                        title == null ? "" : title,
                        URLDecoder.decode(href, webviewController.charset),
                        URLDecoder.decode(linkAddress, webviewController.charset)
                ));
                table.add(row);
                index++;
            }
            String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
            String html = HtmlTools.html(null, style, StringTable.tableDiv(table));
            linksController.webEngine.loadContent(html);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        images
     */
    protected void images(Document doc) {
        try {
            imagesController.webEngine.loadContent("");
            if (doc == null || !tabPane.getTabs().contains(imagesTab)) {
                return;
            }
            NodeList aList = doc.getElementsByTagName("img");
            if (aList == null || aList.getLength() < 1) {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(
                    message("Index"), message("Link"), message("Name"), message("Title"),
                    message("Address"), message("FullAddress")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (int i = 0; i < aList.getLength(); i++) {
                org.w3c.dom.Node node = aList.item(i);
                if (node == null) {
                    continue;
                }
                Element element = (Element) node;
                String href = element.getAttribute("src");
                if (href == null || href.isBlank()) {
                    continue;
                }
                String linkAddress = href;
                try {
                    URL url = new URL(new URL(element.getBaseURI()), href);
                    linkAddress = url.toString();
                } catch (Exception e) {
                }
                String name = element.getAttribute("alt");
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        index + "",
                        "<a href=\"" + linkAddress + "\">" + (name == null ? message("Link") : name) + "</a>",
                        "<img src=\"" + linkAddress + "\" " + (name == null ? "" : "alt=\"" + name + "\"") + " width=100/>",
                        name == null ? "" : name,
                        URLDecoder.decode(href, webviewController.charset),
                        URLDecoder.decode(linkAddress, webviewController.charset)
                ));
                table.add(row);
                index++;
            }
            String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
            String html = HtmlTools.html(null, style, StringTable.tableDiv(table));
            imagesController.webEngine.loadContent(html);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        elements
     */
    @FXML
    protected void queryElement() {
        try {
            String value = elementInputController.value();
            if (value == null || value.isBlank()) {
                popError(message("InvalidData"));
                return;
            }
            elementInputController.refreshList();
            elementController.webEngine.loadContent("");
            HTMLDocument doc = (HTMLDocument) getHtmlDoc(checkFrame());
            if (doc == null || !tabPane.getTabs().contains(elementsTab)) {
                return;
            }
            NodeList aList = null;
            Element element = null;
            if (tagRadio.isSelected()) {
                aList = doc.getElementsByTagName(value);
            } else if (idRadio.isSelected()) {
                element = doc.getElementById(value);
            } else if (nameRadio.isSelected()) {
                aList = doc.getElementsByName(value);
            }
            List<Element> elements = new ArrayList<>();
            if (aList != null && aList.getLength() > 0) {
                for (int i = 0; i < aList.getLength(); i++) {
                    org.w3c.dom.Node node = aList.item(i);
                    if (node != null) {
                        elements.add((Element) node);
                    }
                }
            } else if (element != null) {
                elements.add(element);
            }
            if (elements.isEmpty()) {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(
                    message("Index"), message("Tag"), message("Name"), message("Type"), message("Value"),
                    message("Attributes"), message("Texts")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (Element el : elements) {
                List<String> row = new ArrayList<>();
                NamedNodeMap attrs = el.getAttributes();
                String attrsString = "";
                if (attrs != null) {
                    for (int i = 0; i < attrs.getLength(); i++) {
                        org.w3c.dom.Node node = attrs.item(i);
                        attrsString += node.getNodeName() + "=" + node.getNodeValue() + " ";
                    }
                }
                row.addAll(Arrays.asList(
                        index + "", el.getTagName(), el.getNodeName(), el.getNodeType() + "", el.getNodeValue(),
                        attrsString, el.getTextContent()
                ));
                table.add(row);
                index++;
            }
            String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
            String html = HtmlTools.html(null, style, StringTable.tableDiv(table));
            elementController.webEngine.loadContent(html);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        find
     */
    @FXML
    protected void findString() {
        try {
            String value = findInputController.value();
            if (value == null || value.isBlank()) {
                popError(message("InvalidData"));
                return;
            }
            findInputController.refreshList();
            findController.webEngine.loadContent("");
            String currentHtml = getHtmlText(checkFrame());
            if (currentHtml == null || currentHtml.isBlank() || !tabPane.getTabs().contains(findTab)) {
                return;
            }
            String find = HtmlTools.encodeEscape(value);
            String replace = "<span style=\"color:" + findColorController.rgb()
                    + "; background: " + findBgColorController.rgb()
                    + "; font-size:1.2em;\">" + find + "</span>";
            FindReplaceString finder = FindReplaceString.create()
                    .setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setInputString(currentHtml).setFindString(find).setReplaceString(replace).setAnchor(0)
                    .setIsRegex(false).setCaseInsensitive(false).setMultiline(true);
            finder.run();
            foundCount = finder.getCount();
            findController.webEngine.loadContent(finder.getOutputString());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        others
     */
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
