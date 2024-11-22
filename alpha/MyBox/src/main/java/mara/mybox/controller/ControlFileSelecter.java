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
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlFileSelecter extends BaseController {

    private File file;
    protected File defaultFile;
    protected boolean isSource, isDirectory, checkQuit, permitNull, mustExist;
    protected SimpleBooleanProperty notify;

    @FXML
    protected Label label;
    @FXML
    protected TextField fileInput;
    @FXML
    protected Button openTargetButton;

    public ControlFileSelecter() {
        initSelecter();
    }

    public final ControlFileSelecter initSelecter() {
        file = null;
        defaultFile = null;
        isSource = true;
        isDirectory = false;
        checkQuit = false;
        permitNull = false;
        mustExist = false;
        notify = new SimpleBooleanProperty(false);
        return this;
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
                    pickFile();
                }
            });
        }
        label.setMinWidth(Region.USE_PREF_SIZE);
    }

    /*
        make
     */
    public String defaultName() {
        if (defaultFile == null) {
            String ext = VisitHistoryTools.defaultExt(TargetFileType);
            if (isDirectory) {
                defaultFile = new File(FileTmpTools.generatePath(ext));
            } else {
                defaultFile = FileTmpTools.generateFile(ext);
            }
        }
        return defaultFile.getAbsolutePath();
    }

    public ControlFileSelecter parent(BaseController parent) {
        return parent(parent, null);
    }

    public ControlFileSelecter parent(BaseController parent, String name) {
        parentController = parent;
        if (name != null && !name.isBlank()) {
            baseName = name;
        } else {
            if (parentController != null) {
                baseName = parentController.baseName;
            }
            baseName += (isSource ? "Source" : "Target")
                    + (isDirectory ? "Path" : "File");
        }
        return initFile();
    }

    public ControlFileSelecter initFile() {
        String defaultName = defaultName();
        String filename = UserConfig.getString(baseName, defaultName);
        input(filename);
        return this;
    }

    public void inputFile(File file) {
        if (fileInput != null) {
            isSettingValues = true;
            if (file != null) {
                fileInput.setText(file.getAbsolutePath());
            } else {
                fileInput.clear();
            }
            isSettingValues = false;
        }
        setFile(file);
    }

    public void input(String string) {
        if (fileInput != null) {
            isSettingValues = true;
            fileInput.setText(string);
            isSettingValues = false;
        }
        if (string != null) {
            setFile(new File(string));
        } else {
            setFile(null);
        }
    }

    public File pickFile() {
        if (isSettingValues || fileInput == null) {
            return file;
        }
        String v = fileInput.getText();
        if (v == null || v.isBlank()) {
            file = null;
            return file;
        }
        File inputfile = new File(v);
        if (mustExist && (!inputfile.exists()
                || isDirectory && !inputfile.isDirectory()
                || !isDirectory && !inputfile.isFile())) {
            file = null;
            return file;
        }
        return setFile(inputfile);

    }

    private File setFile(File infile) {
        file = infile;
        if (file != null) {
            UserConfig.setString(baseName, file.getAbsolutePath());
            if (parentController != null) {
                if (isDirectory) {
                    if (isSource) {
                        parentController.sourcePath = file;
                    } else {
                        parentController.targetPath = file;
                    }
                } else {
                    if (isSource) {
//                        parentController.sourceFile = file;
                    } else {
                        parentController.targetFile = file;
                    }
                }
            }
        }
        notify.set(!notify.get());
        if (openTargetButton != null) {
            openTargetButton.setDisable(file == null || !file.exists());
        }
        return file;
    }

    public File file() {
        return file;
    }

    public String text() {
        if (fileInput == null) {
            return null;
        } else {
            return fileInput.getText();
        }
    }

    /*
        set
     */
    public ControlFileSelecter type(int fileType) {
        setFileType(fileType);
        return this;
    }

    public ControlFileSelecter label(String labelString) {
        label.setText(labelString);
//        refreshStyle(thisPane);
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

    /*
        button
     */
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
            inputFile(selectedfile);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        new RecentVisitMenu(this, event, isDirectory) {

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber;
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
                    isSettingValues = true;
                    fileInput.setText(selectedfile.getAbsolutePath());
                    isSettingValues = false;
                }
                setFile(selectedfile);
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
                        isSettingValues = true;
                        fileInput.setText(selectedfile.getAbsolutePath());
                        isSettingValues = false;
                    }
                    setFile(selectedfile);
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

    @FXML
    @Override
    public void openTarget() {
        if (file == null || !file.exists()) {
            return;
        }
        browseURI(file.toURI());
        recordFileOpened(file);
    }

    @Override
    public void cleanPane() {
        try {
            file = null;
            defaultFile = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get
     */
    public boolean isIsSource() {
        return isSource;
    }

    public boolean isIsDirectory() {
        return isDirectory;
    }

    public boolean isCheckQuit() {
        return checkQuit;
    }

    public boolean isPermitNull() {
        return permitNull;
    }

    public boolean isMustExist() {
        return mustExist;
    }

}
