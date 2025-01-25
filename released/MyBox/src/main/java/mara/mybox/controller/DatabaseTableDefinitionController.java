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
import mara.mybox.db.table.BaseTableTools;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
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
        if (task != null) {
            task.cancel();
        }
        listView.getItems().clear();
        task = new FxSingletonTask<Void>(this) {

            List<String> names;

            @Override
            protected boolean handle() {
                try {
                    if (internalRadio.isSelected()) {
                        names = BaseTableTools.internalTableNames();
                    } else {
                        names = BaseTableTools.userTables();
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (names != null) {
                    listView.getItems().setAll(names);
                }
            }
        };
        start(task, listView);
    }

    public void loadDefinition(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        viewController.loadContent("");
        FxTask loadTask = new FxSingletonTask<Void>(this) {

            String html;

            @Override
            protected boolean handle() {
                try {
                    html = TableData2D.tableDefinition(name);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (html != null && !html.isBlank()) {
                    viewController.loadContent(html);
                }
            }

        };
        start(loadTask, false);
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

}
