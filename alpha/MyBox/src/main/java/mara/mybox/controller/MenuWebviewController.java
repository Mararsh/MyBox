package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
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

    @FXML
    protected Button copyToMyBoxClipboardTextButton, copyToMyBoxClipboardHtmlButton,
            copyToSystemClipboardTextButton, copyToSystemClipboardHtmlButton;
    @FXML
    protected Label tagLabel, htmlLabel, textLabel;

    public MenuWebviewController() {
        baseTitle = message("Html");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    public void setParameters(ControlWebView webViewController, Element element) {
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

            webView.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    checkWebviewPane();
                }
            });

            setControlsStyle();
            checkWebviewPane();

            if (webViewController instanceof ControlHtmlEditor) {
                setTitleid(((ControlHtmlEditor) webViewController).htmlEditor.getId());
            } else {
                setTitleid(webView.getId());
            }

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
                bottomLabel.setText(message("Count") + ": " + (html == null ? "0" : StringTools.format(html.length())));

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
            return webViewController.keyEventsFilter(event);
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
    @Override
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        webViewController.popFunctionsMenu(mouseEvent);
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
    public void popHtmlStyle(MouseEvent mouseEvent) {
        PopTools.popHtmlStyle(mouseEvent, webViewController);
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
        webViewController.refresh(false);
    }

    public void backAction() {
        webViewController.backAction();
    }

    public void forwardAction() {
        webViewController.forwardAction();
    }

    /*
        static methods
     */
    public static MenuWebviewController pop(ControlWebView parent, Element element, double x, double y) {
        try {
            if (parent == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(parent, Fxmls.MenuWebviewFxml, parent.webView, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuWebviewController)) {
                return null;
            }
            MenuWebviewController controller = (MenuWebviewController) object;
            controller.setParameters(parent, element);
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
