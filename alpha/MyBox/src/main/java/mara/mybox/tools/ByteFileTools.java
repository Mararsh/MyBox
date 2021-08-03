package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class ByteFileTools {

    // Can not handle file larger than 2g
    public static byte[] readBytes(File file) {
        byte[] data = null;
        try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
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
        try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
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

    public static boolean writeFile(File file, byte[] data) {
        if (file == null || data == null) {
            return false;
        }
        file.getParentFile().mkdirs();
        try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
        return true;
    }

    public static boolean mergeBytesFiles(File file1, File file2, File targetFile) {
        try {
            List<File> files = new ArrayList();
            files.add(file1);
            files.add(file2);
            return mergeBytesFiles(files, targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean mergeBytesFiles(List<File> files, File targetFile) {
        if (files == null || files.isEmpty() || targetFile == null) {
            return false;
        }
        targetFile.getParentFile().mkdirs();
        try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            byte[] buf = new byte[AppValues.IOBufferLength];
            int bufLen;
            for (File file : files) {
                try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
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

}
