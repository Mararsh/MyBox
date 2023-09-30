package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.InfoNode;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class Data2DDefinitionEditor extends InfoTreeNodeEditor {

    protected Data2DDefinitionController manageController;
    protected Data2D data2D;

    @FXML
    protected ControlData2DDefAttributes defAttributesController;
    @FXML
    protected ControlData2DDefColumns columnsController;

    public Data2DDefinitionEditor() {
        defaultExt = "csv";
    }

    protected void setParameters(Data2DDefinitionController manager) {
        manageController = manager;
        attributesController = defAttributesController;
        data2D = null;
        columnsController.setParameters(this);
    }

    protected void load(Data2D data) {
        data2D = data;
        columnsController.load(data2D);
        defAttributesController.editNode(null, data2D);
        nodeChanged(false);
    }

    @Override
    protected void editNode(InfoNode node) {
        if (node != null) {
            data2D = Data2DTools.definitionFromXML(node.getInfo());
        } else {
            data2D = null;
        }
        if (data2D == null) {
            data2D = new DataFileCSV();
        }
        columnsController.load(data2D);
        defAttributesController.editNode(node, data2D);
        nodeChanged(false);
        updateTitle(node);
    }

    @Override
    protected String nodeInfo() {
        if (data2D == null) {
            data2D = new DataFileCSV();
        }
        data2D.setColumns(columnsController.tableData)
                .setColsNumber(columnsController.tableData.size())
                .setScale(defAttributesController.scale)
                .setMaxRandom(defAttributesController.maxRandom)
                .setComments(defAttributesController.descInput.getText())
                .setDataName(nodeTitle());
        String info = Data2DTools.definitionToXML(data2D, true, "");
        return info;
    }

    @FXML
    @Override
    public void clearValue() {
        columnsController.clearAction();
    }

    @Override
    public void pasteNode(InfoNode node) {
        if (node == null) {
            return;
        }
        Data2D data = Data2DTools.definitionFromXML(node.getInfo());
        if (data != null && data.getColumns() != null) {
            columnsController.tableData.addAll(data.getColumns());
        }
        tabPane.getSelectionModel().select(valueTab);
    }

}
