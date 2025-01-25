package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import mara.mybox.image.tools.BufferedImageTools;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ControlImageScale extends ControlImageSize {

    protected ImageSizeController scaleController;

    @FXML
    protected RadioButton dragRadio;

    public void setParameters(ImageSizeController scaleController) {
        this.scaleController = scaleController;
        imageController = scaleController;
        infoLabel = scaleController.commentsLabel;
        checkScaleType();
    }

    @Override
    protected void resetControls() {
        try {
            if (infoLabel != null) {
                infoLabel.setText("");
            }
            super.resetControls();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    protected void switchType() {
        try {
            if (dragRadio.isSelected()) {
                scaleType = ScaleType.Dragging;
                setBox.getChildren().addAll(keepBox);
                if (infoLabel != null) {
                    infoLabel.setText(message("DragSizeComments"));
                }
                initDrag();
                adjustRadio();

            } else {
                super.switchType();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void initDrag() {
        try {
            if (!dragRadio.isSelected()) {
                return;
            }
            Image image = getImage();
            if (image == null) {
                return;
            }
            width = image.getWidth();
            height = image.getHeight();
            scaleController.maskRectangleData = DoubleRectangle.xywh(0, 0, width, height);
            scaleController.showMaskRectangle();
            scaleController.popItemMenu = false;
            scaleController.showAnchors = true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    protected void adjustRadio() {
        super.adjustRadio();
        if (scaleController == null || !dragRadio.isSelected()
                || scaleController.maskRectangleData == null) {
            return;
        }
        scaleController.drawMaskRectangle();
    }

    public void paneClicked() {
        if (!dragRadio.isSelected()
                || scaleController == null || scaleController.maskRectangleData == null) {
            return;
        }
        width = scaleController.maskRectangleData.getWidth();
        height = scaleController.maskRectangleData.getHeight();
        if (keepRatioType != BufferedImageTools.KeepRatioType.None) {
            adjustRadio();

        } else {
            labelSize();
        }
    }

    @Override
    public void afterScaled(Image newImage, long cost) {
        String info = message("Scale") + ": ";
        if (scaleType == ScaleType.Scale) {
            info += message("Times");
        } else if (scaleType == ScaleType.Dragging || scaleType == ScaleType.Pixels) {
            info += (int) Math.round(newImage.getWidth()) + "x" + (int) Math.round(newImage.getHeight());
        }
        info += "  " + message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
//        scaleController.apply(newImage, info);
    }

}
