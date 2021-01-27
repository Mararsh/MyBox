package mara.mybox.controller;

import java.io.File;
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
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-04-14
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportJHUDailyController extends EpidemicReportsImportController {

    public EpidemicReportsImportJHUDailyController() {
        baseTitle = AppVariables.message("ImportEpidemicReportJHUDaily");
    }

    @Override
    public void setLink() {
        link.setText("https://github.com/CSSEGISandData/COVID-19/tree/master/csse_covid_19_data/csse_covid_19_daily_reports");
    }

    //Format 1: FIPS,Admin2,Province_State,Country_Region,Last_Update,Lat,Long_,Confirmed,Deaths,Recovered,Active,Combined_Key
    //Format 2: Province/State,Country/Region,Last Update,Confirmed,Deaths,Recovered,Latitude,Longitude
    //Format 3: Province/State,Country/Region,Last Update,Confirmed,Deaths,Recovered
    @Override
    public long importFile(Connection conn, File file) {
        long importCount = 0, insertCount = 0, updateCount = 0, skipCount = 0, failedCount = 0, lineCount = 0;
        File convertedFile = FileTools.removeBOM(file);
        try ( CSVParser parser = CSVParser.parse(convertedFile, FileTools.charset(convertedFile),
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
            List<String> names = parser.getHeaderNames();
            if (!names.contains("Confirmed") || !names.contains("Recovered")) {
                updateLogs(message("InvalidFormat"), true);
                return -1;
            }
            String country, province, city, levelValue;
            for (CSVRecord record : parser) {
                lineCount++;
                if (task == null || task.isCancelled()) {
                    conn.commit();
                    updateLogs("Canceled", true);
                    return importCount;
                }
                try {
                    if (names.contains("Country_Region")) {
                        country = record.get("Country_Region");
                    } else if (names.contains("Country/Region")) {
                        country = record.get("Country/Region");
                    } else {
                        updateLogs("Failed: not defined country", true);
                        country = null;
                    }
                    province = null;
                    if (country != null) {
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
                    }
                    if (province == null) {
                        if (names.contains("Province_State")) {
                            province = record.get("Province_State");
                        } else if (names.contains("Province/State")) {
                            province = record.get("Province/State");
                        }
                    }
                    if (names.contains("Admin2")) {
                        city = record.get("Admin2");
                    } else {
                        city = null;
                    }
                    if (city != null && !city.isBlank()) {
                        levelValue = message("City");
                    } else if (province != null && !province.isBlank()) {
                        levelValue = message("Province");
                    } else if (country != null && !country.isBlank()) {
                        levelValue = message("Country");
                    } else {
                        updateLogs(lineCount + " " + message("InvalidFormat") + " " + message("MissLocation"), true);
                        break;
                    }

                    String time = null;
                    if (names.contains("Last_Update")) {
                        time = record.get("Last_Update");
                    } else if (names.contains("Last Update")) {
                        time = record.get("Last Update");
                    }
                    if (time == null) {
                        updateLogs(lineCount + " " + message("InvalidFormat") + " " + message("MissTime"), true);
                        break;
                    }

                    // 1/22/2020 17:00
                    // 1/23/20 17:00
                    // 2020-02-19T23:23:02
                    // 2020-03-24 23:37:31
                    Date date;
                    if (time.contains("/")) {
                        date = DateTools.stringToDatetime(time, "mm/dd/yy hh:mm");
                        if (date == null) {
                            date = DateTools.stringToDatetime(time, "mm/dd/yyyy hh:mm");
                        }
                    } else {
                        date = DateTools.stringToDatetime(time.replace("T", " "));
                    }
                    if (date == null) {
                        updateLogs(lineCount + " " + message("InvalidFormat") + " " + message("MissTime"), true);
                        break;
                    }
                    // Do not care timeDuration
                    date = DateTools.stringToDatetime(DateTools.datetimeToString(date, "yyyy-MM-dd") + EpidemicReport.COVID19TIME);
                    if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                        updateLogs(message("Insert") + " " + insertCount + " "
                                + message("Update") + " " + updateCount + " "
                                + message("Skipped") + ":" + skipCount + " "
                                + EpidemicReport.COVID19JHU + ": " + date + " "
                                + country + " " + (province != null ? province : ""), true);
                    }
                    double longitude = -200, latitude = -200;
                    if (names.contains("Long_") && record.get("Long_") != null) {
                        longitude = Double.valueOf(record.get("Long_"));
                    } else if (names.contains("Longitude") && record.get("Longitude") != null) {
                        longitude = Double.valueOf(record.get("Longitude"));
                    }
                    if (names.contains("Lat") && record.get("Lat") != null) {
                        latitude = Double.valueOf(record.get("Lat"));
                    } else if (names.contains("Latitude") && record.get("Latitude") != null) {
                        latitude = Double.valueOf(record.get("Latitude"));
                    }
                    int confirmed = 0, deaths = 0, recovered = 0;
                    if (names.contains("Confirmed") && record.get("Confirmed") != null) {
                        confirmed = Integer.valueOf(record.get("Confirmed"));
                    }
                    if (names.contains("Deaths") && record.get("Deaths") != null) {
                        deaths = Integer.valueOf(record.get("Deaths"));
                    }
                    if (names.contains("Recovered") && record.get("Recovered") != null) {
                        recovered = Integer.valueOf(record.get("Recovered"));
                    }
                    if (confirmed <= 0 && deaths <= 0 && recovered <= 0) {
                        skipCount++;
                        continue;
                    }
                    String FIPS = null;
                    if (names.contains("FIPS")) {
                        FIPS = record.get("FIPS");
                    }
                    GeographyCodeLevel levelCode = new GeographyCodeLevel(levelValue);
                    int level = levelCode.getLevel();

                    Map<String, Object> ret = GeographyCodeTools.encode(conn, geoInsert,
                            level, longitude, latitude, null, country, province, city,
                            null, null, null, null, null, true, false);
                    if (ret == null) {
                        return importCount;
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
                    if (FIPS != null && locationCode.getCode5() == null) {
                        locationCode.setCode5(FIPS);
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
                    String info = message("Line") + " " + lineCount + " "
                            + locationid + " "
                            + dateSting + " : " + confirmed + "," + recovered + "," + deaths + " ";
                    if (report == null) {
                        report = new EpidemicReport();
                        String dataset = EpidemicReport.COVID19JHU;
                        report.setDataSet(dataset);
                        report.setLocationid(locationid);
                        report.setTime(date.getTime());
                    } else {
                        info += "  original: " + report.getConfirmed() + "," + report.getHealed() + "," + report.getDead();
                    }
                    report.setConfirmed(confirmed);
                    report.setHealed(recovered);
                    report.setDead(deaths);
                    report.setSource((short) 2);
                    if (existed) {
                        if (update == null) {
                            skipCount++;
                            if (verboseCheck.isSelected() || (skipCount % 100 == 0)) {
                                updateLogs(message("Skip") + ": " + info, true);
                            }
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
                } catch (Exception e) {
                    updateLogs(e.toString(), true);
                }
            }
            conn.commit();
        } catch (Exception e) {
            updateLogs(e.toString(), true);
        }
        updateLogs(message("Imported") + ":" + importCount + "  " + file + "\n"
                + message("Insert") + ":" + insertCount + " "
                + message("Update") + ":" + updateCount + " "
                + message("FailedCount") + ":" + failedCount + " "
                + message("Skipped") + ":" + skipCount, true);
        return importCount;
    }

}
