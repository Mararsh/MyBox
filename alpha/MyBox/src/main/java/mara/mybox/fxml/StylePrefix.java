package mara.mybox.fxml;

import javafx.scene.Node;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

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
                return new StyleData("colorBrightnessRadio", "", Languages.message("Brightness"), "", "iconBrightness.png");
            case "colorHueRadio":
                return new StyleData("colorHueRadio", "", Languages.message("Hue"), "", "iconHue.png");
            case "colorSaturationRadio":
                return new StyleData("colorSaturationRadio", "", Languages.message("Saturation"), "", "iconSaturation.png");
            case "colorRedRadio":
                return new StyleData("colorRedRadio", "", Languages.message("Red"), "", "");
            case "colorGreenRadio":
                return new StyleData("colorGreenRadio", "", Languages.message("Green"), "", "");
            case "colorBlueRadio":
                return new StyleData("colorBlueRadio", "", Languages.message("Blue"), "", "");
            case "colorYellowRadio":
                return new StyleData("colorYellowRadio", "", Languages.message("Yellow"), "", "");
            case "colorCyanRadio":
                return new StyleData("colorCyanRadio", "", Languages.message("Cyan"), "", "");
            case "colorMagentaRadio":
                return new StyleData("colorMagentaRadio", "", Languages.message("Magenta"), "", "");
            case "colorOpacityRadio":
                return new StyleData("colorOpacityRadio", "", Languages.message("Opacity"), "", "iconOpacity.png");
            case "colorColorRadio":
                return new StyleData("colorColorRadio", "", Languages.message("Color"), "", "iconDraw.png");
            case "colorRGBRadio":
                return new StyleData("colorRGBRadio", "", Languages.message("RGB"), "", "iconRGB.png");
            case "colorIncreaseButton":
                return new StyleData("colorIncreaseButton", Languages.message("Increase"), "ALT+2", "iconPlus.png");
            case "colorDecreaseButton":
                return new StyleData("colorDecreaseButton", Languages.message("Decrease"), "ALT+3", "iconMinus.png");
            case "colorFilterButton":
                return new StyleData("colorFilterButton", Languages.message("Filter"), "ALT+4", "iconFilter.png");
            case "colorInvertButton":
                return new StyleData("colorInvertButton", Languages.message("Invert"), "ALT+5", "iconInvert.png");
            case "colorReplaceRadio":
                return new StyleData("colorReplaceRadio", "", Languages.message("ReplaceColor"), "", "iconReplace.png");
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
                return new StyleData("hisDeleteButton", Languages.message("Delete"), "", "iconDelete.png");
            case "hisClearButton":
                return new StyleData("hisClearButton", Languages.message("Clear"), "", "iconClear.png");
            case "hisAsCurrentButton":
                return new StyleData("hisAsCurrentButton", Languages.message("SetAsCurrentImage"), "", "iconWithdraw.png");
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
                return new StyleData(id, "", Languages.message("Scope"), "F7", "iconTarget.png");
            case "scopeAllRadio":
                return new StyleData("scopeAllRadio", "", Languages.message("WholeImage"), "", "iconLoadSize.png");
            case "scopeMattingRadio":
                return new StyleData("scopeMattingRadio", "", Languages.message("Matting"), "", "iconColorFill.png");
            case "scopeRectangleRadio":
                return new StyleData("scopeRectangleRadio", "", Languages.message("Rectangle"), "", "iconRectangle.png");
            case "scopeCircleRadio":
                return new StyleData("scopeCircleRadio", "", Languages.message("Circle"), "", "iconCircle.png");
            case "scopeEllipseRadio":
                return new StyleData("scopeEllipseRadio", "", Languages.message("Ellipse"), "", "iconEllipse.png");
            case "scopePolygonRadio":
                return new StyleData("scopePolygonRadio", "", Languages.message("Polygon"), "", "iconStar.png");
            case "scopeColorRadio":
                return new StyleData("scopeColorRadio", "", Languages.message("ColorMatching"), "", "iconColor.png");
            case "scopeRectangleColorRadio":
                return new StyleData("scopeRectangleColorRadio", "", Languages.message("RectangleColor"), "", "iconRectangleFilled.png");
            case "scopeCircleColorRadio":
                return new StyleData("scopeCircleColorRadio", "", Languages.message("CircleColor"), "", "iconCircleFilled.png");
            case "scopeEllipseColorRadio":
                return new StyleData("scopeEllipseColorRadio", "", Languages.message("EllipseColor"), "", "iconEllipseFilled.png");
            case "scopePolygonColorRadio":
                return new StyleData("scopePolygonColorRadio", "", Languages.message("PolygonColor"), "", "iconStarFilled.png");
            case "scopeOutlineRadio":
                return new StyleData("scopeOutlineRadio", "", Languages.message("Outline"), "", "iconButterfly.png");
            case "scopeCreateButton":
                return new StyleData("scopeCreateButton", Languages.message("Create"), "", "iconEdit.png");
            case "scopeDeletePointButton":
                return new StyleData("scopeDeletePointButton", Languages.message("Delete"), "", "iconDelete.png");
            case "scopeClearPointsButton":
                return new StyleData("scopeClearPointsButton", Languages.message("Clear"), "", "iconClear.png");
            case "scopeDeleteColorButton":
                return new StyleData("scopeDeleteColorButton", Languages.message("Delete"), "", "iconDelete.png");
            case "scopeClearColorsButton":
                return new StyleData("scopeClearColorsButton", Languages.message("Clear"), "", "iconClear.png");
            case "scopeOutlineFileButton":
                return new StyleData("scopeOutlineFileButton", Languages.message("Open"), "", "iconOpen.png");
            case "scopeOutlineKeepRatioCheck":
                return new StyleData("scopeOutlineKeepRatioCheck", Languages.message("KeepRatio"), "", "iconAspectRatio.png");
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
                return new StyleData("settingsButton", Languages.message("Settings"), "", "iconSetting.png");
            case "settingsClearButton":
                return new StyleData("settingsClearButton", Languages.message("ClearPersonalSettings"), "", "iconClear.png");
            case "settingsOpenButton":
                return new StyleData("settingsOpenButton", Languages.message("OpenDataPath"), "", "iconOpen.png");
            case "settingsRecentOKButton":
                return new StyleData("settingsRecentOKButton", Languages.message("OK"), "", "iconOK.png");
            case "settingsJVMButton":
                return new StyleData("settingsJVMButton", Languages.message("OK"), "", "iconOK.png");
            case "settingsRecentNotButton":
                return new StyleData("settingsRecentNotButton", Languages.message("NotRecord"), "", "iconCancel.png");
            case "settingsRecentClearButton":
                return new StyleData("settingsRecentClearButton", Languages.message("Clear"), "", "iconClear.png");
            case "settingsChangeRootButton":
                return new StyleData("settingsChangeRootButton", Languages.message("Change"), "", "iconOK.png");
            case "settingsImageHisOKButton":
                return new StyleData("settingsImageHisOKButton", Languages.message("OK"), "", "iconOK.png");
            case "settingsImageHisNoButton":
                return new StyleData("settingsImageHisNoButton", Languages.message("NotRecord"), "", "iconCancel.png");
            case "settingsImageHisClearButton":
                return new StyleData("settingsImageHisClearButton", Languages.message("Clear"), "", "iconClear.png");
            default:
                return StyleTools.getStyleData(node, id);
        }
    }

}
