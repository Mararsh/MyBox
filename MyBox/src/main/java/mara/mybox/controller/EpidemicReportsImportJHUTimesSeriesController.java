package mara.mybox.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-04-15
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportJHUTimesSeriesController extends EpidemicReportsImportController {

    public EpidemicReportsImportJHUTimesSeriesController() {
        baseTitle = AppVariables.message("ImportEpidemicReportJHUTimes");
    }

    @Override
    public void setLink() {
        link.setText("https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_time_series/");
    }

    //Province/State,Country/Region,Lat,Long,1/22/20,1/23/20,...
    @Override
    public long importFile(Connection conn, File file) {
        long importCount = 0, insertCount = 0, updateCount = 0, skipCount = 0, failedCount = 0, lineCount = 0, dataCount = 0;
        EpidemicReport.ValueName valueName;
        try ( CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""));
                 PreparedStatement geoInsert = conn.prepareStatement(TableGeographyCode.Insert);
                 PreparedStatement equalQuery = conn.prepareStatement(TableEpidemicReport.ExistQuery);
                 PreparedStatement update
                = replaceCheck.isSelected() ? conn.prepareStatement(TableEpidemicReport.UpdateAsEPid) : null;
                 PreparedStatement insert = conn.prepareStatement(TableEpidemicReport.Insert)) {
            conn.setAutoCommit(false);
            equalQuery.setMaxRows(1);
            if (TableGeographyCode.China(conn) == null) {
                updateLogs(message("LoadingPredefinedGeographyCodes"), true);
                GeographyCodeTools.importPredefined(conn);
            }
            String filename = file.getAbsolutePath();
            if (filename.contains("_confirmed")) {
                valueName = EpidemicReport.ValueName.Confirmed;
            } else if (filename.contains("_deaths")) {
                valueName = EpidemicReport.ValueName.Dead;
            } else if (filename.contains("_recovered")) {
                valueName = EpidemicReport.ValueName.Healed;
            } else {
                return -1;
            }
            List<String> names = parser.getHeaderNames();
            if (!names.contains("Province/State") || !names.contains("1/22/20")) {
                updateLogs(message("InvalidFormat"), true);
                return -1;
            }
            for (CSVRecord record : parser) {
                lineCount++;
                if (task == null || task.isCancelled()) {
                    conn.commit();
                    updateLogs("Canceled", true);
                    return importCount;
                }
                try {
                    String country = record.get("Country/Region");
                    if (country == null || country.isBlank()) {
                        updateLogs("Failed: not defined country", true);
                        continue;
                    }
                    String province = null;
                    if (country.contains("China")) {
                        country = "China";
                    } else if (country.contains("Taiwan")) {
                        country = "China";
                        province = "Taiwan";
                    } else if (country.contains("Hong Kong")) {
                        country = "China";
                        province = "Hong Kong";
                    } else if (country.contains("Macau")) {
                        country = "China";
                        province = "Macau";
                    }
                    if (province == null) {
                        province = record.get("Province/State");
                    }
                    String levelValue;
                    if (province != null && !province.isBlank()) {
                        levelValue = message("Province");
                    } else {
                        levelValue = message("Country");
                    }
                    double longitude = Double.valueOf(record.get("Long"));
                    double latitude = Double.valueOf(record.get("Lat"));

                    GeographyCodeLevel levelCode = new GeographyCodeLevel(levelValue);
                    int level = levelCode.getLevel();
                    updateLogs(lineCount + "  " + levelCode.getName() + " "
                            + country + " " + (province != null ? province : ""), true);
                    Map<String, Object> ret = GeographyCodeTools.encode(conn, geoInsert,
                            level, longitude, latitude, null, country, province, null,
                            null, null, null, null, null, true, false);
                    if (ret == null) {
                        updateLogs("Failed: can not load/insert grography code.  "
                                + lineCount + "  " + levelCode.getName() + " "
                                + country + " " + (province != null ? province : ""), true);
                        continue;
                    }
                    if (ret.get("mesasge") != null) {
                        updateLogs((String) ret.get("mesasge"), true);
                    }
                    if (ret.get("code") == null) {
                        updateLogs("Failed: " + lineCount + "  " + levelCode.getName() + " "
                                + country + " " + (province != null ? province : ""), true);
                        return importCount;
                    }
                    GeographyCode locationCode = (GeographyCode) ret.get("code");
                    for (int i = 4; i < names.size(); i++) {
                        dataCount++;
                        if (task == null || task.isCancelled()) {
                            conn.commit();
                            updateLogs("Canceled", true);
                            return importCount;
                        }
                        String d = names.get(i);
                        Date date = DateTools.stringToDatetime(d + EpidemicReport.COVID19TIME, "MM/dd/yy hh:mm:ss");
                        if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                            updateLogs(message("Insert") + " " + insertCount + " "
                                    + message("Update") + " " + updateCount + " "
                                    + message("Skipped") + ":" + skipCount + " "
                                    + EpidemicReport.COVID19JHU + ": " + date + " "
                                    + country + " " + (province != null ? province : "") + " "
                                    + message(valueName.name()), true);
                        }
                        int value = Integer.valueOf(record.get(d));
                        if (value <= 0) {
                            skipCount++;
                            if (verboseCheck.isSelected() || (skipCount % 100 == 0)) {
                                updateLogs(message("Skip") + ": " + date + " "
                                        + country + " " + (province != null ? province : "") + " "
                                        + message(valueName.name()), true);
                            }
                            continue;
                        }
                        long locationid = locationCode.getId();
                        String dateSting = DateTools.datetimeToString(date.getTime());
                        equalQuery.setString(1, EpidemicReport.COVID19JHU);
                        equalQuery.setString(2, dateSting);
                        equalQuery.setLong(3, locationid);
                        EpidemicReport report = null;
                        try ( ResultSet results = equalQuery.executeQuery()) {
                            if (results.next()) {
                                report = TableEpidemicReport.read(conn, results);
                            }
                        }
                        boolean existed = report != null;
                        if (report == null) {
                            report = new EpidemicReport();
                            String dataset = EpidemicReport.COVID19JHU;
                            report.setDataSet(dataset);
                            report.setLocationid(locationid);
                            report.setTime(date.getTime());
                        }
                        switch (valueName) {
                            case Confirmed:
                                report.setConfirmed(value);
                                break;
                            case Healed:
                                report.setHealed(value);
                                break;
                            case Dead:
                                report.setDead(value);
                                break;
                            default:
                                continue;
                        }
                        report.setSource((short) 2);
                        String info = message("Line") + " " + lineCount + " "
                                + message("Data") + " " + dataCount + " "
                                + message("Line") + " " + lineCount + " "
                                + message("Data") + " " + dataCount + " "
                                + country + " " + (province != null ? province : "") + " " + locationid + " " + locationCode.getName() + " "
                                + date + " " + message(valueName.name()) + ": " + value;
                        if (existed) {
                            if (update == null) {
                                skipCount++;
                                continue;
                            }
                            if (TableEpidemicReport.updateAsEPid(update, report)) {
                                updateCount++;
                                importCount++;
                                if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                    updateLogs(message("Update") + ": " + info, true);
                                }
                            } else {
                                updateLogs(message("Update") + ": " + message("Failed") + "  " + info, true);
                                failedCount++;
                            }
                        } else {
                            if (TableEpidemicReport.insert(insert, report)) {
                                insertCount++;
                                importCount++;
                                if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                    updateLogs(message("Insert") + ": " + info, true);
                                }
                            } else {
                                updateLogs(message("Insert") + ": " + message("Failed") + "  " + info, true);
                                failedCount++;
                            }
                        }
                        if (importCount % DerbyBase.BatchSize == 0) {
                            conn.commit();
                        }
                    }
                } catch (Exception e) {
                    updateLogs(e.toString(), true);
                }
            }
            conn.commit();
        } catch (Exception e) {
            updateLogs(e.toString(), true);
        }
        updateLogs(message("Imported") + ":" + importCount + "  " + file + "\n"
                + message("Total") + ":" + dataCount + " "
                + message("Insert") + ":" + insertCount + " "
                + message("Update") + ":" + updateCount + " "
                + message("FailedCount") + ":" + failedCount + " "
                + message("Skipped") + ":" + skipCount, true);
        return dataCount;
    }

}
