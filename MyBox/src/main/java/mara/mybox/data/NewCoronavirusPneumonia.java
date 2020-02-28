package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static mara.mybox.data.EpidemicReport.create;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-23
 * @License Apache License Version 2.0
 */
public class NewCoronavirusPneumonia extends EpidemicReport {

    public static String BaiduAddress = "https://voice.baidu.com/act/newpneumonia/newpneumonia/?from=osari_pc_3";
    public static String TencentAddress = "https://api.inews.qq.com/newsqa/v1/query/pubished/daily/list?";

    public static Map<String, Object> readDaiduData(File file) {
        Map<String, Object> values = new HashMap<>();
        values.put("file", file.toString());
        values.put("areaReports", new ArrayList<>());
        values.put("citiesReportsMap", new HashMap<>());
        try {
            String flagStart = "\"mapLastUpdatedTime\"";
            String flagEnd = ",\"dataSource\":";
            boolean started = false;
            StringBuilder s = new StringBuilder();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("utf-8"))) {
                char[] buf = new char[1024];
                String bufStr;
                int len;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    int pos = bufStr.indexOf(flagEnd);
                    if (pos >= 0) {
                        s.append(bufStr);
                        break;
                    }
                    pos = bufStr.indexOf(flagStart);
                    if (pos >= 0) {
                        s.append(bufStr.substring(pos));
                        started = true;
                    } else if (started) {
                        s.append(bufStr);
                    }
                }
            }

            String jsonString = StringTools.decodeUnicode(s.toString());
            File tempFile = FileTools.getTempFile("json");
            FileTools.writeFile(tempFile, jsonString);
            values.put("srcJson", tempFile.getAbsolutePath());

            int pos = jsonString.indexOf("2020");
            String dateString = jsonString.substring(pos, pos + 16) + ":00";
            Date reportTime = DateTools.stringToDatetime(dateString, CommonValues.DatetimeFormat6);
            values.put("time", reportTime);

            while (true) {
                int startPos = jsonString.indexOf("{\"confirmed\":\"");
                if (startPos < 0) {
                    break;
                }
                jsonString = jsonString.substring(startPos);
                int endPos = jsonString.indexOf("]},{");
                if (endPos < 0) {
                    endPos = jsonString.indexOf("]}]");
                    if (endPos < 0) {
                        break;
                    }
                }
                String area = jsonString.substring(0, endPos + 2);
                readBaiduArea(values, area);
                jsonString = jsonString.substring(endPos + 3);
            }
            List<EpidemicReport> areaReports = (List<EpidemicReport>) values.get("areaReports");
            Collections.sort(areaReports, (EpidemicReport r1, EpidemicReport r2) -> {
                if (message("China").equals(r1.getCountry()) && !message("China").equals(r2.getCountry())) {
                    return -1;
                }
                if (!message("China").equals(r1.getCountry()) && message("China").equals(r2.getCountry())) {
                    return 1;
                }
                return r2.getConfirmed() - r1.getConfirmed();
            });
            values.put("areaReports", areaReports);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return values;
    }

    public static void readBaiduArea(Map<String, Object> values,
            String areaValues) {
        try {
            Date reportTime = (Date) values.get("time");
            List<EpidemicReport> areaReports = (List<EpidemicReport>) values.get("areaReports");
            int confirmed = 0, crued = 0, died = 0, startPos, endPos;
            startPos = areaValues.indexOf("\"confirmed\":\"");
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"confirmed\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    confirmed = 0;
                } else {
                    confirmed = Integer.valueOf(field);
                }
            }
            startPos = areaValues.indexOf("\"died\":\"");
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"died\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    died = 0;
                } else {
                    died = Integer.valueOf(field);
                }
            }
            startPos = areaValues.indexOf("\"crued\":\"");
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"crued\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    crued = 0;
                } else {
                    crued = Integer.valueOf(field);
                }
            }
            startPos = areaValues.indexOf("\"area\":\"");
            String area = null;
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"area\":\"".length());
                endPos = field.indexOf("\"");
                area = field.substring(0, endPos);
            }
            GeographyCode code = TableGeographyCode.readArea(area);
            if (code == null && !(AppVariables.getLanguage().startsWith("zh"))) {
                String key = GeographyCode.countriesChineseKey().get(area);
                if (key != null) {
                    code = TableGeographyCode.readArea(message("en", key));
//                    logger.debug(area + " " + key);
                } else {
                    code = TableGeographyCode.readChineseProvince(area);
//                    logger.debug(area);
                }
            }
            EpidemicReport areaReport = create().
                    setDataSet(message("NewCoronavirusPneumonia"))
                    .setTime(reportTime.getTime())
                    .setConfirmed(confirmed).setHealed(crued).setDead(died);
            if (code != null && code.getLongitude() >= -180) {
//                logger.debug(area + " " + code.getLevel() + " " + code.getCountry() + " " + code.getProvince() + " " + code.getFullAddress());
                areaReport.setCountry(code.getCountry())
                        .setLevel(code.getLevel())
                        .setLongitude(code.getLongitude()).setLatitude(code.getLatitude());
                if (message("Province").equals(code.getLevel())) {
                    areaReport.setProvince(code.getAddress());
                }
            } else {
                areaReport.setCountry(message("China")).setProvince(area)
                        .setLevel(message("Province"))
                        .setLongitude(-200).setLatitude(-200);
//                logger.debug(area + " " + areaReport.getLevel() + " " + areaReport.getCountry() + " " + areaReport.getProvince());
            }

            areaReports.add(areaReport);
            values.put("areaReports", areaReports);

            startPos = areaValues.indexOf("\"subList\":[");
            String cities;
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"subList\":[".length());
                endPos = field.indexOf("]");
                cities = field.substring(0, endPos);
                if (!cities.isBlank()) {
                    readBaiduCities(values, areaReport, cities);
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void readBaiduCities(Map<String, Object> values,
            EpidemicReport areaReport, String citiesValues) {
        try {
            Map<EpidemicReport, List<EpidemicReport>> citiesReportsMap
                    = (Map<EpidemicReport, List<EpidemicReport>>) values.get("citiesReportsMap");
            List<EpidemicReport> citiesReports = new ArrayList<>();
            String data = citiesValues;
            while (true) {
                int startPos = data.indexOf("{");
                if (startPos < 0) {
                    break;
                }
                data = data.substring(startPos);
                int endPos = data.indexOf("}");
                if (endPos < 0) {
                    break;
                }
                String city = data.substring(0, endPos + 1);
                EpidemicReport cityReport = baiduReadCity(values, areaReport, city);
                if (cityReport != null) {
                    citiesReports.add(cityReport);
                }
                if (endPos >= data.length()) {
                    break;
                }
                data = data.substring(endPos + 1);
            }

            if (!citiesReports.isEmpty()) {
                Collections.sort(citiesReports, (EpidemicReport r1, EpidemicReport r2) -> {
                    return r2.getConfirmed() - r1.getConfirmed();
                });
                citiesReportsMap.put(areaReport, citiesReports);
            }
            values.put("citiesReportsMap", citiesReportsMap);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static EpidemicReport baiduReadCity(Map<String, Object> values,
            EpidemicReport areaReport, String cityValues) {
        try {
            Date reportTime = (Date) values.get("time");
            int confirmed = 0, crued = 0, died = 0, startPos, endPos;
            startPos = cityValues.indexOf("\"confirmed\":\"");
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"confirmed\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    confirmed = 0;
                } else {
                    confirmed = Integer.valueOf(field);
                }
            }
            startPos = cityValues.indexOf("\"died\":\"");
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"died\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    died = 0;
                } else {
                    died = Integer.valueOf(field);
                }
            }
            startPos = cityValues.indexOf("\"crued\":\"");
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"crued\":\"".length());
                endPos = field.indexOf("\"");
                field = field.substring(0, endPos);
                if (field.isBlank()) {
                    crued = 0;
                } else {
                    crued = Integer.valueOf(field);
                }
            }
            startPos = cityValues.indexOf("\"city\":\"");
            String city = null;
            if (startPos >= 0) {
                String field = cityValues.substring(startPos + "\"city\":\"".length());
                endPos = field.indexOf("\"");
                city = field.substring(0, endPos);
            }
            String province = areaReport.getProvince();
            GeographyCode code = TableGeographyCode.readChineseCity(province, city);
            EpidemicReport cityReport = create().
                    setDataSet(message("NewCoronavirusPneumonia"))
                    .setLevel(message("City"))
                    .setCountry(areaReport.getCountry()).setProvince(province)
                    .setTime(reportTime.getTime())
                    .setConfirmed(confirmed).setHealed(crued).setDead(died);
            if (code != null && code.getLongitude() >= -180) {
                cityReport.setCity(code.getAddress())
                        .setLongitude(code.getLongitude()).setLatitude(code.getLatitude());
            } else {
                cityReport.setCity(city)
                        .setLongitude(-200).setLatitude(-200);
            }
//            logger.debug(city + "" + report.getLevel());

            return cityReport;

        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    /*
        Expired methods.
     */
    public static EpidemicReport ChinaProvinceReport(
            String dataset, long time, String province,
            int confirmed, int suspected, int healed, int dead) {
        GeographyCode code = GeographyCode.query(province);
        EpidemicReport report = create().setDataSet(dataset)
                .setCountry(message("China")).setProvince(message(province))
                .setLevel(message("Province"))
                .setConfirmed(confirmed).setSuspected(suspected)
                .setHealed(healed).setDead(dead)
                .setTime(time);
        if (code != null) {
            report.setLongitude(code.longitude).setLatitude(code.latitude);
        } else {
            report.setLongitude(-200).setLatitude(-200); // GaoDe Map only supports geography codes of China
        }
        return report;
    }

    public static EpidemicReport CountryReport(
            String dataset, long time, String country,
            int confirmed, int suspected, int healed, int dead) {
        GeographyCode code = TableGeographyCode.read(message(country));
        EpidemicReport report = create().setDataSet(dataset)
                .setCountry(message(country)).setLevel(message("Country"))
                .setConfirmed(confirmed).setSuspected(suspected)
                .setHealed(healed).setDead(dead)
                .setTime(time);
        if (code != null) {
            report.setLongitude(code.longitude).setLatitude(code.latitude);
        } else {
            report.setLongitude(-200).setLatitude(-200); // GaoDe Map only supports geography codes of China
        }
        return report;
    }

}
