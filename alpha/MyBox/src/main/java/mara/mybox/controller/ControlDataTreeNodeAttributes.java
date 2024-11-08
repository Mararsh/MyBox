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
public class ControlDataTreeNodeAttributes extends BaseController {

    protected BaseDataTreeController dataController;
    protected BaseDataTreeNodeController nodeController;
    protected BaseDataTable dataTable;
    protected TableDataNode dataNodeTable;
    protected boolean changed;

    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Label chainLabel;

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;
            nodeController = dataController.nodeController;
            dataTable = dataController.dataTable;
            dataNodeTable = dataController.dataNodeTable;

            titleInput = nodeController.titleInput;
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
        if (nodeController.currentNode == null) {
            return;
        }
        isSettingValues = true;
        titleInput.setText(nodeController.currentNode.getTitle());
        isSettingValues = false;

        if (nodeController.currentNode.getNodeid() < 0) {
            idInput.setText(message("NewData"));
        } else {
            idInput.setText(nodeController.currentNode.getNodeid() + "");
        }
        timeInput.setText(DateTools.datetimeToString(nodeController.currentNode.getUpdateTime()));
        selectButton.setVisible(nodeController.currentNode.getNodeid() < 0 || nodeController.parentNode == null);
        refreshParentNode();
        attributesChanged(false);
    }

    protected void copyNode() {
        isSettingValues = true;
        parentController.setTitle(parentController.baseTitle + ": " + message("NewData"));
        idInput.setText(message("NewData"));
        titleInput.appendText(" " + message("Copy"));
        nodeController.currentNode = null;
        selectButton.setVisible(true);
        isSettingValues = false;
        attributesChanged(true);
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
        if (isSettingValues || nodeController == null) {
            return;
        }
        this.changed = changed;
        nodeController.attributesChanged();
    }

    protected DataNode pickAttributes() {
        String title = titleInput.getText();
        if (title == null || title.isBlank()) {
            return null;
        }
        DataNode node = DataNode.create();
        if (nodeController.currentNode != null) {
            node.setNodeid(nodeController.currentNode.getNodeid());
            node.setDataTable(dataTable);
        }
        if (nodeController.parentNode != null) {
            node.setParentid(nodeController.parentNode.getNodeid());
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
                    if (nodeController.parentNode == null) {
                        chainName = "";
                    } else {
                        chainName = dataController.treeController.chainName(conn, nodeController.parentNode);
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
