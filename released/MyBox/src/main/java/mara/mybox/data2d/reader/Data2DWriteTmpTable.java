package mara.mybox.data2d.reader;

import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.data.Data2DRow;

/**
 * @Author Mara
 * @CreateDate 2022-12-3
 * @License Apache License Version 2.0
 */
public class Data2DWriteTmpTable extends Data2DWriteTable {

    protected TmpTable tmpTable;

    public static Data2DWriteTmpTable create(Data2D_Edit data) {
        Data2DWriteTmpTable op = new Data2DWriteTmpTable();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (!super.checkParameters() || !(writerTable instanceof TmpTable)) {
            return false;
        }
        tmpTable = (TmpTable) writerTable;
        return true;
    }

    @Override
    public void makeTableRow(Data2DRow data2DRow) {
        tmpTable.makeTmpRow(data2DRow, sourceRow, rowIndex);
    }

}
