package mara.mybox.fxml.style;

import static mara.mybox.value.Languages.message;

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
        StyleData data = match(id);
        if (data != null) {
            return data;
        }
        return startsWith(id);

    }

    private static StyleData match(String id) {
        switch (id) {
            case "tableThumbCheck":
                return new StyleData("tableThumbCheck", "", message("Thumbnail"), "", "iconThumbsList.png");
            case "openCheck":
                return new StyleData("openCheck", "", message("OpenWhenComplete"), "", "iconOpenPath.png");
            case "bookmarksCheck":
                return new StyleData("bookmarksCheck", "", message("Bookmarks"), "", "iconTree.png");
            case "thumbCheck":
                return new StyleData("thumbCheck", "", message("Thumbnails"), "", "iconBrowse.png");
            case "rulerXCheck":
                return new StyleData("rulerXCheck", "", message("Rulers"), "", "iconXRuler.png");
            case "gridCheck":
                return new StyleData(id, message("GridLines"), "", "iconGrid.png");
            case "statisticCheck":
                return new StyleData("statisticCheck", "", message("Statistic"), "", "iconStatistic.png");
            case "transparentBackgroundCheck":
                return new StyleData(id, message("TransparentBackground"), "", "iconOpacity.png");
            case "transparentCheck":
                return new StyleData(id, message("CountTransparent"), "", "iconOpacity.png");
            case "displaySizeCheck":
                return new StyleData("displaySizeCheck", "", message("DisplaySize"), "", "iconNumber.png");
            case "closeAfterCheck":
                return new StyleData(id, message("CloseAfterHandled"), "", "iconClose.png");
            case "deskewCheck":
                return new StyleData("deskewCheck", "", message("Deskew"), "", "iconShear.png");
            case "invertCheck":
                return new StyleData("invertCheck", "", message("Invert"), "", "iconInvert.png");
            case "pickColorCheck":
                return new StyleData(id, message("PickColor"), "CTRL+K / ALT+K", "iconPickColor.png");
            case "ditherCheck":
                return new StyleData(id, message("DitherComments"), "", "");
            case "withNamesCheck":
            case "sourceWithNamesCheck":
            case "targetWithNamesCheck":
                return new StyleData(id, message("FirstLineAsNamesComments"), "", "");
            case "clearDataWhenLoadImageCheck":
                return new StyleData(id, message("ClearDataWhenLoadImage"), "", "iconClear.png");
        }
        return null;
    }

    private static StyleData startsWith(String id) {
        if (id.startsWith("leftPane")) {
            return new StyleData(id, message("ControlLeftPane"), "", "iconDoubleLeft.png");
        }
        if (id.startsWith("rightPane")) {
            return new StyleData(id, message("ControlRightPane"), "", "iconDoubleRight.png");
        }
        if (id.startsWith("contextMenu")) {
            return new StyleData(id, message("ContextMenu"), "", "iconMenu.png");
        }
        if (id.startsWith("toolbar")) {
            return new StyleData(id, message("Toolbar"), "", "iconPanes.png");
        }
        if (id.startsWith("tips")) {
            return new StyleData(id, message("Tips"), "", "iconTips.png");
        }
        if (id.startsWith("handleTransparent")) {
            return new StyleData(id, message("HandleTransparent"), "", "iconOpacity.png");
        }
        if (id.startsWith("eightNeighbor")) {
            return new StyleData(id, message("EightNeighborCheckComments"), "", "");
        }
        if (id.startsWith("openPath")) {
            return new StyleData(id, message("OpenDirectory"), "", "iconOpenPath.png");
        }
        if (id.startsWith("wrap")) {
            return new StyleData(id, message("Wrap"), "", "iconWrap.png");
        }
        if (id.startsWith("editable")) {
            return new StyleData(id, message("Editable"), "", "iconEdit.png");
        }
        if (id.startsWith("childWindow")) {
            return new StyleData(id, message("ChildWindowTips"), "", "iconWindow.png");
        }
        if (id.startsWith("scope")) {
            if (id.startsWith("scopeExclude")) {
                return new StyleData(id, message("ScopeExclude"), "", "iconInvert.png");
            }
            if (id.startsWith("scopeOutlineKeepRatio")) {
                return new StyleData(id, message("KeepRatio"), "", "iconAspectRatio.png");
            }
            return new StyleData(id, message("Scope"), "", "iconTarget.png");
        }
        if (id.startsWith("csv")) {
            return new StyleData(id, "CSV", "", "iconCSV.png");
        }
        if (id.startsWith("excel")) {
            return new StyleData(id, "Excel", "", "iconExcel.png");
        }
        if (id.startsWith("texts")) {
            return new StyleData(id, message("Texts"), "", "iconTxt.png");
        }
        if (id.startsWith("matrix")) {
            return new StyleData(id, message("Matrix"), "", "iconMatrix.png");
        }
        if (id.startsWith("database")) {
            return new StyleData(id, message("DatabaseTable"), "", "iconDatabase.png");
        }
        if (id.startsWith("systemClipboard")) {
            return new StyleData(id, message("SystemClipboard"), "", "iconSystemClipboard.png");
        }
        if (id.startsWith("myBoxClipboard")) {
            return new StyleData(id, message("MyBoxClipboard"), "", "iconClipboard.png");
        }
        if (id.startsWith("html")) {
            return new StyleData(id, "Html", "", "iconHtml.png");
        }
        if (id.startsWith("xml")) {
            return new StyleData(id, "XML", "", "iconXML.png");
        }
        if (id.startsWith("pdf")) {
            if (id.equals("pdfMemBox")) {
                return new StyleData("pdfMemBox", "", message("PdfMemComments"), "", "");
            }
            return new StyleData(id, "PDF", "", "iconPDF.png");
        }
        if (id.startsWith("json")) {
            return new StyleData(id, "json", "", "iconJSON.png");
        }
        if (id.startsWith("coordinate")) {
            return new StyleData(id, message("Coordinate"), "", "iconLocation.png");
        }
        if (id.startsWith("refreshSwitch")) {
            return new StyleData(id, message("RefreshWhenSwitch"), "", "iconRefreshSwitch.png");
        }
        if (id.startsWith("refreshChange")) {
            return new StyleData(id, message("RefreshWhenChange"), "", "iconRefresh.png");
        }
        if (id.startsWith("nodesList")) {
            return new StyleData(id, message("List"), "", "iconList.png");
        }
        if (id.startsWith("verify")) {
            return new StyleData(id, message("Validate"), "", "iconVerify.png");
        }
        if (id.startsWith("miao")) {
            return new StyleData(id, message("Meow"), message("MiaoPrompt"), "", "iconCat.png");
        }
        if (id.startsWith("typesetting")) {
            return new StyleData(id, message("TypesettingWhenWrite"), "", "iconTypesetting.png");
        }
        if (id.startsWith("lostFocusCommit")) {
            return new StyleData(id, message("CommitModificationWhenDataCellLoseFocusComments"), "", "iconInput.png");
        }
        if (id.startsWith("pop")) {
            switch (id) {
                case "popAnchorMenuCheck":
                    return new StyleData(id, message("PopAnchorMenu"), "", "iconShape.png");
                case "popLineMenuCheck":
                    return new StyleData(id, message("PopLineMenu"), "", "iconShape.png");
                default:
                    return new StyleData(id, message("Pop"), "", "iconPop.png");
            }
        }
        if (id.startsWith("anchor")) {
            return new StyleData(id, message("ShowAnchors"), "", "iconAnchor.png");
        }
        if (id.startsWith("addPoint")) {
            return new StyleData(id, message("AddPointWhenLeftClick"), "", "iconNewItem.png");
        }
        return null;
    }

}
