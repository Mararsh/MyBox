package mara.mybox.data2d.operate;

import java.io.File;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.writer.Data2DWriter;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DSavePage extends Data2DOperate {

    public static Data2DSavePage saveAsFile(Data2D_Edit data, File targetFile) {
        if (data == null || targetFile == null) {
            return null;
        }
        Data2DWriter writer = data.selfWriter().setTargetFile(targetFile);
        return writeTo(data, writer);

    }

    public static Data2DSavePage writeTo(Data2D_Edit data, Data2DWriter writer) {
        if (data == null || writer == null) {
            return null;
        }
        Data2DSavePage operate = new Data2DSavePage();
        if (!operate.setSourceData(data)) {
            return null;
        }
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
            targetRow = sourceRow;
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

}
