package mara.mybox.controller;

import java.awt.Rectangle;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import org.w3c.dom.*;

/**
 * @Author Mara
 * @CreateDate 2023-6-24
 * @License Apache License Version 2.0
 */
public class ControlSvgTranscode extends BaseController {

    protected float width, height, docWidth, docHeight, inputWidth, inputHeight;
    protected Rectangle area, inputArea, docArea;

    @FXML
    protected TextField widthInput, heightInput, areaInput;

    public void setDoc(Document doc) {
        checkDoc(doc);
        if (docWidth > 0) {
            widthInput.setText(docWidth + "");
        }
        if (docHeight > 0) {
            heightInput.setText(docHeight + "");
        }
        if (docArea != null) {
            areaInput.setText((int) docArea.getX() + " " + (int) docArea.getY()
                    + " " + (int) docArea.getWidth() + " " + (int) docArea.getWidth());
        }
    }

    public void checkDoc(Document doc) {
        try {
            docWidth = 0f;
            docHeight = 0f;
            docArea = null;
            if (doc == null) {
                return;
            }
            NodeList svglist = doc.getElementsByTagName("svg");
            if (svglist == null || svglist.getLength() == 0) {
                return;
            }
            NamedNodeMap attrs = svglist.item(0).getAttributes();
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    String name = attr.getNodeName().toLowerCase();
                    String value = attr.getNodeValue();
                    switch (name) {
                        case "width":
                             try {
                            docWidth = Float.parseFloat(value);
                        } catch (Exception e) {
                        }
                        break;
                        case "height":
                            try {
                            docHeight = Float.parseFloat(value);
                        } catch (Exception e) {
                        }
                        break;
                        case "viewbox":
                            try {
                            String[] v = value.split(" ");
                            if (v != null && v.length >= 4) {
                                docArea = new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]),
                                        Integer.parseInt(v[2]), Integer.parseInt(v[3]));
                            }
                        } catch (Exception e) {
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void checkInputs() {
        inputWidth = 0f;
        try {
            inputWidth = Float.parseFloat(widthInput.getText());
        } catch (Exception e) {
        }
        inputHeight = 0f;
        try {
            inputHeight = Float.parseFloat(heightInput.getText());
        } catch (Exception e) {
        }
        inputArea = null;
        try {
            String[] v = areaInput.getText().split(" ");
            if (v != null && v.length >= 4) {
                inputArea = new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]),
                        Integer.parseInt(v[2]), Integer.parseInt(v[3]));
            }
        } catch (Exception e) {
        }
    }

    public void checkValues() {
        width = inputWidth > 0 ? inputWidth : docWidth;
        height = inputHeight > 0 ? inputHeight : docHeight;
        area = inputArea == null ? inputArea : docArea;
    }

    public void checkValues(Document doc) {
        checkDoc(doc);
        checkValues();
    }

    public void pickValues() {
        checkInputs();
        checkValues();
    }

}
