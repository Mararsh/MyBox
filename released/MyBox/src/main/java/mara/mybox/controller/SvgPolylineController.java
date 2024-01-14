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
public class SvgPolylineController extends BaseSvgShapeController {

    @FXML
    protected ControlPoints pointsController;

    @Override
    public void initMore() {
        try {
            shapeName = message("Polyline");

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
                maskPolylineData.setAll(list);
            } else {
                maskPolylineData.clear();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void showShape() {
        showMaskPolyline();
    }

    @Override
    public void setShapeInputs() {
        if (maskPolylineData != null) {
            pointsController.loadList(maskPolylineData.getPoints());
        } else {
            pointsController.loadList(null);
        }
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskPolylineData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("polyline");
            }
            shapeElement.setAttribute("points", DoublePoint.toText(maskPolylineData.getPoints(), 3, " "));
            return true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        try {
            maskPolylineData.setAll(pointsController.getPoints());
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
    public static SvgPolylineController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgPolylineController controller = (SvgPolylineController) WindowTools.childStage(
                    editor, Fxmls.SvgPolylineFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
