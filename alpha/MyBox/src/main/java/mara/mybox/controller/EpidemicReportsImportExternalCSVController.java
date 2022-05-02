package mara.mybox.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.EpidemicReportTools;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-04-03
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportExternalCSVController extends EpidemicReportsImportController {

    protected boolean predefined = false;

    public EpidemicReportsImportExternalCSVController() {
        baseTitle = Languages.message("ImportEpidemicReportExternalCSVFormat");
    }

    @Override
    protected boolean validHeader(List<String> names) {
        if ((!names.contains("DataSet") && !names.contains(Languages.message("en", "DataSet")) && !names.contains(Languages.message("zh", "DataSet")))
                || (!names.contains("Level") && !names.contains(Languages.message("en", "Level")) && !names.contains(Languages.message("zh", "Level")))
                || (!names.contains("Confirmed") && !names.contains(Languages.message("en", "Confirmed")) && !names.contains(Languages.message("zh", "Confirmed")))) {
            updateLogs(Languages.message("InvalidFormat"), true);
            return false;
        }
        return true;
    }

    @Override
    protected String insertStatement() {
        return TableEpidemicReport.Insert;
    }

    @Override
    protected String updateStatement() {
        return TableEpidemicReport.UpdateAsEPid;
    }

    // Data Set,Time,Confirmed,Healed,Dead,Increased Confirmed,Increased Healed,Increased Dead,Data Source,
    // Level,Continent,Country,Province,City,County,Town,Village,Building,Longitude,Latitude
    // 数据集,时间,确认,治愈,死亡,新增确诊,新增治愈,新增死亡,数据源,级别,洲,国家,省,市,区县,乡镇,村庄,建筑物,经度,纬度
    @Override
    public long importFile(Connection conn, File file) {
        long importCount = 0, insertCount = 0, updateCount = 0, skipCount = 0, failedCount = 0, lineCount = 0;
        File validFile = FileTools.removeBOM(file);
        try ( CSVParser parser = CSVParser.parse(validFile, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
            List<String> names = parser.getHeaderNames();
            if ((!names.contains("DataSet") && !names.contains(Languages.message("en", "DataSet")) && !names.contains(Languages.message("zh", "DataSet")))
                    || (!names.contains("Level") && !names.contains(Languages.message("en", "Level")) && !names.contains(Languages.message("zh", "Level")))
                    || (!names.contains("Confirmed") && !names.contains(Languages.message("en", "Confirmed")) && !names.contains(Languages.message("zh", "Confirmed")))) {
                updateLogs(Languages.message("InvalidFormat"), true);
                return -1;
            }
            String lang = names.contains(Languages.message("zh", "DataSet")) ? "zh" : "en";
            try ( PreparedStatement geoInsert = conn.prepareStatement(TableGeographyCode.Insert);
                     PreparedStatement equalQuery = conn.prepareStatement(TableEpidemicReport.ExistQuery);
                     PreparedStatement update
                    = replaceCheck.isSelected() ? conn.prepareStatement(updateStatement()) : null;
                     PreparedStatement insert = conn.prepareStatement(insertStatement())) {
                conn.setAutoCommit(false);
                if (TableGeographyCode.China(conn) == null) {
                    updateLogs(Languages.message("LoadingPredefinedGeographyCodes"), true);
                    GeographyCodeTools.importPredefined(conn);
                }
                for (CSVRecord record : parser) {
                    ++lineCount;
                    if (task == null || task.isCancelled()) {
                        updateLogs("Canceled", true);
                        conn.commit();
                        return importCount;
                    }
                    equalQuery.setMaxRows(1);
                    Map<String, Object> ret = EpidemicReportTools.readExtenalRecord(conn, geoInsert, lang, names, record);
                    if (ret.get("message") != null) {
                        updateLogs((String) ret.get("message"), true);
                    }
                    if (ret.get("report") == null) {
                        failedCount++;
                        continue;
                    }
                    EpidemicReport report = (EpidemicReport) ret.get("report");
                    if (predefined) {
                        report.setSource((short) 1);
                    }
                    String date = DateTools.datetimeToString(report.getTime()).substring(0, 10) + EpidemicReport.COVID19TIME;
                    equalQuery.setString(1, report.getDataSet());
                    equalQuery.setString(2, date);
                    equalQuery.setLong(3, report.getLocationid());
                    EpidemicReport exist = null;
                    try ( ResultSet results = equalQuery.executeQuery()) {
                        if (results.next()) {
                            exist = TableEpidemicReport.read(conn, results);
                        }
                    }
                    String info = Languages.message("Line") + " " + lineCount + " "
                            + report.getDataSet() + " "
                            + date + " " + report.getLocation().getName() + " "
                            + Languages.message("Confirmed") + ": " + report.getConfirmed() + " "
                            + Languages.message("Healed") + ": " + report.getHealed() + " "
                            + Languages.message("Dead") + ": " + report.getDead();

                    if (exist != null) {
                        if (update == null) {
                            skipCount++;
                            if (verboseCheck.isSelected() || (skipCount % 100 == 0)) {
                                updateLogs(Languages.message("Insert") + " " + insertCount
                                        + " " + Languages.message("Update") + " " + updateCount
                                        + " " + Languages.message("Skipped") + ":" + skipCount + " " + info,
                                        true);
                            }
                            continue;
                        }
                        report.setEpid(exist.getEpid());
                        if (TableEpidemicReport.updateAsEPid(update, report)) {
                            updateCount++;
                            importCount++;
                            if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                updateLogs(Languages.message("Update") + ": " + info, true);
                            }
                        } else {
                            updateLogs(Languages.message("Update") + ": " + Languages.message("Failed") + "  " + info, true);
                            failedCount++;
                        }
                    } else {
                        if (TableEpidemicReport.insert(insert, report)) {
                            insertCount++;
                            importCount++;
                            if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                updateLogs(Languages.message("Insert") + ": " + info, true);
                            }
                        } else {
                            updateLogs(Languages.message("Insert") + ": " + Languages.message("Failed") + "  " + info, true);
                            failedCount++;
                        }
                    }
                    if (importCount % DerbyBase.BatchSize == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            updateLogs(e.toString(), true);
        }
        updateLogs(Languages.message("Imported") + ":" + importCount + "  " + file + "\n"
                + Languages.message("Insert") + ":" + insertCount + " "
                + Languages.message("Update") + ":" + updateCount + " "
                + Languages.message("FailedCount") + ":" + failedCount + " "
                + Languages.message("Skipped") + ":" + skipCount, true);
        return importCount;
    }

}
