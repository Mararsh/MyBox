package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.PathConnection;
import mara.mybox.db.table.TablePathConnection;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class DirectorySynchronizeSftpController extends DirectorySynchronizeController {

    protected ChannelSftp sftp;
    protected TablePathConnection tablePathConnection;
    protected ObservableList<PathConnection> tableData;
    protected PathConnection currentConnection;

    @FXML
    protected TableView<PathConnection> tableView;
    @FXML
    protected TableColumn<PathConnection, String> titleColumn, hostColumn, pathColumn;
    @FXML
    protected TextField titleInput, hostInput, userInput, pathInput, timeoutInput, retryInput, timeInput;
    @FXML
    protected PasswordField passwordInput;
    @FXML
    protected CheckBox hostKeyCheck;

    public DirectorySynchronizeSftpController() {
        baseTitle = message("DirectorySynchronizeSFTP");
    }

    @Override
    public void initTarget() {
        try {
            tablePathConnection = new TablePathConnection();

            tableData = FXCollections.observableArrayList();
            tableView.setItems(tableData);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PathConnection>() {
                @Override
                public void changed(ObservableValue<? extends PathConnection> v, PathConnection oldV, PathConnection newV) {
                    if (isSettingValues) {
                        return;
                    }
                    editProfile(newV);
                }
            });

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
            pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourcePathInput.textProperty())
                            .or(sourcePathInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(hostInput.textProperty().isEmpty())
                            .or(pathInput.textProperty().isEmpty())
            );
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        loadProfiles();
        editProfile(null);
    }

    /*
        profiles
     */
    public void loadProfiles() {
        if (task != null) {
            task.cancel();
        }
        tableData.clear();
        task = new SingletonTask<Void>(this) {
            private List<PathConnection> profiles;

            @Override
            protected boolean handle() {
                profiles = tablePathConnection.read(PathConnection.Type.SFTP, -1);
                return profiles != null;
            }

            @Override
            protected void whenSucceeded() {
                isSettingValues = true;
                tableData.setAll(profiles);
                isSettingValues = false;
            }

        };
        start(task);
    }

    public void editProfile(PathConnection profile) {
        currentConnection = profile;
        if (currentConnection == null) {
            currentConnection = new PathConnection();
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
    public void addProfile() {
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
        String host = hostInput.getText().trim();
        if (host.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("Host"));
            return false;
        }
        String remotePath = pathInput.getText().trim();
        if (remotePath.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("RemotePath"));
            return false;
        }
        currentConnection.setTimeout(timeout);
        currentConnection.setRetry(retry);
        currentConnection.setTitle(titleInput.getText().trim());
        currentConnection.setHost(host);
        currentConnection.setUsername(userInput.getText());
        currentConnection.setPassword(passwordInput.getText());
        currentConnection.setPath(remotePath);
        currentConnection.setType(PathConnection.Type.SFTP);
        currentConnection.setPort(22);
        currentConnection.setHostKeyCheck(hostKeyCheck.isSelected());
        currentConnection.setModifyTime(new Date());
        return true;
    }

    @FXML
    public void saveProfile() {
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
                loadProfiles();
            }

        };
        start(task);
    }

    @FXML
    public void copyProfile() {
        if (currentConnection == null) {
            currentConnection = new PathConnection();
        }
        currentConnection.setPcnid(-1);
        String title = currentConnection.getTitle();
        currentConnection.setTitle(title == null ? message("Copy") : title + " " + message("Copy"));
        editProfile(currentConnection);
    }

    @FXML
    public void deleteProfiles() {
        List<PathConnection> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tablePathConnection.deleteData(selected) >= 0;
            }

            @Override
            protected void whenSucceeded() {
                loadProfiles();
                editProfile(null);
            }

        };
        start(task);
    }

    @FXML
    public void clearProfiles() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tablePathConnection.clear(PathConnection.Type.SFTP) >= 0;
            }

            @Override
            protected void whenSucceeded() {
                loadProfiles();
                editProfile(null);
            }

        };
        start(task);
    }

    @FXML
    public void refreshProfiles() {
        loadProfiles();
    }

    /*
        sftp
     */
    @Override
    protected boolean checkTarget() {
        return pickProfile();
    }

    @Override
    public boolean doTask() {
        try {
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
            return ok;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    public boolean sftp() {
        try {
            JSch jsch = new JSch();
            Session sshSession = jsch.getSession(currentConnection.getUsername(), currentConnection.getHost(), 22);
            showLogs("Session created.");
            sshSession.setPassword(currentConnection.getPassword());
            sshSession.setTimeout(currentConnection.getTimeout());

            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", hostKeyCheck.isSelected() ? "yes" : "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            updateLogs("SSH session connected: " + currentConnection.getHost(), true, true);
            updateLogs("Opening Channel...", true, true);
            sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();
            updateLogs("SFTP 登录成功", true, true);

            Iterator<LsEntry> iterator = sftp.ls(currentConnection.getPath()).iterator();
            List<String> names = new ArrayList<>();
            while (iterator.hasNext()) {
                String name = iterator.next().getFilename();
                names.add(name);
            }
            updateLogs(names.toString(), true, true);

            sftp.disconnect();
            sftp.exit();
            sshSession.disconnect();
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
