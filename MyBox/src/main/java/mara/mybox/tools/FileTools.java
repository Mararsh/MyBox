/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import mara.mybox.objects.CommonValues;
import static mara.mybox.objects.CommonValues.UserFilePath;
import mara.mybox.objects.FileSynchronizeAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author mara
 * @CreateDate 2018-6-2 11:01:45
 * @Description
 */
public class FileTools {

    private static final Logger logger = LogManager.getLogger();

    public static long getFileCreateTime(final String filename) {
        try {
            FileTime t = Files.readAttributes(Paths.get(filename), BasicFileAttributes.class).creationTime();
            return t.toMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getFilePath(final String filename) {
        if (filename == null) {
            return null;
        }
        int pos = filename.lastIndexOf("/");
        if (pos < 0) {
            return "";
        }
        return filename.substring(0, pos);
    }

    public static String getFileName(final String filename) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = filename.lastIndexOf("/");
        if (pos >= 0) {
            fname = fname.substring(pos + 1);
        }
        return fname;
    }

    public static String getFilePrefix(final String filename) {
        String fname = getFileName(filename);
        if (fname == null) {
            return null;
        }
        int pos = fname.lastIndexOf(".");
        if (pos >= 0) {
            fname = fname.substring(0, pos);
        }
        return fname;
    }

    public static String getFileSuffix(final String filename) {
        if (filename == null) {
            return null;
        }
        String suffix;
        int pos = filename.lastIndexOf(".");
        if (pos >= 0 && filename.length() > pos) {
            suffix = filename.substring(pos + 1);
        } else {
            suffix = "";
        }
        return suffix;
    }

    public static String replaceFileSuffix(String filename, String newSuffix) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = filename.lastIndexOf(".");
        if (pos >= 0) {
            fname = fname.substring(0, pos) + "." + newSuffix;
        } else {
            fname += "." + newSuffix;
        }
        return fname;
    }

    public static String getTempFile(String filename) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = filename.lastIndexOf(".");
        if (pos >= 0) {
            fname = fname.substring(0, pos) + new Date().getTime() + "." + fname.substring(pos + 1);
        }
        return fname;
    }

    public static File getTempFile() {
        File file = new File(UserFilePath + "/temp" + new Date().getTime() + ValueTools.getRandomInt(100));
        while (file.exists()) {
            file = new File(UserFilePath + "/temp" + new Date().getTime() + ValueTools.getRandomInt(100));
        }
        return file;
    }

    public static boolean isPDF(String filename) {
        String suffix = getFileSuffix(filename);
        if (suffix == null) {
            return false;
        }
        return "PDF".equals(suffix.toUpperCase());
    }

    public static String insertFileName(String filename, String inStr) {
        if (filename == null) {
            return null;
        }
        if (inStr == null) {
            return filename;
        }
        int pos = filename.lastIndexOf(".");
        if (pos < 0) {
            return filename + inStr;
        }
        return filename.substring(0, pos) + inStr + "." + filename.substring(pos + 1);
    }

    public static File getHelpFile(String helpFile) {

        String filepath = UserFilePath + "/" + helpFile;
        File file = new File(UserFilePath + "/" + helpFile);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static String showFileSizeKB(long size) {
        long kb = (long) (size / 1024f + 0.5);
        if (kb == 0) {
            kb = 1;
        }
        String s = kb + "";
        String t = "";
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--, count++) {
            if (count > 0 && (count % 3 == 0)) {
                t = "," + t;
            }
            t = s.charAt(i) + t;
        }
        return t + " KB";
    }

    public static String showFileSize(long size) {
        double kb = size * 1.0f / 1024;
        if (kb < 1024) {
            return ValueTools.roundDouble3(kb) + " KB";
        } else {
            double mb = size * 1.0f / (1024 * 1024);
            if (mb < 1024) {
                return ValueTools.roundDouble3(mb) + " MB";
            } else {
                double gb = size * 1.0f / (1024 * 1024 * 1024);
                return ValueTools.roundDouble3(gb) + " GB";
            }
        }
    }

    public static String showFileSize2(long size) {
        double kb = size * 1.0f / 1024;
        String s = ValueTools.roundDouble3(kb) + " KB";
        if (kb < 1024) {
            return s;
        }

        double mb = size * 1.0f / (1024 * 1024);
        if (mb < 1024) {
            return s + " (" + ValueTools.roundDouble3(mb) + " MB)";
        } else {
            double gb = size * 1.0f / (1024 * 1024 * 1024);
            return s + " (" + ValueTools.roundDouble3(gb) + " GB)";
        }

    }

    public static class FileSortType {

        public static final int FileName = 0;
        public static final int ModifyTime = 1;
        public static final int CreateTime = 2;
        public static final int Size = 3;

    }

    public static void sortFiles(List<File> files, int sortTpye) {
        switch (sortTpye) {
            case FileSortType.FileName:
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return f1.getAbsolutePath().compareTo(f1.getAbsolutePath());
                    }
                });
                break;

            case FileSortType.ModifyTime:
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        long t1 = f1.lastModified();
                        long t2 = f2.lastModified();
                        if (t1 == t2) {
                            return 0;
                        }
                        if (t1 > t2) {
                            return 1;
                        }
                        return -1;
                    }
                });
                break;

            case FileSortType.CreateTime:
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        long t1 = FileTools.getFileCreateTime(f1.getAbsolutePath());
                        long t2 = FileTools.getFileCreateTime(f2.getAbsolutePath());
                        if (t1 == t2) {
                            return 0;
                        }
                        if (t1 > t2) {
                            return 1;
                        }
                        return -1;
                    }
                });
                break;

            case FileSortType.Size:
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        long t1 = f1.length();
                        long t2 = f2.length();
                        if (t1 == t2) {
                            return 0;
                        }
                        if (t1 > t2) {
                            return 1;
                        }
                        return -1;
                    }
                });
                break;

        }
    }

    public static boolean isSupportedImage(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String suffix = getFileSuffix(file.getName()).toLowerCase();
        return CommonValues.SupportedImages.contains(suffix);
    }

    public static FileSynchronizeAttributes copyWholeDirectory(File sourcePath, File targetPath) {
        FileSynchronizeAttributes attr = new FileSynchronizeAttributes();
        copyWholeDirectory(sourcePath, targetPath, attr);
        return attr;
    }

    public static boolean copyWholeDirectory(File sourcePath, File targetPath, FileSynchronizeAttributes attr) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (attr == null) {
                attr = new FileSynchronizeAttributes();
            }
            if (targetPath.exists()) {
                if (!deleteDir(targetPath)) {
                    return false;
                }
            }
            targetPath.mkdirs();
            File[] files = sourcePath.listFiles();
            for (File file : files) {
                File targetFile = new File(targetPath + File.separator + file.getName());
                if (file.isFile()) {
                    if (copyFile(file, targetFile, attr)) {
                        attr.setCopiedFilesNumber(attr.getCopiedFilesNumber() + 1);
                    } else if (!attr.isContinueWhenError()) {
                        return false;
                    }
                } else {
                    if (copyWholeDirectory(file, targetFile, attr)) {
                        attr.setCopiedDirectoriesNumber(attr.getCopiedDirectoriesNumber() + 1);
                    } else if (!attr.isContinueWhenError()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean copyFile(File sourceFile, File targetFile, FileSynchronizeAttributes attr) {
        try {
            if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            if (attr == null) {
                attr = new FileSynchronizeAttributes();
            }
            if (!targetFile.exists()) {
                if (attr.isCopyAttrinutes()) {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()));
                }
            } else if (!attr.isCanReplace() || targetFile.isDirectory()) {
                return false;
            } else if (attr.isCopyAttrinutes()) {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                boolean success = deleteDir(file);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static String getFontFile(String fontName) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            return getFontFile("/usr/share/fonts/", fontName);

        } else if (os.contains("windows")) {
            return getFontFile("C:/Windows/Fonts/", fontName);

        } else if (os.contains("mac")) {
            String f = getFontFile("/Library/Fonts/", fontName);
            if (f != null) {
                return f;
            } else {
                return getFontFile("/System/Library/Fonts/", fontName);
            }
        }
        return null;
    }

    public static String getFontFile(String path, String fontName) {
        if (new File(path + fontName + ".ttf ").exists()) {
            return path + fontName + ".ttf ";
        } else if (new File(path + fontName.toLowerCase() + ".ttf ").exists()) {
            return path + fontName + ".ttf ";
        } else if (new File(path + fontName.toUpperCase() + ".ttf ").exists()) {
            return path + fontName + ".ttf ";
        } else {
            return null;
        }
    }

}
