package mara.mybox.fxml.converter;

import java.time.LocalDate;
import javafx.util.StringConverter;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-25
 * @License Apache License Version 2.0
 */
public class LocalDateStringConverter extends StringConverter<LocalDate> {

    @Override
    public LocalDate fromString(String value) {
        return DateTools.stringToLocalDate(value);
    }

    @Override
    public String toString(LocalDate value) {
        return DateTools.localDateToString(value);
    }

}
