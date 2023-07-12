package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantization.ColorCount;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.KMeansClustering;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.ImageQuantizationFactory.KMeansClusteringQuantization;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.FloatTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageAnalyseDominantController extends BaseController {

    protected ImageAnalyseController analyseController;
    protected int colorNumber, regionSize, kmeansLoop, weight1, weight2, weight3;
    protected List<Color> colors;

    @FXML
    protected ToggleGroup algorithmGroup;
    @FXML
    protected RadioButton kmeansRadio;
    @FXML
    protected Button paletteButton;
    @FXML
    protected ComboBox<String> colorsNumberSelectors, regionSizeSelector, weightSelector,
            kmeansLoopSelector;

    @FXML
    protected Tab colorTab, pieTab;
    @FXML
    protected ControlWebView colorsController;
    @FXML
    protected PieChart dominantPie;
    @FXML
    protected FlowPane loopPane;
    @FXML
    protected Label actualLoopLabel;

    public ImageAnalyseDominantController() {
        TipsLabelKey = "ImageQuantizationComments";
    }

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

            algorithmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkAlgorithm();
                }
            });
            checkAlgorithm();

            colorNumber = UserConfig.getInt(baseName + "ColorNumber", 16);
            colorNumber = colorNumber <= 0 ? 16 : colorNumber;
            colorsNumberSelectors.getItems().addAll(Arrays.asList(
                    "8", "5", "3", "6", "16", "27", "64", "256", "128"));
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
            regionSizeSelector.getItems().addAll(Arrays.asList("4096", "1024", "256", "8192", "512", "128"));
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
            MyBoxLog.error(e);
        }

    }

    public void checkAlgorithm() {
        loopPane.setDisable(!kmeansRadio.isSelected());
        actualLoopLabel.setVisible(kmeansRadio.isSelected());
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
        task = new SingletonCurrentTask<Void>(this) {
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
                    if (kmeansRadio.isSelected()) {
                        KMeansClusteringQuantization kquantization
                                = (KMeansClusteringQuantization) ImageQuantizationFactory
                                        .create(image, null, KMeansClustering, colorNumber,
                                                regionSize, weight1, weight2, weight3,
                                                true, false, false);
                        kquantization.getKmeans().setMaxIteration(kmeansLoop);
                        quantization = kquantization;
                    } else {
                        quantization = ImageQuantizationFactory.create(image,
                                null, QuantizationAlgorithm.PopularityQuantization, colorNumber,
                                regionSize, weight1, weight2, weight3,
                                true, false, false);
                    }
                    if (quantization == null) {
                        return false;
                    }
                    quantization.operate();
                    StringTable table = quantization.countTable(null);
                    if (table == null) {
                        return false;
                    }
                    table.setTitle(kmeansRadio.isSelected() ? message("DominantKMeansComments")
                            : message("DominantPopularityComments"));
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
        actualLoopLabel.setText("");
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
            if (kmeansRadio.isSelected()) {
                actualLoopLabel.setText(message("ActualLoop") + ":"
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
