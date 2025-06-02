package mara.mybox.data2d.modify;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSaveAttributes extends Data2DModify {

    protected Data2D attributes;

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
        writer.setDataName(attributes.getDataName())
                .setTargetData(attributes)
                .setColumns(attributes.getColumns())
                .setHeaderNames(attributes.columnNames());
        addWriter(writer);
    }

    @Override
    public void handleRow(List<String> row, long index) {
        try {
            sourceRow = row;
            sourceRowIndex = index;
            targetRow = null;
            if (sourceRow == null) {
                return;
            }
            targetRow = new ArrayList<>();
            for (Data2DColumn col : attributes.columns) {
                int colIndex = col.getIndex();
                if (colIndex < 0 || colIndex >= sourceRow.size()) {
                    targetRow.add(null);
                } else {
                    targetRow.add(sourceRow.get(colIndex));
                }
            }
            writeRow();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

}
