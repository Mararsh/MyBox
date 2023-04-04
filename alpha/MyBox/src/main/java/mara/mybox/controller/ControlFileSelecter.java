package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlFileSelecter extends BaseController {

    protected File file, defaultFile;
    protected String savedName;
    protected boolean isSource, isDirectory, checkQuit, permitNull, mustExist;
    protected SimpleBooleanProperty notify, valid;

    @FXML
    protected Label label;
    @FXML
    protected TextField fileInput;
    @FXML
    protected Button selectPathButton;

    public ControlFileSelecter() {
        isSource = true;
        isDirectory = false;
        checkQuit = false;
        permitNull = false;
        mustExist = false;
        notify = new SimpleBooleanProperty(false);
        valid = new SimpleBooleanProperty(false);
    }

    public static ControlFileSelecter create() {
        return new ControlFileSelecter();
    }

    @Override
    public void initControls() {
        super.initControls();
        if (fileInput != null) {
            fileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkFileInput();
                }
            });
        }
        label.setMinWidth(Region.USE_PREF_SIZE);
    }

    public ControlFileSelecter init() {
        if (savedName != null) {
            String saved = UserConfig.getString(savedName, defaultFile != null ? defaultFile.getAbsolutePath() : null);
            if (saved != null) {
                if (fileInput != null) {
                    fileInput.setText(saved);
                } else {
                    setFile(new File(saved));
                }
            }
        } else {
            if (defaultFile != null) {
                if (fileInput != null) {
                    fileInput.setText(defaultFile.getAbsolutePath());
                } else {
                    setFile(defaultFile);
                }
            }
        }
        return this;
    }

    public ControlFileSelecter label(String labelString) {
        label.setText(labelString);
//        refreshStyle(thisPane);
        return this;
    }

    public ControlFileSelecter savedName(String savedName) {
        this.savedName = savedName;
        return this;
    }

    public ControlFileSelecter baseName(String baseName) {
        this.baseName = baseName;
        return this;
    }

    public ControlFileSelecter defaultFile(File defaultFile) {
        this.defaultFile = defaultFile;
        return this;
    }

    public ControlFileSelecter isSource(boolean isSource) {
        this.isSource = isSource;
        mustExist = isSource;
        return this;
    }

    public ControlFileSelecter isDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
        return this;
    }

    public ControlFileSelecter checkQuit(boolean checkQuit) {
        this.checkQuit = checkQuit;
        return this;
    }

    public ControlFileSelecter permitNull(boolean permitNull) {
        this.permitNull = permitNull;
        return this;
    }

    public ControlFileSelecter mustExist(boolean mustExist) {
        this.mustExist = mustExist;
        return this;
    }

    protected ControlFileSelecter type(int fileType) {
        setFileType(fileType);
        return this;
    }

    protected void checkFileInput() {
        if (isSettingValues || fileInput == null) {
            return;
        }
        String v = fileInput.getText();
        if (v == null || v.isEmpty()) {
            if (permitNull) {
                fileInput.setStyle(null);
                valid.set(true);
            } else {
                fileInput.setStyle(UserConfig.badStyle());
                valid.set(false);
            }
            file = null;
            notify.set(!notify.get());
            return;
        }
        File inputfile = new File(v);
        if (mustExist && (!inputfile.exists()
                || isDirectory && !inputfile.isDirectory()
                || !isDirectory && !inputfile.isFile())) {
            if (fileInput != null) {
                fileInput.setStyle(UserConfig.badStyle());
            }
            valid.set(false);
            file = null;
            notify.set(!notify.get());
            return;
        }
        fileInput.setStyle(null);
        setFile(inputfile);
    }

    protected void setFile(File file) {
        if (file == null) {
            return;
        }
        this.file = file;
        if (savedName != null) {
            UserConfig.setString(savedName, file.getAbsolutePath());
        }
        if (isSource) {
            recordFileOpened(file);
        } else {
            recordFileWritten(file);
        }
        valid.set(true);
        notify.set(!notify.get());
    }

    @FXML
    public void selectFile() {
        try {
            File selectedfile;
            File path = UserConfig.getPath(isSource ? baseName + "SourcePath" : baseName + "TargetPath");
            if (path == null) {
                path = defaultFile;
            }
            if (isDirectory) {
                DirectoryChooser chooser = new DirectoryChooser();
                if (path != null && path.exists()) {
                    chooser.setInitialDirectory(path);
                }
                selectedfile = chooser.showDialog(getMyStage());

            } else {
                if (isSource) {
                    selectedfile = FxFileTools.selectFile(this, path, sourceExtensionFilter);
                } else {
                    selectedfile = chooseSaveFile(path, defaultFile != null ? defaultFile.getName() : null,
                            targetExtensionFilter);
                }
            }
            if (selectedfile == null || (mustExist && !selectedfile.exists())) {
                return;
            }
            if (fileInput != null) {
                fileInput.setText(selectedfile.getAbsolutePath());
            } else {
                setFile(selectedfile);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void showRecentFilesMenu(Event event) {
        if (checkQuit && !checkBeforeNextAction()) {
            return;
        }
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {

            @Override
            public List<VisitHistory> recentFiles() {
                if (isDirectory) {
                    return null;
                } else {
                    int fileNumber = AppVariables.fileRecentNumber * 3 / 4;
                    return VisitHistoryTools.getRecentFileRead(getSourceFileType(), fileNumber);
                }
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber;
                if (isDirectory) {
                    pathNumber = AppVariables.fileRecentNumber;
                } else {
                    pathNumber = AppVariables.fileRecentNumber / 4 + 1;
                }
                if (isSource) {
                    return VisitHistoryTools.getRecentPathRead(SourcePathType, pathNumber);
                } else {
                    return VisitHistoryTools.getRecentPathWrite(TargetPathType, pathNumber);
                }
            }

            @Override
            public void handleSelect() {
                selectFile();
            }

            @Override
            public void handleFile(String fname) {
                File selectedfile = new File(fname);
                if (mustExist && !selectedfile.exists()) {
                    selectFile();
                    return;
                }
                if (fileInput != null) {
                    fileInput.setText(selectedfile.getAbsolutePath());
                } else {
                    setFile(selectedfile);
                }
            }

            @Override
            public void handlePath(String fname) {
                File selectedfile = new File(fname);
                if (mustExist && !selectedfile.exists()) {
                    selectFile();
                    return;
                }
                if (isDirectory) {
                    if (fileInput != null) {
                        fileInput.setText(selectedfile.getAbsolutePath());
                    } else {
                        setFile(selectedfile);
                    }
                } else {
                    UserConfig.setString(isSource ? baseName + "SourcePath" : baseName + "TargetPath", fname);
                    selectFile();
                }
            }

        }.pop();
    }

    @FXML
    public void pickRecentFiles(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectFile();
        } else {
            showRecentFilesMenu(event);
        }
    }

    @FXML
    public void popRecentFiles(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showRecentFilesMenu(event);
        }
    }

    public void input(String string) {
        if (fileInput == null) {
            setFile(new File(string));
        } else {
            fileInput.setText(string);
        }
    }

    public String text() {
        if (fileInput == null) {
            return null;
        } else {
            return fileInput.getText();
        }
    }

    public File file() {
        return file;
    }

    @Override
    public void cleanPane() {
        try {
            notify = null;
            valid = null;
            file = null;
            defaultFile = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
