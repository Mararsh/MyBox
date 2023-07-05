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
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class SvgAddShapeController extends BaseChildController {

    protected SvgEditorController editor;
    protected TreeItem<XmlTreeNode> treeItem;
    protected SVG svg;

    @FXML
    protected Label parentLabel;
    @FXML
    protected ControlSvgShape shapeController;

    public SvgAddShapeController() {
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

            shapeController.setParameters(editor, hierarchyNumber);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            if (shapeController.shape == null) {
                popError(message("NoData"));
                return;
            }
            Node newNode = editor.treeController.doc.importNode(shapeController.shape, true);
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
    public static SvgAddShapeController open(SvgEditorController editorController, TreeItem<XmlTreeNode> item) {
        SvgAddShapeController controller = (SvgAddShapeController) WindowTools.openChildStage(
                editorController.getMyWindow(), Fxmls.SvgAddShapeFxml);
        if (controller != null) {
            controller.setParameters(editorController, item);
            controller.requestMouse();
        }
        return controller;
    }

}
