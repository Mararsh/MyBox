package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.FileBackup;
import static mara.mybox.db.data.FileBackup.Default_Max_Backups;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.FxBackgroundTask;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableFileNameCell;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class FileBackupController extends BaseTableViewController<FileBackup> {

    protected TableFileBackup tableFileBackup;
    protected int maxBackups;

    @FXML
    protected TableColumn<FileBackup, String> backupColumn;
    @FXML
    protected TableColumn<FileBackup, Long> sizeColumn;
    @FXML
    protected TableColumn<FileBackup, Date> timeColumn;
    @FXML
    protected VBox backupsListBox;
    @FXML
    protected TextField maxBackupsInput;
    @FXML
    protected Label fileLabel;
    @FXML
    protected Button okMaxButton, useButton;
    @FXML
    protected CheckBox backupCheck;

    public FileBackupController() {
        baseTitle = message("FileBackups");
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            sizeColumn.setCellFactory(new TableFileSizeCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("recordTime"));
            timeColumn.setCellFactory(new TableDateCell());

            backupColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(viewButton, new Tooltip(message("View") + "\nCTRL+P / ALT+P"));
            NodeStyleTools.setTooltip(useButton, new Tooltip(message("Use") + "\n" + message("DoubleClick")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BaseController parent) {
        try {
            if (parent == null || parent.sourceFile == null) {
                close();
                return;
            }
            parentController = parent;
            baseName = parent.baseName;
            sourceFile = parentController.sourceFile;
            tableFileBackup = new TableFileBackup();
            if (parentController instanceof BaseImageController) {
                backupColumn.setCellFactory(new TableFileNameCell());
            }

            fileLabel.setText(sourceFile.getAbsolutePath());
            setTitle(baseTitle + " - " + sourceFile.getAbsolutePath());

            backupCheck.setSelected(UserConfig.getBoolean(baseName + "BackupWhenSave", true));
            backupCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "BackupWhenSave", backupCheck.isSelected());
                }
            });

            maxBackups = UserConfig.getInt("MaxFileBackups", Default_Max_Backups);
            if (maxBackups <= 0) {
                maxBackups = Default_Max_Backups;
                UserConfig.setInt("MaxFileBackups", Default_Max_Backups);
            }
            maxBackupsInput.setText(maxBackups + "");
            maxBackupsInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(maxBackupsInput.getText());
                        if (v >= 0) {
                            maxBackups = v;
                            UserConfig.setInt("MaxFileBackups", v);
                            maxBackupsInput.setStyle(null);
                            okMaxButton.setDisable(false);
                        } else {
                            maxBackupsInput.setStyle(UserConfig.badStyle());
                            okMaxButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        maxBackupsInput.setStyle(UserConfig.badStyle());
                        okMaxButton.setDisable(true);
                    }
                }
            });

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected boolean validFile() {
        return sourceFile != null
                && parentController != null
                && sourceFile.equals(parentController.sourceFile)
                && parentController != null
                && parentController.isShowing();
    }

    @Override
    public void itemDoubleClicked() {
        useBackup();
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean none = isNoneSelected();
        boolean validFile = validFile();
        viewButton.setDisable(none);
        useButton.setDisable(none || !validFile);
    }

    @FXML
    @Override
    public void refreshAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxBackgroundTask<Void>(this) {
            private List<FileBackup> list;

            @Override
            protected boolean handle() {
                try {
                    list = tableFileBackup.read(sourceFile);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (list != null && !list.isEmpty()) {
                    tableData.setAll(list);
                } else {
                    tableData.clear();
                }
                bottomLabel.setText(message("Total") + ": " + tableData.size());
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void deleteAction() {
        List<FileBackup> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            clearAction();
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private int deletedCount = 0;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    for (FileBackup item : selected) {
                        deletedCount += tableFileBackup.deleteData(conn, item);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Deleted") + ":" + deletedCount);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (deletedCount > 0) {
                    refreshAction();
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void clearAction() {
        if (!PopTools.askSure(getTitle(), message("SureClearData"))) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private long deletedCount = 0;

            @Override
            protected boolean handle() {
                deletedCount = tableFileBackup.clearBackups(task, sourceFile.getAbsolutePath());
                return true;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (deletedCount > 0) {
                    popInformation(message("Deleted") + ":" + deletedCount);
                    refreshAction();
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void viewAction() {
        FileBackup selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        ControllerTools.popTarget(this, selected.getBackup().getAbsolutePath(), true);
    }

    @FXML
    public void useBackup() {
        if (!validFile()) {
            popError(message("InvalidData"));
            return;
        }
        FileBackup selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        File backup = selected.getBackup();
        if (backup == null || !backup.exists()) {
            popError(message("InvalidData"));
            refreshAction();
            return;
        }
        if (!PopTools.askSure(getTitle(), message("SureOverrideCurrentFile"),
                message("CurrentFile") + ":\n   " + sourceFile + "\n" + FileTools.showFileSize(sourceFile.length())
                + "\n\n" + message("OverrideBy") + ":\n   " + backup + "\n" + FileTools.showFileSize(backup.length()))) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    tableFileBackup.addBackup(sourceFile);
                    if (!validFile()) {
                        return false;
                    }
                    return FileCopyTools.copyFile(backup, sourceFile, true, true);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (validFile()) {
                    parentController.sourceFileChanged(sourceFile);
                }
                refreshAction();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public boolean popAction() {
        viewAction();
        return true;
    }

    @FXML
    public void okMax() {
        try {
            UserConfig.setInt("MaxFileBackups", maxBackups);
            popSuccessful();
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void openPath() {
        FxTask pathtask = new FxSingletonTask<Void>(this) {
            File path;

            @Override
            protected boolean handle() {
                path = tableFileBackup.path(sourceFile);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (path == null) {
                    path = new File(AppPaths.getBackupsPath());
                }
                browseURI(path.toURI());
            }

        };
        start(pathtask, false);
    }

    /*
        static
     */
    public static FileBackupController load(BaseController parent) {
        try {
            FileBackupController controller = (FileBackupController) WindowTools.branchStage(
                    parent, Fxmls.FileBackupFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void updateList(File file) {
        try {
            if (file == null || !file.isFile() || !file.exists()) {
                return;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!window.isShowing()) {
                    continue;
                }
                Object object = window.getUserData();
                if (object == null || !(object instanceof FileBackupController)) {
                    continue;
                }
                try {
                    FileBackupController controller = (FileBackupController) object;
                    if (!file.equals(controller.sourceFile)) {
                        continue;
                    }
                    controller.refreshAction();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
