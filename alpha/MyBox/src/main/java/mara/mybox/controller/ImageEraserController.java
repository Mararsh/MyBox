package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ShapeTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageEraserController extends ImagePolylinesController {

    @FXML
    protected ComboBox<String> widthSelector;

    public ImageEraserController() {
        baseTitle = message("Eraser");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Eraser");

            shapeStyle = new ShapeStyle(baseName);
            shapeStyle.setStrokeColor(Color.WHITE)
                    .setIsFillColor(false)
                    .setIsStrokeDash(false)
                    .setStrokeLineCap(StrokeLineCap.BUTT)
                    .setStrokeLineJoin(StrokeLineJoin.MITER)
                    .setStrokeLineLimit(10f);

            widthSelector.setValue((int) shapeStyle.getStrokeWidth() + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initStroke() {
        ControlStroke.setWidthList(widthSelector, imageView, (int) shapeStyle.getStrokeWidth());
    }

    @Override
    public boolean checkStroke() {
        float v = -1;
        try {
            v = Float.parseFloat(widthSelector.getValue());
        } catch (Exception e) {
        }
        if (v <= 0) {
            popError(message("InvalidParameter") + ": " + message("Width"));
            return false;
        }
        shapeStyle.setStrokeWidth(v).save();
        return true;
    }

    @Override
    protected Image handleShape(FxTask currentTask) {
        return ShapeTools.drawErase(currentTask, srcImage(), maskPolylinesData, shapeStyle);
    }

    /*
        static methods
     */
    public static ImageEraserController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEraserController controller = (ImageEraserController) WindowTools.branchStage(
                    parent, Fxmls.ImageEraserFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
