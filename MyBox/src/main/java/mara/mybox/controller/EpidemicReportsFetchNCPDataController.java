package mara.mybox.controller;

import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import mara.mybox.data.EpidemicReport;
import static mara.mybox.data.EpidemicReport.create;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.NewCoronavirusPneumonia;
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
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-16
 * @License Apache License Version 2.0
 */
public class EpidemicReportsFetchNCPDataController extends BaseController {

    protected EpidemicReportsController parent;
    protected Date baiduReportTime;
    protected List<EpidemicReport> baiduReports, tenentReports;
    protected Map<EpidemicReport, List<EpidemicReport>> baiduCitiesReports;
    protected StringTable baiduTable;
    protected WebEngine webEngineBaidu, webEngineTencent;
    protected StringBuilder tengxunJsonBuilder;
    protected String baiduAddress, tencentAddress;
    protected LoadingController loadingController;
    protected File tencentHtmlFile;

    @FXML
    protected WebView webViewBaidu, webViewTencent;
    @FXML
    protected TextArea jsonAreaBaidu, xmlAreaBaidu, jsonAreaTencent, xmlAreaTencent, logsTencentArea;
    @FXML
    protected Button dataImportBaiduButton, dataImportTencentButton,
            saveAsBaiduButton, saveAsTencentButton, catButton;
    @FXML
    protected TabPane tengxunPane;
    @FXML
    protected Tab tencentHtmlTab, tencentLogsTab;
    @FXML
    protected TextField tencentInput, baiduInput;

    public EpidemicReportsFetchNCPDataController() {
        baseTitle = AppVariables.message("FetchNCPData");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            webEngineBaidu = webViewBaidu.getEngine();
            webEngineBaidu.setJavaScriptEnabled(true);

            webEngineTencent = webViewTencent.getEngine();
            webEngineTencent.setJavaScriptEnabled(true);

            dataImportBaiduButton.setDisable(true);
            saveAsBaiduButton.setDisable(true);
            dataImportTencentButton.setDisable(true);
            saveAsTencentButton.setDisable(true);

            tencentAddress = NewCoronavirusPneumonia.TencentAddress;
            baiduAddress = NewCoronavirusPneumonia.BaiduAddress;

            tencentInput.textProperty().addListener((ObservableValue<? extends String> ov, String oldv, String newv) -> {
                String v = tencentInput.getText().trim();
                if (v.isBlank()) {
                    return;
                }
                tencentAddress = v;
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

                        Map<String, Object> baiduData = NewCoronavirusPneumonia.readDaiduData(pageFile);
                        baiduReportTime = (Date) baiduData.get("time");
                        baiduReports = (List<EpidemicReport>) baiduData.get("areaReports");
                        baiduCitiesReports = (Map<EpidemicReport, List<EpidemicReport>>) baiduData.get("citiesReportsMap");

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
                                    report.getLatitude() >= -90 ? report.getLatitude() + "" : ""
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
                                            cityReport.getLatitude() >= -90 ? cityReport.getLatitude() + "" : ""
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
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory);

            String filePrefix = directory.getAbsolutePath() + File.separator
                    + message("NewCoronavirusPneumonia") + "_" + message("IntimeData") + "_"
                    + DateTools.datetimeToString(baiduReportTime.getTime()).replace(":", "-");

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        try {
                            FileTools.writeFile(new File(filePrefix + ".html"), baiduTable.html());
                            FileTools.writeFile(new File(filePrefix + ".json"), jsonAreaBaidu.getText());
                            FileTools.writeFile(new File(filePrefix + ".xml"), xmlAreaBaidu.getText());

                            List<EpidemicReport> allReports = new ArrayList();
                            allReports.addAll(baiduReports);
                            for (EpidemicReport report : baiduReports) {
                                List<EpidemicReport> citiesReports = baiduCitiesReports.get(report);
                                if (citiesReports == null || citiesReports.isEmpty()) {
                                    continue;
                                }
                                allReports.addAll(citiesReports);
                            }
                            EpidemicReport.writeExcel(new File(filePrefix + ".xlsx"), allReports);
                            EpidemicReport.writeTxt(new File(filePrefix + ".txt"), allReports);
                            return true;
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        popSuccessful();
                        browseURI(directory.toURI());
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void tencentAction() {
        tengxunPane.getSelectionModel().select(tencentLogsTab);
        logsTencentArea.clear();
        jsonAreaTencent.clear();
        xmlAreaTencent.clear();
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private StringBuilder tengxunXmlBuilder, tengXunHtmlBuilder;
                private String title;

                @Override
                protected boolean handle() {
                    try {
                        GeographyCode.importCodes();

                        tenentReports = new ArrayList();
                        tengxunJsonBuilder = new StringBuilder();
                        tengXunHtmlBuilder = new StringBuilder();

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
                                : GeographyCode.chineseProvincesKeys()) {
                            if (isCancelled()) {
                                return false;
                            }
                            String province = message(provinceKey);
                            URL url = new URL(tencentAddress + "province="
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

                            List<EpidemicReport> reports = readTencentProvince(province, pageFile);
                            tenentReports.addAll(reports);
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
                                        report.getLatitude() >= -90 ? report.getLatitude() + "" : ""
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
                                logsTencentArea.appendText(province + " " + cities + "\n");
                            });
                            for (String city : cities) {
                                if (isCancelled()) {
                                    return false;
                                }
                                url = new URL(tencentAddress
                                        + "province=" + URLEncoder.encode(message("zh", provinceKey), "UTF-8")
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
                                reports = readTencentCity(province, city, pageFile);
                                tenentReports.addAll(reports);

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
                                            report.getLatitude() >= -90 ? report.getLatitude() + "" : ""
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

                        return true;

                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    String tengXunHtml = HtmlTools.html(message("NewCoronavirusPneumonia"),
                            tengXunHtmlBuilder.toString());
                    webEngineTencent.loadContent(tengXunHtml);
                    tencentHtmlFile = FileTools.getTempFile();
                    FileTools.writeFile(tencentHtmlFile, tengXunHtml);
                    tengXunHtmlBuilder = null;

                    xmlAreaTencent.setText(tengxunXmlBuilder.toString());
                    tengxunXmlBuilder = null;

                    jsonAreaTencent.setText("{\"EpidemicReports\":\"" + title + "\": \"reports\":[\n\n");
                    jsonAreaTencent.appendText(tengxunJsonBuilder.toString());
                    jsonAreaTencent.appendText("\n]}");
                    tengxunJsonBuilder = null;

                    dataImportTencentButton.setDisable(false);
                    saveAsTencentButton.setDisable(false);
                    tengxunPane.getSelectionModel().select(tencentHtmlTab);
                }
            };
            loadingController = openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public List<EpidemicReport> readTencentProvince(String province,
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
                    logsTencentArea.appendText(province + " " + size + "\n");
                });
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return provinceReports;
    }

    public List<EpidemicReport> readTencentCity(String province, String city,
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
                    logsTencentArea.appendText(province + " " + city + "  " + size + "\n");
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
                saveTencentAction();
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
    public void saveTencentAction() {
        if (tenentReports == null || tenentReports.isEmpty()) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory);

            String filePrefix = directory.getAbsolutePath() + File.separator
                    + message("NewCoronavirusPneumonia") + "_" + message("HistoricalData")
                    + "_2020-01-20__" + DateTools.datetimeToString(new Date()).replace(":", "-");

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        try {
                            if (tencentHtmlFile != null && tencentHtmlFile.exists()) {
                                Files.copy(tencentHtmlFile, new File(filePrefix + ".html"));
                            }
                            FileTools.writeFile(new File(filePrefix + ".json"), jsonAreaTencent.getText());
                            FileTools.writeFile(new File(filePrefix + ".xml"), xmlAreaTencent.getText());
                            EpidemicReport.writeExcel(new File(filePrefix + ".xlsx"), tenentReports);
                            EpidemicReport.writeTxt(new File(filePrefix + ".txt"), tenentReports);
                            return true;
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        popSuccessful();
                        browseURI(directory.toURI());
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void importTencentAction() {
        if (tenentReports == null || tenentReports.isEmpty()) {
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
                        TableEpidemicReport.write(tenentReports);
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
