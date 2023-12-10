package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageThresholdingController extends BasePixelsController {

    protected int threshold, small, big;

    @FXML
    protected TextField thresholdInput, smallInput, bigInput;

    public ImageThresholdingController() {
        baseTitle = message("Thresholding");
        TipsLabelKey = message("ImageThresholdingComments");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }
            NodeStyleTools.setTooltip(thresholdInput, new Tooltip("0~255"));
            NodeStyleTools.setTooltip(smallInput, new Tooltip("0~255"));
            NodeStyleTools.setTooltip(bigInput, new Tooltip("0~255"));

            threshold = UserConfig.getInt(baseName + "Threshold", 128);
            if (threshold < 0 || threshold > 255) {
                threshold = 128;
            }
            thresholdInput.setText(threshold + "");
            thresholdInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    try {
                        if (newValue) {
                            return;
                        }
                        int v = Integer.parseInt(thresholdInput.getText());
                        if (v >= 0 && v <= 255) {
                            threshold = v;
                            thresholdInput.setStyle(null);
                            UserConfig.setInt(baseName + "Threshold", threshold);
                        } else {
                            thresholdInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        thresholdInput.setStyle(UserConfig.badStyle());
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
                    try {
                        if (newValue) {
                            return;
                        }
                        int v = Integer.parseInt(smallInput.getText());
                        if (v >= 0 && v <= 255) {
                            small = v;
                            smallInput.setStyle(null);
                            UserConfig.setInt(baseName + "Small", small);
                        } else {
                            smallInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        smallInput.setStyle(UserConfig.badStyle());
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
                    try {
                        if (newValue) {
                            return;
                        }
                        int v = Integer.parseInt(bigInput.getText());
                        if (v >= 0 && v <= 255) {
                            big = v;
                            bigInput.setStyle(null);
                            UserConfig.setInt(baseName + "Big", big);
                        } else {
                            bigInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        bigInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        if (threshold >= 0 && threshold <= 255
                && big >= 0 && big <= 255
                && small >= 0 && small <= 255) {
            return true;
        } else {
            popError(message("InvalidParameter"));
            return false;
        }
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    inImage, inScope, PixelsOperation.OperationType.Thresholding)
                    .setIntPara1(threshold)
                    .setIntPara2(big)
                    .setIntPara3(small)
                    .setIsDithering(false)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(task);
            operation = message("Thresholding");
            opInfo = message("Threshold") + ": " + threshold;
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageThresholdingController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageThresholdingController controller = (ImageThresholdingController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageThresholdingFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
