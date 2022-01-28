package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.SquareRootCoordinate;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public class ChartTools {

    public static final String DefaultBubbleStyle
            = "radial-gradient(center 50% 50%, radius 50%, transparent 0%, transparent 90%, derive(-fx-bubble-fill,0%) 100%)";

    public enum LabelType {
        NotDisplay, NameAndValue, Value, Name, Pop, Point
    }

    public enum LabelLocation {
        Above, Below, Center
    }

    public enum ChartCoordinate {
        Cartesian, LogarithmicE, Logarithmic10, SquareRoot
    }

    public static Chart style(Chart chart, String cssFile) {
        chart.getStylesheets().add(Chart.class.getResource(cssFile).toExternalForm());
        return chart;
    }

    public static Node makeLabel(XYChart.Data item, boolean xy,
            LabelType labelType, boolean pop, ChartCoordinate chartCoordinate, int scale) {
        if (item == null || item.getNode() == null) {
            return null;
        }
        try {
            String name, value, extra;
            if (xy) {
                name = item.getXValue().toString();
                value = item.getYValue().toString();
            } else {
                name = item.getYValue().toString();
                value = item.getXValue().toString();
            }
            if (item.getExtraValue() != null) {
                extra = " - " + DoubleTools.format((double) item.getExtraValue(), scale);
            } else {
                extra = "";
            }
            if (pop || labelType == LabelType.Pop) {
                NodeStyleTools.setTooltip(item.getNode(), name + " - " + value + extra);
            }
            if (labelType == null || labelType == LabelType.NotDisplay) {
                return null;
            }
            String display = null;
            String labelValue = DoubleTools.format(ChartTools.realValue(chartCoordinate, Double.valueOf(value)), scale);
            switch (labelType) {
                case Name:
                    display = name;
                    break;
                case Value:
                    display = labelValue + extra;
                    break;
                case NameAndValue:
                    display = name + " - " + labelValue + extra;
                    break;
            }
            if (display != null && !display.isBlank()) {
                Text text = new Text(display);
                text.setTextAlignment(TextAlignment.CENTER);
                return text;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    // This can set more than 8 colors. javafx only supports 8 colors defined in css
    // This should be called after data have been assigned to pie
    public static void setPieColors(PieChart pie, boolean showLegend) {
        List<String> palette = FxColorTools.randomRGB(pie.getData().size());
        setPieColors(pie, palette, showLegend);
    }

    public static void setPieColors(PieChart pie, List<String> palette, boolean showLegend) {
        if (pie == null || palette == null || pie.getData() == null || pie.getData().size() > palette.size()) {
            return;
        }
        for (int i = 0; i < pie.getData().size(); i++) {
            PieChart.Data data = pie.getData().get(i);
            data.getNode().setStyle("-fx-pie-color: " + palette.get(i) + ";");
        }
        pie.setLegendVisible(showLegend);
        if (showLegend) {
            Set<Node> legendItems = pie.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    for (int i = 0; i < pie.getData().size(); i++) {
                        String name = pie.getData().get(i).getName();
                        if (name.equals(legendLabel.getText())) {
                            legend.setStyle("-fx-background-color: " + palette.get(i));
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void setBarChartColors(XYChart chart, boolean showLegend) {
        List<String> palette = FxColorTools.randomRGB(chart.getData().size());
        setBarChartColors(chart, palette, showLegend);
    }

    public static void setBarChartColors(XYChart chart, List<String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null || seriesList.size() > palette.size()) {
            return;
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series series = seriesList.get(i);
            if (series.getData() == null) {
                continue;
            }
            for (int j = 0; j < series.getData().size(); j++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(j);
                if (item.getNode() != null) {
                    String color = palette.get(i);
                    item.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
        setLegend(chart, palette, showLegend);
    }

    public static void setBarChartColors(XYChart chart, Map<String, String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null) {
            return;
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series series = seriesList.get(i);
            if (series.getData() == null) {
                continue;
            }
            String name = series.getName();
            String color = palette.get(name);
            for (int j = 0; j < series.getData().size(); j++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(j);
                Node node = item.getNode();
                if (node != null) {
                    node.setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
        setLegend(chart, palette, showLegend);
    }

    public static void setLineChartColors(XYChart chart, int lineWidth, Map<String, String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null || seriesList.size() > palette.size()) {
            return;
        }
        if (lineWidth < 0) {
            lineWidth = 4;
        }
        for (XYChart.Series series : seriesList) {
            Node seriesNode = series.getNode();
            if (seriesNode == null) {
                continue;
            }
            String name = series.getName();
            String color = palette.get(name);
            if (color == null) {
                color = FxColorTools.randomRGB();
            }
            Node node = seriesNode.lookup(".chart-series-line");
            if (node != null) {
                node.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: " + lineWidth + "px;");
            }
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(i);
                if (item.getNode() == null) {
                    continue;
                }
                node = item.getNode().lookup(".chart-line-symbol");
                if (node != null) {
                    node.setStyle("-fx-background-color:  " + color + ", white;");
                }
            }
        }
        setLegend(chart, palette, showLegend);
    }

    public static void setAreaChartColors(XYChart chart, int lineWidth, Map<String, String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null) {
            return;
        }
        if (lineWidth < 0) {
            lineWidth = 1;
        }
        for (XYChart.Series series : seriesList) {
            if (series.getData() == null) {
                continue;
            }
            Node seriesNode = series.getNode();
            if (seriesNode == null) {
                continue;
            }
            String name = series.getName();
            String color = palette.get(name);
            if (color == null) {
                color = FxColorTools.randomRGB();
            }
            Node node = seriesNode.lookup(".chart-series-area-line");
            if (node != null) {
                node.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: " + lineWidth + "px;");
            }
            node = seriesNode.lookup(".chart-series-area-fill");
            if (node != null) {
                node.setStyle("-fx-fill: " + color + "44;");
            }
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(i);
                if (item.getNode() == null) {
                    continue;
                }
                node = item.getNode().lookup(".chart-area-symbol");
                if (node != null) {
                    node.setStyle("-fx-background-color:  " + color + ", white;");
                }
            }
        }
        setLegend(chart, palette, showLegend);
    }

    public static void setScatterChart​Colors(XYChart chart, Map<String, String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null || seriesList.size() > palette.size()) {
            return;
        }
        for (XYChart.Series series : seriesList) {
            String name = series.getName();
            String color = palette.get(name);
            if (color == null) {
                color = FxColorTools.randomRGB();
            }
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(i);
                if (item.getNode() == null) {
                    continue;
                }
                Node node = item.getNode().lookup(".chart-symbol");
                if (node != null) {
                    node.setStyle("-fx-background-color:  " + color + ";");
                }
            }
        }
        setLegend(chart, palette, showLegend);
    }

    public static void setBubbleChart​Colors(XYChart chart, String bubbleStyle, Map<String, String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null || seriesList.size() > palette.size()) {
            return;
        }
        for (int s = 0; s < seriesList.size(); s++) {
            XYChart.Series series = seriesList.get(s);
            String name = series.getName();
            String color = palette.get(name);
            if (color == null) {
                color = FxColorTools.randomRGB();
            }
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(i);
                if (item.getNode() == null) {
                    continue;
                }
                Node node = item.getNode().lookup(".chart-bubble");
                if (node != null) {
                    String style = "-fx-bubble-fill:  " + color + ";";
                    if (bubbleStyle != null && !bubbleStyle.isBlank()) {
                        style += " -fx-background-color: " + bubbleStyle + ";";
                    }
                    node.setStyle(style);
                }
            }
        }
        setLegend(chart, palette, showLegend);
    }

    public static void setLegend(XYChart chart, Map<String, String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null) {
            return;
        }
        chart.setLegendVisible(showLegend);
        if (showLegend) {
            Set<Node> legendItems = chart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    for (int i = 0; i < seriesList.size(); i++) {
                        String name = seriesList.get(i).getName();
                        String color = palette.get(name);
                        if (color != null && name.equals(legendLabel.getText())) {
                            legend.setStyle("-fx-background-color: " + color);
                        }
                    }
                }
            }
        }
    }

    public static void setLegend(XYChart chart, List<String> palette, boolean showLegend) {
        if (chart == null || palette == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null) {
            return;
        }
        chart.setLegendVisible(showLegend);
        if (showLegend) {
            Set<Node> legendItems = chart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    for (int i = 0; i < seriesList.size(); i++) {
                        if (seriesList.get(i).getName().equals(legendLabel.getText())) {
                            legend.setStyle("-fx-background-color: " + palette.get(i));
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void setChartCoordinate(XYChart chart, ChartCoordinate chartCoordinate) {
        Axis axis = chart.getYAxis();
        if (axis instanceof NumberAxis) {
            setChartCoordinate((NumberAxis) axis, chartCoordinate);
        }
        axis = chart.getXAxis();
        if (axis instanceof NumberAxis) {
            setChartCoordinate((NumberAxis) axis, chartCoordinate);
        }
    }

    public static void setChartCoordinate(NumberAxis numberAxis, ChartCoordinate chartCoordinate) {
        switch (chartCoordinate) {
            case LogarithmicE:
                numberAxis.setTickLabelFormatter(new LogarithmicECoordinate());
                break;
            case Logarithmic10:
                numberAxis.setTickLabelFormatter(new Logarithmic10Coordinate());
                break;
            case SquareRoot:
                numberAxis.setTickLabelFormatter(new SquareRootCoordinate());
                break;
        }
    }

    public static String label(LabelType labelType, String name, double value) {
        if (null == labelType) {
            return null;
        } else {
            switch (labelType) {
                case NotDisplay:
                    return null;
                case Name:
                    return name;
                case Value:
                    return StringTools.format(value);
                case Pop:
                    return "Pop###" + name + ": " + StringTools.format(value);
                default:
                    return name + ": " + StringTools.format(value);
            }
        }
    }

    public static double realValue(ChartCoordinate chartCoordinate, double coordinateValue) {
        if (chartCoordinate == null) {
            return coordinateValue;
        }
        switch (chartCoordinate) {
            case LogarithmicE:
                return Math.pow(Math.E, coordinateValue);
            case Logarithmic10:
                return Math.pow(10, coordinateValue);
            case SquareRoot:
                return coordinateValue * coordinateValue;
        }
        return coordinateValue;
    }

    public static double coordinateValue(ChartCoordinate chartCoordinate, double value) {
        if (chartCoordinate == null || value <= 0) {
            return value;
        }
        switch (chartCoordinate) {
            case LogarithmicE:
                return Math.log(value);
            case Logarithmic10:
                return Math.log10(value);
            case SquareRoot:
                return Math.sqrt(value);
        }
        return value;
    }

}
