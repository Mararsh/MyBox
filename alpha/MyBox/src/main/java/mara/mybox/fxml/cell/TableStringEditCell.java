package mara.mybox.fxml.cell;

import javafx.util.converter.DefaultStringConverter;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableStringEditCell<S> extends TableAutoCommitCell<S, String> {

    public TableStringEditCell() {
        super(new DefaultStringConverter());
    }

    protected String getCellValue() {
        return getItem();
    }

    protected boolean setCellValue(String inValue) {
        String value = inValue == null ? null : inValue.replaceAll("\\\\n", "\n");
        boolean changed = changed(value);
        commit(value, valid(value), changed);
        return changed;
    }

    @Override
    public void commitEdit(String inValue) {
        try {
            clearEditor();
            setCellValue(inValue);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
