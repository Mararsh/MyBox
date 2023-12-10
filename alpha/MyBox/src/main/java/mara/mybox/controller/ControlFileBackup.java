package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
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
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class ControlFileBackup extends BaseController {
    
    protected TableFileBackup tableFileBackup;
    
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
                    if (!backupCheck.isSelected()) {
                        totalLabel.setText("");
                    }
                    
                }
            });
            
            backupButton.disableProperty().bind(backupCheck.selectedProperty().not());
            
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }
    
    public void loadBackups(File file) {
        sourceFile = file;
        totalLabel.setText("");
    }
    
    public void addBackup(FxTask task, File file) {
        sourceFile = file;
        FxTask backTask = new FxTask<Void>(this) {
            private int total;
            private FileBackup backup;
            
            @Override
            protected boolean handle() {
                backup = null;
                total = 0;
                if (sourceFile == null || !sourceFile.exists()) {
                    return false;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    backup = tableFileBackup.addBackup(conn, sourceFile);
                    if (backup != null) {
                        total = tableFileBackup.count(conn, sourceFile);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }
            
            @Override
            protected void whenSucceeded() {
                if (backup != null) {
                    totalLabel.setText(message("Saved") + ": " + backup.getBackup() + "\n"
                            + message("Total") + ": " + total);
                    FileBackupController.updateList(sourceFile);
                }
            }
            
            @Override
            protected void whenFailed() {
                totalLabel.setText(error != null ? error : message("FailBackup"));
            }
            
        };
        start(backTask, false);
    }
    
    public boolean needBackup() {
        return backupCheck != null && backupCheck.isSelected();
    }
    
    @FXML
    public void showBackups() {
        FileBackupController.load(this);
    }
    
}
