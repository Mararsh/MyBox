package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2021-7-22
 * @License Apache License Version 2.0
 */
public class MenuWebviewController extends MenuController {

    protected HTMLEditor editor;
    protected WebView webView;
    protected Element element;
    protected BaseWebViewController webViewController;

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

    public void setParameters(BaseWebViewController webViewController, WebView webview, Element element) {
        try {
            if (webViewController == null) {
                return;
            }
            this.webViewController = webViewController;
            this.webView = webview;
            this.element = element;
            if (webView == null) {
                return;
            }
            if (webView.getId() == null) {
                editor = WebViewTools.editor(webView);
                if (editor != null) {
                    titleLabel.setText(editor.getId());
                }
            } else {
                titleLabel.setText(webView.getId());
            }

            webView.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    checkWebviewPane();
                }
            });

            setControlsStyle();
            checkWebviewPane();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkWebviewPane() {
        if (webView == null) {
            return;
        }
        String tag;
        if (element != null) {
            tag = element.getTagName();
            tagLabel.setText(message("Tag") + ": " + tag);
        } else {
            tagLabel.setText("");
        }
        if (TextClipboardTools.isMonitoring()) {
            NodeStyleTools.setTooltip(copyToSystemClipboardTextButton, new Tooltip(message("CopyToClipboards") + "\nCTRL+c / ALT+c / CTRL+t / ALT+t"));
            NodeStyleTools.setTooltip(copyToSystemClipboardHtmlButton, new Tooltip(message("CopyToClipboards") + "\nCTRL+h / ALT+h"));
        } else {
            NodeStyleTools.setTooltip(copyToSystemClipboardTextButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c / CTRL+t / ALT+t"));
            NodeStyleTools.setTooltip(copyToSystemClipboardHtmlButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+h / ALT+h"));
        }
        NodeStyleTools.setTooltip(selectButton, new Tooltip(message("SelectNode") + "\nCTRL+u / ALT+u"));
        selectButton.setDisable(element == null);

        String html = WebViewTools.getHtml(webView);
        bottomLabel.setText(message("Length") + ": " + (html == null ? "0" : html.length()));

        String htmlSelection = WebViewTools.selectedHtml(webView.getEngine());
        htmlLabel.setText(message("Selection") + ": " + (htmlSelection == null ? "0" : htmlSelection.length()));
        copyToSystemClipboardHtmlButton.setDisable(htmlSelection == null || htmlSelection.isBlank());
        copyToMyBoxClipboardHtmlButton.setDisable(copyToSystemClipboardHtmlButton.isDisable());

        String textSelection = WebViewTools.selectedText(webView.getEngine());
        textLabel.setText(message("Selection") + ": " + (textSelection == null ? "0" : textSelection.length()));
        copyToSystemClipboardTextButton.setDisable(textSelection == null || textSelection.isBlank());
        copyToMyBoxClipboardTextButton.setDisable(copyToSystemClipboardTextButton.isDisable());

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
    public boolean controlAltT() {
        copyTextToSystemClipboard();
        return true;
    }

    @FXML
    public void copyTextToSystemClipboard() {
        if (webView == null) {
            return;
        }
        String text = WebViewTools.selectedText(webView.getEngine());
        if (text == null || text.isEmpty()) {
            popError(message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, text);
        checkWebviewPane();
    }

    @FXML
    public void copyTextToMyboxClipboard() {
        if (webView == null) {
            return;
        }
        String text = WebViewTools.selectedText(webView.getEngine());
        if (text == null || text.isEmpty()) {
            popError(message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToMyBoxClipboard(myController, text);
        checkWebviewPane();
    }

    @Override
    public boolean controlAltH() {
        copyHtmlToSystemClipboard();
        return true;
    }

    @FXML
    public void copyHtmlToSystemClipboard() {
        if (webView == null) {
            return;
        }
        String html = WebViewTools.selectedHtml(webView.getEngine());
        if (html == null || html.isEmpty()) {
            popError(message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, html);
        checkWebviewPane();
    }

    @FXML
    public void copyHtmlToMyboxClipboard() {
        if (webView == null) {
            return;
        }
        String html = WebViewTools.selectedHtml(webView.getEngine());
        if (html == null || html.isEmpty()) {
            popError(message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToMyBoxClipboard(myController, html);
        checkWebviewPane();
    }

    @FXML
    @Override
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        if (webViewController == null) {
            return;
        }
        webViewController.popFunctionsMenu(mouseEvent);
    }

    @FXML
    @Override
    public void findAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.findAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.saveAsAction();
    }

    @FXML
    public void editAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.editAction();
    }

    @FXML
    @Override
    public void popAction() {
        if (webViewController == null) {
            return;
        }
        webViewController.popAction();
    }

    /*
        static methods
     */
    public static MenuWebviewController pop(BaseWebViewController parent, WebView webview, Element element, double x, double y) {
        try {
            if (parent == null || webview == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(parent, Fxmls.MenuWebviewFxml, webview, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuWebviewController)) {
                return null;
            }
            MenuWebviewController controller = (MenuWebviewController) object;
            controller.setParameters(parent, webview, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MenuWebviewController pop(BaseWebViewController parent, Element element, double x, double y) {
        try {
            return pop(parent, parent.webView, element, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static MenuWebviewController pop(WebView webview, Element element, double x, double y) {
        try {
            return pop((BaseWebViewController) (webview.getUserData()), webview, element, x, y);
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
