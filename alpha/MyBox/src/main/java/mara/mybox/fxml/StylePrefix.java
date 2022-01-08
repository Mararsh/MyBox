package mara.mybox.fxml;

import javafx.scene.Node;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StylePrefix {

    public static StyleData his(Node node, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "hisDeleteButton":
                return new StyleData("hisDeleteButton", message("Delete"), "", "iconDelete.png");
            case "hisClearButton":
                return new StyleData("hisClearButton", message("Clear"), "", "iconClear.png");
            case "hisAsCurrentButton":
                return new StyleData("hisAsCurrentButton", message("SetAsCurrentImage"), "", "iconWithdraw.png");
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
                return new StyleData("scopeAllRadio", "", message("WholeImage"), "", "iconSelectAll.png");
            case "scopeMattingRadio":
                return new StyleData("scopeMattingRadio", "", message("Matting"), "", "iconColorFill.png");
            case "scopeRectangleRadio":
                return new StyleData("scopeRectangleRadio", "", message("Rectangle"), "", "iconRectangle.png");
            case "scopeCircleRadio":
                return new StyleData("scopeCircleRadio", "", message("Circle"), "", "iconCircle.png");
            case "scopeEllipseRadio":
                return new StyleData("scopeEllipseRadio", "", message("Ellipse"), "", "iconEllipse.png");
            case "scopePolygonRadio":
                return new StyleData("scopePolygonRadio", "", message("Polygon"), "", "iconStar.png");
            case "scopeColorRadio":
                return new StyleData("scopeColorRadio", "", message("ColorMatching"), "", "iconColor.png");
            case "scopeRectangleColorRadio":
                return new StyleData("scopeRectangleColorRadio", "", message("RectangleColor"), "", "iconRectangleFilled.png");
            case "scopeCircleColorRadio":
                return new StyleData("scopeCircleColorRadio", "", message("CircleColor"), "", "iconCircleFilled.png");
            case "scopeEllipseColorRadio":
                return new StyleData("scopeEllipseColorRadio", "", message("EllipseColor"), "", "iconEllipseFilled.png");
            case "scopePolygonColorRadio":
                return new StyleData("scopePolygonColorRadio", "", message("PolygonColor"), "", "iconStarFilled.png");
            case "scopeOutlineRadio":
                return new StyleData("scopeOutlineRadio", "", message("Outline"), "", "iconButterfly.png");
            case "scopeCreateButton":
                return new StyleData("scopeCreateButton", message("Create"), "", "iconEdit.png");
            case "scopeDeletePointButton":
                return new StyleData("scopeDeletePointButton", message("Delete"), "", "iconDelete.png");
            case "scopeClearPointsButton":
                return new StyleData("scopeClearPointsButton", message("Clear"), "", "iconClear.png");
            case "scopeDeleteColorButton":
                return new StyleData("scopeDeleteColorButton", message("Delete"), "", "iconDelete.png");
            case "scopeClearColorsButton":
                return new StyleData("scopeClearColorsButton", message("Clear"), "", "iconClear.png");
            case "scopeOutlineFileButton":
                return new StyleData("scopeOutlineFileButton", message("Open"), "", "iconOpen.png");
            case "scopeOutlineKeepRatioCheck":
                return new StyleData("scopeOutlineKeepRatioCheck", message("KeepRatio"), "", "iconAspectRatio.png");
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
                return new StyleData("settingsButton", message("Settings"), "", "iconSetting.png");
            case "settingsClearButton":
                return new StyleData("settingsClearButton", message("ClearPersonalSettings"), "", "iconClear.png");
            case "settingsOpenButton":
                return new StyleData("settingsOpenButton", message("OpenDataPath"), "", "iconOpen.png");
            case "settingsRecentOKButton":
                return new StyleData("settingsRecentOKButton", message("OK"), "", "iconOK.png");
            case "settingsJVMButton":
                return new StyleData("settingsJVMButton", message("OK"), "", "iconOK.png");
            case "settingsRecentNotButton":
                return new StyleData("settingsRecentNotButton", message("NotRecord"), "", "iconCancel.png");
            case "settingsRecentClearButton":
                return new StyleData("settingsRecentClearButton", message("Clear"), "", "iconClear.png");
            case "settingsChangeRootButton":
                return new StyleData("settingsChangeRootButton", message("Change"), "", "iconOK.png");
            case "settingsImageHisOKButton":
                return new StyleData("settingsImageHisOKButton", message("OK"), "", "iconOK.png");
            case "settingsImageHisNoButton":
                return new StyleData("settingsImageHisNoButton", message("NotRecord"), "", "iconCancel.png");
            case "settingsImageHisClearButton":
                return new StyleData("settingsImageHisClearButton", message("Clear"), "", "iconClear.png");
            default:
                return StyleTools.getStyleData(node, id);
        }
    }

}
