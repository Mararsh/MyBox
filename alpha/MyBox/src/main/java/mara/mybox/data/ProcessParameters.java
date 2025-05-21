package mara.mybox.data;

import java.io.File;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-4-11 10:54:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ProcessParameters implements Cloneable {

    public FileInformation currentSourceFile;
    public File currentTargetPath;
    public int startIndex, currentIndex;
    public String status, targetPath, targetRootPath;
    public boolean targetSubDir, isBatch;
    public int fromPage, toPage, startPage, acumFrom, acumStart, acumDigit;
    public String password;
    public int currentPage;

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            ProcessParameters newCode = (ProcessParameters) super.clone();
            if (currentSourceFile != null) {
                newCode.currentSourceFile = currentSourceFile;
            }
            if (currentTargetPath != null) {
                newCode.currentTargetPath = new File(currentTargetPath.getAbsolutePath());
            }
            return newCode;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

}
