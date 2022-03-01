package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Window;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2D;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-16
 * @License Apache License Version 2.0
 */
public class DataTablesController extends BaseData2DController {

    @FXML
    protected Button tableDefinitionButton;

    public DataTablesController() {
        baseTitle = message("DatabaseTable");
        TipsLabelKey = "DataTableTips";
        type = Data2DDefinition.Type.DatabaseTable;
    }

    @Override
    public void checkButtons() {
        super.checkButtons();
        tableDefinitionButton.setDisable(false);
    }

    @FXML
    protected void tableDefinition() {
        if (loadController.data2D == null) {
            return;
        }
        String html = TableData2D.tableDefinition(loadController.data2D.getSheet());
        if (html != null) {
            HtmlPopController.openHtml(this, html);
        } else {
            popError(message("NotFound"));
        }
    }

    @FXML
    public void sql() {
        DatabaseSQLController.open(this instanceof MyBoxTablesController);
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
            controller.loadDef(def);
        }
        return controller;
    }

}
