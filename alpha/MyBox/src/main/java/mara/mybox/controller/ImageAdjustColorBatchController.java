package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperation.ColorActionType;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-22
 * @License Apache License Version 2.0
 */
public class ImageAdjustColorBatchController extends BaseImageEditBatchController {

    protected OperationType colorOperationType;
    protected ColorActionType colorActionType;
    protected int colorValue;

    @FXML
    protected ControlImageAdjustColor optionsController;

    public ImageAdjustColorBatchController() {
        baseTitle = message("AdjustColor") + " - " + message("Batch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        colorOperationType = optionsController.colorOperationType;
        colorActionType = optionsController.colorActionType;
        colorValue = optionsController.colorValue;
        return super.makeMoreParameters();
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        if (null == colorOperationType || colorActionType == null) {
            return null;
        }
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(source, null,
                    colorOperationType, colorActionType);
            pixelsOperation.setSkipTransparent(!handleTransparentCheck.isSelected());
            switch (colorOperationType) {
                case Hue:
                    pixelsOperation.setFloatPara1(colorValue / 360.0f);
                    break;
                case Brightness:
                case Saturation:
                    pixelsOperation.setFloatPara1(colorValue / 100.0f);
                    break;
                case Red:
                case Green:
                case Blue:
                case Yellow:
                case Cyan:
                case Magenta:
                case RGB:
                case Opacity:
                    pixelsOperation.setIntPara1(colorValue);
                    break;
            }
            BufferedImage target = pixelsOperation.operate();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

}