package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @Description
 * @License Apache License Version 2.0
 */
public class ControlFileSelecter extends BaseController {

    protected File file;
    protected String name, defaultValue;
    protected boolean isSource, isDirectory, checkQuit, permitNull, mustExist;

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
    }

    public ControlFileSelecter label(String labelString) {
        label.setText(labelString);
//        FxmlControl.refreshStyle(thisPane);
        return this;
    }

    public ControlFileSelecter name(String name, boolean init) {
        this.name = name;
        if (name != null && init) {
            String saved = AppVariables.getUserConfigValue(name, defaultValue);
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
        SourceFileType = fileType;
        SourcePathType = fileType;
        AddFileType = fileType;
        AddPathType = fileType;
        TargetPathType = fileType;
        TargetFileType = fileType;
        sourcePathKey = VisitHistoryTools.getPathKey(fileType);
        targetPathKey = sourcePathKey;
        SaveAsOptionsKey = VisitHistoryTools.getSaveAsOptionsKey(fileType);
        if (fileType == VisitHistory.FileType.Image) {
            sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        } else if (fileType == VisitHistory.FileType.PDF) {
            sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Text) {
            sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Bytes) {
            sourceExtensionFilter = CommonFxValues.AllExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Markdown) {
            sourceExtensionFilter = CommonFxValues.MarkdownExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Html) {
            sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Gif) {
            sourceExtensionFilter = CommonFxValues.GifExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Tif) {
            sourceExtensionFilter = CommonFxValues.TiffExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Media) {
            sourceExtensionFilter = CommonFxValues.JdkMediaExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Icc) {
            sourceExtensionFilter = CommonFxValues.IccProfileExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Certificate) {
            sourceExtensionFilter = CommonFxValues.KeyStoreExtensionFilter;
        } else if (fileType == VisitHistory.FileType.TextEditHistory) {
            sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        } else {
            sourceExtensionFilter = CommonFxValues.AllExtensionFilter;
        }
        targetExtensionFilter = sourceExtensionFilter;

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
            AppVariables.setUserConfigValue(name, file.getAbsolutePath());
        }
        if (isSource) {
            recordFileOpened(file);
        } else {
            recordFileWritten(file);
        }
    }

    @FXML
    public void selectFile() {
        try {
            File selectedfile;
            File path = AppVariables.getUserConfigPath(
                    isSource ? sourcePathKey : targetPathKey);
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
            logger.error(e.toString());
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
                    int fileNumber = AppVariables.fileRecentNumber * 2 / 3 + 1;
                    return VisitHistoryTools.getRecentFile(SourceFileType, fileNumber);
                }
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber;
                if (isDirectory) {
                    pathNumber = AppVariables.fileRecentNumber;
                } else {
                    pathNumber = AppVariables.fileRecentNumber / 3 + 1;
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
                    AppVariables.setUserConfigValue(isSource ? sourcePathKey : targetPathKey, fname);
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

}
