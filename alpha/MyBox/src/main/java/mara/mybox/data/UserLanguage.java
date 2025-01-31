package mara.mybox.data;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import mara.mybox.tools.ConfigTools;
import static mara.mybox.value.AppVariables.useChineseWhenBlankTranslation;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-12-27
 * @License Apache License Version 2.0
 */
public class UserLanguage extends ResourceBundle {

    protected Map<String, String> items;
    protected String language;

    public UserLanguage(String name) throws Exception {
        File file = Languages.interfaceLanguageFile(name);
        if (!file.exists()) {
            items = null;
            throw new Exception();
        } else {
            language = name;
            items = ConfigTools.readValues(file);
        }
    }

    public UserLanguage(Map<String, String> items) {
        this.items = items;
    }

    public boolean isValid() {
        return items != null;
    }

    @Override
    public Object handleGetObject(String key) {
        String value = null;
        if (items != null) {
            value = items.get(key);
        }
        if (value == null) {
            value = useChineseWhenBlankTranslation
                    ? Languages.BundleZhCN.getString(key)
                    : Languages.BundleEn.getString(key);
        }
        if (value == null) {
            value = key;
        }
        return value;
    }

    @Override
    protected Set<String> handleKeySet() {
        return Languages.BundleEn.keySet();
    }

    /*
        get/set
     */
    @Override
    public Enumeration<String> getKeys() {
        return Languages.BundleEn.getKeys();
    }

    public Map<String, String> getItems() {
        return items;
    }

    public UserLanguage setItems(Map<String, String> items) {
        this.items = items;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public UserLanguage setLanguage(String language) {
        this.language = language;
        return this;
    }

}
