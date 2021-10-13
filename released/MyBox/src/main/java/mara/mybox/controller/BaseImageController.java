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

            if (sampledView != null) {
                sampledView.setVisible(false);
            }
            initImageView();
            initViewControls();
            initMaskPane();
            initMaskControls(false);
            initOperationBox();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean controlAltK() {
        if (pickColorCheck != null) {
            pickColorCheck.setSelected(!pickColorCheck.isSelected());
        } else {
            isPickingColor = !isPickingColor;
            checkPickingColor();
        }
        return true;
    }

    @Override
    public boolean controlAltT() {
        if (selectAreaCheck != null) {
            selectAreaCheck.setSelected(!selectAreaCheck.isSelected());
        } else {
            UserConfig.setBoolean(baseName + "SelectArea", !UserConfig.getBoolean(baseName + "SelectArea", false));
            checkSelect();
        }
        return true;
    }

    @Override
    public boolean controlAlt1() {
        loadedSize();
        return true;
    }

    @Override
    public boolean controlAlt2() {
        paneSize();
        return true;
    }

    @Override
    public boolean controlAlt3() {
        zoomIn();
        return true;
    }

    @Override
    public boolean controlAlt4() {
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
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
