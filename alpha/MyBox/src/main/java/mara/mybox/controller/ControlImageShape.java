package mara.mybox.controller;

import java.io.File;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-7-5
 * @License Apache License Version 2.0
 */
public class ControlImageShape extends BaseImageController {

    protected ControlSvgShape svgShape;
    protected Element shape;

    @Override
    public void initControls() {
        try {
            super.initControls();

            imageView.toBack();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadBackGround() {
        try {
            imageView.toBack();
            File tmpFile = svgShape.svgOptionsController.toImage();
            if (tmpFile != null && tmpFile.exists()) {
                loadImage(FxImageTools.readImage(tmpFile));
                FileDeleteTools.delete(tmpFile);
                imageView.setOpacity(svgShape.svgOptionsController.bgOpacity);
            } else {
                loadImage(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void displayShape(Element element) {
        try {
            initMaskControls(false);
            if (isSettingValues || element == null) {
                return;
            }
            shape = element;
            isSettingValues = true;
            switch (element.getNodeName().toLowerCase()) {
                case "rect":
                    setMaskRectangleLineVisible(true);
                    maskRectangleLine.setStroke(svgShape.strokeColorController.color());
                    maskRectangleLine.setStrokeWidth(svgShape.strokeWidth);

                    double x = Double.parseDouble(shape.getAttribute("x"));
                    double y = Double.parseDouble(shape.getAttribute("y"));
                    double width = Double.parseDouble(shape.getAttribute("width"));
                    double height = Double.parseDouble(shape.getAttribute("height"));
                    maskRectangleData = new DoubleRectangle(x, y, x + width - 1, y + height - 1);
                    drawMaskRectangleLineAsData();
                    break;
                case "circle":

                    break;
                case "ellipse":

                    break;
                case "line":

                    break;
                case "polyline":

                    break;
                case "polygon":

                    break;
                case "path":

                    break;
                default:
                    popError(message("InvalidData"));
                    isSettingValues = false;
                    return;
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
