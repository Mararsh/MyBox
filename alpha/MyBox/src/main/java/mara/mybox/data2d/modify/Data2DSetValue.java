package mara.mybox.data2d.modify;

import java.util.List;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public class Data2DSetValue extends Data2DModify {

    public static Data2DSetValue create(Data2D_Edit data, SetValue setValue) {
        if (data == null || setValue == null) {
            return null;
        }
        Data2DSetValue operate = new Data2DSetValue();
        if (!operate.setSourceData(data)
                || !operate.initSetValue(setValue)) {
            return null;
        }
        operate.initWriter();
        return operate;
    }

    @Override
    public void handleRow(List<String> row, long index) {
        setValue(row, index);
    }

}
