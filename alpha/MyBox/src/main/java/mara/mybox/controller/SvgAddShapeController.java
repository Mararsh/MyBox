package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.data.SVG;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
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
    protected ToggleGroup elementType;
    @FXML
    protected RadioButton rectRadio, circleRadio, ellipseRadio, lineRadio,
            polylineRadio, polygonRadio, pathRadio;
    @FXML
    protected TabPane selectPane;
    @FXML
    protected ScrollPane shapePane;
    @FXML
    protected VBox optionsBox, rectangleBox, circleBox, ellipseBox, lineBox,
            polylineBox, polygonBox, pathBox;
    @FXML
    protected TextArea pathArea, styleArea;
    @FXML
    protected ComboBox<String> strokeWidthSelector;

    public SvgAddShapeController() {
        baseTitle = message("SvgAddShape");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            elementType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkType();
                }
            });

            strokeWidthSelector.getItems().addAll(
                    "2px", "1px", "3px", "4px", "0", "6px", "10px", "15px", "20px"
            );
            strokeWidthSelector.setValue(UserConfig.getString(baseName + "StrokeWidth", "2px"));

            optionsBox.getChildren().remove(selectPane);
            VBox.setVgrow(tabPane, Priority.ALWAYS);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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

            checkType();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkType() {
        try {
            if (rectRadio.isSelected()) {
                shapePane.setContent(rectangleBox);

            } else if (circleRadio.isSelected()) {
                shapePane.setContent(circleBox);

            } else if (ellipseRadio.isSelected()) {
                shapePane.setContent(ellipseBox);

            } else if (lineRadio.isSelected()) {
                shapePane.setContent(lineBox);

            } else if (polylineRadio.isSelected()) {
                shapePane.setContent(polylineBox);

            } else if (polygonRadio.isSelected()) {
                shapePane.setContent(polygonBox);

            } else if (pathRadio.isSelected()) {
                shapePane.setContent(pathBox);

            } else {
                shapePane.setContent(null);

            }
            refreshStyle(shapePane);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popExamplesPathMenu(Event event) {
        if (UserConfig.getBoolean("SvgPathExamplesPopWhenMouseHovering", false)) {
            showExamplesPathMenu(event);
        }
    }

    @FXML
    public void showExamplesPathMenu(Event event) {
        PopTools.popValues(this, pathArea, "SvgPathExamples", HelpTools.svgPathExamples(), event);
    }

    @FXML
    public void popExamplesStyleMenu(Event event) {
        if (UserConfig.getBoolean("SvgStyleExamplesPopWhenMouseHovering", false)) {
            showExamplesStyleMenu(event);
        }
    }

    @FXML
    public void showExamplesStyleMenu(Event event) {
        PopTools.popValues(this, styleArea, "SvgStyleExamples", HelpTools.svgStyleExamples(), event);
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("SvgHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.svgHelps(true));
    }

    @FXML
    public void metaAction() {

    }

    @FXML
    public void popHtml() {

    }

    @FXML
    @Override
    public void goAction() {

    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (treeItem == null) {
                close();
                return;
            }
            XmlTreeNode treeNode = treeItem.getValue();
            if (treeNode == null) {
                close();
                return;
            }
            Node node = treeNode.getNode();
            if (node == null) {
                close();
                return;
            }

            Node newNode = null;
//            if (elementRadio.isSelected()) {
//                String name = nameInput.getText();
//                if (name == null || name.isBlank()) {
//                    popError(message("InvalidParameter") + ": " + message("Name"));
//                    return;
//                }
//                Element element = doc.createElement(name.trim());
//                for (Node attr : attributesData) {
//                    element.setAttribute(attr.getNodeName(), attr.getNodeValue());
//                }
//                newNode = element;
//
//            } else if (textRadio.isSelected()) {
//                newNode = doc.createTextNode(value);
//
//            } else if (cdataRadio.isSelected()) {
//                newNode = doc.createCDATASection(value);
//
//            } else if (commentRadio.isSelected()) {
//                newNode = doc.createComment(value);
//            } else {
//                return;
//            }
            TreeItem<XmlTreeNode> newItem = new TreeItem(new XmlTreeNode(newNode));
            node.appendChild(newNode);
            treeItem.getChildren().add(newItem);

            close();

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
