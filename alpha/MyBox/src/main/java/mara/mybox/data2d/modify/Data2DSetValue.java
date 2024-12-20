package mara.mybox.data2d.modify;

import java.util.List;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Empty;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Null;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Skip;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Zero;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public class Data2DSetValue extends Data2DModify {

    public static Data2DSetValue create(Data2D_Edit data, SetValue value) {
        if (data == null || value == null) {
            return null;
        }
        Data2DSetValue operate = new Data2DSetValue();
        if (!operate.setSourceData(data)
                || !operate.initSetValue(value)) {
            return null;
        }
        operate.initWriter();
        return operate;
    }

    @Override
    public void handleRow(List<String> row, long index) {
        setValue(row, index);
    }

    public static String validValue2(Data2DColumn column,
            String currentValue, String newValue,
            ColumnDefinition.InvalidAs invalidAs) {
        try {
            if (invalidAs == null
                    || invalidAs == InvalidAs.Use
                    || column.validValue(newValue)) {
                return newValue;
            }
            switch (invalidAs) {
                case Zero:
                    return "0";
                case Null:
                    return null;
                case Empty:
                    return "";
                case Skip:
                    return currentValue;
            }
        } catch (Exception e) {
        }
        return currentValue;
    }

}
