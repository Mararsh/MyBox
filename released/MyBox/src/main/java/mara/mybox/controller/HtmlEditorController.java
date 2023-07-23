package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends WebAddressController {

    @FXML
    protected ControlHtmlEditor editController;

    public HtmlEditorController() {
        baseTitle = message("HtmlEditor");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            webViewController = editController.webViewController;
            webView = webViewController.webView;
            webEngine = webViewController.webEngine;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            editController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    panesLoad();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void goAction() {
        editController.loadAddress(addressInput.getText());
    }

    @Override
    public boolean loadFile(File file) {
        return editController.loadFile(file);
    }

    @Override
    public boolean loadAddress(String address) {
        return editController.loadAddress(address);
    }

    @Override
    public boolean loadContents(String contents) {
        return editController.loadContents(contents);
    }

    @Override
    public boolean loadContents(String address, String contents) {
        return editController.loadContents(address, contents);
    }

    public void panesLoad() {
        sourceFile = editController.sourceFile;
    }

    @FXML
    @Override
    public void createAction() {
        if (editController.create()) {
            addressInput.setText("");
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        editController.refreshAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (editController.keyEventsFilter(event)) {
            return true;
        }
        return super.keyEventsFilter(event);
    }


    /*
        static
     */
    public static HtmlEditorController open() {
        try {
            HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HtmlEditorController openFile(File file) {
        try {
            HtmlEditorController controller = open();
            if (controller != null && file != null) {
                controller.sourceFileChanged(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HtmlEditorController openAddress(String address) {
        try {
            HtmlEditorController controller = open();
            if (controller != null && address != null) {
                controller.loadAddress(address);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HtmlEditorController openHtml(String html) {
        try {
            HtmlEditorController controller = open();
            if (controller != null && html != null) {
                controller.loadContents(html);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
