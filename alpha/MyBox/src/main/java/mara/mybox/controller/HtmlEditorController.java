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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

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

    @FXML
    protected HBox addressBox;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab textsTab, codesTab, editorTab, markdownTab, backupTab;
    @FXML
    protected Button characterViewButton, characterCodesButton, synchronizeCodesButton, pasteTxtButton,
            characterEditorButton, synchronizeEditorButton, characterMarkdownButton, synchronizeMarkdownButton,
            characterTextsButton, synchronizeTextsButton;
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
        baseTitle = Languages.message("HtmlEditor");
        TipsLabelKey = "HtmlEditorTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initCodesTab();
            initEdtiorTab();
            initMarkdownTab();
            initTextsTab();
            initBackupsTab();

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

            String syn = Languages.message("SynchronizeChangesToOtherPanes") + "\nF1";
            NodeTools.setTooltip(synchronizeCodesButton, new Tooltip(syn));
            NodeTools.setTooltip(synchronizeEditorButton, new Tooltip(syn));
            NodeTools.setTooltip(synchronizeMarkdownButton, new Tooltip(syn));
            NodeTools.setTooltip(synchronizeTextsButton, new Tooltip(syn));
            NodeTools.setTooltip(pasteTxtButton, new Tooltip(Languages.message("PasteTexts")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        view
     */
    @Override
    protected void pageIsLoading() {
        super.pageIsLoading();
        pageLoaded = false;
        codesChanged(false);
        richTextChanged(false);
        markdownChanged(false);
        textsChanged(false);
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
        String info = address + "\n" + Languages.message("Loading...");
        codesArea.setText(info);
        markdownArea.setText(info);
        htmlEditor.setHtmlText(info);
        textsArea.setText(info);
    }

    @Override
    protected void afterPageLoaded() {
        super.afterPageLoaded();
        pageLoaded = true;
        synchronizeMain();
        saveButton.setDisable(false);
        saveAsButton.setDisable(false);
        synchronizeCodesButton.setDisable(false);
        synchronizeEditorButton.setDisable(false);
        synchronizeMarkdownButton.setDisable(false);
        synchronizeTextsButton.setDisable(false);
    }

    @Override
    protected void updateFileStatus(boolean changed) {
        String t = getBaseTitle();
        if (address != null) {
            t += "  " + address;
        } else if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        if (changed) {
            t += "*";
        }
        getMyStage().setTitle(t);
        if (!changed) {
            codesChanged(false);
            richTextChanged(false);
            markdownChanged(false);
            textsChanged(false);
        }
        fileChanged = changed;
    }

    @FXML
    public void synchronizeMain() {
        try {
            String html = WebViewTools.getHtml(webEngine);
            synchronizePair(html, false);
            Platform.runLater(() -> {
                backupController.loadBackups(sourceFile);
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean keyF1() {
        synchronizePair();
        return true;
    }

    @FXML
    public void refreshPair() {
        synchronizeMain();
    }

    @Override
    public String currentHtml() {
        try {
            if (framesDoc.isEmpty()) {
                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                if (tab == editorTab) {
                    return htmlEditor.getHtmlText();

                } else if (tab == markdownTab) {
                    Node document = htmlParser.parse(markdownArea.getText());
                    return HtmlWriteTools.html(null, htmlRender.render(document));
                } else if (tab == textsTab) {
                    return textsArea.getText(); /// ******
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return codesArea.getText();
    }

    @FXML
    public void synchronizePair() {
        synchronizePair(currentHtml(), true);
    }

    public void synchronizePair(String html, boolean updated) {
        try {
            Platform.runLater(() -> {
                htmlEditor.setDisable(true);
                if (StringTools.include(html, "<FRAMESET ", true)) {
                    htmlEditor.setHtmlText("<p>" + Languages.message("FrameSetAndSelectFrame") + "</p>");
                } else {
                    htmlEditor.setHtmlText(html);
                    if (pageLoaded) {
                        htmlEditor.setDisable(false);
                    }
                }
                richTextChanged(updated);
            });
            Platform.runLater(() -> {
                codesArea.setEditable(false);
                codesArea.setText(html);
                if (pageLoaded) {
                    codesArea.setEditable(true);
                }
                codesChanged(updated);
            });
            Platform.runLater(() -> {
                markdown(html, updated);
            });
            Platform.runLater(() -> {  // *******
                textsArea.setEditable(false);
                textsArea.setText(html);
                if (pageLoaded) {
                    textsArea.setEditable(true);
                }
                textsChanged(updated);
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (sourceFile == null) {
            saveAsAction();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String html = currentHtml();
            if (html == null) {
                popError(Languages.message("NoData"));
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try {
                        Charset charset = HtmlReadTools.htmlCharset(html);
                        File tmpFile = TmpFileTools.getTempFile();
                        try ( BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile, charset, false))) {
                            out.write(html);
                            out.flush();
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                        if (backupController.isBack()) {
                            backupController.addBackup(sourceFile);
                        }
                        return FileTools.rename(tmpFile, sourceFile);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    recordFileWritten(sourceFile);
                    updateFileStatus(false);
                    loadFile(sourceFile);
                }

            };
            handling(task);
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
            if (!checkBeforeNextAction()) {
                return;
            }
            sourceFile = null;
            urlSelector.getEditor().setText("");
            webEngine.loadContent(HtmlWriteTools.emptyHmtl());
            getMyStage().setTitle(getBaseTitle());
            updateFileStatus(false);
            saveButton.setDisable(false);
            saveAsButton.setDisable(false);
            synchronizeCodesButton.setDisable(true);
            synchronizeEditorButton.setDisable(true);
            synchronizeMarkdownButton.setDisable(true);
            synchronizeTextsButton.setDisable(true);
            backupController.loadBackups(null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        codes
     */
    protected void codesChanged(boolean changed) {
        codesChanged = changed;
        codesTab.setText(Languages.message("HtmlCodes") + (changed ? " *" : ""));
        codesLabel.setText(Languages.message("Total") + ": " + StringTools.format(codesArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void popCodesMenu(MouseEvent event) {
        popCodesMenu((javafx.scene.Node) event.getSource(), event.getScreenX() + 40, event.getScreenY() + 40);
    }

    public void popCodesMenu(javafx.scene.Node owner, double x, double y) {
        try {
            MenuTextEditController controller = MenuTextEditController.open(myController, codesArea, x, y);
            controller.setWidth(500);

            controller.addNode(new Separator());

            List<javafx.scene.Node> aNodes = new ArrayList<>();

            Button br = new Button(Languages.message("BreakLine"));
            br.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("<br>\n");
                }
            });
            aNodes.add(br);

            Button p = new Button(Languages.message("Paragraph"));
            p.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("\n<p>" + Languages.message("Paragraph") + "</p>\n");
                }
            });
            aNodes.add(p);

            Button table = new Button(Languages.message("Table"));
            table.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TableSizeController controller = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
                    controller.setParameters(myController);
                    controller.notify.addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            addTable(controller.rowsNumber, controller.colsNumber);
                            controller.closeStage();
                        }
                    });
                }
            });
            aNodes.add(table);

            Button tableRow = new Button(Languages.message("TableRow"));
            tableRow.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String value = PopTools.askValue(baseTitle, "", Languages.message("ColumnsNumber"), "3");
                    if (value == null) {
                        return;
                    }
                    try {
                        int colsNumber = Integer.valueOf(value);
                        if (colsNumber > 0) {
                            String s = "    <tr>";
                            for (int j = 1; j <= colsNumber; j++) {
                                s += "<td> v" + j + " </td>";
                            }
                            s += "</tr>\n";
                            insertCodesText(s);
                        } else {
                            popError(Languages.message("InvalidData"));
                        }
                    } catch (Exception e) {
                        popError(Languages.message("InvalidData"));
                    }
                }
            });
            aNodes.add(tableRow);

            Button image = new Button(Languages.message("Image"));
            image.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("<img src=\"https://mararsh.github.io/MyBox/iconGo.png\" alt=\"ReadMe\" />");
                }
            });
            aNodes.add(image);

            Button link = new Button(Languages.message("Link"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("<a href=\"https://github.com/Mararsh/MyBox\">MyBox</a>");
                }
            });
            aNodes.add(link);

            controller.addFlowPane(aNodes);
            controller.addNode(new Separator());

            List<javafx.scene.Node> headNodes = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                String name = Languages.message("Headings") + " " + i;
                int level = i;
                Button head = new Button(name);
                head.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        insertCodesText("<H" + level + ">" + name + "</H" + level + ">\n");
                    }
                });
                headNodes.add(head);
            }

            controller.addFlowPane(headNodes);
            controller.addNode(new Separator());

            List<javafx.scene.Node> listNodes = new ArrayList<>();
            Button numberedList = new Button(Languages.message("NumberedList"));
            numberedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("\n<ol>\n"
                            + "    <li>Item 1\n"
                            + "    </li>\n"
                            + "    <li>Item 2\n"
                            + "    </li>\n"
                            + "    <li>Item 3\n"
                            + "    </li>\n"
                            + "</ol>\n");
                }
            });
            listNodes.add(numberedList);

            Button bulletedList = new Button(Languages.message("BulletedList"));
            bulletedList.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("\n<ul>\n"
                            + "    <li>Item 1\n"
                            + "    </li>\n"
                            + "    <li>Item 2\n"
                            + "    </li>\n"
                            + "    <li>Item 3\n"
                            + "    </li>\n"
                            + "</ul>\n");
                }
            });
            listNodes.add(bulletedList);

            controller.addFlowPane(listNodes);
            controller.addNode(new Separator());

            List<javafx.scene.Node> codeNodes = new ArrayList<>();
            Button block = new Button(Languages.message("Block"));
            block.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("\n<div>\n" + Languages.message("Block") + "\n</div>\n");
                }
            });
            codeNodes.add(block);

            Button codes = new Button(Languages.message("Codes"));
            codes.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("\n<PRE><CODE> \n" + Languages.message("Codes") + "\n</CODE></PRE>\n");
                }
            });
            codeNodes.add(codes);

            Button local = new Button(Languages.message("ReferLocalFile"));
            local.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    File file = mara.mybox.fxml.FxFileTools.selectFile(myController, VisitHistory.FileType.All);
                    if (file != null) {
                        insertCodesText(UrlTools.decodeURL(file, Charset.defaultCharset()));
                    }
                }
            });
            codeNodes.add(local);

            Button style = new Button(Languages.message("Style"));
            style.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("<style type=\"text/css\">\n"
                            + "    table { max-width:95%; margin : 10px;  border-style: solid; border-width:2px; border-collapse: collapse;}\n"
                            + "    th, td { border-style: solid; border-width:1px; padding: 8px; border-collapse: collapse;}\n"
                            + "    th { font-weight:bold;  text-align:center;}\n"
                            + "</style>\n"
                    );
                }
            });
            codeNodes.add(style);

            Button separatorLine = new Button(Languages.message("SeparateLine"));
            separatorLine.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("\n<hr>\n");
                }
            });
            codeNodes.add(separatorLine);

            Button font = new Button(Languages.message("Font"));
            font.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("<font size=\"3\" color=\"red\">" + Languages.message("Font") + "</font>");
                }
            });
            codeNodes.add(font);

            Button bold = new Button(Languages.message("Bold"));
            bold.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("<b>" + Languages.message("Bold") + "</b>");
                }
            });
            codeNodes.add(bold);

            controller.addFlowPane(codeNodes);
            controller.addNode(new Separator());

            List<javafx.scene.Node> charNodes = new ArrayList<>();

            Button blank = new Button(Languages.message("Blank"));
            blank.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&nbsp;");
                }
            });
            charNodes.add(blank);

            Button lt = new Button(Languages.message("<"));
            lt.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&lt;");
                }
            });
            charNodes.add(lt);

            Button gt = new Button(Languages.message(">"));
            gt.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&gt;");
                }
            });
            charNodes.add(gt);

            Button and = new Button(Languages.message("&"));
            and.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&amp;");
                }
            });
            charNodes.add(and);

            Button quot = new Button(Languages.message("\""));
            quot.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&quot;");
                }
            });
            charNodes.add(quot);

            Button registered = new Button(Languages.message("Registered"));
            registered.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&reg;");
                }
            });
            charNodes.add(registered);

            Button copyright = new Button(Languages.message("Copyright"));
            copyright.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&copy;");
                }
            });
            charNodes.add(copyright);

            Button trademark = new Button(Languages.message("Trademark"));
            trademark.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertCodesText("&trade;");
                }
            });
            charNodes.add(trademark);

            controller.addFlowPane(charNodes);
            controller.addNode(new Separator());

            Hyperlink about = new Hyperlink(Languages.message("AboutHtml"));
            about.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (Languages.isChinese()) {
                        openLink("https://baike.baidu.com/item/html");
                    } else {
                        openLink("https://developer.mozilla.org/en-US/docs/Web/HTML");
                    }
                }
            });
            controller.addNode(about);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void insertCodesText(String string) {
        IndexRange range = codesArea.getSelection();
        codesArea.insertText(range.getStart(), string);
        codesArea.requestFocus();
    }

    public void addTable(int rowsNumber, int colsNumber) {
        String s = "<table>\n    <tr>";
        for (int j = 1; j <= colsNumber; j++) {
            s += "<th> col" + j + " </th>";
        }
        s += "</tr>\n";
        for (int i = 1; i <= rowsNumber; i++) {
            s += "    <tr>";
            for (int j = 1; j <= colsNumber; j++) {
                s += "<td> v" + i + "-" + j + " </td>";
            }
            s += "</tr>\n";
        }
        s += "</table>\n";
        insertCodesText(s);
    }

    @FXML
    public void pasteCodesTxt() {
        String string = TextClipboardTools.getSystemClipboardString();
        if (string == null || string.isBlank()) {
            popError(Languages.message("NoData"));
            return;
        }
        string = UrlTools.encodeEscape(string);
        insertCodesText(string);
    }

    @FXML
    public void clearCodes() {
        codesArea.clear();
    }

    @FXML
    public void editCodes() {
        TextEditerController controller = (TextEditerController) WindowTools.openStage(Fxmls.TextEditerFxml);
        controller.loadContexts(codesArea.getText());
        controller.toFront();
    }

    @FXML
    public void synchronizeCodes() {
        synchronizePair(codesArea.getText(), true);
    }

    /*
        richText
     */
    protected void richTextChanged(boolean changed) {
        heChanged = changed;
        editorTab.setText(Languages.message("RichText") + (changed ? " *" : ""));
        String c = htmlEditor.getHtmlText();
        int len = 0;
        if (c != null && !c.isEmpty()) {
            len = htmlEditor.getHtmlText().length();
        }
        editorLabel.setText(Languages.message("Total") + ": " + StringTools.format(len));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void popEditorMenu(MouseEvent mouseEvent) { // ****
        try {
            MenuTextEditController controller = MenuTextEditController.open(myController, markdownArea, mouseEvent);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void synchronizeEditor() { // ****
        synchronizePair(codesArea.getText(), true);
    }

    @FXML
    public void clearEditor() {
        codesArea.clear();
    }

    /*
        Markdown
     */
    protected void markdown(String html, boolean changed) {
        if (html == null || html.isEmpty()) {
            markdownArea.setText("");
            if (pageLoaded) {
                markdownArea.setEditable(true);
            }
            markdownChanged(changed);
            return;
        }
        if (StringTools.include(html, "<FRAMESET ", true)) {
            markdownArea.setText(Languages.message("FrameSetAndSelectFrame"));
            markdownChanged(changed);
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
                    Platform.runLater(() -> {
                        markdownArea.setText(md);
                        if (pageLoaded) {
                            markdownArea.setEditable(true);
                        }
                        markdownChanged(changed);
                    });
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void markdownChanged(boolean changed) {
        mdChanged = changed;
        markdownTab.setText("Markdown" + (changed ? " *" : ""));
        markdownLabel.setText(Languages.message("Total") + ": " + StringTools.format(markdownArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    protected void editMarkdown() {
        MarkdownEditerController controller
                = (MarkdownEditerController) openStage(Fxmls.MarkdownEditorFxml);
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
    public void synchronizeMarkdown() { // ****
        synchronizePair(codesArea.getText(), true);
    }

    /*
        texts
     */
    protected void textsChanged(boolean changed) {
        textsChanged = changed;
        textsTab.setText(Languages.message("Texts") + (changed ? " *" : ""));
        textsLabel.setText(Languages.message("Total") + ": " + StringTools.format(textsArea.getLength()));
        if (changed) {
            updateFileStatus(true);
        }
    }

    @FXML
    public void popTextsMenu(MouseEvent mouseEvent) {
        MenuTextEditController.open(myController, markdownArea, mouseEvent);
    }

    @FXML
    public void synchronizeTexts() { // ****
        synchronizePair(codesArea.getText(), true);
    }

    @FXML
    protected void editTexts() {
        MarkdownEditerController controller
                = (MarkdownEditerController) openStage(Fxmls.MarkdownEditorFxml);
        controller.loadMarkdown(markdownArea.getText());
    }

    @FXML
    protected void clearTexts() {
        textsArea.clear();
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!fileChanged) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(Languages.message("FileChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
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

}
