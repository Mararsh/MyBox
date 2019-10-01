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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.BarcodeTools;
import mara.mybox.tools.BarcodeTools.BarcodeType;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;
import mara.mybox.value.CommonValues;
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
public class BarcodeCreatorController extends ImageViewerController {

    protected int dpi, fontSize, orientation, qrWidth, qrHeight, qrMargin,
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
    protected HBox codeBox, imageParaBox, actionBox;
    @FXML
    protected VBox d1ParaBox, qrParaBox, pdf417ParaBox, dmParaBox;
    @FXML
    protected ComboBox<String> dpiSelector, typeSelecor, sizeSelector, fontSelector,
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
        baseTitle = AppVariables.message("BarcodeCreator");

    }

    @Override
    public void initializeNext2() {
        try {

            initCodeBox();
            initD1ParaBox();
            initQRParaBox();
            initPDF417ParaBox();
            initDataMatrixParaBox();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initCodeBox() {
        try {
            codeType = BarcodeType.Code39;

            for (BarcodeType type : BarcodeType.values()) {
                typeSelecor.getItems().add(type.name());
            }
            typeSelecor.getItems().add(0, "-------" + message("2DimensionalBarcode") + "-------");
            typeSelecor.getItems().add(4, "-------" + message("1DimensionalBarcode") + "-------");
            typeSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (newV.startsWith("-----")) {
                        startButton.setDisable(true);
                        return;
                    }
                    startButton.setDisable(false);
                    codeType = BarcodeType.valueOf(newV);
                    AppVariables.setUserConfigValue("BarcodeType", newV);

                    contentBox.getChildren().clear();
                    switch (codeType) {
                        case QR_Code:
                            contentBox.getChildren().addAll(codeBox, qrParaBox, actionBox, scrollPane);
                            codeInput.setText("MyBox 5.6 \n欢迎报告问题和提出需求。");
                            break;
                        case PDF_417:
                            contentBox.getChildren().addAll(codeBox, pdf417ParaBox, actionBox, scrollPane);
                            codeInput.setText("MyBox 5.6 \n欢迎报告问题和提出需求。");
                            break;
                        case DataMatrix:
                            codeInput.setText("01234567890");
                            contentBox.getChildren().addAll(codeBox, dmParaBox, actionBox, scrollPane);
                            break;
                        case USPS_Intelligent_Mail:
                            codeInput.setText("01234567890123456789");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
                            suggestedSettings();
                            break;
                        case UPCE: //  7
                            codeInput.setText("0123456");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
                            suggestedSettings();
                            break;
                        case UPCA:  // 11
                            codeInput.setText("01234567890");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
                            suggestedSettings();
                            break;
                        case ITF_14: // 13
                            codeInput.setText("0123456789012");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
                            suggestedSettings();
                            break;
                        case EAN13:  // 12
                            codeInput.setText("012345678901");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
                            suggestedSettings();
                            break;
                        case EAN8:  // 7
                            codeInput.setText("0123456");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
                            suggestedSettings();
                            break;
                        case EAN_128:    //
                            codeInput.setText("55012345678");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
                            suggestedSettings();
                            break;
                        default:
                            codeInput.setText("0123456789");
                            contentBox.getChildren().addAll(codeBox, imageParaBox, d1ParaBox, actionBox, scrollPane);
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
            typeSelecor.getSelectionModel().select(
                    AppVariables.getUserConfigValue("BarcodeType", BarcodeType.QR_Code.name()));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initD1ParaBox() {
        try {
            dpi = 300;
            dpiSelector.getItems().addAll(Arrays.asList(
                    "300", "160", "96", "72", "240", "120", "600", "400"
            ));
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    dpi = Integer.valueOf(dpiSelector.getValue());
                    AppVariables.setUserConfigInt("BarcodeDpi", dpi);
                }
            });
            dpiSelector.getSelectionModel().select(AppVariables.getUserConfigValue("BarcodeDpi", "300"));

            orientation = 0;
            orientationSelecor.getItems().addAll(Arrays.asList(
                    "0", "90", "180", "270"
            ));
            orientationSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    orientation = Integer.valueOf(orientationSelecor.getValue());
                    AppVariables.setUserConfigInt("BarcodeOrientation", orientation);
                }
            });
            orientationSelecor.getSelectionModel().select(AppVariables.getUserConfigValue("BarcodeOrientation", "0"));

            textPostion = HumanReadablePlacement.HRP_BOTTOM;
            textPositionSelector.getItems().addAll(Arrays.asList(
                    message("Bottom"), message("Top"), message("None")
            ));
            textPositionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (newV.equals(message("Bottom"))) {
                        textPostion = HumanReadablePlacement.HRP_BOTTOM;
                    } else if (newV.equals(message("Top"))) {
                        textPostion = HumanReadablePlacement.HRP_TOP;
                    } else if (newV.equals(message("None"))) {
                        textPostion = HumanReadablePlacement.HRP_NONE;
                    }
                    AppVariables.setUserConfigValue("BarcodeTextPosition", newV);
                }
            });
            textPositionSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("BarcodeTextPosition", message("Bottom")));

            fontName = AppVariables.getUserConfigValue("BarcodeFontName", "Arial");
            fontSelector.getItems().addAll(javafx.scene.text.Font.getFamilies());
            fontSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    fontName = newValue;
                    AppVariables.setUserConfigValue("BarcodeFontName", newValue);
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
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            fontSize = v;
                            AppVariables.setUserConfigInt("BarcodeFontSize", v);
                            FxmlControl.setEditorNormal(sizeSelector);
                        } else {
                            FxmlControl.setEditorBadStyle(sizeSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(sizeSelector);
                    }
                }
            });
            sizeSelector.getSelectionModel().select(AppVariables.getUserConfigInt("BarcodeFontSize", 8) + "");

            narrowWidth = 0.19;
            narrowWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.valueOf(newValue);
                        if (v > 0) {
                            narrowWidth = v;
                            AppVariables.setUserConfigValue("BarcodeNarrowWdith", newValue);
                            narrowWidthInput.setStyle(null);
                        } else {
                            narrowWidthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        narrowWidthInput.setStyle(badStyle);
                    }
                }
            });
            narrowWidthInput.setText(AppVariables.getUserConfigValue("BarcodeNarrowWdith", "0.19"));
            FxmlControl.setTooltip(narrowWidthInput, message("Millimeters"));

            barRatio = 2.5;
            barRatioSelecor.getItems().addAll(Arrays.asList(
                    "2.5", "3.0", "2.0"
            ));
            barRatioSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldV, String newV) {
                    try {
                        double v = Double.valueOf(newV);
                        if (v > 0) {
                            barRatio = v;
                            AppVariables.setUserConfigValue("BarcodeBarRatio", newV);
                            FxmlControl.setEditorNormal(barRatioSelecor);
                        } else {
                            FxmlControl.setEditorBadStyle(barRatioSelecor);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(barRatioSelecor);
                    }
                }
            });
            barRatioSelecor.getSelectionModel().select(AppVariables.getUserConfigValue("BarcodeBarRatio", "2.5"));

            height1 = 15;
            height1Input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            height1 = v;
                            AppVariables.setUserConfigInt("BarcodeHeight", v);
                            height1Input.setStyle(null);
                        } else {
                            height1Input.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        height1Input.setStyle(badStyle);
                    }
                }
            });
            height1Input.setText(AppVariables.getUserConfigInt("BarcodeHeight", 15) + "");
            FxmlControl.setTooltip(height1Input, message("Millimeters"));

            quietWidth = 0.25;
            quietWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.valueOf(newValue);
                        if (v >= 0) {
                            quietWidth = v;
                            AppVariables.setUserConfigValue("BarcodeQuietWdith", newValue);
                            quietWidthInput.setStyle(null);
                        } else {
                            quietWidthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        quietWidthInput.setStyle(badStyle);
                    }
                }
            });
            quietWidthInput.setText(AppVariables.getUserConfigValue("BarcodeQuietWdith", "0.25"));
            FxmlControl.setTooltip(quietWidthInput, message("Millimeters"));

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initQRParaBox() {
        try {

            qrWidth = 200;
            qrWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            qrWidth = v;
                            AppVariables.setUserConfigInt("BarcodeWdith2", v);
                            qrWidthInput.setStyle(null);
                        } else {
                            qrWidthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        qrWidthInput.setStyle(badStyle);
                    }
                }
            });
            qrWidthInput.setText(AppVariables.getUserConfigInt("BarcodeWdith2", 200) + "");
            FxmlControl.setTooltip(qrWidthInput, message("Pixels"));

            qrHeight = 200;
            qrHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            qrHeight = v;
                            AppVariables.setUserConfigInt("BarcodeHeight2", v);
                            qrHeightInput.setStyle(null);
                        } else {
                            qrHeightInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        qrHeightInput.setStyle(badStyle);
                    }
                }
            });
            qrHeightInput.setText(AppVariables.getUserConfigInt("BarcodeHeight2", 200) + "");
            FxmlControl.setTooltip(qrHeightInput, message("Pixels"));

            qrMargin = 2;
            qrMarginInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            qrMargin = v;
                            AppVariables.setUserConfigInt("BarcodeMargin", v);
                            qrMarginInput.setStyle(null);
                        } else {
                            qrMarginInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        qrMarginInput.setStyle(badStyle);
                    }
                }
            });
            qrMarginInput.setText(AppVariables.getUserConfigInt("BarcodeMargin", 2) + "");
            FxmlControl.setTooltip(qrMarginInput, message("BarcodeMarginTips"));

            qrErrorCorrectionLevel = ErrorCorrectionLevel.H;
            qrErrorCorrectionSelecor.getItems().addAll(Arrays.asList(
                    message("ErrorCorrectionLevelL"), message("ErrorCorrectionLevelM"),
                    message("ErrorCorrectionLevelQ"), message("ErrorCorrectionLevelH")
            ));
            qrErrorCorrectionSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    if (message("ErrorCorrectionLevelL").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.L;
                    } else if (message("ErrorCorrectionLevelM").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.M;
                    } else if (message("ErrorCorrectionLevelQ").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.Q;
                    } else if (message("ErrorCorrectionLevelH").equals(newV)) {
                        qrErrorCorrectionLevel = ErrorCorrectionLevel.H;
                    }
                    AppVariables.setUserConfigValue("QRErrorCorrection", newV);
                }
            });
            qrErrorCorrectionSelecor.getSelectionModel().select(
                    AppVariables.getUserConfigValue("QRErrorCorrection", message("ErrorCorrectionLevelH")));

            File pic = FxmlControl.getInternalFile("/img/About.png", "image", "About.png");
            if (pic != null) {
                sourceFileInput.setText(pic.getAbsolutePath());
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initPDF417ParaBox() {
        try {
            pdf417Width = 300;
            pdf417WidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            pdf417Width = v;
                            AppVariables.setUserConfigInt("PDF417Width", v);
                            pdf417WidthInput.setStyle(null);
                        } else {
                            pdf417WidthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        pdf417WidthInput.setStyle(badStyle);
                    }
                }
            });
            pdf417WidthInput.setText(AppVariables.getUserConfigInt("PDF417Width", 300) + "");
            FxmlControl.setTooltip(pdf417WidthInput, message("Pixels"));

            pdf417Height = 100;
            pdf417HeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            pdf417Height = v;
                            AppVariables.setUserConfigInt("PDF417Height", v);
                            pdf417HeightInput.setStyle(null);
                        } else {
                            pdf417HeightInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        pdf417HeightInput.setStyle(badStyle);
                    }
                }
            });
            pdf417HeightInput.setText(AppVariables.getUserConfigInt("PDF417Height", 100) + "");
            FxmlControl.setTooltip(pdf417HeightInput, message("Pixels"));

            pdf417Margin = 10;
            pdf417MarginInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            pdf417Margin = v;
                            AppVariables.setUserConfigInt("PDF417Margin", v);
                            pdf417MarginInput.setStyle(null);
                        } else {
                            pdf417MarginInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        pdf417MarginInput.setStyle(badStyle);
                    }
                }
            });
            pdf417MarginInput.setText(AppVariables.getUserConfigInt("PDF417Margin", 10) + "");
            FxmlControl.setTooltip(pdf417MarginInput, message("Pixels"));

            pdf417Compact = Compaction.AUTO;
            pdf417CompactionSelecor.getItems().addAll(Arrays.asList(
                    Compaction.AUTO.name(), Compaction.TEXT.name(),
                    Compaction.BYTE.name(), Compaction.NUMERIC.name(),
                    message("None")
            ));
            pdf417CompactionSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (message("None").equals(newV)) {
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
                    AppVariables.setUserConfigValue("PDF417Compaction", newV);
                }
            });
            pdf417CompactionSelecor.getSelectionModel().select(
                    AppVariables.getUserConfigValue("PDF417Compaction", Compaction.AUTO.name()));

            pdf417ErrorCorrectionLevel = 3;
            pdf417ErrorCorrectionSelecor.getItems().addAll(Arrays.asList(
                    message("PDF417ErrorCorrection0"), message("PDF417ErrorCorrection1"),
                    message("PDF417ErrorCorrection2"), message("PDF417ErrorCorrection3"),
                    message("PDF417ErrorCorrection4"), message("PDF417ErrorCorrection5"),
                    message("PDF417ErrorCorrection6"), message("PDF417ErrorCorrection7"),
                    message("PDF417ErrorCorrection8")
            ));
            pdf417ErrorCorrectionSelecor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    pdf417ErrorCorrectionLevel = Integer.valueOf(newV.substring(0, 1));
                    AppVariables.setUserConfigValue("PDF417ErrorCorrection", newV);
                }
            });
            pdf417ErrorCorrectionSelecor.getSelectionModel().select(
                    AppVariables.getUserConfigValue("PDF417ErrorCorrection", message("PDF417ErrorCorrection3")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initDataMatrixParaBox() {
        try {
            dmWidth = 100;
            dmWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            dmWidth = v;
                            AppVariables.setUserConfigInt("DataMatrixWidth", v);
                            dmWidthInput.setStyle(null);
                        } else {
                            dmWidthInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        dmWidthInput.setStyle(badStyle);
                    }
                }
            });
            dmWidthInput.setText(AppVariables.getUserConfigInt("DataMatrixWidth", 100) + "");
            FxmlControl.setTooltip(dmWidthInput, message("Pixels"));

            dmHeight = 100;
            dmHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            dmHeight = v;
                            AppVariables.setUserConfigInt("DataMatrixHeight", v);
                            dmHeightInput.setStyle(null);
                        } else {
                            dmHeightInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        dmHeightInput.setStyle(badStyle);
                    }
                }
            });
            dmHeightInput.setText(AppVariables.getUserConfigInt("DataMatrixHeight", 100) + "");
            FxmlControl.setTooltip(dmHeightInput, message("Pixels"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void suggestedSettings() {
        narrowWidthInput.setText(DoubleTools.scale(BarcodeTools.defaultModuleWidth(codeType), 2) + "");
        barRatioSelecor.getSelectionModel().select(DoubleTools.scale(BarcodeTools.defaultBarRatio(codeType), 2) + "");
    }

    @Override
    public void sourceFileChanged(final File file) {
        sourceFile = file;
    }

    @FXML
    @Override
    public void startAction() {
        try {
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private BufferedImage bufferedImage;

                    @Override
                    protected boolean handle() {
                        try {

                            AbstractBarcodeBean bean = null;
                            switch (codeType) {
                                case QR_Code:
                                    bufferedImage = BarcodeTools.QR(codeInput.getText(),
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
                        bottomLabel.setText(AppVariables.message("Pixels") + ":"
                                + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight()
                        );
                        loadedSize();
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    null, CommonImageValues.ImageExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        String format = FileTools.getFileSuffix(file.getName());
                        final BufferedImage bufferedImage
                                = FxmlImageManufacture.getBufferedImage(imageView.getImage());
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return bufferedImage != null
                                && ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    }

                    @Override
                    protected void whenSucceeded() {
                        popInformation(AppVariables.message("Saved"));
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

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
                    = (BarcodeDecoderController) openStage(CommonValues.BarcodeDecoderFxml);
        }
        decodeController.loadImage(imageView.getImage());
        decodeController.startAction();

    }

}
