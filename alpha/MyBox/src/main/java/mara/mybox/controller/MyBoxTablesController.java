package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataInternalTable;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.DataFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
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
    protected Button helpDefinitionButton;
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

            saveButton.setDisable(true);
            recoverButton.setDisable(true);
            helpDefinitionButton.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        super.setControlsStyle();
        NodeStyleTools.setTooltip(helpDefinitionButton, new Tooltip(message("TableDefinition")));
    }

    public void loadList() {
        try {
            listView.getItems().setAll(DataFactory.InternalTables);
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
            saveButton.setDisable(false);
            recoverButton.setDisable(false);
            helpDefinitionButton.setDisable(false);
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

    @FXML
    protected void tableDefinition() {
        String html = DataFactory.tableDefinition(dataTable.getSheet());
        if (html != null) {
            HtmlPopController.openHtml(this, html);
        } else {
            popError(message("NotFound"));
        }
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
        if (def != null) {
            controller.loadTable(def.getSheet());
        }
        return controller;
    }

}
