package mara.mybox.value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import mara.mybox.data.UserLanguage;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.CurrentBundle;
import static mara.mybox.value.AppVariables.CurrentLangName;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class Languages {

    public static final Locale LocaleZhCN = Locale.of("zh", "CN");
    public static final Locale LocaleEn = Locale.of("en");
    //    public static final Locale LocaleFrFR = new Locale("fr", "FR");
    //    public static final Locale LocaleEsES = new Locale("es", "ES");
    //    public static final Locale LocaleRuRU = new Locale("ru", "RU");

    public static final ResourceBundle BundleZhCN = ResourceBundle.getBundle("bundles/Messages", LocaleZhCN);
    public static final ResourceBundle BundleEn = ResourceBundle.getBundle("bundles/Messages", LocaleEn);
    //    public static final ResourceBundle BundleFrFR = ResourceBundle.getBundle("bundles/Messages", LocaleFrFR);
    //    public static final ResourceBundle BundleEsES = ResourceBundle.getBundle("bundles/Messages", LocaleEsES);
    //    public static final ResourceBundle BundleRuRU = ResourceBundle.getBundle("bundles/Messages", LocaleRuRU);

    public static String sysDefaultLanguage() {
        try {
            return Locale.getDefault().getLanguage().toLowerCase();
        } catch (Exception e) {
            return "en";
        }
    }

    public static void setLanguage(String lang) {
        if (lang == null) {
            lang = sysDefaultLanguage();
        }
        CurrentBundle = getBundle(lang);
        try {
            if (CurrentBundle instanceof UserLanguage) {
                CurrentLangName = ((UserLanguage) CurrentBundle).getLanguage();
            } else {
                CurrentLangName = CurrentBundle.getLocale().getLanguage();
            }
            UserConfig.setString("language", CurrentLangName);
        } catch (Exception e) {
        }
    }

    public static String getLangName() {
        if (CurrentLangName == null) {
            String defaultLang = sysDefaultLanguage();
            String lang = null;
            try {
                lang = UserConfig.getString("language", defaultLang);
            } catch (Exception e) {
            }
            if (lang != null) {
                CurrentLangName = lang.toLowerCase();
            } else {
                CurrentLangName = defaultLang;
                try {
                    UserConfig.setString("language", CurrentLangName);
                } catch (Exception e) {
                }
            }
        }
        return CurrentLangName;
    }

    public static String embedLangName() {
        try {
            String lang = sysDefaultLanguage();
            if (isChinese(lang)) {
                return "zh";
            }
        } catch (Exception e) {
        }
        return "en";
    }

    public static String embedFileLang() {
        try {
            String lang = CurrentLangName;
            if (isChinese(lang)) {
                return "zh";
            } else if (isEnglish(lang)) {
                return "en";
            }
        } catch (Exception e) {
        }
        return embedLangName();
    }

    public static boolean isChinese() {
        return isChinese(getLangName());
    }

    public static boolean isChinese(String lang) {
        if (lang == null) {
            return false;
        }
        return lang.equals("zh") || lang.startsWith("zh_");
    }

    public static boolean isEnglish(String lang) {
        if (lang == null) {
            return false;
        }
        return lang.equals("en") || lang.startsWith("en_");
    }

    public static ResourceBundle getBundle() {
        if (CurrentBundle == null) {
            CurrentBundle = getBundle(getLangName());
        }
        return CurrentBundle;
    }

    public static ResourceBundle getBundle(String language) {
        String lang = language;
        if (lang == null) {
            lang = sysDefaultLanguage();
        }
        ResourceBundle bundle = tryBundle(lang);
        if (bundle == null) {
            lang = embedLangName();
            if ("zh".equals(lang)) {
                bundle = BundleZhCN;
            } else {
                bundle = BundleEn;
            }
        }
        return bundle;
    }

    private static ResourceBundle tryBundle(String language) {
        String lang = language;
        if (lang == null) {
            return null;
        }
        ResourceBundle bundle = null;
        if (lang.equals("en") || lang.startsWith("en_")) {
            bundle = BundleEn;
        } else if (lang.equals("zh") || lang.startsWith("zh_")) {
            bundle = BundleZhCN;
        } else {
            File file = interfaceLanguageFile(lang);
            if (file.exists()) {
                try {
                    bundle = new UserLanguage(lang);
                } catch (Exception e) {
                }
            }
        }
        return bundle;
    }

    public static ResourceBundle refreshBundle() {
        CurrentBundle = getBundle(getLangName());
        return CurrentBundle;
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

    public static String messageIgnoreFirstCase(String msg) {
        try {
            if (msg == null || msg.isBlank()) {
                return msg;
            }
            return getBundle().getString(msg);
        } catch (Exception e) {
            try {
                return getBundle().getString(msg.substring(0, 1).toUpperCase() + msg.substring(1, msg.length()));
            } catch (Exception ex) {
                return msg;
            }
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

    public static File interfaceLanguageFile(String langName) {
        return new File(AppVariables.MyBoxLanguagesPath + File.separator + "Messages_" + langName + ".properties");
    }

    public static Locale locale() {
        String lang = CurrentLangName;
        if (lang == null) {
            return Languages.LocaleEn;
        } else if (lang.equals("en") || lang.startsWith("en_")) {
            return Languages.LocaleEn;
        } else if (lang.equals("zh") || lang.startsWith("zh_")) {
            return Languages.LocaleZhCN;
        } else {
            try {
                return new Locale.Builder().setLanguage(lang).build();
            } catch (Exception e) {
                return Languages.LocaleEn;
            }
        }
    }

    public static boolean match(String matchTo, String s) {
        try {
            if (matchTo == null || s == null) {
                return false;
            }
            return message("en", matchTo).equals(s)
                    || message("zh", matchTo).equals(s)
                    || message(matchTo).equals(s)
                    || matchTo.equals(s);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean matchIgnoreCase(String matchTo, String s) {
        try {
            if (matchTo == null || s == null) {
                return false;
            }
            return message("en", matchTo).equalsIgnoreCase(s)
                    || message("zh", matchTo).equalsIgnoreCase(s)
                    || message(matchTo).equalsIgnoreCase(s)
                    || matchTo.equalsIgnoreCase(s);
        } catch (Exception e) {
            return false;
        }
    }

    public static void checkStatus() {
        MyBoxLog.console("LocaleEn: " + LocaleEn.getDisplayName());
        MyBoxLog.console("BundleEn: " + BundleEn.getBaseBundleName());
        MyBoxLog.console("LocaleZhCN: " + LocaleZhCN.getDisplayName());
        MyBoxLog.console("BundleZhCN: " + BundleZhCN.getBaseBundleName());

        MyBoxLog.console("CurrentLangName: " + CurrentLangName);
        MyBoxLog.console("CurrentBundle: " + CurrentBundle.getBaseBundleName());

        MyBoxLog.console("getLangName: " + getLangName());
        MyBoxLog.console("embedLangName: " + embedLangName());
        MyBoxLog.console("isChinese: " + isChinese());
        MyBoxLog.console("getBundle: " + getBundle().getBaseBundleName());
        MyBoxLog.console("sysDefaultLanguage();: " + sysDefaultLanguage());
        MyBoxLog.console("message(\"en\", \"FileInformation\"): " + message("en", "FileInformation"));
        MyBoxLog.console("message(\"zh\", \"FileInformation\"): " + message("zh", "FileInformation"));
        MyBoxLog.console("message( \"FileInformation\"): " + message("FileInformation"));
    }

}
