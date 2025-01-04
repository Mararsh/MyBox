package mara.mybox.controller;

import com.google.zxing.pdf417.encoder.Compaction;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data.ImageItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.image.FxImageTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.BarcodeTools;
import mara.mybox.tools.BarcodeTools.BarcodeType;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.krysalis.barcode4j.ChecksumMode;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.fourstate.RoyalMailCBCBean;
import org.krysalis.barcode4j.impl.fourstate.USPSIntelligentMailBean;
import org.krysalis.barcode4j.impl.int2of5.ITF14Bean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.postnet.POSTNETBean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

/**
 * @Author Mara
 * @CreateDate 2019-9-24
 * @Description
 * @License Apache License Version 2.0
 */
public class BarcodeCreatorController extends BaseImageController {

    protected int fontSize, orientation, qrWidth, qrHeight, qrMargin,
            pdf417ErrorCorrectionLevel, pdf417Width, pdf417Height, pdf417Margin,
            dmWidth, dmHeight;
    protected double narrowWidth, height1, barRatio, quietWidth;
    protected BarcodeType codeType;
    protected String fontName;
    protected HumanReadablePlacement textPostion;
    protected ErrorCorrectionLevel qrErrorCorrectionLevel;
    protected Compaction pdf417Compact;
    protected BarcodeDecoderController decodeController;

    @FXML
    protected VBox optionsBox, d1ParaBox, qrParaBox, pdf417ParaBox, dmParaBox;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected ComboBox<String> sizeSelector, fontSelector,
            orientationSelecor, barRatioSelecor, textPositionSelector, qrErrorCorrectionSelecor,
            pdf417ErrorCorrectionSelecor, pdf417CompactionSelecor;
    @FXML
    protected Label promptLabel, commentsLabel;
    @FXML
    protected TextArea codeInput;
    @FXML
    protected TextField narrowWidthInput, height1Input, quietWidthInput,
            qrHeightInput, qrWidthInput, qrMarginInput,
            pdf417WidthInput, pdf417HeightInput, pdf417MarginInput,
            dmWidthInput, dmHeightInput;
    @FXML
    protected Button validateButton;

    public BarcodeCreatorController() {
        baseTitle = Languages.message("BarcodeCreator");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initCodeBox();
            initD1ParaBox();
            initQRParaBox();
            initPDF417ParaBox();
            initDataMatrixParaBox();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initCodeBox() {
        try {
            codeType = BarcodeType.QR_Code;

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle oldV, Toggle newV) {
                    if (newV == null) {
                        startButton.setDisable(true);
                        return;
                    }
                    startButton.setDisable(false);
                    codeType = BarcodeType.valueOf(((RadioButton) newV).getText());
                    UserConfig.setString("BarcodeType", codeType.name());

                    optionsBox.getChildren().clear();
                    switch (codeType) {
                        case QR_Code:
                            optionsBox.getChildren().addAll(qrParaBox);
                            codeInput.setText("MyBox " + AppValues.AppVersion
                                    + " \n欢迎报告问题和提出需求。");
                            break;
                        case PDF_417:
                            optionsBox.getChildren().addAll(pdf417ParaBox);
                            codeInput.setText("MyBox " + AppValues.AppVersion
                                    + " \n欢迎报告问题和提出需求。");
                            break;
                        case DataMatrix:
                            codeInput.setText("01234567890");
                            optionsBox.getChildren().addAll(dmParaBox);
                            break;
                        case USPS_Intelligent_Mail:
                            codeInput.setText("01234567890123456789");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                            break;
                        case UPCE: //  7
                            codeInput.setText("0123456");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                            break;
                        case UPCA:  // 11
                            codeInput.setText("01234567890");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                            break;
                        case ITF_14: // 13
                            codeInput.setText("0123456789012");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                            break;
                        case EAN13:  // 12
                            codeInput.setText("012345678901");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                            break;
                        case EAN8:  // 7
                            codeInput.setText("0123456");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                            break;
                        case EAN_128:    //
                            codeInput.setText("55012345678");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                            break;
                        default:
                            codeInput.setText("0123456789");
                            optionsBox.getChildren().addAll(d1ParaBox);
                            suggestedSettings();
                    }

                    switch (codeType) {
                        case POSTNET:
                        case Codabar:
                        case Royal_Mail_Customer_Barcode:
                        case USPS_Intelligent_Mail:
                            validateButton.setDisable(true);
                            break;
                        default:
                            validateButton.setDisable(false);
                    }

                }
            });
            NodeTools.setRadioSelected(typeGroup,
                    UserConfig.getString("BarcodeType", BarcodeType.QR_Code.name()));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initD1ParaBox() {
        try {
            orientation = 0;
            orientationSelecor.getItems().addAll(Arrays.asList(
                    "0", "90", "180", "270"
            ));
            orientationSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    orientation = Integer.parseInt(orientationSelecor.getValue());
                    UserConfig.setInt("BarcodeOrientation", orientation);
                }
            });
            orientationSelecor.getSelectionModel().select(UserConfig.getString("BarcodeOrientation", "0"));

            textPostion = HumanReadablePlacement.HRP_BOTTOM;
            textPositionSelector.getItems().addAll(Arrays.asList(Languages.message("Bottom"), Languages.message("Top"), Languages.message("None")
            ));
            textPositionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (newV.equals(Languages.message("Bottom"))) {
                        textPostion = HumanReadablePlacement.HRP_BOTTOM;
                    } else if (newV.equals(Languages.message("Top"))) {
                        textPostion = HumanReadablePlacement.HRP_TOP;
                    } else if (newV.equals(Languages.message("None"))) {
                        textPostion = HumanReadablePlacement.HRP_NONE;
                    }
                    UserConfig.setString("BarcodeTextPosition", newV);
                }
            });
            textPositionSelector.getSelectionModel().select(UserConfig.getString("BarcodeTextPosition", Languages.message("Bottom")));

            fontName = UserConfig.getString("BarcodeFontName", "Arial");
            fontSelector.getItems().addAll(javafx.scene.text.Font.getFamilies());
            fontSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    fontName = newValue;
                    UserConfig.setString("BarcodeFontName", newValue);
                }
            });
            fontSelector.getSelectionModel().select(fontName);

            fontSize = 6;
            List<String> sizes = Arrays.asList(
                    "6", "5", "4", "8", "9", "10", "2", "3", "1", "12", "14", "15", "17");
            sizeSelector.getItems().addAll(sizes);
            sizeSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            fontSize = v;
                            UserConfig.setInt("BarcodeFontSize", v);
                            ValidationTools.setEditorNormal(sizeSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(sizeSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(sizeSelector);
                    }
                }
            });
            sizeSelector.getSelectionModel().select(UserConfig.getInt("BarcodeFontSize", 8) + "");

            narrowWidth = 0.19;
            narrowWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.parseDouble(newValue);
                        if (v > 0) {
                            narrowWidth = v;
                            UserConfig.setString("BarcodeNarrowWdith", newValue);
                            narrowWidthInput.setStyle(null);
                        } else {
                            narrowWidthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        narrowWidthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            narrowWidthInput.setText(UserConfig.getString("BarcodeNarrowWdith", "0.19"));

            barRatio = 2.5;
            barRatioSelecor.getItems().addAll(Arrays.asList(
                    "2.5", "3.0", "2.0"
            ));
            barRatioSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldV, String newV) {
                    try {
                        double v = Double.parseDouble(newV);
                        if (v > 0) {
                            barRatio = v;
                            UserConfig.setString("BarcodeBarRatio", newV);
                            ValidationTools.setEditorNormal(barRatioSelecor);
                        } else {
                            ValidationTools.setEditorBadStyle(barRatioSelecor);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(barRatioSelecor);
                    }
                }
            });
            barRatioSelecor.getSelectionModel().select(UserConfig.getString("BarcodeBarRatio", "2.5"));

            height1 = 15;
            height1Input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            height1 = v;
                            UserConfig.setInt("BarcodeHeight", v);
                            height1Input.setStyle(null);
                        } else {
                            height1Input.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        height1Input.setStyle(UserConfig.badStyle());
                    }
                }
            });
            height1Input.setText(UserConfig.getInt("BarcodeHeight", 15) + "");

            quietWidth = 0.25;
            quietWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.parseDouble(newValue);
                        if (v >= 0) {
                            quietWidth = v;
                            UserConfig.setString("BarcodeQuietWdith", newValue);
                            quietWidthInput.setStyle(null);
                        } else {
                            quietWidthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        quietWidthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            quietWidthInput.setText(UserConfig.getString("BarcodeQuietWdith", "0.25"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void initQRParaBox() {
        try {

            qrWidth = 200;
            qrWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            qrWidth = v;
                            UserConfig.setInt("BarcodeWdith2", v);
                            qrWidthInput.setStyle(null);
                        } else {
                            qrWidthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        qrWidthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            qrWidthInput.setText(UserConfig.getInt("BarcodeWdith2", 200) + "");

            qrHeight = 200;
            qrHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            qrHeight = v;
                            UserConfig.setInt("BarcodeHeight2", v);
                            qrHeightInput.setStyle(null);
                        } else {
                            qrHeightInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        qrHeightInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            qrHeightInput.setText(UserConfig.getInt("BarcodeHeight2", 200) + "");

            qrMargin = 2;
            qrMarginInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            qrMargin = v;
                            UserConfig.setInt("BarcodeMargin", v);
                            qrMarginInput.setStyle(null);
                        } else {
                            qrMarginInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        qrMarginInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            qrMarginInput.setText(UserConfig.getInt("BarcodeMargin", 2) + "");

            qrErrorCorrectionLevel = ErrorCorrectionLevel.H;
            qrErrorCorrectionSelecor.getItems().addAll(Arrays.asList(Languages.message("ErrorCorrectionLevelL"), Languages.message("ErrorCorrectionLevelM"),
                    Languages.message("ErrorCorrectionLevelQ"), Languages.message("ErrorCorrectionLevelH")
            ));
            qrErrorCorrectionSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (Languages.message("ErrorCorrectionLevelL").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.L;
                    } else if (Languages.message("ErrorCorrectionLevelM").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.M;
                    } else if (Languages.message("ErrorCorrectionLevelQ").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.Q;
                    } else if (Languages.message("ErrorCorrectionLevelH").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.H;
                    }
                    UserConfig.setString("QRErrorCorrection", newV);
                }
            });
            qrErrorCorrectionSelecor.getSelectionModel().select(UserConfig.getString("QRErrorCorrection", Languages.message("ErrorCorrectionLevelH")));

            File pic = ImageItem.exampleImageFile();
            if (pic != null) {
                sourceFileInput.setText(pic.getAbsolutePath());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initPDF417ParaBox() {
        try {
            pdf417Width = 300;
            pdf417WidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            pdf417Width = v;
                            UserConfig.setInt("PDF417Width", v);
                            pdf417WidthInput.setStyle(null);
                        } else {
                            pdf417WidthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        pdf417WidthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            pdf417WidthInput.setText(UserConfig.getInt("PDF417Width", 300) + "");

            pdf417Height = 100;
            pdf417HeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            pdf417Height = v;
                            UserConfig.setInt("PDF417Height", v);
                            pdf417HeightInput.setStyle(null);
                        } else {
                            pdf417HeightInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        pdf417HeightInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            pdf417HeightInput.setText(UserConfig.getInt("PDF417Height", 100) + "");

            pdf417Margin = 10;
            pdf417MarginInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            pdf417Margin = v;
                            UserConfig.setInt("PDF417Margin", v);
                            pdf417MarginInput.setStyle(null);
                        } else {
                            pdf417MarginInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        pdf417MarginInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            pdf417MarginInput.setText(UserConfig.getInt("PDF417Margin", 10) + "");

            pdf417Compact = Compaction.AUTO;
            pdf417CompactionSelecor.getItems().addAll(Arrays.asList(Compaction.AUTO.name(), Compaction.TEXT.name(),
                    Compaction.BYTE.name(), Compaction.NUMERIC.name(),
                    Languages.message("None")
            ));
            pdf417CompactionSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (Languages.message("None").equals(newV)) {
                            pdf417Compact = null;
                        } else {
                            pdf417Compact = Compaction.valueOf(newV);
                            if (pdf417Compact == null) {
                                pdf417Compact = Compaction.AUTO;
                            }
                        }
                    } catch (Exception e) {
                        pdf417Compact = Compaction.AUTO;
                    }
                    UserConfig.setString("PDF417Compaction", newV);
                }
            });
            pdf417CompactionSelecor.getSelectionModel().select(UserConfig.getString("PDF417Compaction", Compaction.AUTO.name()));

            pdf417ErrorCorrectionLevel = 3;
            pdf417ErrorCorrectionSelecor.getItems().addAll(Arrays.asList(Languages.message("PDF417ErrorCorrection0"), Languages.message("PDF417ErrorCorrection1"),
                    Languages.message("PDF417ErrorCorrection2"), Languages.message("PDF417ErrorCorrection3"),
                    Languages.message("PDF417ErrorCorrection4"), Languages.message("PDF417ErrorCorrection5"),
                    Languages.message("PDF417ErrorCorrection6"), Languages.message("PDF417ErrorCorrection7"),
                    Languages.message("PDF417ErrorCorrection8")
            ));
            pdf417ErrorCorrectionSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    pdf417ErrorCorrectionLevel = Integer.parseInt(newV.substring(0, 1));
                    UserConfig.setString("PDF417ErrorCorrection", newV);
                }
            });
            pdf417ErrorCorrectionSelecor.getSelectionModel().select(UserConfig.getString("PDF417ErrorCorrection", Languages.message("PDF417ErrorCorrection3")));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initDataMatrixParaBox() {
        try {
            dmWidth = 100;
            dmWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            dmWidth = v;
                            UserConfig.setInt("DataMatrixWidth", v);
                            dmWidthInput.setStyle(null);
                        } else {
                            dmWidthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        dmWidthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            dmWidthInput.setText(UserConfig.getInt("DataMatrixWidth", 100) + "");

            dmHeight = 100;
            dmHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            dmHeight = v;
                            UserConfig.setInt("DataMatrixHeight", v);
                            dmHeightInput.setStyle(null);
                        } else {
                            dmHeightInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        dmHeightInput.setStyle(UserConfig.badStyle());
                    }
                }
            });
            dmHeightInput.setText(UserConfig.getInt("DataMatrixHeight", 100) + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(narrowWidthInput, Languages.message("Millimeters"));
            NodeStyleTools.setTooltip(height1Input, Languages.message("Millimeters"));
            NodeStyleTools.setTooltip(quietWidthInput, Languages.message("Millimeters"));
            NodeStyleTools.setTooltip(qrMarginInput, Languages.message("BarcodeMarginTips"));
            NodeStyleTools.setTooltip(qrHeightInput, Languages.message("Pixels"));
            NodeStyleTools.setTooltip(qrWidthInput, Languages.message("Pixels"));
            NodeStyleTools.setTooltip(pdf417HeightInput, Languages.message("Pixels"));
            NodeStyleTools.setTooltip(pdf417WidthInput, Languages.message("Pixels"));
            NodeStyleTools.setTooltip(pdf417MarginInput, Languages.message("Pixels"));
            NodeStyleTools.setTooltip(dmHeightInput, Languages.message("Pixels"));
            NodeStyleTools.setTooltip(dmWidthInput, Languages.message("Pixels"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void suggestedSettings() {
        narrowWidthInput.setText(DoubleTools.scale(BarcodeTools.defaultModuleWidth(codeType), 2) + "");
        barRatioSelecor.getSelectionModel().select(DoubleTools.scale(BarcodeTools.defaultBarRatio(codeType), 2) + "");
    }

    @Override
    public void checkSourceFileInput() {
        String v = sourceFileInput.getText();
        if (v == null || v.isEmpty()) {
            sourceFile = null;
            return;
        }
        final File file = new File(v);
        if (!file.exists()) {
            sourceFile = null;
            return;
        }
        sourceFile = file;
    }

    @FXML
    @Override
    public void clearAction() {
        sourceFileInput.setText("");
    }

    @FXML
    @Override
    public void startAction() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private BufferedImage bufferedImage;

            @Override
            protected boolean handle() {
                try {
                    AbstractBarcodeBean bean = null;
                    switch (codeType) {
                        case QR_Code:
                            bufferedImage = BarcodeTools.QR(this, codeInput.getText(),
                                    qrErrorCorrectionLevel, qrWidth, qrHeight, qrMargin,
                                    sourceFile);
                            return bufferedImage != null;
                        case PDF_417:
                            bufferedImage = BarcodeTools.PDF417(codeInput.getText(),
                                    pdf417ErrorCorrectionLevel, pdf417Compact,
                                    pdf417Width, pdf417Height, pdf417Margin);
                            return bufferedImage != null;
                        case DataMatrix:
                            bufferedImage = BarcodeTools.DataMatrix(codeInput.getText(),
                                    dmWidth, dmHeight);
                            return bufferedImage != null;
//                                    DataMatrixBean dm = new DataMatrixBean();
//                                    bean = dm;
//                                    break;
                        case Code39:
                            Code39Bean code39 = new Code39Bean();
                            code39.setWideFactor(barRatio);
                            code39.setExtendedCharSetEnabled(true);
                            code39.setChecksumMode(ChecksumMode.CP_AUTO);
                            bean = code39;
                            break;
                        case Code128:
                            Code128Bean code128 = new Code128Bean();
                            bean = code128;
                            break;
                        case Codabar:
                            CodabarBean codabar = new CodabarBean();
                            codabar.setWideFactor(barRatio);
                            bean = codabar;
                            break;
                        case Interleaved2Of5:
                            Interleaved2Of5Bean interleaved2Of5 = new Interleaved2Of5Bean();
                            interleaved2Of5.setWideFactor(barRatio);
                            bean = interleaved2Of5;
                            break;
                        case ITF_14:
                            ITF14Bean itf14 = new ITF14Bean();
                            itf14.setWideFactor(barRatio);
                            bean = itf14;
                            break;
                        case POSTNET:
                            POSTNETBean postnet = new POSTNETBean();
                            bean = postnet;
                            break;
                        case EAN13:
                            EAN13Bean ean13 = new EAN13Bean();
                            bean = ean13;
                            break;
                        case EAN8:
                            EAN8Bean ean8 = new EAN8Bean();
                            bean = ean8;
                            break;
                        case EAN_128:
                            EAN128Bean ean128 = new EAN128Bean();
                            bean = ean128;
                            break;
                        case UPCA:
                            UPCABean upca = new UPCABean();
                            bean = upca;
                            break;
                        case UPCE:
                            UPCEBean upce = new UPCEBean();
                            bean = upce;
                            break;
                        case Royal_Mail_Customer_Barcode:
                            RoyalMailCBCBean rmail = new RoyalMailCBCBean();
                            bean = rmail;
                            break;
                        case USPS_Intelligent_Mail:
                            USPSIntelligentMailBean usps = new USPSIntelligentMailBean();
                            bean = usps;
                            break;
                    }
                    if (bean == null) {
                        return false;
                    }
                    bean.setFontName(fontName);
                    bean.setFontSize(fontSize);
                    bean.setModuleWidth(narrowWidth);
                    bean.setHeight(height1);
                    bean.setQuietZone(quietWidth);
                    bean.setMsgPosition(textPostion);

                    BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                            dpi, BufferedImage.TYPE_BYTE_BINARY, false, orientation);
                    bean.generateBarcode(canvas, codeInput.getText());
                    canvas.finish();

                    bufferedImage = canvas.getBufferedImage();
                    return bufferedImage != null;

                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                bottomLabel.setText(Languages.message("Pixels") + ":"
                        + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight()
                );
                loadedSize();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAsAction() {
        File file = saveAsFile();
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                String format = FileNameTools.ext(file.getName());
                BufferedImage bufferedImage
                        = FxImageTools.toBufferedImage(imageView.getImage());
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                if (!ImageFileWriters.writeImageFile(this,
                        bufferedImage, format, file.getAbsolutePath())) {
                    return false;
                }
                recordFileWritten(file);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                popInformation(Languages.message("Saved"));
            }

        };
        start(task);
    }

    @FXML
    public void validateAction() {
        if (imageView.getImage() == null) {
            return;
        }
        if (decodeController == null
                || decodeController.getMyStage() == null
                || !decodeController.getMyStage().isShowing()) {
            decodeController
                    = (BarcodeDecoderController) openStage(Fxmls.BarcodeDecoderFxml);
        }
        decodeController.loadImage(imageView.getImage());
        decodeController.startAction();

    }

}
