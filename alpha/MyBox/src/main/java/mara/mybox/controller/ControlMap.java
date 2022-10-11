package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.MapOptions;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.SystemConfig;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-7-27
 * @License Apache License Version 2.0
 */
public class ControlMap extends BaseController {

    protected String title;
    protected WebEngine webEngine;
    protected boolean mapLoaded;
    protected MapOptions mapOptions;
    protected SimpleBooleanProperty drawNotify, dataNotify;

    @FXML
    protected WebView mapView;
    @FXML
    protected ControlMapOptions mapOptionsController;
    @FXML
    protected Label titleLabel;
    @FXML
    protected VBox snapBox;

    public ControlMap() {
        baseTitle = message("Map");

        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        targetExtensionFilter = FileFilters.HtmlExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            drawNotify = new SimpleBooleanProperty();
            dataNotify = new SimpleBooleanProperty();
            mapOptions = new MapOptions(this);

            initWebEngine();

            loadMap();

            initOptionsControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initOptionsControls() {
        if (mapOptionsController != null) {
            mapOptionsController.setParameters(this);
        }
    }

    public void checkFirstRun(BaseController parent) {
        try {
            this.parentController = parent;
            initSplitPanes();

            if (SystemConfig.getBoolean("MapRunFirstTime" + AppValues.AppVersion, true)) {
                HtmlPopController controller = HtmlPopController.openHtml(parentController, LocationTools.gaodeMap());
                controller.handling(message("FirstRunInfo"));
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            controller.loadAddress(LocationTools.tiandituFile(true).toURI().toString());
                        });
                    }
                }, 2000);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            SystemConfig.setBoolean("MapRunFirstTime" + AppValues.AppVersion, false);
                            controller.closeStage();
                            if (parentController != null) {
                                parentController.reload();
                            }
                        });
                    }
                }, 4000);
            }
        } catch (Exception e) {
        }
    }

    public void loadMap() {
        if (webEngine == null || isSettingValues) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mapLoaded = false;
        if (mapOptionsController != null) {
            mapOptionsController.optionsBox.setDisable(true);
        }
        if (mapOptions.isGaoDeMap()) {
            webEngine.loadContent(LocationTools.gaodeMap());
        } else {
            webEngine.load(LocationTools.tiandituFile(mapOptions.isIsGeodetic()).toURI().toString());
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!mapLoaded) {
                        return;
                    }
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    if (mapOptions.isGaoDeMap()) {
                        setLanguage();
                    } else {
                        webEngine.executeScript("setControl('zoom'," + mapOptions.isZoom() + ");");
                        webEngine.executeScript("setControl('scale'," + mapOptions.isScale() + ");");
                        webEngine.executeScript("setControl('mapType'," + mapOptions.isType() + ");");
                        webEngine.executeScript("setControl('symbols'," + mapOptions.isSymbols() + ");");
                    }
                    drawPoints();
                    if (mapOptionsController != null) {
                        mapOptionsController.optionsBox.setDisable(false);
                    }
                });
            }

        }, 0, 500);
    }

    public void setLanguage() {
        try {
            if (isSettingValues || mapOptions.getLanguage() == null) {
                return;
            }
            Platform.runLater(() -> {
                webEngine.executeScript("setLanguage(\"" + mapOptions.getLanguage() + "\");");
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setMapStyle() {
        try {
            if (isSettingValues || mapOptions.getMapStyle() == null) {
                return;
            }
            Platform.runLater(() -> {
                webEngine.executeScript("setStyle(\"" + mapOptions.getMapStyle() + "\");");
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setMapSize() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setZoom(" + mapOptions.getMapSize() + ");");
        });
        if (mapOptionsController != null) {
            mapOptionsController.setMapSize(mapOptions.getMapSize());
        }
    }

    public void setStandardLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (mapOptions.isStandardLayer()) {
                float opacity = mapOptions.getStandardOpacity();
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setStandardLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideStandardLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setSatelliteLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (mapOptions.isSatelliteLayer()) {
                float opacity = mapOptions.getSatelliteOpacity();
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setSatelliteLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideSatelliteLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setRoadLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (mapOptions.isRoadLayer()) {
                float opacity = mapOptions.getRoadOpacity();
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setRoadLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideRoadLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setTrafficLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (mapOptions.isTrafficLayer()) {
                float opacity = mapOptions.getTrafficOpacity();
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setTrafficLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideTrafficLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setShowZoom() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('zoom'," + mapOptions.isZoom() + ");");
        });
    }

    public void setShowScale() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('scale'," + mapOptions.isScale() + ");");
        });
    }

    public void setShowType() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('mapType'," + mapOptions.isType() + ");");
        });
    }

    public void setShowSymbols() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('symbols'," + mapOptions.isSymbols() + ");");
        });
    }

    public void setDataMax() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        dataNotify.set(!dataNotify.get());
    }

    protected void mapClicked(double longitude, double latitude) {

    }

    protected void mouseMoved(double longitude, double latitude) {

    }

    public void drawPoints() {
        drawNotify.set(!drawNotify.get());
    }

    protected String writePointsTable() {
        return "";
    }

    public void reloadData() {

    }

    public void initWebEngine() {
        try {
            if (mapView == null) {
                return;
            }
            mapLoaded = false;
            webEngine = mapView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            webEngine.setOnAlert((WebEvent<String> ev) -> {
                mapEvents(ev.getData());
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void mapEvents(String data) {
        try {
//            MyBoxLog.debug(data);
            if (data.equals("Loaded")) {
                mapLoaded = true;
                if (mapOptionsController != null) {
                    mapOptionsController.mapLoaded();
                    return;
                }
            } else if (data.startsWith("zoomSize:")) {
                int v = Integer.parseInt(data.substring("zoomSize:".length()));
                if (v != mapOptions.getMapSize()) {
                    if (mapOptionsController != null) {
                        mapOptionsController.setMapSize(v);
                    }
                    mapOptions.setMapSize(v);
                }
                return;
            }
            boolean isClicked = true;
            if (data.startsWith("click:")) {
                data = data.substring(6);
            } else if (data.startsWith("move:")) {
                data = data.substring(5);
                isClicked = false;
            } else {
                return;
            }
            String[] values = data.split(",");
            double longitude = Double.valueOf(values[0]);
            double latitude = Double.valueOf(values[1]);
            if (isClicked) {
                mapClicked(longitude, latitude);
            } else {
                mouseMoved(longitude, latitude);
            }
            if (bottomLabel != null) {
                bottomLabel.setText(data);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawPoint(GeographyCode code) {
        drawPoint(code.getLongitude(), code.getLatitude(), code.getLabel(), code.getInfo());
    }

    protected void drawPoint(double longitude, double latitude, String label, String info) {
        try {
            drawPoint(longitude, latitude, label, info,
                    mapOptions.getMarkerImageFile().getAbsolutePath(),
                    mapOptions.getMarkerSize(),
                    mapOptions.getTextColor());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawPoint(double longitude, double latitude,
            String label, String info, String markerImage, int markSize, Color textColor) {
        try {
            if (webEngine == null || !mapLoaded
                    || !LocationTools.validCoordinate(longitude, latitude)) {
                return;
            }
            String pLabel = "";
            if (mapOptions.isMarkerLabel()) {
                pLabel += label;
            }
            if (mapOptions.isMarkerCoordinate()) {
                if (!pLabel.isBlank()) {
                    pLabel += "</BR>";
                }
                pLabel += longitude + "," + latitude;
            }
            pLabel = jsString(pLabel);
            String pInfo = jsString(mapOptions.isPopInfo() ? info : null);
            String pImage = markerImage;
            pImage = (pImage == null || pImage.trim().isBlank())
                    ? "null" : "'" + pImage.replaceAll("\\\\", "/") + "'";
            String pColor = textColor == null ? "null" : "'" + FxColorTools.color2rgb(textColor) + "'";
            webEngine.executeScript("addMarker("
                    + longitude + "," + latitude
                    + ", " + pLabel + ", " + pInfo + ", " + pImage
                    + ", " + markSize
                    + ", " + mapOptions.getTextSize()
                    + ", " + pColor + ", " + mapOptions.isBold() + ");");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected String jsString(String string) {
        return string == null ? "null"
                : "'" + string.replaceAll("'", AppValues.MyBoxSeparator).replaceAll("\n", "</BR>") + "'";
    }

    @FXML
    @Override
    public void clearAction() {
        if (mapLoaded) {
            webEngine.executeScript("clearMap();");
        }
        if (titleLabel != null) {
            titleLabel.setText("");
        }
    }

    @FXML
    public void refreshAction() {
        drawPoints();
    }

    @FXML
    public void aboutCoordinateSystem() {
        try {
            StringTable table = new StringTable(null, Languages.message("AboutCoordinateSystem"));
            table.newLinkRow("ChinaCommonGeospatialInformationServices", "https://www.tianditu.gov.cn/");
            table.newLinkRow("", "https://www.tianditu.gov.cn/world_coronavirusmap/");
            table.newLinkRow("ChineseCoordinateSystems", "https://politics.stackexchange.com/questions/40991/why-must-chinese-maps-be-obfuscated");
            table.newLinkRow("", "https://zhuanlan.zhihu.com/p/62243160");
            table.newLinkRow("", "https://blog.csdn.net/qq_36377037/article/details/86479796");
            table.newLinkRow("", "https://www.zhihu.com/question/31204062?sort=created");
            table.newLinkRow("", "https://blog.csdn.net/ssxueyi/article/details/102622156");
            table.newLinkRow("EPSGCodes", "http://epsg.io/4490");
            table.newLinkRow("", "http://epsg.io/4479");
            table.newLinkRow("", "http://epsg.io/4326");
            table.newLinkRow("", "http://epsg.io/3857");
            table.newLinkRow("TrackingData", "https://www.microsoft.com/en-us/download/details.aspx?id=52367");
            table.newLinkRow("", "https://www.datarepository.movebank.org/discover");
            table.newLinkRow("", "https://sumo.dlr.de/docs/Data/Scenarios/TAPASCologne.html");
            table.newLinkRow("", "https://blog.csdn.net/souvenir001/article/details/52180335");
            table.newLinkRow("", "https://www.cnblogs.com/genghenggao/p/9625511.html");
            table.newLinkRow("TianDiTuAPI", "http://lbs.tianditu.gov.cn/api/js4.0/guide.html");
            table.newLinkRow("TianDiTuKey", "https://console.tianditu.gov.cn/api/key");
            table.newLinkRow("GaoDeAPI", "https://lbs.amap.com/api/javascript-api/summary");
            table.newLinkRow("GaoDeKey", "https://console.amap.com/dev/index");
            File htmFile = HtmlWriteTools.writeHtml(table.html());
            openLink(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected String snapName() {
        String name = titleLabel.getText();
        if (name.isBlank()) {
            name = (Languages.message("Locations") + "_" + DateTools.datetimeToString(new Date()));
        }
        name += "_dpi" + dpi;
        return FileNameTools.filter(name);
    }

    public void snapHtml() {
        final String htmlTitle = snapName();
        File htmlFile = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                htmlTitle, FileFilters.HtmlExtensionFilter);
        if (htmlFile == null) {
            return;
        }
        recordFileWritten(htmlFile, VisitHistory.FileType.Html);

        double scale = NodeTools.dpiScale(dpi);
        scale = scale > 1 ? scale : 1;
        SnapshotParameters snapPara = new SnapshotParameters();
        snapPara.setFill(Color.TRANSPARENT);
        snapPara.setTransform(Transform.scale(scale, scale));
        final Image mapSnap = snapBox.snapshot(snapPara, null);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        String subPath = FileNameTools.prefix(htmlFile.getName());
                        String path = htmlFile.getParent() + "/" + subPath;
                        (new File(path)).mkdirs();

                        StringBuilder s = new StringBuilder();
                        s.append("<h1  class=\"center\">").append(htmlTitle).append("</h1>\n");
                        s.append("<hr>\n");

                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(writePointsTable());
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(mapSnap, null);
                        ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "map.jpg");
                        String imageName = subPath + "/map.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\"></div>\n");
                        s.append("<hr>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        String html = HtmlWriteTools.html(htmlTitle, HtmlStyles.styleValue("Default"), s.toString());
                        TextFileTools.writeFile(htmlFile, html, Charset.forName("utf-8"));

                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return htmlFile.exists();
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.error(e.toString());
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    browseURI(htmlFile.toURI());
                }

            };
            start(task);
        }

    }

    @Override
    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (webEngine != null) {
                webEngine.getLoadWorker().cancel();
                webEngine = null;
            }
            if (mapOptionsController != null) {
                mapOptionsController.cleanPane();
            }

        } catch (Exception e) {
        }
        super.cleanPane();

    }

}
