package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import mara.mybox.data.Data2D;
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
    protected boolean changed;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab textTab, tableTab;
    @FXML
    protected ControlData2DEditText textController;
    @FXML
    protected ControlData2DEditTable tableController;
    @FXML
    protected Button deleteRowsButton;

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            data2D = dataController.data2D;
            baseName = dataController.baseName;

            tableController.setParameters(this);
            textController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
        try {
            tableController.loadTableData();
            textController.loadData();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            try {
                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                if (tab == tableTab) {
                    return tableController.keyEventsFilter(event);

                } else if (tab == textTab) {
                    return textController.keyEventsFilter(event);

                }
            } catch (Exception e) {
                MyBoxLog.error(e);
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
