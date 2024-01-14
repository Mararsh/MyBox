package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2024-1-2
 * @License Apache License Version 2.0
 */
public class SvgEllipseController extends BaseSvgShapeController {

    @FXML
    protected ControlEllipse ellipseController;

    @Override
    public void initMore() {
        try {
            shapeName = message("Ellipse");
            ellipseController.setParameters(this);

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
            float cx, cy, rx, ry;
            try {
                cx = Float.parseFloat(node.getAttribute("cx"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                cy = Float.parseFloat(node.getAttribute("cy"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                rx = Float.parseFloat(node.getAttribute("rx"));
            } catch (Exception e) {
                rx = -1f;
            }
            if (rx <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            try {
                ry = Float.parseFloat(node.getAttribute("ry"));
            } catch (Exception e) {
                ry = -1f;
            }
            if (ry <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            maskEllipseData = DoubleEllipse.ellipse(cx, cy, rx, ry);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void showShape() {
        showMaskEllipse();
    }

    @Override
    public void setShapeInputs() {
        ellipseController.loadValues();
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskEllipseData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("ellipse");
            }
            shapeElement.setAttribute("cx", scaleValue(maskEllipseData.getCenterX()));
            shapeElement.setAttribute("cy", scaleValue(maskEllipseData.getCenterY()));
            shapeElement.setAttribute("rx", scaleValue(maskEllipseData.getRadiusX()));
            shapeElement.setAttribute("ry", scaleValue(maskEllipseData.getRadiusY()));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        return ellipseController.pickValues();
    }


    /*
        static
     */
    public static SvgEllipseController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgEllipseController controller = (SvgEllipseController) WindowTools.childStage(
                    editor, Fxmls.SvgEllipseFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
