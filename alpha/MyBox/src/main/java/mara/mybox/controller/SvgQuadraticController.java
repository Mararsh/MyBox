package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
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
public class SvgQuadraticController extends BaseSvgShapeController {

    @FXML
    protected ControlQuadratic quadraticController;

    @Override
    public void initMore() {
        try {
            shapeName = message("QuadraticCurve");
            quadraticController.setParameters(this);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean elementToShape(Element node) {
        return false;
    }

    @Override
    public void showShape() {
        showMaskQuadratic();
    }

    @Override
    public void setShapeInputs() {
        quadraticController.loadValues();
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskQuadraticData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("path");
            }
            shapeElement.setAttribute("d", maskQuadraticData.pathAbs());
            return true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        return quadraticController.pickValues();
    }


    /*
        static
     */
    public static SvgQuadraticController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgQuadraticController controller = (SvgQuadraticController) WindowTools.childStage(
                    editor, Fxmls.SvgQuadraticFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
