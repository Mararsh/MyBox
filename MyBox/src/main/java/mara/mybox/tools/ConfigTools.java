package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58

 * @Description
 * @License Apache License Version 2.0
 */
public class ConfigTools {

    private static final Logger logger = LogManager.getLogger();

    public static String getLastPath() {
        try {
            if (AppVaribles.LastPath != null) {
                return AppVaribles.LastPath;
            }
            AppVaribles.LastPath = readConfigValue("LastPath");
            if (AppVaribles.LastPath != null) {
                return AppVaribles.LastPath;
            }
            AppVaribles.LastPath = System.getProperty("user.home");
            return AppVaribles.LastPath;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean setLastPath(String path) {
        if (setConfigValue("LastPath", path)) {
            AppVaribles.LastPath = path;
            return true;
        } else {
            return false;
        }
    }

    public static String readConfigValue(String key) {
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(CommonValues.UserConfigFile));
            Properties conf = new Properties();
            conf.load(in);
            String value = conf.getProperty(key);
            in.close();
            return value;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean setConfigValue(String key, String value) {
        try {
            OutputStream out = new FileOutputStream(CommonValues.UserConfigFile);
            Properties conf = new Properties();
            conf.setProperty(key, value);
            conf.store(out, "Update " + key);
            out.close();
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }
}
