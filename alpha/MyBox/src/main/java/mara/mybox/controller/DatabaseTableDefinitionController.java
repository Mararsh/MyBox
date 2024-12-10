package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.DataInternalTable;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-1-11
 * @License Apache License Version 2.0
 */
public class DatabaseTableDefinitionController extends BaseController {

    public boolean internalTables;

    @FXML
    protected ListView<String> listView;
    @FXML
    protected ControlWebView viewController;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton internalRadio;

    public DatabaseTableDefinitionController() {
        baseTitle = message("TableDefinition");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    loadDefinition(nv);
                }
            });

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    loadList();
                }
            });

            loadList();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadList() {
        try {
            List<String> names;
            if (internalRadio.isSelected()) {
                names = DataInternalTable.InternalTables;
            } else {
                names = Data2DTableTools.userTables();
            }
            listView.getItems().setAll(names);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadDefinition(String name) {
        try {
            if (name == null || name.isBlank()) {
                return;
            }
            String html = TableData2D.tableDefinition(name);
            if (html != null && !html.isBlank()) {
                viewController.loadContents(html);
            } else {
                popError(message("NotFound"));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static DatabaseTableDefinitionController open() {
        DatabaseTableDefinitionController controller
                = (DatabaseTableDefinitionController) WindowTools.openStage(Fxmls.DatabaseTableDefinitionFxml);
        controller.requestMouse();
        return controller;
    }

    public static DatabaseTableDefinitionController open(boolean internalTables) {
        DatabaseTableDefinitionController controller = open();
        if (internalTables) {
            controller.internalRadio.setSelected(true);
        }
        return controller;
    }

    public static DatabaseTableDefinitionController load(String tableName) {
        DatabaseTableDefinitionController controller = open();
        controller.loadDefinition(tableName);
        return controller;
    }

}
