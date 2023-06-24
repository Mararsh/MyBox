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

    protected float width, height;
    protected Rectangle area;

    @FXML
    protected TextField widthInput, heightInput, areaInput;

    public void input(Document doc) {
        try {
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
                            widthInput.setText(value);
                            break;
                        case "height":
                            heightInput.setText(value);
                            break;
                        case "viewbox":
                            areaInput.setText(value);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void pickValues() {
        width = 0f;
        try {
            width = Float.parseFloat(widthInput.getText());
        } catch (Exception e) {
        }
        height = 0f;
        try {
            height = Float.parseFloat(heightInput.getText());
        } catch (Exception e) {
        }
        try {
            height = Float.parseFloat(heightInput.getText());
        } catch (Exception e) {
        }
        area = null;
        try {
            String[] v = areaInput.getText().split(" ");
            if (v != null && v.length >= 4) {
                area = new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]),
                        Integer.parseInt(v[2]), Integer.parseInt(v[3]));
            }
        } catch (Exception e) {
        }
    }

}
