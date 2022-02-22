package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-10
 * @License Apache License Version 2.0
 */
public class MyBoxTablesController extends DataTablesController {

    public MyBoxTablesController() {
        baseTitle = message("MyBoxTables");
        type = Data2DDefinition.Type.InternalTable;
    }

    /*
        static
     */
    public static MyBoxTablesController oneOpen() {
        MyBoxTablesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MyBoxTablesController) {
                try {
                    controller = (MyBoxTablesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MyBoxTablesController) WindowTools.openStage(Fxmls.MyBoxTablesFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static MyBoxTablesController open(Data2DDefinition def) {
        MyBoxTablesController controller = oneOpen();
        if (def != null) {
            controller.loadDef(def);
        }
        return controller;
    }

}
