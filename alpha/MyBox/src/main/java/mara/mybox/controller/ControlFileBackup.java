package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mara.mybox.db.data.FileBackup;
import mara.mybox.db.table.TableFileBackup;
import static mara.mybox.db.table.TableFileBackup.Default_Max_Backups;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class ControlFileBackup extends BaseController {

    protected TableFileBackup tableFileBackup;
    protected int maxBackups;

    @FXML
    protected CheckBox backupCheck;
    @FXML
    protected VBox backupsListBox;
    @FXML
    protected ListView<FileBackup> backupsList;
    @FXML
    protected TextField maxBackupsInput;
    @FXML
    protected Button okMaxButton, clearBackupsButton, deleteBackupButton, viewBackupButton, useBackupButton;

    public ControlFileBackup() {
    }

    // call this to init
    public void setControls(BaseController parent, String baseName) {
        try {
            this.parentController = parent;
            this.baseName = baseName;

            tableFileBackup = new TableFileBackup();

            backupCheck.setSelected(UserConfig.getBoolean(baseName + "Backup", true));
            checkStatus();
            backupCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    checkStatus();
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
                        int v = Integer.valueOf(maxBackupsInput.getText());
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

            backupsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            backupsList.setCellFactory(new Callback<ListView<FileBackup>, ListCell<FileBackup>>() {
                @Override
                public ListCell<FileBackup> call(ListView<FileBackup> param) {
                    ListCell<FileBackup> cell = new ListCell<FileBackup>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        protected void updateItem(FileBackup item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(null);
                            if (empty || item == null || item.getBackup() == null) {
                                setText(null);
                                return;
                            }
                            setText(DateTools.datetimeToString(item.getRecordTime()) + "  "
                                    + FileTools.showFileSize(item.getBackup().length()));
                            if (parentController instanceof ImageManufactureController) {
                                int width = AppVariables.thumbnailWidth;
                                BufferedImage bufferedImage = ImageFileReaders.readImage(item.getBackup(), width);
                                if (bufferedImage != null) {
                                    view.setFitWidth(width);
                                    view.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                                    setGraphic(view);
                                }
                            }
                        }
                    };
                    return cell;
                }
            });

            backupsList.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    useBackup();
                }
            });

            deleteBackupButton.disableProperty().bind(backupsList.getSelectionModel().selectedItemProperty().isNull());
            viewBackupButton.disableProperty().bind(deleteBackupButton.disableProperty());
            useBackupButton.disableProperty().bind(deleteBackupButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkStatus() {
        if (backupCheck.isSelected()) {
            if (!thisPane.getChildren().contains(backupsListBox)) {
                thisPane.getChildren().add(backupsListBox);
            }
            loadBackups();
        } else {
            if (thisPane.getChildren().contains(backupsListBox)) {
                thisPane.getChildren().remove(backupsListBox);
            }
            backupsList.getItems().clear();
        }
        thisPane.applyCss();
        UserConfig.setBoolean(baseName + "Backup", backupCheck.isSelected());
    }

    public void loadBackups(File file) {
        this.sourceFile = file;
        loadBackups();
    }

    public void loadBackups() {
        backupsList.getItems().clear();
        if (sourceFile == null || !backupCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private List<FileBackup> list;
                private File currentFile;

                @Override
                protected boolean handle() {
                    try {
                        currentFile = sourceFile;
                        String key = currentFile.getAbsolutePath();
                        list = tableFileBackup.read(key);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (list != null && currentFile.equals(sourceFile)) {
                        backupsList.getItems().addAll(list);
                    }
                }

            };
            start(task, false, null);
        }

    }

    public FileBackup addBackup(File sourceFile) {
        this.sourceFile = sourceFile;
        return addBackup();
    }

    public FileBackup addBackup() {
        if (sourceFile == null) {
            return null;
        }
        File backupFile = newBackupFile();
        if (backupFile == null) {
            return null;
        }
        backupFile.getParentFile().mkdirs();
        FileCopyTools.copyFile(sourceFile, backupFile, false, false);
        FileBackup newBackup = tableFileBackup.insertData(new FileBackup(sourceFile, backupFile));
        if (newBackup != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    backupsList.getItems().add(0, newBackup);
                    backupsList.refresh();
                }
            });
        }
        return newBackup;
    }

    public File newBackupFile() {
        if (sourceFile == null) {
            return null;
        }
        String path = AppPaths.getFileBackupsPath(sourceFile);
        if (path == null) {
            return null;
        }
        File backupFile = new File(path + FileNameTools.appendName(sourceFile.getName(), "-" + DateTools.nowFileString()));
        return backupFile;
    }

    @FXML
    public void refreshBackups() {
        loadBackups();
    }

    @FXML
    public void clearBackups() {
        if (!PopTools.askSure(getBaseTitle(), Languages.message("SureClear"))) {
            return;
        }
        backupsList.getItems().clear();
        if (sourceFile == null) {
            return;
        }
        tableFileBackup.clearBackups(sourceFile.getAbsolutePath());
    }

    @FXML
    public void deleteBackups() {
        FileBackup selected = backupsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        TableFileBackup.deleteBackup(selected);
        backupsList.getItems().remove(selected);
    }

    @FXML
    public void viewBackup() {
        FileBackup selected = backupsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        ControllerTools.openTarget(null, selected.getBackup().getAbsolutePath(), true);
    }

    @FXML
    public void useBackup() {
        if (sourceFile == null) {
            return;
        }
        FileBackup selected = backupsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        File backup = selected.getBackup();
        if (backup == null || !backup.exists()) {
            backupsList.getItems().remove(selected);
            return;
        }
        if (!PopTools.askSure(getBaseTitle(), Languages.message("SureOverrideCurrentFile"),
                Languages.message("CurrentFile") + ":\n   " + sourceFile + "\n" + FileTools.showFileSize(sourceFile.length())
                + "\n\n" + Languages.message("OverrideBy") + ":\n   " + backup + "\n" + FileTools.showFileSize(backup.length()))) {
            return;
        }
        addBackup();
        FileCopyTools.copyFile(backup, sourceFile, true, true);
        parentController.sourceFileChanged(sourceFile);
    }

    public FileBackup selectedBackup() {
        return backupsList.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void okMax() {
        try {
            UserConfig.setInt("MaxFileBackups", maxBackups);
            popSuccessful();
            loadBackups();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void openPath() {
        if (sourceFile == null) {
            return;
        }
        File path = new File(AppPaths.getFileBackupsPath(sourceFile));
        browseURI(path.toURI());
    }

    public boolean isBack() {
        return backupCheck != null && backupCheck.isSelected();
    }

}
