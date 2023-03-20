package mara.mybox.fxml.converter;

import javafx.util.StringConverter;

/**
 * @Author Mara
 * @CreateDate 2021-11-12
 * @License Apache License Version 2.0
 */
public class DoubleStringFromatConverter extends StringConverter<Double> {

    @Override
    public Double fromString(String value) {
        try {
            return Double.parseDouble(value.trim().replaceAll(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(Double value) {
        try {
            return value.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
