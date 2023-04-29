package mara.mybox.tools;

import java.io.File;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileTmpTools {

    public static String getTempFileName() {
        return getTempFileName(AppVariables.MyBoxTempPath.getAbsolutePath());
    }

    public static String getTempFileName(String path) {
        return path + File.separator + DateTools.nowFileString() + "_" + IntTools.random(100);
    }

    public static String getTempFileName(String path, String prefix) {
        if (prefix == null) {
            return getTempFileName(path);
        }
        return path + File.separator + FileNameTools.filter(prefix)
                + "_" + DateTools.nowFileString() + "_" + IntTools.random(100);
    }

    public static File getTempFile() {
        return getPathTempFile(AppVariables.MyBoxTempPath.getAbsolutePath());
    }

    public static File getTempFile(String suffix) {
        return getPathTempFile(AppVariables.MyBoxTempPath.getAbsolutePath(), suffix);
    }

    public static File getPathTempFile(String path) {
        File file = new File(getTempFileName(path));
        while (file.exists()) {
            file = new File(getTempFileName(path));
        }
        return file;
    }

    public static File getPathTempFile(String path, String suffix) {
        String s = FileNameTools.filter(suffix);
        File file = new File(getTempFileName(path) + s);
        while (file.exists()) {
            file = new File(getTempFileName(path) + s);
        }
        return file;
    }

    public static File getPathTempFile(String path, String prefix, String suffix) {
        String p = FileNameTools.filter(prefix);
        String s = FileNameTools.filter(suffix);
        if (p != null && !p.isBlank()) {
            File tFile = new File(path + File.separator + p + s);
            while (tFile.exists()) {
                tFile = new File(getTempFileName(path, p) + s);
            }
            return tFile;
        }
        return getPathTempFile(path, s);
    }

    public static File getTempDirectory() {
        return getPathTempDirectory(AppVariables.MyBoxTempPath.getAbsolutePath());
    }

    public static File getPathTempDirectory(String path) {
        File file = new File(getTempFileName(path) + File.separator);
        while (file.exists()) {
            file = new File(getTempFileName(path) + File.separator);
        }
        file.mkdirs();
        return file;
    }

    public static File pdfFile() {
        return getPathTempFile(AppPaths.getGeneratedPath(), ".pdf");
    }

    public static File csvFile() {
        return getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
    }

    public static File txtFile() {
        return getPathTempFile(AppPaths.getGeneratedPath(), ".txt");
    }

    public static File excelFile() {
        return getPathTempFile(AppPaths.getGeneratedPath(), ".xlsx");
    }

}
