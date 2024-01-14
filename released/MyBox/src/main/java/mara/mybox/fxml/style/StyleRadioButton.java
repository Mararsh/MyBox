package mara.mybox.fxml.style;

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
        StyleData d = equals(id);
        if (d != null) {
            return d;
        }
        return start(id);
    }

    private static StyleData equals(String id) {
        switch (id) {
            case "miaoRadio":
                return new StyleData(id, message("Meow"), message("MiaoPrompt"), "", "iconCat.png");
            case "lineRadio":
                return new StyleData(id, message("StraightLine"), "", "iconLine.png");
            case "rectangleRadio":
                return new StyleData(id, message("Rectangle"), "", "iconRectangle.png");
            case "circleRadio":
                return new StyleData(id, message("Circle"), "", "iconCircle.png");
            case "ellipseRadio":
                return new StyleData(id, message("Ellipse"), "", "iconEllipse.png");
            case "polylineRadio":
                return new StyleData(id, message("Polyline"), "", "iconPolyline.png");
            case "polylinesRadio":
                return new StyleData(id, message("Graffiti"), "", "iconPolylines.png");
            case "polygonRadio":
                return new StyleData(id, message("Polygon"), "", "iconStar.png");
            case "quadraticRadio":
                return new StyleData(id, message("QuadraticCurve"), "", "iconQuadratic.png");
            case "cubicRadio":
                return new StyleData(id, message("CubicCurve"), "", "iconCubic.png");
            case "arcRadio":
                return new StyleData(id, message("ArcCurve"), "", "iconArc.png");
            case "svgRadio":
                return new StyleData(id, message("SVGPath"), "", "iconSVG.png");
            case "eraserRadio":
                return new StyleData(id, message("Eraser"), "", "iconEraser.png");
            case "mosaicRadio":
                return new StyleData(id, message("Mosaic"), "", "iconMosaic.png");
            case "frostedRadio":
                return new StyleData(id, message("FrostedGlass"), "", "iconFrosted.png");
            case "horizontalBarChartRadio":
                return new StyleData(id, message("HorizontalBarChart"), "", "iconBarChartH.png");
            case "barChartRadio":
                return new StyleData(id, message("BarChart"), "", "iconBarChart.png");
            case "stackedBarChartRadio":
                return new StyleData(id, message("StackedBarChart"), "", "iconStackedBarChart.png");
            case "verticalLineChartRadio":
                return new StyleData(id, message("VerticalLineChart"), "", "iconLineChartV.png");
            case "lineChartRadio":
                return new StyleData(id, message("LineChart"), "", "iconLineChart.png");
            case "pieRadio":
                return new StyleData(id, message("PieChart"), "", "iconPieChart.png");
            case "areaChartRadio":
                return new StyleData(id, message("AreaChart"), "", "iconAreaChart.png");
            case "stackedAreaChartRadio":
                return new StyleData(id, message("StackedAreaChart"), "", "iconStackedAreaChart.png");
            case "scatterChartRadio":
                return new StyleData(id, message("ScatterChart"), "", "iconScatterChart.png");
            case "bubbleChartRadio":
                return new StyleData(id, message("BubbleChart"), "", "iconBubbleChart.png");
            case "mapRadio":
                return new StyleData(id, message("Map"), "", "iconMap.png");
            case "pcxSelect":
                return new StyleData(id, "pcx", message("PcxComments"), "", "");
            case "setRadio":
                return new StyleData(id, message("Set"), "", "");
            case "plusRadio":
                return new StyleData(id, message("Plus"), "", "");
            case "minusRadio":
                return new StyleData(id, message("Minus"), "", "");
            case "filterRadio":
                return new StyleData(id, message("Filter"), "", "");
            case "invertRadio":
                return new StyleData(id, message("Invert"), "", "");
            case "colorRGBRadio":
                return new StyleData(id, message("RGB"), "", "");
            case "colorBrightnessRadio":
                return new StyleData(id, message("Brightness"), "", "iconBrightness.png");
            case "colorHueRadio":
                return new StyleData(id, message("Hue"), "", "iconHue.png");
            case "colorSaturationRadio":
                return new StyleData(id, message("Saturation"), "", "iconSaturation.png");
            case "colorRedRadio":
                return new StyleData(id, message("Red"), "", "");
            case "colorGreenRadio":
                return new StyleData(id, message("Green"), "", "");
            case "colorBlueRadio":
                return new StyleData(id, message("Blue"), "", "");
            case "colorYellowRadio":
                return new StyleData(id, message("Yellow"), "", "");
            case "colorCyanRadio":
                return new StyleData(id, message("Cyan"), "", "");
            case "colorMagentaRadio":
                return new StyleData(id, message("Magenta"), "", "");
            case "colorOpacityRadio":
                return new StyleData(id, message("Opacity"), "", "iconOpacity.png");
            case "listRadio":
                return new StyleData(id, message("List"), "", "iconList.png");
            case "thumbRadio":
                return new StyleData(id, message("ThumbnailsList"), "", "iconThumbsList.png");
            case "gridRadio":
                return new StyleData(id, message("Grid"), "", "iconBrowse.png");

        }
        return null;
    }

    private static StyleData start(String id) {
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
            return new StyleData(id, "PDF", "", "iconPDF.png");
        }
        if (id.startsWith("json")) {
            return new StyleData(id, "json", "", "iconJSON.png");
        }
        if (id.startsWith("scope")) {
            switch (id) {
                case "scopeWholeRadio":
                    return new StyleData(id, message("WholeImage"), "", "iconSelectAll.png");
                case "scopeMattingRadio":
                    return new StyleData(id, message("Matting"), "", "iconColorFill.png");
                case "scopeRectangleRadio":
                    return new StyleData(id, message("Rectangle"), "", "iconRectangle.png");
                case "scopeCircleRadio":
                    return new StyleData(id, message("Circle"), "", "iconCircle.png");
                case "scopeEllipseRadio":
                    return new StyleData(id, message("Ellipse"), "", "iconEllipse.png");
                case "scopePolygonRadio":
                    return new StyleData(id, message("Polygon"), "", "iconStar.png");
                case "scopeColorRadio":
                    return new StyleData(id, message("ColorMatching"), "", "iconColor.png");
                case "scopeOutlineRadio":
                    return new StyleData(id, message("Outline"), "", "iconButterfly.png");
                case "scopeOutlineKeepRatioCheck":
                    return new StyleData(id, message("KeepRatio"), "", "iconAspectRatio.png");
            }
        }
        return null;
    }

}
