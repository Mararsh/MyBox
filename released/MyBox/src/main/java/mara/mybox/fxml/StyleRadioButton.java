package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

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
        switch (id) {
            case "miaoRadio":
                return new StyleData(id, Languages.message("Meow"), Languages.message("MiaoPrompt"), "", "iconCat.png");
            case "polylineRadio":
                return new StyleData("polylineRadio", "", Languages.message("Polyline"), "", "iconPolyline.png");
            case "linesRadio":
                return new StyleData("linesRadio", "", Languages.message("DrawLines"), "", "iconDraw.png");
            case "rectangleRadio":
                return new StyleData("rectangleRadio", "", Languages.message("Rectangle"), "", "iconRectangle.png");
            case "circleRadio":
                return new StyleData("circleRadio", "", Languages.message("Circle"), "", "iconCircle.png");
            case "ellipseRadio":
                return new StyleData("ellipseRadio", "", Languages.message("Ellipse"), "", "iconEllipse.png");
            case "polygonRadio":
                return new StyleData("polygonRadio", "", Languages.message("Polygon"), "", "iconStar.png");
            case "eraserRadio":
                return new StyleData("eraserRadio", "", Languages.message("Eraser"), "", "iconEraser.png");
            case "mosaicRadio":
                return new StyleData("mosaicRadio", "", Languages.message("Mosaic"), "", "iconMosaic.png");
            case "frostedRadio":
                return new StyleData("frostedRadio", "", Languages.message("FrostedGlass"), "", "iconFrosted.png");
            case "shapeRectangleRadio":
                return new StyleData("shapeRectangleRadio", "", Languages.message("Rectangle"), "", "iconRectangle.png");
            case "shapeCircleRadio":
                return new StyleData("shapeCircleRadio", "", Languages.message("Circle"), "", "iconCircle.png");
            case "horizontalBarsChartRadio":
                return new StyleData(id, "", Languages.message("HorizontalBarsChart"), "", "iconBarsChartH.png");
            case "verticalBarsChartRadio":
                return new StyleData(id, "", Languages.message("VerticalBarsChart"), "", "iconBarsChart.png");
            case "linesChartRadio":
                return new StyleData(id, "", Languages.message("VerticalLinesChart"), "", "iconLinesChart.png");
            case "linesChartHRadio":
                return new StyleData(id, "", Languages.message("HorizontalLinesChart"), "", "iconLinesChartH.png");
            case "pieRadio":
                return new StyleData(id, "", Languages.message("PieChart"), "", "iconPieChart.png");
            case "mapRadio":
                return new StyleData(id, "", Languages.message("Map"), "", "iconMap.png");
            case "pcxSelect":
                return new StyleData("pcxSelect", "pcx", Languages.message("PcxComments"), "", "");
            default:
                return null;
        }
    }

}
