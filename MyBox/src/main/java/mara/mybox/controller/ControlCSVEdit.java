package mara.mybox.controller;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.table.BaseTable;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-9-24
 * @License Apache License Version 2.0
 */
public class ControlCSVEdit extends BaseController {

    protected BaseTable tableDefinition;

    @FXML
    protected Button inputButton;

    public void init(BaseController parent, BaseTable tableDefinition) {
        parentController = parent;
        this.tableDefinition = tableDefinition;
    }

    @FXML
    protected void popInputMenu(MouseEvent mouseEvent) {
        try {
            if (tableDefinition == null) {
                return;
            }
            if (parentController.popMenu != null && parentController.popMenu.isShowing()) {
                parentController.popMenu.hide();
            }
            parentController.popMenu = new ContextMenu();
            popMenu = parentController.popMenu;
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("InputCSVNecessaryFields"));
            menu.setOnAction((ActionEvent event) -> {
                editCSVFile(tableDefinition.importNecessaryFields());
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("InputCSVAllFields"));
            menu.setOnAction((ActionEvent event) -> {
                editCSVFile(tableDefinition.importAllFields());
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void editCSVFile(List<String> fields) {
        TextEditerController controller
                = (TextEditerController) openStage(CommonValues.TextEditerFxml);
        controller.hideLeftPane();
        controller.hideRightPane();
        if (fields == null || fields.isEmpty()) {
            return;
        }
        String header = "", line = "", separator;
        for (String field : fields) {
            separator = header.isEmpty() ? "" : ",";
            header += separator + message(field);
            line += separator;
        }
        controller.setMainArea(header + "\n" + line + "\n" + line + "\n" + line + "\n");
    }

    /*
        get/set
     */
    public BaseTable getTableDefinition() {
        return tableDefinition;
    }

    public void setTableDefinition(BaseTable tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

}
