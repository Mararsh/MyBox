package mara.mybox.data2d.writer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class Data2DWriter {

    protected Data2D data2D;
    protected File sourceFile;
    protected Operation operation;
    protected long rowIndex; // 1-based
    protected long count;
    protected int columnsNumber, colsLen, scale = -1, colIndex, dataIndex, digit;
    protected List<String> sourceRow, targetRow;
    protected List<Integer> cols;
    protected boolean writerStopped, needCheckTask, errorContinue;
    protected SingletonTask task;
    protected boolean failed;
    protected SetValue setValue;
    protected String dataValue;
    protected Random random = new Random();

    public static enum Operation {
        SetValue, Delete, ClearData
    }

    public abstract void scanData();

    public abstract void writeRow();

    public static Data2DWriter create(Data2D_Edit data) {
        if (data == null) {
            return null;
        }
        if (data instanceof DataFileExcel) {
            return new DataFileExcelWriter((DataFileExcel) data);
        } else if (data instanceof DataFileCSV) {
            return new DataFileCSVWriter((DataFileCSV) data);
        } else if (data instanceof DataTable) {
            return new DataTableWriter((DataTable) data);
        }
        return null;
    }

    public void init(Data2D data) {
        this.data2D = data;
        task = data2D.getTask();
    }

    public Data2DWriter start(Operation operation) {
        if (data2D == null || !data2D.validData() || operation == null) {
            failed = true;
            return null;
        }
        sourceFile = data2D.getFile();
        switch (operation) {
            case SetValue:
                if (cols == null || cols.isEmpty() || setValue == null) {
                    failed = true;
                    return null;
                }
                digit = setValue.countFinalDigit(data2D.getDataSize());
                dataIndex = setValue.getStart();
                dataValue = setValue.getValue();
                random = new Random();
                break;
            case Delete:
                break;
            case ClearData:
                break;

        }
        this.operation = operation;
        writerStopped = false;
        needCheckTask = task != null;
        columnsNumber = data2D.columnsNumber();
        rowIndex = 0;
        count = 0;
        if (scale < 0) {
            scale = data2D.getScale();
        }
        data2D.startFilter();
        scanData();
        afterScanned();
        return this;
    }

    public boolean isClearData() {
        return operation == Operation.ClearData
                || (operation == Operation.Delete && !data2D.needFilter());
    }

    public void handleRow() {
        try {
            targetRow = null;
            data2D.filterDataRow(sourceRow, rowIndex);
            boolean filterPassed = data2D.filterPassed() && !data2D.filterReachMaxPassed();
            switch (operation) {
                case SetValue:
                    handleSetValues(filterPassed);
                    break;
                case Delete:
                    handleDelete(filterPassed);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void handleSetValues(boolean filterPassed) {
        try {
            String expResult = null, currentValue;
            if (filterPassed) {
                if (setValue.isExpression() && dataValue != null) {
                    data2D.calculateDataRowExpression(dataValue, sourceRow, rowIndex);
                    expResult = data2D.expressionResult();
                    data2D.error = data2D.expressionError();
                    if (data2D.error != null) {
                        if (errorContinue) {
                            return;
                        } else {
                            failed = true;
                            writerStopped = true;
                            task.setError(data2D.error);
                            return;
                        }
                    }
                }
                count++;
            } else if (data2D instanceof DataTable) {
                return;
            }
            targetRow = new ArrayList<>();
            for (int i = 0; i < data2D.columns.size(); i++) {
                if (i < sourceRow.size()) {
                    currentValue = sourceRow.get(i);
                } else {
                    currentValue = null;
                }
                String v;
                if (filterPassed && cols.contains(i)) {
                    if (setValue.isBlank()) {
                        v = "";
                    } else if (setValue.isZero()) {
                        v = "0";
                    } else if (setValue.isOne()) {
                        v = "1";
                    } else if (setValue.isRandom()) {
                        v = data2D.random(random, i, false);
                    } else if (setValue.isRandom()) {
                        v = data2D.random(random, i, false);
                    } else if (setValue.isRandomNonNegative()) {
                        v = data2D.random(random, i, true);
                    } else if (setValue.isScale()) {
                        v = setValue.scale(currentValue);
                    } else if (setValue.isSuffix()) {
                        v = currentValue == null ? dataValue : currentValue + dataValue;
                    } else if (setValue.isPrefix()) {
                        v = currentValue == null ? dataValue : dataValue + currentValue;
                    } else if (setValue.isSuffixNumber()) {
                        String suffix = StringTools.fillLeftZero(dataIndex++, digit);
                        v = currentValue == null ? suffix : currentValue + suffix;
                    } else if (setValue.isExpression()) {
                        v = expResult;
                    } else {
                        v = dataValue;
                    }
                } else {
                    v = currentValue;
                }
                targetRow.add(v);
            }
            writeRow();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void handleDelete(boolean filterPassed) {
        try {
            if (data2D.error != null) {
                if (errorContinue) {
                    return;
                } else {
                    failed = true;
                    writerStopped = true;
                    task.setError(data2D.error);
                    return;
                }
            }
            if (filterPassed) {
                count++;
            }
            deleteRow(filterPassed);
        } catch (Exception e) {
        }
    }

    public void deleteRow(boolean needDelete) {
        if (!needDelete) {
            targetRow = sourceRow;
            writeRow();
        }
    }

    public void afterScanned() {
        try {
            data2D.stopFilter();

        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public boolean writerStopped() {
        return writerStopped || (needCheckTask && (task == null || task.isCancelled()));
    }

    /*
        get
     */
    public long getCount() {
        return count;
    }

    public boolean isFailed() {
        return failed;
    }

    /*
        set
     */
    public Data2DWriter setOperation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public Data2DWriter setCols(List<Integer> cols) {
        this.cols = cols;
        return this;
    }

    public Data2DWriter setErrorContinue(boolean errorContinue) {
        this.errorContinue = errorContinue;
        return this;
    }

    public Data2DWriter setTask(SingletonTask task) {
        this.task = task;
        return this;
    }

    public Data2DWriter setSetValue(SetValue setValue) {
        this.setValue = setValue;
        return this;
    }

}
