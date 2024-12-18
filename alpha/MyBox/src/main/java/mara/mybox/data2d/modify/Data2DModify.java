package mara.mybox.data2d.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.SetValue;
import static mara.mybox.data.SetValue.ValueType.Empty;
import static mara.mybox.data.SetValue.ValueType.Expression;
import static mara.mybox.data.SetValue.ValueType.Null;
import static mara.mybox.data.SetValue.ValueType.NumberPrefix;
import static mara.mybox.data.SetValue.ValueType.NumberPrefixString;
import static mara.mybox.data.SetValue.ValueType.NumberReplace;
import static mara.mybox.data.SetValue.ValueType.NumberSuffix;
import static mara.mybox.data.SetValue.ValueType.NumberSuffixString;
import static mara.mybox.data.SetValue.ValueType.One;
import static mara.mybox.data.SetValue.ValueType.Prefix;
import static mara.mybox.data.SetValue.ValueType.Random;
import static mara.mybox.data.SetValue.ValueType.RandomNonNegative;
import static mara.mybox.data.SetValue.ValueType.Scale;
import static mara.mybox.data.SetValue.ValueType.Suffix;
import static mara.mybox.data.SetValue.ValueType.Zero;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.writer.Data2DWriter;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Empty;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Null;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Skip;
import static mara.mybox.db.data.ColumnDefinition.InvalidAs.Zero;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public abstract class Data2DModify extends Data2DOperate {

    protected Data2DWriter writer;
    protected long rowsNumber;

    protected int dataIndex, digit;
    protected SetValue setValue;
    protected String dataValue;
    protected Random random = new Random();

    public void initWriter() {
        writer = sourceData.selfWriter();
        addWriter(writer);
    }

    public boolean initSetValue(SetValue v) {
        if (sourceData == null || v == null) {
            return false;
        }
        setValue = v;
        digit = setValue.countFinalDigit(sourceData.getRowsNumber());
        dataIndex = setValue.getStart();
        dataValue = setValue.getValue();
        random = new Random();
        return true;
    }

    @Override
    public boolean checkParameters() {
        rowsNumber = 0;
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
            passFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMax = sourceData.filterReachMaxPassed();
            boolean handle = passFilter && !reachMax;
            SetValue.ValueType valueType = setValue.getType();
            String expResult = null, currentValue;
            if (handle) {
                if (valueType == SetValue.ValueType.Expression && dataValue != null) {
                    if (sourceData.calculateDataRowExpression(dataValue, sourceRow, sourceRowIndex)) {
                        expResult = sourceData.expressionResult();
                    } else {
                        failStop(sourceData.expressionError());
                        return;
                    }
                }
            }
            if (!handle && (sourceData instanceof DataTable)) {
                return;
            }
            targetRow = new ArrayList<>();
            int rowSize = sourceRow.size();
            for (int i = 0; i < sourceData.columns.size(); i++) {
                if (i < rowSize) {
                    currentValue = sourceRow.get(i);
                } else {
                    currentValue = null;
                }
                String v;
                if (handle && cols.contains(i)) {
                    switch (valueType) {
                        case Zero ->
                            v = "0";
                        case One ->
                            v = "1";
                        case Empty ->
                            v = "";
                        case Null ->
                            v = null;
                        case Random ->
                            v = sourceData.random(random, i, false);
                        case RandomNonNegative ->
                            v = sourceData.random(random, i, true);
                        case Scale ->
                            v = setValue.scale(currentValue);
                        case Prefix ->
                            v = currentValue == null ? dataValue : (dataValue + currentValue);
                        case Suffix ->
                            v = currentValue == null ? dataValue : (currentValue + dataValue);
                        case NumberSuffix -> {
                            String suffix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = currentValue == null ? suffix : (currentValue + suffix);
                        }
                        case NumberPrefix -> {
                            String prefix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = currentValue == null ? prefix : (prefix + currentValue);
                        }
                        case NumberReplace -> {
                            v = StringTools.fillLeftZero(dataIndex++, digit);
                        }
                        case NumberSuffixString -> {
                            String suffix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = dataValue;
                            v = v == null ? suffix : (v + suffix);
                        }
                        case NumberPrefixString -> {
                            String prefix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = dataValue;
                            v = v == null ? prefix : (prefix + v);
                        }
                        case Expression ->
                            v = expResult;
                        default ->
                            v = dataValue;
                    }
                    Data2DColumn column = sourceData.columns.get(i);
                    if (!column.validValue(v) && invalidAs != null) {
                        switch (invalidAs) {
                            case Zero:
                                v = "0";
                                break;
                            case Null:
                                v = null;
                                break;
                            case Empty:
                                v = "";
                                break;
                            case Skip:
                                v = currentValue;
                                break;
                            case Fail:
                                failStop(message("InvalidData") + ". "
                                        + message("Column") + ":" + column.getColumnName() + "  "
                                        + message("Value") + ": " + v);
                                return;
                        }
                    }
                    handledCount++;
                } else {
                    v = currentValue;
                }
                targetRow.add(v);
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
