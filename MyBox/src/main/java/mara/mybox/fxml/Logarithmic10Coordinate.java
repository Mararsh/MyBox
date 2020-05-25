package mara.mybox.fxml;

import javafx.util.StringConverter;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2020-05-16
 * @License Apache License Version 2.0
 */
public class Logarithmic10Coordinate extends StringConverter<Number> {

    @Override
    public String toString(Number value) {
        return StringTools.format(Math.pow(10, value.doubleValue()));
    }

    @Override
    public Number fromString(String string) {
        return null;
    }
}
