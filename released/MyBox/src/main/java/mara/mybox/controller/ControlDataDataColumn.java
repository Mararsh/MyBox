package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeDataColumn;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class ControlDataDataColumn extends BaseDataValuesController {

    @FXML
    protected ControlData2DColumnEdit columnController;

    @Override
    public void initEditor() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void editColumn(Data2DColumn column) {
        if (column != null) {
            String s = nodeEditor.titleInput.getText();
            if (s == null || s.isBlank()) {
                isSettingValues = true;
                nodeEditor.titleInput.setText(column.getColumnName());
                isSettingValues = false;
            }
        }
        columnController.loadColumn(column);
    }

    @Override
    protected void editValues() {
        try {
            Data2DColumn column = null;
            if (nodeEditor.currentNode != null) {
                column = TableNodeDataColumn.toColumn(nodeEditor.currentNode);
            }
            editColumn(column);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        if (node == null) {
            return null;
        }
        Data2DColumn column = columnController.pickValues(false);
        if (column == null) {
            return null;
        }
        return TableNodeDataColumn.fromColumn(node, column);
    }


    /*
        static
     */
    public static DataTreeNodeEditorController loadColumn(BaseController parent, Data2DColumn column) {
        try {
            DataTreeNodeEditorController controller
                    = DataTreeNodeEditorController.openTable(parent, new TableNodeDataColumn());
            ((ControlDataDataColumn) controller.valuesController).editColumn(column);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
}
