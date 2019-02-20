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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.ImageConvert.KeepRatioType;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.data.ImageAttributes;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.tools.ValueTools;

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
    private int finalX, finalY, cp_density, cs_density, sourceX, sourceY, selectX, selectY, cs_X, cs_Y, cd_X, cd_Y;
    private float cp_inchX, cp_inchY, cp_cmX, cp_cmY;
    private float cd_inchX, cd_inchY, cd_cmX, cd_cmY;
    private boolean fromSource, cp_useInch, cd_useInch;

    @FXML
    private ToggleGroup cp_sizeGroup, cp_DensityGroup, predefinedGroup, ratioGroup, cs_DensityGroup, cd_sizeGroup;
    @FXML
    private TextField targetLabel, adjustLabel;
    @FXML
    private TextField cp_widthInches, cp_heightInches, cp_widthCM, cp_heightCM, cp_densityInput;
    @FXML
    private ComboBox<String> predeinfedDisplayList, predeinfedIconList, predeinfedPrintList, predeinfedPhotoList;
    @FXML
    private Button useButton;
    @FXML
    private HBox cd_pixelsBox, sourcePixelsBox, cs_pixelsBox, ratioBox, sourceBox, adjustBox;
    @FXML
    private TextField cs_width, cs_height, cs_densityInput;
    @FXML
    private TextField source_width, source_height;
    @FXML
    private TextField cd_width, cd_height, cd_heightInches, cd_widthInches, cd_widthCM, cd_heightCM;
    @FXML
    private CheckBox sourceCheck, radioCheck;
    @FXML
    private Label ratioLabel;
    @FXML
    private TabPane tabPane;

    @Override
    protected void initializeNext() {
        try {
            fromSource = false;
            targetLabel.setText("");
            adjustLabel.setText("");
            ratioLabel.setText("");

            init_source();
            initPredefined();
            init_cp();
            init_cs();
            init_cd();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> ov, Tab oldValue, Tab newValue) {
                    recalculate();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void recalculate() {
        String tab = tabPane.getSelectionModel().getSelectedItem().getText();
        if (AppVaribles.getMessage("PredefinedPixelsNumber").equals(tab)) {
            predefined_determineValues();
        } else if (AppVaribles.getMessage("CalculatePixelsNumber").equals(tab)) {
            cp_calculateValues();
        } else if (AppVaribles.getMessage("CalculateOutputSize").equals(tab)) {
            cs_calculateValues();
        } else if (AppVaribles.getMessage("CalculateOutputDensity").equals(tab)) {
            cd_calculateValues();
        }
    }

    private void initPredefined() {
        predefinedGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                predefined_determineValues();
            }
        });

        definePredefinedDisplayValues();
        predeinfedDisplayList.getItems().addAll(predefinedDiaplayValues);
        predeinfedDisplayList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                predefined_determineValues();
            }
        });
        predeinfedDisplayList.getSelectionModel().select(3);

        definePredefinedPhotoValues();
        predeinfedPhotoList.getItems().addAll(predeinfedPhotoValues);
        predeinfedPhotoList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                predefined_determineValues();
            }
        });
        predeinfedPhotoList.getSelectionModel().select(1);

        definePredefinedIconValues();
        predeinfedIconList.getItems().addAll(predeinfedIconValues);
        predeinfedIconList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                predefined_determineValues();
            }
        });
        predeinfedIconList.getSelectionModel().select(2);

        definePredefinedPrintValues();
        predeinfedPrintList.getItems().addAll(predeinfedPrintValues);
        predeinfedPrintList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                predefined_determineValues();
            }
        });
        predeinfedPrintList.getSelectionModel().select(1);
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

    private void init_source() {
        sourceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                if (!fromSource) {
                    sourcePixelsBox.setDisable(!sourceCheck.isSelected());
                    ratioBox.setDisable(!sourceCheck.isSelected());
                }
                recalculate();
            }
        });

        radioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                if (!fromSource) {
                    adjustBox.setDisable(!newValue);
                }
                recalculate();
            }
        });

        ratioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                recalculate();
            }
        });

        source_width.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    sourceX = Integer.valueOf(newValue);
                    if (sourceY > 0) {
                        ratioLabel.setText(AppVaribles.getMessage("AspectRatio") + ": "
                                + ValueTools.roundDouble3(1.0f * sourceX / sourceY));
                    }
                    recalculate();
                } catch (Exception e) {
                    sourceX = 0;
                    ratioLabel.setText("");
                }
            }
        });
        FxmlTools.setNonnegativeValidation(source_width);

        source_height.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    sourceY = Integer.valueOf(newValue);
                    if (sourceX > 0) {
                        ratioLabel.setText(AppVaribles.getMessage("AspectRatio") + ": "
                                + ValueTools.roundDouble3(1.0f * sourceX / sourceY));
                    }
                    recalculate();
                } catch (Exception e) {
                    sourceY = 0;
                    ratioLabel.setText("");
                }
            }
        });
        FxmlTools.setNonnegativeValidation(source_height);

    }

    private void init_cp() {
        cp_DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                cp_checkDensity();
            }
        });
//        FxmlTools.setRadioSelected(cp_DensityGroup, AppVaribles.getUserConfigValue("density", null));
        cp_checkDensity();
        cp_densityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                cp_checkDensity();
            }
        });
//        cp_densityInput.setText(AppVaribles.getUserConfigValue("densityInput", null));
        FxmlTools.setNonnegativeValidation(cp_densityInput);

        cp_sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton selected = (RadioButton) cp_sizeGroup.getSelectedToggle();
                cp_useInch = selected.getText().equals(AppVaribles.getMessage("Inches"));
                if (cp_useInch) {
                    if (cp_inchX > 0) {
                        cp_widthCM.setText(Math.round(cp_inchX * 254.0f) / 100.0f + "");
                        cp_widthCM.setStyle(null);
                    }
                    if (cp_inchY > 0) {
                        cp_heightCM.setText(Math.round(cp_inchY * 254.0f) / 100.0f + "");
                        cp_heightCM.setStyle(null);
                    }
                } else {
                    if (cp_cmX > 0) {
                        cp_widthInches.setText(Math.round(cp_cmX * 100 / 2.54f) / 100.0f + "");
                        cp_widthInches.setStyle(null);
                    }
                    if (cp_cmY > 0) {
                        cp_heightInches.setText(Math.round(cp_cmY * 100 / 2.54f) / 100.0f + "");
                        cp_heightInches.setStyle(null);
                    }
                }
                cp_calculateValues();
            }
        });

        cp_widthInches.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cp_inchX = Float.valueOf(newValue);
                    cp_widthInches.setStyle(null);
                    AppVaribles.setUserConfigValue("widthInches", cp_inchX + "");
                    if (cp_useInch) {
                        cp_widthCM.setText(Math.round(cp_inchX * 254.0f) / 100.0f + "");
                        cp_widthCM.setStyle(null);
                    }
                } catch (Exception e) {
                    cp_inchX = 0;
                    cp_widthInches.setStyle(badStyle);
                }
                cp_calculateValues();
            }
        });
//        cp_widthInches.setText(AppVaribles.getUserConfigValue("widthInches", "1"));

        cp_heightInches.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cp_inchY = Float.valueOf(newValue);
                    cp_heightInches.setStyle(null);
                    AppVaribles.setUserConfigValue("heightInches", cp_inchY + "");
                    if (cp_useInch) {
                        cp_heightCM.setText(Math.round(cp_inchY * 254.0f) / 100.0f + "");
                        cp_heightCM.setStyle(null);
                    }
                } catch (Exception e) {
                    cp_inchY = 0;
                    cp_heightInches.setStyle(badStyle);
                }
                cp_calculateValues();
            }
        });
//        cp_heightInches.setText(AppVaribles.getUserConfigValue("heightInches", "1.5"));

        cp_widthCM.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cp_cmX = Float.valueOf(newValue);
                    cp_widthCM.setStyle(null);
                    AppVaribles.setUserConfigValue("widthCM", cp_cmX + "");
                    if (!cp_useInch) {
                        cp_widthInches.setText(Math.round(cp_cmX * 100 / 2.54f) / 100.0f + "");
                        cp_widthInches.setStyle(null);
                    }
                } catch (Exception e) {
                    cp_cmX = 0;
                    cp_widthCM.setStyle(badStyle);
                }
                cp_calculateValues();
            }
        });
//        cp_widthCM.setText(AppVaribles.getUserConfigValue("widthCM", "21.0"));

        cp_heightCM.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cp_cmY = Float.valueOf(newValue);
                    cp_heightCM.setStyle(null);
                    AppVaribles.setUserConfigValue("heightCM", cp_cmY + "");
                    if (!cp_useInch) {
                        cp_heightInches.setText(Math.round(cp_cmY * 100 / 2.54f) / 100.0f + "");
                        cp_heightInches.setStyle(null);
                    }
                } catch (Exception e) {
                    cp_cmY = 0;
                    cp_heightCM.setStyle(badStyle);
                }
                cp_calculateValues();
            }
        });
//        cp_heightCM.setText(AppVaribles.getUserConfigValue("heightCM", "29.7"));
    }

    private void init_cs() {
        cs_width.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cs_X = Integer.valueOf(newValue);
                } catch (Exception e) {
                    cs_X = 0;
                }
                cs_calculateValues();
            }
        });
        FxmlTools.setNonnegativeValidation(cs_width);

        cs_height.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cs_Y = Integer.valueOf(newValue);
                } catch (Exception e) {
                    cs_Y = 0;
                }
                cs_calculateValues();
            }
        });
        FxmlTools.setNonnegativeValidation(cs_height);

        cs_DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                cs_checkDensity();
            }
        });
//        FxmlTools.setRadioSelected(cp_DensityGroup, AppVaribles.getUserConfigValue("density", null));
        cs_checkDensity();
        cs_densityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                cs_checkDensity();
            }
        });
//        cp_densityInput.setText(AppVaribles.getUserConfigValue("densityInput", null));
        FxmlTools.setNonnegativeValidation(cs_densityInput);

    }

    private void init_cd() {
        cd_width.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cd_X = Integer.valueOf(newValue);
                } catch (Exception e) {
                    cd_X = 0;
                }
                cd_calculateValues();
            }
        });
        FxmlTools.setNonnegativeValidation(cs_width);

        cd_height.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cd_Y = Integer.valueOf(newValue);
                } catch (Exception e) {
                    cd_Y = 0;
                }
                cd_calculateValues();
            }
        });
        FxmlTools.setNonnegativeValidation(cs_height);

        cd_sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                RadioButton selected = (RadioButton) cd_sizeGroup.getSelectedToggle();
                cd_useInch = selected.getText().equals(AppVaribles.getMessage("Inches"));
                if (cd_useInch) {
                    if (cd_inchX > 0) {
                        cd_widthCM.setText(Math.round(cd_inchX * 254.0f) / 100.0f + "");
                        cd_widthCM.setStyle(null);
                    }
                    if (cd_inchY > 0) {
                        cd_heightCM.setText(Math.round(cd_inchY * 254.0f) / 100.0f + "");
                        cd_heightCM.setStyle(null);
                    }
                } else {
                    if (cd_cmX > 0) {
                        cd_widthInches.setText(Math.round(cd_cmX * 100 / 2.54f) / 100.0f + "");
                        cd_widthInches.setStyle(null);
                    }
                    if (cd_cmY > 0) {
                        cd_heightInches.setText(Math.round(cd_cmY * 100 / 2.54f) / 100.0f + "");
                        cd_heightInches.setStyle(null);
                    }
                }
                cd_calculateValues();
            }
        });

        cd_widthInches.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cd_inchX = Float.valueOf(newValue);
                    cd_widthInches.setStyle(null);
                    AppVaribles.setUserConfigValue("widthInches", cd_inchX + "");
                    if (cd_useInch) {
                        cd_widthCM.setText(Math.round(cd_inchX * 254.0f) / 100.0f + "");
                        cd_widthCM.setStyle(null);
                    }
                } catch (Exception e) {
                    cd_inchX = 0;
                    cd_widthInches.setStyle(badStyle);
                }
                cd_calculateValues();
            }
        });
//        cd_widthInches.setText(AppVaribles.getUserConfigValue("widthInches", "1"));

        cd_heightInches.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cd_inchY = Float.valueOf(newValue);
                    cd_heightInches.setStyle(null);
                    AppVaribles.setUserConfigValue("heightInches", cd_inchY + "");
                    if (cd_useInch) {
                        cd_heightCM.setText(Math.round(cd_inchY * 254.0f) / 100.0f + "");
                        cd_heightCM.setStyle(null);
                    }
                } catch (Exception e) {
                    cd_inchY = 0;
                    cd_heightInches.setStyle(badStyle);
                }
                cd_calculateValues();
            }
        });
//        cd_heightInches.setText(AppVaribles.getUserConfigValue("heightInches", "1.5"));

        cd_widthCM.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cd_cmX = Float.valueOf(newValue);
                    cd_widthCM.setStyle(null);
                    AppVaribles.setUserConfigValue("widthCM", cd_cmX + "");
                    if (!cd_useInch) {
                        cd_widthInches.setText(Math.round(cd_cmX * 100 / 2.54f) / 100.0f + "");
                        cd_widthInches.setStyle(null);
                    }
                } catch (Exception e) {
                    cd_cmX = 0;
                    cd_widthCM.setStyle(badStyle);
                }
                cd_calculateValues();
            }
        });
//        cd_widthCM.setText(AppVaribles.getUserConfigValue("widthCM", "21.0"));

        cd_heightCM.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    cd_cmY = Float.valueOf(newValue);
                    cd_heightCM.setStyle(null);
                    AppVaribles.setUserConfigValue("heightCM", cd_cmY + "");
                    if (!cd_useInch) {
                        cd_heightInches.setText(Math.round(cd_cmY * 100 / 2.54f) / 100.0f + "");
                        cd_heightInches.setStyle(null);
                    }
                } catch (Exception e) {
                    cd_cmY = 0;
                    cd_heightCM.setStyle(badStyle);
                }
                cd_calculateValues();
            }
        });
//        cd_heightCM.setText(AppVaribles.getUserConfigValue("heightCM", "29.7"));

    }

    @FXML
    private void close(ActionEvent event) {
        closeStage();
    }

    @FXML
    private void useResult(ActionEvent event) {
        if (finalX <= 0 || finalY <= 0) {
            alertInformation(AppVaribles.getMessage("Invalid"));
            return;
        }
        if (parentXInput != null) {
            parentXInput.setText(finalX + "");
        }
        if (parentYInput != null) {
            parentYInput.setText(finalY + "");
        }
        closeStage();
    }

    public void setSource(final ImageAttributes parentAttributes,
            final TextField parentXInput, final TextField parentYInput) {
        try {
            fromSource = true;
            this.parentAttributes = parentAttributes;
            this.parentXInput = parentXInput;
            this.parentYInput = parentYInput;

            sourceCheck.setSelected(true);
            sourceCheck.setDisable(true);
            sourcePixelsBox.setDisable(true);
            ratioBox.setDisable(true);

            if (parentAttributes != null
                    && parentAttributes.getSourceWidth() > 0 && parentAttributes.getSourceHeight() > 0) {
                source_width.setText(parentAttributes.getSourceWidth() + "");
                source_height.setText(parentAttributes.getSourceHeight() + "");
                radioCheck.setSelected(parentAttributes.isKeepRatio());
                if (parentAttributes.isKeepRatio()) {
                    int rd = parentAttributes.getRatioAdjustion();
                    switch (rd) {
                        case KeepRatioType.BaseOnWidth:
                            FxmlTools.setRadioSelected(ratioGroup, AppVaribles.getMessage("BaseOnWidth"));
                            break;
                        case KeepRatioType.BaseOnHeight:
                            FxmlTools.setRadioSelected(ratioGroup, AppVaribles.getMessage("BaseOnHeight"));
                            break;
                        case KeepRatioType.BaseOnLarger:
                            FxmlTools.setRadioSelected(ratioGroup, AppVaribles.getMessage("BaseOnLarger"));
                            break;
                        case KeepRatioType.BaseOnSmaller:
                            FxmlTools.setRadioSelected(ratioGroup, AppVaribles.getMessage("BaseOnSamller"));
                            break;
                        default:
                            break;
                    }
                }

                cs_width.setText(parentAttributes.getSourceWidth() + "");
                cs_height.setText(parentAttributes.getSourceHeight() + "");

                cd_width.setText(parentAttributes.getSourceWidth() + "");
                cd_height.setText(parentAttributes.getSourceHeight() + "");

            } else {
                radioCheck.setSelected(false);
            }

            if (this.parentXInput != null || this.parentYInput != null) {
                useButton.setVisible(true);
                useButton.setDisable(true);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void predefined_determineValues() {
        RadioButton selected = (RadioButton) predefinedGroup.getSelectedToggle();
        if (selected == null) {
            return;
        }
        String v = null;
        if (selected.getText().equals(AppVaribles.getMessage("Photo"))) {
            v = predeinfedPhotoList.getSelectionModel().getSelectedItem();
        } else if (selected.getText().equals(AppVaribles.getMessage("Display"))) {
            v = predeinfedDisplayList.getSelectionModel().getSelectedItem();
        } else if (selected.getText().equals(AppVaribles.getMessage("Print"))) {
            v = predeinfedPrintList.getSelectionModel().getSelectedItem();
        } else if (selected.getText().equals(AppVaribles.getMessage("Icon"))) {
            v = predeinfedIconList.getSelectionModel().getSelectedItem();
        }

        if (v == null || v.isEmpty() || v.startsWith("--")) {
            return;
        }
        String p = v.split(" ")[0];
        String[] vs = p.split("x");
        if (vs.length != 2) {
            return;
        }
        selectX = Integer.valueOf(vs[0]);
        selectY = Integer.valueOf(vs[1]);
        String label = AppVaribles.getMessage("SelectedPixelsNumber") + ": " + v + "  "
                + AppVaribles.getMessage("AspectRatio") + ": "
                + ValueTools.roundDouble3(1.0f * selectX / selectY);
        targetLabel.setText(label);
        useButton.setDisable(false);
        adjustValues();
    }

    private void cp_checkDensity() {
        try {
            RadioButton selected = (RadioButton) cp_DensityGroup.getSelectedToggle();
            String s = selected.getText();
            cp_densityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(cp_densityInput.getText());
                if (inputValue > 0) {
                    AppVaribles.setUserConfigValue("densityInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue > 0) {
                    cp_density = inputValue;
                    AppVaribles.setUserConfigValue("density", s);
                } else {
                    cp_density = 0;
                    cp_densityInput.setStyle(FxmlTools.badStyle);
                }

            } else {
                cp_density = Integer.parseInt(s.substring(0, s.length() - 3));
                AppVaribles.setUserConfigValue("density", s);
            }
            cp_calculateValues();

        } catch (Exception e) {
            cp_density = 0;
            logger.error(e.toString());
        }
    }

    private void cp_calculateValues() {
        selectX = Math.round(cp_cmX * cp_density / 2.54f);
        selectY = Math.round(cp_cmY * cp_density / 2.54f);
        if (selectX <= 0 || selectY <= 0) {
            useButton.setDisable(true);
            targetLabel.setText("");
            adjustLabel.setText("");
            return;
        }
        String label = AppVaribles.getMessage("CalculatedPixelsNumber") + ": "
                + selectX + "x" + selectY + "       " + cp_cmX + "cm x " + cp_cmY + " cm   "
                + cp_density + "dpi" + "   "
                + AppVaribles.getMessage("AspectRatio") + ": "
                + ValueTools.roundDouble3(1.0f * selectX / selectY);
        targetLabel.setText(label);
        adjustValues();
    }

    private void cs_checkDensity() {
        try {
            RadioButton selected = (RadioButton) cs_DensityGroup.getSelectedToggle();
            String s = selected.getText();
            cs_densityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(cs_densityInput.getText());
                if (inputValue > 0) {
                    AppVaribles.setUserConfigValue("densityInput", inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue > 0) {
                    cs_density = inputValue;
                    AppVaribles.setUserConfigValue("density", s);
                } else {
                    cs_density = 0;
                    cs_densityInput.setStyle(FxmlTools.badStyle);
                }

            } else {
                cs_density = Integer.parseInt(s.substring(0, s.length() - 3));
                AppVaribles.setUserConfigValue("density", s);
            }
            cs_calculateValues();

        } catch (Exception e) {
            cs_density = 0;
            logger.error(e.toString());
        }
    }

    private void cs_calculateValues() {
        selectX = cs_X;
        selectY = cs_Y;
        if (cs_X <= 0 || cs_Y <= 0 || cs_density <= 0) {
            useButton.setDisable(true);
            targetLabel.setText("");
            adjustLabel.setText("");
            return;
        }
        double cmX = ValueTools.roundDouble2(selectX * 2.54f / cs_density);
        double cmY = ValueTools.roundDouble2(selectY * 2.54f / cs_density);
        String label = AppVaribles.getMessage("CalculatedPixelsNumber") + ": "
                + selectX + "x" + selectY + "       " + cmX + "cm x " + cmY + " cm   "
                + cs_density + "dpi" + "   "
                + AppVaribles.getMessage("AspectRatio") + ": "
                + ValueTools.roundDouble3(1.0f * selectX / selectY);
        targetLabel.setText(label);
        adjustValues();
    }

    private void cd_calculateValues() {
        selectX = cd_X;
        selectY = cd_Y;
        if (!cd_useInch) {
            cd_inchX = cd_cmX / 2.54f;
            cd_inchY = cd_cmY / 2.54f;
        }
        if (cd_X <= 0 || cd_Y <= 0
                || (cd_useInch && (cd_inchX <= 0 || cd_inchY <= 0))
                || (!cd_useInch && (cd_cmX <= 0 || cd_cmY <= 0))) {
            useButton.setDisable(true);
            targetLabel.setText("");
            adjustLabel.setText("");
            return;
        }
        int densityX = Math.round(cd_X / cd_inchX);
        int densityY = Math.round(cd_Y / cd_inchY);
        String label = AppVaribles.getMessage("CalculatedPixelsNumber") + ": "
                + selectX + "x" + selectY + "       " + cd_cmX + "cm x " + cd_cmY + " cm   "
                + densityX + "dpi x " + densityY + "dpi   "
                + AppVaribles.getMessage("AspectRatio") + ": "
                + ValueTools.roundDouble3(1.0f * selectX / selectY);
        targetLabel.setText(label);
        adjustValues();

    }

    private void adjustValues() {
        adjustLabel.setText("");
        finalX = selectX;
        finalY = selectY;
        if (selectX <= 0 || selectY <= 0) {
            useButton.setDisable(true);
            targetLabel.setText("");
            return;
        }
        useButton.setDisable(false);

        if (!sourceCheck.isSelected() || !radioCheck.isSelected() || sourceX <= 0 || sourceY <= 0) {
            adjustLabel.setText("");
            return;
        }

        long ratioX = Math.round(selectX * 1000 / sourceX);
        long ratioY = Math.round(selectY * 1000 / sourceY);
        if (ratioX <= 0 || ratioY <= 0 || ratioX == ratioY) {
            return;
        }

        RadioButton selected = (RadioButton) ratioGroup.getSelectedToggle();
        String s = selected.getText();
        if (getMessage("BaseOnWidth").equals(s)) {
            finalY = Math.round(sourceY * selectX / sourceX);
        } else if (getMessage("BaseOnHeight").equals(s)) {
            finalX = Math.round(sourceX * selectY / sourceY);
        } else if (getMessage("BaseOnLarger").equals(s)) {
            if (ratioX > ratioY) {
                finalY = Math.round(sourceY * selectX / sourceX);
            } else {
                finalX = Math.round(sourceX * selectY / sourceY);
            }
        } else if (getMessage("BaseOnSamller").equals(s)) {
            if (ratioX > ratioY) {
                finalX = Math.round(sourceX * selectY / sourceY);
            } else {
                finalY = Math.round(sourceY * selectX / sourceX);
            }
        } else {
            return;
        }

        String label = AppVaribles.getMessage("AdjustedPixelsNumber") + ": "
                + finalX + "x" + finalY + "   "
                + AppVaribles.getMessage("AspectRatio") + ": "
                + ValueTools.roundDouble3(1.0f * finalX / finalY);
        adjustLabel.setText(label);
    }

    public int getFinalX() {
        return finalX;
    }

    public void setFinalX(int finalX) {
        this.finalX = finalX;
    }

    public int getFinalY() {
        return finalY;
    }

    public void setFinalY(int finalY) {
        this.finalY = finalY;
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
