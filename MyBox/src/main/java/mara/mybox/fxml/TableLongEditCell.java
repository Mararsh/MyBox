package mara.mybox.fxml;

import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableLongEditCell<T, P> extends TableAutoCommitCell<T, P> {

    private String getString() {
        return getItem() == null ? "" : getItem() + "";
    }

    private String getString(P v) {
        return getConverter().toString(v);
    }

    private P fromString(String v) {
        return getConverter().fromString(v);
    }
}
