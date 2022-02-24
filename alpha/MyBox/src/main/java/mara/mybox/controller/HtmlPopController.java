package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-7
 * @License Apache License Version 2.0
 */
public class HtmlPopController extends BaseWebViewController {

    protected WebView sourceWebView;
    protected ChangeListener listener;

    @FXML
    protected CheckBox refreshChangeCheck;
    @FXML
    protected Button refreshButton;

    public HtmlPopController() {
        baseTitle = Languages.message("Html");
    }

    @Override
    public void setStageStatus() {
    }

    public void setControls() {
        try {
            listener = new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                    if (refreshChangeCheck.isVisible() && refreshChangeCheck.isSelected()) {
                        if (newState == Worker.State.SUCCEEDED) {
                            refreshAction();
                        }
                    }
                }
            };

            refreshChangeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldState, Boolean newState) {
                    if (sourceWebView == null) {
                        refreshChangeCheck.setVisible(false);
                        refreshButton.setVisible(false);
                        return;
                    }
                    if (refreshChangeCheck.isVisible() && refreshChangeCheck.isSelected()) {
                        sourceWebView.getEngine().getLoadWorker().stateProperty().addListener(listener);
                    } else {
                        sourceWebView.getEngine().getLoadWorker().stateProperty().removeListener(listener);
                    }
                }
            });
            refreshChangeCheck.setSelected(UserConfig.getBoolean(baseName + "Sychronized", true));

            setAsPop(baseName);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void openWebView(String baseName, WebView sourceWebView, String address) {
        try {
            this.baseName = baseName;

            this.sourceWebView = sourceWebView;
            loadContents(address, WebViewTools.getHtml(sourceWebView));

            setControls();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void openHtml(String baseName, String html, String address) {
        try {
            this.baseName = baseName;

            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);

            loadContents(address, html);

            setControls();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void openAddress(String baseName, String address) {
        try {
            this.baseName = baseName;

            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);

            loadAddress(address);

            setControls();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (sourceWebView == null) {
            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);
            return;
        }
        loadContents(webViewController.address, WebViewTools.getHtml(sourceWebView));
    }

    @Override
    public void cleanPane() {
        try {
            if (sourceWebView != null) {
                sourceWebView.getEngine().getLoadWorker().stateProperty().removeListener(listener);
            }
            listener = null;
            sourceWebView = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static HtmlPopController openWebView(BaseController parent, WebView srcWebView) {
        try {
            if (srcWebView == null) {
                return null;
            }
            HtmlPopController controller = (HtmlPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.HtmlPopFxml, false);
            if (parent instanceof BaseWebViewController) {
                BaseWebViewController c = (BaseWebViewController) parent;
                controller.openWebView(parent.baseName, srcWebView, c.webViewController == null ? null : c.webViewController.address);
            } else {
                controller.openWebView(parent.baseName, srcWebView, null);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlPopController openHtml(BaseController parent, String html) {
        try {
            if (parent == null || html == null) {
                return null;
            }
            HtmlPopController controller = (HtmlPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.HtmlPopFxml, false);
            if (parent instanceof BaseWebViewController) {
                BaseWebViewController c = (BaseWebViewController) parent;
                controller.openHtml(parent.baseName, html, c.webViewController == null ? null : c.webViewController.address);
            } else {
                controller.openHtml(parent.baseName, html, null);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlPopController openAddress(BaseController parent, String address) {
        try {
            if (parent == null || address == null) {
                return null;
            }
            HtmlPopController controller = (HtmlPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.HtmlPopFxml, false);
            controller.openAddress(parent.baseName, address);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlPopController openHtml(String html) {
        try {
            if (html == null) {
                return null;
            }
            HtmlPopController controller = (HtmlPopController) WindowTools.openStage(Fxmls.HtmlPopFxml);
            controller.openHtml("HtmlPop", html, null);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
