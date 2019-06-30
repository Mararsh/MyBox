package mara.mybox.fxml;

import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

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

    public static ControlStyle getControlStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {

            // Buttons
            case "selectSourceButton":
                return new ControlStyle("selectSourceButton", getMessage("Select"), "", "iconOpen.png");

            case "okButton":
                return new ControlStyle("okButton", getMessage("OK"), "ENTER / F1 / CTRL+g", "iconOK.png");

            case "createButton":
                return new ControlStyle("createButton", getMessage("Create"), "CTRL+n", "iconAddFile.png");

            case "deleteButton":
                return new ControlStyle("deleteButton", getMessage("Delete"), "CTRL+d / DELETE", "iconDelete.png");

            case "copyButton":
                return new ControlStyle("copyButton", getMessage("Copy"), "CTRL+c", "iconCopy.png");

            case "pasteButton":
                return new ControlStyle("pasteButton", getMessage("Paste"), "CTRL+v", "iconPaste.png");

            case "saveButton":
                return new ControlStyle("saveButton", getMessage("Save"), "F2 / CTRL+s", "iconSave.png");

            case "saveAsButton":
                return new ControlStyle("saveAsButton", getMessage("SaveAs"), "", "iconSaveAs.png");

            case "selectAllButton":
                return new ControlStyle("selectAllButton", getMessage("SelectAll"), "CTRL+a", "iconSelectAll.png");

            case "cropButton":
                return new ControlStyle("cropButton", getMessage("Crop"), "CTRL+x", "iconCrop.png");

            case "recoverButton":
                return new ControlStyle("recoverButton", getMessage("Recover"), "CTRL+r", "iconRecover.png");

            case "renameButton":
                return new ControlStyle("renameButton", getMessage("Rename"), "", "iconRename.png");

            case "infoButton":
                return new ControlStyle("infoButton", getMessage("Information"), "CTRL+i", "iconInfo.png");

            case "metaButton":
                return new ControlStyle("metaButton", getMessage("MetaData"), "", "iconMeta.png");

            case "nextButton":
                return new ControlStyle("nextButton", getMessage("Next"), "PAGE DOWN", "iconNext.png");

            case "previousButton":
                return new ControlStyle("previousButton", getMessage("Previous"), "PAGE UP", "iconPrevious.png");

            case "firstButton":
                return new ControlStyle("firstButton", getMessage("First"), "CTRL+HOME", "iconFirst.png");

            case "lastButton":
                return new ControlStyle("lastButton", getMessage("Last"), "CTRL+END", "iconLast.png");

            case "goButton":
                return new ControlStyle("goButton", getMessage("Go"), "", "iconGo.png");

            case "viewButton":
                return new ControlStyle("viewButton", getMessage("View"), "", "iconView.png");

            case "popButton":
                return new ControlStyle("popButton", getMessage("Pop"), "CTRL+p", "iconPop.png");

            case "refButton":
                return new ControlStyle("refButton", getMessage("Ref"), getMessage("UseCurrentImageAsReference"), "CTRL+f", "iconRef.png");

            case "ref2Button":
                return new ControlStyle("refButton2", getMessage("Ref"), getMessage("UseCurrentImageAsReference"), "CTRL+f", "iconRef.png");

            case "redoButton":
                return new ControlStyle("redoButton", getMessage("Redo"), "CTRL+y", "iconRedo.png");

            case "undoButton":
                return new ControlStyle("undoButton", getMessage("Undo"), "CTRL+z", "iconUndo.png");

            case "imageSizeButton":
                return new ControlStyle("imageSizeButton", getMessage("LoadedSize"), "CTRL+1", "iconPicSmall.png");

            case "paneSizeButton":
                return new ControlStyle("paneSizeButton", getMessage("PaneSize"), "CTRL+2", "iconPicBig.png");

            case "zoomInButton":
                return new ControlStyle("zoomInButton", getMessage("ZoomIn"), "CTRL+3", "iconZoomIn.png");

            case "zoomOutButton":
                return new ControlStyle("zoomOutButton", getMessage("ZoomOut"), "CTRL+4", "iconZoomOut.png");

            case "rotateLeftButton":
                return new ControlStyle("rotateLeftButton", getMessage("RotateLeft"), "", "iconRotateLeft.png");

            case "rotateRightButton":
                return new ControlStyle("rotateRightButton", getMessage("RotateRight"), "", "iconRotateRight.png");

            case "turnOverButton":
                return new ControlStyle("turnOverButton", getMessage("TurnOver"), "", "iconTurnOver.png");

            case "moveLeftButton":
                return new ControlStyle("moveLeftButton", getMessage("MoveLeft"), "", "iconLeft.png");

            case "moveRightButton":
                return new ControlStyle("moveRightButton", getMessage("MoveRight"), "", "iconRight.png");

            case "moveUpButton":
                return new ControlStyle("moveUpButton", getMessage("MoveUp"), "", "iconUp.png");

            case "moveDownButton":
                return new ControlStyle("moveDownButton", getMessage("MoveDown"), "", "iconDown.png");

            case "previewButton":
                return new ControlStyle("previewButton", getMessage("Preview"), getMessage("PreviewComments"), "", "iconPreview.png");

            case "startButton":
                return new ControlStyle("startButton", "", "ENTER", "");

            case "closeButton":
                return new ControlStyle("closeButton", getMessage("Close"), "F4 / ENTER", "iconClose.png");

            case "findNextButton":
                return new ControlStyle("findNextButton", getMessage("Next"), "CTRL+n", "iconNext.png");

            case "findPreviousButton":
                return new ControlStyle("findPreviousButton", getMessage("Previous"), "CTRL+p", "iconPrevious.png");

            case "findLastButton":
                return new ControlStyle("findLastButton", getMessage("Last"), "CTRL+l", "iconLast.png");

            case "findFirstButton":
                return new ControlStyle("findFirstButton", getMessage("First"), "CTRL+f", "iconFirst.png");

            case "replaceButton":
                return new ControlStyle("replaceButton", getMessage("Replace"), "CTRL+e", "iconReplace.png");

            case "replaceAllButton":
                return new ControlStyle("replaceAllButton", getMessage("ReplaceAll"), "", "iconReplaceAll.png");

            case "clearButton":
                return new ControlStyle("clearButton", getMessage("Clear"), "", "iconClear.png");

            case "withdrawButton":
                return new ControlStyle("withdrawButton", getMessage("Withdraw"), "", "iconWithdraw.png");

            case "scopeClearButton":
                return new ControlStyle("scopeClearButton", getMessage("Clear"), "", "iconClear.png");

            case "scopeDeleteButton":
                return new ControlStyle("scopeDeleteButton", getMessage("Delete"), "", "iconDelete.png");

            case "polygonWithdrawButton":
                return new ControlStyle("polygonWithdrawButton", getMessage("Withdraw"), "", "iconWithdraw.png");

            case "polygonClearButton":
                return new ControlStyle("polygonClearButton", getMessage("Delete"), "", "iconClear.png");

            case "addFilesButton":
                return new ControlStyle("addFilesButton", getMessage("AddFiles"), "", "iconAddFile.png");

            case "insertFilesButton":
                return new ControlStyle("insertFilesButton", getMessage("InsertFiles"), "", "iconInsertFile.png");

            case "selectSourcePathButton":
                return new ControlStyle("selectSourcePathButton", getMessage("Select"), "", "iconFolder.png");

            case "selectTargetPathButton":
                return new ControlStyle("selectTargetPathButton", getMessage("Select"), "", "iconFolder.png");

            case "selectTargetFileButton":
                return new ControlStyle("selectTargetFileButton", getMessage("Select"), "", "iconOpen.png");

            case "addDirectoryButton":
                return new ControlStyle("addDirectoryButton", getMessage("AddDirectory"), "", "iconAddFolder.png");

            case "insertDirectoryButton":
                return new ControlStyle("insertDirectoryButton", getMessage("InsertDirectory"), "", "iconInsertFolder.png");

            case "deleteFileButton":
                return new ControlStyle("deleteFileButton", getMessage("Delete"), "", "iconDeleteFile.png");

            case "deleteFilesButton":
                return new ControlStyle("deleteFilesButton", getMessage("Delete"), "", "iconDeleteFile.png");

            case "openTargetButton":
                return new ControlStyle("openTargetButton", getMessage("Open"), "", "iconOpen.png");

            case "viewTargetFileButton":
                return new ControlStyle("viewTargetFileButton", getMessage("View"), "", "iconView.png");

            case "browseButton":
                return new ControlStyle("browseButton", getMessage("Browse"), "", "iconBrowse.png");

            case "selectRefButton":
                return new ControlStyle("selectRefButton", getMessage("Select"), "", "iconOpen.png");

            case "mirrorHButton":
                return new ControlStyle("mirrorHButton", getMessage("MirrorHorizontal"), "", "iconHorizontal.png");

            case "mirrorVButton":
                return new ControlStyle("mirrorVButton", getMessage("MirrorVertical"), "", "iconVertical.png");

            case "shearButton":
                return new ControlStyle("shearButton", getMessage("Shear"), "", "iconShear.png");

            case "saveImagesButton":
                return new ControlStyle("saveImagesButton", getMessage("SaveAsImages"),
                        getMessage("SaveAsImages") + "\n" + getMessage("FilePrefixInput"), "", "");

            case "saveTiffButton":
                return new ControlStyle("saveTiffButton", getMessage("SaveAsTiff"), "", "iconTiff.png");

            case "savePdfButton":
                return new ControlStyle("savePdfButton", getMessage("SaveAsPDF"), "", "iconPDF.png");

            case "clearRowsButton":
                return new ControlStyle("clearRowsButton", getMessage("Clear"), "", "iconClear.png");

            case "clearColsButton":
                return new ControlStyle("clearColsButton", getMessage("Clear"), "", "iconClear.png");

            case "examplesButton":
                return new ControlStyle("examplesButton", getMessage("Examples"), "", "iconExamples.png");

            case "testButton":
                return new ControlStyle("testButton", getMessage("Test"), "", "iconRun.png");

            case "editButton":
                return new ControlStyle("editButton", getMessage("Edit"), "", "iconEdit.png");

            case "openForeImageButton":
                return new ControlStyle("openForeImageButton", getMessage("Select"), "", "iconOpen.png");

            case "viewForeImageButton":
                return new ControlStyle("viewForeImageButton", getMessage("View"), "", "iconView.png");

            case "foreImagePaneSizeButton":
                return new ControlStyle("foreImagePaneSizeButton", getMessage("PaneSize"), "", "iconPicBig.png");

            case "foreImageImageSizeButton":
                return new ControlStyle("foreImageImageSizeButton", getMessage("ImageSize"), "", "iconPicSmall.png");

            case "openBackImageButton":
                return new ControlStyle("openBackImageButton", getMessage("Select"), "", "iconOpen.png");

            case "viewBackImageButton":
                return new ControlStyle("viewBackImageButton", getMessage("View"), "", "iconView.png");

            case "backImagePaneSizeButton":
                return new ControlStyle("backImagePaneSizeButton", getMessage("PaneSize"), "", "iconPicBig.png");

            case "backImageImageSizeButton":
                return new ControlStyle("backImageImageSizeButton", getMessage("ImageSize"), "", "iconPicSmall.png");

            case "newWindowButton":
                return new ControlStyle("newWindowButton", getMessage("OpenInNewWindow"), "", "iconView.png");

            case "strightButton":
                return new ControlStyle("strightButton", getMessage("Straighten"), "", "iconRefresh.png");

            case "activeButton":
                return new ControlStyle("activeButton", getMessage("Active"), "", "iconActive.png");

            case "inactiveButton":
                return new ControlStyle("inactiveButton", getMessage("Inactive"), "", "iconInactive.png");

            case "thumbsListButton":
                return new ControlStyle("thumbsListButton", getMessage("ThumbnailsList"), "", "iconThumbsList.png");

            case "filesListButton":
                return new ControlStyle("filesListButton", getMessage("FilesList"), "", "iconList.png");

            case "snapshotButton":
                return new ControlStyle("snapshotButton", getMessage("Snapshot"), "", "iconSnapshot.png");

            case "splitButton":
                return new ControlStyle("splitButton", getMessage("Split"), "", "iconSplit.png");

            case "sampleButton":
                return new ControlStyle("sampleButton", getMessage("Sample"), "", "iconSample.png");

            case "statisticButton":
                return new ControlStyle("statisticButton", getMessage("Statistic"), "", "iconStatistic.png");

            case "manufactureButton":
                return new ControlStyle("manufactureButton", getMessage("Manufacture"), "", "iconEdit.png");

            case "calculatorButton":
                return new ControlStyle("calculatorButton", getMessage("PixelsCalculator"), "", "iconCalculator.png");

            case "filterButton":
                return new ControlStyle("filterButton", getMessage("Filter"), "", "iconFilter.png");

            case "locateLineButton":
                return new ControlStyle("locateLineButton", getMessage("Go"), "", "iconGo.png");

            case "locateObjectButton":
                return new ControlStyle("locateObjectButton", getMessage("Go"), "", "iconGo.png");

            case "recoveryAllButton":
                return new ControlStyle("recoveryAllButton", getMessage("RecoverAll"), "", "iconRecover.png");

            case "recoverySelectedButton":
                return new ControlStyle("recoverySelectedButton", getMessage("RecoverSelected"), "", "iconRestoreFile.png");

            case "playButton":
                return new ControlStyle("playButton", getMessage("Play"), "", "iconPlay.png");

            case "pauseButton":
                return new ControlStyle("pauseButton", getMessage("Pause"), "", "iconPause.png");

            case "selectSoundButton":
                return new ControlStyle("selectSoundButton", getMessage("Select"), "", "iconOpen.png");

            case "selectMusicButton":
                return new ControlStyle("selectMusicButton", getMessage("Select"), "", "iconOpen.png");

            case "pixSelectButton":
                return new ControlStyle("pixSelectButton", getMessage("Select"), "", "iconOpen.png");

            case "refreshButton":
                return new ControlStyle("refreshButton", getMessage("Refresh"), "", "iconRecover.png");

            case "loadButton":
                return new ControlStyle("loadButton", getMessage("Load"), "", "iconGo.png");

            case "setButton":
                return new ControlStyle("setButton", getMessage("Set"), "", "iconEqual.png");

            case "increaseButton":
                return new ControlStyle("increaseButton", getMessage("Increase"), "", "iconPlus.png");

            case "decreaseButton":
                return new ControlStyle("decreaseButton", getMessage("Decrease"), "", "iconMinus.png");

            case "invertButton":
                return new ControlStyle("invertButton", getMessage("Invert"), "", "iconInvert.png");

            case "originalButton":
                return new ControlStyle("originalButton", getMessage("OriginalSize"), "", "iconOriginalSize.png");

            case "wowButton":
                return new ControlStyle("wowButton", getMessage("WowAsExample"), "", "iconWOW.png");

            case "suggestButton":
                return new ControlStyle("suggestButton", getMessage("SuggestedSettings"), "", "iconIdea.png");

            case "originalImageButton":
                return new ControlStyle("originalImageButton", getMessage("OriginalImage"), "", "iconPhoto.png");

            case "xmlButton":
                return new ControlStyle("xmlButton", "XML", "", "iconXML.png");

            case "exportButton":
                return new ControlStyle("exportButton", getMessage("Export"), "", "iconExport.png");

            case "importButton":
                return new ControlStyle("importButton", getMessage("Import"), "", "iconImport.png");

            case "refreshHeaderButton":
                return new ControlStyle("refreshHeaderButton", getMessage("Refresh"), "", "iconRefresh.png");

            case "refreshXmlButton":
                return new ControlStyle("refreshXmlButton", getMessage("Refresh"), "", "iconRefresh.png");

            case "refreshCieDataButton":
                return new ControlStyle("refreshCieDataButton", getMessage("Refresh"), "", "iconRefresh.png");

            case "exportCieDataButton":
                return new ControlStyle("exportCieDataButton", getMessage("Export"), "", "iconExport.png");

            case "exportDiagramButton":
                return new ControlStyle("exportDiagramButton", getMessage("Export"), "", "iconExport.png");

            case "exportXmlButton":
                return new ControlStyle("exportXmlButton", getMessage("Export"), "", "iconExport.png");

            case "validateButton":
                return new ControlStyle("validateButton", getMessage("Validate"), "", "iconView.png");

            case "copyAsAButton":
                return new ControlStyle("copyAsAButton", getMessage("CopyAsMatrixA"), "", "iconCopyA.png");

            case "copyAsBButton":
                return new ControlStyle("copyAsAButton", getMessage("CopyAsMatrixB"), "", "iconCopyB.png");

            case "export10DegreeButton":
                return new ControlStyle("export10DegreeButton", getMessage("Export"), "", "iconExport.png");

            case "export2DegreeButton":
                return new ControlStyle("export2DegreeButton", getMessage("Export"), "", "iconExport.png");

            case "calculateXYZButton":
                return new ControlStyle("calculateXYZButton", getMessage("Calculate"), "", "iconCalculator.png");

            case "calculateXYButton":
                return new ControlStyle("calculateXYButton", getMessage("Calculate"), "", "iconCalculator.png");

            case "calculateDisplayButton":
                return new ControlStyle("calculateDisplayButton", getMessage("Display"), "", "iconGraph.png");

            case "displayDataButton":
                return new ControlStyle("displayDataButton", getMessage("Display"), "", "iconGraph.png");

            case "fileExportButton":
                return new ControlStyle("fileExportButton", getMessage("Export"), "", "iconExport.png");

            case "editOkButton":
                return new ControlStyle("editOkButton", getMessage("OK"), "", "iconOK.png");

            case "valueOkButton":
                return new ControlStyle("valueOkButton", getMessage("OK"), "", "iconOK.png");

            case "plusButton":
                return new ControlStyle("plusButton", getMessage("Plus"), "", "iconPlus.png");

            case "minusButton":
                return new ControlStyle("minusButton", getMessage("Minus"), "", "iconMinus.png");

            case "multiplyButton":
                return new ControlStyle("multiplyButton", getMessage("Multiply"), "", "iconMultiply.png");

            case "calculateButton":
                return new ControlStyle("calculateButton", getMessage("Calculate"), "", "iconCalculator.png");

            case "matrixPlusButton":
                return new ControlStyle("matrixPlusButton", getMessage("A + B"), "", "iconPlus.png");

            case "matrixMinusButton":
                return new ControlStyle("matrixMinusButton", getMessage("A - B"), "", "iconMinus.png");

            case "matrixMultiplyButton":
                return new ControlStyle("matrixMultiplyButton", getMessage("AB"), "", "iconMultiply.png");

            case "hadamardProductButton":
                return new ControlStyle("hadamardProductButton", getMessage("HadamardProductComments"), "", "iconAsterisk.png");

            case "kroneckerProductButton":
                return new ControlStyle("kroneckerProductButton", getMessage("KroneckerProductComments"), "", "iconCancel.png");

            case "verticalMergeButton":
                return new ControlStyle("verticalMergeButton", getMessage("VerticalMergeComments"), "", "iconVerticalMerge.png");

            case "horizontalMergeButton":
                return new ControlStyle("horizontalMergeButton", getMessage("HorizontalMergeComments"), "", "iconHorizontalMerge.png");

            case "calculateAllButton":
                return new ControlStyle("calculateAllButton", getMessage("Calculate"), "", "iconCalculator.png");

            case "iccSelectButton":
                return new ControlStyle("iccSelectButton", getMessage("Select"), "", "iconOpen.png");

            // RadioButton
            case "miaoButton":
                return new ControlStyle("miaoButton", getMessage("Meow"), getMessage("MiaoPrompt"), "", "iconCat.png");

            case "pcxSelect":
                return new ControlStyle("pcxSelect", "pcx", getMessage("PcxComments"), "", "");

            // ToggleButton
            case "pickColorButton":
                return new ControlStyle("pickColorButton", getMessage("PickColor"), getMessage("ColorPickerComments"), "", "iconPickColor.png");

            case "pickFillColorButton":
                return new ControlStyle("pickFillColorButton", getMessage("PickColor"), getMessage("ColorPickerComments"), "", "iconPickColor.png");

            // ImageView
            case "tipsView":
                return new ControlStyle("tipsView", "", "", "", "iconTips.png");

            case "linksView":
                return new ControlStyle("linksView", "", getMessage("Links"), "", "iconLink.png");

            case "refTipsView":
                return new ControlStyle("refTipsView", "", getMessage("ImageRefTips"), "", "iconTips.png");

            case "textTipsView":
                return new ControlStyle("textTipsView", "", "", "iconTips.png");

            case "preAlphaTipsView":
                return new ControlStyle("preAlphaTipsView", "", getMessage("PremultipliedAlphaTips"), "", "iconTips.png");

            case "shapeTipsView":
                return new ControlStyle("shapeTipsView", "", getMessage("ImageShapeTip"), "", "iconTips.png");

            case "scopeTipsView":
                return new ControlStyle("scopeTipsView", "", getMessage("ImageScopeTips"), "", "iconTips.png");

            case "weiboTipsView":
                return new ControlStyle("weiboTipsView", "", getMessage("WeiboAddressComments"), "", "iconTips.png");

            case "thresholdingTipsView":
                return new ControlStyle("thresholdingTipsView", "", getMessage("ThresholdingComments"), "", "iconTips.png");

            case "quantizationTipsView":
                return new ControlStyle("quantizationTipsView", "", getMessage("QuantizationComments"), "", "iconTips.png");

            case "BWThresholdTipsView":
                return new ControlStyle("BWThresholdTipsView", "", getMessage("BWThresholdComments"), "", "iconTips.png");

            case "distanceTipsView":
                return new ControlStyle("distanceTipsView", "", getMessage("ColorMatchComments"), "", "iconTips.png");

            case "weiboSnapTipsView":
                return new ControlStyle("weiboSnapTipsView", "", getMessage("htmlSnapComments"), "", "iconTips.png");

            case "fontTipsView":
                return new ControlStyle("fontTipsView", "", getMessage("FontFileComments"), "", "iconTips.png");

            case "pdfPageSizeTipsView":
                return new ControlStyle("pdfPageSizeTipsView", "", getMessage("PdfPageSizeComments"), "", "iconTips.png");

            case "startEndListTipsView":
                return new ControlStyle("startEndListTipsView", "", getMessage("StartEndComments"), "", "iconTips.png");

            case "pdfMemTipsView":
                return new ControlStyle("pdfMemTipsView", "", getMessage("PdfMemComments"), "", "iconTips.png");

            case "ditherTipsView":
                return new ControlStyle("ditherTipsView", "", getMessage("DitherComments"), "", "iconTips.png");

            case "effectTipsView":
                return new ControlStyle("effectTipsView", "", "", "", "iconTips.png");

            // CheckBox
            case "miaoCheck":
                return new ControlStyle("miaoCheck", getMessage("Meow"), getMessage("MiaoPrompt"), "", "iconCat.png");

            case "pdfMemBox":
                return new ControlStyle("pdfMemBox", "", getMessage("PdfMemComments"), "", "");

            case "openCheck":
                return new ControlStyle("openCheck", "", getMessage("OpenWhenComplete"), "", "iconOpen2.png");

//            case "preAlphaCheck":
//                return new ControlStyle("preAlphaCheck", "", "", "");
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
        ControlStyle style = getControlStyle(id);
        setTips(node, style);

        if (node.getStyleClass().contains("main-button")) {
            return;
        }
        if (mustStyle || AppVaribles.ControlColor != ColorStyle.Default) {
            setColorStyle(node, style, AppVaribles.ControlColor);
        }

        if (AppVaribles.controlDisplayText && node instanceof Labeled) {
            setTextStyle(node, style, AppVaribles.ControlColor);
        }

    }

    public static ColorStyle getConfigColorStyle() {
        return ControlStyle.getColorStyle(AppVaribles.getUserConfigValue("ControlColor", "default"));
    }

    public static ContentDisplay getConfigControlContent() {
        return ControlStyle.getControlContent(AppVaribles.getUserConfigValue("ControlContent", "image"));
    }

    public static boolean setConfigColorStyle(String value) {
        AppVaribles.ControlColor = getColorStyle(value);
        return AppVaribles.setUserConfigValue("ControlColor", value);
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

    public static String getTips(String id) {
        return getTips(getControlStyle(id));
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
        String tips = getTips(id);
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
        setColorStyle(node, AppVaribles.ControlColor);
    }

    public static void setColorStyle(Node node, ColorStyle style) {
        String id = node.getId();
        if (id == null) {
            return;
        }
        setIcon(node, getIcon(id, style));

    }

    public static void setColorStyle(Node node, ControlStyle style) {
        setColorStyle(node, style, AppVaribles.ControlColor);
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

            if (node instanceof Labeled) {
                v.setFitWidth(20);
                v.setFitHeight(20);
                ((Labeled) node).setGraphic(v);

            } else if (node instanceof ImageView) {
                ((ImageView) node).setImage(v.getImage());

            }

        } catch (Exception e) {
            logger.debug(node.getId() + " " + e.toString());

        }
    }

    public static String getIcon(String id, ColorStyle color) {
        try {
            return getIcon(getControlStyle(id), color);
        } catch (Exception e) {
            return id + " " + e.toString();
        }
    }

    public static String getIcon(ControlStyle style, ColorStyle color) {
        try {
            if (style == null || style.getIconName() == null
                    || style.getIconName().isEmpty()) {
                return null;
            }
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
            return path + style.getIconName();
        } catch (Exception e) {
            return null;
        }
    }

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
