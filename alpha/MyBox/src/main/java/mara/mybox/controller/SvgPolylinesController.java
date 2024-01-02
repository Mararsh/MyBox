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
public class SvgPolylinesController extends BaseSvgShapeController {

    @FXML
    protected ControlLines linesController;

    @Override
    public void initMore() {
        try {
            shapeName = message("Polylines");

            popItemMenu = popLineMenuCheck.isSelected();
            showAnchors = false;
            addPointWhenClick = false;
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
        showMaskPolylines();
    }

    @Override
    public void setShapeInputs() {
        if (maskPolylinesData != null) {
            linesController.loadList(maskPolylinesData.getLines());
        } else {
            linesController.loadList(null);
        }
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskPolylinesData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("path");
            }
            shapeElement.setAttribute("d", maskPolylinesData.pathAbs());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        try {
            maskPolylinesData.setLines(linesController.getLines());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        linesController.removeLastItem();
        goShape();
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        linesController.clear();
        goShape();
    }


    /*
        static
     */
    public static SvgPolylinesController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgPolylinesController controller = (SvgPolylinesController) WindowTools.childStage(
                    editor, Fxmls.SvgPolylinesFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
