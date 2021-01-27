package mara.mybox.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.data.GeographyCodeTools;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.table.TableGeographyCode;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-04-23
 * @License Apache License Version 2.0
 */
public class GeographyCodeSelectorController extends BaseController {

    protected GeographyCodeUserController userController;
    protected LoadingController loading;

    @FXML
    protected TreeView<Text> treeView;

    public GeographyCodeSelectorController() {
        baseTitle = message("GeographyCode");
    }

    public void loadTree(GeographyCodeUserController userController) {
        this.userController = userController;
        loadTree();
    }

    protected void loadTree() {
        treeView.setRoot(null);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private GeographyCode earch;
                private List<GeographyCode> continents, others;
                private List<Long> haveChildren;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                        String sql = "SELECT * FROM Geography_Code WHERE gcid=1 ";
                        if (loading != null) {
                            loading.setInfo(sql);
                        }
                        earch = TableGeographyCode.earth(conn);
                        if (earch == null) {
                            GeographyCodeTools.importPredefined(conn);
                            earch = TableGeographyCode.earth(conn);
                            if (earch == null) {
                                return false;
                            }
                        }
                        conn.setReadOnly(true);
                        if (loading != null) {
                            loading.setInfo(message("LoadingContinents"));
                        }
                        sql = "SELECT * FROM Geography_Code WHERE level=2 AND continent>=2 AND continent<=8 ORDER BY gcid ";
                        if (loading != null) {
                            loading.setInfo(sql);
                        }
                        continents = TableGeographyCode.queryCodes(conn, sql, true);

                        String condition = "";
                        for (int i = 3; i <= 9; i++) {
                            String ic = "level=" + i;
                            for (short j = 3; j < i; j++) {
                                GeographyCodeLevel jLevel = new GeographyCodeLevel(j);
                                ic += " AND " + jLevel.getKey() + "<=0 ";
                            }
                            if (condition.isBlank()) {
                                condition += "( " + ic + ")";
                            } else {
                                condition += " OR ( " + ic + ")";
                            }
                        }
                        sql = "SELECT * FROM Geography_Code WHERE "
                                + " ( " + condition + " ) AND "
                                + " (continent<2 OR continent>8) "
                                + " ORDER BY gcid ";
                        others = TableGeographyCode.queryCodes(conn, sql, true);

                        if (loading != null) {
                            loading.setInfo(message("CheckingLeafNodes"));
                        }
                        haveChildren = new ArrayList();
                        List<GeographyCode> checkEmpty = new ArrayList<>();
                        checkEmpty.addAll(continents);
                        if (others != null && !others.isEmpty()) {
                            checkEmpty.addAll(others);
                        }
                        haveChildren = TableGeographyCode.haveChildren(conn, checkEmpty);

                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    Text earthNode = new Text(message("Earth"));
                    earthNode.setOnMouseClicked((MouseEvent event) -> {
                        userController.codeSelected(earch);
                    });
                    TreeItem<Text> earthItem = new TreeItem(earthNode);
                    earthItem.setExpanded(true);
                    treeView.setRoot(earthItem);
                    treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    addNodes(earthItem, continents, haveChildren);

                    if (!others.isEmpty()) {
                        Text othersNode = new Text(message("Others"));
                        TreeItem<Text> othersItem = new TreeItem(othersNode);
                        earthItem.getChildren().add(othersItem);
                        addNodes(othersItem, others, haveChildren);
                    }
                }
            };
            if (userController != null) {
                loading = userController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                loading = openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void addNodes(TreeItem<Text> parent, List<GeographyCode> codes,
            List<Long> haveChildren) {
        if (parent == null || codes == null || codes.isEmpty()) {
            return;
        }
        for (GeographyCode code : codes) {
            long codeid = code.getId();
            Text codeNode = new Text(code.getName());
            codeNode.setOnMouseClicked((MouseEvent event) -> {
                userController.codeSelected(code);
            });
            codeNode.setUserData(codeid);
            TreeItem<Text> codeItem = new TreeItem(codeNode);
            parent.getChildren().add(codeItem);

            if (haveChildren != null && haveChildren.contains(codeid)) {
                TreeItem<Text> dummyItem = new TreeItem(new Text("Loading"));
                codeItem.getChildren().add(dummyItem);
                codeItem.setExpanded(false);
                codeItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !codeItem.isLeaf() && !loaded(codeItem)) {
                                nodeTree(codeItem, code);
                            }
                        });
            }
        }
    }

    protected boolean loaded(TreeItem<Text> item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        try {
            TreeItem<Text> dummyItem = (TreeItem<Text>) (item.getChildren().get(0));
            return !"Loading".equals(dummyItem.getValue().getText());
        } catch (Exception e) {
            return true;
        }
    }

    protected void nodeTree(TreeItem<Text> parent, GeographyCode code) {
        if (parent == null || code == null) {
            return;
        }
        parent.getChildren().clear();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<GeographyCode> children;
                private List<Long> haveChildren;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                        conn.setReadOnly(true);
                        GeographyCodeLevel level = code.getLevelCode();
                        short codeLevel = level.getLevel();
                        if (level.getKey() == null || codeLevel < 2 || codeLevel > 8) {
                            return false;
                        }
                        if (loading != null) {
                            loading.setInfo(message("Loading") + " " + level.getName()
                                    + " " + code.getName());
                        }
                        String condition = "";
                        for (int i = codeLevel + 1; i <= 9; i++) {
                            String ic = "level=" + i;
                            for (int j = codeLevel + 1; j < i; j++) {
                                GeographyCodeLevel jLevel = new GeographyCodeLevel(Short.valueOf(j + ""));
                                ic += " AND " + jLevel.getKey() + "<=0 ";
                            }
                            if (condition.isBlank()) {
                                condition += "( " + ic + ")";
                            } else {
                                condition += " OR ( " + ic + ")";
                            }
                        }
                        String sql = "SELECT * FROM Geography_Code WHERE "
                                + " ( " + condition + " ) AND "
                                + level.getKey() + "=" + code.getId()
                                + " ORDER BY gcid ";
                        children = TableGeographyCode.queryCodes(conn, sql, true);
                        if (children == null || children.isEmpty()) {
                            return true;
                        }

                        if (loading != null) {
                            loading.setInfo(message("CheckingLeafNodes"));
                        }
                        haveChildren = TableGeographyCode.haveChildren(conn, children);

                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    addNodes(parent, children, haveChildren);
                }
            };
            if (userController != null) {
                loading = userController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                loading = openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void addNode(GeographyCode parent, GeographyCode child) {
        if (parent == null || child == null) {
            return;
        }
        addNode(treeView.getRoot(), parent, child);
    }

    protected void addNode(TreeItem<Text> node, GeographyCode parent, GeographyCode child) {
        if (node == null || parent == null || child == null) {
            return;
        }
        if (node.getValue().getUserData() != null) {
            long current = (long) (node.getValue().getUserData());
            if (current == parent.getId()) {
                Text childNode = new Text(child.getName());
                childNode.setOnMouseClicked((MouseEvent event) -> {
                    userController.codeSelected(child);
                });
                childNode.setUserData(child.getId());
                TreeItem<Text> codeItem = new TreeItem(childNode);
                node.getChildren().add(codeItem);
                node.setExpanded(true);
                return;
            }
        }
        if (node.isLeaf()) {
            return;
        }
        for (TreeItem<Text> subNode : node.getChildren()) {
            addNode(subNode, parent, child);
        }
    }

    protected void removeNode(GeographyCode code) {
        if (code == null) {
            return;
        }
        removeNode(treeView.getRoot(), code);
    }

    protected void removeNode(TreeItem<Text> node, GeographyCode code) {
        if (node == null || code == null || node.isLeaf()) {
            return;
        }
        for (TreeItem<Text> subNode : node.getChildren()) {
            if (subNode.getValue().getUserData() != null) {
                long subCode = (long) (subNode.getValue().getUserData());
                if (subCode == code.getId()) {
                    node.getChildren().remove(subNode);
                    return;
                }
            }
            removeNode(subNode, code);
        }
    }

}
