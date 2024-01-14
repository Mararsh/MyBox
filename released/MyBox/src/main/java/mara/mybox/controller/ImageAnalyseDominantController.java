package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantization.ColorCount;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.ImageQuantizationFactory.KMeansClusteringQuantization;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.FloatTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageAnalyseDominantController extends BaseController {

    protected ImageAnalyseController analyseController;
    protected List<Color> colors;

    @FXML
    protected ControlImageQuantization quantizationController;
    @FXML
    protected Button paletteButton;

    @FXML
    protected Tab colorTab, pieTab;
    @FXML
    protected ControlWebView colorsController;
    @FXML
    protected PieChart dominantPie;

    public ImageAnalyseDominantController() {
        TipsLabelKey = "ImageQuantizationComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            quantizationController.defaultForAnalyse();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(paletteButton, message("AddInColorPalette"));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void goAction() {
        loadDominantData(null);
    }

    public void loadDominantData(BufferedImage inImage) {
        if (task != null) {
            task.cancel();
        }
        clear();
        task = new FxSingletonTask<Void>(this) {
            private ImageQuantization quantization;
            private String html;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage image = inImage;
                    if (image == null) {
                        image = analyseController.bufferedImageToHandle();
                    }
                    if (image == null) {
                        return false;
                    }
                    task.setInfo(message("CalculatingDominantColors"));

                    quantization = ImageQuantizationFactory.create(
                            image, null, quantizationController, true);
                    if (quantizationController.algorithm == QuantizationAlgorithm.KMeansClustering) {
                        KMeansClusteringQuantization q = (KMeansClusteringQuantization) quantization;
                        q.getKmeans().setMaxIteration(quantizationController.kmeansLoop);
                    }
                    if (quantization == null) {
                        return false;
                    }
                    quantization.start();
                    StringTable table = quantization.countTable(this, null);
                    if (table == null) {
                        return false;
                    }
                    html = StringTable.tableHtml(table);
                    return true;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                showDominantData(quantization, html);
            }

        };
        start(task);
    }

    public void clear() {
        colorsController.clear();
        dominantPie.getData().clear();
        quantizationController.resultsLabel.setText("");
        colors = null;
    }

    protected void showDominantData(ImageQuantization quantization, String html) {
        if (quantization == null || html == null) {
            return;
        }
        try {
            List<ColorCount> sortedCounts = quantization.getSortedCounts();
            long total = quantization.getTotalCount();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            colors = new ArrayList<>();
            for (int i = 0; i < sortedCounts.size(); ++i) {
                ColorCount count = sortedCounts.get(i);
                Color color = ColorConvertTools.converColor(count.color);
                colors.add(color);
                String name = "#" + FxColorTools.color2rgba(color).substring(2, 8) + "  "
                        + FloatTools.percentage(count.count, total) + "%";
                pieChartData.add(new PieChart.Data(name, count.count));
            }
            dominantPie.setData(pieChartData);
            for (int i = 0; i < colors.size(); ++i) {
                PieChart.Data data = pieChartData.get(i);
                data.getNode().setStyle("-fx-pie-color: " + FxColorTools.color2css(colors.get(i)) + ";");
            }
            dominantPie.setLegendSide(Side.TOP);
            dominantPie.setLegendVisible(true);
            Set<Node> legendItems = dominantPie.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    String colorString = legendLabel.getText().substring(0, 9);
                    legend.setStyle("-fx-background-color: " + colorString);
                }
            }

            colorsController.loadContents​(html);
            if (quantizationController.algorithm == QuantizationAlgorithm.KMeansClustering) {
                quantizationController.resultsLabel.setText(message("ActualLoop") + ":"
                        + ((KMeansClusteringQuantization) quantization).getKmeans().getLoopCount());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void addPalette() {
        if (colors == null || colors.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        ColorsManageController.addColors(colors);
    }

}
