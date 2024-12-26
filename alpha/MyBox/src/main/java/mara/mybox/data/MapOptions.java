package mara.mybox.data;

import java.io.File;
import java.sql.Connection;
import javafx.scene.paint.Color;
import mara.mybox.controller.MapController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-8-6
 * @License Apache License Version 2.0
 */
public class MapOptions {

    protected MapController mapController;
    protected String baseName, mapType, mapStyle, language;
    protected int markerSize, textSize, mapSize;
    protected boolean isSettingValues, isGeodetic, fitView, popInfo,
            standardLayer, satelliteLayer, roadLayer, trafficLayer,
            zoom, scale, type, symbols, bold, markerLabel, markerCoordinate;
    protected float standardOpacity, satelliteOpacity, roadOpacity, trafficOpacity;
    protected File markerImageFile;
    protected Color textColor;
    protected GeoCoordinateSystem coordinateSystem;

    public MapOptions(MapController mapController) {
        try {
            this.mapController = mapController;
            baseName = mapController.getBaseName();
            initValues();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public final void initValues() {
        try (Connection conn = DerbyBase.getConnection()) {
            mapType = UserConfig.getString(conn, baseName + "MapType", "Gaode");
            mapStyle = UserConfig.getString(conn, baseName + "MapStyle", "default");
            language = UserConfig.getString(conn, baseName + "Language", Languages.isChinese() ? "zh_cn" : "en");
            markerSize = UserConfig.getInt(conn, baseName + "MarkerSize", 24);
            textSize = UserConfig.getInt(conn, baseName + "TextSize", 12);
            mapSize = UserConfig.getInt(conn, baseName + "MapSize", 9);
            isGeodetic = UserConfig.getBoolean(conn, baseName + "Geodetic", true);
            fitView = UserConfig.getBoolean(conn, baseName + "FitView", true);
            popInfo = UserConfig.getBoolean(conn, baseName + "PopInfo", true);
            standardLayer = UserConfig.getBoolean(conn, baseName + "StandardLayer", true);
            satelliteLayer = UserConfig.getBoolean(conn, baseName + "SatelliteLayer", false);
            roadLayer = UserConfig.getBoolean(conn, baseName + "RoadLayer", false);
            trafficLayer = UserConfig.getBoolean(conn, baseName + "TrafficLayer", false);
            zoom = UserConfig.getBoolean(conn, baseName + "Zoom", true);
            scale = UserConfig.getBoolean(conn, baseName + "Scale", true);
            type = UserConfig.getBoolean(conn, baseName + "Type", true);
            symbols = UserConfig.getBoolean(conn, baseName + "Symbols", false);
            bold = UserConfig.getBoolean(conn, baseName + "Bold", false);
            markerLabel = UserConfig.getBoolean(conn, baseName + "MarkerLabel", true);
            markerCoordinate = UserConfig.getBoolean(conn, baseName + "MarkerCoordinate", false);
            standardOpacity = (float) UserConfig.getDouble(conn, baseName + "StandardOpacity", 1f);
            satelliteOpacity = (float) UserConfig.getDouble(conn, baseName + "SatelliteOpacity", 1f);
            roadOpacity = (float) UserConfig.getDouble(conn, baseName + "RoadOpacity", 1f);
            trafficOpacity = (float) UserConfig.getDouble(conn, baseName + "TrafficOpacity", 1f);
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

    public void initMap(String mapType) {
        try {
            this.mapType = mapType;
            if (isGaoDeMap()) {
                coordinateSystem = GeoCoordinateSystem.GCJ02();

            } else {
                coordinateSystem = GeoCoordinateSystem.CGCS2000();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isGaoDeMap() {
        return "GaoDe".equals(mapType);
    }

    public void drawPoints() {
        if (mapController == null || mapController.isIsSettingValues()) {
            return;
        }
        mapController.refreshAction();
    }

    public int textSize() {
        if (textSize <= 0) {
            textSize = 12;
        }
        return textSize;
    }

    public int markSize() {
        if (markerSize <= 0) {
            markerSize = 24;
        }
        return markerSize;
    }

    public String image() {
        if (markerImageFile == null) {
            markerImageFile = pointImage();
        }
        return markerImageFile.getAbsolutePath();
    }

    public Color textColor() {
        if (textColor == null) {
            textColor = Color.BLACK;
        }
        return textColor;
    }

    public File circleImage() {
        if (AppVariables.ControlColor == StyleData.StyleColor.Customize) {
            return new File(AppVariables.MyboxDataPath + "/buttons/iconCircle.png");
        } else {
            return FxFileTools.getInternalFile("/" + StyleTools.getIconPath() + "iconCircle.png", "map",
                    AppVariables.ControlColor.name() + "Circle.png");
        }
    }

    public File pointImage() {
        if (AppVariables.ControlColor == StyleData.StyleColor.Customize) {
            return new File(AppVariables.MyboxDataPath + "/buttons/iconLocation.png");
        } else {
            return FxFileTools.getInternalFile("/" + StyleTools.getIconPath() + "iconLocation.png", "map",
                    AppVariables.ControlColor.name() + "Point.png");
        }
    }

    public File chineseHistoricalCapitalsImage() {
        markerImageFile = FxFileTools.getInternalFile("/img/jade.png", "image", "jade.png");
        return markerImageFile;
    }

    public File europeanGadwallsImage() {
        markerImageFile = FxFileTools.getInternalFile("/img/Gadwalls.png", "image", "Gadwalls.png");
        return markerImageFile;
    }

    public File spermWhalesImage() {
        markerImageFile = FxFileTools.getInternalFile("/img/SpermWhale.png", "image", "SpermWhale.png");
        return markerImageFile;
    }

    /*
        get/set
     */
    public MapController getMapController() {
        return mapController;
    }

    public MapOptions setMapController(MapController mapController) {
        this.mapController = mapController;
        return this;
    }

    public String getBaseName() {
        return baseName;
    }

    public MapOptions setBaseName(String baseName) {
        this.baseName = baseName;
        return this;
    }

    public int getMarkerSize() {
        return markerSize;
    }

    public MapOptions setMarkerSize(int markerSize) {
        if (markerSize <= 0) {
            return this;
        }
        this.markerSize = markerSize;
        UserConfig.setInt(baseName + "MarkerSize", markerSize);
        drawPoints();
        return this;
    }

    public int getTextSize() {
        return textSize;
    }

    public MapOptions setTextSize(int textSize) {
        if (textSize <= 0) {
            return this;
        }
        this.textSize = textSize;
        UserConfig.setInt(baseName + "TextSize", textSize);
        drawPoints();
        return this;
    }

    public int getMapSize() {
        return mapSize;
    }

    public MapOptions setMapSize(int mapSize) {
        if (mapSize <= 0) {
            return this;
        }
        this.mapSize = mapSize;
        UserConfig.setInt(baseName + "MapSize", mapSize);
        if (mapController != null) {
//            mapController.setMapSize();
        }
        return this;
    }

    public boolean isFitView() {
        return fitView;
    }

    public MapOptions setFitView(boolean fitView) {
        this.fitView = fitView;
        UserConfig.setBoolean(baseName + "FitView", fitView);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setFitView();
        }
        return this;
    }

    public boolean isPopInfo() {
        return popInfo;
    }

    public MapOptions setPopInfo(boolean popInfo) {
        this.popInfo = popInfo;
        UserConfig.setBoolean(baseName + "PopInfo", popInfo);
        drawPoints();
        return this;
    }

    public boolean isStandardLayer() {
        return standardLayer;
    }

    public MapOptions setStandardLayer(boolean standardLayer) {
        this.standardLayer = standardLayer;
        UserConfig.setBoolean(baseName + "StandardLayer", standardLayer);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setStandardLayer();
        }
        return this;
    }

    public boolean isSatelliteLayer() {
        return satelliteLayer;
    }

    public MapOptions setSatelliteLayer(boolean satelliteLayer) {
        this.satelliteLayer = satelliteLayer;
        UserConfig.setBoolean(baseName + "SatelliteLayer", satelliteLayer);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setSatelliteLayer();
        }
        return this;
    }

    public boolean isRoadLayer() {
        return roadLayer;
    }

    public MapOptions setRoadLayer(boolean roadLayer) {
        this.roadLayer = roadLayer;
        UserConfig.setBoolean(baseName + "RoadLayer", roadLayer);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setRoadLayer();
        }
        return this;
    }

    public boolean isTrafficLayer() {
        return trafficLayer;
    }

    public MapOptions setTrafficLayer(boolean trafficLayer) {
        this.trafficLayer = trafficLayer;
        UserConfig.setBoolean(baseName + "TrafficLayer", trafficLayer);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setTrafficLayer();
        }
        return this;
    }

    public boolean isZoom() {
        return zoom;
    }

    public MapOptions setZoom(boolean zoom) {
        this.zoom = zoom;
        UserConfig.setBoolean(baseName + "Zoom", zoom);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setShowZoom();
        }
        return this;
    }

    public boolean isScale() {
        return scale;
    }

    public MapOptions setScale(boolean scale) {
        this.scale = scale;
        UserConfig.setBoolean(baseName + "Scale", scale);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setShowScale();
        }
        return this;
    }

    public boolean isType() {
        return type;
    }

    public MapOptions setType(boolean type) {
        this.type = type;
        UserConfig.setBoolean(baseName + "Type", type);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setShowType();
        }
        return this;
    }

    public boolean isSymbols() {
        return symbols;
    }

    public MapOptions setSymbols(boolean symbols) {
        this.symbols = symbols;
        UserConfig.setBoolean(baseName + "Symbols", symbols);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setShowSymbols();
        }
        return this;
    }

    public boolean isBold() {
        return bold;
    }

    public MapOptions setBold(boolean bold) {
        this.bold = bold;
        UserConfig.setBoolean(baseName + "Bold", bold);
        drawPoints();
        return this;
    }

    public boolean isMarkerLabel() {
        return markerLabel;
    }

    public MapOptions setMarkerLabel(boolean markerLabel) {
        this.markerLabel = markerLabel;
        UserConfig.setBoolean(baseName + "MarkerLabel", markerLabel);
        drawPoints();
        return this;
    }

    public boolean isMarkerCoordinate() {
        return markerCoordinate;
    }

    public MapOptions setMarkerCoordinate(boolean markerCoordinate) {
        this.markerCoordinate = markerCoordinate;
        UserConfig.setBoolean(baseName + "MarkerCoordinate", markerCoordinate);
        drawPoints();
        return this;
    }

    public float getStandardOpacity() {
        return standardOpacity;
    }

    public MapOptions setStandardOpacity(float standardOpacity) {
        if (standardOpacity <= 0) {
            return this;
        }
        standardLayer = true;
        this.standardOpacity = standardOpacity;
        UserConfig.setDouble(baseName + "StandardOpacity", standardOpacity);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setStandardLayer();
        }
        return this;
    }

    public float getSatelliteOpacity() {
        return satelliteOpacity;
    }

    public MapOptions setSatelliteOpacity(float satelliteOpacity) {
        if (satelliteOpacity <= 0) {
            return this;
        }
        satelliteLayer = true;
        this.satelliteOpacity = satelliteOpacity;
        UserConfig.setDouble(baseName + "SatelliteOpacity", satelliteOpacity);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setSatelliteLayer();
        }
        return this;
    }

    public float getRoadOpacity() {
        return roadOpacity;
    }

    public MapOptions setRoadOpacity(float roadOpacity) {
        if (roadOpacity <= 0) {
            return this;
        }
        roadLayer = true;
        this.roadOpacity = roadOpacity;
        UserConfig.setDouble(baseName + "RoadOpacity", roadOpacity);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setRoadLayer();
        }
        return this;
    }

    public float getTrafficOpacity() {
        return trafficOpacity;
    }

    public MapOptions setTrafficOpacity(float trafficOpacity) {
        if (trafficOpacity <= 0) {
            return this;
        }
        trafficLayer = true;
        this.trafficOpacity = trafficOpacity;
        UserConfig.setDouble(baseName + "TrafficOpacity", trafficOpacity);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setTrafficLayer();
        }
        return this;
    }

    public File getMarkerImageFile() {
        return markerImageFile;
    }

    public MapOptions setMarkerImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return this;
        }
        markerImageFile = file;
        mapController.recordFileOpened(file, VisitHistory.FileType.Image);
        UserConfig.setString(baseName + "MarkerImageFile", markerImageFile.getAbsolutePath());
        drawPoints();
        return this;
    }

    public String getMapType() {
        return mapType;
    }

    public MapOptions setMapType(String mapType) {
        GeoCoordinateSystem gs;
        if ("GaoDe".equals(mapType)) {
            gs = GeoCoordinateSystem.GCJ02();
        } else {
            mapType = "TianDiTu";
            gs = GeoCoordinateSystem.CGCS2000();
        }
        this.mapType = mapType;
        UserConfig.setString(baseName + "MapType", mapType);
        setCoordinateSystem(gs);
        return this;
    }

    public GeoCoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public MapOptions setCoordinateSystem(GeoCoordinateSystem coordinateSystem) {
        if (coordinateSystem == null) {
            coordinateSystem = GeoCoordinateSystem.CGCS2000();
        }
        this.coordinateSystem = coordinateSystem;
        UserConfig.setString(baseName + "CoordinateSystem", coordinateSystem.name());
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.loadMap();
        }
        return this;
    }

    public String getMapStyle() {
        return mapStyle;
    }

    public MapOptions setMapStyle(String mapStyle) {
        this.mapStyle = mapStyle;
        UserConfig.setString(baseName + "MapStyle", mapStyle);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setMapStyle();
        }
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public MapOptions setLanguage(String language) {
        this.language = language;
        UserConfig.setString(baseName + "Language", language);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.setLanguage();
        }
        return this;
    }

    public boolean isIsGeodetic() {
        return isGeodetic;
    }

    public MapOptions setIsGeodetic(boolean isGeodetic) {
        this.isGeodetic = isGeodetic;
        UserConfig.setBoolean(baseName + "Geodetic", isGeodetic);
        if (mapController != null && !mapController.isIsSettingValues()) {
//            mapController.loadMap();
        }
        return this;
    }

    public Color getTextColor() {
        return textColor;
    }

    public MapOptions setTextColor(Color textColor) {
        if (textColor == null) {
            textColor = Color.BLACK;
        }
        this.textColor = textColor;
        UserConfig.setString(baseName + "TextColor", textColor.toString());
        drawPoints();
        return this;
    }

}
