package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-12-29
 * @License Apache License Version 2.0
 */
public class SvgCircleController extends BaseSvgShapeController {

    @FXML
    protected ControlCircle circleController;

    @Override
    public void initMore() {
        try {
            shapeName = message("Circle");
            circleController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean elementToShape(Element node) {
        try {
            float x, y, r;
            try {
                x = Float.parseFloat(node.getAttribute("cx"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(node.getAttribute("cy"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                r = Float.parseFloat(node.getAttribute("r"));
            } catch (Exception e) {
                r = -1f;
            }
            if (r <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            maskCircleData = new DoubleCircle(x, y, r);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void showShape() {
        showMaskCircle();
    }

    @Override
    public void setShapeInputs() {
        circleController.loadValues();
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskCircleData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("circle");
            }
            shapeElement.setAttribute("cx", scaleValue(maskCircleData.getCenterX()));
            shapeElement.setAttribute("cy", scaleValue(maskCircleData.getCenterY()));
            shapeElement.setAttribute("r", scaleValue(maskCircleData.getRadius()));
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
    public static SvgCircleController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgCircleController controller = (SvgCircleController) WindowTools.childStage(
                    editor, Fxmls.SvgCircleFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
