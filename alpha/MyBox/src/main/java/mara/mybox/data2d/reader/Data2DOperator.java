package mara.mybox.data2d.reader;

import java.util.List;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.fxml.SingletonTask;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2DOperator {

    protected Data2DReader reader;
    protected Data2D data2D;
    protected SingletonTask task;
    protected List<Integer> cols, otherCols;
    protected int colsLen, scale = -1;
    protected boolean includeRowNumber, formatValues;
    protected List<String> sourceRow;
    protected long rowIndex; // 1-based 
    protected CSVPrinter csvPrinter;
    protected InvalidAs invalidAs;

    public boolean setData(Data2D_Edit data) {
        reader = Data2DReader.create(data);
        if (reader == null) {
            return false;
        }
        data2D = reader.data2D;
        task = reader.task;
        reader.operator = this;
        return true;
    }

    public Data2DOperator start() {
        if (data2D == null || !data2D.validData()
                || !checkParameters()
                || !reader.start()) {
            return null;
        }

        return this;
    }

    public boolean checkParameters() {
        return true;
    }

    public void handleData() {
        reader.readRows();
    }

    public void handleRow() {
    }

    public boolean end() {
        return true;
    }

    public boolean failed() {
        return reader.isFailed();
    }

    /*
        set
     */
    public Data2DOperator setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

    public Data2DOperator setCols(List<Integer> cols) {
        this.cols = cols;
        if (cols == null) {
            colsLen = 0;
        } else {
            colsLen = cols.size();
        }
        return this;
    }

    public Data2DOperator setOtherCols(List<Integer> otherCols) {
        this.otherCols = otherCols;
        return this;
    }

    public Data2DOperator setReader(Data2DReader reader) {
        this.reader = reader;
        return this;
    }

    public Data2DOperator setData2D(Data2D data2D) {
        this.data2D = data2D;
        return this;
    }

    public Data2DOperator setIncludeRowNumber(boolean includeRowNumber) {
        this.includeRowNumber = includeRowNumber;
        return this;
    }

    public Data2DOperator setSourceRow(List<String> sourceRow) {
        this.sourceRow = sourceRow;
        return this;
    }

    public Data2DOperator setCsvPrinter(CSVPrinter csvPrinter) {
        this.csvPrinter = csvPrinter;
        return this;
    }

    public Data2DOperator setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public Data2DOperator setInvalidAs(InvalidAs invalidAs) {
        this.invalidAs = invalidAs;
        return this;
    }

    public Data2DOperator setFormatValues(boolean formatValues) {
        this.formatValues = formatValues;
        return this;
    }

    /*
        get
     */
    public long getRowIndex() {
        return reader.getRowIndex();
    }

}
