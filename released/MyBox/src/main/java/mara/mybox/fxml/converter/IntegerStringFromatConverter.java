package mara.mybox.fxml.converter;

import javafx.util.StringConverter;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2021-11-12
 * @License Apache License Version 2.0
 */
public class IntegerStringFromatConverter extends StringConverter<Integer> {

    @Override
    public Integer fromString(String value) {
        try {
            return Integer.parseInt(value.trim().replaceAll(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(Integer value) {
        try {
            return StringTools.format(value);
        } catch (Exception e) {
            return "";
        }
    }
}
