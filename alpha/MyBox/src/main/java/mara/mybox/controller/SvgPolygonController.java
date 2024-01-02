package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.DoublePoint;
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
public class SvgPolygonController extends BaseSvgShapeController {

    @FXML
    protected ControlPoints pointsController;

    @Override
    public void initMore() {
        try {
            shapeName = message("Polygon");

            addPointCheck.setSelected(true);
            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;
            popItemMenu = popLineMenuCheck.isSelected();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean elementToShape(Element node) {
        try {
            List<DoublePoint> list = DoublePoint.parseImageCoordinates(node.getAttribute("points"));
            if (list != null && !list.isEmpty()) {
                maskPolygonData.setAll(list);
            } else {
                maskPolygonData.clear();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void showShape() {
        showMaskPolygon();
    }

    @Override
    public void setShapeInputs() {
        if (maskPolygonData != null) {
            pointsController.loadList(maskPolygonData.getPoints());
        } else {
            pointsController.loadList(null);
        }
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskPolygonData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("polygon");
            }
            shapeElement.setAttribute("points", DoublePoint.toText(maskPolygonData.getPoints(), 3, " "));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        try {
            maskPolygonData.setAll(pointsController.getPoints());
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
        pointsController.removeLastItem();
        goShape();
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        pointsController.clear();
        goShape();
    }


    /*
        static
     */
    public static SvgPolygonController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgPolygonController controller = (SvgPolygonController) WindowTools.childStage(
                    editor, Fxmls.SvgPolygonFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
