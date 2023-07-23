package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.tools.FileDeleteTools;

/**
 * @Author Mara
 * @CreateDate 2023-7-5
 * @License Apache License Version 2.0
 */
public class ControlSvgImage extends BaseImageController {

    protected ControlSvgShape svgShapeControl;
    protected DoublePoint lastPoint;

    @FXML
    protected Label infoLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            imageView.toBack();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadBackGround() {
        try {
            imageView.toBack();
            File tmpFile = svgShapeControl.optionsController.toImage();
            if (tmpFile != null && tmpFile.exists()) {
                loadImage(FxImageTools.readImage(tmpFile));
                FileDeleteTools.delete(tmpFile);
                setBackGroundOpacity();
            } else {
                loadImage(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setBackGroundOpacity() {
        imageView.setOpacity(svgShapeControl.optionsController.bgOpacity);
    }

    @Override
    protected void checkSelect() {
    }

}
