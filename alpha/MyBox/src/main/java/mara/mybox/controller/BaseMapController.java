package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.GeoCoordinateSystem;
import mara.mybox.data.MapPoint;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-7-27
 * @License Apache License Version 2.0
 */
public class BaseMapController extends BaseController {

    protected MapOptionsController optionsController;

    protected String mapTitle, mapType, mapStyle, language;
    protected WebEngine webEngine;
    protected boolean mapLoaded, isGeodetic, isFitView,
            isPopInfo, isBold, isMarkLabel, isMarkCoordinate,
            showStandardLayer, showSatelliteLayer, showRoadLayer, showTrafficLayer,
            showZoomControl, showScaleControl, showTypeControl, showSymbolsControl;
    protected float standardOpacity, satelliteOpacity, roadOpacity, trafficOpacity;
    protected File markerImageFile;
    protected Color textColor;
    protected GeoCoordinateSystem coordinateSystem;
    protected int markerSize, textSize, mapZoom, interval;
    protected SimpleBooleanProperty loadNotify, drawNotify;
    protected List<MapPoint> mapPoints;

    @FXML
    protected WebView mapView;
    @FXML
    protected Label topLabel, titleLabel;
    @FXML
    protected VBox snapBox;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    public void initMap() {
        try (Connection conn = DerbyBase.getConnection()) {

            webEngine = mapView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            if (bottomLabel != null) {
                webEngine.setOnStatusChanged((WebEvent<String> ev) -> {
                    bottomLabel.setText(ev.getData());
                });
            }
            webEngine.setOnAlert((WebEvent<String> ev) -> {
                mapEvents(ev.getData());
            });

            loadNotify = new SimpleBooleanProperty();
            drawNotify = new SimpleBooleanProperty();
            interval = 1;
            mapLoaded = false;
            mapPoints = null;

            readMapType(conn);

            readPointOptions(conn);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        map events
     */
    public void mapEvents(String data) {
        try {
            if (bottomLabel != null) {
                bottomLabel.setText(data);
            }
            if (data.equals("Loaded")) {
                mapLoaded();
                return;
            } else if (data.startsWith("zoomSize:")) {
                int v = Integer.parseInt(data.substring("zoomSize:".length()));
                isSettingValues = true;
                setMapZoom(v);
                isSettingValues = false;
                return;
            }
            boolean isClicked = true;
            if (data.startsWith("click:")) {
                data = data.substring(6);
            } else if (data.startsWith("move:")) {
                data = data.substring(5);
                isClicked = false;
            } else {
                popInformation(data);
                return;
            }
            String[] values = data.split(",");
            double longitude = Double.parseDouble(values[0]);
            double latitude = Double.parseDouble(values[1]);
            if (isClicked) {
                mouseClick(longitude, latitude);
            } else {
                mouseMove(longitude, latitude);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void mapLoaded() {
        Platform.runLater(() -> {
            try (Connection conn = DerbyBase.getConnection()) {
                mapLoaded = true;
                readStandardLayer(conn);
                readSatelliteLayer(conn);
                readRoadLayer(conn);
                readTrafficLayer(conn);
                readFitView(conn);
                readLanguage(conn);
                readShowZoom(conn);
                readShowScale(conn);
                readShowType(conn);
                readShowSymbols(conn);
                readMapZoom(conn);
            } catch (Exception e) {
                MyBoxLog.error(e);
                return;
            }
            refreshAction();
        });
        Platform.requestNextPulse();
    }

    protected void mouseClick(double longitude, double latitude) {

    }

    protected void mouseMove(double longitude, double latitude) {

    }

    /*
        draw map
     */
    public void clearMap() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mapLoaded) {
            if (webEngine != null) {
                webEngine.getLoadWorker().cancel();
                webEngine.executeScript("clearMap();");
            }
        }
        if (topLabel != null) {
            topLabel.setText("");
        }
        if (titleLabel != null) {
            titleLabel.setText("");
        }
        if (bottomLabel != null) {
            bottomLabel.setText("");
        }
    }

    protected String jsString(String string) {
        return string == null ? "null"
                : "'" + StringTools.replaceHtmlLineBreak(string.replaceAll("'", AppValues.MyBoxSeparator)) + "'";
    }

    public void beforeDrawPoints(List<MapPoint> points) {
        clearMap();
        mapPoints = points;
        if (mapPoints != null && !mapPoints.isEmpty()) {
            if (topLabel != null) {
                topLabel.setText(message("DataNumber") + ":" + points.size());
            } else {
                bottomLabel.setText(message("DataNumber") + ":" + points.size());
            }
        }
        readPointOptions();
    }

    public void drawPoints(List<MapPoint> points) {
        if (!mapLoaded || points == null || points.isEmpty()) {
            return;
        }
        beforeDrawPoints(points);
        int size = points.size();
        if (interval <= 0) {
            interval = 1;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            private boolean frameEnd = true, centered = false;
            private int index = 0;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        if (!frameEnd || timer == null) {
                            return;
                        }
                        if (mapPoints == null || mapPoints.isEmpty()) {
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            return;
                        }
                        frameEnd = false;
                        MapPoint point = mapPoints.get(index);
                        drawPoint(point);
                        if (!centered) {
                            webEngine.executeScript("setCenter("
                                    + point.getLongitude() + ", " + point.getLatitude() + ");");
                            centered = true;
                        }
                        index++;
                        bottomLabel.setText(index + " / " + size);
                        if (index >= mapPoints.size()) {
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            applyFitView(true);
                        }
                        frameEnd = true;
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                });
                Platform.requestNextPulse();
            }

        }, 0, interval);
    }

    public void drawPoint(MapPoint point) {
        try {
            if (webEngine == null || !mapLoaded || point == null) {
                return;
            }
            double lo = point.getLongitude();
            double la = point.getLatitude();
            if (!LocationTools.validCoordinate(lo, la)) {
                return;
            }
            String label = point.getLabel();
            String pLabel = "";
            if (isMarkLabel) {
                pLabel += label;
            }
            if (isMarkCoordinate) {
                if (!pLabel.isBlank()) {
                    pLabel += "</BR>";
                }
                pLabel += lo + "," + la;
            }
            webEngine.executeScript("addMarker("
                    + lo + "," + la
                    + ", " + jsString(pLabel)
                    + ", " + jsString(isPopInfo ? point.getInfo() : null)
                    + ", '" + image().replaceAll("\\\\", "/") + "'"
                    + ", " + markSize()
                    + ", " + textSize()
                    + ", '" + FxColorTools.color2rgb(textColor()) + "'"
                    + ", " + isBold + ", " + isPopInfo + ");");
            titleLabel.setText(label);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void moveCenter(MapPoint point) {
        if (!mapLoaded || point == null) {
            return;
        }
        webEngine.executeScript("setCenter("
                + point.getLongitude() + ", " + point.getLatitude() + ");");
    }


    /*
        map options
     */
    public void readMapType(Connection conn) {
        try {
            mapType = UserConfig.getString(conn, baseName + "MapType", "GaoDe");
            isGeodetic = UserConfig.getBoolean(conn, baseName + "Geodetic", true);
            mapZoom = UserConfig.getInt(baseName + "MapZoom", 9);

            applyMapType(mapType, isGeodetic);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyMapType(String type) {
        applyMapType(type, isGeodetic);
    }

    public void setMapType(String type, boolean isGeo) {
        try {
            UserConfig.setString(baseName + "MapType", type);
            UserConfig.setBoolean(baseName + "Geodetic", isGeo);
            applyMapType(type, isGeo);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyMapType(String type, boolean isGeo) {
        try {
            mapType = type;
            isGeodetic = isGeo;
            if (isGaoDeMap()) {
                webEngine.loadContent(LocationTools.gaodeMap(mapZoom));
            } else {
                webEngine.load(LocationTools.tiandituFile(isGeodetic, mapZoom).toURI().toString());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isGaoDeMap() {
        return "GaoDe".equals(mapType);
    }

    public void readPointOptions() {
        try (Connection conn = DerbyBase.getConnection()) {
            readPointOptions(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void readPointOptions(Connection conn) {
        try {
            isFitView = UserConfig.getBoolean(conn, baseName + "FitView", true);
            isPopInfo = UserConfig.getBoolean(conn, baseName + "PopInfo", true);
            isBold = UserConfig.getBoolean(conn, baseName + "Bold", false);
            isMarkLabel = UserConfig.getBoolean(conn, baseName + "MarkerLabel", true);
            isMarkCoordinate = UserConfig.getBoolean(conn, baseName + "MarkerCoordinate", false);
            markerSize = UserConfig.getInt(conn, baseName + "MarkerSize", 24);
            textSize = UserConfig.getInt(conn, baseName + "TextSize", 12);
            mapZoom = UserConfig.getInt(conn, baseName + "MapZoom", 9);
            String v = UserConfig.getString(conn, baseName + "MarkerImageFile", null);
            if (v == null) {
                markerImageFile = this.pointImage();
            } else {
                markerImageFile = new File(v);
                if (!markerImageFile.exists() || !markerImageFile.isFile()) {
                    markerImageFile = this.pointImage();
                }
            }
            v = UserConfig.getString(conn, baseName + "TextColor", null);
            try {
                textColor = Color.web(v);
            } catch (Exception e) {
                textColor = Color.BLACK;
            }
            v = UserConfig.getString(conn, baseName + "CoordinateSystem", null);
            coordinateSystem = new GeoCoordinateSystem(v);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setMapTitle(String title) {
        mapTitle = title;
    }

    public void readLanguage(Connection conn) {
        try {
            language = UserConfig.getString(conn, baseName + "Language",
                    Languages.isChinese() ? "zh_cn" : "en");
            applyLanguage();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setLanguage(String lang) {
        language = lang;
        UserConfig.setString(baseName + "Language", lang);
        applyLanguage();
    }

    public void applyLanguage() {
        if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setLanguage(\"" + language + "\");");
    }

    public void readMapZoom(Connection conn) {
        mapZoom = UserConfig.getInt(baseName + "MapZoom", 9);
        applyMapZoom(mapZoom);
    }

    public void setMapZoom(int zoom) {
        if (zoom <= 0) {
            return;
        }
        mapZoom = zoom;
        UserConfig.setInt(baseName + "MapZoom", zoom);
        applyMapZoom(zoom);
    }

    public void applyMapZoom(int zoom) {
        if (!mapLoaded || isSettingValues || zoom <= 0) {
            return;
        }
        webEngine.executeScript("setZoom(" + zoom + ");");
        if (WindowTools.isRunning(optionsController)) {
            optionsController.takeMapZoom(zoom);
        }
    }

    public void readStandardLayer(Connection conn) {
        try {
            showStandardLayer = UserConfig.getBoolean(conn, baseName + "StandardLayer", true);
            standardOpacity = UserConfig.getFloat(conn, baseName + "StandardOpacity", 1f);
            applyStandardLayer();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setShowStandardLayer(boolean show) {
        showStandardLayer = show;
        UserConfig.setBoolean(baseName + "StandardLayer", show);
        applyStandardLayer();
    }

    public void setStandardOpacity(float opacity) {
        if (opacity <= 0) {
            return;
        }
        showStandardLayer = true;
        standardOpacity = opacity;
        UserConfig.setBoolean(baseName + "StandardLayer", true);
        UserConfig.setDouble(baseName + "StandardOpacity", standardOpacity);
        applyStandardLayer();
    }

    public void applyStandardLayer() {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (showStandardLayer) {
                if (standardOpacity >= 0 && standardOpacity <= 1) {
                    webEngine.executeScript("setStandardLayerOpacity(" + standardOpacity + ");");
                }
            } else {
                webEngine.executeScript("hideStandardLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void readSatelliteLayer(Connection conn) {
        try {
            showSatelliteLayer = UserConfig.getBoolean(conn, baseName + "SatelliteLayer", false);
            satelliteOpacity = UserConfig.getFloat(conn, baseName + "SatelliteOpacity", 1f);
            applySatelliteLayer();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setShowSatelliteLayer(boolean show) {
        showSatelliteLayer = show;
        UserConfig.setBoolean(baseName + "SatelliteLayer", show);
        applySatelliteLayer();
    }

    public void setSatelliteOpacity(float opacity) {
        if (opacity <= 0) {
            return;
        }
        showSatelliteLayer = true;
        satelliteOpacity = opacity;
        UserConfig.setBoolean(baseName + "SatelliteLayer", true);
        UserConfig.setDouble(baseName + "SatelliteOpacity", satelliteOpacity);
        applySatelliteLayer();
    }

    public void applySatelliteLayer() {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (showSatelliteLayer) {
                if (satelliteOpacity >= 0 && satelliteOpacity <= 1) {
                    webEngine.executeScript("setSatelliteLayerOpacity(" + satelliteOpacity + ");");
                }
            } else {
                webEngine.executeScript("hideSatelliteLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void readRoadLayer(Connection conn) {
        try {
            showRoadLayer = UserConfig.getBoolean(conn, baseName + "RoadLayer", false);
            roadOpacity = UserConfig.getFloat(conn, baseName + "RoadOpacity", 1f);
            applyRoadLayer();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setShowRoadLayer(boolean show) {
        showRoadLayer = show;
        UserConfig.setBoolean(baseName + "RoadLayer", show);
        applyRoadLayer();
    }

    public void setRoadOpacity(float opacity) {
        if (opacity <= 0) {
            return;
        }
        showRoadLayer = true;
        roadOpacity = opacity;
        UserConfig.setBoolean(baseName + "RoadLayer", true);
        UserConfig.setDouble(baseName + "RoadOpacity", roadOpacity);
        applyRoadLayer();
    }

    public void applyRoadLayer() {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (showRoadLayer) {
                if (roadOpacity >= 0 && roadOpacity <= 1) {
                    webEngine.executeScript("setRoadLayerOpacity(" + roadOpacity + ");");
                }
            } else {
                webEngine.executeScript("hideRoadLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void readTrafficLayer(Connection conn) {
        try {
            showTrafficLayer = UserConfig.getBoolean(conn, baseName + "TrafficLayer", false);
            trafficOpacity = UserConfig.getFloat(conn, baseName + "TrafficOpacity", 1f);
            applyTrafficLayer();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setShowTrafficLayer(boolean show) {
        showTrafficLayer = show;
        UserConfig.setBoolean(baseName + "TrafficLayer", show);
        applyTrafficLayer();
    }

    public void setTrafficOpacity(float opacity) {
        if (opacity <= 0) {
            return;
        }
        showTrafficLayer = true;
        trafficOpacity = opacity;
        UserConfig.setBoolean(baseName + "TrafficLayer", true);
        UserConfig.setDouble(baseName + "TrafficOpacity", trafficOpacity);
        applyTrafficLayer();
    }

    public void applyTrafficLayer() {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (showTrafficLayer) {
                if (trafficOpacity >= 0 && trafficOpacity <= 1) {
                    webEngine.executeScript("setTrafficLayerOpacity(" + trafficOpacity + ");");
                }
            } else {
                webEngine.executeScript("hideTrafficLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void readFitView(Connection conn) {
        isFitView = UserConfig.getBoolean(conn, baseName + "FitView", true);
        applyFitView(isFitView);
    }

    public void setIsFitView(boolean setTrue) {
        isFitView = setTrue;
        UserConfig.setBoolean(baseName + "FitView", setTrue);
        applyFitView(isFitView);
    }

    public void applyFitView(boolean setTrue) {
        if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
            return;
        }
        if (setTrue) {
            webEngine.executeScript("map.setFitView();");
        }
    }

    public void readShowZoom(Connection conn) {
        showZoomControl = UserConfig.getBoolean(conn, baseName + "ShowZoomControl", true);
        applyShowZoom();
    }

    public void setShowZoom(boolean show) {
        UserConfig.setBoolean(baseName + "ShowZoomControl", show);
        showZoomControl = show;
        applyShowZoom();
    }

    public void applyShowZoom() {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('zoom'," + showZoomControl + ");");
    }

    public void readShowScale(Connection conn) {
        showScaleControl = UserConfig.getBoolean(conn, baseName + "ShowScaleControl", true);
        applyShowScale();
    }

    public void setShowScale(boolean show) {
        showScaleControl = show;
        UserConfig.setBoolean(baseName + "ShowScaleControl", show);
        applyShowScale();
    }

    public void applyShowScale() {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('scale'," + showScaleControl + ");");
    }

    public void readShowType(Connection conn) {
        showTypeControl = UserConfig.getBoolean(conn, baseName + "ShowTypeControl", true);
        applyShowType();
    }

    public void setShowType(boolean show) {
        showTypeControl = show;
        UserConfig.setBoolean(baseName + "ShowTypeControl", show);
        applyShowType();
    }

    public void applyShowType() {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('mapType'," + showTypeControl + ");");
    }

    public void readShowSymbols(Connection conn) {
        showSymbolsControl = UserConfig.getBoolean(conn, baseName + "ShowSymbolsControl", false);
        applyShowSymbols();
    }

    public void setShowSymbols(boolean show) {
        showSymbolsControl = show;
        UserConfig.setBoolean(baseName + "ShowSymbolsControl", show);
        applyShowSymbols();
    }

    public void applyShowSymbols() {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('symbols'," + showSymbolsControl + ");");
    }

    public void readMapStyle(Connection conn) {
        try {
            if (!mapLoaded || isSettingValues) {
                return;
            }
            mapStyle = UserConfig.getString(conn, baseName + "MapStyle", "default");
            if (mapStyle != null) {
                webEngine.executeScript("setStyle(\"" + mapStyle + "\");");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setMapStyle(String mapStyle) {
        UserConfig.setString(baseName + "MapStyle", mapStyle);
    }

    public void applyMinLevel() {
        if (isSettingValues) {
            return;
        }
        applyMapZoom(isGaoDeMap() ? 3 : 1);
    }

    /*
        map points options
     */
    public void setMarkerImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        markerImageFile = file;
        recordFileOpened(file, VisitHistory.FileType.Image);
        UserConfig.setString(baseName + "MarkerImageFile", markerImageFile.getAbsolutePath());
        refreshAction();
    }

    public void setMarkerSize(int size) {
        if (markerSize <= 0) {
            return;
        }
        markerSize = size;
        UserConfig.setInt(baseName + "MarkerSize", markerSize);
        refreshAction();
    }

    public void setTextSize(int size) {
        if (size <= 0) {
            return;
        }
        textSize = size;
        UserConfig.setInt(baseName + "TextSize", textSize);
        refreshAction();
    }

    public void setTextColor(Color color) {
        if (color == null) {
            color = Color.BLACK;
        }
        textColor = color;
        UserConfig.setString(baseName + "TextColor", textColor.toString());
        refreshAction();
    }

    public void setMarkerLabel(boolean markerLabel) {
        isMarkLabel = markerLabel;
        UserConfig.setBoolean(baseName + "MarkerLabel", markerLabel);
        refreshAction();
    }

    public void setMarkerCoordinate(boolean markerCoordinate) {
        isMarkCoordinate = markerCoordinate;
        UserConfig.setBoolean(baseName + "MarkerCoordinate", markerCoordinate);
        refreshAction();
    }

    public void setBold(boolean bold) {
        isBold = bold;
        UserConfig.setBoolean(baseName + "Bold", bold);
        refreshAction();
    }

    public void setPopInfo(boolean popInfo) {
        isPopInfo = popInfo;
        UserConfig.setBoolean(baseName + "PopInfo", popInfo);
        refreshAction();
    }

    public String image() {
        if (markerImageFile == null) {
            markerImageFile = pointImage();
        }
        return markerImageFile.getAbsolutePath();
    }

    public File pointImage() {
        if (AppVariables.ControlColor == StyleData.StyleColor.Customize) {
            return new File(AppVariables.MyboxDataPath + "/buttons/iconLocation.png");
        } else {
            return FxFileTools.getInternalFile("/" + StyleTools.getIconPath() + "iconLocation.png", "map",
                    AppVariables.ControlColor.name() + "Point.png");
        }
    }

    public Color textColor() {
        if (textColor == null) {
            textColor = Color.BLACK;
        }
        return textColor;
    }

    public int markSize() {
        if (markerSize <= 0) {
            markerSize = 24;
        }
        return markerSize;
    }

    public int textSize() {
        if (textSize <= 0) {
            textSize = 12;
        }
        return textSize;
    }

    /*
        action
     */
    @FXML
    public void optionsAction() {
        if (WindowTools.isRunning(optionsController)) {
            return;
        }
        optionsController = MapOptionsController.open(this);
    }

    @FXML
    @Override
    public void clearAction() {
        clearMap();
    }

    @FXML
    @Override
    public void refreshAction() {
        drawPoints(mapPoints);
    }

    protected String snapName() {
        String name = titleLabel.getText();
        if (name.isBlank()) {
            name = (message("Locations") + "_" + DateTools.datetimeToString(new Date()));
        }
        return FileNameTools.filter(name);
    }

    @FXML
    @Override
    public boolean popAction() {
        ImagePopController.openImage(this, snapMap());
        return true;
    }

    public Image snapMap() {
        return NodeTools.snap(snapBox);
    }

    @FXML
    public void htmlAction() {
        Image mapSnap = snapMap();
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    String title = mapTitle == null ? titleLabel.getText() : mapTitle;
                    if (title == null || title.isBlank()) {
                        title = message("Locations") + "_" + DateTools.datetimeToString(new Date());
                    }
                    StringBuilder s = new StringBuilder();
                    s.append("<h1  class=\"center\">").append(title).append("</h1>\n");
                    s.append("<hr>\n");

                    File imageFile = FileTmpTools.generateFile("jpg");
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(mapSnap, null);
                    ImageFileWriters.writeImageFile(this, bufferedImage, "jpg", imageFile.getAbsolutePath());
                    if (isCancelled()) {
                        return false;
                    }
                    s.append("<h2  class=\"center\">").append(message("Image")).append("</h2>\n");
                    s.append("<div align=\"center\"><img src=\"").append(imageFile.toURI().toString())
                            .append("\"  style=\"max-width:95%;\"></div>\n");
                    s.append("<hr>\n");

                    if (task == null || isCancelled()) {
                        return false;
                    }

                    if (mapPoints != null && !mapPoints.isEmpty()) {
                        List<String> names = new ArrayList<>();
                        names.addAll(Arrays.asList(message("Longitude"), message("Latitude"),
                                message("Label"), message("Information"), message("CoordinateSystem")));
                        StringTable table = new StringTable(names);
                        for (MapPoint code : mapPoints) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            table.add(code.htmlValues());
                        }
                        s.append(table.div());
                    }

                    html = HtmlWriteTools.html(title, HtmlStyles.styleValue("Default"), s.toString());
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                WebBrowserController.openHtml(html, true);
            }

        };
        start(task);
    }

    @FXML
    public void dataAction() {
        if (mapPoints == null || mapPoints.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private List<Data2DColumn> columns;
            private List<List<String>> data;

            @Override
            protected boolean handle() {
                try {
                    columns = new ArrayList<>();
                    columns.add(new Data2DColumn(message("Longitude"), ColumnType.Longitude));
                    columns.add(new Data2DColumn(message("Latitude"), ColumnType.Latitude));
                    columns.add(new Data2DColumn(message("Label"), ColumnType.String, 160));
                    columns.add(new Data2DColumn(message("Information"), ColumnType.String, 300));
                    columns.add(new Data2DColumn(message("CoordinateSystem"), ColumnType.String, 80));

                    data = new ArrayList<>();
                    for (MapPoint code : mapPoints) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        data.add(code.dataValues());
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                Data2DManufactureController.openData(
                        mapTitle == null ? titleLabel.getText() : mapTitle,
                        columns, data);
            }

        };
        start(task);
    }

    @FXML
    public void aboutCoordinateSystem() {
        openHtml(HelpTools.aboutCoordinateSystem());
    }

    @Override
    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            clearAction();
            if (webEngine != null) {
                webEngine = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();

    }

}
