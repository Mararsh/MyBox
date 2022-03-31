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
import javafx.scene.control.RadioButton;
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
import mara.mybox.tools.IconTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeNodeImportController extends BaseBatchFileController {

    protected TreeManageController treeController;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected TableTag tableTag;
    protected TreeNode rootNode;
    protected String category;

    @FXML
    protected RadioButton overrideRadio, createRadio, skipRadio;
    @FXML
    protected CheckBox iconCheck;
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

    public void importExamples(TreeManageController treeController) {
        setManage(treeController);
        File file = TreeNode.exampleFile(category);
        if (file == null) {
            return;
        }
        isSettingValues = true;
        overrideRadio.fire();
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
        try ( Connection conn = DerbyBase.getConnection();
                 BufferedReader reader = new BufferedReader(new FileReader(file, TextFileTools.charset(file)))) {
            conn.setAutoCommit(false);
            rootNode = tableTreeNode.findAndCreateRoot(conn, category);
            if (rootNode == null) {
                return -1;
            }
            String line;
            while ((line = reader.readLine()) != null && line.isBlank()) {
            }
            if (line.startsWith(AppValues.MyBoxSeparator)) {
                return importByMyBoxSeparator(conn, reader);
            } else {
                return importByBlankLine(conn, reader, line);
            }
        } catch (Exception e) {
            updateLogs(e.toString());
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
            Map<String, TreeNode> parents = new HashMap<>();
            boolean isWebFavorite = TreeNode.WebFavorite.equals(category);
            boolean downIcon = iconCheck.isSelected();
            while (line != null) {
                parentid = getParent(conn, parents, line);
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
                        time = DateTools.stringToDatetime(line.substring(TreeNode.TimePrefix.length()));
                        line = reader.readLine();
                    } else {
                        time = DateTools.stringToDatetime(line);
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
            Map<String, TreeNode> parents = new HashMap<>();
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                parentid = getParent(conn, parents, line);
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
                        time = DateTools.stringToDatetime(line.substring(TreeNode.TimePrefix.length()));
                        line = reader.readLine();
                    } else {
                        time = DateTools.stringToDatetime(line);
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
                    if (value != null && !value.isBlank() && TreeNode.WebFavorite.equals(category)) {
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
                if (time == null) {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
                }
                if (writeNode(conn, parentid, time, name, value, more, tagsString)) {
                    count++;
                }
            }
            conn.commit();
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        return count;
    }

    public long getParent(Connection conn, Map<String, TreeNode> parents, String parentChain) {
        try {
            if (TreeNode.RootIdentify.equals(parentChain)) {
                return -1;
            } else {
                TreeNode parentNode;
                if (parents.containsKey(parentChain)) {
                    parentNode = parents.get(parentChain);
                } else {
                    parentNode = tableTreeNode.findAndCreateChain(conn, rootNode.getNodeid(), parentChain);
                    if (parentNode == null) {
                        return -2;
                    }
                    parents.put(parentChain, parentNode);
                }
                return parentNode.getNodeid();
            }
        } catch (Exception e) {
            updateLogs(e.toString());
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
                if (name.equals(category) || name.equals(message("category"))) {
                    currentNode = tableTreeNode.findAndCreateRoot(conn, category);
                }
            } else {
                if (!createRadio.isSelected()) {
                    currentNode = tableTreeNode.find(conn, parentid, name);
                }
                if (currentNode != null) {
                    if (overrideRadio.isSelected()) {
                        currentNode.setValue(value == null ? null : value.trim())
                                .setMore(more == null || more.isBlank() ? null : more)
                                .setUpdateTime(time);
                        currentNode = tableTreeNode.updateData(conn, currentNode);
                    }
                } else {
                    currentNode = TreeNode.create().setParentid(parentid)
                            .setCategory(category).setTitle(name)
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
            updateLogs(e.toString());
            MyBoxLog.debug(e);
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
            updateLogs(e.toString());
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void donePost() {
        if (treeController != null) {
            treeController.nodesController.loadTree();
            treeController.tagsController.refreshAction();
            treeController.refreshTimes();
            treeController.alertInformation(message("Imported") + ": " + totalItemsHandled);
            closeStage();
        } else {
            tableView.refresh();
            if (miaoCheck != null && miaoCheck.isSelected()) {
                SoundTools.miao3();
            }
        }
    }

}
