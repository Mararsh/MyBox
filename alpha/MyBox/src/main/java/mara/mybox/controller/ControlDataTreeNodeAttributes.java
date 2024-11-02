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
    protected DataNode parentNode, currentNode;
    protected boolean changed;

    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Label chainLabel;

    public ControlDataTreeNodeAttributes() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

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

    public void setParameters(BaseDataTreeController controller) {
        try {
            dataController = controller;
            nodeController = dataController.nodeController;
            dataTable = dataController.dataTable;
            dataNodeTable = dataController.dataNodeTable;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        attributes
     */
    protected void loadAttributes(DataNode node) {
        currentNode = node;
        isSettingValues = true;
        if (node != null) {
            idInput.setText(node.getNodeid() + "");
            titleInput.setText(node.getTitle());
            timeInput.setText(DateTools.datetimeToString(node.getUpdateTime()));
            selectButton.setVisible(node.getNodeid() < 0 || node.getParentid() < 0);
        } else {
            idInput.setText(message("NewData"));
            titleInput.setText("");
            timeInput.setText("");
            selectButton.setVisible(true);
        }
        isSettingValues = false;
        refreshParentNode();
        attributesChanged(false);
    }

    protected void copyNode() {
        isSettingValues = true;
        parentController.setTitle(parentController.baseTitle + ": " + message("NewData"));
        idInput.setText(message("NewData"));
        titleInput.appendText(" " + message("Copy"));
        currentNode = null;
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
        if (currentNode != null) {
            node.setNodeid(currentNode.getNodeid());
            node.setDataTable(dataTable);
        }
        if (parentNode != null) {
            node.setParentid(parentNode.getNodeid());
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

    protected void checkParentNode(DataNode node) {
        if (parentNode == null || node.getNodeid() != parentNode.getNodeid()) {
            return;
        }
        refreshParentNode();
    }

    protected void setParentNode(DataNode node) {
        parentNode = node;
        refreshParentNode();
    }

    protected void refreshParentNode() {
        FxTask updateTask = new FxTask<Void>(this) {
            private String chainName;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (currentNode != null) {
                        if (currentNode.getParentid() >= 0) {
                            parentNode = dataNodeTable.query(conn, currentNode.getParentid());
                        } else {
                            parentNode = dataNodeTable.readData(conn, parentNode);
                        }
                    }
                    if (parentNode == null) {
                        chainName = "";
                    } else {
                        chainName = dataController.treeController.chainName(conn, parentNode);
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
        start(updateTask, false);
    }

}
