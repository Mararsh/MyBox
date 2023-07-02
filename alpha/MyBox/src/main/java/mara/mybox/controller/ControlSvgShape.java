package mara.mybox.controller;

import java.awt.Rectangle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class ControlSvgShape extends BaseImageController {
    
    protected SVG svg;
    protected Rectangle svgRect;
    protected Document doc;
    protected Node parentNode;
    protected Element newShape;
    protected WebEngine webEngine;
    protected double width, height;
    protected float bgOpacity;
    protected int strokeWidth;
    
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
    protected Tab shapeTab, styleTab, penTab, xmlTab;
    @FXML
    protected ScrollPane shapePane;
    @FXML
    protected VBox shapeBox, rectangleBox, circleBox, ellipseBox, lineBox,
            polylineBox, polygonBox, pathBox;
    @FXML
    protected TextField circleCenterXInput, circleCenterYInput, circleRadiusInput,
            rectXInput, rectYInput, rectWidthInput, rectHeightInput, dashInput;
    @FXML
    protected TextArea pathArea, styleArea;
    @FXML
    protected ComboBox<String> strokeWidthSelector;
    @FXML
    protected CheckBox fillCheck;
    @FXML
    protected ControlColorSet fillColorController, strokeColorController;
    @FXML
    protected ControlSvgView viewController;
    
    @Override
    public void initControls() {
        try {
            super.initControls();
            
            elementType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    makeShape();
                }
            });
            
            strokeWidth = UserConfig.getInt(baseName + "StrokeWidth", 2);
            strokeWidthSelector.getItems().addAll(
                    "2", "1", "3", "4", "0", "6", "10", "15", "20"
            );
            strokeWidthSelector.setValue(strokeWidth + "");
            strokeWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            strokeWidth = v;
                            UserConfig.setInt(baseName + "StrokeWidth", v);
                            strokeWidthSelector.getEditor().setStyle(null);
                        } else {
                            strokeWidthSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        strokeWidthSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });
            
            strokeColorController.init(this, baseName + "StrokeColor", Color.BLACK);
            fillColorController.init(this, baseName + "FillColor", Color.TRANSPARENT);
            
            fillCheck.setSelected(false);
            
            shapeBox.getChildren().remove(selectPane);
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public void setParameters(Document inDoc, Node parent, Rectangle rect) {
        try {
            doc = (Document) inDoc.cloneNode(true);
            String hierarchyNumber = XmlTools.hierarchyNumber(parent);
            parentNode = XmlTools.find(doc, hierarchyNumber);
            svg = new SVG(doc);
            svgRect = rect;
            makeShape();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public void newDoc() {
        try {
            doc = SvgTools.blankDoc();
            svg = new SVG(doc);
            parentNode = svg.getSvgNode();
            makeShape();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public void makeShape() {
        try {
            if (doc == null || parentNode == null) {
                return;
            }
            if (svg == null) {
                svg = new SVG(doc);
            }
            try {
                parentNode.removeChild(newShape);
            } catch (Exception e) {
            }
            width = svg.getWidth();
            if (width <= 0) {
                if (svg.getViewBox() != null) {
                    width = svg.getViewBox().getWidth();
                }
                if (width <= 0 && svgRect != null) {
                    width = svgRect.getWidth();
                }
            }
            height = svg.getHeight();
            if (height <= 0) {
                if (svg.getViewBox() != null) {
                    height = svg.getViewBox().getHeight();
                }
                if (height <= 0 && svgRect != null) {
                    height = svgRect.getHeight();
                }
            }
            
            if (rectRadio.isSelected()) {
                shapePane.setContent(rectangleBox);
                newShape = doc.createElement("rect");
                newShape.setAttribute("x", width / 4 + "");
                newShape.setAttribute("y", height / 4 + "");
                newShape.setAttribute("width", width / 2 + "");
                newShape.setAttribute("height", height / 2 + "");
                
                rectXInput.setText(newShape.getAttribute("x"));
                rectYInput.setText(newShape.getAttribute("y"));
                rectWidthInput.setText(newShape.getAttribute("width"));
                rectHeightInput.setText(newShape.getAttribute("height"));
                
            } else if (circleRadio.isSelected()) {
                shapePane.setContent(circleBox);
                newShape = doc.createElement("circle");
                newShape.setAttribute("cx", width / 2 + "");
                newShape.setAttribute("cy", height / 2 + "");
                newShape.setAttribute("r", Math.min(width, height) / 4 + "");
                
                circleCenterXInput.setText(newShape.getAttribute("cx"));
                circleCenterYInput.setText(newShape.getAttribute("cy"));
                circleRadiusInput.setText(newShape.getAttribute("r"));
                
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
            refreshStyle(shapeBox);
            
            newShape.setAttribute("stroke", strokeColorController.rgb());
            newShape.setAttribute("stroke-width", strokeWidth + "");
            if (fillCheck.isSelected()) {
                newShape.setAttribute("fill", fillColorController.rgb());
            } else {
                newShape.setAttribute("fill", "none");
            }
            
            parentNode.appendChild(newShape);
            viewController.loadDoc(doc, newShape);
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public Element newElement() {
        try {
            
            return newShape;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
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
    @Override
    public boolean synchronizeAction() {
        makeShape();
        return true;
    }
    
    @FXML
    @Override
    public void clearAction() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == shapeTab) {
            
        }
        newElement();
    }
    
    @FXML
    public void popXml() {
        
    }
    
    @FXML
    public void popHtml() {
        
    }
    
}
