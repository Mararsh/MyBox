package mara.mybox.controller;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.StringTable;
import static mara.mybox.data.StringTable.tableDiv;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlStage.openScene;
import mara.mybox.fxml.TableCoordinateCell;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.tools.DateTools;
import static mara.mybox.tools.DateTools.datetimeToString;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import thridparty.LabeledBarChart;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReportsController extends TableManageController<EpidemicReport> {

    protected String currentDataSet, currentTime, currentSizeQuery, currentDataQuery,
            currentCountry, currentProvince, currentCity, currentDistrict, currentTownship,
            currentNeighborhood, currentTitle, currentClearSQL, currentChartsSQL, currentMapSQL;
    protected NodeType currentNodeLevel;
    protected boolean currentNodeIsLeaf, allZero, isSnapping;
    protected ChartsType currentChartsType;
    protected SpecialType currentSpecialType;
    protected File htmlFile;
    protected Tab currentTab;
    protected SingletonTask treeTask, dataTask, chartsTask, exportTask, importTask, htmlTask, clearTask;
    protected int dpi, timeLocationIndex, timeLocationIndex2, interval;
    protected ObservableList<EpidemicReport> chartsData;
    protected List<String> datasets;
    protected Map<String, List<Date>> timesMap;
    protected Map<String, List<String>> countriesMap, provincesMap, citiesMap;
    protected LabeledBarChart numberBarChart, increasedBarChart, ratioBarChart;
    protected File numberSnapshotsFile, increasedSnapshotsFile, ratioSnapshotsFile;

    @FXML
    protected TreeView treeView;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab chartsDataTab, numberTab, increasedTab, ratioTab, confirmedTab, healedTab, deadTab, mapTab;
    @FXML
    protected TableView<EpidemicReport> chartsTableView;
    @FXML
    protected TableColumn<EpidemicReport, String> datasetColumn, levelColumn,
            countryColumn, provinceColumn, cityColumn, commentsColumn,
            chartsDatasetColumn, chartsLevelColumn, chartsCountryColumn,
            chartsProvinceColumn, chartsCityColumn, chartsCommentsColumn;
    @FXML
    protected TableColumn<EpidemicReport, Double> longtitudeColumn, latitudeColumn,
            chartsLongtitudeColumn, chartsLatitudeColumn;
    @FXML
    protected TableColumn<EpidemicReport, Date> timeColumn, chartsTimeColumn;
    @FXML
    protected TableColumn<EpidemicReport, Integer> confirmedColumn, headledColumn, deadColumn,
            increasedConfirmedColumn, increasedHeadledColumn, increasedDeadColumn,
            chartsConfirmedColumn, chartsHeadledColumn, chartsDeadColumn,
            chartsIncreasedConfirmedColumn, chartsIncreasedHeadledColumn, chartsIncreasedDeadColumn;
    @FXML
    protected TableColumn<EpidemicReport, Double> headledRatioColumn, deadRatioColumn,
            chartsHeadledRatioColumn, chartsDeadRatioColumn;
    @FXML
    protected Button examplesButton, chinaButton, helpMeButton, catButton, statisticButton,
            dataExportButton, dataExportChartsButton, fillButton, sureButton;
    @FXML
    protected VBox numberChartBox, increasedChartBox, ratioChartBox,
            confirmedChartBox, healedChartBox, deadChartBox;
    @FXML
    protected LineChart numberLineChart, increasedLineChart, ratioLineChart,
            confirmedLineChart, healedLineChart, deadLineChart;
    @FXML
    protected PieChart deadPie, healedPie, confirmedPie;
    @FXML
    protected Label nameLabel, chartsSizeLabel;
    @FXML
    protected TextField chartsSQLLabel, dataSQLLabel, mapSQLLabel;
    @FXML
    protected EpidemicReportMapController mapController;
    @FXML
    protected CheckBox recountCheck, valuesCheck, legendCheck;
    @FXML
    private ComboBox<String> dpiSelector, intervalSelector;

    protected enum NodeType {
        None, All, DataSet, AllTime, Time, Countries, Provinces, Country,
        Province, Cities, City
    }

    protected enum SpecialType {
        None, ExceptChina, Filled
    }

    protected enum ChartsType {
        TimeBased, LocationBased, TimeLocationBased, None
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
            initChartsTable();

            mapController.parent = this;
            currentDataSet = null;
            currentNodeLevel = null;
            currentTime = null;
            currentSpecialType = null;

            dpi = 96;
            List<String> dpiValues = new ArrayList();
            dpiValues.addAll(Arrays.asList("96", "120", "160", "300"));
            String sValue = Toolkit.getDefaultToolkit().getScreenResolution() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(0, sValue);
            sValue = (int) Screen.getPrimary().getDpi() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(sValue);
            dpiSelector.getItems().addAll(dpiValues);
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            dpi = Integer.parseInt(newValue);
                            AppVariables.setUserConfigValue("EpidemicReportDPI", dpi + "");
                        } catch (Exception e) {
                            dpi = 96;
                        }
                    });
            dpiSelector.getSelectionModel().select(AppVariables.getUserConfigValue("EpidemicReportDPI", "96"));

            interval = 1000;
            intervalSelector.getItems().addAll(Arrays.asList(
                    "1000", "800", "500", "1500", "200", "2000", "3000", "5000", "300", "10000"
            ));
            intervalSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.valueOf(intervalSelector.getValue());
                    if (v > 0) {
                        interval = v;
                        AppVariables.setUserConfigValue("EpidemicReportInterval", interval + "");
                        FxmlControl.setEditorNormal(intervalSelector);
                        if (isSettingValues) {
                            return;
                        }
                        if (currentChartsType == ChartsType.TimeBased
                                || currentChartsType == ChartsType.TimeLocationBased) {
                            drawCharts();
                        }
                    } else {
                        FxmlControl.setEditorBadStyle(intervalSelector);
                    }
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            });

            valuesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue("EpidemicReportValues", newValue);
                        drawCharts();
                    });

            legendCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue("EpidemicReportLegend", newValue);
                        drawCharts();
                    });

            isSettingValues = true;
            valuesCheck.setSelected(AppVariables.getUserConfigBoolean("EpidemicReportValues", true));
            legendCheck.setSelected(AppVariables.getUserConfigBoolean("EpidemicReportLegend", true));
            intervalSelector.getSelectionModel().select(AppVariables.getUserConfigValue("EpidemicReportInterval", "1000"));
            isSettingValues = false;

            numberBarChart = LabeledBarChart.create().setIntValue(true)
                    .setDisplayLabel(valuesCheck.isSelected()).style("/styles/EpidemicReportBarChart.css");
            increasedBarChart = LabeledBarChart.create().setIntValue(true)
                    .setDisplayLabel(valuesCheck.isSelected()).style("/styles/EpidemicReportBarChart.css");
            ratioBarChart = LabeledBarChart.create().setIntValue(false)
                    .setDisplayLabel(valuesCheck.isSelected()).style("/styles/EpidemicReportRatio.css");

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    protected void initColumns() {
        try {
            datasetColumn.setCellValueFactory(new PropertyValueFactory<>("dataSet"));
            levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
            countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
            provinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));
            cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
            confirmedColumn.setCellValueFactory(new PropertyValueFactory<>("confirmed"));
            increasedConfirmedColumn.setCellValueFactory(new PropertyValueFactory<>("increasedConfirmed"));
            headledColumn.setCellValueFactory(new PropertyValueFactory<>("healed"));
            increasedHeadledColumn.setCellValueFactory(new PropertyValueFactory<>("increasedHealed"));
            headledRatioColumn.setCellValueFactory(new PropertyValueFactory<>("healedRatio"));
            deadColumn.setCellValueFactory(new PropertyValueFactory<>("dead"));
            increasedDeadColumn.setCellValueFactory(new PropertyValueFactory<>("increasedDead"));
            deadRatioColumn.setCellValueFactory(new PropertyValueFactory<>("deadRatio"));
            longtitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            longtitudeColumn.setCellFactory(new TableCoordinateCell());
            latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            latitudeColumn.setCellFactory(new TableCoordinateCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            timeColumn.setCellFactory(new TableDateCell());
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

            tableView.setRowFactory((TableView<EpidemicReport> param) -> {
                return new FilledRow();
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected class FilledRow extends TableRow<EpidemicReport> {

        @Override
        protected void updateItem(EpidemicReport item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setTextFill(null);
                return;
            }
            if ("Filled".equals(item.getComments())) {
                setStyle("-fx-background-color: thistle");
            } else {
                setStyle(null);
            }
        }
    };

    protected void initChartsTable() {
        try {
            chartsData = FXCollections.observableArrayList();
            chartsTableView.setItems(chartsData);

            chartsDatasetColumn.setCellValueFactory(new PropertyValueFactory<>("dataSet"));
            chartsLevelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
            chartsCountryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
            chartsProvinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));
            chartsCityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
            chartsConfirmedColumn.setCellValueFactory(new PropertyValueFactory<>("confirmed"));
            chartsIncreasedConfirmedColumn.setCellValueFactory(new PropertyValueFactory<>("increasedConfirmed"));
            chartsHeadledColumn.setCellValueFactory(new PropertyValueFactory<>("healed"));
            chartsIncreasedHeadledColumn.setCellValueFactory(new PropertyValueFactory<>("increasedHealed"));
            chartsHeadledRatioColumn.setCellValueFactory(new PropertyValueFactory<>("healedRatio"));
            chartsDeadColumn.setCellValueFactory(new PropertyValueFactory<>("dead"));
            chartsIncreasedDeadColumn.setCellValueFactory(new PropertyValueFactory<>("increasedDead"));
            chartsDeadRatioColumn.setCellValueFactory(new PropertyValueFactory<>("deadRatio"));
            chartsLongtitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            chartsLongtitudeColumn.setCellFactory(new TableCoordinateCell());
            chartsLatitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            chartsLatitudeColumn.setCellFactory(new TableCoordinateCell());
            chartsTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            chartsTimeColumn.setCellFactory(new TableDateCell());
            chartsCommentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

            chartsTableView.setRowFactory((TableView<EpidemicReport> param) -> {
                return new FilledRow();
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            FxmlControl.setTooltip(examplesButton, message("EpidemicReportsExamplesComments"));
            FxmlControl.setTooltip(chinaButton, message("ChineseProvincesEpidemicReports"));
            FxmlControl.setTooltip(helpMeButton, message("FetchNCPData"));
            FxmlControl.setTooltip(clearButton, message("Clear") + "\nCTRL+g\n\n" + message("RelativeClearComments"));
            FxmlControl.setTooltip(catButton, message("MyBoxInternetDataPath"));
            FxmlControl.setTooltip(statisticButton, message("EpidemicReportStatisticComments"));
            FxmlControl.setTooltip(recountCheck, message("EpidemicReportStatisticComments"));
            FxmlControl.setTooltip(dataExportButton, message("RelativeExportComments"));
            FxmlControl.setTooltip(dataExportChartsButton, message("RelativeExportComments"));
            FxmlControl.setTooltip(dpiSelector, message("SnapDpiComments"));
            FxmlControl.setTooltip(fillButton, message("EpidemicReportsFillComments"));
            FxmlControl.setTooltip(sureButton, message("SureFilledData"));

            String backFile = AppVariables.getSystemConfigValue("EpidemicReportBackup6.1.5", "");
            if (!backFile.isBlank()) {
                alertInformation(message("EpidemicReportBackup615Comments")
                        + "\n\n" + backFile);
                AppVariables.deleteSystemConfigValue("EpidemicReportBackup6.1.5");
                examplesAction();
            } else if (!AppVariables.getSystemConfigBoolean("EpidemicReportExamples6.1.5", false)) {
                alertInformation(message("EpidemicReportsExamplesComments"));
                AppVariables.setSystemConfigValue("EpidemicReportExamples6.1.5", true);
                examplesAction();
            } else {
                currentNodeLevel = NodeType.All;
                currentDataSet = null;
                currentTime = null;
                currentSpecialType = SpecialType.None;
                loadTree();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void loadTree() {
        loadTree(false);
    }

    protected void loadTree(boolean requireStatistic) {
        treeView.setRoot(null);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        synchronized (this) {
            if (treeTask != null) {
                return;
            }
            treeTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        if (requireStatistic || recountCheck.isSelected()) {
                            TableEpidemicReport.statistic();
                        }

                        datasets = TableEpidemicReport.datasets();
                        if (datasets == null || datasets.isEmpty()) {
                            return true;
                        }
                        countriesMap = new HashMap<>();
                        timesMap = new HashMap<>();
                        provincesMap = new HashMap<>();
                        citiesMap = new HashMap<>();
                        for (String dataset : datasets) {
                            List<String> countries = TableEpidemicReport.countries(dataset);
                            if (countries != null && !countries.isEmpty()) {
                                countriesMap.put(dataset, countries);
                                for (String country : countries) {
                                    List<String> provinces = TableEpidemicReport.provinces(dataset, country);
                                    if (provinces != null && !provinces.isEmpty()) {
                                        provincesMap.put(dataset + country, provinces);
                                        for (String province : provinces) {
                                            List<String> cities = TableEpidemicReport.cities(dataset, country, province);
                                            if (cities != null && !cities.isEmpty()) {
                                                citiesMap.put(dataset + country + province, cities);
                                            }
                                        }
                                    } else {
                                        List<String> cities = TableEpidemicReport.cities(dataset, country, null);
                                        if (cities != null && !cities.isEmpty()) {
                                            citiesMap.put(dataset + country, cities);
                                        }
                                    }
                                }
                            }
                            List<Date> times = TableEpidemicReport.times(dataset);
                            if (times != null && !times.isEmpty()) {
                                timesMap.put(dataset, times);
                            }
                        }
                        Platform.runLater(() -> {
                            writeTree();
                        });
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                protected void writeTree() {
                    if (datasets == null || datasets.isEmpty()) {
                        return;
                    }
                    isSettingValues = true;
                    Text allLink = new Text(message("All"));
                    allLink.setOnMouseClicked((MouseEvent event) -> {
                        currentNodeLevel = NodeType.All;
                        currentDataSet = null;
                        currentTime = null;
                        currentSpecialType = SpecialType.None;
                        load();
                    });
                    TreeItem root = new TreeItem<>(allLink);
                    root.setExpanded(true);
                    treeView.setRoot(root);

                    for (String dataset : datasets) {
                        Text datasetLink = new Text(dataset);
                        datasetLink.setOnMouseClicked((MouseEvent event) -> {
                            currentNodeLevel = NodeType.DataSet;
                            currentDataSet = dataset;
                            currentTime = null;
                            currentSpecialType = SpecialType.None;
                            load();
                        });
                        TreeItem<Text> datasetItem = new TreeItem<>(datasetLink);
                        datasetItem.setExpanded(true);
                        root.getChildren().add(datasetItem);
                        datasetTree(datasetItem, dataset, SpecialType.None);

                        Text exceptLink = new Text(message("ExceptChina"));
                        exceptLink.setOnMouseClicked((MouseEvent event) -> {
                            currentNodeLevel = NodeType.DataSet;
                            currentDataSet = dataset;
                            currentTime = null;
                            currentSpecialType = SpecialType.ExceptChina;
                            load();
                        });
                        TreeItem<Text> exceptItem = new TreeItem<>(exceptLink);
                        exceptItem.setExpanded(false);
                        datasetItem.getChildren().add(exceptItem);
                        datasetTree(exceptItem, dataset, SpecialType.ExceptChina);

                        Text filledLink = new Text(message("FilledData"));
                        filledLink.setOnMouseClicked((MouseEvent event) -> {
                            currentNodeLevel = NodeType.DataSet;
                            currentDataSet = dataset;
                            currentTime = null;
                            currentSpecialType = SpecialType.Filled;
                            load();
                        });
                        TreeItem<Text> filledItem = new TreeItem<>(filledLink);
                        filledItem.setExpanded(false);
                        datasetItem.getChildren().add(filledItem);
                        datasetTree(filledItem, dataset, SpecialType.Filled);

                    }

                    isSettingValues = false;

                }

                protected void datasetTree(TreeItem parent, String dataset,
                        SpecialType special) {
                    Text timesLink = new Text(message("Time"));
                    timesLink.setOnMouseClicked((MouseEvent event) -> {
                        currentNodeLevel = NodeType.AllTime;
                        currentDataSet = dataset;
                        currentTime = null;
                        currentSpecialType = special;
                        load();
                    });
                    TreeItem<Text> timesItem = new TreeItem<>(timesLink);
                    timesItem.setExpanded(false);
                    parent.getChildren().add(timesItem);

                    timeTree(timesItem, dataset, special);

                    Text countriesLink = new Text(message("Countries"));
                    countriesLink.setOnMouseClicked((MouseEvent event) -> {
                        currentNodeLevel = NodeType.Countries;
                        currentDataSet = dataset;
                        currentTime = null;
                        currentSpecialType = special;
                        load();
                    });
                    TreeItem<Text> countriesItem = new TreeItem<>(countriesLink);
                    countriesItem.setExpanded(false);
                    parent.getChildren().add(countriesItem);

                    countrieTree(countriesItem, dataset, null, special);
                }

                protected void timeTree(TreeItem parent, String dataset,
                        SpecialType special) {
                    if (dataset == null) {
                        return;
                    }
                    List<Date> times = timesMap.get(dataset);
                    if (times == null || times.isEmpty()) {
                        return;
                    }
                    for (Date time : times) {
                        String timeString = DateTools.datetimeToString(time);
                        Text timeLink = new Text(timeString);
                        timeLink.setOnMouseClicked((MouseEvent event) -> {
                            currentNodeLevel = NodeType.Time;
                            currentDataSet = dataset;
                            currentTime = timeString;
                            currentSpecialType = special;
                            load();
                        });
                        TreeItem<Text> timeItem = new TreeItem<>(timeLink);
                        timeItem.setExpanded(false);
                        parent.getChildren().add(timeItem);
                        countrieTree(timeItem, dataset, timeString, special);
                    }

                }

                protected void countrieTree(TreeItem parent, String dataset,
                        String time, SpecialType special) {
                    if (parent == null || dataset == null) {
                        return;
                    }
                    List<String> countries = countriesMap.get(dataset);
                    for (String country : countries) {
                        if (country.isBlank()) {
                            continue;
                        }
                        Text countryLink = new Text(country);
                        TreeItem<Text> countryItem = new TreeItem<>(countryLink);
                        countryItem.setExpanded(false);
                        parent.getChildren().add(countryItem);
                        countryLink.setOnMouseClicked((MouseEvent event) -> {
                            if (countryItem.isLeaf()) {
                                currentNodeLevel = NodeType.Country;
                            } else {
                                List<String> provinces = provincesMap.get(dataset + country);
                                if (provinces == null || provinces.isEmpty()) {
                                    currentNodeLevel = NodeType.Cities;
                                    currentProvince = null;
                                } else {
                                    currentNodeLevel = NodeType.Provinces;
                                }
                            }
                            currentDataSet = dataset;
                            currentTime = time;
                            currentCountry = country;
                            currentSpecialType = special;
                            load();
                        });

                        provincesTree(countryItem, dataset, country, time, special);
                    }
                }

                protected void provincesTree(TreeItem parent, String dataset,
                        String country, String time, SpecialType special) {
                    if (parent == null || country == null) {
                        return;
                    }
                    List<String> provinces = provincesMap.get(dataset + country);
                    if (provinces == null || provinces.isEmpty()) {
                        citiesTree(parent, dataset, country, null, time, special);
                        return;
                    }
                    for (String province : provinces) {
                        if (province.isBlank()) {
                            continue;
                        }
                        Text provinceLink = new Text(province);
                        TreeItem<Text> provinceItem = new TreeItem<>(provinceLink);
                        provinceItem.setExpanded(false);
                        parent.getChildren().add(provinceItem);
                        provinceLink.setOnMouseClicked((MouseEvent event) -> {
                            if (provinceItem.isLeaf()) {
                                currentNodeLevel = NodeType.Province;
                            } else {
                                currentNodeLevel = NodeType.Cities;
                            }
                            currentDataSet = dataset;
                            currentTime = time;
                            currentCountry = country;
                            currentProvince = province;
                            currentSpecialType = special;
                            load();
                        });

                        citiesTree(provinceItem, dataset, country, province, time, special);
                    }
                }

                protected void citiesTree(TreeItem parent, String dataset,
                        String country, String province, String time,
                        SpecialType special) {
                    if (parent == null || country == null) {
                        return;
                    }
                    List<String> cities;
                    if (province == null) {
                        cities = citiesMap.get(dataset + country);
                    } else {
                        cities = citiesMap.get(dataset + country + province);
                    }
                    if (cities == null || cities.isEmpty()) {
                        return;
                    }
                    for (String city : cities) {
                        if (city.isBlank()) {
                            continue;
                        }
                        Text cityLink = new Text(city);
                        cityLink.setOnMouseClicked((MouseEvent event) -> {
                            currentNodeLevel = NodeType.City;
                            currentDataSet = dataset;
                            currentTime = time;
                            currentCountry = country;
                            currentProvince = province;
                            currentCity = city;
                            currentSpecialType = special;
                            load();
                        });
                        TreeItem<Text> cityItem = new TreeItem<>(cityLink);
                        cityItem.setExpanded(false);
                        parent.getChildren().add(cityItem);
                    }
                }

                @Override
                protected void whenSucceeded() {
                    load();
                }

                @Override
                protected void taskQuit() {
                    endTime = new Date().getTime();
                    treeTask = null;
                }
            };
            openHandlingStage(treeTask, Modality.WINDOW_MODAL, message("EpidemicReportsLoadingStatistic"));
            Thread thread = new Thread(treeTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void load() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        tableData.clear();
        tableView.getColumns().clear();
        chartsData.clear();
        chartsTableView.getColumns().clear();
        nameLabel.setText("");
        chartsSQLLabel.setText("");
        dataSQLLabel.setText("");
        mapSQLLabel.setText("");

        if (currentNodeLevel == null || currentNodeLevel == NodeType.None) {
            return;
        }

        String dataWhere, dataOrder, chartsWhere, chartsOrder, mapWhere, mapOrder;
        currentTitle = message("EpidemicReport");
        if (currentNodeLevel != NodeType.All) {
            if (currentDataSet == null) {
                return;
            }
            currentTitle += " " + currentDataSet;
            if (currentTime != null) {
                tableView.getColumns().add(levelColumn);
                chartsTableView.getColumns().add(chartsLevelColumn);
                currentTitle += " " + currentTime;
                dataOrder = " ORDER BY confirmed desc";
                chartsOrder = mapOrder = " ORDER BY confirmed desc";
            } else {
                tableView.getColumns().addAll(timeColumn, levelColumn);
                chartsTableView.getColumns().addAll(chartsTimeColumn, chartsLevelColumn);
                dataOrder = " ORDER BY time desc, confirmed desc";
                chartsOrder = mapOrder = " ORDER BY time asc , confirmed desc";
            }
        } else {
            dataOrder = " ORDER BY data_set, time desc, country, province, city ";
            chartsOrder = mapOrder = "";
        }
        switch (currentNodeLevel) {
            case All:
                currentTitle += " " + message("All");

                dataWhere = "";
                tableView.getColumns().addAll(datasetColumn, timeColumn, levelColumn,
                        countryColumn, provinceColumn, cityColumn);

                chartsWhere = mapWhere = null;
                currentChartsType = ChartsType.None;
                break;
            case DataSet:
                dataWhere = " WHERE data_set='" + currentDataSet + "' ";
                tableView.getColumns().addAll(countryColumn, provinceColumn, cityColumn);

                chartsWhere = mapWhere = dataWhere + " AND level='" + message("Global") + "' ";

                currentChartsType = ChartsType.TimeBased;
                break;
            case Countries:
                currentTitle += " " + message("Countries");

                dataWhere = " WHERE data_set='" + currentDataSet + "'  ";
                tableView.getColumns().addAll(countryColumn, provinceColumn, cityColumn);

                chartsWhere = dataWhere + " AND level='" + message("Country") + "' "
                        + " AND Country IS NOT NULL";
                chartsTableView.getColumns().addAll(chartsCountryColumn);

                mapWhere = dataWhere + " AND level='" + message("Global") + "' ";

                currentChartsType = ChartsType.TimeLocationBased;
                break;
            case AllTime:
                currentTitle += " " + message("Countries");

                dataWhere = " WHERE data_set='" + currentDataSet + "'  ";
                tableView.getColumns().addAll(countryColumn, provinceColumn, cityColumn);

                chartsWhere = dataWhere + " AND level='" + message("Country") + "' "
                        + " AND Country IS NOT NULL ";
                chartsTableView.getColumns().addAll(chartsCountryColumn);

                mapWhere = dataWhere + " AND level='" + message("Global") + "' ";

                currentChartsType = ChartsType.TimeLocationBased;
                break;
            case Time:
                if (currentTime == null) {
                    return;
                }
                dataWhere = " WHERE data_set='" + currentDataSet + "'  "
                        + " AND time='" + currentTime + "' ";
                tableView.getColumns().addAll(countryColumn, provinceColumn, cityColumn);

                chartsWhere = mapWhere = dataWhere + " AND level='" + message("Country") + "' ";
                chartsTableView.getColumns().addAll(chartsCountryColumn);

                currentChartsType = ChartsType.LocationBased;
                break;
            case Country:
                if (currentCountry == null) {
                    return;
                }
                currentTitle += " " + currentCountry;

                dataWhere = " WHERE data_set='" + currentDataSet + "'  "
                        + " AND country='" + currentCountry + "' ";
                if (currentTime == null) {
                    currentChartsType = ChartsType.TimeBased;
                } else {
                    dataWhere += " AND time='" + currentTime + "' ";
                    currentChartsType = ChartsType.LocationBased;
                }

                chartsWhere = mapWhere = dataWhere + " AND level='" + message("Country") + "' ";
                tableView.getColumns().addAll(provinceColumn, cityColumn);
                break;
            case Provinces:
                if (currentCountry == null) {
                    return;
                }
                currentTitle += " " + currentCountry;

                dataWhere = " WHERE data_set='" + currentDataSet + "' "
                        + " AND country='" + currentCountry + "' ";
                tableView.getColumns().addAll(provinceColumn, cityColumn);

                if (currentTime == null) {
                    mapWhere = dataWhere + " AND level='" + message("Country") + "' ";
                    currentChartsType = ChartsType.TimeLocationBased;
                } else {
                    dataWhere += " AND time='" + currentTime + "' ";
                    mapWhere = dataWhere + " AND level='" + message("Province") + "' ";
                    currentChartsType = ChartsType.LocationBased;
                }

                chartsWhere = dataWhere + " AND level='" + message("Province") + "' ";
                chartsTableView.getColumns().addAll(chartsProvinceColumn);
                break;
            case Province:
                if (currentCountry == null || currentProvince == null) {
                    return;
                }
                currentTitle += " " + currentCountry + " " + currentProvince;

                dataWhere = " WHERE  data_set='" + currentDataSet + "'  "
                        + " AND country='" + currentCountry + "' "
                        + " AND province='" + currentProvince + "' ";
                if (currentTime == null) {
                    currentChartsType = ChartsType.TimeBased;
                } else {
                    dataWhere += " AND time='" + currentTime + "' ";
                    currentChartsType = ChartsType.LocationBased;
                }
                tableView.getColumns().addAll(cityColumn);

                chartsWhere = mapWhere = dataWhere + " AND level='" + message("Province") + "' ";
                chartsTableView.getColumns().addAll(chartsProvinceColumn);
                break;
            case Cities:
                if (currentCountry == null) {
                    return;
                }
                if (currentProvince == null) {      // Cities without province level
                    currentTitle += " " + currentCountry;
                    dataWhere = " WHERE  data_set='" + currentDataSet + "' "
                            + " AND country='" + currentCountry + "' "
                            + " AND level='" + message("City") + "' ";
                    if (currentTime == null) {
                        mapWhere = " WHERE  data_set='" + currentDataSet + "' "
                                + " AND country='" + currentCountry + "' "
                                + " AND level='" + message("Country") + "' ";
                        currentChartsType = ChartsType.TimeLocationBased;
                    } else {
                        dataWhere += " AND time='" + currentTime + "' ";
                        mapWhere = " WHERE  data_set='" + currentDataSet + "' "
                                + " AND country='" + currentCountry + "' "
                                + " AND time='" + currentTime + "' "
                                + " AND level='" + message("Country") + "' ";
                        currentChartsType = ChartsType.LocationBased;
                    }
                    tableView.getColumns().addAll(cityColumn);

                    chartsWhere = dataWhere;
                    chartsTableView.getColumns().addAll(chartsCityColumn);
                } else {
                    currentTitle += " " + currentCountry + " " + currentProvince;
                    dataWhere = " WHERE  data_set='" + currentDataSet + "' "
                            + " AND country='" + currentCountry + "' "
                            + " AND province='" + currentProvince + "' ";
                    if (currentTime == null) {
                        mapWhere = dataWhere + " AND level='" + message("Province") + "' ";
                        currentChartsType = ChartsType.TimeLocationBased;

                    } else {
                        dataWhere += " AND time='" + currentTime + "' ";
                        mapWhere = dataWhere
                                + " AND NOT (level='" + message("Country") + "' ) "
                                + " AND NOT (level='" + message("Province") + "' ) ";
                        currentChartsType = ChartsType.LocationBased;
                    }
                    tableView.getColumns().addAll(cityColumn);

                    chartsWhere = dataWhere
                            + " AND NOT (level='" + message("Country") + "' ) "
                            + " AND NOT (level='" + message("Province") + "' ) ";
                    chartsTableView.getColumns().addAll(chartsCityColumn);
                }
                break;
            case City:
                if (currentCountry == null || currentCity == null) {
                    return;
                }
                if (currentProvince == null) {
                    currentTitle += " " + currentCountry + " " + currentCity;
                    dataWhere = " WHERE data_set='" + currentDataSet + "'  "
                            + " AND country='" + currentCountry + "' "
                            + " AND city='" + currentCity + "' "
                            + " AND level='" + message("City") + "' ";
                    if (currentTime == null) {
                        currentChartsType = ChartsType.TimeBased;
                    } else {
                        dataWhere += " AND time='" + currentTime + "' ";
                        currentChartsType = ChartsType.LocationBased;
                    }

                    chartsWhere = mapWhere = dataWhere;
                } else {
                    currentTitle += " " + currentCountry + " " + currentProvince + " " + currentCity;
                    dataWhere = " WHERE data_set='" + currentDataSet + "'  "
                            + " AND country='" + currentCountry + "' "
                            + " AND province='" + currentProvince + "' "
                            + " AND city='" + currentCity + "' "
                            + " AND NOT (level='" + message("Country") + "' ) "
                            + " AND NOT (level='" + message("Province") + "' ) ";
                    if (currentTime == null) {
                        currentChartsType = ChartsType.TimeBased;
                    } else {
                        dataWhere += " AND time='" + currentTime + "' ";
                        currentChartsType = ChartsType.LocationBased;
                    }

                    chartsWhere = mapWhere = dataWhere;
                }
                break;
            default:
                return;
        }

        sureButton.setVisible(currentSpecialType == SpecialType.Filled);
        switch (currentSpecialType) {
            case Filled:
                dataWhere += " AND comments='Filled' ";
                chartsWhere = mapWhere = null;
                currentChartsType = ChartsType.None;
                currentTitle += " " + message("FilledData");
                break;
            case ExceptChina:
                dataWhere += " AND ( country IS NULL OR country<>'" + message("China") + "' )";
                chartsWhere += " AND ( country IS NULL OR country<>'" + message("China") + "' )";
                mapWhere += " AND ( country IS NULL OR country<>'" + message("China") + "' )";
                currentTitle += " " + message("ExceptChina");
                break;
        }

        currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + dataWhere;
        currentDataQuery = "SELECT * FROM Epidemic_Report " + dataWhere + dataOrder;
        currentClearSQL = "DELETE FROM Epidemic_Report " + dataWhere;
        dataSQLLabel.setText(currentDataQuery);

        if (chartsWhere != null) {
            currentChartsSQL = "SELECT * FROM Epidemic_Report " + chartsWhere + chartsOrder;
            chartsSQLLabel.setText(currentChartsSQL);
        } else {
            currentChartsSQL = null;
            chartsSQLLabel.setText("");
        }
        if (mapWhere != null) {
            currentMapSQL = "SELECT * FROM Epidemic_Report " + mapWhere + mapOrder;
            mapSQLLabel.setText(currentMapSQL);
        } else {
            currentMapSQL = null;
            mapSQLLabel.setText("");
        }
        nameLabel.setText(currentTitle);

        tableView.getColumns().addAll(confirmedColumn, increasedConfirmedColumn,
                headledColumn, increasedHeadledColumn, headledRatioColumn,
                deadColumn, increasedDeadColumn, deadRatioColumn,
                longtitudeColumn, latitudeColumn, commentsColumn);
        chartsTableView.getColumns().addAll(chartsConfirmedColumn, chartsIncreasedConfirmedColumn,
                chartsHeadledColumn, chartsIncreasedHeadledColumn, chartsHeadledRatioColumn,
                chartsDeadColumn, chartsIncreasedDeadColumn, chartsDeadRatioColumn,
                chartsLongtitudeColumn, chartsLatitudeColumn, chartsCommentsColumn);

        currentTab = tabPane.getSelectionModel().getSelectedItem();

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private int total, pagesNumber;
                private List<EpidemicReport> reports;

                @Override
                protected boolean handle() {
                    total = readDataSize();
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
                    reports = readData(start, end - start + 1);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (reports != null) {
                        isSettingValues = true;
                        tableData.setAll(reports);
                        isSettingValues = false;
                        tableView.refresh();
                    }
                    checkSelected();
                    setPagination(total, pagesNumber);
                    drawCharts();
                    if (tabPane.getTabs().contains(currentTab)) {
                        tabPane.getSelectionModel().select(currentTab);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL, message("EpidemicReportsLoadingStatistic"));
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public int readDataSize() {
//        logger.debug(currentSizeQuery);
        return TableEpidemicReport.sizeQuery(currentSizeQuery);
    }

    @Override
    public List<EpidemicReport> readData(int offset, int number) {
//        logger.debug(currentDataQuery);
        String sql = currentDataQuery
                + " OFFSET " + offset + " ROWS FETCH NEXT " + number + " ROWS ONLY";
        return TableEpidemicReport.dataQuery(sql);
    }

    protected void initCharts() {
        if (isSettingValues) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (interval <= 0) {
            interval = 1000;
        }
        numberChartBox.getChildren().clear();
        ratioChartBox.getChildren().clear();
        increasedChartBox.getChildren().clear();
        confirmedChartBox.getChildren().clear();
        healedChartBox.getChildren().clear();
        deadChartBox.getChildren().clear();
        chartsSizeLabel.setText("");

        tabPane.getTabs().removeAll(chartsDataTab, numberTab, increasedTab, ratioTab,
                confirmedTab, healedTab, deadTab, mapTab);

        if (currentChartsType == null || currentChartsType == ChartsType.None) {
            return;
        }
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (!tabPane.getTabs().contains(numberTab)) {
            tabPane.getTabs().addAll(chartsDataTab, numberTab, increasedTab, ratioTab);
        }
        switch (currentChartsType) {
            case TimeBased:
                tabPane.getTabs().removeAll(confirmedTab, healedTab, deadTab);
                numberChartBox.getChildren().add(numberLineChart);
                increasedChartBox.getChildren().add(increasedLineChart);
                ratioChartBox.getChildren().add(ratioLineChart);

                break;
            case LocationBased:
                numberChartBox.getChildren().add(numberBarChart);
                increasedChartBox.getChildren().add(increasedBarChart);
                ratioChartBox.getChildren().add(ratioBarChart);
                if (!tabPane.getTabs().contains(confirmedTab)) {
                    tabPane.getTabs().addAll(confirmedTab, healedTab, deadTab);
                }
                confirmedChartBox.getChildren().add(confirmedPie);
                healedChartBox.getChildren().add(healedPie);
                deadChartBox.getChildren().add(deadPie);
                break;
            case TimeLocationBased:
                numberChartBox.getChildren().add(numberBarChart);
                increasedChartBox.getChildren().add(increasedBarChart);
                ratioChartBox.getChildren().add(ratioBarChart);
                if (!tabPane.getTabs().contains(confirmedTab)) {
                    tabPane.getTabs().addAll(confirmedTab, healedTab, deadTab);
                }
                confirmedChartBox.getChildren().add(confirmedLineChart);
                healedChartBox.getChildren().add(healedLineChart);
                deadChartBox.getChildren().add(deadLineChart);
                break;
            default:
                break;
        }
        tabPane.getTabs().add(mapTab);
        if (tabPane.getTabs().contains(tab)) {
            tabPane.getSelectionModel().select(tab);
        }

        // https://stackoverflow.com/questions/29124723/javafx-chart-auto-scaling-wrong-with-low-numbers?r=SearchResults
        numberBarChart.setAnimated(false);
        numberBarChart.getData().clear();
        numberBarChart.setAnimated(true);
        numberBarChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        numberBarChart.setTitle(currentTitle + " - " + message("Number"));
        numberBarChart.setDisplayLabel(valuesCheck.isSelected());
        numberBarChart.setLegendVisible(legendCheck.isSelected());

        increasedBarChart.setAnimated(false);
        increasedBarChart.getData().clear();
        increasedBarChart.setAnimated(true);
        increasedBarChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        increasedBarChart.setTitle(currentTitle + " - " + message("Increased"));
        increasedBarChart.setDisplayLabel(valuesCheck.isSelected());
        increasedBarChart.setLegendVisible(legendCheck.isSelected());

        ratioBarChart.setAnimated(false);
        ratioBarChart.getData().clear();
        ratioBarChart.setAnimated(true);
        ratioBarChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        ratioBarChart.setTitle(currentTitle + " - " + message("Ratio"));
        ratioBarChart.setDisplayLabel(valuesCheck.isSelected());
        ratioBarChart.setLegendVisible(legendCheck.isSelected());

        numberLineChart.setAnimated(false);
        numberLineChart.getData().clear();
        numberLineChart.setAnimated(true);
        numberLineChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        numberLineChart.setTitle(currentTitle + " - " + message("Number"));
        numberLineChart.setCreateSymbols(true);
        numberLineChart.setLegendVisible(legendCheck.isSelected());

        increasedLineChart.setAnimated(false);
        increasedLineChart.getData().clear();
        increasedLineChart.setAnimated(true);
        increasedLineChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        increasedLineChart.setTitle(currentTitle + " - " + message("Increased"));
        increasedLineChart.setCreateSymbols(true);
        increasedLineChart.setLegendVisible(legendCheck.isSelected());

        ratioLineChart.setAnimated(false);
        ratioLineChart.getData().clear();
        ratioLineChart.setAnimated(true);
        ratioLineChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        ratioLineChart.setTitle(currentTitle + " - " + message("Ratio"));
        ratioLineChart.setCreateSymbols(true);
        ratioLineChart.setLegendVisible(legendCheck.isSelected());

        confirmedPie.getData().clear();
        confirmedPie.setTitle(currentTitle + " - " + message("Confirmed"));
        confirmedPie.setLegendVisible(legendCheck.isSelected());

        healedPie.getData().clear();
        healedPie.setTitle(currentTitle + " - " + message("Healed"));
        healedPie.setLegendVisible(legendCheck.isSelected());

        deadPie.getData().clear();
        deadPie.setTitle(currentTitle + " - " + message("Dead"));
        deadPie.setLegendVisible(legendCheck.isSelected());

        confirmedLineChart.setAnimated(false);
        confirmedLineChart.getData().clear();
        confirmedLineChart.setAnimated(true);
        confirmedLineChart.getXAxis().setAnimated(false);
        confirmedLineChart.setTitle(currentTitle + " - " + message("Confirmed"));
        confirmedLineChart.setCreateSymbols(true);
        confirmedLineChart.setLegendVisible(legendCheck.isSelected());

        healedLineChart.setAnimated(false);
        healedLineChart.getData().clear();
        healedLineChart.setAnimated(true);
        healedLineChart.getXAxis().setAnimated(false);
        healedLineChart.setTitle(currentTitle + " - " + message("Healed"));
        healedLineChart.setCreateSymbols(true);
        healedLineChart.setLegendVisible(legendCheck.isSelected());

        deadLineChart.setAnimated(false);
        deadLineChart.getData().clear();
        deadLineChart.setAnimated(true);
        deadLineChart.getXAxis().setAnimated(false);
        deadLineChart.setTitle(currentTitle + " - " + message("Dead"));
        deadLineChart.setCreateSymbols(true);
        deadLineChart.setLegendVisible(legendCheck.isSelected());

        mapController.clearAction();
    }

    protected void drawCharts() {
        if (isSettingValues) {
            return;
        }
        initCharts();
        if (currentChartsType == null || currentChartsType == ChartsType.None
                || currentChartsSQL == null) {
            return;
        }
        synchronized (this) {
            if (chartsTask != null) {
                chartsTask.cancel();
            }
            chartsTask = new SingletonTask<Void>() {
                private List<EpidemicReport> chartsReports, mapReports;

                @Override
                protected boolean handle() {
                    chartsReports = TableEpidemicReport.dataQuery(currentChartsSQL);
                    if (currentChartsSQL.equals(currentMapSQL)) {
                        mapReports = chartsReports;
                    } else {
                        mapReports = TableEpidemicReport.dataQuery(currentMapSQL);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (null == chartsReports || chartsReports.isEmpty()) {
                        tabPane.getTabs().removeAll(chartsDataTab, numberTab, increasedTab, ratioTab,
                                confirmedTab, healedTab, deadTab, mapTab);
                        return;
                    }
                    isSettingValues = true;
                    chartsData.setAll(chartsReports);
                    isSettingValues = false;
                    chartsTableView.refresh();
                    chartsSizeLabel.setText(message("Size") + ": " + chartsReports.size());

                    allZero = true;
                    for (EpidemicReport report : chartsReports) {
                        if (report.getIncreasedConfirmed() != 0) {
                            allZero = false;
                            break;
                        }
                    }
                    if (allZero) {
                        popInformation(message("EpidemicReportAllZeroComments"), 6000);
                    }
                    if (null != currentChartsType) {
                        LoadingController loading = openHandlingStage(Modality.WINDOW_MODAL);
                        switch (currentChartsType) {
                            case TimeBased:
                                drawTimeBasedCharts(chartsReports);
                                mapController.drawTimeBasedMap(interval, mapLevel(), valuesCheck.isSelected(), mapReports);
                                break;
                            case LocationBased:
                                drawLocationBasedCharts(chartsReports);
                                mapController.drawLocationBasedMap(mapLevel(), valuesCheck.isSelected(), mapReports);
                                break;
                            case TimeLocationBased:
                                drawTimeLocationBasedCharts(chartsReports);
                                mapController.drawTimeBasedMap(interval, mapLevel(), valuesCheck.isSelected(), mapReports);
                                break;
                            default:
                                break;
                        }
                        if (loading != null) {
                            loading.closeStage();
                        }
                    }

                }

                @Override
                protected void taskQuit() {
                    endTime = new Date().getTime();
                    chartsTask = null;
                }
            };
            openHandlingStage(chartsTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(chartsTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected XYChart.Data lineDataNode(String label, Object value) {
        XYChart.Data data = new XYChart.Data(label, value);
        if (valuesCheck.isSelected()) {
            Label text = new Label(value + "");
            text.setStyle("-fx-background-color: transparent;  -fx-font-size: 0.8em;");
            data.setNode(text);
        }
        return data;
    }

    protected XYChart.Data lineDataNode(String label, String prefix,
            Object value) {
        XYChart.Data data = new XYChart.Data(label, value);
        if (valuesCheck.isSelected()) {
            Label text = new Label(prefix + "\n" + value);
            text.setStyle("-fx-background-color: transparent;  -fx-font-size: 0.7em; -fx-font-weight: bolder;");
            data.setNode(text);
        }
        return data;
    }

    protected XYChart.Data dataNode(String label, Object value) {
        XYChart.Data data = new XYChart.Data(label, value);
        return data;
    }

    protected void drawTimeBasedCharts(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }
            XYChart.Series confirmedSeries = new XYChart.Series();
            XYChart.Series healedSeries = new XYChart.Series();
            XYChart.Series deadSeries = new XYChart.Series();
            confirmedSeries.setName(message("Confirmed"));
            healedSeries.setName(message("Healed"));
            deadSeries.setName(message("Dead"));

            XYChart.Series increasedConfirmedSeries = new XYChart.Series();
            XYChart.Series increasedHealedSeries = new XYChart.Series();
            XYChart.Series increasedDeadSeries = new XYChart.Series();
            increasedConfirmedSeries.setName(message("IncreasedConfirmed"));
            increasedHealedSeries.setName(message("IncreasedHealed"));
            increasedDeadSeries.setName(message("IncreasedDead"));

            XYChart.Series healedRatioSeries = new XYChart.Series();
            XYChart.Series deadRatioSeries = new XYChart.Series();
            healedRatioSeries.setName(message("HealedRatio"));
            deadRatioSeries.setName(message("DeadRatio"));

            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());

                confirmedSeries.getData().add(lineDataNode(label, report.getConfirmed()));
                healedSeries.getData().add(lineDataNode(label, report.getHealed()));
                deadSeries.getData().add(lineDataNode(label, report.getDead()));

                increasedConfirmedSeries.getData().add(lineDataNode(label, report.getIncreasedConfirmed()));
                increasedHealedSeries.getData().add(lineDataNode(label, report.getIncreasedHealed()));
                increasedDeadSeries.getData().add(lineDataNode(label, report.getIncreasedDead()));

                healedRatioSeries.getData().add(lineDataNode(label, report.getHealedRatio()));
                deadRatioSeries.getData().add(lineDataNode(label, report.getDeadRatio()));
            }
            numberLineChart.getData().add(0, confirmedSeries);
            numberLineChart.getData().add(1, healedSeries);
            numberLineChart.getData().add(2, deadSeries);

            increasedLineChart.getData().add(0, increasedConfirmedSeries);
            increasedLineChart.getData().add(1, increasedHealedSeries);
            increasedLineChart.getData().add(2, increasedDeadSeries);

            ratioLineChart.getData().add(0, healedRatioSeries);
            ratioLineChart.getData().add(1, deadRatioSeries);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected String locationLabel(EpidemicReport report) {
        String level = report.getLevel();
        if (message("City").equals(level)) {
            return report.getCity();
        } else if (message("AllTime").equals(level)) {
            return report.getCountry();
        } else if (message("Countries").equals(level)) {
            return report.getCountry();
        } else if (message("CountryProvinces").equals(level)) {
            return report.getProvince();
        } else if (message("ProvinceCities").equals(level)) {
            return report.getCity();
        } else if (message("Province").equals(level)) {
            return report.getProvince();
        } else if (message("Country").equals(level)) {
            return report.getCountry();
        } else if (message("City").equals(level)) {
            return report.getCity();
        } else {
            logger.debug(level);
            return null;
        }
    }

    protected int mapLevel() {
        switch (currentNodeLevel) {
            case Time:
                return 3;
            case Country:
                return 3;
            case Provinces:
                return 5;
            case Province:
                return 7;
            case Cities:
                return 9;
            case City:
                return 9;
        }
        return 3;
    }

    protected void drawLocationBasedCharts(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }
            XYChart.Series confirmedSeries = new XYChart.Series();
            XYChart.Series healedSeries = new XYChart.Series();
            XYChart.Series deadSeries = new XYChart.Series();
            confirmedSeries.setName(message("Confirmed"));
            healedSeries.setName(message("Healed"));
            deadSeries.setName(message("Dead"));

            XYChart.Series increasedConfirmedSeries = new XYChart.Series();
            XYChart.Series increasedHealedSeries = new XYChart.Series();
            XYChart.Series increasedDeadSeries = new XYChart.Series();
            increasedConfirmedSeries.setName(message("IncreasedConfirmed"));
            increasedHealedSeries.setName(message("IncreasedHealed"));
            increasedDeadSeries.setName(message("IncreasedDead"));

            XYChart.Series healedRatioSeries = new XYChart.Series();
            XYChart.Series deadRatioSeries = new XYChart.Series();
            healedRatioSeries.setName(message("HealedRatio"));
            deadRatioSeries.setName(message("DeadRatio"));

            for (EpidemicReport report : reports) {
                String label = locationLabel(report);

                confirmedSeries.getData().add(dataNode(label, report.getConfirmed()));
                healedSeries.getData().add(dataNode(label, report.getHealed()));
                deadSeries.getData().add(dataNode(label, report.getDead()));

                increasedConfirmedSeries.getData().add(dataNode(label, report.getIncreasedConfirmed()));
                increasedHealedSeries.getData().add(dataNode(label, report.getIncreasedHealed()));
                increasedDeadSeries.getData().add(dataNode(label, report.getIncreasedDead()));

                healedRatioSeries.getData().add(dataNode(label, report.getHealedRatio()));
                deadRatioSeries.getData().add(dataNode(label, report.getDeadRatio()));
            }

            numberBarChart.getData().add(0, confirmedSeries);
            numberBarChart.getData().add(1, healedSeries);
            numberBarChart.getData().add(2, deadSeries);

            increasedBarChart.getData().add(0, increasedConfirmedSeries);
            increasedBarChart.getData().add(1, increasedHealedSeries);
            increasedBarChart.getData().add(2, increasedDeadSeries);

            ratioBarChart.getData().add(0, healedRatioSeries);
            ratioBarChart.getData().add(1, deadRatioSeries);

            drawLocationBasedPies(reports);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void drawLocationBasedPies(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }
            int totalConfirmed = 0, totalDead = 0, totalHealed = 0;
            for (EpidemicReport report : reports) {
                totalConfirmed += report.getConfirmed();
                totalDead += report.getDead();
                totalHealed += report.getHealed();
            }
            ObservableList<PieChart.Data> confirmedData = FXCollections.observableArrayList();
            ObservableList<PieChart.Data> deadData = FXCollections.observableArrayList();
            ObservableList<PieChart.Data> headedData = FXCollections.observableArrayList();
            boolean showLabel = valuesCheck.isSelected();
            String baselabel, label;
            for (EpidemicReport report : reports) {
                baselabel = locationLabel(report);
                if (totalConfirmed > 0) {
                    label = baselabel;
                    if (showLabel) {
                        label += " " + DoubleTools.scale(report.getConfirmed() * 100.0d / totalConfirmed, 1) + "%"
                                + " " + report.getConfirmed();
                    }
                    confirmedData.add(new PieChart.Data(label, report.getConfirmed()));
                }
                if (totalDead > 0) {
                    label = baselabel;
                    if (showLabel) {
                        label += " " + DoubleTools.scale(report.getDead() * 100.0d / totalDead, 1) + "%"
                                + " " + report.getDead();
                    }
                    deadData.add(new PieChart.Data(label, report.getDead()));
                }
                if (totalHealed > 0) {
                    label = baselabel;
                    if (showLabel) {
                        label += " " + DoubleTools.scale(report.getHealed() * 100.0d / totalHealed, 1) + "%"
                                + " " + report.getHealed();
                    }
                    headedData.add(new PieChart.Data(label, report.getHealed()));
                }
            }
            confirmedPie.setData(confirmedData);
            deadPie.setData(deadData);
            healedPie.setData(headedData);

            FxmlControl.setPieColors(confirmedPie, legendCheck.isSelected());
            FxmlControl.setPieColors(deadPie, legendCheck.isSelected());
            FxmlControl.setPieColors(healedPie, legendCheck.isSelected());

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected boolean locationMatch(String location, EpidemicReport report) {
        switch (currentNodeLevel) {
            case Provinces:
                if (location.equals(report.getProvince())) {
                    return true;
                }
                break;
            case Cities:
                if (location.equals(report.getCity())) {
                    return true;
                }
                break;
            default:
                if (location.equals(report.getCountry())) {
                    return true;
                }
                break;
        }
        return false;
    }

    protected List<String> timeLocationValues(List<EpidemicReport> reports,
            List<Date> validTimes,
            Map<Date, List<EpidemicReport>> timesLocations) {
        try {
            if (reports == null || validTimes == null || timesLocations == null) {
                return null;
            }
            List<Date> times = timesMap.get(currentDataSet);
            final List<String> locations;
            switch (currentNodeLevel) {
                case AllTime:
                case Countries:
                    locations = countriesMap.get(currentDataSet);
                    break;
                case Provinces:
                    locations = provincesMap.get(currentDataSet + currentCountry);
                    break;
                case Cities:
                    locations = citiesMap.get(currentDataSet + currentCountry + currentProvince);
                    break;
                default:
                    return null;
            }
            if (locations == null || locations.isEmpty()) {
                return null;
            }
            for (Date time : times) {
                List<EpidemicReport> timeReports = new ArrayList();
                boolean hasData = false;
                for (String location : locations) {
                    if (location.isBlank()) {
                        continue;
                    }
                    EpidemicReport timeReport = null;
                    for (EpidemicReport report : reports) {
                        if (report.getTime() == time.getTime()) {
                            if (locationMatch(location, report)) {
                                if (report.getConfirmed() > 0) {
                                    timeReport = report;
                                    hasData = true;
                                }
                                break;
                            }
                        }
                    }
                    if (timeReport == null) {
                        timeReport = new EpidemicReport();
                        switch (currentNodeLevel) {
                            case AllTime:
                            case Countries:
                                timeReport.setLevel(message("Country")).setCountry(location);
                                break;
                            case Provinces:
                                timeReport.setLevel(message("Province")).setProvince(location);
                                break;
                            case Cities:
                                timeReport.setLevel(message("City")).setCity(location);
                                break;
                            default:
                                continue;
                        }
                    }
                    timeReports.add(timeReport);
                }
                if (!hasData) {
                    continue;
                }

                timesLocations.put(time, timeReports);
                validTimes.add(time);
            }

            return locations;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    protected void drawTimeLocationBasedCharts(
            List<EpidemicReport> reports) {
        try {
            Map<Date, List<EpidemicReport>> timesLocations = new HashMap<>();
            List<Date> validTimes = new ArrayList();
            List<String> locations = timeLocationValues(reports, validTimes, timesLocations);
            if (validTimes.isEmpty()) {
                return;
            }
            drawTimeLocationBasedBarCharts(reports, locations, validTimes, timesLocations);
            drawTimeLocationBasedLineCharts(reports, locations, validTimes, timesLocations);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void drawTimeLocationBasedBarCharts(
            List<EpidemicReport> reports) {
        try {
            Map<Date, List<EpidemicReport>> timesLocations = new HashMap<>();
            List<Date> validTimes = new ArrayList();
            List<String> locations = timeLocationValues(reports, validTimes, timesLocations);
            if (validTimes.isEmpty()) {
                return;
            }
            drawTimeLocationBasedBarCharts(reports, locations, validTimes, timesLocations);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void drawTimeLocationBasedBarCharts(List<EpidemicReport> reports,
            List<String> locations, List<Date> validTimes,
            Map<Date, List<EpidemicReport>> timesLocations) {
        try {
            final LoadingController loading;
            final SnapshotParameters snapPara;
            final double scale;
            List<File> numberSnapshots, increasedSnapshots, ratioSnapshots;
            if (isSnapping) {
                loading = openHandlingStage(Modality.WINDOW_MODAL);
                double scalev = dpi / Screen.getPrimary().getDpi();
                scale = scalev > 1 ? scalev : 1;
                snapPara = new SnapshotParameters();
                snapPara.setFill(Color.WHITE);
                snapPara.setTransform(Transform.scale(scale, scale));
                numberSnapshots = new ArrayList();
                increasedSnapshots = new ArrayList();
                ratioSnapshots = new ArrayList();
            } else {
                loading = null;
                snapPara = null;
                numberSnapshots = increasedSnapshots = ratioSnapshots = null;
                scale = 1;
            }
            numberBarChart.setAnimated(false);
            numberBarChart.getData().clear();
            numberBarChart.getXAxis().setAnimated(false);
            numberBarChart.getYAxis().setAnimated(false);

            increasedBarChart.setAnimated(false);
            increasedBarChart.getData().clear();
            increasedBarChart.getXAxis().setAnimated(false);
            increasedBarChart.getYAxis().setAnimated(false);

            ratioBarChart.setAnimated(false);
            ratioBarChart.getData().clear();
            ratioBarChart.getXAxis().setAnimated(false);
            ratioBarChart.getYAxis().setAnimated(false);
            ratioBarChart.setTitle(currentTitle + " - " + message("Ratio"));

            int realInterval = isSnapping ? 200 : interval;

            timeLocationIndex = validTimes.size() - 1;
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        Date time = validTimes.get(timeLocationIndex);
                        List<EpidemicReport> timeReports = timesLocations.get(time);
                        drawTimeLocationBasedBarCharts(time, timeReports);

                        if (isSnapping && snapPara != null) {
                            File file = FileTools.getTempFile(".png");
                            BufferedImage image = SwingFXUtils.fromFXImage(snap(snapPara, scale, numberTab, numberBarChart), null);
                            ImageFileWriters.writeImageFile(image, "png", file.getAbsolutePath());
                            numberSnapshots.add(file);

                            file = FileTools.getTempFile(".png");
                            image = SwingFXUtils.fromFXImage(snap(snapPara, scale, increasedTab, increasedBarChart), null);
                            ImageFileWriters.writeImageFile(image, "png", file.getAbsolutePath());
                            increasedSnapshots.add(file);

                            file = FileTools.getTempFile(".png");
                            image = SwingFXUtils.fromFXImage(snap(snapPara, scale, ratioTab, ratioBarChart), null);
                            ImageFileWriters.writeImageFile(image, "png", file.getAbsolutePath());
                            ratioSnapshots.add(file);

                        }

                        timeLocationIndex--;
                        if (timeLocationIndex < 0) {
                            if (isSnapping && snapPara != null) {
                                timer.cancel();
                                timer = null;

                                numberSnapshotsFile = new File(AppVariables.MyBoxTempPath + File.separator + "numberSnapshots.gif");
                                ImageGifFile.writeImageFiles(numberSnapshots, numberSnapshotsFile, interval, true);
                                numberSnapshots.clear();

                                increasedSnapshotsFile = new File(AppVariables.MyBoxTempPath + File.separator + "increasedSnapshots.gif");
                                ImageGifFile.writeImageFiles(increasedSnapshots, increasedSnapshotsFile, interval, true);
                                increasedSnapshots.clear();

                                ratioSnapshotsFile = new File(AppVariables.MyBoxTempPath + File.separator + "ratioSnapshots.gif");
                                ImageGifFile.writeImageFiles(ratioSnapshots, ratioSnapshotsFile, interval, true);
                                ratioSnapshots.clear();

                                if (loading != null) {
                                    loading.closeStage();
                                }
                                isSnapping = false;
                                drawTimeLocationBasedBarCharts(chartsData);
                                mapController.snapMap();
                            } else {
                                timeLocationIndex = validTimes.size() - 1;
                            }
                        }
                    });
                }

            }, 0, realInterval);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void drawTimeLocationBasedBarCharts(Date time,
            List<EpidemicReport> timeReports) {

        numberBarChart.getData().clear();
        numberBarChart.setTitle(currentTitle + " - " + DateTools.datetimeToString(time));
        numberBarChart.setAlternativeRowFillVisible(true);
        numberBarChart.setAlternativeColumnFillVisible(true);
        XYChart.Series confirmedSeries = new XYChart.Series();
        XYChart.Series healedSeries = new XYChart.Series();
        XYChart.Series deadSeries = new XYChart.Series();
        confirmedSeries.setName(message("Confirmed"));
        healedSeries.setName(message("Healed"));
        deadSeries.setName(message("Dead"));

        increasedBarChart.getData().clear();
        increasedBarChart.setTitle(currentTitle + " - " + DateTools.datetimeToString(time));
        increasedBarChart.setAlternativeRowFillVisible(true);
        increasedBarChart.setAlternativeColumnFillVisible(true);
        XYChart.Series increasedConfirmedSeries = new XYChart.Series();
        XYChart.Series increasedHealedSeries = new XYChart.Series();
        XYChart.Series increasedDeadSeries = new XYChart.Series();
        increasedConfirmedSeries.setName(message("IncreasedConfirmed"));
        increasedHealedSeries.setName(message("IncreasedHealed"));
        increasedDeadSeries.setName(message("IncreasedDead"));

        ratioBarChart.getData().clear();
        ratioBarChart.setTitle(currentTitle + " - " + message("Ratio") + " - " + DateTools.datetimeToString(time));
        ratioBarChart.setAlternativeRowFillVisible(true);
        ratioBarChart.setAlternativeColumnFillVisible(true);
        XYChart.Series healedRatioSeries = new XYChart.Series();
        XYChart.Series deadRatioSeries = new XYChart.Series();
        healedRatioSeries.setName(message("HealedRatio"));
        deadRatioSeries.setName(message("DeadRatio"));

        if (timeReports == null || timeReports.isEmpty()) {
            return;
        }
        Collections.sort(timeReports, (EpidemicReport r1, EpidemicReport r2) -> {
            double diff = r2.getConfirmed() - r1.getConfirmed();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        });
        for (EpidemicReport report : timeReports) {
            String label;
            switch (currentNodeLevel) {
                case AllTime:
                case Countries:
                    label = report.getCountry();
                    break;
                case Provinces:
                    label = report.getProvince();
                    break;
                case Cities:
                    label = report.getCity();
                    break;
                default:
                    continue;
            }

            if (label == null || label.isBlank()) {
                continue;
            }
            confirmedSeries.getData().add(dataNode(label, report.getConfirmed()));
            healedSeries.getData().add(dataNode(label, report.getHealed()));
            deadSeries.getData().add(dataNode(label, report.getDead()));

            increasedConfirmedSeries.getData().add(dataNode(label, report.getIncreasedConfirmed()));
            increasedHealedSeries.getData().add(dataNode(label, report.getIncreasedHealed()));
            increasedDeadSeries.getData().add(dataNode(label, report.getIncreasedDead()));

            healedRatioSeries.getData().add(dataNode(label, report.getHealedRatio()));
            deadRatioSeries.getData().add(dataNode(label, report.getDeadRatio()));

        }
        numberBarChart.getData().add(0, confirmedSeries);
        numberBarChart.getData().add(1, healedSeries);
        numberBarChart.getData().add(2, deadSeries);

        increasedBarChart.getData().add(0, increasedConfirmedSeries);
        increasedBarChart.getData().add(1, increasedHealedSeries);
        increasedBarChart.getData().add(2, increasedDeadSeries);

        ratioBarChart.getData().add(0, healedRatioSeries);
        ratioBarChart.getData().add(1, deadRatioSeries);
    }

    protected Image snap(SnapshotParameters snapPara, double scale,
            Tab tab, BarChart chart) {
        tabPane.getSelectionModel().select(tab);
        Bounds bounds = chart.getLayoutBounds();
        int imageWidth = (int) Math.round(bounds.getWidth() * scale);
        int imageHeight = (int) Math.round(bounds.getHeight() * scale);
        return chart.snapshot(snapPara, new WritableImage(imageWidth, imageHeight));

    }

    protected void drawTimeLocationBasedLineCharts(List<EpidemicReport> reports,
            List<String> locations, List<Date> validTimes,
            Map<Date, List<EpidemicReport>> timesLocations) {
        try {
            List<Date> ascTimes = new ArrayList<>();
            ascTimes.addAll(validTimes);
            Collections.sort(ascTimes, (Date d1, Date d2) -> {
                long diff = d1.getTime() - d2.getTime();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                } else {
                    return 0;
                }
            });
            Map<String, XYChart.Series> confirmedSeriesMap = new HashMap<>();
            Map<String, XYChart.Series> healedSeriesMap = new HashMap<>();
            Map<String, XYChart.Series> deadSeriesMap = new HashMap<>();

            for (int i = 0; i < locations.size(); i++) {
                String location = locations.get(i);
                XYChart.Series confirmedSeries = new XYChart.Series();
                confirmedSeries.setName(location);
                confirmedSeriesMap.put(location, confirmedSeries);
                confirmedLineChart.getData().add(i, confirmedSeries);

                XYChart.Series healedSeries = new XYChart.Series();
                healedSeries.setName(location);
                healedSeriesMap.put(location, healedSeries);
                healedLineChart.getData().add(i, healedSeries);

                XYChart.Series deadSeries = new XYChart.Series();
                deadSeries.setName(location);
                deadSeriesMap.put(location, deadSeries);
                deadLineChart.getData().add(i, deadSeries);
            }

            for (int i = 0; i < ascTimes.size(); i++) {
                Date time = ascTimes.get(i);
                List<EpidemicReport> timeReports = timesLocations.get(time);
                if (timeReports == null || timeReports.isEmpty()) {
                    continue;
                }
                for (EpidemicReport report : timeReports) {
                    String location;
                    switch (currentNodeLevel) {
                        case AllTime:
                        case Countries:
                            location = report.getCountry();
                            break;
                        case Provinces:
                            location = report.getProvince();
                            break;
                        case Cities:
                            location = report.getCity();
                            break;
                        default:
                            continue;
                    }
                    if (location == null || location.isBlank()) {
                        continue;
                    }
                    String timeString = DateTools.datetimeToString(time);
                    XYChart.Series confirmedSeries = confirmedSeriesMap.get(location);
                    XYChart.Series healedSeries = healedSeriesMap.get(location);
                    XYChart.Series deadSeries = deadSeriesMap.get(location);
                    if (i == ascTimes.size() - 1) {
                        confirmedSeries.getData().add(lineDataNode(timeString, location, report.getConfirmed()));
                        healedSeries.getData().add(lineDataNode(timeString, location, report.getHealed()));
                        deadSeries.getData().add(lineDataNode(timeString, location, report.getDead()));
                    } else {
                        confirmedSeries.getData().add(lineDataNode(timeString, report.getConfirmed()));
                        healedSeries.getData().add(lineDataNode(timeString, report.getHealed()));
                        deadSeries.getData().add(lineDataNode(timeString, report.getDead()));
                    }
                }
            }

            FxmlControl.setLineChartColors(confirmedLineChart, legendCheck.isSelected());
            FxmlControl.setLineChartColors(healedLineChart, legendCheck.isSelected());
            FxmlControl.setLineChartColors(deadLineChart, legendCheck.isSelected());

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        super.checkSelected();
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        sureButton.setDisable(selection == 0);
    }

    @FXML
    @Override
    public void addAction() {
        try {
            EpidemicReportEditController controller
                    = (EpidemicReportEditController) openScene(null, CommonValues.EpidemicReportEditFxml);
            if (controller != null) {
                controller.parent = this;
                if (currentDataSet != null) {
                    controller.datasetSelector.setValue(currentDataSet);
                }
                if (currentCountry != null) {
                    controller.countrySelector.setValue(currentCountry);
                }
                if (currentProvince != null) {
                    controller.provinceSelector.setValue(currentProvince);
                }
                if (currentCity != null) {
                    controller.citySelector.setValue(currentCity);
                }
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
                            CommonValues.EpidemicReportsCountriesEditFxml);
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
            loadTree(false);
            return true;
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
    public void sureAction() {
        final List<EpidemicReport> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    TableEpidemicReport.sure(selected);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    load();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        htmlAction();
    }

    @FXML
    public void htmlAction() {
        try {
            String name = (message("EpidemicReport") + "-" + currentTitle + "_"
                    + DateTools.datetimeToString(new Date())).replaceAll(":", "-");
            htmlFile = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    name, CommonFxValues.HtmlExtensionFilter, true);
            if (htmlFile == null) {
                return;
            }
            recordFileWritten(htmlFile);

            currentTab = tabPane.getSelectionModel().getSelectedItem();
            if (currentChartsType == ChartsType.TimeLocationBased) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                isSnapping = true;
                drawTimeLocationBasedBarCharts(chartsData);
            } else if (tabPane.getTabs().contains(mapTab)) {
                mapController.snapMap();
            } else {
                makeHtml(null);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void makeHtml(File mapImageFile) {
        try {
            double scale = dpi / Screen.getPrimary().getDpi();
            scale = scale > 1 ? scale : 1;
            SnapshotParameters snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(Transform.scale(scale, scale));

            final Image numberLineChartSnap, numberBarChartSnap,
                    increasedLineChartSnap, increasedBarChartSnap,
                    ratioLineChartSnap, ratioBarChartSnap,
                    confirmedPieSnap, healedPieSnap, deadPieSnap,
                    confirmedLineChartSnap, healedLineChartSnap, deadLineChartSnap;
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
                if (numberChartBox.getChildren().contains(numberBarChart)
                        && currentChartsType != ChartsType.TimeLocationBased) {
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

            if (tabPane.getTabs().contains(increasedTab)) {
                tabPane.getSelectionModel().select(increasedTab);
                if (increasedChartBox.getChildren().contains(increasedLineChart)) {
                    Bounds bounds = increasedLineChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    increasedLineChartSnap = increasedLineChart.snapshot(snapPara, snapshot);
                } else {
                    increasedLineChartSnap = null;
                }
                if (increasedChartBox.getChildren().contains(increasedBarChart)
                        && currentChartsType != ChartsType.TimeLocationBased) {
                    Bounds bounds = increasedBarChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    increasedBarChartSnap = increasedBarChart.snapshot(snapPara, snapshot);
                } else {
                    increasedBarChartSnap = null;
                }
            } else {
                increasedLineChartSnap = null;
                increasedBarChartSnap = null;
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
                if (ratioChartBox.getChildren().contains(ratioBarChart)
                        && currentChartsType != ChartsType.TimeLocationBased) {
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
                if (confirmedChartBox.getChildren().contains(confirmedPie)) {
                    Bounds bounds = confirmedPie.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    confirmedPieSnap = confirmedPie.snapshot(snapPara, snapshot);
                } else {
                    confirmedPieSnap = null;
                }
                if (confirmedChartBox.getChildren().contains(confirmedLineChart)) {
                    Bounds bounds = confirmedLineChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    confirmedLineChartSnap = confirmedLineChart.snapshot(snapPara, snapshot);
                } else {
                    confirmedLineChartSnap = null;
                }
            } else {
                confirmedPieSnap = null;
                confirmedLineChartSnap = null;
            }

            if (tabPane.getTabs().contains(healedTab)) {
                tabPane.getSelectionModel().select(healedTab);
                if (healedChartBox.getChildren().contains(healedPie)) {
                    Bounds bounds = healedPie.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    healedPieSnap = healedPie.snapshot(snapPara, snapshot);
                } else {
                    healedPieSnap = null;
                }
                if (healedChartBox.getChildren().contains(healedLineChart)) {
                    Bounds bounds = healedLineChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    healedLineChartSnap = healedLineChart.snapshot(snapPara, snapshot);
                } else {
                    healedLineChartSnap = null;
                }
            } else {
                healedPieSnap = null;
                healedLineChartSnap = null;
            }

            if (tabPane.getTabs().contains(deadTab)) {
                tabPane.getSelectionModel().select(deadTab);
                if (deadChartBox.getChildren().contains(deadPie)) {
                    Bounds bounds = deadPie.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    deadPieSnap = deadPie.snapshot(snapPara, snapshot);
                } else {
                    deadPieSnap = null;
                }
                if (deadChartBox.getChildren().contains(deadLineChart)) {
                    Bounds bounds = deadLineChart.getLayoutBounds();
                    int imageWidth = (int) Math.round(bounds.getWidth() * scale);
                    int imageHeight = (int) Math.round(bounds.getHeight() * scale);
                    WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
                    deadLineChartSnap = deadLineChart.snapshot(snapPara, snapshot);
                } else {
                    deadLineChartSnap = null;
                }
            } else {
                deadPieSnap = null;
                deadLineChartSnap = null;
            }

            List<EpidemicReport> rows = chartsTableView.getSelectionModel().getSelectedItems();
            if (rows == null || rows.isEmpty()) {
                rows = chartsData;
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
                if (htmlTask != null) {
                    return;
                }
                htmlTask = new SingletonTask<Void>() {

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
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (numberBarChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(numberBarChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "numberBarChartSnap.jpg");
                                String imageName = subPath + "/numberBarChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (increasedLineChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(increasedLineChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "increasedLineChartSnap.jpg");
                                String imageName = subPath + "/increasedLineChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (increasedBarChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(increasedBarChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "increasedBarChartSnap.jpg");
                                String imageName = subPath + "/increasedBarChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (ratioLineChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(ratioLineChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "ratioLineChartSnap.jpg");
                                String imageName = subPath + "/ratioLineChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (ratioBarChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(ratioBarChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "ratioBarChartSnap.jpg");
                                String imageName = subPath + "/ratioBarChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (confirmedPieSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(confirmedPieSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "confirmedPieSnap.jpg");
                                String imageName = subPath + "/confirmedPieSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (healedPieSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(healedPieSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "healedPieSnap.jpg");
                                String imageName = subPath + "/healedPieSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (deadPieSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(deadPieSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "deadPieSnap.jpg");
                                String imageName = subPath + "/deadPieSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (confirmedLineChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(confirmedLineChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "confirmedLineChartSnap.jpg");
                                String imageName = subPath + "/confirmedLineChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (healedLineChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(healedLineChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "healedLineChartSnap.jpg");
                                String imageName = subPath + "/healedLineChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (deadLineChartSnap != null) {
                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(deadLineChartSnap, null);
                                ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "deadLineChartSnap.jpg");
                                String imageName = subPath + "/deadLineChartSnap.jpg";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (numberSnapshotsFile != null && numberSnapshotsFile.exists()) {
                                numberSnapshotsFile.renameTo(new File(path + File.separator + "numberSnapshots.gif"));
                                String imageName = subPath + "/numberSnapshots.gif";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }
                            if (increasedSnapshotsFile != null && increasedSnapshotsFile.exists()) {
                                increasedSnapshotsFile.renameTo(new File(path + File.separator + "increasedSnapshots.gif"));
                                String imageName = subPath + "/increasedSnapshots.gif";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }
                            if (ratioSnapshotsFile != null && ratioSnapshotsFile.exists()) {
                                ratioSnapshotsFile.renameTo(new File(path + File.separator + "ratioSnapshots.gif"));
                                String imageName = subPath + "/ratioSnapshots.gif";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            if (mapImageFile != null && mapImageFile.exists()) {
                                mapImageFile.renameTo(new File(path + File.separator + "mapSnap.gif"));
                                String imageName = subPath + "/mapSnap.gif";
                                s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                if (htmlTask == null || isCancelled()) {
                                    return false;
                                }
                            }

                            String html = HtmlTools.html("", s.toString());
                            FileTools.writeFile(htmlFile, html);

                            if (htmlTask == null || isCancelled()) {
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

                    @Override
                    protected void taskQuit() {
                        endTime = new Date().getTime();
                        htmlTask = null;
                    }
                };
                openHandlingStage(htmlTask, Modality.WINDOW_MODAL);
                Thread thread = new Thread(htmlTask);
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
        loadTree(false);
    }

    @Override
    public void loadExamples() {
        File file;
        if ("zh".equals(AppVariables.getLanguage())) {
            file = FxmlControl.getInternalFile("/data/db/Epidemic_Report_zh_6.2.1.txt",
                    "data", "Epidemic_Report_zh_6.2.1.txt");
        } else {
            file = FxmlControl.getInternalFile("/data/db/Epidemic_Report_en_6.2.1.txt",
                    "data", "Epidemic_Report_en_6.2.1.txt");
        }
        importFile(file);
    }

    // This is run in a task instead of FX application thread
    protected void importFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        List<EpidemicReport> data = EpidemicReport.readTxt(file);
        if (data.isEmpty()) {
            return;
        }
        TableEpidemicReport.write(data);
    }

    @FXML
    public void fetchAction() {
        EpidemicReportsFetchNCPDataController controller
                = (EpidemicReportsFetchNCPDataController) openStage(CommonValues.EpidemicReportsFetchNCPDataFxml);
        controller.parent = this;
    }

    @FXML
    @Override
    public void clearAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("SureClearConditions")
                + "\n\n" + currentTitle + "\n\n" + currentClearSQL);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonSure) {
            return;
        }
        synchronized (this) {
            if (clearTask != null) {
                return;
            }
            clearTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return new TableEpidemicReport().update(currentClearSQL) >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    refreshAction();
                }

                @Override
                protected void taskQuit() {
                    endTime = new Date().getTime();
                    clearTask = null;
                }
            };
            openHandlingStage(clearTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(clearTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void statisticAction() {
        loadTree(true);
    }

    @FXML
    public void fillAction() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private int count = 0;

                @Override
                protected boolean handle() {
                    count = TableEpidemicReport.fillData();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("TotalFilledData") + ": " + count, 5000);
                    loadTree();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void exportTableDataAction() {
        exportData(currentDataQuery);
    }

    @FXML
    protected void exportChartsDataAction() {
        exportData(currentChartsSQL);
    }

    protected void exportData(String sql) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory);

            String filePrefix = directory.getAbsolutePath() + File.separator
                    + (currentTitle + "_" + datetimeToString(new Date())).replaceAll(":", "-");
            synchronized (this) {
                if (exportTask != null) {
                    return;
                }
                exportTask = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        List<EpidemicReport> data = TableEpidemicReport.dataQuery(sql);
                        EpidemicReport.writeTxt(new File(filePrefix + ".txt"), data);
                        EpidemicReport.writeExcel(new File(filePrefix + ".xlsx"), data);
                        EpidemicReport.writeJson(new File(filePrefix + ".json"), data);
                        EpidemicReport.writeXml(new File(filePrefix + ".xml"), data);
                        EpidemicReport.writeHtml(new File(filePrefix + ".html"), data);
                        data.clear();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        popSuccessful();
                        browseURI(directory.toURI());
                    }

                    @Override
                    protected void taskQuit() {
                        endTime = new Date().getTime();
                        exportTask = null;
                    }
                };
                openHandlingStage(exportTask, Modality.WINDOW_MODAL);
                Thread thread = new Thread(exportTask);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void importAction() {
        final FileChooser fileChooser = new FileChooser();
        File path = AppVariables.getUserConfigPath(sourcePathKey);
        if (path.exists()) {
            fileChooser.setInitialDirectory(path);
        }
        fileChooser.getExtensionFilters().addAll(CommonFxValues.TextExtensionFilter);
        File file = fileChooser.showOpenDialog(getMyStage());
        if (file == null || !file.exists()) {
            return;
        }
        recordFileOpened(file);
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    importFile(file);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    refreshAction();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public boolean leavingScene() {
        try {
            if (treeTask != null) {
                treeTask.cancel();
                treeTask = null;
            }
            if (dataTask != null) {
                dataTask.cancel();
                dataTask = null;
            }
            if (chartsTask != null) {
                chartsTask.cancel();
                chartsTask = null;
            }
            if (importTask != null) {
                importTask.cancel();
                importTask = null;
            }
            if (exportTask != null) {
                exportTask.cancel();
                exportTask = null;
            }
            if (htmlTask != null) {
                htmlTask.cancel();
                htmlTask = null;
            }
            mapController.leavingScene();
        } catch (Exception e) {
        }
        return super.leavingScene();

    }

}
