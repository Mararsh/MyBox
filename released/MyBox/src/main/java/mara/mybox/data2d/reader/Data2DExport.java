package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DExport extends Data2DOperator {

    protected ControlDataConvert convertController;

    public static Data2DExport create(Data2D_Edit data) {
        Data2DExport op = new Data2DExport();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return cols != null && !cols.isEmpty() && convertController != null;
    }

    @Override
    public void handleRow() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            convertController.writeRow(row);
        } catch (Exception e) {
        }
    }

    /*
        set
     */
    public Data2DExport setConvertController(ControlDataConvert convertController) {
        this.convertController = convertController;
        return this;
    }

}
