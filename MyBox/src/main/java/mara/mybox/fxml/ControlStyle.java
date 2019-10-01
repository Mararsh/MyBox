package mara.mybox.fxml;

import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-4-26 7:16:14
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ControlStyle {

    public static Map<String, ControlStyle> ControlsList;
    public static String DefaultButtonPath = "buttons/";
    public static String RedButtonPath = "buttons/";
    public static String LightBlueButtonPath = "buttonsLightBlue/";
    public static String BlueButtonPath = "buttonsBlue/";
    public static String PinkButtonPath = "buttonsPink/";
    public static String OrangeButtonPath = "buttonsOrange/";

    public static enum ColorStyle {
        Default, Red, Blue, LightBlue, Pink, Orange
    }

    private String id, name, comments, shortcut, iconName;

    public ControlStyle(String id) {
        this.id = id;
    }

    public ControlStyle(String id, String name, String shortcut, String iconName) {
        this.id = id;
        this.name = name;
        this.shortcut = shortcut;
        this.iconName = iconName;
    }

    public ControlStyle(String id, String name, String comments, String shortcut, String iconName) {
        this.id = id;
        this.name = name;
        this.comments = comments;
        this.shortcut = shortcut;
        this.iconName = iconName;
    }

    /*
        Static methods
     */
    public static ControlStyle getControlStyle(Node node) {
        if (node == null || node.getId() == null) {
            return null;
        }
        String id = node.getId();
        ControlStyle style;
        if (id.startsWith("his")) {
            style = getHisControlStyle(id);
        } else if (id.startsWith("settings")) {
            style = getSettingsControlStyle(id);
        } else if (id.startsWith("scope")) {
            style = getScopeControlStyle(id);
        } else if (id.startsWith("color")) {
            style = getColorControlStyle(id);
        } else if (id.startsWith("imageManu")) {
            style = getImageManuControlStyle(id);
        } else if (node instanceof ImageView) {
            style = getImageViewStyle(id);
        } else if (node instanceof RadioButton) {
            style = getRadioButtonStyle(id);
        } else if (node instanceof CheckBox) {
            style = getCheckBoxStyle(id);
        } else if (node instanceof ToggleButton) {
            style = getToggleButtonStyle(id);
        } else {
            style = getOtherControlStyle(id);

        }
        return style;
    }

    public static ControlStyle getSettingsControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "settingsButton":
                return new ControlStyle("settingsButton", message("Settings"), "", "iconSetting.png");

            case "settingsClearButton":
                return new ControlStyle("settingsClearButton", message("ClearPersonalSettings"), "", "iconClear.png");

            case "settingsOpenButton":
                return new ControlStyle("settingsOpenButton", message("OpenUserPath"), "", "iconOpen.png");

            case "settingsStrokeColorButton":
                return new ControlStyle("settingsStrokeColorButton", message("ColorPalette"), "", "iconPalette.png");

            case "settingsAnchorColorButton":
                return new ControlStyle("settingsAnchorColorButton", message("ColorPalette"), "", "iconPalette.png");

            case "settingsAlphaColorButton":
                return new ControlStyle("settingsAlphaColorButton", message("ColorPalette"), "", "iconPalette.png");

            case "settingsRecentOKButton":
                return new ControlStyle("settingsRecentOKButton", message("OK"), "", "iconOK.png");

            case "settingsJVMButton":
                return new ControlStyle("settingsJVMButton", message("OK"), "", "iconOK.png");

            case "settingsRecentNotButton":
                return new ControlStyle("settingsRecentNotButton", message("NotRecord"), "", "iconCancel.png");

            case "settingsRecentClearButton":
                return new ControlStyle("settingsRecentClearButton", message("Clear"), "", "iconClear.png");

            case "settingsChangeRootButton":
                return new ControlStyle("settingsChangeRootButton", message("Change"), "", "iconOK.png");

            case "settingsImageHisOKButton":
                return new ControlStyle("settingsImageHisOKButton", message("OK"), "", "iconOK.png");

            case "settingsImageHisNoButton":
                return new ControlStyle("settingsImageHisNoButton", message("NotRecord"), "", "iconCancel.png");

            case "settingsImageHisClearButton":
                return new ControlStyle("settingsImageHisClearButton", message("Clear"), "", "iconClear.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getImageManuControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "imageManuHisDeleteButton":
                return new ControlStyle("hisDeleteButton", message("Delete"), "", "iconDelete.png");

            case "imageManuHisClearButton":
                return new ControlStyle("hisClearButton", message("Clear"), "", "iconClear.png");

            case "imageManuHisAsCurrentButton":
                return new ControlStyle("hisAsCurrentButton", message("SetAsCurrentImage"), "", "iconWithdraw.png");

            case "imageManuHisPreviousButton":
                return new ControlStyle("imageManuHisPreviousButton", message("Previous"), "", "iconPrevious.png");

            case "imageManuHisNextButton":
                return new ControlStyle("imageManuHisNextButton", message("Next"), "", "iconNext.png");

            case "imageManuSelectRefButton":
                return new ControlStyle("imageManuSelectRefButton", message("Select"), "", "iconOpen.png");

            case "imageManuRefMetaButton":
                return new ControlStyle("imageManuRefMetaButton", message("MetaData"), "", "iconMeta.png");

            case "imageManuRefIInfoButton":
                return new ControlStyle("imageManuRefIInfoButton", message("Information"), "", "iconInfo.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getHisControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "hisDeleteButton":
                return new ControlStyle("hisDeleteButton", message("Delete"), "", "iconDelete.png");

            case "hisClearButton":
                return new ControlStyle("hisClearButton", message("Clear"), "", "iconClear.png");

            case "hisAsCurrentButton":
                return new ControlStyle("hisAsCurrentButton", message("SetAsCurrentImage"), "", "iconWithdraw.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getColorControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "colorBrightnessRadio":
                return new ControlStyle("colorBrightnessRadio", "", message("Brightness"), "", "iconBrightness.png");

            case "colorHueRadio":
                return new ControlStyle("colorHueRadio", "", message("Hue"), "", "iconHue.png");

            case "colorSaturationRadio":
                return new ControlStyle("colorSaturationRadio", "", message("Saturation"), "", "iconSaturation.png");

            case "colorRedRadio":
                return new ControlStyle("colorRedRadio", "", message("Red"), "", "");

            case "colorGreenRadio":
                return new ControlStyle("colorGreenRadio", "", message("Green"), "", "");

            case "colorBlueRadio":
                return new ControlStyle("colorBlueRadio", "", message("Blue"), "", "");

            case "colorYellowRadio":
                return new ControlStyle("colorYellowRadio", "", message("Yellow"), "", "");

            case "colorCyanRadio":
                return new ControlStyle("colorCyanRadio", "", message("Cyan"), "", "");

            case "colorMagentaRadio":
                return new ControlStyle("colorMagentaRadio", "", message("Magenta"), "", "");

            case "colorOpacityRadio":
                return new ControlStyle("colorOpacityRadio", "", message("Opacity"), "", "iconOpacity.png");

            case "colorColorRadio":
                return new ControlStyle("colorColorRadio", "", message("Color"), "", "iconDraw.png");

            case "colorRGBRadio":
                return new ControlStyle("colorRGBRadio", "", message("RGB"), "", "iconRGB.png");

            case "colorIncreaseButton":
                return new ControlStyle("colorIncreaseButton", message("Increase"), "ALT+2", "iconPlus.png");

            case "colorDecreaseButton":
                return new ControlStyle("colorDecreaseButton", message("Decrease"), "ALT+3", "iconMinus.png");

            case "colorFilterButton":
                return new ControlStyle("colorFilterButton", message("Filter"), "ALT+4", "iconFilter.png");

            case "colorInvertButton":
                return new ControlStyle("colorInvertButton", message("Invert"), "ALT+5", "iconInvert.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getScopeControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "scopeAllRadio":
                return new ControlStyle("scopeAllRadio", "", message("WholeImage"), "", "iconPicSmall.png");

            case "scopeMattingRadio":
                return new ControlStyle("scopeMattingRadio", "", message("Matting"), "", "iconColorFill.png");

            case "scopeRectangleRadio":
                return new ControlStyle("scopeRectangleRadio", "", message("Rectangle"), "", "iconRectangle.png");

            case "scopeCircleRadio":
                return new ControlStyle("scopeCircleRadio", "", message("Circle"), "", "iconCircle.png");

            case "scopeEllipseRadio":
                return new ControlStyle("scopeEllipseRadio", "", message("Ellipse"), "", "iconEllipse.png");

            case "scopePolygonRadio":
                return new ControlStyle("scopePolygonRadio", "", message("Polygon"), "", "iconStar.png");

            case "scopeColorRadio":
                return new ControlStyle("scopeColorRadio", "", message("ColorMatching"), "", "iconColor.png");

            case "scopeRectangleColorRadio":
                return new ControlStyle("scopeRectangleColorRadio", "", message("RectangleColor"), "", "iconRectangleFilled.png");

            case "scopeCircleColorRadio":
                return new ControlStyle("scopeCircleColorRadio", "", message("CircleColor"), "", "iconCircleFilled.png");

            case "scopeEllipseColorRadio":
                return new ControlStyle("scopeEllipseColorRadio", "", message("EllipseColor"), "", "iconEllipseFilled.png");

            case "scopePolygonColorRadio":
                return new ControlStyle("scopePolygonColorRadio", "", message("PolygonColor"), "", "iconStarFilled.png");

            case "scopeOutlineRadio":
                return new ControlStyle("scopeOutlineRadio", "", message("Outline"), "", "iconButterfly.png");

            case "scopeSaveButton":
                return new ControlStyle("scopeSaveButton", message("Save"), "", "iconSave.png");

            case "scopeCreateButton":
                return new ControlStyle("scopeCreateButton", message("Create"), "", "iconEdit.png");

            case "scopeUseButton":
                return new ControlStyle("scopeUseButton", message("Use"), "", "iconOK2.png");

            case "scopeClearButton":
                return new ControlStyle("scopeClearButton", message("Clear"), "", "iconClear.png");

            case "scopeDeleteButton":
                return new ControlStyle("scopeDeleteButton", message("Delete"), "", "iconDelete.png");

            case "scopeDeletePointButton":
                return new ControlStyle("scopeDeletePointButton", message("Create"), "", "iconOK2.png");

            case "scopeClearPointsButton":
                return new ControlStyle("scopeClearPointsButton", message("Delete"), "", "iconClear.png");

            case "scopeDeleteColorButton":
                return new ControlStyle("scopeDeleteColorButton", message("Clear"), "", "iconOK2.png");

            case "scopeClearColorsButton":
                return new ControlStyle("scopeClearColorsButton", message("Delete"), "", "iconClear.png");

            case "scopeOutlineFileButton":
                return new ControlStyle("scopeOutlineFileButton", message("Open"), "", "iconOpen.png");

            case "scopeSetCheck":
                return new ControlStyle("scopeSetCheck", message("SetScope"), "", "iconTarget.png");

            case "scopeManageCheck":
                return new ControlStyle("scopeManageCheck", message("ManageScope"), "", "iconData.png");

            case "scopeOutlineKeepRatioCheck":
                return new ControlStyle("scopeOutlineKeepRatioCheck", message("KeepRatio"), "", "iconAspectRatio.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getRadioButtonStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "miaoButton":
                return new ControlStyle("miaoButton", message("Meow"), message("MiaoPrompt"), "", "iconCat.png");

            case "pcxSelect":
                return new ControlStyle("pcxSelect", "pcx", message("PcxComments"), "", "");

            case "polylineRadio":
                return new ControlStyle("polylineRadio", "", message("Polyline"), "", "iconPolyline.png");

            case "linesRadio":
                return new ControlStyle("linesRadio", "", message("DrawLines"), "", "iconDraw.png");

            case "rectangleRadio":
                return new ControlStyle("rectangleRadio", "", message("Rectangle"), "", "iconRectangle.png");

            case "circleRadio":
                return new ControlStyle("circleRadio", "", message("Circle"), "", "iconCircle.png");

            case "ellipseRadio":
                return new ControlStyle("ellipseRadio", "", message("Ellipse"), "", "iconEllipse.png");

            case "polygonRadio":
                return new ControlStyle("polygonRadio", "", message("Polygon"), "", "iconStar.png");

            case "eraserRadio":
                return new ControlStyle("eraserRadio", "", message("Eraser"), "", "iconEraser.png");

            case "mosaicRadio":
                return new ControlStyle("mosaicRadio", "", message("Mosaic"), "", "iconMosaic.png");

            case "frostedRadio":
                return new ControlStyle("frostedRadio", "", message("FrostedGlass"), "", "iconFrosted.png");

            case "shapeRectangleRadio":
                return new ControlStyle("shapeRectangleRadio", "", message("Rectangle"), "", "iconRectangle.png");

            case "shapeCircleRadio":
                return new ControlStyle("shapeCircleRadio", "", message("Circle"), "", "iconCircle.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getCheckBoxStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "tableSubdirCheck":
                return new ControlStyle("tableSubdirCheck", "", message("HandleSubDirectories"), "", "iconFolder.png");

            case "tableExpandDirCheck":
                return new ControlStyle("tableExpandDirCheck", "", message("ExpandDirectories"), "", "iconUnfold.png");

            case "tableThumbCheck":
                return new ControlStyle("tableThumbCheck", "", message("Thumbnail"), "", "iconThumbsList.png");

            case "miaoCheck":
                return new ControlStyle("miaoCheck", message("Meow"), message("MiaoPrompt"), "", "iconCat.png");

            case "pdfMemBox":
                return new ControlStyle("pdfMemBox", "", message("PdfMemComments"), "", "");

            case "openCheck":
                return new ControlStyle("openCheck", "", message("OpenWhenComplete"), "", "iconOpen2.png");

            case "selectAreaCheck":
                return new ControlStyle("selectAreaCheck", "", message("SelectArea"), "", "iconTarget.png");

            case "openSaveCheck":
                return new ControlStyle("openSaveCheck", "", message("OpenAfterSave"), "", "iconOpen2.png");

            case "deleteConfirmCheck":
                return new ControlStyle("deleteConfirmCheck", "", message("ConfirmWhenDelete"), "", "iconConfirm.png");

            case "bookmarksCheck":
                return new ControlStyle("bookmarksCheck", "", message("Bookmarks"), "", "iconTree.png");

            case "thumbCheck":
                return new ControlStyle("thumbCheck", "", message("Thumbnails"), "", "iconBrowse.png");

            case "coordinateCheck":
                return new ControlStyle("coordinateCheck", "", message("Coordinate"), "", "iconLocation.png");

            case "rulerXCheck":
                return new ControlStyle("rulerXCheck", "", message("RulerX"), "", "iconXRuler.png");

            case "rulerYCheck":
                return new ControlStyle("rulerYCheck", "", message("RulerY"), "", "iconYRuler.png");

            case "statisticCheck":
                return new ControlStyle("statisticCheck", "", message("Statistic"), "", "iconStatistic.png");

            case "transparentBackgroundCheck":
                return new ControlStyle("transparentBackgroundCheck", "", message("TransparentBackground"), "", "iconOpacity.png");

            case "saveRotationCheck":
                return new ControlStyle("saveRotationCheck", "", message("SaveRotation"), "", "iconSave.png");

            case "displaySizeCheck":
                return new ControlStyle("displaySizeCheck", "", message("DisplaySize"), "", "iconInfo2.png");

            case "topCheck":
                return new ControlStyle("topCheck", "", message("AlwayOnTop"), "", "iconTop.png");

            case "saveCloseCheck":
                return new ControlStyle("saveCloseCheck", "", message("SaveClose"), "", "iconFlower.png");

            case "synchronizeCheck":
                return new ControlStyle("synchronizeCheck", "", message("Synchronized"), "", "iconSynchronize.png");

            case "deskewCheck":
                return new ControlStyle("deskewCheck", "", message("Deskew"), "", "iconShear.png");

            case "invertCheck":
                return new ControlStyle("invertCheck", "", message("Invert"), "", "iconInvert.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getImageViewStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "tipsView":
                return new ControlStyle("tipsView", "", "", "", "iconTips.png");

            case "linksView":
                return new ControlStyle("linksView", "", message("Links"), "", "iconLink.png");

            case "refTipsView":
                return new ControlStyle("refTipsView", "", message("ImageRefTips"), "", "iconTips.png");

            case "textTipsView":
                return new ControlStyle("textTipsView", "", "", "iconTips.png");

            case "preAlphaTipsView":
                return new ControlStyle("preAlphaTipsView", "", message("PremultipliedAlphaTips"), "", "iconTips.png");

            case "weiboTipsView":
                return new ControlStyle("weiboTipsView", "", message("WeiboAddressComments"), "", "iconTips.png");

            case "thresholdingTipsView":
                return new ControlStyle("thresholdingTipsView", "", message("ThresholdingComments"), "", "iconTips.png");

            case "quantizationTipsView":
                return new ControlStyle("quantizationTipsView", "", message("QuantizationComments"), "", "iconTips.png");

            case "BWThresholdTipsView":
                return new ControlStyle("BWThresholdTipsView", "", message("BWThresholdComments"), "", "iconTips.png");

            case "distanceTipsView":
                return new ControlStyle("distanceTipsView", "", message("ColorMatchComments"), "", "iconTips.png");

            case "weiboSnapTipsView":
                return new ControlStyle("weiboSnapTipsView", "", message("htmlSnapComments"), "", "iconTips.png");

            case "fontTipsView":
                return new ControlStyle("fontTipsView", "", message("FontFileComments"), "", "iconTips.png");

            case "pdfPageSizeTipsView":
                return new ControlStyle("pdfPageSizeTipsView", "", message("PdfPageSizeComments"), "", "iconTips.png");

            case "pdfMemTipsView":
                return new ControlStyle("pdfMemTipsView", "", message("PdfMemComments"), "", "iconTips.png");

            case "ditherTipsView":
                return new ControlStyle("ditherTipsView", "", message("DitherComments"), "", "iconTips.png");

            case "effectTipsView":
                return new ControlStyle("effectTipsView", "", "", "", "iconTips.png");

            case "leftPaneControl":
                return new ControlStyle("leftPaneControl", "", "", "", "iconsDoubleLeft.png");

            case "rightPaneControl":
                return new ControlStyle("rightPaneControl", "", "", "", "iconsDoubleRight.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getToggleButtonStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "pickColorButton":
                return new ControlStyle("pickColorButton", message("PickColor"), message("ColorPickerComments"), "", "iconPickColor.png");

            case "pickFillColorButton":
                return new ControlStyle("pickFillColorButton", message("PickColor"), message("ColorPickerComments"), "", "iconPickColor.png");

            default:
                return getOtherControlStyle(id);
        }

    }

    public static ControlStyle getOtherControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "selectSourceButton":
                return new ControlStyle("selectSourceButton", message("Select"), "", "iconOpen.png");

            case "okButton":
                return new ControlStyle("okButton", message("OK"), "F1 / CTRL+e / ALT+e", "iconOK.png");

            case "cancelButton":
                return new ControlStyle("cancelButton", message("Cancel"), "ESC", "iconCancel.png");

            case "createButton":
                return new ControlStyle("createButton", message("Create"), "CTRL+n", "iconEdit.png");

            case "deleteButton":
                return new ControlStyle("deleteButton", message("Delete"), "DELETE / CTRL+d / ALT+d", "iconDelete.png");

            case "copyButton":
                return new ControlStyle("copyButton", message("Copy"), "CTRL+c / ALT+c ", "iconCopy.png");

            case "pasteButton":
                return new ControlStyle("pasteButton", message("Paste"), "CTRL+v / ALT+v", "iconPaste.png");

            case "saveButton":
                return new ControlStyle("saveButton", message("Save"), "F2 / CTRL+s / ALT+s", "iconSave.png");

            case "saveAsButton":
                return new ControlStyle("saveAsButton", message("SaveAs"), "F11 / CTRL+f / ALT+f", "iconSaveAs.png");

            case "selectAllButton":
                return new ControlStyle("selectAllButton", message("SelectAll"), "CTRL+a / ALT+a", "iconSelectAll.png");

            case "cropButton":
                return new ControlStyle("cropButton", message("Crop"), "CTRL+x / ALT+x", "iconCrop.png");

            case "recoverButton":
                return new ControlStyle("recoverButton", message("Recover"), "F3 / CTRL+r / ALT+r", "iconRecover.png");

            case "renameButton":
                return new ControlStyle("renameButton", message("Rename"), "", "iconRename.png");

            case "infoButton":
                return new ControlStyle("infoButton", message("Information"), "CTRL+i", "iconInfo.png");

            case "metaButton":
                return new ControlStyle("metaButton", message("MetaData"), "", "iconMeta.png");

            case "statisticButton":
                return new ControlStyle("statisticButton", message("Statistic"), "", "iconStatistic.png");

            case "setButton":
                return new ControlStyle("setButton", message("Set"), "F1 / ALT+1", "iconEqual.png");

            case "nextButton":
                return new ControlStyle("nextButton", message("Next"), "PAGE DOWN", "iconNext.png");

            case "previousButton":
                return new ControlStyle("previousButton", message("Previous"), "PAGE UP", "iconPrevious.png");

            case "firstButton":
                return new ControlStyle("firstButton", message("First"), "CTRL+HOME", "iconFirst.png");

            case "lastButton":
                return new ControlStyle("lastButton", message("Last"), "CTRL+END", "iconLast.png");

            case "goButton":
                return new ControlStyle("goButton", message("Go"), "", "iconGo.png");

            case "viewButton":
                return new ControlStyle("viewButton", message("View"), "", "iconView.png");

            case "popButton":
                return new ControlStyle("popButton", message("Pop"), "CTRL+p", "iconPop.png");

            case "refButton":
                return new ControlStyle("refButton", message("Ref"), message("UseCurrentImageAsReference"), "CTRL+f", "iconRef.png");

            case "ref2Button":
                return new ControlStyle("refButton2", message("Ref"), message("UseCurrentImageAsReference"), "CTRL+f", "iconRef.png");

            case "redoButton":
                return new ControlStyle("redoButton", message("Redo"), "CTRL+y / ALT+y", "iconRedo.png");

            case "undoButton":
                return new ControlStyle("undoButton", message("Undo"), "CTRL+z / ALT+z", "iconUndo.png");

            case "moreButton":
                return new ControlStyle("moreButton", "", message("MoreControls"), "F12 / CTRL+m / ALT+m", "iconMore.png");

            case "imageSizeButton":
                return new ControlStyle("imageSizeButton", message("LoadedSize"), "CTRL+1", "iconPicSmall.png");

            case "paneSizeButton":
                return new ControlStyle("paneSizeButton", message("PaneSize"), "CTRL+2", "iconPicBig.png");

            case "zoomInButton":
                return new ControlStyle("zoomInButton", message("ZoomIn"), "CTRL+3", "iconZoomIn.png");

            case "zoomOutButton":
                return new ControlStyle("zoomOutButton", message("ZoomOut"), "CTRL+4", "iconZoomOut.png");

            case "rotateLeftButton":
                return new ControlStyle("rotateLeftButton", message("RotateLeft"), "", "iconRotateLeft.png");

            case "rotateRightButton":
                return new ControlStyle("rotateRightButton", message("RotateRight"), "", "iconRotateRight.png");

            case "turnOverButton":
                return new ControlStyle("turnOverButton", message("TurnOver"), "", "iconTurnOver.png");

            case "moveLeftButton":
                return new ControlStyle("moveLeftButton", message("MoveLeft"), "", "iconLeft.png");

            case "moveRightButton":
                return new ControlStyle("moveRightButton", message("MoveRight"), "", "iconRight.png");

            case "upFilesButton":
                return new ControlStyle("upFilesButton", message("MoveUp"), "", "iconUp.png");

            case "downFilesButton":
                return new ControlStyle("downFilesButton", message("MoveDown"), "", "iconDown.png");

            case "previewButton":
                return new ControlStyle("previewButton", message("Preview"), message("PreviewComments"), "", "iconPreview.png");

            case "startButton":
                return new ControlStyle("startButton", "", "F1 / CTRL+e / ALT+e", "");

            case "closeButton":
                return new ControlStyle("closeButton", message("Close"), "F4", "iconClose.png");

            case "findNextButton":
                return new ControlStyle("findNextButton", message("Next"), "CTRL+n", "iconNext.png");

            case "findPreviousButton":
                return new ControlStyle("findPreviousButton", message("Previous"), "CTRL+p", "iconPrevious.png");

            case "findLastButton":
                return new ControlStyle("findLastButton", message("Last"), "CTRL+l", "iconLast.png");

            case "findFirstButton":
                return new ControlStyle("findFirstButton", message("First"), "CTRL+f", "iconFirst.png");

            case "replaceButton":
                return new ControlStyle("replaceButton", message("Replace"), "CTRL+e", "iconReplace.png");

            case "replaceAllButton":
                return new ControlStyle("replaceAllButton", message("ReplaceAll"), "", "iconReplaceAll.png");

            case "clearButton":
                return new ControlStyle("clearButton", message("Clear"), "", "iconClear.png");

            case "clearFilesButton":
                return new ControlStyle("clearFilesButton", message("Clear"), "", "iconClear.png");

            case "withdrawButton":
                return new ControlStyle("withdrawButton", message("Withdraw"), "", "iconWithdraw.png");

            case "polygonWithdrawButton":
                return new ControlStyle("polygonWithdrawButton", message("Withdraw"), "", "iconWithdraw.png");

            case "polygonClearButton":
                return new ControlStyle("polygonClearButton", message("Delete"), "", "iconClear.png");

            case "addFilesButton":
                return new ControlStyle("addFilesButton", message("AddFiles"), "", "iconAddFile.png");

            case "insertFilesButton":
                return new ControlStyle("insertFilesButton", message("InsertFiles"), "", "iconInsertFile.png");

            case "selectSourcePathButton":
                return new ControlStyle("selectSourcePathButton", message("Select"), "", "iconFolder.png");

            case "selectTargetPathButton":
                return new ControlStyle("selectTargetPathButton", message("Select"), "", "iconFolder.png");

            case "selectTargetFileButton":
                return new ControlStyle("selectTargetFileButton", message("Select"), "", "iconOpen.png");

            case "addDirectoryButton":
                return new ControlStyle("addDirectoryButton", message("AddDirectory"), "", "iconAddFolder.png");

            case "insertDirectoryButton":
                return new ControlStyle("insertDirectoryButton", message("InsertDirectory"), "", "iconInsertFolder.png");

            case "deleteFileButton":
                return new ControlStyle("deleteFileButton", message("Delete"), "", "iconDeleteFile.png");

            case "deleteFilesButton":
                return new ControlStyle("deleteFilesButton", message("Delete"), "", "iconDeleteFile.png");

            case "selectAllFilesButton":
                return new ControlStyle("selectAllFilesButton", message("SelectAll"), "CTRL+a", "iconSelectAll.png");

            case "unselectAllFilesButton":
                return new ControlStyle("unselectAllFilesButton", message("UnselectAll"), "", "iconSelectNone.png");

            case "openTargetButton":
                return new ControlStyle("openTargetButton", message("Open"), "", "iconOpen.png");

            case "viewTargetFileButton":
                return new ControlStyle("viewTargetFileButton", message("View"), "", "iconView.png");

            case "viewFileButton":
                return new ControlStyle("viewFileButton", message("View"), "", "iconView.png");

            case "browseButton":
                return new ControlStyle("browseButton", message("Browse"), "", "iconBrowse.png");

            case "selectRefButton":
                return new ControlStyle("selectRefButton", message("Select"), "", "iconOpen.png");

            case "sytemClipboardButton":
                return new ControlStyle("sytemClipboardButton", "", message("LoadImageInSystemClipboard"), "", "iconPicSmall.png");

            case "mirrorHButton":
                return new ControlStyle("mirrorHButton", message("MirrorHorizontal"), "", "iconHorizontal.png");

            case "mirrorVButton":
                return new ControlStyle("mirrorVButton", message("MirrorVertical"), "", "iconVertical.png");

            case "shearButton":
                return new ControlStyle("shearButton", message("Shear"), "", "iconShear.png");

            case "saveImagesButton":
                return new ControlStyle("saveImagesButton", message("SaveAsImages"),
                        message("SaveAsImages") + "\n" + message("FilePrefixInput"), "", "");

            case "saveTiffButton":
                return new ControlStyle("saveTiffButton", message("SaveAsTiff"), "", "iconTiff.png");

            case "savePdfButton":
                return new ControlStyle("savePdfButton", message("SaveAsPDF"), "", "iconPDF.png");

            case "pdfSetButton":
                return new ControlStyle("pdfSetButton", message("Set"), "", "iconEqual.png");

            case "clearRowsButton":
                return new ControlStyle("clearRowsButton", message("Clear"), "", "iconClear.png");

            case "clearColsButton":
                return new ControlStyle("clearColsButton", message("Clear"), "", "iconClear.png");

            case "examplesButton":
                return new ControlStyle("examplesButton", message("Examples"), "", "iconExamples.png");

            case "testButton":
                return new ControlStyle("testButton", message("Test"), "", "iconRun.png");

            case "demoButton":
                return new ControlStyle("demoButton", message("Demo"), "", "iconRun.png");

            case "editButton":
                return new ControlStyle("editButton", message("Edit"), "", "iconEdit.png");

            case "openForeImageButton":
                return new ControlStyle("openForeImageButton", message("Select"), "", "iconOpen.png");

            case "viewForeImageButton":
                return new ControlStyle("viewForeImageButton", message("View"), "", "iconView.png");

            case "foreImagePaneSizeButton":
                return new ControlStyle("foreImagePaneSizeButton", message("PaneSize"), "", "iconPicBig.png");

            case "foreImageImageSizeButton":
                return new ControlStyle("foreImageImageSizeButton", message("ImageSize"), "", "iconPicSmall.png");

            case "openBackImageButton":
                return new ControlStyle("openBackImageButton", message("Select"), "", "iconOpen.png");

            case "viewBackImageButton":
                return new ControlStyle("viewBackImageButton", message("View"), "", "iconView.png");

            case "backImagePaneSizeButton":
                return new ControlStyle("backImagePaneSizeButton", message("PaneSize"), "", "iconPicBig.png");

            case "backImageImageSizeButton":
                return new ControlStyle("backImageImageSizeButton", message("ImageSize"), "", "iconPicSmall.png");

            case "newWindowButton":
                return new ControlStyle("newWindowButton", message("OpenInNewWindow"), "", "iconView.png");

            case "strightButton":
                return new ControlStyle("strightButton", message("Straighten"), "", "iconRefresh.png");

            case "activeButton":
                return new ControlStyle("activeButton", message("Active"), "", "iconActive.png");

            case "inactiveButton":
                return new ControlStyle("inactiveButton", message("Inactive"), "", "iconInactive.png");

            case "thumbsListButton":
                return new ControlStyle("thumbsListButton", message("ThumbnailsList"), "", "iconThumbsList.png");

            case "filesListButton":
                return new ControlStyle("filesListButton", message("FilesList"), "", "iconList.png");

            case "snapshotButton":
                return new ControlStyle("snapshotButton", message("Snapshot"), "", "iconSnapshot.png");

            case "splitButton":
                return new ControlStyle("splitButton", message("Split"), "", "iconSplit.png");

            case "sampleButton":
                return new ControlStyle("sampleButton", message("Sample"), "", "iconSample.png");

            case "manufactureButton":
                return new ControlStyle("manufactureButton", message("Manufacture"), "", "iconEdit.png");

            case "calculatorButton":
                return new ControlStyle("calculatorButton", message("PixelsCalculator"), "", "iconCalculator.png");

            case "filterButton":
                return new ControlStyle("filterButton", message("Filter"), "", "iconFilter.png");

            case "locateLineButton":
                return new ControlStyle("locateLineButton", message("Go"), "", "iconGo.png");

            case "locateObjectButton":
                return new ControlStyle("locateObjectButton", message("Go"), "", "iconGo.png");

            case "recoveryAllButton":
                return new ControlStyle("recoveryAllButton", message("RecoverAll"), "", "iconRecover.png");

            case "recoverySelectedButton":
                return new ControlStyle("recoverySelectedButton", message("RecoverSelected"), "", "iconRestoreFile.png");

            case "playButton":
                return new ControlStyle("playButton", message("Play"), "", "iconPlay.png");

            case "pauseButton":
                return new ControlStyle("pauseButton", message("Pause"), "", "iconPause.png");

            case "selectSoundButton":
                return new ControlStyle("selectSoundButton", message("Select"), "", "iconOpen.png");

            case "selectMusicButton":
                return new ControlStyle("selectMusicButton", message("Select"), "", "iconOpen.png");

            case "pixSelectButton":
                return new ControlStyle("pixSelectButton", message("Select"), "", "iconOpen.png");

            case "refreshButton":
                return new ControlStyle("refreshButton", message("Refresh"), "", "iconRefresh.png");

            case "loadButton":
                return new ControlStyle("loadButton", message("Load"), "", "iconGo.png");

            case "increaseButton":
                return new ControlStyle("increaseButton", message("Increase"), "", "iconPlus.png");

            case "decreaseButton":
                return new ControlStyle("decreaseButton", message("Decrease"), "", "iconMinus.png");

            case "invertButton":
                return new ControlStyle("invertButton", message("Invert"), "", "iconInvert.png");

            case "originalButton":
                return new ControlStyle("originalButton", message("OriginalSize"), "", "iconOriginalSize.png");

            case "wowButton":
                return new ControlStyle("wowButton", message("WowAsExample"), "", "iconWOW.png");

            case "suggestButton":
                return new ControlStyle("suggestButton", message("SuggestedSettings"), "", "iconIdea.png");

            case "originalImageButton":
                return new ControlStyle("originalImageButton", message("OriginalImage"), "", "iconPhoto.png");

            case "xmlButton":
                return new ControlStyle("xmlButton", "XML", "", "iconXML.png");

            case "exportButton":
                return new ControlStyle("exportButton", message("Export"), "", "iconExport.png");

            case "importButton":
                return new ControlStyle("importButton", message("Import"), "", "iconImport.png");

            case "refreshHeaderButton":
                return new ControlStyle("refreshHeaderButton", message("Refresh"), "", "iconRefresh.png");

            case "refreshXmlButton":
                return new ControlStyle("refreshXmlButton", message("Refresh"), "", "iconRefresh.png");

            case "refreshCieDataButton":
                return new ControlStyle("refreshCieDataButton", message("Refresh"), "", "iconRefresh.png");

            case "exportCieDataButton":
                return new ControlStyle("exportCieDataButton", message("Export"), "", "iconExport.png");

            case "exportDiagramButton":
                return new ControlStyle("exportDiagramButton", message("Export"), "", "iconExport.png");

            case "exportXmlButton":
                return new ControlStyle("exportXmlButton", message("Export"), "", "iconExport.png");

            case "validateButton":
                return new ControlStyle("validateButton", message("Validate"), "", "iconView.png");

            case "copyAsAButton":
                return new ControlStyle("copyAsAButton", message("CopyAsMatrixA"), "", "iconCopyA.png");

            case "copyAsBButton":
                return new ControlStyle("copyAsAButton", message("CopyAsMatrixB"), "", "iconCopyB.png");

            case "export10DegreeButton":
                return new ControlStyle("export10DegreeButton", message("Export"), "", "iconExport.png");

            case "export2DegreeButton":
                return new ControlStyle("export2DegreeButton", message("Export"), "", "iconExport.png");

            case "calculateXYZButton":
                return new ControlStyle("calculateXYZButton", message("Calculate"), "", "iconCalculator.png");

            case "calculateXYButton":
                return new ControlStyle("calculateXYButton", message("Calculate"), "", "iconCalculator.png");

            case "calculateDisplayButton":
                return new ControlStyle("calculateDisplayButton", message("Display"), "", "iconGraph.png");

            case "displayDataButton":
                return new ControlStyle("displayDataButton", message("Display"), "", "iconGraph.png");

            case "fileExportButton":
                return new ControlStyle("fileExportButton", message("Export"), "", "iconExport.png");

            case "editOkButton":
                return new ControlStyle("editOkButton", message("OK"), "", "iconOK.png");

            case "valueOkButton":
                return new ControlStyle("valueOkButton", message("OK"), "", "iconOK.png");

            case "plusButton":
                return new ControlStyle("plusButton", message("Plus"), "", "iconPlus.png");

            case "minusButton":
                return new ControlStyle("minusButton", message("Minus"), "", "iconMinus.png");

            case "multiplyButton":
                return new ControlStyle("multiplyButton", message("Multiply"), "", "iconMultiply.png");

            case "calculateButton":
                return new ControlStyle("calculateButton", message("Calculate"), "", "iconCalculator.png");

            case "matrixPlusButton":
                return new ControlStyle("matrixPlusButton", message("A + B"), "", "iconPlus.png");

            case "matrixMinusButton":
                return new ControlStyle("matrixMinusButton", message("A - B"), "", "iconMinus.png");

            case "matrixMultiplyButton":
                return new ControlStyle("matrixMultiplyButton", message("AB"), "", "iconMultiply.png");

            case "hadamardProductButton":
                return new ControlStyle("hadamardProductButton", message("HadamardProductComments"), "", "iconAsterisk.png");

            case "kroneckerProductButton":
                return new ControlStyle("kroneckerProductButton", message("KroneckerProductComments"), "", "iconCancel.png");

            case "verticalMergeButton":
                return new ControlStyle("verticalMergeButton", message("VerticalMergeComments"), "", "iconVerticalMerge.png");

            case "horizontalMergeButton":
                return new ControlStyle("horizontalMergeButton", message("HorizontalMergeComments"), "", "iconHorizontalMerge.png");

            case "calculateAllButton":
                return new ControlStyle("calculateAllButton", message("Calculate"), "", "iconCalculator.png");

            case "iccSelectButton":
                return new ControlStyle("iccSelectButton", message("Select"), "", "iconOpen.png");

            case "useButton":
                return new ControlStyle("useButton", message("Use"), "", "iconOK2.png");

            case "commonColorsButton":
                return new ControlStyle("commonColorsButton", message("CommonColors"), "", "iconColor.png");

            case "paletteButton":
                return new ControlStyle("paletteButton", message("ColorPalette"), "", "iconPalette.png");

            case "fillPaletteButton":
                return new ControlStyle("fillPaletteButton", message("ColorPalette"), "", "iconPalette.png");

            case "htmlButton":
                return new ControlStyle("htmlButton", message("DisplayHtml"), "", "iconHtml.png");

            case "recoverJVMButton":
                return new ControlStyle("recoverJVMButton", message("Recover"), "", "iconRecover.png");

            case "ocrPathButton":
                return new ControlStyle("ocrPathButton", message("Select"), "", "iconOpen.png");

            case "runButton":
                return new ControlStyle("runButton", message("Run"), "", "iconGo.png");

            case "saveOcrButton":
                return new ControlStyle("saveOcrButton", message("Save"), "", "iconSave.png");

            case "blackwhiteButton":
                return new ControlStyle("blackwhiteButton", message("BlackOrWhite"), "", "iconBlackWhite.png");

            case "greyButton":
                return new ControlStyle("greyButton", message("Greyscale"), "", "iconGreyscale.png");

            case "setEnhanceButton":
                return new ControlStyle("setEnhanceButton", message("Set"), "", "iconEqual.png");

            case "setScaleButton":
                return new ControlStyle("setScaleButton", message("Set"), "", "iconEqual.png");

            case "setBinaryButton":
                return new ControlStyle("setBinaryButton", message("Set"), "", "iconEqual.png");

            case "setRotateButton":
                return new ControlStyle("setRotateButton", message("Set"), "", "iconEqual.png");

            case "deskewButton":
                return new ControlStyle("deskewButton", "", message("Deskew"), "", "iconShear.png");

            case "moveUpButton":
                return new ControlStyle("moveUpButton", message("MoveUp"), "", "iconUp.png");

            case "moveDownButton":
                return new ControlStyle("moveDownButton", message("MoveDown"), "", "iconDown.png");

            case "zoomIn2Button":
                return new ControlStyle("zoomIn2Button", message("ZoomIn"), "", "iconZoomIn.png");

            case "zoomOut2Button":
                return new ControlStyle("zoomOut2Button", message("ZoomOut"), "", "iconZoomOut.png");

            case "imageSize2Button":
                return new ControlStyle("imageSize2Button", message("LoadedSize"), "", "iconPicSmall.png");

            case "paneSize2Button":
                return new ControlStyle("paneSize2Button", message("PaneSize"), "", "iconPicBig.png");

            default:
                return null;

        }

    }

    public static void setStyle(Node node) {
        if (node == null) {
            return;
        }
        setStyle(node, node.getId());
    }

    public static void setStyle(Node node, String id) {
        setStyle(node, id, false);
    }

    public static void setStyle(Node node, String id, boolean mustStyle) {
        if (node == null || id == null) {
            return;
        }
        ControlStyle style = getControlStyle(node);
        setTips(node, style);

        if (node.getStyleClass().contains("main-button")) {
            return;
        }
        setColorStyle(node, style, AppVariables.ControlColor);
//        if (mustStyle || AppVariables.ControlColor != ColorStyle.Default) {
//            setColorStyle(node, style, AppVariables.ControlColor);
//        }

        if (AppVariables.controlDisplayText && node instanceof Labeled) {
            setTextStyle(node, style, AppVariables.ControlColor);
        }

    }

    public static ColorStyle getConfigColorStyle() {
        return ControlStyle.getColorStyle(AppVariables.getUserConfigValue("ControlColor", "default"));
    }

    public static ContentDisplay getConfigControlContent() {
        return ControlStyle.getControlContent(AppVariables.getUserConfigValue("ControlContent", "image"));
    }

    public static boolean setConfigColorStyle(String value) {
        AppVariables.ControlColor = getColorStyle(value);
        return AppVariables.setUserConfigValue("ControlColor", value);
    }

    public static ColorStyle getColorStyle(String color) {
        if (color == null) {
            return ColorStyle.Default;
        }
        switch (color.toLowerCase()) {
            case "red":
                return ColorStyle.Red;
            case "blue":
                return ColorStyle.Blue;
            case "lightblue":
                return ColorStyle.LightBlue;
            case "pink":
                return ColorStyle.Pink;
            case "orange":
                return ColorStyle.Orange;
            default:
                return ColorStyle.Default;
        }
    }

    public static ContentDisplay getControlContent(String value) {
        if (value == null) {
            return ContentDisplay.GRAPHIC_ONLY;
        }
        switch (value.toLowerCase()) {
            case "graphic":
                return ContentDisplay.GRAPHIC_ONLY;
            case "text":
                return ContentDisplay.TEXT_ONLY;
            case "top":
                return ContentDisplay.TOP;
            case "left":
                return ContentDisplay.LEFT;
            case "right":
                return ContentDisplay.RIGHT;
            case "bottom":
                return ContentDisplay.BOTTOM;
            case "center":
                return ContentDisplay.CENTER;
            default:
                return ContentDisplay.GRAPHIC_ONLY;

        }
    }

    public static String getTips(ControlStyle style) {
        if (style == null) {
            return null;
        }
        String tips = null;
        String name = style.getName();
        String comments = style.getComments();
        String shortcut = style.getShortcut();
        if (comments != null && !comments.isEmpty()) {
            tips = comments;
            if (shortcut != null && !shortcut.isEmpty()) {
                tips += "\n" + shortcut;
            }
        } else if (name != null && !name.isEmpty()) {
            tips = name;
            if (shortcut != null && !shortcut.isEmpty()) {
                tips += "\n" + shortcut;
            }
        } else if (shortcut != null && !shortcut.isEmpty()) {
            tips = shortcut;
        }
        return tips;
    }

    public static void setTips(Node node) {
        String id = node.getId();
        if (id == null) {
            return;
        }
        ControlStyle style = getControlStyle(node);
        String tips = getTips(style);
        if (tips == null || tips.isEmpty()) {
            return;
        }
        FxmlControl.setTooltip(node, new Tooltip(tips));
    }

    public static void setTips(Node node, ControlStyle style) {
        if (node == null) {
            return;
        }
        String tips = getTips(style);
        if (tips == null || tips.isEmpty()) {
            return;
        }
        FxmlControl.setTooltip(node, new Tooltip(tips));
    }

    public static void setTextStyle(Node node, ControlStyle controlStyle,
            ColorStyle colorStyle) {
        try {
            if (node == null
                    || controlStyle == null
                    || !(node instanceof Labeled)) {
                return;
            }
            Labeled label = ((Labeled) node);
//            switch (colorStyle) {
//                case Red:
//                    label.setTextFill(Color.RED);
//                    break;
//                case Pink:
//                    label.setTextFill(Color.PINK);
//                    break;
//                case Blue:
//                    label.setTextFill(Color.BLUE);
//                    break;
//                case Orange:
//                    label.setTextFill(Color.ORANGE);
//                    break;
//                default:
//                    label.setTextFill(Color.BLUE);
//                    break;
//            }

            String name = controlStyle.getName();
            if (name != null && !name.isEmpty()) {
                label.setText(name);
            } else {
                label.setText(controlStyle.getComments());
            }

        } catch (Exception e) {
            logger.debug(node.getId() + " " + e.toString());

        }
    }

    public static void setColorStyle(Node node) {
        setColorStyle(node, AppVariables.ControlColor);
    }

    public static void setColorStyle(Node node, ColorStyle color) {
        String id = node.getId();
        if (id == null) {
            return;
        }
        ControlStyle controlStyle = getControlStyle(node);
        setIcon(node, getIcon(controlStyle, color));

    }

    public static void setColorStyle(Node node, ControlStyle style) {
        setColorStyle(node, style, AppVariables.ControlColor);
    }

    public static void setColorStyle(Node node, ControlStyle style, ColorStyle color) {
        setIcon(node, getIcon(style, color));
    }

    public static void setIcon(Node node, String icon) {
        try {
            if (node == null || icon == null || icon.isEmpty()) {
                return;
            }
            ImageView v = new ImageView(icon);
            v.setFitWidth(AppVariables.iconSize);
            v.setFitHeight(AppVariables.iconSize);

            if (node instanceof Labeled) {
                ((Labeled) node).setGraphic(v);

            } else if (node instanceof ImageView) {
                ((ImageView) node).setImage(v.getImage());

            }

        } catch (Exception e) {
            logger.debug(node.getId() + " " + e.toString());

        }
    }

    public static String getIcon(ControlStyle style, ColorStyle color) {
        try {
            if (style == null || style.getIconName() == null
                    || style.getIconName().isEmpty()) {
                return null;
            }
            String path = getIconPath(color);
            return path + style.getIconName();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getIcon(String name) {
        String path = getIconPath();
        return path + name;
    }

    public static String getIconPath() {
        return getIconPath(AppVariables.ControlColor);
    }

    public static String getIconPath(ColorStyle color) {
        try {
            if (color == null) {
                color = ColorStyle.Default;
            }
            String path;
            switch (color) {
                case Red:
                    path = RedButtonPath;
                    break;
                case Blue:
                    path = BlueButtonPath;
                    break;
                case LightBlue:
                    path = LightBlueButtonPath;
                    break;
                case Pink:
                    path = PinkButtonPath;
                    break;
                case Orange:
                    path = OrangeButtonPath;
                    break;
                default:
                    path = DefaultButtonPath;
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
