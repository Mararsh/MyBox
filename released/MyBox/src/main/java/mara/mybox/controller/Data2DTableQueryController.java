package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.table.TableNodeSQL;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-14
 * @License Apache License Version 2.0
 */
public class Data2DTableQueryController extends ControlDataSQL {

    protected BaseData2DLoadController data2DController;
    protected Data2D data2D;

    @Override
    public void setControlsStyle() {
        super.setControlsStyle();
        NodeStyleTools.setTooltip(listButton, new Tooltip(message("Names")));
    }

    public void setParameters(BaseData2DLoadController controller) {
        try {
            data2DController = controller;
            data2D = data2DController.data2D;

            parentController = data2DController;
            baseName = data2DController.baseName;
            nodeTable = new TableNodeSQL();

            super.initEditor();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void selectAction() {
        DataSelectSQLController.open(this);
    }

    @FXML
    @Override
    public void saveAction() {
        ControlDataSQL.open(this, sqlArea.getText());
    }

    @FXML
    @Override
    protected void tableDefinition() {
        if (data2D != null) {
            String info = data2D.dataInfo();
            if (info != null && !info.isBlank()) {
                HtmlPopController.showHtml(this, HtmlWriteTools.table(info));
            }
        } else {
            DatabaseTableDefinitionController.open();
        }
    }

    @FXML
    protected void popColumnNames(Event event) {
        if (UserConfig.getBoolean("ColumnNamesPopWhenMouseHovering", false)) {
            showColumnNames(event);
        }
    }

    @FXML
    protected void showColumnNames(Event event) {
        PopTools.popColumnNames(this, event, sqlArea, "ColumnNames", data2D);
    }

    /*
        static
     */
    public static Data2DTableQueryController open(BaseData2DLoadController parent) {
        try {
            Data2DTableQueryController controller = (Data2DTableQueryController) WindowTools.branchStage(parent, Fxmls.Data2DTableQueryFxml);
            controller.setParameters(parent);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
