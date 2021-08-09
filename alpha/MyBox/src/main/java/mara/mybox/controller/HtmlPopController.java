package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
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

    @FXML
    protected CheckBox openCheck;

    public HtmlPopController() {
        baseTitle = Languages.message("Html");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            openCheck.setSelected(UserConfig.getBoolean(baseName + "Open", true));
            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Open", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setStageStatus(String prefix, int minSize) {
        setAsPopup(baseName);
    }

    @FXML
    public void popLinksStyle(MouseEvent mouseEvent) {
        popMenu = PopTools.popHtmlStyle(mouseEvent, this, popMenu, webView.getEngine());
    }

    @Override
    protected void afterSaveAs(File file) {
        if (openCheck.isSelected()) {
            ControllerTools.openHtmlEditor(null, file);
        }
    }


    /*
        static
     */
    public static HtmlPopController open(BaseController parent, WebView srcWebview) {
        try {
            if (srcWebview == null) {
                return null;
            }
            return open(parent, WebViewTools.getHtml(srcWebview));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static HtmlPopController open(BaseController parent, String html) {
        try {
            if (html == null) {
                return null;
            }
            HtmlPopController controller = (HtmlPopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.HtmlPopFxml, false);
            if (parent instanceof BaseWebViewController) {
                BaseWebViewController c = (BaseWebViewController) parent;
                controller.loadContents(c.address, html);
            } else {
                controller.loadContents(html);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
