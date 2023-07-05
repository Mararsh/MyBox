package mara.mybox.controller;

import java.awt.Rectangle;
import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
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
public class ControlSvgOptions extends BaseController {

    protected Document doc, view;
    protected Node focusedNode;
    protected float width, height, bgOpacity;
    protected Rectangle viewBox;
    protected SimpleBooleanProperty drawNotify;

    @FXML
    protected TextField widthInput, heightInput, viewBoxInput;
    @FXML
    protected CheckBox bgColorCheck;
    @FXML
    protected ControlColorSet bgColorController;
    @FXML
    protected ComboBox<String> opacitySelector;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(widthInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(heightInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(viewBoxInput, new Tooltip(message("BlankInvalidtoUseDefault")));
            NodeStyleTools.setTooltip(viewButton, new Tooltip(message("Image")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            drawNotify = new SimpleBooleanProperty(false);

            bgColorController.init(this, baseName + "BackgroundColor", Color.TRANSPARENT);
            bgColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (bgColorCheck.isSelected()) {
                        makeView();
                    }
                }
            });

            bgColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    makeView();
                }
            });

            bgOpacity = UserConfig.getFloat(baseName + "BackgroundOpacity", 0.3f);
            opacitySelector.getItems().addAll(
                    "0.3", "0", "1.0", "0.05", "0.02", "0.1", "0.2", "0.5", "0.8", "0.6", "0.4", "0.7", "0.9"
            );
            opacitySelector.setValue(bgOpacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        bgOpacity = Float.parseFloat(newValue);
                        if (bgOpacity >= 0) {
                            UserConfig.setFloat(baseName + "BackgroundOpacity", bgOpacity);
                        }
                        opacitySelector.getEditor().setStyle(null);
                        makeView();
                    } catch (Exception e) {
                        opacitySelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadDoc(Document doc, Node focus) {
        try {
            if (doc == null) {
                return;
            }
            this.doc = (Document) doc.cloneNode(true);
            focusedNode = focus;
            SVG svg = new SVG(doc);
            width = svg.getWidth();
            if (width > 0) {
                widthInput.setText(width + "");
            } else {
                widthInput.clear();
            }
            height = svg.getHeight();
            if (height > 0) {
                heightInput.setText(height + "");
            } else {
                heightInput.clear();
            }
            viewBox = svg.getViewBox();
            if (viewBox != null) {
                viewBoxInput.setText(SvgTools.viewBoxString(viewBox));
            } else {
                viewBoxInput.clear();
            }

            makeView();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeView() {
        try {
            if (isSettingValues) {
                return;
            }
            if (doc == null) {
                view = null;
                return;
            }
            view = SvgTools.focus(doc, focusedNode, bgOpacity);
            Element svgNode = XmlTools.findName(view, "svg", 0);
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

            drawNotify.set(!drawNotify.get());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void goSize() {
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
        makeView();
    }

    @FXML
    public void defaultSize() {
        loadDoc(doc, focusedNode);
    }

    public String toXML() {
        return XmlTools.transform(view);
    }

    public File toImage() {
        return SvgTools.docToImage(this, view, width, height, viewBox);
    }

    public File toPDF() {
        return SvgTools.docToPDF(this, view, width, height, viewBox);
    }

}
