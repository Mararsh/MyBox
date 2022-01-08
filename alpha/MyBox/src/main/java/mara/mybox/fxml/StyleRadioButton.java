package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StyleRadioButton {

    public static StyleData radioButtonStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("csv")) {
            return new StyleData(id, "", "CSV", "", "iconCSV.png");
        }
        if (id.startsWith("excel")) {
            return new StyleData(id, "", "Excel", "", "iconExcel.png");
        }
        if (id.startsWith("texts")) {
            return new StyleData(id, "", message("Texts"), "", "iconTxt.png");
        }
        if (id.startsWith("matrix")) {
            return new StyleData(id, "", message("Matrix"), "", "iconSplit.png");
        }
        if (id.startsWith("systemClipboard")) {
            return new StyleData(id, "", message("SystemClipboard"), "", "iconSystemClipboard.png");
        }
        if (id.startsWith("myBoxClipboard")) {
            return new StyleData(id, "", message("MyBoxClipboard"), "", "iconClipboard.png");
        }
        if (id.startsWith("html")) {
            return new StyleData(id, "", "Html", "", "iconHtml.png");
        }
        if (id.startsWith("xml")) {
            return new StyleData(id, "", "XML", "", "iconXML.png");
        }
        if (id.startsWith("pdf")) {
            return new StyleData(id, "", "PDF", "", "iconPDF.png");
        }
        if (id.startsWith("json")) {
            return new StyleData(id, "", "json", "", "iconJSON.png");
        }
        switch (id) {
            case "miaoRadio":
                return new StyleData(id, message("Meow"), message("MiaoPrompt"), "", "iconCat.png");
            case "polylineRadio":
                return new StyleData("polylineRadio", "", message("Polyline"), "", "iconPolyline.png");
            case "linesRadio":
                return new StyleData("linesRadio", "", message("DrawLines"), "", "iconDraw.png");
            case "rectangleRadio":
                return new StyleData("rectangleRadio", "", message("Rectangle"), "", "iconRectangle.png");
            case "circleRadio":
                return new StyleData("circleRadio", "", message("Circle"), "", "iconCircle.png");
            case "ellipseRadio":
                return new StyleData("ellipseRadio", "", message("Ellipse"), "", "iconEllipse.png");
            case "polygonRadio":
                return new StyleData("polygonRadio", "", message("Polygon"), "", "iconStar.png");
            case "eraserRadio":
                return new StyleData("eraserRadio", "", message("Eraser"), "", "iconEraser.png");
            case "mosaicRadio":
                return new StyleData("mosaicRadio", "", message("Mosaic"), "", "iconMosaic.png");
            case "frostedRadio":
                return new StyleData("frostedRadio", "", message("FrostedGlass"), "", "iconFrosted.png");
            case "shapeRectangleRadio":
                return new StyleData("shapeRectangleRadio", "", message("Rectangle"), "", "iconRectangle.png");
            case "shapeCircleRadio":
                return new StyleData("shapeCircleRadio", "", message("Circle"), "", "iconCircle.png");
            case "horizontalBarsChartRadio":
                return new StyleData(id, "", message("HorizontalBarsChart"), "", "iconBarsChartH.png");
            case "verticalBarsChartRadio":
                return new StyleData(id, "", message("VerticalBarsChart"), "", "iconBarsChart.png");
            case "linesChartRadio":
                return new StyleData(id, "", message("VerticalLinesChart"), "", "iconLinesChart.png");
            case "linesChartHRadio":
                return new StyleData(id, "", message("HorizontalLinesChart"), "", "iconLinesChartH.png");
            case "pieRadio":
                return new StyleData(id, "", message("PieChart"), "", "iconPieChart.png");
            case "mapRadio":
                return new StyleData(id, "", message("Map"), "", "iconMap.png");
            case "pcxSelect":
                return new StyleData("pcxSelect", "pcx", message("PcxComments"), "", "");
            case "colorBrightnessRadio":
                return new StyleData("colorBrightnessRadio", "", message("Brightness"), "", "iconBrightness.png");
            case "colorHueRadio":
                return new StyleData("colorHueRadio", "", message("Hue"), "", "iconHue.png");
            case "colorSaturationRadio":
                return new StyleData("colorSaturationRadio", "", message("Saturation"), "", "iconSaturation.png");
            case "colorRedRadio":
                return new StyleData("colorRedRadio", "", message("Red"), "", "");
            case "colorGreenRadio":
                return new StyleData("colorGreenRadio", "", message("Green"), "", "");
            case "colorBlueRadio":
                return new StyleData("colorBlueRadio", "", message("Blue"), "", "");
            case "colorYellowRadio":
                return new StyleData("colorYellowRadio", "", message("Yellow"), "", "");
            case "colorCyanRadio":
                return new StyleData("colorCyanRadio", "", message("Cyan"), "", "");
            case "colorMagentaRadio":
                return new StyleData("colorMagentaRadio", "", message("Magenta"), "", "");
            case "colorOpacityRadio":
                return new StyleData("colorOpacityRadio", "", message("Opacity"), "", "iconOpacity.png");
            case "colorColorRadio":
                return new StyleData("colorColorRadio", "", message("Color"), "", "iconDraw.png");
            case "colorRGBRadio":
                return new StyleData("colorRGBRadio", "", message("RGB"), "", "iconRGB.png");
            case "colorIncreaseButton":
                return new StyleData("colorIncreaseButton", message("Increase"), "ALT+2", "iconPlus.png");
            case "colorDecreaseButton":
                return new StyleData("colorDecreaseButton", message("Decrease"), "ALT+3", "iconMinus.png");
            case "colorFilterButton":
                return new StyleData("colorFilterButton", message("Filter"), "ALT+4", "iconFilter.png");
            case "colorInvertButton":
                return new StyleData("colorInvertButton", message("Invert"), "ALT+5", "iconInvert.png");
            case "colorReplaceRadio":
                return new StyleData("colorReplaceRadio", message("ReplaceColor"), "", "iconReplace.png");
            case "setRadio":
                return new StyleData(id, message("Set"), "", "iconEqual.png");
            case "invertRadio":
                return new StyleData(id, message("Invert"), "", "iconInvert.png");
            case "increaseRadio":
                return new StyleData(id, message("Increase"), "", "iconPlus.png");
            case "decreaseRadio":
                return new StyleData(id, message("Decrease"), "", "iconMinus.png");
            case "filterRadio":
                return new StyleData(id, message("Filter"), "", "iconFilter.png");
            default:
                return null;
        }
    }

}
