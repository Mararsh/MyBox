package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageQuantization extends BaseController {

    protected int regionSize, quanColors, kmeansLoop,
            rgbWeight1, rgbWeight2, rgbWeight3, hsbWeight1, hsbWeight2, hsbWeight3;
    protected QuantizationAlgorithm algorithm;

    @FXML
    protected ToggleGroup quanGroup;
    @FXML
    protected RadioButton rgbQuanRadio, hsbQuanRadio, popularQuanRadio, kmeansQuanRadio;
    @FXML
    protected VBox setBox;
    @FXML
    protected FlowPane regionPane, numberPane, loopPane, rgbWeightPane, hsbWeightPane;
    @FXML
    protected ComboBox<String> rgbWeightSelector, hsbWeightSelector, quanColorsSelector,
            regionSizeSelector, kmeansLoopSelector;
    @FXML
    protected CheckBox quanDitherCheck, quanDataCheck, firstColorCheck;
    @FXML
    protected Label resultsLabel;
    @FXML
    protected ImageView imageQuantizationTipsView;

    public ControlImageQuantization() {
        TipsLabelKey = "ImageQuantizationComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            quanColors = UserConfig.getInt(baseName + "QuanColorsNumber", 256);
            quanColors = regionSize <= 0 ? 256 : quanColors;
            quanColorsSelector.getItems().addAll(Arrays.asList(
                    "27", "64", "8", "5", "3", "4", "12", "16", "256", "512", "1024", "2048", "4096", "216", "343", "128", "1000", "729", "1728", "8000"));
            quanColorsSelector.setValue(quanColors + "");
            quanColorsSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            quanColors = v;
                            if (!isSettingValues) {
                                UserConfig.setInt(baseName + "QuanColorsNumber", quanColors);
                            }
                            ValidationTools.setEditorNormal(quanColorsSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(quanColorsSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(quanColorsSelector);
                    }
                }
            });

            regionSize = UserConfig.getInt(baseName + "RegionSize", 4096);
            regionSize = regionSize <= 0 ? 4096 : regionSize;
            regionSizeSelector.getItems().addAll(Arrays.asList("4096", "1024", "256", "8192", "2048", "512", "128", "16", "27", "64"));
            regionSizeSelector.setValue(regionSize + "");
            regionSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            regionSize = v;
                            if (!isSettingValues) {
                                UserConfig.setInt(baseName + "RegionSize", regionSize);
                            }
                            regionSizeSelector.getEditor().setStyle(null);
                        } else {
                            regionSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        regionSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            rgbWeight1 = 2;
            rgbWeight2 = 4;
            rgbWeight3 = 3;
            String defaultV = UserConfig.getString(baseName + "RGBWeights", "2:4:3");
            rgbWeightSelector.getItems().addAll(Arrays.asList(
                    "2:4:3", "1:1:1", "4:4:2", "2:1:1", "21:71:7", "299:587:114", "2126:7152:722"
            ));
            rgbWeightSelector.setValue(defaultV);
            rgbWeightSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        String[] values = newValue.split(":");
                        int v1 = Integer.parseInt(values[0]);
                        int v2 = Integer.parseInt(values[1]);
                        int v3 = Integer.parseInt(values[2]);
                        if (v1 <= 0 || v2 <= 0 || v3 <= 0) {
                            rgbWeightSelector.getEditor().setStyle(UserConfig.badStyle());
                            return;
                        }
                        rgbWeight1 = v1;
                        rgbWeight2 = v2;
                        rgbWeight3 = v3;
                        rgbWeightSelector.getEditor().setStyle(null);
                        if (!isSettingValues) {
                            UserConfig.setString(baseName + "RGBWeights", newValue);
                        }
                    } catch (Exception e) {
                        rgbWeightSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            hsbWeight1 = 6;
            hsbWeight2 = 10;
            hsbWeight3 = 100;
            defaultV = UserConfig.getString(baseName + "HSBWeights", "6:10:100");
            hsbWeightSelector.getItems().addAll(Arrays.asList(
                    "6:10:100", "12:4:10", "24:10:10", "12:10:40", "24:10:40", "12:20:40", "12:10:80", "6:10:80"
            ));
            hsbWeightSelector.setValue(defaultV);
            hsbWeightSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        String[] values = newValue.split(":");
                        int v1 = Integer.parseInt(values[0]);
                        int v2 = Integer.parseInt(values[1]);
                        int v3 = Integer.parseInt(values[2]);
                        if (v1 <= 0 || v2 <= 0 || v3 <= 0) {
                            hsbWeightSelector.getEditor().setStyle(UserConfig.badStyle());
                            return;
                        }
                        hsbWeight1 = v1;
                        hsbWeight2 = v2;
                        hsbWeight3 = v3;
                        hsbWeightSelector.getEditor().setStyle(null);
                        if (!isSettingValues) {
                            UserConfig.setString(baseName + "HSBWeights", newValue);
                        }
                    } catch (Exception e) {
                        hsbWeightSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            kmeansLoop = UserConfig.getInt(baseName + "KmeansLoop", 10000);
            kmeansLoop = kmeansLoop <= 0 ? 10000 : kmeansLoop;
            kmeansLoopSelector.getItems().addAll(Arrays.asList(
                    "10000", "5000", "3000", "1000", "500", "100", "20000"));
            kmeansLoopSelector.setValue(kmeansLoop + "");
            kmeansLoopSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            kmeansLoop = v;
                            if (!isSettingValues) {
                                UserConfig.setInt(baseName + "KmeansLoop", kmeansLoop);
                            }
                            ValidationTools.setEditorNormal(kmeansLoopSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(kmeansLoopSelector);
                    }
                }
            });

            quanDitherCheck.setSelected(UserConfig.getBoolean(baseName + "QuanDither", true));
            quanDitherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        UserConfig.setBoolean(baseName + "QuanDither", newValue);
                    }
                }
            });

            quanDataCheck.setSelected(UserConfig.getBoolean(baseName + "QuanData", true));
            quanDataCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        UserConfig.setBoolean(baseName + "QuanData", newValue);
                    }
                }
            });

            firstColorCheck.setSelected(UserConfig.getBoolean(baseName + "QuanFirstColor", true));
            firstColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        UserConfig.setBoolean(baseName + "QuanFirstColor", newValue);
                    }
                }
            });

            quanGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) {
                    checkAlgorithm();
                }
            });

            checkAlgorithm();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkAlgorithm() {
        setBox.getChildren().clear();
        resultsLabel.setText("");

        if (rgbQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.RGBUniformQuantization;
            setBox.getChildren().addAll(numberPane, rgbWeightPane);

        } else if (hsbQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.HSBUniformQuantization;
            setBox.getChildren().addAll(numberPane, hsbWeightPane);

        } else if (popularQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.PopularityQuantization;
            setBox.getChildren().addAll(numberPane, regionPane, rgbWeightPane);

        } else if (kmeansQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.KMeansClustering;
            setBox.getChildren().addAll(numberPane, regionPane, rgbWeightPane);

        } else {
            algorithm = null;
        }

        refreshStyle(setBox);
    }

    protected void defaultForAnalyse() {
        isSettingValues = true;

        kmeansQuanRadio.setSelected(true);

        quanDataCheck.setVisible(false);
        quanDataCheck.setSelected(true);

        quanDitherCheck.setSelected(true);
        firstColorCheck.setSelected(true);

        quanColorsSelector.setValue("5");
        regionSizeSelector.setValue("256");
        rgbWeightSelector.setValue("2:4:3");
        kmeansLoopSelector.setValue("10000");

        isSettingValues = false;
    }

    protected void defaultForSvg() {
        isSettingValues = true;

        kmeansQuanRadio.setSelected(true);

        quanDataCheck.setVisible(false);
        quanDataCheck.setSelected(true);

        quanDitherCheck.setSelected(true);
        firstColorCheck.setSelected(true);

        quanColorsSelector.setValue("16");
        regionSizeSelector.setValue("4096");
        rgbWeightSelector.setValue("2:4:3");
        kmeansLoopSelector.setValue("10000");

        isSettingValues = false;
    }

    /*
        get
     */
    public int getRegionSize() {
        return regionSize;
    }

    public void setRegionSize(int regionSize) {
        this.regionSize = regionSize;
    }

    public int getQuanColors() {
        return quanColors;
    }

    public void setQuanColors(int quanColors) {
        this.quanColors = quanColors;
    }

    public int getKmeansLoop() {
        return kmeansLoop;
    }

    public void setKmeansLoop(int kmeansLoop) {
        this.kmeansLoop = kmeansLoop;
    }

    public QuantizationAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(QuantizationAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getRgbWeight1() {
        return rgbWeight1;
    }

    public void setRgbWeight1(int rgbWeight1) {
        this.rgbWeight1 = rgbWeight1;
    }

    public int getRgbWeight2() {
        return rgbWeight2;
    }

    public void setRgbWeight2(int rgbWeight2) {
        this.rgbWeight2 = rgbWeight2;
    }

    public int getRgbWeight3() {
        return rgbWeight3;
    }

    public void setRgbWeight3(int rgbWeight3) {
        this.rgbWeight3 = rgbWeight3;
    }

    public int getHsbWeight1() {
        return hsbWeight1;
    }

    public void setHsbWeight1(int hsbWeight1) {
        this.hsbWeight1 = hsbWeight1;
    }

    public int getHsbWeight2() {
        return hsbWeight2;
    }

    public void setHsbWeight2(int hsbWeight2) {
        this.hsbWeight2 = hsbWeight2;
    }

    public int getHsbWeight3() {
        return hsbWeight3;
    }

    public void setHsbWeight3(int hsbWeight3) {
        this.hsbWeight3 = hsbWeight3;
    }

    public VBox getSetBox() {
        return setBox;
    }

    public void setSetBox(VBox setBox) {
        this.setBox = setBox;
    }

    public FlowPane getNumberPane() {
        return numberPane;
    }

    public void setNumberPane(FlowPane numberPane) {
        this.numberPane = numberPane;
    }

    public FlowPane getRgbWeightPane() {
        return rgbWeightPane;
    }

    public void setRgbWeightPane(FlowPane rgbWeightPane) {
        this.rgbWeightPane = rgbWeightPane;
    }

    public FlowPane getHsbWeightPane() {
        return hsbWeightPane;
    }

    public void setHsbWeightPane(FlowPane hsbWeightPane) {
        this.hsbWeightPane = hsbWeightPane;
    }

    public ComboBox<String> getRgbWeightSelector() {
        return rgbWeightSelector;
    }

    public void setRgbWeightSelector(ComboBox<String> rgbWeightSelector) {
        this.rgbWeightSelector = rgbWeightSelector;
    }

    public ComboBox<String> getHsbWeightSelector() {
        return hsbWeightSelector;
    }

    public void setHsbWeightSelector(ComboBox<String> hsbWeightSelector) {
        this.hsbWeightSelector = hsbWeightSelector;
    }

    public ToggleGroup getQuanGroup() {
        return quanGroup;
    }

    public void setQuanGroup(ToggleGroup quanGroup) {
        this.quanGroup = quanGroup;
    }

    public RadioButton getRgbQuanRadio() {
        return rgbQuanRadio;
    }

    public void setRgbQuanRadio(RadioButton rgbQuanRadio) {
        this.rgbQuanRadio = rgbQuanRadio;
    }

    public RadioButton getHsbQuanRadio() {
        return hsbQuanRadio;
    }

    public void setHsbQuanRadio(RadioButton hsbQuanRadio) {
        this.hsbQuanRadio = hsbQuanRadio;
    }

    public RadioButton getPopularQuanRadio() {
        return popularQuanRadio;
    }

    public void setPopularQuanRadio(RadioButton popularQuanRadio) {
        this.popularQuanRadio = popularQuanRadio;
    }

    public RadioButton getKmeansQuanRadio() {
        return kmeansQuanRadio;
    }

    public void setKmeansQuanRadio(RadioButton kmeansQuanRadio) {
        this.kmeansQuanRadio = kmeansQuanRadio;
    }

    public FlowPane getRegionPane() {
        return regionPane;
    }

    public void setRegionPane(FlowPane regionPane) {
        this.regionPane = regionPane;
    }

    public FlowPane getLoopPane() {
        return loopPane;
    }

    public void setLoopPane(FlowPane loopPane) {
        this.loopPane = loopPane;
    }

    public ComboBox<String> getQuanColorsSelector() {
        return quanColorsSelector;
    }

    public void setQuanColorsSelector(ComboBox<String> quanColorsSelector) {
        this.quanColorsSelector = quanColorsSelector;
    }

    public ComboBox<String> getRegionSizeSelector() {
        return regionSizeSelector;
    }

    public void setRegionSizeSelector(ComboBox<String> regionSizeSelector) {
        this.regionSizeSelector = regionSizeSelector;
    }

    public ComboBox<String> getKmeansLoopSelector() {
        return kmeansLoopSelector;
    }

    public void setKmeansLoopSelector(ComboBox<String> kmeansLoopSelector) {
        this.kmeansLoopSelector = kmeansLoopSelector;
    }

    public CheckBox getQuanDitherCheck() {
        return quanDitherCheck;
    }

    public void setQuanDitherCheck(CheckBox quanDitherCheck) {
        this.quanDitherCheck = quanDitherCheck;
    }

    public CheckBox getQuanDataCheck() {
        return quanDataCheck;
    }

    public void setQuanDataCheck(CheckBox quanDataCheck) {
        this.quanDataCheck = quanDataCheck;
    }

    public CheckBox getFirstColorCheck() {
        return firstColorCheck;
    }

    public void setFirstColorCheck(CheckBox firstColorCheck) {
        this.firstColorCheck = firstColorCheck;
    }

    public Label getResultsLabel() {
        return resultsLabel;
    }

    public void setResultsLabel(Label resultsLabel) {
        this.resultsLabel = resultsLabel;
    }

    public ImageView getImageQuantizationTipsView() {
        return imageQuantizationTipsView;
    }

    public void setImageQuantizationTipsView(ImageView imageQuantizationTipsView) {
        this.imageQuantizationTipsView = imageQuantizationTipsView;
    }

}
