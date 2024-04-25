package mara.mybox.controller;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.PdfTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;

/**
 * @Author Mara
 * @CreateDate 2024-4-24
 * @License Apache License Version 2.0
 */
public class ControlPdfPageAttributes extends BaseController {

    protected String waterText, waterImageFile, header, footer,
            waterTextFontFile, headerFontFile, footerFontFile, numberFontFile;
    protected boolean setWaterText, setWaterImage, setHeader, setFooter, setNumber;
    protected int waterTextSize, headerSize, footerSize, numberSize,
            waterTextMargin, waterTextRotate, waterTextOpacity, waterTextRows, waterTextColumns,
            waterImageWidth, waterImageHeight, waterImageRotate, waterImageOpacity,
            waterImageMargin, waterImageRows, waterImageColumns;
    protected BlendMode waterTextBlend, waterImageBlend;
    protected Color waterTextColor, headerColor, footerColor, numberColor;

    @FXML
    protected CheckBox waterTextCheck, waterImageCheck, headerCheck, footerCheck, numberCheck;
    @FXML
    protected ControlTTFSelector waterTextFontController, headerFontController,
            footerFontController, numberFontController;
    @FXML
    protected TextField waterTextInput, waterTextRotateInput, waterTextOpacityInput,
            waterTextMarginInput, waterTextRowsInput, waterTextColumnsInput,
            waterImageWidthInput, waterImageHeightInput, waterImageRotateInput, waterImageOpacityInput,
            waterImageMarginInput, waterImageRowsInput, waterImageColumnsInput,
            headerInput, footerInput;
    @FXML
    protected ComboBox<String> waterTextSizeSelector, headerSizeSelector,
            footerSizeSelector, numberSizeSelector;
    @FXML
    protected ComboBox<BlendMode> waterTextBlendSelector, waterImageBlendSelector;
    @FXML
    protected ControlColorSet waterTextColorController, headerColorController,
            footerColorController, numberColorController;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> fsize = Arrays.asList(
                    "20", "14", "18", "15", "9", "10", "12", "17",
                    "22", "24", "36", "48", "64", "72", "96");
            List<BlendMode> modes = PdfTools.pdfBlendModes();

            initWaterText(fsize, modes);
            initWaterImage(modes);
            initHeader(fsize);
            initFooter(fsize);
            initNumber(fsize);

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    protected void initWaterText(List<String> fsize, List<BlendMode> modes) {
        try {
            setWaterText = UserConfig.getBoolean(baseName + "SetWaterText", true);
            waterTextCheck.setSelected(setWaterText);

            waterTextFontController.name(baseName);

            waterTextSizeSelector.getItems().addAll(fsize);
            waterTextSize = UserConfig.getInt(baseName + "WaterTextSize", 20);
            waterTextSizeSelector.setValue(waterTextSize + "");

            waterText = UserConfig.getString(baseName + "WaterText", "");
            waterTextInput.setText(waterText);

            waterTextColorController.init(this, baseName + "WaterTextColor", javafx.scene.paint.Color.BLACK);

            waterTextMargin = UserConfig.getInt(baseName + "WaterTextMargin", 20);
            waterTextMarginInput.setText(waterTextMargin + "");

            waterTextRotate = UserConfig.getInt(baseName + "WaterTextRotate", 45);
            waterTextRotateInput.setText(waterTextRotate + "");

            waterTextOpacity = UserConfig.getInt(baseName + "WaterTextOpacity", 100);
            waterTextOpacityInput.setText(waterTextOpacity + "");

            waterTextBlend = BlendMode.NORMAL;
            waterTextBlendSelector.getItems().addAll(modes);
            waterTextBlendSelector.setConverter(new StringConverter<BlendMode>() {

                @Override
                public String toString(BlendMode object) {
                    return BlendMode.getCOSName(object).getName();
                }

                @Override
                public BlendMode fromString(String string) {
                    return BlendMode.getInstance(COSName.getPDFName(string));
                }
            });
            waterTextBlendSelector.setValue(waterTextBlend);

            waterTextRows = UserConfig.getInt(baseName + "WaterTextRows", 3);
            waterTextRowsInput.setText(waterTextRows + "");

            waterTextColumns = UserConfig.getInt(baseName + "WaterTextColumns", 3);
            waterTextColumnsInput.setText(waterTextColumns + "");

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    protected void initWaterImage(List<BlendMode> modes) {
        try {
            setWaterImage = UserConfig.getBoolean(baseName + "SetWaterImage", true);
            waterImageCheck.setSelected(setWaterImage);

            waterImageFile = UserConfig.getString(baseName + "WaterImageFile", null);
            sourceFileInput.setText(waterImageFile);
            sourceFileInput.setStyle(null);

            waterImageWidth = UserConfig.getInt(baseName + "WaterImageWidth", 100);
            waterImageWidthInput.setText(waterImageWidth + "");

            waterImageHeight = UserConfig.getInt(baseName + "WaterImageHeight", 100);
            waterImageHeightInput.setText(waterImageHeight + "");

            waterImageMargin = UserConfig.getInt(baseName + "WaterImageMargin", 20);
            waterImageMarginInput.setText(waterImageMargin + "");

            waterImageRotate = UserConfig.getInt(baseName + "WaterImageRotate", 45);
            waterImageRotateInput.setText(waterImageRotate + "");

            waterImageOpacity = UserConfig.getInt(baseName + "WaterImageOpacity", 100);
            waterImageOpacityInput.setText(waterImageOpacity + "");

            waterImageBlend = BlendMode.NORMAL;
            waterImageBlendSelector.getItems().addAll(modes);
            waterImageBlendSelector.setConverter(new StringConverter<BlendMode>() {

                @Override
                public String toString(BlendMode object) {
                    return BlendMode.getCOSName(object).getName();
                }

                @Override
                public BlendMode fromString(String string) {
                    return BlendMode.getInstance(COSName.getPDFName(string));
                }
            });

            waterImageBlendSelector.setValue(waterImageBlend);

            waterImageRows = UserConfig.getInt(baseName + "WaterImageRows", 3);
            waterImageRowsInput.setText(waterImageRows + "");

            waterImageColumns = UserConfig.getInt(baseName + "WaterImageColumns", 3);
            waterImageColumnsInput.setText(waterImageColumns + "");

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    protected void initHeader(List<String> fsize) {
        try {
            setHeader = UserConfig.getBoolean(baseName + "SetHeader", true);
            headerCheck.setSelected(setHeader);

            headerFontController.name(baseName);

            headerSizeSelector.getItems().addAll(fsize);
            headerSize = UserConfig.getInt(baseName + "HeaderSize", 20);
            headerSizeSelector.setValue(headerSize + "");

            header = UserConfig.getString(baseName + "Header", "");
            headerInput.setText(header);

            headerColorController.init(this, baseName + "HeaderColor", javafx.scene.paint.Color.BLACK);

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    protected void initFooter(List<String> fsize) {
        try {
            setFooter = UserConfig.getBoolean(baseName + "SetFooter", true);
            footerCheck.setSelected(setFooter);

            footerFontController.name(baseName);

            footerSizeSelector.getItems().addAll(fsize);
            footerSize = UserConfig.getInt(baseName + "FooterSize", 20);
            footerSizeSelector.setValue(footerSize + "");

            footer = UserConfig.getString(baseName + "Footer", "");
            footerInput.setText(footer);

            footerColorController.init(this, baseName + "FooterColor", javafx.scene.paint.Color.BLACK);

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    protected void initNumber(List<String> fsize) {
        try {
            setNumber = UserConfig.getBoolean(baseName + "SetNumber", true);
            numberCheck.setSelected(setNumber);

            numberFontController.name(baseName);

            numberSizeSelector.getItems().addAll(fsize);
            numberSize = UserConfig.getInt(baseName + "NumberSize", 20);
            numberSizeSelector.setValue(numberSize + "");

            numberColorController.init(this, baseName + "NumberColor", javafx.scene.paint.Color.BLACK);

        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
        }
    }

    public boolean pickWaterText() {
        try {
            setWaterText = waterTextCheck.isSelected();
            UserConfig.setBoolean(baseName + "SetWaterText", setWaterText);
            if (!setWaterText) {
                return true;
            }

            String sv = waterTextInput.getText();
            if (sv != null && !sv.isBlank()) {
                waterText = sv.trim();
                UserConfig.setString(baseName + "WaterText", waterText);
            } else {
                popError(message("InvalidParameter") + ": " + message("WatermarkText"));
                return false;
            }

            int iv;
            waterTextFontFile = waterTextFontController.ttfFile;
            if (waterTextFontFile != null && !waterTextFontFile.isBlank()) {
                try {
                    iv = Integer.parseInt(waterTextSizeSelector.getValue());
                } catch (Exception e) {
                    iv = -1;
                }
                if (iv > 0) {
                    waterTextSize = iv;
                    UserConfig.setInt(baseName + "WaterTextSize", iv);
                } else {
                    popError(message("InvalidParameter") + ": "
                            + message("WatermarkText") + "-" + message("FontSize"));
                    return false;
                }
            }

            try {
                iv = Integer.parseInt(waterTextMarginInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkText") + "-" + message("Margin"));
                return false;
            }
            waterTextMargin = iv;
            UserConfig.setInt(baseName + "WaterTextMargin", iv);

            try {
                iv = Integer.parseInt(waterTextRotateInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkText") + "-" + message("Rotate"));
                return false;
            }
            waterTextRotate = iv;
            UserConfig.setInt(baseName + "WaterTextRotate", iv);

            try {
                iv = Integer.parseInt(waterTextOpacityInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv >= 0) {
                waterTextOpacity = iv;
                UserConfig.setInt(baseName + "WaterTextOpacity", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkText") + "-" + message("Opacity"));
                return false;
            }

            try {
                iv = Integer.parseInt(waterTextRowsInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv > 0) {
                waterTextRows = iv;
                UserConfig.setInt(baseName + "WaterTextRows", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkText") + "-" + message("RowsNumber"));
                return false;
            }

            try {
                iv = Integer.parseInt(waterTextColumnsInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv > 0) {
                waterTextColumns = iv;
                UserConfig.setInt(baseName + "WaterTextColumns", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkText") + "-" + message("ColumnsNumber"));
                return false;
            }

            waterTextBlend = waterTextBlendSelector.getValue();
            waterTextColor = waterTextColorController.awtColor();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
            return false;
        }
    }

    public boolean pickWaterImage() {
        try {
            setWaterImage = waterImageCheck.isSelected();
            UserConfig.setBoolean(baseName + "SetWaterImage", setWaterImage);
            sourceFileInput.setStyle(null);

            if (!setWaterImage) {
                return true;
            }

            String sv = sourceFileInput.getText();
            if (sv != null && !sv.isBlank()) {
                File file = new File(sv);
                if (!file.exists() || !file.isFile()) {
                    popError(message("InvalidParameter") + ": " + message("WatermarkImage"));
                    return false;
                }
                waterImageFile = file.getAbsolutePath();
                UserConfig.setString(baseName + "WaterImageFile", waterImageFile);
            } else {
                popError(message("InvalidParameter") + ": " + message("WatermarkImage"));
                return false;
            }

            int iv;
            try {
                iv = Integer.parseInt(waterImageWidthInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv > 0) {
                waterImageWidth = iv;
                UserConfig.setInt(baseName + "WaterImageWidth", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkImage") + "-" + message("Width"));
                return false;
            }

            try {
                iv = Integer.parseInt(waterImageHeightInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv > 0) {
                waterImageHeight = iv;
                UserConfig.setInt(baseName + "WaterImageHeight", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkImage") + "-" + message("Height"));
                return false;
            }

            try {
                iv = Integer.parseInt(waterImageMarginInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkImage") + "-" + message("Margin"));
                return false;
            }
            waterImageMargin = iv;
            UserConfig.setInt(baseName + "WaterImageMargin", iv);

            try {
                iv = Integer.parseInt(waterImageRotateInput.getText());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkImage") + "-" + message("Rotate"));
                return false;
            }
            waterImageRotate = iv;
            UserConfig.setInt(baseName + "WaterImageRotate", iv);

            try {
                iv = Integer.parseInt(waterImageOpacityInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv >= 0) {
                waterImageOpacity = iv;
                UserConfig.setInt(baseName + "WaterImageOpacity", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkImage") + "-" + message("Opacity"));
                return false;
            }

            try {
                iv = Integer.parseInt(waterImageRowsInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv > 0) {
                waterImageRows = iv;
                UserConfig.setInt(baseName + "WaterImageRows", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkImage") + "-" + message("RowsNumber"));
                return false;
            }

            try {
                iv = Integer.parseInt(waterImageColumnsInput.getText());
            } catch (Exception e) {
                iv = -1;
            }
            if (iv > 0) {
                waterImageColumns = iv;
                UserConfig.setInt(baseName + "WaterImageColumns", iv);
            } else {
                popError(message("InvalidParameter") + ": "
                        + message("WatermarkImage") + "-" + message("ColumnsNumber"));
                return false;
            }

            waterImageBlend = waterImageBlendSelector.getValue();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
            return false;
        }
    }

    public boolean pickHeader() {
        try {
            setHeader = headerCheck.isSelected();
            UserConfig.setBoolean(baseName + "SetHeader", setHeader);
            if (!setHeader) {
                return true;
            }

            String sv = headerInput.getText();
            if (sv != null && !sv.isBlank()) {
                header = sv.trim();
                UserConfig.setString(baseName + "Header", header);
            } else {
                popError(message("InvalidParameter") + ": " + message("Header"));
                return false;
            }

            headerFontFile = headerFontController.ttfFile;
            int iv;
            if (headerFontFile != null && !headerFontFile.isBlank()) {
                try {
                    iv = Integer.parseInt(headerSizeSelector.getValue());
                } catch (Exception e) {
                    iv = -1;
                }
                if (iv > 0) {
                    headerSize = iv;
                    UserConfig.setInt(baseName + "HeaderSize", iv);
                } else {
                    popError(message("InvalidParameter") + ": "
                            + message("WatermarkText") + "-" + message("FontSize"));
                    return false;
                }
            }

            headerColor = headerColorController.awtColor();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
            return false;
        }
    }

    public boolean pickFooter() {
        try {
            setFooter = footerCheck.isSelected();
            UserConfig.setBoolean(baseName + "SetFooter", setFooter);
            if (!setFooter) {
                return true;
            }

            String sv = footerInput.getText();
            if (sv != null && !sv.isBlank()) {
                footer = sv.trim();
                UserConfig.setString(baseName + "Footer", footer);
            } else {
                popError(message("InvalidParameter") + ": " + message("Footer"));
                return false;
            }

            footerFontFile = footerFontController.ttfFile;
            int iv;
            if (footerFontFile != null && !footerFontFile.isBlank()) {
                try {
                    iv = Integer.parseInt(footerSizeSelector.getValue());
                } catch (Exception e) {
                    iv = -1;
                }
                if (iv > 0) {
                    footerSize = iv;
                    UserConfig.setInt(baseName + "FooterSize", iv);
                } else {
                    popError(message("InvalidParameter") + ": "
                            + message("WatermarkText") + "-" + message("FontSize"));
                    return false;
                }
            }

            footerColor = footerColorController.awtColor();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
            return false;
        }
    }

    public boolean pickNumber() {
        try {
            setNumber = numberCheck.isSelected();
            UserConfig.setBoolean(baseName + "SetNumber", setNumber);
            if (!setNumber) {
                return true;
            }

            numberFontFile = numberFontController.ttfFile;
            int iv;
            if (numberFontFile != null && !numberFontFile.isBlank()) {
                try {
                    iv = Integer.parseInt(numberSizeSelector.getValue());
                } catch (Exception e) {
                    iv = -1;
                }
                if (iv > 0) {
                    numberSize = iv;
                    UserConfig.setInt(baseName + "NumberSize", iv);
                } else {
                    popError(message("InvalidParameter") + ": "
                            + message("WatermarkText") + "-" + message("FontSize"));
                    return false;
                }
            }

            numberColor = numberColorController.awtColor();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
            return false;
        }
    }

    public boolean pickValues() {
        try {
            if (!pickWaterText() || !pickWaterImage()
                    || !pickHeader() || !pickFooter() || !pickNumber()) {
                return false;
            }
            if (!setWaterText && !setWaterImage
                    && !setHeader && !setFooter && !setNumber) {
                popError(message("NothingHandled"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e, baseName);
            return false;
        }
    }

    public float waterTextWidth(PDFont font) {
        return PdfTools.fontWidth(font, waterText, waterTextSize);
    }

    public float waterTextHeight(PDFont font) {
        return PdfTools.fontHeight(font, waterTextSize);
    }

}
