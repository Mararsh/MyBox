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
public class MapController extends BaseController {

    protected String mapTitle, mapType;
    protected WebEngine webEngine;
    protected boolean mapLoaded, fitView, isPopInfo, isBold, isMarkLabel, isMarkCoordinate;
    protected File markerImageFile;
    protected Color textColor;
    protected GeoCoordinateSystem coordinateSystem;
    protected int markerSize, textSize, mapSize, interval;
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

    /*
        map
     */
    public void initMap() {
        try (Connection conn = DerbyBase.getConnection()) {

            webEngine = mapView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            webEngine.setOnAlert((WebEvent<String> ev) -> {
                mapEvents(ev.getData());
            });

            loadNotify = new SimpleBooleanProperty();
            drawNotify = new SimpleBooleanProperty();
            interval = 1;
            mapLoaded = false;
            mapPoints = null;
            mapType = UserConfig.getString(conn, baseName + "MapType", "tianditu");

            if (isGaoDeMap()) {
                webEngine.loadContent(LocationTools.gaodeMap());
            } else {
                boolean isGeodetic = UserConfig.getBoolean(conn, baseName + "Geodetic", true);
                webEngine.load(LocationTools.tiandituFile(isGeodetic).toURI().toString());
            }

            loadPointOptions(conn);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadPointOptions() {
        try (Connection conn = DerbyBase.getConnection()) {
            loadPointOptions(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadPointOptions(Connection conn) {
        try {
            fitView = UserConfig.getBoolean(conn, baseName + "FitView", true);
            isPopInfo = UserConfig.getBoolean(conn, baseName + "PopInfo", true);
            isBold = UserConfig.getBoolean(conn, baseName + "Bold", false);
            isMarkLabel = UserConfig.getBoolean(conn, baseName + "MarkerLabel", true);
            isMarkCoordinate = UserConfig.getBoolean(conn, baseName + "MarkerCoordinate", false);
            markerSize = UserConfig.getInt(conn, baseName + "MarkerSize", 24);
            textSize = UserConfig.getInt(conn, baseName + "TextSize", 12);
            mapSize = UserConfig.getInt(conn, baseName + "MapSize", 9);
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

    public void mapEvents(String data) {
        try {
//            MyBoxLog.console(data);
            if (bottomLabel != null) {
                bottomLabel.setText(data);
            }
            if (data.equals("Loaded")) {
                mapLoaded();
                return;
            } else if (data.startsWith("zoomSize:")) {
                int v = Integer.parseInt(data.substring("zoomSize:".length()));
                setMapSize(v);
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
            double longitude = Double.parseDouble(values[0]);
            double latitude = Double.parseDouble(values[1]);
            if (isClicked) {
                mapClicked(longitude, latitude);
            } else {
                mouseMoved(longitude, latitude);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void mapLoaded() {
        Platform.runLater(() -> {
            try (Connection conn = DerbyBase.getConnection()) {
                mapLoaded = true;
                if (isGaoDeMap()) {
                    applyStandardLayer(conn);
                    applySatelliteLayer(conn);
                    applyRoadLayer(conn);
                    applyTrafficLayer(conn);
                    applyFitView(conn);
                    applyLanguage(conn);
                } else {
                    applyShowZoom(conn);
                    applyShowScale(conn);
                    applyShowType(conn);
                    applyShowSymbols(conn);
                }
                applyMapSize(conn);
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        });
        Platform.requestNextPulse();
    }

    protected void mapClicked(double longitude, double latitude) {

    }

    protected void mouseMoved(double longitude, double latitude) {

    }

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

    public void setMapTitle(String title) {
        mapTitle = title;
    }

    public void applyLanguage(Connection conn) {
        try {
            if (!mapLoaded || isSettingValues || webEngine == null || !isGaoDeMap()) {
                return;
            }
            String language = UserConfig.getString(conn, baseName + "Language",
                    Languages.isChinese() ? "zh_cn" : "en");
            webEngine.executeScript("setLanguage(\"" + language + "\");");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyMapStyle(Connection conn) {
        try {
            if (!mapLoaded || isSettingValues || webEngine == null) {
                return;
            }
            String mapStyle = UserConfig.getString(conn, baseName + "MapStyle", "default");
            if (mapStyle != null) {
                webEngine.executeScript("setStyle(\"" + mapStyle + "\");");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyMapSize(Connection conn) {
        if (!mapLoaded || isSettingValues || webEngine == null) {
            return;
        }
        mapSize = UserConfig.getInt(baseName + "MapSize", 9);
        applyMapSize(mapSize);
    }

    public void applyMapSize(int size) {
        if (!mapLoaded || isSettingValues || webEngine == null || size <= 0) {
            return;
        }
        mapSize = size;
        webEngine.executeScript("setZoom(" + size + ");");
    }

    public void setMapSize(int size) {
        if (!mapLoaded || isSettingValues || webEngine == null) {
            return;
        }
        UserConfig.setInt(baseName + "MapSize", size);
        applyMapSize(size);
    }

    public void applyStandardLayer(Connection conn) {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (UserConfig.getBoolean(conn, baseName + "StandardLayer", true)) {
                float opacity = UserConfig.getFloat(conn, baseName + "StandardOpacity", 1f);
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setStandardLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideStandardLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applySatelliteLayer(Connection conn) {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (UserConfig.getBoolean(conn, baseName + "SatelliteLayer", false)) {
                float opacity = UserConfig.getFloat(conn, baseName + "SatelliteOpacity", 1f);
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setSatelliteLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideSatelliteLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyRoadLayer(Connection conn) {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (UserConfig.getBoolean(conn, baseName + "RoadLayer", false)) {
                float opacity = UserConfig.getFloat(conn, baseName + "RoadOpacity", 1f);
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setRoadLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideRoadLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyTrafficLayer(Connection conn) {
        try {
            if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
                return;
            }
            if (UserConfig.getBoolean(conn, baseName + "TrafficLayer", false)) {
                float opacity = UserConfig.getFloat(conn, baseName + "TrafficOpacity", 1f);
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setTrafficLayerOpacity(" + opacity + ");");
                }
            } else {
                webEngine.executeScript("hideTrafficLayer();");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyFitView(Connection conn) {
        if (!mapLoaded || isSettingValues || !isGaoDeMap()) {
            return;
        }
        fitView = UserConfig.getBoolean(conn, baseName + "FitView", true);
        applyFitView(fitView);
    }

    public void applyFitView(boolean setTrue) {
        fitView = setTrue;
        if (fitView) {
            webEngine.executeScript("map.setFitView();");
        }
    }

    public void setFitView(boolean setTrue) {
        UserConfig.setBoolean(baseName + "FitView", setTrue);
        applyFitView(setTrue);
    }

    public void applyShowZoom(Connection conn) {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('zoom',"
                + UserConfig.getBoolean(conn, baseName + "ShowZoomControl", true)
                + ");");
    }

    public void applyShowScale(Connection conn) {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('scale',"
                + UserConfig.getBoolean(conn, baseName + "ShowScaleControl", true)
                + ");");
    }

    public void applyShowType(Connection conn) {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('mapType',"
                + UserConfig.getBoolean(conn, baseName + "ShowTypeControl", true)
                + ");");
    }

    public void applyShowSymbols(Connection conn) {
        if (!mapLoaded || isSettingValues || isGaoDeMap()) {
            return;
        }
        webEngine.executeScript("setControl('symbols',"
                + UserConfig.getBoolean(conn, baseName + "ShowSymbolsControl", false)
                + ");");
    }

    public void applyMinLevel() {
        if (isSettingValues) {
            return;
        }
        applyMapSize(isGaoDeMap() ? 3 : 1);
    }

    public boolean isGaoDeMap() {
        return "GaoDe".equals(mapType);
    }


    /*
        data
     */
    public void setMarkerImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        markerImageFile = file;
        recordFileOpened(file, VisitHistory.FileType.Image);
        UserConfig.setString(baseName + "MarkerImageFile", markerImageFile.getAbsolutePath());
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
        loadPointOptions();
    }

    public void drawPoints(List<MapPoint> points) {
        if (!mapLoaded || webEngine == null || points == null || points.isEmpty()) {
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

    protected void drawPoint(MapPoint point) {
        if (webEngine == null || !mapLoaded || point == null) {
            return;
        }
        drawPoint(point.getLongitude(), point.getLatitude(),
                point.getLabel(), point.getInfo(), point.getMarkSize(),
                point.getMarkerImage(), point.getTextSize(), point.getTextColor(),
                point.isIsBold(), point.isIsPopInfo()
        );
    }

    protected void drawPoint(double lo, double la, String label, String info, int markSize,
            String markerImage, int textSize, Color textColor, boolean isBold, boolean isPopInfo) {
        try {
            if (webEngine == null || !mapLoaded
                    || !LocationTools.validCoordinate(lo, la)) {
                return;
            }
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
            String pImage = markerImage != null ? markerImage : image();
            Color pColor = textColor != null ? textColor : textColor();
            int mSize = markSize > 0 ? markSize : markSize();
            int tSize = textSize > 0 ? textSize : textSize();
            webEngine.executeScript("addMarker("
                    + lo + "," + la
                    + ", " + jsString(pLabel)
                    + ", " + jsString(isPopInfo ? info : null)
                    + ", '" + pImage.replaceAll("\\\\", "/") + "'"
                    + ", " + (mSize > 0 ? mSize : 24)
                    + ", " + (tSize > 0 ? tSize : 12)
                    + ", '" + FxColorTools.color2rgb(pColor) + "'"
                    + ", " + isBold + ", " + isPopInfo + ");");
            titleLabel.setText(label);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initPoints2(List<MapPoint> points) {
        if (!mapLoaded || webEngine == null || points == null || points.isEmpty()) {
            return;
        }
        beforeDrawPoints(points);
//        setMinLevel();
        applyFitView(false);
        mapPoints = points;
        if (mapPoints != null && !mapPoints.isEmpty()) {
            webEngine.executeScript("setCenter("
                    + mapPoints.get(0).getLongitude() + ", " + mapPoints.get(0).getLatitude() + ");");
        }
    }

    public void moveCenter(MapPoint point) {
        if (!mapLoaded || webEngine == null || point == null) {
            return;
        }
        webEngine.executeScript("setCenter("
                + point.getLongitude() + ", " + point.getLatitude() + ");");
    }

    /*
        action
     */
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
