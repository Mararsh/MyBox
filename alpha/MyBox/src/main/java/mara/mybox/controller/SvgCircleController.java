package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-12-29
 * @License Apache License Version 2.0
 */
public class SvgCircleController extends BaseSvgShapeController {

    @FXML
    protected ControlCircle circleController;

    public SvgCircleController() {
        baseTitle = message("Circle");
    }

    @Override
    public void initMore() {
        try {
            circleController.setParameters(this);
            infoLabel.setText(message("ShapeDragMoveTips"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setShape() {
        circleController.loadValues();
    }

    @Override
    public boolean shape2Element() {
        try {
            if (element == null) {
                element = doc.createElement("circle");
            }
            element.setAttribute("cx", scaleValue(maskCircleData.getCenterX()));
            element.setAttribute("cy", scaleValue(maskCircleData.getCenterY()));
            element.setAttribute("r", scaleValue(maskCircleData.getRadius()));
            return true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        return circleController.pickValues();
    }


    /*
        static
     */
    public static SvgCircleController open(SvgEditorController editor, TreeItem<XmlTreeNode> item) {
        try {
            if (editor == null) {
                return null;
            }
            SvgCircleController controller = (SvgCircleController) WindowTools.childStage(
                    editor, Fxmls.SvgCircleFxml);
            controller.setParameters(editor, item);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
