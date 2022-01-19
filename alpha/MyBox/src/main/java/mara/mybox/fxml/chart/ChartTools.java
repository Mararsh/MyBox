package mara.mybox.fxml.chart;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public class ChartTools {

    public enum LabelType {
        NotDisplay, NameAndValue, Value, Name, Pop
    }

    public enum ChartCoordinate {
        Cartesian, LogarithmicE, Logarithmic10, SquareRoot
    }

    public static void setLineChartColors(LineChart chart, Map<String, String> locationColors, boolean showLegend) {
        if (chart == null || locationColors == null) {
            return;
        }
        List<XYChart.Series> seriesList = chart.getData();
        if (seriesList == null || seriesList.size() > locationColors.size()) {
            return;
        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYChart.Series series = seriesList.get(i);
            Node node = series.getNode().lookup(".chart-series-line");
            if (node != null) {
                String name = series.getName();
                String color = locationColors.get(name);
                if (color == null) {
                    MyBoxLog.debug(name);
                } else {
                    node.setStyle("-fx-stroke: " + color + ";");
                }
            }
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
                        if (name.equals(legendLabel.getText())) {
                            String color = locationColors.get(name);
                            if (color == null) {
                                MyBoxLog.debug(name);
                            } else {
                                legend.setStyle("-fx-background-color: " + color);
                                break;
                            }
                        }
                    }
                }
            }
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

    public static void setBarChartColors(BarChart chart, boolean showLegend) {
        List<String> palette = FxColorTools.randomRGB(chart.getData().size());
        setBarChartColors(chart, palette, showLegend);
    }

    public static void setBarChartColors(BarChart chart, List<String> palette, boolean showLegend) {
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

    public static void setBarChartColors(BarChart chart, Map<String, String> palette, boolean showLegend) {
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
            for (int j = 0; j < series.getData().size(); j++) {
                XYChart.Data item = (XYChart.Data) series.getData().get(j);
                if (item.getNode() != null) {
                    String color = palette.get(series.getName());
                    item.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
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
