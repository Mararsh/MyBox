package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.TreeNodeTag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableTag;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.IconTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeNodeImportController extends BaseBatchFileController {

    protected TreeManageController treeController;
    protected TreeNodesController nodesController;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected TableTag tableTag;
    protected TreeNode rootNode;
    protected String category;
    protected Map<String, TreeNode> parents;
    protected boolean isWebFavorite, downIcon;

    @FXML
    protected CheckBox replaceCheck, iconCheck;
    @FXML
    protected Label formatLabel;

    public TreeNodeImportController() {
        baseTitle = message("Import");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setManage(TreeManageController treeController) {
        this.treeController = treeController;
        tableTreeNode = treeController.tableTreeNode;
        tableTreeNodeTag = treeController.tableTreeNodeTag;
        tableTag = treeController.tableTag;
        category = treeController.category;
        iconCheck.setVisible(treeController instanceof WebFavoritesController);
    }

    public void setManage(TreeNodesController nodeController) {
        this.nodesController = nodeController;
        tableTreeNode = nodeController.tableTreeNode;
        tableTreeNodeTag = nodeController.tableTreeNodeTag;
        tableTag = new TableTag();
        category = nodeController.category;
        iconCheck.setVisible(false);
    }

    public void importExamples() {
        File file = TreeNode.exampleFile(category);
        if (file == null) {
            return;
        }
        isSettingValues = true;
        replaceCheck.setSelected(true);
        isSettingValues = false;
        startFile(file);
    }

    @Override
    public boolean makeMoreParameters() {
        if (category == null) {
            return false;
        }
        if (tableTreeNode == null) {
            tableTreeNode = new TableTreeNode();
        }
        parents = new HashMap<>();
        rootNode = tableTreeNode.findAndCreateRoot(category);
        if (rootNode == null) {
            return false;
        }
        parents.put(rootNode.getTitle(), rootNode);
        parents.put(message(rootNode.getTitle()), rootNode);
        isWebFavorite = TreeNode.WebFavorite.equals(category);
        downIcon = iconCheck.isSelected();
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            long count = importFile(srcFile);
            if (count >= 0) {
                totalItemsHandled += count;
                return message("Imported") + ": " + count;
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public long importFile(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        File validFile = FileTools.removeBOM(file);
        try (Connection conn = DerbyBase.getConnection();
                BufferedReader reader = new BufferedReader(new FileReader(validFile, TextFileTools.charset(validFile)))) {
            conn.setAutoCommit(false);
            String line;
            while ((line = reader.readLine()) != null && line.isBlank()) {
            }
            if (line.startsWith(AppValues.MyBoxSeparator)) {
                return importByMyBoxSeparator(conn, reader);
            } else {
                return importByBlankLine(conn, reader, line);
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return -1;
        }
    }

    public long importByBlankLine(Connection conn, BufferedReader reader, String firstLine) {
        if (conn == null || reader == null || rootNode == null) {
            return -1;
        }
        long count = 0;
        try {
            conn.setAutoCommit(false);
            String line = firstLine, name, value, more, tagsString;
            Date time;
            long parentid, baseTime = new Date().getTime();
            while (line != null) {
                parentid = getParent(conn, line);
                if (parentid < -1) {
                    break;
                }
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.isBlank()) {
                    while ((line = reader.readLine()) != null && line.isBlank()) {
                    }
                    continue;
                }
                name = line;
                line = reader.readLine();
                value = null;
                time = null;
                more = null;
                tagsString = null;
                if (line != null && !line.isBlank()) {
                    if (line.startsWith(TreeNode.TimePrefix)) {
                        time = DateTools.encodeDate(line.substring(TreeNode.TimePrefix.length()));
                        line = reader.readLine();
                    } else {
                        time = DateTools.encodeDate(line);
                        if (time != null) {
                            line = reader.readLine();
                        }
                    }
                    if (line.startsWith(TreeNode.TagsPrefix)) {
                        tagsString = line.substring(TreeNode.TagsPrefix.length());
                        line = reader.readLine();
                    }
                    value = line;
                    if (value != null && !line.isBlank()) {
                        while ((line = reader.readLine()) != null && !line.isBlank()) {
                            value += System.lineSeparator() + line;
                        }
                        if (isWebFavorite) {
                            String[] lines = value.split("\n");
                            if (lines.length > 1) {
                                value = lines[0];
                                more = lines[0];
                            }
                            if (more == null || more.isBlank()) {
                                try {
                                    File iconFile = IconTools.readIcon(value, downIcon);
                                    if (iconFile != null && iconFile.exists()) {
                                        more = iconFile.getAbsolutePath();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                if (time == null) {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
                }
                if (writeNode(conn, parentid, time, name, value, more, tagsString)) {
                    count++;
                }
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
            }
            conn.commit();
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        return count;
    }

    public long importByMyBoxSeparator(Connection conn, BufferedReader reader) {
        if (conn == null || reader == null || rootNode == null) {
            return -1;
        }
        long count = 0;
        try {
            conn.setAutoCommit(false);
            String line, name, value, more, tagsString;
            Date time;
            long parentid, baseTime = new Date().getTime();
            int morePrefixLen = TreeNode.MorePrefix.length();
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                parentid = getParent(conn, line);
                if (parentid < -1) {
                    break;
                }
                value = null;
                time = null;
                more = null;
                tagsString = null;
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line == null) {
                    break;
                }
                if (line.startsWith(AppValues.MyBoxSeparator)) {
                    continue;
                }
                name = line;
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line != null && !line.startsWith(AppValues.MyBoxSeparator)) {
                    if (line.startsWith(TreeNode.TimePrefix)) {
                        time = DateTools.encodeDate(line.substring(TreeNode.TimePrefix.length()));
                        line = reader.readLine();
                    } else {
                        time = DateTools.encodeDate(line);
                        if (time != null) {
                            line = reader.readLine();
                        }
                    }
                    if (line.startsWith(TreeNode.TagsPrefix)) {
                        tagsString = line.substring(TreeNode.TagsPrefix.length());
                        line = reader.readLine();
                    }
                    value = line;
                    if (value != null && !value.startsWith(AppValues.MyBoxSeparator)) {
                        while ((line = reader.readLine()) != null && !line.startsWith(AppValues.MyBoxSeparator)) {
                            value += "\n" + line;
                        }
                    }

                    if (value != null && !value.isBlank()) {
                        int pos = value.indexOf(TreeNode.MorePrefix);
                        if (pos >= 0) {
                            more = value.substring(pos + morePrefixLen).strip();
                            value = value.substring(0, pos);
                        } else if (isWebFavorite) {
                            String[] lines = value.split("\n");
                            if (lines.length > 1) {
                                value = lines[0];
                                more = lines[0];
                            }
                            if (more == null || more.isBlank()) {
                                try {
                                    File iconFile = IconTools.readIcon(value, iconCheck.isSelected());
                                    if (iconFile != null && iconFile.exists()) {
                                        more = iconFile.getAbsolutePath();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                if (time == null) {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
                }
                if (writeNode(conn, parentid, time, name, value, more, tagsString)) {
                    count++;
                }
            }
            conn.commit();
        } catch (Exception e) {
            showLogs(e.toString());
        }
        return count;
    }

    public long getParent(Connection conn, String parentChain) {
        try {
            if (TreeNode.RootIdentify.equals(parentChain)) {
                return -1;
            } else {
                TreeNode parentNode;
                long parentid = rootNode.getNodeid();
                if (parents.containsKey(parentChain)) {
                    parentNode = parents.get(parentChain);
                    parentid = parentNode.getNodeid();
                } else {
                    String chain = parentChain;
                    String prefix = rootNode.getTitle() + TreeNode.NodeSeparater;
                    if (chain.startsWith(prefix)) {
                        chain = chain.substring(prefix.length());
                    } else {
                        prefix = message(rootNode.getTitle()) + TreeNode.NodeSeparater;
                        if (chain.startsWith(prefix)) {
                            chain = chain.substring(prefix.length());
                        } else {
                            prefix = "";
                        }
                    }
                    String[] nodes = chain.split(TreeNode.NodeSeparater);
                    for (String node : nodes) {
                        parentNode = tableTreeNode.find(conn, parentid, node);
                        if (parentNode == null) {
                            parentNode = TreeNode.create()
                                    .setCategory(category)
                                    .setParentid(parentid)
                                    .setUpdateTime(new Date())
                                    .setTitle(node);
                            parentNode = tableTreeNode.insertData(conn, parentNode);
                        }
                        parentid = parentNode.getNodeid();
                        parents.put(prefix + node, parentNode);
                        prefix = prefix + node + TreeNode.NodeSeparater;
                    }
                }
                return parentid;
            }
        } catch (Exception e) {
            showLogs(e.toString());
            MyBoxLog.console(e);
            return -3;
        }
    }

    public boolean writeNode(Connection conn, long parentid, Date time,
            String name, String value, String more, String tags) {
        try {
            if (conn == null || name == null || name.isBlank()) {
                return false;
            }
            TreeNode currentNode = null;
            if (parentid < 0) {
                if (name.equals(category) || name.equals(message(category))) {
                    currentNode = tableTreeNode.findAndCreateRoot(conn, category);
                }
            } else {
                currentNode = tableTreeNode.find(conn, parentid, name);
                if (currentNode != null) {
                    if (replaceCheck.isSelected()) {
                        currentNode.setValue(value == null ? null : value.trim())
                                .setMore(more == null || more.isBlank() ? null : more)
                                .setUpdateTime(time);
                        currentNode = tableTreeNode.updateData(conn, currentNode);
                    }
                } else {
                    currentNode = TreeNode.create()
                            .setCategory(category)
                            .setParentid(parentid)
                            .setTitle(name)
                            .setValue(value == null ? null : value.trim())
                            .setMore(more == null || more.isBlank() ? null : more)
                            .setUpdateTime(time);
                    currentNode = tableTreeNode.insertData(conn, currentNode);
                }
            }
            if (currentNode == null) {
                return false;
            }
            writeTags(conn, currentNode.getNodeid(), tags);
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public void writeTags(Connection conn, long nodeid, String s) {
        try {
            if (conn == null || s == null || s.isBlank()) {
                return;
            }
            String[] values = s.split(TreeNode.TagsSeparater);
            if (values == null || values.length == 0) {
                return;
            }
            for (int i = 0; i < values.length; i = i + 2) {
                String tagName = values[i];
                Color color = null;
                try {
                    color = Color.web(values[i + 1]);
                } catch (Exception e) {
                }
                Tag tag = tableTag.findAndCreate(conn, category, tagName);
                tag.setColor(color);
                tag = tableTag.updateData(conn, tag);
                if (tag == null) {
                    continue;
                }
                if (tableTreeNodeTag.query(conn, nodeid, tag.getTgid()) == null) {
                    TreeNodeTag nodeTag = new TreeNodeTag(nodeid, tag.getTgid());
                    tableTreeNodeTag.insertData(conn, nodeTag);
                }
            }
        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    @Override
    public void afterTask() {
        if (treeController != null) {
            treeController.nodesController.loadTree();
            treeController.tagsController.refreshAction();
            treeController.refreshTimes();
            if (!AppVariables.isTesting) {
                treeController.alertInformation(message("Imported") + ": " + totalItemsHandled);
            }
            closeStage();
        } else if (nodesController != null) {
            nodesController.loadTree();
            if (!AppVariables.isTesting) {
                nodesController.alertInformation(message("Imported") + ": " + totalItemsHandled);
            }
            closeStage();
        } else {
            tableView.refresh();
            if (miaoCheck != null && miaoCheck.isSelected()) {
                SoundTools.miao3();
            }
        }
    }

}
