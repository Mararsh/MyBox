package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantization.ColorCount;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.KMeansClustering;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.ImageQuantizationFactory.KMeansClusteringQuantization;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.FloatTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAnalyseDominantController extends BaseController {

    protected ImageAnalyseController analyseController;
    protected int colorNumber, regionSize, kmeansLoop, weight1, weight2, weight3;
    protected List<Color> kmeansColors, popularityColors;

    @FXML
    protected Button paletteButton;
    @FXML
    protected ComboBox<String> colorsNumberSelectors, regionSizeSelector, weightSelector,
            kmeansLoopSelector;
    @FXML
    protected ControlWebView colorsController;
    @FXML
    protected PieChart dominantPie;
    @FXML
    protected Label actualLoopLabel;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(paletteButton, message("AddInColorPalette"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorNumber = UserConfig.getInt(baseName + "ColorNumber", 16);
            colorNumber = colorNumber <= 0 ? 16 : colorNumber;
            colorsNumberSelectors.getItems().addAll(Arrays.asList(
                    "16", "8", "5", "6", "27", "64", "258", "128"));
            colorsNumberSelectors.setValue(colorNumber + "");
            colorsNumberSelectors.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            colorNumber = v;
                            UserConfig.setInt(baseName + "ColorNumber", colorNumber);
                            ValidationTools.setEditorNormal(colorsNumberSelectors);
//                            loadData(false, true);
                        } else {
                            ValidationTools.setEditorBadStyle(colorsNumberSelectors);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(colorsNumberSelectors);
                    }
                }
            });

            regionSize = UserConfig.getInt(baseName + "RegionSize", 256);
            regionSize = regionSize <= 0 ? 256 : regionSize;
            regionSizeSelector.getItems().addAll(Arrays.asList("256", "1024", "64", "512", "1024", "4096", "128"));
            regionSizeSelector.setValue(regionSize + "");
            regionSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            regionSize = v;
                            UserConfig.setInt(baseName + "RegionSize", regionSize);
                            regionSizeSelector.getEditor().setStyle(null);
//                            loadData(false, true, false);
                        } else {
                            regionSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        regionSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            weight1 = 2;
            weight2 = 4;
            weight3 = 4;
            String defaultV = UserConfig.getString(baseName + "RGBWeights1", "2:4:3");
            weightSelector.getItems().addAll(Arrays.asList(
                    "2:4:3", "1:1:1", "4:4:2", "2:1:1", "21:71:7", "299:587:114", "2126:7152:722"
            ));
            weightSelector.setValue(defaultV);
            weightSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        String[] values = newValue.split(":");
                        int v1 = Integer.parseInt(values[0]);
                        int v2 = Integer.parseInt(values[1]);
                        int v3 = Integer.parseInt(values[2]);
                        if (v1 <= 0 || v2 <= 0 || v3 <= 0) {
                            weightSelector.getEditor().setStyle(UserConfig.badStyle());
                            return;
                        }
                        weight1 = v1;
                        weight2 = v2;
                        weight1 = v3;
                        weightSelector.getEditor().setStyle(null);
                        UserConfig.setString(baseName + "RGBWeights1", newValue);
//                        loadData(false, true, false);
                    } catch (Exception e) {
                        weightSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            kmeansLoop = 10000;
            kmeansLoopSelector.getItems().addAll(Arrays.asList(
                    "10000", "5000", "3000", "1000", "20000"));
            kmeansLoopSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            kmeansLoop = v;
                            ValidationTools.setEditorNormal(kmeansLoopSelector);
//                            loadData(false, true, false);
                        } else {
                            ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                    }
                }
            });
            kmeansLoopSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void clear() {
        colorsController.loadContents("");
        dominantPie.getData().clear();
    }

    protected boolean loadDominantData(BufferedImage image) {
        try {
//            if (loadingController != null) {
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadingController.setInfo(message("CalculatingDominantColors"));
//                    }
//                });
//            }
            KMeansClusteringQuantization quantization = (KMeansClusteringQuantization) ImageQuantizationFactory
                    .create(image, null, KMeansClustering, colorNumber,
                            regionSize, weight1, weight2, weight3,
                            true, true, true);
            quantization.getKmeans().setMaxIteration(kmeansLoop);
            showDominantData(quantization, image, message("DominantKMeansComments"));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return false;
    }

//    protected boolean loadDominant2Data(BufferedImage image) {
//        try {
//            if (loadingController != null) {
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadingController.setInfo(message("CalculatingDominantColors"));
//                    }
//                });
//            }
//            ImageQuantization quantization = ImageQuantizationFactory.create(image,
//                    null, QuantizationAlgorithm.PopularityQuantization, colorNumber2,
//                    regionSize2, weight21, weight22, weight23,
//                    true, true, true);
//            return showDominantData(quantization, image,
//                    message("DominantPopularityComments"), dominantView2, dominantPie2);
//        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
//        }
//        return false;
//    }
    protected boolean showDominantData(ImageQuantization quantization,
            BufferedImage image, String title) {
        if (quantization == null || image == null) {
            return false;
        }
        try {
            quantization.operate();
            StringTable table = quantization.countTable(null);
            if (table == null) {
                return false;
            }
            table.setTitle(title);
            final long total = quantization.getTotalCount();
            final String html = StringTable.tableHtml(table);
            List<ColorCount> sortedCounts = quantization.getSortedCounts();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    colorsController.loadContents​(html);
                    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                    List<Color> colors = new ArrayList<>();
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
                        String colorString = FxColorTools.color2rgb(colors.get(i));
                        PieChart.Data data = pieChartData.get(i);
                        data.getNode().setStyle("-fx-pie-color: " + colorString + ";");

                    }
                    if (quantization instanceof KMeansClusteringQuantization) {
                        kmeansColors = colors;
                        actualLoopLabel.setText(message("ActualLoop") + ":"
                                + ((KMeansClusteringQuantization) quantization).getKmeans().getLoopCount());
                    } else {
                        popularityColors = colors;
                    }
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

                }
            });

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @FXML
    public void addPalette() {
        ColorsManageController.addColors(kmeansColors);
    }

    @FXML
    public void addPopularity() {
        ColorsManageController.addColors(popularityColors);

    }

}
