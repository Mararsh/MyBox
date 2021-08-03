package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlFileSelecter extends BaseController {

    protected File file;
    protected String name, defaultValue;
    protected boolean isSource, isDirectory, checkQuit, permitNull, mustExist;
    protected SimpleBooleanProperty notify;

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
    }

    public static ControlFileSelecter create() {
        return new ControlFileSelecter();
    }

    @Override
    public void initControls() {
        super.initControls();
        if (fileInput != null) {
            fileInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkFileInput();
                    });
        }
        label.setMinWidth(Region.USE_PREF_SIZE);
    }

    public ControlFileSelecter label(String labelString) {
        label.setText(labelString);
//        refreshStyle(thisPane);
        return this;
    }

    public ControlFileSelecter name(String name, boolean init) {
        this.name = name;
        if (name != null && init) {
            String saved = UserConfig.getUserConfigString(name, defaultValue);
            if (saved != null) {
                if (fileInput != null) {
                    fileInput.setText(saved);
                } else {
                    setFile(new File(saved));
                }
            }
        }
        return this;
    }

    public ControlFileSelecter defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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
                file = null;
                fileInput.setStyle(null);
                notify.set(!notify.get());
            } else {
                fileInput.setStyle(badStyle);
            }
            return;
        }
        File inputfile = new File(v);
        if (mustExist && !inputfile.exists()) {
            if (fileInput != null) {
                fileInput.setStyle(badStyle);
            }
            return;
        }
        if (isDirectory) {
            if (mustExist && !inputfile.isDirectory()) {
                if (fileInput != null) {
                    fileInput.setStyle(badStyle);
                }
                return;
            }
        } else {
            if (mustExist && !inputfile.isFile()) {
                if (fileInput != null) {
                    fileInput.setStyle(badStyle);
                }
                return;
            }
        }
        fileInput.setStyle(null);
        setFile(inputfile);
    }

    protected void setFile(File file) {
        if (file == null) {
            return;
        }
        this.file = file;
        if (name != null) {
            UserConfig.setUserConfigString(name, file.getAbsolutePath());
        }
        if (isSource) {
            recordFileOpened(file);
        } else {
            recordFileWritten(file);
        }
        notify.set(!notify.get());
    }

    @FXML
    public void selectFile() {
        try {
            File selectedfile;
            File path = UserConfig.getUserConfigPath(
                    isSource ? baseName + "SourcePath" : baseName + "TargetPath");
            if (isDirectory) {
                DirectoryChooser chooser = new DirectoryChooser();
                if (path != null && path.exists()) {
                    chooser.setInitialDirectory(path);
                }
                selectedfile = chooser.showDialog(getMyStage());

            } else if (mustExist) {
                FileChooser fileChooser = new FileChooser();
                if (path != null && path.exists()) {
                    fileChooser.setInitialDirectory(path);
                }
                fileChooser.getExtensionFilters().addAll(
                        isSource ? sourceExtensionFilter : targetExtensionFilter);
                selectedfile = fileChooser.showOpenDialog(getMyStage());

            } else {
                selectedfile = chooseSaveFile(path, defaultValue,
                        isSource ? sourceExtensionFilter : targetExtensionFilter);
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
    public void popRecentFiles(MouseEvent event) {
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
                    return VisitHistoryTools.getRecentReadWrite(getSourceFileType(), fileNumber);
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
                return VisitHistoryTools.getRecentPath(SourcePathType, pathNumber);
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
                    UserConfig.setUserConfigString(isSource ? baseName + "SourcePath" : baseName + "TargetPath", fname);
                    selectFile();
                }
            }

        }.pop();
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

}
