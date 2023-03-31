package mara.mybox.value;

import java.io.File;
import java.util.Date;
import mara.mybox.db.table.TableStringValue;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;

/**
 * @Author Mara
 * @Update 2021-9-1
 * @License Apache License Version 2.0
 */
public class AppPaths {

    public static File defaultPath() {
        return new File(getGeneratedPath());
    }

    public static boolean sysPath(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        return filename.startsWith(AppVariables.MyBoxTempPath.getAbsolutePath() + File.separator)
                || filename.startsWith(getImageClipboardPath() + File.separator)
                || filename.startsWith(getDataClipboardPath() + File.separator)
                || filename.startsWith(getImageHisPath() + File.separator)
                || filename.startsWith(getImageScopePath() + File.separator)
                || filename.startsWith(getLanguagesPath() + File.separator)
                || filename.startsWith(getBackupsPath() + File.separator);
    }

    public static String getPath(String name) {
        try {
            String pathString = AppVariables.MyboxDataPath + File.separator + name;
            File path = new File(pathString);
            if (!path.exists()) {
                path.mkdirs();
            }
            return path.toString();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static String getImageClipboardPath() {
        return getPath("imageClipboard");
    }

    public static String getDataPath() {
        return getPath("data");
    }

    public static String getDataClipboardPath() {
        return getPath("dataClipboard");
    }

    public static String getImageHisPath() {
        return getPath("imageHistories");
    }

    public static String getImageHisPath(File file) {
        if (file == null) {
            return null;
        }
        try {
            String key = "ImageHisPath-" + file.getAbsolutePath();
            String pathname = TableStringValue.read(key);
            if (pathname == null) {
                String fname = file.getName();
                String subPath = FileNameTools.prefix(fname);
                subPath += FileNameTools.suffix(fname);
                pathname = getImageHisPath() + File.separator + subPath
                        + (new Date()).getTime() + File.separator;
                TableStringValue.write(key, pathname);
            }
            File path = new File(pathname);
            if (!path.exists()) {
                path.mkdirs();
            }
            return pathname;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String getImageScopePath() {
        return getPath("imageScopes");
    }

    public static String getLanguagesPath() {
        return getPath("mybox_languages");
    }

    public static String getBackupsPath() {
        return getPath("fileBackups");
    }

    public static String getFileBackupsPath(File file) {
        if (file == null) {
            return null;
        }
        try {
            String key = "BackupPath-" + file;
            String fileBackupsPath = TableStringValue.read(key);
            if (fileBackupsPath == null) {
                String fname = file.getName();
                fileBackupsPath = getBackupsPath() + File.separator
                        + FileNameTools.prefix(fname) + FileNameTools.suffix(fname)
                        + DateTools.nowFileString() + File.separator;
                TableStringValue.write(key, fileBackupsPath);
            }
            File path = new File(fileBackupsPath);
            if (!path.exists()) {
                path.mkdirs();
            }
            return fileBackupsPath;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String getDownloadsPath() {
        return getPath("downloads");
    }

    public static String getGeneratedPath() {
        return getPath("generated");
    }

    public static String getIconsPath() {
        return getPath("icons");
    }

}
