package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.DoublePath;
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
public class SvgPathController extends BaseSvgShapeController {

    protected DoublePath initData;

    @FXML
    protected ControlPath2D pathController;

    @Override
    public void initMore() {
        try {
            shapeName = message("SVGPath");

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setInitData(DoublePath initData) {
        this.initData = initData;
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            if (initData != null) {
                loadSvgPath(initData);
                initData = null;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean elementToShape(Element node) {
        try {
            String d = node.getAttribute("d");
            maskPathData = new DoublePath(this, d);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void showShape() {
        showMaskPath();
    }

    @Override
    public void setShapeInputs() {
        if (maskPathData != null) {
            pathController.loadPath(maskPathData.getContent());
        } else {
            pathController.loadPath(null);
        }
    }

    @Override
    public boolean shape2Element() {
        try {
            if (maskPathData == null) {
                return false;
            }
            if (shapeElement == null) {
                shapeElement = doc.createElement("path");
            }
            shapeElement.setAttribute("d", maskPathData.getContent());
            return true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @Override
    public boolean pickShape() {
        try {
            if (!pathController.pickValue()) {
                return false;
            }
            maskPathData.setContent(pathController.getText());
            maskPathData.setSegments(pathController.getSegments());
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
        pathController.removeLastItem();
        goShape();
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        pathController.clear();
        goShape();
    }

    /*
        static
     */
    public static SvgPathController drawShape(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgPathController controller = (SvgPathController) WindowTools.childStage(
                    editor, Fxmls.SvgPathFxml);
            controller.setParameters(editor, item, element);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static SvgPathController loadPath(SvgEditorController editor,
            TreeItem<XmlTreeNode> item, DoublePath pathData) {
        try {
            if (editor == null || item == null) {
                return null;
            }
            SvgPathController controller = (SvgPathController) WindowTools.childStage(
                    editor, Fxmls.SvgPathFxml);
            controller.setInitData(pathData);
            controller.setParameters(editor, item, null);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
