package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

/**
 * @Author Mara
 * @CreateDate 2020-9-15
 * @License Apache License Version 2.0
 */
public class ControlSelectPixels extends BaseController {

    protected BaseImageController imageController;

    @FXML
    protected ControlImageScope handleController;

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (super.handleKeyEvent(event)) {
            return true;
        }
        return handleController.handleKeyEvent(event);
    }

    @FXML
    public void saveScope() {
        ControlDataImageScope.open(this, handleController.scope);
    }

    @FXML
    @Override
    public void selectAction() {
        DataSelectImageScopeController.open(this);
    }

    @Override
    public boolean controlAltT() {
        selectAction();
        return true;
    }

}
