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
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeLeaf;
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
    protected TableTree tableTree;
    protected TableTreeLeaf tableTreeLeaf;
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
        tableTree = treeController.tableTree;
        tableTreeLeaf = treeController.tableTreeLeaf;
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
        if (tableTree == null) {
            tableTree = new TableTree();
        }
        if (tableTreeLeaf == null) {
            tableTreeLeaf = new TableTreeLeaf();
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
            rootNode = tableTree.findAndCreateRoot(conn, category);
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
            String line = firstLine, name, value, more;
            Date time;
            long baseTime = new Date().getTime();
            TreeNode treeNode;
            Map<String, TreeNode> owners = new HashMap<>();
            long rootid = rootNode.getNodeid();
            boolean isWebFavorite = TreeNode.WebFavorite.equals(category);
            boolean downIcon = iconCheck.isSelected();
            while (line != null) {
                if (owners.containsKey(line)) {
                    treeNode = owners.get(line);
                } else {
                    treeNode = tableTree.findAndCreateChain(conn, rootid, line);
                    if (treeNode == null) {
                        break;
                    }
                    count++;
                    owners.put(line, treeNode);
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
                long nodeid = treeNode.getNodeid();
                name = line;
                line = reader.readLine();
                value = null;
                time = null;
                more = null;
                if (line != null && !line.isBlank()) {
                    time = DateTools.stringToDatetime(line);
                    if (time == null) {
                        value = line;
                    } else {
                        value = reader.readLine();
                    }
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
                TreeLeaf exist = null;
                if (!createRadio.isSelected()) {
                    exist = tableTreeLeaf.find(conn, nodeid, name);
                }
                if (exist != null) {
                    if (overrideRadio.isSelected()) {
                        exist.setValue(value == null ? null : value.trim())
                                .setMore(more == null || more.isBlank() ? null : more)
                                .setTime(time);
                        if (tableTreeLeaf.updateData(conn, exist) != null) {
                            count++;
                        }
                    }
                } else {
                    TreeLeaf leaf = TreeLeaf.create().setParentid(nodeid).setCategory(category)
                            .setName(name)
                            .setValue(value == null ? null : value.trim())
                            .setMore(more == null || more.isBlank() ? null : more)
                            .setTime(time);
                    if (tableTreeLeaf.insertData(conn, leaf) != null) {
                        count++;
                    }
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
            String line, name, value, more;
            Date time;
            long baseTime = new Date().getTime();
            TreeNode treeNode;
            Map<String, TreeNode> owners = new HashMap<>();
            long rootid = rootNode.getNodeid();
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                if (owners.containsKey(line)) {
                    treeNode = owners.get(line);
                } else {
                    treeNode = tableTree.findAndCreateChain(conn, rootid, line);
                    if (treeNode == null) {
                        break;
                    }
                    owners.put(line, treeNode);
                }
                long nodeid = treeNode.getNodeid();
                value = null;
                time = null;
                more = null;
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
                    if (line.startsWith(TreeLeaf.TimePrefix)) {
                        time = DateTools.stringToDatetime(line.substring(TreeLeaf.TimePrefix.length()));
                    } else {
                        time = DateTools.stringToDatetime(line);
                    }
                    if (time == null) {
                        value = line;
                    }
                    while ((line = reader.readLine()) != null && !line.startsWith(AppValues.MyBoxSeparator)) {
                        if (value == null) {
                            value = line;
                        } else {
                            value += System.lineSeparator() + line;
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
                TreeLeaf exist = null;
                if (!createRadio.isSelected()) {
                    exist = tableTreeLeaf.find(conn, nodeid, name);
                }
                if (exist != null) {
                    if (overrideRadio.isSelected()) {
                        exist.setValue(value == null ? null : value.trim())
                                .setMore(more == null || more.isBlank() ? null : more)
                                .setTime(time);
                        if (tableTreeLeaf.updateData(conn, exist) != null) {
                            count++;
                        }
                    }
                } else {
                    TreeLeaf leaf = TreeLeaf.create().setParentid(nodeid).setCategory(category)
                            .setName(name)
                            .setValue(value == null ? null : value.trim())
                            .setMore(more == null || more.isBlank() ? null : more)
                            .setTime(time);
                    if (tableTreeLeaf.insertData(conn, leaf) != null) {
                        count++;
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        return count;
    }

    @Override
    public void donePost() {
        if (treeController != null) {
            treeController.nodesController.loadTree();
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
