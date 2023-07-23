package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import mara.mybox.data.SVG;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class SvgEditShapeController extends BaseChildController {

    protected SvgEditorController editor;
    protected TreeItem<XmlTreeNode> treeItem;
    protected SVG svg;
    protected Element element;

    @FXML
    protected Label parentLabel;
    @FXML
    protected ControlSvgShape shapeController;

    public SvgEditShapeController() {
        baseTitle = message("SvgEditShape");
    }

    public void setParameters(SvgEditorController editorController, TreeItem<XmlTreeNode> item) {
        try {
            editor = editorController;
            treeItem = item;
            svg = new SVG(editor.treeController.doc);

            String hierarchyNumber = treeItem.getValue().hierarchyNumber();
            String info = editorController.sourceFile != null
                    ? editorController.sourceFile.getAbsolutePath() + "   " : "";
            parentLabel.setText(message("Edit") + ": " + info + " - "
                    + message("HierarchyNumber") + ": " + hierarchyNumber);

            element = (Element) item.getValue().getNode();
            shapeController.editShape(editor, element);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (treeItem == null) {
                close();
                return;
            }
            shapeController.synchronizeAction();
            if (shapeController.element == null) {
                popError(message("NoData"));
                return;
            }
            if (treeItem.getParent() == null) {
                editor.treeController.loadNode(shapeController.element);
            } else {
                treeItem.getParent().getValue().getNode().replaceChild(shapeController.element, element);
                treeItem.setValue(new XmlTreeNode(shapeController.element));
                editor.treeController.focusItem(treeItem);
                editor.domChanged(true);
            }

            close();
            editor.popInformation(message("UpdateSuccessfully"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static SvgEditShapeController open(SvgEditorController editorController, TreeItem<XmlTreeNode> item) {
        SvgEditShapeController controller = (SvgEditShapeController) WindowTools.openStage(Fxmls.SvgEditShapeFxml);
        if (controller != null) {
            controller.setParameters(editorController, item);
            controller.requestMouse();
        }
        return controller;
    }

}
