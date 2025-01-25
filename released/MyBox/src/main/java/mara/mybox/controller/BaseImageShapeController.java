package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.data.DoublePath;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ColorDemos;
import mara.mybox.fxml.image.ShapeTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class BaseImageShapeController extends BaseImageEditController {

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

    public BaseImageShapeController() {
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
                blender = blendController.pickValues(-1);
            }
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
            blender = blendController.pickValues(-1);
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
                newImage = handleShape(this);
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

    protected Image handleShape(FxTask currentTask) {
        return ShapeTools.drawShape(currentTask, srcImage(), shapeData, shapeStyle, blender);
    }

    @FXML
    @Override
    public void okAction() {
        passHandled(currentImage());
    }

    @FXML
    @Override
    public void options() {
        ImageShapeOptionsController.open(this, false);
    }

    @Override
    public void popSvgPath(DoublePath pathData) {
        ImageSVGPathController.loadPath(imageController, pathData);
    }

    @FXML
    @Override
    public void clearAction() {
        loadImage(srcImage());
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ColorDemos.blendColor(currentTask, files,
                SwingFXUtils.fromFXImage(demoImage, null),
                strokeController.colorController.color(), srcFile());
    }

}
