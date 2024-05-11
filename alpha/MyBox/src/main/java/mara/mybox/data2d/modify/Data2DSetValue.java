package mara.mybox.data2d.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.SetValue;
import mara.mybox.data.SetValue.ValueType;
import static mara.mybox.data.SetValue.ValueType.Empty;
import static mara.mybox.data.SetValue.ValueType.Null;
import static mara.mybox.data.SetValue.ValueType.NumberPrefix;
import static mara.mybox.data.SetValue.ValueType.NumberReplace;
import static mara.mybox.data.SetValue.ValueType.NumberSuffix;
import static mara.mybox.data.SetValue.ValueType.One;
import static mara.mybox.data.SetValue.ValueType.Prefix;
import static mara.mybox.data.SetValue.ValueType.RandomNonNegative;
import static mara.mybox.data.SetValue.ValueType.Scale;
import static mara.mybox.data.SetValue.ValueType.Suffix;
import static mara.mybox.data.SetValue.ValueType.Zero;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-8-16
 * @License Apache License Version 2.0
 */
public class Data2DSetValue extends Data2DOperate {

    protected int dataIndex, digit;
    protected SetValue setValue;
    protected String dataValue;
    protected Random random = new Random();

    public static Data2DSetValue create(Data2D_Edit data, SetValue setValue) {
        if (data == null || setValue == null) {
            return null;
        }
        Data2DSetValue operate = new Data2DSetValue();
        if (!operate.setSourceData(data)
                || !operate.initParameters(setValue)) {
            return null;
        }
        operate.addWriter(data.selfWriter());
        return operate;
    }

    public boolean initParameters(SetValue v) {
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
    public void handleRow(List<String> row, long index) {
        try {
            sourceRow = row;
            sourceRowIndex = index;
            targetRow = null;
            passFilter = sourceData.filterDataRow(sourceRow, sourceRowIndex);
            reachMax = sourceData.filterReachMaxPassed();
            boolean handle = passFilter && !reachMax;
            ValueType valueType = setValue.getType();
            String expResult = null, currentValue;
            if (handle) {
                if (valueType == ValueType.Expression && dataValue != null) {
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
                            v = currentValue == null ? dataValue : dataValue + currentValue;
                        case Suffix ->
                            v = currentValue == null ? dataValue : currentValue + dataValue;
                        case NumberSuffix -> {
                            String suffix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = currentValue == null ? suffix : currentValue + suffix;
                        }
                        case NumberPrefix -> {
                            String prefix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = currentValue == null ? prefix : prefix + currentValue;
                        }
                        case NumberReplace -> {
                            v = StringTools.fillLeftZero(dataIndex++, digit);
                        }
                        case NumberSuffixString -> {
                            String suffix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = dataValue;
                            v = v == null ? suffix : v + suffix;
                        }
                        case NumberPrefixString -> {
                            String prefix = StringTools.fillLeftZero(dataIndex++, digit);
                            v = dataValue;
                            v = v == null ? prefix : prefix + v;
                        }
                        case Expression ->
                            v = expResult;
                        default ->
                            v = dataValue;
                    }
                    handledCount++;
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

}
