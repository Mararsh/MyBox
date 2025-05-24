package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-8-6
 * @License Apache License Version 2.0
 */
public class MapOptionsController extends BaseController {

    protected BaseMapController mapController;

    @FXML
    protected CheckBox fitViewCheck, popInfoCheck,
            standardLayerCheck, satelliteLayerCheck, roadLayerCheck, trafficLayerCheck,
            zoomCheck, scaleCheck, typeCheck, symbolsCheck,
            markerLabelCheck, markerCoordinateCheck, boldCheck;
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
            styleDefaultRadio, styleIndigoRadio, styleBlackRadio;
    @FXML
    protected VBox optionsBox, mapBox, languageBox, controlsBox, layersBox, sizeBox,
            markerTextBox, markerImageBox;
    @FXML
    protected FlowPane baseTextPane, textColorPane;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected ControlImage imageController;

    public MapOptionsController() {
        baseTitle = message("MapOptions");
        TipsLabelKey = "MapComments";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    public void setParameters(BaseMapController controller) {
        try {
            if (controller == null) {
                close();
                return;
            }
            mapController = controller;
            mapController.optionsController = this;
            imageController.setParameter(this,
                    mapController.defaultMarkerImage,
                    mapController.markerImage.getAbsolutePath());

            setControlListeners();

            setControlValues();

            mapTypeChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setControlListeners() {
        try {
            mapGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        if (isSettingValues) {
                            return;
                        }
                        mapTypeChanged();
                        if (mapController != null) {
                            mapController.setMapType(gaodeRadio.isSelected() ? "GaoDe" : "TianDiTu",
                                    geodeticRadio.isSelected());
                        }
                    }
            );

            projectionGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        if (isSettingValues || mapController == null) {
                            return;
                        }
                        mapController.setMapType(gaodeRadio.isSelected() ? "GaoDe" : "TianDiTu",
                                geodeticRadio.isSelected());
                    }
            );

            langGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        if (isSettingValues || mapController == null) {
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
                        mapController.setLanguage(lang);
                    }
            );

//            mapStyleGroup.selectedToggleProperty().addListener(
//                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
//                        if (isSettingValues || mapController == null) {
//                            return;
//                        }
//                        String style;
//                        if (styleIndigoRadio.isSelected()) {
//                            style = "indigo";
//                        } else if (styleBlackRadio.isSelected()) {
//                            style = "black";
//                        } else {
//                            style = "default";
//                        }
//                        mapController.setMapStyle(style);
//                    }
//            );
            imageController.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    mapController.applyMarkerImage(imageController.currentAddress);
                }
            });

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
                        if (mapController != null) {
                            mapController.setMarkerSize(v);
                        }
                    } else {
                        markerSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    markerSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

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
                                if (mapController != null) {
                                    mapController.setMapZoom(v);
                                }
                                mapSizeSelector.getEditor().setStyle(null);
                            } else {
                                mapSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            mapSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

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
                                if (mapController != null) {
                                    mapController.setTextSize(v);
                                }
                                textSizeSelector.getEditor().setStyle(null);
                            } else {
                                textSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            textSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            colorSetController.init(this, baseName + "Color", Color.BLACK);
            colorSetController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (isSettingValues || mapController == null) {
                        return;
                    }
                    mapController.setTextColor(colorSetController.color());
                }
            });

            markerLabelCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues || mapController == null) {
                            return;
                        }
                        mapController.setMarkerLabel(markerLabelCheck.isSelected());
                    });

            markerCoordinateCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setMarkerCoordinate(markerCoordinateCheck.isSelected());
            });

            boldCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setBold(boldCheck.isSelected());
            });

            popInfoCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setPopInfo(popInfoCheck.isSelected());
            });

            fitViewCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setIsFitView(fitViewCheck.isSelected());
            });

            zoomCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setShowZoom(zoomCheck.isSelected());
            });

            scaleCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setShowScale(scaleCheck.isSelected());
            });

            typeCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setShowType(typeCheck.isSelected());
            });

            symbolsCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues || mapController == null) {
                    return;
                }
                mapController.setShowSymbols(symbolsCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setControlValues() {
        if (mapController == null) {
            return;
        }
        try {
            isSettingValues = true;
            if (mapController.isGaoDeMap()) {
                gaodeRadio.setSelected(true);
            } else {
                tiandituRadio.setSelected(true);
            }
            geodeticRadio.setSelected(mapController.isGeodetic);

            mapSizeSelector.setValue(mapController.mapZoom + "");

            markerSizeSelector.setValue(mapController.markerSize + "");
            textSizeSelector.setValue(mapController.textSize + "");
            markerLabelCheck.setSelected(mapController.isMarkLabel);
            markerCoordinateCheck.setSelected(mapController.isMarkCoordinate);
            boldCheck.setSelected(mapController.isBold);

            popInfoCheck.setSelected(mapController.isPopInfo);
            fitViewCheck.setSelected(mapController.isFitView);

            zoomCheck.setSelected(mapController.showZoomControl);
            scaleCheck.setSelected(mapController.showScaleControl);
            typeCheck.setSelected(mapController.showTypeControl);
            symbolsCheck.setSelected(mapController.showSymbolsControl);

            standardLayerCheck.setSelected(mapController.showStandardLayer);
            satelliteLayerCheck.setSelected(mapController.showSatelliteLayer);
            roadLayerCheck.setSelected(mapController.showRoadLayer);
            trafficLayerCheck.setSelected(mapController.showTrafficLayer);

            standardOpacitySelector.setValue(mapController.standardOpacity + "");
            roadOpacitySelector.setValue(mapController.roadOpacity + "");
            satelliteOpacitySelector.setValue(mapController.satelliteOpacity + "");
            trafficOpacitySelector.setValue(mapController.trafficOpacity + "");

            if ("zh_en".equals(mapController.language)) {
                chineseEnglishRadio.setSelected(true);
            } else if ("en".equals(mapController.language)) {
                englishRadio.setSelected(true);
            } else {
                chineseRadio.setSelected(true);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        isSettingValues = false;
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
            mapSizeSelector.getSelectionModel().select(mapController.mapZoom + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        optionsBox.setDisable(false);
        isSettingValues = false;
    }

    public void takeMapZoom(int zoom) {
        isSettingValues = true;
        mapSizeSelector.setValue(zoom + "");
        isSettingValues = false;
    }

    public void setStandardLayer() {
        try {
            if (isSettingValues || mapController == null) {
                return;
            }
            if (standardLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(standardOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(standardOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(standardOpacitySelector);
                }
                mapController.setStandardOpacity(opacity);
            } else {
                mapController.setShowStandardLayer(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setSatelliteLayer() {
        try {
            if (isSettingValues || mapController == null) {
                return;
            }
            if (satelliteLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(satelliteOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(satelliteOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(satelliteOpacitySelector);
                }
                mapController.setSatelliteOpacity(opacity);
            } else {
                mapController.setShowSatelliteLayer(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setRoadLayer() {
        try {
            if (isSettingValues || mapController == null) {
                return;
            }
            if (roadLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(roadOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(roadOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(roadOpacitySelector);
                }
                mapController.setRoadOpacity(opacity);
            } else {
                mapController.setShowRoadLayer(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void setTrafficLayer() {
        try {
            if (isSettingValues || mapController == null) {
                return;
            }
            if (trafficLayerCheck.isSelected()) {
                float opacity = Float.parseFloat(trafficOpacitySelector.getValue());
                if (opacity >= 0 && opacity <= 1) {
                    ValidationTools.setEditorNormal(trafficOpacitySelector);
                } else {
                    ValidationTools.setEditorBadStyle(trafficOpacitySelector);
                }
                mapController.setTrafficOpacity(opacity);
            } else {
                mapController.setShowTrafficLayer(false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @FXML
    public void aboutCoordinateSystem() {
        mapController.aboutCoordinateSystem();
    }

    @Override
    public void cleanPane() {
        try {
            if (mapController != null) {
                mapController.optionsController = null;
                mapController = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();

    }

    /*
        static methods
     */
    public static MapOptionsController open(BaseMapController mapController) {
        try {
            if (mapController == null) {
                return null;
            }
            MapOptionsController controller = (MapOptionsController) mapController
                    .referredTopStage(Fxmls.MapOptionsFxml);
            controller.requestMouse();
            controller.setParameters(mapController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
