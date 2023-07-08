package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ControlImageScale extends ControlImageSize {

    protected ImageManufactureScaleController scaleController;
    protected ImageManufactureController manuController;

    @FXML
    protected RadioButton dragRadio;

    public void setParameters(ImageManufactureScaleController scaleController) {
        this.scaleController = scaleController;
        manuController = scaleController.imageController;
        infoLabel = scaleController.commentsLabel;
        super.setParameters(manuController);
    }

    @Override
    protected void loadImage() {
        image = imageController.imageView.getImage();
        originalSize();
    }

    @Override
    protected void initScaleType() {
        try {
            scaleController.imageController.resetImagePane();
            scaleController.imageController.imageTab();
            infoLabel.setText("");
            super.initScaleType();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected void makeScaleType() {
        try {
            if (dragRadio.isSelected()) {
                scaleType = ScaleType.Dragging;
                setBox.getChildren().addAll(keepBox);
                infoLabel.setText(Languages.message("DragSizeComments"));
                initDrag();
                checkKeepType();
                checkRatio();

            } else {

                super.makeScaleType();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initDrag() {
        try {
            if (imageController == null) {
                return;
            }
            width = imageController.imageView.getImage().getWidth();
            height = imageController.imageView.getImage().getHeight();
            imageController.setMaskRectangleVisible(true);
            imageController.maskRectangleData = new DoubleRectangle(0, 0, width - 1, height - 1);
            imageController.drawMaskRectangle();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void imageClicked() {
        if (imageController == null || !isDrag() || imageController.maskRectangleData == null) {
            return;
        }
        if (keepRatioType != BufferedImageTools.KeepRatioType.None) {
            int[] wh = ScaleTools.scaleValues(
                    (int) imageController.imageView.getImage().getWidth(),
                    (int) imageController.imageView.getImage().getHeight(),
                    (int) imageController.maskRectangleData.getWidth(),
                    (int) imageController.maskRectangleData.getHeight(),
                    keepRatioType);
            width = wh[0];
            height = wh[1];

            imageController.maskRectangleData = new DoubleRectangle(
                    imageController.maskRectangleData.getSmallX(),
                    imageController.maskRectangleData.getSmallY(),
                    imageController.maskRectangleData.getSmallX() + width - 1,
                    imageController.maskRectangleData.getSmallY() + height - 1);
            imageController.drawMaskRectangle();

        } else {
            width = imageController.maskRectangleData.getWidth();
            height = imageController.maskRectangleData.getHeight();
        }
        labelSize();
    }

    @Override
    public void scale(Image newImage, long cost) {
        manuController.popSuccessful();
        String newSize = (int) Math.round(newImage.getWidth()) + "x" + (int) Math.round(newImage.getHeight());
        if (scaleType == ScaleType.Scale) {
            manuController.updateImage(ImageManufactureController_Image.ImageOperation.Scale2, scale + "", newSize, newImage, cost);
        } else if (scaleType == ScaleType.Dragging || scaleType == ScaleType.Pixels) {
            manuController.updateImage(ImageManufactureController_Image.ImageOperation.Scale2, "Pixels", newSize, newImage, cost);
        }

        String info = Languages.message("OriginalSize") + ": " + (int) Math.round(manuController.image.getWidth())
                + "x" + (int) Math.round(manuController.image.getHeight()) + "\n"
                + Languages.message("CurrentSize") + ": " + Math.round(newImage.getWidth())
                + "x" + Math.round(newImage.getHeight());
        infoLabel.setText(info);

        if (scaleType == ScaleType.Dragging) {
            initDrag();
        }
    }

}
