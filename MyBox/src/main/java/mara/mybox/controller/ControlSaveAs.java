package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-9
 * @License Apache License Version 2.0
 */
public class ControlSaveAs extends BaseController {

    protected File file;
    protected String prefix;
    protected SimpleBooleanProperty fileSelected;

    public ControlSaveAs() {
        fileSelected = new SimpleBooleanProperty(false);
    }

    public static ControlSaveAs create() {
        return new ControlSaveAs();
    }

    protected ControlSaveAs type(int fileType) {
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

    protected ControlSaveAs prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    protected void setFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        this.file = file;
        recordFileOpened(file);
        recordFileWritten(file);
        fileSelected.set(true);
    }

    public void selectFile() {
        File selectedfile = chooseSaveFile(
                AppVariables.getUserConfigPath(VisitHistoryTools.getPathKey(TargetFileType)),
                prefix, targetExtensionFilter, true);
        if (selectedfile == null) {
            return;
        }
        setFile(selectedfile);
    }

    @FXML
    public void popRecentFiles(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {

            @Override
            public List<VisitHistory> recentFiles() {
                int fileNumber = AppVariables.fileRecentNumber * 2 / 3 + 1;
                return VisitHistoryTools.getRecentFile(SourceFileType, fileNumber);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber / 3 + 1;
                return VisitHistoryTools.getRecentPath(SourcePathType, pathNumber);
            }

            @Override
            public void handleSelect() {
                selectFile();
            }

            @Override
            public void handleFile(String fname) {
                File selectedfile = new File(fname);
                if (selectedfile.exists()) {
                    selectFile();
                    return;
                }
                setFile(selectedfile);
            }

            @Override
            public void handlePath(String fname) {
                File selectedfile = new File(fname);
                if (!selectedfile.exists()) {
                    selectFile();
                    return;
                }
                AppVariables.setUserConfigValue(targetPathKey, fname);
                selectFile();
            }

        }.pop();
    }

}
