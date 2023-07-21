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
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class SvgEditShapeController extends BaseChildController {

    protected SvgEditorController editor;
    protected TreeItem<XmlTreeNode> treeItem;
    protected SVG svg;

    @FXML
    protected Label parentLabel;
    @FXML
    protected ControlSvgShape shapeController;

    public SvgEditShapeController() {
        baseTitle = message("SvgAddShape");
    }

    public void setParameters(SvgEditorController editorController, TreeItem<XmlTreeNode> item) {
        try {
            editor = editorController;
            treeItem = item;
            svg = new SVG(editor.treeController.doc);

            String hierarchyNumber = treeItem.getValue().hierarchyNumber();
            String info = editorController.sourceFile != null
                    ? editorController.sourceFile.getAbsolutePath() + "   " : "";
            parentLabel.setText(message("AddInto") + ": " + info + hierarchyNumber);

            shapeController.editShape(editor, (Element) item.getValue().getNode());

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
            Node newNode = editor.treeController.doc.importNode(shapeController.element, true);
            treeItem.getValue().getNode().appendChild(newNode);
            TreeItem<XmlTreeNode> newItem = new TreeItem(new XmlTreeNode(newNode));
            treeItem.getChildren().add(newItem);

            close();

            editor.treeController.focusItem(newItem);
            editor.domChanged(true);
            editor.popInformation(message("CreatedSuccessfully"));

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
