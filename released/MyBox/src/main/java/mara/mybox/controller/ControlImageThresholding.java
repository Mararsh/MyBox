package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageThresholding extends BaseController {

    protected int threshold, small, big;

    @FXML
    protected TextField thresholdInput, smallInput, bigInput;

    public ControlImageThresholding() {
        TipsLabelKey = "ImageThresholdingComments";
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(thresholdInput, new Tooltip("0~255"));
            NodeStyleTools.setTooltip(smallInput, new Tooltip("0~255"));
            NodeStyleTools.setTooltip(bigInput, new Tooltip("0~255"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            threshold = UserConfig.getInt(baseName + "Threshold", 128);
            if (threshold < 0 || threshold > 255) {
                threshold = 128;
            }
            thresholdInput.setText(threshold + "");
            thresholdInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkThreshold();
                    }
                }
            });

            small = UserConfig.getInt(baseName + "Small", 0);
            if (small < 0 || small > 255) {
                small = 0;
            }
            smallInput.setText(small + "");
            smallInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkSmall();
                    }
                }
            });

            big = UserConfig.getInt(baseName + "Big", 255);
            if (big < 0 || big > 255) {
                big = 255;
            }
            bigInput.setText(big + "");
            bigInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        checkBig();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkThreshold() {
        int v;
        try {
            v = Integer.parseInt(thresholdInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v <= 255) {
            threshold = v;
            thresholdInput.setStyle(null);
            UserConfig.setInt(baseName + "Threshold", threshold);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("Threshold"));
            thresholdInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    public boolean checkBig() {
        int v;
        try {
            v = Integer.parseInt(bigInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v <= 255) {
            big = v;
            bigInput.setStyle(null);
            UserConfig.setInt(baseName + "BigValue", big);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("BigValue"));
            bigInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    public boolean checkSmall() {
        int v;
        try {
            v = Integer.parseInt(smallInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v >= 0 && v <= 255) {
            small = v;
            smallInput.setStyle(null);
            UserConfig.setInt(baseName + "SmallValue", small);
            return true;
        } else {
            popError(message("InvalidParameter") + ": " + message("SmallValue"));
            smallInput.setStyle(UserConfig.badStyle());
            return false;
        }
    }

    public PixelsOperation pickValues() {
        if (!checkThreshold() || !checkBig() || !checkSmall()) {
            return null;
        }
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                    null, null, PixelsOperation.OperationType.Thresholding)
                    .setIntPara1(threshold)
                    .setIntPara2(big)
                    .setIntPara3(small)
                    .setIsDithering(false);
            return pixelsOperation;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

}
