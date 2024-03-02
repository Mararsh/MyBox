package mara.mybox.controller;

import java.awt.Rectangle;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @Author Mara
 * @CreateDate 2023-7-2
 * @License Apache License Version 2.0
 */
public class ControlSvgViewOptions extends BaseController {

    protected Document doc;
    protected Node focusedNode;
    protected float width, height, bgOpacity;
    protected Rectangle viewBox;

    @FXML
    protected TextField widthInput, heightInput, viewBoxInput;
    @FXML
    protected CheckBox bgColorCheck;
    @FXML
    protected ControlColorSet bgColorController;
    @FXML
    protected ComboBox<String> opacitySelector;
    @FXML
    protected VBox bgBox;
    @FXML
    protected FlowPane colorPane;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(widthInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(heightInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(viewBoxInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(viewButton, new Tooltip(message("Image")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            bgColorController.init(this, baseName + "BackgroundColor", Color.WHITE);

            bgColorCheck.setSelected(UserConfig.getBoolean(baseName + "ShowBackgroundColor", false));

            bgOpacity = UserConfig.getFloat(baseName + "BackgroundOpacity", 0.3f);
            if (bgOpacity < 0) {
                bgOpacity = 0.3f;
            }
            opacitySelector.getItems().addAll(
                    "0.3", "0", "1.0", "0.05", "0.02", "0.1", "0.2", "0.5", "0.8", "0.6", "0.4", "0.7", "0.9"
            );
            opacitySelector.setValue(bgOpacity + "");
            opacitySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOpacity();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadExcept(FxTask currentTask, Document srcDoc, Node except) {
        load(currentTask, srcDoc, null, except);
    }

    public void loadFocus(FxTask currentTask, Document srcDoc, Node focus) {
        load(currentTask, srcDoc, focus, null);
    }

    public void load(FxTask currentTask, Document srcDoc, Node focus, Node except) {
        try {
            if (srcDoc == null) {
                doc = null;
                return;
            }
            doc = (Document) srcDoc.cloneNode(true);
            focusedNode = focus;
            if (currentTask != null && !currentTask.isWorking()) {
                return;
            }
            if (except != null) {
                XmlTools.remove(doc, except);
            }
            initOptions();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initOptions() {
        try {
            if (doc == null) {
                return;
            }
            SVG svg = new SVG(doc);
            width = svg.getWidth();
            height = svg.getHeight();
            viewBox = svg.getViewBox();
            if (viewBox != null) {
                viewBoxInput.setText(SvgTools.viewBoxString(viewBox));
                if (width <= 0) {
                    width = (float) viewBox.getWidth();
                }
                if (height <= 0) {
                    height = (float) viewBox.getHeight();
                }
            } else {
                viewBoxInput.clear();
            }
            if (width > 0) {
                widthInput.setText(width + "");
            } else {
                widthInput.clear();
            }
            if (height > 0) {
                heightInput.setText(height + "");
            } else {
                heightInput.clear();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        width = -1;
        try {
            float v = Float.parseFloat(widthInput.getText());
            if (v > 0) {
                width = v;
            }
        } catch (Exception e) {
        }

        height = -1;
        try {
            float v = Float.parseFloat(heightInput.getText());
            if (v > 0) {
                height = v;
            }
        } catch (Exception e) {
        }
        viewBox = SvgTools.viewBox(viewBoxInput.getText());

        if (checkOpacity()) {
            UserConfig.setFloat(baseName + "BackgroundOpacity", bgOpacity);
            UserConfig.setBoolean(baseName + "ShowBackgroundColor", bgColorCheck.isSelected());
            return true;
        } else {
            return false;
        }
    }

    public boolean checkOpacity() {
        float v = -1;
        try {
            v = Float.parseFloat(opacitySelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0) {
            bgOpacity = v;
            opacitySelector.getEditor().setStyle(null);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("BackgroundOpacity"));
            opacitySelector.getEditor().setStyle(UserConfig.badStyle());
            return false;
        }
    }

    @FXML
    public void defaultSize() {
        initOptions();
    }

    public Document toSVG(FxTask currentTask) {
        try {
            if (doc == null) {
                return null;
            }
            Document svgDoc = SvgTools.focus(currentTask, doc, focusedNode, bgOpacity);
            if (svgDoc == null || (currentTask != null && !currentTask.isWorking())) {
                return null;
            }
            Element svgNode = XmlTools.findName(svgDoc, "svg", 0);
            if (width > 0) {
                svgNode.setAttribute("width", width + "");
            } else {
                svgNode.removeAttribute("width");
            }
            if (height > 0) {
                svgNode.setAttribute("height", height + "");
            } else {
                svgNode.removeAttribute("height");
            }
            if (viewBox != null) {
                svgNode.setAttribute("viewBox", SvgTools.viewBoxString(viewBox));
            } else {
                svgNode.removeAttribute("viewBox");
            }
            if (bgColorCheck.isSelected()) {
                String style = svgNode.getAttribute("style");
                svgNode.setAttribute("style",
                        (style == null || style.isBlank() ? "" : style + ";")
                        + "background-color: " + bgColorController.rgb() + "; ");
            }
            return svgDoc;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String toXML(FxTask currentTask) {
        return XmlTools.transform(toSVG(currentTask));
    }

    public File toImage(FxTask currentTask) {
        return SvgTools.docToImage(currentTask, this, toSVG(currentTask), width, height, viewBox);
    }

    public File toPDF(FxTask currentTask) {
        return SvgTools.docToPDF(currentTask, this, toSVG(currentTask), width, height, viewBox);
    }

}
