package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import mara.mybox.MyBox;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @Description Read data outside database
 * @License Apache License Version 2.0
 */
public class ConfigTools {

    public static File configFile() {
        File iniFile = new File(MyBox.defaultDataPath() + File.separator + "MyBox.ini");
        return iniFile;
    }

    public static Map<String, String> readConfigValues() {
        try {
            File iniFile = configFile();
            if (!iniFile.exists()) {
                return null;
            }
            Map<String, String> values = new HashMap<>();
            try ( InputStream in = new BufferedInputStream(new FileInputStream(iniFile))) {
                Properties conf = new Properties();
                conf.load(in);
                for (String key : conf.stringPropertyNames()) {
                    values.put(key, conf.getProperty(key));
                }
            }
            return values;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static String readConfigValue(String key) {
        try {
            File iniFile = configFile();
            if (!iniFile.exists()) {
                return null;
            }
            String value;
            try ( InputStream in = new BufferedInputStream(new FileInputStream(iniFile))) {
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

    public static boolean writeConfigValue(String key, String value) {
        try {
            File iniFile = configFile();
            if (!iniFile.exists()) {
                if (!iniFile.createNewFile()) {
                    return false;
                }
            }
            Properties conf = new Properties();
            try ( InputStream in = new FileInputStream(iniFile)) {
                conf.load(in);
            }
            try ( OutputStream out = new FileOutputStream(iniFile)) {
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
}
