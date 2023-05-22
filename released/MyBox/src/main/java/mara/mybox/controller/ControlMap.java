package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.MapOptions;
import mara.mybox.data.MapPoint;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-7-27
 * @License Apache License Version 2.0
 */
public class ControlMap extends BaseController {

    protected String mapTitle;
    protected WebEngine webEngine;
    protected boolean mapLoaded;
    protected MapOptions mapOptions;
    protected SimpleBooleanProperty loadNotify, drawNotify;
    protected List<MapPoint> mapPoints;
    protected int interval;

    @FXML
    protected WebView mapView;
    @FXML
    protected ControlMapOptions mapOptionsController;
    @FXML
    protected Label topLabel, titleLabel;
    @FXML
    protected VBox snapBox;

    public ControlMap() {
        baseTitle = message("Map");

        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        targetExtensionFilter = FileFilters.HtmlExtensionFilter;
    }

    /*
        init map
     */
    public void initMap() {
        try {
            loadNotify = new SimpleBooleanProperty();
            drawNotify = new SimpleBooleanProperty();
            mapOptions = new MapOptions(this);
            interval = 1;

            initWebEngine();

            loadMap();

            if (mapOptionsController != null) {
                mapOptionsController.setParameters(this);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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

            loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    mapLoaded();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void loadMap() {
        if (webEngine == null) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mapLoaded = false;
        mapPoints = null;
        if (mapOptionsController != null) {
            mapOptionsController.optionsBox.setDisable(true);
        }
        webEngine.getLoadWorker().cancel();
        if (mapOptions.isGaoDeMap()) {
            webEngine.loadContent(LocationTools.gaodeMap());
        } else {
            webEngine.load(LocationTools.tiandituFile(mapOptions.isIsGeodetic()).toURI().toString());
        }
    }

    public void mapEvents(String data) {
        try {
            if (bottomLabel != null) {
                bottomLabel.setText(data);
            }
            if (data.equals("Loaded")) {
                loadNotify.set(!loadNotify.get());
                return;
            } else if (data.startsWith("zoomSize:")) {
                int v = Integer.parseInt(data.substring("zoomSize:".length()));
                if (mapOptionsController != null) {
                    mapOptionsController.setMapSize(v);
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
            double longitude = Double.parseDouble(values[0]);
            double latitude = Double.parseDouble(values[1]);
            if (isClicked) {
                mapClicked(longitude, latitude);
            } else {
                mouseMoved(longitude, latitude);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void mapLoaded() {
        mapLoaded = true;
        if (mapOptionsController != null) {
            mapOptionsController.mapLoaded();
        }
        setMapSize();
        if (mapOptions.isGaoDeMap()) {
            setStandardLayer();
            setSatelliteLayer();
            setRoadLayer();
            setTrafficLayer();
            setFitView();
            setLanguage();
        } else {
            setShowZoom();
            setShowScale();
            setShowType();
            setShowSymbols();
        }
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

    /*
        options
     */
    public void setMapTitle(String title) {
        mapTitle = title;
    }

    public void setLanguage() {
        try {
            if (!mapLoaded || isSettingValues
                    || !mapOptions.isGaoDeMap() || mapOptions.getLanguage() == null) {
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
            if (!mapLoaded || isSettingValues || mapOptions.getMapStyle() == null) {
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
        if (!mapLoaded || isSettingValues || webEngine == null) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setZoom(" + mapOptions.getMapSize() + ");");
        });

    }

    public void setStandardLayer() {
        try {
            if (!mapLoaded || isSettingValues || !mapOptions.isGaoDeMap()) {
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
            if (!mapLoaded || isSettingValues || !mapOptions.isGaoDeMap()) {
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
            if (!mapLoaded || isSettingValues || !mapOptions.isGaoDeMap()) {
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
            if (!mapLoaded || isSettingValues || !mapOptions.isGaoDeMap()) {
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
        if (!mapLoaded || isSettingValues || mapOptions.isGaoDeMap()) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('zoom'," + mapOptions.isZoom() + ");");
        });
    }

    public void setShowScale() {
        if (!mapLoaded || isSettingValues || mapOptions.isGaoDeMap()) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('scale'," + mapOptions.isScale() + ");");
        });
    }

    public void setShowType() {
        if (!mapLoaded || isSettingValues || mapOptions.isGaoDeMap()) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('mapType'," + mapOptions.isType() + ");");
        });
    }

    public void setShowSymbols() {
        if (!mapLoaded || isSettingValues || mapOptions.isGaoDeMap()) {
            return;
        }
        Platform.runLater(() -> {
            webEngine.executeScript("setControl('symbols'," + mapOptions.isSymbols() + ");");
        });
    }

    public void setFitView() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        if (mapOptions.isGaoDeMap() && mapOptions.isFitView()) {
            Platform.runLater(() -> {
                webEngine.executeScript("map.setFitView();");
            });
        }
    }

    public void setMinLevel() {
        if (isSettingValues) {
            return;
        }
        if (mapOptionsController != null) {
            mapOptionsController.setMapSize(mapOptions.isGaoDeMap() ? 3 : 1);
        }
    }

    /*
        data
     */
    public void drawPoints() {
        drawNotify.set(!drawNotify.get());
    }

    protected String writePointsTable() {
        return "";
    }

    public void reloadData() {

    }

    protected void drawPoint(MapPoint point) {
        if (webEngine == null || !mapLoaded || point == null) {
            return;
        }
        drawPoint(point.getLongitude(), point.getLatitude(),
                point.getLabel(), point.getInfo(), point.getMarkSize(),
                point.getMarkerImage(), point.getTextSize(), point.getTextColor(),
                point.isIsBold()
        );
    }

    protected void drawPoint(double lo, double la, String label, String info, int markSize,
            String markerImage, int textSize, Color textColor, boolean isBold) {
        try {
            if (webEngine == null || !mapLoaded
                    || !LocationTools.validCoordinate(lo, la)) {
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
                pLabel += lo + "," + la;
            }
            String pImage = markerImage != null ? markerImage : mapOptions.image();
            Color pColor = textColor != null ? textColor : mapOptions.textColor();
            int mSize = markSize > 0 ? markSize : mapOptions.markSize();
            int tSize = textSize > 0 ? textSize : mapOptions.textSize();
            webEngine.executeScript("addMarker("
                    + lo + "," + la
                    + ", " + jsString(pLabel)
                    + ", " + jsString(mapOptions.isPopInfo() ? info : null)
                    + ", '" + pImage.replaceAll("\\\\", "/") + "'"
                    + ", " + (mSize > 0 ? mSize : 24)
                    + ", " + (tSize > 0 ? tSize : 12)
                    + ", '" + FxColorTools.color2rgb(pColor) + "'"
                    + ", " + isBold + ");");
            titleLabel.setText(label);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected String jsString(String string) {
        return string == null ? "null"
                : "'" + StringTools.replaceHtmlLineBreak(string.replaceAll("'", AppValues.MyBoxSeparator)) + "'";
    }

    public void initPoints(List<MapPoint> points) {
        if (!mapLoaded || webEngine == null || points == null || points.isEmpty()) {
            return;
        }
        setPoints(points);
//        setMinLevel();
        mapOptions.setFitView(false);
        mapPoints = points;
        if (mapPoints != null && !mapPoints.isEmpty()) {
            webEngine.executeScript("setCenter("
                    + mapPoints.get(0).getLongitude() + ", " + mapPoints.get(0).getLatitude() + ");");
        }
    }

    public void setPoints(List<MapPoint> points) {
        clearMap();
        mapPoints = points;
        if (mapPoints != null && !mapPoints.isEmpty()) {
            if (topLabel != null) {
                topLabel.setText(message("DataNumber") + ":" + points.size());
            } else {
                bottomLabel.setText(message("DataNumber") + ":" + points.size());
            }
        }
    }

    public void drawPoints(List<MapPoint> points) {
        if (!mapLoaded || webEngine == null || points == null || points.isEmpty()) {
            return;
        }
        setPoints(points);
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
                            setFitView();
                        }
                        frameEnd = true;
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                });
            }

        }, 0, interval);
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
        drawPoints();
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
        task = new SingletonTask<Void>(this) {
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
                    ImageFileWriters.writeImageFile(bufferedImage, "jpg", imageFile.getAbsolutePath());
                    s.append("<h2  class=\"center\">").append(message("Image")).append("</h2>\n");
                    s.append("<div align=\"center\"><img src=\"").append(imageFile.toURI().toString())
                            .append("\"  style=\"max-width:95%;\"></div>\n");
                    s.append("<hr>\n");

                    if (task == null || task.isCancelled()) {
                        return false;
                    }

                    if (mapPoints != null && !mapPoints.isEmpty()) {
                        List<String> names = new ArrayList<>();
                        names.addAll(Arrays.asList(message("Longitude"), message("Latitude"),
                                message("Label"), message("Information"), message("CoordinateSystem")));
                        StringTable table = new StringTable(names);
                        for (MapPoint code : mapPoints) {
                            if (task == null || task.isCancelled()) {
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
                    MyBoxLog.error(e.toString());
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
        task = new SingletonTask<Void>(this) {

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
                        if (task == null || task.isCancelled()) {
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
                DataManufactureController.open(columns, data);
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
            if (mapOptionsController != null) {
                mapOptionsController.cleanPane();
            }
        } catch (Exception e) {
        }
        super.cleanPane();

    }

}
