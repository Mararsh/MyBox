package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @License Apache License Version 2.0
 *
 */
public class BaseShapeController extends BaseShapeController_MouseEvents {

    @Override
    public void initControls() {
        try {
            super.initControls();

            clearMask();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initMaskPane() {
        try {

            super.initMaskPane();

            resetShapeOptions();
            initMaskControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void resetShapeOptions() {
        showAnchors = UserConfig.getBoolean(baseName + "ImageShapeShowAnchor", true);
        popAnchorMenu = UserConfig.getBoolean(baseName + "ImageShapeAnchorPopMenu", true);
        addPointWhenClick = UserConfig.getBoolean(baseName + "ImageShapeAddPointWhenLeftClick", true);
        String aShape = UserConfig.getString(baseName + "ImageShapeAnchorShape", "Rectangle");
        if ("Circle".equals(aShape)) {
            anchorShape = AnchorShape.Circle;
        } else if ("Number".equals(aShape)) {
            anchorShape = AnchorShape.Number;
        } else {
            anchorShape = AnchorShape.Rectangle;
        }
        popShapeMenu = true;
        supportPath = false;
        maskControlDragged = false;
    }

    public void initMaskControls() {
        try {
            if (anchorCheck != null) {
                anchorCheck.setSelected(UserConfig.getBoolean(baseName + "ImageShapeShowAnchor", true));
                anchorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        UserConfig.setBoolean(baseName + "ImageShapeShowAnchor", anchorCheck.isSelected());
                        showAnchors = anchorCheck.isSelected();
                        setMaskAnchorsStyle();
                    }
                });
            }

            if (popAnchorCheck != null) {
                popAnchorCheck.setSelected(UserConfig.getBoolean(baseName + "ImageShapeAnchorPopMenu", true));
                popAnchorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        UserConfig.setBoolean(baseName + "ImageShapeAnchorPopMenu", popAnchorCheck.isSelected());
                        popAnchorMenu = popAnchorCheck.isSelected();
                    }
                });
            }

            if (addPointCheck != null) {
                addPointCheck.setSelected(UserConfig.getBoolean(baseName + "ImageShapeAddPointWhenLeftClick", true));
                addPointCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        addPointWhenClick = addPointCheck.isSelected();
                        if (!isSettingValues) {
                            UserConfig.setBoolean(baseName + "ImageShapeAddPointWhenLeftClick", addPointCheck.isSelected());
                        }
                    }
                });

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
        redrawMaskShape();
    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        super.setImageChanged(imageChanged);
        if (imageChanged) {
            redrawMaskShape();
        }
    }

    @Override
    protected String moreDisplayInfo() {
        if (maskRectangle != null && maskRectangle.isVisible() && maskRectangleData != null) {
            return Languages.message("SelectedSize") + ":"
                    + (int) (maskRectangleData.getWidth() / widthRatio()) + "x"
                    + (int) (maskRectangleData.getHeight() / heightRatio());
        } else {
            return "";
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }

            clearMask();
            maskShapeChanged();

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @FXML
    @Override
    public void options() {
        ImageShapeOptionsController.open(this);
    }

    @Override
    public void cleanPane() {
        try {
            maskShapeChanged = null;
            maskShapeDataChanged = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
