package mara.mybox.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.ContextMenuEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class MarkdownEditorController extends TextEditorController {

    protected MutableDataHolder htmlOptions;
    protected Parser htmlParser;
    protected HtmlRenderer htmlRenderer;
    protected long htmlPage, codesPage;
    protected double htmlScrollLeft, htmlScrollTop;

    @FXML
    protected TextArea codesArea;
    @FXML
    protected CheckBox wrapCodesCheck, refreshChangeHtmlCheck, refreshChangeCodesCheck;
    @FXML
    protected ControlWebView webViewController;

    public MarkdownEditorController() {
        baseTitle = message("MarkdownEditer");
        TipsLabelKey = "MarkdownEditerTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            webViewController.setParent(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setMarkdownType();
    }

    @Override
    public void makeEditContextMenu(javafx.scene.Node node) {
        try {
            if (node == mainArea) {
                node.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                    @Override
                    public void handle(ContextMenuEvent event) {
                        MenuMarkdownEditController.mdMenu(myController, node, event);
                    }
                });
            } else {
                super.makeEditContextMenu(node);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void initPairBox() {
        try {
            refreshChangeHtmlCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshChangeHtml", true));
            refreshChangeHtmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshChangeHtml", newValue);
                }
            });

            wrapCodesCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            wrapCodesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                UserConfig.setBoolean(baseName + "Wrap", wrapCodesCheck.isSelected());
                codesArea.setWrapText(wrapCodesCheck.isSelected());
            });
            codesArea.setWrapText(wrapCodesCheck.isSelected());

            refreshChangeCodesCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshChangeCodes", true));
            refreshChangeCodesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshChangeCodes", newValue);
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

    @Override
    protected void initPage(File file) {
        super.initPage(file);
        htmlPage = -1;
        codesPage = -1;
    }

    @FXML
    @Override
    public void refreshPairAction() {
        if (isSettingValues) {
            return;
        }
        markdown2html();
    }

    @Override
    protected void updatePairArea() {
        refreshPairAction();
    }

    @Override
    protected void clearPairArea() {
        try {
            codesArea.setText("");
            if (webViewController != null) {
                webViewController.loadContents(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // https://github.com/vsch/flexmark-java/wiki/Usage
    protected void makeHtmlConverter() {
        try {
            htmlOptions = MarkdownTools.htmlOptions();
            htmlParser = Parser.builder(htmlOptions).build();
            htmlRenderer = HtmlRenderer.builder(htmlOptions).build();

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        markdown2html(refreshChangeHtmlCheck.isSelected(), refreshChangeCodesCheck.isSelected());
    }

    protected void markdown2html(boolean updateHtml, boolean updateCodes) {
        if (webViewController == null || !updateHtml && !updateCodes) {
            return;
        }
        if (pairTask != null) {
            pairTask.cancel();
        }
        if (updateHtml) {
            webViewController.loadContents(null);
        }
        htmlScrollLeft = codesArea.getScrollLeft();
        htmlScrollTop = codesArea.getScrollTop();
        if (updateCodes) {
            codesArea.clear();
        }
        if (mainArea.getText().isEmpty()) {
            return;
        }
        pairTask = new FxTask<Void>(this) {

            private String html;

            @Override
            protected boolean handle() {
                try {
                    if (htmlOptions == null || htmlParser == null || htmlRenderer == null) {
                        makeHtmlConverter();
                    }
                    Node document = htmlParser.parse(mainArea.getText());
                    html = htmlRenderer.render(document);

                    html = HtmlWriteTools.html(null, html);
                    return html != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    if (updateHtml) {
                        webViewController.loadContents(html);
                        htmlPage = sourceInformation.getCurrentPage();
                    }
                    if (updateCodes) {
                        codesArea.setText(html);
                        codesPage = sourceInformation.getCurrentPage();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                codesArea.setScrollLeft(htmlScrollLeft);
                                codesArea.setScrollTop(htmlScrollTop);
                            }
                        }, 300);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

        };
        start(pairTask, false);
    }

    @FXML
    @Override
    public void createAction() {
        super.createAction();
        clearPairArea();
    }

    @FXML
    public void options() {
        MarkdownOptionsController.open(this);
    }

    @FXML
    public void editHtmlAction() {
        webViewController.editAction();
    }

    @FXML
    public void refreshHtml() {
        markdown2html(true, false);
    }

    @FXML
    public void refreshCodes() {
        markdown2html(false, true);
    }

    @FXML
    @Override
    public boolean popAction() {
        MarkdownPopController.open(this, mainArea);
        return true;
    }

    @FXML
    public void popHtmlAction() {
        webViewController.popAction();
    }

    @FXML
    public void popCodesAction() {
        TextPopController.openInput(this, codesArea);
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();
            Point2D localToScreen = mainArea.localToScreen(mainArea.getWidth() - 80, 80);
            MenuMarkdownEditController.mdMenu(myController, mainArea, localToScreen.getX(), localToScreen.getY());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    public boolean menuHtmlAction() {
        return webViewController.menuAction();
    }

    @FXML
    public boolean menuCodesAction() {
        try {
            Point2D localToScreen = codesArea.localToScreen(codesArea.getWidth() - 80, 80);
            MenuHtmlCodesController.htmlMenu(this, codesArea, localToScreen.getX(), localToScreen.getY());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    public void popHtmlFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("WebviewFunctionsPopWhenMouseHovering", true)) {
            showHtmlFunctionsMenu(event);
        }
    }

    @FXML
    public void showHtmlFunctionsMenu(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.showFunctionsMenu(event);
    }

    /*
        static
     */
    public static MarkdownEditorController open() {
        try {
            MarkdownEditorController controller = (MarkdownEditorController) WindowTools.openStage(Fxmls.MarkdownEditorFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MarkdownEditorController open(File file) {
        try {
            MarkdownEditorController controller = open();
            if (controller != null && file != null) {
                controller.sourceFileChanged(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MarkdownEditorController edit(String texts) {
        MarkdownEditorController controller = open();
        if (controller != null) {
            controller.loadContents(texts);
        }
        return controller;
    }

}
