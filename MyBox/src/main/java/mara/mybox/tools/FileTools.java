package mara.mybox.tools;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FileSynchronizeAttributes;
import mara.mybox.data.TextEditInformation;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.MyBoxTempPath;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.commons.io.FileUtils;

/**
 * @Author mara
 * @CreateDate 2018-6-2 11:01:45
 * @Description
 */
public class FileTools {

    public static enum FileSortMode {
        ModifyTimeDesc, ModifyTimeAsc, CreateTimeDesc, CreateTimeAsc,
        SizeDesc, SizeAsc, NameDesc, NameAsc, FormatDesc, FormatAsc
    }

    public static FileSortMode sortMode(String mode) {
        for (FileSortMode v : FileSortMode.values()) {
            if (v.name().equals(mode) || message(v.name()).equals(mode)) {
                return v;
            }
        }
        return null;
    }

    public static String filenameFilter(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        Pattern pattern = Pattern.compile(CommonValues.FileNameSpecialChars);
        return pattern.matcher(name).replaceAll("_");
    }

    public static long createTime(final String filename) {
        try {
            FileTime t = Files.readAttributes(Paths.get(filename), BasicFileAttributes.class).creationTime();
            return t.toMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    public static long createTime(File file) {
        if (file == null || !file.exists()) {
            return -1;
        }
        return createTime(file.getAbsolutePath());
    }

    public static String getFilePrefix(File file) {
        try {
            return getFilePrefix(file.getName());
        } catch (Exception e) {
            return "";
        }
    }

    public static String getName(final String filename) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = fname.lastIndexOf(File.separator);
        if (pos >= 0) {
            fname = fname.substring(pos + 1);
        }
        return fname;
    }

    public static String namePrefix(final String file) {
        if (file == null) {
            return null;
        }
        String fname = getName(file);
        int pos = fname.lastIndexOf('.');
        if (pos >= 0) {
            fname = fname.substring(0, pos);
        }
        return fname;
    }

    // filename may include path or not, it is decided by caller
    public static String getFilePrefix(String file) {
        if (file == null) {
            return null;
        }
        String path, name;
        int pos = file.lastIndexOf(File.separator);
        if (pos >= 0) {
            path = file.substring(0, pos + 1);
            name = file.substring(pos + 1);
        } else {
            path = "";
            name = file;
        }
        pos = name.lastIndexOf('.');
        if (pos >= 0) {
            return path + name.substring(0, pos);
        } else {
            return path + name;
        }
    }

    public static String getFileSuffix(File file) {
        if (file == null) {
            return null;
        }
        return getFileSuffix(file.getName());
    }

    // not include "."
    public static String getFileSuffix(String file) {
        if (file == null || file.endsWith(File.separator)) {
            return null;
        }
        if (file.endsWith(".")) {
            return "";
        }
        String name = getName(file);
        int pos = name.lastIndexOf('.');
        if (pos >= 0) {
            return name.substring(pos + 1);
        } else {
            return "";
        }
    }

    public static String replaceFileSuffix(String file, String newSuffix) {
        if (file == null || newSuffix == null) {
            return null;
        }
        String path, name;
        int pos = file.lastIndexOf(File.separator);
        if (pos >= 0) {
            path = file.substring(0, pos + 1);
            name = file.substring(pos + 1);
        } else {
            path = "";
            name = file;
        }
        pos = name.lastIndexOf('.');
        if (pos >= 0) {
            name = path + name.substring(0, pos + 1) + newSuffix;
        } else {
            name = path + name + "." + newSuffix;
        }
        return name;
    }

    public static String appendName(String file, String append) {
        if (file == null) {
            return null;
        }
        if (append == null || append.isEmpty()) {
            return file;
        }
        String path, name;
        int pos = file.lastIndexOf(File.separator);
        if (pos >= 0) {
            path = file.substring(0, pos + 1);
            name = file.substring(pos + 1);
        } else {
            path = "";
            name = file;
        }
        pos = name.lastIndexOf('.');
        if (pos >= 0) {
            return path + name.substring(0, pos) + append + name.substring(pos);
        } else {
            return path + name + append;
        }
    }

    public static String getTempFileName() {
        return getTempFileName(MyBoxTempPath.getAbsolutePath());
    }

    public static File getTempFile() {
        return getPathTempFile(MyBoxTempPath.getAbsolutePath());
    }

    public static File getTempFile(String suffix) {
        return getPathTempFile(MyBoxTempPath.getAbsolutePath(), suffix);
    }

    public static File getTempDirectory() {
        return getPathTempDirectory(MyBoxTempPath.getAbsolutePath());
    }

    public static String getTempFileName(String path) {
        return path + File.separator + new Date().getTime() + IntTools.getRandomInt(100);
    }

    public static File getPathTempFile(String path) {
        File file = new File(getTempFileName(path));
        while (file.exists()) {
            file = new File(getTempFileName(path));
        }
        return file;
    }

    public static File getPathTempFile(String path, String suffix) {
        File file = new File(getTempFileName(path) + suffix);
        while (file.exists()) {
            file = new File(getTempFileName(path) + suffix);
        }
        return file;
    }

    public static File getPathTempDirectory(String path) {
        File file = new File(getTempFileName(path) + File.separator);
        while (file.exists()) {
            file = new File(getTempFileName(path) + File.separator);
        }
        file.mkdirs();
        return file;
    }

    public static String showFileSize(long size) {
        double kb = size * 1.0d / 1024;
        if (kb < 1024) {
            return DoubleTools.scale3(kb) + " KB";
        } else {
            double mb = kb / 1024;
            if (mb < 1024) {
                return DoubleTools.scale3(mb) + " MB";
            } else {
                double gb = mb / 1024;
                return DoubleTools.scale3(gb) + " GB";
            }
        }
    }

    public static int compareFilename(File f1, File f2) {
        if (f1 == null) {
            return f2 == null ? 0 : -1;
        }
        if (f2 == null) {
            return 1;
        }
        if (f1.isFile() && f2.isFile() && f1.getParent().equals(f2.getParent())) {
            return StringTools.compareWithNumber(f1.getName(), f2.getName());
        } else {
            Comparator<Object> compare = Collator.getInstance(Locale.getDefault());
            return compare.compare(f1.getAbsolutePath(), f2.getAbsolutePath());
        }
    }

    public static void sortFiles(List<File> files, FileSortMode sortMode) {
        if (files == null || files.isEmpty() || sortMode == null) {
            return;
        }
        try {
            switch (sortMode) {
                case ModifyTimeDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f2.lastModified() - f1.lastModified();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;

                case ModifyTimeAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f1.lastModified() - f2.lastModified();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;

                case NameDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return compareFilename(f2, f1);
                        }
                    });
                    break;

                case NameAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return compareFilename(f1, f2);
                        }
                    });
                    break;

                case CreateTimeDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long t1 = FileTools.createTime(f1.getAbsolutePath());
                            long t2 = FileTools.createTime(f2.getAbsolutePath());
                            long diff = t2 - t1;
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;

                case CreateTimeAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long t1 = FileTools.createTime(f1.getAbsolutePath());
                            long t2 = FileTools.createTime(f2.getAbsolutePath());
                            long diff = t1 - t2;
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;

                case SizeDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f2.length() - f1.length();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;

                case SizeAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f1.length() - f2.length();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;

                case FormatDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return FileTools.getFileSuffix(f2).compareTo(FileTools.getFileSuffix(f1));
                        }
                    });
                    break;

                case FormatAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return FileTools.getFileSuffix(f1).compareTo(FileTools.getFileSuffix(f2));
                        }
                    });
                    break;

            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static List<File> sortFiles(File path, FileSortMode sortMode) {
        if (path == null || !path.isDirectory()) {
            return null;
        }
        File[] pathFiles = path.listFiles();
        if (pathFiles == null || pathFiles.length == 0) {
            return null;
        }
        List<File> files = new ArrayList<>();
        for (File file : pathFiles) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        if (files.isEmpty()) {
            return null;
        }
        sortFiles(files, sortMode);
        return files;
    }

    public static void sortFileInformations(List<FileInformation> files, FileSortMode sortMode) {
        if (files == null || files.isEmpty() || sortMode == null) {
            return;
        }
        switch (sortMode) {
            case ModifyTimeDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f2.getFile().lastModified() - f1.getFile().lastModified());
                    }
                });
                break;

            case ModifyTimeAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f1.getFile().lastModified() - f2.getFile().lastModified());
                    }
                });
                break;

            case NameDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return compareFilename(f2.getFile(), f1.getFile());
                    }
                });
                break;

            case NameAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return compareFilename(f1.getFile(), f2.getFile());
                    }
                });
                break;

            case CreateTimeDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        long t1 = FileTools.createTime(f1.getFile().getAbsolutePath());
                        long t2 = FileTools.createTime(f2.getFile().getAbsolutePath());
                        return (int) (t2 - t1);
                    }
                });
                break;

            case CreateTimeAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        long t1 = FileTools.createTime(f1.getFile().getAbsolutePath());
                        long t2 = FileTools.createTime(f2.getFile().getAbsolutePath());
                        return (int) (t1 - t2);
                    }
                });
                break;

            case SizeDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f2.getFile().length() - f1.getFile().length());
                    }
                });
                break;

            case SizeAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f1.getFile().length() - f2.getFile().length());
                    }
                });
                break;

            case FormatDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return FileTools.getFileSuffix(f2.getFile()).compareTo(FileTools.getFileSuffix(f1.getFile()));
                    }
                });
                break;

            case FormatAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return FileTools.getFileSuffix(f1.getFile()).compareTo(FileTools.getFileSuffix(f2.getFile()));
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

    public static boolean copyFile(String sourceFile, String targetFile) {
        return copyFile(new File(sourceFile), new File(targetFile));
    }

    public static boolean copyFile(File sourceFile, File targetFile) {
        return copyFile(sourceFile, targetFile, false, true);
    }

    public static boolean copyFile(File sourceFile, File targetFile,
            boolean isCanReplace, boolean isCopyAttrinutes) {
        try {
            if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                return false;
            }
            if (!targetFile.exists()) {
                if (isCopyAttrinutes) {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()));
                }
            } else if (!isCanReplace || targetFile.isDirectory()) {
                return false;
            } else if (isCopyAttrinutes) {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static FileSynchronizeAttributes copyWholeDirectory(File sourcePath, File targetPath) {
        FileSynchronizeAttributes attr = new FileSynchronizeAttributes();
        copyWholeDirectory(sourcePath, targetPath, attr);
        return attr;
    }

    public static boolean copyWholeDirectory(File sourcePath, File targetPath,
            FileSynchronizeAttributes attr) {
        return copyWholeDirectory(sourcePath, targetPath, attr, true);
    }

    public static boolean copyWholeDirectory(File sourcePath, File targetPath,
            FileSynchronizeAttributes attr, boolean clearTarget) {
        try {
            if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
                return false;
            }
            if (attr == null) {
                attr = new FileSynchronizeAttributes();
            }
            if (targetPath.exists()) {
                if (clearTarget && !deleteDir(targetPath)) {
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

    public static boolean copyFile(File sourceFile, File targetFile,
            FileSynchronizeAttributes attr) {
        if (attr == null) {
            attr = new FileSynchronizeAttributes();
        }
        return copyFile(sourceFile, targetFile, attr.isCanReplace(), attr.isCopyAttrinutes());
    }

    public static boolean rename(File sourceFile, File targetFile) {
        try {
            if (sourceFile == null || !sourceFile.exists() || targetFile == null) {
                return false;
            }
            if (!delete(targetFile)) {
                return false;
            }
            System.gc();
            FileUtils.moveFile(sourceFile, targetFile);
//            Files.move(sourceFile.toPath(), targetFile.toPath(),
//                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

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

    public static boolean clearDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
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

    public static void deleteNestedDir(File sourceDir) {
        try {
            if (sourceDir == null || !sourceDir.exists() || !sourceDir.isDirectory()) {
                return;
            }
            System.gc();
            File targetTmpDir = getTempDirectory();
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

    // Return files number and total length
    public static long[] countDirectorySize(File dir, boolean countSubdir) {
        long[] size = new long[2];
        try {
            if (dir == null) {
                return size;
            }
            if (dir.isFile()) {
                size[0] = 1;
                size[1] = dir.length();

            } else if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                size[0] = 0;
                size[1] = 0;
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            size[0]++;
                            size[1] += file.length();
                        } else if (file.isDirectory()) {
                            if (countSubdir) {
                                long[] fsize = countDirectorySize(file, countSubdir);
                                size[0] += fsize[0];
                                size[1] += fsize[1];
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return size;
    }

    public static List<File> allFiles(File file) {
        if (file == null) {
            return null;
        }
        List<File> files = new ArrayList<>();
        if (file.isFile()) {
            files.add(file);
        } else if (file.isDirectory()) {
            File[] dirFiles = file.listFiles();
            if (dirFiles != null) {
                for (File dirFile : dirFiles) {
                    files.addAll(allFiles(dirFile));
                }
            }
        }
        return files;
    }

    public static List<File> splitFileByFilesNumber(File file, String filename, long filesNumber) {
        try {
            if (file == null || filesNumber <= 0) {
                return null;
            }
            long bytesNumber = file.length() / filesNumber;
            List<File> splittedFiles = new ArrayList<>();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                String newFilename;
                int digit = (filesNumber + "").length();
                byte[] buf = new byte[(int) bytesNumber];
                int bufLen, fileIndex = 1, startIndex = 0, endIndex = 0;
                while ((fileIndex < filesNumber) && (bufLen = inputStream.read(buf)) > 0) {
                    endIndex += bufLen;
                    newFilename = filename + "-cut-f" + StringTools.fillLeftZero(fileIndex, digit)
                            + "-b" + (startIndex + 1) + "-b" + endIndex;
                    try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFilename))) {
                        if (bytesNumber > bufLen) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        outputStream.write(buf);
                    }
                    splittedFiles.add(new File(newFilename));
                    fileIndex++;
                    startIndex = endIndex;
                }
                buf = new byte[(int) (file.length() - endIndex)];
                bufLen = inputStream.read(buf);
                if (bufLen > 0) {
                    endIndex += bufLen;
                    newFilename = filename + "-cut-f" + StringTools.fillLeftZero(fileIndex, digit)
                            + "-b" + (startIndex + 1) + "-b" + endIndex;
                    try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFilename))) {
                        outputStream.write(buf);
                    }
                    splittedFiles.add(new File(newFilename));
                }
            }
            return splittedFiles;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static List<File> splitFileByBytesNumber(File file,
            String filename, long bytesNumber) {
        try {
            if (file == null || bytesNumber <= 0) {
                return null;
            }
            List<File> splittedFiles = new ArrayList<>();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                String newFilename;
                long fnumber = file.length() / bytesNumber;
                if (file.length() % bytesNumber > 0) {
                    fnumber++;
                }
                int digit = (fnumber + "").length();
                byte[] buf = new byte[(int) bytesNumber];
                int bufLen, fileIndex = 1, startIndex = 0, endIndex = 0;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    endIndex += bufLen;
                    newFilename = filename + "-cut-f" + StringTools.fillLeftZero(fileIndex, digit)
                            + "-b" + (startIndex + 1) + "-b" + endIndex;
                    try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFilename))) {
                        if (bytesNumber > bufLen) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        outputStream.write(buf);
                    }
                    splittedFiles.add(new File(newFilename));
                    fileIndex++;
                    startIndex = endIndex;
                }
            }
            return splittedFiles;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static List<File> splitFileByStartEndList(File file,
            String filename, List<Long> startEndList) {
        try {
            if (file == null || startEndList == null
                    || startEndList.isEmpty() || startEndList.size() % 2 > 0) {
                return null;
            }
            List<File> splittedFiles = new ArrayList<>();
            for (int i = 0; i < startEndList.size(); i += 2) {
                File f = cutFile(file, filename, startEndList.get(i), startEndList.get(i + 1));
                if (f != null) {
                    splittedFiles.add(f);
                }
            }
            return splittedFiles;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    // 1-based start, that is: from (start - 1) to ( end - 1) actually
    public static File cutFile(File file,
            String filename, long startIndex, long endIndex) {
        try {
            if (file == null || startIndex < 1 || startIndex > endIndex) {
                return null;
            }
            File tempFile = FileTools.getTempFile();
            String newFilename = filename + "-cut-b" + startIndex + "-b" + endIndex;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                if (startIndex > 1) {
                    inputStream.skip(startIndex - 1);
                }
                int cutLength = (int) (endIndex - startIndex + 1);
                byte[] buf = new byte[cutLength];
                int bufLen;
                bufLen = inputStream.read(buf);
                if (bufLen <= 0) {
                    return null;
                }
                try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                    if (cutLength > bufLen) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                        newFilename = filename + "-cut-b" + startIndex + "-b" + bufLen;
                    }
                    outputStream.write(buf);
                }
            }
            File actualFile = new File(newFilename);

            if (FileTools.rename(tempFile, actualFile)) {
                return actualFile;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static boolean mergeFiles(File file1, File file2, File targetFile) {
        try {
            List<File> files = new ArrayList();
            files.add(file1);
            files.add(file2);
            return mergeFiles(files, targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean mergeFiles(List<File> files, File targetFile) {
        if (files == null || files.isEmpty() || targetFile == null) {
            return false;
        }
        try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            byte[] buf = new byte[CommonValues.IOBufferLength];
            int bufLen;
            for (File file : files) {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                    while ((bufLen = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, bufLen);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
        return true;
    }

    public static byte[] readBytes(File file) {
        byte[] data = null;
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            int bufSize = (int) file.length();
            data = new byte[bufSize];
            int readLen = inputStream.read(data);
            if (readLen > 0 && readLen < bufSize) {
                data = ByteTools.subBytes(data, 0, readLen);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return data;
    }

    public static byte[] readBytes(File file, long offset, int length) {
        if (file == null || offset < 0 || length <= 0) {
            return null;
        }
        byte[] data;
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            data = new byte[length];
            inputStream.skip(offset);
            int readLen = inputStream.read(data);
            if (readLen > 0 && readLen < length) {
                data = ByteTools.subBytes(data, 0, readLen);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
        return data;
    }

    public static Charset charset(File file) {
        try {
            TextEditInformation info = new TextEditInformation(file);
            if (TextTools.checkCharset(info)) {
                return info.getCharset();
            } else {
                return Charset.forName("utf-8");
            }
        } catch (Exception e) {
            return Charset.forName("utf-8");
        }
    }

    public static boolean isUTF8(File file) {
        Charset charset = charset(file);
        return charset.equals(Charset.forName("utf-8"));
    }

    public static String readTexts(File file) {
        return readTexts(file, charset(file));
    }

    public static String readTexts(File file, Charset charset) {
        StringBuilder s = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                s.append(line).append(System.lineSeparator());
            }
        } catch (Exception e) {
            return null;
        }
        return s.toString();
    }

    public static File writeFile(File file, String data) {
        return writeFile(file, data, Charset.forName("utf-8"));
//        if (file.exists()) {
//            return writeFile(file, data, charset(file));
//        } else {
//            return writeFile(file, data, Charset.forName("utf-8"));
//        }
    }

    public static File writeFile(File file, String data, Charset charset) {
        if (file == null || data == null) {
            return null;
        }
        try ( FileWriter writer = new FileWriter(file, charset != null ? charset : Charset.forName("utf-8"))) {
            writer.write(data);
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
        return file;
    }

    public static File writeFile(String data) {
        return writeFile(getTempFile(), data);
    }

    public static boolean writeFile(File file, byte[] data) {
        if (file == null || data == null) {
            return false;
        }
        try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
        return true;
    }

    public static boolean same(File file1, File file2) {
        return Arrays.equals(SystemTools.SHA1(file1), SystemTools.SHA1(file2));

    }

    public static int bufSize(File file, int memPart) {
        Runtime r = Runtime.getRuntime();
        long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
        long min = Math.min(file.length(), availableMem / memPart);
        return min > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) min;
    }

    public static File removeBOM(File file) {
        try {
            String setName = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] header = new byte[4];
                int readLen;
                if ((readLen = inputStream.read(header, 0, 4)) > 0) {
                    header = ByteTools.subBytes(header, 0, readLen);
                    setName = TextTools.checkCharsetByBom(header);
                    if (setName == null) {
                        return file;
                    }
                }
            }
            File tmpFile = getTempFile();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                int bomSize = TextTools.bomSize(setName);
                inputStream.skip(bomSize);
                int readLen;
                byte[] buf = new byte[bufSize(file, 16)];
                while ((readLen = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, readLen);
                }
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

}
