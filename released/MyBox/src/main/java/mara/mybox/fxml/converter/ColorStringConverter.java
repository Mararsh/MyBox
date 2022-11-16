package mara.mybox.fxml.converter;

import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * @Author Mara
 * @CreateDate 2022-1-23
 * @License Apache License Version 2.0
 */
public class ColorStringConverter extends StringConverter<Color> {

    @Override
    public Color fromString(String value) {
        try {
            return Color.web(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(Color value) {
        try {
            return value.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
