package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebView;
import mara.mybox.bufferedimage.ColorComponentTools;
import mara.mybox.bufferedimage.ColorComponentTools.ColorComponent;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageStatistic;
import mara.mybox.calculation.IntStatistic;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageAnalyseController extends BaseImageController {

    protected ImageStatistic data;
    protected long nonTransparent;

    @FXML
    protected CheckBox sortCheck, componentsLegendCheck, grayHistCheck, redHistCheck,
            greenHistCheck, blueHistCheck, alphaHistCheck,
            hueHistCheck, saturationHistCheck, brightnessHistCheck;
    @FXML
    protected Tab statisticTab, histogramTab, dominantTab, redTab, greenTab, blueTab,
            hueTab, brightnessTab, saturationTab, grayTab, alphaTab;
    @FXML
    protected BarChart colorsBarchart, grayBarchart, redBarchart, greenBarchart, blueBarchart,
            hueBarchart, saturationBarchart, brightnessBarchart, alphaBarchart;
    @FXML
    protected Button refreshButton;
    @FXML
    protected WebView grayView, redView, greenView, blueView,
            hueView, saturationView, brightnessView, alphaView;
    @FXML
    protected ControlWebView statisticController;
    @FXML
    protected ImageAnalyseDominantController dominantController;

    public ImageAnalyseController() {
        baseTitle = message("ImageAnalyse");
        TipsLabelKey = "ImageAnalyseTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initComponentsTab();

            dominantController.analyseController = this;

            sortCheck.setSelected(UserConfig.getBoolean(baseName + "Sort", true));
            sortCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "Sort", newVal);
                    showColorData();
                }
            });

            tabPane.disableProperty().bind(imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            loadData();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected void loadData() {
        if (image == null || isSettingValues) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        statisticController.clear();
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
        dominantController.clear();
        updateStageTitle(sourceFile);
        task = new FxSingletonTask<Void>(this) {
            private BufferedImage bufferedImage;

            @Override
            protected boolean handle() {
                try {
                    bufferedImage = bufferedImageToHandle();
                    if (bufferedImage == null) {
                        return false;
                    }
                    task.setInfo(message("CalculatingImageComponents"));
                    ImageStatistic imageStatistic = ImageStatistic.create(bufferedImage);
                    data = imageStatistic.analyze(this);
                    nonTransparent = imageStatistic.getNonTransparent();
                    return data != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                showStatistic();
                showComponentsHistogram();
                showColorData();
                dominantController.loadDominantData(bufferedImage);
                String title = getBaseTitle();
                if (sourceFile != null) {
                    title += " - " + sourceFile.getAbsolutePath();
                }
                getMyStage().setTitle(title);
            }

        };
        start(task);
    }

    public BufferedImage bufferedImageToHandle() {
        try {
            return SwingFXUtils.fromFXImage(image, null);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    /*
        statistic
     */
    protected void showStatistic() {
        try {
            if (image == null || data == null) {
                return;
            }
            StringBuilder s = new StringBuilder();
            long imageSize = (long) (image.getWidth() * image.getHeight());
            s.append("<P>").append(message("Pixels")).append(":").append(StringTools.format(imageSize)).append("<BR>")
                    .append(message("NonTransparent")).append(":").append(StringTools.format(nonTransparent))
                    .append("(").append(FloatTools.percentage(nonTransparent, imageSize)).append("%)").append("</P>");
            String indent = "    ";
            s.append(indent).append(indent).append("<DIV align=\"center\" >\n");
            s.append(indent).append(indent).append(indent).append("<TABLE>\n");

            s.append(indent).append(indent).append(indent).append(indent).
                    append("<TR  style=\"font-weight:bold; \">");
            s.append("<TH>").append(message("Name")).append("</TH>");
            s.append("<TH>").append(message("Mean")).append("</TH>");
            s.append("<TH>").append(message("StandardDeviation")).append("</TH>");
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

            s.append(indent).append(indent).append(indent).append("</TABLE>\n");
            s.append(indent).append(indent).append("</DIV>\n");

            String html = HtmlWriteTools.html(null, HtmlStyles.styleValue("Default"), s.toString());
            statisticController.loadContents(html);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
            s.append(componentColumn(component, (int) d.getStandardDeviation()));
            s.append(componentColumn(component, (int) d.getSkewness()));
            s.append(componentColumn(component, d.getMode()));
            s.append(componentColumn(component, d.getMedian()));
            s.append(componentColumn(component, d.getMaximum()));
            s.append(componentColumn(component, d.getMinimum()));
            s.append("</TR>\n");
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return "";
        }
    }

    protected String componentColumn(ColorComponent component, int value) {
        try {
            Color color = ColorConvertTools.converColor(ColorComponentTools.color(component, value));
            String rgb = FxColorTools.color2rgb(color);
            int width = (int) (50 * ColorComponentTools.percentage(component, value));
            return "<TD><DIV style=\"white-space:nowrap;\">" + value + "</BR>\n"
                    + "  <DIV style=\"display: inline-block; width: 50px; background-color: #EEEEEE; \">\n"
                    + "  <DIV style=\"display: inline-block; width: " + width + "px;  background-color: "
                    + rgb + "; \">&nbsp;</DIV></DIV>\n"
                    + "</DIV></TD>\n";
        } catch (Exception e) {
            MyBoxLog.error(e);
            return value + "";
        }
    }

    /*
        Histograms
     */
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
                    legend.setStyle("-fx-background-color: "
                            + FxColorTools.color2css(ColorComponentTools.componentColor(legendLabel.getText())));
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
            MyBoxLog.error(e);
        }
    }

    protected void showComponentsHistogram() {
        if (image == null || data == null || colorsBarchart.getData() == null) {
            return;
        }

        // https://stackoverflow.com/questions/29124723/javafx-chart-auto-scaling-wrong-with-low-numbers?r=SearchResults
        colorsBarchart.setAnimated(false);
        colorsBarchart.getData().clear();
        colorsBarchart.setAnimated(true);
        colorsBarchart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true

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
            StringTable table = new StringTable(names, message(component.name()));
            int[] histogram = data.histogram(component);

            List<List<Integer>> sort = new ArrayList<>();
            for (int i = histogram.length - 1; i >= 0; --i) {
                List<Integer> dataRow = new ArrayList<>();
                dataRow.add(i);
                dataRow.add(histogram[i]);
                sort.add(dataRow);
            }
            if (sortCheck.isSelected()) {
                Collections.sort(sort, new Comparator<List<Integer>>() {
                    @Override
                    public int compare(List<Integer> v1, List<Integer> v2) {
                        int diff = v1.get(1) - v2.get(1);
                        if (diff == 0) {
                            return 0;
                        } else if (diff > 0) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            }
            for (List<Integer> dataRow : sort) {
                List<String> row = new ArrayList<>();
                int value = dataRow.get(0);
                int count = dataRow.get(1);
                java.awt.Color aColor = ColorComponentTools.color(component, value);
                int red = aColor.getRed();
                int green = aColor.getGreen();
                int blue = aColor.getBlue();
                Color fColor = ColorConvertTools.converColor(aColor);
                row.addAll(Arrays.asList(value + "", StringTools.format(count),
                        FloatTools.percentage(count, nonTransparent) + "%",
                        "<DIV style=\"width: 50px;  background-color:"
                        + FxColorTools.color2rgb(fColor) + "; \">&nbsp;&nbsp;&nbsp;</DIV>",
                        red + " ", green + " ", blue + " ",
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            if (image == null || data == null) {
                return;
            }
            if (task != null && !task.isQuit()) {
                return;
            }
            final File file = chooseSaveFile();
            if (file == null) {
                return;
            }
            double scale = NodeTools.dpiScale();
            SnapshotParameters snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(Transform.scale(scale, scale));

            // Display the object when make snapshot, so need switch to each tab
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

            tabPane.getSelectionModel().select(statisticTab);
            String html = statisticController.currentHtml();
            Thread.sleep(50);
            final String colorsViewHml = HtmlReadTools.body(html);

            final Image colorsBarchartSnap = colorsBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(dominantTab);
            dominantController.tabPane.getSelectionModel().select(dominantController.colorTab);
            html = dominantController.colorsController.currentHtml();
            Thread.sleep(50);
            final String dominantViewHml = HtmlReadTools.body(html);

            Thread.sleep(50);
            dominantController.tabPane.getSelectionModel().select(dominantController.pieTab);
            final Image dominantPieSnap = dominantController.dominantPie.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(grayTab);
            html = WebViewTools.getHtml(grayView);
            Thread.sleep(50);
            final String greyHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image greyBarchartSnap = grayBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(redTab);
            html = WebViewTools.getHtml(redView);
            Thread.sleep(50);
            final String redHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image redBarchartSnap = redBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(greenTab);
            html = WebViewTools.getHtml(greenView);
            Thread.sleep(50);
            final String greenHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image greenBarchartSnap = greenBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(blueTab);
            html = WebViewTools.getHtml(blueView);
            Thread.sleep(50);
            final String blueHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image blueBarchartSnap = blueBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(hueTab);
            html = WebViewTools.getHtml(hueView);
            Thread.sleep(50);
            final String hueHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image hueBarchartSnap = hueBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(brightnessTab);
            html = WebViewTools.getHtml(brightnessView);
            Thread.sleep(50);
            final String brightnessHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image brightnessBarchartSnap = brightnessBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(saturationTab);
            html = WebViewTools.getHtml(saturationView);
            Thread.sleep(50);
            final String saturationHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image saturationBarchartSnap = saturationBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(alphaTab);
            html = WebViewTools.getHtml(alphaView);
            Thread.sleep(50);
            final String alphaHtml = HtmlReadTools.body(html);

            Thread.sleep(50);
            final Image alphaBarchartSnap = alphaBarchart.snapshot(snapPara, null);

            tabPane.getSelectionModel().select(currentTab);

            task = new FxSingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {

                        String subPath = FileNameTools.prefix(file.getName());
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
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "image.jpg");
                        String imageName = subPath + "/image.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\"></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        s.append("<hr>\n");

                        s.append("<h2  class=\"center\">").append(message("Summary")).append("</h2>\n");
                        s.append(colorsViewHml);
                        bufferedImage = SwingFXUtils.fromFXImage(colorsBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "colorsBarchartImage.jpg");
                        imageName = subPath + "/colorsBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append("\n<h2  class=\"center\">").append(message("DominantColors")).append("</h2>\n");
                        s.append(dominantViewHml);
                        bufferedImage = SwingFXUtils.fromFXImage(dominantPieSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "dominantPieImage.jpg");
                        imageName = subPath + "/dominantPieImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(greyHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(greyBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "greyBarchartImage.jpg");
                        imageName = subPath + "/greyBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(redHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(redBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "redBarchartImage.jpg");
                        imageName = subPath + "/redBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(greenHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(greenBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "greenBarchartImage.jpg");
                        imageName = subPath + "/greenBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(blueHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(blueBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "blueBarchartImage.jpg");
                        imageName = subPath + "/blueBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(hueHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(hueBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "hueBarchartImage.jpg");
                        imageName = subPath + "/hueBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(saturationHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(saturationBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "saturationBarchartImage.jpg");
                        imageName = subPath + "/saturationBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(brightnessHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(brightnessBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "brightnessBarchartImage.jpg");
                        imageName = subPath + "/brightnessBarchartImage.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\" ></div>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(alphaHtml);
                        bufferedImage = SwingFXUtils.fromFXImage(alphaBarchartSnap, null);
                        ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", path + File.separator + "alphaBarchartImage.jpg");
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
                    WebBrowserController.openFile(file);
                    recordFileWritten(file);

                }
            };
            start(task);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            fitSize();
            loadData();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
