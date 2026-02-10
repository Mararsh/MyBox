package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ShapeTools;
import mara.mybox.image.data.PixelsBlend.ImagesBlendMode;
import mara.mybox.image.data.PixelsBlend.TransparentAs;
import mara.mybox.image.data.PixelsBlendFactory;
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
    @FXML
    protected ControlColorSet showColorController, eraserColorController;

    public ImageEraserController() {
        baseTitle = message("Eraser");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Eraser");

            showColorController.init(this, baseName + "ShowColor", Color.WHITE);
            eraserColorController.init(this, baseName + "EraserColor", Color.TRANSPARENT);

            shapeStyle = new ShapeStyle(baseName);
            shapeStyle.setStrokeColor(showColorController.color())
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
        shapeStyle.setStrokeColor(showColorController.color())
                .setStrokeWidth(v)
                .save();
        return true;
    }

    @Override
    public boolean checkBlend() {
        blender = PixelsBlendFactory.create(ImagesBlendMode.NORMAL)
                .setBlendMode(ImagesBlendMode.NORMAL)
                .setWeight(1f)
                .setBaseAbove(false)
                .setBaseTransparentAs(TransparentAs.Another)
                .setOverlayTransparentAs(TransparentAs.Another);
        return true;
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                shapeData = currentMaskShapeData();
                Color eraserColor = eraserColorController.color();
                shapeStyle.setStrokeColor(eraserColor);
                if (eraserColor.equals(Color.TRANSPARENT)) {
                    newImage = ShapeTools.drawErase(this, srcImage(), maskPolylinesData, shapeStyle);
                } else {
                    newImage = ShapeTools.drawShape(this, srcImage(), maskPolylinesData, shapeStyle, blender);
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                imageView.setImage(newImage);
                drawMaskShape();
                hideMaskShape();
                passHandled(currentImage());
            }

        };
        start(task, okButton);
    }

    /*
        static methods
     */
    public static ImageEraserController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEraserController controller = (ImageEraserController) WindowTools.referredStage(
                    parent, Fxmls.ImageEraserFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
