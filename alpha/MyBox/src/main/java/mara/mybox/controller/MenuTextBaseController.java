package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-6-27
 * @License Apache License Version 2.0
 */
public class MenuTextBaseController extends MenuController {

    protected TextInputControl textInput;

    public MenuTextBaseController() {
        baseTitle = Languages.message("Value");
    }

    @Override
    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            this.node = node;
            if (node != null) {
                if (node instanceof TextInputControl) {
                    textInput = (TextInputControl) node;
                } else if (node instanceof ComboBox) {
                    ComboBox cb = (ComboBox) node;
                    if (cb.isEditable()) {
                        textInput = cb.getEditor();
                    }
                }
            }
            super.setParameters(parent, node, x, y);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        if (textInput == null) {
            super.myBoxClipBoard();
        } else {
            TextClipboardPopController.open(parentController, node);
        }
    }

    @FXML
    public void popButtons() {
        if (parentController == null || node == null) {
            return;
        }
        MenuTextEditController.open(parentController, node, initX, initY);
    }

    @FXML
    @Override
    public void findAction() {
        if (textInput == null) {
            return;
        }
        Window window = thisPane.getScene().getWindow();
        FindPopController.open(parentController, node, window.getX(), window.getY());
        window.hide();
    }

    @FXML
    @Override
    public void replaceAction() {
        if (textInput == null) {
            return;
        }
        Window window = thisPane.getScene().getWindow();
        FindReplacePopController.open(parentController, node, window.getX(), window.getY());
        window.hide();
    }

}
