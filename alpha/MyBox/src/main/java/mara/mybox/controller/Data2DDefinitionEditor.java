package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class Data2DDefinitionEditor extends BaseInfoTreeNodeController {

    protected Data2DDefinitionController manageController;

    @FXML
    protected ControlData2DDefColumns columnsController;

    public Data2DDefinitionEditor() {
        defaultExt = "csv";
    }

    protected void setParameters(Data2DDefinitionController manageController) {
        this.manageController = manageController;
        columnsController.editor = this;
        super.setParameters(manageController);
    }

    @Override
    protected void editNode(InfoNode node) {
        if (node != null) {
            columnsController.load(node.getValue());
        } else {
            columnsController.load(null);
        }
        attributesController.editNode(node);
        nodeChanged(false);
    }

    @Override
    public InfoNode pickNodeData() {
        InfoNode node = super.pickNodeData();
        if (node == null) {
            return null;
        }
        node.setValue(columnsController.toXML());
        return node;
    }

    @FXML
    @Override
    public void clearValue() {
        columnsController.clearAction();
    }

}
