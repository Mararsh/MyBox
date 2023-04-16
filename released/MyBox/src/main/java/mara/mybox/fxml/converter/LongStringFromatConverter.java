package mara.mybox.fxml.converter;

import javafx.util.StringConverter;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2021-11-12
 * @License Apache License Version 2.0
 */
public class LongStringFromatConverter extends StringConverter<Long> {

    @Override
    public Long fromString(String value) {
        try {
            return Long.parseLong(value.trim().replaceAll(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(Long value) {
        try {
            return StringTools.format(value);
        } catch (Exception e) {
            return "";
        }
    }
}
