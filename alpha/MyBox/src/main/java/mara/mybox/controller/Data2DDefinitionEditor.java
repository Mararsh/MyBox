package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import static mara.mybox.db.data.InfoNode.NodeSeparater;
import static mara.mybox.value.Languages.message;

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
        super.setParameters(manageController);
    }

    @Override
    protected void editNode(InfoNode node) {
        isSettingValues = true;
        if (node != null) {
            if (valueInput != null) {
                valueInput.setText(node.getValue());
            }
            if (moreInput != null) {
                moreInput.setText(node.getMore());
            }
        } else {
            columnsController.clearAction();
        }
        isSettingValues = false;

        attributesController.editNode(node);
        showEditorPane();
        if (valueTab != null) {
            valueTab.setText(treeController.valueMsg);
        }
    }

    @Override
    public InfoNode pickNodeData() {
        String name = attributesController.nameInput.getText();
        if (name == null || name.isBlank()) {
            popError(message("InvalidParameters") + ": " + treeController.nameMsg);
            if (tabPane != null && attributesTab != null) {
                tabPane.getSelectionModel().select(attributesTab);
            }
            return null;
        }
        if (name.contains(NodeSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + NodeSeparater + "\"");
            return null;
        }

        if (name.contains(NodeSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + NodeSeparater + "\"");
            return null;
        }
        InfoNode node = InfoNode.create()
                .setCategory(treeController.category).setTitle(name);

        if (valueInput != null) {
            node.setValue(valueInput.getText());
        }
        if (moreInput != null) {
            node.setMore(moreInput.getText());
        }
        return node;
    }

    @FXML
    @Override
    public void clearValue() {
        columnsController.clearAction();
    }

}
