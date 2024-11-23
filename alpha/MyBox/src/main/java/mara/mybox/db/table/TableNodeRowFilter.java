package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNodeRowFilter extends BaseNodeTable {

    public TableNodeRowFilter() {
        tableName = "Node_Row_Filter";
        treeName = message("RowFilter");
        dataName = message("RowFilter");
        dataFxml = Fxmls.ControlDataRowFilterFxml;
        examplesFileName = "RowFilter";
        defineColumns();
    }

    public final TableNodeRowFilter defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("max_match", ColumnType.Long));
        addColumn(new ColumnDefinition("match_true", ColumnType.Boolean));
        addColumn(new ColumnDefinition("script", ColumnType.Clob));
        return this;
    }

    @Override
    public String label(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        switch (name) {
            case "max_match":
                return message("MaximumNumber");
            case "match_true":
                return message("MatchTrue");
            case "script":
                return message("JavaScript");
        }
        return name;
    }

}
