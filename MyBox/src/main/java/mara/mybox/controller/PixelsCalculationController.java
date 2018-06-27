/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.controller.ImageConverterAttributesController.RatioAdjustion;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-25
 * @Description
 * @License Apache License Version 2.0
 */
public class PixelsCalculationController extends BaseController {

    private List<String> predefinedDiaplayValues, predeinfedPrintValues, predeinfedPhotoValues, predeinfedIconValues;

    private ImageAttributes parentAttributes;
    private TextField parentXInput, parentYInput;
    private int x, y, density;
    private float inchX, inchY, cmX, cmY;
    private boolean useInch;

    @FXML
    private ToggleGroup sizeGroup, DensityGroup, predefinedGroup;
    @FXML
    private Label sourceLabel, targetLabel, adjustLabel;
    @FXML
    private TextField widthInches, heightInches, widthCM, heightCM, densityInput;
    @FXML
    private ComboBox<String> predeinfedDisplayList, predeinfedIconList, predeinfedPrintList, predeinfedPhotoList;
    @FXML
    private Button useButton;

    @Override
    protected void initializeNext() {
        try {
            sourceLabel.setText("");
            targetLabel.setText("");

            predefinedGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) predefinedGroup.getSelectedToggle();
                    if (selected.getText().equals(AppVaribles.getMessage("Display"))) {
                        determineValues(predeinfedDisplayList.getSelectionModel().getSelectedItem());
                    } else if (selected.getText().equals(AppVaribles.getMessage("Photo"))) {
                        determineValues(predeinfedPhotoList.getSelectionModel().getSelectedItem());
                    } else if (selected.getText().equals(AppVaribles.getMessage("Icon"))) {
                        determineValues(predeinfedIconList.getSelectionModel().getSelectedItem());
                    } else if (selected.getText().equals(AppVaribles.getMessage("Print"))) {
                        determineValues(predeinfedPrintList.getSelectionModel().getSelectedItem());
                    }
                }
            });

            definePredefinedDisplayValues();
            predeinfedDisplayList.getItems().addAll(predefinedDiaplayValues);
            predeinfedDisplayList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    RadioButton selected = (RadioButton) predefinedGroup.getSelectedToggle();
                    if (selected.getText().equals(AppVaribles.getMessage("Display"))) {
                        determineValues((String) newValue);
                    }
                }
            });
            predeinfedDisplayList.getSelectionModel().select(3);

            definePredefinedPhotoValues();
            predeinfedPhotoList.getItems().addAll(predeinfedPhotoValues);
            predeinfedPhotoList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    RadioButton selected = (RadioButton) predefinedGroup.getSelectedToggle();
                    if (selected.getText().equals(AppVaribles.getMessage("Photo"))) {
                        determineValues((String) newValue);
                    }
                }
            });
            predeinfedPhotoList.getSelectionModel().select(1);

            definePredefinedIconValues();
            predeinfedIconList.getItems().addAll(predeinfedIconValues);
            predeinfedIconList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    RadioButton selected = (RadioButton) predefinedGroup.getSelectedToggle();
                    if (selected.getText().equals(AppVaribles.getMessage("Icon"))) {
                        determineValues((String) newValue);
                    }
                }
            });
            predeinfedIconList.getSelectionModel().select(2);

            definePredefinedPrintValues();
            predeinfedPrintList.getItems().addAll(predeinfedPrintValues);
            predeinfedPrintList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    RadioButton selected = (RadioButton) predefinedGroup.getSelectedToggle();
                    if (selected.getText().equals(AppVaribles.getMessage("Print"))) {
                        determineValues((String) newValue);
                    }
                }
            });
            predeinfedPrintList.getSelectionModel().select(1);

            DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkDensity();
                }
            });
            FxmlTools.setRadioSelected(DensityGroup, AppVaribles.getConfigValue("density", null));
            checkDensity();
            densityInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkDensity();
                }
            });
            densityInput.setText(AppVaribles.getConfigValue("densityInput", null));
            FxmlTools.setNonnegativeValidation(densityInput);

            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
                    useInch = selected.getText().equals(AppVaribles.getMessage("Inches"));
                    if (useInch) {
                        if (inchX > 0) {
                            widthCM.setText(Math.round(inchX * 254.0f) / 100.0f + "");
                            widthCM.setStyle(null);
                        }
                        if (inchY > 0) {
                            heightCM.setText(Math.round(inchY * 254.0f) / 100.0f + "");
                            heightCM.setStyle(null);
                        }
                    } else {
                        if (cmX > 0) {
                            widthInches.setText(Math.round(cmX * 100 / 2.54f) / 100.0f + "");
                            widthInches.setStyle(null);
                        }
                        if (cmY > 0) {
                            heightInches.setText(Math.round(cmY * 100 / 2.54f) / 100.0f + "");
                            heightInches.setStyle(null);
                        }
                    }
                    calculateValues();
                }
            });

            widthInches.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        inchX = Float.valueOf(newValue);
                        widthInches.setStyle(null);
                        AppVaribles.setConfigValue("widthInches", inchX + "");
                        if (useInch) {
                            widthCM.setText(Math.round(inchX * 254.0f) / 100.0f + "");
                            widthCM.setStyle(null);
                        }
                    } catch (Exception e) {
                        inchX = 0;
                        widthInches.setStyle(badStyle);
                    }
                    calculateValues();
                }
            });
            widthInches.setText(AppVaribles.getConfigValue("widthInches", "1"));

            heightInches.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        inchY = Float.valueOf(newValue);
                        heightInches.setStyle(null);
                        AppVaribles.setConfigValue("heightInches", inchY + "");
                        if (useInch) {
                            heightCM.setText(Math.round(inchY * 254.0f) / 100.0f + "");
                            heightCM.setStyle(null);
                        }
                    } catch (Exception e) {
                        inchY = 0;
                        heightInches.setStyle(badStyle);
                    }
                    calculateValues();
                }
            });
            heightInches.setText(AppVaribles.getConfigValue("heightInches", "1.5"));

            widthCM.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        cmX = Float.valueOf(newValue);
                        widthCM.setStyle(null);
                        AppVaribles.setConfigValue("widthCM", cmX + "");
                        if (!useInch) {
                            widthInches.setText(Math.round(cmX * 100 / 2.54f) / 100.0f + "");
                            widthInches.setStyle(null);
                        }
                    } catch (Exception e) {
                        cmX = 0;
                        widthCM.setStyle(badStyle);
                    }
                    calculateValues();
                }
            });
            widthCM.setText(AppVaribles.getConfigValue("widthCM", "21.0"));

            heightCM.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        cmY = Float.valueOf(newValue);
                        heightCM.setStyle(null);
                        AppVaribles.setConfigValue("heightCM", cmY + "");
                        if (!useInch) {
                            heightInches.setText(Math.round(cmY * 100 / 2.54f) / 100.0f + "");
                            heightInches.setStyle(null);
                        }
                    } catch (Exception e) {
                        cmY = 0;
                        heightCM.setStyle(badStyle);
                    }
                    calculateValues();
                }
            });
            heightCM.setText(AppVaribles.getConfigValue("heightCM", "29.7"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkDensity() {
        try {
            RadioButton selected = (RadioButton) DensityGroup.getSelectedToggle();
            String s = selected.getText();
            densityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(densityInput.getText());
                if (inputValue > 0) {
                    AppVaribles.setConfigValue("densityInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue > 0) {
                    density = inputValue;
                    AppVaribles.setConfigValue("density", s);
                } else {
                    density = 0;
                    densityInput.setStyle(FxmlTools.badStyle);
                }

            } else {
                density = Integer.parseInt(s.substring(0, s.length() - 3));
                AppVaribles.setConfigValue("density", s);
            }
            calculateValues();

        } catch (Exception e) {
            density = 0;
            logger.error(e.toString());
        }
    }

    private void definePredefinedDisplayValues() {
        predefinedDiaplayValues = new ArrayList();
        predefinedDiaplayValues.add("960x540    qHD    16:9");
        predefinedDiaplayValues.add("1280x720   720p   16:9");
        predefinedDiaplayValues.add("1366x768   WXGA   16:9");
        predefinedDiaplayValues.add("1920x1080  1080p  16:9");
        predefinedDiaplayValues.add("2560x1440  QHD    16:9");
        predefinedDiaplayValues.add("----------------------------");
        predefinedDiaplayValues.add("640x480    VGA    4:3");
        predefinedDiaplayValues.add("800x600    SVGA   4:3");
        predefinedDiaplayValues.add("1024x768   XGA    4:3");
        predefinedDiaplayValues.add("1400x1050  SXGA+  4:3");
        predefinedDiaplayValues.add("1600x1200  UXGA   4:3");
        predefinedDiaplayValues.add("2048x1536  QXGA   4:3");
        predefinedDiaplayValues.add("----------------------------");
        predefinedDiaplayValues.add("1920x1200  WUXGA  16:10");
        predefinedDiaplayValues.add("1680x1050  WSXGA+ 16:10");
        predefinedDiaplayValues.add("1440x900   WXGA+  16:10");
        predefinedDiaplayValues.add("1280x800   WXGA   16:10");
        predefinedDiaplayValues.add("1024x600   WSVGA  16:10 ");
        predefinedDiaplayValues.add("800x480    WVGA   16:10");
        predefinedDiaplayValues.add("1280x1024  SXGA   5:4");

    }

    private void definePredefinedPhotoValues() {
        String inch = AppVaribles.getMessage("inches");
        String cm = AppVaribles.getMessage("cm");
        predeinfedPhotoValues = new ArrayList();
        predeinfedPhotoValues.add("416x277    " + AppVaribles.getMessage("ChineseIDCard") + "           3.3" + cm + "x2.2" + cm + "    320dpi");
        predeinfedPhotoValues.add("416x605    " + AppVaribles.getMessage("ChinesePassport") + "            3.3" + cm + "x4.8" + cm + "    320dpi");
        predeinfedPhotoValues.add("---------------------------------------------------------");
        predeinfedPhotoValues.add("208x140    " + AppVaribles.getMessage("ChineseIDCard") + "           3.3" + cm + "x2.2" + cm + "    160dpi");
        predeinfedPhotoValues.add("208x304    " + AppVaribles.getMessage("ChinesePassport") + "            3.3" + cm + "x4.8" + cm + "    160dpi");
        predeinfedPhotoValues.add("---------------------------------------------------------");
        predeinfedPhotoValues.add("320x480       1" + inch + "x1.5" + inch + "   2.5" + cm + "x3.5" + cm + "    320dpi");
        predeinfedPhotoValues.add("480x640    1.5" + inch + "x2" + inch + "   3.5" + cm + "x4.9" + cm + "    320dpi");
        predeinfedPhotoValues.add("1600x1200   5" + inch + "x3.5" + inch + "   12.7" + cm + "x8.9" + cm + "    320dpi");
        predeinfedPhotoValues.add("1920x1280  6" + inch + "x4" + inch + "     15.2" + cm + "x10.2" + cm + "    320dpi");
        predeinfedPhotoValues.add("2240x1600  7" + inch + "x5" + inch + "     17.8" + cm + "x12.7" + cm + "    320dpi");
        predeinfedPhotoValues.add("1920x2560  6" + inch + "x8" + inch + "     15.2" + cm + "x20.3" + cm + "    320dpi");
        predeinfedPhotoValues.add("3200x2560  10" + inch + "x8" + inch + "    25.4" + cm + "x20.3" + cm + "    320dpi");
        predeinfedPhotoValues.add("3840x2560  12" + inch + "x8" + inch + "    30.5" + cm + "x20.3" + cm + "    320dpi");
        predeinfedPhotoValues.add("3200x3840  10" + inch + "x12" + inch + "   25.4" + cm + "x30.5" + cm + "    320dpi");
        predeinfedPhotoValues.add("4800x3200  15" + inch + "x10" + inch + "   38.1" + cm + "x25.4" + cm + "    320dpi");
        predeinfedPhotoValues.add("---------------------------------------------------------");
        predeinfedPhotoValues.add("160x240       1" + inch + "x1.5" + inch + "   2.5" + cm + "x3.5" + cm + "    160dpi");
        predeinfedPhotoValues.add("240x320    1.5" + inch + "x2" + inch + "   3.5" + cm + "x4.9" + cm + "    160dpi");
        predeinfedPhotoValues.add("800x600   5" + inch + "x3.5" + inch + "   12.7" + cm + "x8.9" + cm + "    160dpi");
        predeinfedPhotoValues.add("960x640  6" + inch + "x4" + inch + "     15.2" + cm + "x10.2" + cm + "    160dpi");
        predeinfedPhotoValues.add("1120x800  7" + inch + "x5" + inch + "     17.8" + cm + "x12.7" + cm + "    160dpi");
        predeinfedPhotoValues.add("960x1280  6" + inch + "x8" + inch + "     15.2" + cm + "x20.3" + cm + "    160dpi");
        predeinfedPhotoValues.add("1600x1280  10" + inch + "x8" + inch + "    25.4" + cm + "x20.3" + cm + "    160dpi");
        predeinfedPhotoValues.add("1920x1280  12" + inch + "x8" + inch + "    30.5" + cm + "x20.3" + cm + "    160dpi");
        predeinfedPhotoValues.add("1600x1920  10" + inch + "x12" + inch + "   25.4" + cm + "x30.5" + cm + "    160dpi");
        predeinfedPhotoValues.add("2400x1600  15" + inch + "x10" + inch + "   38.1" + cm + "x25.4" + cm + "    160dpi");

    }

    private void definePredefinedIconValues() {
        predeinfedIconValues = new ArrayList();

        predeinfedIconValues.add("--------- Android Icons Size ----------");
        predeinfedIconValues.add("36x36      Low           120dpi");
        predeinfedIconValues.add("48x48      Medium        160dpi");
        predeinfedIconValues.add("72x72      High          240dpi");
        predeinfedIconValues.add("96x96      Extra-high    320dpi");
        predeinfedIconValues.add("144x144    xx-high       480dpi");
        predeinfedIconValues.add("192x192    xxx-high      640dpi");

        // https://developer.apple.com/library/archive/qa/qa1686/_index.html
        predeinfedIconValues.add("--------- Apple Icons Size ----------");
        predeinfedIconValues.add("512x512    iTunesArtwork          App list in iTunes");
        predeinfedIconValues.add("1024x1024  iTunesArtwork@2x       App list in iTunes for devices with retina display");
        predeinfedIconValues.add("120x120    Icon-60@2x.png         Home screen on iPhone/iPod Touch with retina display");
        predeinfedIconValues.add("180x180    Icon-60@3x.png         Home screen on iPhone with retina HD display");
        predeinfedIconValues.add("76x76      Icon-76.png            Home screen on iPad");
        predeinfedIconValues.add("152x152    Icon-76@2x.png         Home screen on iPad with retina display");
        predeinfedIconValues.add("167x167    Icon-83.5@2x.png       Home screen on iPad Pro");
        predeinfedIconValues.add("40x40      Icon-Small-40.png      Spotlight ");
        predeinfedIconValues.add("80x80      Icon-Small-40@2x.png   Spotlight on devices with retina display ");
        predeinfedIconValues.add("120x120    Icon-Small-40@3x.png   Spotlight on devices with retina HD display");
        predeinfedIconValues.add("29x29      Icon-Small.png         Settings ");
        predeinfedIconValues.add("58x58      Icon-Small@2x.png      Settings on devices with retina display ");
        predeinfedIconValues.add("87x87      Icon-Small@3x.png      Settings on devices with retina HD display ");
        predeinfedIconValues.add("57x57      Icon.png               Home screen on iPhone/iPod touch (iOS 6.1 and earlier) ");
        predeinfedIconValues.add("114x114    Icon@2x.png            Home screen on iPhone/iPod Touch with retina display (iOS 6.1 and earlier) ");
        predeinfedIconValues.add("72x72      Icon-72.png            Home screen on iPad (iOS 6.1 and earlier) ");
        predeinfedIconValues.add("144x144    Icon-72@2x.png         Home screen on iPad with retina display (iOS 6.1 and earlier)");
        predeinfedIconValues.add("50x50      Icon-Small-50.png      Spotlight on iPad (iOS 6.1 and earlier) ");
        predeinfedIconValues.add("100x100    Icon-Small-50@2x.png   Spotlight on iPad with retina display (iOS 6.1 and earlier) ");

    }

    private void definePredefinedPrintValues() {
        predeinfedPrintValues = new ArrayList();

        predeinfedPrintValues.add("--------- 300dpi ----------");
        predeinfedPrintValues.add("2480x3508      A4 (16k)  21.0cm x 29.7cm     300dpi");
        predeinfedPrintValues.add("1748x2480      A5 (32k)  14.8cm x 21.0cm     300dpi");
        predeinfedPrintValues.add("1240x1748      A6 (64k)  10.5cm x 14.8cm     300dpi");
        predeinfedPrintValues.add("3508x4960      A3 (8k)   29.7cm x 42.0cm     300dpi");
        predeinfedPrintValues.add("4960x7015      A2 (4k)   42.0cm x 59.4cm     300dpi");
        predeinfedPrintValues.add("7015x9933      A1 (2k)   59.4cm x 84.1cm     300dpi");
        predeinfedPrintValues.add("9933x14043     A0 (1k)   84.1cm x 118.9cm    300dpi");
        predeinfedPrintValues.add("2079x2953      B5        17.6cm x 25.0cm     300dpi");
        predeinfedPrintValues.add("2953x4169      B4	    25.0cm x 35.3cm     300dpi");
        predeinfedPrintValues.add("4169x4906      B2	    35.3cm x 50.0cm     300dpi");
        predeinfedPrintValues.add("2705x3827      C4	    22.9cm x 32.4cm     300dpi");
        predeinfedPrintValues.add("1913x2705      C5	    16.2cm x 22.9cm     300dpi");
        predeinfedPrintValues.add("1347x1913      C6	    11.4cm x 16.2cm     300dpi");

    }

    @FXML
    private void close(ActionEvent event) {
        getMyStage().close();
    }

    @FXML
    private void useResult(ActionEvent event) {
        if (x <= 0 || y <= 0) {
            popInformation(AppVaribles.getMessage("Invalid"));
            return;
        }
        if (parentXInput != null && parentYInput != null) {
            parentXInput.setText(x + "");
            parentYInput.setText(y + "");
        }
        getMyStage().close();
    }

    public void setSource(ImageAttributes parentAttributes, TextField parentXInput, TextField parentYInput) {
        this.parentAttributes = parentAttributes;
        this.parentXInput = parentXInput;
        this.parentYInput = parentYInput;
        if (parentAttributes.getSourceWidth() <= 0 && parentAttributes.getSourceHeight() <= 0) {
            return;
        }
        String label = AppVaribles.getMessage("SourceImagePixelsNumber") + ": "
                + parentAttributes.getSourceWidth() + "x" + parentAttributes.getSourceHeight()
                + "         "
                + AppVaribles.getMessage("KeepRatio") + ": " + AppVaribles.getMessage(parentAttributes.isKeepRatio() + "");
        if (parentAttributes.isKeepRatio()) {
            int rd = parentAttributes.getRatioAdjustion();
            if (rd == RatioAdjustion.BaseOnWidth) {
                label += "  " + AppVaribles.getMessage("BaseOnWidth");
            } else if (rd == RatioAdjustion.BaseOnHeight) {
                label += "  " + AppVaribles.getMessage("BaseOnHeight");
            } else if (rd == RatioAdjustion.BaseOnLarger) {
                label += "  " + AppVaribles.getMessage("BaseOnLarger");
            } else if (rd == RatioAdjustion.BaseOnSmaller) {
                label += "  " + AppVaribles.getMessage("BaseOnSamller");
            }
        }
        sourceLabel.setText(label);
        useButton.setVisible(true);
        adjustValues();
    }

    private void determineValues(String v) {
        if (v == null || v.isEmpty() || v.startsWith("--")) {
            return;
        }
        String p = v.split(" ")[0];
        String[] vs = p.split("x");
        if (vs.length != 2) {
            return;
        }
        x = Integer.valueOf(vs[0]);
        y = Integer.valueOf(vs[1]);
        String label = AppVaribles.getMessage("SelectedPixelsNumber") + ": " + v;
        targetLabel.setText(label);
        adjustValues();
    }

    private void calculateValues() {
        x = Math.round(cmX * density / 2.54f);
        y = Math.round(cmY * density / 2.54f);
        String label = AppVaribles.getMessage("CalculatedPixelsNumber") + ": "
                + x + "x" + y + "       " + cmX + "cm x " + cmY + " cm   "
                + density + "dpi";
        if (x <= 0 || y <= 0) {
            label += "   " + AppVaribles.getMessage("Invalid");
        } else {
            adjustValues();
        }
        targetLabel.setText(label);
    }

    private void adjustValues() {
        adjustLabel.setText("");
        if (x <= 0 || y <= 0
                || parentAttributes == null || !parentAttributes.isKeepRatio()) {
            return;
        }

        int sourceX = parentAttributes.getSourceWidth();
        int sourceY = parentAttributes.getSourceHeight();
        if (sourceX <= 0 || sourceY <= 0) {
            return;
        }

        long ratioX = Math.round(x * 1000 / sourceX);
        long ratioY = Math.round(y * 1000 / sourceY);
        if (ratioX <= 0 || ratioY <= 0) {
            return;
        }
        if (ratioX == ratioY) {
            return;
        }
        int rd = parentAttributes.getRatioAdjustion();
        if (rd == RatioAdjustion.BaseOnWidth) {
            y = Math.round(x * sourceY / sourceX);
        } else if (rd == RatioAdjustion.BaseOnHeight) {
            x = Math.round(y * sourceX / sourceY);
        } else if (rd == RatioAdjustion.BaseOnLarger) {
            if (ratioX > ratioY) {
                y = Math.round(x * sourceY / sourceX);
            } else {
                x = Math.round(y * sourceX / sourceY);
            }
        } else if (rd == RatioAdjustion.BaseOnSmaller) {
            if (ratioX > ratioY) {
                x = Math.round(y * sourceX / sourceY);
            } else {
                y = Math.round(x * sourceY / sourceX);
            }
        }
        String label = AppVaribles.getMessage("AdjustedPixelsNumber") + ": "
                + x + "x" + y;
        adjustLabel.setText(label);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public TextField getParentXInput() {
        return parentXInput;
    }

    public void setParentXInput(TextField parentXInput) {
        this.parentXInput = parentXInput;
    }

    public TextField getParentYInput() {
        return parentYInput;
    }

    public void setParentYInput(TextField parentYInput) {
        this.parentYInput = parentYInput;
    }

}
