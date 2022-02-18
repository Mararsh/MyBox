package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-16
 * @License Apache License Version 2.0
 */
public class DataTablesController extends Data2DListController {

    protected boolean internal;

    @FXML
    protected Button tableDefinitionButton;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Label tableLabel;

    public DataTablesController() {
        baseTitle = message("DatabaseTable");
        TipsLabelKey = "ColorsManageTips";
        internal = false;
    }

    public Data2DDefinition.Type type() {
        return internal ? Data2DDefinition.Type.InternalTable : Data2DDefinition.Type.DatabaseTable;
    }

    @Override
    public void setData2D() {
        dataController.setDataType(this, type());
        data2D = dataController.data2D;
        loadController = dataController.editController.tableController;
    }

    @Override
    public void setQueryConditions() {
        queryConditions = " data_type=" + Data2DDefinition.type(type());
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            saveButton.setDisable(true);
            recoverButton.setDisable(true);
            tableDefinitionButton.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void load(Data2DDefinition source) {
        try {
            if (source == null || !dataController.checkBeforeNextAction()) {
                return;
            }
            dataController.resetStatus();
            data2D = Data2D.create(source.getType());
            data2D.cloneAll(source);
            dataController.loadDef(data2D);

            saveButton.setDisable(false);
            recoverButton.setDisable(false);
            tableDefinitionButton.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        load(data2D);
    }

    @FXML
    @Override
    public void saveAction() {
        dataController.save();
    }

    @FXML
    protected void tableDefinition() {
        String html = TableData2D.tableDefinition(data2D.getSheet());
        if (html != null) {
            HtmlPopController.openHtml(this, html);
        } else {
            popError(message("NotFound"));
        }
    }

    @FXML
    public void sql() {
        DatabaseSQLController.open(internal);
    }

    @Override
    public void myBoxClipBoard() {
        dataController.myBoxClipBoard();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return dataController.keyEventsFilter(event);
        }
        return true;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    /*
        static
     */
    public static DataTablesController oneOpen() {
        DataTablesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataTablesController) {
                try {
                    controller = (DataTablesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataTablesController) WindowTools.openStage(Fxmls.DataTablesFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static DataTablesController open(Data2DDefinition def) {
        DataTablesController controller = oneOpen();
        if (def != null) {
            controller.load(def);
        }
        return controller;
    }

}
