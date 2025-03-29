package mara.mybox.value;

import java.io.File;
import mara.mybox.dev.MyBoxLog;

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

    public static String getImageScopePath() {
        return getPath("imageScopes");
    }

    public static String getLanguagesPath() {
        return getPath("mybox_languages");
    }

    public static String getBackupsPath() {
        return getPath("fileBackups");
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

    public static String getMatrixPath() {
        return getPath("managed" + File.separator + "matrix");
    }

}
