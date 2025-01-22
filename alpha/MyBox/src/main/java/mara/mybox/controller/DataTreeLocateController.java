package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-1-21
 * @License Apache License Version 2.0
 */
public class DataTreeLocateController extends BaseChildController {

    protected BaseDataTreeViewController treeController;
    protected BaseNodeTable nodeTable;
    protected long longKey;
    protected String stringKey;

    @FXML
    protected ToggleGroup objectGroup;
    @FXML
    protected RadioButton idRadio, titleRadio, valueRadio, timeRadio;
    @FXML
    protected Label keyLabel;
    @FXML
    protected TextField keyInput;
    @FXML
    protected CheckBox expandedCheck;

    public void setParameters(BaseDataTreeViewController controller) {
        try {
            treeController = controller;
            nodeTable = treeController.nodeTable;
            baseName = treeController.baseName + "_Locate";
            baseTitle = treeController.baseTitle + message("Locate");
            setTitle(baseTitle);
            valueRadio.setText(nodeTable.label(nodeTable.majorColumnName));

            objectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    objectChanged();
                }
            });
            objectChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void objectChanged() {
        try {
            keyInput.clear();

            if (idRadio.isSelected() || timeRadio.isSelected()) {
                keyLabel.setText(message("Equals"));
            } else {
                keyLabel.setText(message("Contains"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            String input = keyInput.getText();
            if (input == null || input.isBlank()) {
                popError(message("InvalidParameter"));
                return;
            }
            stringKey = input;
            try {
                if (idRadio.isSelected()) {
                    longKey = Long.parseLong(stringKey);

                } else if (timeRadio.isSelected()) {
                    Date d = DateTools.encodeDate(stringKey);
                    longKey = d.getTime();
                    stringKey = DateTools.datetimeToString(d);

                }
            } catch (Exception e) {
                popError(message("InvalidParameter"));
                return;
            }

            TableStringValues.add(baseName + currentName() + "Histories", input);

            TreeItem<DataNode> item = matchFrom(treeController.getRootItem());
            if (item != null) {
                treeController.focusItem(item);

            } else {
                if (expandedCheck.isSelected()) {
                    popInformation(message("NotFound"));
                } else {
                    checkData();
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public TreeItem<DataNode> matchFrom(TreeItem<DataNode> fromItem) {
        if (fromItem == null) {
            return null;
        }
        if (matchNode(fromItem.getValue())) {
            return fromItem;
        }
        List<TreeItem<DataNode>> children = fromItem.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<DataNode> child : children) {
            TreeItem<DataNode> find = matchFrom(child);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

    public boolean matchNode(DataNode node) {
        try {
            if (node == null) {
                return false;
            }
            if (idRadio.isSelected()) {
                return node.getNodeid() == longKey;

            } else if (titleRadio.isSelected()) {
                String title = node.getTitle();
                return title != null && title.contains(stringKey);

            } else if (valueRadio.isSelected()) {
                String value = (String) nodeTable.getValue(node, nodeTable.majorColumnName);
                return value.contains(stringKey);

            } else if (timeRadio.isSelected()) {
                return DateTools.datetimeToString(node.getUpdateTime()).equals(stringKey);

            }
        } catch (Exception e) {
        }
        return false;
    }

    public void checkData() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataNode node;

            @Override
            protected boolean handle() {
                String sql = "SELECT * FROM " + nodeTable.tableName
                        + " WHERE parentid<>nodeid AND ";
                if (idRadio.isSelected()) {
                    sql += "nodeid=" + longKey;

                } else if (titleRadio.isSelected()) {
                    sql += "title like '%" + DerbyBase.stringValue(stringKey) + "%' ";

                } else if (valueRadio.isSelected()) {
                    sql += nodeTable.majorColumnName + " like '%" + DerbyBase.stringValue(stringKey) + "%' ";

                } else if (timeRadio.isSelected()) {
                    sql += "update_time>='" + stringKey + ".000' AND update_time<='" + stringKey + ".999'";
                }
                sql += " ORDER BY nodeid ASC FETCH FIRST ROW ONLY";
//                MyBoxLog.console(sql);
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(sql);
                        ResultSet results = query.executeQuery()) {
                    if (results != null && results.next()) {
                        node = nodeTable.readData(results);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (node != null) {
                    treeController.focusNode(node);
                } else {
                    popInformation(message("NotFound"));
                }
            }

        };
        start(task);
    }

    protected String currentName() {
        if (idRadio.isSelected()) {
            return "ID";
        } else if (titleRadio.isSelected()) {
            return "Title";
        } else if (valueRadio.isSelected()) {
            return "Value";
        } else if (timeRadio.isSelected()) {
            return "Time";
        } else {
            return "";
        }
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean(baseName + currentName() + "HistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popSavedValues(this, keyInput, event, baseName + currentName() + "Histories");
    }

    /*
        static
     */
    public static DataTreeLocateController open(BaseDataTreeViewController parent) {
        try {
            DataTreeLocateController controller = (DataTreeLocateController) WindowTools.branchStage(
                    parent, Fxmls.DataTreeLocateFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
