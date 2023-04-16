package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2021-7-22
 * @License Apache License Version 2.0
 */
public class MenuWebviewController extends MenuController {

    protected WebView webView;
    protected Element element;
    protected ControlWebView webViewController;
    protected EventHandler<MouseEvent> mouseReleasedHandler;

    @FXML
    protected Button copyToMyBoxClipboardTextButton, copyToMyBoxClipboardHtmlButton,
            copyToSystemClipboardTextButton, copyToSystemClipboardHtmlButton;
    @FXML
    protected Label tagLabel, htmlLabel, textLabel;
    @FXML
    protected CheckBox editableCheck;

    public MenuWebviewController() {
        baseTitle = message("Html");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(editableCheck, new Tooltip(message("Editable") + "\n" + message("HtmlEditableComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(ControlWebView webViewController, Element element, double x, double y) {
        try {
            if (webViewController == null) {
                return;
            }
            this.baseName = webViewController.baseName;
            this.webViewController = webViewController;
            this.webView = webViewController.webView;
            this.element = element;
            if (webView == null) {
                return;
            }

            mouseReleasedHandler = new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    checkWebviewPane();
                }
            };
            webView.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);

            checkWebviewPane();

            setTitleid(webView.getId());
            editableCheck.setSelected(UserConfig.getBoolean("WebViewEditable", false));
            editableCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    webViewController.setEditable(editableCheck.isSelected());
                }
            });
            super.setParameters(webViewController, webView, x, y);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkWebviewPane() {
        if (webView == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tag;
                if (element != null) {
                    tag = element.getTagName();
                    tagLabel.setText(message("Tag") + ": " + tag);
                } else {
                    tagLabel.setText("");
                }
                if (TextClipboardTools.isMonitoringCopy()) {
                    NodeStyleTools.setTooltip(copyToSystemClipboardTextButton, new Tooltip(message("CopyToClipboards") + "\nCTRL+c / ALT+c / CTRL+t / ALT+t"));
                    NodeStyleTools.setTooltip(copyToSystemClipboardHtmlButton, new Tooltip(message("CopyToClipboards") + "\nCTRL+h / ALT+h"));
                } else {
                    NodeStyleTools.setTooltip(copyToSystemClipboardTextButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c / CTRL+t / ALT+t"));
                    NodeStyleTools.setTooltip(copyToSystemClipboardHtmlButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+h / ALT+h"));
                }
                NodeStyleTools.setTooltip(selectButton, new Tooltip(message("SelectNode") + "\nCTRL+u / ALT+u"));
                selectButton.setDisable(element == null);

                String html = WebViewTools.getHtml(webView);
                bottomLabel.setText(message("CharactersNumber") + ": " + (html == null ? "0" : StringTools.format(html.length())));

                String htmlSelection = WebViewTools.selectedHtml(webView.getEngine());
                htmlLabel.setText(message("Selection") + ": " + (htmlSelection == null ? "0" : StringTools.format(htmlSelection.length())));
                copyToSystemClipboardHtmlButton.setDisable(htmlSelection == null || htmlSelection.isBlank());
                copyToMyBoxClipboardHtmlButton.setDisable(copyToSystemClipboardHtmlButton.isDisable());

                String textSelection = WebViewTools.selectedText(webView.getEngine());
                textLabel.setText(message("Selection") + ": " + (textSelection == null ? "0" : StringTools.format(textSelection.length())));
                copyToSystemClipboardTextButton.setDisable(textSelection == null || textSelection.isBlank());
                copyToMyBoxClipboardTextButton.setDisable(copyToSystemClipboardTextButton.isDisable());

            }
        });
    }

    public void setElement(Element element) {
        this.element = element;
        checkWebviewPane();
    }

    @FXML
    @Override
    public void selectAction() {
        if (webView == null || element == null) {
            return;
        }
        WebViewTools.selectElement(webView, element);
        checkWebviewPane();
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (webView == null) {
            return;
        }
        WebViewTools.selectAll(webView.getEngine());
        checkWebviewPane();
    }

    @FXML
    @Override
    public void selectNoneAction() {
        if (webView == null) {
            return;
        }
        WebViewTools.selectNone(webView.getEngine());
        checkWebviewPane();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (webViewController != null) {
                return webViewController.keyEventsFilter(event);
            }
        }
        return true;
    }

    @Override
    public boolean controlAltT() {
        copyTextToSystemClipboard();
        return true;
    }

    @FXML
    public void copyTextToSystemClipboard() {
        if (webViewController.copyTextToSystemClipboard()) {
            checkWebviewPane();
        }
    }

    @FXML
    public void copyTextToMyboxClipboard() {
        if (webViewController.copyTextToMyboxClipboard()) {
            checkWebviewPane();
        }
    }

    @Override
    public boolean controlAltH() {
        copyHtmlToSystemClipboard();
        return true;
    }

    @FXML
    public void copyHtmlToSystemClipboard() {
        if (webViewController.copyHtmlToSystemClipboard()) {
            checkWebviewPane();
        }
    }

    @FXML
    public void copyHtmlToMyboxClipboard() {
        if (webViewController.copyHtmlToMyboxClipboard()) {
            checkWebviewPane();
        }
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("WebviewFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        if (webViewController == null) {
            return;
        }
        webViewController.showFunctionsMenu(event);
    }

    @FXML
    @Override
    public void findAction() {
        webViewController.findAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        webViewController.saveAsAction();
    }

    @FXML
    public void editAction() {
        webViewController.editAction();
    }

    @FXML
    protected void showHtmlStyle(Event event) {
        PopTools.popHtmlStyle(event, webViewController);
    }

    @FXML
    protected void popHtmlStyle(Event event) {
        if (UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false)) {
            showHtmlStyle(event);
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        webViewController.popAction();
        return true;
    }

    public void zoomIn() {
        webViewController.zoomIn();
    }

    public void zoomOut() {
        webViewController.zoomOut();
    }

    public void refreshAction() {
        webViewController.refresh();
    }

    public void backAction() {
        webViewController.backAction();
    }

    public void forwardAction() {
        webViewController.forwardAction();
    }

    @FXML
    public void snapAction() {
        if (webView == null) {
            return;
        }
        ImageViewerController.openImage(NodeTools.snap(webView));
    }

    @FXML
    public void scriptAction() {
        if (webViewController == null) {
            return;
        }
        JavaScriptController.open(webViewController);
    }

    @Override
    public void cleanPane() {
        try {
            if (webView != null) {
                webView.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
            mouseReleasedHandler = null;
            webViewController = null;
            webView = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static MenuWebviewController pop(ControlWebView parent, Element element, double x, double y) {
        try {
            if (parent == null) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof MenuWebviewController) {
                    try {
                        MenuWebviewController controller = (MenuWebviewController) object;
                        if (controller.webView != null && controller.webView.equals(parent.webView)) {
                            controller.close();
                        }
                    } catch (Exception e) {
                    }
                }
            }
            MenuWebviewController controller = (MenuWebviewController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.MenuWebviewFxml, false);
            controller.setParameters(parent, element, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MenuWebviewController running(WebView webview) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MenuWebviewController) {
                MenuWebviewController controller = (MenuWebviewController) object;
                if (webview == controller.webView) {
                    return controller;
                }
            }
        }
        return null;
    }

}
