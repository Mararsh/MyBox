package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import static mara.mybox.db.data.DataNode.MoreSeparater;
import static mara.mybox.db.data.DataNode.ValueSeparater;
import mara.mybox.db.data.MathFunction;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableMathFunction extends BaseTreeData<MathFunction> {

    public TableMathFunction() {
        tableName = "Math_Function";
        idColumnName = "funcid";
        defineColumns();
    }

    public final TableMathFunction defineColumns() {
        addColumn(new ColumnDefinition("funcid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("name", ColumnType.String, true));
        addColumn(new ColumnDefinition("variables", ColumnType.String));
        addColumn(new ColumnDefinition("expression", ColumnType.Clob));
        addColumn(new ColumnDefinition("domain", ColumnType.Clob));
        return this;
    }

    @Override
    public boolean setValue(MathFunction data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        return data.setValue(column, value);
    }

    @Override
    public Object getValue(MathFunction data, String column) {
        if (data == null || column == null) {
            return null;
        }
        return data.getValue(column);
    }

    @Override
    public boolean valid(MathFunction data) {
        if (data == null) {
            return false;
        }
        return data.valid();
    }

    @Override
    public long insertData(Connection conn, String title, String info) {
        try {
            MathFunction func = new MathFunction().setName(title);
            if (info != null && !info.isBlank()) {
                if (info.contains(ValueSeparater)) {
                    String[] ss = info.split(ValueSeparater);
                    if (ss.length > 0) {
                        func.setName(ss[0].trim());
                    }
                    if (ss.length > 1) {
                        func.setVariables(ss[1].trim());
                    }
                    if (ss.length > 2) {
                        func.setExpression(ss[2].trim());
                    }
                    if (ss.length > 3) {
                        func.setDomain(ss[3].trim());
                    }
                } else {
                    String prefix = "Names:::";
                    if (info.startsWith(prefix)) {
                        info = info.substring(prefix.length());
                        int pos = info.indexOf("\n");
                        String names;
                        if (pos >= 0) {
                            names = info.substring(0, pos);
                            info = info.substring(pos);
                        } else {
                            names = info;
                            info = null;
                        }
                        pos = names.indexOf(",");
                        if (pos >= 0) {
                            func.setName(names.substring(0, pos));
                            String vs = names.substring(pos).trim();
                            if (vs.length() > 0) {
                                func.setVariables(vs.substring(1));
                            }
                        } else {
                            func.setName(names);
                        }
                    }
                    if (info != null && info.contains(MoreSeparater)) {
                        String[] ss = info.split(MoreSeparater);
                        func.setExpression(ss[0].trim());
                        if (ss.length > 1) {
                            func.setDomain(ss[1].trim());
                        }
                    } else {
                        func.setExpression(info);
                    }
                }
            }
            func = insertData(conn, func);
            return func.getFuncid();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return -1;
        }
    }

}
