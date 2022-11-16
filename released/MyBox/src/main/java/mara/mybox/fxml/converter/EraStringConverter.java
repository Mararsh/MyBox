package mara.mybox.fxml.converter;

import javafx.util.StringConverter;
import mara.mybox.data.Era;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-25
 * @License Apache License Version 2.0
 */
public class EraStringConverter extends StringConverter<Era> {

    @Override
    public Era fromString(String value) {
        try {
            if (value == null) {
                return (null);
            }
            value = value.trim();
            if (value.length() < 1) {
                return (null);
            }
            return new Era(value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString(Era value) {
        return DateTools.textEra(value);
    }

}
