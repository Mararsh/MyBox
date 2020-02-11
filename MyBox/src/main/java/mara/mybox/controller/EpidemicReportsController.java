package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.StringTable;
import static mara.mybox.data.StringTable.tableDiv;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlStage.openScene;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.fxml.TableDoubleCell;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReportsController extends TableManageController<EpidemicReport> {

    protected ObservableList<String> dataSets;
    protected String currentDataSet, currentDataQuery, currentSizeQuery, currentTitle;
    protected ChartsType chartsType;
    protected File htmlFile;
    protected Tab currentTab;

    @FXML
    protected TreeView treeView;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab numberTab, ratioTab, confirmedTab, healedTab, deadTab, mapTab;
    @FXML
    protected TableColumn<EpidemicReport, Long> dataidColumn;
    @FXML
    protected TableColumn<EpidemicReport, String> datasetColumn, labelColumn, countryColumn, provinceColumn,
            cityColumn, commentsColumn;
    @FXML
    protected TableColumn<EpidemicReport, Double> longtitudeColumn, latitudeColumn;
    @FXML
    protected TableColumn<EpidemicReport, Date> timeColumn;
    @FXML
    protected TableColumn<EpidemicReport, Integer> confirmedColumn, suspectedColumn, headledColumn, deadColumn;
    @FXML
    protected TableColumn<EpidemicReport, Double> headledRatioColumn, deadRatioColumn;
    @FXML
    protected Button examplesButton, chinaButton;
    @FXML
    protected VBox numberChartBox, ratioChartBox;
    @FXML
    protected BarChart numberBarChart, ratioBarChart;
    @FXML
    protected LineChart numberLineChart, ratioLineChart;
    @FXML
    protected PieChart deadPie, headledPie, confirmedPie;
    @FXML
    protected WebView mapView;
    @FXML
    protected Label nameLabel;
    @FXML
    protected EpidemicReportMapController mapController;

    protected enum ChartsType {
        TimeBasedMap, LocationBased, TimeBasedNoMap, None
    }

    public EpidemicReportsController() {
        baseTitle = message("EpidemicReport");

        dataName = "Epidemic_Report";

        TipsLabelKey = "EpidemicReportsChartColorComments";
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            mapController.parent = this;

            dataSets = FXCollections.observableArrayList();
            currentSizeQuery = " SELECT count(dataid) FROM Epidemic_Report "
                    + " WHERE  country IS NULL AND province IS NULL ";
            currentDataQuery = "SELECT * FROM Epidemic_Report "
                    + " WHERE  country IS NULL AND province IS NULL "
                    + " ORDER BY data_set, time, confirmed desc ";
            currentTitle = message("EpidemicReport");
            chartsType = ChartsType.None;
            loadTree();

            pageSizeSelector.setValue("200");

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    protected void initColumns() {
        try {
            dataidColumn.setCellValueFactory(new PropertyValueFactory<>("dataid"));
            datasetColumn.setCellValueFactory(new PropertyValueFactory<>("dataSet"));
            labelColumn.setCellValueFactory(new PropertyValueFactory<>("dataLabel"));
            countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
            provinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));
            cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
            confirmedColumn.setCellValueFactory(new PropertyValueFactory<>("confirmed"));
            suspectedColumn.setCellValueFactory(new PropertyValueFactory<>("suspected"));
            headledColumn.setCellValueFactory(new PropertyValueFactory<>("healed"));
            headledRatioColumn.setCellValueFactory(new PropertyValueFactory<>("healedRatio"));
            deadColumn.setCellValueFactory(new PropertyValueFactory<>("dead"));
            deadRatioColumn.setCellValueFactory(new PropertyValueFactory<>("deadRatio"));
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
            longtitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            longtitudeColumn.setCellFactory(new TableDoubleCell());
            latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            latitudeColumn.setCellFactory(new TableDoubleCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(examplesButton, message("EpidemicReportsExamplesComments"));
        FxmlControl.setTooltip(chinaButton, message("ChineseProvincesEpidemicReports"));
        if (!AppVariables.getUserConfigBoolean("EpidemicReportExamples", false)) {
            alertInformation(message("EpidemicReportsExamplesComments"));
            AppVariables.setUserConfigValue("EpidemicReportExamples", true);
            examplesAction();
        }
    }

    protected void loadTree() {
        treeView.setRoot(null);
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                private List<String> datasets;
                private Map<String, List<Date>> timesMap;
                private Map<String, List<String>> countriesMap, provincesMap;

                @Override
                protected boolean handle() {
                    datasets = TableEpidemicReport.datasets();
                    if (datasets == null || datasets.isEmpty()) {
                        return true;
                    }
                    timesMap = new HashMap<>();
                    countriesMap = new HashMap<>();
                    provincesMap = new HashMap<>();
                    for (String dataset : datasets) {
                        List<String> countries = TableEpidemicReport.countries(dataset);
                        if (countries != null && !countries.isEmpty()) {
                            countriesMap.put(dataset, countries);
                            for (String country : countries) {
                                List<String> provinces = TableEpidemicReport.provinces(dataset, country);
                                if (provinces != null && !provinces.isEmpty()) {
                                    provincesMap.put(dataset + country, provinces);
                                }
                            }
                        }
                        List<Date> times = TableEpidemicReport.times(dataset);
                        if (times != null && !times.isEmpty()) {
                            timesMap.put(dataset, times);
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (datasets == null || datasets.isEmpty()) {
                        return;
                    }
                    isSettingValues = true;
                    Text allLink = new Text(message("All"));
                    allLink.setOnMouseClicked((MouseEvent event) -> {
                        currentDataSet = null;
                        currentSizeQuery = " SELECT count(dataid) FROM Epidemic_Report "
                                + " WHERE  country IS NULL AND province IS NULL ";
                        currentDataQuery = "SELECT * FROM Epidemic_Report "
                                + " WHERE  country IS NULL AND province IS NULL "
                                + " ORDER BY data_set, time, confirmed desc ";
                        currentTitle = message("EpidemicReport");
                        chartsType = ChartsType.None;
                        loadReports();
                    });
                    TreeItem root = new TreeItem<>(allLink);
                    root.setExpanded(true);
                    treeView.setRoot(root);
                    for (String dataset : datasets) {
                        Text datasetLink = new Text(dataset);
                        datasetLink.setOnMouseClicked((MouseEvent event) -> {
                            currentDataSet = dataset;
                            currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report "
                                    + " WHERE data_set='" + dataset + "' AND country IS NULL AND province IS NULL";
                            currentDataQuery = "SELECT * FROM Epidemic_Report "
                                    + " WHERE data_set='" + dataset + "' AND country IS NULL AND province IS NULL "
                                    + " ORDER BY  time , confirmed desc ";
                            currentTitle = dataset;
                            chartsType = ChartsType.TimeBasedNoMap;
                            loadReports();
                        });
                        TreeItem<Text> datasetItem = new TreeItem<>(datasetLink);
                        datasetItem.setExpanded(true);
                        root.getChildren().add(datasetItem);

                        List<Date> times = timesMap.get(dataset);
                        if (times != null) {
                            Text timesLink = new Text(message("Time"));
                            timesLink.setOnMouseClicked((MouseEvent event) -> {
                                currentDataSet = dataset;
                                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report "
                                        + " WHERE data_set='" + dataset + "' AND country IS NULL AND province IS NULL";
                                currentDataQuery = "SELECT * FROM Epidemic_Report "
                                        + " WHERE data_set='" + dataset + "' AND  country IS NULL AND province IS NULL "
                                        + " ORDER BY  time , confirmed desc ";
                                currentTitle = dataset;
                                chartsType = ChartsType.TimeBasedNoMap;
                                loadReports();
                            });
                            TreeItem<Text> timesItem = new TreeItem<>(timesLink);
                            timesItem.setExpanded(false);
                            datasetItem.getChildren().add(timesItem);

                            for (Date time : times) {
                                Text timeLink = new Text(DateTools.datetimeToString(time));
                                TreeItem<Text> timeItem = new TreeItem<>(timeLink);
                                timeItem.setExpanded(false);
                                timesItem.getChildren().add(timeItem);

                                Text globalLink = new Text(message("Global"));
                                globalLink.setOnMouseClicked((MouseEvent event) -> {
                                    currentDataSet = dataset;
                                    currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report "
                                            + " WHERE data_set='" + dataset + "' AND time='" + DateTools.datetimeToString(time)
                                            + "' AND country IS NOT NULL AND province IS NULL";
                                    currentDataQuery = "SELECT * FROM Epidemic_Report "
                                            + " WHERE data_set='" + dataset + "' AND time='" + DateTools.datetimeToString(time)
                                            + "' AND country IS NOT NULL AND province IS NULL"
                                            + " ORDER BY  time , confirmed desc ";
                                    currentTitle = dataset + " - " + message("Global") + " - " + DateTools.datetimeToString(time);
                                    chartsType = ChartsType.LocationBased;
                                    loadReports();
                                });
                                TreeItem<Text> globalItem = new TreeItem<>(globalLink);
                                globalItem.setExpanded(false);
                                timeItem.getChildren().add(globalItem);

                                Text chinaLink = new Text(message("China"));
                                chinaLink.setOnMouseClicked((MouseEvent event) -> {
                                    currentDataSet = dataset;
                                    currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report "
                                            + " WHERE data_set='" + dataset + "' AND country='" + message("China")
                                            + "' AND time='" + DateTools.datetimeToString(time)
                                            + "' AND province IS NOT NULL";
                                    currentDataQuery = "SELECT * FROM Epidemic_Report "
                                            + " WHERE data_set='" + dataset + "' AND country='" + message("China")
                                            + "' AND time='" + DateTools.datetimeToString(time)
                                            + "' AND province IS NOT NULL"
                                            + " ORDER BY  time , confirmed desc ";
                                    currentTitle = dataset + " - " + message("China") + " - " + DateTools.datetimeToString(time);
                                    chartsType = ChartsType.LocationBased;
                                    loadReports();
                                });
                                TreeItem<Text> chinaItem = new TreeItem<>(chinaLink);
                                chinaItem.setExpanded(false);
                                timeItem.getChildren().add(chinaItem);

                            }
                        }

                        List<String> countries = countriesMap.get(dataset);
                        if (countries != null) {
                            Text countriesLink = new Text(message("Countries"));
                            countriesLink.setOnMouseClicked((MouseEvent event) -> {
                                currentDataSet = dataset;
                                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report "
                                        + " WHERE data_set='" + dataset + "' AND country IS NULL AND province IS NULL";
                                currentDataQuery = "SELECT * FROM Epidemic_Report "
                                        + " WHERE data_set='" + dataset + "' AND  country IS NULL AND province IS NULL "
                                        + " ORDER BY  time , confirmed desc ";
                                currentTitle = dataset;
                                chartsType = ChartsType.TimeBasedNoMap;
                                loadReports();
                            });
                            TreeItem<Text> countriesItem = new TreeItem<>(countriesLink);
                            countriesItem.setExpanded(false);
                            datasetItem.getChildren().add(countriesItem);

                            for (String country : countries) {
                                Text countryLink = new Text(country);
                                countryLink.setOnMouseClicked((MouseEvent event) -> {
                                    currentDataSet = dataset;
                                    currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report WHERE "
                                            + "data_set='" + dataset + "' AND country='" + country + "' AND province IS NULL";
                                    currentDataQuery = "SELECT * FROM Epidemic_Report WHERE "
                                            + "data_set='" + dataset + "' AND country='" + country + "' AND province IS NULL"
                                            + " ORDER BY  time ,confirmed desc ";
                                    currentTitle = dataset + " - " + country;
                                    chartsType = ChartsType.TimeBasedMap;
                                    loadReports();
                                });
                                TreeItem<Text> countryItem = new TreeItem<>(countryLink);
                                countryItem.setExpanded(false);
                                countriesItem.getChildren().add(countryItem);

                                List<String> provinces = provincesMap.get(dataset + country);
                                if (provinces != null) {
                                    for (String province : provinces) {
                                        Text provinceLink = new Text(province);
                                        provinceLink.setOnMouseClicked((MouseEvent event) -> {
                                            currentDataSet = dataset;
                                            currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report WHERE "
                                                    + "data_set='" + dataset + "' AND country='" + country
                                                    + "' AND province='" + province + "'";
                                            currentDataQuery = "SELECT * FROM Epidemic_Report WHERE "
                                                    + "data_set='" + dataset + "' AND country='" + country
                                                    + "' AND province='" + province + "'"
                                                    + " ORDER BY   time ,confirmed desc";
                                            currentTitle = dataset + " - " + country + " - " + province;
                                            chartsType = ChartsType.TimeBasedMap;
                                            loadReports();
                                        });
                                        TreeItem<Text> provinceItem = new TreeItem<>(provinceLink);
                                        provinceItem.setExpanded(false);
                                        countryItem.getChildren().add(provinceItem);
                                    }
                                }

                            }
                        }

                        if (currentSizeQuery != null && currentDataQuery != null) {
                            loadReports();
                        }
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void loadReports() {
        tableData.clear();
        nameLabel.setText(currentTitle);
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                private int total, pagesNumber;
                private List<EpidemicReport> reports;

                @Override
                protected boolean handle() {
                    total = TableEpidemicReport.sizeQuery(currentSizeQuery);
                    if (total <= pageSize) {
                        pagesNumber = 1;
                    } else {
                        pagesNumber = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
                    }
                    if (currentPage <= 0) {
                        currentPage = 1;
                    }
                    if (currentPage > pagesNumber) {
                        currentPage = pagesNumber;
                    }
                    int start = pageSize * (currentPage - 1);
                    int end = Math.min(start + pageSize - 1, total);
                    String sql = currentDataQuery
                            + " OFFSET " + start + " ROWS FETCH NEXT " + (end - start + 1) + " ROWS ONLY";
                    reports = TableEpidemicReport.dataQuery(sql);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (reports != null) {
                        isSettingValues = true;
                        tableData.addAll(reports);
                        isSettingValues = false;
                        tableView.refresh();
                    }
                    checkSelected();
                    setPagination(total, pagesNumber);
                    drawCharts();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void drawCharts() {
        if (isSettingValues) {
            return;
        }
        numberChartBox.getChildren().clear();
        ratioChartBox.getChildren().clear();

        if (chartsType == null || chartsType == ChartsType.None) {
            tabPane.getTabs().removeAll(numberTab, ratioTab, confirmedTab, healedTab, deadTab, mapTab);
            return;
        } else {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            tabPane.getTabs().removeAll(mapTab);
            if (!tabPane.getTabs().contains(numberTab)) {
                tabPane.getTabs().addAll(numberTab, ratioTab);
            }
            if (chartsType == ChartsType.TimeBasedNoMap || chartsType == ChartsType.TimeBasedMap) {
                tabPane.getTabs().removeAll(confirmedTab, healedTab, deadTab);
                numberChartBox.getChildren().add(numberLineChart);
                ratioChartBox.getChildren().add(ratioLineChart);

            } else if (chartsType == ChartsType.LocationBased) {
                numberChartBox.getChildren().add(numberBarChart);
                ratioChartBox.getChildren().add(ratioBarChart);
                if (!tabPane.getTabs().contains(confirmedTab)) {
                    tabPane.getTabs().addAll(confirmedTab, healedTab, deadTab);
                }
            }
            if (chartsType != ChartsType.TimeBasedNoMap) {
                tabPane.getTabs().add(mapTab);
            }
            if (tabPane.getTabs().contains(tab)) {
                tabPane.getSelectionModel().select(tab);
            }
        }

        // https://stackoverflow.com/questions/29124723/javafx-chart-auto-scaling-wrong-with-low-numbers?r=SearchResults
        numberBarChart.setAnimated(false);
        numberBarChart.getData().clear();
        numberBarChart.setAnimated(true);
        numberBarChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        numberBarChart.setTitle(currentTitle + " - " + message("Number"));

        ratioBarChart.setAnimated(false);
        ratioBarChart.getData().clear();
        ratioBarChart.setAnimated(true);
        ratioBarChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        ratioBarChart.setTitle(currentTitle + " - " + message("Ratio"));

        numberLineChart.setAnimated(false);
        numberLineChart.getData().clear();
        numberLineChart.setAnimated(true);
        numberLineChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        numberLineChart.setTitle(currentTitle + " - " + message("Number"));

        ratioLineChart.setAnimated(false);
        ratioLineChart.getData().clear();
        ratioLineChart.setAnimated(true);
        ratioLineChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        ratioLineChart.setTitle(currentTitle + " - " + message("Ratio"));

        confirmedPie.getData().clear();
        confirmedPie.setTitle(currentTitle + " - " + message("Confirmed"));
        headledPie.getData().clear();
        headledPie.setTitle(currentTitle + " - " + message("Healed"));
        deadPie.getData().clear();
        deadPie.setTitle(currentTitle + " - " + message("Dead"));

        mapController.clearAction();

        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                private List<EpidemicReport> reports;

                @Override
                protected boolean handle() {
                    reports = TableEpidemicReport.dataQuery(currentDataQuery);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (chartsType == ChartsType.TimeBasedNoMap) {
                        drawTimeBasedNumberLineChart(reports);
                        drawTimeBasedRatioLineChart(reports);

                    } else if (chartsType == ChartsType.TimeBasedMap) {
                        drawTimeBasedNumberLineChart(reports);
                        drawTimeBasedRatioLineChart(reports);
                        mapController.drawTimeBasedMap(reports);

                    } else if (chartsType == ChartsType.LocationBased) {
                        drawLocationBasedNumberBarChart(reports);
                        drawLocationBasedNumberPieChart(reports);
                        drawLocationBasedRatioBarChart(reports);
                        mapController.drawLocationBasedMap(reports);

                    }

                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void drawTimeBasedNumberLineChart(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }

            XYChart.Series confirmedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getConfirmed());
                Tooltip.install(data.getNode(), new Tooltip(report.getConfirmed() + ""));
                confirmedSeries.getData().add(data);
            }
            confirmedSeries.setName(message("Confirmed"));
            numberLineChart.getData().add(0, confirmedSeries);
            String colorString = FxmlColor.rgb2Hex(Color.BLUE);
            for (Node n
                    : numberLineChart.lookupAll(".default-color0.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            XYChart.Series healedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getHealed());
                Tooltip.install(data.getNode(), new Tooltip(report.getHealed() + ""));
                healedSeries.getData().add(data);
            }
            healedSeries.setName(message("Healed"));
            numberLineChart.getData().add(1, healedSeries);
            colorString = FxmlColor.rgb2Hex(Color.RED);
            for (Node n
                    : numberLineChart.lookupAll(".default-color1.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            XYChart.Series deadSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getDead());
                Tooltip.install(data.getNode(), new Tooltip(report.getDead() + ""));
                deadSeries.getData().add(data);
            }
            deadSeries.setName(message("Dead"));
            numberLineChart.getData().add(2, deadSeries);
            colorString = FxmlColor.rgb2Hex(Color.BLACK);
            for (Node n
                    : numberLineChart.lookupAll(".default-color2.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            numberLineChart.setLegendVisible(true);
            Set<Node> legendItems = numberLineChart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    if (message("Dead").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLACK));
                    } else if (message("Healed").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.RED));
                    } else if (message("Confirmed").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLUE));
                    }
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void drawTimeBasedRatioLineChart(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }

            XYChart.Series healedRatioSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getHealedRatio());
                healedRatioSeries.getData().add(data);
            }
            healedRatioSeries.setName(message("HealedRatio"));
            ratioLineChart.getData().add(0, healedRatioSeries);
            String colorString = FxmlColor.rgb2Hex(Color.RED);
            for (Node n
                    : ratioLineChart.lookupAll(".default-color0.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            XYChart.Series deadRatioSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getDeadRatio());
                deadRatioSeries.getData().add(data);
            }
            deadRatioSeries.setName(message("DeadRatio"));
            ratioLineChart.getData().add(1, deadRatioSeries);
            colorString = FxmlColor.rgb2Hex(Color.BLACK);
            for (Node n
                    : ratioLineChart.lookupAll(".default-color1.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            ratioLineChart.setLegendVisible(true);
            Set<Node> ratioLegendItems = ratioLineChart.lookupAll("Label.chart-legend-item");
            if (ratioLegendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : ratioLegendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    if (message("DeadRatio").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLACK));
                    } else if (message("HealedRatio").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.RED));
                    }
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void drawLocationBasedNumberBarChart(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }

            XYChart.Series confirmedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label;
                if (report.getCountry() != null) {
                    label = report.getCountry();
                } else {
                    label = message("All");
                }
                if (report.getProvince() != null) {
                    label += report.getProvince();
                }
                confirmedSeries.getData().add(new XYChart.Data(label, report.getConfirmed()));
            }
            confirmedSeries.setName(message("Confirmed"));
            numberBarChart.getData().add(0, confirmedSeries);
            String colorString = FxmlColor.rgb2Hex(Color.BLUE);
            for (Node n
                    : numberBarChart.lookupAll(".default-color0.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            XYChart.Series healedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label;
                if (report.getCountry() != null) {
                    label = report.getCountry();
                } else {
                    label = message("All");
                }
                if (report.getProvince() != null) {
                    label += report.getProvince();
                }
                healedSeries.getData().add(new XYChart.Data(label, report.getHealed()));
            }
            healedSeries.setName(message("Healed"));
            numberBarChart.getData().add(1, healedSeries);
            colorString = FxmlColor.rgb2Hex(Color.RED);
            for (Node n
                    : numberBarChart.lookupAll(".default-color1.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            XYChart.Series deadSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label;
                if (report.getCountry() != null) {
                    label = report.getCountry();
                } else {
                    label = message("All");
                }
                if (report.getProvince() != null) {
                    label += report.getProvince();
                }
                deadSeries.getData().add(new XYChart.Data(label, report.getDead()));
            }
            deadSeries.setName(message("Dead"));
            numberBarChart.getData().add(2, deadSeries);
            colorString = FxmlColor.rgb2Hex(Color.BLACK);
            for (Node n
                    : numberBarChart.lookupAll(".default-color2.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            numberBarChart.setLegendVisible(true);
            Set<Node> legendItems = numberBarChart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    if (message("Dead").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLACK));
                    } else if (message("Healed").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.RED));
                    } else if (message("Confirmed").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLUE));
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void drawLocationBasedNumberPieChart(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }
            int confirmed = 0, dead = 0, healed = 0;
            for (EpidemicReport report : reports) {
                confirmed += report.getConfirmed();
                dead += report.getDead();
                healed += report.getHealed();
            }
            if (confirmed > 0) {
                ObservableList<PieChart.Data> confirmedData = FXCollections.observableArrayList();
                for (EpidemicReport report : reports) {
                    String label;
                    if (report.getCountry() != null) {
                        label = report.getCountry();
                    } else {
                        label = message("All");
                    }
                    if (report.getProvince() != null) {
                        label += " " + report.getProvince();
                    }
                    label += " " + DoubleTools.scale(report.getConfirmed() * 100.0d / confirmed, 1) + "%";
                    confirmedData.add(new PieChart.Data(label + " " + report.getConfirmed(), report.getConfirmed()));
                }
                confirmedPie.setData(confirmedData);
            }

            if (dead > 0) {
                ObservableList<PieChart.Data> deadData = FXCollections.observableArrayList();
                for (EpidemicReport report : reports) {
                    String label;
                    if (report.getCountry() != null) {
                        label = report.getCountry();
                    } else {
                        label = message("All");
                    }
                    if (report.getProvince() != null) {
                        label += " " + report.getProvince();
                    }
                    label += " " + DoubleTools.scale(report.getDead() * 100.0d / dead, 1) + "%";
                    deadData.add(new PieChart.Data(label + " " + report.getDead(), report.getDead()));
                }
                deadPie.setData(deadData);
            }

            if (healed > 0) {
                ObservableList<PieChart.Data> headedData = FXCollections.observableArrayList();
                for (EpidemicReport report : reports) {
                    String label;
                    if (report.getCountry() != null) {
                        label = report.getCountry();
                    } else {
                        label = message("All");
                    }
                    if (report.getProvince() != null) {
                        label += " " + report.getProvince();
                    }
                    label += " " + DoubleTools.scale(report.getHealed() * 100.0d / healed, 1) + "%";
                    headedData.add(new PieChart.Data(label + " " + report.getHealed(), report.getHealed()));
                }
                headledPie.setData(headedData);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void drawLocationBasedRatioBarChart(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }

            XYChart.Series healedRatioSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label;
                if (report.getCountry() != null) {
                    label = report.getCountry();
                } else {
                    label = message("All");
                }
                if (report.getProvince() != null) {
                    label += report.getProvince();
                }
                healedRatioSeries.getData().add(new XYChart.Data(label, report.getHealedRatio()));
            }
            healedRatioSeries.setName(message("HealedRatio"));
            ratioBarChart.getData().add(0, healedRatioSeries);
            String colorString = FxmlColor.rgb2Hex(Color.RED);
            for (Node n
                    : ratioBarChart.lookupAll(".default-color0.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            XYChart.Series deadRatioSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label;
                if (report.getCountry() != null) {
                    label = report.getCountry();
                } else {
                    label = message("All");
                }
                if (report.getProvince() != null) {
                    label += report.getProvince();
                }
                deadRatioSeries.getData().add(new XYChart.Data(label, report.getDeadRatio()));
            }
            deadRatioSeries.setName(message("DeadRatio"));
            ratioBarChart.getData().add(1, deadRatioSeries);
            colorString = FxmlColor.rgb2Hex(Color.BLACK);
            for (Node n
                    : ratioBarChart.lookupAll(".default-color1.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            ratioBarChart.setLegendVisible(true);
            Set<Node> ratioLegendItems = ratioBarChart.lookupAll("Label.chart-legend-item");
            if (ratioLegendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : ratioLegendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    if (message("DeadRatio").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLACK));
                    } else if (message("HealedRatio").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.RED));
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    @FXML
    @Override
    public void addAction() {
        try {
            EpidemicReportEditController controller
                    = (EpidemicReportEditController) openScene(null, CommonValues.EpidemicReportEditFxml);
            if (controller != null) {
                controller.parent = this;
                controller.datasetSelector.setValue(currentDataSet);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void ChineseProvincesReport() {
        try {
            EpidemicReportsChineseProvincesEditController controller
                    = (EpidemicReportsChineseProvincesEditController) openScene(null,
                            CommonValues.EpidemicReportsChineseProvincesEditFxml);
            if (controller != null) {
                controller.parent = this;
                controller.load(currentDataSet);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void globalReport() {
        try {
            EpidemicReportsCountriesEditController controller
                    = (EpidemicReportsCountriesEditController) openScene(null,
                            CommonValues.EpidemicReportsCountiresEditFxml);
            if (controller != null) {
                controller.parent = this;
                controller.load(currentDataSet);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void editAction() {
        EpidemicReport selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            EpidemicReportEditController controller
                    = (EpidemicReportEditController) openScene(null, CommonValues.EpidemicReportEditFxml);
            if (controller != null) {
                controller.parent = this;
                controller.loadReport(selected);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected boolean deleteSelectedData() {
        List<EpidemicReport> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return false;
        }
        if (TableEpidemicReport.deleteData(selected)) {
            return TableEpidemicReport.summary();
        } else {
            return false;
        }
    }

    @Override
    protected boolean clearData() {
        treeView.setRoot(null);
        nameLabel.setText("");
        if (currentDataSet == null) {
            return new TableEpidemicReport().clear();
        } else {
            return TableEpidemicReport.delete(currentDataSet);
        }
    }

    @FXML
    public void htmlAction() {
        try {
            String name = StringTools.replaceAll(message("EpidemicReport") + "-" + currentTitle, ":", "-");
            htmlFile = chooseSaveFile(AppVariables.getUserConfigPath("HtmlFilePath"),
                    name, CommonFxValues.HtmlExtensionFilter, true);
            if (htmlFile == null) {
                return;
            }
            recordFileWritten(htmlFile);

            currentTab = tabPane.getSelectionModel().getSelectedItem();
            if (tabPane.getTabs().contains(mapTab)) {

                mapController.snapMap();
            } else {
                makeHtml(null);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void makeHtml(List<Image> mapImages) {
        try {
            double scale = FxmlControl.dpiScale();
            SnapshotParameters snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(Transform.scale(scale, scale));

            final Image numberLineChartSnap, numberBarChartSnap, ratioLineChartSnap, ratioBarChartSnap,
                    confirmedPieSnap, healedPieSnap, deadPieSnap;
            if (tabPane.getTabs().contains(numberTab)) {
                tabPane.getSelectionModel().select(numberTab);
                if (numberChartBox.getChildren().contains(numberLineChart)) {
                    Bounds bounds = numberLineChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    numberLineChartSnap = numberLineChart.snapshot(snapPara, snapshot);
                } else {
                    numberLineChartSnap = null;
                }
                if (numberChartBox.getChildren().contains(numberBarChart)) {
                    Bounds bounds = numberBarChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    numberBarChartSnap = numberBarChart.snapshot(snapPara, snapshot);
                } else {
                    numberBarChartSnap = null;
                }
            } else {
                numberLineChartSnap = null;
                numberBarChartSnap = null;
            }

            if (tabPane.getTabs().contains(ratioTab)) {
                tabPane.getSelectionModel().select(ratioTab);
                if (ratioChartBox.getChildren().contains(ratioLineChart)) {
                    Bounds bounds = ratioLineChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    ratioLineChartSnap = ratioLineChart.snapshot(snapPara, snapshot);
                } else {
                    ratioLineChartSnap = null;
                }
                if (ratioChartBox.getChildren().contains(ratioBarChart)) {
                    Bounds bounds = ratioBarChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    ratioBarChartSnap = ratioBarChart.snapshot(snapPara, snapshot);
                } else {
                    ratioBarChartSnap = null;
                }
            } else {
                ratioLineChartSnap = null;
                ratioBarChartSnap = null;
            }

            if (tabPane.getTabs().contains(confirmedTab)) {
                tabPane.getSelectionModel().select(confirmedTab);
                Bounds bounds = confirmedPie.getLayoutBounds();
                int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                confirmedPieSnap = confirmedPie.snapshot(snapPara, snapshot);
            } else {
                confirmedPieSnap = null;
            }

            if (tabPane.getTabs().contains(healedTab)) {
                tabPane.getSelectionModel().select(healedTab);
                Bounds bounds = headledPie.getLayoutBounds();
                int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                healedPieSnap = headledPie.snapshot(snapPara, snapshot);
            } else {
                healedPieSnap = null;
            }

            if (tabPane.getTabs().contains(deadTab)) {
                tabPane.getSelectionModel().select(deadTab);
                Bounds bounds = deadPie.getLayoutBounds();
                int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                deadPieSnap = deadPie.snapshot(snapPara, snapshot);
            } else {
                deadPieSnap = null;
            }

            List<EpidemicReport> rows = tableView.getSelectionModel().getSelectedItems();
            if (rows == null || rows.isEmpty()) {
                rows = tableData;
            }
            if (rows == null || rows.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> names = new ArrayList<>();
            String title;
            boolean all = currentDataSet == null || message("All").equals(currentDataSet);
            if (all) {
                names.add(message("DataSet"));
                title = message("EpidemicReport");
            } else {
                title = message("EpidemicReport") + " - " + currentDataSet;
            }
            names.addAll(Arrays.asList(message("Time"), message("Country"), message("Province"), message("City"),
                    message("Confirmed"), message("Suspected"), message("Healed"), message("Dead"),
                    message("Longitude"), message("Latitude"), message("Label"), message("Comments")
            ));
            StringTable table = new StringTable(names, title);
            for (EpidemicReport data : rows) {
                List<String> row = new ArrayList<>();
                if (all) {
                    row.add(data.getDataSet());
                }
                if (data.getTime() > 0) {
                    row.add(DateTools.datetimeToString(data.getTime()));
                } else {
                    row.add("");
                }
                if (data.getCountry() != null) {
                    row.add(data.getCountry());
                } else {
                    row.add("");
                }
                if (data.getProvince() != null) {
                    row.add(data.getProvince());
                } else {
                    row.add("");
                }
                if (data.getCountry() != null) {
                    row.add(data.getCountry());
                } else {
                    row.add("");
                }
                if (data.getCity() != null) {
                    row.add(data.getCity());
                } else {
                    row.add("");
                }
                row.addAll(Arrays.asList(data.getSuspected() + "", data.getHealed() + "",
                        data.getDead() + "", data.getLongitude() + "", data.getLatitude() + ""
                ));
                if (data.getDataLabel() != null) {
                    row.add(data.getDataLabel());
                } else {
                    row.add("");
                }
                if (data.getComments() != null) {
                    row.add(data.getComments());
                } else {
                    row.add("");
                }
                table.add(row);
            }
            final String dataTable = tableDiv(table);

            tabPane.getSelectionModel().select(currentTab);

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        try {

                            String subPath = FileTools.getFilePrefix(htmlFile.getName());
                            String path = htmlFile.getParent() + "/" + subPath;
                            (new File(path)).mkdirs();

                            StringBuilder s = new StringBuilder();
                            s.append("<h1  class=\"center\">").append(currentTitle).append("</h1>\n");
                            s.append("<hr>\n");

                            s.append("<h2  class=\"center\">").append(message("Data")).append("</h2>\n");
                            s.append(dataTable);

                            if (numberLineChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(numberLineChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "numberLineChartSnap.jpg");
                                String imageName = subPath + "/numberLineChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (numberBarChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(numberBarChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "numberBarChartSnap.jpg");
                                String imageName = subPath + "/numberBarChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (ratioLineChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(ratioLineChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "ratioLineChartSnap.jpg");
                                String imageName = subPath + "/ratioLineChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (ratioBarChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(ratioBarChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "ratioBarChartSnap.jpg");
                                String imageName = subPath + "/ratioBarChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (confirmedPieSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(confirmedPieSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "confirmedPieSnap.jpg");
                                String imageName = subPath + "/confirmedPieSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (healedPieSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(healedPieSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "healedPieSnap.jpg");
                                String imageName = subPath + "/healedPieSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (deadPieSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(deadPieSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "deadPieSnap.jpg");
                                String imageName = subPath + "/deadPieSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (mapImages != null && !mapImages.isEmpty()) {
                                if (mapImages.size() == 1) {
                                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(mapImages.get(0), null);
                                    ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "mapSnap.jpg");
                                    String imageName = subPath + "/mapSnap.jpg";
                                    s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                } else {
                                    List<BufferedImage> images = new ArrayList();
                                    for (Image image : mapImages) {
                                        images.add(SwingFXUtils.fromFXImage(image, null));
                                    }
                                    File outFile = new File(path + File.separator + "mapSnap.gif");
                                    ImageGifFile.writeImages(images, outFile, mapController.interval);
                                    String imageName = subPath + "/mapSnap.gif";
                                    s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                }
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                            }

                            String html = HtmlTools.html("", s.toString());
                            FileTools.writeFile(htmlFile, html);

                            if (task == null || isCancelled()) {
                                return false;
                            }
                            return htmlFile.exists();
                        } catch (Exception e) {
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        browseURI(htmlFile.toURI());
//                        FxmlStage.openHtmlEditor(null, file);

                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTree();
    }

    @Override
    public void loadExamples() {
        EpidemicReport.writeNCPs();
    }

    @Override
    public boolean leavingScene() {
        try {
            mapController.leavingScene();
        } catch (Exception e) {
        }
        return super.leavingScene();

    }

}
