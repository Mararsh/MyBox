package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.data.InfoNodeTag;
import mara.mybox.db.data.Tag;
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
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-9
 * @License Apache License Version 2.0
 */
public class TreeNodeImportController extends BaseBatchFileController {

    protected BaseInfoTreeController nodesController;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected TableTag tableTag;
    protected InfoNode rootNode;
    protected String category;
    protected Map<String, Long> parents;
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

    @Override
    public void initControls() {
        try {
            super.initControls();

            replaceCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceExisted", true));

            replaceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceExisted", newv);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setCaller(BaseInfoTreeController nodeController) {
        this.nodesController = nodeController;
        tableTreeNode = nodeController.tableTreeNode;
        tableTreeNodeTag = nodeController.tableTreeNodeTag;
        tableTag = new TableTag();
        category = nodeController.category;
        iconCheck.setVisible((nodeController instanceof ControlInfoTreeManage)
                && (category.equals(InfoNode.WebFavorite)
                || category.equals(message(InfoNode.WebFavorite))));
    }

    public void importExamples() {
        File file = InfoNode.exampleFile(category);
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
        parents.put(rootNode.getTitle(), rootNode.getNodeid());
        parents.put(message(rootNode.getTitle()), rootNode.getNodeid());
        isWebFavorite = InfoNode.WebFavorite.equals(category);
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
                    if (line.startsWith(InfoNode.TimePrefix)) {
                        time = DateTools.encodeDate(line.substring(InfoNode.TimePrefix.length()));
                        line = reader.readLine();
                    } else {
                        time = DateTools.encodeDate(line);
                        if (time != null) {
                            line = reader.readLine();
                        }
                    }
                    if (line.startsWith(InfoNode.TagsPrefix)) {
                        tagsString = line.substring(InfoNode.TagsPrefix.length());
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
            int morePrefixLen = InfoNode.MorePrefix.length();
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
                    if (line.startsWith(InfoNode.TimePrefix)) {
                        time = DateTools.encodeDate(line.substring(InfoNode.TimePrefix.length()));
                        line = reader.readLine();
                    } else {
                        time = DateTools.encodeDate(line);
                        if (time != null) {
                            line = reader.readLine();
                        }
                    }
                    if (line.startsWith(InfoNode.TagsPrefix)) {
                        tagsString = line.substring(InfoNode.TagsPrefix.length());
                        line = reader.readLine();
                    }
                    value = line;
                    if (value != null && !value.startsWith(AppValues.MyBoxSeparator)) {
                        while ((line = reader.readLine()) != null && !line.startsWith(AppValues.MyBoxSeparator)) {
                            value += "\n" + line;
                        }
                    }

                    if (value != null && !value.isBlank()) {
                        int pos = value.indexOf(InfoNode.MorePrefix);
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
            if (InfoNode.RootIdentify.equals(parentChain)) {
                return -1;
            } else {

                long parentid = rootNode.getNodeid();
                if (parents.containsKey(parentChain)) {
                    parentid = parents.get(parentChain);
                } else {
                    String chain = parentChain;
                    String prefix = rootNode.getTitle() + InfoNode.NodeSeparater;
                    if (chain.startsWith(prefix)) {
                        chain = chain.substring(prefix.length());
                    } else {
                        prefix = message(rootNode.getTitle()) + InfoNode.NodeSeparater;
                        if (chain.startsWith(prefix)) {
                            chain = chain.substring(prefix.length());
                        } else {
                            prefix = "";
                        }
                    }
                    String[] nodes = chain.split(InfoNode.NodeSeparater);
                    for (String node : nodes) {
                        InfoNode parentNode = tableTreeNode.find(conn, parentid, node);
                        if (parentNode == null) {
                            parentNode = InfoNode.create()
                                    .setCategory(category)
                                    .setParentid(parentid)
                                    .setUpdateTime(new Date())
                                    .setTitle(node);
                            parentNode = tableTreeNode.insertData(conn, parentNode);
                        }
                        parentid = parentNode.getNodeid();
                        parents.put(prefix + node, parentid);
                        prefix = prefix + node + InfoNode.NodeSeparater;
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
            InfoNode currentNode = null;
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
                    currentNode = InfoNode.create()
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
            String[] values = s.split(InfoNode.TagsSeparater);
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
                    InfoNodeTag nodeTag = new InfoNodeTag(nodeid, tag.getTgid());
                    tableTreeNodeTag.insertData(conn, nodeTag);
                }
            }
        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    @Override
    public void afterTask() {
        if (nodesController != null) {
            nodesController.loadTree();
            if (!AppVariables.isTesting) {
                nodesController.alertInformation(message("Imported") + ": " + totalItemsHandled);
            }
            closeStage();
            nodesController.afterImport();
        } else {
            tableView.refresh();
            if (miaoCheck != null && miaoCheck.isSelected()) {
                SoundTools.miao3();
            }
        }
    }

}
