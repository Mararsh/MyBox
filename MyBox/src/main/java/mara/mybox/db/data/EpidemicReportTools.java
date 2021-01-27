package mara.mybox.db.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-8-11
 * @License Apache License Version 2.0
 */
public class EpidemicReportTools {

    // Only copy base attributes.
    public static EpidemicReport copy(EpidemicReport report) {
        try {
            EpidemicReport cloned = EpidemicReport.create()
                    .setDataSet(report.getDataSet()).setLocation(report.getLocation()).setLocationid(report.getLocationid())
                    .setConfirmed(report.getConfirmed()).setHealed(report.getHealed()).setDead(report.getDead())
                    .setTime(report.getTime()).setSource(report.getSource());
            return cloned;
        } catch (Exception e) {
            return null;
        }
    }

    public static Number getNumber(EpidemicReport report, String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            if (message("Confirmed").equals(name) || "confirmed".equals(name)) {
                return report.getConfirmed();

            } else if (message("Healed").equals(name) || "healed".equals(name)) {
                return report.getHealed();

            } else if (message("Dead").equals(name) || "dead".equals(name)) {
                return report.getDead();

            } else if (message("IncreasedConfirmed").equals(name) || "increased_confirmed".equals(name)) {
                return report.getIncreasedConfirmed();

            } else if (message("IncreasedHealed").equals(name) || "increased_healed".equals(name)) {
                return report.getIncreasedHealed();

            } else if (message("IncreasedDead").equals(name) || "increased_dead".equals(name)) {
                return report.getIncreasedDead();

            } else if (message("HealedConfirmedPermillage").equals(name) || "healed_confirmed_permillage".equals(name)) {
                return report.getHealedConfirmedPermillage();

            } else if (message("DeadConfirmedPermillage").equals(name) || "dead_confirmed_permillage".equals(name)) {
                return report.getDeadConfirmedPermillage();

            } else if (message("ConfirmedPopulationPermillage").equals(name) || "confirmed_population_permillage".equals(name)) {
                return report.getConfirmedPopulationPermillage();

            } else if (message("HealedPopulationPermillage").equals(name) || "healed_population_permillage".equals(name)) {
                return report.getHealedPopulationPermillage();

            } else if (message("DeadPopulationPermillage").equals(name) || "dead_population_permillage".equals(name)) {
                return report.getDeadPopulationPermillage();

            } else if (message("ConfirmedAreaPermillage").equals(name) || "confirmed_area_permillage".equals(name)) {
                return report.getConfirmedAreaPermillage();

            } else if (message("HealedAreaPermillage").equals(name) || "healed_area_permillage".equals(name)) {
                return report.getHealedAreaPermillage();

            } else if (message("DeadAreaPermillage").equals(name) || "dead_area_permillage".equals(name)) {
                return report.getDeadAreaPermillage();

            } else {
                return null;
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
        import
     */
    // DataSet,Time,Locationid,Confirmed,Healed,Dead,IncreasedConfirmed,IncreasedHealed,IncreasedDead,DataSource
    public static EpidemicReport readIntenalRecord(List<String> names, CSVRecord record) {
        try {
            EpidemicReport report = new EpidemicReport();
            report.setDataSet(record.get("DataSet"));
            report.setTime(DateTools.stringToDatetime(record.get("Time")).getTime());
            report.setLocationid(Long.valueOf(record.get("Locationid")));
            report.setConfirmed(Long.valueOf(record.get("Confirmed")));
            report.setHealed(Long.valueOf(record.get("Healed")));
            report.setDead(Long.valueOf(record.get("Dead")));
            report.setIncreasedConfirmed(Long.valueOf(record.get("IncreasedConfirmed")));
            report.setIncreasedHealed(Long.valueOf(record.get("IncreasedHealed")));
            report.setIncreasedDead(Long.valueOf(record.get("IncreasedDead")));
            report.setSource(Short.valueOf(record.get("DataSource")));
            return report;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    // Data Set,Time,Confirmed,Healed,Dead,Increased Confirmed,Increased Healed,Increased Dead,Data Source,
    // Level,Continent,Country,Province,City,County,Town,Village,Building,Longitude,Latitude
    // 数据集,时间,确认,治愈,死亡,新增确诊,新增治愈,新增死亡,数据源,级别,洲,国家,省,市,区县,乡镇,村庄,建筑物,经度,纬度
    public static Map<String, Object> readExtenalRecord(Connection conn, PreparedStatement geoInsert,
            String lang, List<String> names, CSVRecord record) {
        Map<String, Object> ret = new HashMap<>();
        try {
            EpidemicReport report = new EpidemicReport();
            if (names.contains("DataSet")) {
                report.setDataSet(record.get("DataSet"));
            } else if (names.contains(message(lang, "DataSet"))) {
                report.setDataSet(record.get(message(lang, "DataSet")));
            } else {
                ret.put("message", "Miss DataSet");
                return ret;
            }
            if (names.contains("Time")) {
                try {
                    report.setTime(DateTools.stringToDatetime(record.get("Time")).getTime());
                } catch (Exception e) {
                    ret.put("message", "Miss Time1");
                    return ret;
                }
            } else if (names.contains(message(lang, "Time"))) {
                try {
                    report.setTime(DateTools.stringToDatetime(record.get(message(lang, "Time"))).getTime());
                } catch (Exception e) {
                    ret.put("message", "Miss Time2");
                    return ret;
                }
            } else {
                ret.put("message", "Miss Time3");
                return ret;
            }
            if (names.contains("Confirmed")) {
                try {
                    report.setConfirmed(Long.valueOf(record.get("Confirmed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Confirmed"))) {
                try {
                    report.setConfirmed(Long.valueOf(record.get(message(lang, "Confirmed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("Healed")) {
                try {
                    report.setHealed(Long.valueOf(record.get("Healed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Healed"))) {
                try {
                    report.setHealed(Long.valueOf(record.get(message(lang, "Healed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("Dead")) {
                try {
                    report.setDead(Long.valueOf(record.get("Dead")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Dead"))) {
                try {
                    report.setDead(Long.valueOf(record.get(message(lang, "Dead"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("IncreasedConfirmed")) {
                try {
                    report.setIncreasedConfirmed(Long.valueOf(record.get("IncreasedConfirmed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "IncreasedConfirmed"))) {
                try {
                    report.setIncreasedConfirmed(Long.valueOf(record.get(message(lang, "IncreasedConfirmed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("IncreasedHealed")) {
                try {
                    report.setIncreasedHealed(Long.valueOf(record.get("IncreasedHealed")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "IncreasedHealed"))) {
                try {
                    report.setIncreasedHealed(Long.valueOf(record.get(message(lang, "IncreasedHealed"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("IncreasedDead")) {
                try {
                    report.setIncreasedDead(Long.valueOf(record.get("IncreasedDead")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "IncreasedDead"))) {
                try {
                    report.setIncreasedDead(Long.valueOf(record.get(message(lang, "IncreasedDead"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("DataSource")) {
                try {
                    report.setSource(EpidemicReport.sourceValue(lang, record.get("DataSource")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "DataSource"))) {
                try {
                    report.setSource(EpidemicReport.sourceValue(lang, record.get(message(lang, "DataSource"))));
                } catch (Exception e) {
                }
            }
            GeographyCodeLevel levelCode;
            if (names.contains("Level")) {
                levelCode = new GeographyCodeLevel(record.get("Level"));
            } else if (names.contains(message(lang, "Level"))) {
                levelCode = new GeographyCodeLevel(record.get(message(lang, "Level")));
            } else {
                ret.put("message", "Miss level");
                return ret;
            }
            double longitude = -200;
            if (names.contains("Longitude")) {
                try {
                    longitude = Double.valueOf(record.get("Longitude"));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Longitude"))) {
                try {
                    longitude = Double.valueOf(record.get(message(lang, "Longitude")));
                } catch (Exception e) {
                }
            }
            double latitude = -200;
            if (names.contains("Latitude")) {
                try {
                    latitude = Double.valueOf(record.get("Latitude"));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Latitude"))) {
                try {
                    latitude = Double.valueOf(record.get(message(lang, "latitude")));
                } catch (Exception e) {
                }
            }
            String continent = names.contains(message(lang, "Continent"))
                    ? record.get(message(lang, "Continent")) : (names.contains("Continent") ? record.get("Continent") : null);
            String country = names.contains(message(lang, "Country"))
                    ? record.get(message(lang, "Country")) : (names.contains("Country") ? record.get("Country") : null);
            String province = names.contains(message(lang, "Province"))
                    ? record.get(message(lang, "Province")) : (names.contains("Province") ? record.get("Province") : null);
            String city = names.contains(message(lang, "City"))
                    ? record.get(message(lang, "City")) : (names.contains("City") ? record.get("City") : null);
            String county = names.contains(message(lang, "County"))
                    ? record.get(message(lang, "County")) : (names.contains("County") ? record.get("County") : null);
            String town = names.contains(message(lang, "Town"))
                    ? record.get(message(lang, "Town")) : (names.contains("Town") ? record.get("Town") : null);
            String village = names.contains(message(lang, "Village"))
                    ? record.get(message(lang, "Village")) : (names.contains("Village") ? record.get("Village") : null);
            String building = names.contains(message(lang, "Building"))
                    ? record.get(message(lang, "Building")) : (names.contains("Building") ? record.get("Building") : null);
            String poi = names.contains(message(lang, "PointOfInterest"))
                    ? record.get(message(lang, "PointOfInterest")) : (names.contains("PointOfInterest") ? record.get("PointOfInterest") : null);
            Map<String, Object> codeRet = GeographyCodeTools.encode(conn, geoInsert, levelCode.getLevel(),
                    longitude, latitude, continent, country, province, city, county, town, village, building, poi, true, false);
            if (codeRet.get("message") != null) {
                String msg = (String) codeRet.get("message");
                if (!msg.trim().isBlank()) {
                    ret.put("message", codeRet.get("message"));
                }
            }
            if (codeRet.get("code") == null) {
                return ret;
            }
            GeographyCode code = (GeographyCode) codeRet.get("code");
            report.setLocationid(code.getId());
            report.setLocation(code);
            ret.put("report", report);
            return ret;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            ret.put("message", e.toString());
            return ret;
        }
    }

}
