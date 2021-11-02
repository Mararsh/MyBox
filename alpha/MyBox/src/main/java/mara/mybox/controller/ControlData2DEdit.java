package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.data.Data2D;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import static mara.mybox.value.Languages.message;

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

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab sheetTab, textTab, tableTab;
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

    @FXML
    @Override
    public void saveAction() {

    }

    @FXML
    public void refreshAction() {

    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Copy"));
            menu.setOnAction((ActionEvent event) -> {
                copyAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Pop"));
            menu.setOnAction((ActionEvent event) -> {
                popAction();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
