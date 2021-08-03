package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StyleButton {

    public static StyleData buttonStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        StyleData data = startsWith(id);
        if (data != null) {
            return data;
        }
        return match(id);
    }

    public static StyleData startsWith(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("ok")) {
            switch (id) {
                case "okButton":
                    return new StyleData("okButton", Languages.message("OK"), "F1 / CTRL+e / ALT+e", "iconOK.png");
                default:
                    return new StyleData(id, Languages.message("OK"), "", "iconOK.png");
            }
        }
        if (id.startsWith("start")) {
            switch (id) {
                case "startButton":
                    return new StyleData(id, Languages.message("Start"), "F1 / CTRL+e / ALT+e", "iconStart.png");
                default:
                    return new StyleData(id, Languages.message("Start"), "", "iconStart.png");
            }
        }
        if (id.startsWith("saveAs")) {
            switch (id) {
                case "saveAsButton":
                    return new StyleData(id, Languages.message("SaveAs"), "F11", "iconSaveAs.png");
                default:
                    return new StyleData(id, Languages.message("SaveAs"), "", "iconSaveAs.png");
            }
        }
        if (id.startsWith("save")) {
            switch (id) {
                case "saveButton":
                    return new StyleData(id, Languages.message("Save"), "F2 / CTRL+s / ALT+s", "iconSave.png");
                case "saveImagesButton":
                    return new StyleData(id, Languages.message("SaveAsImages"), Languages.message("SaveAsImages") + "\n" + Languages.message("FilePrefixInput"), "", "");
                case "saveTiffButton":
                    return new StyleData(id, Languages.message("SaveAsTiff"), "", "iconTIF.png");
                case "savePdfButton":
                    return new StyleData(id, Languages.message("SaveAsPDF"), "", "iconPDF.png");
                default:
                    return new StyleData(id, Languages.message("Save"), "", "iconSave.png");
            }
        }
        if (id.startsWith("create")) {
            switch (id) {
                case "createButton":
                    return new StyleData(id, Languages.message("Create"), "CTRL+n", "iconAdd.png");
                default:
                    return new StyleData(id, Languages.message("Create"), "", "iconAdd.png");
            }
        }
        if (id.startsWith("add")) {
            switch (id) {
                case "addButton":
                    return new StyleData(id, Languages.message("Add"), "CTRL+n", "iconAdd.png");
                case "addFilesButton":
                    return new StyleData(id, Languages.message("AddFiles"), "", "iconFileAdd.png");
                case "addDirectoryButton":
                    return new StyleData(id, Languages.message("AddDirectory"), "", "iconFolderAdd.png");
                case "rowAddButton":
                    return new StyleData(id, Languages.message("Add"), "", "iconRowAdd.png");
                case "addImageButton":
                    return new StyleData(id, Languages.message("Image"), "", "iconLoadSize.png");
                case "addTableButton":
                    return new StyleData(id, Languages.message("Table"), "", "iconSplit.png");
                default:
                    return new StyleData(id, Languages.message("Add"), "", "iconAdd.png");
            }
        }
        if (id.startsWith("clear")) {
            switch (id) {
                case "clearButton":
                    return new StyleData(id, Languages.message("Clear"), "CTRL+g", "iconClear.png");
                default:
                    return new StyleData(id, Languages.message("Clear"), "", "iconClear.png");
            }
        }
        if (id.startsWith("plus")) {
            return new StyleData(id, Languages.message("Add"), "", "iconPlus.png");
        }
        if (id.startsWith("query")) {
            return new StyleData(id, Languages.message("Query"), "", "iconQuery.png");
        }
        if (id.startsWith("reset")) {
            return new StyleData(id, Languages.message("Reset"), "", "iconRecover.png");
        }
        if (id.startsWith("analyse")) {
            return new StyleData(id, Languages.message("Analyse"), "", "iconAnalyse.png");
        }
        if (id.startsWith("ocr")) {
            return new StyleData(id, Languages.message("OCR"), "", "iconAnalyse.png");
        }
        if (id.startsWith("yes")) {
            return new StyleData(id, Languages.message("Yes"), "", "iconYes.png");
        }
        if (id.startsWith("confirm")) {
            return new StyleData(id, Languages.message("Confirm"), "", "iconYes.png");
        }
        if (id.startsWith("complete")) {
            return new StyleData(id, Languages.message("Complete"), "", "iconYes.png");
        }
        if (id.startsWith("selectFile")) {
            return new StyleData(id, Languages.message("Select"), "", "iconOpen.png");
        }
        if (id.startsWith("selectPath")) {
            return new StyleData(id, Languages.message("Select"), "", "iconFolder.png");
        }
        if (id.startsWith("mybox")) {
            return new StyleData(id, "MyBox", "", "iconMyBox.png");
        }
        if (id.startsWith("download")) {
            return new StyleData(id, Languages.message("Download"), "", "iconDownload.png");
        }
        if (id.startsWith("default")) {
            return new StyleData(id, Languages.message("Default"), "", "iconDefault.png");
        }
        if (id.startsWith("random")) {
            return new StyleData(id, Languages.message("Random"), "", "iconRandom.png");
        }
        if (id.startsWith("example")) {
            return new StyleData(id, Languages.message("Example"), "", "iconExamples.png");
        }
        if (id.startsWith("sql")) {
            return new StyleData(id, "SQL", "", "iconSQL.png");
        }
        if (id.startsWith("charts")) {
            return new StyleData(id, Languages.message("Charts"), "", "iconCharts.png");
        }
        if (id.startsWith("recover")) {
            switch (id) {
                case "recoverButton":
                    return new StyleData("recoverButton", Languages.message("Recover"), "F3 / CTRL+r / ALT+r", "iconRecover.png");
                case "recoveryAllButton":
                    return new StyleData("recoveryAllButton", Languages.message("RecoverAll"), "", "iconRecover.png");
                case "recoverySelectedButton":
                    return new StyleData("recoverySelectedButton", Languages.message("RecoverSelected"), "", "iconFileRestore.png");
                default:
                    return new StyleData(id, Languages.message("Recover"), "", "iconRecover.png");
            }
        }
        if (id.startsWith("zoomIn")) {
            switch (id) {
                case "zoomInButton":
                    return new StyleData(id, Languages.message("ZoomIn"), "CTRL+3", "iconZoomIn.png");
                default:
                    return new StyleData(id, Languages.message("ZoomIn"), "", "iconZoomIn.png");
            }
        }
        if (id.startsWith("zoomOut")) {
            switch (id) {
                case "zoomOutButton":
                    return new StyleData(id, Languages.message("ZoomOut"), "CTRL+4", "iconZoomOut.png");
                default:
                    return new StyleData(id, Languages.message("ZoomOut"), "", "iconZoomOut.png");
            }
        }
        if (id.startsWith("copyToSystemClipboard")) {
            return new StyleData(id, Languages.message("CopyToSystemClipboard"), "", "iconCopySystem.png");
        }
        if (id.startsWith("copyToMyBoxClipboard")) {
            return new StyleData(id, Languages.message("CopyToMyBoxClipboard"), "", "iconCopy.png");
        }
        if (id.startsWith("CopyToClipboards")) {
            return new StyleData(id, Languages.message("CopyToClipboards"), "", "iconCopy.png");
        }
        if (id.startsWith("copy")) {
            switch (id) {
                case "copyButton":
                    return new StyleData(id, Languages.message("Copy"), "CTRL+c / ALT+c ", "iconCopy.png");
                case "copyAsAButton":
                    return new StyleData("copyAsAButton", Languages.message("CopyAsMatrixA"), "", "iconCopyA.png");
                case "copyAsBButton":
                    return new StyleData("copyAsAButton", Languages.message("CopyAsMatrixB"), "", "iconCopyB.png");
                case "copyEnglishButton":
                    return new StyleData(id, Languages.message("CopyEnglish"), "CTRL+e / ALT+e ", "iconCopy.png");
                default:
                    return new StyleData(id, Languages.message("Copy"), "", "iconCopy.png");
            }
        }
        if (id.startsWith("paste")) {
            switch (id) {
                case "pasteButton":
                    return new StyleData(id, Languages.message("Paste"), "CTRL+v / ALT+v", "iconPaste.png");
                case "pasteTxtButton":
                    return new StyleData(id, Languages.message("PasteTexts"), "", "iconPaste.png");
                case "pasteContentInSystemClipboardButton":
                    return new StyleData(id, Languages.message("PasteContentInSystemClipboard"), "", "iconPasteSystem.png");
                default:
                    return new StyleData(id, Languages.message("Paste"), "", "iconPaste.png");
            }
        }
        if (id.startsWith("next")) {
            switch (id) {
                case "nextButton":
                    return new StyleData(id, Languages.message("Next"), "PAGE DOWN", "iconNext.png");
                default:
                    return new StyleData(id, Languages.message("Next"), "", "iconNext.png");
            }
        }
        if (id.startsWith("previous")) {
            switch (id) {
                case "previousButton":
                    return new StyleData(id, Languages.message("Previous"), "PAGE UP", "iconPrevious.png");
                default:
                    return new StyleData(id, Languages.message("Previous"), "", "iconPrevious.png");
            }
        }
        if (id.startsWith("backward")) {
            return new StyleData(id, Languages.message("Backward"), "", "iconPrevious.png");
        }
        if (id.startsWith("forward")) {
            return new StyleData(id, Languages.message("Forward"), "", "iconNext.png");
        }
        if (id.startsWith("more")) {
            return new StyleData(id, Languages.message("More"), "", "iconMore.png");
        }
        if (id.startsWith("palette")) {
            switch (id) {
                case "paletteManageButton":
                    return new StyleData(id, Languages.message("ColorPaletteManage"), "", "iconPalette.png");
                default:
                    return new StyleData(id, Languages.message("ColorPalette"), "", "iconPalette.png");
            }
        }
        if (id.startsWith("color")) {
            return new StyleData(id, Languages.message("ColorPalette"), "", "iconColor.png");
        }
        if (id.startsWith("openFolder")) {
            return new StyleData(id, Languages.message("Open"), "", "iconOpen.png");
        }
        if (id.startsWith("delete")) {
            switch (id) {
                case "deleteButton":
                    return new StyleData("deleteButton", Languages.message("Delete"), "DELETE / CTRL+d / ALT+d", "iconDelete.png");
                default:
                    return new StyleData(id, Languages.message("Delete"), "", "iconDelete.png");
            }
        }
        if (id.startsWith("input")) {
            return new StyleData(id, Languages.message("Input"), "", "iconData.png");
        }
        if (id.startsWith("dataImport") || id.startsWith("import")) {
            return new StyleData(id, Languages.message("Import"), "", "iconImport.png");
        }
        if (id.startsWith("dataExport") || id.startsWith("export")) {
            return new StyleData(id, Languages.message("Export"), "", "iconExport.png");
        }
        if (id.startsWith("data")) {
            return new StyleData(id, Languages.message("Data"), "", "iconData.png");
        }
        if (id.startsWith("query")) {
            return new StyleData(id, Languages.message("Query"), "", "iconData.png");
        }
        if (id.startsWith("map")) {
            return new StyleData(id, Languages.message("Map"), "", "iconMap.png");
        }
        if (id.startsWith("dataset")) {
            return new StyleData(id, Languages.message("DataSet"), "", "iconDataset.png");
        }
        if (id.startsWith("sureButton")) {
            return new StyleData(id, Languages.message("Sure"), "", "iconYes.png");
        }
        if (id.startsWith("fill")) {
            return new StyleData(id, Languages.message("Fill"), "", "iconButterfly.png");
        }
        if (id.startsWith("cat")) {
            return new StyleData(id, Languages.message("Meow"), "", "iconCat.png");
        }
        if (id.startsWith("edit")) {
            switch (id) {
                case "editOkButton":
                    return new StyleData(id, Languages.message("OK"), "", "iconOK.png");
                default:
                    return new StyleData(id, Languages.message("Edit"), "", "iconEdit.png");
            }
        }
        if (id.startsWith("size")) {
            return new StyleData(id, Languages.message("Size"), "", "iconSplit.png");
        }
        if (id.startsWith("selectImage")) {
            return new StyleData(id, Languages.message("Image"), "", "iconFolderImage.png");
        }
        if (id.startsWith("equal")) {
            return new StyleData(id, Languages.message("EqualTo"), "", "iconEqual.png");
        }
        if (id.startsWith("selectAll")) {
            switch (id) {
                case "selectAllButton":
                    return new StyleData(id, Languages.message("SelectAll"), "CTRL+a / ALT+a", "iconSelectAll.png");
                default:
                    return new StyleData(id, Languages.message("SelectAll"), "", "iconSelectAll.png");
            }
        }
        if (id.startsWith("selectNone")) {
            switch (id) {
                case "selectNoneButton":
                    return new StyleData(id, Languages.message("UnselectAll"), "CTRL+o / ALT+O", "iconSelectNone.png");
                default:
                    return new StyleData(id, Languages.message("UnselectAll"), "", "iconSelectNone.png");
            }
        }
        if (id.startsWith("use")) {
            return new StyleData(id, Languages.message("Use"), "", "iconYes.png");
        }
        if (id.startsWith("refresh")) {
            return new StyleData(id, Languages.message("Refresh"), "", "iconRefresh.png");
        }
        if (id.startsWith("giveUp")) {
            return new StyleData(id, Languages.message("GiveUp"), "", "iconCatFoot.png");
        }
        if (id.startsWith("manufacture")) {
            return new StyleData(id, Languages.message("Manufacture"), "", "iconEdit.png");
        }
        if (id.startsWith("run")) {
            return new StyleData(id, Languages.message("Run"), "", "iconRun.png");
        }
        if (id.startsWith("info")) {
            switch (id) {
                case "infoButton":
                    return new StyleData(id, Languages.message("Information"), "CTRL+i", "iconInfo.png");
                default:
                    return new StyleData(id, Languages.message("Information"), "", "iconInfo.png");
            }
        }
        if (id.startsWith("view")) {
            return new StyleData(id, Languages.message("View"), "", "iconView.png");
        }
        if (id.startsWith("html")) {
            return new StyleData(id, Languages.message("Html"), "", "iconHtml.png");
        }
        if (id.startsWith("link")) {
            return new StyleData(id, Languages.message("Link"), "", "iconLink.png");
        }
        if (id.startsWith("stop")) {
            return new StyleData(id, Languages.message("Stop"), "", "iconStop.png");
        }
        if (id.startsWith("synchronize")) {
            return new StyleData(id, Languages.message("Synchronize"), "", "iconSynchronize.png");
        }
        if (id.startsWith("function")) {
            return new StyleData(id, "", "", "iconFunction.png");
        }
        if (id.startsWith("style")) {
            return new StyleData(id, Languages.message("Style"), "", "iconStyle.png");
        }
        if (id.startsWith("panesMenu")) {
            return new StyleData(id, Languages.message("Panes"), "", "iconPanes.png");
        }
        if (id.startsWith("extract")) {
            return new StyleData(id, Languages.message("Extract"), "", "iconExtract.png");
        }
        if (id.startsWith("demo")) {
            return new StyleData(id, Languages.message("Demo"), "", "iconDemo.png");
        }
        if (id.startsWith("count")) {
            return new StyleData(id, Languages.message("Count"), "", "iconCalculator.png");
        }
        if (id.startsWith("delimiter")) {
            return new StyleData(id, Languages.message("Delimiter"), "", "iconDelimiter.png");
        }
        if (id.startsWith("matrixA")) {
            return new StyleData(id, Languages.message("SetAsMatrixA"), "", "iconA.png");
        }
        if (id.startsWith("matrixB")) {
            return new StyleData(id, Languages.message("SetAsMatrixB"), "", "iconB.png");
        }
        if (id.startsWith("width")) {
            return new StyleData(id, Languages.message("Width"), "", "iconXRuler.png");
        }
        if (id.startsWith("go")) {
            return new StyleData(id, Languages.message("Go"), "", "iconGo.png");
        }
        if (id.startsWith("preview")) {
            return new StyleData(id, Languages.message("PreviewComments"), "", "iconPreview.png");
        }
        if (id.startsWith("rotateLeft")) {
            return new StyleData(id, Languages.message("RotateLeft"), "", "iconRotateLeft.png");
        }
        if (id.startsWith("rotateRight")) {
            return new StyleData(id, Languages.message("RotateRight"), "", "iconRotateRight.png");
        }
        if (id.startsWith("turnOver")) {
            return new StyleData(id, Languages.message("TurnOver"), "", "iconTurnOver.png");
        }
        if (id.startsWith("rename")) {
            return new StyleData(id, Languages.message("Rename"), "", "iconRename.png");
        }
        if (id.startsWith("header")) {
            return new StyleData(id, "", "", "iconHeader.png");
        }
        if (id.startsWith("list")) {
            return new StyleData(id, Languages.message("List"), "", "iconList.png");
        }
        if (id.startsWith("codes")) {
            return new StyleData(id, "", "", "iconMeta.png");
        }
        if (id.startsWith("fold")) {
            return new StyleData(id, Languages.message("Fold"), "", "iconMinus.png");
        }
        if (id.startsWith("unford")) {
            return new StyleData(id, Languages.message("Unfold"), "", "iconTree.png");
        }
        if (id.startsWith("moveData")) {
            return new StyleData(id, Languages.message("Move"), "", "iconRef.png");
        }
        if (id.startsWith("csv")) {
            return new StyleData(id, "CSV", "", "iconCSV.png");
        }
        if (id.startsWith("excel")) {
            return new StyleData(id, "Excel", "", "iconExcel.png");
        }
        if (id.startsWith("history")) {
            return new StyleData(id, Languages.message("History"), "", "iconHistory.png");
        }
        if (id.startsWith("ssl")) {
            return new StyleData(id, "SSL", "", "iconSSL.png");
        }
        if (id.startsWith("ignore")) {
            return new StyleData(id, Languages.message("Ignore"), "", "iconIgnore.png");
        }
        if (id.startsWith("github")) {
            return new StyleData(id, "github", "", "iconGithub.png");
        }
        if (id.startsWith("txt")) {
            return new StyleData(id, Languages.message("Texts"), "", "iconTxt.png");
        }
        if (id.startsWith("clipboard")) {
            return new StyleData(id, Languages.message("Clipboard"), "", "iconClipboard.png");
        }
        if (id.startsWith("myBoxClipboard")) {
            return new StyleData(id, Languages.message("MyBoxClipboard"), "CTRL+m", "iconClipboard.png");
        }
        if (id.startsWith("systemClipboard")) {
            return new StyleData(id, Languages.message("SystemClipboard"), "CTRL+j", "iconSystemClipboard.png");
        }
        if (id.startsWith("loadContentInSystemClipboard")) {
            return new StyleData(id, Languages.message("LoadContentInSystemClipboard"), "", "iconImageSystem.png");
        }
        if (id.startsWith("number")) {
            return new StyleData(id, "", "", "iconNumber.png");
        }
        if (id.startsWith("trim")) {
            return new StyleData(id, "", "", "iconNumber.png");
        }
        if (id.startsWith("lowerLetter")) {
            return new StyleData(id, "", "", "iconLowerLetter.png");
        }
        if (id.startsWith("upperLetter")) {
            return new StyleData(id, "", "", "iconUpperLetter.png");
        }
        if (id.startsWith("character")) {
            return new StyleData(id, "", "", "iconCharacter.png");
        }
        if (id.startsWith("buttons")) {
            return new StyleData(id, Languages.message("Buttons"), "", "iconButtons.png");
        }
        if (id.startsWith("openPath")) {
            return new StyleData(id, Languages.message("Directory"), "", "iconOpen2.png");
        }
        if (id.startsWith("closePop")) {
            return new StyleData(id, Languages.message("Close"), "ESC / F6", "iconCancel.png");
        }
        if (id.startsWith("message")) {
            return new StyleData(id, Languages.message("SendMessage"), "", "iconMessage.png");
        }
        if (id.startsWith("pop")) {
            return new StyleData(id, Languages.message("Pop"), "CTRL+p", "iconPop.png");
        }
        if (id.startsWith("left")) {
            return new StyleData(id, Languages.message("Pop"), "CTRL+p", "iconPop.png");
        }
        if (id.startsWith("play")) {
            switch (id) {
                case "playButton":
                    return new StyleData(id, Languages.message("Play"), "F1", "iconPlay.png");
                default:
                    return new StyleData(id, Languages.message("Play"), "", "iconPlay.png");
            }
        }
        return null;
    }

    public static StyleData match(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id) {
            case "selectButton":
                return new StyleData(id, Languages.message("Select"), "", "iconSelect.png");
            case "unselectButton":
                return new StyleData(id, Languages.message("Unselect"), "", "iconSelectNone.png");
            case "selectAllFilesButton":
                return new StyleData("selectAllFilesButton", Languages.message("SelectAll"), "CTRL+a", "iconSelectAll.png");
            case "unselectAllFilesButton":
                return new StyleData("unselectAllFilesButton", Languages.message("UnselectAll"), "", "iconSelectNone.png");
            case "cancelButton":
                return new StyleData("cancelButton", Languages.message("Cancel"), "ESC", "iconCancel.png");
            case "cropButton":
                return new StyleData("cropButton", Languages.message("Crop"), "CTRL+x / ALT+x", "iconCrop.png");
            case "metaButton":
                return new StyleData("metaButton", Languages.message("MetaData"), "", "iconMeta.png");
            case "pButton":
                return new StyleData(id, Languages.message("Paragraph"), "", "iconP.png");
            case "brButton":
                return new StyleData(id, Languages.message("Newline"), "", "iconBr.png");
            case "statisticButton":
                return new StyleData("statisticButton", Languages.message("Statistic"), "", "iconStatistic.png");
            case "setButton":
                return new StyleData("setButton", Languages.message("Set"), "F1", "iconEqual.png");
            case "firstButton":
                return new StyleData("firstButton", Languages.message("First"), "ALT+HOME", "iconFirst.png");
            case "lastButton":
                return new StyleData("lastButton", Languages.message("Last"), "ALT+END", "iconLast.png");
            case "refButton":
                return new StyleData("refButton", Languages.message("Ref"), Languages.message("UseCurrentImageAsReference"), "CTRL+f", "iconRef.png");
            case "ref2Button":
                return new StyleData("refButton2", Languages.message("Ref"), Languages.message("UseCurrentImageAsReference"), "CTRL+f", "iconRef.png");
            case "redoButton":
                return new StyleData("redoButton", Languages.message("Redo"), "CTRL+y / ALT+y", "iconRedo.png");
            case "undoButton":
                return new StyleData("undoButton", Languages.message("Undo"), "CTRL+z / ALT+z", "iconUndo.png");
            case "imageSizeButton":
                return new StyleData("imageSizeButton", Languages.message("LoadedSize"), "CTRL+1", "iconLoadSize.png");
            case "paneSizeButton":
                return new StyleData("paneSizeButton", Languages.message("PaneSize"), "CTRL+2", "iconPaneSize.png");
            case "moveLeftButton":
                return new StyleData("moveLeftButton", Languages.message("MoveLeft"), "", "iconLeft.png");
            case "moveRightButton":
                return new StyleData("moveRightButton", Languages.message("MoveRight"), "", "iconRight.png");
            case "upFilesButton":
                return new StyleData("upFilesButton", Languages.message("MoveUp"), "", "iconUp.png");
            case "downFilesButton":
                return new StyleData("downFilesButton", Languages.message("MoveDown"), "", "iconDown.png");
            case "closeButton":
                return new StyleData("closeButton", Languages.message("Close"), "F4", "iconClose.png");
            case "findNextButton":
                return new StyleData("findNextButton", Languages.message("Next"), "CTRL+2", "iconNext.png");
            case "findPreviousButton":
                return new StyleData("findPreviousButton", Languages.message("Previous"), "CTRL+1", "iconPrevious.png");
            case "findButton":
                return new StyleData(id, Languages.message("Find"), "CTRL+f", "iconFind.png");
            case "replaceButton":
                return new StyleData("replaceButton", Languages.message("Replace"), "CTRL+h", "iconReplace.png");
            case "replaceAllButton":
                return new StyleData("replaceAllButton", Languages.message("ReplaceAll"), "CTRL+w", "iconReplaceAll.png");
            case "withdrawButton":
                return new StyleData("withdrawButton", Languages.message("Withdraw"), "ESC / CTRL+w / ALT+w", "iconWithdraw.png");
            case "insertFilesButton":
                return new StyleData("insertFilesButton", Languages.message("InsertFiles"), "", "iconFileInsert.png");
            case "insertDirectoryButton":
                return new StyleData("insertDirectoryButton", Languages.message("InsertDirectory"), "", "iconFolderInsert.png");
            case "openTargetButton":
                return new StyleData("openTargetButton", Languages.message("Open"), "", "iconOpen.png");
            case "browseButton":
                return new StyleData("browseButton", Languages.message("Browse"), "", "iconBrowse.png");
            case "mirrorHButton":
                return new StyleData("mirrorHButton", Languages.message("MirrorHorizontal"), "", "iconHorizontal.png");
            case "mirrorVButton":
                return new StyleData("mirrorVButton", Languages.message("MirrorVertical"), "", "iconVertical.png");
            case "shearButton":
                return new StyleData("shearButton", Languages.message("Shear"), "", "iconShear.png");
            case "setAllOrSelectedButton":
                return new StyleData(id, Languages.message("SetAllOrSelected"), "", "iconEqual.png");
            case "testButton":
                return new StyleData("testButton", Languages.message("Test"), "", "iconGo.png");
            case "openForeImageButton":
                return new StyleData("openForeImageButton", Languages.message("Select"), "", "iconOpen.png");
            case "foreImagePaneSizeButton":
                return new StyleData("foreImagePaneSizeButton", Languages.message("PaneSize"), "", "iconPaneSize.png");
            case "foreImageImageSizeButton":
                return new StyleData("foreImageImageSizeButton", Languages.message("ImageSize"), "", "iconLoadSize.png");
            case "openBackImageButton":
                return new StyleData("openBackImageButton", Languages.message("Select"), "", "iconOpen.png");
            case "backImagePaneSizeButton":
                return new StyleData("backImagePaneSizeButton", Languages.message("PaneSize"), "", "iconPaneSize.png");
            case "backImageImageSizeButton":
                return new StyleData("backImageImageSizeButton", Languages.message("ImageSize"), "", "iconLoadSize.png");
            case "openWindowButton":
                return new StyleData("newWindowButton", Languages.message("OpenInNewWindow"), "", "iconWindow.png");
            case "newTabButton":
                return new StyleData(id, Languages.message("NewTab"), "", "iconPlus.png");
            case "strightButton":
                return new StyleData("strightButton", Languages.message("Straighten"), "", "iconRefresh.png");
            case "activeButton":
                return new StyleData("activeButton", Languages.message("Active"), "", "iconActive.png");
            case "inactiveButton":
                return new StyleData("inactiveButton", Languages.message("Inactive"), "", "iconInactive.png");
            case "thumbsListButton":
                return new StyleData("thumbsListButton", Languages.message("ThumbnailsList"), "", "iconThumbsList.png");
            case "filesListButton":
                return new StyleData("filesListButton", Languages.message("FilesList"), "", "iconList.png");
            case "gridButton":
                return new StyleData(id, Languages.message("Grid"), "", "iconBrowse.png");
            case "snapshotButton":
                return new StyleData("snapshotButton", Languages.message("Snapshot"), "", "iconSnapshot.png");
            case "splitButton":
                return new StyleData("splitButton", Languages.message("Split"), "", "iconSplit.png");
            case "sampleButton":
                return new StyleData("sampleButton", Languages.message("Sample"), "", "iconSample.png");
            case "calculatorButton":
                return new StyleData("calculatorButton", Languages.message("PixelsCalculator"), "", "iconCalculator.png");
            case "filterButton":
                return new StyleData("filterButton", Languages.message("Filter"), "", "iconFilter.png");
            case "locateLineButton":
                return new StyleData("locateLineButton", Languages.message("Go"), "", "iconGo.png");
            case "locateObjectButton":
                return new StyleData("locateObjectButton", Languages.message("Go"), "", "iconGo.png");
            case "pauseButton":
                return new StyleData("pauseButton", Languages.message("Pause"), "", "iconPause.png");
            case "pixSelectButton":
                return new StyleData("pixSelectButton", Languages.message("Select"), "", "iconOpen.png");
            case "loadButton":
                return new StyleData("loadButton", Languages.message("Load"), "", "iconGo.png");
            case "increaseButton":
                return new StyleData("increaseButton", Languages.message("Increase"), "", "iconPlus.png");
            case "decreaseButton":
                return new StyleData("decreaseButton", Languages.message("Decrease"), "", "iconMinus.png");
            case "invertButton":
                return new StyleData("invertButton", Languages.message("Invert"), "", "iconInvert.png");
            case "originalButton":
                return new StyleData("originalButton", Languages.message("OriginalSize"), "", "iconOriginalSize.png");
            case "suggestButton":
                return new StyleData("suggestButton", Languages.message("SuggestedSettings"), "", "iconIdea.png");
            case "originalImageButton":
                return new StyleData("originalImageButton", Languages.message("OriginalImage"), "", "iconPhoto.png");
            case "xmlButton":
                return new StyleData("xmlButton", "XML", "", "iconXML.png");
            case "refreshHeaderButton":
                return new StyleData("refreshHeaderButton", Languages.message("Refresh"), "", "iconRefresh.png");
            case "refreshXmlButton":
                return new StyleData("refreshXmlButton", Languages.message("Refresh"), "", "iconRefresh.png");
            case "refreshCieDataButton":
                return new StyleData("refreshCieDataButton", Languages.message("Refresh"), "", "iconRefresh.png");
            case "validateButton":
                return new StyleData("validateButton", Languages.message("Validate"), "", "iconView.png");
            case "calculateXYZButton":
                return new StyleData("calculateXYZButton", Languages.message("Calculate"), "", "iconCalculator.png");
            case "calculateXYButton":
                return new StyleData("calculateXYButton", Languages.message("Calculate"), "", "iconCalculator.png");
            case "calculateDisplayButton":
                return new StyleData("calculateDisplayButton", Languages.message("Display"), "", "iconGraph.png");
            case "displayDataButton":
                return new StyleData("displayDataButton", Languages.message("Display"), "", "iconGraph.png");
            case "valueOkButton":
                return new StyleData("valueOkButton", Languages.message("OK"), "", "iconOK.png");
            case "plusButton":
                return new StyleData("plusButton", Languages.message("Plus"), "", "iconPlus.png");
            case "minusButton":
                return new StyleData("minusButton", Languages.message("Minus"), "", "iconMinus.png");
            case "multiplyButton":
                return new StyleData("multiplyButton", Languages.message("Multiply"), "", "iconMultiply.png");
            case "calculateButton":
                return new StyleData("calculateButton", Languages.message("Calculate"), "", "iconCalculator.png");
            case "calculateAllButton":
                return new StyleData("calculateAllButton", Languages.message("Calculate"), "", "iconCalculator.png");
            case "iccSelectButton":
                return new StyleData("iccSelectButton", Languages.message("Select"), "", "iconOpen.png");
            case "ocrPathButton":
                return new StyleData("ocrPathButton", Languages.message("Select"), "", "iconOpen.png");
            case "blackwhiteButton":
                return new StyleData("blackwhiteButton", Languages.message("BlackOrWhite"), "", "iconBlackWhite.png");
            case "greyButton":
                return new StyleData("greyButton", Languages.message("Greyscale"), "", "iconGreyscale.png");
            case "setEnhanceButton":
                return new StyleData("setEnhanceButton", Languages.message("Set"), "", "iconEqual.png");
            case "setScaleButton":
                return new StyleData("setScaleButton", Languages.message("Set"), "", "iconEqual.png");
            case "setBinaryButton":
                return new StyleData("setBinaryButton", Languages.message("Set"), "", "iconEqual.png");
            case "setRotateButton":
                return new StyleData("setRotateButton", Languages.message("Set"), "", "iconEqual.png");
            case "deskewButton":
                return new StyleData("deskewButton", "", Languages.message("Deskew"), "", "iconShear.png");
            case "moveUpButton":
                return new StyleData("moveUpButton", Languages.message("MoveUp"), "", "iconUp.png");
            case "moveDownButton":
                return new StyleData("moveDownButton", Languages.message("MoveDown"), "", "iconDown.png");
            case "moveTopButton":
                return new StyleData("moveTopButton", Languages.message("MoveTop"), "", "iconDoubleUp.png");
            case "zoomIn2Button":
                return new StyleData("zoomIn2Button", Languages.message("ZoomIn"), "", "iconZoomIn.png");
            case "zoomOut2Button":
                return new StyleData("zoomOut2Button", Languages.message("ZoomOut"), "", "iconZoomOut.png");
            case "imageSize2Button":
                return new StyleData("imageSize2Button", Languages.message("LoadedSize"), "", "iconLoadSize.png");
            case "paneSize2Button":
                return new StyleData("paneSize2Button", Languages.message("PaneSize"), "", "iconPaneSize.png");
            case "pagePreviousButton":
                return new StyleData("pagePreviousButton", Languages.message("PreviousPage"), "ALT+PAGE_UP", "iconPrevious.png");
            case "pageNextButton":
                return new StyleData("pageNextButton", Languages.message("NextPage"), "ALT+PAGE_DOWN", "iconNext.png");
            case "pageFirstButton":
                return new StyleData("pageFirstButton", Languages.message("FirstPage"), "ALT+HOME", "iconFirst.png");
            case "pageLastButton":
                return new StyleData("pageLastButton", Languages.message("LastPage"), "ALT+END", "iconLast.png");
            case "allButton":
                return new StyleData("allButton", Languages.message("All"), "CTRL+a", "iconCheckAll.png");
            case "refreshHtmlButton":
                return new StyleData("refreshHtmlButton", Languages.message("Refresh"), "", "iconRefresh.png");
            case "refreshTextButton":
                return new StyleData("refreshTextButton", Languages.message("Refresh"), "", "iconRefresh.png");
            case "refreshMarkdownButton":
                return new StyleData("refreshMarkdownButton", Languages.message("Refresh"), "", "iconRefresh.png");
            case "streamMediaButton":
                return new StyleData(id, Languages.message("StreamMedia"), "", "iconLink.png");
            case "helpMeButton":
                return new StyleData(id, Languages.message("HelpMe"), "", "iconCatFoot.png");
            case "pickColorButton":
                return new StyleData(id, Languages.message("PickColor"), Languages.message("ColorPickerComments"), "", "iconPickColor.png");
            case "locationButton":
                return new StyleData(id, Languages.message("Locate"), "", "iconLocation.png");
            case "footButton":
                return new StyleData(id, Languages.message("Footprints"), "", "iconCatFoot.png");
            case "chinaButton":
                return new StyleData(id, Languages.message("China"), "", "iconChina.png");
            case "globalButton":
                return new StyleData(id, Languages.message("Global"), "", "iconGlobal.png");
            case "rowDeleteButton":
                return new StyleData(id, Languages.message("Delete"), "", "iconRowDelete.png");
            default:
                return null;
        }
    }

}
