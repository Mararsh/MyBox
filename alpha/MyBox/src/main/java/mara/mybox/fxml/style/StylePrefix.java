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
        if (id.startsWith("colorReplace")) {
            return new StyleData(id, "", message("ReplaceColor"), "", "iconReplace.png");
        } else if (id.startsWith("colorColor")) {
            return new StyleData(id, "", message("Color"), "", "iconDraw.png");
        } else if (id.startsWith("colorBlend")) {
            return new StyleData(id, "", message("Blend"), "", "iconCross.png");
        } else if (id.startsWith("colorRGB")) {
            return new StyleData(id, "", message("RGB"), "", "iconRGB.png");
        } else if (id.startsWith("colorBrightness")) {
            return new StyleData(id, "", message("Brightness"), "", "iconBrightness.png");
        } else if (id.startsWith("colorHue")) {
            return new StyleData(id, "", message("Hue"), "", "iconHue.png");
        } else if (id.startsWith("colorSaturation")) {
            return new StyleData(id, "", message("Saturation"), "", "iconSaturation.png");
        } else if (id.startsWith("colorRed")) {
            return new StyleData(id, "", message("Red"), "", "");
        } else if (id.startsWith("colorGreen")) {
            return new StyleData(id, "", message("Green"), "", "");
        } else if (id.startsWith("colorBlue")) {
            return new StyleData(id, "", message("Blue"), "", "");
        } else if (id.startsWith("colorYellow")) {
            return new StyleData(id, "", message("Yellow"), "", "");
        } else if (id.startsWith("colorCyan")) {
            return new StyleData(id, "", message("Cyan"), "", "");
        } else if (id.startsWith("colorMagenta")) {
            return new StyleData(id, "", message("Magenta"), "", "");
        } else if (id.startsWith("colorOpacity")) {
            return new StyleData(id, "", message("Opacity"), "", "iconOpacity.png");
        } else if (id.startsWith("colorSet")) {
            return new StyleData(id, message("Set"), "CTRL+S / ALT+S", "iconEqual.png");
        } else if (id.startsWith("colorIncrease")) {
            return new StyleData(id, message("Increase"), "CTRL+I / ALT+I", "iconPlus.png");
        } else if (id.startsWith("colorDecrease")) {
            return new StyleData(id, message("Decrease"), "CTRL+D / ALT+D", "iconMinus.png");
        } else if (id.startsWith("colorFilter")) {
            return new StyleData(id, message("Filter"), "CTRL+F / ALT+F", "iconFilter.png");
        } else if (id.startsWith("colorInvert")) {
            return new StyleData(id, message("Invert"), "CTRL+X / ALT+X", "iconInvert.png");
        } else {
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
                return new StyleData(id, message("SetAsCurrentImage"), "", "iconUndo.png");
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
                return new StyleData(id, message("Open"), "", "iconSelectFile.png");
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
            case "settingsClearButton":
                return new StyleData(id, message("ClearPersonalSettings"), "", "iconClear.png");
            case "settingsOpenButton":
                return new StyleData(id, message("OpenDataPath"), "", "iconOpenPath.png");
            case "settingsRecentOKButton":
                return new StyleData(id, message("OK"), "", "iconOK.png");
            case "settingsJVMButton":
                return new StyleData(id, message("OK"), "", "iconOK.png");
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
