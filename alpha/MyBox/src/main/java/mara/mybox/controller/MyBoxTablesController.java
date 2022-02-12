package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataInternalTable;
import mara.mybox.data.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-10
 * @License Apache License Version 2.0
 */
public class MyBoxTablesController extends BaseController {

    protected DataInternalTable dataTable;

    @FXML
    protected ListView<String> listView;
    @FXML
    protected Button clearDataButton, deleteDataButton, renameDataButton;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Label tableLabel;

    public MyBoxTablesController() {
        baseTitle = message("MyBoxTables");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.setDataType(this, Data2D.Type.InternalTable, true);
            dataTable = (DataInternalTable) dataController.data2D;
            dataController.tableController.dataLabel = tableLabel;
            dataController.tableController.baseTitle = baseTitle;

            listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> o, String ov, String nv) {
                    if (nv != null) {
                        loadTable(nv);
                    }
                }
            });

            loadList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadList() {
        try {
            List<String> internalTables = new ArrayList<>();
            String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T' AND NOT (TABLENAME like '"
                    + DataTable.UserTablePrefix + "%') ORDER BY TABLENAME";
            try ( Connection conn = DerbyBase.getConnection();
                     Statement statement = conn.createStatement();
                     ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    internalTables.add(resultSet.getString("TABLENAME"));
                }
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
            listView.getItems().setAll(internalTables);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadTable(String tableName) {
        try {
            if (tableName == null || !dataController.checkBeforeNextAction()) {
                return;
            }
            dataTable.setTable(tableName);
            dataController.readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        loadList();
    }

    @FXML
    @Override
    public void recoverAction() {
        loadTable(dataTable.getSheet());
    }

    @FXML
    @Override
    public void saveAction() {
        dataController.save();
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

    @Override
    public void cleanPane() {
        try {
            dataController = null;
            dataTable = null;
        } catch (Exception e) {
        }
        super.cleanPane();
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
        controller.loadTable(def.getSheet());
        return controller;
    }

}
