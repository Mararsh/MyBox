package mara.mybox.data2d.operate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
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
    protected String colName;
    protected int colIndex;

    public static Data2DFrequency create(Data2D_Edit data) {
        Data2DFrequency op = new Data2DFrequency();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return super.checkParameters() && frequency != null && colName != null;
    }

    @Override
    public boolean go() {
        if (!sourceData.isTable() || sourceData.needFilter()) {
            return reader.start(false);
        } else {
            return goTable();
        }
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
            handledCount = 1;
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
                    handledCount++;
                }
            }
            frequency.clear();
            return super.end();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean goTable() {

        try {
            int total = 0;
            conn = conn();
            String sql = "SELECT count(*) AS mybox99_count FROM " + sourceData.getSheet();
            if (task != null) {
                task.setInfo(sql);
            }
            try (PreparedStatement statement = conn.prepareStatement(sql);
                    ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    total = results.getInt("mybox99_count");
                }
            } catch (Exception e) {
            }
            if (total == 0) {
                if (task != null) {
                    task.setError(message("NoData"));
                }
                return false;
            }
            targetRow = new ArrayList<>();
            targetRow.add(message("All"));
            targetRow.add(total + "");
            targetRow.add("100");
            writeRow();
            handledCount = 0;
            sql = "SELECT " + colName + ", count(*) AS mybox99_count FROM " + sourceData.getSheet()
                    + " GROUP BY " + colName + " ORDER BY mybox99_count DESC";
            if (task != null) {
                task.setInfo(sql);
            }
            try (PreparedStatement statement = conn.prepareStatement(sql);
                    ResultSet results = statement.executeQuery()) {
                String sname = DerbyBase.savedName(colName);
                while (results.next() && task != null && !task.isCancelled()) {
                    targetRow = new ArrayList<>();
                    Object c = results.getObject(sname);
                    targetRow.add(c != null ? c.toString() : null);
                    int count = results.getInt("mybox99_count");
                    targetRow.add(count + "");
                    targetRow.add(DoubleTools.percentage(count, total, scale));
                    writeRow();
                    handledCount++;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                } else {
                    MyBoxLog.error(e);
                }
                return false;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
        return true;
    }

    /*
        set
     */
    public Data2DFrequency setFrequency(Frequency frequency) {
        this.frequency = frequency;
        return this;
    }

    public Data2DFrequency setColName(String colName) {
        this.colName = colName;
        return this;
    }

    public Data2DFrequency setColIndex(int colIndex) {
        this.colIndex = colIndex;
        return this;
    }

}
