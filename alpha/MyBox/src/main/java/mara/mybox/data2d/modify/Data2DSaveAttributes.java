package mara.mybox.data2d.modify;

import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSaveAttributes extends Data2DModify {

    public static Data2DSaveAttributes create(Data2D_Edit data, Data2D attrs) {
        if (data == null || attrs == null) {
            return null;
        }
        Data2DSaveAttributes operate = new Data2DSaveAttributes();
        if (!operate.setSourceData(data)) {
            return null;
        }
        operate.attributes = attrs;
        operate.initWriter();
        return operate;
    }

    @Override
    public void initWriter() {
        writer = sourceData.selfWriter();
        writer.setColumns(attributes.getColumns())
                .setHeaderNames(attributes.columnNames());
        addWriter(writer);
    }

    @Override
    public void handleRow(List<String> row, long index) {
        applyAttributes(row, index);
    }

}
