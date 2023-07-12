package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.data.MapOptions;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-8-6
 * @License Apache License Version 2.0
 */
public class ControlMapOptions extends BaseController {

    protected ControlMap mapController;
    protected MapOptions mapOptions;

    @FXML
    protected CheckBox fitViewCheck, popInfoCheck,
            standardLayerCheck, satelliteLayerCheck, roadLayerCheck, trafficLayerCheck,
            zoomCheck, scaleCheck, typeCheck, symbolsCheck, boldCheck,
            markerLabelCheck, markerCoordinateCheck;
    @FXML
    protected ComboBox<String> standardOpacitySelector, satelliteOpacitySelector,
            roadOpacitySelector, trafficOpacitySelector,
            markerSizeSelector, mapSizeSelector, textSizeSelector;
    @FXML
    protected ToggleGroup mapGroup, coordinateGroup, projectionGroup,
            langGroup, markerImageGroup, mapStyleGroup;
    @FXML
    protected RadioButton tiandituRadio, gaodeRadio, cgcs2000Radio, gcj02Radio,
            mercatorRadio, geodeticRadio, chineseEnglishRadio, chineseRadio, englishRadio,
            styleDefaultRadio, styleIndigoRadio, styleBlackRadio,
            markerPointRadio, markerCircleRadio, markerImageRadio;
    @FXML
    protected TextField markerImageInput;
    @FXML
    protected VBox optionsBox, mapBox, languageBox, controlsBox, layersBox, sizeBox,
            markerTextBox, markerImageBox;
    @FXML
    protected FlowPane baseTextPane, textColorPane, markerImagePane;
    @FXML
    protected ControlColorSet colorSetController;

    public ControlMapOptions() {
        baseTitle = Languages.message("MapOptions");
        TipsLabelKey = "MapComments";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    public void setParameters(ControlMap mapController) {
        try {
            this.mapController = mapController;
            this.mapOptions = mapController.mapOptions;
            this.baseName = mapController.baseName;
            this.baseTitle = mapController.baseTitle + " " + baseTitle;

            setControlListeners();

            setControlValues();

            mapTypeChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setControlListeners() {
        try {
            if (mapGroup != null) {
                mapGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            if (isSettingValues) {
                                return;
                            }
                            mapTypeChanged();
                            mapOptions.setMapType(gaodeRadio.isSelected() ? "GaoDe" : "TianDiTu");
                        }
                );
            }

            if (projectionGroup != null) {
                projectionGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            if (isSettingValues) {
                                return;
                            }
                            mapOptions.setIsGeodetic(geodeticRadio.isSelected());
                        }
                );
            }

            if (langGroup != null) {
                langGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            if (isSettingValues) {
                                return;
                            }
                            String lang;
                            if (chineseEnglishRadio.isSelected()) {
                                lang = "zh_en";
                            } else if (englishRadio.isSelected()) {
                                lang = "en";
                            } else {
                                lang = "zh_cn";
                            }
                            mapOptions.setLanguage(lang);
                        }
                );
            }

            if (mapStyleGroup != null) {
                mapStyleGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            if (isSettingValues) {
                                return;
                            }
                            String style;
                            if (styleIndigoRadio.isSelected()) {
                                style = "indigo";
                            } else if (styleBlackRadio.isSelected()) {
                                style = "black";
                            } else {
                                style = "default";
                            }
                            mapOptions.setMapStyle(style);
                        }
                );
            }
            if (markerImageGroup != null) {
                markerImageGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                            if (isSettingValues) {
                                return;
                            }
                            File file;
                            if (markerCircleRadio.isSelected()) {
                                file = mapOptions.circleImage();
                            } else if (markerImageRadio.isSelected()) {
                                String v = markerImageInput.getText();
                                if (v == null || v.isBlank()) {
                                    file = mapOptions.pointImage();
                                } else {
                                    file = new File(v);
                                    if (!file.exists() || !file.isFile()) {
                                        file = mapOptions.pointImage();
                                    }
                                }
                            } else {
                                file = mapOptions.pointImage();
                            }
                            mapOptions.setMarkerImageFile(file);
                        });
            }

            if (markerImageInput != null) {
                markerImageInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            if (isSettingValues) {
                                return;
                            }
                            String v = markerImageInput.getText();
                            if (v == null || v.isEmpty()) {
                                return;
                            }
                            mapOptions.setMarkerImageFile(new File(v));
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
                markerSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            markerSizeSelector.getEditor().setStyle(null);
                            mapOptions.setMarkerSize(v);
                        } else {
                            markerSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        markerSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                });
            }

            if (mapSizeSelector != null) {
                mapSizeSelector.getItems().addAll(Arrays.asList(
                        "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"
                ));
                mapSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                if (isSettingValues) {
                                    return;
                                }
                                int v = Integer.parseInt(newValue);
                                if (v > 0) {
                                    mapOptions.setMapSize(v);
                                    mapSizeSelector.getEditor().setStyle(null);
                                } else {
                                    mapSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                                }
                            } catch (Exception e) {
                                mapSizeSelector.getEditor().setStyle(UserConfig.badStyle());
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
                                if (isSettingValues) {
                                    return;
                                }
                                int v = Integer.parseInt(newValue);
                                if (v > 0) {
                                    mapOptions.setTextSize(v);
                                    textSizeSelector.getEditor().setStyle(null);
                                } else {
                                    textSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                                }
                            } catch (Exception e) {
                                textSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        });
            }

            colorSetController.init(this, baseName + "Color", Color.BLACK);
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setTextColor((Color) nv);
                }
            });

            if (markerLabelCheck != null) {
                markerLabelCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setMarkerLabel(markerLabelCheck.isSelected());
                });
            }
            if (markerCoordinateCheck != null) {
                markerCoordinateCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setMarkerCoordinate(markerCoordinateCheck.isSelected());
                });
            }

            if (boldCheck != null) {
                boldCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setBold(boldCheck.isSelected());
                });
            }

            if (popInfoCheck != null) {
                popInfoCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setPopInfo(popInfoCheck.isSelected());
                });
            }

            if (fitViewCheck != null) {
                fitViewCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setFitView(fitViewCheck.isSelected());
                });
            }
            if (zoomCheck != null) {
                zoomCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setZoom(zoomCheck.isSelected());
                });
            }
            if (scaleCheck != null) {
                scaleCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setScale(scaleCheck.isSelected());
                });
            }
            if (typeCheck != null) {
                typeCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setType(typeCheck.isSelected());
                });
            }
            if (symbolsCheck != null) {
                symbolsCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    mapOptions.setSymbols(symbolsCheck.isSelected());
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setControlValues() {
        try {
            isSettingValues = true;
            if (mapOptions.isGaoDeMap()) {
                gaodeRadio.setSelected(true);
            } else {
                tiandituRadio.setSelected(true);
            }
            mapSizeSelector.getSelectionModel().select(mapOptions.getMapSize() + "");
            if (standardOpacitySelector != null) {
                standardOpacitySelector.setValue(mapOptions.getStandardOpacity() + "");
                roadOpacitySelector.setValue(mapOptions.getRoadOpacity() + "");
                satelliteOpacitySelector.setValue(mapOptions.getSatelliteOpacity() + "");
                trafficOpacitySelector.setValue(mapOptions.getTrafficOpacity() + "");
            }
            if ("zh_en".equals(mapOptions.getLanguage())) {
                chineseEnglishRadio.setSelected(true);
            } else if ("en".equals(mapOptions.getLanguage())) {
                englishRadio.setSelected(true);
            } else {
                chineseRadio.setSelected(true);
            }
            if (markerSizeSelector != null) {
                markerSizeSelector.setValue(mapOptions.getMarkerSize() + "");
            }
            if (markerImageInput != null) {
                markerImageInput.setText(mapOptions.getMarkerImageFile() + "");
            }
            if (textSizeSelector != null) {
                textSizeSelector.setValue(mapOptions.getTextSize() + "");
            }
            if (markerLabelCheck != null) {
                markerLabelCheck.setSelected(mapOptions.isMarkerLabel());
            }
            if (markerCoordinateCheck != null) {
                markerCoordinateCheck.setSelected(mapOptions.isMarkerCoordinate());
            }
            if (boldCheck != null) {
                boldCheck.setSelected(mapOptions.isBold());
            }
            if (popInfoCheck != null) {
                popInfoCheck.setSelected(mapOptions.isPopInfo());
            }
            if (fitViewCheck != null) {
                fitViewCheck.setSelected(mapOptions.isFitView());
            }
            if (zoomCheck != null) {
                zoomCheck.setSelected(mapOptions.isZoom());
            }
            if (scaleCheck != null) {
                scaleCheck.setSelected(mapOptions.isScale());
            }
            if (typeCheck != null) {
                typeCheck.setSelected(mapOptions.isType());
            }
            if (symbolsCheck != null) {
                symbolsCheck.setSelected(mapOptions.isSymbols());
            }
            if (geodeticRadio != null) {
                geodeticRadio.setSelected(mapOptions.isIsGeodetic());
            }
            File file = mapOptions.getMarkerImageFile();
            if (file == null || !file.exists() || !file.isFile()) {
                markerPointRadio.setSelected(true);
            } else if (file.equals(mapOptions.circleImage())) {
                markerCircleRadio.setSelected(true);
            } else if (file.equals(mapOptions.pointImage())) {
                markerPointRadio.setSelected(true);
            } else {
                markerImageRadio.setSelected(true);
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void mapTypeChanged() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        optionsBox.setDisable(true);
        try {
            if (gaodeRadio.isSelected()) {

                gcj02Radio.setSelected(true);
                gcj02Radio.setDisable(false);
                cgcs2000Radio.setDisable(true);
                mercatorRadio.setSelected(true);
                geodeticRadio.setDisable(true);

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

                tiandituRadio.setSelected(true);

                cgcs2000Radio.setSelected(true);
                cgcs2000Radio.setDisable(false);
                gcj02Radio.setDisable(true);
                geodeticRadio.setDisable(false);

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
            mapSizeSelector.getSelectionModel().select(mapOptions.getMapSize() + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        optionsBox.setDisable(false);
        isSettingValues = false;
    }

    protected void mapLoaded() {
        optionsBox.setDisable(false);
        isSettingValues = false;
    }

    public void drawPoints() {
        if (isSettingValues || mapController == null) {
            return;
        }
        mapController.drawPoints();
    }

    public void setMapSize(int size) {
        if (mapController != null && !mapController.mapLoaded || isSettingValues) {
            return;
        }
        mapSizeSelector.getSelectionModel().select(size + "");
    }

    public void setStandardLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (standardLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(standardOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(standardOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(standardOpacitySelector);
                }
                mapOptions.setStandardOpacity(opacity);
            } else {
                mapOptions.setStandardLayer(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setSatelliteLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (satelliteLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(satelliteOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(satelliteOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(satelliteOpacitySelector);
                }
                mapOptions.setSatelliteOpacity(opacity);
            } else {
                mapOptions.setSatelliteLayer(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setRoadLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (roadLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(roadOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(roadOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(roadOpacitySelector);
                }
                mapOptions.setRoadOpacity(opacity);
            } else {
                mapOptions.setRoadLayer(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setTrafficLayer() {
        try {
            if (isSettingValues) {
                return;
            }
            if (trafficLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(trafficOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(trafficOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(trafficOpacitySelector);
                }
                mapOptions.setTrafficOpacity(opacity);
            } else {
                mapOptions.setTrafficLayer(false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void initImageFile(File file) {
        try {
            if (isSettingValues || file == null || !file.exists()) {
                return;
            }
            isSettingValues = true;
            markerImageRadio.setSelected(true);
            markerImageInput.setText(file.getAbsolutePath());
            isSettingValues = false;
            mapOptions.setMarkerImageFile(file);
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    @FXML
    public void selectMarkerImage() {
        try {
            if (isSettingValues) {
                return;
            }
            selectMarkerImage(FxFileTools.selectFile(this));
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    public void selectMarkerImage(File file) {
        try {
            if (isSettingValues || file == null || !file.exists()) {
                return;
            }
            markerImageInput.setText(file.getAbsolutePath());
            recordFileOpened(file);
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    public void showMarkerImageMenu(Event event) {
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
                selectMarkerImage(file);
            }

        }.pop();
    }

    @FXML
    public void pickMarkerImage(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectMarkerImage();
        } else {
            showMarkerImageMenu(event);
        }
    }

    @FXML
    public void popMarkerImage(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showMarkerImageMenu(event);
        }
    }

    @FXML
    public void aboutCoordinateSystem() {
        mapController.aboutCoordinateSystem();
    }

}
