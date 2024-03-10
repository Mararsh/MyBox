package mara.mybox.data2d.modify;

import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.operate.Data2DOperate;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public class Data2DClear extends Data2DOperate {

    public static Data2DClear create(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        Data2DClear operate = new Data2DClear();
        if (!operate.setSourceData(data)) {
            return null;
        }
        operate.addWriter(data.selfWriter());
        return operate;
    }

    @Override
    public void handleData() {
        count = sourceData.getDataSize();
    }

}
