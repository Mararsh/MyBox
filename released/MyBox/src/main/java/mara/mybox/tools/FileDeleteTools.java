package mara.mybox.tools;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import mara.mybox.dev.MyBoxLog;

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
                return deleteDir(file);
            } else {
                return file.delete();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean deleteDir(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists() && file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDir(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
        //        FileUtils.deleteQuietly(dir);
        //        return true;
    }

    public static boolean clearDir(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists() && file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isDirectory()) {
                        if (clearDir(child)) {
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

    public static boolean deleteDirExcept(File dir, File except) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.equals(except)) {
                        continue;
                    }
                    if (!deleteDirExcept(child, except)) {
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
            File targetTmpDir = FileTmpTools.getTempDirectory();
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
                                    new File(target).getParentFile().mkdirs();
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
            File[] children = dir.listFiles();
            if (children == null || children.length == 0) {
                if (trash) {
                    Desktop.getDesktop().moveToTrash(dir);
                } else {
                    deleteDir(dir);
                }
                return ++count;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    count = deleteEmptyDir(child, count, trash);
                }
            }
            children = dir.listFiles();
            if (children == null || children.length == 0) {
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

    public static void clearJavaIOTmpPath() {
        try {
            System.gc();
            clearDir(FileTools.javaIOTmpPath());
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
    }

}
