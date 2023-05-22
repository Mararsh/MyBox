package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.FileNode;
import mara.mybox.db.data.PathConnection;
import mara.mybox.db.table.TablePathConnection;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.tools.FileTools.showFileSize;
import mara.mybox.tools.FloatTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class ControlRemoteConnection extends BaseSysTableController<PathConnection> {

    protected BaseTaskController taskController;
    protected Session sshSession;
    protected ChannelSftp sftp;
    protected TablePathConnection tablePathConnection;
    protected PathConnection currentConnection;
    protected int count;

    @FXML
    protected TableColumn<PathConnection, String> titleColumn, hostColumn, pathColumn;
    @FXML
    protected TextField titleInput, hostInput, protocalInput, portInput, userInput, pathInput,
            timeoutInput, retryInput, timeInput;
    @FXML
    protected PasswordField passwordInput;
    @FXML
    protected CheckBox hostKeyCheck;
    @FXML
    protected Label statusLabel;

    public ControlRemoteConnection() {
        baseTitle = message("DirectorySynchronizeSFTP");
    }

    @Override
    public void setTableDefinition() {
        tablePathConnection = new TablePathConnection();
        tableDefinition = tablePathConnection;
        queryConditions = " type='" + PathConnection.Type.SFTP.name() + "'";
    }

    @Override
    protected void initColumns() {
        try {
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
            pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(BaseTaskController taskController) {
        try {
            this.taskController = taskController;
            this.baseName = taskController.baseName;

            loadTableData();
            editProfile(null);
            statusLabel.setText(message("Disconnected"));
            taskController.setTitle(taskController.baseTitle + " " + message("Disconnected"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void itemClicked() {
        editAction();
    }

    @Override
    public void itemDoubleClicked() {
        taskController.startAction();
    }

    @FXML
    @Override
    public void editAction() {
        PathConnection selected = selectedItem();
        if (selected == null) {
            return;
        }
        editProfile(selected);
    }

    public void editProfile(PathConnection profile) {
        currentConnection = profile;
        if (currentConnection == null) {
            currentConnection = new PathConnection();
            editingIndex = -1;
        } else {
            editingIndex = tableData.indexOf(currentConnection);
        }
        titleInput.setText(currentConnection.getTitle());
        hostInput.setText(currentConnection.getHost());
        protocalInput.setText(currentConnection.getType().name().toLowerCase());
        portInput.setText(currentConnection.getPort() + "");
        userInput.setText(currentConnection.getUsername());
        passwordInput.setText(currentConnection.getPassword());
        pathInput.setText(currentConnection.getPath());
        timeoutInput.setText(currentConnection.getTimeout() + "");
        retryInput.setText(currentConnection.getRetry() + "");
        hostKeyCheck.setSelected(currentConnection.isHostKeyCheck());
        timeInput.setText(DateTools.datetimeToString(currentConnection.getModifyTime()));
    }

    @FXML
    @Override
    public void addAction() {
        editProfile(null);
    }

    protected boolean pickProfile() {
        if (currentConnection == null) {
            currentConnection = new PathConnection();
        }
        int port, timeout, retry;
        try {
            port = Integer.parseInt(portInput.getText());
        } catch (Exception e) {
            port = -1;
        }
        if (port <= 0) {
            popError(message("InvalidParameter") + ": " + message("Port"));
            return false;
        }
        try {
            timeout = Integer.parseInt(timeoutInput.getText());
        } catch (Exception e) {
            timeout = -1;
        }
        if (timeout <= 0) {
            popError(message("InvalidParameter") + ": " + message("ConnectionTimeout"));
            return false;
        }
        try {
            retry = Integer.parseInt(retryInput.getText());
        } catch (Exception e) {
            retry = -1;
        }
        if (retry < 0) {
            popError(message("InvalidParameter") + ": " + message("MaxRetries"));
            return false;
        }
        String host = hostInput.getText();
        if (host == null || host.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("Host"));
            return false;
        }
        currentConnection.setPort(port);
        currentConnection.setTimeout(timeout);
        currentConnection.setRetry(retry);
        currentConnection.setTitle(titleInput.getText());
        currentConnection.setHost(host.trim());
        currentConnection.setUsername(userInput.getText());
        currentConnection.setPassword(passwordInput.getText());
        currentConnection.setPath(fixFilename(pathInput.getText()));
        currentConnection.setType(PathConnection.Type.SFTP);
        currentConnection.setPort(22);
        currentConnection.setHostKeyCheck(hostKeyCheck.isSelected());
        currentConnection.setModifyTime(new Date());
        return true;
    }

    @FXML
    @Override
    public void saveAction() {
        if (!pickProfile()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                currentConnection = tablePathConnection.writeData(currentConnection);
                return currentConnection != null;
            }

            @Override
            protected void whenSucceeded() {
                loadTableData();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void copyAction() {
        if (currentConnection == null) {
            currentConnection = new PathConnection();
        } else {
            try {
                currentConnection = currentConnection.copy();
            } catch (Exception e) {
            }
        }
        String title = currentConnection.getTitle();
        currentConnection.setPcnid(-1);
        currentConnection.setTitle(title == null ? message("Copy") : title + " " + message("Copy"));
        editProfile(currentConnection);
    }

    /*
        sftp
     */
    public boolean connect(SingletonTask<Void> task) {
        try {
            disconnect();
            if (currentConnection == null) {
                return false;
            }
            this.task = task;
            int repeat = 0;
            boolean ok = false;
            while (repeat++ <= currentConnection.getRetry()) {
                ok = sftp();
                if (ok) {
                    break;
                } else {
                    showLogs("Retry...");
                }
            }
            showLogs("Login in path: " + currentConnection.getPath());
            mkdirs(currentConnection.getPath(), -1, -1);
            return ok;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean sftp() {
        try {
            if (currentConnection == null) {
                return false;
            }
            sshSession = new JSch().getSession(currentConnection.getUsername(),
                    currentConnection.getHost(), 22);
            sshSession.setPassword(currentConnection.getPassword());
            sshSession.setTimeout(currentConnection.getTimeout());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", hostKeyCheck.isSelected() ? "yes" : "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            showLogs("SSH session connected: " + currentConnection.getHost());
            showLogs("Opening channel for sftp...");

            sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();
            showLogs("sftp channel connected.");
            showLogs("version: " + sftp.version());
            showLogs("home: " + sftp.getHome());
            String path = currentConnection.getPath();
            if (path == null || path.isBlank()) {
                currentConnection.setPath(sftp.getHome());
            }
            Platform.runLater(() -> {
                statusLabel.setText(message("Connected") + ": " + currentConnection.getHost());
                taskController.setTitle(taskController.baseTitle + " " + statusLabel.getText());
            });
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean disconnect() {
        try {
            if (sftp != null) {
                sftp.disconnect();
                sftp.exit();
                sftp = null;
                showLogs("Channel exited.");
            }
            if (sshSession != null) {
                sshSession.disconnect();
                sshSession = null;
                showLogs("Session disconnected.");
            }
            Platform.runLater(() -> {
                statusLabel.setText(message("Disconnected"));
                taskController.setTitle(taskController.baseTitle + " " + statusLabel.getText());
            });
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public String host() {
        try {
            if (currentConnection == null) {
                return null;
            }
            return currentConnection.getHost();
        } catch (Exception e) {
            return null;
        }
    }

    public String fixFilename(String filename) {
        try {
            return filename.replaceAll("\\\\", "/");
        } catch (Exception e) {
            return filename;
        }
    }

    public SftpATTRS stat(String filename) {
        try {
            String name = fixFilename(filename);
            showLogs("stat " + name);
            return sftp.stat(name);
        } catch (Exception e) {
            showLogs(e.toString());
            return null;
        }
    }

    public Iterator<LsEntry> ls(String filename) {
        try {
            String name = fixFilename(filename);
            showLogs("ls " + name);
            return sftp.ls(name).iterator();
        } catch (Exception e) {
            showLogs(e.toString());
            return null;
        }
    }

    public FileNode FileNode(String nodename) {
        return FileNode(null, nodename);
    }

    public FileNode FileNode(FileNode parent, String nodename) {
        return new FileNode()
                .setNodename(nodename)
                .setParentFile(parent)
                .setIsRemote(true)
                .attrs(stat(nodename));
    }

    public List<FileNode> children(FileNode targetNode) {
        List<FileNode> children = new ArrayList<>();
        try {
            Iterator<ChannelSftp.LsEntry> iterator = ls(targetNode.fullName());
            if (iterator == null) {
                return children;
            }
            while (iterator.hasNext()) {
                if (task == null || task.isCancelled()) {
                    return children;
                }
                ChannelSftp.LsEntry entry = iterator.next();
                String name = entry.getFilename();
                if (name == null || name.isBlank() || ".".equals(name) || "..".equals(name)) {
                    continue;
                }
                FileNode fileInfo = new FileNode()
                        .setNodename(name)
                        .setParentFile(targetNode)
                        .setIsRemote(true)
                        .attrs(entry.getAttrs());
                children.add(fileInfo);
            }
        } catch (Exception e) {
            showLogs(e.toString());
        }
        return children;
    }

    public boolean fileExist(String filename) {
        return stat(filename) != null;
    }

    public boolean isDirectory(String filename) {
        try {
            if ("/".equals(filename)) {
                return true;
            }
            SftpATTRS attrs = stat(filename);
            return attrs != null && attrs.isDir();
        } catch (Exception e) {
//            showLogs(e.toString());
            return false;
        }
    }

    public long fileLength(String filename) {
        try {
            SftpATTRS attrs = stat(filename);
            if (attrs != null) {
                return attrs.getSize();
            }
        } catch (Exception e) {
//            showLogs(e.toString());
        }
        return -1;
    }

    public long fileModifyTime(String filename) {
        try {
            SftpATTRS attrs = stat(filename);
            if (attrs != null) {
                return attrs.getMTime() * 1000l;
            }
        } catch (Exception e) {
//            showLogs(e.toString());
        }
        return -1;
    }

    public boolean put(File sourceFile, String target, boolean copyMtime, int permission) {
        try {
            if (task == null || task.isCancelled()
                    || target == null || sourceFile == null
                    || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            String sourceName = sourceFile.getAbsolutePath();
            String targetName = fixFilename(target);
            showLogs("put " + sourceName + " " + targetName);
            sftp.put(sourceName, targetName, new ProgressMonitor(sourceFile.length()));
            showLogs(MessageFormat.format(message("FilesGenerated"), targetName));
            if (copyMtime) {

            }
            setStat(targetName, copyMtime ? (int) (sourceFile.lastModified() / 1000) : -1, permission);
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean setStat(String target, int time, int permission) {
        try {
            if (task == null || task.isCancelled() || target == null) {
                return false;
            }
            if (time > 0 || permission > 0) {
                String targetName = fixFilename(target);
                SftpATTRS attrs = stat(targetName);
                if (attrs == null) {
                    return false;
                }
                String msg = "";
                if (permission > 0) {
                    msg += message("SetPermissions") + ": " + permission;
                    attrs.setPERMISSIONS(permission);
                }
                if (time > 0) {
                    attrs.setACMODTIME(time, time);
                    msg += "  " + message("CopyModifyTime") + ": "
                            + DateTools.datetimeToString(time * 1000l);
                }
                showLogs(msg);
                sftp.setStat(targetName, attrs);
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean get(String source, SftpATTRS attrs, File targetFile, boolean copyMtime) {
        try {
            if (task == null || task.isCancelled()
                    || targetFile == null || source == null || attrs == null) {
                return false;
            }
            targetFile.getParentFile().mkdirs();
            String targetName = targetFile.getAbsolutePath();
            showLogs("get " + source + " " + targetName);
            sftp.get(source, targetName, new ProgressMonitor(attrs.getSize()));
            showLogs(MessageFormat.format(message("FilesGenerated"), targetName));
            if (copyMtime) {
                long time = attrs.getMTime() * 1000l;
                showLogs(message("CopyModifyTime") + ": " + DateTools.datetimeToString(time));
                targetFile.setLastModified(time);
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean renameFile(String filename, String newname) {
        try {
            String name = fixFilename(filename);
            showLogs("rename " + name + " " + newname);
            sftp.rename(name, newname);
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean delete(String filename) {
        if (isDirectory(filename)) {
            return deleteDirectory(filename);
        } else {
            return deleteFile(filename);
        }
    }

    public boolean deleteFile(String filename) {
        try {
            String name = fixFilename(filename);
            showLogs("rm " + name);
            sftp.rm(name);
            count++;
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean deleteDirectory(String dirname) {
        try {
            dirname = fixFilename(dirname);
            if (!clearDirectory(dirname)) {
                return false;
            }
            showLogs("rmdir " + dirname);
            sftp.rmdir(dirname);
            count++;
            return true;
        } catch (Exception e) {
            error = e.toString();
            showLogs(error);
            return false;
        }
    }

    public boolean clearDirectory(String dirname) {
        try {
            dirname = fixFilename(dirname);
            Iterator<LsEntry> iterator = ls(dirname);
            if (iterator == null) {
                return false;
            }
            while (iterator.hasNext()) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                LsEntry entry = iterator.next();
                String child = entry.getFilename();
                if (child == null || child.isBlank()
                        || ".".equals(child) || "..".equals(child)) {
                    continue;
                }
                child = dirname + "/" + child;
                SftpATTRS attrs = entry.getAttrs();
                if (attrs.isDir()) {
                    if (clearDirectory(child)) {
                        showLogs("rmdir " + child);
                        sftp.rmdir(child);
                        count++;
                    } else {
                        if (task != null) {
                            task.cancel();
                        }
                        return false;
                    }
                } else {
                    showLogs("rm " + child);
                    sftp.rm(child);
                    count++;
                }
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean mkdirs(String filename) {
        return mkdirs(filename, -1, -1);
    }

    public boolean mkdirs(String filename, int time, int permission) {
        try {
            if (filename == null || filename.isBlank()) {
                return false;
            }
            String fixedName = fixFilename(filename);
            String[] names = fixedName.split("/");
            String parent = "";
            for (String name : names) {
                if (name == null || name.isBlank()
                        || ".".equals(name) || "..".equals(name)) {
                    continue;
                }
                String path = parent + "/" + name;
                SftpATTRS attrs = null;
                try {
                    attrs = sftp.stat(path);
                } catch (Exception e) {
                }
                if (attrs == null) {
                    showLogs("mkdirs " + path);
                    sftp.mkdir(path);
//                    setStat(path, -1, permission);   // seems can not change mtime of directory 
                }
                parent = path;
            }
            return true;
        } catch (Exception e) {
            error = e.toString();
            showLogs(error);
            return false;
        }
    }

    public void showLogs(String log) {
        taskController.showLogs(log);
        if (task != null) {
            task.setInfo(log);
        }
    }

    public void updateLogs(String log, boolean immediate) {
        taskController.updateLogs(log, true, immediate);
        if (task != null) {
            task.setInfo(log);
        }
    }

    public class ProgressMonitor implements SftpProgressMonitor {

        private final long srcLen;
        private long len = 0;

        public ProgressMonitor(long srcLen) {
            this.srcLen = srcLen;
        }

        @Override
        public boolean count(long count) {
            len += count;
            if (taskController.verboseCheck.isSelected() && len % 500 == 0) {
                if (srcLen > 0) {
                    taskController.updateLogs(message("Status") + ": "
                            + FloatTools.percentage(len, srcLen) + "%   "
                            + showFileSize(len) + "/" + showFileSize(srcLen));
                } else {
                    taskController.updateLogs(message("Status") + ": " + showFileSize(len));
                }
            }
            return true;
        }

        @Override
        public void end() {
        }

        @Override
        public void init(int op, String src, String dest, long max) {
        }
    }

}
