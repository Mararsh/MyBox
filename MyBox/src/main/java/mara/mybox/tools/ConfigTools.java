package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @Description
 * @License Apache License Version 2.0
 */
public class ConfigTools {

    // Keep this method to migrate data from config file to derby db.
    public static Map<String, String> readConfigValuesFromFile() {
        try {
            Map<String, String> values = new HashMap<>();
            try (InputStream in = new BufferedInputStream(new FileInputStream(CommonValues.UserConfigFile))) {
                Properties conf = new Properties();
                conf.load(in);
                for (String key : conf.stringPropertyNames()) {
                    values.put(key, conf.getProperty(key));
                }
            }
            return values;
        } catch (Exception e) {
//            logger.error(e.toString());
            return null;
        }
    }

//    public static String readConfigValue(String key) {
//        try {
//            String value = null;
//            try (InputStream in = new BufferedInputStream(new FileInputStream(CommonValues.UserConfigFile))) {
//                Properties conf = new Properties();
//                conf.load(in);
//                value = conf.getProperty(key);
//            }
//            return value;
//        } catch (Exception e) {
//            logger.error(e.toString());
//            return null;
//        }
//    }
//    public static boolean writeConfigValue(String key, String value) {
//        try {
//            Properties conf = new Properties();
//            try (InputStream in = new FileInputStream(CommonValues.UserConfigFile)) {
//                conf.load(in);
//            }
//            try (OutputStream out = new FileOutputStream(CommonValues.UserConfigFile)) {
//                if (value == null) {
//                    conf.remove(key);
//                } else {
//                    conf.setProperty(key, value);
//                }
//                conf.store(out, "Update " + key);
//            }
//            return true;
//        } catch (Exception e) {
////            logger.error(e.toStsring());
//            return false;
//        }
//    }
}
