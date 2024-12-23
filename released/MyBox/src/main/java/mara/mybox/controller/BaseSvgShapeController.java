package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.data.DoublePath;
import mara.mybox.data.SVG;
import mara.mybox.data.ShapeStyle;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public abstract class BaseSvgShapeController extends BaseShapeController {

    protected SvgEditorController editor;
    protected TreeItem<XmlTreeNode> treeItem;
    protected SVG svg;
    protected Document doc;
    protected Element srcElement, shapeElement;
    protected String shapeName;

    @FXML
    protected Tab shapeTab, styleTab, xmlTab;
    @FXML
    protected Label infoLabel;
    @FXML
    protected TextArea styleArea, xmlArea;
    @FXML
    protected CheckBox wrapXmlCheck;
    @FXML
    protected Label xmlLabel;
    @FXML
    protected ControlStroke strokeController;
    @FXML
    protected ControlSvgViewOptions optionsController;

    public abstract boolean elementToShape(Element node);

    public abstract void setShapeInputs();

    public abstract void showShape();

    public abstract boolean pickShape();

    public abstract boolean shape2Element();

    public void setParameters(SvgEditorController controller,
            TreeItem<XmlTreeNode> item, Element element) {
        try {
            if (controller == null) {
                close();
                return;
            }
            editor = controller;
            treeItem = item;
            Document srcDoc = editor.treeController.doc;
            doc = (Document) srcDoc.cloneNode(true);
            svg = new SVG(doc);
            srcElement = element;
            shapeElement = null;

            clearMask();
            resetShapeOptions();

            initMore();

            String hierarchyNumber = treeItem.getValue().hierarchyNumber();
            String info = element != null ? message("Edit") : message("Add");
            info += "." + shapeName + "\n";
            info += editor.sourceFile != null
                    ? (message("File") + ": " + editor.sourceFile.getName() + "\n") : "";
            info += message("HierarchyNumber") + ": " + hierarchyNumber;
            infoLabel.setText(info);

            baseTitle = message("SVGEditor") + "-" + info.replaceAll("\n", " - ");
            setTitle(baseTitle);

            recoverButton.setVisible(srcElement != null);

            strokeController.setParameters(this);
            shapeStyle = strokeController.pickValues();

            optionsController.bgBox.getChildren().remove(optionsController.colorPane);
            if (task != null) {
                task.cancel();
            }
            task = new FxSingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    try {
                        optionsController.loadExcept(this, srcDoc, srcElement);
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    loadBackGround();
                }

            };
            start(task);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMore() {

    }

    public String scaleValue(double d) {
        return DoubleTools.imageScale(d) + "";
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (editor == null || !editor.isShowing() || treeItem == null) {
                close();
                return;
            }
            if (shapeElement == null) {
                popError(message("NoData"));
                return;
            }
            if (srcElement == null) {
                Node newNode = editor.treeController.doc.importNode(shapeElement, true);
                treeItem.getValue().getNode().appendChild(newNode);
                TreeItem<XmlTreeNode> newItem = new TreeItem(new XmlTreeNode(newNode));
                treeItem.getChildren().add(newItem);

                close();
                editor.treeController.focusItem(newItem);
                editor.domChanged(true);
                editor.popInformation(message("CreatedSuccessfully"));
            } else {
                if (treeItem.getParent() == null) {
                    editor.treeController.loadNode(shapeElement);
                } else {
                    treeItem.getParent().getValue().getNode().replaceChild(shapeElement, srcElement);
                    treeItem.setValue(new XmlTreeNode(shapeElement));
                }

                close();
                editor.treeController.focusItem(treeItem);
                editor.domChanged(true);
                editor.popInformation(message("UpdateSuccessfully"));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        view
     */
    @FXML
    public void goView() {
        if (!optionsController.pickValues()) {
            return;
        }
        loadBackGround();
    }

    public void loadBackGround() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image bgImage;

            @Override
            protected boolean handle() {
                try {
                    bgImage = null;
                    File tmpFile = optionsController.toImage(this);
                    if (tmpFile != null && tmpFile.exists()) {
                        bgImage = FxImageTools.readImage(this, tmpFile);
                        FileDeleteTools.delete(tmpFile);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (image == null) {
                    loadImage(bgImage);
                } else {
                    image = bgImage;
                    imageView.setImage(bgImage);
                    showShape();
                }
                imageView.setOpacity(optionsController.bgOpacity);
            }

        };
        start(task);
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            return makeSvg();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean makeSvg() {
        try {
            clearMask();
            initShape();
            strokeController.setWidthList();

            if (srcElement != null) {
                shapeElement = (Element) srcElement.cloneNode(true);
                elementToShape(shapeElement);
                elementToStyle(shapeElement);
                setShapeInputs();
                loadXml(shapeElement);
                showShape();

            } else {
                shapeElement = null;
                shapeStyle = strokeController.pickValues();
                showShape();
                shape2Element();
                style2Element();
                setShapeInputs();
                loadXml(shapeElement);
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        shape
     */
    public void initShape() {
    }

    @FXML
    public void goShape() {
        if (pickShape()) {
            maskShapeDataChanged();
        }
    }

    @Override
    public void maskShapeDataChanged() {
        if (shape2Element()) {
            loadShape();
        }
    }

    public void loadShape() {
        setShapeInputs();
        loadXml(shapeElement);
        showShape();
    }

    @FXML
    @Override
    public void recoverAction() {
        makeSvg();
    }

    @Override
    public void popSvgPath(DoublePath pathData) {
        SvgPathController.loadPath(editor, treeItem, pathData);
    }

    /*
        style
     */
    public void elementToStyle(Element node) {
        try {
            if (node == null) {
                return;
            }
            try {
                strokeController.colorController.setColor(Color.web(node.getAttribute("stroke")));
            } catch (Exception e) {
                strokeController.colorController.setColor(Color.BLACK);
            }
            try {
                strokeController.widthSelector.setValue(Float.valueOf(node.getAttribute("stroke-width")) + "");
            } catch (Exception e) {
                strokeController.widthSelector.setValue("2");
            }

            String v = node.getAttribute("stroke-dasharray");
            strokeController.dashCheck.setSelected(v != null && !v.isBlank());
            strokeController.arrayInput.setText(v);

            v = node.getAttribute("stroke-linecap");
            if (v == null || v.isBlank()) {
                strokeController.capButtRadio.setSelected(false);
                strokeController.capSquareRadio.setSelected(false);
                strokeController.capRoundRadio.setSelected(false);
            } else {
                if ("square".equalsIgnoreCase(v)) {
                    strokeController.capSquareRadio.setSelected(true);
                } else if ("round".equalsIgnoreCase(v)) {
                    strokeController.capRoundRadio.setSelected(true);
                } else {
                    strokeController.capButtRadio.setSelected(true);
                }
            }

            v = node.getAttribute("fill");
            if (v == null || v.isBlank() || "none".equalsIgnoreCase(v)) {
                strokeController.fillCheck.setSelected(false);
            } else {
                strokeController.fillCheck.setSelected(true);
                try {
                    strokeController.fillController.setColor(Color.web(v));
                } catch (Exception e) {
                    strokeController.fillController.setColor(Color.TRANSPARENT);
                }
            }
            v = node.getAttribute("fill-opacity");
            try {
                strokeController.fillOpacitySelector.setValue(Float.valueOf(v) + "");
            } catch (Exception e) {
                strokeController.fillOpacitySelector.setValue("1");
            }

            styleArea.setText(node.getAttribute("style"));

            shapeStyle = strokeController.pickValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean style2Element() {
        try {
            if (shapeStyle == null || shapeElement == null) {
                popError(message("InvalidData"));
                return false;
            }
            if (shapeStyle.getStrokeWidth() <= 0) {
                shapeElement.removeAttribute("stroke");
                shapeElement.removeAttribute("stroke-width");
            } else {
                shapeElement.setAttribute("stroke", shapeStyle.getStrokeColorCss());
                shapeElement.setAttribute("stroke-width", shapeStyle.getStrokeWidth() + "");
            }

            if (shapeStyle.isIsFillColor()) {
                shapeElement.setAttribute("fill", shapeStyle.getFilleColorCss());
                if (shapeStyle.getFillOpacity() >= 0) {
                    shapeElement.setAttribute("fill-opacity", shapeStyle.getFillOpacity() + "");
                } else {
                    shapeElement.removeAttribute("fill-opacity");
                }
            } else {
                shapeElement.setAttribute("fill", "none");
                shapeElement.removeAttribute("fill-opacity");
            }

            if (shapeStyle.isIsStrokeDash()) {
                String dash = shapeStyle.getStrokeDashText();
                if (dash != null && !dash.isBlank()) {
                    shapeElement.setAttribute("stroke-dasharray", dash);
                } else {
                    shapeElement.removeAttribute("stroke-dasharray");
                }
            } else {
                shapeElement.removeAttribute("stroke-dasharray");
            }

            String v = shapeStyle.getStrokeLineCapText();
            if (v != null && !v.isBlank()) {
                shapeElement.setAttribute("stroke-linecap", v);
            } else {
                shapeElement.removeAttribute("stroke-linecap");
            }

            String more = shapeStyle.getMore();
            if (more != null && !more.isBlank()) {
                shapeElement.setAttribute("style", more);
            } else {
                shapeElement.removeAttribute("style");
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @FXML
    public void goStyle() {
        String v = styleArea.getText();
        if (v != null && !v.isBlank()) {
            v = StringTools.trimBlanks(v);
            shapeStyle.setMore(v);
            TableStringValues.add("SvgStyleHistories", v);
        } else {
            shapeStyle.setMore(null);
        }
        applyStyle();
    }

    @FXML
    public void goSroke() {
        ShapeStyle style = strokeController.pickValues();
        if (style == null) {
            return;
        }
        if (shapeStyle != null) {
            style.setMore(shapeStyle.getMore());
        }
        shapeStyle = style;
        applyStyle();
    }

    public void applyStyle() {
        if (style2Element()) {
            loadXml(shapeElement);
            showShape();
        }
    }

    @FXML
    public void popExamplesStyleMenu(Event event) {
        if (UserConfig.getBoolean("SvgStyleExamplesPopWhenMouseHovering", false)) {
            showExamplesStyleMenu(event);
        }
    }

    @FXML
    public void showExamplesStyleMenu(Event event) {
        PopTools.popMappedValues(this, styleArea, "SvgStyleExamples", HelpTools.svgStyleExamples(), event);
    }

    @FXML
    protected void popStyleHistories(Event event) {
        if (UserConfig.getBoolean("SvgStyleHistoriesPopWhenMouseHovering", false)) {
            showStyleHistories(event);
        }
    }

    @FXML
    protected void showStyleHistories(Event event) {
        PopTools.popSavedValues(this, styleArea, event, "SvgStyleHistories");
    }

    @FXML
    protected void clearStyle() {
        styleArea.clear();
    }

    @FXML
    @Override
    public void options() {
        ImageShapeOptionsController.open(this, false);
    }

    /*
        xml
     */
    public void initXML() {
        try {
            wrapXmlCheck.setSelected(UserConfig.getBoolean(baseName + "WarpXML", true));
            wrapXmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WarpXML", newValue);
                    xmlArea.setWrapText(newValue);
                }
            });

            xmlArea.setWrapText(wrapXmlCheck.isSelected());

            xmlArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    updateXmlCount();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateXmlCount() {
        String s = xmlArea.getText();
        if (s == null || s.isBlank()) {
            xmlLabel.setText("");
        } else {
            xmlLabel.setText(message("Count") + ": " + s.length());
        }
    }

    public void loadXml(Element element) {
        try {
            isSettingValues = true;
            xmlArea.setText(XmlTools.transform(element, true));
            isSettingValues = false;
            updateXmlCount();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void goXml() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    Element e = XmlTools.toElement(this, myController, xmlArea.getText());
                    if (e == null || !isWorking()) {
                        return false;
                    }
                    shapeElement = (Element) doc.importNode(e, true);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                updateXmlCount();
                elementToShape(shapeElement);
                elementToStyle(shapeElement);
                setShapeInputs();
                showShape();
            }

        };
        start(task);
    }

    @FXML
    public void popXml() {
        TextPopController.openInput(this, xmlArea);
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("SVGHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.svgHelps(true));
    }

}
