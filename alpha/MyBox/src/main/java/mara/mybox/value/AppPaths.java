package mara.mybox.value;

import java.io.File;
import java.util.Date;
import mara.mybox.db.table.TableStringValue;
import mara.mybox.tools.FileNameTools;

/**
 * @Author Mara
 * @Update 2021-9-1
 * @License Apache License Version 2.0
 */
public class AppPaths {

    public static String getImageClipboardPath() {
        String imageClipboardPath = AppVariables.MyboxDataPath + File.separator + "imageClipboard";
        File path = new File(imageClipboardPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageClipboardPath;
    }

    public static String getDataClipboardPath() {
        String dataClipboardPath = AppVariables.MyboxDataPath + File.separator + "dataClipboard";
        File path = new File(dataClipboardPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return dataClipboardPath;
    }

    public static String getImageHisPath() {
        String imageHistoriesPath = AppVariables.MyboxDataPath + File.separator + "imageHistories";
        File path = new File(imageHistoriesPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageHistoriesPath;
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

    public static String getImageScopePath() {
        String imageScopesPath = AppVariables.MyboxDataPath + File.separator + "imageScopes";
        File path = new File(imageScopesPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return imageScopesPath;
    }

}
