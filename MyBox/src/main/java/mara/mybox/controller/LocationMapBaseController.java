package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationMapBaseController extends LocationBaseController {

    protected WebEngine webEngine;
    protected static String html;
    protected boolean mapLoaded;
    protected String markerImage;
    protected int markerSize, mapZoom;

    @FXML
    protected WebView webView;
    @FXML
    protected CheckBox standardLayerCheck, satelliteLayerCheck, roadLayerCheck, trafficLayerCheck, fitViewCheck;
    @FXML
    protected ComboBox<String> standardOpacitySelector, satelliteOpacitySelector,
            roadOpacitySelector, trafficOpacitySelector, markerSizeSelector;
    @FXML
    protected ToggleGroup langGroup, markerGroup;
    @FXML
    protected RadioButton chineseEnglishRadio, chineseRadio, englishRadio,
            markerPointRadio, markerCircleRadio, markerImageRadio, markerDataImageRadio,
            textNoneRadio, textCoordinateRadio, textAddressRadio,
            textValueRadio, textLabelRadio, textSizeRadio, textTimeRadio;
    @FXML
    protected CheckBox popLabelCheck, popAddressCheck, popCoordinateCheck, popCityCheck, popDetailedCheck,
            popValueCheck, popSizeCheck, popTimeCheck, popCommentsCheck, popImageCheck;

    public LocationMapBaseController() {
        baseTitle = AppVariables.message("Map");

        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = "ImageFilePath";
        sourcePathKey = "ImageFilePath";

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

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

            markerSize = 36;
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
                                } else {
                                    markerSizeSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                markerSizeSelector.getEditor().setStyle(badStyle);
                            }
                        });
                markerSizeSelector.setValue(AppVariables.getUserConfigInt(baseName + "MarkerSize", 36) + "");
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

    protected void mapClicked(double longtitude, double latitude) {

    }

    protected void mouseMoved(double longtitude, double latitude) {

    }

    public void initWebEngine() {
        try {
            if (webView == null) {
                return;
            }
            webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            mapLoaded = false;
            if (rightPane != null) {
                rightPane.setDisable(true);
            }
            webEngine.setOnAlert((WebEvent<String> ev) -> {
                try {
                    String data = ev.getData();
                    if (data.equals("Loaded")) {
                        mapLoaded();
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
                    double longtitude = Double.valueOf(values[0]);
                    double latitude = Double.valueOf(values[1]);
                    if (isClicked) {
                        mapClicked(longtitude, latitude);
                    } else {
                        mouseMoved(longtitude, latitude);
                    }
                    if (bottomLabel != null) {
                        bottomLabel.setText(data);
                    }
                } catch (Exception e) {
                }
            });

            webEngine.setOnError((WebErrorEvent event) -> {
                if (bottomLabel != null) {
                    bottomLabel.setText(event.getMessage());
                }
            });
            webEngine.setOnStatusChanged((WebEvent<String> ev) -> {
                if (bottomLabel != null) {
                    bottomLabel.setText(ev.getData());
                }
            });

            webEngine.getLoadWorker().stateProperty().addListener(
                    (ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) -> {
                        try {
                            switch (newState) {
                                case RUNNING:
                                    break;
                                case SUCCEEDED:
                                    break;
                                case CANCELLED:
                                    break;
                                case FAILED:
                                    break;
                            }
                        } catch (Exception e) {
                            logger.debug(e.toString());
                        }
                    });

            webEngine.getLoadWorker().exceptionProperty().addListener(
                    (ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) -> {
                        if (nt == null) {
                            return;
                        }
                        bottomLabel.setText(nt.getMessage());
                    });

            webEngine.locationProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        bottomLabel.setText(newv);
                    });

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
        if (rightPane != null) {
            rightPane.setDisable(false);
        }
        checkLanguage();
    }

    @FXML
    @Override
    public void clearAction() {
        webEngine.executeScript("clearMap();");
    }

    @Override
    public boolean leavingScene() {
        try {
            if (webEngine != null) {
                webEngine.getLoadWorker().cancel();
                webEngine = null;
            }
            NetworkTools.defaultSSL();

        } catch (Exception e) {
        }
        return super.leavingScene();

    }

}
