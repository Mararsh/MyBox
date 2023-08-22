package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ShapeTools;
import mara.mybox.fxml.SingletonCurrentTask;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ControlImageShapeOptions extends ControlShapeOptions {

    protected ImageManufactureShapeController shapeController;
    protected ImageManufactureController editor;
    protected ImageView maskView, imageView;

    @FXML
    protected ControlImagesBlend blendController;

    public void setParameters(ImageManufactureShapeController penController) {
        try {
            this.shapeController = penController;
            editor = penController.editor;
            maskView = editor.maskView;
            imageView = editor.imageView;

            super.setParameters(editor);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void switchShapeBySelection() {
        super.switchShapeBySelection();
        redrawShape();
    }

    @Override
    public void initShapeControls() {
        try {
            super.initShapeControls();

            blendController.setParameters(this);

            blendController.optionChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    redrawShape();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public void setShapeControls() {
        try {
            super.setShapeControls();

            maskView.setImage(imageView.getImage());
            maskView.setOpacity(1);
            maskView.setVisible(true);
            imageView.setVisible(false);
            imageView.toBack();
            setBlender();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setBlender() {
        blendController.backImage = imageView.getImage();
        blendController.foreImage = FxImageTools.createImage(
                (int) (imageView.getImage().getWidth() * 3 / 4), (int) (imageView.getImage().getHeight() * 3 / 4),
                strokeColorController.color());
        blendController.x = (int) (blendController.backImage.getWidth() - blendController.foreImage.getWidth()) / 2;
        blendController.y = (int) (blendController.backImage.getHeight() - blendController.foreImage.getHeight()) / 2;
    }

    @Override
    public void shapeDataChanged() {
        super.shapeDataChanged();
        redrawShape();
    }

    @Override
    public void redrawShape() {
        if (imageController == null || shapeType == null) {
            return;
        }
        DoubleShape shapeData = editor.currentMaskShapeData();
        if (shapeData == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = ShapeTools.drawShape(imageView.getImage(),
                        shapeData, style, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                maskView.setImage(newImage);
                maskView.setOpacity(1);
                maskView.setVisible(true);
                imageView.setVisible(false);
                imageView.toBack();
                editor.imageTab();
                editor.drawMaskShape();
                editor.hideMaskShape();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled() || shapeType == null) {
            return;
        }
        String name = shapeType.name();
        isSettingValues = true;
        typeGroup.selectToggle(null);
        shapeType = null;
        isSettingValues = false;

        editor.popSuccessful();
        editor.updateImage(ImageOperation.Shape, name, null, maskView.getImage(), 0);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (parametersController.keyEventsFilter(event)) {
            return true;
        }
        return super.keyEventsFilter(event);
    }

}
