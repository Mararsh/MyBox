package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-20
 * @License Apache License Version 2.0
 */
public class SvgElementEditController extends ImageViewerController {

    protected SvgEditorController editorController;
    protected TreeItem<XmlTreeNode> treeItem;
    protected Node root, node;
    protected float opacity;

    protected int scaleWidth, scaleHeight, canvasWidth, canvasHeight, repeatH, repeatV,
            interval, margin;

    @FXML
    protected Label infoLabel;
    @FXML
    protected FlowPane typePane;
    @FXML
    protected RadioButton rectRadio, circleRadio, ellipseRadio, lineRadio,
            polylineRadio, polygonRadio, pathRadio;
    @FXML
    protected VBox setBox, styleBox, rectangleBox, circleBox, ellipseBox, lineBox,
            polylineBox, polygonBox, pathBox;
    @FXML
    protected ComboBox<String> opacitySelector;
    @FXML
    protected ImageView shapeView;

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

            opacitySelector.getItems().addAll(
                    Arrays.asList(message("ScopeTransparency0.5"), message("ScopeTransparency0"), message("ScopeTransparency1"),
                            message("ScopeTransparency0.2"), message("ScopeTransparency0.8"), message("ScopeTransparency0.3"),
                            message("ScopeTransparency0.6"), message("ScopeTransparency0.7"), message("ScopeTransparency0.9"),
                            message("ScopeTransparency0.4"))
            );
            opacitySelector.getSelectionModel().select(UserConfig.getString(baseName + "ViewTransparency", message("ScopeTransparency0.5")));
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    checkopacity();
                }
            });
            opacity = 0.5f;
            checkopacity();

            imageView.toBack();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkopacity() {
        try {
            String newVal = opacitySelector.getValue();
            if (newVal == null) {
                return;
            }
            float f = Float.parseFloat(newVal.substring(0, 3));
            if (f >= 0 && f <= 1.0) {
                opacity = 1 - f;
                imageView.setOpacity(opacity);
                ValidationTools.setEditorNormal(opacitySelector);
                UserConfig.setString(baseName + "ViewTransparency", newVal);
            } else {
                ValidationTools.setEditorBadStyle(opacitySelector);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(opacitySelector);
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        super.viewSizeChanged(change);
        shapeView.setLayoutX(imageView.getLayoutX());
        shapeView.setLayoutY(imageView.getLayoutY());
        shapeView.setFitWidth(imageView.getFitWidth());
        shapeView.setFitHeight(imageView.getFitHeight());
    }

    public void setParameters(SvgEditorController editorController, TreeItem<XmlTreeNode> treeItem) {
        try {
            this.editorController = editorController;
            this.treeItem = treeItem;

            String hierarchyNumber = treeItem.getValue().hierarchyNumber();
            String info = editorController.sourceFile != null
                    ? editorController.sourceFile.getAbsolutePath() + "\n" : "";
            infoLabel.setText(info + hierarchyNumber);
            root = treeItem.getValue().getNode().getOwnerDocument().cloneNode(true);
            node = XmlTools.find(root, hierarchyNumber);
            if (node != null && node.getParentNode() != null) {
                node.getParentNode().removeChild(node);
                node = node.cloneNode(true);
                String name = node.getNodeName().toLowerCase();
                switch (name) {
                    case "rect":
                        rectRadio.setSelected(true);
                        break;
                    case "circle":
                        circleRadio.setSelected(true);
                        break;
                    case "ellipse":
                        ellipseRadio.setSelected(true);
                        break;
                    case "line":
                        lineRadio.setSelected(true);
                        break;
                    case "polyline":
                        polylineRadio.setSelected(true);
                        break;
                    case "polygon":
                        polygonRadio.setSelected(true);
                        break;
                    case "path":
                        pathRadio.setSelected(true);
                        break;
                }
                loadImage(root, node);
            }

            typePane.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImage(Node root, Node node) {
        if (node == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }

        task = new SingletonCurrentTask<Void>(this) {

            Image bgImage, shapeImage;

            @Override
            protected boolean handle() {
                try {
                    int width = editorController.svgOptionsController.width;
                    int height = editorController.svgOptionsController.height;
                    bgImage = SvgTools.nodeToFxImage(myController, width, height, root);
                    width = (int) bgImage.getWidth();
                    height = (int) bgImage.getHeight();
                    String shape = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\""
                            + width + "\" height=\"" + height + "\" >"
                            + SvgTools.transform(node, false) + "</svg>";
                    shapeImage = SvgTools.textToFxImage(myController, width, height, shape);
                    return shapeImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                shapeView.setImage(shapeImage);
                loadImage(bgImage);

            }
        };
        start(task);
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
