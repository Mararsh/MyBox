package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import java.util.Iterator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-15
 * @License Apache License Version 2.0
 */
public class RemotePathPermissionController extends RemotePathHandleFilesController {

    protected int value;

    @FXML
    protected ToggleGroup opGroup;
    @FXML
    protected RadioButton chmodRadio, chownRadio, chgrpRadio;
    @FXML
    protected TextField valueInput;
    @FXML
    protected CheckBox dirCheck;

    public RemotePathPermissionController() {
        baseTitle = message("RemotePathPermission");
        doneString = message("Changed");
    }

    @Override
    public void setParameters(RemotePathManageController manageController) {
        try {
            super.setParameters(manageController);

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkOperation();
                }
            });
            checkOperation();

            dirCheck.setSelected(UserConfig.getBoolean(baseName + "HandleFilesInDirectories", true));
            dirCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "HandleFilesInDirectories", nv);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    public void checkOperation() {
        if (chownRadio.isSelected()) {
            valueInput.setText(UserConfig.getString(baseName + "ChownValue", ""));
        } else if (chgrpRadio.isSelected()) {
            valueInput.setText(UserConfig.getString(baseName + "ChgrpValue", ""));
        } else {
            valueInput.setText(UserConfig.getString(baseName + "ChmodValue", "755"));
        }
    }

    @Override
    public boolean checkParameters() {
        try {
            if (!super.checkParameters()) {
                return false;
            }
            if (chownRadio.isSelected()) {
                try {
                    value = Integer.parseInt(valueInput.getText());
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("Value"));
                    return false;
                }
                UserConfig.setString(baseName + "ChownValue", valueInput.getText());

            } else if (chgrpRadio.isSelected()) {
                try {
                    value = Integer.parseInt(valueInput.getText());
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("Value"));
                    return false;
                }
                UserConfig.setString(baseName + "ChgrpValue", valueInput.getText());

            } else {
                try {
                    value = Integer.parseInt(valueInput.getText(), 8);
                } catch (Exception e) {
                    popError(message("InvalidParameter") + ": " + message("Value"));
                    return false;
                }
                UserConfig.setString(baseName + "ChmodValue", valueInput.getText());
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean handleFile(FxTask currentTask, String srcfile) {
        try {
            changeFile(srcfile);
            if (!dirCheck.isSelected()) {
                return true;
            }
            SftpATTRS attrs = manageController.remoteController.stat(srcfile);
            if (attrs != null && attrs.isDir()) {
                return changeFilesInDirectory(currentTask, srcfile);
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean changeFile(String srcfile) {
        try {
            if (chownRadio.isSelected()) {
                showLogs("chown " + value + " " + srcfile);
                manageController.remoteController.sftp.chown(value, srcfile);

            } else if (chgrpRadio.isSelected()) {
                showLogs("chgrp " + value + " " + srcfile);
                manageController.remoteController.sftp.chgrp(value, srcfile);

            } else {
                showLogs("chmod " + value + " " + srcfile);
                manageController.remoteController.sftp.chmod(value, srcfile);

            }
            doneCount++;
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean changeFilesInDirectory(FxTask currentTask, String dir) {
        try {
            Iterator<ChannelSftp.LsEntry> iterator = manageController.remoteController.ls(dir);
            if (iterator == null) {
                return false;
            }
            boolean ok;
            while (iterator.hasNext()) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                ChannelSftp.LsEntry entry = iterator.next();
                String child = entry.getFilename();
                if (child == null || child.isBlank()
                        || ".".equals(child) || "..".equals(child)) {
                    continue;
                }
                child = dir + "/" + child;
                ok = changeFile(child);
                if (ok) {
                    if (entry.getAttrs().isDir()) {
                        ok = changeFilesInDirectory(currentTask, child);
                    }
                }
                if (!ok && !continueCheck.isSelected()) {
                    if (currentTask != null) {
                        currentTask.cancel();
                    }
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterTask(boolean ok) {
        super.afterTask(ok);
        if (manageController != null) {
            manageController.loadPath();
        }
    }

    /*
        static methods
     */
    public static RemotePathPermissionController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathPermissionController controller = (RemotePathPermissionController) WindowTools.branchStage(
                    manageController, Fxmls.RemotePathPermissionFxml);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
