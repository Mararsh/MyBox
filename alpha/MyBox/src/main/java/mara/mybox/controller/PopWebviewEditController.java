package mara.mybox.controller;

import java.util.Date;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2021-7-22
 * @License Apache License Version 2.0
 */
public class PopWebviewEditController extends PopTextBaseController {

    protected WebView webView;
    protected Element element;
    protected BaseWebViewController webViewController;

    @FXML
    protected Button selectButton, copyToMyBoxClipboardTextButton, copyToMyBoxClipboardHtmlButton,
            copyToSystemClipboardTextButton, copyToSystemClipboardHtmlButton;
    @FXML
    protected Label tagLabel, htmlLabel, textLabel;

    public PopWebviewEditController() {
        baseTitle = Languages.message("Edit");
    }

    public void setParameters(BaseWebViewController webViewController, Element element) {
        try {
            if (webViewController == null) {
                return;
            }
            this.webViewController = webViewController;
            this.webView = webViewController.webView;
            this.element = element;
            if (webView == null) {
                return;
            }
            titleLabel.setText(webView.getId());
            String tag;
            if (element != null) {
                tag = element.getTagName();
                tagLabel.setText(Languages.message("Tag") + ": " + tag);
            } else {
                tag = null;
                tagLabel.setText("");
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
        if (TextClipboardTools.isMonitoring()) {
            NodeTools.setTooltip(copyToSystemClipboardTextButton, new Tooltip(Languages.message("CopyToClipboards") + "\nCTRL+c / ALT+c"));
            NodeTools.setTooltip(copyToSystemClipboardHtmlButton, new Tooltip(Languages.message("CopyToClipboards") + "\nCTRL+c / ALT+c"));
        } else {
            NodeTools.setTooltip(copyToSystemClipboardTextButton, new Tooltip(Languages.message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c"));
            NodeTools.setTooltip(copyToSystemClipboardHtmlButton, new Tooltip(Languages.message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c"));
        }
        NodeTools.setTooltip(selectButton, new Tooltip(Languages.message("SelectNode")));
        selectButton.setDisable(element == null);

        String html = WebViewTools.getHtml(webView);
        bottomLabel.setText(Languages.message("Length") + ": " + (html == null ? "0" : html.length()));

        String htmlSelection = WebViewTools.selectedHtml(webView.getEngine());
        htmlLabel.setText(Languages.message("Selection") + ": " + (htmlSelection == null ? "0" : htmlSelection.length()));
        copyToSystemClipboardHtmlButton.setDisable(htmlSelection == null || htmlSelection.isBlank());
        copyToMyBoxClipboardHtmlButton.setDisable(copyToSystemClipboardHtmlButton.isDisable());

        String textSelection = WebViewTools.selectedText(webView.getEngine());
        textLabel.setText(Languages.message("Selection") + ": " + (textSelection == null ? "0" : textSelection.length()));
        copyToSystemClipboardTextButton.setDisable(textSelection == null || textSelection.isBlank());
        copyToMyBoxClipboardTextButton.setDisable(copyToSystemClipboardTextButton.isDisable());

    }

    @FXML
    public void selectAction() {
        if (webView == null || element == null) {
            return;
        }
        String id = element.getAttribute("id");
        String newid = new Date().getTime() + "";
        element.setAttribute("id", newid);
        WebViewTools.selectNode(webView.getEngine(), newid);
        if (id != null) {
            element.setAttribute("id", id);
        } else {
            element.removeAttribute("id");
        }
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

    @FXML
    public void copyTextClip() {
        if (webView == null) {
            return;
        }
        String text = WebViewTools.selectedText(webView.getEngine());
        if (text == null || text.isEmpty()) {
            popError(Languages.message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, text);
        checkWebviewPane();
    }

    @FXML
    public void copyTextMybox() {
        if (webView == null) {
            return;
        }
        String text = WebViewTools.selectedText(webView.getEngine());
        if (text == null || text.isEmpty()) {
            popError(Languages.message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToMyBoxClipboard(myController, text);
        checkWebviewPane();
    }

    @FXML
    public void copyHtmlClip() {
        if (webView == null) {
            return;
        }
        String html = WebViewTools.selectedHtml(webView.getEngine());
        if (html == null || html.isEmpty()) {
            popError(Languages.message("SelectedNone"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, html);
        checkWebviewPane();
    }

    @FXML
    public void copyHtmlMybox() {
        if (webView == null) {
            return;
        }
        String html = WebViewTools.selectedHtml(webView.getEngine());
        if (html == null || html.isEmpty()) {
            popError(Languages.message("SelectedNone"));
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

    /*
        static methods
     */
    public static PopWebviewEditController open(BaseWebViewController parent, Element element, double x, double y) {
        try {
            if (parent == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(parent, Fxmls.PopWebviewEditFxml, parent.webView, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof PopWebviewEditController)) {
                return null;
            }
            PopWebviewEditController controller = (PopWebviewEditController) object;
            controller.setParameters(parent, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }
}
