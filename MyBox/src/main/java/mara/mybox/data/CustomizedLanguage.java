package mara.mybox.data;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-27
 * @License Apache License Version 2.0
 */
public class CustomizedLanguage extends ResourceBundle {

    protected Map<String, String> items;

    public CustomizedLanguage(String name) throws Exception {
        File file = new File(AppVariables.MyBoxLanguagesPath + File.separator + name);
        if (!file.exists()) {
            throw new Exception("InvalidValue");
        }
        items = ConfigTools.readValues(file);
    }

    public CustomizedLanguage(Map<String, String> items) {
        this.items = items;
    }

    @Override
    public Object handleGetObject(String key) {
        String value = null;
        if (items != null) {
            value = items.get(key);
        }
        if (value == null) {
            value = CommonValues.BundleEn.getString(key);
        }
        if (value == null) {
            value = key;
        }
        return value;
    }

    @Override
    public Enumeration<String> getKeys() {
        return CommonValues.BundleEn.getKeys();
    }

    @Override
    protected Set<String> handleKeySet() {
        return CommonValues.BundleEn.keySet();
    }
}
