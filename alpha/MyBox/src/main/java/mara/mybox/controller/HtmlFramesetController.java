package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.FileInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-27
 * @License Apache License Version 2.0
 */
public class HtmlFramesetController extends FilesMergeController {

    protected List<File> validFiles;

    public HtmlFramesetController() {
        baseTitle = message("HtmlFrameset");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    protected boolean openWriter() {
        try {
            validFiles = new ArrayList<>();
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    @Override
    public String handleFile(FxTask currentTask, FileInformation info) {
        try {
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            File file = info.getFile();
            if (file == null || !file.isFile() || !match(file)) {
                return message("Skip" + ": " + file);
            }
            validFiles.add(file);
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    @Override
    protected boolean closeWriter() {
        try {
            if (!HtmlWriteTools.generateFrameset(validFiles, targetFile)) {
                updateLogs(message("Failed"), true, true);
                return false;
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
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
