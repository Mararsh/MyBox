package mara.mybox.controller;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.StringTable;
import static mara.mybox.data.StringTable.tableDiv;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlStage.openScene;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.fxml.TableDoubleCell;
import mara.mybox.image.ImageManufacture;
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
    protected String currentDataSet, currentLevel, currentTime, currentSizeQuery, currentDataQuery,
            currentCountry, currentProvince, currentCity, currentDistrict, currentTownship,
            currentNeighborhood, currentTitle, currentClearSQL, currentChartsSQL;
    protected boolean currentNodeIsLeaf, allZero;
    protected ChartsType chartsType;
    protected File htmlFile;
    protected Tab currentTab;
    protected SingletonTask treeTask;
    protected int dpi;

    @FXML
    protected TreeView treeView;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab numberTab, increasedTab, ratioTab, confirmedTab, healedTab, deadTab, mapTab;
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
    protected TableColumn<EpidemicReport, Integer> confirmedColumn, headledColumn, deadColumn,
            increasedConfirmedColumn, increasedHeadledColumn, increasedDeadColumn;
    @FXML
    protected TableColumn<EpidemicReport, Double> headledRatioColumn, deadRatioColumn;
    @FXML
    protected Button examplesButton, chinaButton, helpMeButton, catButton, statisticButton;
    @FXML
    protected VBox numberChartBox, increasedChartBox, ratioChartBox;
    @FXML
    protected BarChart numberBarChart, increasedBarChart, ratioBarChart;
    @FXML
    protected LineChart numberLineChart, increasedLineChart, ratioLineChart;
    @FXML
    protected PieChart deadPie, headledPie, confirmedPie;
    @FXML
    protected Label nameLabel;
    @FXML
    protected EpidemicReportMapController mapController;
    @FXML
    protected CheckBox recountCheck;
    @FXML
    private ComboBox<String> dpiSelector;

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
            currentDataSet = null;
            currentLevel = "All";

            chartsType = ChartsType.None;

            pageSizeSelector.setValue("200");

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
            increasedConfirmedColumn.setCellValueFactory(new PropertyValueFactory<>("increasedConfirmed"));
            headledColumn.setCellValueFactory(new PropertyValueFactory<>("healed"));
            increasedHeadledColumn.setCellValueFactory(new PropertyValueFactory<>("increasedHealed"));
            headledRatioColumn.setCellValueFactory(new PropertyValueFactory<>("healedRatio"));
            deadColumn.setCellValueFactory(new PropertyValueFactory<>("dead"));
            increasedDeadColumn.setCellValueFactory(new PropertyValueFactory<>("increasedDead"));
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
        try {
            super.afterSceneLoaded();
            FxmlControl.setTooltip(examplesButton, message("EpidemicReportsExamplesComments"));
            FxmlControl.setTooltip(chinaButton, message("ChineseProvincesEpidemicReports"));
            FxmlControl.setTooltip(helpMeButton, message("FetchNPCData"));
            FxmlControl.setTooltip(clearButton, message("Clear") + "\nCTRL+g\n\n" + message("RelativeClearComments"));
            FxmlControl.setTooltip(catButton, message("MyBoxInternetDataPath"));
            FxmlControl.setTooltip(statisticButton, message("EpidemicReportStatisticComments"));
            FxmlControl.setTooltip(recountCheck, message("EpidemicReportStatisticComments"));

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
        synchronized (this) {
            if (treeTask != null) {
                return;
            }
            treeTask = new SingletonTask<Void>() {
                private List<String> datasets, countries;
                private Map<String, List<Date>> timesMap;
                private Map<String, List<String>> provincesMap, citiesMap;

                @Override
                protected boolean handle() {
                    if (requireStatistic || recountCheck.isSelected()) {
                        TableEpidemicReport.statistic();
                    }

                    datasets = TableEpidemicReport.datasets();
                    if (datasets == null || datasets.isEmpty()) {
                        return true;
                    }
                    timesMap = new HashMap<>();
                    provincesMap = new HashMap<>();
                    citiesMap = new HashMap<>();
                    for (String dataset : datasets) {
                        countries = TableEpidemicReport.countries(dataset);
                        if (countries != null && !countries.isEmpty()) {
                            for (String country : countries) {
                                List<String> provinces = TableEpidemicReport.provinces(dataset, country);
                                if (provinces != null && !provinces.isEmpty()) {
                                    provincesMap.put(country, provinces);
                                    for (String province : provinces) {
                                        List<String> cities = TableEpidemicReport.cities(dataset, country, province);
                                        if (cities != null && !cities.isEmpty()) {
                                            citiesMap.put(country + province, cities);
                                        }
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
                }

                protected void writeTree() {
                    if (datasets == null || datasets.isEmpty()) {
                        return;
                    }
                    isSettingValues = true;
                    Text allLink = new Text(message("All"));
                    allLink.setOnMouseClicked((MouseEvent event) -> {
                        loadAll();
                    });
                    TreeItem root = new TreeItem<>(allLink);
                    root.setExpanded(true);
                    treeView.setRoot(root);

                    for (String dataset : datasets) {
                        Text datasetLink = new Text(dataset);
                        datasetLink.setOnMouseClicked((MouseEvent event) -> {
                            loadDataset(dataset);
                        });
                        TreeItem<Text> datasetItem = new TreeItem<>(datasetLink);
                        datasetItem.setExpanded(true);
                        root.getChildren().add(datasetItem);

                        timeTree(datasetItem, dataset);

                        Text countriesLink = new Text(message("Countries"));
                        countriesLink.setOnMouseClicked((MouseEvent event) -> {
                            loadCountries(dataset);
                        });
                        TreeItem<Text> countriesItem = new TreeItem<>(countriesLink);
                        countriesItem.setExpanded(false);
                        datasetItem.getChildren().add(countriesItem);

                        countrieTree(countriesItem, dataset, null);
                    }

                    isSettingValues = false;

                }

                protected void timeTree(TreeItem parent, String dataset) {
                    if (dataset == null) {
                        return;
                    }
                    List<Date> times = timesMap.get(dataset);
                    if (times == null || times.isEmpty()) {
                        return;
                    }
                    Text timesLink = new Text(message("Time"));
                    timesLink.setOnMouseClicked((MouseEvent event) -> {
                        loadAllTime(dataset);
                    });
                    TreeItem<Text> timesItem = new TreeItem<>(timesLink);
                    timesItem.setExpanded(false);
                    parent.getChildren().add(timesItem);

                    for (Date time : times) {
                        String timeString = DateTools.datetimeToString(time);
                        Text timeLink = new Text(timeString);
                        timeLink.setOnMouseClicked((MouseEvent event) -> {
                            loadTime(dataset, timeLink.getText());
                        });
                        TreeItem<Text> timeItem = new TreeItem<>(timeLink);
                        timeItem.setExpanded(false);
                        timesItem.getChildren().add(timeItem);
                        countrieTree(timeItem, dataset, timeString);
                    }

                }

                protected void countrieTree(TreeItem parent, String dataset,
                        String time) {
                    if (parent == null || countries == null || countries.isEmpty()) {
                        return;
                    }

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
                                loadCountry(dataset, time, country);
                            } else {
                                loadCountryProvinces(dataset, time, country);
                            }
                        });

                        provincesTree(countryItem, dataset, country, time);
                    }
                }

                protected void provincesTree(TreeItem parent, String dataset,
                        String country, String time) {
                    if (parent == null || country == null) {
                        return;
                    }
                    List<String> provinces = provincesMap.get(country);
                    if (provinces == null || provinces.isEmpty()) {
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
                                loadProvince(dataset, time, country, province);
                            } else {
                                loadProvinceCities(dataset, time, country, province);
                            }
                        });

                        citiesTree(provinceItem, dataset, country, province, time);
                    }
                }

                protected void citiesTree(TreeItem parent, String dataset,
                        String country, String province, String time) {
                    if (parent == null || country == null || province == null) {
                        return;
                    }
                    List<String> cities = citiesMap.get(country + province);
                    if (cities == null || cities.isEmpty()) {
                        return;
                    }
                    for (String city : cities) {
                        if (city.isBlank()) {
                            continue;
                        }
                        Text cityLink = new Text(city);
                        cityLink.setOnMouseClicked((MouseEvent event) -> {
                            loadCity(dataset, time, country, province, city);
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

    public void loadAll() {
        currentDataSet = null;
        currentLevel = "All";
        currentTime = null;
        load();
    }

    public void loadDataset(String dataset) {
        currentDataSet = dataset;
        currentLevel = "Dataset";
        currentTime = null;
        load();
    }

    public void loadCountries(String dataset) {
        currentDataSet = dataset;
        currentLevel = "Countries";
        currentTime = null;
        load();
    }

    public void loadAllTime(String dataset) {
        currentDataSet = dataset;
        currentLevel = "AllTime";
        currentTime = null;
        load();
    }

    public void loadTime(String dataset, String time) {
        currentDataSet = dataset;
        currentLevel = "Time";
        currentTime = time;
        load();
    }

    public void loadCountry(String dataset, String time, String country) {
        currentDataSet = dataset;
        currentLevel = "Country";
        currentTime = time;
        currentCountry = country;
        load();
    }

    public void loadCountryProvinces(String dataset, String time, String country) {
        currentDataSet = dataset;
        currentLevel = "CountryProvinces";
        currentTime = time;
        currentCountry = country;
        load();
    }

    public void loadProvince(String dataset, String time,
            String country, String province) {
        currentDataSet = dataset;
        currentLevel = "Province";
        currentTime = time;
        currentCountry = country;
        currentProvince = province;
        load();
    }

    public void loadProvinceCities(String dataset, String time,
            String country, String province) {
        currentDataSet = dataset;
        currentLevel = "ProvinceCities";
        currentTime = time;
        currentCountry = country;
        currentProvince = province;
        load();
    }

    public void loadCity(String dataset, String time,
            String country, String province, String city) {
        currentDataSet = dataset;
        currentLevel = "City";
        currentTime = time;
        currentCountry = country;
        currentProvince = province;
        currentCity = city;
        load();
    }

    @Override
    public void load() {
        tableData.clear();
        nameLabel.setText("");
        currentTitle = message("EpidemicReport");
        tableView.getColumns().clear();
        if (!"All".equals(currentLevel)) {
            if (currentDataSet == null) {
                return;
            }
            currentTitle += " " + currentDataSet;
            if (currentTime != null) {
                currentTitle += " " + currentTime;
            } else {
                tableView.getColumns().add(timeColumn);
            }
        }
        String where;
        switch (currentLevel) {
            case "All":
                currentSizeQuery = " SELECT count(dataid) FROM Epidemic_Report ";
                currentDataQuery = "SELECT * FROM Epidemic_Report "
                        + " ORDER BY data_set, time desc, country, province, confirmed desc ";
                currentTitle = message("EpidemicReport");
                currentClearSQL = "DELETE FROM Epidemic_Report";
                currentChartsSQL = null;
                chartsType = ChartsType.None;
                tableView.getColumns().addAll(datasetColumn, countryColumn, provinceColumn, cityColumn);
                break;
            case "Dataset":
                where = " WHERE data_set='" + currentDataSet + "' ";
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + " ORDER BY time desc, confirmed desc";
                currentTitle += " " + message("Global");
                currentClearSQL = "DELETE FROM Epidemic_Report " + where;
                currentChartsSQL = "SELECT * FROM Epidemic_Report "
                        + " WHERE data_set='" + currentDataSet + "' AND level='" + message("Global") + "'"
                        + " ORDER BY time , confirmed desc";
                chartsType = ChartsType.TimeBasedNoMap;
                tableView.getColumns().addAll(countryColumn, provinceColumn, cityColumn);
                break;
            case "Countries":
            case "AllTime":
                where = " WHERE data_set='" + currentDataSet + "' "
                        + " AND level='" + message("Country") + "' ";
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + " ORDER BY time desc, confirmed desc";
                currentTitle += " " + message("Global");
                currentClearSQL = "DELETE FROM Epidemic_Report WHERE data_set='" + currentDataSet + "' ";
                currentChartsSQL = "SELECT * FROM Epidemic_Report "
                        + " WHERE data_set='" + currentDataSet + "' AND level='" + message("Global") + "'"
                        + " ORDER BY time , confirmed desc";
                chartsType = ChartsType.TimeBasedNoMap;
                tableView.getColumns().addAll(countryColumn);
                break;
            case "Time":
                if (currentTime == null) {
                    return;
                }
                where = " WHERE data_set='" + currentDataSet + "' "
                        + " AND level='" + message("Country") + "' "
                        + " AND time='" + currentTime + "' ";
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + " ORDER BY confirmed desc";
                currentTitle += " " + message("Global");
                currentClearSQL = "DELETE FROM Epidemic_Report WHERE data_set='"
                        + currentDataSet + "' AND time='" + currentTime + "' ";
                currentChartsSQL = "SELECT * FROM Epidemic_Report "
                        + where + " ORDER BY confirmed desc";
                chartsType = ChartsType.LocationBased;
                tableView.getColumns().addAll(countryColumn);
                break;
            case "Country":
                if (currentCountry == null) {
                    return;
                }
                where = " WHERE data_set='" + currentDataSet + "' "
                        + " AND country='" + currentCountry + "' "
                        + " AND level='" + message("Country") + "' ";
                if (currentTime == null) {
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY time, confirmed desc";
                    chartsType = ChartsType.TimeBasedMap;
                } else {
                    where += " AND time='" + currentTime + "' ";
                    tableView.getColumns().addAll(provinceColumn);
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY confirmed desc";
                    chartsType = ChartsType.LocationBased;
                }
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + (currentTime == null ? " ORDER BY time desc, confirmed desc" : " ORDER BY confirmed desc");
                currentTitle += " " + currentCountry;
                currentClearSQL = "DELETE FROM Epidemic_Report WHERE data_set='"
                        + currentDataSet + "' AND country='" + currentCountry + "' ";
                if (currentTime != null) {
                    currentClearSQL += " AND time='" + currentTime + "' ";
                }
                break;
            case "CountryProvinces":
                if (currentCountry == null) {
                    return;
                }
                where = " WHERE data_set='" + currentDataSet + "' "
                        + " AND country='" + currentCountry + "' ";
                if (currentTime == null) {
                    where += " AND level='" + message("Country") + "' ";
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY time, confirmed desc";
                    chartsType = ChartsType.TimeBasedMap;
                } else {
                    where += " AND ( level='" + message("Province")
                            + "' OR level='" + message("Country") + "') "
                            + " AND time='" + currentTime + "' ";
                    tableView.getColumns().addAll(provinceColumn);
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + " WHERE data_set='" + currentDataSet + "' "
                            + " AND country='" + currentCountry + "' "
                            + " AND level='" + message("Province") + "' "
                            + " AND time='" + currentTime + "' "
                            + " ORDER BY confirmed desc";
                    chartsType = ChartsType.LocationBased;
                }
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + (currentTime == null ? " ORDER BY time desc, confirmed desc" : " ORDER BY confirmed desc");
                currentTitle += " " + currentCountry;
                currentClearSQL = "DELETE FROM Epidemic_Report WHERE data_set='"
                        + currentDataSet + "' AND country='" + currentCountry + "' ";
                if (currentTime != null) {
                    currentClearSQL += " AND time='" + currentTime + "' ";
                }
                break;
            case "Province":
                if (currentCountry == null || currentProvince == null) {
                    return;
                }
                where = " WHERE  data_set='" + currentDataSet + "' "
                        + " AND country='" + currentCountry + "' "
                        + " AND province='" + currentProvince + "' "
                        + " AND level='" + message("Province") + "' ";
                if (currentTime == null) {
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY time, confirmed desc";
                    chartsType = ChartsType.TimeBasedMap;
                } else {
                    where += " AND time='" + currentTime + "' ";
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY confirmed desc";
                    chartsType = ChartsType.LocationBased;
                    tableView.getColumns().addAll(cityColumn);
                }
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + (currentTime == null ? " ORDER BY time desc, confirmed desc" : " ORDER BY confirmed desc");
                currentTitle += " " + currentCountry + " " + currentProvince;
                currentClearSQL = "DELETE FROM Epidemic_Report WHERE data_set='"
                        + currentDataSet + "' AND country='" + currentCountry + "' "
                        + " AND province='" + currentProvince + "' ";
                if (currentTime != null) {
                    currentClearSQL += " AND time='" + currentTime + "' ";
                }
                break;
            case "ProvinceCities":
                if (currentCountry == null || currentProvince == null) {
                    return;
                }
                where = " WHERE  data_set='" + currentDataSet + "' "
                        + " AND country='" + currentCountry + "' "
                        + " AND province='" + currentProvince + "' ";
                if (currentTime == null) {
                    where += " AND level='" + message("Province") + "' ";
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY time, confirmed desc";
                    chartsType = ChartsType.TimeBasedMap;
                } else {
                    where += " AND NOT (level='" + message("Country") + "' ) "
                            + " AND time='" + currentTime + "' ";
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + " WHERE data_set='" + currentDataSet + "' "
                            + " AND country='" + currentCountry + "' "
                            + " AND province='" + currentProvince + "' "
                            + " AND NOT (level='" + message("Country") + "' ) "
                            + " AND NOT (level='" + message("Province") + "' ) "
                            + " AND time='" + currentTime + "' "
                            + " ORDER BY confirmed desc";
                    chartsType = ChartsType.LocationBased;
                    tableView.getColumns().addAll(cityColumn);
                }
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + (currentTime == null ? " ORDER BY time desc, confirmed desc" : " ORDER BY confirmed desc");
                currentTitle += " " + currentCountry + " " + currentProvince;
                currentClearSQL = "DELETE FROM Epidemic_Report WHERE data_set='"
                        + currentDataSet + "' AND country='" + currentCountry + "' "
                        + " AND province='" + currentProvince + "' ";
                if (currentTime != null) {
                    currentClearSQL += " AND time='" + currentTime + "' ";
                }
                break;
            case "City":
                if (currentCountry == null || currentProvince == null || currentCity == null) {
                    return;
                }
                where = " WHERE data_set='" + currentDataSet + "' "
                        + " AND country='" + currentCountry + "' "
                        + " AND province='" + currentProvince + "' "
                        + " AND city='" + currentCity + "' "
                        + " AND NOT(level='" + message("Province") + "') ";
                if (currentTime == null) {
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY time, confirmed desc";
                    chartsType = ChartsType.TimeBasedMap;
                } else {
                    where += " AND time='" + currentTime + "' ";
                    currentChartsSQL = "SELECT * FROM Epidemic_Report "
                            + where + " ORDER BY confirmed desc";
                    chartsType = ChartsType.LocationBased;
                }
                currentSizeQuery = "SELECT count(dataid) FROM Epidemic_Report " + where;
                currentDataQuery = "SELECT * FROM Epidemic_Report " + where
                        + (currentTime == null ? " ORDER BY time desc, confirmed desc" : " ORDER BY confirmed desc");
                currentTitle += " " + currentCountry + " " + currentProvince + " " + currentCity;
                currentClearSQL = "DELETE FROM Epidemic_Report WHERE data_set='"
                        + currentDataSet + "' AND country='" + currentCountry + "' "
                        + " AND province='" + currentProvince + "' "
                        + " AND city='" + currentCity + "' ";
                if (currentTime != null) {
                    currentClearSQL += " AND time='" + currentTime + "' ";
                }
                break;
        }
        tableView.getColumns().addAll(confirmedColumn, increasedConfirmedColumn,
                headledColumn, increasedHeadledColumn, headledRatioColumn,
                deadColumn, increasedDeadColumn, deadRatioColumn,
                longtitudeColumn, latitudeColumn);

        nameLabel.setText(currentTitle);
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

    protected void drawCharts() {
        if (isSettingValues) {
            return;
        }
        numberChartBox.getChildren().clear();
        ratioChartBox.getChildren().clear();
        increasedChartBox.getChildren().clear();

        if (chartsType == null || chartsType == ChartsType.None) {
            tabPane.getTabs().removeAll(numberTab, increasedTab, ratioTab,
                    confirmedTab, healedTab, deadTab, mapTab);
            return;
        } else {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            tabPane.getTabs().removeAll(mapTab);
            if (!tabPane.getTabs().contains(numberTab)) {
                tabPane.getTabs().addAll(numberTab, increasedTab, ratioTab);
            }
            if (chartsType == ChartsType.TimeBasedNoMap || chartsType == ChartsType.TimeBasedMap) {
                tabPane.getTabs().removeAll(confirmedTab, healedTab, deadTab);
                numberChartBox.getChildren().add(numberLineChart);
                increasedChartBox.getChildren().add(increasedLineChart);
                ratioChartBox.getChildren().add(ratioLineChart);

            } else if (chartsType == ChartsType.LocationBased) {
                numberChartBox.getChildren().add(numberBarChart);
                increasedChartBox.getChildren().add(increasedBarChart);
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

        increasedBarChart.setAnimated(false);
        increasedBarChart.getData().clear();
        increasedBarChart.setAnimated(true);
        increasedBarChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        increasedBarChart.setTitle(currentTitle + " - " + message("Increased"));

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

        increasedLineChart.setAnimated(false);
        increasedLineChart.getData().clear();
        increasedLineChart.setAnimated(true);
        increasedLineChart.getXAxis().setAnimated(false);  // X-Axis labels are messed if true
        increasedLineChart.setTitle(currentTitle + " - " + message("Increased"));

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
                    reports = TableEpidemicReport.dataQuery(currentChartsSQL);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (null == reports || reports.isEmpty()) {
                        tabPane.getTabs().removeAll(numberTab, increasedTab, ratioTab, confirmedTab, healedTab, deadTab, mapTab);
                        return;
                    }

                    allZero = true;
                    for (EpidemicReport report : reports) {
                        if (report.getIncreasedConfirmed() != 0) {
                            allZero = false;
                            break;
                        }
                    }
                    if (allZero) {
                        popInformation(message("EpidemicReportAllZeroComments"), 6000);
                    }
                    if (null != chartsType) {
                        switch (chartsType) {
                            case TimeBasedNoMap:
                                drawTimeBasedNumberLineChart(reports);
                                drawTimeBasedIncreasedLineChart(reports);
                                drawTimeBasedRatioLineChart(reports);
                                break;
                            case TimeBasedMap:
                                drawTimeBasedNumberLineChart(reports);
                                drawTimeBasedIncreasedLineChart(reports);
                                drawTimeBasedRatioLineChart(reports);
                                mapController.drawTimeBasedMap(mapLevel(), reports);
                                break;
                            case LocationBased:
                                drawLocationBasedNumberBarChart(reports);
                                drawLocationBasedIncreasedBarChart(reports);
                                drawLocationBasedNumberPieChart(reports);
                                drawLocationBasedRatioBarChart(reports);
                                mapController.drawLocationBasedMap(mapLevel(), reports);
                                break;
                            default:
                                break;
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

    protected void drawTimeBasedIncreasedLineChart(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }

            XYChart.Series confirmedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getIncreasedConfirmed());
                Tooltip.install(data.getNode(), new Tooltip(report.getIncreasedConfirmed() + ""));
                confirmedSeries.getData().add(data);
            }
            confirmedSeries.setName(message("IncreasedConfirmed"));
            increasedLineChart.getData().add(0, confirmedSeries);
            String colorString = FxmlColor.rgb2Hex(Color.BLUE);
            for (Node n
                    : increasedLineChart.lookupAll(".default-color0.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            XYChart.Series healedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getIncreasedHealed());
                Tooltip.install(data.getNode(), new Tooltip(report.getIncreasedHealed() + ""));
                healedSeries.getData().add(data);
            }
            healedSeries.setName(message("IncreasedHealed"));
            increasedLineChart.getData().add(1, healedSeries);
            colorString = FxmlColor.rgb2Hex(Color.RED);
            for (Node n
                    : increasedLineChart.lookupAll(".default-color1.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            XYChart.Series deadSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                String label = DateTools.datetimeToString(report.getTime());
                XYChart.Data data = new XYChart.Data(label, report.getIncreasedDead());
                Tooltip.install(data.getNode(), new Tooltip(report.getIncreasedDead() + ""));
                deadSeries.getData().add(data);
            }
            deadSeries.setName(message("IncreasedDead"));
            increasedLineChart.getData().add(2, deadSeries);
            colorString = FxmlColor.rgb2Hex(Color.BLACK);
            for (Node n
                    : increasedLineChart.lookupAll(".default-color2.chart-series-line")) {
                n.setStyle("-fx-stroke: " + colorString + "; ");
            }

            increasedLineChart.setLegendVisible(true);
            Set<Node> legendItems = increasedLineChart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    if (message("IncreasedDead").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLACK));
                    } else if (message("IncreasedHealed").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.RED));
                    } else if (message("IncreasedConfirmed").equals(legendLabel.getText())) {
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

    protected String locationLabel(EpidemicReport report) {
        if (message("City").equals(report.getLevel())) {
            return report.getCity();
        } else if (message("Province").equals(report.getLevel())) {
            return report.getProvince();
        } else if (message("Country").equals(report.getLevel())) {
            return report.getCountry();
        } else if (message("City").equals(report.getLevel())) {
            return report.getCity();
        } else {
            return message("Global");
        }
    }

    protected int mapLevel() {
        switch (currentLevel) {
            case "Time":
                return 3;
            case "Country":
                return 3;
            case "CountryProvinces":
                return 5;
            case "Province":
                return 7;
            case "ProvinceCities":
                return 9;
            case "City":
                return 9;
        }
        return 3;
    }

    protected void drawLocationBasedNumberBarChart(List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }

            XYChart.Series confirmedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                confirmedSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getConfirmed()));
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
                healedSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getHealed()));
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
                deadSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getDead()));
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

    protected void drawLocationBasedIncreasedBarChart(
            List<EpidemicReport> reports) {
        try {
            if (reports == null || reports.isEmpty()) {
                return;
            }
            XYChart.Series confirmedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                confirmedSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getIncreasedConfirmed()));
            }
            confirmedSeries.setName(message("IncreasedConfirmed"));
            increasedBarChart.getData().add(0, confirmedSeries);
            String colorString = FxmlColor.rgb2Hex(Color.BLUE);
            for (Node n
                    : increasedBarChart.lookupAll(".default-color0.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            XYChart.Series healedSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                healedSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getIncreasedHealed()));
            }
            healedSeries.setName(message("IncreasedHealed"));
            increasedBarChart.getData().add(1, healedSeries);
            colorString = FxmlColor.rgb2Hex(Color.RED);
            for (Node n
                    : increasedBarChart.lookupAll(".default-color1.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            XYChart.Series deadSeries = new XYChart.Series();
            for (EpidemicReport report : reports) {
                deadSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getIncreasedDead()));
            }
            deadSeries.setName(message("IncreasedDead"));
            increasedBarChart.getData().add(2, deadSeries);
            colorString = FxmlColor.rgb2Hex(Color.BLACK);
            for (Node n
                    : increasedBarChart.lookupAll(".default-color2.chart-bar")) {
                n.setStyle("-fx-bar-fill: " + colorString + "; ");
            }

            increasedBarChart.setLegendVisible(true);
            Set<Node> legendItems = increasedBarChart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    if (message("IncreasedDead").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.BLACK));
                    } else if (message("IncreasedHealed").equals(legendLabel.getText())) {
                        legend.setStyle("-fx-background-color: " + FxmlColor.rgb2Hex(Color.RED));
                    } else if (message("IncreasedConfirmed").equals(legendLabel.getText())) {
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
                    String label = locationLabel(report) + " "
                            + DoubleTools.scale(report.getConfirmed() * 100.0d / confirmed, 1) + "%"
                            + " " + report.getConfirmed();
                    confirmedData.add(new PieChart.Data(label, report.getConfirmed()));
                }
                confirmedPie.setData(confirmedData);
            }

            if (dead > 0) {
                ObservableList<PieChart.Data> deadData = FXCollections.observableArrayList();
                for (EpidemicReport report : reports) {
                    String label = locationLabel(report) + " "
                            + DoubleTools.scale(report.getDead() * 100.0d / dead, 1) + "%"
                            + " " + report.getDead();
                    deadData.add(new PieChart.Data(label, report.getDead()));
                }
                deadPie.setData(deadData);
            }

            if (healed > 0) {
                ObservableList<PieChart.Data> headedData = FXCollections.observableArrayList();
                for (EpidemicReport report : reports) {
                    String label = locationLabel(report) + " "
                            + DoubleTools.scale(report.getHealed() * 100.0d / healed, 1) + "%"
                            + " " + report.getHealed();
                    headedData.add(new PieChart.Data(label, report.getHealed()));
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
                healedRatioSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getHealedRatio()));
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
                deadRatioSeries.getData().add(
                        new XYChart.Data(locationLabel(report), report.getDeadRatio()));
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
            double scale = dpi / Screen.getPrimary().getDpi();
            scale = scale > 1 ? scale : 1;
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
                                    int width = 0;
                                    for (Image image : mapImages) {
                                        images.add(SwingFXUtils.fromFXImage(image, null));
                                        if (image.getWidth() > width) {
                                            width = (int) image.getWidth();
                                        }
                                    }
                                    File outFile = new File(path + File.separator + "mapSnap.gif");
                                    ImageGifFile.writeImages(images, outFile, mapController.interval);
                                    String imageName = subPath + "/mapSnap.gif";
                                    s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");

                                    if (width > 1000) {
                                        List<BufferedImage> simages = new ArrayList();
                                        for (BufferedImage image : images) {
                                            if (image.getWidth() > 1000) {
                                                simages.add(ImageManufacture.scaleImageWidthKeep(image, 1000));
                                            } else {
                                                simages.add(image);
                                            }
                                        }
                                        outFile = new File(path + File.separator + "mapSnapSmall.gif");
                                        ImageGifFile.writeImages(simages, outFile, mapController.interval);
                                        imageName = subPath + "/mapSnapSmall.gif";
                                        s.append("</br><div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"width:85%;\" ></div>\n");
                                    }

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
        loadTree(false);
    }

    @Override
    public void loadExamples() {
        EpidemicReport.importNCPs();
//        EpidemicReport.writeNCPs();
    }

    @FXML
    public void fetchAction() {
        EpidemicReportsFetchNPCDataController controller
                = (EpidemicReportsFetchNPCDataController) openStage(CommonValues.EpidemicReportsFetchNPCDataFxml);
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
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return new TableEpidemicReport().update(currentClearSQL) >= 0;
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

    @FXML
    public void statisticAction() {
        loadTree(true);
    }

    @Override
    public boolean leavingScene() {
        try {
            if (treeTask != null) {
                treeTask.cancel();
                treeTask = null;
            }
            mapController.leavingScene();
        } catch (Exception e) {
        }
        return super.leavingScene();

    }

}
