package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
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
            if (targetFileController != null) {
                targetFile = targetFileController.file;
            }
            if (targetFile == null) {
                return false;
            }
            targetFile = makeTargetFile(targetFile, targetFile.getParentFile());
            if (targetFile == null) {
                return false;
            }
            if (!openWriter()) {
                return false;
            }
            return super.makeMoreParameters();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
    public String handleFile(File file) {
        try {
            if (task == null || task.isCancelled()) {
                return message("Canceled");
            }
            if (file == null || !file.isFile() || !match(file)) {
                return message("Skip" + ": " + file);
            }
            byte[] buf = new byte[AppValues.IOBufferLength];
            int bufLen;
            FileInformation d = (FileInformation) tableData.get(currentParameters.currentIndex);
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(d.getFile()))) {
                while ((bufLen = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bufLen);
                }
            }
            return message("Handled") + ": " + file;
        } catch (Exception e) {
            return file + " " + e.toString();
        }
    }

    @Override
    public void afterTask() {
        if (closeWriter()) {
            targetFileGenerated(targetFile);
        }
        super.afterTask();
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
