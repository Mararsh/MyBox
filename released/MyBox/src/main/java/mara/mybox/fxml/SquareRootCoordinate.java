package mara.mybox.fxml;

import javafx.util.StringConverter;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2020-05-16
 * @License Apache License Version 2.0
 */
public class SquareRootCoordinate extends StringConverter<Number> {

    @Override
    public String toString(Number value) {
        double d = value.doubleValue();
        return StringTools.format(d * d);
    }

    @Override
    public Number fromString(String string) {
        return null;
    }
}
