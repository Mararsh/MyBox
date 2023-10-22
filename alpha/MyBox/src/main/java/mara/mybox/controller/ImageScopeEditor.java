package mara.mybox.controller;

import javafx.fxml.FXML;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ImageScopeEditor extends InfoTreeNodeEditor {

    protected ImageScopeController scopeController;

    @FXML
    protected ControlImageScopeInput valuesController;

    public ImageScopeEditor() {
        defaultExt = "png";
    }

    protected void setParameters(ImageScopeController scopeController) {
        this.scopeController = scopeController;

    }

}
