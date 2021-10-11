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

    public static String getName(final String filename) {
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

    public static String appendName(String file, String append) {
        if (file == null) {
            return null;
        }
        if (append == null || append.isEmpty()) {
            return file;
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
            return path + name.substring(0, pos) + append + name.substring(pos);
        } else {
            return path + name + append;
        }
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

    public static String getFilePrefix(File file) {
        try {
            return getFilePrefix(file.getName());
        } catch (Exception e) {
            return "";
        }
    }

    // filename may include path or not, it is decided by caller
    public static String getFilePrefix(String file) {
        if (file == null) {
            return null;
        }
        String path;
        String name;
        int pos = file.lastIndexOf(File.separator);
        if (pos >= 0) {
            path = file.substring(0, pos + 1);
            name = (pos < file.length() - 1) ? file.substring(pos + 1) : "";
        } else {
            path = "";
            name = file;
        }
        pos = name.lastIndexOf('.');
        return pos >= 0 ? path + name.substring(0, pos) : path + name;
    }

    public static String prefixFilter(File file) {
        try {
            return prefixFilter(file.getName());
        } catch (Exception e) {
            return "";
        }
    }

    public static String prefixFilter(String file) {
        String prefix = getFilePrefix(file);
        if (prefix == null || prefix.isBlank()) {
            return prefix;
        }
        Pattern pattern = Pattern.compile(AppValues.FileNameSpecialChars);
        return pattern.matcher(prefix).replaceAll("_");
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
        String name = getName(file);
        int pos = name.lastIndexOf('.');
        return (pos >= 0 && pos < name.length() - 1) ? name.substring(pos + 1) : "";
    }

    public static String replaceFileSuffix(String file, String newSuffix) {
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

    public static int compareFilename(File f1, File f2) {
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
