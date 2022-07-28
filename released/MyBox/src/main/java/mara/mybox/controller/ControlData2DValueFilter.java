package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DValueFilter extends ControlData2DRowFilter {

    public ControlData2DValueFilter() {
        TipsLabelKey = "ValueFilterTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            data2D = Data2D.create(Data2DDefinition.Type.Texts);
            List<Data2DColumn> columns = new ArrayList<>();
            columns.add(new Data2DColumn("x", ColumnDefinition.ColumnType.String));
            data2D.setColumns(columns);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
}
