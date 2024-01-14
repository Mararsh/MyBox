package mara.mybox.tools;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileDeleteTools {

    public static boolean delete(String fileName) {
        return delete(null, fileName);
    }

    public static boolean delete(FxTask currentTask, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        return delete(currentTask, new File(fileName));
    }

    public static boolean delete(File file) {
        return delete(null, file);
    }

    public static boolean delete(FxTask currentTask, File file) {
        try {
            if (file == null || !file.exists()) {
                return true;
            }
            System.gc();
            if (file.isDirectory()) {
                return deleteDir(currentTask, file);
            } else {
                return file.delete();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean deleteDir(File file) {
        return delete(null, file);
    }

    public static boolean deleteDir(FxTask currentTask, File file) {
        if (file == null || (currentTask != null && !currentTask.isWorking())) {
            return false;
        }
        if (file.exists() && file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if ((currentTask != null && !currentTask.isWorking())
                            || !deleteDir(currentTask, child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
        //        FileUtils.deleteQuietly(dir);
        //        return true;
    }

    public static boolean clearDir(FxTask currentTask, File file) {
        if (file == null || (currentTask != null && !currentTask.isWorking())) {
            return false;
        }
        if (file.exists() && file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if ((currentTask != null && !currentTask.isWorking())) {
                        return false;
                    }
                    if (child.isDirectory()) {
                        if (clearDir(currentTask, child)) {
                            child.delete();
                        } else {
                            return false;
                        }
                    } else {
                        child.delete();
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

    public static boolean deleteDirExcept(FxTask currentTask, File dir, File except) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if ((currentTask != null && !currentTask.isWorking())) {
                        return false;
                    }
                    if (child.equals(except)) {
                        continue;
                    }
                    if (!deleteDirExcept(currentTask, child, except)) {
                        return false;
                    }
                }
            }
        }
        return delete(currentTask, dir);
    }

    public static void deleteNestedDir(FxTask currentTask, File sourceDir) {
        try {
            if ((currentTask != null && !currentTask.isWorking())
                    || sourceDir == null || !sourceDir.exists() || !sourceDir.isDirectory()) {
                return;
            }
            System.gc();
            File targetTmpDir = FileTmpTools.getTempDirectory();
            deleteNestedDir(currentTask, sourceDir, targetTmpDir);
            deleteDir(currentTask, targetTmpDir);
            deleteDir(currentTask, sourceDir);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void deleteNestedDir(FxTask currentTask, File sourceDir, File tmpDir) {
        try {
            if ((currentTask != null && !currentTask.isWorking())
                    || sourceDir == null || !sourceDir.exists()) {
                return;
            }
            if (sourceDir.isDirectory()) {
                File[] files = sourceDir.listFiles();
                if (files == null || files.length == 0) {
                    return;
                }
                for (File file : files) {
                    if ((currentTask != null && !currentTask.isWorking())) {
                        return;
                    }
                    if (file.isDirectory()) {
                        File[] subfiles = file.listFiles();
                        if (subfiles != null) {
                            for (File subfile : subfiles) {
                                if ((currentTask != null && !currentTask.isWorking())) {
                                    return;
                                }
                                if (subfile.isDirectory()) {
                                    String target = tmpDir.getAbsolutePath() + File.separator + subfile.getName();
                                    new File(target).getParentFile().mkdirs();
                                    Files.move(Paths.get(subfile.getAbsolutePath()), Paths.get(target));
                                } else {
                                    delete(currentTask, subfile);
                                }
                            }
                        }
                        file.delete();
                    } else {
                        delete(currentTask, file);
                    }
                }
                if ((currentTask != null && !currentTask.isWorking())) {
                    return;
                }
                deleteNestedDir(currentTask, tmpDir, sourceDir);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static int deleteEmptyDir(FxTask currentTask, File dir, boolean trash) {
        return deleteEmptyDir(currentTask, dir, 0, trash);
    }

    public static int deleteEmptyDir(FxTask currentTask, File dir, int count, boolean trash) {
        if ((currentTask != null && !currentTask.isWorking())) {
            return count;
        }
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children == null || children.length == 0) {
                boolean ok;
                if (trash) {
                    ok = Desktop.getDesktop().moveToTrash(dir);
                } else {
                    ok = deleteDir(currentTask, dir);
                }
                if (ok) {
                    return ++count;
                } else {
                    return count;
                }
            }
            for (File child : children) {
                if ((currentTask != null && !currentTask.isWorking())) {
                    return count;
                }
                if (child.isDirectory()) {
                    count = deleteEmptyDir(currentTask, child, count, trash);
                }
            }
            children = dir.listFiles();
            if (children == null || children.length == 0) {
                boolean ok;
                if (trash) {
                    ok = Desktop.getDesktop().moveToTrash(dir);
                } else {
                    ok = deleteDir(currentTask, dir);
                }
                if (ok) {
                    return ++count;
                } else {
                    return count;
                }
            }
        }
        return count;
    }

    public static void clearJavaIOTmpPath(FxTask currentTask) {
        try {
            System.gc();
            clearDir(currentTask, FileTools.javaIOTmpPath());
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
    }

}
