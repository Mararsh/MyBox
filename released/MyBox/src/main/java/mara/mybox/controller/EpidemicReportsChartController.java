package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.EpidemicReportTools;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.DataFactory;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.chart.ChartTools;
import mara.mybox.fxml.chart.ChartTools.ChartCoordinate;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import mara.mybox.fxml.chart.LabeledBarChart;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.imagefile.ImageGifFile;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class EpidemicReportsChartController extends GeographyCodeMapController {

    protected EpidemicReportsController reportsController;
    protected EpidemicReportsColorsController colorsController;
    protected QueryCondition queryCondition;
    protected String chartQuerySQL, chartName;
    protected List<String> orderNames, valuesNames;
    protected int topNumber, snapWidth, mapLoadTime;
    protected long totalSize;
    protected List<String> datasets, chartTimes;
    protected Map<String, List<EpidemicReport>> timesReports, locationsReports;
    protected List<GeographyCode> chartLocations;
    protected boolean multipleDatasets, mapCentered, snapEnd;
    protected double maxValue;
    protected Side legendSide;
    protected float lenUnit;
    protected LabelType labelType;
    protected ChartCoordinate chartCoordinate;
    protected LoadingController loading;

    @FXML
    protected StackPane chartPane;
    @FXML
    protected ComboBox<String> labelSizeSelector, gifWidthSelector;
    @FXML
    protected ToggleGroup chartGroup, labelGroup, legendGroup, numberCoordinateGroup;
    @FXML
    protected VBox chartBox, mapBox, valueTypeBox, displayBox, mapOptionsBox;
    @FXML
    protected CheckBox categoryAxisCheck, confirmedCheck, healedCheck, deadCheck,
            increasedConfirmedCheck, increasedHealedCheck, increasedDeadCheck,
            HealedConfirmedPermillageCheck, DeadConfirmedPermillageCheck,
            ConfirmedPopulationPermillageCheck, DeadPopulationPermillageCheck, HealedPopulationPermillageCheck,
            ConfirmedAreaPermillageCheck, HealedAreaPermillageCheck, DeadAreaPermillageCheck,
            hlinesCheck, vlinesCheck;
    @FXML
    protected RadioButton numbersRadio, ratiosRadio, increasedRadio, confirmedRatio,
            increasedConfirmedRadio, healedRadio, healedRatioRadio, increasedHealedRadio,
            deadRadio, deadRatioRadio, increasedDeadRadio,
            horizontalBarChartRadio, barChartRadio, verticalLineChartRadio, lineChartRadio, pieRadio, mapRadio,
            cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio;

    public EpidemicReportsChartController() {
        baseTitle = message("EpidemicReport");

    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            geoTable = new TableGeographyCode();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            valuesNames = new ArrayList();

            initChartOptions();
            initMapOptions();
            initSnapOptions();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initChartOptions() {
        try {
            chartGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        drawChart();
                    });

            chartCoordinate = ChartCoordinate.Cartesian;
            numberCoordinateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        if (logarithmicERadio.isSelected()) {
                            chartCoordinate = ChartCoordinate.LogarithmicE;
                        } else if (logarithmic10Radio.isSelected()) {
                            chartCoordinate = ChartCoordinate.Logarithmic10;
                        } else if (squareRootRadio.isSelected()) {
                            chartCoordinate = ChartCoordinate.SquareRoot;
                        } else {
                            chartCoordinate = ChartCoordinate.Cartesian;
                        }
                        drawChart();
                    });

            labelType = LabelType.NameAndValue;
            labelGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                if (isSettingValues || newValue == null) {
                    return;
                }
                String value = ((RadioButton) newValue).getText();
                if (message("NameAndValue").equals(value)) {
                    labelType = LabelType.NameAndValue;
                } else if (message("Value").equals(value)) {
                    labelType = LabelType.Value;
                } else if (message("Name").equals(value)) {
                    labelType = LabelType.Name;
                } else if (message("NotDisplay").equals(value)) {
                    labelType = LabelType.NotDisplay;
                } else if (message("Pop").equals(value)) {
                    labelType = LabelType.Pop;
                } else {
                    labelType = LabelType.NameAndValue;
                }
                drawChart();
            });

            mapOptionsController.textSize = 12;
            labelSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "15", "16", "18", "9", "8", "18", "20", "24"
            ));
            labelSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        mapOptionsController.textSize = v;
                        labelSizeSelector.getEditor().setStyle(null);
                        UserConfig.setInt("EpidemicReportChartTextSize", mapOptionsController.textSize);
                        if (!isSettingValues) {
                            drawChart();
                        }
                    } else {
                        labelSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    labelSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

            legendSide = null;
            legendGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        String value = ((RadioButton) newValue).getText();
                        if (message("NotDisplay").equals(value)) {
                            legendSide = null;
                        } else if (message("Left").equals(value)) {
                            legendSide = Side.LEFT;
                        } else if (message("Top").equals(value)) {
                            legendSide = Side.TOP;
                        } else if (message("Bottom").equals(value)) {
                            legendSide = Side.BOTTOM;
                        } else {
                            legendSide = Side.RIGHT;
                        }
                        drawChart();
                    });

            categoryAxisCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean("EpidemicReportDisplayCategoryAxis", newValue);
                drawChart();
            });

            loopCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean("EpidemicReportChartLoop", newValue);
                drawChart();
            });

            hlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean("EpidemicReportDisplayHlines", newValue);
                drawChart();
            });

            vlinesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean("EpidemicReportDisplayVlines", newValue);
                drawChart();
            });

            isSettingValues = true;
            labelSizeSelector.getSelectionModel().select(UserConfig.getString("EpidemicReportChartTextSize", "12"));
            categoryAxisCheck.setSelected(UserConfig.getBoolean("EpidemicReportDisplayCategoryAxis", false));
            hlinesCheck.setSelected(UserConfig.getBoolean("EpidemicReportDisplayHlines", true));
            vlinesCheck.setSelected(UserConfig.getBoolean("EpidemicReportDisplayVlines", true));
            loopCheck.setSelected(UserConfig.getBoolean("EpidemicReportChartLoop", true));
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSnapOptions() {
        try {
            snapWidth = UserConfig.getInt(baseName + "SnapWidth", 800);
            List<String> widthValues = new ArrayList();
            widthValues.addAll(Arrays.asList("800", "1000", "500", "300", "1200", "1600", "2000", "2500"));
            gifWidthSelector.getItems().addAll(widthValues);
            gifWidthSelector.setValue(snapWidth + "");
            gifWidthSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0) {
                        snapWidth = v;
                        UserConfig.setInt(baseName + "SnapWidth", snapWidth);
                    }
                } catch (Exception e) {
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initMapOptions() {
        try {
            mapOptionsController.markerTextBox.getChildren().removeAll(mapOptionsController.baseTextPane);
            mapOptionsController.optionsBox.getChildren().removeAll(
                    mapOptionsController.dataBox, mapOptionsController.markerImageBox);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void loadCharts() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!initCharts()) {
            return;
        }
        colorsController.reset();
        drawChart();
    }

    protected boolean initCharts() {
        clearChart();
        if (reportsController == null
                || reportsController.topNumber <= 0
                || reportsController.dataTimes.isEmpty()
                || reportsController.orderNames.isEmpty()
                || reportsController.timesReports.isEmpty()) {
            return false;
        }
        topNumber = reportsController.topNumber;
        queryCondition = reportsController.queryCondition;
        chartQuerySQL = reportsController.dataQuerySQL;
        datasets = reportsController.datasets;
        timesReports = reportsController.timesReports;
        locationsReports = reportsController.locationsReports;
        chartLocations = reportsController.dataLocations;
        multipleDatasets = reportsController.datasets.size() > 1;
        maxValue = reportsController.maxValue;
        totalSize = reportsController.dataSize;
        chartTimes = new ArrayList<>();
        chartTimes.addAll(reportsController.dataTimes);
        Collections.reverse(chartTimes);
        mapOptionsController.mapSize = 3;
        frameIndex = 0;

        orderNames = new ArrayList<>();
        orderNames.addAll(reportsController.orderNames);
        orderNames.remove("time");
        if (orderNames.isEmpty()) {
            return false;
        }
        setValuesChecks();

        if (chartTimes.size() > 1) {
            displayBox.setDisable(false);
            isSettingValues = true;
            setPause(false);
            List<String> frames = new ArrayList<>();
            for (int i = chartTimes.size() - 1; i >= 0; i--) {
                String date = chartTimes.get(i);
                frames.add(date);
            }
            frameSelector.getItems().clear();
            frameSelector.getItems().addAll(frames);
            isSettingValues = false;
        } else {
            displayBox.setDisable(true);
        }
        return true;
    }

    public void clearChart() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        chartBox.getChildren().clear();
        chartBox.setVisible(false);
        mapBox.setVisible(false);
        valuesNames.clear();
        titleLabel.setText("");
        frameLabel.setText("");
        mapCentered = false;
    }

    protected void setValuesChecks() {
        for (Node node : valueTypeBox.getChildren()) {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
                ((CheckBox) node).setDisable(false);
            }
        }
        if (orderNames == null) {
            return;
        }
        boolean first = false;
        for (String name : orderNames) {
            if ("confirmed".equals(name)) {
                confirmedCheck.setSelected(true);
                if (!first) {
                    confirmedCheck.setDisable(true);
                    first = true;
                }
            } else if ("healed".equals(name)) {
                healedCheck.setSelected(true);
                if (!first) {
                    healedCheck.setDisable(true);
                    first = true;
                }
            } else if ("dead".equals(name)) {
                deadCheck.setSelected(true);
                if (!first) {
                    deadCheck.setDisable(true);
                    first = true;
                }
            } else if ("increased_confirmed".equals(name)) {
                increasedConfirmedCheck.setSelected(true);
                if (!first) {
                    increasedConfirmedCheck.setDisable(true);
                    first = true;
                }
            } else if ("increased_healed".equals(name)) {
                increasedHealedCheck.setSelected(true);
                if (!first) {
                    increasedHealedCheck.setDisable(true);
                    first = true;
                }
            } else if ("increased_dead".equals(name)) {
                increasedDeadCheck.setSelected(true);
                if (!first) {
                    increasedDeadCheck.setDisable(true);
                    first = true;
                }
            } else if ("healed_confirmed_permillage".equals(name)) {
                HealedConfirmedPermillageCheck.setSelected(true);
                if (!first) {
                    HealedConfirmedPermillageCheck.setDisable(true);
                    first = true;
                }
            } else if ("dead_confirmed_permillage".equals(name)) {
                DeadConfirmedPermillageCheck.setSelected(true);
                if (!first) {
                    DeadConfirmedPermillageCheck.setDisable(true);
                    first = true;
                }
            } else if ("confirmed_population_permillage".equals(name)) {
                ConfirmedPopulationPermillageCheck.setSelected(true);
                if (!first) {
                    ConfirmedPopulationPermillageCheck.setDisable(true);
                    first = true;
                }
            } else if ("healed_population_permillage".equals(name)) {
                HealedPopulationPermillageCheck.setSelected(true);
                if (!first) {
                    HealedPopulationPermillageCheck.setDisable(true);
                    first = true;
                }
            } else if ("dead_population_permillage".equals(name)) {
                DeadPopulationPermillageCheck.setSelected(true);
                if (!first) {
                    DeadPopulationPermillageCheck.setDisable(true);
                    first = true;
                }
            } else if ("confirmed_area_permillage".equals(name)) {
                ConfirmedAreaPermillageCheck.setSelected(true);
                if (!first) {
                    ConfirmedAreaPermillageCheck.setDisable(true);
                    first = true;
                }
            } else if ("healed_area_permillage".equals(name)) {
                HealedAreaPermillageCheck.setSelected(true);
                if (!first) {
                    HealedAreaPermillageCheck.setDisable(true);
                    first = true;
                }
            } else if ("dead_area_permillage".equals(name)) {
                DeadAreaPermillageCheck.setSelected(true);
                if (!first) {
                    DeadAreaPermillageCheck.setDisable(true);
                    first = true;
                }
            }
        }
    }

    protected void checkValues() {
        valuesNames.clear();
        if (confirmedCheck.isSelected()) {
            valuesNames.add(message("Confirmed"));
        }
        if (healedCheck.isSelected()) {
            valuesNames.add(message("Healed"));
        }
        if (deadCheck.isSelected()) {
            valuesNames.add(message("Dead"));
        }
        if (increasedConfirmedCheck.isSelected()) {
            valuesNames.add(message("IncreasedConfirmed"));
        }
        if (increasedHealedCheck.isSelected()) {
            valuesNames.add(message("IncreasedHealed"));
        }
        if (increasedDeadCheck.isSelected()) {
            valuesNames.add(message("IncreasedDead"));
        }
        if (HealedConfirmedPermillageCheck.isSelected()) {
            valuesNames.add(message("HealedConfirmedPermillage"));
        }
        if (DeadConfirmedPermillageCheck.isSelected()) {
            valuesNames.add(message("DeadConfirmedPermillage"));
        }
        if (ConfirmedPopulationPermillageCheck.isSelected()) {
            valuesNames.add(message("ConfirmedPopulationPermillage"));
        }
        if (HealedPopulationPermillageCheck.isSelected()) {
            valuesNames.add(message("HealedPopulationPermillage"));
        }
        if (DeadPopulationPermillageCheck.isSelected()) {
            valuesNames.add(message("DeadPopulationPermillage"));
        }
        if (ConfirmedAreaPermillageCheck.isSelected()) {
            valuesNames.add(message("ConfirmedAreaPermillage"));
        }
        if (HealedAreaPermillageCheck.isSelected()) {
            valuesNames.add(message("HealedAreaPermillage"));
        }
        if (DeadAreaPermillageCheck.isSelected()) {
            valuesNames.add(message("DeadAreaPermillage"));
        }
    }

    @FXML
    public void drawChart() {
        if (isSettingValues) {
            return;
        }
        try {
            clearChart();
            checkValues();
            if (chartTimes.isEmpty() || valuesNames.isEmpty()
                    || chartLocations.isEmpty() || timesReports.isEmpty()) {
                return;
            }
            setPause(false);
            titleLabel.setText(queryCondition.getTitle().replaceAll("\n", " "));

            if (mapRadio.isSelected()) {
                mapOptionsBox.setDisable(false);
                chartName = message("Map");
                chartBox.getChildren().clear();
                chartBox.setVisible(false);
                mapBox.setVisible(true);
                mapBox.toFront();
            } else {
                mapOptionsBox.setDisable(true);
                mapBox.setVisible(false);
                chartBox.setVisible(true);
                chartBox.toFront();
                if (horizontalBarChartRadio.isSelected()) {
                    chartName = message("HorizontalBarChart");
                } else if (barChartRadio.isSelected()) {
                    chartName = message("BarChart");
                } else if (verticalLineChartRadio.isSelected()) {
                    if (locationsReports == null) {
                        return;
                    }
                    chartName = message("VerticalLineChart");
                } else if (lineChartRadio.isSelected()) {
                    if (locationsReports == null) {
                        return;
                    }
                    chartName = message("LineChart");
                } else if (pieRadio.isSelected()) {
                    chartName = message("PieChart");
                }
            }

            try {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if (interval <= 0) {
                    interval = 1000;
                }
                frameCompleted = true;
                timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (!frameCompleted || timer == null) {
                                return;
                            }
                            if (chartTimes == null || chartTimes.isEmpty()
                                    || timesReports == null || locationsReports.isEmpty()) {
                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }
                                return;
                            }
                            fixFrameIndex();
                            frameCompleted = false;
                            String time = chartTimes.get(frameIndex);
                            List<EpidemicReport> timeReports = timesReports.get(time);
                            drawChart(time, timeReports);
                            frameCompleted = true;
                            ++frameIndex;
                            if (chartTimes.size() == 1
                                    || (!loopCheck.isSelected() && frameIndex >= chartTimes.size())) {
                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }
                            }
                        });
                    }

                }, 0, interval);

            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawChart(String time, List<EpidemicReport> timeReports) {
        try {
            if (valuesNames.isEmpty() || time == null
                    || timeReports == null || timeReports.isEmpty()) {
                return;
            }
            frameLabel.setText(time);
            if (mapRadio.isSelected()) {
                drawMap(timeReports);
            } else {
                if (horizontalBarChartRadio.isSelected()) {
                    if (valuesNames.size() == 1) {
                        drawValueBarsHorizontal(timeReports);
                    } else {
                        drawValuesBarsHorizontal(timeReports);
                    }
                } else if (barChartRadio.isSelected()) {
                    if (valuesNames.size() == 1) {
                        drawValueBarsVertical(timeReports);
                    } else {
                        drawValuesBarsVertical(timeReports);
                    }
                } else if (verticalLineChartRadio.isSelected()) {
                    drawValuesLines(time, timeReports, true);
                } else if (lineChartRadio.isSelected()) {
                    drawValuesLines(time, timeReports, false);
                } else if (pieRadio.isSelected()) {
                    drawPies(time, timeReports);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void drawFrames() {
        drawChart();
    }

    @Override
    public void drawFrame(String time) {
        frameIndex = chartTimes.indexOf(time);
        drawFrame();
    }

    @Override
    public void drawFrame() {
        try {
            setPause(true);
            fixFrameIndex();
            String time = chartTimes.get(frameIndex);
            List<EpidemicReport> timeReports = timesReports.get(time);
            drawChart(time, timeReports);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void fixFrameIndex() {
        if (frameIndex > chartTimes.size() - 1) {
            frameIndex -= chartTimes.size();
        } else if (frameIndex < 0) {
            frameIndex += chartTimes.size();
        }
    }

    protected void drawPies(String time, List<EpidemicReport> timeReports) {
        try {
            if (time == null || timeReports == null || timeReports.isEmpty()) {
                return;
            }
            chartBox.getChildren().clear();
            HBox line = new HBox();
            int colsNum = (int) Math.sqrt(valuesNames.size());
            colsNum = Math.max(colsNum, valuesNames.size() / colsNum);
            for (int i = 0; i < valuesNames.size(); i++) {
                String valueName = valuesNames.get(i);
                if (i % colsNum == 0) {
                    line = new HBox();
                    line.setAlignment(Pos.TOP_CENTER);
                    line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    line.setSpacing(5);
                    VBox.setVgrow(line, Priority.ALWAYS);
                    HBox.setHgrow(line, Priority.ALWAYS);
                    chartBox.getChildren().add(line);
                }
                PieChart pie = new PieChart();
                pie.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(pie, Priority.ALWAYS);
                HBox.setHgrow(pie, Priority.ALWAYS);
                pie.setAnimated(false);
                if (legendSide == null) {
                    pie.setLegendVisible(false);
                } else {
                    pie.setLegendVisible(true);
                    pie.setLegendSide(legendSide);
                }
                pie.setTitle(valueName + " - " + time);
//                pie.setClockwise(true);
                pie.setLabelLineLength(0d);
                line.getChildren().add(pie);
                drawPie(valueName, pie, time, timeReports);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawPie(String valueName, PieChart pie,
            String time, List<EpidemicReport> timeReports) {
        try {
            if (valueName == null || time == null || pie == null
                    || timeReports == null || timeReports.isEmpty()) {
                return;
            }
            float total = 0;
            for (EpidemicReport report : timeReports) {
                Number value = EpidemicReportTools.getNumber(report, valueName);
                if (value == null) {
                    continue;
                }
                total += value.floatValue();
            }
            if (total == 0) {
                return;
            }

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            pie.setData(pieData);

            String label;
            List<String> palette = new ArrayList();
            for (EpidemicReport report : timeReports) {
                String name = (multipleDatasets ? report.getDataSet() + " - " : "") + report.getLocationFullName();
                Number value = EpidemicReportTools.getNumber(report, valueName);
                if (value == null) {
                    continue;
                }
                double d = value.doubleValue();
                double percent = DoubleTools.scale(d * 100 / total, 1);
                String labelValue = StringTools.format(d);
                switch (labelType) {
                    case Name:
                        label = name;
                        break;
                    case Value:
                        label = percent + "% " + labelValue;
                        break;
                    case NameAndValue:
                        label = name + " " + percent + "% " + labelValue;
                        break;
                    case NotDisplay:
                    default:
                        label = "";
                        break;
                }
                PieChart.Data item = new PieChart.Data(label, d);
                pieData.add(item);
                if (labelType == LabelType.Pop) {
                    NodeStyleTools.setTooltip(item.getNode(), name + " " + percent + "% " + labelValue);
                }
                palette.add(colorsController.locationColor(report.getLocationFullName()));
            }

            ChartTools.setPieColors(pie, palette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected LineChart addLinesChart(boolean vertical) {
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setSide(Side.BOTTOM);
        categoryAxis.setTickLabelsVisible(categoryAxisCheck.isSelected());
        categoryAxis.setGapStartAndEnd(true);

        categoryAxis.setTickLabelRotation(90);
        categoryAxis.setAnimated(false);
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setSide(Side.LEFT);
        numberAxis.setAnimated(false);
        LineChart lineChart;
        if (vertical) {
            categoryAxis.setEndMargin(100);
            lineChart = new LineChart(categoryAxis, numberAxis);
        } else {
            categoryAxis.setEndMargin(20);
            lineChart = new LineChart(numberAxis, categoryAxis);
        }
        lineChart.setAlternativeRowFillVisible(false);
        lineChart.setAlternativeColumnFillVisible(false);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lineChart.setVerticalGridLinesVisible(vlinesCheck.isSelected());
        lineChart.setHorizontalGridLinesVisible(hlinesCheck.isSelected());
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        if (legendSide == null) {
            lineChart.setLegendVisible(false);
        } else {
            lineChart.setLegendVisible(true);
            lineChart.setLegendSide(legendSide);
        }
        ChartTools.setChartCoordinate(numberAxis, chartCoordinate);
        return lineChart;
    }

    protected XYChart.Data lineDataNode(String label, String prefix, double value, boolean vertical, boolean last) {
        double coordinateValue = ChartTools.coordinateValue(chartCoordinate, value);
        XYChart.Data data;
        if (vertical) {
            data = new XYChart.Data(label, coordinateValue);
        } else {
            data = new XYChart.Data(coordinateValue, label);
        }
        String finalLabel = prefix == null ? label : prefix + "\n" + label;
        String finalValue = StringTools.format(value);
        if (last) {
            String valueLabel = finalLabel + " " + finalValue;
            Label text = new Label(valueLabel);
            text.setStyle("-fx-background-color: transparent;  -fx-font-size: " + mapOptionsController.textSize + "px; -fx-font-weight: bolder;");
            data.setNode(text);
        } else if (labelType == LabelType.Pop) {
            Label text = new Label("");
            text.setStyle("-fx-background-color: transparent;  -fx-font-size: " + mapOptionsController.textSize + "px; -fx-font-weight: bolder;");
            data.setNode(text);
            NodeStyleTools.setTooltip(text, finalLabel + " " + finalValue);
        } else {
            String valueLabel;
            switch (labelType) {
                case Name:
                    valueLabel = finalLabel;
                    break;
                case NameAndValue:
                    valueLabel = finalLabel + " " + finalValue;
                    break;
                case Value:
                    valueLabel = finalValue;
                    break;
                default:
                    valueLabel = "";
                    break;
            }
            Label text = new Label(valueLabel);
            text.setStyle("-fx-background-color: transparent;  -fx-font-size: " + mapOptionsController.textSize + "px;");
            data.setNode(text);
        }
        return data;
    }

    protected void drawValuesLines(String time, List<EpidemicReport> timeReports, boolean vertical) {
        try {
            if (locationsReports == null
                    || time == null || timeReports == null || timeReports.isEmpty()) {
                return;
            }
            chartBox.getChildren().clear();
            HBox line = new HBox();
            int colsNum = (int) Math.sqrt(valuesNames.size());
            colsNum = Math.max(colsNum, valuesNames.size() / colsNum);
            for (int i = 0; i < valuesNames.size(); i++) {
                String valueName = valuesNames.get(i);
                if (i % colsNum == 0) {
                    line = new HBox();
                    line.setAlignment(Pos.TOP_CENTER);
                    line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    line.setSpacing(5);
                    VBox.setVgrow(line, Priority.ALWAYS);
                    HBox.setHgrow(line, Priority.ALWAYS);
                    chartBox.getChildren().add(line);
                }
                LineChart lineChart = addLinesChart(vertical);
                lineChart.setTitle(valueName + " - " + time);
                lineChart.setAlternativeRowFillVisible(false);
                lineChart.setAlternativeColumnFillVisible(false);
                lineChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(lineChart, Priority.ALWAYS);
                HBox.setHgrow(lineChart, Priority.ALWAYS);
                line.getChildren().add(lineChart);
                drawValueLines(valueName, lineChart, time, vertical);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawValueLines(String valueName, LineChart lineChart,
            String time, boolean vertical) {
        try {
            if (valueName == null || lineChart == null
                    || time == null || locationsReports == null) {
                return;
            }
            Map<String, String> palette = new HashMap();
            Map<String, XYChart.Series> seriesMap = new HashMap();
            for (int i = 0; i < chartTimes.size(); i++) {
                String date = chartTimes.get(i);
                int dateCompare = date.compareTo(time);
                if (dateCompare > 0) {
                    break;
                }
                List<EpidemicReport> reports = locationsReports.get(date);
                if (reports == null || reports.isEmpty()) {
                    continue;
                }
                for (int j = 0; j < reports.size(); j++) {
                    EpidemicReport report = reports.get(j);
                    Number value = EpidemicReportTools.getNumber(report, valueName);
                    if (value == null) {
                        continue;
                    }
                    GeographyCode location = report.getLocation();
                    String locationName = location.getFullName();
                    String lineName = (multipleDatasets ? report.getDataSet() + " - " : "") + locationName;
                    XYChart.Series series = seriesMap.get(lineName);
                    if (series == null) {
                        series = new XYChart.Series();
                        series.setName(lineName);
                        lineChart.getData().add(series);
                        seriesMap.put(lineName, series);
                        palette.put(lineName, colorsController.locationColor(locationName));
                    }
                    if (dateCompare == 0) {
                        series.getData().add(lineDataNode(date, lineName, value.doubleValue(), vertical, true));
                    } else if (multipleDatasets) {
                        series.getData().add(lineDataNode(date, report.getDataSet(), value.doubleValue(), vertical, false));
                    } else {
                        series.getData().add(lineDataNode(date, null, value.doubleValue(), vertical, false));
                    }
                }
            }

            ChartTools.setLineChartColors(lineChart, 4, palette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected LabeledBarChart addVerticalBarChart() {
        chartBox.getChildren().clear();
//        boolean intValue = !message("HealedRatio").equals(valueName)
//                && !message("DeadRatio").equals(valueName);
        LabeledBarChart barChart = new LabeledBarChart(new CategoryAxis(), new NumberAxis());
        barChart.setLabelType(labelType)
                .setTextSize(mapOptionsController.textSize)
                .setChartCoordinate(chartCoordinate);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);
        barChart.setBarGap(0.0);
        barChart.setCategoryGap(0.0);
        barChart.setAnimated(false);
        barChart.getXAxis().setAnimated(false);
        barChart.getYAxis().setAnimated(false);
        barChart.getXAxis().setTickLabelRotation(90);
        barChart.setVerticalGridLinesVisible(vlinesCheck.isSelected());
        barChart.setHorizontalGridLinesVisible(hlinesCheck.isSelected());
        barChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        if (legendSide == null) {
            barChart.setLegendVisible(false);
        } else {
            barChart.setLegendVisible(true);
            barChart.setLegendSide(legendSide);
        }
        chartBox.getChildren().add(barChart);
        return barChart;
    }

    protected LabeledBarChart addHorizontalBarChart() {
        chartBox.getChildren().clear();
        LabeledBarChart barChart = new LabeledBarChart(new NumberAxis(), new CategoryAxis());
        barChart.setLabelType(labelType)
                .setTextSize(mapOptionsController.textSize)
                .setChartCoordinate(chartCoordinate);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);
        barChart.setBarGap(0.0);
        barChart.setCategoryGap(0.0);
        barChart.setAnimated(false);
        barChart.getXAxis().setAnimated(false);
        barChart.getYAxis().setAnimated(false);
        barChart.setVerticalGridLinesVisible(vlinesCheck.isSelected());
        barChart.setHorizontalGridLinesVisible(hlinesCheck.isSelected());
        barChart.getXAxis().setTickLabelRotation(90);
        barChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        if (legendSide == null) {
            barChart.setLegendVisible(false);
        } else {
            barChart.setLegendVisible(true);
            barChart.setLegendSide(legendSide);
        }
        chartBox.getChildren().add(barChart);
        return barChart;
    }

    protected void drawValuesBarsVertical(List<EpidemicReport> timeReports) {
        try {
            if (valuesNames.size() <= 1 || timeReports == null || timeReports.isEmpty()) {
                return;
            }
            LabeledBarChart barChart = addVerticalBarChart();

            Map<String, String> palette = new HashMap();
            List<XYChart.Series> seriesList = new ArrayList<>();
            for (int i = 0; i < valuesNames.size(); i++) {
                String valueName = valuesNames.get(i);
                XYChart.Series series = new XYChart.Series();
                series.setName(valueName);
                seriesList.add(series);
                barChart.getData().add(i, series);
                palette.put(valueName, colorsController.rgb(valueName));
            }
            for (EpidemicReport report : timeReports) {
                String label = (multipleDatasets ? report.getDataSet() + " - " : "") + report.getLocationFullName();
                for (int i = 0; i < valuesNames.size(); i++) {
                    XYChart.Series series = seriesList.get(i);
                    String valueName = valuesNames.get(i);
                    Number value = EpidemicReportTools.getNumber(report, valueName);
                    if (value == null) {
                        continue;
                    }
                    double coordinateValue = ChartTools.coordinateValue(chartCoordinate, value.doubleValue());
                    XYChart.Data item = new XYChart.Data(label, coordinateValue);
                    series.getData().add(item);
                }
            }
            ChartTools.setBarChartColors(barChart, palette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawValuesBarsHorizontal(List<EpidemicReport> timeReports) {
        try {
            if (valuesNames.size() <= 1 || timeReports == null || timeReports.isEmpty()) {
                return;
            }
            LabeledBarChart barChart = addHorizontalBarChart();

            Map<String, String> palette = new HashMap();
            List<XYChart.Series> seriesList = new ArrayList<>();
            for (int i = 0; i < valuesNames.size(); i++) {
                String valueName = valuesNames.get(i);
                XYChart.Series series = new XYChart.Series();
                series.setName(valueName);
                seriesList.add(series);
                barChart.getData().add(i, series);
                palette.put(valueName, colorsController.rgb(valueName));
            }
            for (int i = timeReports.size() - 1; i >= 0; i--) {
                EpidemicReport report = timeReports.get(i);
                String label = (multipleDatasets ? report.getDataSet() + " - " : "") + report.getLocationFullName();
                for (int j = 0; j < valuesNames.size(); j++) {
                    XYChart.Series series = seriesList.get(j);
                    String valueName = valuesNames.get(j);
                    Number value = EpidemicReportTools.getNumber(report, valueName);
                    if (value == null) {
                        continue;
                    }
                    double coordinateValue = ChartTools.coordinateValue(chartCoordinate, value.doubleValue());
                    XYChart.Data item = new XYChart.Data(coordinateValue, label);
                    series.getData().add(item);
                }
            }

            ChartTools.setBarChartColors(barChart, palette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawValueBarsVertical(List<EpidemicReport> timeReports) {
        if (valuesNames.size() != 1 || timeReports == null || timeReports.isEmpty()) {
            return;
        }
        String valueName = orderNames.get(0);
        LabeledBarChart barChart = addVerticalBarChart();

        Map<String, String> palette = new HashMap();
        for (int i = 0; i < timeReports.size(); i++) {
            EpidemicReport report = timeReports.get(i);
            String location = report.getLocationFullName();
            String label = (multipleDatasets ? report.getDataSet() + " - " : "") + location;

            Number value = EpidemicReportTools.getNumber(report, valueName);
            if (value == null) {
                continue;
            }
            double coordinateValue = ChartTools.coordinateValue(chartCoordinate, value.doubleValue());
            XYChart.Series series = new XYChart.Series();
            series.setName(label);
            palette.put(label, colorsController.locationColor(location));
            XYChart.Data item = new XYChart.Data(label, coordinateValue);
            series.getData().add(item);
            barChart.getData().add(series);
        }
        ChartTools.setBarChartColors(barChart, palette, legendSide != null);
    }

    protected void drawValueBarsHorizontal(List<EpidemicReport> timeReports) {
        if (valuesNames.size() != 1 || timeReports == null || timeReports.isEmpty()) {
            return;
        }
        String valueName = orderNames.get(0);
        LabeledBarChart barChart = addHorizontalBarChart();

        Map<String, String> palette = new HashMap();
        for (int i = timeReports.size() - 1; i >= 0; i--) {
            EpidemicReport report = timeReports.get(i);
            String location = report.getLocationFullName();
            String label = (multipleDatasets ? report.getDataSet() + " - " : "") + location;
            palette.put(label, colorsController.locationColor(location));
            Number value = EpidemicReportTools.getNumber(report, valueName);
            if (value == null) {
                continue;
            }
            double coordinateValue = ChartTools.coordinateValue(chartCoordinate, value.doubleValue());
            XYChart.Series series = new XYChart.Series();
            series.setName(label);
            XYChart.Data item = new XYChart.Data(coordinateValue, label);
            series.getData().add(item);
            barChart.getData().add(series);
        }
        ChartTools.setBarChartColors(barChart, palette, legendSide != null);
    }

    protected void drawMap(List<EpidemicReport> timeReports) {
        try {
            if (timeReports == null || timeReports.isEmpty()
                    || webEngine == null || !mapOptionsController.mapLoaded) {
                return;
            }
            webEngine.executeScript("clearMap();");
            for (EpidemicReport report : timeReports) {
                GeographyCode location = report.getLocation();
                if (!GeographyCodeTools.validCoordinate(location)) {
                    continue;
                }
                if (!mapCentered) {
                    webEngine.executeScript("setCenter("
                            + location.getLongitude() + "," + location.getLatitude() + ");");
                    mapCentered = true;
                }
                Color textColor = textColor();
                String name = (multipleDatasets ? report.getDataSet() + " - " : "") + location.getFullName();
                name = textColor == null ? name
                        : "<span style=\"color:" + FxColorTools.color2rgb(textColor) + "\">" + name + "</span>";
                String value = "";
                for (String valueName : valuesNames) {
                    value += " <span style=\"color:" + colorsController.rgb(valueName) + "\">" + EpidemicReportTools.getNumber(report, valueName) + "</span> ";
                }
                String label;
                switch (labelType) {
                    case Name:
                        label = "<div>" + name + "</div>";
                        break;
                    case NameAndValue:
                        label = "<div>" + name + value + "</div>";
                        break;
                    case NotDisplay:
                    case Pop:
                        label = "";
                        break;
                    case Value:
                    default:
                        label = value;
                        break;
                }
                String info = mapOptionsController.popInfoCheck.isSelected()
                        ? "<div>" + name + value + "</div></br>"
                        + DataFactory.displayData(reportsController.tableDefinition, report, displayNames(), true) : "";
                mapOptionsController.markerSize = markSize(EpidemicReportTools.getNumber(report, orderNames.get(0)).doubleValue());
                drawPoint(location.getLongitude(), location.getLatitude(),
                        label, circleImage(), info, null);
            }
//            if (mapOptionsController.mapName == MapName.GaoDe) {
//                webEngine.executeScript("map.setFitView();");
//            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    // maximum marker size of GaoDe Map is 64
    protected int markSize(double value) {
        if (maxValue == 0) {
            mapOptionsController.markerSize = 20;
            return mapOptionsController.markerSize;
        }
        double d, m;
//        switch (chartCoordinate) {
//            case LogarithmicE:
//                d = Math.log(value);
//                m = Math.log(maxValue);
//                break;
//            case Logarithmic10:
//                d = Math.log10(value);
//                m = Math.log10(maxValue);
//                break;
//            case SquareRoot:
//                d = Math.sqrt(value);
//                m = Math.sqrt(maxValue);
//                break;
//            default:
//                d = value;
//                m = maxValue;
//        }
        d = Math.log(value);
        m = Math.log(maxValue);
        mapOptionsController.markerSize = Math.min(60, Math.max(10, (int) (d * 60 / m)));
        return mapOptionsController.markerSize;
    }

    @Override
    public void drawPoints() {
        if (mapRadio.isSelected()) {
            drawChart();
        }
    }

    @Override
    protected void snapAllMenu() {
        MenuItem menu = new MenuItem(message("JpgAllFrames"));
        menu.setOnAction((ActionEvent event) -> {
            snapAllFrames("jpg");
        });
        popMenu.getItems().add(menu);

        menu = new MenuItem(message("PngAllFrames"));
        menu.setOnAction((ActionEvent event) -> {
            snapAllFrames("png");
        });
        popMenu.getItems().add(menu);

        menu = new MenuItem(message("GifAllFrames"));
        menu.setOnAction((ActionEvent event) -> {
            snapAllFrames("gif");
        });
        popMenu.getItems().add(menu);
    }

    @Override
    protected void snapAllFrames(String format) {
        try {
            if (isSettingValues) {
                return;
            }
            DirectoryChooser chooser = new DirectoryChooser();
            File path = UserConfig.getPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image));
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory, VisitHistory.FileType.Image);
            String snapName = snapName(false);
            File filePath = new File(directory.getAbsolutePath() + File.separator + snapName + File.separator);
            filePath.mkdirs();
            final String filePrefix = filePath + File.separator + snapName;

            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            setPause(false);
            frameIndex = 0;
            frameCompleted = true;
            webEngine.executeScript("clearMap();");
            titleLabel.setText("");
            frameLabel.setText("");
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }

            String rformat = format.equals("gif") ? "png" : format;
            final SnapshotParameters snapPara;
            final double scale;
            double scalev = NodeTools.dpiScale(dpi);
            scale = scalev > 1 ? scalev : 1;
            snapPara = new SnapshotParameters();
            snapPara.setFill(Color.WHITE);
            snapPara.setTransform(Transform.scale(scale, scale));

            List<File> snapshots = new ArrayList<>();
            loading = handling();
            snapEnd = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {

                private Timer frameTimer;

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (!snapEnd || timer == null) {
                            return;
                        }
                        if (loading == null || loading.isIsCanceled()
                                || timesReports == null || timesReports.isEmpty()) {
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            if (frameTimer != null) {
                                frameTimer.cancel();
                                frameTimer = null;
                            }
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                            drawChart();
                            return;
                        }
                        snapEnd = false;
                        Platform.runLater(() -> {
                            loading.setInfo(message("Snapping") + ": " + (frameIndex + 1) + "/" + chartTimes.size());
                        });
                        if (frameTimer != null) {
                            frameTimer.cancel();
                            frameTimer = null;
                        }
                        drawFrame();
                        frameTimer = new Timer();
                        frameTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (frameTimer == null) {
                                    return;
                                }
                                if (frameCompleted) {
                                    if (frameTimer != null) {
                                        frameTimer.cancel();
                                        frameTimer = null;
                                    }
                                    Platform.runLater(() -> {
                                        snap();
                                    });
                                }
                            }
                        }, 50, 50);
                    });
                }

                private void snap() {
                    try {
                        Image snap = snapBox.snapshot(snapPara, null);
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snap, null);
                        if (format.equals("gif") && bufferedImage.getWidth() > snapWidth) {
                            bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, snapWidth);
                        }
                        File imageFile = new File(filePrefix + "_Frame" + frameIndex + "." + rformat);
                        ImageFileWriters.writeImageFile(bufferedImage, rformat, imageFile.getAbsolutePath());
                        snapshots.add(imageFile);

                        frameIndex++;
                        if (frameIndex >= chartTimes.size()) {
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            if (frameTimer != null) {
                                frameTimer.cancel();
                                frameTimer = null;
                            }
                            if (snapshots.size() == 1) {
                                ControllerTools.openImageViewer(snapshots.get(0));
                            } else if (snapshots.size() > 1) {
                                if (format.equals("gif")) {
                                    File gifFile = new File(filePrefix + ".gif");
                                    if (loading != null) {
                                        Platform.runLater(() -> {
                                            loading.setInfo(message("Saving") + ": " + gifFile);
                                        });
                                    }
                                    ImageGifFile.writeImageFiles(snapshots, gifFile, interval, true);
                                    if (gifFile.exists()) {
                                        if (loading != null) {
                                            Platform.runLater(() -> {
                                                loading.setInfo(message("Opening") + ": " + gifFile);
                                            });
                                        }
                                        ImagesEditorController controller = (ImagesEditorController) openStage(Fxmls.ImagesEditorFxml);
                                        controller.open(gifFile);
                                    }

                                } else {
                                    browseURI(filePath.toURI());
                                }
                            }
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                            drawChart();
                        }

                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    snapEnd = true;
                }

            }, 0, 100);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    protected List<String> displayNames() {
        return Arrays.asList("data_set", " time", "confirmed", "healed", "dead",
                "increased_confirmed", "increased_healed", "increased_dead",
                "level", " longitude", "latitude",
                "chinese_name", "english_name", "alias1", "code1", "area", "population");
    }

    @Override
    protected String writePointsTable() {
        try {
            List<BaseData> list = new ArrayList<>();
            for (EpidemicReport report : timesReports.get(chartTimes.get(frameIndex))) {
                if (task == null || task.isCancelled()) {
                    return "";
                }
                list.add(report);
            }
            return DataFactory.htmlDataList(reportsController.tableDefinition, list, displayNames());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void clearAction() {
        webEngine.executeScript("clearMap();");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get/set
     */
    public EpidemicReportsController getReportsController() {
        return reportsController;
    }

    public void setReportsController(EpidemicReportsController reportsController) {
        this.reportsController = reportsController;
    }

    public EpidemicReportsColorsController getColorsController() {
        return colorsController;
    }

    public void setColorsController(EpidemicReportsColorsController colorsController) {
        this.colorsController = colorsController;
    }

}
