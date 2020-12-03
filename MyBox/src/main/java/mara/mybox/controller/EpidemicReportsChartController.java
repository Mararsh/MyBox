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
import javafx.geometry.Bounds;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.QueryCondition;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlControl.ChartCoordinate;
import mara.mybox.fxml.FxmlControl.LabelType;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.Logarithmic10Coordinate;
import mara.mybox.fxml.LogarithmicECoordinate;
import mara.mybox.fxml.SquareRootCoordinate;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import thridparty.LabeledBarChart;
import thridparty.LabeledHorizontalBarChart;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class EpidemicReportsChartController extends GeographyCodeMapController {

    protected EpidemicReportsController reportsController;
    protected EpidemicReportsSettingsController settingsController;
    protected QueryCondition queryCondition;
    protected String chartQuerySQL, chartName;
    protected List<String> orderNames, valuesNames;
    protected int topNumber, snapWidth, mapLoadTime, totalSize;
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
    protected TabPane optionsTabPane;
    @FXML
    protected Tab chartOptionsTab, mapOptionsTab;
    @FXML
    protected ComboBox<String> labelSizeSelector;
    @FXML
    protected ToggleGroup chartGroup, labelGroup, legendGroup, numberCoordinateGroup;
    @FXML
    protected HBox chartTypeBox, titleBox;
    @FXML
    protected VBox viewBox, chartBox, mapBox, valueTypeBox, chartSnapBox;
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
            horizontalBarsChartRadio, verticalBarsChartRadio, linesChartRadio, linesChartHRadio, pieRadio, mapRadio,
            cartesianRadio, logarithmicERadio, logarithmic10Radio, squareRootRadio;

    public EpidemicReportsChartController() {
        baseTitle = AppVariables.message("EpidemicReport");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            valuesNames = new ArrayList();
            initChartOptions();
            initMapOptions();

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
            labelSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                mapOptionsController.textSize = v;
                                labelSizeSelector.getEditor().setStyle(null);
                                AppVariables.setUserConfigInt("EpidemicReportChartTextSize", mapOptionsController.textSize);
                                if (!isSettingValues) {
                                    drawChart();
                                }
                            } else {
                                labelSizeSelector.getEditor().setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            labelSizeSelector.getEditor().setStyle(badStyle);
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

            categoryAxisCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue("EpidemicReportDisplayCategoryAxis", newValue);
                        drawChart();
                    });

            loopCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue("EpidemicReportChartLoop", newValue);
                        drawChart();
                    });

            hlinesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue("EpidemicReportDisplayHlines", newValue);
                        drawChart();
                    });

            vlinesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue("EpidemicReportDisplayVlines", newValue);
                        drawChart();
                    });

            isSettingValues = true;
            labelSizeSelector.getSelectionModel().select(AppVariables.getUserConfigValue("EpidemicReportChartTextSize", "12"));
            categoryAxisCheck.setSelected(AppVariables.getUserConfigBoolean("EpidemicReportDisplayCategoryAxis", false));
            hlinesCheck.setSelected(AppVariables.getUserConfigBoolean("EpidemicReportDisplayHlines", true));
            vlinesCheck.setSelected(AppVariables.getUserConfigBoolean("EpidemicReportDisplayVlines", true));
            loopCheck.setSelected(AppVariables.getUserConfigBoolean("EpidemicReportChartLoop", true));
            isSettingValues = false;

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
        settingsController.reset();
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
        totalSize = reportsController.totalSize;
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
            playBox.setVisible(true);
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
            playBox.setVisible(false);
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
//                chartOptionsTab.setDisable(true);
                mapOptionsTab.setDisable(false);
//                optionsTabPane.getSelectionModel().select(mapOptionsTab);
                chartName = message("Map");
                chartBox.getChildren().clear();
                chartBox.setVisible(false);
                mapBox.setVisible(true);
                mapBox.toFront();
            } else {
//                chartOptionsTab.setDisable(false);
                mapOptionsTab.setDisable(true);
                optionsTabPane.getSelectionModel().select(chartOptionsTab);
                mapBox.setVisible(false);
                chartBox.setVisible(true);
                chartBox.toFront();
                if (horizontalBarsChartRadio.isSelected()) {
                    chartName = message("HorizontalBarsChart");
                } else if (verticalBarsChartRadio.isSelected()) {
                    chartName = message("VerticalBarsChart");
                } else if (linesChartRadio.isSelected()) {
                    if (locationsReports == null) {
                        return;
                    }
                    chartName = message("VerticalLinesChart");
                } else if (linesChartHRadio.isSelected()) {
                    if (locationsReports == null) {
                        return;
                    }
                    chartName = message("HorizontalLinesChart");
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
                if (horizontalBarsChartRadio.isSelected()) {
                    if (valuesNames.size() == 1) {
                        drawValueBarsHorizontal(timeReports);
                    } else {
                        drawValuesBarsHorizontal(timeReports);
                    }
                } else if (verticalBarsChartRadio.isSelected()) {
                    if (valuesNames.size() == 1) {
                        drawValueBarsVertical(timeReports);
                    } else {
                        drawValuesBarsVertical(timeReports);
                    }
                } else if (linesChartRadio.isSelected()) {
                    drawValuesLines(time, timeReports, true);
                } else if (linesChartHRadio.isSelected()) {
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
                Number value = report.getNumber(valueName);
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
                Number value = report.getNumber(valueName);
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
                    FxmlControl.setTooltip(item.getNode(), name + " " + percent + "% " + labelValue);
                }
                palette.add(settingsController.locationColor(report.getLocationFullName()));
            }

            FxmlControl.setPieColors(pie, palette, legendSide != null);
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
        return lineChart;
    }

    protected XYChart.Data lineDataNode(String label, String prefix, double value, boolean vertical, boolean last) {
        double coordinateValue = FxmlControl.coordinateValue(chartCoordinate, value);
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
            FxmlControl.setTooltip(text, finalLabel + " " + finalValue);
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
                    Number value = report.getNumber(valueName);
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
                        palette.put(lineName, settingsController.locationColor(locationName));
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

            FxmlControl.setLineChartColors(lineChart, palette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected LabeledBarChart addVerticalBarChart() {
        chartBox.getChildren().clear();
//        boolean intValue = !message("HealedRatio").equals(valueName)
//                && !message("DeadRatio").equals(valueName);
        LabeledBarChart barChart
                = LabeledBarChart.create(categoryAxisCheck.isSelected(), chartCoordinate)
                        .setIntValue(false)
                        .setLabelType(labelType)
                        .setTextSize(mapOptionsController.textSize);
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

    protected LabeledHorizontalBarChart addHorizontalBarChart() {
        chartBox.getChildren().clear();
        LabeledHorizontalBarChart barChart
                = LabeledHorizontalBarChart.create(categoryAxisCheck.isSelected(), chartCoordinate)
                        .setIntValue(false)
                        .setLabelType(labelType)
                        .setTextSize(mapOptionsController.textSize);
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
                palette.put(valueName, settingsController.rgb(valueName));
            }
            for (EpidemicReport report : timeReports) {
                String label = (multipleDatasets ? report.getDataSet() + " - " : "") + report.getLocationFullName();
                for (int i = 0; i < valuesNames.size(); i++) {
                    XYChart.Series series = seriesList.get(i);
                    String valueName = valuesNames.get(i);
                    Number value = report.getNumber(valueName);
                    if (value == null) {
                        continue;
                    }
                    double coordinateValue = FxmlControl.coordinateValue(chartCoordinate, value.doubleValue());
                    XYChart.Data item = new XYChart.Data(label, coordinateValue);
                    series.getData().add(item);
                    if (labelType == LabelType.Pop) {
                        FxmlControl.setTooltip(item.getNode(), label + " " + value);
                    }
                }
            }
            FxmlControl.setBarChartColors(barChart, palette, legendSide != null);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawValuesBarsHorizontal(List<EpidemicReport> timeReports) {
        try {
            if (valuesNames.size() <= 1 || timeReports == null || timeReports.isEmpty()) {
                return;
            }
            LabeledHorizontalBarChart barChart = addHorizontalBarChart();

            Map<String, String> palette = new HashMap();
            List<XYChart.Series> seriesList = new ArrayList<>();
            for (int i = 0; i < valuesNames.size(); i++) {
                String valueName = valuesNames.get(i);
                XYChart.Series series = new XYChart.Series();
                series.setName(valueName);
                seriesList.add(series);
                barChart.getData().add(i, series);
                palette.put(valueName, settingsController.rgb(valueName));
            }
            for (int i = timeReports.size() - 1; i >= 0; i--) {
                EpidemicReport report = timeReports.get(i);
                String label = (multipleDatasets ? report.getDataSet() + " - " : "") + report.getLocationFullName();
                for (int j = 0; j < valuesNames.size(); j++) {
                    XYChart.Series series = seriesList.get(j);
                    String valueName = valuesNames.get(j);
                    Number value = report.getNumber(valueName);
                    if (value == null) {
                        continue;
                    }
                    double coordinateValue = FxmlControl.coordinateValue(chartCoordinate, value.doubleValue());
                    XYChart.Data item = new XYChart.Data(coordinateValue, label);
                    series.getData().add(item);
                    if (labelType == LabelType.Pop) {
                        FxmlControl.setTooltip(item.getNode(), label + " " + value);
                    }
                }
            }

            FxmlControl.setBarChartColors(barChart, palette, legendSide != null);
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

            Number value = report.getNumber(valueName);
            if (value == null) {
                continue;
            }
            double coordinateValue = FxmlControl.coordinateValue(chartCoordinate, value.doubleValue());
            XYChart.Series series = new XYChart.Series();
            series.setName(label);
            palette.put(label, settingsController.locationColor(location));
            XYChart.Data item = new XYChart.Data(label, coordinateValue);
            series.getData().add(item);
            barChart.getData().add(series);
            if (labelType == LabelType.Pop) {
                FxmlControl.setTooltip(item.getNode(), label + " " + value);
            }
        }
        FxmlControl.setBarChartColors(barChart, palette, legendSide != null);
    }

    protected void drawValueBarsHorizontal(List<EpidemicReport> timeReports) {
        if (valuesNames.size() != 1 || timeReports == null || timeReports.isEmpty()) {
            return;
        }
        String valueName = orderNames.get(0);
        LabeledHorizontalBarChart barChart = addHorizontalBarChart();

        Map<String, String> palette = new HashMap();
        for (int i = timeReports.size() - 1; i >= 0; i--) {
            EpidemicReport report = timeReports.get(i);
            String location = report.getLocationFullName();
            String label = (multipleDatasets ? report.getDataSet() + " - " : "") + location;
            palette.put(label, settingsController.locationColor(location));
            Number value = report.getNumber(valueName);
            if (value == null) {
                continue;
            }
            double coordinateValue = FxmlControl.coordinateValue(chartCoordinate, value.doubleValue());
            XYChart.Series series = new XYChart.Series();
            series.setName(label);
            XYChart.Data item = new XYChart.Data(coordinateValue, label);
            series.getData().add(item);
            barChart.getData().add(series);
            if (labelType == LabelType.Pop) {
                FxmlControl.setTooltip(item.getNode(), label + " " + value);
            }
        }
        FxmlControl.setBarChartColors(barChart, palette, legendSide != null);
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
                if (!location.validCoordinate()) {
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
                        : "<span style=\"color:" + FxmlColor.color2rgb(textColor) + "\">" + name + "</span>";
                String value = "";
                for (String valueName : valuesNames) {
                    value += " <span style=\"color:" + settingsController.rgb(valueName) + "\">" + report.getNumber(valueName) + "</span> ";
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
                        ? "<div>" + name + value + "</div></br>" + location.info("</br>") : "";
                mapOptionsController.markerSize = markSize(report.getNumber(orderNames.get(0)).doubleValue());
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

    @FXML
    protected void popSnapMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("SnapCurrentFrame"));
            menu.setOnAction((ActionEvent event) -> {
                snapCurrentFrame();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("JpgAllFrames"));
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

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void snapCurrentFrame() {
        String name = titleLabel.getText()
                + (frameLabel.getText().isBlank() ? "" : " " + frameLabel.getText())
                + ".png";
        File file = chooseSaveFile(AppVariables.getUserConfigPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image)),
                name, CommonFxValues.ImageExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file, VisitHistory.FileType.Image);

        double scale = dpi / Screen.getPrimary().getDpi();
        scale = scale > 1 ? scale : 1;
        SnapshotParameters snapPara = new SnapshotParameters();
        snapPara.setFill(Color.TRANSPARENT);
        snapPara.setTransform(Transform.scale(scale, scale));

        Bounds bounds = chartSnapBox.getLayoutBounds();
        int imageWidth = (int) Math.round(bounds.getWidth() * scale);
        int imageHeight = (int) Math.round(bounds.getHeight() * scale);
        WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
        final Image mapSnap = chartSnapBox.snapshot(snapPara, snapshot);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        String format = FileTools.getFileSuffix(file);
                        format = format == null || format.isBlank() ? "png" : format;
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(mapSnap, null);
                        ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                        return file.exists();
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.error(e.toString());
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    FxmlStage.openImageViewer(file);
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    protected void snapAllFrames(String format) {
        try {
            if (isSettingValues) {
                return;
            }
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image));
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory, VisitHistory.FileType.Image);
            String name = queryCondition.getTitle().replaceAll("\\\"|\n|:", "");
            String filePath = directory.getAbsolutePath() + File.separator + name + File.separator;
            new File(filePath).mkdirs();
            final String filePrefix = filePath + File.separator + name;

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
            snapWidth = settingsController.snapWidth;
            dpi = settingsController.dpi;

            String rformat = format.equals("gif") ? "png" : format;
            final SnapshotParameters snapPara;
            final double scale;
            double scalev = dpi / Screen.getPrimary().getDpi();
            scale = scalev > 1 ? scalev : 1;
            snapPara = new SnapshotParameters();
            snapPara.setFill(Color.WHITE);
            snapPara.setTransform(Transform.scale(scale, scale));

            Bounds bounds = chartSnapBox.getLayoutBounds();
            int imageWidth = (int) Math.round(bounds.getWidth() * scale);
            int imageHeight = (int) Math.round(bounds.getHeight() * scale);

            List<File> snapshots = new ArrayList<>();
            loading = openHandlingStage(Modality.WINDOW_MODAL);
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
                        Image snap = chartSnapBox.snapshot(snapPara, new WritableImage(imageWidth, imageHeight));
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snap, null);
                        if (format.equals("gif") && bufferedImage.getWidth() > snapWidth) {
                            bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, snapWidth);
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
                                FxmlStage.openImageViewer(snapshots.get(0));
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
                                        ImageGifViewerController controller
                                                = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                                        controller.sourceFileChanged(gifFile);
                                    }

                                } else {
                                    browseURI(directory.toURI());
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
    public boolean leavingScene() {
        try {
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
        } catch (Exception e) {
        }
        return super.leavingScene();
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

    public EpidemicReportsSettingsController getSettingsController() {
        return settingsController;
    }

    public void setSettingsController(EpidemicReportsSettingsController settingsController) {
        this.settingsController = settingsController;
    }

}
