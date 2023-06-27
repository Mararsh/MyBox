package mara.mybox.controller;

import java.awt.Rectangle;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data.SVG;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SvgTools;

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

    public void setSVG(SVG svg) {
        checkSVG(svg);
        if (docWidth > 0) {
            widthInput.setText(docWidth + "");
        }
        if (docHeight > 0) {
            heightInput.setText(docHeight + "");
        }
        if (docArea != null) {
            areaInput.setText(SvgTools.viewBoxString(docArea));
        }
    }

    public void checkSVG(SVG svg) {
        try {
            docWidth = 0f;
            docHeight = 0f;
            docArea = null;
            if (svg != null) {
                docWidth = svg.getWidth();
                docHeight = svg.getHeight();
                docArea = svg.getViewBox();
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
        inputArea = SvgTools.viewBox(areaInput.getText());
    }

    public void checkValues() {
        width = inputWidth > 0 ? inputWidth : docWidth;
        height = inputHeight > 0 ? inputHeight : docHeight;
        area = inputArea == null ? inputArea : docArea;
    }

    public void checkValues(SVG svg) {
        checkSVG(svg);
        checkValues();
    }

    public void pickValues() {
        checkInputs();
        checkValues();
    }

}
