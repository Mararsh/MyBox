package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import mara.mybox.data.EpidemicReport;
import static mara.mybox.data.EpidemicReport.create;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import static mara.mybox.tools.NetworkTools.trustAllManager;
import static mara.mybox.tools.NetworkTools.trustAllVerifier;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-16
 * @License Apache License Version 2.0
 */
public class EpidemicReportsFetchNPCDataController extends BaseController {

    protected EpidemicReportsController parent;
    protected Date baiduReportTime;
    protected List<EpidemicReport> baiduReports, tengXunReports;
    protected Map<EpidemicReport, List<EpidemicReport>> baiduCitiesReports;
    protected StringTable baiduTable;
    protected WebEngine webEngineBaidu, webEngineTengXun;
    protected StringBuilder tengxunJsonBuilder;
    protected String tengXunHtml, baiduAddress, tengxunAddress;
    protected LoadingController loadingController;

    @FXML
    protected WebView webViewBaidu, webViewTengXun;
    @FXML
    protected TextArea jsonAreaBaidu, xmlAreaBaidu, jsonAreaTengXun, xmlAreaTengXun, logsTengxunArea;
    @FXML
    protected Button dataImportBaiduButton, dataImportTengXunButton,
            saveAsBaiduButton, saveAsTengXunButton, catButton;
    @FXML
    protected TabPane tengxunPane;
    @FXML
    protected Tab tengxunHtmlTab, tengxunLogsTab;
    @FXML
    protected TextField tengxunInput, baiduInput;

    public EpidemicReportsFetchNPCDataController() {
        baseTitle = AppVariables.message("FetchNPCData");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            webEngineBaidu = webViewBaidu.getEngine();
            webEngineBaidu.setJavaScriptEnabled(true);

            webEngineTengXun = webViewTengXun.getEngine();
            webEngineTengXun.setJavaScriptEnabled(true);

            dataImportBaiduButton.setDisable(true);
            saveAsBaiduButton.setDisable(true);
            dataImportTengXunButton.setDisable(true);
            saveAsTengXunButton.setDisable(true);

            baiduAddress = "https://voice.baidu.com/act/newpneumonia/newpneumonia/?from=osari_pc_3";
            tengxunAddress = "https://api.inews.qq.com/newsqa/v1/query/pubished/daily/list?";

            tengxunInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        String v = tengxunInput.getText().trim();
                        if (v.isBlank()) {
                            return;
                        }
                        tengxunAddress = v;
                    });

            baiduInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        String v = baiduInput.getText().trim();
                        if (v.isBlank()) {
                            return;
                        }
                        baiduAddress = v;
                    });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            FxmlControl.setTooltip(catButton, message("MyBoxInternetDataPath"));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    public void baiduAction() {
        jsonAreaBaidu.clear();
        xmlAreaBaidu.clear();
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private StringBuilder baiduJson, baiduXml;

                @Override
                protected boolean handle() {
                    try {
                        GeographyCode.importCodes();

                        baiduReports = new ArrayList();
                        baiduCitiesReports = new HashMap<>();
                        baiduJson = new StringBuilder();

                        URL url = new URL(baiduAddress);
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

                        baiduReadData(pageFile);

                        List<String> names = new ArrayList<>();
                        String title = message("EpidemicReport") + " " + message("NewCoronavirusPneumonia") + " "
                                + "" + DateTools.datetimeToString(baiduReportTime.getTime());
                        names.addAll(Arrays.asList(message("Level"),
                                message("Country"), message("Province"), message("City"),
                                message("Confirmed"), message("Healed"), message("Dead"),
                                message("Longitude"), message("Latitude")
                        ));
                        baiduTable = new StringTable(names, title);
                        baiduXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                        baiduXml.append("<EpidemicReports time=\"").
                                append(DateTools.datetimeToString(baiduReportTime)).
                                append("\">\n");
                        String indent = "    ";
                        baiduJson = new StringBuilder("{\"time\":\"" + DateTools.datetimeToString(baiduReportTime)
                                + ", \"EpidemicReports\": [\n");

                        for (EpidemicReport report : baiduReports) {
                            List<String> row = new ArrayList<>();
                            row.addAll(Arrays.asList(report.getLevel(),
                                    report.getCountry(), report.getProvince() != null ? report.getProvince() : "", "",
                                    report.getConfirmed() + "", report.getHealed() + "", report.getDead() + "",
                                    report.getLongitude() >= -180 ? report.getLongitude() + "" : "",
                                    report.getLatitude() >= -180 ? report.getLatitude() + "" : ""
                            ));
                            baiduTable.add(row);

                            baiduXml.append(indent).append("<EpidemicReport level=\"").append(report.getLevel()).append("\" ")
                                    .append(" country=\"").append(report.getCountry()).append("\" ");
                            if (report.getProvince() != null) {
                                baiduXml.append(" province=\"").append(report.getProvince()).append("\" ");
                            }
                            if (report.getLongitude() >= -180) {
                                baiduXml.append(" longtitude=\"").append(report.getLongitude()).append("\" ")
                                        .append(" latitude=\"").append(report.getLatitude()).append("\" ");
                            }
                            baiduXml.append(" confirmed=\"").append(report.getConfirmed()).append("\" ")
                                    .append(" healed=\"").append(report.getHealed()).append("\" ")
                                    .append(" dead=\"").append(report.getDead()).append("\" >\n");

                            baiduJson.append(indent).append("{\"level\":\"").append(report.getLevel())
                                    .append("\",\"country\":\"").append(report.getCountry()).append("\"");
                            if (report.getProvince() != null) {
                                baiduJson.append(",\"province\":\"").append(report.getProvince()).append("\"");
                            }
                            if (report.getLongitude() >= -180) {
                                baiduJson.append(",\"longtitude\":").append(report.getLongitude())
                                        .append(",\"latitude\":").append(report.getLatitude());
                            }
                            baiduJson.append(",\"confirmed\":").append(report.getConfirmed())
                                    .append(",\"healed\":").append(report.getHealed())
                                    .append(",\"dead\":").append(report.getDead());

                            List<EpidemicReport> citiesReports = baiduCitiesReports.get(report);
                            if (citiesReports != null && !citiesReports.isEmpty()) {
                                baiduJson.append(",\"cities\":[\n");
                                for (int i = 0; i < citiesReports.size(); i++) {
                                    EpidemicReport cityReport = citiesReports.get(i);
                                    row = new ArrayList<>();
                                    row.addAll(Arrays.asList(cityReport.getLevel(),
                                            cityReport.getCountry(), cityReport.getProvince() != null ? cityReport.getProvince() : "",
                                            cityReport.getCity() != null ? cityReport.getCity() : "",
                                            cityReport.getConfirmed() + "", cityReport.getHealed() + "", cityReport.getDead() + "",
                                            cityReport.getLongitude() >= -180 ? cityReport.getLongitude() + "" : "",
                                            cityReport.getLatitude() >= -180 ? cityReport.getLatitude() + "" : ""
                                    ));
                                    baiduTable.add(row);

                                    baiduXml.append(indent).append(indent).append("<EpidemicReport level=\"").append(cityReport.getLevel()).append("\" ")
                                            .append(" country=\"").append(cityReport.getCountry()).append("\" ");
                                    if (cityReport.getProvince() != null) {
                                        baiduXml.append(" province=\"").append(cityReport.getProvince()).append("\" ");
                                    }
                                    if (cityReport.getCity() != null) {
                                        baiduXml.append(" city=\"").append(cityReport.getCity()).append("\" ");
                                    }
                                    if (cityReport.getLongitude() >= -180) {
                                        baiduXml.append(" longtitude=\"").append(cityReport.getLongitude()).append("\" ")
                                                .append(" latitude=\"").append(cityReport.getLatitude()).append("\" ");
                                    }
                                    baiduXml.append(" confirmed=\"").append(cityReport.getConfirmed()).append("\" ")
                                            .append(" healed=\"").append(cityReport.getHealed()).append("\" ")
                                            .append(" dead=\"").append(cityReport.getDead()).append("\" />\n");

                                    baiduJson.append(indent).append(indent).append(indent).
                                            append("{\"level\":\"").append(cityReport.getLevel())
                                            .append("\",\"country\":\"").append(cityReport.getCountry()).append("\"");
                                    if (cityReport.getProvince() != null) {
                                        baiduJson.append(",\"province\":\"").append(cityReport.getProvince()).append("\"");
                                    }
                                    if (cityReport.getCity() != null) {
                                        baiduJson.append(",\"city\":\"").append(cityReport.getCity()).append("\"");
                                    }
                                    if (cityReport.getLongitude() >= -180) {
                                        baiduJson.append(",\"longtitude\":").append(cityReport.getLongitude())
                                                .append(",\"latitude\":").append(cityReport.getLatitude());
                                    }
                                    baiduJson.append(",\"confirmed\":").append(cityReport.getConfirmed())
                                            .append(",\"healed\":").append(cityReport.getHealed())
                                            .append(",\"dead\":").append(cityReport.getDead()).append("}");
                                    if (i < citiesReports.size() - 1) {
                                        baiduJson.append(",");
                                    } else {
                                        baiduJson.append("]");
                                    }
                                    baiduJson.append("\n");
                                }
                                baiduJson.append(indent).append("}\n");
                            } else {
                                baiduJson.append("}\n");
                            }
                            baiduXml.append(indent).append("</EpidemicReport>\n");
                        }
                        baiduXml.append(indent).append("</EpidemicReports>\n");
                        baiduJson.append("]}");

                        return true;

                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    webEngineBaidu.loadContent(baiduTable.html());
                    xmlAreaBaidu.setText(baiduXml.toString());
                    jsonAreaBaidu.setText(baiduJson.toString());
                    dataImportBaiduButton.setDisable(false);
                    saveAsBaiduButton.setDisable(false);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public void baiduReadData(File file) {
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

            String data = StringTools.decodeUnicode(s.toString());
            int pos = data.indexOf("2020");
            String dateString = data.substring(pos, pos + 16) + ":00";
            baiduReportTime = DateTools.stringToDatetime(dateString, CommonValues.DatetimeFormat6);

            while (true) {
                int startPos = data.indexOf("{\"confirmed\":\"");
                if (startPos < 0) {
                    break;
                }
                data = data.substring(startPos);
                int endPos = data.indexOf("]},{");
                if (endPos < 0) {
                    endPos = data.indexOf("]}]");
                    if (endPos < 0) {
                        break;
                    }
                }
                String area = data.substring(0, endPos + 2);
                baiduReadArea(area);
                data = data.substring(endPos + 3);
            }
            Collections.sort(baiduReports, (EpidemicReport r1, EpidemicReport r2) -> {
                if (message("China").equals(r1.getCountry()) && !message("China").equals(r2.getCountry())) {
                    return -1;
                }
                if (!message("China").equals(r1.getCountry()) && message("China").equals(r2.getCountry())) {
                    return 1;
                }
                return r2.getConfirmed() - r1.getConfirmed();
            });
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void baiduReadArea(String areaValues) {
        try {
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
                    .setTime(baiduReportTime.getTime())
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

            baiduReports.add(areaReport);

            startPos = areaValues.indexOf("\"subList\":[");
            String cities;
            if (startPos >= 0) {
                String field = areaValues.substring(startPos + "\"subList\":[".length());
                endPos = field.indexOf("]");
                cities = field.substring(0, endPos);
                if (!cities.isBlank()) {
                    baiduReadCities(areaReport, cities);
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void baiduReadCities(EpidemicReport areaReport, String citiesValues) {
        try {
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
                EpidemicReport cityReport = baiduReadCity(areaReport, city);
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
                baiduCitiesReports.put(areaReport, citiesReports);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public EpidemicReport baiduReadCity(EpidemicReport areaReport,
            String cityValues) {
        try {
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
                    .setTime(baiduReportTime.getTime())
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

    @FXML
    public void importBaiduAction() {
        if (baiduReports == null || baiduReports.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        TableEpidemicReport.write(baiduReports);
                        for (EpidemicReport report : baiduReports) {
                            List<EpidemicReport> citiesReports = baiduCitiesReports.get(report);
                            if (citiesReports == null || citiesReports.isEmpty()) {
                                continue;
                            }
                            TableEpidemicReport.write(citiesReports);
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (parent != null) {
                        parent.getMyStage().toFront();
                        parent.loadTree();
                    } else {
                        openStage(CommonValues.EpidemicReportsFxml);
                    }
//                    closeStage();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void popBaiduPath(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Html);
            }

            @Override
            public void handleSelect() {
                saveBaiduAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void saveBaiduAction() {
        if (baiduReportTime == null) {
            return;
        }
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                message("NewCoronavirusPneumonia") + "_"
                + DateTools.datetimeToString(baiduReportTime.getTime()).replace(":", "-") + ".html",
                CommonFxValues.TextExtensionFilter, false);
        if (file == null) {
            return;
        }
        recordFileWritten(file, targetPathKey, VisitHistory.FileType.Html, VisitHistory.FileType.Html);

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        FileTools.writeFile(file, baiduTable.html());
                        String filename = FileTools.replaceFileSuffix(file.getAbsolutePath(), "json");
                        FileTools.writeFile(new File(filename), jsonAreaBaidu.getText());
                        filename = FileTools.replaceFileSuffix(file.getAbsolutePath(), "xml");
                        FileTools.writeFile(new File(filename), xmlAreaBaidu.getText());
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    browseURI(file.getParentFile().toURI());
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void tengXunAction() {
        tengxunPane.getSelectionModel().select(tengxunLogsTab);
        logsTengxunArea.clear();
        jsonAreaTengXun.clear();
        xmlAreaTengXun.clear();
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private StringBuilder tengxunXmlBuilder;
                private String title;

                @Override
                protected boolean handle() {
                    try {
                        GeographyCode.importCodes();

                        tengXunReports = new ArrayList();
                        tengxunJsonBuilder = new StringBuilder();
                        StringBuilder tengXunHtmlBuilder = new StringBuilder();

                        List<String> names = new ArrayList<>();
                        title = message("NewCoronavirusPneumonia") + " " + message("China");
                        names.addAll(Arrays.asList(message("Time"),
                                message("Level"), message("Province"), message("City"),
                                message("Confirmed"), message("Healed"), message("Dead"),
                                message("Longitude"), message("Latitude")
                        ));

                        tengxunXmlBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                        tengxunXmlBuilder.append("<EpidemicReports title=\"").append(title).append("\">\n");
                        String indent = "    ";

                        for (String provinceKey
                                : GeographyCode.ChineseProvinces()) {
                            if (isCancelled()) {
                                return false;
                            }
                            String province = message(provinceKey);
                            URL url = new URL(tengxunAddress + "province="
                                    + URLEncoder.encode(message("zh", provinceKey), "UTF-8"));
                            if (loadingController != null) {
                                Platform.runLater(() -> {
                                    loadingController.setInfo(province);
                                });
                            }
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

                            List<EpidemicReport> reports = tengXunReadProvince(province, pageFile);
                            tengXunReports.addAll(reports);
                            StringTable tengXunTable = new StringTable(names, title + " " + province);
                            tengxunXmlBuilder.append(indent).append("<Province ")
                                    .append(" level=\"").append(message("Province")).append("\" ")
                                    .append(" province=\"").append(province).append("\">\n");
                            tengxunXmlBuilder.append(indent).append(indent).append("<Reports>\n");
                            for (EpidemicReport report : reports) {
                                if (isCancelled()) {
                                    return false;
                                }
                                List<String> row = new ArrayList<>();
                                row.addAll(Arrays.asList(DateTools.datetimeToString(report.getTime()),
                                        report.getLevel(), report.getProvince(),
                                        report.getCity() != null ? report.getCity() : "",
                                        report.getConfirmed() + "", report.getHealed() + "", report.getDead() + "",
                                        report.getLongitude() >= -180 ? report.getLongitude() + "" : "",
                                        report.getLatitude() >= -180 ? report.getLatitude() + "" : ""
                                ));
                                tengXunTable.add(row);

                                tengxunXmlBuilder.append(indent).append(indent).append(indent).append("<Report ")
                                        .append(" time=\"").append(DateTools.datetimeToString(report.getTime())).append("\" ")
                                        .append(" confirmed=\"").append(report.getConfirmed()).append("\" ")
                                        .append(" healed=\"").append(report.getHealed()).append("\" ")
                                        .append(" dead=\"").append(report.getDead()).append("\" ");
                                if (report.getLongitude() >= -180) {
                                    tengxunXmlBuilder.append(" longtitude=\"").append(report.getLongitude()).append("\" ")
                                            .append(" latitude=\"").append(report.getLatitude()).append("\"/>\n");
                                } else {
                                    tengxunXmlBuilder.append("/>\n");
                                }
                            }
                            tengXunHtmlBuilder.append(StringTable.tableDiv(tengXunTable));
                            tengxunXmlBuilder.append(indent).append(indent).append("</Reports>\n");

                            List<String> cities = TableGeographyCode.chineseCities(province);
                            Platform.runLater(() -> {
                                logsTengxunArea.appendText(province + " " + cities + "\n");
                            });
                            for (String city : cities) {
                                if (isCancelled()) {
                                    return false;
                                }
                                url = new URL(tengxunAddress + "province=" + URLEncoder.encode(message("zh", provinceKey), "UTF-8")
                                        + "&city=" + URLEncoder.encode(city, "UTF-8"));
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
                                reports = tengXunReadCity(province, city, pageFile);
                                tengXunReports.addAll(reports);

                                tengxunXmlBuilder.append(indent).append(indent).append("<City ")
                                        .append(" level=\"").append(message("City")).append("\" ")
                                        .append(" province=\"").append(province).append("\" ")
                                        .append(" city=\"").append(city).append("\">\n");

                                tengXunTable = new StringTable(names, title + " " + province + " " + city);
                                for (EpidemicReport report : reports) {
                                    if (isCancelled()) {
                                        return false;
                                    }
                                    List<String> row = new ArrayList<>();
                                    row.addAll(Arrays.asList(DateTools.datetimeToString(report.getTime()),
                                            report.getLevel(), report.getProvince(),
                                            report.getCity() != null ? report.getCity() : "",
                                            report.getConfirmed() + "", report.getHealed() + "", report.getDead() + "",
                                            report.getLongitude() >= -180 ? report.getLongitude() + "" : "",
                                            report.getLatitude() >= -180 ? report.getLatitude() + "" : ""
                                    ));
                                    tengXunTable.add(row);

                                    tengxunXmlBuilder.append(indent).append(indent).append(indent).append(indent).append("<Report ")
                                            .append(" time=\"").append(DateTools.datetimeToString(report.getTime())).append("\" ")
                                            .append(" confirmed=\"").append(report.getConfirmed()).append("\" ")
                                            .append(" healed=\"").append(report.getHealed()).append("\" ")
                                            .append(" dead=\"").append(report.getDead()).append("\" ");
                                    if (report.getLongitude() >= -180) {
                                        tengxunXmlBuilder.append(" longtitude=\"").append(report.getLongitude()).append("\" ")
                                                .append(" latitude=\"").append(report.getLatitude()).append("\"/>\n");
                                    } else {
                                        tengxunXmlBuilder.append("/>\n");
                                    }

                                }
                                tengxunXmlBuilder.append(indent).append(indent).append("</City>\n");
                                tengXunHtmlBuilder.append(StringTable.tableDiv(tengXunTable));
                            }
                            tengxunXmlBuilder.append(indent).append("</Province>\n");

                        }

                        tengxunXmlBuilder.append("</Reports>\n");

                        tengXunHtml = HtmlTools.html(message("NewCoronavirusPneumonia"), tengXunHtmlBuilder.toString());

                        return true;

                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    webEngineTengXun.loadContent(tengXunHtml);
                    xmlAreaTengXun.setText(tengxunXmlBuilder.toString());
                    tengxunXmlBuilder = null;
                    jsonAreaTengXun.setText("{\"EpidemicReports\":\"" + title + "\": \"reports\":[\n\n");
                    jsonAreaTengXun.appendText(tengxunJsonBuilder.toString());
                    jsonAreaTengXun.appendText("\n]}");
                    tengxunJsonBuilder = null;
                    dataImportTengXunButton.setDisable(false);
                    saveAsTengXunButton.setDisable(false);
                    tengxunPane.getSelectionModel().select(tengxunHtmlTab);
                }
            };
            loadingController = openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public List<EpidemicReport> tengXunReadProvince(String province,
            File file) {
        final List<EpidemicReport> provinceReports = new ArrayList<>();
        try {
            String data = FileTools.readTexts(file);
            String json = data.replace("\"ret\":0,\"info\":\"\"",
                    "\"province\":\"" + province + "\"") + "\n";

            GeographyCode code = TableGeographyCode.readChineseProvince(province);
            while (true) {
                EpidemicReport report = create().setDataSet(message("NewCoronavirusPneumonia"))
                        .setLevel(message("Province")).setCountry(message("China"));
                if (code != null && code.getLongitude() >= -180) {
                    report.setProvince(code.getAddress())
                            .setLongitude(code.getLongitude()).setLatitude(code.getLatitude());
                } else {
                    report.setProvince(province)
                            .setLongitude(-200).setLatitude(-200);
                }

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
                report.setTime(DateTools.stringToDatetime("2020-" + month + "-" + day + " 13:00:00").getTime());

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
//                report.setCountry(data.substring(0, endPos));

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
//                report.setProvince(data.substring(0, endPos));

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
                provinceReports.add(report);
            }

            if (!provinceReports.isEmpty()) {
                if (tengxunJsonBuilder.length() > 0) {
                    tengxunJsonBuilder.append(",\n");
                }
                tengxunJsonBuilder.append(json);
            }

            if (loadingController != null) {
                Platform.runLater(() -> {
                    int size = provinceReports.size();
                    loadingController.setInfo(province + " " + size);
                    logsTengxunArea.appendText(province + " " + size + "\n");
                });
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return provinceReports;
    }

    public List<EpidemicReport> tengXunReadCity(String province, String city,
            File file) {
        final List<EpidemicReport> citiesReports = new ArrayList<>();
        try {
            String data = FileTools.readTexts(file);
            String json = ",\n" + data.replace("\"ret\":0,\"info\":\"\"",
                    "\"province\":\"" + province + "\",\"city\":\"" + city + "\"");

            GeographyCode code = TableGeographyCode.readChineseCity(province, city);
            while (true) {
                EpidemicReport cityReport = create().setDataSet(message("NewCoronavirusPneumonia"))
                        .setLevel(message("City")).setCountry(message("China")).setProvince(province);
                if (code != null && code.getLongitude() >= -180) {
                    cityReport.setCity(code.getAddress())
                            .setLongitude(code.getLongitude()).setLatitude(code.getLatitude());
                } else {
                    cityReport.setCity(city)
                            .setLongitude(-200).setLatitude(-200);
                }
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
                cityReport.setTime(DateTools.stringToDatetime("2020-" + month + "-" + day + " 13:00:00").getTime());

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
//                cityReport.setCity(data.substring(0, endPos));

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
                citiesReports.add(cityReport);

            }
            if (!citiesReports.isEmpty()) {
                tengxunJsonBuilder.append(json);
            }
            if (loadingController != null) {
                Platform.runLater(() -> {
                    int size = citiesReports.size();
                    loadingController.setInfo(province + " " + city + "  " + size);
                    logsTengxunArea.appendText(province + " " + city + "  " + size + "\n");
                });
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return citiesReports;
    }

    @FXML
    public void popTengXunPath(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Html);
            }

            @Override
            public void handleSelect() {
                saveTengXunAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void saveTengXunAction() {
        if (tengXunReports == null || tengXunReports.isEmpty()) {
            return;
        }
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                message("NewCoronavirusPneumonia") + "_historical"
                + DateTools.datetimeToString(new Date()).replace(":", "-") + ".html",
                CommonFxValues.TextExtensionFilter, false);
        if (file == null) {
            return;
        }
        recordFileWritten(file, targetPathKey, VisitHistory.FileType.Html, VisitHistory.FileType.Html);

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        FileTools.writeFile(file, tengXunHtml);
                        String filename = FileTools.replaceFileSuffix(file.getAbsolutePath(), "json");
                        FileTools.writeFile(new File(filename), jsonAreaTengXun.getText());
                        filename = FileTools.replaceFileSuffix(file.getAbsolutePath(), "xml");
                        FileTools.writeFile(new File(filename), xmlAreaTengXun.getText());
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    browseURI(file.getParentFile().toURI());
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void importTengXunAction() {
        if (tengXunReports == null || tengXunReports.isEmpty()) {
            return;
        }

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        TableEpidemicReport.write(tengXunReports);
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (parent != null) {
                        parent.getMyStage().toFront();
                        parent.loadTree();
                    } else {
                        openStage(CommonValues.EpidemicReportsFxml);
                    }
//                    closeStage();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
