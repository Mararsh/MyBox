package mara.mybox.data2d.reader;

import mara.mybox.data2d.DataMatrix;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class MatrixReader extends Data2DReader {

    protected DataMatrix matrix;

    public MatrixReader(DataMatrix data) {
        matrix = data;
        sourceData = data;
    }

    @Override
    public void readColumnNames() {
        names = matrix.columnNames();
    }

    @Override
    public void readTotal() {
        sourceIndex = matrix.rowsNumber;
    }

    @Override
    public void readPage() {
        scanPage();
    }

    @Override
    public void readRows() {
        scanPage();
    }

}
