package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-8-6
 * @License Apache License Version 2.0
 */
public class ControlMapOptions extends BaseController {

    protected BaseMapController mapController;
    protected String markerImage;
    protected int markerSize, textSize, mapSize, dataMax;
    protected File markerImageFile;
    protected WebEngine webEngine;
    protected boolean mapLoaded;
    protected MapName mapName;
    protected CoordinateSystem coordinateSystem;

    public enum MapName {
        TianDiTu, GaoDe
    }

    @FXML
    protected CheckBox fitViewCheck, popInfoCheck,
            standardLayerCheck, satelliteLayerCheck, roadLayerCheck, trafficLayerCheck,
            zoomCheck, scaleCheck, typeCheck, symbolsCheck, boldCheck,
            markerLabelCheck, markerAddressCheck, markerCoordinateCheck,
            markerDatasetCheck, markerValueCheck, markerSizeCheck,
            markerStartCheck, markerEndCheck, markerDurationCheck,
            markerSpeedCheck, markerDirectionCheck;
    @FXML
    protected ComboBox<String> standardOpacitySelector, satelliteOpacitySelector,
            roadOpacitySelector, trafficOpacitySelector, dataMaximumSelector,
            markerSizeSelector, mapSizeSelector, textSizeSelector;
    @FXML
    protected ToggleGroup dataGroup, mapGroup, coordinateGroup, projectionGroup,
            langGroup, markerImageGroup, mapStyleGroup, textColorGroup;
    @FXML
    protected RadioButton tiandituRadio, gaodeRadio, cgcs2000Radio, gcj02Radio,
            mercatorRadio, geodeticRadio, currentPageRadio, currentQueryRadio,
            chineseEnglishRadio, chineseRadio, englishRadio,
            styleDefaultRadio, styleIndigoRadio, styleBlackRadio,
            markerPointRadio, markerCircleRadio, markerImageRadio, markerDatasetRadio, markerDataRadio,
            dataColorRadio, setColorRadio;
    @FXML
    protected TextField markerImageInput;
    @FXML
    protected VBox optionsBox, dataBox, languageBox, controlsBox, layersBox, sizeBox,
            markerTextBox, markerImageBox;
    @FXML
    protected FlowPane locationTextPane, baseTextPane, textColorPane,
            markerImagePane, dataNumberPane;
    @FXML
    protected ColorSet colorSetController;

    public ControlMapOptions() {
        baseTitle = AppVariables.message("MapOptions");
        TipsLabelKey = "MapComments";
    }

    public void initOptions(BaseMapController mapController) {
        this.mapController = mapController;
        this.webEngine = mapController.webEngine;
        this.baseName = mapController.baseName;
        this.baseTitle = mapController.baseTitle + " " + baseTitle;
        initOptions();
        setMap();
    }

    public void initOptions() {
        try {
            mapLoaded = false;
            markerSize = 24;
            textSize = 12;
            mapSize = AppVariables.getUserConfigInt(baseName + "MapSize", 3);
            dataMax = AppVariables.getUserConfigInt(baseName + "DataMax", 300);

            if (mapGroup == null) {
                return;
            }

            if (dataGroup != null) {
                dataGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            setData();
                        }
                );
            }

            if (mapGroup != null) {
                mapGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            setMap();
                        }
                );
            }

            if (projectionGroup != null) {
                projectionGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            checkProjection();
                        }
                );
            }

            if (langGroup != null) {
                langGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            checkLanguage();
                        }
                );
            }

            if (mapStyleGroup != null) {
                mapStyleGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            checkMapStyle();
                        }
                );
            }
            if (markerImageGroup != null) {
                markerImageGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            if (isSettingValues) {
                                return;
                            }
                            String type;
                            if (markerCircleRadio.isSelected()) {
                                type = "Circle";
                            } else if (markerImageRadio.isSelected()) {
                                type = "Image";
                            } else if (markerDatasetRadio.isSelected()) {
                                type = "Dataset";
                            } else if (markerDataRadio.isSelected()) {
                                type = "Data";
                            } else {
                                type = "Point";
                            }
                            AppVariables.setUserConfigValue(baseName + "MarkerImageType", type);
                            drawPoints();
                        }
                );
            }

            if (markerImageInput != null) {
                markerImageInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            String v = markerImageInput.getText();
                            if (v == null || v.isEmpty()) {
                                return;
                            }
                            final File file = new File(v);
                            if (!file.exists() || !file.isFile()) {
                                return;
                            }
                            if (markerImageFile == null || !markerImageFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                                markerImageFile = file;
                                recordFileOpened(file, VisitHistory.FileType.Image);
                                AppVariables.setUserConfigValue(baseName + "MarkerImageFile", markerImageFile.getAbsolutePath());
                                if (!isSettingValues) {
                                    drawPoints();
                                }
                            }
                        });
            }

            if (dataMaximumSelector != null) {
                dataMaximumSelector.getItems().addAll(Arrays.asList(
                        "300", "500", "200", "100", "1000", "2000", "5000", "10000", "50"
                ));
                dataMaximumSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                int v = Integer.parseInt(newValue);
                                if (v > 0) {
                                    dataMaximumSelector.getEditor().setStyle(null);
                                    if (dataMax != v) {
                                        dataMax = v;
                                        AppVariables.setUserConfigInt(baseName + "DataMax", dataMax);
                                        if (!isSettingValues && currentQueryRadio.isSelected()) {
                                            mapController.reloadData();
                                        }
                                    }
                                } else {
                                    dataMaximumSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                dataMaximumSelector.getEditor().setStyle(badStyle);
                            }
                        });
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
                                        drawPoints();
                                    }
                                } else {
                                    markerSizeSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                markerSizeSelector.getEditor().setStyle(badStyle);
                            }
                        });
            }

            if (mapSizeSelector != null) {
                mapSizeSelector.getItems().addAll(Arrays.asList(
                        "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"
                ));
                mapSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue == null || isSettingValues) {
                                return;
                            }
                            try {
                                int v = Integer.valueOf(newValue);
                                setMapSize(v, true, false);
                            } catch (Exception e) {
                            }
                        });
            }

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
                                        drawPoints();
                                    }
                                } else {
                                    textSizeSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                textSizeSelector.getEditor().setStyle(badStyle);
                            }
                        });
            }

            colorSetController.init(this, baseName + "Color", Color.BLACK);
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (setColorRadio.isSelected()) {
                        drawPoints();
                    }
                }
            });

            if (textColorGroup != null) {
                textColorGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            if (isSettingValues) {
                                return;
                            }
                            AppVariables.setUserConfigValue(baseName + "TextDataColor", dataColorRadio.isSelected());
                            if (!isSettingValues) {
                                drawPoints();
                            }
                        }
                );
            }

            if (markerLabelCheck != null) {
                markerLabelCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerLabel", markerLabelCheck.isSelected());
                            if (!isSettingValues) {
                                drawPoints();
                            }
                        });
            }
            if (markerAddressCheck != null) {
                markerAddressCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerAddress", markerAddressCheck.isSelected());
                            if (!isSettingValues) {
                                drawPoints();
                            }
                        });
            }
            if (markerCoordinateCheck != null) {
                markerCoordinateCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerCoordinate", markerCoordinateCheck.isSelected());
                            if (!isSettingValues) {
                                drawPoints();
                            }
                        });
            }
            if (markerDatasetCheck != null) {
                markerDatasetCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerDataset", markerDatasetCheck.isSelected());
                            drawPoints();
                        });
            }
            if (markerValueCheck != null) {
                markerValueCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerValue", markerValueCheck.isSelected());
                            if (!isSettingValues) {
                                drawPoints();
                            }
                        });
            }
            if (markerSizeCheck != null) {
                markerSizeCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerSize", markerSizeCheck.isSelected());
                            drawPoints();
                        });
            }
            if (markerStartCheck != null) {
                markerStartCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerStart", markerStartCheck.isSelected());
                            if (!isSettingValues) {
                                drawPoints();
                            }
                        });
            }
            if (markerEndCheck != null) {
                markerEndCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerEnd", markerEndCheck.isSelected());
                            if (!isSettingValues) {
                                drawPoints();
                            }
                        });
            }
            if (markerDurationCheck != null) {
                markerDurationCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "\"MarkerDuration", markerDurationCheck.isSelected());
                            drawPoints();
                        });
            }
            if (markerSpeedCheck != null) {
                markerSpeedCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerSpeed", markerSpeedCheck.isSelected());
                            drawPoints();
                        });
            }
            if (markerDirectionCheck != null) {
                markerDirectionCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerDirection", markerDirectionCheck.isSelected());
                            drawPoints();
                        });
            }

            if (boldCheck != null) {
                boldCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "MarkerBold", boldCheck.isSelected());
                            drawPoints();
                        });
            }

            if (popInfoCheck != null) {
                popInfoCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "PopInfo", popInfoCheck.isSelected());
                            drawPoints();
                        });
            }

            if (fitViewCheck != null) {
                fitViewCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "FitView", fitViewCheck.isSelected());
                            drawPoints();
                        });
            }
            if (zoomCheck != null) {
                zoomCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "ControlZoom", zoomCheck.isSelected());
                            if (!isSettingValues) {
                                webEngine.executeScript("setControl('zoom'," + zoomCheck.isSelected() + ");");
                            }
                        });
            }
            if (scaleCheck != null) {
                scaleCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "ControlScale", scaleCheck.isSelected());
                            if (!isSettingValues) {
                                webEngine.executeScript("setControl('scale'," + scaleCheck.isSelected() + ");");
                            }
                        });
            }
            if (typeCheck != null) {
                typeCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "ControlType", typeCheck.isSelected());
                            if (!isSettingValues) {
                                webEngine.executeScript("setControl('mapType'," + typeCheck.isSelected() + ");");
                            }
                        });
            }
            if (symbolsCheck != null) {
                symbolsCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "ControlSymbols", symbolsCheck.isSelected());
                            if (!isSettingValues) {
                                webEngine.executeScript("setControl('symbols'," + symbolsCheck.isSelected() + ");");
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
            if (dataMaximumSelector != null) {
                dataMaximumSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "DataMax", "300"));
            }
            if (markerSizeSelector != null) {
                markerSizeSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "MarkerSize", "24"));
            }
            if (markerImageInput != null) {
                markerImageInput.setText(AppVariables.getUserConfigValue(baseName + "MarkerImageFile", ""));
            }

            if (textSizeSelector != null) {
                textSizeSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "TextSize", "12"));
            }
            if (markerLabelCheck != null) {
                markerLabelCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerLabel", true));
            }
            if (markerAddressCheck != null) {
                markerAddressCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerAddress", false));
            }
            if (markerCoordinateCheck != null) {
                markerCoordinateCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerCoordinate", false));
            }
            if (markerDatasetCheck != null) {
                markerDatasetCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerDataset", false));
            }
            if (markerValueCheck != null) {
                markerValueCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerValue", false));
            }
            if (markerSizeCheck != null) {
                markerSizeCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerSize", false));
            }
            if (markerStartCheck != null) {
                markerStartCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerStart", true));
            }
            if (markerEndCheck != null) {
                markerEndCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerEnd", true));
            }
            if (markerDurationCheck != null) {
                markerDurationCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerDuration", false));
            }
            if (markerSpeedCheck != null) {
                markerSpeedCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerSpeed", false));
            }
            if (markerDirectionCheck != null) {
                markerDirectionCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerDirection", false));
            }
            if (boldCheck != null) {
                boldCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MarkerBold", false));
            }
            if (popInfoCheck != null) {
                popInfoCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "PopInfo", true));
            }
            if (fitViewCheck != null) {
                fitViewCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "FitView", true));
            }
            if (zoomCheck != null) {
                zoomCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ControlZoom", true));
            }
            if (scaleCheck != null) {
                scaleCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ControlScale", true));
            }
            if (typeCheck != null) {
                typeCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ControlType", true));
            }
            if (symbolsCheck != null) {
                symbolsCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ControlSymbols", false));
            }
            if (geodeticRadio != null
                    && "EPSG:4326".equals(AppVariables.getUserConfigValue(baseName + "Projection", "EPSG:900913"))) {
                geodeticRadio.fire();
            }
            String type = AppVariables.getUserConfigValue(baseName + "MarkerImageType", "Point");
            if ("Circle".equals(type)) {
                markerCircleRadio.fire();
            } else if ("Image".equals(type)) {
                markerImageRadio.fire();
            } else if ("Dataset".equals(type)) {
                markerDatasetRadio.fire();
            } else if ("Data".equals(type)) {
                markerDataRadio.fire();
            } else {
                markerPointRadio.fire();
            }
            if (AppVariables.getUserConfigBoolean(baseName + "TextDataColor", false)) {
                dataColorRadio.fire();
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void setMap() {
        if (webEngine == null || isSettingValues) {
            return;
        }
        isSettingValues = true;
        mapLoaded = false;
        optionsBox.setDisable(true);
        try {
            if (gaodeRadio != null && gaodeRadio.isSelected()) {
                mapName = MapName.GaoDe;
                File map = FxmlControl.getInternalFile("/js/GaoDeMap.html", "js", "GaoDeMap.html", true);
                String html = FileTools.readTexts(map);
                html = html.replace("06b9e078a51325a843dfefd57ffd876c", AppVariables.getUserConfigValue("GaoDeMapWebKey", CommonValues.GaoDeMapWebKey));
                webEngine.loadContent(html);

                gcj02Radio.setSelected(true);
                gcj02Radio.setDisable(false);
                cgcs2000Radio.setDisable(true);
                mercatorRadio.setSelected(true);
                geodeticRadio.setDisable(true);
                coordinateSystem = CoordinateSystem.GCJ02();

                if (optionsBox.getChildren().contains(controlsBox)) {
                    optionsBox.getChildren().removeAll(controlsBox);
                }
                if (!optionsBox.getChildren().contains(languageBox)) {
                    optionsBox.getChildren().addAll(layersBox, languageBox);
                }
                if (!sizeBox.getChildren().contains(fitViewCheck)) {
                    sizeBox.getChildren().add(0, fitViewCheck);
                }

                mapSizeSelector.getItems().clear();
                mapSizeSelector.getItems().addAll(Arrays.asList(
                        "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"
                ));

            } else {
                mapName = MapName.TianDiTu;
                File map = FxmlControl.getInternalFile("/js/tianditu.html", "js", "tianditu.html", true);
                String html = FileTools.readTexts(map);
                html = html.replace("0ddeb917def62b4691500526cc30a9b1", AppVariables.getUserConfigValue("TianDiTuWebKey", CommonValues.TianDiTuWebKey));
                if (geodeticRadio.isSelected()) {
                    html = html.replace("'EPSG:900913", "EPSG:4326");
                }
                FileTools.writeFile(map, html);
                String name = map.getAbsolutePath();
                name = name.replace("\\\\", "/");
                webEngine.load("file:///" + name);

                cgcs2000Radio.setSelected(true);
                cgcs2000Radio.setDisable(false);
                gcj02Radio.setDisable(true);
                geodeticRadio.setDisable(false);

                coordinateSystem = CoordinateSystem.CGCS2000();

                if (optionsBox.getChildren().contains(languageBox)) {
                    optionsBox.getChildren().removeAll(languageBox, layersBox);
                }
                if (!optionsBox.getChildren().contains(controlsBox)) {
                    optionsBox.getChildren().addAll(controlsBox);
                }
                if (sizeBox.getChildren().contains(fitViewCheck)) {
                    sizeBox.getChildren().remove(fitViewCheck);
                }
                mapSizeSelector.getItems().clear();
                mapSizeSelector.getItems().addAll(Arrays.asList(
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"
                ));

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        isSettingValues = false;
        initMap();

    }

    protected void initMap() {
        if (webEngine == null || isSettingValues) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
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
                    mapSizeSelector.getSelectionModel().select(mapSize + "");
                    if (gaodeRadio != null && gaodeRadio.isSelected()) {
                        checkLanguage();
                    } else {
                        webEngine.executeScript("setControl('zoom'," + zoomCheck.isSelected() + ");");
                        webEngine.executeScript("setControl('scale'," + scaleCheck.isSelected() + ");");
                        webEngine.executeScript("setControl('mapType'," + typeCheck.isSelected() + ");");
                        webEngine.executeScript("setControl('symbols'," + symbolsCheck.isSelected() + ");");
                    }
                    drawPoints();
                });
            }

        }, 0, 500);
    }

    protected void mapLoaded() {
        if (mapLoaded) {
            return;
        }
        mapLoaded = true;
        optionsBox.setDisable(false);
    }

    public void setData() {
        if (currentPageRadio.isSelected()) {
            if (dataBox.getChildren().contains(dataNumberPane)) {
                dataBox.getChildren().remove(dataNumberPane);
            }
        } else {
            if (!dataBox.getChildren().contains(dataNumberPane)) {
                dataBox.getChildren().add(dataNumberPane);
            }
        }
        if (webEngine == null || isSettingValues || mapController == null) {
            return;
        }
        mapController.reloadData();
    }

    public void drawPoints() {
        if (isSettingValues || mapController == null) {
            return;
        }
        mapController.drawPoints();
    }

    public void checkProjection() {
        if (webEngine == null || isSettingValues) {
            return;
        }
        if (mercatorRadio.isSelected()) {
            AppVariables.setUserConfigValue(baseName + "Projection", "EPSG:900913");
        } else {
            AppVariables.setUserConfigValue(baseName + "Projection", "EPSG:4326");
        }
        setMap();
    }

    public void checkLanguage() {
        try {
            if (isSettingValues || englishRadio == null) {
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
            MyBoxLog.error(e.toString());
        }
    }

    public void checkMapStyle() {
        try {
            if (isSettingValues || styleDefaultRadio == null) {
                return;
            }
            Platform.runLater(() -> {
                if (styleDefaultRadio.isSelected()) {
                    webEngine.executeScript("setStyle(\"default\");");
                } else if (styleBlackRadio.isSelected()) {
                    webEngine.executeScript("setStyle(\"black\");");
                } else if (styleIndigoRadio.isSelected()) {
                    webEngine.executeScript("setStyle(\"indigo\");");
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setMapSize(int size, boolean setMap, boolean setSelector) {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        mapSize = size;
        AppVariables.setUserConfigInt(baseName + "MapSize", mapSize);
        if (setMap) {
            webEngine.executeScript("setZoom(" + size + ");");
        }
        if (setSelector) {
            isSettingValues = true;
            mapSizeSelector.getSelectionModel().select(size + "");
            isSettingValues = false;
        }
    }

    public void setStandardLayer() {
        try {
            if (isSettingValues) {
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
            MyBoxLog.error(e.toString());
        }

    }

    public void setSatelliteLayer() {
        try {
            if (isSettingValues) {
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
            MyBoxLog.error(e.toString());
        }

    }

    public void setRoadLayer() {
        try {
            if (isSettingValues) {
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
            MyBoxLog.error(e.toString());
        }

    }

    public void setTrafficLayer() {
        try {
            if (isSettingValues) {
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
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    public void selectMarkerImage() {
        try {
            File file = FxmlControl.selectFile(this, VisitHistory.FileType.Image);
            if (file == null) {
                return;
            }
            markerImageInput.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popMarkerImage(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {

            @Override
            public void handleSelect() {
                selectMarkerImage();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectMarkerImage();
                    return;
                }
                markerImageInput.setText(file.getAbsolutePath());
            }

        }
                .setSourcePathKey(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image))
                .setSourceFileType(VisitHistory.FileType.Image)
                .setSourcePathType(VisitHistory.FileType.Image)
                .setSourceExtensionFilter(CommonFxValues.ImageExtensionFilter)
                .pop();
    }

    @FXML
    public void aboutCoordinateSystem() {
        mapController.aboutCoordinateSystem();
    }

}
