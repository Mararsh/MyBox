package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
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
public class InfoTreeNodeImportController extends BaseBatchFileController {

    protected BaseInfoTreeController treeController;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected TableTag tableTag;
    protected InfoNode rootNode;
    protected String category;
    protected boolean isWebFavorite, downIcon;

    @FXML
    protected ToggleGroup existedGroup;
    @FXML
    protected RadioButton updateRadio, skipRadio, createRadio;
    @FXML
    protected CheckBox iconCheck;
    @FXML
    protected Label formatLabel;

    public InfoTreeNodeImportController() {
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

            String existed = UserConfig.getString(baseName + "Existed", "Update");
            if ("Create".equalsIgnoreCase(existed)) {
                createRadio.setSelected(true);
            } else if ("Skip".equalsIgnoreCase(existed)) {
                skipRadio.setSelected(true);
            } else {
                updateRadio.setSelected(true);
            }
            existedGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle bv) {
                    if (isSettingValues) {
                        return;
                    }
                    if (createRadio.isSelected()) {
                        UserConfig.setString(baseName + "Existed", "Create");
                    } else if (skipRadio.isSelected()) {
                        UserConfig.setString(baseName + "Existed", "Skip");
                    } else {
                        UserConfig.setString(baseName + "Existed", "Update");
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setCaller(BaseInfoTreeController controller) {
        this.treeController = controller;
        tableTreeNode = treeController.tableTreeNode;
        tableTreeNodeTag = treeController.tableTreeNodeTag;
        tableTag = new TableTag();
        category = treeController.category;
        isWebFavorite = InfoNode.isWebFavorite(category);
        iconCheck.setVisible(isWebFavorite);
    }

    public void importExamples() {
        File file = InfoNode.exampleFile(category);
        if (file == null) {
            return;
        }
        isSettingValues = true;
        updateRadio.setSelected(true);
        iconCheck.setSelected(false);
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
        rootNode = tableTreeNode.findAndCreateRoot(category);
        if (rootNode == null) {
            return false;
        }
        downIcon = isWebFavorite && iconCheck.isSelected();
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            long count = importFile(currentTask, srcFile);
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

    public long importFile(FxTask currentTask, File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        File validFile = FileTools.removeBOM(currentTask, file);
        if (currentTask != null && !currentTask.isWorking()) {
            return -4;
        }
        if (validFile == null) {
            return -1;
        }
        try (Connection conn = DerbyBase.getConnection();
                BufferedReader reader = new BufferedReader(new FileReader(validFile, TextFileTools.charset(validFile)))) {
            conn.setAutoCommit(false);
            String line;
            while ((line = reader.readLine()) != null && line.isBlank()) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return -4;
                }
            }
            if (line == null) {
                return -2;
            }
            if (line.startsWith(AppValues.MyBoxSeparator)) {
                return importByMyBoxSeparator(currentTask, conn, reader);
            } else {
                return importByBlankLine(currentTask, conn, reader, line);
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return -3;
        }
    }

    public long importByBlankLine(FxTask currentTask, Connection conn, BufferedReader reader, String firstLine) {
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
                if (currentTask != null && !currentTask.isWorking()) {
                    return -4;
                }
                parentid = getParent(currentTask, conn, line);
                if (parentid < -1) {
                    break;
                }
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.isBlank()) {
                    while ((line = reader.readLine()) != null && line.isBlank()) {
                        if (currentTask != null && !currentTask.isWorking()) {
                            return -4;
                        }
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
                            if (currentTask != null && !currentTask.isWorking()) {
                                return -4;
                            }
                            value += System.lineSeparator() + line;
                        }
                        if (isWebFavorite) {
                            String[] lines = value.split("\n");
                            if (lines.length > 1) {
                                value = lines[0];
                                more = lines[1];
                            }
                            if (more == null || more.isBlank()) {
                                try {
                                    File iconFile = IconTools.readIcon(currentTask, value, downIcon);
                                    if (iconFile != null && iconFile.exists()) {
                                        more = iconFile.getAbsolutePath();
                                    }
                                } catch (Exception e) {
                                }
                            }
                            if (more != null && !more.isBlank()) {
                                value += InfoNode.ValueSeparater + "\n" + more;
                            }
                        }
                    }
                }
                if (time == null) {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
                }
                if (writeNode(conn, parentid, time, name, value, tagsString)) {
                    count++;
                }
                while ((line = reader.readLine()) != null && line.isBlank()) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return -4;
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        return count;
    }

    public long importByMyBoxSeparator(FxTask currentTask, Connection conn, BufferedReader reader) {
        if (conn == null || reader == null || rootNode == null) {
            return -1;
        }
        long count = 0;
        try {
            conn.setAutoCommit(false);
            String line, name, info, tagsString;
            Date time;
            long parentid, baseTime = new Date().getTime();
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return -4;
                }
                parentid = getParent(currentTask, conn, line);
                if (currentTask != null && !currentTask.isWorking()) {
                    return -4;
                }
                if (parentid < -1) {
                    break;
                }
                info = null;
                time = null;
                tagsString = null;
                while ((line = reader.readLine()) != null && line.isBlank()) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return -4;
                    }
                }
                if (line == null) {
                    break;
                }
                if (line.startsWith(AppValues.MyBoxSeparator)) {
                    continue;
                }
                name = line;
                while ((line = reader.readLine()) != null && line.isBlank()) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return -4;
                    }
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
                    info = line;
                    if (info != null && !info.startsWith(AppValues.MyBoxSeparator)) {
                        while ((line = reader.readLine()) != null && !line.startsWith(AppValues.MyBoxSeparator)) {
                            if (currentTask != null && !currentTask.isWorking()) {
                                return -4;
                            }
                            info += "\n" + line;
                        }
                    }
                    if (info != null && !info.isBlank() && isWebFavorite) {
                        try {
                            File iconFile = IconTools.readIcon(currentTask, info, downIcon);
                            if (iconFile != null && iconFile.exists()) {
                                info += InfoNode.ValueSeparater + "\n" + iconFile.getAbsolutePath();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                if (time == null) {
                    time = new Date(baseTime - count * 1000); // to keep the order of id
                }
                if (writeNode(conn, parentid, time, name, info, tagsString)) {
                    count++;
                }
            }
            conn.commit();
        } catch (Exception e) {
            showLogs(e.toString());
        }
        return count;
    }

    public long getParent(FxTask currentTask, Connection conn, String parentChain) {
        try {
            if (InfoNode.RootIdentify.equals(parentChain)) {
                return -1;
            } else {
                long parentid = rootNode.getNodeid();
                String chain = parentChain;
                String prefix = rootNode.getTitle() + InfoNode.TitleSeparater;
                if (chain.startsWith(prefix)) {
                    chain = chain.substring(prefix.length());
                } else {
                    prefix = message(rootNode.getTitle()) + InfoNode.TitleSeparater;
                    if (chain.startsWith(prefix)) {
                        chain = chain.substring(prefix.length());
                    }
                }
                String[] names = chain.split(InfoNode.TitleSeparater);
                for (String name : names) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return -4;
                    }
                    InfoNode parentNode = tableTreeNode.find(conn, parentid, name);
                    if (parentNode == null) {
                        parentNode = InfoNode.create()
                                .setCategory(category)
                                .setParentid(parentid)
                                .setUpdateTime(new Date())
                                .setTitle(name);
                        parentNode = tableTreeNode.insertData(conn, parentNode);
                    }
                    parentid = parentNode.getNodeid();
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
            String name, String info, String tags) {
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
                info = InfoNode.encodeInfo(category, info);
                currentNode = tableTreeNode.find(conn, parentid, name);
                if (currentNode != null) {
                    if (updateRadio.isSelected()) {
                        currentNode.setInfo(info)
                                .setUpdateTime(time);
                        currentNode = tableTreeNode.updateData(conn, currentNode);

                    } else if (createRadio.isSelected()) {
                        currentNode = InfoNode.create()
                                .setCategory(category)
                                .setParentid(parentid)
                                .setTitle(name)
                                .setInfo(info)
                                .setUpdateTime(time);
                        currentNode = tableTreeNode.insertData(conn, currentNode);
                    } else {
                        return false;
                    }
                } else {
                    currentNode = InfoNode.create()
                            .setCategory(category)
                            .setParentid(parentid)
                            .setTitle(name)
                            .setInfo(info)
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
            String[] values = s.split(InfoNode.TagSeparater);
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
        if (treeController != null) {
            treeController.loadTree();

            closeStage();

            treeController.refreshTagss();
            treeController.refreshTimes();
            if (!AppVariables.isTesting) {
                treeController.popInformation(message("Imported") + ": " + totalItemsHandled);
            }

        } else {
            tableView.refresh();
            if (miaoCheck != null && miaoCheck.isSelected()) {
                SoundTools.miao3();
            }
        }
    }

    @FXML
    public void demo() {
        File file = InfoNode.exampleFile(category);
        if (file == null) {
            file = InfoNode.exampleFile(InfoNode.Notebook);
        }
        TextEditorController.open(file);
    }

    @FXML
    public void aboutTreeInformation() {
        openHtml(HelpTools.aboutTreeInformation());
    }

}
