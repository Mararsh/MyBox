/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import static mara.mybox.objects.CommonValues.UserFilePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author mara
 * @CreateDate 2018-6-2 11:01:45
 *
 * @Description
 */
public class FileTools {

    private static final Logger logger = LogManager.getLogger();

    public static long getFileCreateTime(String filename) {
        try {
            FileTime t = Files.readAttributes(Paths.get(filename), BasicFileAttributes.class).creationTime();
            return t.toMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getFilePath(String filename) {
        if (filename == null) {
            return null;
        }
        int pos = filename.lastIndexOf("/");
        if (pos < 0) {
            return "";
        }
        return filename.substring(0, pos);
    }

    public static String getFileName(String filename) {
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

    public static String getFilePrefix(String filename) {
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

    public static String getFileSuffix(String filename) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = filename.lastIndexOf(".");
        if (pos >= 0) {
            fname = fname.substring(pos + 1);
        }
        return fname;
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

    public static String showFileSize(long size) {
        String s = size + "";
        String t = "";
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--, count++) {
            if (count > 0 && (count % 3 == 0)) {
                t = "," + t;
            }
            t = s.charAt(i) + t;
        }
        return t;
    }
//            double size = (double) info.getFileSize();
//            if (info.getFileSize() > 1000 * 1000) {
//                FileSize.setText(ValueTools.roundDouble(size / (1000 * 1000)) + "GB");
//            } else if (info.getFileSize() > 1000) {
//                FileSize.setText(ValueTools.roundDouble(size / 1000) + "MB");
//            } else {
//                FileSize.setText(size + "KB");
//            }

}
