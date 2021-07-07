package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-7-4
 * @License Apache License Version 2.0
 */
public class PopTextsClipboardController extends PopNodesController {

    @FXML
    protected ControlTextsClipboard clipboardController;
    @FXML
    protected Label commentsLabel;

    public void setParameters(BaseController parent, TextInputControl textInput) {
        try {
            clipboardController.setParameters(textInput, true);
            commentsLabel.setVisible(textInput.isEditable() && !textInput.isDisable());
            setParameters(parent);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setStyle() {
        setStyle(thisPane);
        setStyle(clipboardController.getThisPane());
    }

}
