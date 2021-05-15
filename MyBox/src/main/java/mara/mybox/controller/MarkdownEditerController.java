package mara.mybox.controller;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class MarkdownEditerController extends TextEditerController {

    protected WebEngine webEngine;
    protected MutableDataSet htmlOptions;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRenderer;
    protected int indentSize = 4;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab htmlTab, codesTab;
    @FXML
    protected TextArea htmlArea;
    @FXML
    protected ComboBox<String> emulationSelector, indentSelector, styleSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck;
    @FXML
    protected TextField titleInput;
    @FXML
    protected TitledPane conversionPane;
    @FXML
    protected ControlWebview webviewController;

    public MarkdownEditerController() {
        baseTitle = AppVariables.message("MarkdownEditer");
        TipsLabelKey = "MarkdownEditerTips";
    }

    @Override
    public void setFileType() {
        setMarkdownType();
    }

    @Override
    public void initControls() {
        try {
//            initPage(null);
            super.initControls();

            initHtmlTab();
            initConversionOptions();

            if (!AppVariables.getUserConfigBoolean(baseName + "ShowHtml", true)) {
                tabPane.getTabs().remove(htmlTab);
            }
            htmlTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    AppVariables.setUserConfigValue(baseName + "ShowHtml", false);
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initHtmlTab() {
        try {
            webviewController.setValues(this);
            webEngine = webviewController.webView.getEngine();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void initConversionOptions() {
        try {
            conversionPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "ConversionPane", true));
            conversionPane.expandedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "ConversionPane", conversionPane.isExpanded());
                        updateHtmlConverter();
                    });

            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Emulation", "GITHUB"));
            emulationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "Emulation", newValue);
                    updateHtmlConverter();
                }
            });

            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Indent", "4"));
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                            AppVariables.setUserConfigValue(baseName + "Indent", newValue);
                            updateHtmlConverter();
                        }
                    } catch (Exception e) {
                    }
                }
            });

            trimCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Trim", false));
            trimCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Indent", trimCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            appendCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Append", false));
            appendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Append", appendCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            discardCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Discard", false));
            discardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Discard", discardCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            linesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Trim", false));
            linesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Trim", linesCheck.isSelected());
                    updateHtmlConverter();
                }
            });

            List<String> styles = new ArrayList<>();
            for (HtmlTools.HtmlStyle style : HtmlTools.HtmlStyle.values()) {
                styles.add(message(style.name()));
            }
            styleSelector.getItems().addAll(styles);
            styleSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "HtmlStyle", message("Default")));
            styleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVariables.setUserConfigValue(baseName + "HtmlStyle", newValue);
                    updateHtmlConverter();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void refreshPairAction() {
        if (isSettingValues || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        markdown2html();
    }

    @Override
    protected void setPairAreaSelection() {
    }

    @Override
    protected void scrollTopPairArea(double value) {
    }

    @Override
    protected void scrollLeftPairArea(double value) {
    }

    @Override
    protected void clearPairArea() {
        htmlArea.setText("");
        if (webEngine != null) {
            webEngine.loadContent("");
        }
    }

    // https://github.com/vsch/flexmark-java/wiki/Usage
    protected void makeHtmlConverter() {
        try {
            htmlOptions = new MutableDataSet();
            htmlOptions.setFrom(ParserEmulationProfile.valueOf(emulationSelector.getValue()));
            htmlOptions.set(Parser.EXTENSIONS, Arrays.asList(
                    AbbreviationExtension.create(),
                    DefinitionExtension.create(),
                    FootnoteExtension.create(),
                    TablesExtension.create(),
                    TypographicExtension.create()
            ));

            htmlOptions.set(HtmlRenderer.INDENT_SIZE, indentSize)
                    //                    .set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
                    //                    .set(TablesExtension.COLUMN_SPANS, false)
                    .set(TablesExtension.TRIM_CELL_WHITESPACE, trimCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected())
                    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, discardCheck.isSelected())
                    .set(TablesExtension.APPEND_MISSING_COLUMNS, appendCheck.isSelected());

            htmlParser = Parser.builder(htmlOptions).build();
            htmlRenderer = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void updateHtmlConverter() {
        makeHtmlConverter();
        markdown2html();
    }

    public void loadMarkdown(String md) {
        mainArea.setText(md);
        markdown2html();
    }

    protected void markdown2html() {
        if (webEngine == null
                || !tabPane.getTabs().contains(htmlTab)
                && !tabPane.getTabs().contains(codesTab)) {
            return;
        }
        webEngine.getLoadWorker().cancel();
        webEngine.loadContent("");
        htmlArea.clear();
        if (mainArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            double htmlScrollLeft = htmlArea.getScrollLeft();
            double htmlScrollTop = htmlArea.getScrollTop();
            int htmlAnchor = htmlArea.getAnchor();
            int htmlCaretPosition = htmlArea.getCaretPosition();
            double htmlWidth = (Integer) webEngine.executeScript("document.documentElement.scrollWidth || document.body.scrollWidth;");
            double htmlHeight = (Integer) webEngine.executeScript("document.documentElement.scrollHeight || document.body.scrollHeight;");

            task = new SingletonTask<Void>() {

                private String html;

                @Override
                protected boolean handle() {
                    try {
                        if (htmlOptions == null || htmlParser == null || htmlRenderer == null) {
                            makeHtmlConverter();
                        }
                        Node document = htmlParser.parse(mainArea.getText());
                        html = htmlRenderer.render(document);

                        String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", message("Default"));
                        html = HtmlTools.html(titleInput.getText(), style, html);
                        return html != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        if (tabPane.getTabs().contains(codesTab)) {
                            Platform.runLater(() -> {
                                htmlArea.setText(html);
                                htmlArea.setScrollLeft(htmlScrollLeft);
                                htmlArea.setScrollTop(htmlScrollTop);
                                htmlArea.selectRange(htmlAnchor, htmlCaretPosition);
                            });
                        }
                        if (tabPane.getTabs().contains(htmlTab)) {
                            Platform.runLater(() -> {
                                webEngine.loadContent(html);
                                webEngine.executeScript("window.scrollTo(" + htmlWidth + "," + htmlHeight + ");");
                            });
                        }

                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        webEngine.getLoadWorker().cancel();
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
    public void popPanesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            CheckMenuItem updateMenu = new CheckMenuItem(message("UpdateSynchronously"));
            updateMenu.setSelected(AppVariables.getUserConfigBoolean(baseName + "UpdateSynchronously", false));
            updateMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.setUserConfigValue(baseName + "UpdateSynchronously", updateMenu.isSelected());
                    if (updateMenu.isSelected()) {
                        updatePairArea();
                    }
                }
            });
            popMenu.getItems().add(updateMenu);
            popMenu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem checkMenu;
            checkMenu = new CheckMenuItem(message("Html"));
            checkMenu.setSelected(tabPane.getTabs().contains(htmlTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(htmlTab)) {
                    tabPane.getTabs().remove(htmlTab);
                } else {
                    tabPane.getTabs().add(htmlTab);
                    Platform.runLater(() -> {
                        markdown2html();
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowHtml",
                        tabPane.getTabs().contains(htmlTab));
            });
            popMenu.getItems().add(checkMenu);

            checkMenu = new CheckMenuItem(message("HtmlCodes"));
            checkMenu.setSelected(tabPane.getTabs().contains(codesTab));
            checkMenu.setOnAction((ActionEvent event) -> {
                if (tabPane.getTabs().contains(codesTab)) {
                    tabPane.getTabs().remove(codesTab);
                } else {
                    tabPane.getTabs().add(codesTab);
                    Platform.runLater(() -> {
                        markdown2html();
                    });
                }
                AppVariables.setUserConfigValue(baseName + "ShowCodes",
                        tabPane.getTabs().contains(codesTab));
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
        Common methods
     */
    protected void insertText(String string) {
        IndexRange range = mainArea.getSelection();
        mainArea.insertText(range.getStart(), string);
        mainArea.requestFocus();
    }

    protected void addTextInFrontOfCurrentLine(String string) {
        IndexRange range = mainArea.getSelection();
        int first = range.getStart();
        while (first > 0) {
            if ("\n".equals(mainArea.getText(first - 1, first))) {
                break;
            }
            first--;
        }
        mainArea.requestFocus();
        mainArea.insertText(first, string);
    }

    @FXML
    protected void addTextInFrontOfEachLine(String prefix) {
        IndexRange range = mainArea.getSelection();
        int start = range.getStart();
        int end = range.getEnd();
        addTextInFrontOfCurrentLine(prefix);

        if (start == end) {
            return;
        }
        int prefixLen = prefix.length();
        start += prefixLen;
        end += prefixLen;
        int pos;
        while (true) {
            pos = mainArea.getText(start, end).indexOf('\n');
            if (pos < 0) {
                break;
            }
            mainArea.insertText(start + pos + 1, prefix);
            start += pos + prefixLen + 1;
            end += prefixLen;
            int len = mainArea.getLength();
            if (start >= end || start >= len || end >= len) {
                break;
            }
        }
        mainArea.requestFocus();
    }

    protected void addTextAround(String string) {
        addTextAround(string, string);
    }

    protected void addTextAround(String prefix, String suffix) {
        IndexRange range = mainArea.getSelection();
        if (range.getLength() == 0) {
            String s = prefix + message("Text") + suffix;
            mainArea.insertText(range.getStart(), s);
            mainArea.selectRange(range.getStart() + prefix.length(),
                    range.getStart() + prefix.length() + message("Text").length());
        } else {
            mainArea.insertText(range.getStart(), prefix);
            mainArea.insertText(range.getEnd() + prefix.length(), suffix);
        }
        mainArea.requestFocus();
    }

    /*
        Input formats
     */
    @FXML
    public void popListMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("NumberedList"));
            menu.setOnAction((ActionEvent event) -> {
                IndexRange range = mainArea.getSelection();
                int start = range.getStart();
                int end = range.getEnd();
                addTextInFrontOfCurrentLine("1. ");
                if (start == end) {
                    return;
                }
                start += 3;
                end += 3;
                int pos;
                int count = 1;
                while (true) {
                    pos = mainArea.getText(start, end).indexOf('\n');
                    if (pos < 0) {
                        break;
                    }
                    count++;
                    mainArea.insertText(start + pos + 1, count + ". ");
                    int nlen = 2 + (count + "").length();
                    start += pos + 1 + nlen;
                    end += nlen;
                    int len = mainArea.getLength();
                    if (start >= end || start >= len || end >= len) {
                        break;
                    }
                }
                mainArea.requestFocus();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("BulletedList"));
            menu.setOnAction((ActionEvent event) -> {
                addTextInFrontOfEachLine("- ");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
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

    @FXML
    public void popHeaderMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;
            for (int i = 1; i <= 6; i++) {
                String name = message("Headings") + " " + i;
                String value = "";
                for (int h = 0; h < i; h++) {
                    value += "#";
                }
                String h = value + " ";
                menu = new MenuItem(name);
                menu.setOnAction((ActionEvent event) -> {
                    addTextInFrontOfCurrentLine(h);
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
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

    @FXML
    public void popCodesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Bold"));
            menu.setOnAction((ActionEvent event) -> {
                addTextAround("**");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Italic"));
            menu.setOnAction((ActionEvent event) -> {
                addTextAround("*");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("BoldItalic"));
            menu.setOnAction((ActionEvent event) -> {
                addTextAround("***");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Quote"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("\n\n>");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Codes"));
            menu.setOnAction((ActionEvent event) -> {
                addTextAround("`");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("CodesBlock"));
            menu.setOnAction((ActionEvent event) -> {
                addTextAround("\n```\n", "\n```\n");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ReferLocalFile"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxmlControl.selectFile(this, VisitHistory.FileType.All);
                if (file == null) {
                    return;
                }
                insertText(HtmlTools.decodeURL(file));
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SeparatorLine"));
            menu.setOnAction((ActionEvent event) -> {
                insertText("\n---\n");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
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

    @FXML
    public void addImage() {
        insertText("![" + message("Name") + "](http://" + message("Address") + ")");
    }

    @FXML
    public void addlink() {
        insertText("[" + message("Name") + "](http://" + message("Address") + ")");
    }

    @FXML
    public void addTable() {
        TableSizeController controller = (TableSizeController) openStage(CommonValues.TableSizeFxml, true);
        controller.setValues(this);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                addTable(controller.rowsNumber, controller.colsNumber);
                controller.closeStage();
            }
        });
    }

    public void addTable(int rowsNumber, int colsNumber) {
        String s = "\n|";
        for (int j = 1; j <= colsNumber; j++) {
            s += " col" + j + " |";
        }
        s += "    \n";
        for (int i = 1; i <= rowsNumber; i++) {
            s += "|";
            for (int j = 1; j <= colsNumber; j++) {
                s += " v" + i + "-" + j + " |";
            }
            s += "    \n";
        }
        insertText(s);
    }

    @FXML
    public void addP() {
        insertText("    \n" + message("Paragraph") + "    \n");
    }

    @FXML
    public void addBr() {
        insertText("    \n");
    }

    @FXML
    @Override
    public void clearAction() {
        mainArea.clear();
    }

    /*
        Buttons Action
     */
    @Override
    public MarkdownEditerController openNewStage() {
        return (MarkdownEditerController) openStage(CommonValues.MarkdownEditorFxml);
    }

    @FXML
    protected void editHtml() {
        String text = htmlArea.getText();
        if (text.isEmpty()) {
            return;
        }
        HtmlEditorController controller
                = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
        controller.loadContents(text);
    }

    @FXML
    @Override
    public void createAction() {
        super.createAction();
        clearPairArea();
    }

}
