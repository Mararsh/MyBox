package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.FileBackup;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.value.AppPaths.getBackupsPath;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class ControlFileBackup extends BaseController {

    protected FileBackupController fileBackupController;
    protected TableFileBackup tableFileBackup;
    protected File backupPath, backupFile;

    @FXML
    protected CheckBox backupCheck;
    @FXML
    protected Button backupButton;
    @FXML
    protected Label totalLabel;

    public ControlFileBackup() {
    }

    // call this to init
    public void setParameters(BaseController parent, String name) {
        try {
            this.parentController = parent;
            this.baseName = name;

            tableFileBackup = new TableFileBackup();

            backupCheck.setSelected(UserConfig.getBoolean(baseName + "Backup", false));

            backupCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "Backup", backupCheck.isSelected());

                }
            });

            backupButton.disableProperty().bind(backupCheck.selectedProperty().not());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadBackups(File file) {
        this.sourceFile = file;
        totalLabel.setText("");
        backupPath = null;
        if (fileBackupController != null) {
            fileBackupController.close();
        }
    }

    public void addBackup(SingletonTask task, File sourceFile) {
        this.sourceFile = sourceFile;
        addBackup(task);
    }

    public void addBackup(SingletonTask task) {
        if (sourceFile == null || !sourceFile.exists()) {
            return;
        }
        SingletonTask backTask = new SingletonTask<Void>(this) {
            private int total;

            @Override
            protected boolean handle() {
                try {
                    List<FileBackup> list = backup(this);
                    total = list != null ? list.size() : 0;
                    return backupFile != null && total > 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                totalLabel.setText(message("Saved") + ": " + backupFile + "\n"
                        + message("Total") + ": " + total);
            }

            @Override
            protected void whenFailed() {
                totalLabel.setText(message("Failed"));
            }

        };
        start(backTask, false);
    }

    public List<FileBackup> backup(SingletonTask task) {
        try (Connection conn = DerbyBase.getConnection()) {
            backupPath = tableFileBackup.path(conn, sourceFile);
            if (backupPath == null) {
                String fname = sourceFile.getName();
                String ext = FileNameTools.suffix(fname);
                backupPath = new File(getBackupsPath() + File.separator
                        + (ext == null || ext.isBlank() ? "x" : ext) + File.separator
                        + FileNameTools.prefix(fname) + new Date().getTime() + File.separator);
            }
            backupPath.mkdirs();
            backupFile = new File(backupPath,
                    FileNameTools.append(sourceFile.getName(), "-" + DateTools.nowFileString()));
            if (!FileCopyTools.copyFile(sourceFile, backupFile, false, false) || !backupFile.exists()) {
                return null;
            }
            FileBackup newBackup = new FileBackup(sourceFile, backupFile);
            return tableFileBackup.addBackups(conn, newBackup);
        } catch (Exception e) {
            task.setError(e.toString());
            return null;
        }
    }

    public boolean needBackup() {
        return backupCheck != null && backupCheck.isSelected();
    }

    @FXML
    public void showBackups() {
        fileBackupController = FileBackupController.load(this);
    }

}
