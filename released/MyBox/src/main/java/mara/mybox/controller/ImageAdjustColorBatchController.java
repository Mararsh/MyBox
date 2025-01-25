package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.image.data.PixelsOperation;
import mara.mybox.image.data.PixelsOperation.ColorActionType;
import mara.mybox.image.data.PixelsOperation.OperationType;
import mara.mybox.image.data.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ColorDemos;
import mara.mybox.fxml.FxTask;
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
        baseTitle = message("ImageBatch") + " - " + message("AdjustColor");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems()));

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
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        if (null == colorOperationType || colorActionType == null) {
            return null;
        }
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(source, null,
                    colorOperationType, colorActionType)
                    .setSkipTransparent(!handleTransparentCheck.isSelected())
                    .setTask(currentTask);
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
            BufferedImage target = pixelsOperation.start();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void makeDemoFiles(FxTask dTask, List<String> files, File demoFile, BufferedImage demoImage) {
        ColorDemos.adjustColor(dTask, files, demoImage, null, demoFile);
    }

}
