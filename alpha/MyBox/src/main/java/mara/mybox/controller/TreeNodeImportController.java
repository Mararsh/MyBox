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
import mara.mybox.value.Languages;
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
        baseTitle = message("ImportWebFavorites");
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
        if (treeController instanceof WebFavoritesController) {
            formatLabel.setText(message("ImportNotesComments"));
        } else {
            formatLabel.setText(message("ImportNotesComments"));
        }
        iconCheck.setVisible(treeController instanceof WebFavoritesController);
    }

    public void importExamples(TreeManageController treeController) {
        setManage(treeController);
        String lang = Languages.isChinese() ? "zh" : "en";
        File file;
        if (treeController instanceof WebFavoritesController) {
            file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/db/WebFavorites_Examples_" + lang + ".txt",
                    "data", "WebFavorites_Examples_" + lang + ".txt");
        } else {
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
            if (!"WebFavorites".equals(category) && line.startsWith(AppValues.MyBoxSeparator)) {
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
            while (line != null) {
                if (owners.containsKey(line)) {
                    treeNode = owners.get(line);
                } else {
                    treeNode = tableTree.findAndCreateChain(conn, rootid, line);
                    if (treeNode == null) {
                        break;
                    }
                    owners.put(line, treeNode);
                }
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line == null) {
                    break;
                }
                long nodeid = treeNode.getNodeid();
                name = line;
                line = reader.readLine();
                value = null;
                time = null;
                more = null;
                if (line != null) {
                    time = DateTools.stringToDatetime(line);
                    if (time == null) {
                        value = line;
                    } else {
                        value = reader.readLine();
                    }
                    if (value != null) {
                        if ("WebFavorites".equals(category)) {
                            more = reader.readLine();
                            if (more != null) {
                                line = reader.readLine();
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
                        } else {
                            while ((line = reader.readLine()) != null && !line.isBlank()) {
                                value += System.lineSeparator() + line;
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
                        exist.setValue(value);
                        exist.setTime(time);
                        exist.setMore(more == null || more.isBlank() ? null : more);
                        if (tableTreeLeaf.updateData(conn, exist) != null) {
                            count++;
                        }
                    }
                } else if (tableTreeLeaf.insertData(conn, new TreeLeaf(nodeid, name, value, more, time)) != null) {
                    count++;
                }
                if (line == null) {
                    while ((line = reader.readLine()) != null && line.isBlank()) {
                    }
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
            String line, name, value;
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
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line == null) {
                    break;
                }
                name = line;
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line != null) {
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
                        exist.setValue(value);
                        exist.setTime(time);
                        if (tableTreeLeaf.updateData(conn, exist) != null) {
                            count++;
                        }
                    }
                } else if (tableTreeLeaf.insertData(conn, new TreeLeaf(nodeid, name, value, time)) != null) {
                    count++;
                }
                if (line == null) {
                    break;
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
