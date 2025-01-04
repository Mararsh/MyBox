package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.BaseTable;
import static mara.mybox.db.table.BaseTableTools.internalTables;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-10
 * @License Apache License Version 2.0
 */
public class MyBoxTablesController extends BaseData2DListController {

    public MyBoxTablesController() {
        baseTitle = message("MyBoxTables");
    }

    @Override
    public void setConditions() {
        try {
            queryConditions = " data_type = " + Data2D.type(Data2DDefinition.DataType.InternalTable);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void loadList() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    Map<String, BaseTable> internalTables = internalTables();
                    for (String name : internalTables.keySet()) {
                        internalTables.get(name).recordTable(conn);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                loadTableData();
            }

        };
        start(task);
    }

    @Override
    protected int deleteData(FxTask currentTask, List<Data2DDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected long clearData(FxTask currentTask) {
        return tableDefinition.deleteCondition(queryConditions);
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
