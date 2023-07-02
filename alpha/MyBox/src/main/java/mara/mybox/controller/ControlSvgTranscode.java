package mara.mybox.controller;

import java.awt.Rectangle;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SvgTools;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2023-6-24
 * @License Apache License Version 2.0
 */
public class ControlSvgTranscode extends BaseController {

    protected float width, height, inputWidth, inputHeight;
    protected Rectangle area, inputArea;

    @FXML
    protected TextField widthInput, heightInput, areaInput;

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
        inputArea = SvgTools.viewBox(areaInput.getText());
    }

    public void checkValues(Document doc) {
        try {
            float docWidth = 0f;
            float docHeight = 0f;
            Rectangle docArea = null;
            if (doc != null) {
                SVG svg = new SVG(doc);
                docWidth = svg.getWidth();
                docHeight = svg.getHeight();
                docArea = svg.getViewBox();
            }
            width = inputWidth > 0 ? inputWidth : docWidth;
            height = inputHeight > 0 ? inputHeight : docHeight;
            area = inputArea == null ? inputArea : docArea;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
