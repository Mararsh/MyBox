package mara.mybox.controller;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.bufferedimage.ImageColorSpace;
import mara.mybox.color.CIEColorSpace;
import mara.mybox.color.CIEData;
import mara.mybox.color.CIEDataTools;
import mara.mybox.color.ChromaticityDiagram;
import mara.mybox.color.ChromaticityDiagram.DataType;
import mara.mybox.color.ColorValue;
import mara.mybox.color.SRGB;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-5-20
 * @License Apache License Version 2.0
 */
public class ChromaticityDiagramController extends BaseImageController {

    protected boolean isLine, inputInit = true;
    protected int dotSize, fontSize;
    protected java.awt.Color bgColor, calculateColor;
    protected final ObservableList<ColorValue> calculatedValues = FXCollections.observableArrayList();
    protected ObservableList<CIEData> degree2nm1Data, degree10nm1Data, degree2nm5Data, degree10nm5Data;
    protected double X, Y = 1, Z, x = 0.4, y = 0.5;

    @FXML
    protected TabPane displayPane;
    @FXML
    protected Tab diaTab, cie21Tab, cie25Tab, cie101Tab, cie105Tab;
    @FXML
    protected ComboBox<String> fontSelector;
    @FXML
    protected CheckBox cdProPhotoCheck, cdColorMatchCheck, cdNTSCCheck, cdPALCheck, cdAppleCheck, cdAdobeCheck,
            cdSRGBCheck, cdECICheck, cdCIECheck, cdSMPTECCheck, degree2Check, degree10Check,
            waveCheck, whitePointsCheck, cdGridCheck, calculateCheck, inputCheck;
    @FXML
    protected TextArea sourceInputArea, sourceDataArea;
    @FXML
    protected HtmlTableController calculateViewController,
            d2n1Controller, d2n5Controller, d10n1Controller, d10n5Controller;
    @FXML
    protected TextField XInput, YInput, ZInput, xInput, yInput;
    @FXML
    protected Button okSizeButton, calculateXYZButton, calculateXYButton, displayDataButton;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected ToggleGroup bgGroup, dotGroup;
    @FXML
    protected RadioButton bgTransparentRadio, bgWhiteRadio, bgBlackRadio,
            dotLine4pxRadio, dotDot6pxRadio, dotDot10pxRadio, dotDot4pxRadio, dotDot12pxRadio,
            dotLine1pxRadio, dotLine2pxRadio, dotLine6pxRadio, dotLine10pxRadio;

    public ChromaticityDiagramController() {
        baseTitle = Languages.message("DrawChromaticityDiagram");
        TipsLabelKey = "ChromaticityDiagramTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDisplay();
            initDataBox();
            initCIEData();
            initDiagram();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(YInput, new Tooltip(Languages.message("1-based")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private void initDisplay() {
        try {
            bgGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkBackground();
                }
            });

            dotGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    checkDotType();
                }
            });

            List<String> fontList = Arrays.asList("20", "24", "28", "30", "18", "16", "15", "14", "12", "10");
            fontSelector.getItems().addAll(fontList);
            fontSelector.setVisibleRowCount(fontList.size());
            fontSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkFontSize();
                }
            });

            calculateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    if (!isSettingValues) {
                        displayChromaticityDiagram();
                    }
                }
            });
            inputCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            waveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdGridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            whitePointsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            degree2Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            degree10Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdProPhotoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdColorMatchCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdNTSCCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdPALCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdAppleCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdAdobeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdSRGBCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdECICheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdCIECheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdSMPTECCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });

            isSettingValues = true;
            bgColor = null;
            isLine = true;
            dotSize = 4;
            bgTransparentRadio.setSelected(true);
            dotLine4pxRadio.setSelected(true);
            fontSelector.getSelectionModel().select(0);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void initDiagram() {
        try {
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    calculateColor();
                }
            });

            imageView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                        refinePane();
                    }
                }
            });
            imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                        refinePane();
                    }
                }
            });
            scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                        refinePane();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    private void initDataBox() {
        try {
            XInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        X = Double.parseDouble(newValue);
                        XInput.setStyle(null);
                    } catch (Exception e) {
                        XInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            YInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        Y = Double.parseDouble(newValue);
                        if (Y == 0) {
                            YInput.setStyle(UserConfig.badStyle());
                        } else {
                            YInput.setStyle(null);
                        }
                    } catch (Exception e) {
                        YInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            ZInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        Z = Double.parseDouble(newValue);
                        ZInput.setStyle(null);
                    } catch (Exception e) {
                        ZInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        x = Double.parseDouble(newValue);
                        double z = 1 - x - y;
                        if (x > 1 || x < 0 || z < 0 || z > 1) {
                            xInput.setStyle(UserConfig.badStyle());
                        } else {
                            xInput.setStyle(null);
                        }
                    } catch (Exception e) {
                        xInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        y = Double.parseDouble(newValue);
                        double z = 1 - x - y;
                        if (y > 1 || y <= 0 || z < 0 || z > 1) {
                            yInput.setStyle(UserConfig.badStyle());
                        } else {
                            yInput.setStyle(null);
                        }
                    } catch (Exception e) {
                        yInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            calculateXYZButton.disableProperty().bind(Bindings.isEmpty(XInput.textProperty())
                    .or(XInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(YInput.textProperty()))
                    .or(YInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(ZInput.textProperty()))
                    .or(ZInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

            calculateXYButton.disableProperty().bind(Bindings.isEmpty(xInput.textProperty())
                    .or(xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(yInput.textProperty()))
                    .or(yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

            displayDataButton.disableProperty().bind(Bindings.isEmpty(sourceDataArea.textProperty())
            );

            sourceInputArea.setStyle(" -fx-text-fill: gray;");
            sourceInputArea.setText(Languages.message("ChromaticityDiagramTips"));
            sourceInputArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(
                        ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (inputInit) {
                        sourceInputArea.clear();
                        sourceInputArea.setStyle(null);
                        inputInit = false;
                    }
                }
            });

            sourceInputArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    checkInputs();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkInputs() {
        String data = CIEDataTools.cieString(sourceInputArea.getText());
        if (data != null) {
            sourceDataArea.setText(data);
        } else {
            popError(Languages.message("NoData"));
            sourceDataArea.clear();
        }
    }

    private void checkDotType() {
        isLine = false;
        if (dotDot6pxRadio.isSelected()) {
            dotSize = 6;
        } else if (dotDot10pxRadio.isSelected()) {
            dotSize = 10;
        } else if (dotDot4pxRadio.isSelected()) {
            dotSize = 4;
        } else if (dotDot12pxRadio.isSelected()) {
            dotSize = 12;
        } else if (dotLine4pxRadio.isSelected()) {
            isLine = true;
            dotSize = 4;
        } else if (dotLine1pxRadio.isSelected()) {
            isLine = true;
            dotSize = 1;
        } else if (dotLine2pxRadio.isSelected()) {
            isLine = true;
            dotSize = 2;
        } else if (dotLine6pxRadio.isSelected()) {
            isLine = true;
            dotSize = 6;
        } else if (dotLine10pxRadio.isSelected()) {
            isLine = true;
            dotSize = 10;
        } else {
            dotSize = 6;
        }
        if (!isSettingValues) {
            displayChromaticityDiagram();
        }
    }

    private void checkBackground() {
        if (bgTransparentRadio.isSelected()) {
            bgColor = null;
        } else if (bgWhiteRadio.isSelected()) {
            bgColor = java.awt.Color.WHITE;
        } else if (bgBlackRadio.isSelected()) {
            bgColor = java.awt.Color.BLACK;
        } else {
            bgColor = null;
        }
        if (!isSettingValues) {
            displayChromaticityDiagram();
        }
    }

    private void checkFontSize() {
        try {
            int v = Integer.parseInt(fontSelector.getValue());
            if (v > 0) {
                fontSize = v;
                fontSelector.getEditor().setStyle(null);
                if (!isSettingValues) {
                    displayChromaticityDiagram();
                }
            } else {
                fontSelector.getEditor().setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            fontSelector.getEditor().setStyle(UserConfig.badStyle());
        }
    }

    private void initCIEData() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private StringTable degree2nm1Table, degree10nm1Table, degree2nm5Table, degree10nm5Table;

            @Override
            protected boolean handle() {
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

                degree2nm1Data = FXCollections.observableArrayList();
                degree2nm1Data.addAll(CIEDataTools.cie1931Observer2Degree1nmData(cs));
                degree2nm1Table = CIEDataTools.cieTable(degree2nm1Data, cs, Languages.message("CIE1931Observer2DegreeAndSRGB"));

                degree2nm5Data = FXCollections.observableArrayList();
                degree2nm5Data.addAll(CIEDataTools.cie1931Observer2Degree5nmData(cs));
                degree2nm5Table = CIEDataTools.cieTable(degree2nm5Data, cs, Languages.message("CIE1931Observer2DegreeAndSRGB"));

                degree10nm1Data = FXCollections.observableArrayList();
                degree10nm1Data.addAll(CIEDataTools.cie1964Observer10Degree1nmData(cs));
                degree10nm1Table = CIEDataTools.cieTable(degree10nm1Data, cs, Languages.message("CIE1964Observer10DegreeAndSRGB"));

                degree10nm5Data = FXCollections.observableArrayList();
                degree10nm5Data.addAll(CIEDataTools.cie1964Observer10Degree5nmData(cs));
                degree10nm5Table = CIEDataTools.cieTable(degree10nm5Data, cs, Languages.message("CIE1964Observer10DegreeAndSRGB"));

                return true;
            }

            @Override
            protected void whenSucceeded() {
                d2n1Controller.loadTable(degree2nm1Table);
                d2n5Controller.loadTable(degree2nm5Table);
                d10n1Controller.loadTable(degree10nm1Table);
                d10n5Controller.loadTable(degree10nm5Table);

                afterInitCIEData();
            }

        };
        start(task);
    }

    private void afterInitCIEData() {
        colorSetController.init(this, baseName + "Color", Color.THISTLE);
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private String texts;

            @Override
            protected boolean handle() {
                texts = TextFileTools.readTexts(this, file);
                return texts != null;
            }

            @Override
            protected void whenSucceeded() {
                sourceInputArea.setStyle(null);
                inputInit = false;
//                            bottomLabel.setText(file.getAbsolutePath() + "\t" + AppVariables.getMessage("ChromaticityDiagramComments"));
                isSettingValues = true;
                sourceInputArea.setText(texts);
                sourceInputArea.home();
                isSettingValues = false;
                checkInputs();
            }

            @Override
            protected void whenFailed() {
                popError(Languages.message("NoData"));
                sourceDataArea.clear();
            }

        };
        start(task);
    }

    @FXML
    public void displayDataAction() {
        if (sourceDataArea.getText().isEmpty()) {
            return;
        }
        isSettingValues = true;
        inputCheck.setSelected(true);
        isSettingValues = false;
        displayChromaticityDiagram();
    }

    public void showDiagramPathMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, true) {

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPathWrite(VisitHistory.FileType.Image);
            }

            @Override
            public void handleSelect() {
                saveAction();
            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void pickDiagramPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            saveAction();
        } else {
            showDiagramPathMenu(event);
        }
    }

    @FXML
    public void popDiagramPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showDiagramPathMenu(event);
        }
    }

    @FXML
    public void noElements() {
        isSettingValues = true;

        cdProPhotoCheck.setSelected(false);
        cdColorMatchCheck.setSelected(false);
        cdNTSCCheck.setSelected(false);
        cdPALCheck.setSelected(false);
        cdAppleCheck.setSelected(false);
        cdAdobeCheck.setSelected(false);
        cdSRGBCheck.setSelected(false);
        cdECICheck.setSelected(false);
        cdCIECheck.setSelected(false);
        cdSMPTECCheck.setSelected(false);
        degree2Check.setSelected(false);
        degree10Check.setSelected(false);
        waveCheck.setSelected(false);
        inputCheck.setSelected(false);
        calculateCheck.setSelected(false);
        whitePointsCheck.setSelected(false);

        isSettingValues = false;

        displayChromaticityDiagram();
    }

    @FXML
    public void allElements() {
        isSettingValues = true;

        cdProPhotoCheck.setSelected(true);
        cdColorMatchCheck.setSelected(true);
        cdNTSCCheck.setSelected(true);
        cdPALCheck.setSelected(true);
        cdAppleCheck.setSelected(true);
        cdAdobeCheck.setSelected(true);
        cdSRGBCheck.setSelected(true);
        cdECICheck.setSelected(true);
        cdCIECheck.setSelected(true);
        cdSMPTECCheck.setSelected(true);
        degree2Check.setSelected(true);
        degree10Check.setSelected(true);
        waveCheck.setSelected(true);
        inputCheck.setSelected(true);
        calculateCheck.setSelected(true);
        whitePointsCheck.setSelected(true);

        isSettingValues = false;

        displayChromaticityDiagram();
    }

    @Override
    public double scale(double d) {
        return DoubleTools.scale(d, 8);
    }

    protected void calculateColor() {
        CIEData d = new CIEData((Color) colorSetController.rect.getFill());
        isSettingValues = true;
        XInput.setText(scale(d.getX()) + "");
        YInput.setText(scale(d.getY()) + "");
        ZInput.setText(scale(d.getZ()) + "");
        calculateXYZAction();
        isSettingValues = false;
        if (calculateCheck.isSelected()) {
            displayChromaticityDiagram();
        }
    }

    @FXML
    public void calculateXYZAction() {
        CIEData d = new CIEData(-1, X, Y, Z);
        xInput.setText(scale(d.getNormalizedX()) + "");
        yInput.setText(scale(d.getNormalizedY()) + "");
        displayCalculatedValued();
    }

    @FXML
    public void calculateXYAction() {
        CIEData d = new CIEData(x, y);
        XInput.setText(scale(d.getX()) + "");
        YInput.setText(scale(d.getY()) + "");
        ZInput.setText(scale(d.getZ()) + "");
        displayCalculatedValued();
    }

    private void displayCalculatedValued() {
        if (x >= 0 && x <= 1 && y > 0 && y <= 1 && (x + y) <= 1) {
            double[] srgb = CIEColorSpace.XYZd50toSRGBd65(X, Y, Z);
            if (!isSettingValues) {
                isSettingValues = true;
                Color pColor = new Color((float) srgb[0], (float) srgb[1], (float) srgb[2], 1d);
                colorSetController.rect.setFill(pColor);
                isSettingValues = false;
            }
            Color pColor = (Color) colorSetController.rect.getFill();
            calculateColor = new java.awt.Color((float) pColor.getRed(), (float) pColor.getGreen(), (float) pColor.getBlue());

            List<ColorValue> values = new ArrayList<>();
            double[] XYZ = {X, Y, Z};
            values.add(new ColorValue("XYZ", "D50", XYZ));

            double[] xyz = {x, y, 1 - x - y};
            values.add(new ColorValue("xyz", "D50", xyz));

            double[] cieLab = CIEColorSpace.XYZd50toCIELab(X, Y, Z);
            values.add(new ColorValue("CIE-L*ab", "D50", cieLab));

            double[] LCHab = CIEColorSpace.LabtoLCHab(cieLab);
            values.add(new ColorValue("LCH(ab)", "D50", LCHab));

            double[] cieLuv = CIEColorSpace.XYZd50toCIELuv(X, Y, Z);
            values.add(new ColorValue("CIE-L*uv", "D50", cieLuv));

            double[] LCHuv = CIEColorSpace.LuvtoLCHuv(cieLuv);
            values.add(new ColorValue("LCH(uv)", "D50", LCHuv));

            double[] hsb = {pColor.getHue(), pColor.getSaturation(),
                pColor.getBrightness()};
            values.add(new ColorValue("HSB", "D65", hsb));

            values.add(new ColorValue("sRGB", "D65 sRGB_Gamma", srgb, 255));
            double[] sRGBLinear = CIEColorSpace.XYZd50toSRGBd65Linear(X, Y, Z);
            values.add(new ColorValue("sRGB", "D65 Linear", sRGBLinear, 255));

            double[] adobeRGB = CIEColorSpace.XYZd50toAdobeRGBd65(X, Y, Z);
            values.add(new ColorValue("Adobe RGB", "D65 Gamma 2.2", adobeRGB, 255));

            double[] adobeRGBLinear = CIEColorSpace.XYZd50toAdobeRGBd65Linear(X, Y, Z);
            values.add(new ColorValue("Adobe RGB", "D65 Linear", adobeRGBLinear, 255));

            double[] appleRGB = CIEColorSpace.XYZd50toAppleRGBd65(X, Y, Z);
            values.add(new ColorValue("Apple RGB", "D65 Gamma 1.8", appleRGB, 255));

            double[] appleRGBLinear = CIEColorSpace.XYZd50toAppleRGBd65Linear(X, Y, Z);
            values.add(new ColorValue("Apple RGB", "D65 Linear", appleRGBLinear, 255));

            float[] eciRGB = SRGB.srgb2profile(ImageColorSpace.eciRGBProfile(), pColor);
            values.add(new ColorValue("ECI RGB", "D50", FloatTools.toDouble(eciRGB), 255));

            double[] cmyk = SRGB.rgb2cmyk(pColor);
            values.add(new ColorValue("Calculated CMYK", "D65 sRGB_Gamma", cmyk, 100));

            float[] cmyk2 = SRGB.srgb2profile(ImageColorSpace.eciCmykProfile(), pColor);
            values.add(new ColorValue("ECI CMYK", "D65 sRGB_Gamma", FloatTools.toDouble(cmyk2), 100));

            cmyk2 = SRGB.srgb2profile(ImageColorSpace.adobeCmykProfile(), pColor);
            values.add(new ColorValue("Adobe CMYK Uncoated FOGRA29", "D65 sRGB_Gamma", FloatTools.toDouble(cmyk2), 100));

            calculatedValues.clear();
            calculatedValues.addAll(values);

            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(Languages.message("ColorSpace"), Languages.message("Conditions"), Languages.message("Values")));
            StringTable table = new StringTable(names, null);
            for (ColorValue value : calculatedValues) {
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(value.getColorSpace(), value.getConditions(), value.getValues()));
                table.add(row);
            }
            calculateViewController.loadTable(table);

            if (calculateCheck.isSelected()) {
                displayChromaticityDiagram();
            }

        } else {
            calculateColor = null;
            calculatedValues.clear();
            calculateViewController.clear();
        }

    }

    private void displayChromaticityDiagram() {
        if (isSettingValues) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private Image image;

            @Override
            protected boolean handle() {
                try {
                    LinkedHashMap<ChromaticityDiagram.DataType, Boolean> selections = new LinkedHashMap();
                    selections.put(DataType.CIE2Degree, degree2Check.isSelected());
                    selections.put(DataType.CIE10Degree, degree10Check.isSelected());
                    selections.put(DataType.CIEDataSource, inputCheck.isSelected());
                    selections.put(DataType.Calculate, calculateCheck.isSelected());
                    selections.put(DataType.Wave, waveCheck.isSelected());
                    selections.put(DataType.WhitePoints, whitePointsCheck.isSelected());
                    selections.put(DataType.Grid, cdGridCheck.isSelected());
                    selections.put(DataType.CIELines, cdCIECheck.isSelected());
                    selections.put(DataType.ECILines, cdECICheck.isSelected());
                    selections.put(DataType.sRGBLines, cdSRGBCheck.isSelected());
                    selections.put(DataType.AdobeLines, cdAdobeCheck.isSelected());
                    selections.put(DataType.AppleLines, cdAppleCheck.isSelected());
                    selections.put(DataType.PALLines, cdPALCheck.isSelected());
                    selections.put(DataType.NTSCLines, cdNTSCCheck.isSelected());
                    selections.put(DataType.ColorMatchLines, cdColorMatchCheck.isSelected());
                    selections.put(DataType.ProPhotoLines, cdProPhotoCheck.isSelected());
                    selections.put(DataType.SMPTECLines, cdSMPTECCheck.isSelected());

                    ChromaticityDiagram cd = ChromaticityDiagram.create()
                            //                                .setWidth(width).setHeight(height)
                            .setIsLine(isLine).setDotSize(dotSize)
                            .setBgColor(bgColor).setFontSize(fontSize)
                            .setDataSourceTexts(sourceDataArea.getText());
                    if (x >= 0 && x <= 1 && y > 0 && y <= 1) {
                        cd.setCalculateX(x).setCalculateY(y)
                                .setCalculateColor(calculateColor);
                    }
                    image = SwingFXUtils.toFXImage(cd.drawData(selections), null);

                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.debug(e);
                }
                return image != null;
            }

            @Override
            protected void whenSucceeded() {
                imageView.setImage(image);
                ImageViewTools.paneSize(scrollPane, imageView);
            }

        };
        start(task);
    }

    @FXML
    public void sizeAction() {
        if (!isSettingValues) {
            displayChromaticityDiagram();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                "ChromaticityDiagram", FileFilters.ImageExtensionFilter);
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                String format = FileNameTools.ext(file.getName());
                final BufferedImage bufferedImage = FxImageTools.toBufferedImage(imageView.getImage());
                if (this == null || this.isCancelled()) {
                    return false;
                }
                ImageFileWriters.writeImageFile(this, bufferedImage, format, file.getAbsolutePath());
                return true;
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(file, VisitHistory.FileType.Image);
                ImageEditorController.openFile(file);
            }

        };
        start(task);
    }

    @FXML
    public void aboutColor() {
        openHtml(HelpTools.aboutColor());
    }

    @FXML
    @Override
    public boolean popAction() {
        Tab currentTab = displayPane.getSelectionModel().getSelectedItem();
        if (currentTab == cie21Tab) {
            d2n1Controller.webViewController.popAction();
            return true;

        } else if (currentTab == cie25Tab) {
            d2n5Controller.webViewController.popAction();
            return true;

        } else if (currentTab == cie101Tab) {
            d10n1Controller.webViewController.popAction();
            return true;

        } else if (currentTab == cie105Tab) {
            d10n5Controller.webViewController.popAction();
            return true;

        }
        return super.popAction();
    }

    @FXML
    @Override
    public boolean menuAction() {
        Tab currentTab = displayPane.getSelectionModel().getSelectedItem();
        if (currentTab == cie21Tab) {
            d2n1Controller.webViewController.menuAction();
            return true;

        } else if (currentTab == cie25Tab) {
            d2n5Controller.webViewController.menuAction();
            return true;

        } else if (currentTab == cie101Tab) {
            d10n1Controller.webViewController.menuAction();
            return true;

        } else if (currentTab == cie105Tab) {
            d10n5Controller.webViewController.menuAction();
            return true;

        }
        return super.menuAction();
    }

}
