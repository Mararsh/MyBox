package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.WebFavorite;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-11
 * @License Apache License Version 2.0
 */
public class WebFavoritesImportController extends BaseBatchFileController {

    protected WebFavoritesController favoritesController;
    protected TableTree tableTree;
    protected TableWebFavorite tableWebFavorite;
    protected TreeNode rootNode;

    @FXML
    protected RadioButton overrideRadio, createRadio, skipRadio;
    @FXML
    protected CheckBox iconCheck;

    public WebFavoritesImportController() {
        baseTitle = AppVariables.message("ImportWebFavorites");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void importExamples(WebFavoritesController favoritesController) {
        this.favoritesController = favoritesController;
        tableTree = favoritesController.tableTree;
        tableWebFavorite = favoritesController.tableWebFavorite;
        String lang = AppVariables.isChinese() ? "zh" : "en";
        File dataFile = FxmlControl.getInternalFile("/data/db/WebFavorites_Examples_" + lang + ".txt",
                "data", "WebFavorites_Examples_" + lang + ".txt", false);
        isSettingValues = true;
        overrideRadio.fire();
        iconCheck.setSelected(false);
        isSettingValues = false;
        startFile(dataFile);
    }

    @Override
    public boolean makeMoreParameters() {
        if (tableTree == null) {
            tableTree = new TableTree();
        }
        if (tableWebFavorite == null) {
            tableWebFavorite = new TableWebFavorite();
        }
        rootNode = tableTree.findAndCreateRoot(message("WebFavorites"));
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            long count = importData(srcFile);
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

    public long importData(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        long count = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 BufferedReader reader = new BufferedReader(new FileReader(file, FileTools.charset(file)))) {
            conn.setAutoCommit(false);
            String line = null, title, address, icon;
            TreeNode node;
            long rootid = rootNode.getNodeid();
            Map<String, TreeNode> owners = new HashMap<>();
            while (true) {
                while ((line = reader.readLine()) != null && line.isBlank()) {
                }
                if (line == null) {
                    break;
                }
                if (owners.containsKey(line)) {
                    node = owners.get(line);
                } else {
                    node = tableTree.findAndCreateChain(conn, rootid, line);
                    if (node == null) {
                        break;
                    }
                    owners.put(line, node);
                }
                long nodeid = node.getNodeid();
                title = reader.readLine();
                if (title == null || title.isBlank()) {
                    continue;
                }
                address = reader.readLine();
                try {
                    URL url = new URL(address);
                    icon = reader.readLine();
                    if (icon == null || icon.isBlank()) {
                        File iconFile = HtmlTools.readIcon(address, iconCheck.isSelected());
                        if (iconFile != null && iconFile.exists()) {
                            icon = iconFile.getAbsolutePath();
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
                WebFavorite exist = null;
                if (!createRadio.isSelected()) {
                    exist = tableWebFavorite.find(conn, nodeid, title);
                }
                if (exist != null) {
                    if (overrideRadio.isSelected()) {
                        exist.setAddress(address);
                        exist.setIcon(icon == null || icon.isBlank() ? null : icon);
                        if (tableWebFavorite.updateData(conn, exist) != null) {
                            count++;
                        }
                    }
                } else if (tableWebFavorite.insertData(conn, new WebFavorite(nodeid, title, address, icon)) != null) {
                    count++;
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
        if (favoritesController != null) {
            favoritesController.treeController.loadTree();
            favoritesController.alertInformation(message("Imported") + ": " + totalItemsHandled);
            closeStage();
        } else {
            tableView.refresh();
            if (miaoCheck != null && miaoCheck.isSelected()) {
                FxmlControl.miao3();
            }
        }
    }

}
