package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.tools.EpidemicReportTools;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.ControlStyle;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * @Author Mara
 * @CreateDate 2020-5-3
 * @License Apache License Version 2.0
 */
public class EpidemicReportsExportController extends DataExportController {

    protected List<String> extraFields;

    @FXML
    protected CheckBox SourceCheck, IncreasedConfirmedCheck, IncreasedHealedCheck, IncreasedDeadCheck,
            HealedConfirmedPermillageCheck, DeadConfirmedPermillageCheck,
            ConfirmedPopulationPermillageCheck, DeadPopulationPermillageCheck, HealedPopulationPermillageCheck,
            ConfirmedAreaPermillageCheck, HealedAreaPermillageCheck, DeadAreaPermillageCheck;

    public EpidemicReportsExportController() {
        baseTitle = message("ExportEpidemicReports");
        baseName = "EpidemicReport";
    }

    protected boolean validTopOrder() {
        if (savedCondition == null) {
            return false;
        }
        if (savedCondition.getTop() <= 0) {
            return true;
        }
        String order = savedCondition.getOrder();
        if (order == null || order.isBlank()) {
            return false;
        }
        order = order.trim().toLowerCase();
        return order.startsWith("time ") || order.startsWith("time,");
    }

    protected void checkFields() {
        extraFields = new ArrayList<>();
        if (IncreasedConfirmedCheck.isSelected()) {
            extraFields.add("IncreasedConfirmed");
        }
        if (IncreasedHealedCheck.isSelected()) {
            extraFields.add("IncreasedHealed");
        }
        if (IncreasedDeadCheck.isSelected()) {
            extraFields.add("IncreasedDead");
        }
        if (HealedConfirmedPermillageCheck.isSelected()) {
            extraFields.add("HealedConfirmedPermillage");
        }
        if (DeadConfirmedPermillageCheck.isSelected()) {
            extraFields.add("DeadConfirmedPermillage");
        }
        if (ConfirmedPopulationPermillageCheck.isSelected()) {
            extraFields.add("ConfirmedPopulationPermillage");
        }
        if (DeadPopulationPermillageCheck.isSelected()) {
            extraFields.add("DeadPopulationPermillage");
        }
        if (HealedPopulationPermillageCheck.isSelected()) {
            extraFields.add("HealedPopulationPermillage");
        }
        if (ConfirmedAreaPermillageCheck.isSelected()) {
            extraFields.add("ConfirmedAreaPermillage");
        }
        if (HealedAreaPermillageCheck.isSelected()) {
            extraFields.add("HealedAreaPermillage");
        }
        if (DeadAreaPermillageCheck.isSelected()) {
            extraFields.add("DeadAreaPermillage");
        }
        if (SourceCheck.isSelected()) {
            extraFields.add("Source");
        }
    }

    @Override
    public void start() {
        checkFields();
        if (currentPage) {
            super.start();
            return;
        }
        final int top = savedCondition.getTop();
        if (top <= 0) {
            super.start();
            return;
        }
        if (!validTopOrder()) {
            alertError(message("TimeAsOrderWhenSetTop"));
            ControlStyle.setIcon(startButton, ControlStyle.getIcon("iconStart.png"));
            startButton.applyCss();
            startButton.setUserData(null);
            return;
        }

        synchronized (this) {
            if (task != null && !task.isQuit() ) {
                return;
            }
            dataSize = 0;
            tabPane.getSelectionModel().select(logsTab);
            startTime = new Date().getTime();
            initLogs();
            task = new SingletonTask<Void>() {

                List<EpidemicReport> reports;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                        conn.setReadOnly(true);
                        String where = savedCondition.getWhere();
                        String order = savedCondition.getOrder();
                        int topNumber = savedCondition.getTop();
                        currentSQL = savedCondition.getPrefix() + " "
                                + (where == null || where.isBlank() ? "" : " WHERE " + where)
                                + (order == null || order.isBlank() ? "" : " ORDER BY " + order);
                        updateLogs(currentSQL + "\n" + message("NumberTopDataDaily") + ": " + topNumber);
                        try ( ResultSet results = conn.createStatement().executeQuery(currentSQL)) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String lastDate = null;
                            List<EpidemicReport> timeReports = new ArrayList();
                            reports = new ArrayList();
                            while (results.next()) {
                                if (isCancelled()) {
                                    return false;
                                }
                                EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, false);
                                String date = dateFormat.format(report.getTime());
                                long locationid = report.getLocationid();
                                boolean existed = false;
                                if (lastDate == null || !date.equals(lastDate)) {
                                    if (timeReports.size() > 0) {
                                        updateLogs(MessageFormat.format(message("ReadTopDateData"), timeReports.size(), lastDate));
                                        reports.addAll(timeReports);
                                    }
                                    timeReports = new ArrayList();
                                    lastDate = date;
                                } else {
                                    if (timeReports.size() >= topNumber) {
                                        continue;
                                    }
                                    for (EpidemicReport timeReport : timeReports) {
                                        if (isCancelled()) {
                                            return false;
                                        }
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
                                }
                            }
                            if (timeReports.size() > 0) {
                                updateLogs(MessageFormat.format(message("ReadTopDateData"), timeReports.size(), lastDate));
                                reports.addAll(timeReports);
                            }
                        }

                        dataSize = reports.size();
                        updateLogs(message("DataSize") + ": " + dataSize);
                        if (dataSize == 0) {
                            return false;
                        }

                        writeFiles(reports, titleInput.getText().trim());

                        return true;
                    } catch (Exception e) {
                        if (loading != null) {
                            loading.setInfo(e.toString());
                        }
                        logger.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    browseURI(targetPath.toURI());
                    updateLogs(message("MissionCompleted"));
                    ControlStyle.setIcon(startButton, ControlStyle.getIcon("iconStart.png"));
                    startButton.applyCss();
                    startButton.setUserData(null);
                }

                @Override
                protected void whenCanceled() {
                    updateLogs(message("Canceled"));
                }
            };
            task.setSelf(task);Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void writeFiles(List<EpidemicReport> reports, String title) {
        try {
            String filePrefix = targetPath.getAbsolutePath() + File.separator;
            filePrefix += targetNameInput.getText().trim();

            if (externalCheck != null && externalCheck.isSelected()) {
                File file = new File(filePrefix + ".csv");
                updateLogs(message("Exporting") + " " + file);
                EpidemicReportTools.writeExternalCSV(file, reports, extraFields);
            }
            if (xmlCheck != null && xmlCheck.isSelected()) {
                File file = new File(filePrefix + ".xml");
                updateLogs(message("Exporting") + " " + file);
                EpidemicReportTools.writeXml(file, reports, extraFields);
            }
            if (jsonCheck != null && jsonCheck.isSelected()) {
                File file = new File(filePrefix + ".json");
                updateLogs(message("Exporting") + " " + file);
                EpidemicReportTools.writeJson(file, reports, extraFields);
            }
            if (xlsxCheck != null && xlsxCheck.isSelected()) {
                File file = new File(filePrefix + ".xlsx");
                updateLogs(message("Exporting") + " " + file);
                EpidemicReportTools.writeExcel(file, reports, extraFields);
            }
            if (htmlCheck != null && htmlCheck.isSelected()) {
                File file = new File(filePrefix + ".html");
                updateLogs(message("Exporting") + " " + file);
                EpidemicReportTools.writeHtml(file, title, reports, extraFields);
            }

        } catch (Exception e) {
            if (loading != null) {
                loading.setInfo(e.toString());
            }
            updateLogs(e.toString());
            logger.debug(e.toString());
        }
    }

    @Override
    protected void writeInternalCSVHeader(CSVPrinter printer) {
        EpidemicReportTools.writeInternalCSVHeader(printer);
    }

    @Override
    protected void writeInternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, false);
        EpidemicReportTools.writeInternalCSV(printer, report);
    }

    @Override
    protected void writeExternalCSVHeader(CSVPrinter printer) {
        EpidemicReportTools.writeExternalCSVHeader(printer, extraFields);
    }

    @Override
    protected void writeExternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, true);
        EpidemicReportTools.writeExternalCSV(printer, report, extraFields);
    }

    @Override
    protected void writeXML(Connection conn, FileWriter writer, ResultSet results, String indent) {
        EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, true);
        EpidemicReportTools.writeXml(writer, indent, report, extraFields);
    }

    @Override
    protected String writeJSON(Connection conn, FileWriter writer, ResultSet results, String indent) {
        EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, true);
        return EpidemicReportTools.writeJson(writer, indent, report, extraFields).toString();
    }

    @Override
    protected List<String> columnLabels() {
        return EpidemicReportTools.externalNames(extraFields);
    }

    @Override
    protected void writeExcel(Connection conn, XSSFSheet sheet, ResultSet results, int count) {
        EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, true);
        EpidemicReportTools.writeExcel(sheet, count, report, extraFields);
    }

    @Override
    protected List<String> htmlRow(Connection conn, ResultSet results) {
        EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, true);
        return EpidemicReportTools.values(report, extraFields);
    }

}
