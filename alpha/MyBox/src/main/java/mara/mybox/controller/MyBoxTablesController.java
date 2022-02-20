package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import mara.mybox.data.DataInternalTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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
        internal = true;
    }

    @Override
    public void initList() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    DataInternalTable dataTable = new DataInternalTable();
                    TableData2DDefinition tableData2DDefinition = dataTable.getTableData2DDefinition();
                    for (String name : DataInternalTable.InternalTables) {
                        if (tableData2DDefinition.queryTable(conn, name, Data2DDefinition.Type.InternalTable) == null) {
                            dataTable.readDefinitionFromDB(conn, name);
                        }
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
            controller.load(def);
        }
        return controller;
    }

}
