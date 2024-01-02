package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-12-31
 * @License Apache License Version 2.0
 */
public class SvgRectangleController extends BaseSvgShapeController {

    @FXML
    protected ControlRectangle rectController;

    @Override
    public void initMore() {
        try {
            shapeName = message("Rectangle");
            rectController.setParameters(this);

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
            float x, y, w, h, rx = 0, ry = 0;
            try {
                x = Float.parseFloat(node.getAttribute("x"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(node.getAttribute("y"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                w = Float.parseFloat(node.getAttribute("width"));
            } catch (Exception e) {
                w = -1f;
            }
            if (w <= 0) {
                popError(message("InvalidParameter") + ": " + message("Width"));
                return false;
            }
            try {
                h = Float.parseFloat(node.getAttribute("height"));
            } catch (Exception e) {
                h = -1f;
            }
            if (h <= 0) {
                popError(message("InvalidParameter") + ": " + message("Height"));
                return false;
            }
            try {
                rx = Float.parseFloat(node.getAttribute("rx"));
            } catch (Exception e) {
            }
            try {
                ry = Float.parseFloat(node.getAttribute("ry"));
            } catch (Exception e) {
            }
            maskRectangleData = DoubleRectangle.xywh(x, y, w, h);
            maskRectangleData.setRoundx(rx);
            maskRectangleData.setRoundy(ry);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void initShape() {
        rectController.setRoundList();
    }

    @Override
    public void showShape() {
        showMaskRectangle();
    }

    @Override
    public void setShapeInputs() {
        rectController.loadValues();
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskRectangleData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("rect");
            }
            shapeElement.setAttribute("x", scaleValue(maskRectangleData.getX()));
            shapeElement.setAttribute("y", scaleValue(maskRectangleData.getY()));
            shapeElement.setAttribute("width", scaleValue(maskRectangleData.getWidth()));
            shapeElement.setAttribute("height", scaleValue(maskRectangleData.getHeight()));
            shapeElement.setAttribute("rx", scaleValue(maskRectangleData.getRoundx()));
            shapeElement.setAttribute("ry", scaleValue(maskRectangleData.getRoundy()));

            return true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        return rectController.pickValues();
    }


    /*
        static
     */
    public static SvgRectangleController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgRectangleController controller = (SvgRectangleController) WindowTools.childStage(
                    editor, Fxmls.SvgRectangleFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
