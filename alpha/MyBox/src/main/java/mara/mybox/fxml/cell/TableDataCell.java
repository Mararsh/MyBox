package mara.mybox.fxml.cell;

import java.util.List;
import javafx.util.converter.DefaultStringConverter;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2022-9-26
 * @License Apache License Version 2.0
 */
public class TableDataCell extends TableAutoCommitCell<List<String>, String> {

    protected ControlData2DLoad dataControl;
    protected Data2DColumn dataColumn;
    protected final int trucSize = 200;
    protected boolean supportMultipleLine;

    public TableDataCell(ControlData2DLoad dataControl, Data2DColumn dataColumn) {
        super(new DefaultStringConverter());
        this.dataControl = dataControl;
        this.dataColumn = dataColumn;
        supportMultipleLine = dataControl.getData2D().supportMultipleLine() && dataColumn.isTextType();
    }

    protected String getCellValue() {
        return getItem();
    }

    protected boolean setCellValue(String inValue) {
        String value = inValue;
        if (value != null && supportMultipleLine) {
            value = value.replaceAll("\\\\n", "\n");
        }
        boolean changed = changed(value);
        commit(value, valid(value), changed);
        return changed;
    }

    @Override
    public boolean valid(String value) {
        return dataColumn.validValue(value) && dataControl.getData2D().validValue(value);
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
        setDataStyle();
        displayData(item);
    }

    public void setDataStyle() {
        try {
            setStyle(dataControl.getData2D().cellStyle(dataControl.getStyleFilter(),
                    rowIndex(), dataColumn.getColumnName()));
        } catch (Exception e) {
        }
    }

    public void displayData(String item) {
        setText(dataColumn.display(item, trucSize));
    }

}
