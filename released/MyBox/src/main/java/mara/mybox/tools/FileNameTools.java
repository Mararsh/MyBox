package mara.mybox.tools;

import java.io.File;
import java.text.Collator;
import java.util.Locale;
import java.util.regex.Pattern;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileNameTools {

    public static String filter(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        Pattern pattern = Pattern.compile(AppValues.FileNameSpecialChars);
        return pattern.matcher(name).replaceAll("_");
    }

    public static String name(String filename) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = fname.lastIndexOf(File.separator);
        if (pos >= 0) {
            fname = (pos < fname.length() - 1) ? fname.substring(pos + 1) : "";
        }
        return fname;
    }

    public static String append(String filename, String append) {
        if (filename == null) {
            return null;
        }
        if (append == null || append.isEmpty()) {
            return filename;
        }
        String path;
        String name;
        int pos = filename.lastIndexOf(File.separator);
        if (pos >= 0) {
            path = filename.substring(0, pos + 1);
            name = pos < filename.length() - 1 ? filename.substring(pos + 1) : "";
        } else {
            path = "";
            name = filename;
        }
        pos = name.lastIndexOf('.');
        if (pos >= 0) {
            return path + name.substring(0, pos) + append + name.substring(pos);
        } else {
            return path + name + append;
        }
    }

    public static String prefix(String filename) {
        if (filename == null) {
            return null;
        }
        String name = name(filename);
        int pos = name.lastIndexOf('.');
        if (pos >= 0) {
            name = name.substring(0, pos);
        }
        return name;
    }

    // not include "."
    public static String suffix(String filename) {
        if (filename == null || filename.endsWith(File.separator)) {
            return null;
        }
        String name = name(filename);
        int pos = name.lastIndexOf('.');
        return (pos >= 0 && pos < name.length() - 1) ? name.substring(pos + 1) : "";
    }

    public static String replaceSuffix(String file, String newSuffix) {
        if (file == null || newSuffix == null) {
            return null;
        }
        String path;
        String name;
        int pos = file.lastIndexOf(File.separator);
        if (pos >= 0) {
            path = file.substring(0, pos + 1);
            name = pos < file.length() - 1 ? file.substring(pos + 1) : "";
        } else {
            path = "";
            name = file;
        }
        pos = name.lastIndexOf('.');
        if (pos >= 0) {
            return path + name.substring(0, pos + 1) + newSuffix;
        } else {
            return path + name + "." + newSuffix;
        }
    }

    public static int compareName(File f1, File f2) {
        try {
            if (f1 == null) {
                return f2 == null ? 0 : -1;
            }
            if (f2 == null) {
                return 1;
            }
            if (f1.isFile() && f2.isFile() && f1.getParent().equals(f2.getParent())) {
                return StringTools.compareWithNumber(f1.getName(), f2.getName());
            } else {
                Collator compare = Collator.getInstance(Locale.getDefault());
                return compare.compare(f1.getAbsolutePath(), f2.getAbsolutePath());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return 1;
        }
    }

}
