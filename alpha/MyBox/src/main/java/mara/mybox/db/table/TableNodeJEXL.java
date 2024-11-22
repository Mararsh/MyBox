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
public class TableNodeJEXL extends BaseNodeTable {

    public TableNodeJEXL() {
        tableName = "Node_JEXL";
        treeName = message("JEXL");
        dataName = message("JEXL");
        dataFxml = Fxmls.ControlDataJEXLFxml;
        examplesFileName = "JEXL";
        defineColumns();
    }

    public final TableNodeJEXL defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("script", ColumnType.Clob));
        addColumn(new ColumnDefinition("context", ColumnType.Clob));
        addColumn(new ColumnDefinition("parameters", ColumnType.String).setLength(FilenameMaxLength));
        return this;
    }

}
