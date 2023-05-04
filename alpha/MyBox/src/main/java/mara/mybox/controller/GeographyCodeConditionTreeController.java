package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-03-27
 * @License Apache License Version 2.0
 */
public class GeographyCodeConditionTreeController extends ControlConditionTree {

    protected LoadingController loading;

    public GeographyCodeConditionTreeController() {
        baseTitle = Languages.message("GeographyCode");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            List<String> s = new ArrayList();
            s.add(Languages.message("AllLocations"));
            treeView.setSelectedTitles(s);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadTree() {
        if (task != null) {
            task.cancel();
        }
        treeView.setRoot(null);
        task = new SingletonTask<Void>(this) {
            private GeographyCode earch;
            private List<GeographyCode> continents, others;
            private List<Long> haveChildren;
            private List<Short> haveLevels;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
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
                        loading.setInfo(Languages.message("LoadingContinents"));
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
                        loading.setInfo(Languages.message("CheckingLeafNodes"));
                    }
                    haveChildren = TableGeographyCode.haveChildren(conn, nodes);

                    if (loading != null) {
                        loading.setInfo(Languages.message("LoadingLevels"));
                    }
                    haveLevels = new ArrayList();
                    try (PreparedStatement query = conn.prepareStatement(TableGeographyCode.LevelSizeQuery)) {
                        query.setMaxRows(1);
                        for (short i = 2; i <= 9; i++) {
                            query.setLong(1, i);
                            try (ResultSet results = query.executeQuery()) {
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
                    MyBoxLog.debug(error);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                CheckBoxTreeItem<ConditionNode> allItem = new CheckBoxTreeItem(
                        ConditionNode.create(Languages.message("Earth"))
                                .setCode(earch)
                                .setTitle(Languages.message("AllLocations"))
                                .setCondition(""));
                allItem.setExpanded(true);
                treeView.setRoot(allItem);
                addNodes(allItem, continents, haveChildren, haveLevels);

                if (!others.isEmpty()) {
                    CheckBoxTreeItem<ConditionNode> othersItem = new CheckBoxTreeItem(
                            ConditionNode.create(Languages.message("Others"))
                                    .setTitle(Languages.message("Others"))
                                    .setCondition(""));
                    allItem.getChildren().add(othersItem);
                    addNodes(othersItem, others, haveChildren, null);
                }
                treeView.setSelection();
            }
        };
        loading = start(task);
    }

    protected void addNodes(CheckBoxTreeItem<ConditionNode> parent, List<GeographyCode> codes,
            List<Long> haveChildren, List<Short> haveLevels) {
        if (parent == null || parent.getValue() == null
                || codes == null || codes.isEmpty()) {
            return;
        }
        ConditionNode parantNode = parent.getValue();
        GeographyCode parantCode = parantNode.getCode();
        GeographyCodeLevel parentLevel = parantCode == null ? null : parantCode.getLevelCode();
        String prefix = parantNode.getTitle() != null
                ? (Languages.message("AllLocations").equals(parantNode.getTitle()) ? "" : parantNode.getTitle() + " - ")
                : (parantCode == null ? "" : parantCode.getFullName() + " - ");
        if (parantCode != null && parentLevel != null) {
            CheckBoxTreeItem<ConditionNode> valueItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("Self"))
                            .setTitle((prefix.isBlank() ? Languages.message("Earth") + " - " : prefix) + Languages.message("Self"))
                            .setCondition("Geography_Code.gcid=" + parantCode.getGcid())
            );
            parent.getChildren().add(valueItem);

            CheckBoxTreeItem<ConditionNode> predefinedItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("PredefinedData"))
                            .setTitle(prefix + Languages.message("PredefinedData"))
                            .setCondition((parantCode.getGcid() == 1 ? ""
                                    : parentLevel.getKey() + "=" + parantCode.getGcid() + " AND ")
                                    + " gcsource=2")
            );
            parent.getChildren().add(predefinedItem);

            CheckBoxTreeItem<ConditionNode> inputtedItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("InputtedData"))
                            .setTitle(prefix + Languages.message("InputtedData"))
                            .setCondition((parantCode.getGcid() == 1 ? ""
                                    : parentLevel.getKey() + "=" + parantCode.getGcid() + " AND ")
                                    + " gcsource<>2")
            );
            parent.getChildren().add(inputtedItem);

            if (haveLevels != null) {
                for (short levelValue : haveLevels) {
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
        if (task != null) {
            task.cancel();
        }
        parent.getChildren().clear();
        task = new SingletonTask<Void>(this) {
            private List<GeographyCode> nodes;
            private List<Long> haveChildren;
            private List<Short> haveLevels;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    conn.setReadOnly(true);
                    GeographyCodeLevel level = code.getLevelCode();
                    int codeLevel = level.getLevel();
                    if (level.getKey() == null || codeLevel < 2 || codeLevel > 8) {
                        return false;
                    }
                    if (loading != null) {
                        loading.setInfo(Languages.message("Loading") + " " + level.getName()
                                + " " + code.getName());
                    }
                    nodes = TableGeographyCode.queryChildren(conn, code.getGcid());
                    if (nodes == null || nodes.isEmpty()) {
                        return false;
                    }

                    if (loading != null) {
                        loading.setInfo(Languages.message("CheckingLeafNodes"));
                    }
                    haveChildren = TableGeographyCode.haveChildren(conn, nodes);

                    if (loading != null) {
                        loading.setInfo(Languages.message("LoadingLevels"));
                    }
                    haveLevels = new ArrayList();
                    for (int i = codeLevel + 1; i <= 9; i++) {
                        String sql = "SELECT gcid FROM Geography_Code WHERE "
                                + " level=" + i + " AND "
                                + level.getKey() + "=" + code.getGcid()
                                + " OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY";
                        try (ResultSet results = conn.createStatement().executeQuery(sql)) {
                            if (results.next()) {
                                haveLevels.add(Short.parseShort(i + ""));
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
        loading = start(task);
    }

    protected boolean loaded(TreeItem item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        Object child = item.getChildren().get(0);
        return child instanceof CheckBoxTreeItem;
    }

}
