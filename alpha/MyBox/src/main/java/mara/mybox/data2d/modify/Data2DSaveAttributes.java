package mara.mybox.data2d.modify;

import java.util.ArrayList;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSaveAttributes extends Data2DOperate {

    protected Data2D attributes;

    public static Data2DSaveAttributes create(Data2D_Edit data, Data2D attributes) {
        if (data == null || attributes == null) {
            return null;
        }
        Data2DSaveAttributes operate = new Data2DSaveAttributes();
        if (!operate.setSourceData(data)) {
            return null;
        }
        operate.attributes = attributes;
        Data2DWriter writer = attributes.selfWriter();
        operate.addWriter(writer);
        return operate;
    }

    @Override
    public boolean handleRow() {
        try {
            targetRow = null;
            if (sourceRow == null) {
                return false;
            }
            targetRow = new ArrayList<>();
            for (Data2DColumn column : attributes.columns) {
                int dataIndex = column.getIndex();
                if (dataIndex < 0 || dataIndex >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    targetRow.add(sourceRow.get(dataIndex));
                }
            }
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

}
