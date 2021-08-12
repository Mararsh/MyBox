package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
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
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableFactory;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.EpidemicReportTools;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.fxml.WindowTools.openScene;
import mara.mybox.fxml.cell.TableMessageCell;
import mara.mybox.fxml.cell.TableTimeCell;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReportsController extends BaseDataManageController<EpidemicReport> {

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
    protected ControlTimeTree timeController;
    @FXML
    protected EpidemicReportsChartController chartController;
    @FXML
    protected EpidemicReportsColorsController colorsController;
    @FXML
    protected Tab chartsTab, colorsTab;
    @FXML
    protected Button chinaButton, globalButton, statisticButton, dataExportChartsButton, fillButton;
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
    protected Label timeOrderLabel;

    public EpidemicReportsController() {
        baseTitle = Languages.message("EpidemicReport");
        TipsLabelKey = "EpidemicReportTips";

        prefixEditable = false;
        supportTop = true;
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableEpidemicReport(false).readDefinitionFromDB("Epidemic_Report_Statistic_View");
        tableDefinition.setIdColumn("epid");
        tableDefinition.getPrimaryColumns().add("epid");
    }

    @Override
    public void setTableValues() {
        queryPrefix = TableEpidemicReport.StatisticViewSelect;
        sizePrefix = TableEpidemicReport.SizeSelectPrefix;
        clearPrefix = TableEpidemicReport.ClearPrefix;
        String html = tableDefinition.columnsTable() + "</BR><HR>"
                + new TableEpidemicReport().createTableStatement().replaceAll("\n", "</BR>") + "</BR></BR>"
                + new TableGeographyCode().createTableStatement().replaceAll("\n", "</BR>") + "</BR></BR>"
                + TableEpidemicReport.CreateStatisticView.replaceAll("\n", "</BR>");
        tableDefinitionString = HtmlWriteTools.html(tableName, html);
        viewDefinition = tableDefinition;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            datasets = new ArrayList<>();
            dataTimes = new ArrayList<>();
            timesReports = new HashMap<>();
            dataLocations = new ArrayList<>();
            locationsReports = new HashMap<>();
            orderNames = new ArrayList<>();

            sourceController.setParent(this);
            geoController.setParent(this);
            timeController.setParent(this, true);
            chartController.setReportsController(this);
            chartController.setColorsController(colorsController);
            colorsController.setReportsController(this);
            colorsController.setChartController(chartController);

            initOrder();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            datasetColumn.setCellValueFactory(new PropertyValueFactory<>("dataSet"));
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationFullName"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            timeColumn.setCellFactory(new TableTimeCell());
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
            MyBoxLog.error(e.toString());
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
            } else if (colorsController != null) {
                switch (item.getSource()) {
                    case 1:
                        setStyle("-fx-background-color: " + colorsController.predefinedColorSetController.rgb());
                        break;
                    case 2:
                        setStyle("-fx-background-color: " + colorsController.inputtedColorSetController.rgb());
                        break;
                    case 3:
                        setStyle("-fx-background-color: " + colorsController.filledColorSetController.rgb());
                        break;
                    case 4:
                        setStyle("-fx-background-color: " + colorsController.statisticColorSetController.rgb());
                        break;
                    default:
                        setStyle(null);
                }
            }
        }
    };

    @Override
    protected void initOrder() {
        try {
            super.initOrder();
            topNumber = 10;
            chartMaxSelector.getItems().addAll(Arrays.asList("10", Languages.message("EpidemicReportsTopUnlimit"),
                    "20", "30", "50", "8", "15", "25", "60", "5", "100", "200", "360"
            ));
            chartMaxSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    if (Languages.message("EpidemicReportsTopUnlimit").equals(newValue)) {
                        topNumber = -1;
                        ValidationTools.setEditorNormal(chartMaxSelector);
                        UserConfig.setString("EpidemicReportMaxChart", newValue);
                        adjustOrderList();
                        return;
                    }
                    int v = Integer.valueOf(chartMaxSelector.getValue());
                    topNumber = v;
                    UserConfig.setString("EpidemicReportMaxChart", topNumber + "");
                    ValidationTools.setEditorNormal(chartMaxSelector);
                    adjustOrderList();
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            });

            isSettingValues = true;
            chartMaxSelector.getSelectionModel().select(UserConfig.getString("EpidemicReportMaxChart", "10"));
            isSettingValues = false;
            adjustOrderList();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void adjustOrderList() {
        if (topNumber > 0) {
            timeOrderLabel.setText(Languages.message("Time") + " " + Languages.message("Descending"));
            orderByList.getItems().removeAll(Languages.message("Time") + " " + Languages.message("Descending"),
                    Languages.message("Time") + " " + Languages.message("Ascending"));
            orderByList.getSelectionModel().clearSelection();
            orderByList.getSelectionModel().select(Languages.message("Confirmed") + " " + Languages.message("Descending"));
        } else {
            timeOrderLabel.setText("");
            if (!orderByList.getItems().contains(Languages.message("Time") + " " + Languages.message("Descending"))) {
                orderByList.getItems().add(0, Languages.message("Time") + " " + Languages.message("Ascending"));
                orderByList.getItems().add(0, Languages.message("Time") + " " + Languages.message("Descending"));
            }
            orderByList.getSelectionModel().clearSelection();
            orderByList.getSelectionModel().select(Languages.message("Time") + " " + Languages.message("Ascending"));
            orderByList.getSelectionModel().select(Languages.message("Confirmed") + " " + Languages.message("Descending"));
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            if (ControllerTools.mapFirstRun(this)) {
                return;
            }

            super.afterSceneLoaded();
            chartController.initMap(this);

            tabsPane.getTabs().clear();
            tabsPane.getTabs().add(infoTab);
            loadTrees(false);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(chinaButton, Languages.message("ChineseProvincesEpidemicReports"));
            NodeStyleTools.setTooltip(globalButton, Languages.message("GlobalEpidemicReports"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<String> datasets;
                private List<Date> times;

                @Override
                protected boolean handle() {
                    int count = 1;
                    while (count++ < 5) {
                        try ( Connection conn = DerbyBase.getConnection()) {
                            conn.setReadOnly(true);
                            datasets = TableEpidemicReport.datasets(conn);
                            times = TableEpidemicReport.times(conn);
                            return true;
                        } catch (Exception e) {
                            MyBoxLog.debug(count + "  " + e.toString());
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
                    timeController.loadTree("time", times, false);
                    if (datasets == null || datasets.isEmpty()) {
                        if (PopTools.askSure(getBaseTitle(), Languages.message("ImportEpidemicReportJHUPredefined") + " ?")) {
                            predefined();
                        }

                    } else if (load) {
                        queryData();
                    }
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    protected String checkWhere() {
        String sourceConditions = sourceController.check();
        String geoConditions = geoController.check();
        String timeConditions = timeController.check();
        if (sourceConditions == null) {
            popError(Languages.message("MissDataset") + "\n" + Languages.message("SetConditionsComments"));
            return null;
        }
        if (geoConditions == null) {
            popError(Languages.message("MissLocation") + "\n" + Languages.message("SetConditionsComments"));
            return null;
        }
        if (timeConditions == null) {
            popError(Languages.message("MissTime") + "\n" + Languages.message("SetConditionsComments"));
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
            super.checkOrderBy();
            if (topNumber > 0) {
                queryOrder = queryOrder.isBlank() ? "time DESC" : "time DESC, " + queryOrder;
                String t = Languages.message("Time") + " " + Languages.message("Descending");
                orderTitle = orderTitle.isBlank() ? "\"" + t + "\"" : orderTitle + " \"" + t + "\"";
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkOrderValues() {
        orderNames.clear();
        if (topNumber <= 0) {
            return;
        }
        String order = queryCondition.getOrder();
        if (order == null || order.isBlank()) {
            popError(Languages.message("OrderValuesShouldSet"));
            queryCondition = null;
            return;
        }
        order = order.trim().toLowerCase();
        if (!order.startsWith("time desc")) {
            queryCondition.setOrder("time DESC, " + queryCondition.getOrder());
            order = "time desc," + order;
        }
        String[] names = order.replaceAll("desc|asc", "").split(",");
        for (String name : names) {
            String n = name.trim();
            if (!orderNames.contains(n)) {
                orderNames.add(n);
            }
        }
        if (orderNames.isEmpty()
                || (orderNames.size() == 1 && orderNames.contains("time"))) {
            popError(Languages.message("OrderValuesShouldSet"));
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
        String[] names = order.replaceAll("desc|asc", "").split(",");
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
            alertError(Languages.message("TimeAsOrderWhenSetTop"));
            return false;
        }
        tabsPane.getTabs().clear();
        topNumber = queryCondition.getTop();
        if (topNumber > 0) {
            tabsPane.getTabs().addAll(chartsTab, dataTab, infoTab, colorsTab);
            colorsController.adjustTabs(true);
        } else {
            tabsPane.getTabs().addAll(dataTab, infoTab, colorsTab);
            colorsController.adjustTabs(false);
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
            return DerbyBase.size(sizeQuerySQL);
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
        dataQueryString = dataQuerySQL + "</br>" + Languages.message("NumberTopDataDaily") + ": " + topNumber;
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            if (loading != null) {
                loading.setInfo(Languages.message("LoadingChartData") + "\n" + dataQuerySQL);
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
                            loading.setInfo(MessageFormat.format(Languages.message("ReadTopDateData"), timeReports.size(), lastDate));
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
                        Number n = EpidemicReportTools.getNumber(report, valueName);
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
                    statement.setLong(1, location.getId());
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
                MyBoxLog.debug(e.toString());
            }

            if (loading != null) {
                loading.setInfo(Languages.message("DateNumber") + ": " + dataTimes.size()
                        + " " + Languages.message("TotalSize") + ": " + totalSize);
            }
            Platform.runLater(() -> {
                chartController.loadCharts();
            });
            return true;

        } catch (Exception e) {
            if (loading != null) {
                loading.setInfo(e.toString());
            }
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    public List<EpidemicReport> readTopPageData() {
        if (dataTimes == null || dataTimes.isEmpty()
                || timesReports == null || timesReports.isEmpty()) {
            return null;
        }
        pageQueryString = pageQueryString + "</br>" + Languages.message("NumberTopDataDaily") + ": " + topNumber;
        List<EpidemicReport> data = new ArrayList();
        int start = 0;
        int targetStart = currentPageStart - 1;
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
            String info = "<SPAN class=\"boldText\">" + Languages.message("DateNumber") + ": </SPAN>";
            info += "<SPAN class=\"valueText\">" + dataTimes.size() + "</SPAN></br>";
            info += "<SPAN class=\"boldText\">" + Languages.message("LocationsNumber") + ": </SPAN>";
            info += "<SPAN class=\"valueText\">" + dataLocations.size() + "</SPAN></br>";
            info += "<SPAN class=\"boldText\">" + Languages.message("DataNumber") + ": </SPAN>";
            info += "<SPAN class=\"valueText\">" + totalSize + "</SPAN></br></br>";
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
    public void editAction(ActionEvent event) {
        EpidemicReport selected = (EpidemicReport) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            addAction(event);
            return;
        }
        try {
            EpidemicReportEditController controller
                    = (EpidemicReportEditController) openScene(null, Fxmls.EpidemicReportEditFxml);
            controller.reportsController = this;
            controller.loadReport(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void viewAction() {
        EpidemicReport selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        HtmlReadTools.htmlTable(baseTitle, TableFactory.displayData(tableDefinition, selected, null, true));
    }

    @FXML
    public void locationAction() {
        EpidemicReport selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getLocation() == null) {
            return;
        }
        LocationInMapController controller
                = (LocationInMapController) openScene(null, Fxmls.LocationInMapFxml);
        controller.loadCoordinate(null, selected.getLocation().getLongitude(), selected.getLocation().getLatitude());
    }

    @Override
    protected int deleteData(List<EpidemicReport> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        return new TableEpidemicReport().deleteData(data);
    }

    public void setSelectedData(EpidemicReport.SourceType sourceType) {
        List<EpidemicReport> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        final List<EpidemicReport> reports = new ArrayList();
        reports.addAll(selected);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    short source = EpidemicReport.source(sourceType);
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
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
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

            MenuItem menu = new MenuItem(Languages.message("SetAsInputtedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.InputtedData);
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SetAsPredefinedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.PredefinedData);
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SetAsStatisticData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.StatisticData);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SetAsFilledData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(EpidemicReport.SourceType.FilledData);
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void ChineseProvincesReport() {
        try {
            EpidemicReportsEditController controller
                    = (EpidemicReportsEditController) openScene(null, Fxmls.EpidemicReportsEditFxml);
            controller.load(this, true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void globalReport() {
        try {
            EpidemicReportsEditController controller
                    = (EpidemicReportsEditController) openScene(null, Fxmls.EpidemicReportsEditFxml);
            controller.load(this, false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {
        try {
            EpidemicReportEditController controller
                    = (EpidemicReportEditController) openScene(null, Fxmls.EpidemicReportEditFxml);
            controller.reportsController = this;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    @FXML
    public void statisticAction() {
        EpidemicReportsStatisticController controller
                = (EpidemicReportsStatisticController) openStage(Fxmls.EpidemicReportsStatisticFxml);
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

            menu = new MenuItem(Languages.message("ImportEpidemicReportExternalCSVFormat"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportExternalCSVController controller
                        = (EpidemicReportsImportExternalCSVController) openStage(Fxmls.EpidemicReportsImportExternalCSVFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("ImportEpidemicReportJHUTimes"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportJHUTimesSeriesController controller
                        = (EpidemicReportsImportJHUTimesSeriesController) openStage(Fxmls.EpidemicReportsImportJHUTimeSeriesFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("ImportEpidemicReportJHUDaily"));
            menu.setOnAction((ActionEvent event) -> {
                EpidemicReportsImportJHUDailyController controller
                        = (EpidemicReportsImportJHUDailyController) openStage(Fxmls.EpidemicReportsImportJHUDailyFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("ImportEpidemicReportJHUPredefined"));
            menu.setOnAction((ActionEvent event) -> {
                predefined();
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void predefined() {
        EpidemicReportsImportExternalCSVController controller
                = (EpidemicReportsImportExternalCSVController) openStage(Fxmls.EpidemicReportsImportExternalCSVFxml);
        controller.parent = this;
        File file = FxFileTools.getInternalFile("/data/db/Epidemic_Report_JHU_2020924.csv",
                "data", "Epidemic_Report_JHU_2020924.csv");
        controller.predefined = false;
        controller.startFile(file, true, true);
    }

    @Override
    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            geoController.cleanPane();
            sourceController.cleanPane();
            timeController.cleanPane();
            chartController.cleanPane();
            colorsController.cleanPane();

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
