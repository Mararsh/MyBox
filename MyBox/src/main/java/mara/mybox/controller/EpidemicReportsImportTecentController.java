package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import mara.mybox.data.EpidemicReport;
import static mara.mybox.data.EpidemicReport.create;
import mara.mybox.data.GeographyCode;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.NetworkTools.trustAllManager;
import static mara.mybox.tools.NetworkTools.trustAllVerifier;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * @Author Mara
 * @CreateDate 2020-2-16
 * @License Apache License Version 2.0
 */
public class EpidemicReportsImportTecentController extends EpidemicReportsImportBaiduController {

    protected XSSFSheet sheet;

    public EpidemicReportsImportTecentController() {
        baseTitle = AppVariables.message("ImportEpidemicReportTecent");
        Address = "https://api.inews.qq.com/newsqa/v1/query/pubished/daily/list?";
        Dataset = "COVID-19_Tencent";
    }

    @Override
    protected boolean doTask() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            if (TableGeographyCode.China(conn) == null) {
                updateLogs(message("LoadingPredefinedGeographyCodes"), true);
                GeographyCode.predefined(conn);
            }
            List<GeographyCode> provinces = TableGeographyCode.queryCodes(conn,
                    "SELECT * FROM Geography_Code WHERE level=4 AND country=100", false);
            for (GeographyCode provinceCode : provinces) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                String province = provinceCode.getChineseName();
                String provinceEncode = URLEncoder.encode(province, "UTF-8");
                URL url = new URL(address + "province=" + provinceEncode);
                updateLogs(url.toString(), true);

                File pageFile = FileTools.getTempFile(".txt");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllManager(), new SecureRandom());
                connection.setSSLSocketFactory(sc.getSocketFactory());
                connection.setHostnameVerifier(trustAllVerifier());
                connection.connect();
                try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                         BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pageFile))) {
                    byte[] buf = new byte[CommonValues.IOBufferLength];
                    int len;
                    while ((len = inStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                }
                List<EpidemicReport> provinceReports = readProvince(provinceCode, pageFile);
                if (provinceReports.isEmpty()) {
                    updateLogs(province + " " + message("DataSize") + ":" + provinceReports.size(), true);
                } else if (TableEpidemicReport.write(conn, provinceReports, replaceCheck.isSelected()) > 0) {
                    updateLogs(message("Imported") + " " + province + " " + message("DataSize") + ":" + provinceReports.size(), true);
                } else {
                    updateLogs(message("Skip") + " " + province + " " + message("DataSize") + ":" + provinceReports.size(), true);
                }
                conn.commit();

                List<GeographyCode> cities = TableGeographyCode.queryCodes(conn,
                        "SELECT * FROM Geography_Code WHERE level=5 AND country=100 AND province=" + provinceCode.getGcid(), false);
                for (GeographyCode cityCode : cities) {
                    if (task == null || task.isCancelled()) {
                        return false;
                    }
                    String city = cityCode.getChineseName();
                    String cityEncode = URLEncoder.encode(city, "UTF-8");
                    url = new URL(address
                            + "province=" + provinceEncode
                            + "&city=" + cityEncode);
                    pageFile = FileTools.getTempFile(".txt");
                    connection = (HttpsURLConnection) url.openConnection();
                    sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllManager(), new SecureRandom());
                    connection.setSSLSocketFactory(sc.getSocketFactory());
                    connection.setHostnameVerifier(trustAllVerifier());
                    connection.connect();
                    try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pageFile))) {
                        byte[] buf = new byte[CommonValues.IOBufferLength];
                        int len;
                        while ((len = inStream.read(buf)) != -1) {
                            outputStream.write(buf, 0, len);
                        }
                    }

                    List<EpidemicReport> cityReports = readCity(provinceCode, cityCode, pageFile);
                    if (cityReports.isEmpty()) {
                        updateLogs(province + " " + message("DataSize") + ":" + cityReports.size(), true);
                    } else if (TableEpidemicReport.write(conn, cityReports, replaceCheck.isSelected()) > 0) {
                        updateLogs(message("Imported") + " " + province + " " + city + " " + message("DataSize") + ":" + cityReports.size(), true);
                    } else {
                        updateLogs(message("Skip") + " " + province + " " + city + " " + message("DataSize") + ":" + cityReports.size(), true);
                    }
                    conn.commit();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            return false;
        }

    }

    protected List<EpidemicReport> readProvince(GeographyCode province, File file) {
        final List<EpidemicReport> provinceReports = new ArrayList<>();
        try {
            String data = FileTools.readTexts(file);
            while (true) {
                if (task == null || task.isCancelled()) {
                    return provinceReports;
                }
                EpidemicReport report = create()
                        .setDataSet(Dataset).setSource(2)
                        .setLocationid(province.getGcid());
                int startPos = data.indexOf("\"date\":\"");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"date\":\"".length();
                data = data.substring(startPos);
                int endPos = data.indexOf(".");
                if (endPos < 0) {
                    break;
                }
                int month = Integer.parseInt(data.substring(0, endPos));
                int day = Integer.parseInt(data.substring(endPos + 1, endPos + 3));
                String timeString = "2020-" + month + "-" + day + EpidemicReport.COVID19TIME;
                report.setTime(DateTools.stringToDatetime(timeString).getTime());

                startPos = data.indexOf("\"country\":\"");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"country\":\"".length();
                data = data.substring(startPos);
                endPos = data.indexOf("\"");
                if (endPos < 0) {
                    break;
                }

                startPos = data.indexOf("\"province\":\"");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"province\":\"".length();
                data = data.substring(startPos);
                endPos = data.indexOf("\"");
                if (endPos < 0) {
                    break;
                }

                startPos = data.indexOf("\"confirm\":");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"confirm\":".length();
                data = data.substring(startPos);
                endPos = data.indexOf(",");
                if (endPos < 0) {
                    break;
                }
                report.setConfirmed(Integer.parseInt(data.substring(0, endPos)));

                startPos = data.indexOf("\"dead\":");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"dead\":".length();
                data = data.substring(startPos);
                endPos = data.indexOf(",");
                if (endPos < 0) {
                    break;
                }
                report.setDead(Integer.parseInt(data.substring(0, endPos)));

                startPos = data.indexOf("\"heal\":");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"heal\":".length();
                data = data.substring(startPos);
                endPos = data.indexOf(",");
                if (endPos < 0) {
                    break;
                }
                report.setHealed(Integer.parseInt(data.substring(0, endPos)));

                if (report.getConfirmed() <= 0 && report.getHealed() <= 0 && report.getDead() <= 0) {
                    continue;
                }

                provinceReports.add(report);
                updateLogs(Dataset + " " + province.getName() + " " + timeString + " "
                        + report.getConfirmed() + " " + report.getHealed() + " " + report.getDead(), false);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return provinceReports;
    }

    protected List<EpidemicReport> readCity(GeographyCode province, GeographyCode city, File file) {
        final List<EpidemicReport> citiesReports = new ArrayList<>();
        try {
            String data = FileTools.readTexts(file);
            while (true) {
                if (task == null || task.isCancelled()) {
                    return citiesReports;
                }
                EpidemicReport cityReport = create()
                        .setDataSet(Dataset).setSource(2)
                        .setLocationid(province.getGcid());
                int startPos = data.indexOf("\"date\":\"");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"date\":\"".length();
                data = data.substring(startPos);
                int endPos = data.indexOf(".");
                if (endPos < 0) {
                    break;
                }
                int month = Integer.parseInt(data.substring(0, endPos));
                int day = Integer.parseInt(data.substring(endPos + 1, endPos + 3));
                String timeString = "2020-" + month + "-" + day + EpidemicReport.COVID19TIME;
                cityReport.setTime(DateTools.stringToDatetime(timeString).getTime());

                startPos = data.indexOf("\"city\":\"");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"city\":\"".length();
                data = data.substring(startPos);
                endPos = data.indexOf("\"");
                if (endPos < 0) {
                    break;
                }

                startPos = data.indexOf("\"confirm\":");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"confirm\":".length();
                data = data.substring(startPos);
                endPos = data.indexOf(",");
                if (endPos < 0) {
                    break;
                }
                cityReport.setConfirmed(Integer.parseInt(data.substring(0, endPos)));

                startPos = data.indexOf("\"dead\":");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"dead\":".length();
                data = data.substring(startPos);
                endPos = data.indexOf(",");
                if (endPos < 0) {
                    break;
                }
                cityReport.setDead(Integer.parseInt(data.substring(0, endPos)));

                startPos = data.indexOf("\"heal\":");
                if (startPos < 0) {
                    break;
                }
                startPos = startPos + "\"heal\":".length();
                data = data.substring(startPos);
                endPos = data.indexOf(",");
                if (endPos < 0) {
                    break;
                }
                cityReport.setHealed(Integer.parseInt(data.substring(0, endPos)));

                if (cityReport.getConfirmed() <= 0 && cityReport.getHealed() <= 0 && cityReport.getDead() <= 0) {
                    continue;
                }

                citiesReports.add(cityReport);
                updateLogs(Dataset + " " + province.getName() + " " + city.getName()
                        + " " + timeString + " "
                        + cityReport.getConfirmed() + " " + cityReport.getHealed() + " " + cityReport.getDead(), false);

            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return citiesReports;
    }

}
