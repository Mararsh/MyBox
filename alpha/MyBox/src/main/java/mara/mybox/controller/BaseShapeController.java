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
        showAnchors = UserConfig.getBoolean(baseName + "ShowAnchor", true);
        popItemMenu = UserConfig.getBoolean(baseName + "ItemPopMenu", true);
        addPointWhenClick = UserConfig.getBoolean(baseName + "AddPointWhenLeftClick", true);
        String aShape = UserConfig.getString(baseName + "AnchorShape", "Rectangle");
        if ("Circle".equals(aShape)) {
            anchorShape = AnchorShape.Circle;
        } else if ("Name".equals(aShape)) {
            anchorShape = AnchorShape.Name;
        } else {
            anchorShape = AnchorShape.Rectangle;
        }
        popShapeMenu = true;
        maskControlDragged = false;
    }

    public void initMaskControls() {
        try {
            if (anchorCheck != null) {
                anchorCheck.setSelected(UserConfig.getBoolean(baseName + "ShowAnchor", true));
                anchorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        UserConfig.setBoolean(baseName + "ShowAnchor", anchorCheck.isSelected());
                        showAnchors = anchorCheck.isSelected();
                        setMaskAnchorsStyle();
                    }
                });
            }

            if (popAnchorMenuCheck != null) {
                popAnchorMenuCheck.setSelected(UserConfig.getBoolean(baseName + "ItemPopMenu", true));
                popAnchorMenuCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        UserConfig.setBoolean(baseName + "ItemPopMenu", popAnchorMenuCheck.isSelected());
                        popItemMenu = popAnchorMenuCheck.isSelected();
                    }
                });
            }

            if (popLineMenuCheck != null) {
                popLineMenuCheck.setSelected(UserConfig.getBoolean(baseName + "ItemPopMenu", true));
                popLineMenuCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        UserConfig.setBoolean(baseName + "ItemPopMenu", popLineMenuCheck.isSelected());
                        popItemMenu = popLineMenuCheck.isSelected();
                    }
                });
            }

            if (addPointCheck != null) {
                addPointCheck.setSelected(UserConfig.getBoolean(baseName + "AddPointWhenLeftClick", true));
                addPointCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        addPointWhenClick = addPointCheck.isSelected();
                        if (!isSettingValues) {
                            UserConfig.setBoolean(baseName + "AddPointWhenLeftClick", addPointCheck.isSelected());
                        }
                    }
                });

            }

            if (shapeCanMoveCheck != null) {
                shapeCanMoveCheck.setSelected(UserConfig.getBoolean(baseName + "ShapeCanMove", true));
                shapeCanMoveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        if (!isSettingValues) {
                            UserConfig.setBoolean(baseName + "ShapeCanMove", shapeCanMoveCheck.isSelected());
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
        notifySize();
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
        ImageShapeOptionsController.open(this, true);
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
