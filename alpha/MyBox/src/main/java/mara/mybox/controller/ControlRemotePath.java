package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.PathConnection;
import mara.mybox.db.table.TablePathConnection;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class ControlRemotePath extends BaseSysTableController<PathConnection> {

    protected BaseTaskController taskController;
    protected Session sshSession;
    protected ChannelSftp sftp;
    protected TablePathConnection tablePathConnection;
    protected PathConnection currentConnection;

    @FXML
    protected TableColumn<PathConnection, String> titleColumn, hostColumn, pathColumn;
    @FXML
    protected TextField titleInput, hostInput, userInput, pathInput, timeoutInput, retryInput, timeInput;
    @FXML
    protected PasswordField passwordInput;
    @FXML
    protected CheckBox hostKeyCheck;

    public ControlRemotePath() {
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
            loadTableData();
            editProfile(null);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void itemClicked() {
        editAction();
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
        int timeout, retry;
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
        String remotePath = pathInput.getText();
        if (remotePath == null || remotePath.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("RemotePath"));
            return false;
        }
        currentConnection.setTimeout(timeout);
        currentConnection.setRetry(retry);
        currentConnection.setTitle(titleInput.getText().trim());
        currentConnection.setHost(host.trim());
        currentConnection.setUsername(userInput.getText());
        currentConnection.setPassword(passwordInput.getText());
        currentConnection.setPath(fixFilename(remotePath.trim()));
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
    public boolean isConnected() {
        return sftp != null;
    }

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
            mkdirs(currentConnection.getPath());
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
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public String fixFilename(String filename) {
        try {
            return filename.replaceAll("\\\\", "/");
        } catch (Exception e) {
            return filename;
        }
    }

    public LsEntry find(String filename) {
        try {
            if (filename == null || filename.isBlank() || "/".equals(filename)) {
                return null;
            }
            String parent = fixFilename(new File(filename).getParent());
            Iterator<LsEntry> iterator = ls(parent);
            while (iterator.hasNext()) {
                LsEntry entry = iterator.next();
                if (filename.equals(parent + "/" + entry.getFilename())) {
                    return entry;
                }
            }
        } catch (Exception e) {
//            showLogs(e.toString());
        }
        return null;
    }

    public Iterator<LsEntry> ls(String filename) {
        try {
            String name = fixFilename(filename);
            showLogs("ls " + fixFilename(name));
            return sftp.ls(fixFilename(name)).iterator();
        } catch (Exception e) {
//            showLogs(e.toString());
            return null;
        }
    }

    public boolean fileExist(String filename) {
        return find(filename) != null;
    }

    public List<String> fileChildren(String filename) {
        List<String> list = new ArrayList<>();
        try {
            filename = fixFilename(filename);
            Iterator<LsEntry> iterator = ls(filename);
            while (iterator.hasNext()) {
                LsEntry entry = iterator.next();
                String name = entry.getFilename();
                if (name == null || name.isBlank()
                        || ".".equals(name) || "..".equals(name)) {
                    continue;
                }
                list.add(filename + "/" + name);
            }
        } catch (Exception e) {
//            showLogs(e.toString());
        }
        return list;
    }

    public boolean isDirectory(String filename) {
        try {
            if ("/".equals(filename)) {
                return true;
            }
            LsEntry entry = find(filename);
            return entry != null && entry.getAttrs().isDir();
        } catch (Exception e) {
//            showLogs(e.toString());
            return false;
        }
    }

    public long fileLength(String filename) {
        try {
            LsEntry entry = find(filename);
            if (entry != null) {
                return entry.getAttrs().getSize();
            }
        } catch (Exception e) {
//            showLogs(e.toString());
        }
        return -1;
    }

    public long fileModifyTime(String filename) {
        try {
            LsEntry entry = find(filename);
            if (entry != null) {
                return entry.getAttrs().getMTime() * 1000l;
            }
        } catch (Exception e) {
//            showLogs(e.toString());
        }
        return -1;
    }

    public void deleteFile(String filename) {
        try {
            String name = fixFilename(filename);
            showLogs("rm " + fixFilename(name));
            sftp.rm(name);
        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    public void mkdirs(String filename) {
        try {
            String fixedName = fixFilename(filename);
            showLogs("mkdirs " + fixedName);
            String[] names = fixedName.split("/");
            String parent = null;
            for (String name : names) {
                if (name == null || name.isBlank()
                        || ".".equals(name) || "..".equals(name)) {
                    continue;
                }
                String path = (parent == null ? "" : parent + "/") + name;
                try {
                    sftp.mkdir(path);
                } catch (Exception e) {
                }
                parent = path;
            }
        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    public boolean copyFile(File sourceFile, String targetFile) {
        try {
            if (task == null || task.isCancelled()
                    || sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            String fixedName = fixFilename(targetFile);
            showLogs("put " + fixedName);
            sftp.put(sourceFile.getAbsolutePath(), fixedName);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e.toString() + " " + targetFile);
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

}
