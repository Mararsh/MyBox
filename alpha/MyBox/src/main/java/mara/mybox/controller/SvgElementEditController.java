package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-20
 * @License Apache License Version 2.0
 */
public class SvgElementEditController extends ImageViewerController {

    protected SvgEditorController editorController;
    protected TreeItem<XmlTreeNode> treeItem;
    protected Node node;

    protected int scaleWidth, scaleHeight, canvasWidth, canvasHeight, repeatH, repeatV,
            interval, margin;

    @FXML
    protected VBox setBox, styleBox, rectangleBox, circleBox, ellipseBox, lineBox,
            polylineBox, polygonBox, pathBox;

    public SvgElementEditController() {
        baseTitle = message("SvgElement");
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(SvgEditorController editorController, TreeItem<XmlTreeNode> treeItem) {
        try {
            this.editorController = editorController;
            this.treeItem = treeItem;

            File tmpFile = SvgTools.textToImage(this, editorController.currentXML);
            loadImageFile(tmpFile);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void drawAction() {
        if (node == null) {
            return;
        }
        int nodeType = node.getNodeType();
        String name = node.getNodeName().toLowerCase();
        if (nodeType == Node.ELEMENT_NODE) {
            switch (name) {
                case "rect":
                    setBox.getChildren().addAll(rectangleBox, styleBox);
                    break;
                case "circle":
                    setBox.getChildren().addAll(circleBox, styleBox);
                    break;
                case "ellipse":
                    setBox.getChildren().addAll(ellipseBox, styleBox);
                    break;
                case "line":
                    setBox.getChildren().addAll(lineBox, styleBox);
                    break;
                case "polyline":
                    setBox.getChildren().addAll(polylineBox, styleBox);
                    break;
                case "polygon":
                    setBox.getChildren().addAll(polygonBox, styleBox);
                    break;
                case "path":
                    setBox.getChildren().addAll(pathBox, styleBox);
                    break;
            }
        }
//        if (setBox.getChildren().isEmpty()) {
//            if (!thisPane.getChildren().contains(baseBox)) {
//                thisPane.getChildren().add(1, baseBox);
//            }
//            super.load(node);
//
//        } else {
//            if (thisPane.getChildren().contains(baseBox)) {
//                thisPane.getChildren().remove(baseBox);
//            }
//            VBox.setVgrow(setBox, Priority.ALWAYS);
//            VBox.setVgrow(styleBox, Priority.ALWAYS);
//            thisPane.setDisable(false);
//        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    @Override
    public boolean keyF6() {
        close();
        return false;
    }


    /*
        static
     */
    public static SvgElementEditController open(SvgEditorController editorController, TreeItem<XmlTreeNode> treeItem) {
        try {
            SvgElementEditController controller = (SvgElementEditController) WindowTools.openChildStage(
                    editorController.getMyWindow(), Fxmls.SvgElementEditFxml);
            if (controller != null) {
                controller.setParameters(editorController, treeItem);
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
