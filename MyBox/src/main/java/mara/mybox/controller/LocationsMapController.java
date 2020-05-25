package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationsMapController extends BaseController {

    protected WebEngine webEngine;
    protected String html, markerImage, title;
    protected boolean mapLoaded;
    protected int markerSize, textSize, mapSize;
    protected List<GeographyCode> codes;
    protected double longitude, latitude;

    @FXML
    protected WebView webView;
    @FXML
    protected CheckBox standardLayerCheck, satelliteLayerCheck, roadLayerCheck, trafficLayerCheck, fitViewCheck;
    @FXML
    protected ComboBox<String> standardOpacitySelector, satelliteOpacitySelector,
            roadOpacitySelector, trafficOpacitySelector,
            markerSizeSelector, mapSizeSelector, textSizeSelector;
    @FXML
    protected ToggleGroup langGroup, markerGroup;
    @FXML
    protected RadioButton chineseEnglishRadio, chineseRadio, englishRadio,
            markerPointRadio, markerCircleRadio, markerImageRadio, markerDataImageRadio,
            textNoneRadio, textCoordinateRadio, textAddressRadio,
            textValueRadio, textLabelRadio, textSizeRadio, textTimeRadio;
    @FXML
    protected CheckBox popLabelCheck, popAddressCheck, popCoordinateCheck, popCityCheck, popDetailedCheck,
            popValueCheck, popSizeCheck, popTimeCheck, popCommentsCheck, popImageCheck, popInfoCheck;
    @FXML
    protected Label titleLabel;
    @FXML
    protected VBox mapOptionsBox;

    public LocationsMapController() {
        baseTitle = AppVariables.message("Map");

        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        targetPathKey = "HtmlFilePath";
        targetExtensionFilter = CommonFxValues.HtmlExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            mapLoaded = false;

            initWebEngine();
            initMapOptions();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void initMapOptions() {
        try {
            if (standardLayerCheck == null) {
                return;
            }

            if (langGroup != null) {
                langGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            checkLanguage();
                        }
                );
            }

            if (standardLayerCheck != null) {
                standardLayerCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            setStandardLayer();
                        });

                standardOpacitySelector.getItems().addAll(Arrays.asList(
                        "1", "0.5", "0.8", "0.3", "0.1", "0.6", "0.7", "0.4", "0.2", "0.9", "0"
                ));
                standardOpacitySelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            setStandardLayer();
                        });

                satelliteLayerCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            setSatelliteLayer();
                        });

                satelliteOpacitySelector.getItems().addAll(Arrays.asList(
                        "1", "0.5", "0.8", "0.3", "0.1", "0.6", "0.7", "0.4", "0.2", "0.9", "0"
                ));
                satelliteOpacitySelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            setSatelliteLayer();
                        });

                roadLayerCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            setRoadLayer();
                        });

                roadOpacitySelector.getItems().addAll(Arrays.asList(
                        "1", "0.5", "0.8", "0.3", "0.1", "0.6", "0.7", "0.4", "0.2", "0.9", "0"
                ));
                roadOpacitySelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            setRoadLayer();
                        });

                trafficLayerCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            setTrafficLayer();
                        });

                trafficOpacitySelector.getItems().addAll(Arrays.asList(
                        "1", "0.5", "0.8", "0.3", "0.1", "0.6", "0.7", "0.4", "0.2", "0.9", "0"
                ));
                trafficOpacitySelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            setTrafficLayer();
                        });
            }

            markerSize = 24;
            if (markerSizeSelector != null) {
                markerSizeSelector.getItems().addAll(Arrays.asList(
                        "36", "24", "48", "64", "20", "30", "40", "50", "15"
                ));
                markerSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                int v = Integer.parseInt(newValue);
                                if (v > 0) {
                                    markerSize = v;
                                    markerSizeSelector.getEditor().setStyle(null);
                                    AppVariables.setUserConfigInt(baseName + "MarkerSize", markerSize);
                                    if (!isSettingValues) {
                                        drawLocations();
                                    }
                                } else {
                                    markerSizeSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                markerSizeSelector.getEditor().setStyle(badStyle);
                            }
                        });
            }

            mapSize = 3;
            if (mapSizeSelector != null) {
                mapSizeSelector.getItems().addAll(Arrays.asList(
                        "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"
                ));
                mapSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            mapSize = Integer.valueOf(newValue);
                            AppVariables.setUserConfigInt(baseName + "MapSize", mapSize);
                            if (!isSettingValues) {
                                drawLocations();
                            }
                        });
            }

            textSize = 12;
            if (textSizeSelector != null) {
                textSizeSelector.getItems().addAll(Arrays.asList(
                        "14", "12", "10", "15", "16", "18", "9", "8", "18", "20", "24"
                ));
                textSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                int v = Integer.parseInt(newValue);
                                if (v > 0) {
                                    textSize = v;
                                    textSizeSelector.getEditor().setStyle(null);
                                    AppVariables.setUserConfigInt(baseName + "TextSize", textSize);
                                    if (!isSettingValues) {
                                        drawLocations();
                                    }
                                } else {
                                    textSizeSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                textSizeSelector.getEditor().setStyle(badStyle);
                            }
                        });
            }

            if (popInfoCheck != null) {
                popInfoCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "PopInfo", popInfoCheck.isSelected());
                            if (!isSettingValues) {
                                drawLocations();
                            }
                        });
            }

            isSettingValues = true;
            if (standardOpacitySelector != null) {
                standardOpacitySelector.getSelectionModel().select("1");
                roadOpacitySelector.getSelectionModel().select("1");
                satelliteOpacitySelector.getSelectionModel().select("1");
                trafficOpacitySelector.getSelectionModel().select("1");
            }
            if (englishRadio != null && AppVariables.currentBundle != CommonValues.BundleZhCN) {
                englishRadio.fire();
            }
            if (markerSizeSelector != null) {
                markerSizeSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue(baseName + "MarkerSize", "24"));
            }
            if (mapSizeSelector != null) {
                mapSizeSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue(baseName + "MapSize", "3"));
            }
            if (textSizeSelector != null) {
                textSizeSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue(baseName + "TextSize", "12"));
            }
            if (popInfoCheck != null) {
                popInfoCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "PopInfo", true));
            }
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void checkLanguage() {
        try {
            if (isSettingValues || !mapLoaded || englishRadio == null) {
                return;
            }
            if (englishRadio.isSelected()) {
                webEngine.executeScript("setLanguage(\"en\");");
            } else if (chineseEnglishRadio.isSelected()) {
                webEngine.executeScript("setLanguage(\"zh_en\");");
            } else {
                webEngine.executeScript("setLanguage(\"zh_cn\");");
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setStandardLayer() {
        try {
            if (isSettingValues || !mapLoaded) {
                return;
            }
            if (!standardLayerCheck.isSelected()) {
                webEngine.executeScript("hideStandardLayer();");
            } else {
                float opacity = Float.valueOf(standardOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setStandardLayerOpacity(" + opacity + ");");
                    FxmlControl.setEditorNormal(standardOpacitySelector);
                } else {
                    FxmlControl.setEditorBadStyle(standardOpacitySelector);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void setSatelliteLayer() {
        try {
            if (isSettingValues || !mapLoaded) {
                return;
            }
            if (!satelliteLayerCheck.isSelected()) {
                webEngine.executeScript("hideSatelliteLayer();");
            } else {
                float opacity = Float.valueOf(satelliteOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setSatelliteLayerOpacity(" + opacity + ");");
                    FxmlControl.setEditorNormal(satelliteOpacitySelector);
                } else {
                    FxmlControl.setEditorBadStyle(satelliteOpacitySelector);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void setRoadLayer() {
        try {
            if (isSettingValues || !mapLoaded) {
                return;
            }
            if (!roadLayerCheck.isSelected()) {
                webEngine.executeScript("hideRoadLayer();");
            } else {
                float opacity = Float.valueOf(roadOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setRoadLayerOpacity(" + opacity + ");");
                    FxmlControl.setEditorNormal(roadOpacitySelector);
                } else {
                    FxmlControl.setEditorBadStyle(roadOpacitySelector);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void setTrafficLayer() {
        try {
            if (isSettingValues || !mapLoaded) {
                return;
            }
            if (!trafficLayerCheck.isSelected()) {
                webEngine.executeScript("hideTrafficLayer();");
            } else {
                float opacity = Float.valueOf(trafficOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    webEngine.executeScript("setTrafficLayerOpacity(" + opacity + ");");
                    FxmlControl.setEditorNormal(trafficOpacitySelector);
                } else {
                    FxmlControl.setEditorBadStyle(trafficOpacitySelector);
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void mapClicked(double longitude, double latitude) {

    }

    protected void mouseMoved(double longitude, double latitude) {

    }

    public void initWebEngine() {
        try {
            if (webView == null) {
                return;
            }
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            mapLoaded = false;
            if (mapOptionsBox != null) {
                mapOptionsBox.setDisable(true);
            }
            webEngine.setOnAlert((WebEvent<String> ev) -> {
                try {
                    String data = ev.getData();
                    if (data.equals("Loaded")) {
                        mapLoaded();
                        return;
                    } else if (data.startsWith("zoomSize:")) {
                        mapSize = Integer.valueOf(data.substring("zoomSize:".length()));
                        return;
                    }
                    boolean isClicked = true;
                    if (data.startsWith("click:")) {
                        data = data.substring(6);
                    } else if (data.startsWith("move:")) {
                        data = data.substring(5);
                        isClicked = false;
                    }
                    String[] values = data.split(",");
                    longitude = Double.valueOf(values[0]);
                    latitude = Double.valueOf(values[1]);
                    if (isClicked) {
                        mapClicked(longitude, latitude);
                    } else {
                        mouseMoved(longitude, latitude);
                    }
                    if (bottomLabel != null) {
                        bottomLabel.setText(data);
                    }
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
            });

//            webEngine.setOnError((WebErrorEvent event) -> {
//                if (bottomLabel != null) {
//                    bottomLabel.setText(event.getMessage());
//                }
//            });
//            webEngine.setOnStatusChanged((WebEvent<String> ev) -> {
//                if (bottomLabel != null) {
//                    bottomLabel.setText(ev.getData());
//                }
//            });
//            webEngine.getLoadWorker().stateProperty().addListener(
//                    (ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) -> {
//                        try {
//                            switch (newState) {
//                                case RUNNING:
//                                    break;
//                                case SUCCEEDED:
//                                    break;
//                                case CANCELLED:
//                                    break;
//                                case FAILED:
//                                    break;
//                            }
//                        } catch (Exception e) {
//                            logger.debug(e.toString());
//                        }
//                    });
//            webEngine.getLoadWorker().exceptionProperty().addListener(
//                    (ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) -> {
//                        if (nt == null) {
//                            return;
//                        }
//                        bottomLabel.setText(nt.getMessage());
//                    });
//            webEngine.locationProperty().addListener(
//                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
//                        bottomLabel.setText(newv);
//                    });
            if (html == null) {
                File map = FxmlControl.getInternalFile("/js/GaoDeMap.html", "js", "GaoDeMap.html", true);
                html = FileTools.readTexts(map);
            }
            NetworkTools.trustAll();
//            NetworkTools.myBoxSSL();
//            TableBrowserBypassSSL.write("amap.com");
//            TableBrowserBypassSSL.write("webapi.amap.com");
//            TableBrowserBypassSSL.write("alibabacorp.com");
            webEngine.loadContent(html);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public String locationImage() {
        String path = "/" + ControlStyle.getIconPath();
        return FxmlControl.getInternalFile(path + "iconLocation.png", "map",
                AppVariables.ControlColor.name() + "Point.png").getAbsolutePath();
    }

    protected void mapLoaded() {
        mapLoaded = true;
        if (mapOptionsBox != null) {
            mapOptionsBox.setDisable(false);
        }
        checkLanguage();
//        if (!locationsDrawn && codes != null || !codes.isEmpty()) {
//            drawGeographyCodes();
//        }
    }

    protected void drawGeographyCodes(int mapSize, List<GeographyCode> codes, String title) {
        this.mapSize = mapSize;
        isSettingValues = true;
        mapSizeSelector.getSelectionModel().select(mapSize + "");
        isSettingValues = false;
        this.title = title;
        if (this.title == null) {
            this.title = "";
        }
        titleLabel.setText(this.title);
        this.codes = codes;
        drawLocations();
    }

    protected void drawLocations() {
        try {
            if (!mapLoaded || codes == null || codes.isEmpty()) {
                return;
            }
            webEngine.executeScript("clearMap();");
            for (GeographyCode code : codes) {
                if (!code.validCoordinate()) {
                    continue;
                }
                String info = popInfoCheck.isSelected() ? code.info("</br>") : "";
                LocationTools.addMarkerInGaoDeMap(webEngine,
                        code.getLongitude(), code.getLatitude(),
                        code.getName(), info,
                        locationImage(), true, mapSize, markerSize, textSize);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (codes == null || codes.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (title == null || title.isBlank()) {
            title = (message("Locations") + "_"
                    + DateTools.datetimeToString(new Date())).replaceAll(":", "-");
        }
        File htmlFile = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                title.replaceAll("\"", ""), CommonFxValues.HtmlExtensionFilter, true);
        if (htmlFile == null) {
            return;
        }
        recordFileWritten(htmlFile);

        double scale = dpi / Screen.getPrimary().getDpi();
        scale = scale > 1 ? scale : 1;
        SnapshotParameters snapPara = new SnapshotParameters();
        snapPara.setFill(Color.TRANSPARENT);
        snapPara.setTransform(Transform.scale(scale, scale));
        Bounds bounds = webView.getLayoutBounds();
        int imageWidth = (int) Math.round(bounds.getWidth() * scale);
        int imageHeight = (int) Math.round(bounds.getHeight() * scale);
        WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
        final Image mapSnap = webView.snapshot(snapPara, snapshot);

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        String subPath = FileTools.getFilePrefix(htmlFile.getName());
                        String path = htmlFile.getParent() + "/" + subPath;
                        (new File(path)).mkdirs();

                        StringBuilder s = new StringBuilder();
                        s.append("<h1  class=\"center\">").append(title).append("</h1>\n");
                        s.append("<hr>\n");
                        List<String> names = new ArrayList<>();
                        names.addAll(Arrays.asList(message("chineseName"), message("EnglishName"),
                                message("Longitude"), message("Latitude"),
                                message("Code"), message("Code"), message("Code"), message("Code"), message("Code"),
                                message("Alias"), message("Alias"), message("Alias"), message("Alias"), message("Alias")
                        ));
                        StringTable table = new StringTable(names);
                        for (GeographyCode code : codes) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            List<String> row = new ArrayList<>();
                            row.addAll(Arrays.asList(
                                    code.getChineseName() == null ? "" : code.getChineseName(),
                                    code.getEnglishName() == null ? "" : code.getEnglishName(),
                                    code.getLongitude() >= -180 && code.getLongitude() <= 180 ? code.getLongitude() + "" : "",
                                    code.getLatitude() >= -90 && code.getLatitude() <= 90 ? code.getLatitude() + "" : "",
                                    code.getCode1() == null ? "" : code.getCode1(),
                                    code.getCode2() == null ? "" : code.getCode2(),
                                    code.getCode3() == null ? "" : code.getCode3(),
                                    code.getCode4() == null ? "" : code.getCode4(),
                                    code.getCode5() == null ? "" : code.getCode5(),
                                    code.getAlias1() == null ? "" : code.getAlias1(),
                                    code.getAlias2() == null ? "" : code.getAlias2(),
                                    code.getAlias3() == null ? "" : code.getAlias3(),
                                    code.getAlias4() == null ? "" : code.getAlias4(),
                                    code.getAlias5() == null ? "" : code.getAlias5()
                            ));
                            table.add(row);
                        }
                        s.append(StringTable.tableDiv(table));
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(mapSnap, null);
                        ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "map.jpg");
                        String imageName = subPath + "/map.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\"></div>\n");
                        s.append("<hr>\n");

                        String html = HtmlTools.html(title, s.toString());
                        FileTools.writeFile(htmlFile, html);

                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return htmlFile.exists();
                    } catch (Exception e) {
                        error = e.toString();
                        logger.error(e.toString());
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    browseURI(htmlFile.toURI());
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    @Override
    public void clearAction() {
        if (mapLoaded) {
            webEngine.executeScript("clearMap();");
        }
    }

    @Override
    public boolean leavingScene() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (webEngine != null) {
                webEngine.getLoadWorker().cancel();
                webEngine = null;
            }
//            NetworkTools.defaultSSL();

        } catch (Exception e) {
        }
        return super.leavingScene();

    }

}
