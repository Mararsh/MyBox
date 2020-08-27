package mara.mybox.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.tools.GeographyCodeTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-03-27
 * @License Apache License Version 2.0
 */
public class GeographyCodeConditionTreeController extends ConditionTreeController {

    protected LoadingController loading;

    public GeographyCodeConditionTreeController() {
        baseTitle = message("GeographyCode");
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            List<String> s = new ArrayList();
            s.add(message("AllLocations"));
            treeView.setSelectedTitles(s);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void loadTree() {
        treeView.setRoot(null);
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private GeographyCode earch;
                private List<GeographyCode> continents, others;
                private List<Long> haveChildren;
                private List<Integer> haveLevels;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                        earch = TableGeographyCode.earth(conn);
                        if (earch == null) {
                            GeographyCodeTools.importPredefined(conn, loading);
                            earch = TableGeographyCode.earth(conn);
                            if (earch == null) {
                                return false;
                            }
                        }
                        conn.setReadOnly(true);
                        conn.setAutoCommit(true);

                        if (loading != null) {
                            loading.setInfo(message("LoadingContinents"));
                        }
                        continents = new ArrayList<>();
                        others = new ArrayList<>();
                        List<GeographyCode> nodes = TableGeographyCode.queryChildren(conn, earch.getGcid());
                        nodes.forEach((node) -> {
                            if (node.getLevel() == 2 && node.getGcid() >= 2 && node.getGcid() <= 8) {
                                continents.add(node);
                            } else if (node.getLevel() > 1) {
                                others.add(node);
                            }
                        });

                        if (loading != null) {
                            loading.setInfo(message("CheckingLeafNodes"));
                        }
                        haveChildren = TableGeographyCode.haveChildren(conn, nodes);

                        if (loading != null) {
                            loading.setInfo(message("LoadingLevels"));
                        }
                        haveLevels = new ArrayList();
                        try ( PreparedStatement query = conn.prepareStatement(TableGeographyCode.LevelSizeQuery)) {
                            query.setMaxRows(1);
                            for (int i = 2; i <= 9; i++) {
                                query.setLong(1, i);
                                try ( ResultSet results = query.executeQuery()) {
                                    if (results.next()) {
                                        int size = results.getInt(1);
                                        if (size > 0) {
                                            haveLevels.add(i);
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        logger.debug(error);
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    CheckBoxTreeItem<ConditionNode> allItem = new CheckBoxTreeItem(
                            ConditionNode.create(message("Earth"))
                                    .setCode(earch)
                                    .setTitle(message("AllLocations"))
                                    .setCondition(""));
                    allItem.setExpanded(true);
                    treeView.setRoot(allItem);
                    addNodes(allItem, continents, haveChildren, haveLevels);

                    if (!others.isEmpty()) {
                        CheckBoxTreeItem<ConditionNode> othersItem = new CheckBoxTreeItem(
                                ConditionNode.create(message("Others"))
                                        .setTitle(message("Others"))
                                        .setCondition(""));
                        allItem.getChildren().add(othersItem);
                        addNodes(othersItem, others, haveChildren, null);
                    }
                    treeView.setSelection();
                }
            };
            if (getUserController() != null) {
                loading = getUserController().openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                loading = openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void addNodes(CheckBoxTreeItem<ConditionNode> parent, List<GeographyCode> codes,
            List<Long> haveChildren, List<Integer> haveLevels) {
        if (parent == null || parent.getValue() == null
                || codes == null || codes.isEmpty()) {
            return;
        }
        ConditionNode parantNode = parent.getValue();
        GeographyCode parantCode = parantNode.getCode();
        GeographyCodeLevel parentLevel = parantCode == null ? null : parantCode.getLevelCode();
        String prefix = parantNode.getTitle() != null
                ? (message("AllLocations").equals(parantNode.getTitle()) ? "" : parantNode.getTitle() + " - ")
                : (parantCode == null ? "" : parantCode.getFullName() + " - ");
        if (parantCode != null && parentLevel != null) {
            CheckBoxTreeItem<ConditionNode> valueItem = new CheckBoxTreeItem(
                    ConditionNode.create(message("Self"))
                            .setTitle((prefix.isBlank() ? message("Earth") + " - " : prefix) + message("Self"))
                            .setCondition("Geography_Code.gcid=" + parantCode.getGcid())
            );
            parent.getChildren().add(valueItem);

            CheckBoxTreeItem<ConditionNode> predefinedItem = new CheckBoxTreeItem(
                    ConditionNode.create(message("PredefinedData"))
                            .setTitle(prefix + message("PredefinedData"))
                            .setCondition((parantCode.getGcid() == 1 ? ""
                                    : parentLevel.getKey() + "=" + parantCode.getGcid() + " AND ")
                                    + " predefined=1")
            );
            parent.getChildren().add(predefinedItem);

            CheckBoxTreeItem<ConditionNode> inputtedItem = new CheckBoxTreeItem(
                    ConditionNode.create(message("InputtedData"))
                            .setTitle(prefix + message("InputtedData"))
                            .setCondition((parantCode.getGcid() == 1 ? ""
                                    : parentLevel.getKey() + "=" + parantCode.getGcid() + " AND ")
                                    + " predefined<>1")
            );
            parent.getChildren().add(inputtedItem);

            if (haveLevels != null) {
                for (int levelValue : haveLevels) {
                    GeographyCodeLevel level = new GeographyCodeLevel(levelValue);
                    CheckBoxTreeItem<ConditionNode> levelItem = new CheckBoxTreeItem(
                            ConditionNode.create(level.getName())
                                    .setTitle(prefix + level.getName())
                                    .setCondition("level=" + levelValue
                                            + (parantCode.getGcid() == 1 ? ""
                                            : " AND " + parentLevel.getKey() + "=" + parantCode.getGcid()))
                    );
                    parent.getChildren().add(levelItem);
                }
            }
        }

        for (GeographyCode code : codes) {
            long codeid = code.getGcid();
            GeographyCodeLevel codeLevel = code.getLevelCode();

            CheckBoxTreeItem<ConditionNode> codeItem = new CheckBoxTreeItem(
                    ConditionNode.create(code.getName())
                            .setCode(code)
                            .setTitle(prefix + code.getName())
                            .setCondition(codeLevel.getKey() + "=" + codeid)
            );
            parent.getChildren().add(codeItem);

            if (haveChildren != null && haveChildren.contains(codeid)) {
                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                codeItem.getChildren().add(dummyItem);

                codeItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !codeItem.isLeaf() && !loaded(codeItem)) {
                                nodeTree(codeItem, code);
                            }
                        });
                codeItem.setExpanded(treeView.getExpandedNodes().contains(prefix + code.getName()));
            }

        }
        if (parent.isSelected()) {
            for (TreeItem<ConditionNode> child : parent.getChildren()) {
                ((CheckBoxTreeItem<ConditionNode>) child).setSelected(true);
            }
        }
    }

    protected void nodeTree(CheckBoxTreeItem<ConditionNode> parent, GeographyCode code) {
        if (parent == null || code == null) {
            return;
        }
        parent.getChildren().clear();
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<GeographyCode> nodes;
                private List<Long> haveChildren;
                private List<Integer> haveLevels;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                        conn.setReadOnly(true);
                        GeographyCodeLevel level = code.getLevelCode();
                        int codeLevel = level.getLevel();
                        if (level.getKey() == null || codeLevel < 2 || codeLevel > 8) {
                            return false;
                        }
                        if (loading != null) {
                            loading.setInfo(message("Loading") + " " + level.getName()
                                    + " " + code.getName());
                        }
                        nodes = TableGeographyCode.queryChildren(conn, code.getGcid());
                        if (nodes == null || nodes.isEmpty()) {
                            return false;
                        }

                        if (loading != null) {
                            loading.setInfo(message("CheckingLeafNodes"));
                        }
                        haveChildren = TableGeographyCode.haveChildren(conn, nodes);

                        if (loading != null) {
                            loading.setInfo(message("LoadingLevels"));
                        }
                        haveLevels = new ArrayList();
                        for (int i = codeLevel + 1; i <= 9; i++) {
                            String sql = "SELECT gcid FROM Geography_Code WHERE "
                                    + " level=" + i + " AND "
                                    + level.getKey() + "=" + code.getGcid()
                                    + " OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY";
                            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                                if (results.next()) {
                                    haveLevels.add(i);
                                }
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    addNodes(parent, nodes, haveChildren, haveLevels);
                }
            };
            if (getUserController() != null) {
                loading = getUserController().openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                loading = openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected boolean loaded(TreeItem item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        Object child = item.getChildren().get(0);
        return child instanceof CheckBoxTreeItem;
    }

}
