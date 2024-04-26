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

    public static String tmpFilename(String path) {
        return path + File.separator + DateTools.nowFileString() + "_" + IntTools.random(100);
    }

    public static String tmpFilename(String path, String prefix) {
        if (path == null) {
            return null;
        }
        if (prefix == null) {
            return FileTmpTools.tmpFilename(path);
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

    public static File tmpFile(String prefix, String ext) {
        return getPathTempFile(AppVariables.MyBoxTempPath.getAbsolutePath(), prefix,
                ext == null || ext.isBlank() ? null : "." + ext);
    }

    public static File getPathTempFile(String path) {
        if (path == null) {
            return null;
        }
        new File(path).mkdirs();
        File file = new File(FileTmpTools.tmpFilename(path));
        while (file.exists()) {
            file = new File(FileTmpTools.tmpFilename(path));
        }
        return file;
    }

    public static File getPathTempFile(String path, String suffix) {
        if (path == null) {
            return null;
        }
        new File(path).mkdirs();
        String s = FileNameTools.filter(suffix);
        s = s == null || s.isBlank() ? "" : s;
        File file = new File(FileTmpTools.tmpFilename(path) + s);
        while (file.exists()) {
            file = new File(FileTmpTools.tmpFilename(path) + s);
        }
        return file;
    }

    public static File getPathTempFile(String path, String prefix, String suffix) {
        if (path == null) {
            return null;
        }
        new File(path).mkdirs();
        String p = FileNameTools.filter(prefix);
        String s = FileNameTools.filter(suffix);
        s = s == null || s.isBlank() ? "" : s;
        if (p != null && !p.isBlank()) {
            File tFile = new File(path + File.separator + p + s);
            while (tFile.exists()) {
                tFile = new File(tmpFilename(path, p) + s);
            }
            return tFile;
        }
        return getPathTempFile(path, s);
    }

    public static File getTempDirectory() {
        return getPathTempDirectory(AppVariables.MyBoxTempPath.getAbsolutePath());
    }

    public static File getPathTempDirectory(String path) {
        if (path == null) {
            return null;
        }
        new File(path).mkdirs();
        File file = new File(FileTmpTools.tmpFilename(path) + File.separator);
        while (file.exists()) {
            file = new File(FileTmpTools.tmpFilename(path) + File.separator);
        }
        file.mkdirs();
        return file;
    }

    public static boolean isTmpFile(File file) {
        return file != null
                && file.getAbsolutePath().startsWith(
                        AppVariables.MyBoxTempPath.getAbsolutePath() + File.separator);
    }

    public static String generatePath(String type) {
        String path = AppPaths.getGeneratedPath() + File.separator
                + (type == null || type.isBlank() ? "x" : FileNameTools.filter(type));
        new File(path).mkdirs();
        return path;
    }

    public static File generateFile(String ext) {
        return getPathTempFile(generatePath(ext), ext == null || ext.isBlank() ? null : "." + ext);
    }

    public static File generateFile(String prefix, String ext) {
        return getPathTempFile(generatePath(ext), prefix, ext == null || ext.isBlank() ? null : "." + ext);
    }

    public static boolean isGenerateFile(File file) {
        return file != null
                && file.getAbsolutePath().startsWith(
                        AppPaths.getGeneratedPath() + File.separator);
    }

}
