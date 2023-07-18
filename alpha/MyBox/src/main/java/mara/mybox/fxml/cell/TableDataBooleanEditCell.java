package mara.mybox.fxml.cell;

import java.util.List;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2022-10-5
 * @License Apache License Version 2.0
 */
public class TableDataBooleanEditCell extends TableCheckboxCell<List<String>, String> {

    protected ControlData2DLoad dataControl;
    protected Data2DColumn dataColumn;
    protected int colIndex;

    public TableDataBooleanEditCell(ControlData2DLoad dataControl, Data2DColumn dataColumn, int colIndex) {
        super();
        this.dataControl = dataControl;
        this.dataColumn = dataColumn;
        this.colIndex = colIndex;
    }

    @Override
    protected boolean getCellValue(int rowIndex) {
        try {
            if (rowIndex < 0 || rowIndex >= dataControl.getTableData().size()) {
                return false;
            }
            List<String> row = dataControl.getTableData().get(rowIndex);
            return StringTools.string2Boolean(row.get(colIndex));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void setCellValue(int rowIndex, boolean value) {
        try {
            if (isChanging || rowIndex < 0
                    || rowIndex >= dataControl.getTableData().size()) {
                return;
            }
            List<String> row = dataControl.getTableData().get(rowIndex);
            if ((value + "").equalsIgnoreCase(getItem())) {
                return;
            }
            isChanging = true;
            row.set(colIndex, value + "");
            dataControl.getTableData().set(rowIndex, row);
            isChanging = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
        try {
            setStyle(dataControl.getData2D().cellStyle(dataControl.getStyleFilter(),
                    rowIndex(), dataColumn.getColumnName()));
        } catch (Exception e) {
        }
    }

    public static Callback<TableColumn, TableCell>
            create(ControlData2DLoad dataControl, Data2DColumn dataColumn, int colIndex) {
        return new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableDataBooleanEditCell(dataControl, dataColumn, colIndex);
            }
        };
    }

}
