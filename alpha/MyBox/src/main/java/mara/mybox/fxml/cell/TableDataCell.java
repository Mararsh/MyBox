package mara.mybox.fxml.cell;

import java.util.List;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.controller.BaseData2DTableController;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableDataCell extends TableAutoCommitCell<List<String>, String> {

    protected BaseData2DTableController dataTable;
    protected Data2DColumn dataColumn;
    protected final int trucSize = 200;
    protected boolean supportMultipleLine;

    public TableDataCell(BaseData2DTableController dataTable, Data2DColumn dataColumn) {
        super(new DefaultStringConverter());
        this.dataTable = dataTable;
        this.dataColumn = dataColumn;
        supportMultipleLine = dataTable.getData2D().supportMultipleLine() && dataColumn.supportMultipleLine();
    }

    protected String getCellValue() {
        return getItem();
    }

    @Override
    public boolean setCellValue(String inValue) {
        try {
            String value = inValue;
            if (value != null && supportMultipleLine) {
                value = value.replaceAll("\\\\n", "\n");
            }
            return super.setCellValue(value);
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public boolean valid(String value) {
        try {
            if (!dataTable.getData2D().validValue(value)) {
                return false;
            }
            if (!dataTable.getData2D().validateEdit()) {
                return true;
            }
            return dataColumn.validValue(value);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setStyle(null);
        if (empty) {
            setText(null);
            setGraphic(null);
            return;
        }
        setDataStyle(item);
        displayData(item);
    }

    public void displayData(String item) {
        try {
            setText(dataColumn.format(item, trucSize, InvalidAs.Use));
        } catch (Exception e) {
            setText(item);
        }
    }

    public void setDataStyle(String item) {
        try {
            String style = dataTable.getData2D().cellStyle(dataTable.getStyleFilter(),
                    rowIndex(), dataColumn.getColumnName());
            if (style != null) {
                setStyle(style);
            } else if (dataColumn.validValue(item)) {
                setStyle(null);
            } else {
                setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
        }
    }

}
