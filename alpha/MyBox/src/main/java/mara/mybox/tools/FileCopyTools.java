package mara.mybox.tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileCopyTools {

    public static boolean copyFile(String sourceFile, String targetFile) {
        return copyFile(new File(sourceFile), new File(targetFile));
    }

    public static boolean copyFile(File sourceFile, File targetFile) {
        return copyFile(sourceFile, targetFile, false, true);
    }

    public static boolean copyFile(File sourceFile, File targetFile, boolean isCanReplace, boolean isCopyAttrinutes) {
        try {
            if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            if (!targetFile.exists()) {
                if (isCopyAttrinutes) {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()), StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()));
                }
            } else if (!isCanReplace || targetFile.isDirectory()) {
                return false;
            } else if (isCopyAttrinutes) {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static boolean copyFile(File sourceFile, File targetFile, FileSynchronizeAttributes attr) {
        if (attr == null) {
            attr = new FileSynchronizeAttributes();
        }
        return copyFile(sourceFile, targetFile, attr.isCanReplace(), attr.isCopyAttrinutes());
    }

    public static FileSynchronizeAttributes copyWholeDirectory(File sourcePath, File targetPath) {
        FileSynchronizeAttributes attr = new FileSynchronizeAttributes();
        copyWholeDirectory(sourcePath, targetPath, attr);
        return attr;
    }

    public static boolean copyWholeDirectory(File sourcePath, File targetPath, FileSynchronizeAttributes attr) {
        return copyWholeDirectory(sourcePath, targetPath, attr, true);
    }

    public static boolean copyWholeDirectory(File sourcePath, File targetPath, FileSynchronizeAttributes attr, boolean clearTarget) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (targetPath.getAbsolutePath().startsWith(sourcePath.getAbsolutePath())) {
                MyBoxLog.error(message("TargetPathShouldNotSourceSub"));
                return false;
            }
            if (attr == null) {
                attr = new FileSynchronizeAttributes();
            }
            if (targetPath.exists()) {
                if (clearTarget && !FileDeleteTools.deleteDir(targetPath)) {
                    return false;
                }
            } else {
                targetPath.mkdirs();
            }
            File[] files = sourcePath.listFiles();
            if (files == null) {
                return false;
            }
            for (File file : files) {
                File targetFile = new File(targetPath + File.separator + file.getName());
                if (file.isFile()) {
                    if (copyFile(file, targetFile, attr)) {
                        attr.setCopiedFilesNumber(attr.getCopiedFilesNumber() + 1);
                    } else if (!attr.isContinueWhenError()) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (copyWholeDirectory(file, targetFile, attr, clearTarget)) {
                        attr.setCopiedDirectoriesNumber(attr.getCopiedDirectoriesNumber() + 1);
                    } else if (!attr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

}
