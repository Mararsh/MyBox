package mara.mybox.data2d.writer;

import java.nio.charset.Charset;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.data.Data2DDefinition;

/**
 * @Author Mara
 * @CreateDate 2025-3-17
 * @License Apache License Version 2.0
 */
public class DataMatrixWriter extends DataFileTextWriter {

    protected String dataType;

    public DataMatrixWriter() {
        fileSuffix = "txt";
    }

    @Override
    public boolean openWriter() {
        try {
            charset = Charset.forName("utf-8");
            delimiter = DataMatrix.MatrixDelimiter;
            writeHeader = false;
            return super.openWriter();
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void recordTargetData() {
        try {
            if (recordTargetData) {
                if (targetData == null) {
                    targetData = Data2D.create(Data2DDefinition.DataType.Matrix);
                }
                targetData.setTask(task())
                        .setFile(printFile)
                        .setSheet(dataType != null ? dataType : "Double")
                        .setCharset(charset)
                        .setDelimiter(delimiter)
                        .setHasHeader(writeHeader)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(targetRowIndex);
                Data2D.saveAttributes(conn(), targetData, columns);
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    /*
        get/set
     */
    public String getDataType() {
        return dataType;
    }

    public DataMatrixWriter setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

}
