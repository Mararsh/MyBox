package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 *
 */
public class ControlData2DEdit extends BaseController {

    protected ControlData2D dataController;
    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;

    @FXML
    protected Tab textTab, tableTab;
    @FXML
    protected ControlData2DEditCSV csvController;
    @FXML
    protected ControlData2DEditTable tableController;

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;

            tableController.setParameters(this);
            csvController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setData(Data2D data) {
        try {
            data2D = data;
            tableController.setData(data);
            csvController.setData(data);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isChanged() {
        return data2D != null && data2D.isTableChanged() || csvController.isChanged();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (tableTab.isSelected()) {
                return tableController.keyEventsFilter(event);

            } else if (textTab.isSelected()) {
                return csvController.keyEventsFilter(event);

            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void cleanPane() {
        try {
            dataController = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
