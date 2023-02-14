package mara.mybox.controller;

import java.io.File;
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
            webViewController = editController.webViewController;
            super.initValues();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
    public static HtmlEditorController load(String html) {
        try {
            HtmlEditorController controller = (HtmlEditorController) WindowTools.openStage(Fxmls.HtmlEditorFxml);
            controller.loadContents(html);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
