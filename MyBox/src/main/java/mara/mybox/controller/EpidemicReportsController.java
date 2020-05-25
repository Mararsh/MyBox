package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.QueryCondition;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlStage.openScene;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.fxml.TableMessageCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReportsController extends DataAnalysisController<EpidemicReport> {

    protected String dataFetch;
    protected LoadingController loading;
    protected double maxValue;
    protected List<String> datasets, dataTimes, orderNames;
    protected Map<String, List<EpidemicReport>> timesReports;
    protected Map<String, List<EpidemicReport>> locationsReports;
    protected List<GeographyCode> dataLocations;

    @FXML
    protected EpidemicReportsSourceController sourceController;
    @FXML
    protected TimeTreeController timeController;
    @FXML
    protected EpidemicReportsChartController chartController;
    @FXML
    protected EpidemicReportsSettingsController settingsController;
    @FXML
    protected TabPane conditionTabsPane;
    @FXML
    protected Tab conditionOrderTab;
    @FXML
    protected Tab chartsTab;
    @FXML
    protected Button chinaButton, globalButton,
            statisticButton, dataExportChartsButton, fillButton;
    @FXML
    protected ComboBox<String> chartMaxSelector;
    @FXML
    protected TableColumn<EpidemicReport, String> datasetColumn, locationColumn, sourceColumn;
    @FXML
    protected TableColumn<EpidemicReport, Double> longitudeColumn, latitudeColumn;
    @FXML
    protected TableColumn<EpidemicReport, Date> timeColumn;
    @FXML
    protected TableColumn<EpidemicReport, Long> confirmedColumn, headledColumn, deadColumn,
            increasedConfirmedColumn, increasedHeadledColumn, increasedDeadColumn;
    @FXML
    protected TableColumn<EpidemicReport, Double> healedConfirmedPermillageColumn, deadConfirmedPermillageColumn,
            confirmedPopulationPermillageColumn, deadPopulationPermillageColumn, healedPopulationPermillageColumn,
            confirmedAreaPermillageColumn, deadAreaPermillageColumn, healedAreaPermillageColumn;
    @FXML
    protected ListView orderByList;
    @FXML
    protected Label timeOrderLabel;

//    orderByList
    public EpidemicReportsController() {
        baseTitle = message("EpidemicReport");
        TipsLabelKey = "EpidemicReportTips";

        baseName = "EpidemicReport";
        dataName = "Epidemic_Report";

        prefixEditable = false;
        supportTop = true;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            datasets = new ArrayList<>();
            dataTimes = new ArrayList<>();
            timesReports = new HashMap<>();
            dataLocations = new ArrayList<>();
            locationsReports = new HashMap<>();
            orderNames = new ArrayList<>();

            sourceController.setUserController(this);
            geoController.setUserController(this);
            timeController.setUserController(this);
            chartController.setReportsController(this);
            chartController.setSettingsController(settingsController);
            settingsController.setReportsController(this);
            settingsController.setChartController(chartController);

            initOptions();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initSQL() {
        queryPrefix = TableEpidemicReport.StatisticViewSelect;
        sizePrefix = TableEpidemicReport.SizeSelectPrefix;
        clearPrefix = TableEpidemicReport.ClearPrefix;
    }

    @Override
    protected void initColumns() {
        try {
            datasetColumn.setCellValueFactory(new PropertyValueFactory<>("dataSet"));
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationFullName"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            timeColumn.setCellFactory(new TableDateCell());
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceName"));
            sourceColumn.setCellFactory(new TableMessageCell());
            confirmedColumn.setCellValueFactory(new PropertyValueFactory<>("confirmed"));
            headledColumn.setCellValueFactory(new PropertyValueFactory<>("healed"));
            deadColumn.setCellValueFactory(new PropertyValueFactory<>("dead"));
            increasedConfirmedColumn.setCellValueFactory(new PropertyValueFactory<>("increasedConfirmed"));
            increasedHeadledColumn.setCellValueFactory(new PropertyValueFactory<>("increasedHealed"));
            increasedDeadColumn.setCellValueFactory(new PropertyValueFactory<>("increasedDead"));
            healedConfirmedPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("healedConfirmedPermillage"));
            deadConfirmedPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("deadConfirmedPermillage"));
            confirmedPopulationPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("confirmedPopulationPermillage"));
            deadPopulationPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("deadPopulationPermillage"));
            healedPopulationPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("healedPopulationPermillage"));
            confirmedAreaPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("confirmedAreaPermillage"));
            deadAreaPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("deadAreaPermillage"));
            healedAreaPermillageColumn.setCellValueFactory(new PropertyValueFactory<>("healedAreaPermillage"));

            tableView.setRowFactory((TableView<EpidemicReport> param) -> {
                return new SourceRow();
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected class SourceRow extends TableRow<EpidemicReport> {

        @Override
        protected void updateItem(EpidemicReport item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setTextFill(null);
                return;
            }
            if (this.isSelected()) {
                setStyle("-fx-background-color:  #0096C9; -fx-text-background-color: white");
            } else if (settingsController != null) {
                switch (item.getSource()) {
                    case 1:
                        setStyle("-fx-background-color: " + settingsController.getPredefinedColor());
                        break;
                    case 2:
                        setStyle("-fx-background-color: " + settingsController.getInputtedColor());
                        break;
                    case 3:
                        setStyle("-fx-background-color: " + settingsController.getFilledColor());
                        break;
                    case 4:
                        setStyle("-fx-background-color: " + settingsController.getStatisticColor());
                        break;
                    default:
                        setStyle(null);
                }
            }
        }
    };

    protected void initOptions() {
        try {
            topNumber = 10;
            chartMaxSelector.getItems().addAll(Arrays.asList(
                    "10", message("EpidemicReportsTopUnlimit"),
                    "20", "30", "50", "8", "15", "25", "60", "5", "100", "200", "360"
            ));
            chartMaxSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    if (message("EpidemicReportsTopUnlimit").equals(newValue)) {
                        topNumber = -1;
                        FxmlControl.setEditorNormal(chartMaxSelector);
                        AppVariables.setUserConfigValue("EpidemicReportMaxChart", newValue);
                        adjustOrderList();
                        return;
                    }
                    int v = Integer.valueOf(chartMaxSelector.getValue());
                    topNumber = v;
                    AppVariables.setUserConfigValue("EpidemicReportMaxChart", topNumber + "");
                    FxmlControl.setEditorNormal(chartMaxSelector);
                    adjustOrderList();
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            });

            orderByList.getItems().clear();
            orderByList.getItems().addAll(Arrays.asList(
                    message("ConfirmedDescending"), message("ConfirmedAscending"),
                    message("HealedDescending"), message("HealedAscending"),
                    message("DeadDescending"), message("DeadAscending"),
                    message("IncreasedConfirmedDescending"), message("IncreasedConfirmedAscending"),
                    message("IncreasedHealedDescending"), message("IncreasedHealedAscending"),
                    message("IncreasedDeadDescending"), message("IncreasedDeadAscending"),
                    message("HealedConfirmedPermillageDescending"), message("HealedConfirmedPermillageDescending"),
                    message("DeadConfirmedPermillageDescending"), message("DeadConfirmedPermillageAscending"),
                    message("ConfirmedPopulationPermillageDescending"), message("ConfirmedPopulationPermillageAscending"),
                    message("DeadPopulationPermillageDescending"), message("DeadPopulationPermillageAscending"),
                    message("HealedPopulationPermillageDescending"), message("HealedPopulationPermillageAscending"),
                    message("ConfirmedAreaPermillageDescending"), message("ConfirmedAreaPermillageAscending"),
                    message("HealedAreaPermillageDescending"), message("HealedAreaPermillageAscending"),
                    message("DeadAreaPermillageDescending"), message("DeadAreaPermillageAscending")
            ));
            orderByList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            orderByList.getSelectionModel().select(message("ConfirmedDescending"));

            isSettingValues = true;
            chartMaxSelector.getSelectionModel().select(AppVariables.getUserConfigValue("EpidemicReportMaxChart", "10"));
            isSettingValues = false;
            adjustOrderList();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void adjustOrderList() {
        if (topNumber > 0) {
            timeOrderLabel.setText(message("TimeDescending"));
            orderByList.getItems().removeAll(message("TimeDescending"), message("TimeAscending"));
            orderByList.getSelectionModel().select(message("ConfirmedDescending"));
        } else {
            timeOrderLabel.setText("");
            if (!orderByList.getItems().contains(message("TimeDescending"))) {
                orderByList.getItems().add(0, message("TimeAscending"));
                orderByList.getItems().add(0, message("TimeDescending"));
            }
            orderByList.getSelectionModel().select(message("TimeDescending"));
            orderByList.getSelectionModel().select(message("ConfirmedDescending"));
        }
    }

    @Override
    protected DerbyBase dataTable() {
        return new TableEpidemicReport();
    }

    @Override
    protected String tableDefinition() {
        return DerbyBase.tableDefinition("Epidemic_Report_Statistic_View");
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            settingsController.afterSceneLoaded();
            chartController.initSplitPanes();
            chartController.controlRightPane();

            setButtons();
            FxmlControl.setTooltip(chinaButton, message("ChineseProvincesEpidemicReports"));
            FxmlControl.setTooltip(globalButton, message("GlobalEpidemicReports"));

            tabsPane.getTabs().clear();
            tabsPane.getTabs().add(infoTab);
            loadTrees(false);

            String backFile = AppVariables.getSystemConfigValue("EpidemicReport621Exported", "");
            if (!backFile.isBlank()) {
                browseURI(new File(backFile).getParentFile().toURI());
                alertInformation(message("DataExportedComments") + "\n\n" + backFile);
                AppVariables.deleteSystemConfigValue("EpidemicReport621Exported");
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void loadTrees(boolean load) {
        sourceController.clearTree();
        timeController.clearTree();
        chartController.clearChart();
        if (load) {
            tableData.clear();
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<String> datasets;
                private List<Date> times;

                @Override
                protected boolean handle() {
                    int count = 1;
                    while (count++ < 5) {
                        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                            conn.setReadOnly(true);
                            datasets = TableEpidemicReport.datasets(conn);
                            times = TableEpidemicReport.times(conn);
                            return true;
                        } catch (Exception e) {
                            logger.debug(count + "  " + e.toString());
                            try {
                                Thread.sleep(1000 * count);
                            } catch (Exception ex) {
                            }
                        }
                    }
                    return false;
                }

                @Override
                protected void whenSucceeded() {
                    sourceController.loadTree(datasets);
                    timeController.loadTree(times);
                    if (datasets == null || datasets.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setContentText(AppVariables.message("ImportEpidemicReportJHUPredefined") + " ?");
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        ButtonType buttonYes = new ButtonType(AppVariables.message("Yes"));
                        ButtonType buttonNo = new ButtonType(AppVariables.message("No"));
                        alert.getButtonTypes().setAll(buttonYes, buttonNo);
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonYes) {
                            predefined();
                        }
                    } else if (load) {
                        queryData();
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected String checkWhere() {
        geoController.check();
        timeController.check();
        sourceController.check();
        String sourceConditions = sourceController.getFinalConditions();
        String geoConditions = geoController.getFinalConditions();
        String timeConditions = timeController.getFinalConditions();
        if (sourceConditions == null) {
            popError(message("MissDataset") + "\n" + message("SetConditionsComments"));
            return null;
        }
        if (geoConditions == null) {
            popError(message("MissLocation") + "\n" + message("SetConditionsComments"));
            return null;
        }
        if (timeConditions == null) {
            popError(message("MissTime") + "\n" + message("SetConditionsComments"));
            return null;
        }
        sourceConditions = sourceConditions.trim();
        geoConditions = geoConditions.trim();
        timeConditions = timeConditions.trim();
        String condition;
        if (sourceConditions.isBlank()) {
            if (geoConditions.isBlank()) {
                condition = timeConditions.isBlank() ? "" : timeConditions;
            } else {
                condition = timeConditions.isBlank() ? geoConditions
                        : "( " + geoConditions + ") AND (" + timeConditions + " )";
            }
        } else {
            if (geoConditions.isBlank()) {
                condition = timeConditions.isBlank() ? sourceConditions
                        : "( " + sourceConditions + ") AND (" + timeConditions + " )";
            } else {
                condition = "( " + sourceConditions + ") AND (" + geoConditions + " )"
                        + (timeConditions.isBlank() ? "" : " AND (" + timeConditions + " )");
            }
        }
        return condition;
    }

    @Override
    protected String checkTitle() {
        return sourceController.getFinalTitle() + "\n"
                + geoController.getFinalTitle() + "\n"
                + timeController.getFinalTitle();
    }

    @Override
    protected void checkOrderBy() {
        try {
            if (topNumber > 0) {
                queryOrder = "time DESC";
                orderTitle = "\"" + message("TimeDescending") + "\"";
            } else {
                queryOrder = "";
                orderTitle = "";
            }
            List<String> langsList = orderByList.getSelectionModel().getSelectedItems();
            for (String name : langsList) {
                if (message("TimeDescending").equals(name)) {
                    if (topNumber <= 0) {
                        queryOrder = queryOrder.isBlank() ? "time DESC" : queryOrder + ", time DESC";
                    }

                } else if (message("TimeAscending").equals(name)) {
                    if (topNumber <= 0) {
                        queryOrder = queryOrder.isBlank() ? "time ASC" : queryOrder + ", time ASC";
                    }

                } else if (message("ConfirmedDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "confirmed DESC" : queryOrder + ", confirmed DESC";

                } else if (message("ConfirmedAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "confirmed ASC" : queryOrder + ", confirmed ASC";

                } else if (message("HealedDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed DESC" : queryOrder + ", healed DESC";

                } else if (message("HealedAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed ASC" : queryOrder + ", healed ASC";

                } else if (message("DeadDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead DESC" : queryOrder + ", dead DESC";

                } else if (message("DeadAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead ASC" : queryOrder + ", dead ASC";

                } else if (message("IncreasedConfirmedDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "increased_confirmed DESC" : queryOrder + ", increased_confirmed DESC";

                } else if (message("IncreasedConfirmedAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "increased_confirmed ASC" : queryOrder + ", increased_confirmed ASC";

                } else if (message("IncreasedHealedDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "increased_healed DESC" : queryOrder + ", increased_healed DESC";

                } else if (message("IncreasedHealedAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "increased_healed ASC" : queryOrder + ", increased_healed ASC";

                } else if (message("IncreasedDeadDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "increased_dead DESC" : queryOrder + ", increased_dead DESC";

                } else if (message("IncreasedDeadAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "increased_dead ASC" : queryOrder + ", increased_dead ASC";

                } else if (message("HealedConfirmedPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed_confirmed_permillage DESC" : queryOrder + ", healed_confirmed_permillage DESC";

                } else if (message("HealedConfirmedPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed_confirmed_permillage ASC" : queryOrder + ", healed_confirmed_permillage ASC";

                } else if (message("DeadConfirmedPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead_confirmed_permillage DESC" : queryOrder + ", dead_confirmed_permillage DESC";

                } else if (message("DeadConfirmedPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead_confirmed_permillage ASC" : queryOrder + ", dead_confirmed_permillage ASC";

                } else if (message("ConfirmedPopulationPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "confirmed_population_permillage DESC" : queryOrder + ", confirmed_population_permillage DESC";

                } else if (message("ConfirmedPopulationPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "confirmed_population_permillage ASC" : queryOrder + ", confirmed_population_permillage ASC";

                } else if (message("HealedPopulationPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed_population_permillage DESC" : queryOrder + ", healed_population_permillage DESC";

                } else if (message("HealedPopulationPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed_population_permillage ASC" : queryOrder + ", healed_population_permillage ASC";

                } else if (message("DeadPopulationPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead_population_permillage DESC" : queryOrder + ", dead_population_permillage DESC";

                } else if (message("DeadPopulationPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead_population_permillage ASC" : queryOrder + ", dead_population_permillage ASC";

                } else if (message("ConfirmedAreaPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "confirmed_area_permillage DESC" : queryOrder + ", confirmed_area_permillage DESC";

                } else if (message("ConfirmedAreaPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "confirmed_area_permillage ASC" : queryOrder + ", confirmed_area_permillage ASC";

                } else if (message("HealedAreaPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed_area_permillage DESC" : queryOrder + ", healed_area_permillage DESC";

                } else if (message("HealedAreaPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "healed_area_permillage ASC" : queryOrder + ", healed_area_permillage ASC";

                } else if (message("DeadAreaPermillageDescending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead_area_permillage DESC" : queryOrder + ", dead_area_permillage DESC";

                } else if (message("DeadAreaPermillageAscending").equals(name)) {
                    queryOrder = queryOrder.isBlank() ? "dead_area_permillage ASC" : queryOrder + ", dead_area_permillage ASC";

                } else {
                    continue;
                }
                orderTitle = orderTitle.isBlank() ? "\"" + name + "\"" : orderTitle + " \"" + name + "\"";

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkOrderValues() {
        orderNames.clear();
        if (topNumber <= 0) {
            return;
        }
        String order = queryCondition.getOrder();
        if (order == null || order.isBlank()) {
            popError(message("OrderValuesShouldSet"));
            queryCondition = null;
            return;
        }
        order = order.trim().toLowerCase();
        if (!order.startsWith("time desc")) {
            queryCondition.setOrder("time DESC, " + queryCondition.getOrder());
            order = "time desc," + order;
        }
        String[] names = order.replaceAll("desc", "").replaceAll("asc", "").split(",");
        for (String name : names) {
            String n = name.trim();
            if (!orderNames.contains(n)) {
                orderNames.add(n);
            }
        }
        if (orderNames.isEmpty()
                || (orderNames.size() == 1 && orderNames.contains("time"))) {
            popError(message("OrderValuesShouldSet"));
            queryCondition = null;
            orderNames.clear();
        }
    }

    protected boolean validTopOrder(QueryCondition condition) {
        if (condition == null) {
            return false;
        }
        if (condition.getTop() <= 0) {
            return true;
        }
        String order = condition.getOrder();
        if (order == null || order.isBlank()) {
            return false;
        }
        order = order.trim().toLowerCase();
        if (!order.startsWith("time desc")) {
            return false;
        }
        String[] names = order.replaceAll("desc", "").replaceAll("asc", "").split(",");
        return names.length >= 2;
    }

    @Override
    public boolean preLoadingTableData() {
        Tab currentTab = tabsPane.getSelectionModel().getSelectedItem();
        tabsPane.getTabs().clear();
        tabsPane.getTabs().add(infoTab);
        if (queryCondition == null) {
            return false;
        }
        checkOrderValues();
        if (!validTopOrder(queryCondition)) {
            alertError(message("TimeAsOrderWhenSetTop"));
            return false;
        }
        tabsPane.getTabs().clear();
        topNumber = queryCondition.getTop();
        if (topNumber > 0) {
            tabsPane.getTabs().addAll(chartsTab, dataTab, infoTab, settingsTab);
            settingsController.adjustTabs(true);
        } else {
            tabsPane.getTabs().addAll(dataTab, infoTab, settingsTab);
            settingsController.adjustTabs(false);
        }
        if (tabsPane.getTabs().contains(currentTab)) {
            tabsPane.getSelectionModel().select(currentTab);
        }
        return super.preLoadingTableData();
    }

    @Override
    public int readDataSize() {
        if (topNumber > 0) {
            readTopData();
            return totalSize;
        } else {
            return TableEpidemicReport.size(sizeQuerySQL);
        }
    }

    @Override
    public List<EpidemicReport> readPageData() {
        setPageSQL();
        if (topNumber <= 0) {
            return TableEpidemicReport.dataQuery(pageQuerySQL, true);
        } else {
            return readTopPageData();
        }
    }

    protected boolean readTopData() {
        datasets.clear();
        dataTimes.clear();
        dataLocations.clear();
        timesReports.clear();
        locationsReports.clear();
        totalSize = 0;
        maxValue = 0;
        if (topNumber <= 0 || !dataQuerySQL.contains("time DESC")) {
            return false;
        }
        dataQueryString = dataQuerySQL + "</br>" + message("NumberTopDataDaily") + ": " + topNumber;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            if (loading != null) {
                loading.setInfo(message("LoadingChartData") + "\n" + dataQuerySQL);
            }
            List<Long> validLocationids = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try ( ResultSet results = conn.createStatement().executeQuery(dataQuerySQL)) {
                String lastDate = null;
                List<EpidemicReport> timeReports = new ArrayList();
                String valueName = orderNames.get(1);
                while (results.next()) {
                    EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, false);
                    String date = dateFormat.format(report.getTime());
                    long locationid = report.getLocationid();
                    boolean existed = false;
                    if (lastDate == null || !date.equals(lastDate)) {
                        if (timeReports.size() > 0 && loading != null) {
                            loading.setInfo(MessageFormat.format(
                                    message("ReadTopDateData"), timeReports.size(), lastDate));
                        }
                        dataTimes.add(date);
                        timeReports = new ArrayList();
                        timesReports.put(date, timeReports);
                        lastDate = date;
                    } else {
                        if (timeReports.size() >= topNumber) {
                            continue;
                        }
                        for (EpidemicReport timeReport : timeReports) {
                            if (timeReport.getDataSet().equals(report.getDataSet())
                                    && timeReport.getLocationid() == locationid) {
                                existed = true;
                                break;
                            }
                        }
                    }
                    if (!existed) {
                        GeographyCode location = TableGeographyCode.readCode(conn, locationid, true);
                        report.setLocation(location);
                        timeReports.add(report);
                        totalSize++;
                        Number n = report.value(valueName);
                        if (n != null) {
                            double value = n.doubleValue();
                            if (value > maxValue) {
                                maxValue = value;
                            }
                        }
                        if (!validLocationids.contains(locationid)) {
                            validLocationids.add(locationid);
                            dataLocations.add(location);
                        }
                        String dataset = report.getDataSet();
                        if (!datasets.contains(dataset)) {
                            datasets.add(dataset);
                        }
                    }
                }
            }

            locationsReports.clear();
            try ( PreparedStatement statement = conn.prepareStatement(TableEpidemicReport.LocationidQuery)) {
                for (GeographyCode location : dataLocations) {
                    statement.setLong(1, location.getGcid());
                    try ( ResultSet results = statement.executeQuery()) {
                        while (results.next()) {
                            EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, false);
                            if (report != null && datasets.contains(report.getDataSet())) {
                                String date = dateFormat.format(report.getTime());
                                List<EpidemicReport> dateLocationReports = locationsReports.get(date);
                                if (dateLocationReports == null) {
                                    dateLocationReports = new ArrayList<>();
                                    locationsReports.put(date, dateLocationReports);
                                }
                                report.setLocation(location);
                                report.setLocationFullName(location.getFullName());
                                dateLocationReports.add(report);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug(e.toString());
            }

            if (loading != null) {
                loading.setInfo(message("DateNumber") + ": " + dataTimes.size()
                        + " " + message("TotalSize") + ": " + totalSize);
            }
            Platform.runLater(() -> {
                chartController.loadCharts();
            });
            return true;

        } catch (Exception e) {
            if (loading != null) {
                loading.setInfo(e.toString());
            }
            logger.debug(e.toString());
            return false;
        }

    }

    public List<EpidemicReport> readTopPageData() {
        if (dataTimes == null || dataTimes.isEmpty()
                || timesReports == null || timesReports.isEmpty()) {
            return null;
        }
        pageQueryString = pageQueryString + "</br>" + message("NumberTopDataDaily") + ": " + topNumber;
        List<EpidemicReport> data = new ArrayList();
        int start = 0;
        int targetStart = currentPageStart;
        int targetEnd = currentPageStart + currentPageSize;
        for (String date : dataTimes) {
            List<EpidemicReport> reports = timesReports.get(date);
            int end = start + reports.size();
            if (end <= targetStart) {
                start = end;
                continue;
            }
            if (start >= targetEnd) {
                break;
            }
            int readStart = Math.max(start, targetStart) - start;
            int readEnd = Math.min(end, targetEnd) - start;
            if (readStart < readEnd) {
                data.addAll(reports.subList(readStart, readEnd));
            }
            start = end;
        }
        return data;
    }

    @Override
    protected String loadMoreInfo() {
        if (queryCondition != null && queryCondition.getTop() > 0) {
            String info = "<b>" + message("DateNumber") + ": </b>" + dataTimes.size() + "</br>";
            info += "<b>" + message("LocationsNumber") + ": </b>" + dataLocations.size() + "</br>";
            info += "<b>" + message("DataNumber") + ": </b>" + totalSize + "</br></br>";
            return info;
        } else {
            return "";
        }
    }

    //  Call this when data are changed and need reload all
    @FXML
    @Override
    public void refreshAction() {
        loadTrees(true);
    }

    @FXML
    @Override
    public void editAction() {
        EpidemicReport selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            addAction();
            return;
        }
        try {
            EpidemicReportEditController controller
                    = (EpidemicReportEditController) openScene(null, CommonValues.EpidemicReportEditFxml);
            controller.reportsController = this;
            controller.loadReport(selected);
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
        return TableEpidemicReport.deleteData(selected);
    }

    public void setSelectedData(EpidemicReport.SourceType sourceType) {
        List<EpidemicReport> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        final List<EpidemicReport> reports = new ArrayList();
        reports.addAll(selected);
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    int source = EpidemicReport.source(sourceType);
                    for (EpidemicReport report : reports) {
                        report.setSource(source);
                    }
                    TableEpidemicReport.update(reports);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
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
    protected DataExportController dataExporter() {
        return (EpidemicReportsExportController) openStage(CommonValues.EpidemicReportsExportFxml);
    }

    @FXML
    @Override
    protected void popSetMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("SetAsInputtedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.InputtedData);
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetAsPredefinedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.PredefinedData);
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetAsStatisticData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.StatisticData);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SetAsFilledData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.FilledData);
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void ChineseProvincesReport() {
        try {
            EpidemicReportsEditController controller
                    = (EpidemicReportsEditController) openScene(null, CommonValues.EpidemicReportsEditFxml);
            controller.load(this, true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void globalReport() {
        try {
            EpidemicReportsEditController controller
                    = (EpidemicReportsEditController) openScene(null, CommonValues.EpidemicReportsEditFxml);
            controller.load(this, false);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void addAction() {
        try {
            EpidemicReportEditController controller
                    = (EpidemicReportEditController) openScene(null, CommonValues.EpidemicReportEditFxml);
            controller.reportsController = this;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadCharts() {

    }

    @Override
    protected boolean checkClearCondition() {
        if (!super.checkClearCondition()) {
            return false;
        }
        String where = clearCondition.getWhere() == null || clearCondition.getWhere().isBlank()
                ? "source<>1" : " ( " + clearCondition.getWhere() + " ) AND source<>1";
        clearCondition.setWhere(where);
        return true;
    }

    @Override
    protected void setClearSQL() {
        clearSQL = clearPrefix + " WHERE " + clearCondition.getWhere() + " )";
    }

    @FXML
    public void statisticAction() {
        EpidemicReportsStatisticController controller
                = (EpidemicReportsStatisticController) openStage(CommonValues.EpidemicReportsStatisticFxml);
        controller.parent = this;
    }

    @FXML
    @Override
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("ImportEpidemicReportExternalCSVFormat"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportExternalCSVController controller
                        = (EpidemicReportsImportExternalCSVController) openStage(CommonValues.EpidemicReportsImportExternalCSVFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ImportEpidemicReportJHUTimes"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportJHUTimesSeriesController controller
                        = (EpidemicReportsImportJHUTimesSeriesController) openStage(CommonValues.EpidemicReportsImportJHUTimeSeriesFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportEpidemicReportJHUDaily"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportJHUDailyController controller
                        = (EpidemicReportsImportJHUDailyController) openStage(CommonValues.EpidemicReportsImportJHUDailyFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ImportEpidemicReportBaidu"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportBaiduController controller
                        = (EpidemicReportsImportBaiduController) openStage(CommonValues.EpidemicReportsImportBaiduFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportEpidemicReportTecent"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportTecentController controller
                        = (EpidemicReportsImportTecentController) openStage(CommonValues.EpidemicReportsImportTecentFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ImportEpidemicReportJHUPredefined"));
            menu.setOnAction((ActionEvent event) -> {
                predefined();
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void predefined() {
        EpidemicReportsImportExternalCSVController controller
                = (EpidemicReportsImportExternalCSVController) openStage(CommonValues.EpidemicReportsImportExternalCSVFxml);
        controller.parent = this;
        File file = FxmlControl.getInternalFile("/data/db/Epidemic_Report_JHU.csv",
                "data", "Epidemic_Report_JHU.csv", false);
        controller.predefined = true;
        controller.startFile(file, true);
    }

    @FXML
    public void upAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(orderByList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) orderByList.getItems().get(index);
            orderByList.getItems().set(index, orderByList.getItems().get(index - 1));
            orderByList.getItems().set(index - 1, lang);
            newselected.add(index - 1);
        }
        orderByList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            orderByList.getSelectionModel().select(index);
        }
        orderByList.refresh();
    }

    @FXML
    public void downAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(orderByList.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == orderByList.getItems().size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            String lang = (String) orderByList.getItems().get(index);
            orderByList.getItems().set(index, orderByList.getItems().get(index + 1));
            orderByList.getItems().set(index + 1, lang);
            newselected.add(index + 1);
        }
        orderByList.getSelectionModel().clearSelection();
        for (int index : newselected) {
            orderByList.getSelectionModel().select(index);
        }
        orderByList.refresh();

    }

    @FXML
    public void topAction() {
        List<Integer> selectedIndices = new ArrayList<>();
        selectedIndices.addAll(orderByList.getSelectionModel().getSelectedIndices());
        if (selectedIndices.isEmpty()) {
            return;
        }
        List<String> selected = new ArrayList<>();
        selected.addAll(orderByList.getSelectionModel().getSelectedItems());
        int size = selectedIndices.size();
        for (int i = size - 1; i >= 0; --i) {
            int index = selectedIndices.get(i);
            orderByList.getItems().remove(index);
        }
        orderByList.getSelectionModel().clearSelection();
        orderByList.getItems().addAll(0, selected);
        orderByList.getSelectionModel().selectRange(0, size);
        orderByList.refresh();
    }

    @Override
    public boolean leavingScene() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            geoController.leavingScene();
            sourceController.leavingScene();
            timeController.leavingScene();
            chartController.leavingScene();
            settingsController.leavingScene();

        } catch (Exception e) {
        }
        return super.leavingScene();
    }

}
