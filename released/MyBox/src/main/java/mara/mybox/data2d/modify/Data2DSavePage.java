package mara.mybox.data2d.modify;

import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSavePage extends Data2DModify {

    public static Data2DSavePage save(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        Data2DSavePage operate = new Data2DSavePage();
        if (!operate.setSourceData(data)) {
            return null;
        }
        operate.initWriter();
        return operate;

    }

    @Override
    public boolean handleRow() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = sourceRow;
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

}
