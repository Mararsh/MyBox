package mara.mybox.data2d.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mara.mybox.data.SetValue;
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
            String expResult = null, currentValue;
            if (handle) {
                if (setValue.isExpression() && dataValue != null) {
                    if (sourceData.calculateDataRowExpression(dataValue, sourceRow, sourceRowIndex)) {
                        expResult = sourceData.expressionResult();
                    } else {
                        sourceData.error = sourceData.expressionError();
                        if (errorContinue) {
                            showError(sourceData.error);
                            handle = false;
                        } else {
                            failStop(sourceData.error);
                            return;
                        }
                    }
                }
                handledCount++;
            } else if (sourceData instanceof DataTable) {
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
                    if (setValue.isBlank()) {
                        v = "";
                    } else if (setValue.isZero()) {
                        v = "0";
                    } else if (setValue.isOne()) {
                        v = "1";
                    } else if (setValue.isRandom()) {
                        v = sourceData.random(random, i, false);
                    } else if (setValue.isRandom()) {
                        v = sourceData.random(random, i, false);
                    } else if (setValue.isRandomNonNegative()) {
                        v = sourceData.random(random, i, true);
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

}
