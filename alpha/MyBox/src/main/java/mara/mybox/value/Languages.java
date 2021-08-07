package mara.mybox.value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import mara.mybox.data.UserLanguage;
import mara.mybox.data.UserTableLanguage;
import static mara.mybox.value.AppVariables.currentBundle;
import static mara.mybox.value.AppVariables.currentTableBundle;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class Languages {

    public static final Locale LocaleZhCN = new Locale("zh", "CN");
    public static final Locale LocaleEn = new Locale("en");
    //    public static final Locale LocaleFrFR = new Locale("fr", "FR");
    //    public static final Locale LocaleEsES = new Locale("es", "ES");
    //    public static final Locale LocaleRuRU = new Locale("ru", "RU");

    public static final ResourceBundle BundleZhCN = ResourceBundle.getBundle("bundles/Messages", LocaleZhCN);
    public static final ResourceBundle BundleEn = ResourceBundle.getBundle("bundles/Messages", LocaleEn);

    public static final ResourceBundle TableBundleZhCN = ResourceBundle.getBundle("bundles/TableMessages", LocaleZhCN);
    public static final ResourceBundle TableBundleEn = ResourceBundle.getBundle("bundles/TableMessages", LocaleEn);
    //    public static final ResourceBundle BundleFrFR = ResourceBundle.getBundle("bundles/Messages", LocaleFrFR);
    //    public static final ResourceBundle BundleEsES = ResourceBundle.getBundle("bundles/Messages", LocaleEsES);
    //    public static final ResourceBundle BundleRuRU = ResourceBundle.getBundle("bundles/Messages", LocaleRuRU);

    public static void setLanguage(String lang) {
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        UserConfig.setString("language", lang);
        currentBundle = getBundle(lang);
        currentTableBundle = getTableBundle(lang);
    }

    public static String getLanguage() {
        String lang = UserConfig.getString("language", Locale.getDefault().getLanguage());
        return lang != null ? lang.toLowerCase() : Locale.getDefault().getLanguage().toLowerCase();
    }

    public static boolean isChinese() {
        return getLanguage().startsWith("zh");
    }

    public static ResourceBundle getBundle() {
        if (currentBundle == null) {
            currentBundle = getBundle(getLanguage());
        }
        return currentBundle;
    }

    public static ResourceBundle getBundle(String language) {
        String lang = language;
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        ResourceBundle bundle = null;
        if (lang.startsWith("zh")) {
            bundle = BundleZhCN;
        } else if (lang.startsWith("en")) {
            bundle = BundleEn;
        } else {
            try {
                bundle = new UserLanguage(lang);
            } catch (Exception e) {
            }
            if (bundle == null) {
                bundle = BundleEn;
            }
        }
        return bundle;
    }

    public static String message(String language, String msg) {
        try {
            if (msg.isBlank()) {
                return msg;
            }
            ResourceBundle bundle = getBundle(language);
            return bundle.getString(msg);
        } catch (Exception e) {
            return msg;
        }
    }

    public static String message(String msg) {
        try {
            return getBundle().getString(msg);
        } catch (Exception e) {
            return msg;
        }
    }

    public static ResourceBundle getTableBundle() {
        if (currentTableBundle == null) {
            currentTableBundle = getTableBundle(getLanguage());
        }
        return currentTableBundle;
    }

    public static ResourceBundle getTableBundle(String language) {
        String lang = language;
        if (lang == null) {
            lang = Locale.getDefault().getLanguage().toLowerCase();
        }
        ResourceBundle bundle = null;
        if (lang.startsWith("zh")) {
            bundle = TableBundleZhCN;
        } else if (lang.startsWith("en")) {
            bundle = TableBundleEn;
        } else {
            try {
                bundle = new UserTableLanguage(lang);
            } catch (Exception e) {
            }
            if (bundle == null) {
                bundle = TableBundleEn;
            }
        }
        return bundle;
    }

    public static String tableMessage(String language, String msg) {
        try {
            String s = msg.toLowerCase();
            ResourceBundle bundle = getTableBundle(language);
            return bundle.getString(s);
        } catch (Exception e) {
            return msg;
        }
    }

    public static String tableMessage(String msg) {
        try {
            return getTableBundle().getString(msg);
        } catch (Exception e) {
            return msg;
        }
    }

    public static List<String> userLanguages() {
        List<String> languages = new ArrayList<>();
        try {
            File[] files = AppVariables.MyBoxLanguagesPath.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }
                    String name = file.getName();
                    if (name.endsWith(".properties")) {
                        name = name.substring(0, name.length() - ".properties".length());
                    }
                    if (name.startsWith("Messages_")) {
                        name = name.substring("Messages_".length());
                    } else if (name.startsWith("TableMessages_")) {
                        name = name.substring("TableMessages_".length());
                    }
                    if (!languages.contains(name)) {
                        languages.add(name);
                    }
                }
            }
        } catch (Exception e) {
        }
        return languages;
    }

    public static File tableLanguageFile(String langName) {
        return new File(AppVariables.MyBoxLanguagesPath + File.separator + "TableMessages_" + langName + ".properties");
    }

    public static File interfaceLanguageFile(String langName) {
        return new File(AppVariables.MyBoxLanguagesPath + File.separator + "Messages_" + langName + ".properties");
    }

}
