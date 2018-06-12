/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

/**
 * @Author mara
 * @CreateDate 2018-6-2 11:01:45
 * @Version 1.0
 * @Description
 */
public class FileTools {

    public static String getUrlFile(String url) {
        if (url == null) {
            return null;
        }
        String f = url;
        f = f.replace("file:/", "");
        return f.replace("MyBox-1.0.jar!", "classes");
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
}
