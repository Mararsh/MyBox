package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.DoubleLine;
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
public class SvgLineController extends BaseSvgShapeController {

    @FXML
    protected ControlLine lineController;

    @Override
    public void initMore() {
        try {
            shapeName = message("StraightLine");
            lineController.setParameters(this);

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
            float x1, y1, x2, y2;
            try {
                x1 = Float.parseFloat(node.getAttribute("x1"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x1");
                return false;
            }
            try {
                y1 = Float.parseFloat(node.getAttribute("y1"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y1");
                return false;
            }
            try {
                x2 = Float.parseFloat(node.getAttribute("x2"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x2");
                return false;
            }
            try {
                y2 = Float.parseFloat(node.getAttribute("y2"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y2");
                return false;
            }
            maskLineData = new DoubleLine(x1, y1, x2, y2);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void showShape() {
        showMaskLine();
    }

    @Override
    public void setShapeInputs() {
        lineController.loadValues();
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskLineData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("line");
            }
            shapeElement.setAttribute("x1", scaleValue(maskLineData.getStartX()));
            shapeElement.setAttribute("y1", scaleValue(maskLineData.getStartY()));
            shapeElement.setAttribute("x2", scaleValue(maskLineData.getEndX()));
            shapeElement.setAttribute("y2", scaleValue(maskLineData.getEndY()));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        return lineController.pickValues();
    }


    /*
        static
     */
    public static SvgLineController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgLineController controller = (SvgLineController) WindowTools.childStage(
                    editor, Fxmls.SvgLineFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
