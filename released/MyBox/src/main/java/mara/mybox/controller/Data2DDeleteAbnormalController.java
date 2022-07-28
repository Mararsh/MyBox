package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-7-9
 * @License Apache License Version 2.0
 */
public class Data2DDeleteAbnormalController extends BaseData2DAbnormalController {

    @FXML
    protected Label valuesLabel;
    @FXML
    protected Button dataButton;

    public Data2DDeleteAbnormalController() {
        baseTitle = message("DeleteAbnormalRows");
        TipsLabelKey = "MarkAbnormalValuesTips";
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(clearButton, new Tooltip(message("ClearAbnormalRows")));
            NodeStyleTools.setTooltip(deleteButton, new Tooltip(message("DeleteAbnormalRowsBySelectedRules")));
            NodeStyleTools.setTooltip(dataButton, new Tooltip(message("MarkAbnormalValues")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void loadNull() {
        currentStyle = new Data2DStyle();
        valuesLabel.setText("");
    }

    @Override
    public void loadStyle(Data2DStyle style) {
        if (style == null) {
            loadNull();
            return;
        }
        currentStyle = style;
        String v = "";
    }

    @FXML
    @Override
    public void deleteAction() {

    }

    @FXML
    @Override
    public void clearAction() {

    }


    /*
        static
     */
    public static Data2DDeleteAbnormalController open(ControlData2DEditTable tableController) {
        try {
            Data2DDeleteAbnormalController controller = (Data2DDeleteAbnormalController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DDeleteAbnormalFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
