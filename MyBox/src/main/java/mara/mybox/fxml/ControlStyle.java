package mara.mybox.fxml;

import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
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
        ControlStyle style = null;
        if (id.startsWith("his")) {
            style = getHisControlStyle(id);
        } else if (id.startsWith("settings")) {
            style = getSettingsControlStyle(id);
        } else if (id.startsWith("scope")) {
            style = getScopeControlStyle(id);
        } else if (id.startsWith("color")) {
            style = getColorControlStyle(id);
        } else if (node instanceof ImageView) {
            style = getImageViewStyle(id);
        } else if (node instanceof RadioButton) {
            style = getRadioButtonStyle(id);
        } else if (node instanceof CheckBox) {
            style = getCheckBoxStyle(id);
        } else if (node instanceof ToggleButton) {
            style = getToggleButtonStyle(id);
        } else if (node instanceof Button) {
            style = getButtonControlStyle(id);
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
                return new ControlStyle("settingsOpenButton", message("OpenDataPath"), "", "iconOpen.png");

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
                if (id.endsWith("Button")) {
                    return getButtonControlStyle(id);
                } else {
                    return null;
                }
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
                if (id.endsWith("Button")) {
                    return getButtonControlStyle(id);
                } else {
                    return null;
                }
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

            case "colorReplaceRadio":
                return new ControlStyle("colorReplaceRadio", "", message("ReplaceColor"), "", "iconReplace.png");

            default:
                if (id.endsWith("Button")) {
                    return getButtonControlStyle(id);
                } else {
                    return null;
                }
        }

    }

    public static ControlStyle getScopeControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            case "scopeButton":
                return new ControlStyle(id, "", message("Scope"), "F7", "iconTarget.png");

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

            case "scopeCreateButton":
                return new ControlStyle("scopeCreateButton", message("Create"), "", "iconEdit.png");

            case "scopeDeletePointButton":
                return new ControlStyle("scopeDeletePointButton", message("Delete"), "", "iconDelete.png");

            case "scopeClearPointsButton":
                return new ControlStyle("scopeClearPointsButton", message("Clear"), "", "iconClear.png");

            case "scopeDeleteColorButton":
                return new ControlStyle("scopeDeleteColorButton", message("Delete"), "", "iconDelete.png");

            case "scopeClearColorsButton":
                return new ControlStyle("scopeClearColorsButton", message("Clear"), "", "iconClear.png");

            case "scopeOutlineFileButton":
                return new ControlStyle("scopeOutlineFileButton", message("Open"), "", "iconOpen.png");

            case "scopeOutlineKeepRatioCheck":
                return new ControlStyle("scopeOutlineKeepRatioCheck", message("KeepRatio"), "", "iconAspectRatio.png");

            default:
                if (id.endsWith("Button")) {
                    return getButtonControlStyle(id);
                } else {
                    return null;
                }
        }

    }

    public static ControlStyle getRadioButtonStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "miaoRadio":
                return new ControlStyle(id, message("Meow"), message("MiaoPrompt"), "", "iconCat.png");

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

            case "horizontalBarsChartRadio":
                return new ControlStyle(id, "", message("HorizontalBarsChart"), "", "iconBarsChartH.png");

            case "verticalBarsChartRadio":
                return new ControlStyle(id, "", message("VerticalBarsChart"), "", "iconBarsChart.png");

            case "linesChartRadio":
                return new ControlStyle(id, "", message("VerticalLinesChart"), "", "iconLinesChart.png");

            case "linesChartHRadio":
                return new ControlStyle(id, "", message("HorizontalLinesChart"), "", "iconLinesChartH.png");

            case "pieRadio":
                return new ControlStyle(id, "", message("PieChart"), "", "iconPieChart.png");

            case "mapRadio":
                return new ControlStyle(id, "", message("Map"), "", "iconMap.png");

            case "pcxSelect":
                return new ControlStyle("pcxSelect", "pcx", message("PcxComments"), "", "");

            default:
                return null;
        }

    }

    public static ControlStyle getCheckBoxStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        if (id.startsWith("coordinate")) {
            return new ControlStyle(id, "", message("Coordinate"), "", "iconLocation.png");
        }

        switch (id) {

            case "tableSubdirCheck":
                return new ControlStyle("tableSubdirCheck", "", message("HandleSubDirectories"), "", "iconFolder.png");

            case "tableExpandDirCheck":
                return new ControlStyle("tableExpandDirCheck", "", message("ExpandDirectories"), "", "iconUnfold.png");

            case "tableCreateDirCheck":
                return new ControlStyle("tableCreateDirCheck", "", message("CreateDirectories"), "", "iconFolderLink.png");

            case "countDirCheck":
                return new ControlStyle("countDirCheck", "", message("CountFilesUnderFolders"), "", "iconFolderBrowse.png");

            case "tableThumbCheck":
                return new ControlStyle("tableThumbCheck", "", message("Thumbnail"), "", "iconThumbsList.png");

            case "miaoCheck":
                return new ControlStyle("miaoCheck", message("Meow"), message("MiaoPrompt"), "", "iconCat.png");

            case "pdfMemBox":
                return new ControlStyle("pdfMemBox", "", message("PdfMemComments"), "", "");

            case "openCheck":
                return new ControlStyle("openCheck", "", message("OpenWhenComplete"), "", "iconOpen2.png");

            case "selectAreaCheck":
                return new ControlStyle("selectAreaCheck", "", message("SelectArea"), "CTRL+t / ALT+t", "iconTarget.png");

            case "openSaveCheck":
                return new ControlStyle("openSaveCheck", "", message("OpenAfterSave"), "", "iconOpen2.png");

//            case "deleteConfirmCheck":
//                return new ControlStyle("deleteConfirmCheck", "", message("ConfirmWhenDelete"), "", "iconConfirm.png");
            case "bookmarksCheck":
                return new ControlStyle("bookmarksCheck", "", message("Bookmarks"), "", "iconTree.png");

            case "thumbCheck":
                return new ControlStyle("thumbCheck", "", message("Thumbnails"), "", "iconBrowse.png");

            case "rulerXCheck":
                return new ControlStyle("rulerXCheck", "", message("RulerX"), "", "iconXRuler.png");

            case "rulerYCheck":
                return new ControlStyle("rulerYCheck", "", message("RulerY"), "", "iconYRuler.png");

            case "statisticCheck":
                return new ControlStyle("statisticCheck", "", message("Statistic"), "", "iconStatistic.png");

            case "transparentBackgroundCheck":
                return new ControlStyle("transparentBackgroundCheck", "", message("TransparentBackground"), "", "iconOpacity.png");

            case "transparentCheck":
                return new ControlStyle(id, "", message("CountTransparent"), "", "iconOpacity.png");

            case "displaySizeCheck":
                return new ControlStyle("displaySizeCheck", "", message("DisplaySize"), "", "iconIdea.png");

            case "topCheck":
                return new ControlStyle("topCheck", "", message("AlwayOnTop"), "", "iconTop.png");

            case "saveCloseCheck":
                return new ControlStyle("saveCloseCheck", "", message("CloseAfterHandled"), "", "iconFlower.png");

            case "deskewCheck":
                return new ControlStyle("deskewCheck", "", message("Deskew"), "", "iconShear.png");

            case "invertCheck":
                return new ControlStyle("invertCheck", "", message("Invert"), "", "iconInvert.png");

            case "popCheck":
                return new ControlStyle(id, message("Pop"), "", "iconPop.png");

            case "pickColorCheck":
                return new ControlStyle(id, message("PickColor"), message("ColorPickerComments"), "CTRL+k / ALT+k", "iconPickColor.png");

            case "ditherCheck":
                return new ControlStyle(id, message("DitherComments"), "", "");

            case "withNamesCheck":
            case "sourceWithNamesCheck":
            case "targetWithNamesCheck":
                return new ControlStyle(id, "", message("FirstLineAsNamesComments"), "", "");

            default:
                return null;
        }

    }

    public static ControlStyle getImageViewStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("rightTips")) {
            return new ControlStyle(id, "", "", "iconTipsRight.png");
        }

        if (id.startsWith("sample")) {
            return new ControlStyle(id, "", "", "iconSampled.png");
        }

        if (id.startsWith("leftPane")) {
            return new ControlStyle(id, "", "", "iconDoubleLeft.png");
        }

        if (id.startsWith("rightPane")) {
            return new ControlStyle(id, "", "", "iconDoubleRight.png");
        }

        if (id.startsWith("links")) {
            return new ControlStyle(id, "", message("Links"), "iconLink.png");
        }

        if (id.toLowerCase().endsWith("tipsview")) {
            switch (id) {
                case "refTipsView":
                    return new ControlStyle(id, "", message("ImageRefTips"), "", "iconTips.png");

                case "distanceTipsView":
                    return new ControlStyle(id, "", message("ColorMatchComments"), "", "iconTips.png");

                case "BWThresholdTipsView":
                    return new ControlStyle(id, "", message("BWThresholdComments"), "", "iconTips.png");

                case "pdfMemTipsView":
                    return new ControlStyle(id, "", message("PdfMemComments"), "", "iconTips.png");

                case "pdfPageSizeTipsView":
                    return new ControlStyle(id, "", message("PdfPageSizeComments"), "", "iconTips.png");

                case "preAlphaTipsView":
                    return new ControlStyle(id, "", message("PremultipliedAlphaTips"), "", "iconTips.png");

                case "thresholdingTipsView":
                    return new ControlStyle(id, "", message("ThresholdingComments"), "", "iconTips.png");

                case "quantizationTipsView":
                    return new ControlStyle(id, "", message("QuantizationComments"), "", "iconTips.png");

                default:
                    return new ControlStyle(id, "", "", "iconTips.png");
            }
        }
        return null;

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

            case "fullScreenButton":
                return new ControlStyle(id, message("FullScreen"), "", "iconExpand.png");

            case "soundButton":
                return new ControlStyle(id, message("Mute"), "", "iconMute.png");

            default:
                return null;
        }

    }

    public static ControlStyle getButtonControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("ok")) {
            switch (id) {
                case "okButton":
                    return new ControlStyle("okButton", message("OK"), "F1 / CTRL+e / ALT+e", "iconOK.png");

                default:
                    return new ControlStyle(id, message("OK"), "", "iconOK.png");
            }
        }
        if (id.startsWith("start")) {
            switch (id) {
                case "startButton":
                    return new ControlStyle(id, message("Start"), "F1 / CTRL+e / ALT+e", "iconStart.png");

                default:
                    return new ControlStyle(id, message("Start"), "", "iconStart.png");
            }
        }

        if (id.startsWith("saveAs")) {
            switch (id) {
                case "saveAsButton":
                    return new ControlStyle(id, message("SaveAs"), "F11", "iconSaveAs.png");

                default:
                    return new ControlStyle(id, message("SaveAs"), "", "iconSaveAs.png");
            }
        }
        if (id.startsWith("save")) {
            switch (id) {
                case "saveButton":
                    return new ControlStyle(id, message("Save"), "F2 / CTRL+s / ALT+s", "iconSave.png");

                case "saveImagesButton":
                    return new ControlStyle(id, message("SaveAsImages"),
                            message("SaveAsImages") + "\n" + message("FilePrefixInput"), "", "");

                case "saveTiffButton":
                    return new ControlStyle(id, message("SaveAsTiff"), "", "iconTIF.png");

                case "savePdfButton":
                    return new ControlStyle(id, message("SaveAsPDF"), "", "iconPDF.png");

                default:
                    return new ControlStyle(id, message("Save"), "", "iconSave.png");
            }
        }

        if (id.startsWith("create")) {
            switch (id) {
                case "createButton":
                    return new ControlStyle(id, message("Create"), "CTRL+n", "iconAdd.png");
                default:
                    return new ControlStyle(id, message("Create"), "", "iconAdd.png");
            }

        }
        if (id.startsWith("add")) {
            switch (id) {
                case "addButton":
                    return new ControlStyle(id, message("Add"), "CTRL+n", "iconAdd.png");
                case "addFilesButton":
                    return new ControlStyle(id, message("AddFiles"), "", "iconFileAdd.png");
                case "addDirectoryButton":
                    return new ControlStyle(id, message("AddDirectory"), "", "iconFolderAdd.png");
                case "rowAddButton":
                    return new ControlStyle(id, message("Add"), "", "iconRowAdd.png");
                case "addImageButton":
                    return new ControlStyle(id, message("Image"), "", "iconPicSmall.png");
                case "addTableButton":
                    return new ControlStyle(id, message("Table"), "", "iconSplit.png");
                default:
                    return new ControlStyle(id, message("Add"), "", "iconAdd.png");
            }
        }

        if (id.startsWith("clear")) {
            switch (id) {
                case "clearButton":
                    return new ControlStyle(id, message("Clear"), "CTRL+g", "iconClear.png");
                default:
                    return new ControlStyle(id, message("Clear"), "", "iconClear.png");
            }
        }

        if (id.startsWith("plus")) {
            return new ControlStyle(id, message("Add"), "", "iconPlus.png");
        }
        if (id.startsWith("query")) {
            return new ControlStyle(id, message("Query"), "", "iconQuery.png");
        }
        if (id.startsWith("reset")) {
            return new ControlStyle(id, message("Reset"), "", "iconRecover.png");
        }
        if (id.startsWith("analyse")) {
            return new ControlStyle(id, message("Analyse"), "", "iconAnalyse.png");
        }
        if (id.startsWith("ocr")) {
            return new ControlStyle(id, message("OCR"), "", "iconAnalyse.png");
        }
        if (id.startsWith("yes")) {
            return new ControlStyle(id, message("Yes"), "", "iconYes.png");
        }
        if (id.startsWith("confirm")) {
            return new ControlStyle(id, message("Confirm"), "", "iconYes.png");
        }
        if (id.startsWith("complete")) {
            return new ControlStyle(id, message("Complete"), "", "iconYes.png");
        }
        if (id.startsWith("selectFile")) {
            return new ControlStyle(id, message("Select"), "", "iconOpen.png");
        }
        if (id.startsWith("selectPath")) {
            return new ControlStyle(id, message("Select"), "", "iconFolder.png");
        }

        if (id.startsWith("mybox")) {
            return new ControlStyle(id, "MyBox", "", "iconMyBox.png");
        }

        if (id.startsWith("download")) {
            return new ControlStyle(id, message("Download"), "", "iconDownload.png");
        }
        if (id.startsWith("default")) {
            return new ControlStyle(id, message("Default"), "", "iconDefault.png");
        }
        if (id.startsWith("random")) {
            return new ControlStyle(id, message("Random"), "", "iconRandom.png");
        }
        if (id.startsWith("example")) {
            return new ControlStyle(id, message("Example"), "", "iconExamples.png");
        }
        if (id.startsWith("sql")) {
            return new ControlStyle(id, "SQL", "", "iconSQL.png");
        }
        if (id.startsWith("charts")) {
            return new ControlStyle(id, message("Charts"), "", "iconCharts.png");
        }
        if (id.startsWith("recover")) {
            switch (id) {
                case "recoverButton":
                    return new ControlStyle("recoverButton", message("Recover"), "F3 / CTRL+r / ALT+r", "iconRecover.png");

                case "recoveryAllButton":
                    return new ControlStyle("recoveryAllButton", message("RecoverAll"), "", "iconRecover.png");

                case "recoverySelectedButton":
                    return new ControlStyle("recoverySelectedButton", message("RecoverSelected"), "", "iconFileRestore.png");

                default:
                    return new ControlStyle(id, message("Recover"), "", "iconRecover.png");
            }
        }
        if (id.startsWith("zoomIn")) {
            switch (id) {
                case "zoomInButton":
                    return new ControlStyle(id, message("ZoomIn"), "CTRL+3", "iconZoomIn.png");

                default:
                    return new ControlStyle(id, message("ZoomIn"), "", "iconZoomIn.png");
            }
        }

        if (id.startsWith("zoomOut")) {
            switch (id) {
                case "zoomOutButton":
                    return new ControlStyle(id, message("ZoomOut"), "CTRL+4", "iconZoomOut.png");

                default:
                    return new ControlStyle(id, message("ZoomOut"), "", "iconZoomOut.png");
            }
        }

        if (id.startsWith("copy")) {
            switch (id) {
                case "copyButton":
                    return new ControlStyle(id, message("Copy"), "CTRL+c / ALT+c ", "iconCopy.png");

                case "copyAsAButton":
                    return new ControlStyle("copyAsAButton", message("CopyAsMatrixA"), "", "iconCopyA.png");

                case "copyAsBButton":
                    return new ControlStyle("copyAsAButton", message("CopyAsMatrixB"), "", "iconCopyB.png");

                case "copyEnglishButton":
                    return new ControlStyle(id, message("CopyEnglish"), "CTRL+e / ALT+e ", "iconCopy.png");

                default:
                    return new ControlStyle(id, message("Copy"), "", "iconCopy.png");
            }
        }

        if (id.startsWith("paste")) {
            switch (id) {
                case "pasteButton":
                    return new ControlStyle(id, message("Paste"), "CTRL+v / ALT+v", "iconPaste.png");

                case "pasteTxtButton":
                    return new ControlStyle(id, message("PasteTexts"), "", "iconPaste.png");

                default:
                    return new ControlStyle(id, message("Paste"), "", "iconPaste.png");
            }
        }

        if (id.startsWith("next")) {
            switch (id) {
                case "nextButton":
                    return new ControlStyle(id, message("Next"), "PAGE DOWN", "iconNext.png");

                default:
                    return new ControlStyle(id, message("Next"), "", "iconNext.png");
            }
        }

        if (id.startsWith("previous")) {
            switch (id) {
                case "previousButton":
                    return new ControlStyle(id, message("Previous"), "PAGE UP", "iconPrevious.png");

                default:
                    return new ControlStyle(id, message("Previous"), "", "iconPrevious.png");
            }
        }

        if (id.startsWith("backward")) {
            return new ControlStyle(id, message("Backward"), "", "iconPrevious.png");
        }

        if (id.startsWith("forward")) {
            return new ControlStyle(id, message("Forward"), "", "iconNext.png");
        }

        if (id.startsWith("palette")) {
            switch (id) {
                case "paletteManageButton":
                    return new ControlStyle(id, message("ColorPaletteManage"), "", "iconPalette.png");

                default:
                    return new ControlStyle(id, message("ColorPalette"), "", "iconPalette.png");
            }
        }

        if (id.startsWith("color")) {
            return new ControlStyle(id, message("ColorPalette"), "", "iconColor.png");
        }

        if (id.startsWith("openFolder")) {
            return new ControlStyle(id, message("Open"), "", "iconOpen.png");
        }

        if (id.startsWith("delete")) {
            switch (id) {
                case "deleteButton":
                    return new ControlStyle("deleteButton", message("Delete"), "DELETE / CTRL+d / ALT+d", "iconDelete.png");

                default:
                    return new ControlStyle(id, message("Delete"), "", "iconDelete.png");
            }
        }
        if (id.startsWith("input")) {
            return new ControlStyle(id, message("Input"), "", "iconData.png");
        }

        if (id.startsWith("dataImport") || id.startsWith("import")) {
            return new ControlStyle(id, message("Import"), "", "iconImport.png");
        }

        if (id.startsWith("dataExport") || id.startsWith("export")) {
            return new ControlStyle(id, message("Export"), "", "iconExport.png");
        }
        if (id.startsWith("data")) {
            return new ControlStyle(id, message("Data"), "", "iconData.png");
        }
        if (id.startsWith("query")) {
            return new ControlStyle(id, message("Query"), "", "iconData.png");
        }
        if (id.startsWith("map")) {
            return new ControlStyle(id, message("Map"), "", "iconMap.png");
        }
        if (id.startsWith("dataset")) {
            return new ControlStyle(id, message("DataSet"), "", "iconDataset.png");
        }

        if (id.startsWith("sureButton")) {
            return new ControlStyle(id, message("Sure"), "", "iconYes.png");
        }

        if (id.startsWith("fill")) {
            return new ControlStyle(id, message("Fill"), "", "iconButterfly.png");
        }

        if (id.startsWith("cat")) {
            return new ControlStyle(id, message("Meow"), "", "iconCat.png");
        }

        if (id.startsWith("edit")) {
            switch (id) {
                case "editOkButton":
                    return new ControlStyle(id, message("OK"), "", "iconOK.png");

                default:
                    return new ControlStyle(id, message("Edit"), "", "iconEdit.png");
            }
        }

        if (id.startsWith("size")) {
            return new ControlStyle(id, message("Size"), "", "iconSplit.png");
        }

        if (id.startsWith("selectImage")) {
            return new ControlStyle(id, message("Image"), "", "iconFolderImage.png");
        }

        if (id.startsWith("equal")) {
            return new ControlStyle(id, message("EqualTo"), "", "iconEqual.png");
        }

        if (id.startsWith("selectAll")) {
            switch (id) {
                case "selectAllButton":
                    return new ControlStyle(id, message("SelectAll"), "CTRL+a / ALT+a", "iconSelectAll.png");

                default:
                    return new ControlStyle(id, message("SelectAll"), "", "iconSelectAll.png");
            }

        }

        if (id.startsWith("selectNone")) {
            switch (id) {
                case "selectNoneButton":
                    return new ControlStyle(id, message("UnselectAll"), "CTRL+o / ALT+O", "iconSelectNone.png");

                default:
                    return new ControlStyle(id, message("UnselectAll"), "", "iconSelectNone.png");
            }
        }

        if (id.startsWith("use")) {
            return new ControlStyle(id, message("Use"), "", "iconYes.png");
        }
        if (id.startsWith("refresh")) {
            return new ControlStyle(id, message("Refresh"), "", "iconRefresh.png");
        }

        if (id.startsWith("giveUp")) {
            return new ControlStyle(id, message("GiveUp"), "", "iconCatFoot.png");
        }
        if (id.startsWith("manufacture")) {
            return new ControlStyle(id, message("Manufacture"), "", "iconEdit.png");
        }
        if (id.startsWith("run")) {
            return new ControlStyle(id, message("Run"), "", "iconRun.png");
        }
        if (id.startsWith("info")) {
            switch (id) {
                case "infoButton":
                    return new ControlStyle(id, message("Information"), "CTRL+i", "iconInfo.png");

                default:
                    return new ControlStyle(id, message("Information"), "", "iconInfo.png");
            }
        }
        if (id.startsWith("view")) {
            return new ControlStyle(id, message("View"), "", "iconView.png");
        }

        if (id.startsWith("html")) {
            return new ControlStyle(id, message("Html"), "", "iconHtml.png");
        }
        if (id.startsWith("link")) {
            return new ControlStyle(id, message("Link"), "", "iconLink.png");
        }

        if (id.startsWith("stop")) {
            return new ControlStyle(id, message("Stop"), "", "iconStop.png");
        }

        if (id.startsWith("synchronize")) {
            return new ControlStyle(id, message("Synchronize"), "", "iconSynchronize.png");
        }

        if (id.startsWith("function")) {
            return new ControlStyle(id, "", "", "iconFunction.png");
        }

        if (id.startsWith("style")) {
            return new ControlStyle(id, message("Style"), "", "iconStyle.png");
        }

        if (id.startsWith("panesMenu")) {
            return new ControlStyle(id, message("Panes"), "", "iconPanes.png");
        }

        if (id.startsWith("extract")) {
            return new ControlStyle(id, message("Extract"), "", "iconExtract.png");
        }

        if (id.startsWith("demo")) {
            return new ControlStyle(id, message("Demo"), "", "iconDemo.png");
        }

        if (id.startsWith("count")) {
            return new ControlStyle(id, message("Count"), "", "iconCalculator.png");
        }

        if (id.startsWith("delimiter")) {
            return new ControlStyle(id, message("Delimiter"), "", "iconDelimiter.png");
        }

        if (id.startsWith("matrixA")) {
            return new ControlStyle(id, message("SetAsMatrixA"), "", "iconA.png");
        }

        if (id.startsWith("matrixB")) {
            return new ControlStyle(id, message("SetAsMatrixB"), "", "iconB.png");
        }

        if (id.startsWith("width")) {
            return new ControlStyle(id, message("Width"), "", "iconXRuler.png");
        }
        if (id.startsWith("go")) {
            return new ControlStyle(id, message("Go"), "", "iconGo.png");
        }

        if (id.startsWith("preview")) {
            return new ControlStyle(id, message("PreviewComments"), "", "iconPreview.png");
        }

        if (id.startsWith("rotateLeft")) {
            return new ControlStyle(id, message("RotateLeft"), "", "iconRotateLeft.png");
        }

        if (id.startsWith("rotateRight")) {
            return new ControlStyle(id, message("RotateRight"), "", "iconRotateRight.png");
        }

        if (id.startsWith("turnOver")) {
            return new ControlStyle(id, message("TurnOver"), "", "iconTurnOver.png");
        }

        if (id.startsWith("rename")) {
            return new ControlStyle(id, message("Rename"), "", "iconRename.png");
        }

        if (id.startsWith("header")) {
            return new ControlStyle(id, "", "", "iconHeader.png");
        }

        if (id.startsWith("list")) {
            return new ControlStyle(id, message("List"), "", "iconList.png");
        }

        if (id.startsWith("codes")) {
            return new ControlStyle(id, "", "", "iconMeta.png");
        }

        if (id.startsWith("fold")) {
            return new ControlStyle(id, message("Fold"), "", "iconMinus.png");
        }

        if (id.startsWith("unford")) {
            return new ControlStyle(id, message("Unfold"), "", "iconTree.png");
        }

        if (id.startsWith("moveData")) {
            return new ControlStyle(id, message("Move"), "", "iconRef.png");
        }

        if (id.startsWith("csv")) {
            return new ControlStyle(id, "CSV", "", "iconCSV.png");
        }

        if (id.startsWith("excel")) {
            return new ControlStyle(id, "Excel", "", "iconExcel.png");
        }

        if (id.startsWith("history")) {
            return new ControlStyle(id, message("History"), "", "iconHistory.png");
        }

        if (id.startsWith("ssl")) {
            return new ControlStyle(id, "SSL", "", "iconSSL.png");
        }

        if (id.startsWith("ignore")) {
            return new ControlStyle(id, message("Ignore"), "", "iconIgnore.png");
        }

        if (id.startsWith("github")) {
            return new ControlStyle(id, "github", "", "iconGithub.png");
        }

        if (id.startsWith("txt")) {
            return new ControlStyle(id, message("Texts"), "", "iconTxt.png");
        }

        switch (id) {

            case "selectButton":
                return new ControlStyle(id, message("Select"), "", "iconSelect.png");

            case "unselectButton":
                return new ControlStyle(id, message("Unselect"), "", "iconSelectNone.png");

            case "selectAllFilesButton":
                return new ControlStyle("selectAllFilesButton", message("SelectAll"), "CTRL+a", "iconSelectAll.png");

            case "unselectAllFilesButton":
                return new ControlStyle("unselectAllFilesButton", message("UnselectAll"), "", "iconSelectNone.png");

            case "cancelButton":
                return new ControlStyle("cancelButton", message("Cancel"), "ESC", "iconCancel.png");

            case "cropButton":
                return new ControlStyle("cropButton", message("Crop"), "CTRL+x / ALT+x", "iconCrop.png");

            case "metaButton":
                return new ControlStyle("metaButton", message("MetaData"), "", "iconMeta.png");

            case "pButton":
                return new ControlStyle(id, message("Paragraph"), "", "iconP.png");

            case "brButton":
                return new ControlStyle(id, message("Newline"), "", "iconBr.png");

            case "statisticButton":
                return new ControlStyle("statisticButton", message("Statistic"), "", "iconStatistic.png");

            case "setButton":
                return new ControlStyle("setButton", message("Set"), "F1 / ALT+1", "iconEqual.png");

            case "firstButton":
                return new ControlStyle("firstButton", message("First"), "ALT+HOME", "iconFirst.png");

            case "lastButton":
                return new ControlStyle("lastButton", message("Last"), "ALT+END", "iconLast.png");

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

            case "imageSizeButton":
                return new ControlStyle("imageSizeButton", message("LoadedSize"), "CTRL+1", "iconPicSmall.png");

            case "paneSizeButton":
                return new ControlStyle("paneSizeButton", message("PaneSize"), "CTRL+2", "iconPicBig.png");

            case "moveLeftButton":
                return new ControlStyle("moveLeftButton", message("MoveLeft"), "", "iconLeft.png");

            case "moveRightButton":
                return new ControlStyle("moveRightButton", message("MoveRight"), "", "iconRight.png");

            case "upFilesButton":
                return new ControlStyle("upFilesButton", message("MoveUp"), "", "iconUp.png");

            case "downFilesButton":
                return new ControlStyle("downFilesButton", message("MoveDown"), "", "iconDown.png");

            case "closeButton":
                return new ControlStyle("closeButton", message("Close"), "F4", "iconClose.png");

            case "findNextButton":
                return new ControlStyle("findNextButton", message("Next"), "CTRL+2", "iconNext.png");

            case "findPreviousButton":
                return new ControlStyle("findPreviousButton", message("Previous"), "CTRL+1", "iconPrevious.png");

            case "replaceButton":
                return new ControlStyle("replaceButton", message("Replace"), "CTRL+q / CTRL+h", "iconReplace.png");

            case "replaceAllButton":
                return new ControlStyle("replaceAllButton", message("ReplaceAll"), "CTRL+w", "iconReplaceAll.png");

            case "withdrawButton":
                return new ControlStyle("withdrawButton", message("Withdraw"), "ESC / CTRL+w / ALT+w", "iconWithdraw.png");

            case "insertFilesButton":
                return new ControlStyle("insertFilesButton", message("InsertFiles"), "", "iconFileInsert.png");

            case "insertDirectoryButton":
                return new ControlStyle("insertDirectoryButton", message("InsertDirectory"), "", "iconFolderInsert.png");

            case "openTargetButton":
                return new ControlStyle("openTargetButton", message("Open"), "", "iconOpen.png");

            case "browseButton":
                return new ControlStyle("browseButton", message("Browse"), "", "iconBrowse.png");

            case "sytemClipboardButton":
                return new ControlStyle("sytemClipboardButton", "", message("LoadImageInSystemClipboard"), "", "iconPicSmall.png");

            case "mirrorHButton":
                return new ControlStyle("mirrorHButton", message("MirrorHorizontal"), "", "iconHorizontal.png");

            case "mirrorVButton":
                return new ControlStyle("mirrorVButton", message("MirrorVertical"), "", "iconVertical.png");

            case "shearButton":
                return new ControlStyle("shearButton", message("Shear"), "", "iconShear.png");

            case "setAllOrSelectedButton":
                return new ControlStyle(id, message("SetAllOrSelected"), "", "iconEqual.png");

            case "testButton":
                return new ControlStyle("testButton", message("Test"), "", "iconGo.png");

            case "openForeImageButton":
                return new ControlStyle("openForeImageButton", message("Select"), "", "iconOpen.png");

            case "foreImagePaneSizeButton":
                return new ControlStyle("foreImagePaneSizeButton", message("PaneSize"), "", "iconPicBig.png");

            case "foreImageImageSizeButton":
                return new ControlStyle("foreImageImageSizeButton", message("ImageSize"), "", "iconPicSmall.png");

            case "openBackImageButton":
                return new ControlStyle("openBackImageButton", message("Select"), "", "iconOpen.png");

            case "backImagePaneSizeButton":
                return new ControlStyle("backImagePaneSizeButton", message("PaneSize"), "", "iconPicBig.png");

            case "backImageImageSizeButton":
                return new ControlStyle("backImageImageSizeButton", message("ImageSize"), "", "iconPicSmall.png");

            case "openWindowButton":
                return new ControlStyle("newWindowButton", message("OpenInNewWindow"), "", "iconWindow.png");

            case "newTabButton":
                return new ControlStyle(id, message("NewTab"), "", "iconPlus.png");

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

            case "gridButton":
                return new ControlStyle(id, message("Grid"), "", "iconBrowse.png");

            case "snapshotButton":
                return new ControlStyle("snapshotButton", message("Snapshot"), "", "iconSnapshot.png");

            case "splitButton":
                return new ControlStyle("splitButton", message("Split"), "", "iconSplit.png");

            case "sampleButton":
                return new ControlStyle("sampleButton", message("Sample"), "", "iconSample.png");

            case "calculatorButton":
                return new ControlStyle("calculatorButton", message("PixelsCalculator"), "", "iconCalculator.png");

            case "filterButton":
                return new ControlStyle("filterButton", message("Filter"), "", "iconFilter.png");

            case "locateLineButton":
                return new ControlStyle("locateLineButton", message("Go"), "", "iconGo.png");

            case "locateObjectButton":
                return new ControlStyle("locateObjectButton", message("Go"), "", "iconGo.png");

            case "playButton":
                return new ControlStyle("playButton", message("Play"), "F1", "iconPlay.png");

            case "pauseButton":
                return new ControlStyle("pauseButton", message("Pause"), "", "iconPause.png");

            case "pixSelectButton":
                return new ControlStyle("pixSelectButton", message("Select"), "", "iconOpen.png");

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

            case "suggestButton":
                return new ControlStyle("suggestButton", message("SuggestedSettings"), "", "iconIdea.png");

            case "originalImageButton":
                return new ControlStyle("originalImageButton", message("OriginalImage"), "", "iconPhoto.png");

            case "xmlButton":
                return new ControlStyle("xmlButton", "XML", "", "iconXML.png");

            case "refreshHeaderButton":
                return new ControlStyle("refreshHeaderButton", message("Refresh"), "", "iconRefresh.png");

            case "refreshXmlButton":
                return new ControlStyle("refreshXmlButton", message("Refresh"), "", "iconRefresh.png");

            case "refreshCieDataButton":
                return new ControlStyle("refreshCieDataButton", message("Refresh"), "", "iconRefresh.png");

            case "validateButton":
                return new ControlStyle("validateButton", message("Validate"), "", "iconView.png");

            case "calculateXYZButton":
                return new ControlStyle("calculateXYZButton", message("Calculate"), "", "iconCalculator.png");

            case "calculateXYButton":
                return new ControlStyle("calculateXYButton", message("Calculate"), "", "iconCalculator.png");

            case "calculateDisplayButton":
                return new ControlStyle("calculateDisplayButton", message("Display"), "", "iconGraph.png");

            case "displayDataButton":
                return new ControlStyle("displayDataButton", message("Display"), "", "iconGraph.png");

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

            case "calculateAllButton":
                return new ControlStyle("calculateAllButton", message("Calculate"), "", "iconCalculator.png");

            case "iccSelectButton":
                return new ControlStyle("iccSelectButton", message("Select"), "", "iconOpen.png");

            case "ocrPathButton":
                return new ControlStyle("ocrPathButton", message("Select"), "", "iconOpen.png");

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

            case "moveTopButton":
                return new ControlStyle("moveTopButton", message("MoveTop"), "", "iconDoubleUp.png");

            case "zoomIn2Button":
                return new ControlStyle("zoomIn2Button", message("ZoomIn"), "", "iconZoomIn.png");

            case "zoomOut2Button":
                return new ControlStyle("zoomOut2Button", message("ZoomOut"), "", "iconZoomOut.png");

            case "imageSize2Button":
                return new ControlStyle("imageSize2Button", message("LoadedSize"), "", "iconPicSmall.png");

            case "paneSize2Button":
                return new ControlStyle("paneSize2Button", message("PaneSize"), "", "iconPicBig.png");

            case "pagePreviousButton":
                return new ControlStyle("pagePreviousButton", message("PreviousPage"), "ALT+PAGE_UP", "iconPrevious.png");

            case "pageNextButton":
                return new ControlStyle("pageNextButton", message("NextPage"), "ALT+PAGE_DOWN", "iconNext.png");

            case "pageFirstButton":
                return new ControlStyle("pageFirstButton", message("FirstPage"), "ALT+HOME", "iconFirst.png");

            case "pageLastButton":
                return new ControlStyle("pageLastButton", message("LastPage"), "ALT+END", "iconLast.png");

            case "allButton":
                return new ControlStyle("allButton", message("All"), "CTRL+a", "iconCheckAll.png");

            case "refreshHtmlButton":
                return new ControlStyle("refreshHtmlButton", message("Refresh"), "", "iconRefresh.png");

            case "refreshTextButton":
                return new ControlStyle("refreshTextButton", message("Refresh"), "", "iconRefresh.png");

            case "refreshMarkdownButton":
                return new ControlStyle("refreshMarkdownButton", message("Refresh"), "", "iconRefresh.png");

            case "streamMediaButton":
                return new ControlStyle(id, message("StreamMedia"), "", "iconLink.png");

            case "helpMeButton":
                return new ControlStyle(id, message("HelpMe"), "", "iconCatFoot.png");

            case "pickColorButton":
                return new ControlStyle(id, message("PickColor"), message("ColorPickerComments"), "", "iconPickColor.png");

            case "locationButton":
                return new ControlStyle(id, message("Locate"), "", "iconLocation.png");

            case "footButton":
                return new ControlStyle(id, message("Footprints"), "", "iconCatFoot.png");

            case "chinaButton":
                return new ControlStyle(id, message("China"), "", "iconChina.png");

            case "globalButton":
                return new ControlStyle(id, message("Global"), "", "iconGlobal.png");

            case "rowDeleteButton":
                return new ControlStyle(id, message("Delete"), "", "iconRowDelete.png");

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

    public static String getTips(Node node, ControlStyle style) {
        if (style == null) {
            return null;
        }
        String tips = "";
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
        if (node instanceof Button && ((Button) node).isDefaultButton()) {
            tips += "\nENTER";
        }
        return tips;
    }

    public static void setTips(Node node) {
        String id = node.getId();
        if (id == null) {
            return;
        }
        ControlStyle style = getControlStyle(node);
        setTips(node, style);
    }

    public static void setTips(Node node, String tips) {
        if (tips == null || tips.isEmpty()) {
            return;
        }
        FxmlControl.setTooltip(node, new Tooltip(tips));
    }

    public static void setTips(Node node, ControlStyle style) {
        if (node == null || style == null) {
            return;
        }
        setTips(node, getTips(node, style));
    }

    public static void setName(Node node, String name) {
        String id = node.getId();
        if (id == null) {
            return;
        }
        ControlStyle style = getControlStyle(node);
        style.setName(name);
        setTips(node, style);
    }

    public static void setNameIcon(Node node, String name, String iconName) {
        setIconName(node, iconName);
        String id = node.getId();
        if (id == null) {
            return;
        }
        ControlStyle style = getControlStyle(node);
        style.setName(name);
        setTips(node, style);
    }

    public static void setIconTooltips(Node node, String iconName, String tips) {
        setIconName(node, iconName);
        setTips(node, tips);
    }

    public static void setTextStyle(Node node, ControlStyle controlStyle, ColorStyle colorStyle) {
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
            MyBoxLog.debug(node.getId() + " " + e.toString());

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

    public static void setIconName(Node node, String iconName) {
        setIcon(node, getIcon(iconName));
    }

    public static void setIcon(Node node, String icon) {
        try {
            if (node == null || icon == null || icon.isEmpty()) {
                return;
            }
            ImageView v = new ImageView(icon);

            if (node instanceof Labeled) {
                if (((Labeled) node).getGraphic() != null) {
                    if (node.getStyleClass().contains("big")) {
                        v.setFitWidth(AppVariables.iconSize * 2);
                        v.setFitHeight(AppVariables.iconSize * 2);
                    } else if (node.getStyleClass().contains("halfBig")) {
                        v.setFitWidth(AppVariables.iconSize * 1.5);
                        v.setFitHeight(AppVariables.iconSize * 1.5);
                    } else {
                        v.setFitWidth(AppVariables.iconSize);
                        v.setFitHeight(AppVariables.iconSize);
                    }
                    ((Labeled) node).setGraphic(v);
                }

            } else if (node instanceof ImageView) {
                ImageView nodev = (ImageView) node;
                nodev.setImage(v.getImage());
                if (node.getStyleClass().contains("big")) {
                    nodev.setFitWidth(AppVariables.iconSize * 2);
                    nodev.setFitHeight(AppVariables.iconSize * 2);
                } else {
                    nodev.setFitWidth(AppVariables.iconSize * 1.2);
                    nodev.setFitHeight(AppVariables.iconSize * 1.2);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, node.getId());

        }
    }

    public static String getIcon(ControlStyle style, ColorStyle color) {
        try {
            if (style == null || style.getIconName() == null
                    || style.getIconName().isEmpty()) {
                return null;
            }
            return getIcon(color, style.getIconName());
        } catch (Exception e) {
            MyBoxLog.error(e, style.getIconName());
            return null;
        }
    }

    public static String getIcon(ColorStyle style, String iconName) {
        if (style == null || iconName == null || iconName.isBlank()) {
            return null;
        }
        try {
            String path = getIconPath(style);
            String finalName = iconName;
            if (AppVariables.hidpiIcons && iconName.endsWith(".png") && !iconName.endsWith("_100.png")) {
                finalName = iconName.substring(0, iconName.length() - 4) + "_100.png";
                try {
                    new ImageView(path + finalName);
                } catch (Exception e) {
                    finalName = iconName;
                }
            }
            return path + finalName;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

    public static String getIcon(String iconName) {
        return getIcon(AppVariables.ControlColor, iconName);
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
