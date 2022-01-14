package mara.mybox.controller;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-10
 * @License Apache License Version 2.0
 */
public class ControlImageText extends BaseController {

    protected ImageView imageView;
    protected int lineHeight, x, y, fontSize, shadow, angle, baseX, baseY, textWidth, textHeight;
    protected int margin;
    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected String fontFamily, fontName;
    protected FontPosture fontPosture;
    protected FontWeight fontWeight;
    protected final SimpleBooleanProperty changeNotify;

    @FXML
    protected TextArea textArea;
    @FXML
    protected TextField xInput, yInput;
    @FXML
    protected ComboBox<String> lineHeightSelector, sizeBox, opacitySelector, blendSelector, styleBox, familyBox, angleBox, shadowBox;
    @FXML
    protected CheckBox outlineCheck, verticalCheck, rightToLeftCheck, blendTopCheck, ignoreTransparentCheck;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected ToggleGroup positionGroup;
    @FXML
    protected RadioButton rightBottomRadio, rightTopRadio, leftBottomRadio, leftTopRadio, centerRadio, customRadio;
    @FXML
    protected TextField marginInput;
    @FXML
    protected VBox baseBox;
    @FXML
    protected Label sizeLabel;
    @FXML
    protected HBox goBox;

    public ControlImageText() {
        changeNotify = new SimpleBooleanProperty(false);
    }

    public void notifyChanged() {
        changeNotify.set(!changeNotify.get());
    }

    public void setParameters(BaseController parent, ImageView imageView) {
        try {
            parentController = parent;
            this.imageView = imageView;
            boolean checkBaseAtOnce = !(parentController instanceof ImageManufactureTextController);
            if (checkBaseAtOnce) {
                baseBox.getChildren().removeAll(sizeLabel, goBox);
            }
            fontFamily = UserConfig.getString(baseName + "TextFontFamily", "Arial");
            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.REGULAR;

            fontSize = 24;
            shadow = 0;
            angle = 0;

            lineHeight = UserConfig.getInt(baseName + "TextLineHeight", -1);
            List<String> heights = Arrays.asList(
                    message("Automatic"), "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            lineHeightSelector.getItems().addAll(heights);
            if (lineHeight <= 0) {
                lineHeightSelector.setValue(message("Automatic"));
            } else {
                lineHeightSelector.setValue(lineHeight + "");
            }
            lineHeightSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            lineHeight = v;
                        } else {
                            lineHeight = -1;
                        }
                    } catch (Exception e) {
                        lineHeight = -1;;
                    }
                    UserConfig.setInt(baseName + "TextLineHeight", lineHeight);
                    notifyChanged();
                }
            });

            colorSetController.init(this, baseName + "TextColor", Color.ORANGE);
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    notifyChanged();
                }
            });

            familyBox.getItems().addAll(javafx.scene.text.Font.getFamilies());
            familyBox.getSelectionModel().select(fontFamily);
            familyBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    fontFamily = newValue;
                    UserConfig.setString(baseName + "TextFontFamily", newValue);
                    notifyChanged();
                }
            });

            List<String> styles = Arrays.asList(message("Regular"), message("Bold"), message("Italic"), message("Bold Italic"));
            styleBox.getItems().addAll(styles);
            styleBox.getSelectionModel().select(0);
            styleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("Bold").equals(newValue)) {
                        fontWeight = FontWeight.BOLD;
                        fontPosture = FontPosture.REGULAR;

                    } else if (message("Italic").equals(newValue)) {
                        fontWeight = FontWeight.NORMAL;
                        fontPosture = FontPosture.ITALIC;

                    } else if (message("Bold Italic").equals(newValue)) {
                        fontWeight = FontWeight.BOLD;
                        fontPosture = FontPosture.ITALIC;

                    } else {
                        fontWeight = FontWeight.NORMAL;
                        fontPosture = FontPosture.REGULAR;

                    }
                    notifyChanged();
                }
            });

            List<String> sizes = Arrays.asList(
                    "72", "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            sizeBox.getItems().addAll(sizes);
            sizeBox.getSelectionModel().select(UserConfig.getInt(baseName + "TextFontSize", 72) + "");
            sizeBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            fontSize = v;
                            UserConfig.setInt(baseName + "TextFontSize", v);
                            notifyChanged();
                            ValidationTools.setEditorNormal(sizeBox);
                        } else {
                            ValidationTools.setEditorBadStyle(sizeBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(sizeBox);
                    }
                }
            });

            String mode = UserConfig.getString(baseName + "TextBlendMode", message("NormalMode"));
            blendMode = PixelsBlendFactory.blendMode(mode);
            blendSelector.getItems().addAll(PixelsBlendFactory.blendModes());
            blendSelector.setValue(mode);
            blendSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String mode = blendSelector.getSelectionModel().getSelectedItem();
                    blendMode = PixelsBlendFactory.blendMode(mode);
                    UserConfig.setString(baseName + "TextBlendMode", mode);
                    notifyChanged();
                }
            });

            opacity = UserConfig.getInt(baseName + "TextOpacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacitySelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacitySelector.setValue(opacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            UserConfig.setInt(baseName + "TextOpacity", (int) (f * 100));
                            ValidationTools.setEditorNormal(opacitySelector);
                            notifyChanged();
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            blendTopCheck.setSelected(UserConfig.getBoolean(baseName + "TextBlendTop", true));
            blendTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "TextBlendTop", blendTopCheck.isSelected());
                    notifyChanged();
                }
            });

            ignoreTransparentCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreTransparent", true));
            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "IgnoreTransparent", ignoreTransparentCheck.isSelected());
                    notifyChanged();
                }
            });

            shadowBox.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            shadowBox.getSelectionModel().select(UserConfig.getInt(baseName + "TextShadow", 0) + "");
            shadowBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            shadow = v;
                            UserConfig.setInt(baseName + "TextShadow", v);
                            ValidationTools.setEditorNormal(shadowBox);
                            notifyChanged();
                        } else {
                            ValidationTools.setEditorBadStyle(shadowBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(shadowBox);
                    }
                }
            });

            angleBox.getItems().addAll(Arrays.asList("0", "90", "180", "270", "45", "135", "225", "315",
                    "60", "150", "240", "330", "15", "105", "195", "285", "30", "120", "210", "300"));
            angleBox.getSelectionModel().select(UserConfig.getInt(baseName + "TextAngle", 0) + "");
            angleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            angle = v;
                            UserConfig.setInt(baseName + "TextAngle", v);
                            ValidationTools.setEditorNormal(angleBox);
                            notifyChanged();
                        } else {
                            ValidationTools.setEditorBadStyle(angleBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(angleBox);
                    }
                }
            });

            outlineCheck.setSelected(UserConfig.getBoolean(baseName + "TextOutline", false));
            outlineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TextOutline", outlineCheck.isSelected());
                    notifyChanged();
                }
            });

            verticalCheck.setSelected(UserConfig.getBoolean(baseName + "TextVertical", false));
            verticalCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TextVertical", verticalCheck.isSelected());
                    notifyChanged();
                }
            });

            rightToLeftCheck.setSelected(UserConfig.getBoolean(baseName + "TextRightToLeft", false));
            rightToLeftCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TextRightToLeft", rightToLeftCheck.isSelected());
                    notifyChanged();
                }
            });
            rightToLeftCheck.visibleProperty().bind(verticalCheck.selectedProperty());

            textArea.setText(UserConfig.getString(baseName + "TextValue", "MyBox"));

            margin = UserConfig.getInt(baseName + "Margin", 20);
            marginInput.setText(margin + "");

            positionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkPositionType();
                    if (checkBaseAtOnce) {
                        notifyChanged();
                    }
                }
            });

            if (checkBaseAtOnce) {
                textArea.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkText();
                        notifyChanged();
                    }
                });

                xInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkXY();
                        notifyChanged();
                    }
                });

                yInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkXY();
                        notifyChanged();
                    }
                });

                marginInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkMargin();
                        notifyChanged();
                    }
                });

            }

            checkPositionType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public boolean checkText() {
        if (textArea.getText().isEmpty()) {
            textArea.setStyle(UserConfig.badStyle());
            return false;
        } else {
            textArea.setStyle(null);
            UserConfig.setString(baseName + "TextValue", textArea.getText());
        }
        return true;
    }

    public boolean checkXY() {
        boolean checkImage = imageView != null && imageView.getImage() != null;
        try {
            int v = Integer.parseInt(xInput.getText().trim());
            if (v < 0 || (checkImage && v >= imageView.getImage().getWidth())) {
                xInput.setStyle(UserConfig.badStyle());
                return false;
            } else {
                x = v;
                xInput.setStyle(null);
            }
        } catch (Exception e) {
            xInput.setStyle(UserConfig.badStyle());
            return false;
        }
        try {
            int v = Integer.parseInt(yInput.getText().trim());
            if (v < 0 || (checkImage && v >= imageView.getImage().getHeight())) {
                yInput.setStyle(UserConfig.badStyle());
                return false;
            } else {
                y = v;
                yInput.setStyle(null);
            }
        } catch (Exception e) {
            yInput.setStyle(UserConfig.badStyle());
            return false;
        }
        return true;
    }

    public boolean checkPositionType() {
        xInput.setDisable(true);
        xInput.setStyle(null);
        yInput.setDisable(true);
        yInput.setStyle(null);
        marginInput.setDisable(true);
        marginInput.setStyle(null);

        if (rightBottomRadio.isSelected()) {
            marginInput.setDisable(false);
            return checkMargin();

        } else if (rightTopRadio.isSelected()) {
            marginInput.setDisable(false);
            return checkMargin();

        } else if (leftBottomRadio.isSelected()) {
            marginInput.setDisable(false);
            return checkMargin();

        } else if (leftTopRadio.isSelected()) {
            marginInput.setDisable(false);
            return checkMargin();

        } else if (centerRadio.isSelected()) {
            return true;

        } else if (customRadio.isSelected()) {
            xInput.setDisable(false);
            yInput.setDisable(false);
            return checkXY();
        }
        return false;
    }

    public boolean checkMargin() {
        try {
            int v = Integer.valueOf(marginInput.getText());
            if (v >= 0) {
                margin = v;
                UserConfig.setInt(baseName + "Margin", margin);
                marginInput.setStyle(null);
            } else {
                marginInput.setStyle(UserConfig.badStyle());
                return false;
            }
        } catch (Exception e) {
            marginInput.setStyle(UserConfig.badStyle());
            return false;
        }
        return true;
    }

    public void setLocation(double x, double y) {
        xInput.setText((int) x + "");
        yInput.setText((int) y + "");
        customRadio.fire();
    }

    public boolean checkParameters() {
        return checkText() && checkPositionType();
    }

    public void countBaseXY(Graphics2D g, FontMetrics metrics, double imageWidth, double imageHeight) {
        int yOffset = metrics.getAscent();
        countTextBound(g, metrics);
        if (rightBottomRadio.isSelected()) {
            baseX = (int) imageWidth - 1 - margin - textWidth;
            baseY = (int) imageHeight - 1 - margin + yOffset - textHeight;

        } else if (rightTopRadio.isSelected()) {
            baseX = (int) imageWidth - 1 - margin - textWidth;
            baseY = margin + yOffset;

        } else if (leftBottomRadio.isSelected()) {
            baseX = margin;
            baseY = (int) imageHeight - 1 - margin + yOffset - textHeight;

        } else if (leftTopRadio.isSelected()) {
            baseX = margin;
            baseY = margin + yOffset;

        } else if (centerRadio.isSelected()) {
            baseX = (int) ((imageWidth - textWidth) / 2);
            baseY = (int) ((imageHeight - textHeight) / 2) + yOffset;

        } else if (customRadio.isSelected()) {
            baseX = x;
            baseY = y + yOffset;
        } else {
            baseX = 0;
            baseY = yOffset;
        }
    }

    public void countTextBound(Graphics2D g, FontMetrics metrics) {
        String[] lines = getText().split("\n", -1);
        int lend = lines.length - 1, heightMax = 0, charWidthMax = 0;
        textWidth = 0;
        textHeight = 0;
        if (isVertical()) {
            for (int r = 0; r <= lend; r++) {
                String line = lines[r];
                int rHeight = 0;
                charWidthMax = 0;
                for (int i = 0; i < line.length(); i++) {
                    String c = line.charAt(i) + "";
                    Rectangle2D cBound = metrics.getStringBounds(c, g);
                    rHeight += (int) cBound.getHeight();
                    if (lineHeight <= 0) {
                        int charWidth = (int) cBound.getWidth();
                        if (charWidth > charWidthMax) {
                            charWidthMax = charWidth;
                        }
                    }
                }
                if (lineHeight > 0) {
                    textWidth += lineHeight;
                } else {
                    textWidth += charWidthMax;
                }
                if (rHeight > heightMax) {
                    heightMax = rHeight;
                }
            }
            textHeight = heightMax;
        } else {
            for (String line : lines) {
                Rectangle2D sBound = metrics.getStringBounds(line, g);
                if (lineHeight > 0) {
                    textHeight += lineHeight;
                } else {
                    textHeight += sBound.getHeight();
                }
                int sWidth = (int) sBound.getWidth();
                if (sWidth > charWidthMax) {
                    charWidthMax = sWidth;
                }
            }
            textWidth = charWidthMax;
        }
        if (parentController instanceof ImageManufactureTextController) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    sizeLabel.setText(message("TextSize") + ": " + textWidth + "x" + textHeight);
                }
            });
        }
    }

    @FXML
    @Override
    public void goAction() {
        if (parentController instanceof ImageManufactureTextController) {
            ((ImageManufactureTextController) parentController).goAction();
        }
    }

    /*
        get/set
     */
    public String getText() {
        return textArea.getText();
    }

    public Color getColor() {
        return (Color) colorSetController.rect.getFill();
    }

    public java.awt.Color getAwtColor() {
        return FxColorTools.toAwtColor(getColor());
    }

    public boolean isVertical() {
        return verticalCheck.isSelected();
    }

    public boolean isLeftToRight() {
        return !rightToLeftCheck.isSelected();
    }

    public boolean isOutline() {
        return outlineCheck.isSelected();
    }

    public boolean orderReversed() {
        return !blendTopCheck.isSelected();
    }

    public boolean ignoreTransparent() {
        return ignoreTransparentCheck.isSelected();
    }

    public Font getFont() {
        if (fontWeight == FontWeight.BOLD) {
            if (fontPosture == FontPosture.REGULAR) {
                return new java.awt.Font(fontFamily, java.awt.Font.BOLD, fontSize);
            } else {
                return new java.awt.Font(fontFamily, java.awt.Font.BOLD + java.awt.Font.ITALIC, fontSize);
            }
        } else {
            if (fontPosture == FontPosture.REGULAR) {
                return new java.awt.Font(fontFamily, java.awt.Font.PLAIN, fontSize);
            } else {
                return new java.awt.Font(fontFamily, java.awt.Font.ITALIC, fontSize);
            }
        }
    }

    public javafx.scene.text.Font getFxFont() {
        if (fontWeight == FontWeight.BOLD) {
            if (fontPosture == FontPosture.REGULAR) {
                return javafx.scene.text.Font.font(fontFamily, FontWeight.BOLD, FontPosture.REGULAR, fontSize);
            } else {
                return javafx.scene.text.Font.font(fontFamily, FontWeight.BOLD, FontPosture.ITALIC, fontSize);
            }
        } else {
            if (fontPosture == FontPosture.REGULAR) {
                return javafx.scene.text.Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, fontSize);
            } else {
                return javafx.scene.text.Font.font(fontFamily, FontWeight.NORMAL, FontPosture.ITALIC, fontSize);
            }
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
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

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getShadow() {
        return shadow;
    }

    public void setShadow(int shadow) {
        this.shadow = shadow;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public ImagesBlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public FontPosture getFontPosture() {
        return fontPosture;
    }

    public void setFontPosture(FontPosture fontPosture) {
        this.fontPosture = fontPosture;
    }

    public FontWeight getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(FontWeight fontWeight) {
        this.fontWeight = fontWeight;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public TextField getxInput() {
        return xInput;
    }

    public void setxInput(TextField xInput) {
        this.xInput = xInput;
    }

    public TextField getyInput() {
        return yInput;
    }

    public void setyInput(TextField yInput) {
        this.yInput = yInput;
    }

    public ComboBox<String> getLineHeightSelector() {
        return lineHeightSelector;
    }

    public void setLineHeightSelector(ComboBox<String> lineHeightSelector) {
        this.lineHeightSelector = lineHeightSelector;
    }

    public ComboBox<String> getSizeBox() {
        return sizeBox;
    }

    public void setSizeBox(ComboBox<String> sizeBox) {
        this.sizeBox = sizeBox;
    }

    public ComboBox<String> getOpacitySelector() {
        return opacitySelector;
    }

    public void setOpacitySelector(ComboBox<String> opacitySelector) {
        this.opacitySelector = opacitySelector;
    }

    public ComboBox<String> getBlendSelector() {
        return blendSelector;
    }

    public void setBlendSelector(ComboBox<String> blendSelector) {
        this.blendSelector = blendSelector;
    }

    public ComboBox<String> getStyleBox() {
        return styleBox;
    }

    public void setStyleBox(ComboBox<String> styleBox) {
        this.styleBox = styleBox;
    }

    public ComboBox<String> getFamilyBox() {
        return familyBox;
    }

    public void setFamilyBox(ComboBox<String> familyBox) {
        this.familyBox = familyBox;
    }

    public ComboBox<String> getAngleBox() {
        return angleBox;
    }

    public void setAngleBox(ComboBox<String> angleBox) {
        this.angleBox = angleBox;
    }

    public ComboBox<String> getShadowBox() {
        return shadowBox;
    }

    public void setShadowBox(ComboBox<String> shadowBox) {
        this.shadowBox = shadowBox;
    }

    public CheckBox getOutlineCheck() {
        return outlineCheck;
    }

    public void setOutlineCheck(CheckBox outlineCheck) {
        this.outlineCheck = outlineCheck;
    }

    public CheckBox getVerticalCheck() {
        return verticalCheck;
    }

    public void setVerticalCheck(CheckBox verticalCheck) {
        this.verticalCheck = verticalCheck;
    }

    public CheckBox getRightToLeftCheck() {
        return rightToLeftCheck;
    }

    public void setRightToLeftCheck(CheckBox rightToLeftCheck) {
        this.rightToLeftCheck = rightToLeftCheck;
    }

    public CheckBox getBlendTopCheck() {
        return blendTopCheck;
    }

    public void setBlendTopCheck(CheckBox blendTopCheck) {
        this.blendTopCheck = blendTopCheck;
    }

    public CheckBox getIgnoreTransparentCheck() {
        return ignoreTransparentCheck;
    }

    public void setIgnoreTransparentCheck(CheckBox ignoreTransparentCheck) {
        this.ignoreTransparentCheck = ignoreTransparentCheck;
    }

    public ColorSet getColorSetController() {
        return colorSetController;
    }

    public void setColorSetController(ColorSet colorSetController) {
        this.colorSetController = colorSetController;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public ToggleGroup getPositionGroup() {
        return positionGroup;
    }

    public void setPositionGroup(ToggleGroup positionGroup) {
        this.positionGroup = positionGroup;
    }

    public RadioButton getRightBottomRadio() {
        return rightBottomRadio;
    }

    public void setRightBottomRadio(RadioButton rightBottomRadio) {
        this.rightBottomRadio = rightBottomRadio;
    }

    public RadioButton getRightTopRadio() {
        return rightTopRadio;
    }

    public void setRightTopRadio(RadioButton rightTopRadio) {
        this.rightTopRadio = rightTopRadio;
    }

    public RadioButton getLeftBottomRadio() {
        return leftBottomRadio;
    }

    public void setLeftBottomRadio(RadioButton leftBottomRadio) {
        this.leftBottomRadio = leftBottomRadio;
    }

    public RadioButton getLeftTopRadio() {
        return leftTopRadio;
    }

    public void setLeftTopRadio(RadioButton leftTopRadio) {
        this.leftTopRadio = leftTopRadio;
    }

    public RadioButton getCenterRadio() {
        return centerRadio;
    }

    public void setCenterRadio(RadioButton centerRadio) {
        this.centerRadio = centerRadio;
    }

    public RadioButton getCustomRadio() {
        return customRadio;
    }

    public void setCustomRadio(RadioButton customRadio) {
        this.customRadio = customRadio;
    }

    public TextField getMarginInput() {
        return marginInput;
    }

    public void setMarginInput(TextField marginInput) {
        this.marginInput = marginInput;
    }

    public VBox getBaseBox() {
        return baseBox;
    }

    public void setBaseBox(VBox baseBox) {
        this.baseBox = baseBox;
    }

    public int getBaseX() {
        return baseX;
    }

    public void setBaseX(int baseX) {
        this.baseX = baseX;
    }

    public int getBaseY() {
        return baseY;
    }

    public void setBaseY(int baseY) {
        this.baseY = baseY;
    }

    public int getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    public int getTextHeight() {
        return textHeight;
    }

    public void setTextHeight(int textHeight) {
        this.textHeight = textHeight;
    }

}
