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
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.SVG;
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
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    protected Element element;

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
    protected ControlSvgOptions optionsController;

    public abstract void setShape();

    public abstract boolean pickShape();

    public abstract boolean shape2Element();

    @Override
    public void initControls() {
        try {
            super.initControls();

            initStyleControls();
            initSvgOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(SvgEditorController controller, TreeItem<XmlTreeNode> item) {
        try {
            if (controller == null) {
                close();
                return;
            }
            editor = controller;
            treeItem = item;
            doc = (Document) editor.treeController.doc.cloneNode(true);
            svg = new SVG(doc);
            element = (Element) item.getValue().getNode();

            initMore();

            String hierarchyNumber = treeItem.getValue().hierarchyNumber();
            String info = editor.sourceFile != null
                    ? editor.sourceFile.getAbsolutePath() + "   " : "";
            infoLabel.setText(message("AddInto") + ": " + info + " - "
                    + message("HierarchyNumber") + ": " + hierarchyNumber);

            anchorCheck.setSelected(true);
            showAnchors = true;
            popShapeMenu = true;

            optionsController.loadDoc(doc, element);

            loadElement(element);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMore() {

    }

    public String scaleValue(double d) {
        return DoubleTools.imageScale(d) + "";
    }

    /*
        svg
     */
    public void initSvgOptions() {
        try {
            if (optionsController == null) {
                return;
            }
            optionsController.noBgColor();

            optionsController.sizeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    loadBackGround();
                }
            });

            optionsController.opacityNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    loadBackGround();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadDoc(Document doc, Element node) {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image image;

            @Override
            protected boolean handle() {
                try {
                    Document fdoc = SvgTools.focus(this, doc, node, 0.5f);
                    if (fdoc == null || !isWorking()) {
                        return false;
                    }
//            doc = SvgTools.removeSize(doc);
                    File tmpFile = SvgTools.docToImage(this, myController, fdoc, -1, -1, null);
                    if (tmpFile == null || !isWorking()) {
                        return false;
                    }
                    if (tmpFile.exists()) {
                        image = FxImageTools.readImage(this, tmpFile);
                        FileDeleteTools.delete(tmpFile);
                    } else {
                        image = null;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(image);
            }

        };
        start(task);

    }

    public void loadBackGround() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image image;

            @Override
            protected boolean handle() {
                try {
                    image = null;
                    File tmpFile = optionsController.toImage(this);
                    if (tmpFile != null && tmpFile.exists()) {
                        image = FxImageTools.readImage(this, tmpFile);
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
                loadImage(image);
            }

        };
        start(task);
    }

    public void loadElement(Element element) {
        try {
            if (element != null) {
                loadShape(element);
                loadStyle(element);
                loadXml(element);
            }
            clearMaskShapes();
            drawMaskShape();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        shape
     */
    public boolean loadShape(Element node) {
        try {
            float x, y, r;
            try {
                x = Float.parseFloat(node.getAttribute("cx"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": x");
                return false;
            }
            try {
                y = Float.parseFloat(node.getAttribute("cy"));
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": y");
                return false;
            }
            try {
                r = Float.parseFloat(node.getAttribute("r"));
            } catch (Exception e) {
                r = -1f;
            }
            if (r <= 0) {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                return false;
            }
            element = node;
            maskCircleData = new DoubleCircle(x, y, r);
            setShape();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    public void goShape() {
        if (pickShape() && shape2Element()) {
            loadXml(element);
            drawMaskShape();
        }
    }

    /*
        style
     */
    public void initStyleControls() {
        try {
            strokeController.setParameters(this);
            shapeStyle = strokeController.pickValues();

            styleArea.setText(shapeStyle.getMore());
            styleArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue o, Boolean ov, Boolean nv) {
                    if (isSettingValues || nv) {
                        return;
                    }
                    String v = styleArea.getText();
                    if (v != null && !v.isBlank()) {
                        v = StringTools.trimBlanks(v);
                        shapeStyle.setMore(v);
                        TableStringValues.add("SvgStyleHistories", v);
                    } else {
                        shapeStyle.setMore(null);
                    }
                    goStyle();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadStyle(Element node) {
        try {
            if (node == null) {
                return;
            }
            try {
                strokeController.colorController.setColor(Color.web(node.getAttribute("stroke")));
            } catch (Exception e) {
                strokeController.widthSelector.setValue("-1");
            }
            try {
                strokeController.widthSelector.setValue(Float.valueOf(node.getAttribute("stroke-width")) + "");
            } catch (Exception e) {
                strokeController.widthSelector.setValue("-1");
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
//            v = node.getAttribute("fill-opacity");  // #####
//            try {
//                strokeController.fillOpacitySelector.setValue(Float.valueOf(v) + "");
//            } catch (Exception e) {
//                strokeController.fillOpacitySelector.setValue("-1");
//            }
            styleArea.setText(node.getAttribute("style"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean style2Element() {
        try {
            if (shapeStyle == null || element == null) {
                popError(message("InvalidData"));
                return false;
            }
            if (shapeStyle.getStrokeWidth() <= 0) {
                element.removeAttribute("stroke");
                element.removeAttribute("stroke-width");
            } else {
                element.setAttribute("stroke", shapeStyle.getStrokeColorCss());
                element.setAttribute("stroke-width", shapeStyle.getStrokeWidth() + "");
            }

            if (shapeStyle.isIsFillColor()) {
                element.setAttribute("fill", shapeStyle.getFilleColorCss());
            } else {
                element.removeAttribute("fill");
            }
            if (shapeStyle.getFillOpacity() >= 0) {
                element.setAttribute("fill-opacity", shapeStyle.getFillOpacity() + "");
            } else {
                element.removeAttribute("fill-opacity");
            }

            if (shapeStyle.isIsStrokeDash()) {
                String dash = shapeStyle.getStrokeDashText();
                if (dash != null && !dash.isBlank()) {
                    element.setAttribute("stroke-dasharray", dash);
                } else {
                    element.removeAttribute("stroke-dasharray");
                }
            } else {
                element.removeAttribute("stroke-dasharray");
            }

            String v = shapeStyle.getStrokeLineCapText();
            if (v != null && !v.isBlank()) {
                element.setAttribute("stroke-linecap", v);
            } else {
                element.removeAttribute("stroke-linecap");
            }

            String more = shapeStyle.getMore();
            if (more != null && !more.isBlank()) {
                element.setAttribute("style", more);
            } else {
                element.removeAttribute("style");
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    @FXML
    public void goStyle() {
        if (style2Element()) {
            loadXml(element);
            drawMaskShape();
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
        PopTools.popValues(this, styleArea, "SvgStyleExamples", HelpTools.svgStyleExamples(), event);
    }

    @FXML
    protected void popStyleHistories(Event event) {
        if (UserConfig.getBoolean("SvgStyleHistoriesPopWhenMouseHovering", false)) {
            showStyleHistories(event);
        }
    }

    @FXML
    protected void showStyleHistories(Event event) {
        PopTools.popStringValues(this, styleArea, event, "SvgStyleHistories", false, true);
    }

    @FXML
    protected void clearStyle() {
        styleArea.clear();
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
                    element = (Element) doc.importNode(e, true);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                updateXmlCount();
                loadElement(element);
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
