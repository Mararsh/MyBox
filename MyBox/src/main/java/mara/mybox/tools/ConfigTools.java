package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @Description Read data outside database
 * @License Apache License Version 2.0
 */
public class ConfigTools {

    /*
        MyBox config
     */
    public static String defaultDataPath() {
        return defaultDataPathFile().getAbsolutePath();
    }

    public static File defaultDataPathFile() {
        File defaultPath = new File(System.getProperty("user.home") + File.separator + "mybox");
        if (!defaultPath.exists()) {
            defaultPath.mkdirs();
        } else if (!defaultPath.isDirectory()) {
            defaultPath.delete();
            defaultPath.mkdirs();
        }
        return defaultPath;
    }

    public static File defaultConfigFile() {
        File defaultPath = defaultDataPathFile();
        File configFile = new File(defaultPath.getAbsolutePath() + File.separator + "MyBox.ini");
        return configFile;
    }

    public static Map<String, String> readValues() {
        return ConfigTools.readValues(AppVariables.MyboxConfigFile);
    }

    public static String readValue(String key) {
        return ConfigTools.readValue(AppVariables.MyboxConfigFile, key);
    }

    public static int readInt(String key, int defaultValue) {
        try {
            String v = ConfigTools.readValue(key);
            return Integer.parseInt(v);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean writeConfigValue(String key, String value) {
        try {
            if (!AppVariables.MyboxConfigFile.exists()) {
                if (!AppVariables.MyboxConfigFile.createNewFile()) {
                    return false;
                }
            }
            return writeValue(AppVariables.MyboxConfigFile, key, value);
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static List<String> languages() {
        List<String> languages = new ArrayList<>();
        try {
            File[] files = AppVariables.MyBoxLanguagesPath.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        languages.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
        }
        return languages;
    }

    /*
        General config
     */
    public static Map<String, String> readValues(File file) {
        Map<String, String> values = new HashMap<>();
        try {
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                Properties conf = new Properties();
                conf.load(in);
                for (String key : conf.stringPropertyNames()) {
                    values.put(key, conf.getProperty(key));
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return values;
    }

    public static String readValue(File file, String key) {
        try {
            if (!file.exists() || !file.isFile()) {
                return null;
            }
            String value;
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                Properties conf = new Properties();
                conf.load(in);
                value = conf.getProperty(key);
            }
            return value;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean writeConfigValue(File file, String key, String value) {
        try {
            Properties conf = new Properties();
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                conf.load(in);
            }
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                if (value == null) {
                    conf.remove(key);
                } else {
                    conf.setProperty(key, value);
                }
                conf.store(out, "Update " + key);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean writeValue(File file, String key, String value) {
        try {
            Properties conf = new Properties();
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                conf.load(in);
            }
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                if (value == null) {
                    conf.remove(key);
                } else {
                    conf.setProperty(key, value);
                }
                conf.store(out, "Update " + key);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static boolean writeValues(File file, Map<String, String> values) {
        try {
            if (file == null || values == null) {
                return false;
            }
            Properties conf = new Properties();
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                for (String key : values.keySet()) {
                    conf.setProperty(key, values.get(key));
                }
                conf.store(out, "Update ");
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

}
