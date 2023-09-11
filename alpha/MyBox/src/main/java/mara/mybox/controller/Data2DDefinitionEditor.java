package mara.mybox.controller;

import javafx.fxml.FXML;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class Data2DDefinitionEditor extends BaseInfoTreeNodeEditor {

    protected Data2DDefinitionController manageController;

    @FXML
    protected ControlData2DDefColumns columnsController;

    public Data2DDefinitionEditor() {
        defaultExt = "csv";
    }

    protected void setParameters(Data2DDefinitionController manageController) {
        this.manageController = manageController;
    }

    @FXML
    @Override
    public void clearValue() {
        columnsController.clearAction();
    }

}
