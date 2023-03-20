package mara.mybox.fxml.converter;

import javafx.util.StringConverter;

/**
 * @Author Mara
 * @CreateDate 2021-11-12
 * @License Apache License Version 2.0
 */
public class ShortStringFromatConverter extends StringConverter<Short> {

    @Override
    public Short fromString(String value) {
        try {
            return Short.parseShort(value.trim().replaceAll(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(Short value) {
        try {
            return value.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
