package mara.mybox.data2d.operate;

import java.util.ArrayList;
import java.util.Iterator;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.NumberTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.Frequency;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DFrequency extends Data2DOperate {

    protected Frequency frequency;
    protected int colIndex;

    public static Data2DFrequency create(Data2D_Edit data) {
        Data2DFrequency op = new Data2DFrequency();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return super.checkParameters() && frequency != null;
    }

    @Override
    public boolean handleRow() {
        try {
            frequency.addValue(sourceRow.get(colIndex));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean end() {
        try {
            targetRow = new ArrayList<>();
            targetRow.add(message("All"));
            targetRow.add(frequency.getSumFreq() + "");
            targetRow.add("100");
            writeRow();
            count = 1;
            Iterator iterator = frequency.valuesIterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    Object o = iterator.next();
                    targetRow.clear();
                    String value = o == null ? null : (String) o;
                    targetRow.add(value);
                    targetRow.add(frequency.getCount(value) + "");
                    targetRow.add(NumberTools.format(frequency.getPct(value) * 100, scale));
                    writeRow();
                    count++;
                }
            }
            frequency.clear();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    /*
        set
     */
    public Data2DFrequency setFrequency(Frequency frequency) {
        this.frequency = frequency;
        return this;
    }

    public Data2DFrequency setColIndex(int colIndex) {
        this.colIndex = colIndex;
        return this;
    }

    /*
        get
     */
    public long getCount() {
        return count;
    }

}
