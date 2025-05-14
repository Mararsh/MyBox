package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import java.io.File;
import java.util.Iterator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-27
 * @License Apache License Version 2.0
 */
public class RemotePathGetController extends RemotePathHandleFilesController {

    @FXML
    protected ControlPathInput targetPathInputController;
    @FXML
    protected CheckBox copyMtimeCheck;

    public RemotePathGetController() {
        baseTitle = message("RemotePathGet");
        doneString = message("Downloaded");
    }

    @Override
    public void setParameters(RemotePathManageController manageController) {
        try {
            super.setParameters(manageController);

            targetPathInputController.parent(this);

            copyMtimeCheck.setSelected(UserConfig.getBoolean(baseName + "CopyMtime", true));
            copyMtimeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CopyMtime", nv);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    @Override
    public boolean checkParameters() {
        try {
            if (!super.checkParameters()) {
                return false;
            }
            targetPath = targetPathInputController.pickFile();
            return targetPath != null && targetPath.exists();
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean handleFile(FxTask currentTask, String srcfile) {
        try {
            SftpATTRS attrs = manageController.remoteController.stat(srcfile);
            if (attrs == null) {
                return false;
            }
            if (attrs.isDir()) {
                return downDirectory(currentTask, srcfile, targetPath);
            } else {
                return downFile(currentTask, srcfile, attrs, targetPath);
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean downFile(FxTask currentTask, String srcfile, SftpATTRS attrs, File path) {
        try {
            File target = new File(path + File.separator + new File(srcfile).getName());
            if (manageController.remoteController.get(currentTask, srcfile, attrs,
                    target, copyMtimeCheck.isSelected())) {
                doneCount++;
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean downDirectory(FxTask currentTask, String srcfile, File path) {
        try {
            File target = new File(path + File.separator + new File(srcfile).getName());
            target.getParentFile().mkdirs();
            Iterator<LsEntry> iterator = manageController.remoteController.ls(srcfile);
            if (iterator == null) {
                return false;
            }
            boolean ok;
            while (iterator.hasNext()) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                LsEntry entry = iterator.next();
                String child = entry.getFilename();
                if (child == null || child.isBlank()
                        || ".".equals(child) || "..".equals(child)) {
                    continue;
                }
                child = srcfile + "/" + child;
                SftpATTRS attrs = entry.getAttrs();
                if (attrs.isDir()) {
                    ok = downDirectory(currentTask, child, target);
                } else {
                    ok = downFile(currentTask, child, attrs, target);
                }
                if (!ok && !errorContinueCheck.isSelected()) {
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

    /*
        static methods
     */
    public static RemotePathGetController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathGetController controller = (RemotePathGetController) WindowTools.branchStage(
                    manageController, Fxmls.RemotePathGetFxml);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
