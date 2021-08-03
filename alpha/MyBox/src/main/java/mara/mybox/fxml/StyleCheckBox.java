package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StyleCheckBox {

    public static StyleData checkBoxStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("coordinate")) {
            return new StyleData(id, "", Languages.message("Coordinate"), "", "iconLocation.png");
        }
        switch (id) {
            case "tableSubdirCheck":
                return new StyleData("tableSubdirCheck", "", Languages.message("HandleSubDirectories"), "", "iconFolder.png");
            case "tableExpandDirCheck":
                return new StyleData("tableExpandDirCheck", "", Languages.message("ExpandDirectories"), "", "iconUnfold.png");
            case "tableCreateDirCheck":
                return new StyleData("tableCreateDirCheck", "", Languages.message("CreateDirectories"), "", "iconFolderLink.png");
            case "countDirCheck":
                return new StyleData("countDirCheck", "", Languages.message("CountFilesUnderFolders"), "", "iconFolderBrowse.png");
            case "tableThumbCheck":
                return new StyleData("tableThumbCheck", "", Languages.message("Thumbnail"), "", "iconThumbsList.png");
            case "miaoCheck":
                return new StyleData("miaoCheck", Languages.message("Meow"), Languages.message("MiaoPrompt"), "", "iconCat.png");
            case "pdfMemBox":
                return new StyleData("pdfMemBox", "", Languages.message("PdfMemComments"), "", "");
            case "openCheck":
                return new StyleData("openCheck", "", Languages.message("OpenWhenComplete"), "", "iconOpen2.png");
            case "selectAreaCheck":
                return new StyleData("selectAreaCheck", "", Languages.message("SelectArea"), "CTRL+t / ALT+t", "iconTarget.png");
            case "openSaveCheck":
                return new StyleData("openSaveCheck", "", Languages.message("OpenAfterSave"), "", "iconOpen2.png");
            //            case "deleteConfirmCheck":
            //                return new StyleData("deleteConfirmCheck", "", message("ConfirmWhenDelete"), "", "iconConfirm.png");
            case "bookmarksCheck":
                return new StyleData("bookmarksCheck", "", Languages.message("Bookmarks"), "", "iconTree.png");
            case "thumbCheck":
                return new StyleData("thumbCheck", "", Languages.message("Thumbnails"), "", "iconBrowse.png");
            case "rulerXCheck":
                return new StyleData("rulerXCheck", "", Languages.message("RulerX"), "", "iconXRuler.png");
            case "rulerYCheck":
                return new StyleData("rulerYCheck", "", Languages.message("RulerY"), "", "iconYRuler.png");
            case "statisticCheck":
                return new StyleData("statisticCheck", "", Languages.message("Statistic"), "", "iconStatistic.png");
            case "transparentBackgroundCheck":
                return new StyleData(id, "", Languages.message("TransparentBackground"), "", "iconOpacity.png");
            case "transparentCheck":
                return new StyleData(id, "", Languages.message("CountTransparent"), "", "iconOpacity.png");
            case "displaySizeCheck":
                return new StyleData("displaySizeCheck", "", Languages.message("DisplaySize"), "", "iconIdea.png");
            case "topCheck":
                return new StyleData("topCheck", "", Languages.message("AlwayOnTop"), "", "iconTop.png");
            case "saveCloseCheck":
                return new StyleData("saveCloseCheck", "", Languages.message("CloseAfterHandled"), "", "iconFlower.png");
            case "deskewCheck":
                return new StyleData("deskewCheck", "", Languages.message("Deskew"), "", "iconShear.png");
            case "invertCheck":
                return new StyleData("invertCheck", "", Languages.message("Invert"), "", "iconInvert.png");
            case "popCheck":
                return new StyleData(id, Languages.message("Pop"), "", "iconPop.png");
            case "pickColorCheck":
                return new StyleData(id, Languages.message("PickColor"), Languages.message("ColorPickerComments"), "CTRL+k / ALT+k", "iconPickColor.png");
            case "ditherCheck":
                return new StyleData(id, Languages.message("DitherComments"), "", "");
            case "withNamesCheck":
            case "sourceWithNamesCheck":
            case "targetWithNamesCheck":
                return new StyleData(id, "", Languages.message("FirstLineAsNamesComments"), "", "");
            default:
                return null;
        }
    }

}
