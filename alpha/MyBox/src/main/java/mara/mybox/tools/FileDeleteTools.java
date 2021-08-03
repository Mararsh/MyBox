package mara.mybox.tools;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import mara.mybox.dev.MyBoxLog;
import org.apache.commons.io.FileUtils;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileDeleteTools {

    public static boolean delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        return delete(new File(fileName));
    }

    public static boolean delete(File file) {
        try {
            if (file == null || !file.exists()) {
                return true;
            }
            System.gc();
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
                return true;
            } else {
                return file.delete();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean success = deleteDir(file);
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
        //        FileUtils.deleteQuietly(dir);
        //        return true;
    }

    public static boolean clearDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    } else if (file.isDirectory()) {
                        deleteDir(file);
                    }
                }
            }
        }
        return true;
        //        try {
        //            FileUtils.cleanDirectory(dir);
        //            return true;
        //        } catch (Exception e) {
        //            MyBoxLog.error(e);
        //            return false;
        //        }
    }

    public static boolean deleteDirExcept(File dir, File except) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.equals(except)) {
                        continue;
                    }
                    boolean success = deleteDirExcept(file, except);
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return delete(dir);
    }

    public static void deleteNestedDir(File sourceDir) {
        try {
            if (sourceDir == null || !sourceDir.exists() || !sourceDir.isDirectory()) {
                return;
            }
            System.gc();
            File targetTmpDir = TmpFileTools.getTempDirectory();
            deleteNestedDir(sourceDir, targetTmpDir);
            deleteDir(targetTmpDir);
            deleteDir(sourceDir);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void deleteNestedDir(File sourceDir, File tmpDir) {
        try {
            if (sourceDir.isDirectory()) {
                File[] files = sourceDir.listFiles();
                if (files == null || files.length == 0) {
                    return;
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        File[] subfiles = file.listFiles();
                        if (subfiles != null) {
                            for (File subfile : subfiles) {
                                if (subfile.isDirectory()) {
                                    String target = tmpDir.getAbsolutePath() + File.separator + subfile.getName();
                                    Files.move(Paths.get(subfile.getAbsolutePath()), Paths.get(target));
                                } else {
                                    delete(subfile);
                                }
                            }
                        }
                        file.delete();
                    } else {
                        delete(file);
                    }
                }
                deleteNestedDir(tmpDir, sourceDir);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static int deleteEmptyDir(File dir, boolean trash) {
        return deleteEmptyDir(dir, 0, trash);
    }

    public static int deleteEmptyDir(File dir, int count, boolean trash) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                if (trash) {
                    Desktop.getDesktop().moveToTrash(dir);
                } else {
                    deleteDir(dir);
                }
                return ++count;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    count = deleteEmptyDir(file, count, trash);
                }
            }
            files = dir.listFiles();
            if (files == null || files.length == 0) {
                if (trash) {
                    Desktop.getDesktop().moveToTrash(dir);
                } else {
                    deleteDir(dir);
                }
                return ++count;
            }
        }
        return count;
    }

}
