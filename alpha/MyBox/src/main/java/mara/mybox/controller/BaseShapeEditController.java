package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ShapeTools;
import mara.mybox.fxml.FxSingletonTask;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class BaseShapeEditController extends BaseImageEditController {

    protected DoubleShape shapeData;
    protected PixelsBlend blender;

    @FXML
    protected Tab valuesTab, strokeTab, blendTab;
    @FXML
    protected ControlStroke strokeController;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected Button shapeButton;

    public BaseShapeEditController() {
        TipsLabelKey = "ShapeEditTips";
    }

    @Override
    protected void initMore() {
        try {
            if (strokeController != null) {
                strokeController.setParameters(this);
                shapeStyle = strokeController.pickValues();
            }
            if (blendController != null) {
                blendController.setParameters(this);
                blender = blendController.pickValues();
            }

            resetShapeOptions();

        } catch (Exception e) {
            MyBoxLog.error(e);
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

    public boolean checkStroke() {
        if (strokeController != null) {
            shapeStyle = strokeController.pickValues();
            if (shapeStyle == null) {
                if (strokeTab != null) {
                    tabPane.getSelectionModel().select(strokeTab);
                }
                return false;
            }
        }
        return true;
    }

    public boolean checkBlend() {
        if (blendController != null) {
            blender = blendController.pickValues();
            if (blender == null) {
                if (blendTab != null) {
                    tabPane.getSelectionModel().select(blendTab);
                }
                return false;
            }
        }
        return true;
    }

    public boolean pickShape() {
        return false;
    }

    public void goShape() {
        if (!pickShape()) {
            return;
        }
        drawShape();
    }

    @FXML
    @Override
    public void goAction() {
        if (!checkStroke() || !checkBlend() || !pickShape()) {
            return;
        }
        drawShape();
    }

    public void drawShape() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

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

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            if (anchorCheck != null) {
                anchorCheck.setSelected(true);
            }
            initStroke();
            initShape();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void initStroke() {
        if (strokeController != null) {
            strokeController.setWidthList();
        }
    }

    public void initShape() {
        goAction();
    }

    protected Image handleShape() {
        return ShapeTools.drawShape(task, srcImage(), shapeData, shapeStyle, blender);
    }

    @FXML
    @Override
    public void okAction() {
        editor.updateImage(operation, imageView.getImage());
        if (closeAfterCheck.isSelected()) {
            close();
            editor.popSuccessful();
        } else {
            popSuccessful();
        }
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
