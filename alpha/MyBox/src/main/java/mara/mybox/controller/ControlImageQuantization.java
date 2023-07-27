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
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.KMeansClustering;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.PopularityQuantization;
import static mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm.RGBUniformQuantization;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageQuantization extends BaseController {

    protected int regionSize, weight1, weight2, weight3,
            quanColors, kmeansLoop;
    protected QuantizationAlgorithm algorithm;

    @FXML
    protected ToggleGroup quanGroup;
    @FXML
    protected RadioButton rgbQuanRadio, hsbQuanRadio, popularQuanRadio, kmeansQuanRadio;
    @FXML
    protected FlowPane regionPane, loopPane;
    @FXML
    protected ComboBox<String> weightSelector, quanColorsSelector,
            regionSizeSelector, kmeansLoopSelector;
    @FXML
    protected CheckBox quanDitherCheck, quanDataCheck, firstColorCheck;
    @FXML
    protected Label weightLabel, actualLoopLabel;
    @FXML
    protected ImageView imageQuantizationTipsView;

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
            regionSizeSelector.getItems().addAll(Arrays.asList("4096", "1024", "256", "8192", "512", "128"));
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

            weightSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        String[] values = newValue.split(":");
                        int v1 = Integer.parseInt(values[0]);
                        int v2 = Integer.parseInt(values[1]);
                        int v3 = Integer.parseInt(values[2]);
                        if (v1 <= 0 || v2 <= 0 || v3 <= 0) {
                            weightSelector.getEditor().setStyle(UserConfig.badStyle());
                            return;
                        }
                        weight1 = v1;
                        weight2 = v2;
                        weight3 = v3;
                        weightSelector.getEditor().setStyle(null);
                        if (!isSettingValues) {
                            UserConfig.setString(baseName + (hsbQuanRadio.isSelected() ? "HSBWeights" : "RGBWeights"), newValue);
                        }
                    } catch (Exception e) {
                        weightSelector.getEditor().setStyle(UserConfig.badStyle());
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
        if (rgbQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.RGBUniformQuantization;
        } else if (hsbQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.HSBUniformQuantization;
        } else if (popularQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.PopularityQuantization;
        } else if (kmeansQuanRadio.isSelected()) {
            algorithm = QuantizationAlgorithm.KMeansClustering;
        } else {
            algorithm = null;
            return;
        }
        switch (algorithm) {
            case HSBUniformQuantization:
                if (thisPane.getChildren().contains(regionPane)) {
                    thisPane.getChildren().removeAll(regionPane, loopPane, actualLoopLabel);
                }
                weightLabel.setText(message("HSBWeight"));
                break;
            case RGBUniformQuantization:
                if (thisPane.getChildren().contains(regionPane)) {
                    thisPane.getChildren().removeAll(regionPane, loopPane, actualLoopLabel);
                }
                weightLabel.setText(message("RGBWeight"));
                break;
            case KMeansClustering:
                if (!thisPane.getChildren().contains(regionPane)) {
                    thisPane.getChildren().add(7, regionPane);
                }
                if (!thisPane.getChildren().contains(loopPane)) {
                    thisPane.getChildren().add(8, loopPane);
                    thisPane.getChildren().add(actualLoopLabel);
                }
                actualLoopLabel.setText("");
                weightLabel.setText(message("RGBWeight"));
                break;
            case PopularityQuantization:
                if (!thisPane.getChildren().contains(regionPane)) {
                    thisPane.getChildren().add(7, regionPane);
                }
                if (thisPane.getChildren().contains(loopPane)) {
                    thisPane.getChildren().removeAll(loopPane, actualLoopLabel);
                }
                weightLabel.setText(message("RGBWeight"));
        }
        isSettingValues = true;
        weightSelector.getItems().clear();
        if (hsbQuanRadio.isSelected()) {
            weight1 = 4;
            weight2 = 4;
            weight3 = 1;
            String defaultV = UserConfig.getString(baseName + "HSBWeights", "6:10:100");
            weightSelector.getItems().addAll(Arrays.asList(
                    "6:10:100", "12:4:10", "24:10:10", "12:10:40", "24:10:40", "12:20:40", "12:10:80", "6:10:80"
            ));
            weightSelector.setValue(defaultV);
        } else {
            weight1 = 2;
            weight2 = 4;
            weight3 = 4;
            String defaultV = UserConfig.getString(baseName + "RGBWeights", "2:4:3");
            weightSelector.getItems().addAll(Arrays.asList(
                    "2:4:3", "1:1:1", "4:4:2", "2:1:1", "21:71:7", "299:587:114", "2126:7152:722"
            ));
            weightSelector.setValue(defaultV);
        }
        isSettingValues = false;

        refreshStyle(thisPane);
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
        weightSelector.setValue("2:4:3");
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
        weightSelector.setValue("2:4:3");
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

    public int getWeight1() {
        return weight1;
    }

    public void setWeight1(int weight1) {
        this.weight1 = weight1;
    }

    public int getWeight2() {
        return weight2;
    }

    public void setWeight2(int weight2) {
        this.weight2 = weight2;
    }

    public int getWeight3() {
        return weight3;
    }

    public void setWeight3(int weight3) {
        this.weight3 = weight3;
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

    public ComboBox<String> getWeightSelector() {
        return weightSelector;
    }

    public void setWeightSelector(ComboBox<String> weightSelector) {
        this.weightSelector = weightSelector;
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

    public Label getWeightLabel() {
        return weightLabel;
    }

    public void setWeightLabel(Label weightLabel) {
        this.weightLabel = weightLabel;
    }

    public Label getActualLoopLabel() {
        return actualLoopLabel;
    }

    public void setActualLoopLabel(Label actualLoopLabel) {
        this.actualLoopLabel = actualLoopLabel;
    }

    public ImageView getImageQuantizationTipsView() {
        return imageQuantizationTipsView;
    }

    public void setImageQuantizationTipsView(ImageView imageQuantizationTipsView) {
        this.imageQuantizationTipsView = imageQuantizationTipsView;
    }

}
