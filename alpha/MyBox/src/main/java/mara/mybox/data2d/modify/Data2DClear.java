package mara.mybox.data2d.modify;

import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public class Data2DClear extends Data2DModify {

    public static Data2DClear create(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        Data2DClear operate = new Data2DClear();
        if (!operate.setSourceData(data)) {
            return null;
        }
        operate.initWriter();
        return operate;
    }

    @Override
    public boolean go() {
        handledCount = sourceData.getRowsNumber();
        return true;
    }

}
