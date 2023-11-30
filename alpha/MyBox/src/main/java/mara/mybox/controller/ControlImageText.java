package mara.mybox.controller;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.data.ShapeStyle;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
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
    protected PixelsBlend blend;
    protected ShapeStyle borderStyle;

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

    public void setParameters(BaseController parent, ImageView imageView) {
        parentController = parent;
        this.imageView = imageView;
        baseName = parentController.baseName;

        try (Connection conn = DerbyBase.getConnection()) {
            initText(conn);
            initStyle(conn);
            initBorders(conn);
            initPosition(conn);
            blendController.setParameters(conn, parent, imageView);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initText(Connection conn) {
        try {
            textArea.setText(UserConfig.getString(conn, baseName + "TextValue", "MyBox"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initPosition(Connection conn) {
        try {
            margin = UserConfig.getInt(conn, baseName + "Margin", 20);
            marginInput.setText(margin + "");

            positionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkPositionType();
                }
            });
            checkPositionType();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkPositionType() {
        xInput.setDisable(true);
        yInput.setDisable(true);
        marginInput.setDisable(true);

        if (rightBottomRadio.isSelected()) {
            marginInput.setDisable(false);

        } else if (rightTopRadio.isSelected()) {
            marginInput.setDisable(false);

        } else if (leftBottomRadio.isSelected()) {
            marginInput.setDisable(false);

        } else if (leftTopRadio.isSelected()) {
            marginInput.setDisable(false);

        } else if (centerRadio.isSelected()) {

        } else if (customRadio.isSelected()) {
            xInput.setDisable(false);
            yInput.setDisable(false);
        }
        return true;
    }

    public void initStyle(Connection conn) {
        try {
            rowHeight = UserConfig.getInt(conn, baseName + "TextRowHeight", -1);
            List<String> heights = Arrays.asList(
                    message("Automatic"), "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            rowHeightSelector.getItems().addAll(heights);
            if (rowHeight <= 0) {
                rowHeightSelector.setValue(message("Automatic"));
            } else {
                rowHeightSelector.setValue(rowHeight + "");
            }

            fontColorController.setConn(conn).init(this, baseName + "TextColor", Color.ORANGE);

            fontFamilySelector.getItems().addAll(javafx.scene.text.Font.getFamilies());
            fontFamily = UserConfig.getString(conn, baseName + "TextFontFamily", "Arial");
            fontFamilySelector.getSelectionModel().select(fontFamily);

            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.REGULAR;
            List<String> styles = Arrays.asList(message("Regular"), message("Bold"), message("Italic"), message("Bold Italic"));
            fontStyleSelector.getItems().addAll(styles);
            fontStyleSelector.getSelectionModel().select(0);

            List<String> sizes = Arrays.asList(
                    "72", "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            fontSizeSelector.getItems().addAll(sizes);
            fontSize = UserConfig.getInt(conn, baseName + "TextFontSize", 72);
            fontSizeSelector.getSelectionModel().select(fontSize + "");

            shadowSelector.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            shadow = UserConfig.getInt(conn, baseName + "TextShadow", 0);
            shadowSelector.getSelectionModel().select(shadow + "");

            shadowColorController.setConn(conn).init(this, baseName + "ShadowColor", Color.GREY);

            angleSelector.getItems().addAll(Arrays.asList("0", "90", "180", "270", "45", "135", "225", "315",
                    "60", "150", "240", "330", "15", "105", "195", "285", "30", "120", "210", "300"));
            angle = UserConfig.getInt(conn, baseName + "TextAngle", 0);
            angleSelector.getSelectionModel().select(angle + "");

            outlineCheck.setSelected(UserConfig.getBoolean(conn, baseName + "TextOutline", false));

            verticalCheck.setSelected(UserConfig.getBoolean(conn, baseName + "TextVertical", false));

            rightToLeftCheck.setSelected(UserConfig.getBoolean(conn, baseName + "TextRightToLeft", false));
            rightToLeftCheck.visibleProperty().bind(verticalCheck.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initBorders(Connection conn) {
        try {
            bordersBox.disableProperty().bind(bordersCheck.selectedProperty().not());

            bordersCheck.setSelected(UserConfig.getBoolean(conn, baseName + "Borders", false));

            bordersFillCheck.setSelected(UserConfig.getBoolean(conn, baseName + "BordersFill", true));

            bordersFillColorController.setConn(conn).init(this, baseName + "BordersFillColor", Color.WHITE);

            bordersStrokeColorController.setConn(conn).init(this, baseName + "BordersStrokeColor", Color.WHITE);

            bordersStrokeWidth = UserConfig.getInt(conn, baseName + "BordersStrokeWidth", 0);
            if (bordersStrokeWidth < 0) {
                bordersStrokeWidth = 0;
            }
            bordersStrokeWidthSelector.getItems().addAll(Arrays.asList("0", "1", "2", "4", "3", "5", "10", "6"));
            bordersStrokeWidthSelector.setValue(bordersStrokeWidth + "");

            bordersStrokeDottedCheck.setSelected(UserConfig.getBoolean(conn, baseName + "BordersStrokeDotted", false));

            bordersArc = UserConfig.getInt(conn, baseName + "BordersArc", 0);
            if (bordersArc < 0) {
                bordersArc = 0;
            }
            bordersArcSelector.getItems().addAll(Arrays.asList(
                    "0", "3", "5", "2", "1", "8", "10", "15", "20", "30", "48", "64", "96"));
            bordersArcSelector.setValue(bordersArc + "");

            bordersMargin = UserConfig.getInt(conn, baseName + "BordersMargin", 10);
            bordersMarginInput.setText(bordersMargin + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkText() {
        text = text();
        if (text == null || text.isEmpty()) {
            popError(message("InvalidParameters") + ": " + message("Text"));
            return false;
        }
        return true;
    }

    public boolean checkLocation() {
        if (customRadio.isSelected()) {
            try {
                x = Integer.parseInt(xInput.getText().trim());
            } catch (Exception e) {
                popError(message("InvalidParameters") + ": x");
                return false;
            }
            try {
                y = Integer.parseInt(yInput.getText().trim());
            } catch (Exception e) {
                popError(message("InvalidParameters") + ": y");
                return false;
            }
        }
        if (!marginInput.isDisable()) {
            try {
                margin = Integer.parseInt(marginInput.getText().trim());
            } catch (Exception e) {
                popError(message("InvalidParameters") + ": y" + message("Margins"));
                return false;
            }
        }
        return true;
    }

    public boolean checkStyle() {
        try {
            String s = rowHeightSelector.getValue();
            if (message("Automatic").equals(s)) {
                rowHeight = -1;
            } else {
                int v = Integer.parseInt(s);
                if (v >= 0) {
                    rowHeight = v;
                } else {
                    rowHeight = -1;
                }
            }
        } catch (Exception e) {
            popError(message("InvalidParameters") + ": " + message("RowHeightPx"));
            return false;
        }

        fontFamily = fontFamilySelector.getValue();

        String s = fontStyleSelector.getValue();
        if (message("Bold").equals(s)) {
            fontWeight = FontWeight.BOLD;
            fontPosture = FontPosture.REGULAR;
        } else if (message("Italic").equals(s)) {
            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.ITALIC;
        } else if (message("Bold Italic").equals(s)) {
            fontWeight = FontWeight.BOLD;
            fontPosture = FontPosture.ITALIC;
        } else {
            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.REGULAR;
        }

        int v = -1;
        try {
            v = Integer.parseInt(fontSizeSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0) {
            fontSize = v;
        } else {
            popError(message("InvalidParameters") + ": " + message("FontSize"));
            return false;
        }

        v = -1;
        try {
            v = Integer.parseInt(shadowSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0) {
            shadow = v;
        } else {
            popError(message("InvalidParameters") + ": " + message("Shadow"));
            return false;
        }

        v = -1;
        try {
            v = Integer.parseInt(angleSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0) {
            angle = v;
        } else {
            popError(message("InvalidParameters") + ": " + message("Angle"));
            return false;
        }

        return true;
    }

    public boolean checkBorders() {
        int v = -1;
        try {
            v = Integer.parseInt(bordersMarginInput.getText());
        } catch (Exception e) {
        }
        if (v < 0) {
            popError(message("InvalidParameters") + ": " + message("BordersMargin"));
            return false;
        }
        bordersMargin = v;

        v = -1;
        try {
            v = Integer.parseInt(bordersStrokeWidthSelector.getValue());
        } catch (Exception e) {
        }
        if (v < 0) {
            popError(message("InvalidParameters") + ": " + message("StrokeWidth"));
            return false;
        }
        bordersStrokeWidth = v;

        v = -1;
        try {
            v = Integer.parseInt(bordersArcSelector.getValue());
        } catch (Exception e) {
        }
        if (v < 0) {
            popError(message("InvalidParameters") + ": " + message("Arc"));
            return false;
        }
        bordersArc = v;
        return true;
    }

    public boolean checkBlend() {
        return blendController.checkValues();
    }

    public boolean pickValues() {
        if (!checkText() || !checkLocation()
                || !checkStyle() || !checkBorders() || !checkBlend()) {
            return false;
        }
        blend = null;
        borderStyle = null;
        try (Connection conn = DerbyBase.getConnection()) {
            UserConfig.setString(conn, baseName + "TextValue", text);
            TableStringValues.add(conn, "ImageTextHistories", text);

            UserConfig.setInt(conn, baseName + "TextRowHeight", rowHeight);
            UserConfig.setInt(conn, baseName + "TextAngle", angle);
            UserConfig.setInt(conn, baseName + "TextShadow", shadow);
            UserConfig.setString(conn, baseName + "TextFontFamily", fontFamily);
            UserConfig.setInt(conn, baseName + "TextFontSize", fontSize);
            UserConfig.setInt(conn, baseName + "Margin", margin);
            UserConfig.setBoolean(conn, baseName + "TextOutline", outlineCheck.isSelected());
            UserConfig.setBoolean(conn, baseName + "TextVertical", verticalCheck.isSelected());
            UserConfig.setBoolean(conn, baseName + "TextRightToLeft", rightToLeftCheck.isSelected());

            UserConfig.setInt(conn, baseName + "BordersArc", bordersArc);
            UserConfig.setInt(conn, baseName + "BordersStrokeWidth", bordersStrokeWidth);
            UserConfig.setInt(conn, baseName + "BordersMargin", bordersMargin);
            UserConfig.setBoolean(conn, baseName + "BordersFill", bordersFillCheck.isSelected());
            UserConfig.setBoolean(conn, baseName + "BordersStrokeDotted", bordersStrokeDottedCheck.isSelected());
            UserConfig.setBoolean(conn, baseName + "Borders", bordersCheck.isSelected());

            blend = blendController.pickValues(conn);

            if (bordersCheck.isSelected()) {
                borderStyle = new ShapeStyle(conn, "Text")
                        .setStrokeColor(bordersStrokeColorController.color())
                        .setStrokeWidth(bordersStrokeWidth)
                        .setIsFillColor(bordersFillCheck.isSelected())
                        .setFillColor(bordersFillColorController.color())
                        .setFillOpacity(getOpacity())
                        .setStrokeDashed(bordersStrokeDottedCheck.isSelected());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return blend != null;
    }

    public void setLocation(double x, double y) {
        xInput.setText((int) x + "");
        yInput.setText((int) y + "");
        customRadio.setSelected(true);
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

    public PixelsBlend getBlend() {
        return blend;
    }

    public ShapeStyle getBorderStyle() {
        return borderStyle;
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

    public java.awt.Color bordersStrokeColor() {
        return bordersStrokeColorController.awtColor();
    }

    public java.awt.Color bordersFillColor() {
        return bordersFillColorController.awtColor();
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

}
