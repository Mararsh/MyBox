package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @License Apache License Version 2.0
 */
public class FilesMergeController extends BaseBatchFileController {

    protected BufferedOutputStream outputStream;

    public FilesMergeController() {
        baseTitle = message("FilesMerge");

    }

    @Override
    public boolean makeMoreParameters() {
        try {
            targetFile = makeTargetFile();
            if (targetFile == null) {
                return false;
            }
            if (!openWriter()) {
                return false;
            }
            return super.makeMoreParameters();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    protected boolean openWriter() {
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
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
            byte[] buf = new byte[AppValues.IOBufferLength];
            int bufLen;
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        return message("Canceled");
                    }
                    outputStream.write(buf, 0, bufLen);
                }
            }
            return message("Handled") + ": " + file;
        } catch (Exception e) {
            return e.toString();
        }
    }

    @Override
    public void handleTargetFiles() {
        if (closeWriter()) {
            targetFileGenerated(targetFile);
        }
        super.handleTargetFiles();
    }

    protected boolean closeWriter() {
        try {
            outputStream.close();
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

}
