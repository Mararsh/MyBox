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

            targetPathInputController.baseName(baseName).init();

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
            targetPath = targetPathInputController.file();
            return targetPath != null && targetPath.exists();
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean handleFile(String srcfile) {
        try {
            SftpATTRS attrs = manageController.remoteController.stat(srcfile);
            if (attrs == null) {
                return false;
            }
            if (attrs.isDir()) {
                return downDirectory(srcfile, targetPath);
            } else {
                return downFile(srcfile, attrs, targetPath);
            }
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean downFile(String srcfile, SftpATTRS attrs, File path) {
        try {
            File target = new File(path + File.separator + new File(srcfile).getName());
            if (manageController.remoteController.get(srcfile, attrs,
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

    public boolean downDirectory(String srcfile, File path) {
        try {
            File target = new File(path + File.separator + new File(srcfile).getName());
            target.getParentFile().mkdirs();
            Iterator<LsEntry> iterator = manageController.remoteController.ls(srcfile);
            if (iterator == null) {
                return false;
            }
            boolean ok;
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
                child = srcfile + "/" + child;
                SftpATTRS attrs = entry.getAttrs();
                if (attrs.isDir()) {
                    ok = downDirectory(child, target);
                } else {
                    ok = downFile(child, attrs, target);
                }
                if (!ok && !continueCheck.isSelected()) {
                    if (task != null) {
                        task.cancel();
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
    public void afterTask() {
        super.afterTask();
        if (openCheck.isSelected()) {
            openTarget();
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
            RemotePathGetController controller = (RemotePathGetController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathGetFxml, false);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
