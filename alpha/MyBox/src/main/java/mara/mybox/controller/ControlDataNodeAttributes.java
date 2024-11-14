package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseDataTable;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-11
 * @License Apache License Version 2.0
 */
public class ControlDataNodeAttributes extends BaseController {

    protected DataTreeController treeController;
    protected ControlDataNodeEditor nodeEditor;
    protected BaseDataTable dataTable;
    protected TableDataNode nodeTable;
    protected boolean changed;

    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Label chainLabel;

    public void setParameters(DataTreeController controller) {
        try {
            treeController = controller;
            nodeEditor = treeController.nodeController;
            dataTable = treeController.dataTable;
            nodeTable = treeController.nodeTable;

            titleInput = nodeEditor.titleInput;
            titleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    attributesChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        attributes
     */
    protected void loadAttributes() {
        if (nodeEditor.currentNode == null) {
            return;
        }
        isSettingValues = true;
        titleInput.setText(nodeEditor.currentNode.getTitle());
        isSettingValues = false;

        if (nodeEditor.currentNode.getNodeid() < 0) {
            idInput.setText(message("NewData"));
        } else {
            idInput.setText(nodeEditor.currentNode.getNodeid() + "");
        }
        timeInput.setText(DateTools.datetimeToString(nodeEditor.currentNode.getUpdateTime()));
        selectButton.setVisible(nodeEditor.currentNode.getNodeid() < 0 || nodeEditor.parentNode == null);
        refreshParentNode();
        attributesChanged(false);
    }

    public void renamed(String newName) {
        if (titleInput == null) {
            return;
        }
        isSettingValues = true;
        titleInput.setText(newName);
        isSettingValues = false;
    }

    public void attributesChanged(boolean changed) {
        if (isSettingValues || nodeEditor == null) {
            return;
        }
        this.changed = changed;
        nodeEditor.updateStatus();
    }

    protected DataNode pickAttributes() {
        String title = titleInput.getText();
        if (title == null || title.isBlank()) {
            return null;
        }
        DataNode node = DataNode.create();
        if (nodeEditor.currentNode != null) {
            node.setNodeid(nodeEditor.currentNode.getNodeid());
        }
        if (nodeEditor.parentNode != null) {
            node.setParentid(nodeEditor.parentNode.getNodeid());
        }
        node.setTitle(title);
        node.setUpdateTime(new Date());
        return node;
    }

    /*
        parent
     */
    @FXML
    public void selectParent() {
//        InfoTreeNodeParentController.open(this);
    }
//
//    protected void checkParentNode(DataNode node) {
//        if (parentNode == null || node.getNodeid() != parentNode.getNodeid()) {
//            return;
//        }
//        refreshParentNode();
//    }
//
//    protected void setParentNode(DataNode node) {
//        parentNode = node;
//        refreshParentNode();
//    }

    protected void refreshParentNode() {
        task = new FxTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (nodeEditor.parentNode == null) {
                        chainName = "";
                    } else {
                        chainName = treeController.chainName(conn, nodeEditor.parentNode);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                chainLabel.setText(chainName);
            }
        };
        start(task, thisPane);
    }

}
