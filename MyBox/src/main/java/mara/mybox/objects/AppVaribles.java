package mara.mybox.objects;

import java.io.File;
import java.util.ResourceBundle;
import static mara.mybox.objects.CommonValues.BundleEnUS;
import static mara.mybox.objects.CommonValues.BundleEsES;
import static mara.mybox.objects.CommonValues.BundleFrFR;
import static mara.mybox.objects.CommonValues.BundleRuRU;
import static mara.mybox.objects.CommonValues.BundleZhCN;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:59:49

 * @Description
 * @License Apache License Version 2.0
 */
public class AppVaribles {

    public static ResourceBundle CurrentBundle = CommonValues.BundleDefault;
    public static String LastPath;
    public static File configFile;

    public static String getMessage(String thestr) {
        try {
            return CurrentBundle.getString(thestr);
        } catch (Exception e) {
            return thestr;
        }
    }

    public static String getMessage(String language, String thestr) {
        try {
            if (thestr.trim().isEmpty()) {
                return thestr;
            }
            String value = thestr;
            switch (language.toLowerCase()) {
                case "zh":
                case "zh_cn":
                    value = BundleZhCN.getString(thestr);
                    break;
                case "en":
                case "en_us":
                    value = BundleEnUS.getString(thestr);
                    break;
                case "fr":
                case "fr_fr":
                    value = BundleFrFR.getString(thestr);
                    break;
                case "es":
                case "es_es":
                    value = BundleEsES.getString(thestr);
                    break;
                case "ru":
                case "ru_ru":
                    value = BundleRuRU.getString(thestr);
                    break;
            }
//            logger.debug(language + " " + thestr + " " + value);
            return value;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return thestr;
        }
    }

}
