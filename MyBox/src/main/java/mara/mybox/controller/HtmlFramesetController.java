package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-27
 * @License Apache License Version 2.0
 */
public class HtmlFramesetController extends BaseBatchFileController {

    protected List<File> validFiles;

    @FXML
    protected ControlFileSelecter targetFileController;

    public HtmlFramesetController() {
        baseTitle = AppVariables.message("HtmlFrameset");

        SourceFileType = VisitHistory.FileType.All;
        SourcePathType = VisitHistory.FileType.All;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        AddFileType = VisitHistory.FileType.All;
        AddPathType = VisitHistory.FileType.All;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.All);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);

        sourceExtensionFilter = CommonFxValues.AllExtensionFilter;
        targetExtensionFilter = CommonFxValues.HtmlExtensionFilter;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            targetFileInput = targetFileController.fileInput;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            targetFileController.label(message("TargetFile"))
                    .isDirectory(false).isSource(false).mustExist(false).permitNull(false)
                    .defaultValue("_" + message("Merge"))
                    .name(baseName + "TargetFile", false).type(VisitHistory.FileType.Html);

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            targetFile = targetFileController.file;
            if (targetFile == null) {
                return false;
            }
            validFiles = new ArrayList<>();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        return true;
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            validFiles.add(srcFile);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public void afterHandleFiles() {
        if (HtmlTools.generateFrameset(validFiles, targetFile)) {
            targetFileGenerated(targetFile);
        } else {
            updateLogs(AppVariables.message("Failed"), true, true);
        }
    }

    @Override
    public void viewTarget(File file) {
        if (file == null) {
            return;
        }
        browseURI(file.toURI());
    }

}
