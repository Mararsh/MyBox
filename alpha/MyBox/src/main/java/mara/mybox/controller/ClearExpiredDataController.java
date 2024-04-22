package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.db.table.TableImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.fxml.WindowTools.recordInfo;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-3-31
 * @License Apache License Version 2.0
 */
public class ClearExpiredDataController extends BaseTaskController {

    protected boolean exit;

    @FXML
    protected CheckBox tmpFilesCheck, imageClipboardCheck, imageEditHistoriesCheck,
            filesBackupsCheck, data2dCheck, autoCheck;

    public ClearExpiredDataController() {
        baseTitle = message("ClearExpiredData");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tmpFilesCheck.setSelected(UserConfig.getBoolean(baseName + "TempFiles", true));
            tmpFilesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "TempFiles", nv);
                }
            });

            imageClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "ImageClipboard", true));
            imageClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "ImageClipboard", nv);
                }
            });

            imageEditHistoriesCheck.setSelected(UserConfig.getBoolean(baseName + "ImageEditHistories", true));
            imageEditHistoriesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "ImageEditHistories", nv);
                }
            });

            filesBackupsCheck.setSelected(UserConfig.getBoolean(baseName + "FilesBackups", true));
            filesBackupsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "FilesBackups", nv);
                }
            });

            data2dCheck.setSelected(UserConfig.getBoolean(baseName + "Data2d", true));
            data2dCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Data2d", nv);
                }
            });

            autoCheck.setSelected(UserConfig.getBoolean("ClearExpiredDataBeforeExit", true));
            autoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setBoolean("ClearExpiredDataBeforeExit", autoCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(boolean exit) {
        this.exit = exit;
        if (exit) {
            AppVariables.handlingExit = true;
            startAction();
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            recordInfo(this, message("ClearExpiredData") + "...");

            if (tmpFilesCheck.isSelected()) {
                recordInfo(this, message("Clear") + ": " + AppVariables.MyBoxTempPath);
                FileDeleteTools.clearDir(currentTask, AppVariables.MyBoxTempPath);
            }
            if (currentTask != null && currentTask.isCancelled()) {
                return true;
            }
            try (Connection conn = DerbyBase.getConnection()) {
                if (imageClipboardCheck.isSelected()) {
                    new TableImageClipboard().clearInvalid(this, conn);
                    if (currentTask != null && currentTask.isCancelled()) {
                        return true;
                    }
                }
                if (imageEditHistoriesCheck.isSelected()) {
                    new TableImageEditHistory().clearInvalid(this, conn);
                    if (currentTask != null && currentTask.isCancelled()) {
                        return true;
                    }
                }
                if (filesBackupsCheck.isSelected()) {
                    new TableFileBackup().clearInvalid(this, conn);
                    if (currentTask != null && currentTask.isCancelled()) {
                        return true;
                    }
                }
                if (data2dCheck.isSelected()) {
                    new TableData2DDefinition().clearInvalid(this, conn, true);
                }
            } catch (Exception e) {
                showLogs(e.toString());
            }

        } catch (Exception e) {
            showLogs(e.toString());
        }
        return true;
    }

    @Override
    public void afterTask(boolean ok) {
        if (exit) {
            close();
            AppVariables.handlingExit = false;
            WindowTools.handleExit();
        } else {
            super.afterTask(ok);
        }
    }

    /*
        static methods
     */
    public static ClearExpiredDataController open(boolean exit) {
        try {
            ClearExpiredDataController controller
                    = (ClearExpiredDataController) WindowTools.openStage(Fxmls.ClearExpiredDataFxml);
            controller.setParameters(exit);
            controller.requestMouse();

            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
