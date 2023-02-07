package mara.mybox.fxml.style;

import javafx.scene.Node;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StylePrefix {

    public static StyleData color(Node node, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "colorBrightnessRadio":
                return new StyleData(id, "", message("Brightness"), "", "iconBrightness.png");
            case "colorHueRadio":
                return new StyleData(id, "", message("Hue"), "", "iconHue.png");
            case "colorSaturationRadio":
                return new StyleData(id, "", message("Saturation"), "", "iconSaturation.png");
            case "colorRedRadio":
                return new StyleData(id, "", message("Red"), "", "");
            case "colorGreenRadio":
                return new StyleData(id, "", message("Green"), "", "");
            case "colorBlueRadio":
                return new StyleData(id, "", message("Blue"), "", "");
            case "colorYellowRadio":
                return new StyleData(id, "", message("Yellow"), "", "");
            case "colorCyanRadio":
                return new StyleData(id, "", message("Cyan"), "", "");
            case "colorMagentaRadio":
                return new StyleData(id, "", message("Magenta"), "", "");
            case "colorOpacityRadio":
                return new StyleData(id, "", message("Opacity"), "", "iconOpacity.png");
            case "colorColorRadio":
                return new StyleData(id, "", message("Color"), "", "iconDraw.png");
            case "colorBlendRadio":
                return new StyleData(id, "", message("Blend"), "", "iconCross.png");
            case "colorRGBRadio":
                return new StyleData(id, "", message("RGB"), "", "iconRGB.png");
            case "colorIncreaseButton":
                return new StyleData(id, message("Increase"), "ALT+2", "iconPlus.png");
            case "colorDecreaseButton":
                return new StyleData(id, message("Decrease"), "ALT+3", "iconMinus.png");
            case "colorFilterButton":
                return new StyleData(id, message("Filter"), "ALT+4", "iconFilter.png");
            case "colorInvertButton":
                return new StyleData(id, message("Invert"), "ALT+5", "iconInvert.png");
            case "colorReplaceRadio":
                return new StyleData(id, "", message("ReplaceColor"), "", "iconReplace.png");
            case "setRadio":
                return new StyleData(id, "", message("ReplaceColor"), "", "iconReplace.png");
            case "invertRadio":
                return new StyleData(id, "", message("ReplaceColor"), "", "iconReplace.png");
            case "increaseRadio":
                return new StyleData(id, "", message("ReplaceColor"), "", "iconReplace.png");
            case "decreaseRadio":
                return new StyleData(id, "", message("ReplaceColor"), "", "iconReplace.png");
            case "filterRadio":
                return new StyleData(id, "", message("ReplaceColor"), "", "iconReplace.png");
            default:
                return StyleTools.getStyleData(node, id);
        }
    }

    public static StyleData his(Node node, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "hisDeleteButton":
                return new StyleData(id, message("Delete"), "", "iconDelete.png");
            case "hisClearButton":
                return new StyleData(id, message("Clear"), "", "iconClear.png");
            case "hisAsCurrentButton":
                return new StyleData(id, message("SetAsCurrentImage"), "", "iconWithdraw.png");
            default:
                return StyleTools.getStyleData(node, id);
        }
    }

    public static StyleData scope(Node node, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "scopeButton":
                return new StyleData(id, "", message("Scope"), "", "iconTarget.png");
            case "scopeAllRadio":
                return new StyleData(id, "", message("WholeImage"), "", "iconSelectAll.png");
            case "scopeMattingRadio":
                return new StyleData(id, "", message("Matting"), "", "iconColorFill.png");
            case "scopeRectangleRadio":
                return new StyleData(id, "", message("Rectangle"), "", "iconRectangle.png");
            case "scopeCircleRadio":
                return new StyleData(id, "", message("Circle"), "", "iconCircle.png");
            case "scopeEllipseRadio":
                return new StyleData(id, "", message("Ellipse"), "", "iconEllipse.png");
            case "scopePolygonRadio":
                return new StyleData(id, "", message("Polygon"), "", "iconStar.png");
            case "scopeColorRadio":
                return new StyleData(id, "", message("ColorMatching"), "", "iconColor.png");
            case "scopeRectangleColorRadio":
                return new StyleData(id, "", message("RectangleColor"), "", "iconRectangleFilled.png");
            case "scopeCircleColorRadio":
                return new StyleData(id, "", message("CircleColor"), "", "iconCircleFilled.png");
            case "scopeEllipseColorRadio":
                return new StyleData(id, "", message("EllipseColor"), "", "iconEllipseFilled.png");
            case "scopePolygonColorRadio":
                return new StyleData(id, "", message("PolygonColor"), "", "iconStarFilled.png");
            case "scopeOutlineRadio":
                return new StyleData(id, "", message("Outline"), "", "iconButterfly.png");
            case "scopeCreateButton":
                return new StyleData(id, message("Create"), "", "iconEdit.png");
            case "scopeDeletePointButton":
                return new StyleData(id, message("Delete"), "", "iconDelete.png");
            case "scopeClearPointsButton":
                return new StyleData(id, message("Clear"), "", "iconClear.png");
            case "scopeDeleteColorButton":
                return new StyleData(id, message("Delete"), "", "iconDelete.png");
            case "scopeClearColorsButton":
                return new StyleData(id, message("Clear"), "", "iconClear.png");
            case "scopeOutlineFileButton":
                return new StyleData(id, message("Open"), "", "iconOpen.png");
            case "scopeOutlineKeepRatioCheck":
                return new StyleData(id, message("KeepRatio"), "", "iconAspectRatio.png");
            default:
                return StyleTools.getStyleData(node, id);
        }
    }

    public static StyleData settings(Node node, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "settingsButton":
                return new StyleData(id, message("Settings"), "", "iconSetting.png");
            case "settingsClearButton":
                return new StyleData(id, message("ClearPersonalSettings"), "", "iconClear.png");
            case "settingsOpenButton":
                return new StyleData(id, message("OpenDataPath"), "", "iconOpen.png");
            case "settingsRecentOKButton":
                return new StyleData(id, message("OK"), "", "iconOK.png");
            case "settingsJVMButton":
                return new StyleData(id, message("OK"), "", "iconOK.png");
            case "settingsRecentNotButton":
                return new StyleData(id, message("NotRecord"), "", "iconCancel.png");
            case "settingsRecentClearButton":
                return new StyleData(id, message("Clear"), "", "iconClear.png");
            case "settingsChangeRootButton":
                return new StyleData(id, message("Change"), "", "iconOK.png");
            case "settingsImageHisOKButton":
                return new StyleData(id, message("OK"), "", "iconOK.png");
            case "settingsImageHisNoButton":
                return new StyleData(id, message("NotRecord"), "", "iconCancel.png");
            case "settingsImageHisClearButton":
                return new StyleData(id, message("Clear"), "", "iconClear.png");
            default:
                return StyleTools.getStyleData(node, id);
        }
    }

}
