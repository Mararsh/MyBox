package mara.mybox.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.web.WebEngine;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @Description
 * @License Apache License Version 2.0
 */
public class NetworkTools {

    public static Map<String, String> readCookie(WebEngine webEngine) {
        try {
            String s = (String) webEngine.executeScript("document.cookie;");
            String[] vs = s.split(";");
            Map<String, String> m = new HashMap<>();
            for (String v : vs) {
                logger.debug(v);
                String[] vv = v.split("=");
                if (vv.length < 2) {
                    continue;
                }
                m.put(vv[0].trim(), vv[1].trim());
            }
            logger.debug(m);
            return m;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static boolean checkWeiboPassport() {
        String passport;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            passport = System.getProperty("user.home")
                    + "/AppData/Roaming/mara.mybox.MainApp/webview/localstorage/https_passport.weibo.com_0.localstorage";

        } else if (os.contains("linux")) {
            passport = System.getProperty("user.home")
                    + "/.mara.mybox.MainApp/webview/localstorage/https_passport.weibo.com_0.localstorage";

        } else if (os.contains("mac")) {
            return AppVaribles.getUserConfigBoolean("WeiboPassportChecked", false);

        } else {
            return AppVaribles.getUserConfigBoolean("WeiboPassportChecked", false);
        }
        return new File(passport).exists();
    }

    public static boolean isOtherPlatforms() {
        String p = System.getProperty("os.name").toLowerCase();
        logger.debug(p);
        return !p.contains("windows") && !p.contains("linux");
    }

}
