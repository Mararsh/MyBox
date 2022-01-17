package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebView;
import mara.mybox.bufferedimage.ColorComponentTools;
import mara.mybox.bufferedimage.ColorComponentTools.ColorComponent;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantization.ColorCount;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.KMeansClustering;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.ImageQuantizationFactory.KMeansClusteringQuantization;
import mara.mybox.bufferedimage.ImageStatistic;
import mara.mybox.data.IntStatistic;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.HtmlStyles;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAnalyseController extends ImageViewerController {

    protected ImageStatistic data;
    protected ImageView parentView;
    protected int colorNumber1, regionSize1, regionSize2, colorNumber2, kmeansLoop,
            weight11, weight12, weight13, weight21, weight22, weight23;
    protected List<Color> kmeansColors, popularityColors;
    protected long nonTransparent;

    @FXML
    protected VBox imageBox, dataBox;
    @FXML
    protected HBox dataOpBox;
    @FXML
    protected CheckBox componentsLegendCheck, grayHistCheck, redHistCheck,
            greenHistCheck, blueHistCheck, alphaHistCheck,
            hueHistCheck, saturationHistCheck, brightnessHistCheck;
    @FXML
    protected TabPane dataPane;
    @FXML
    protected Tab colorsTab, dominantTab, redTab, greenTab, blueTab,
            hueTab, brightnessTab, saturationTab, grayTab, alphaTab;
    @FXML
    protected BarChart colorsBarchart, grayBarchart, redBarchart, greenBarchart, blueBarchart,
            hueBarchart, saturationBarchart, brightnessBarchart, alphaBarchart;
    @FXML
    protected Button refreshButton, palette1Button, palette2Button;
    @FXML
    protected ComboBox<String> colorsNumberSelectors1, colorsNumberSelectors2,
            regionSizeSelector1, regionSizeSelector2, weightSelector1, weightSelector2,
            kmeansLoopSelector;
    @FXML
    protected WebView colorsView, dominantView1, dominantView2,
            grayView, redView, greenView, blueView,
            hueView, saturationView, brightnessView, alphaView;
    @FXML
    protected PieChart dominantPie1, dominantPie2;
    @FXML
    protected Label actualLoopLabel;

    public ImageAnalyseController() {
        baseTitle = message("ImageAnalyse");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageBox();
            initDataBox();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initImageBox() {
        try {

            imageBox.disableProperty().bind(imageView.imageProperty().isNull());

            selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    loadData(true, true, true);
                }
            });
            selectAreaCheck.setSelected(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initDataBox() {

        dataOpBox.disableProperty().bind(imageView.imageProperty().isNull());
        dataPane.disableProperty().bind(imageView.imageProperty().isNull());

        initComponentsTab();
        initDominantTab();
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(palette1Button, message("AddInColorPalette"));
            NodeStyleTools.setTooltip(palette2Button, message("AddInColorPalette"));
            NodeStyleTools.setTooltip(tipsView, new Tooltip(
                    message("ImageAnalyseTips") + "\n\n-------------------------\n"
                    + message("QuantizationComments")));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadData() {
        loadData(true, true, true);
    }

    protected void loadData(boolean components, boolean dominant1, boolean dominant2) {
        if (components) {
            colorsView.getEngine().loadContent("");
            colorsBarchart.getData().clear();
            grayView.getEngine().loadContent("");
            grayBarchart.getData().clear();
            redView.getEngine().loadContent("");
            redBarchart.getData().clear();
            greenView.getEngine().loadContent("");
            greenBarchart.getData().clear();
            blueView.getEngine().loadContent("");
            blueBarchart.getData().clear();
            hueView.getEngine().loadContent("");
            hueBarchart.getData().clear();
            saturationView.getEngine().loadContent("");
            saturationBarchart.getData().clear();
            brightnessView.getEngine().loadContent("");
            brightnessBarchart.getData().clear();
            alphaView.getEngine().loadContent("");
            alphaBarchart.getData().clear();
        }
        if (dominant1) {
            dominantView1.getEngine().loadContent("");
            dominantPie1.getData().clear();
        }
        if (dominant2) {
            dominantView2.getEngine().loadContent("");
            dominantPie2.getData().clear();
        }

        if (image == null || isSettingValues
                || (!components && !dominant1 && !dominant2)) {
            return;
        }

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    Image aImage = null;
                    if (selectAreaCheck.isSelected()) {
                        aImage = scopeImage();
                    }
                    if (aImage == null) {
                        aImage = image;
                    }
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(aImage, null);

                    if (components) {
                        loadComponentsData(bufferedImage);
                    }
                    if (dominant1) {
                        loadDominant1Data(bufferedImage);
                    }
                    if (dominant2) {
                        loadDominant2Data(bufferedImage);
                    }

                    return true;
                }

                @Override
                protected void whenSucceeded() {

                }

            };
            loadingController = start(task);
        }
    }

    public void setImage(Image image) {
        try {
            this.image = image;
            imageView.setImage(image);
            fitSize();

            drawMaskRulerXY();
            checkCoordinate();
            setMaskStroke();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void refreshData() {
        if (parentView != null) {
            setImage(parentView.getImage());
        } else {
            setImage(imageView.getImage());
        }
        loadData();
    }


    /*
        Image View
     */
    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }

            loadData();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            imageView.setImage(null);
            alertInformation(message("NotSupported"));
            return false;
        }
    }

    @FXML
    @Override
    public boolean drawMaskRectangleLine() {
        if (selectAreaCheck.isSelected() && super.drawMaskRectangleLine()) {
            loadData();
            return true;
        } else {
            return false;
        }

    }

    public ImageView getParentView() {
        return parentView;
    }

    public void setParentView(ImageView parentView) {
        this.parentView = parentView;
    }

    /*
        Dominant Color
     */
    protected void initDominantTab() {
        try {

            colorNumber1 = UserConfig.getInt(baseName + "ColorNumber1", 16);
            colorNumber1 = colorNumber1 <= 0 ? 16 : colorNumber1;
            colorsNumberSelectors1.getItems().addAll(Arrays.asList(
                    "16", "8", "5", "6", "27", "64", "258", "128"));
            colorsNumberSelectors1.setValue(colorNumber1 + "");
            colorsNumberSelectors1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            colorNumber1 = v;
                            UserConfig.setInt(baseName + "ColorNumber1", colorNumber1);
                            ValidationTools.setEditorNormal(colorsNumberSelectors1);
                            loadData(false, true, false);
                        } else {
                            ValidationTools.setEditorBadStyle(colorsNumberSelectors1);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(colorsNumberSelectors1);
                    }
                }
            });

            regionSize1 = UserConfig.getInt(baseName + "RegionSize1", 256);
            regionSize1 = regionSize1 <= 0 ? 256 : regionSize1;
            regionSizeSelector1.getItems().addAll(Arrays.asList("256", "1024", "64", "512", "1024", "4096", "128"));
            regionSizeSelector1.setValue(regionSize1 + "");
            regionSizeSelector1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            regionSize1 = v;
                            UserConfig.setInt(baseName + "RegionSize1", regionSize1);
                            regionSizeSelector1.getEditor().setStyle(null);
                            loadData(false, true, false);
                        } else {
                            regionSizeSelector1.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        regionSizeSelector1.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            weight11 = 2;
            weight12 = 4;
            weight13 = 4;
            String defaultV = UserConfig.getString(baseName + "RGBWeights1", "2:4:3");
            weightSelector1.getItems().addAll(Arrays.asList(
                    "2:4:3", "1:1:1", "4:4:2", "2:1:1", "21:71:7", "299:587:114", "2126:7152:722"
            ));
            weightSelector1.setValue(defaultV);
            weightSelector1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        String[] values = newValue.split(":");
                        int v1 = Integer.parseInt(values[0]);
                        int v2 = Integer.parseInt(values[1]);
                        int v3 = Integer.parseInt(values[2]);
                        if (v1 <= 0 || v2 <= 0 || v3 <= 0) {
                            weightSelector1.getEditor().setStyle(UserConfig.badStyle());
                            return;
                        }
                        weight11 = v1;
                        weight12 = v2;
                        weight13 = v3;
                        weightSelector1.getEditor().setStyle(null);
                        UserConfig.setString(baseName + "RGBWeights1", newValue);
                        loadData(false, true, false);
                    } catch (Exception e) {
                        weightSelector1.getEditor().setStyle(UserConfig.badStyle());
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
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            kmeansLoop = v;
                            ValidationTools.setEditorNormal(kmeansLoopSelector);
                            loadData(false, true, false);
                        } else {
                            ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                    }
                }
            });
            kmeansLoopSelector.getSelectionModel().select(0);

            colorNumber2 = UserConfig.getInt(baseName + "ColorNumber2", 16);
            colorNumber2 = colorNumber2 <= 0 ? 16 : colorNumber2;
            colorsNumberSelectors2.getItems().addAll(Arrays.asList(
                    "16", "8", "5", "6", "27", "64", "258", "128"));
            colorsNumberSelectors2.setValue(colorNumber2 + "");
            colorsNumberSelectors2.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            colorNumber2 = v;
                            UserConfig.setInt(baseName + "ColorNumber2", colorNumber2);
                            ValidationTools.setEditorNormal(colorsNumberSelectors2);
                            loadData(false, false, true);
                        } else {
                            ValidationTools.setEditorBadStyle(colorsNumberSelectors2);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(colorsNumberSelectors2);
                    }
                }
            });

            regionSize2 = UserConfig.getInt(baseName + "RegionSize2", 256);
            regionSize2 = regionSize2 <= 0 ? 256 : regionSize2;
            regionSizeSelector2.getItems().addAll(Arrays.asList("256", "1024", "64", "512", "1024", "4096", "128"));
            regionSizeSelector2.setValue(regionSize2 + "");
            regionSizeSelector2.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            regionSize2 = v;
                            UserConfig.setInt(baseName + "RegionSize2", regionSize2);
                            regionSizeSelector2.getEditor().setStyle(null);
                            loadData(false, false, true);
                        } else {
                            regionSizeSelector2.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        regionSizeSelector2.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            weight21 = 2;
            weight22 = 4;
            weight23 = 4;
            String defaultV2 = UserConfig.getString(baseName + "RGBWeights2", "2:4:3");
            weightSelector2.getItems().addAll(Arrays.asList(
                    "2:4:3", "1:1:1", "4:4:2", "2:1:1", "21:71:7", "299:587:114", "2126:7152:722"
            ));
            weightSelector2.setValue(defaultV2);
            weightSelector2.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        String[] values = newValue.split(":");
                        int v1 = Integer.parseInt(values[0]);
                        int v2 = Integer.parseInt(values[1]);
                        int v3 = Integer.parseInt(values[2]);
                        if (v1 <= 0 || v2 <= 0 || v3 <= 0) {
                            weightSelector2.getEditor().setStyle(UserConfig.badStyle());
                            return;
                        }
                        weight21 = v1;
                        weight22 = v2;
                        weight23 = v3;
                        weightSelector2.getEditor().setStyle(null);
                        UserConfig.setString(baseName + "RGBWeights2", newValue);
                        loadData(false, false, true);
                    } catch (Exception e) {
                        weightSelector2.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected boolean loadDominant1Data(BufferedImage image) {
        try {
            if (loadingController != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingController.setInfo(message("CalculatingDominantColors"));
                    }
                });
            }
            KMeansClusteringQuantization quantization = (KMeansClusteringQuantization) ImageQuantizationFactory
                    .create(image, null, KMeansClustering, colorNumber1,
                            regionSize1, weight11, weight12, weight13,
                            true, true, true);
            quantization.getKmeans().setMaxIteration(kmeansLoop);
            showDominantData(quantization, image, message("DominantKMeansComments"), dominantView1, dominantPie1);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return false;
    }

    protected boolean loadDominant2Data(BufferedImage image) {
        try {
            if (loadingController != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingController.setInfo(message("CalculatingDominantColors"));
                    }
                });
            }
            ImageQuantization quantization = ImageQuantizationFactory.create(image,
                    null, QuantizationAlgorithm.PopularityQuantization, colorNumber2,
                    regionSize2, weight21, weight22, weight23,
                    true, true, true);
            return showDominantData(quantization, image,
                    message("DominantPopularityComments"), dominantView2, dominantPie2);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return false;
    }

    protected boolean showDominantData(ImageQuantization quantization,
            BufferedImage image, String title, WebView view, PieChart pie) {
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
                    view.getEngine().loadContent​(html);
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
                    pie.setData(pieChartData);
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
                    Set<Node> legendItems = pie.lookupAll("Label.chart-legend-item");
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

    /*
        Color Components
     */
    protected void initComponentsTab() {
        try {

            grayHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistGrey", newVal);
                    showComponentsHistogram();
                }
            });
            grayHistCheck.setSelected(UserConfig.getBoolean("ImageHistGrey", false));

            redHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistRed", newVal);
                    showComponentsHistogram();
                }
            });
            redHistCheck.setSelected(UserConfig.getBoolean("ImageHistRed", true));

            greenHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistGreen", newVal);
                    showComponentsHistogram();
                }
            });
            greenHistCheck.setSelected(UserConfig.getBoolean("ImageHistGreen", true));

            blueHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistBlue", newVal);
                    showComponentsHistogram();
                }
            });
            blueHistCheck.setSelected(UserConfig.getBoolean("ImageHistBlue", true));

            hueHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistHue", newVal);
                    showComponentsHistogram();
                }
            });
            hueHistCheck.setSelected(UserConfig.getBoolean("ImageHistHue", false));

            brightnessHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistBrightness", newVal);
                    showComponentsHistogram();
                }
            });
            brightnessHistCheck.setSelected(UserConfig.getBoolean("ImageHistBrightness", false));

            saturationHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistSaturation", newVal);
                    showComponentsHistogram();
                }
            });
            saturationHistCheck.setSelected(UserConfig.getBoolean("ImageHistSaturation", false));

            alphaHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistAlpha", newVal);
                    showComponentsHistogram();
                }
            });
            alphaHistCheck.setSelected(UserConfig.getBoolean("ImageHistAlpha", false));

            componentsLegendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean("ImageHistLegend", newVal);
                    updateComponentsLegend();
                }
            });
            componentsLegendCheck.setSelected(UserConfig.getBoolean("ImageHistLegend", true));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected class IntStatisticColorCell extends TableCell<IntStatistic, Integer> {

        private final Rectangle rectangle;
        private Color color;

        public IntStatisticColorCell() {
            setContentDisplay(ContentDisplay.LEFT);
            rectangle = new Rectangle(30, 20);
        }

        @Override
        protected void updateItem(final Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item < 0) {
                setGraphic(null);
                setText(null);
                return;
            }

            IntStatistic row = getTableView().getItems().get(getTableRow().getIndex());

            color = ColorConvertTools.converColor(ColorComponentTools.color(row.getName(), item));
            if (color != null) {
                rectangle.setFill(color);
                setGraphic(rectangle);
                setText(item + "");
            } else {
                setGraphic(null);
                setText(null);
            }

        }
    }

    protected boolean loadComponentsData(BufferedImage image) {
        try {
            if (loadingController != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadingController.setInfo(message("CalculatingImageComponents"));
                    }
                });
            }
            ImageStatistic imageStatistic = ImageStatistic.create(image);
            data = imageStatistic.analyze();
            nonTransparent = imageStatistic.getNonTransparent();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    showComponentsTable();
                    if (data != null) {
                        showComponentsHistogram();
                        showColorData();
                    }
                }
            });
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return false;
    }

    protected void showComponentsTable() {
        try {
            if (image == null || isSettingValues) {
                return;
            }
            StringBuilder s = new StringBuilder();
            long imageSize = (long) (image.getWidth() * image.getHeight());
            s.append("<P>").append(message("Pixels")).append(":").append(StringTools.format(imageSize)).append(" ")
                    .append(message("NonTransparent")).append(":").append(StringTools.format(nonTransparent))
                    .append("(").append(FloatTools.percentage(nonTransparent, imageSize)).append("%)").append("</P>");
            String indent = "    ";
            s.append(indent).append(indent).append("<DIV align=\"center\" >\n");
            s.append(indent).append(indent).append(indent).append("<TABLE >\n");

            s.append(indent).append(indent).append(indent).append(indent).
                    append("<TR  style=\"font-weight:bold; \">");
            s.append("<TH>").append(message("ColorComponent")).append("</TH>");
            s.append("<TH>").append(message("Mean")).append("</TH>");
            s.append("<TH>").append(message("Variance")).append("</TH>");
            s.append("<TH>").append(message("Skewness")).append("</TH>");
            s.append("<TH>").append(message("Mode")).append("</TH>");
            s.append("<TH>").append(message("Median")).append("</TH>");
            s.append("<TH>").append(message("Maximum")).append("</TH>");
            s.append("<TH>").append(message("Minimum")).append("</TH>");
            s.append("</TR>\n");

            s.append(componentRow(ColorComponent.Gray, indent));
            s.append(componentRow(ColorComponent.RedChannel, indent));
            s.append(componentRow(ColorComponent.GreenChannel, indent));
            s.append(componentRow(ColorComponent.BlueChannel, indent));
            s.append(componentRow(ColorComponent.Hue, indent));
            s.append(componentRow(ColorComponent.Saturation, indent));
            s.append(componentRow(ColorComponent.Brightness, indent));
            s.append(componentRow(ColorComponent.AlphaChannel, indent));

            s.append(indent).append(indent).append(indent).append("</TABLE >\n");
            s.append(indent).append(indent).append("</DIV>\n");

            final String html = HtmlWriteTools.html(null, HtmlStyles.styleValue("Default"), s.toString());
            colorsView.getEngine().loadContent​(html);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected String componentRow(ColorComponent component, String indent) {
        try {
            if (data == null) {
                return "";
            }
            IntStatistic d = data.statistic(component);
            StringBuilder s = new StringBuilder();
            s.append(indent).append(indent).append(indent).append(indent).append("<TR>");
            s.append("<TD>").append(message(component.name())).append("</TD>");
            s.append(componentColumn(component, (int) d.getMean()));
            s.append(componentColumn(component, (int) d.getVariance()));
            s.append(componentColumn(component, (int) d.getSkewness()));
            s.append(componentColumn(component, d.getMode()));
            s.append(componentColumn(component, d.getMedian()));
            s.append(componentColumn(component, d.getMaximum()));
            s.append(componentColumn(component, d.getMinimum()));
            s.append("</TR>\n");
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    protected String componentColumn(ColorComponent component, int value) {
        try {
            Color color = ColorConvertTools.converColor(ColorComponentTools.color(component, value));
            String rgb = "#" + FxColorTools.color2rgba(color).substring(2, 8);
            String v = StringTools.fillRightBlank(value + "", 3);
            return "<TD align=\"center\"><DIV style=\"white-space:nowrap;\">"
                    + "<DIV style=\"display: inline-block; \">" + v + "&nbsp;&nbsp;</DIV>"
                    + "<DIV style=\"display: inline-block; width: 30px;  background-color:" + rgb
                    + "; \">&nbsp;&nbsp;&nbsp;</DIV></DIV></TD>";
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return value + "";
        }
    }

    protected void showComponentsHistogram() {
        if (isSettingValues || colorsBarchart.getData() == null) {
            return;
        }
        // https://stackoverflow.com/questions/29124723/javafx-chart-auto-scaling-wrong-with-low-numbers?r=SearchResults
        colorsBarchart.setAnimated(false);
        colorsBarchart.getData().clear();
        colorsBarchart.setAnimated(true);
        colorsBarchart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true

        if (image == null || data == null) {
            return;
        }
        List<ColorComponent> selected = new ArrayList();
        if (grayHistCheck.isSelected()) {
            selected.add(ColorComponent.Gray);
        }
        if (redHistCheck.isSelected()) {
            selected.add(ColorComponent.RedChannel);
        }
        if (greenHistCheck.isSelected()) {
            selected.add(ColorComponent.GreenChannel);
        }
        if (blueHistCheck.isSelected()) {
            selected.add(ColorComponent.BlueChannel);
        }
        if (hueHistCheck.isSelected()) {
            selected.add(ColorComponent.Hue);
        }
        if (brightnessHistCheck.isSelected()) {
            selected.add(ColorComponent.Brightness);
        }
        if (saturationHistCheck.isSelected()) {
            selected.add(ColorComponent.Saturation);
        }
        if (alphaHistCheck.isSelected()) {
            selected.add(ColorComponent.AlphaChannel);
        }

        if (selected.isEmpty()) {
            return;
        }
        for (int i = 0; i < selected.size(); ++i) {
            showComponentsHistogram(i, selected.get(i));
        }
        updateComponentsLegend();
    }

    protected void showComponentsHistogram(int index, final ColorComponent component) {
        if (image == null || data == null) {
            return;
        }
        // https://stackoverflow.com/questions/31774771/javafx-chart-axis-only-shows-last-label?r=SearchResults

        int[] histogram = data.histogram(component);

        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < histogram.length; ++i) {
            series.getData().add(new XYChart.Data(i + "", histogram[i]));
        }
        series.setName(message(component.name()));

        colorsBarchart.getData().add(index, series);
        String colorString = FxColorTools.color2rgb(ColorComponentTools.color(component));
        for (Node n
                : colorsBarchart.lookupAll(".default-color" + index + ".chart-bar")) {
            n.setStyle("-fx-bar-fill: " + colorString + "; ");
        }

    }

    // https://stackoverflow.com/questions/31774771/javafx-chart-axis-only-shows-last-label?r=SearchResults
//            colorsBarchart.setAnimated(true);
//            colorsBarchart.getData().add(new XYChart.Series(FXCollections.observableArrayList(new XYChart.Data("", 0))));
    // https://stackoverflow.com/questions/37634769/dynamically-change-chart-colors-using-colorpicker/37646943
    private void updateComponentsLegend() {
        try {
            if (image == null || data == null) {
                return;
            }
            if (!componentsLegendCheck.isSelected()) {
                colorsBarchart.setLegendVisible(false);
                return;
            }
            colorsBarchart.setLegendVisible(true);
            Set<Node> legendItems = colorsBarchart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    String colorString = FxColorTools.color2rgb(ColorComponentTools.componentColor(legendLabel.getText()));
                    legend.setStyle("-fx-background-color: " + colorString);
                }
            }
        } catch (Exception e) {

        }
    }

    @FXML
    public void selectNoneChannels() {
        isSettingValues = true;
        grayHistCheck.setSelected(false);
        redHistCheck.setSelected(false);
        greenHistCheck.setSelected(false);
        blueHistCheck.setSelected(false);
        alphaHistCheck.setSelected(false);
        hueHistCheck.setSelected(false);
        saturationHistCheck.setSelected(false);
        brightnessHistCheck.setSelected(false);
        componentsLegendCheck.setSelected(false);
        isSettingValues = false;
        colorsBarchart.setAnimated(false);
        colorsBarchart.getData().clear();
    }

    @FXML
    public void selectAllChannels() {
        isSettingValues = true;
        grayHistCheck.setSelected(true);
        redHistCheck.setSelected(true);
        greenHistCheck.setSelected(true);
        blueHistCheck.setSelected(true);
        alphaHistCheck.setSelected(true);
        hueHistCheck.setSelected(true);
        saturationHistCheck.setSelected(true);
        brightnessHistCheck.setSelected(true);
        componentsLegendCheck.setSelected(true);
        isSettingValues = false;
        showComponentsHistogram();
    }

    @FXML
    public void addKmeans() {
        ColorsManageController.addColors(kmeansColors);
    }

    @FXML
    public void addPopularity() {
        ColorsManageController.addColors(popularityColors);

    }


    /*
        Color
     */
    public void showColorData() {
        if (data == null) {
            return;
        }
        showColorData(ColorComponent.Gray, grayView, grayBarchart);
        showColorData(ColorComponent.RedChannel, redView, redBarchart);
        showColorData(ColorComponent.GreenChannel, greenView, greenBarchart);
        showColorData(ColorComponent.BlueChannel, blueView, blueBarchart);
        showColorData(ColorComponent.Hue, hueView, hueBarchart);
        showColorData(ColorComponent.Brightness, brightnessView, brightnessBarchart);
        showColorData(ColorComponent.Saturation, saturationView, saturationBarchart);
        showColorData(ColorComponent.AlphaChannel, alphaView, alphaBarchart);

    }

    public void showColorData(ColorComponent component, WebView view, BarChart barchart) {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Value"), message("PixelsNumber"),
                    message("Percentage"), message("Color"),
                    message("Red"), message("Green"), message("Blue"), message("Opacity"),
                    message("Hue"), message("Brightness"), message("Saturation")
            ));
            StringTable table = new StringTable(names, message(component.name()), 3);
            int[] histogram = data.histogram(component);

            for (int i = histogram.length - 1; i >= 0; --i) {
                List<String> row = new ArrayList<>();
                java.awt.Color aColor = ColorComponentTools.color(component, i);
                int red = aColor.getRed();
                int green = aColor.getGreen();
                int blue = aColor.getBlue();
                Color fColor = ColorConvertTools.converColor(aColor);
                row.addAll(Arrays.asList(i + "", StringTools.format(histogram[i]),
                        FloatTools.percentage(histogram[i], nonTransparent) + "%",
                        FxColorTools.color2rgba(fColor), red + " ", green + " ", blue + " ",
                        (int) Math.round(fColor.getOpacity() * 100) + "%",
                        Math.round(fColor.getHue()) + " ",
                        Math.round(fColor.getSaturation() * 100) + "%",
                        Math.round(fColor.getBrightness() * 100) + "%"
                ));
                table.add(row);
            }
            final String html = StringTable.tableHtml(table);
            view.getEngine().loadContent​(html);

            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < histogram.length; ++i) {
                series.getData().add(new XYChart.Data(i + "", histogram[i]));
            }
            series.setName(message(component.name()));

            barchart.setAnimated(false);
            barchart.getData().clear();
            barchart.setAnimated(true);
            barchart.getXAxis().setAnimated(false);
            barchart.getData().add(series);
            String colorString = FxColorTools.color2rgb(ColorComponentTools.color(component));
            for (Node n : barchart.lookupAll(".default-color0.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }
            barchart.setLegendVisible(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            if (image == null || data == null) {
                return;
            }
            String name = null;
            if (sourceFile != null) {
                name = FileNameTools.getFilePrefix(sourceFile.getName());
            }
            final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                    name, targetExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            double scale = NodeTools.dpiScale();
            SnapshotParameters snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(Transform.scale(scale, scale));

            // Display the object when make snapshot, so need switch to each tab
            Tab currentTab = dataPane.getSelectionModel().getSelectedItem();

            dataPane.getSelectionModel().select(colorsTab);
            String html = WebViewTools.getHtml(colorsView);
            Thread.sleep(50);
            final String colorsViewHml = HtmlReadTools.body(html);

            final Image colorsBarchartSnap = colorsBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(dominantTab);
            html = WebViewTools.getHtml(dominantView1);
            Thread.sleep(50);
            final String dominantView1Hml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image dominantPie1Snap = dominantPie1.snapshot(snapPara, null);

            html = WebViewTools.getHtml(dominantView2);
            Thread.sleep(50);
            final String dominantView2Hml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image dominantPie2Snap = dominantPie2.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(grayTab);
            html = WebViewTools.getHtml(grayView);
            Thread.sleep(50);
            final String greyHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image greyBarchartSnap = grayBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(redTab);
            html = WebViewTools.getHtml(redView);
            Thread.sleep(50);
            final String redHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image redBarchartSnap = redBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(greenTab);
            html = WebViewTools.getHtml(greenView);
            Thread.sleep(50);
            final String greenHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image greenBarchartSnap = greenBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(blueTab);
            html = WebViewTools.getHtml(blueView);
            Thread.sleep(50);
            final String blueHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image blueBarchartSnap = blueBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(hueTab);
            html = WebViewTools.getHtml(hueView);
            Thread.sleep(50);
            final String hueHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image hueBarchartSnap = hueBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(brightnessTab);
            html = WebViewTools.getHtml(brightnessView);
            Thread.sleep(50);
            final String brightnessHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image brightnessBarchartSnap = brightnessBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(saturationTab);
            html = WebViewTools.getHtml(saturationView);
            Thread.sleep(50);
            final String saturationHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image saturationBarchartSnap = saturationBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(alphaTab);
            html = WebViewTools.getHtml(alphaView);
            Thread.sleep(50);
            final String alphaHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image alphaBarchartSnap = alphaBarchart.snapshot(snapPara, null);

            dataPane.getSelectionModel().select(currentTab);

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    @Override
                    protected boolean handle() {
                        try {

                            String subPath = FileNameTools.getFilePrefix(file.getName());
                            String path = file.getParent() + "/" + subPath;
                            (new File(path)).mkdirs();

                            StringBuilder s = new StringBuilder();
                            s.append("<h1  class=\"center\">").append(message("ImageAnalyse")).append("</h1>\n");
                            s.append("<hr>\n");

                            s.append("<h2  class=\"center\">").append(message("Image")).append("</h2>\n");
                            if (sourceFile != null) {
                                s.append("<h3  class=\"center\">").append(sourceFile).append("</h3>\n");
                            }
                            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "image.jpg");
                            String imageName = subPath + "/image.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\"></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            s.append("<hr>\n");

                            s.append("<h2  class=\"center\">").append(message("Summary")).append("</h2>\n");
                            s.append(colorsViewHml);
                            bufferedImage = SwingFXUtils.fromFXImage(colorsBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "colorsBarchartImage.jpg");
                            imageName = subPath + "/colorsBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append("\n<h2  class=\"center\">").append(message("DominantColors")).append("</h2>\n");
                            s.append(dominantView1Hml);
                            bufferedImage = SwingFXUtils.fromFXImage(dominantPie1Snap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "dominantPie1Image.jpg");
                            imageName = subPath + "/dominantPie1Image.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(dominantView2Hml);
                            bufferedImage = SwingFXUtils.fromFXImage(dominantPie2Snap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "dominantPie2Image.jpg");
                            imageName = subPath + "/dominantPie2Image.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(greyHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(greyBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "greyBarchartImage.jpg");
                            imageName = subPath + "/greyBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(redHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(redBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "redBarchartImage.jpg");
                            imageName = subPath + "/redBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(greenHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(greenBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "greenBarchartImage.jpg");
                            imageName = subPath + "/greenBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(blueHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(blueBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "blueBarchartImage.jpg");
                            imageName = subPath + "/blueBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(hueHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(hueBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "hueBarchartImage.jpg");
                            imageName = subPath + "/hueBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(saturationHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(saturationBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "saturationBarchartImage.jpg");
                            imageName = subPath + "/saturationBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(brightnessHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(brightnessBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "brightnessBarchartImage.jpg");
                            imageName = subPath + "/brightnessBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            s.append(alphaHtml);
                            bufferedImage = SwingFXUtils.fromFXImage(alphaBarchartSnap, null);
                            ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "alphaBarchartImage.jpg");
                            imageName = subPath + "/alphaBarchartImage.jpg";
                            s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                            if (task == null || isCancelled()) {
                                return false;
                            }

                            String html = HtmlWriteTools.html("", HtmlStyles.styleValue("Default"), s.toString());
                            TextFileTools.writeFile(file, html);

                            if (task == null || isCancelled()) {
                                return false;
                            }
                            return file.exists();
                        } catch (Exception e) {
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        WebBrowserController.oneOpen(file);

                    }
                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
