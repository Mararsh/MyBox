package mara.mybox.fxml.converter;

import javafx.util.StringConverter;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2021-11-12
 * @License Apache License Version 2.0
 */
public class DoubleStringFromatConverter extends StringConverter<Double> {

    @Override
    public Double fromString(String value) {
        try {
            return Double.valueOf(value.trim().replaceAll(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(Double value) {
        try {
            return StringTools.format(value);
        } catch (Exception e) {
            return "";
        }
    }
}
