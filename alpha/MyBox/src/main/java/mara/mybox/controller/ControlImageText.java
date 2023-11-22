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
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-10
 * @License Apache License Version 2.0
 */
public class ControlImageText extends BaseController {

    protected ImageView imageView;
    protected int margin, rowHeight, x, y, fontSize, shadow, angle, baseX, baseY, textY,
            textWidth, textHeight, bordersStrokeWidth, bordersArc, bordersMargin;
    protected String text, fontFamily, fontName;
    protected FontPosture fontPosture;
    protected FontWeight fontWeight;
    protected final SimpleBooleanProperty changeNotify;
    protected boolean checkBaseAtOnce;

    @FXML
    protected TextArea textArea;
    @FXML
    protected TextField xInput, yInput, marginInput, bordersMarginInput;
    @FXML
    protected ComboBox<String> rowHeightSelector, fontSizeSelector, fontStyleSelector,
            fontFamilySelector, angleSelector, shadowSelector,
            bordersStrokeWidthSelector, bordersArcSelector;
    @FXML
    protected CheckBox outlineCheck, verticalCheck, rightToLeftCheck,
            bordersCheck, bordersFillCheck, bordersStrokeDottedCheck;
    @FXML
    protected ControlColorSet fontColorController, shadowColorController,
            bordersFillColorController, bordersStrokeColorController;
    @FXML
    protected ToggleGroup positionGroup;
    @FXML
    protected RadioButton rightBottomRadio, rightTopRadio, leftBottomRadio, leftTopRadio, centerRadio, customRadio;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected VBox bordersBox;
    @FXML
    protected Label sizeLabel;
    @FXML
    protected Button goTextButton, goLocationButton, goBordersButton;

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

            initText();
            initStyle();
            initBorders();

            checkBaseAtOnce = !(parentController instanceof ImageManufactureTextController);
            if (checkBaseAtOnce) {
                sizeLabel.setVisible(false);
                goTextButton.setVisible(false);
                goLocationButton.setVisible(false);
                goBordersButton.setVisible(false);
            }

            checkPositionType();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initText() {
        try {

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

                textArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        String v = textArea.getText();
                        if (v != null && !v.equals(text)) {
                            checkText();
                            notifyChanged();
                        }
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void initStyle() {
        try {

            rowHeight = UserConfig.getInt(baseName + "TextRowHeight", -1);
            List<String> heights = Arrays.asList(
                    message("Automatic"), "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            rowHeightSelector.getItems().addAll(heights);
            if (rowHeight <= 0) {
                rowHeightSelector.setValue(message("Automatic"));
            } else {
                rowHeightSelector.setValue(rowHeight + "");
            }
            rowHeightSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            rowHeight = v;
                        } else {
                            rowHeight = -1;
                        }
                    } catch (Exception e) {
                        rowHeight = -1;;
                    }
                    UserConfig.setInt(baseName + "TextRowHeight", rowHeight);
                    notifyChanged();
                }
            });

            fontColorController.init(this, baseName + "TextColor", Color.ORANGE);
            fontColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    notifyChanged();
                }
            });

            fontFamilySelector.getItems().addAll(javafx.scene.text.Font.getFamilies());
            fontFamily = UserConfig.getString(baseName + "TextFontFamily", "Arial");
            fontFamilySelector.getSelectionModel().select(fontFamily);
            fontFamilySelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    fontFamily = newValue;
                    UserConfig.setString(baseName + "TextFontFamily", newValue);
                    notifyChanged();
                }
            });

            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.REGULAR;
            List<String> styles = Arrays.asList(message("Regular"), message("Bold"), message("Italic"), message("Bold Italic"));
            fontStyleSelector.getItems().addAll(styles);
            fontStyleSelector.getSelectionModel().select(0);
            fontStyleSelector.valueProperty().addListener(new ChangeListener<String>() {
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
            fontSizeSelector.getItems().addAll(sizes);
            fontSize = UserConfig.getInt(baseName + "TextFontSize", 72);
            fontSizeSelector.getSelectionModel().select(fontSize + "");
            fontSizeSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            fontSize = v;
                            UserConfig.setInt(baseName + "TextFontSize", v);
                            notifyChanged();
                            ValidationTools.setEditorNormal(fontSizeSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(fontSizeSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(fontSizeSelector);
                    }
                }
            });

            blendController.setParameters(this, imageView);
            blendController.optionChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    notifyChanged();
                }
            });

            shadowSelector.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            shadow = UserConfig.getInt(baseName + "TextShadow", 0);
            shadowSelector.getSelectionModel().select(shadow + "");
            shadowSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            shadow = v;
                            UserConfig.setInt(baseName + "TextShadow", v);
                            ValidationTools.setEditorNormal(shadowSelector);
                            notifyChanged();
                        } else {
                            ValidationTools.setEditorBadStyle(shadowSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(shadowSelector);
                    }
                }
            });

            shadowColorController.init(this, baseName + "ShadowColor", Color.GREY);
            shadowColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    notifyChanged();
                }
            });

            angleSelector.getItems().addAll(Arrays.asList("0", "90", "180", "270", "45", "135", "225", "315",
                    "60", "150", "240", "330", "15", "105", "195", "285", "30", "120", "210", "300"));
            angle = UserConfig.getInt(baseName + "TextAngle", 0);
            angleSelector.getSelectionModel().select(angle + "");
            angleSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            angle = v;
                            UserConfig.setInt(baseName + "TextAngle", v);
                            ValidationTools.setEditorNormal(angleSelector);
                            notifyChanged();
                        } else {
                            ValidationTools.setEditorBadStyle(angleSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(angleSelector);
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initBorders() {
        try {
            bordersBox.disableProperty().bind(bordersCheck.selectedProperty().not());

            bordersCheck.setSelected(UserConfig.getBoolean(baseName + "Borders", false));
            bordersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Borders", bordersCheck.isSelected());
                    notifyChanged();
                }
            });

            bordersFillCheck.setSelected(UserConfig.getBoolean(baseName + "BordersFill", true));
            bordersFillCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "BordersFill", bordersFillCheck.isSelected());
                    if (showBorders()) {
                        notifyChanged();
                    }
                }
            });

            bordersFillColorController.init(this, baseName + "BordersFillColor", Color.WHITE);
            bordersFillColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (showBorders()) {
                        notifyChanged();
                    }
                }
            });

            bordersStrokeColorController.init(this, baseName + "BordersStrokeColor", Color.WHITE);
            bordersStrokeColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (showBorders()) {
                        notifyChanged();
                    }
                }
            });

            bordersStrokeWidth = UserConfig.getInt(baseName + "BordersStrokeWidth", 0);
            bordersStrokeWidthSelector.getItems().addAll(Arrays.asList("0", "1", "2", "4", "3", "5", "10", "6"));
            bordersStrokeWidthSelector.setValue(bordersStrokeWidth + "");
            bordersStrokeWidthSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    bordersStrokeWidth = 0;
                    try {
                        bordersStrokeWidth = Integer.parseInt(newValue);
                        if (bordersStrokeWidth < 0) {
                            bordersStrokeWidth = 0;
                        }
                    } catch (Exception e) {
                        bordersStrokeWidth = 0;
                    }
                    UserConfig.setInt(baseName + "BordersStrokeWidth", bordersStrokeWidth);
                    if (showBorders()) {
                        notifyChanged();
                    }
                }
            });

            bordersStrokeDottedCheck.setSelected(UserConfig.getBoolean(baseName + "BordersStrokeDotted", false));
            bordersStrokeDottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "BordersStrokeDotted", bordersStrokeDottedCheck.isSelected());
                    if (showBorders()) {
                        notifyChanged();
                    }
                }
            });

            bordersArc = UserConfig.getInt(baseName + "BordersArc", 0);
            bordersArcSelector.getItems().addAll(Arrays.asList(
                    "0", "3", "5", "2", "1", "8", "10", "15", "20", "30", "48", "64", "96"));
            bordersArcSelector.setValue(bordersArc + "");
            bordersArcSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    bordersArc = 0;
                    try {
                        bordersArc = Integer.parseInt(newValue);
                        if (bordersArc < 0) {
                            bordersArc = 0;
                        }
                    } catch (Exception e) {
                        bordersArc = 0;
                    }
                    UserConfig.setInt(baseName + "BordersArc", bordersArc);
                    if (showBorders()) {
                        notifyChanged();
                    }
                }
            });

            bordersMargin = UserConfig.getInt(baseName + "BordersMargin", 10);
            bordersMarginInput.setText(bordersMargin + "");

            if (checkBaseAtOnce) {
                bordersMarginInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        goBorders();
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkText() {
        text = text();
        if (text == null || text.isEmpty()) {
            textArea.setStyle(UserConfig.badStyle());
            return false;
        } else {
            textArea.setStyle(null);
            UserConfig.setString(baseName + "TextValue", text);
            TableStringValues.add("ImageTextHistories", text);
            return true;
        }
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
            int v = Integer.parseInt(marginInput.getText());
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
        customRadio.setSelected(true);
    }

    public boolean checkParameters() {
        return checkText() && checkPositionType() && checkBordersMargin();
    }

    public void countValues(Graphics2D g, FontMetrics metrics, double imageWidth, double imageHeight) {
        countTextBound(g, metrics);
        if (rightBottomRadio.isSelected()) {
            baseX = (int) imageWidth - margin - textWidth;
            baseY = (int) imageHeight - margin - textHeight;

        } else if (rightTopRadio.isSelected()) {
            baseX = (int) imageWidth - margin - textWidth;
            baseY = margin;

        } else if (leftBottomRadio.isSelected()) {
            baseX = margin;
            baseY = (int) imageHeight - margin - textHeight;

        } else if (leftTopRadio.isSelected()) {
            baseX = margin;
            baseY = margin;

        } else if (centerRadio.isSelected()) {
            baseX = (int) ((imageWidth - textWidth) / 2);
            baseY = (int) ((imageHeight - textHeight) / 2);

        } else if (customRadio.isSelected()) {
            baseX = x;
            baseY = y;
        } else {
            baseX = 0;
            baseY = 0;
        }
        textY = baseY + metrics.getAscent();
    }

    public void countTextBound(Graphics2D g, FontMetrics metrics) {
        String[] lines = text().split("\n", -1);
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
                    if (rowHeight <= 0) {
                        int charWidth = (int) cBound.getWidth();
                        if (charWidth > charWidthMax) {
                            charWidthMax = charWidth;
                        }
                    }
                }
                if (rowHeight > 0) {
                    textWidth += rowHeight;
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
                if (rowHeight > 0) {
                    textHeight += rowHeight;
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
    public void goLocation() {
        apply();
    }

    @FXML
    public void goText() {
        apply();
    }

    public void apply() {
        if (!checkParameters()) {
            popError(Languages.message("InvalidParameters"));
            return;
        }
        notifyChanged();
    }

    public boolean checkBordersMargin() {
        try {
            bordersMargin = Integer.parseInt(bordersMarginInput.getText());
            UserConfig.setInt(baseName + "BordersMargin", bordersMargin);
        } catch (Exception e) {
            bordersMarginInput.setStyle(UserConfig.badStyle());
            return false;
        }
        return true;
    }

    @FXML
    public void goBorders() {
        goAction();
    }

    @FXML
    protected void showTextHistories(Event event) {
        PopTools.popStringValues(this, textArea, event, "ImageTextHistories", false, true);
    }

    @FXML
    public void popTextHistories(Event event) {
        if (UserConfig.getBoolean("ImageTextHistoriesPopWhenMouseHovering", false)) {
            showTextHistories(event);
        }
    }

    /*
        refer
     */
    public String text() {
        return textArea.getText();
    }

    public Font font() {
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

    public javafx.scene.text.Font fxFont() {
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

    public java.awt.Color textColor() {
        return fontColorController.awtColor();
    }

    public java.awt.Color shadowColor() {
        return shadowColorController.awtColor();
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
        return !blendController.foreTopCheck.isSelected();
    }

    public boolean ignoreTransparent() {
        return blendController.ignoreTransparentCheck.isSelected();
    }

    public PixelsBlend.ImagesBlendMode getBlendMode() {
        return blendController.blendMode;
    }

    public float getOpacity() {
        return blendController.opacity;
    }

    public PixelsBlend blender() {
        return blendController.pickValues();
    }

    public boolean showBorders() {
        return bordersCheck.isSelected();
    }

    public boolean bordersDotted() {
        return bordersStrokeDottedCheck.isSelected();
    }

    public boolean bordersFilled() {
        return bordersFillCheck.isSelected();
    }

    public Color bordersStrokeColor() {
        return bordersStrokeColorController.color();
    }

    public Color bordersFillColor() {
        return bordersFillColorController.color();
    }

    /*
        get
     */
    public int getRowHeight() {
        return rowHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getShadow() {
        return shadow;
    }

    public int getAngle() {
        return angle;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public String getFontName() {
        return fontName;
    }

    public int getMargin() {
        return margin;
    }

    public int getBaseX() {
        return baseX;
    }

    public int getBaseY() {
        return baseY;
    }

    public int getTextWidth() {
        return textWidth;
    }

    public int getTextHeight() {
        return textHeight;
    }

    public int getTextY() {
        return textY;
    }

    public int getBordersStrokeWidth() {
        return bordersStrokeWidth;
    }

    public int getBordersArc() {
        return bordersArc;
    }

    public int getBordersMargin() {
        return bordersMargin;
    }

    public boolean isCheckBaseAtOnce() {
        return checkBaseAtOnce;
    }

}
