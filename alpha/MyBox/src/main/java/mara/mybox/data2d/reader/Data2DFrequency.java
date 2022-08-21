package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.Frequency;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DFrequency extends Data2DOperator {

    protected Frequency frequency;
    protected int colIndex;
    protected long count;

    public static Data2DFrequency create(Data2D_Edit data) {
        Data2DFrequency op = new Data2DFrequency();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return frequency != null && csvPrinter != null;
    }

    @Override
    public void handleRow() {
        try {
            frequency.addValue(sourceRow.get(colIndex));
        } catch (Exception e) {
        }
    }

    @Override
    public boolean end() {
        try {
            List<String> row = new ArrayList<>();
            row.add(message("All"));
            row.add(frequency.getSumFreq() + "");
            row.add("100");
            csvPrinter.printRecord(row);
            count = 1;
            Iterator iterator = frequency.valuesIterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    Object o = iterator.next();
                    row.clear();
                    String value = o == null ? null : (String) o;
                    row.add(value);
                    row.add(frequency.getCount(value) + "");
                    row.add(DoubleTools.format(frequency.getPct(value) * 100, scale));
                    csvPrinter.printRecord(row);
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
