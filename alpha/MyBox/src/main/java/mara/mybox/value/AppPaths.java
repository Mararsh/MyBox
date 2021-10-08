package mara.mybox.value;

import java.io.File;
import java.util.Date;
import mara.mybox.db.table.TableStringValue;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;

/**
 * @Author Mara
 * @Update 2021-9-1
 * @License Apache License Version 2.0
 */
public class AppPaths {

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

    public static String getFileBackupsPath(File file) {
        if (file == null) {
            return null;
        }
        String key = "BackupPath-" + file;
        String fileBackupsPath = TableStringValue.read(key);
        if (fileBackupsPath == null) {
            fileBackupsPath = AppVariables.MyboxDataPath + File.separator + "fileBackups" + File.separator
                    + FileNameTools.getFilePrefix(file.getName()) + FileNameTools.getFileSuffix(file.getName())
                    + (new Date()).getTime() + File.separator;
            TableStringValue.write(key, fileBackupsPath);
        }
        File path = new File(fileBackupsPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return fileBackupsPath;
    }

}
