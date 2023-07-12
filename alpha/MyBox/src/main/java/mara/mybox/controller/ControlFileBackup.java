package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.FileBackup;
import static mara.mybox.db.data.FileBackup.Default_Max_Backups;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonBackgroundTask;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableFileSizeCell;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.AppPaths.getBackupsPath;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-26
 * @License Apache License Version 2.0
 */
public class ControlFileBackup extends BaseTablePagesController<FileBackup> {

    protected TableFileBackup tableFileBackup;
    protected int maxBackups;
    protected File backupPath;

    @FXML
    protected TableColumn<FileBackup, File> backupColumn;
    @FXML
    protected TableColumn<FileBackup, Long> sizeColumn;
    @FXML
    protected TableColumn<FileBackup, Date> timeColumn;
    @FXML
    protected CheckBox backupCheck;
    @FXML
    protected VBox backupsListBox;
    @FXML
    protected TextField maxBackupsInput;
    @FXML
    protected Button okMaxButton, clearBackupsButton, deleteBackupButton, viewBackupButton, useBackupButton;

    public ControlFileBackup() {
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            sizeColumn.setCellFactory(new TableFileSizeCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("recordTime"));
            timeColumn.setCellFactory(new TableDateCell());

            backupColumn.setCellValueFactory(new PropertyValueFactory<>("backup"));
            backupColumn.setCellFactory(new Callback<TableColumn<FileBackup, File>, TableCell<FileBackup, File>>() {
                @Override
                public TableCell<FileBackup, File> call(TableColumn<FileBackup, File> param) {

                    TableCell<FileBackup, File> cell = new TableCell<FileBackup, File>() {
                        private final ImageView view;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            view = new ImageView();
                            view.setPreserveRatio(true);
                        }

                        @Override
                        public void updateItem(File item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            setText(item.getName());
                            if (parentController instanceof ImageManufactureController) {
                                int width = AppVariables.thumbnailWidth;
                                BufferedImage bufferedImage = ImageFileReaders.readImage(item, width);
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // call this to init
    public void setParameters(BaseController parent, String baseName) {
        try {
            this.parentController = parent;
            this.baseName = baseName;

            tableFileBackup = new TableFileBackup();
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        if (deleteBackupButton != null) {
            deleteBackupButton.setDisable(none);
        }
        if (viewBackupButton != null) {
            viewBackupButton.setDisable(none);
        }
        if (useBackupButton != null) {
            useBackupButton.setDisable(none);
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
            tableData.clear();
        }
        thisPane.applyCss();
        UserConfig.setBoolean(baseName + "Backup", backupCheck.isSelected());
    }

    public void loadBackups(File file) {
        this.sourceFile = file;
        loadBackups();
    }

    public void loadBackups() {
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        if (sourceFile == null || !backupCheck.isSelected()) {
            tableData.clear();
            return;
        }
        backgroundTask = new SingletonBackgroundTask<Void>(this) {
            private List<FileBackup> list;
            private File currentFile;

            @Override
            protected boolean handle() {
                try {
                    currentFile = sourceFile;
                    list = tableFileBackup.read(currentFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (currentFile.equals(sourceFile)) {
                    if (list != null && !list.isEmpty()) {
                        tableData.setAll(list);
                    } else {
                        tableData.clear();
                    }
                }
            }

        };
        start(backgroundTask, thisPane);
    }

    public List<FileBackup> addBackup(SingletonTask task, File sourceFile) {
        this.sourceFile = sourceFile;
        return addBackup(task);
    }

    public List<FileBackup> addBackup(SingletonTask task) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
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
            File backupFile = new File(backupPath,
                    FileNameTools.append(sourceFile.getName(), "-" + DateTools.nowFileString()));
            if (!FileCopyTools.copyFile(sourceFile, backupFile, false, false) || !backupFile.exists()) {
                return null;
            }
            FileBackup newBackup = new FileBackup(sourceFile, backupFile);
            List<FileBackup> savedBackup = tableFileBackup.addBackups(conn, newBackup);
            if (savedBackup != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        tableData.setAll(savedBackup);
                        tableView.refresh();
                    }
                });
            }
            return savedBackup;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    @FXML
    public void refreshBackups() {
        loadBackups();
    }

    @FXML
    public void clearBackups() {
        if (sourceFile == null || !PopTools.askSure(getTitle(), message("SureClear"))) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    tableFileBackup.clearBackups(task, sourceFile.getAbsolutePath());
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                tableData.clear();
            }

        };
        start(task);
    }

    @FXML
    public void deleteBackups() {
        List<FileBackup> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        List<FileBackup> targets = new ArrayList<>();
        targets.addAll(selected);
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    for (FileBackup b : targets) {
                        TableFileBackup.deleteBackup(b);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                tableData.removeAll(targets);
            }
        };
        start(task);
    }

    @FXML
    public void viewBackup() {
        FileBackup selected = selectedItem();
        if (selected == null) {
            return;
        }
        ControllerTools.openTarget(selected.getBackup().getAbsolutePath(), true);
    }

    @FXML
    public void useBackup() {
        if (sourceFile == null) {
            return;
        }
        FileBackup selected = selectedItem();
        if (selected == null) {
            return;
        }
        File backup = selected.getBackup();
        if (backup == null || !backup.exists()) {
            tableData.remove(selected);
            return;
        }
        if (!PopTools.askSure(getTitle(), message("SureOverrideCurrentFile"),
                message("CurrentFile") + ":\n   " + sourceFile + "\n" + FileTools.showFileSize(sourceFile.length())
                + "\n\n" + message("OverrideBy") + ":\n   " + backup + "\n" + FileTools.showFileSize(backup.length()))) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    File tmpFile = FileTmpTools.getTempFile();
                    FileCopyTools.copyFile(backup, tmpFile, true, true);  // backup may be cleared due to max
                    addBackup(task, sourceFile);
                    FileCopyTools.copyFile(tmpFile, sourceFile, true, true);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                parentController.sourceFileChanged(sourceFile);
            }
        };
        start(task);
    }

    public FileBackup selectedBackup() {
        return selectedItem();
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
        File path;
        if (sourceFile == null || backupPath == null) {
            path = new File(AppPaths.getBackupsPath());
        } else {
            path = backupPath;
        }
        browseURI(path.toURI());
    }

    public boolean needBackup() {
        return backupCheck != null && backupCheck.isSelected();
    }

}
