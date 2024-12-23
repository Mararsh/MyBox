package mara.mybox.data2d.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.SetValue;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class Data2DModify extends Data2DOperate {

    protected Data2DWriter writer;
    protected long rowsNumber;

    protected int setValueIndex, setValueDigit;
    protected SetValue setValue;
    protected String setValueParameter;
    protected Random random = new Random();
    protected boolean valueInvalid, validateColumn, rejectInvalid, skipInvalid;

    protected Data2DColumn column;
    protected String columnName, currentValue, newValue;
    protected boolean rowChanged, handleRow;
    protected int rowSize;

    public void initWriter() {
        writer = sourceData.selfWriter();
        addWriter(writer);
    }

    public boolean initSetValue(SetValue v) {
        if (sourceData == null || v == null) {
            return false;
        }
        setValue = v;
        setValueDigit = setValue.countFinalDigit(sourceData.getRowsNumber());
        setValueIndex = setValue.getStart();
        setValueParameter = setValue.getParameter();
        random = new Random();
        return true;
    }

    @Override
    public boolean checkParameters() {
        rowsNumber = 0;

        valueInvalid = false;
        rejectInvalid = sourceData.rejectInvalidWhenSave() || invalidAs == InvalidAs.Fail;
        skipInvalid = invalidAs == InvalidAs.Skip;
        validateColumn = rejectInvalid || skipInvalid;

        return super.checkParameters();
    }

    public void setValue(List<String> row, long index) {
        try {
            sourceRow = row;
            sourceRowIndex = index;
            targetRow = null;
            if (sourceRow == null) {
                return;
            }
            rowPassFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMaxFiltered = sourceData.filterReachMaxPassed();
            handleRow = rowPassFilter && !reachMaxFiltered;
            if (!handleRow && (sourceData instanceof DataTable)) {
                return;
            }
            targetRow = new ArrayList<>();
            rowSize = sourceRow.size();
            rowChanged = false;
            for (int i = 0; i < sourceData.columns.size(); i++) {
                if (i < rowSize) {
                    currentValue = sourceRow.get(i);
                } else {
                    currentValue = null;
                }
                if (handleRow && cols.contains(i)) {
                    column = sourceData.columns.get(i);
                    newValue = setValue.makeValue(sourceData, column,
                            currentValue, sourceRow, sourceRowIndex,
                            setValueIndex, setValueDigit, random);
                    valueInvalid = setValue.valueInvalid;
                    if (!valueInvalid && validateColumn) {
                        if (!column.validValue(newValue)) {
                            valueInvalid = true;
                        }
                    }
                    if (valueInvalid) {
                        if (skipInvalid) {
                            newValue = currentValue;
                        } else if (rejectInvalid) {
                            failStop(message("InvalidData") + ". "
                                    + message("Column") + ":" + column.getColumnName() + "  "
                                    + message("Value") + ": " + newValue);
                            return;
                        }
                    }
                    if ((currentValue == null && newValue != null)
                            || (currentValue != null && !currentValue.equals(newValue))) {
                        rowChanged = true;
                    }
                } else {
                    newValue = currentValue;
                }
                targetRow.add(newValue);
            }
            if (rowChanged) {
                handledCount++;
                setValueIndex++;
            }
            writeRow();
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public boolean end() {
        try {
            if (!super.end()
                    || writer == null
                    || writer.getTargetData() == null) {
                return false;
            }
            rowsNumber = writer.getTargetRowIndex();
            sourceData.setRowsNumber(rowsNumber);
            sourceData.setTableChanged(false);
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    public long rowsCount() {
        if (failed) {
            return -1;
        } else {
            return rowsNumber;
        }
    }

}
