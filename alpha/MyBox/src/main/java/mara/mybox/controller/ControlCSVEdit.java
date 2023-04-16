package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

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
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu = new MenuItem(Languages.message("InputCSVNecessaryFields"));
            menu.setOnAction((ActionEvent event) -> {
                editCSVFile(tableDefinition.importNecessaryFields());
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("InputCSVAllFields"));
            menu.setOnAction((ActionEvent event) -> {
                editCSVFile(tableDefinition.importAllFields());
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            parentController.popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void editCSVFile(List<String> fields) {
        TextEditorController controller
                = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.hideLeftPane();
        controller.hideRightPane();
        if (fields == null || fields.isEmpty()) {
            return;
        }
        String header = "", line = "", separator;
        for (String field : fields) {
            separator = header.isEmpty() ? "" : ",";
            header += separator + Languages.message(field);
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
