package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ShapeTools;
import mara.mybox.fxml.SingletonCurrentTask;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class BaseShapeEditController extends BaseImageEditController {

    protected DoubleShape shapeData;
    protected PixelsBlend blender;

    @FXML
    protected ControlStroke strokeController;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected Button shapeButton;

    @Override
    protected void initMore() {
        try {
            strokeController.setParameters(this);
            shapeStyle = strokeController.pickValues();

            if (blendController != null) {
                blendController.setParameters(this, imageView);
                blender = blendController.pickValues();
            }

            resetShapeOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            if (anchorCheck != null) {
                anchorCheck.setSelected(true);
            }

            initShape();
            fitSize();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void maskShapeDataChanged() {
        notifyShapeDataChanged();
        drawShape();
    }

    @Override
    public void maskShapeChanged() {
        setInputs();
    }

    public void setInputs() {
    }

    public void initShape() {
        drawShape();
    }

    public boolean pickShape() {
        return false;
    }

    @FXML
    public void goShape() {
        if (pickShape()) {
            drawShape();
        }
    }

    @FXML
    public void goStroke() {
        shapeStyle = strokeController.pickValues();
        if (shapeStyle != null) {
            drawShape();
        }
    }

    @FXML
    public void goBlend() {
        blender = blendController.pickValues();
        if (blender != null) {
            drawShape();
        }
    }

    public void drawShape() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                shapeData = currentMaskShapeData();
                newImage = handleShape();
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
            }

        };
        start(task, okButton);
    }

    protected Image handleShape() {
        return ShapeTools.drawShape(srcImage(), shapeData, shapeStyle, blender);
    }

    @Override
    protected void handleImage() {
        handledImage = imageView.getImage();
    }

    @FXML
    @Override
    public void options() {
        ImageShapeOptionsController.open(this, false);
    }

    @FXML
    @Override
    public void clearAction() {
        loadImage(srcImage());
    }

}
