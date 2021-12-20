package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;

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
                    return new StyleData("okButton", message("OK"), "F1 / CTRL+e / ALT+e", "iconOK.png");
                default:
                    return new StyleData(id, message("OK"), "", "iconOK.png");
            }
        }
        if (id.startsWith("start")) {
            switch (id) {
                case "startButton":
                    return new StyleData(id, message("Start"), "F1 / CTRL+e / ALT+e", "iconStart.png");
                default:
                    return new StyleData(id, message("Start"), "", "iconStart.png");
            }
        }
        if (id.startsWith("saveAs")) {
            switch (id) {
                case "saveAsButton":
                    return new StyleData(id, message("SaveAs"), "F11", "iconSaveAs.png");
                default:
                    return new StyleData(id, message("SaveAs"), "", "iconSaveAs.png");
            }
        }
        if (id.startsWith("save")) {
            switch (id) {
                case "saveButton":
                    return new StyleData(id, message("Save"), "F2 / CTRL+s / ALT+s", "iconSave.png");
                case "saveImagesButton":
                    return new StyleData(id, message("SaveAsImages"), message("SaveAsImages") + "\n" + message("FilePrefixInput"), "", "");
                case "saveTiffButton":
                    return new StyleData(id, message("SaveAsTiff"), "", "iconTIF.png");
                case "savePdfButton":
                    return new StyleData(id, message("SaveAsPDF"), "", "iconPDF.png");
                default:
                    return new StyleData(id, message("Save"), "", "iconSave.png");
            }
        }
        if (id.startsWith("create")) {
            switch (id) {
                case "createButton":
                    return new StyleData(id, message("Create"), "CTRL+n", "iconAdd.png");
                case "createDataButton":
                    return new StyleData(id, message("CreateData"), "", "iconAdd.png");
                default:
                    return new StyleData(id, message("Create"), "", "iconAdd.png");
            }
        }
        if (id.startsWith("add")) {
            switch (id) {
                case "addButton":
                    return new StyleData(id, message("Add"), "CTRL+n", "iconAdd.png");
                case "addRowsButton":
                    return new StyleData(id, message("AddRows"), "CTRL+n", "iconNewItem.png");
                case "addFilesButton":
                    return new StyleData(id, message("AddFiles"), "", "iconFileAdd.png");
                case "addDirectoryButton":
                    return new StyleData(id, message("AddDirectory"), "", "iconFolderAdd.png");
                case "addMenuButton":
                    return new StyleData(id, "", "", "iconAdd.png");
                default:
                    return new StyleData(id, message("Add"), "", "iconAdd.png");
            }
        }
        if (id.startsWith("clear")) {
            switch (id) {
                case "clearButton":
                    return new StyleData(id, message("Clear"), "CTRL+g", "iconClear.png");
                default:
                    return new StyleData(id, message("Clear"), "", "iconClear.png");
            }
        }
        if (id.startsWith("plus")) {
            return new StyleData(id, message("Add"), "", "iconPlus.png");
        }
        if (id.startsWith("query")) {
            return new StyleData(id, message("Query"), "", "iconQuery.png");
        }
        if (id.startsWith("reset")) {
            return new StyleData(id, message("Reset"), "", "iconRecover.png");
        }
        if (id.startsWith("analyse")) {
            return new StyleData(id, message("Analyse"), "", "iconAnalyse.png");
        }
        if (id.startsWith("ocr")) {
            return new StyleData(id, message("OCR"), "", "iconAnalyse.png");
        }
        if (id.startsWith("yes")) {
            return new StyleData(id, message("Yes"), "", "iconYes.png");
        }
        if (id.startsWith("confirm")) {
            return new StyleData(id, message("Confirm"), "", "iconYes.png");
        }
        if (id.startsWith("complete")) {
            return new StyleData(id, message("Complete"), "", "iconYes.png");
        }
        if (id.startsWith("selectFile")) {
            return new StyleData(id, message("Select"), "", "iconOpen.png");
        }
        if (id.startsWith("selectPath")) {
            return new StyleData(id, message("Select"), "", "iconFolder.png");
        }
        if (id.startsWith("mybox")) {
            return new StyleData(id, "MyBox", "", "iconMyBox.png");
        }
        if (id.startsWith("download")) {
            return new StyleData(id, message("Download"), "", "iconDownload.png");
        }
        if (id.startsWith("default")) {
            return new StyleData(id, message("Default"), "", "iconDefault.png");
        }
        if (id.startsWith("random")) {
            return new StyleData(id, message("Random"), "", "iconRandom.png");
        }
        if (id.startsWith("example")) {
            return new StyleData(id, message("Example"), "", "iconExamples.png");
        }
        if (id.startsWith("sql")) {
            return new StyleData(id, "SQL", "", "iconSQL.png");
        }
        if (id.startsWith("charts")) {
            return new StyleData(id, message("Charts"), "", "iconCharts.png");
        }
        if (id.startsWith("recover")) {
            switch (id) {
                case "recoverButton":
                    return new StyleData("recoverButton", message("Recover"), "F3 / CTRL+r / ALT+r", "iconRecover.png");
                case "recoveryAllButton":
                    return new StyleData("recoveryAllButton", message("RecoverAll"), "", "iconRecover.png");
                case "recoverySelectedButton":
                    return new StyleData("recoverySelectedButton", message("RecoverSelected"), "", "iconFileRestore.png");
                default:
                    return new StyleData(id, message("Recover"), "", "iconRecover.png");
            }
        }
        if (id.startsWith("zoomIn")) {
            switch (id) {
                case "zoomInButton":
                    return new StyleData(id, message("ZoomIn"), "CTRL+3", "iconZoomIn.png");
                default:
                    return new StyleData(id, message("ZoomIn"), "", "iconZoomIn.png");
            }
        }
        if (id.startsWith("zoomOut")) {
            switch (id) {
                case "zoomOutButton":
                    return new StyleData(id, message("ZoomOut"), "CTRL+4", "iconZoomOut.png");
                default:
                    return new StyleData(id, message("ZoomOut"), "", "iconZoomOut.png");
            }
        }
        if (id.startsWith("copyToSystemClipboard")) {
            return new StyleData(id, message("CopyToSystemClipboard"), "", "iconCopySystem.png");
        }
        if (id.startsWith("copyToMyBoxClipboard")) {
            return new StyleData(id, message("CopyToMyBoxClipboard"), "", "iconCopy.png");
        }
        if (id.startsWith("CopyToClipboards")) {
            return new StyleData(id, message("CopyToClipboards"), "", "iconCopy.png");
        }
        if (id.startsWith("copy")) {
            switch (id) {
                case "copyButton":
                    return new StyleData(id, message("Copy"), "CTRL+c / ALT+c ", "iconCopy.png");
                case "copyAsAButton":
                    return new StyleData("copyAsAButton", message("CopyAsMatrixA"), "", "iconCopyA.png");
                case "copyAsBButton":
                    return new StyleData("copyAsAButton", message("CopyAsMatrixB"), "", "iconCopyB.png");
                case "copyEnglishButton":
                    return new StyleData(id, message("CopyEnglish"), "CTRL+e / ALT+e ", "iconCopy.png");
                default:
                    return new StyleData(id, message("Copy"), "", "iconCopy.png");
            }
        }
        if (id.startsWith("paste")) {
            switch (id) {
                case "pasteButton":
                    return new StyleData(id, message("Paste"), "CTRL+v / ALT+v", "iconPaste.png");
                case "pasteTxtButton":
                    return new StyleData(id, message("PasteTextAsHtml"), "", "iconPaste.png");
                case "pasteContentInSystemClipboardButton":
                    return new StyleData(id, message("PasteContentInSystemClipboard"), "", "iconPasteSystem.png");
                case "pasteContentInDataClipboardButton":
                    return new StyleData(id, message("PasteContentInDataClipboard"), "", "iconPaste.png");
                default:
                    return new StyleData(id, message("Paste"), "", "iconPaste.png");
            }
        }
        if (id.startsWith("next")) {
            switch (id) {
                case "nextButton":
                    return new StyleData(id, message("Next"), "PAGE DOWN", "iconNext.png");
                default:
                    return new StyleData(id, message("Next"), "", "iconNext.png");
            }
        }
        if (id.startsWith("previous")) {
            switch (id) {
                case "previousButton":
                    return new StyleData(id, message("Previous"), "PAGE UP", "iconPrevious.png");
                default:
                    return new StyleData(id, message("Previous"), "", "iconPrevious.png");
            }
        }
        if (id.startsWith("backward")) {
            return new StyleData(id, message("Backward"), "", "iconPrevious.png");
        }
        if (id.startsWith("forward")) {
            return new StyleData(id, message("Forward"), "", "iconNext.png");
        }
        if (id.startsWith("more")) {
            return new StyleData(id, message("More"), "", "iconMore.png");
        }
        if (id.startsWith("palette")) {
            switch (id) {
                case "paletteManageButton":
                    return new StyleData(id, message("ColorPaletteManage"), "", "iconPalette.png");
                default:
                    return new StyleData(id, message("ColorPalette"), "", "iconPalette.png");
            }
        }
        if (id.startsWith("color")) {
            return new StyleData(id, message("ColorPalette"), "", "iconColor.png");
        }
        if (id.startsWith("open")) {
            if (id.startsWith("openPath")) {
                return new StyleData(id, message("Directory"), "", "iconOpen2.png");
            }
            switch (id) {
                case "openWindowButton":
                    return new StyleData(id, message("OpenInNewWindow"), "", "iconWindow.png");
                default:
                    return new StyleData(id, message("Open"), "", "iconOpen.png");
            }
        }
        if (id.startsWith("delete")) {
            switch (id) {
                case "deleteButton":
                    return new StyleData("deleteButton", message("Delete"), "DELETE / CTRL+d / ALT+d", "iconDelete.png");
                default:
                    return new StyleData(id, message("Delete"), "", "iconDelete.png");
            }
        }
        if (id.startsWith("input")) {
            return new StyleData(id, message("Input"), "", "iconData.png");
        }
        if (id.startsWith("dataImport") || id.startsWith("import")) {
            return new StyleData(id, message("Import"), "", "iconImport.png");
        }
        if (id.startsWith("dataExport") || id.startsWith("export")) {
            return new StyleData(id, message("Export"), "", "iconExport.png");
        }
        if (id.startsWith("data")) {
            return new StyleData(id, message("Data"), "", "iconData.png");
        }
        if (id.startsWith("query")) {
            return new StyleData(id, message("Query"), "", "iconData.png");
        }
        if (id.startsWith("map")) {
            return new StyleData(id, message("Map"), "", "iconMap.png");
        }
        if (id.startsWith("dataset")) {
            return new StyleData(id, message("DataSet"), "", "iconDataset.png");
        }
        if (id.startsWith("sureButton")) {
            return new StyleData(id, message("Sure"), "", "iconYes.png");
        }
        if (id.startsWith("fill")) {
            return new StyleData(id, message("Fill"), "", "iconButterfly.png");
        }
        if (id.startsWith("cat")) {
            return new StyleData(id, message("Meow"), "", "iconCat.png");
        }
        if (id.startsWith("edit")) {
            switch (id) {
                case "editOkButton":
                    return new StyleData(id, message("OK"), "", "iconOK.png");
                default:
                    return new StyleData(id, message("Edit"), "", "iconEdit.png");
            }
        }
        if (id.startsWith("size")) {
            return new StyleData(id, message("Size"), "", "iconSplit.png");
        }
        if (id.startsWith("selectImage")) {
            return new StyleData(id, message("Image"), "", "iconFolderImage.png");
        }
        if (id.startsWith("equal")) {
            return new StyleData(id, message("EqualTo"), "", "iconEqual.png");
        }
        if (id.startsWith("selectAll")) {
            switch (id) {
                case "selectAllButton":
                    return new StyleData(id, message("SelectAll"), "CTRL+a / ALT+a", "iconSelectAll.png");
                default:
                    return new StyleData(id, message("SelectAll"), "", "iconSelectAll.png");
            }
        }
        if (id.startsWith("selectNone")) {
            switch (id) {
                case "selectNoneButton":
                    return new StyleData(id, message("UnselectAll"), "CTRL+o / ALT+O", "iconSelectNone.png");
                default:
                    return new StyleData(id, message("UnselectAll"), "", "iconSelectNone.png");
            }
        }
        if (id.startsWith("use")) {
            return new StyleData(id, message("Use"), "", "iconYes.png");
        }
        if (id.startsWith("refresh")) {
            return new StyleData(id, message("Refresh"), "", "iconRefresh.png");
        }
        if (id.startsWith("giveUp")) {
            return new StyleData(id, message("GiveUp"), "", "iconCatFoot.png");
        }
        if (id.startsWith("manufacture")) {
            return new StyleData(id, message("Manufacture"), "", "iconEdit.png");
        }
        if (id.startsWith("run")) {
            return new StyleData(id, message("Run"), "", "iconRun.png");
        }
        if (id.startsWith("info")) {
            switch (id) {
                case "infoButton":
                    return new StyleData(id, message("Information"), "CTRL+i", "iconInfo.png");
                default:
                    return new StyleData(id, message("Information"), "", "iconInfo.png");
            }
        }
        if (id.startsWith("view")) {
            return new StyleData(id, message("View"), "", "iconView.png");
        }
        if (id.startsWith("html")) {
            return new StyleData(id, message("Html"), "", "iconHtml.png");
        }
        if (id.startsWith("link")) {
            return new StyleData(id, message("Link"), "", "iconLink.png");
        }
        if (id.startsWith("stop")) {
            return new StyleData(id, message("Stop"), "", "iconStop.png");
        }
        if (id.startsWith("synchronize")) {
            return new StyleData(id, message("SynchronizeChangesToOtherPanes"), "F10", "iconSynchronize.png");
        }
        if (id.startsWith("function")) {
            return new StyleData(id, "", "", "iconFunction.png");
        }
        if (id.startsWith("style")) {
            return new StyleData(id, message("Style"), "", "iconStyle.png");
        }
        if (id.startsWith("paneSize")) {
            switch (id) {
                case "paneSizeButton":
                    return new StyleData(id, message("PaneSize"), "CTRL+2", "iconPaneSize.png");
                default:
                    return new StyleData(id, message("PaneSize"), "", "iconPaneSize.png");
            }
        }
        if (id.startsWith("panes")) {
            return new StyleData(id, message("Panes"), "", "iconPanes.png");
        }
        if (id.startsWith("extract")) {
            return new StyleData(id, message("Extract"), "", "iconExtract.png");
        }
        if (id.startsWith("demo")) {
            return new StyleData(id, message("Demo"), "", "iconDemo.png");
        }
        if (id.startsWith("count")) {
            return new StyleData(id, message("Count"), "", "iconCalculator.png");
        }
        if (id.startsWith("delimiter")) {
            return new StyleData(id, "", message("Delimiter"), "iconDelimiter.png");
        }
        if (id.startsWith("comma")) {
            return new StyleData(id, message("Comma"), "", "iconDelimiter.png");
        }
        if (id.startsWith("matrixA")) {
            return new StyleData(id, message("SetAsMatrixA"), "", "iconA.png");
        }
        if (id.startsWith("matrixB")) {
            return new StyleData(id, message("SetAsMatrixB"), "", "iconB.png");
        }
        if (id.startsWith("width")) {
            return new StyleData(id, message("Width"), "", "iconXRuler.png");
        }
        if (id.startsWith("go")) {
            return new StyleData(id, message("Go"), "", "iconGo.png");
        }
        if (id.startsWith("preview")) {
            return new StyleData(id, message("PreviewComments"), "", "iconPreview.png");
        }
        if (id.startsWith("rotateLeft")) {
            return new StyleData(id, message("RotateLeft"), "", "iconRotateLeft.png");
        }
        if (id.startsWith("rotateRight")) {
            return new StyleData(id, message("RotateRight"), "", "iconRotateRight.png");
        }
        if (id.startsWith("turnOver")) {
            return new StyleData(id, message("TurnOver"), "", "iconTurnOver.png");
        }
        if (id.startsWith("rename")) {
            return new StyleData(id, message("Rename"), "", "iconRename.png");
        }
        if (id.startsWith("header")) {
            return new StyleData(id, "", "", "iconHeader.png");
        }
        if (id.startsWith("list")) {
            return new StyleData(id, message("List"), "", "iconList.png");
        }
        if (id.startsWith("codes")) {
            return new StyleData(id, "", "", "iconMeta.png");
        }
        if (id.startsWith("fold")) {
            return new StyleData(id, message("Fold"), "", "iconMinus.png");
        }
        if (id.startsWith("unford")) {
            return new StyleData(id, message("Unfold"), "", "iconTree.png");
        }
        if (id.startsWith("moveData")) {
            return new StyleData(id, message("Move"), "", "iconRef.png");
        }
        if (id.startsWith("csv")) {
            return new StyleData(id, "CSV", "", "iconCSV.png");
        }
        if (id.startsWith("excel")) {
            return new StyleData(id, "Excel", "", "iconExcel.png");
        }
        if (id.startsWith("history")) {
            return new StyleData(id, message("History"), "", "iconHistory.png");
        }
        if (id.startsWith("ssl")) {
            return new StyleData(id, "SSL", "", "iconSSL.png");
        }
        if (id.startsWith("ignore")) {
            return new StyleData(id, message("Ignore"), "", "iconIgnore.png");
        }
        if (id.startsWith("github")) {
            return new StyleData(id, "github", "", "iconGithub.png");
        }
        if (id.startsWith("txt")) {
            return new StyleData(id, message("Texts"), "", "iconTxt.png");
        }
        if (id.startsWith("clipboard")) {
            return new StyleData(id, message("Clipboard"), "", "iconClipboard.png");
        }
        if (id.startsWith("myBoxClipboard")) {
            return new StyleData(id, message("MyBoxClipboard"), "CTRL+m", "iconClipboard.png");
        }
        if (id.startsWith("systemClipboard")) {
            return new StyleData(id, message("SystemClipboard"), "CTRL+j", "iconSystemClipboard.png");
        }
        if (id.startsWith("loadContentInSystemClipboard")) {
            return new StyleData(id, message("LoadContentInSystemClipboard"), "", "iconImageSystem.png");
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
        if (id.startsWith("menu")) {
            return new StyleData(id, message("Menu"), message("MenuButtonTips"), "iconMenu.png");
        }

        if (id.startsWith("closePop")) {
            return new StyleData(id, message("Close"), "ESC / F6", "iconCancel.png");
        }
        if (id.startsWith("message")) {
            return new StyleData(id, message("SendMessage"), "", "iconMessage.png");
        }
        if (id.startsWith("pop")) {
            return new StyleData(id, message("Pop"), "CTRL+p", "iconPop.png");
        }
        if (id.startsWith("play")) {
            switch (id) {
                case "playButton":
                    return new StyleData(id, message("Play"), "F1", "iconPlay.png");
                default:
                    return new StyleData(id, message("Play"), "", "iconPlay.png");
            }
        }
        if (id.startsWith("sort")) {
            return new StyleData(id, message("Sort"), "", "iconSort.png");
        }
        if (id.startsWith("minus")) {
            return new StyleData(id, message("Minus"), "", "iconMinus.png");
        }
        if (id.startsWith("calculate")) {
            return new StyleData(id, message("Calculate"), "", "iconCalculator.png");
        }
        if (id.startsWith("columnsAdd")) {
            return new StyleData(id, message("AddColumns"), "", "iconColumnAdd.png");
        }
        if (id.startsWith("columnsDelete")) {
            return new StyleData(id, message("DeleteColumns"), "", "iconColumnDelete.png");
        }
        if (id.startsWith("rowsAdd")) {
            return new StyleData(id, message("AddRows"), "", "iconRowAdd.png");
        }
        if (id.startsWith("rowsDelete")) {
            return new StyleData(id, message("DeleteRows"), "", "iconRowDelete.png");
        }
        if (id.startsWith("set")) {
            switch (id) {
                case "setButton":
                    return new StyleData(id, message("Set"), "F1", "iconEqual.png");
                case "setAllOrSelectedButton":
                    return new StyleData(id, message("SetAllOrSelected"), "", "iconEqual.png");
                case "setValuesButton":
                    return new StyleData(id, message("SetValues"), "", "iconEqual.png");
                default:
                    return new StyleData(id, message("Set"), "", "iconEqual.png");
            }
        }
        if (id.startsWith("hex")) {
            return new StyleData(id, message("FormattedHexadecimal"), "", "iconHex.png");
        }

        if (id.startsWith("location")) {
            return new StyleData(id, message("Location"), "", "iconLocation.png");
        }
        if (id.startsWith("imageSize")) {
            switch (id) {
                case "imageSizeButton":
                    return new StyleData(id, message("LoadedSize"), "CTRL+1", "iconLoadSize.png");
                default:
                    return new StyleData(id, message("LoadedSize"), "", "iconLoadSize.png");
            }
        }
        if (id.startsWith("validate")) {
            return new StyleData(id, message("Validate"), "", "iconAnalyse.png");
        }
        if (id.startsWith("moveUp")) {
            return new StyleData(id, message("MoveUp"), "", "iconUp.png");
        }
        if (id.startsWith("moveDown")) {
            return new StyleData(id, message("MoveDown"), "", "iconDown.png");
        }
        if (id.startsWith("moveLeft")) {
            return new StyleData(id, message("MoveLeft"), "", "iconLeft.png");
        }
        if (id.startsWith("moveRight")) {
            return new StyleData(id, message("MoveRight"), "", "iconRight.png");
        }
        if (id.startsWith("insert")) {
            switch (id) {
                case "insertFilesButton":
                    return new StyleData("insertFilesButton", message("InsertFiles"), "", "iconFileInsert.png");
                case "insertDirectoryButton":
                    return new StyleData("insertDirectoryButton", message("InsertDirectory"), "", "iconFolderInsert.png");
                default:
                    return new StyleData(id, message("Insert"), "", "iconInsert.png");
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
                return new StyleData(id, message("Select"), "", "iconSelect.png");
            case "unselectButton":
                return new StyleData(id, message("Unselect"), "", "iconSelectNone.png");
            case "cancelButton":
                return new StyleData("cancelButton", message("Cancel"), "ESC", "iconCancel.png");
            case "cropButton":
                return new StyleData("cropButton", message("Crop"), "CTRL+x / ALT+x", "iconCrop.png");
            case "metaButton":
                return new StyleData("metaButton", message("MetaData"), "", "iconMeta.png");
            case "pButton":
                return new StyleData(id, message("Paragraph"), "", "iconP.png");
            case "brButton":
                return new StyleData(id, message("Newline"), "", "iconBr.png");
            case "statisticButton":
                return new StyleData("statisticButton", message("Statistic"), "", "iconStatistic.png");
            case "firstButton":
                return new StyleData("firstButton", message("First"), "ALT+HOME", "iconFirst.png");
            case "lastButton":
                return new StyleData("lastButton", message("Last"), "ALT+END", "iconLast.png");
            case "redoButton":
                return new StyleData("redoButton", message("Redo"), "CTRL+y / ALT+y", "iconRedo.png");
            case "undoButton":
                return new StyleData("undoButton", message("Undo"), "CTRL+z / ALT+z", "iconUndo.png");
            case "closeButton":
                return new StyleData("closeButton", message("Close"), "F7", "iconClose.png");
            case "findNextButton":
                return new StyleData("findNextButton", message("Next"), "CTRL+2", "iconNext.png");
            case "findPreviousButton":
                return new StyleData("findPreviousButton", message("Previous"), "CTRL+1", "iconPrevious.png");
            case "findButton":
                return new StyleData(id, message("Find"), "CTRL+f", "iconFind.png");
            case "replaceButton":
                return new StyleData("replaceButton", message("Replace"), "CTRL+h", "iconReplace.png");
            case "replaceAllButton":
                return new StyleData("replaceAllButton", message("ReplaceAll"), "CTRL+w", "iconReplaceAll.png");
            case "withdrawButton":
                return new StyleData("withdrawButton", message("Withdraw"), "ESC / CTRL+w / ALT+w", "iconWithdraw.png");
            case "browseButton":
                return new StyleData("browseButton", message("Browse"), "", "iconBrowse.png");
            case "mirrorHButton":
                return new StyleData("mirrorHButton", message("MirrorHorizontal"), "", "iconHorizontal.png");
            case "mirrorVButton":
                return new StyleData("mirrorVButton", message("MirrorVertical"), "", "iconVertical.png");
            case "shearButton":
                return new StyleData("shearButton", message("Shear"), "", "iconShear.png");
            case "testButton":
                return new StyleData("testButton", message("Test"), "", "iconGo.png");

            case "newTabButton":
                return new StyleData(id, message("NewTab"), "", "iconPlus.png");
            case "strightButton":
                return new StyleData("strightButton", message("Straighten"), "", "iconRefresh.png");
            case "activeButton":
                return new StyleData("activeButton", message("Active"), "", "iconActive.png");
            case "inactiveButton":
                return new StyleData("inactiveButton", message("Inactive"), "", "iconInactive.png");
            case "thumbsListButton":
                return new StyleData("thumbsListButton", message("ThumbnailsList"), "", "iconThumbsList.png");
            case "filesListButton":
                return new StyleData("filesListButton", message("FilesList"), "", "iconList.png");
            case "gridButton":
                return new StyleData(id, message("Grid"), "", "iconBrowse.png");
            case "snapshotButton":
                return new StyleData("snapshotButton", message("Snapshot"), "", "iconSnapshot.png");
            case "splitButton":
                return new StyleData("splitButton", message("Split"), "", "iconSplit.png");
            case "sampleButton":
                return new StyleData("sampleButton", message("Sample"), "", "iconSample.png");
            case "calculatorButton":
                return new StyleData("calculatorButton", message("PixelsCalculator"), "", "iconCalculator.png");
            case "filterButton":
                return new StyleData("filterButton", message("Filter"), "", "iconFilter.png");
            case "pauseButton":
                return new StyleData("pauseButton", message("Pause"), "", "iconPause.png");
            case "pixSelectButton":
                return new StyleData("pixSelectButton", message("Select"), "", "iconOpen.png");
            case "loadButton":
                return new StyleData("loadButton", message("Load"), "", "iconGo.png");
            case "increaseButton":
                return new StyleData("increaseButton", message("Increase"), "", "iconPlus.png");
            case "decreaseButton":
                return new StyleData("decreaseButton", message("Decrease"), "", "iconMinus.png");
            case "invertButton":
                return new StyleData("invertButton", message("Invert"), "", "iconInvert.png");
            case "originalButton":
                return new StyleData("originalButton", message("OriginalSize"), "", "iconOriginalSize.png");
            case "suggestButton":
                return new StyleData("suggestButton", message("SuggestedSettings"), "", "iconIdea.png");
            case "originalImageButton":
                return new StyleData("originalImageButton", message("OriginalImage"), "", "iconPhoto.png");
            case "xmlButton":
                return new StyleData("xmlButton", "XML", "", "iconXML.png");
            case "refreshHeaderButton":
                return new StyleData("refreshHeaderButton", message("Refresh"), "", "iconRefresh.png");
            case "refreshXmlButton":
                return new StyleData("refreshXmlButton", message("Refresh"), "", "iconRefresh.png");
            case "refreshCieDataButton":
                return new StyleData("refreshCieDataButton", message("Refresh"), "", "iconRefresh.png");
            case "calculateXYZButton":
                return new StyleData("calculateXYZButton", message("Calculate"), "", "iconCalculator.png");
            case "calculateXYButton":
                return new StyleData("calculateXYButton", message("Calculate"), "", "iconCalculator.png");
            case "displayDataButton":
                return new StyleData("displayDataButton", message("Display"), "", "iconGraph.png");
            case "valueOkButton":
                return new StyleData("valueOkButton", message("OK"), "", "iconOK.png");
            case "multiplyButton":
                return new StyleData("multiplyButton", message("Multiply"), "", "iconMultiply.png");
            case "iccSelectButton":
                return new StyleData("iccSelectButton", message("Select"), "", "iconOpen.png");
            case "ocrPathButton":
                return new StyleData("ocrPathButton", message("Select"), "", "iconOpen.png");
            case "blackwhiteButton":
                return new StyleData("blackwhiteButton", message("BlackOrWhite"), "", "iconBlackWhite.png");
            case "greyButton":
                return new StyleData("greyButton", message("Greyscale"), "", "iconGreyscale.png");
            case "deskewButton":
                return new StyleData("deskewButton", "", message("Deskew"), "", "iconShear.png");
            case "moveTopButton":
                return new StyleData("moveTopButton", message("MoveTop"), "", "iconDoubleUp.png");
            case "pagePreviousButton":
                return new StyleData("pagePreviousButton", message("PreviousPage"), "ALT+PAGE_UP", "iconPrevious.png");
            case "pageNextButton":
                return new StyleData("pageNextButton", message("NextPage"), "ALT+PAGE_DOWN", "iconNext.png");
            case "pageFirstButton":
                return new StyleData("pageFirstButton", message("FirstPage"), "ALT+HOME", "iconFirst.png");
            case "pageLastButton":
                return new StyleData("pageLastButton", message("LastPage"), "ALT+END", "iconLast.png");
            case "allButton":
                return new StyleData("allButton", message("All"), "CTRL+a", "iconCheckAll.png");
            case "refreshHtmlButton":
                return new StyleData("refreshHtmlButton", message("Refresh"), "", "iconRefresh.png");
            case "refreshTextButton":
                return new StyleData("refreshTextButton", message("Refresh"), "", "iconRefresh.png");
            case "refreshMarkdownButton":
                return new StyleData("refreshMarkdownButton", message("Refresh"), "", "iconRefresh.png");
            case "streamMediaButton":
                return new StyleData(id, message("StreamMedia"), "", "iconLink.png");
            case "helpMeButton":
                return new StyleData(id, message("HelpMe"), "", "iconCatFoot.png");
            case "pickColorButton":
                return new StyleData(id, message("PickColor"), message("ColorPickerComments"), "", "iconPickColor.png");
            case "footButton":
                return new StyleData(id, message("Footprints"), "", "iconCatFoot.png");
            case "chinaButton":
                return new StyleData(id, message("China"), "", "iconChina.png");
            case "globalButton":
                return new StyleData(id, message("Global"), "", "iconGlobal.png");
            case "rowDeleteButton":
                return new StyleData(id, message("Delete"), "", "iconRowDelete.png");
            default:
                return null;
        }
    }

}
