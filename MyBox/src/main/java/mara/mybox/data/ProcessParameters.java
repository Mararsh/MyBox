package mara.mybox.data;

import java.io.File;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-4-11 10:54:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ProcessParameters implements Cloneable {

    public File currentSourceFile, currentTargetPath;
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
                newCode.currentSourceFile = new File(currentSourceFile.getAbsolutePath());
            }
            if (currentTargetPath != null) {
                newCode.currentTargetPath = new File(currentTargetPath.getAbsolutePath());
            }
            return newCode;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

}
