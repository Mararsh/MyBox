package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Modality;
import mara.mybox.controller.EpidemicReportsController.ChartsType;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class EpidemicReportMapController extends LocationMapBaseController {

    protected EpidemicReportsController parent;
    protected int showCount, interval, mapSize;
    protected List<EpidemicReport> reports;

    @FXML
    protected ComboBox<String> intervalSelector, mapSizeSelector;
    @FXML
    protected HBox intervalBox;

    public EpidemicReportMapController() {
        baseTitle = AppVariables.message("EpidemicReportMap");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            interval = 1000;
            intervalSelector.getItems().addAll(Arrays.asList(
                    "1000", "1500", "2000", "3000", "800", "500", "5000", "200", "300", "10000"
            ));
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        setInterval();
                    });
            intervalSelector.setValue(AppVariables.getUserConfigValue("EpidemicReportMapInterval", "1000"));

            mapSize = 3;
            mapSizeSelector.getItems().addAll(Arrays.asList(
                    "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"
            ));
            mapSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        mapSize = Integer.valueOf(mapSizeSelector.getValue());
                    });
            mapSizeSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("EpidemicReportMapSize", "3"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setInterval() {
        try {
            int v = Integer.valueOf(intervalSelector.getValue());
            if (v > 0) {
                interval = v;
                AppVariables.setUserConfigInt(" EpidemicReportMapInterval", interval);
                FxmlControl.setEditorNormal(intervalSelector);
                drawTimeBasedMap();
            } else {
                FxmlControl.setEditorBadStyle(intervalSelector);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void clearAction() {
        webEngine.executeScript("clearMap();");
        if (timer != null) {
            timer.cancel();
        }
    }

    protected void snapMap() {
        if (parent == null) {
            return;
        }
        parent.tabPane.getSelectionModel().select(parent.mapTab);
        List<Image> snapshots = new ArrayList();
        double scale = FxmlControl.dpiScale();
        SnapshotParameters snapPara = new SnapshotParameters();
        snapPara.setFill(Color.TRANSPARENT);
        snapPara.setTransform(Transform.scale(scale, scale));

        Bounds bounds = webView.getLayoutBounds();
        int imageWidth = (int) Math.round(bounds.getWidth() * scale);
        int imageHeight = (int) Math.round(bounds.getHeight() * scale);

        if (parent.chartsType == ChartsType.LocationBased) {
            LoadingController loading = parent.openHandlingStage(Modality.WINDOW_MODAL);
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        snapshots.add(webView.snapshot(snapPara, new WritableImage(imageWidth, imageHeight)));
                        parent.makeHtml(snapshots);
                        loading.closeStage();
                        timer.cancel();
                    });
                }
            }, 200);

        } else if (parent.chartsType == ChartsType.TimeBasedMap) {
            if (reports.isEmpty()
                    || reports.get(reports.size() - 1).getConfirmed() <= 0) {
                return;
            }
            if (timer != null) {
                timer.cancel();
            }
            clearAction();
            if (reports == null || reports.isEmpty()
                    || reports.get(reports.size() - 1).getConfirmed() <= 0) {
                return;
            }
            intervalBox.setVisible(true);
            showCount = 0;
            if (interval <= 0) {
                interval = 1000;
            }
            float unit = 300f / reports.get(reports.size() - 1).getConfirmed();
            LoadingController loading = parent.openHandlingStage(Modality.WINDOW_MODAL);
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        EpidemicReport report = reports.get(showCount);
                        drawTimeBasedMap(report, unit);
                        snapshots.add(webView.snapshot(snapPara, new WritableImage(imageWidth, imageHeight)));
                        showCount++;
                        if (showCount >= reports.size()) {
                            loading.closeStage();
                            parent.makeHtml(snapshots);
                            timer.cancel();
                            drawTimeBasedMap();
                        }
                    });
                }
            }, 0, 200);
        }

    }

    protected String locationLabel(EpidemicReport report) {
        if (message("City").equals(report.getLevel())) {
            return report.getCity();
        } else if (message("Province").equals(report.getLevel())) {
            return report.getProvince();
        } else if (message("Country").equals(report.getLevel())) {
            return report.getCountry();
        } else if (message("City").equals(report.getLevel())) {
            return report.getCity();
        } else {
            return message("Global");
        }
    }

    protected void drawLocationBasedMap(int mapLevel,
            List<EpidemicReport> reports) {
        try {
            this.mapSize = mapLevel;
            clearAction();
            intervalBox.setVisible(false);
            if (reports == null || reports.isEmpty()) {
                return;
            }
            for (EpidemicReport report : reports) {
                if (report.getLongitude() >= -180 && report.getLongitude() <= 180
                        && report.getLatitude() >= -90 && report.getLatitude() <= 90) {
                    String label = "<div><font color=\"black\">" + locationLabel(report) + "</font>";
                    if (report.getConfirmed() > 0) {
                        label += " <font color=\"blue\">" + report.getConfirmed() + "</font>";
                        if (report.getHealed() > 0) {
                            label += " <font color=\"red\">" + report.getHealed() + "</font> ";
                        }
                        if (report.getDead() > 0) {
                            label += " <font color=\"black\">" + report.getDead() + "</font> ";
                        }
                    }
                    label += "</div>";
                    String image = StringTools.replaceAll(locationImage(), "\\", "/");
                    String info = label + "</br>";
                    GeographyCode code = GeographyCode.query(report.getLongitude(), report.getLatitude());
                    if (code != null) {
                        info += code.geography("</br>");
                    } else {
                        info += report.getLongitude() + "," + report.getLatitude();
                    }
                    webEngine.executeScript("addMarker(" + report.getLongitude() + "," + report.getLatitude()
                            + ", " + 20 + ", '" + label + "', '" + info + "', '" + image + "', "
                            + mapSize + ", true);");
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void drawTimeBasedMap(int mapLevel, List<EpidemicReport> reports) {
        this.mapSize = mapLevel;
        this.reports = reports;
        drawTimeBasedMap();
    }

    protected void drawTimeBasedMap() {
        try {
            clearAction();
            if (reports == null || reports.isEmpty()
                    || reports.get(reports.size() - 1).getConfirmed() <= 0) {
                return;
            }
            intervalBox.setVisible(true);
            showCount = 0;
            if (interval <= 0) {
                interval = 1000;
            }
            float unit = 300f / reports.get(reports.size() - 1).getConfirmed();
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        EpidemicReport report = reports.get(showCount);
                        drawTimeBasedMap(report, unit);
                        showCount++;
                        if (showCount >= reports.size()) {
                            showCount = 0;
                        }
                    });
                }
            }, 0, interval);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void drawTimeBasedMap(EpidemicReport report, float unit) {
        try {
            if (report == null
                    || report.getLongitude() < -180 || report.getLongitude() > 180
                    || report.getLatitude() < -90 || report.getLatitude() > 90) {
                return;
            }

            String label = DateTools.datetimeToString(report.getTime()) + "</br>"
                    + "<font color=\"black\">" + locationLabel(report) + "</font></br>";
            label += "<font color=\"blue\">" + AppVariables.message("Confirmed") + ": "
                    + report.getConfirmed() + "</font>"
                    + "<DIV style=\"width: " + (report.getConfirmed() * unit)
                    + "px;  background-color:blue; \">&nbsp;&nbsp;&nbsp;</DIV>"
                    + report.getConfirmed() + "</font></br>";
            label += " <font color=\"red\">" + AppVariables.message("Healed") + ": "
                    + report.getHealed() + "</font>"
                    + "<DIV style=\"width: " + (report.getHealed() * unit)
                    + "px;  background-color:red; \">&nbsp;&nbsp;&nbsp;</DIV>";
            label += " <font color=\"black\">" + AppVariables.message("Dead") + ": "
                    + report.getDead() + "</font> "
                    + "<DIV style=\"width: " + (report.getDead() * unit)
                    + "px;  background-color:black; \">&nbsp;&nbsp;&nbsp;</DIV>";
            String image = StringTools.replaceAll(locationImage(), "\\", "/");
            String info = label + "</br>";
            GeographyCode code = GeographyCode.query(report.getLongitude(), report.getLatitude());
            if (code != null) {
                info += code.geography("</br>");
            } else {
                info += report.getLongitude() + "," + report.getLatitude();
            }
            webEngine.executeScript("addMarker(" + report.getLongitude() + "," + report.getLatitude()
                    + ", " + 20 + ", '" + label + "', '" + info + "', '" + image + "', "
                    + mapSize + ", false);");

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

}
