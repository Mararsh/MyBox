package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.FileExtensions;
import org.apache.commons.io.FileUtils;

/**
 * @Author mara
 * @CreateDate 2018-6-2 11:01:45
 * @Description
 */
public class FileTools {

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

    public static String showFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else {
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
    }

    public static boolean isSupportedImage(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String suffix = FileNameTools.suffix(file.getName()).toLowerCase();
        return FileExtensions.SupportedImages.contains(suffix);
    }

    public static boolean rename(File sourceFile, File targetFile) {
        return rename(sourceFile, targetFile, false);
    }

    public static boolean rename(File sourceFile, File targetFile, boolean noEmpty) {
        try {
            if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()
                    || targetFile == null || sourceFile.equals(targetFile)) {
                return false;
            }
            if (noEmpty && sourceFile.length() == 0) {
                return false;
            }
            synchronized (sourceFile) {
                if (targetFile.exists() && !FileDeleteTools.delete(targetFile)) {
                    return false;
                }
                System.gc();
                FileUtils.moveFile(sourceFile, targetFile);
            }
//            targetFile.getParentFile().mkdirs();
//            Files.move(sourceFile.toPath(), targetFile.toPath(),
//                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, sourceFile + "    " + targetFile);
            return false;
        }
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

    public static boolean same(File file1, File file2) {
        return Arrays.equals(MessageDigestTools.SHA1(file1), MessageDigestTools.SHA1(file2));

    }

    public static int bufSize(File file, int memPart) {
        Runtime r = Runtime.getRuntime();
        long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
        long min = Math.min(file.length(), availableMem / memPart);
        return min > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) min;
    }

    public static boolean hasData(File file) {
        return file != null && file.exists() && file.isFile() && file.length() > 0;
    }

    public static boolean isEqualOrSubPath(String path1, String path2) {
        if (path1 == null || path1.isBlank() || path2 == null || path2.isBlank()) {
            return false;
        }
        String name1 = path1.endsWith(File.separator) ? path1 : (path1 + File.separator);
        String name2 = path1.endsWith(File.separator) ? path2 : (path2 + File.separator);
        return name1.equals(name2) || name1.startsWith(name2);
    }

    public static File removeBOM(File file) {
        if (!hasData(file)) {
            return file;
        }
        String bom = null;
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] header = new byte[4];
            int readLen;
            if ((readLen = inputStream.read(header, 0, 4)) > 0) {
                header = ByteTools.subBytes(header, 0, readLen);
                bom = TextTools.checkCharsetByBom(header);
                if (bom == null) {
                    return file;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            int bomSize = TextTools.bomSize(bom);
            inputStream.skip(bomSize);
            int readLen;
            byte[] buf = new byte[bufSize(file, 16)];
            while ((readLen = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, readLen);
            }
            outputStream.flush();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
        return tmpFile;
    }

    public static File javaIOTmpPath() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

}
