package mara.mybox.controller;

import com.jcraft.jsch.ChannelSftp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-28
 * @License Apache License Version 2.0
 */
public abstract class RemotePathHandleFilesController extends BaseTaskController {

    protected RemotePathManageController manageController;
    protected ChannelSftp sftp;
    protected List<String> names;
    protected int doneCount;
    protected String doneString;

    @FXML
    protected TextArea namesArea;
    @FXML
    protected Label hostLabel;
    @FXML
    protected CheckBox wrapCheck;

    public void setParameters(RemotePathManageController manageController) {
        try {
            this.manageController = manageController;
            logsTextArea = manageController.logsTextArea;
            logsMaxChars = manageController.logsMaxChars;
            verboseCheck = manageController.verboseCheck;

            List<TreeItem<FileNode>> items = manageController.filesTreeView.getSelectionModel().getSelectedItems();
            if (items != null) {
                for (TreeItem<FileNode> item : items) {
                    namesArea.appendText(item.getValue().nodeFullName() + "\n");
                }
            }

            hostLabel.setText(message("Host") + ": " + manageController.remoteController.host());

            wrapCheck.setSelected(UserConfig.getBoolean("RemotePathFilesWrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean("RemotePathFilesWrap", nv);
                    namesArea.setWrapText(nv);
                }
            });
            namesArea.setWrapText(wrapCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    public boolean checkParameters() {
        try {
            String texts = namesArea.getText();
            if (texts == null || texts.isBlank()) {
                popError(message("InvalidParameters") + ": " + message("Files"));
                return false;
            }
            names = new ArrayList<>();
            names.addAll(Arrays.asList(texts.split("\n")));
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!checkParameters()) {
                return false;
            }
            if (manageController.task != null) {
                manageController.task.cancel();
            }
            manageController.tabPane.getSelectionModel().select(manageController.logsTab);
            manageController.initLogs();
            manageController.requestMouse();
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            manageController.task = currentTask;
            manageController.remoteController.task = currentTask;
            if (!manageController.checkConnection()) {
                return false;
            }
            sftp = manageController.remoteController.sftp;
            doneCount = 0;
            for (String name : names) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                showLogs(message("SourceFile") + ": " + name);
                if (handleFile(currentTask, name)) {
                    showLogs(doneString + ": " + name);
                } else {
                    showLogs(message("Failed") + ": " + name);
                    if (!errorContinueCheck.isSelected()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            showLogs(e.toString());
            return false;
        }
    }

    public boolean handleFile(FxTask currentTask, String name) {
        return false;
    }

    @Override
    protected void taskCanceled() {
        super.taskCanceled();
        if (manageController != null) {
            manageController.disconnect();
        }
    }

    @Override
    public void afterTask(boolean ok) {
        showLogs(doneString + ": " + doneCount);
        if (miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
    }

    /*
        static methods
     */
    public static RemotePathHandleFilesController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathHandleFilesController controller = (RemotePathHandleFilesController) WindowTools.branchStage(
                    manageController, Fxmls.RemotePathDeleteFxml);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
