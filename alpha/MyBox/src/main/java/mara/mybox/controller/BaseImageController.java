package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @License Apache License Version 2.0
 *
 * BaseImageController < BaseImageController_Actions < BaseImageController_Image
 * < BaseImageController_MouseEvents < BaseImageController_Shapes <
 * BaseImageController_Mask < BaseImageController_ImageView
 */
public abstract class BaseImageController extends BaseImageController_Actions {

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageView();
            initMaskPane();
            initCheckboxs();

            clearMask();

            if (imageBox != null && imageView != null) {
                imageBox.disableProperty().bind(imageView.imageProperty().isNull());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean controlAltK() {
        if (pickColorCheck != null) {
            pickColorCheck.setSelected(!pickColorCheck.isSelected());
            return true;
        } else if (imageView != null && imageView.getImage() != null) {
            isPickingColor = !isPickingColor;
            checkPickingColor();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAltT() {
        if (selectAreaCheck != null) {
            selectAreaCheck.setSelected(!selectAreaCheck.isSelected());
            return true;
        } else if (canSelect()) {
            UserConfig.setBoolean(baseName + "SelectArea", !UserConfig.getBoolean(baseName + "SelectArea", false));
            finalRefineView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAlt1() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        loadedSize();
        return true;
    }

    @Override
    public boolean controlAlt2() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        paneSize();
        return true;
    }

    @Override
    public boolean controlAlt3() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        zoomIn();
        return true;
    }

    @Override
    public boolean controlAlt4() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return false;
        }
        zoomOut();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            if (loadTask != null) {
                loadTask.cancel();
                loadTask = null;
            }
            if (paletteController != null) {
                paletteController.closeStage();
                paletteController = null;
            }
            maskShapeChanged = null;
            maskShapeDataChanged = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
