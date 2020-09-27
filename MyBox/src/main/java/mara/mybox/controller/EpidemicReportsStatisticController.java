package mara.mybox.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import mara.mybox.data.tools.GeographyCodeTools;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-5-20
 * @License Apache License Version 2.0
 */
public class EpidemicReportsStatisticController extends DataTaskController {

    protected EpidemicReportsController parent;
    protected String dataset;
    protected List<Date> times;
    protected long sumCount = 0, increasedCount = 0, failedCount = 0, unchangedCount = 0;
    protected GeographyCode Earch;
    protected ToggleGroup datasetGroup;

    @FXML
    protected FlowPane datasetPane;
    @FXML
    protected CheckBox accumulateCheck, globalIncreasedCheck, provinceIncreasedCheck, cityIncreasedCheck,
            countyIncreasedCheck, townIncreasedCheck, closeWhenCompleteCheck;

    public EpidemicReportsStatisticController() {
        baseTitle = message("EpidemicReportStatistic");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            datasetGroup = new ToggleGroup();

            List<String> datasets = TableEpidemicReport.datasets();
            for (String d : datasets) {
                RadioButton button = new RadioButton(d);
                datasetGroup.getToggles().add(button);
                datasetPane.getChildren().add(button);
            }
            datasetGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (newValue == null) {
                            return;
                        }
                        RadioButton button = (RadioButton) newValue;
                        setDataset(button.getText());
                    });
            if (!datasetGroup.getToggles().isEmpty()) {
                datasetGroup.selectToggle(datasetGroup.getToggles().get(0));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
        baseTitle = message("EpidemicReportStatistic") + " - " + dataset;
        if (getMyStage() != null) {
            getMyStage().setTitle(baseTitle);
        }
    }

    public void start(String dataset) {
        setDataset(dataset);
        startAction();
    }

    @Override
    protected boolean doTask() {
        int count = 1;
        while (count++ < 5) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                return doTask(conn);
            } catch (Exception e) {
                logger.debug(count + "  " + e.toString());
                try {
                    Thread.sleep(500 * count);
                } catch (Exception ex) {
                }
            }
        }
        return false;
    }

    protected boolean doTask(Connection conn) {
        if (conn == null || dataset == null) {
            return false;
        }
        try ( PreparedStatement equalQuery = conn.prepareStatement(TableEpidemicReport.ExistQuery);
                 PreparedStatement insert = conn.prepareStatement(TableEpidemicReport.Insert);
                 PreparedStatement update = conn.prepareStatement(TableEpidemicReport.UpdateAsEPid)) {
            Earch = TableGeographyCode.earth(conn);
            if (Earch == null) {
                updateLogs(message("LoadingPredefinedGeographyCodes"), true);
                GeographyCodeTools.importPredefined(conn);
                Earch = TableGeographyCode.earth(conn);
                if (Earch == null) {
                    updateLogs(message("Failed"), true);
                    return false;
                }
            }
            if (task == null || task.isCancelled()) {
                return false;
            }
            times = TableEpidemicReport.times(conn, dataset);
            if (task == null || task.isCancelled()) {
                return false;
            }

            if (accumulateCheck.isSelected()) {
                sum(conn, equalQuery, insert, update);
            }

            if (task == null || task.isCancelled()) {
                return false;
            }

            if (globalIncreasedCheck.isSelected()) {
                calculateIncreased(conn, update, 3);
                if (task == null || task.isCancelled()) {
                    return false;
                }
                calculateIncreased(conn, update, 2);
                if (task == null || task.isCancelled()) {
                    return false;
                }
                calculateIncreased(conn, update, 1);
            }

            if (task == null || task.isCancelled()) {
                return false;
            }
            if (provinceIncreasedCheck.isSelected()) {
                calculateIncreased(conn, update, 4);
            }
            if (task == null || task.isCancelled()) {
                return false;
            }
            if (cityIncreasedCheck.isSelected()) {
                calculateIncreased(conn, update, 5);
            }
            if (task == null || task.isCancelled()) {
                return false;
            }
            if (countyIncreasedCheck.isSelected()) {
                calculateIncreased(conn, update, 6);
            }
            if (task == null || task.isCancelled()) {
                return false;
            }
            if (townIncreasedCheck.isSelected()) {
                calculateIncreased(conn, update, 7);
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            logger.debug(e.toString());
            return false;
        }
    }

    protected void sum(Connection conn, PreparedStatement equalQuery,
            PreparedStatement insert, PreparedStatement update) {
        try {
            updateLogs(message("AccumulateData") + "  " + message("Country"), true);
            String sql = "SELECT * FROM Geography_Code WHERE level=3 ORDER BY gcid asc";
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (task == null || task.isCancelled()) {
                        return;
                    }
                    GeographyCode code = TableGeographyCode.readResults(results);
                    sum(conn, equalQuery, insert, update, code);
                }
            }

            if (task == null || task.isCancelled()) {
                return;
            }

            updateLogs(message("AccumulateData") + "  " + message("Continent"), true);
            sql = "SELECT * FROM Geography_Code WHERE level=2 ORDER BY gcid asc";
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (task == null || task.isCancelled()) {
                        return;
                    }
                    GeographyCode code = TableGeographyCode.readResults(results);
                    sum(conn, equalQuery, insert, update, code);
                }
            }
            if (task == null || task.isCancelled()) {
                return;
            }
            updateLogs(message("AccumulateData") + "  " + message("Earth"), true);
            sum(conn, equalQuery, insert, update, Earch);
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            logger.debug(e.toString());
        }
    }

    protected void sum(Connection conn, PreparedStatement equalQuery,
            PreparedStatement insert, PreparedStatement update, GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
            updateLogs(message("AccumulateData") + " " + code.getName(), true);
            conn.setAutoCommit(false);
            long locationid = code.getGcid();
            int confirmed = 0, healed = 0, dead = 0, skipped = 0;
            String level = code.getLevelName();
            for (Date d : times) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                String date = DateTools.datetimeToString(d);
                String locationCondition;
                switch (code.getLevel()) {
                    case 1:
                        locationCondition = "level=3";
                        break;
                    case 2:
                        locationCondition = "level=3 and continent=" + locationid;
                        break;
                    case 3:
                        locationCondition = "level=4 and country=" + locationid;
                        break;
                    default:
                        return;
                }
                String sql = " SELECT sum(confirmed), sum(healed), sum(dead) "
                        + " FROM Epidemic_Report JOIN Geography_Code ON Epidemic_Report.locationid=Geography_Code.gcid WHERE"
                        + "  data_set='" + DerbyBase.stringValue(dataset) + "' "
                        + " AND time='" + date + "' "
                        + " AND " + locationCondition;
                confirmed = healed = dead = 0;
                try ( ResultSet sumResults = conn.createStatement().executeQuery(sql)) {
                    if (sumResults.next()) {
                        confirmed = sumResults.getInt(1);
                        healed = sumResults.getInt(2);
                        dead = sumResults.getInt(3);
                    }
                }
                if (confirmed <= 0 && healed <= 0 && dead <= 0) {
                    skipped++;
                    if (skipped % 30 == 0) {
                        updateLogs(message("Skipped") + " " + skipped + " " + date, true);
                    }
                    continue;
                }
                EpidemicReport report = null;
                equalQuery.setString(1, dataset);
                equalQuery.setString(2, date);
                equalQuery.setLong(3, locationid);
                try ( ResultSet results = equalQuery.executeQuery()) {
                    if (results.next()) {
                        report = TableEpidemicReport.read(conn, results);
                    }
                }
                boolean ok;
                if (report == null) {
                    report = EpidemicReport.create()
                            .setDataSet(dataset)
                            .setLocationid(locationid).setTime(d.getTime())
                            .setConfirmed(confirmed).setHealed(healed).setDead(dead)
                            .setSource(4);
                    ok = TableEpidemicReport.insert(insert, report);
                    String info = level + "  " + code.getName() + " " + date + " "
                            + message("Confirmed") + ":" + confirmed + " "
                            + message("Healed") + ":" + healed + " "
                            + message("Dead") + ":" + dead;
                    if (ok) {
                        sumCount++;
                        info = message("Insert") + " " + sumCount + "  " + info;
                        updateLogs(info, true);
                    } else {
                        failedCount++;
                        info = message("Failed") + " " + failedCount + "  " + info;
                        updateLogs(info, true);
                    }
                } else {
                    long rconfirmed = report.getConfirmed();
                    long rhealed = report.getHealed();
                    long rdead = report.getDead();
                    String info = " ";
                    boolean changed = false;
                    if (confirmed > rconfirmed) {
                        report.setConfirmed(confirmed);
                        changed = true;
                        info += message("Confirmed") + ": " + rconfirmed + "->" + confirmed + "  ";
                    }
                    if (healed > rhealed) {
                        report.setHealed(healed);
                        changed = true;
                        info += message("Healed") + ": " + rhealed + "->" + healed + "  ";
                    }
                    if (dead > rdead) {
                        report.setDead(dead);
                        changed = true;
                        info += message("Dead") + ": " + rdead + "->" + dead + "  ";
                    }
                    info = report.getEpid() + " " + level + "  " + code.getName() + " "
                            + date + "  " + info;
                    if (changed) {
                        report.setSource(4);
                        ok = TableEpidemicReport.updateAsEPid(update, report);
                        if (ok) {
                            sumCount++;
                            info = message("Update") + " " + sumCount + "   " + info;
                            updateLogs(info, true);
                        } else {
                            failedCount++;
                            info = message("Failed") + " " + failedCount + "   " + info;
                            updateLogs(info, true);
                        }
                    } else {
                        unchangedCount++;
                        info = message("Unchanged") + " " + info;
                        updateLogs(info, true);
                    }
                }
            }
            conn.commit();

        } catch (Exception e) {
            updateLogs(e.toString(), true);
            logger.debug(e.toString());
        }
    }

    protected void calculateIncreased(Connection conn, PreparedStatement update, int level) {
        try {
            updateLogs(message("SubtractData") + "  " + new GeographyCodeLevel(level).getName(), true);
            EpidemicReport todayReport, yesterdayReport;
            String sql = TableEpidemicReport.StatisticViewSelect + " WHERE "
                    + " data_set='" + DerbyBase.stringValue(dataset) + "' AND "
                    + " level=" + level + " ORDER BY time asc";
            Map<Long, EpidemicReport> yesterdayReports = new HashMap<>();
            conn.setAutoCommit(false);
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (task == null || task.isCancelled()) {
                        return;
                    }
                    todayReport = TableEpidemicReport.statisticViewQuery(conn, results, false);
                    GeographyCode location = todayReport.getLocation();
                    yesterdayReport = yesterdayReports.get(location.getGcid());
                    yesterdayReports.put(location.getGcid(), todayReport);
                    if (yesterdayReport != null) {
                        long IncreasedConfirmed = todayReport.getConfirmed() - yesterdayReport.getConfirmed();
                        long IncreasedHealed = todayReport.getHealed() - yesterdayReport.getHealed();
                        long IncreasedDead = todayReport.getDead() - yesterdayReport.getDead();
                        String info = location.getLevelName() + "  " + location.getName() + " "
                                + DateTools.datetimeToString(todayReport.getTime()) + " "
                                + message("IncreasedConfirmed") + ":" + IncreasedConfirmed + " "
                                + message("IncreasedHealed") + ":" + IncreasedHealed + " "
                                + message("IncreasedDead") + ":" + IncreasedDead;
                        if (IncreasedConfirmed == todayReport.getIncreasedConfirmed()
                                && IncreasedHealed == todayReport.getIncreasedHealed()
                                && IncreasedDead == todayReport.getIncreasedDead()) {
                            info = message("Unchanged") + " " + info;
                            unchangedCount++;
                            if (unchangedCount % 100 == 0) {
                                updateLogs(info, true);
                            }
                            continue;
                        }
                        if (IncreasedConfirmed < 0 || IncreasedHealed < 0 || IncreasedDead < 0) {
                            info = message("InvalidData") + " " + info;
                            updateLogs(info, true);
                        }
                        todayReport.setIncreasedConfirmed(IncreasedConfirmed);
                        todayReport.setIncreasedHealed(IncreasedHealed);
                        todayReport.setIncreasedDead(IncreasedDead);
                        if (TableEpidemicReport.updateAsEPid(update, todayReport)) {
                            increasedCount++;
                            info = message("Update") + " " + increasedCount + "  " + info;
                            if (increasedCount % 100 == 0) {
                                updateLogs(info, true);
                            }
                        } else {
                            failedCount++;
                            info = message("Failed") + " " + failedCount + "  " + info;
                            updateLogs(info, true);
                        }
                    }
                }
            }
            conn.commit();

        } catch (Exception e) {
            logger.debug(e.toString());
            updateLogs(e.toString(), true);
        }
    }

    @Override
    protected void afterSuccess() {
        if (parent != null && parent.getMyStage().isShowing()) {
            if (closeWhenCompleteCheck != null && closeWhenCompleteCheck.isSelected()) {
                closeStage();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        timer = null;
                        parent.refreshAction();
                    });
                }

            }, 500);
        }
    }

}
