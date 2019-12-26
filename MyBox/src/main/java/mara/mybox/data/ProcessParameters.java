package mara.mybox.data;

import java.io.File;

/**
 * @Author Mara
 * @CreateDate 2019-4-11 10:54:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ProcessParameters {

    public File currentSourceFile, currentTargetPath;
    public int startIndex, currentIndex;
    public String status, targetPath, targetRootPath;
    public boolean targetSubDir, isBatch;
    public int fromPage, toPage, startPage, acumFrom, acumStart, acumDigit;
    public String password;
    public int currentPage;

}
