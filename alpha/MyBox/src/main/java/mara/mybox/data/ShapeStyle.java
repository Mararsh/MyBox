package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import static javafx.scene.shape.StrokeLineCap.ROUND;
import static javafx.scene.shape.StrokeLineCap.SQUARE;
import mara.mybox.fximage.FxColorTools;
import static mara.mybox.fximage.FxColorTools.toAwtColor;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-11
 * @License Apache License Version 2.0
 */
public class ShapeStyle {

    public final static String DefaultStrokeColor = "#c94d58", DefaultAnchorColor = "#0066cc";

    private String name, more;
    private Color strokeColor, fillColor, anchorColor;
    private float strokeWidth, strokeOpacity, fillOpacity, anchorSize;
    private boolean isFillColor, isStrokeDash;
    private List<Double> strokeDash;
    private StrokeLineCap lineCap;

    public ShapeStyle() {
        init("");
    }

    public ShapeStyle(String name) {
        init(name);
    }

    final public void init(String name) {
        this.name = name != null ? name : "";
        try {
            strokeColor = Color.web(UserConfig.getString(name + "StrokeColor", DefaultStrokeColor));
        } catch (Exception e) {
            strokeColor = Color.web(DefaultStrokeColor);
        }
        try {
            fillColor = Color.web(UserConfig.getString(name + "FillColor", "0xFFFFFFFF"));
        } catch (Exception e) {
            fillColor = Color.TRANSPARENT;
        }
        try {
            anchorColor = Color.web(UserConfig.getString(name + "AnchorColor", DefaultAnchorColor));
        } catch (Exception e) {
            anchorColor = Color.web(DefaultAnchorColor);
        }
        strokeWidth = UserConfig.getFloat(name + "StrokeWidth", 2);
        if (strokeWidth < 0) {
            strokeWidth = 2f;
        }
        strokeOpacity = UserConfig.getFloat(name + "StrokeOpacity", 1);
        if (strokeOpacity < 0) {
            strokeOpacity = 1;
        }
        fillOpacity = UserConfig.getFloat(name + "FillOpacity", 1f);
        if (fillOpacity < 0) {
            fillOpacity = 1f;
        }
        isFillColor = UserConfig.getBoolean(name + "IsFillColor", false);
        isStrokeDash = UserConfig.getBoolean(name + "IsStrokeDash", false);
        anchorSize = UserConfig.getFloat(name + "AnchorSize", 10);
        if (anchorSize < 0) {
            anchorSize = 10;
        }
        String text = UserConfig.getString(name + "StrokeDash", null);
        strokeDash = text2StrokeDash(text);
        text = UserConfig.getString(name + "StrokeLineCap", "BUTT");
        lineCap = lineCap(text);
        more = null;
    }

    /*
        static
     */
    public static List<Double> text2StrokeDash(String text) {
        try {
            if (text == null || text.isBlank()) {
                return null;
            }
            String[] values = text.split("\\s+");
            if (values == null || values.length == 0) {
                return null;
            }
            List<Double> dash = new ArrayList<>();
            for (String v : values) {
                dash.add(Double.valueOf(v));
            }
            return dash;
        } catch (Exception e) {
            return null;
        }
    }

    public static String strokeDash2Text(List<Double> dash) {
        try {
            if (dash == null || dash.isEmpty()) {
                return null;
            }
            String text = "";
            for (Double v : dash) {
                text += v + " ";
            }
            return text;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[] strokeDashAwt(List<Double> vs) {
        try {
            if (vs == null || vs.isEmpty()) {
                return null;
            }
            float[] values = new float[vs.size()];
            for (int i = 0; i < vs.size(); i++) {
                values[i] = vs.get(i).floatValue();
            }
            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public static StrokeLineCap lineCap(String text) {
        try {
            if (text == null || text.isBlank()) {
                return StrokeLineCap.BUTT;
            }
            if ("ROUND".equalsIgnoreCase(text)) {
                return StrokeLineCap.ROUND;
            } else if ("SQUARE".equalsIgnoreCase(text)) {
                return StrokeLineCap.SQUARE;
            } else {
                return StrokeLineCap.BUTT;
            }
        } catch (Exception e) {
            return StrokeLineCap.BUTT;
        }
    }

    public static int lineCapAwt(StrokeLineCap v) {
        try {
            if (null == v) {
                return java.awt.BasicStroke.CAP_BUTT;
            } else {
                switch (v) {
                    case ROUND:
                        return java.awt.BasicStroke.CAP_ROUND;
                    case SQUARE:
                        return java.awt.BasicStroke.CAP_SQUARE;
                    default:
                        return java.awt.BasicStroke.CAP_BUTT;
                }
            }
        } catch (Exception e) {
            return java.awt.BasicStroke.CAP_BUTT;
        }
    }

    /*
        set
     */
    public ShapeStyle setName(String name) {
        this.name = name;
        return this;
    }

    public ShapeStyle setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
        UserConfig.setString(name + "StrokeColor", strokeColor != null ? strokeColor.toString() : null);
        return this;
    }

    public ShapeStyle setAnchorColor(Color anchorColor) {
        this.anchorColor = anchorColor;
        UserConfig.setString(name + "AnchorColor", anchorColor != null ? anchorColor.toString() : null);
        return this;
    }

    public ShapeStyle setIsFillColor(boolean isFillColor) {
        this.isFillColor = isFillColor;
        UserConfig.setBoolean(name + "IsFillColor", isFillColor);
        return this;
    }

    public ShapeStyle setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        UserConfig.setString(name + "FillColor", fillColor != null ? fillColor.toString() : null);
        return this;
    }

    public ShapeStyle setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        UserConfig.setFloat(name + "StrokeWidth", strokeWidth);
        return this;
    }

    public ShapeStyle setAnchorSize(float anchorSize) {
        this.anchorSize = anchorSize;
        UserConfig.setFloat(name + "AnchorSize", anchorSize);
        return this;
    }

    public ShapeStyle setStrokeOpacity(float strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
        UserConfig.setFloat(name + "StrokeOpacity", strokeOpacity);
        return this;
    }

    public ShapeStyle setFillOpacity(float fillOpacity) {
        this.fillOpacity = fillOpacity;
        UserConfig.setFloat(name + "FillOpacity", fillOpacity);
        return this;
    }

    public ShapeStyle setIsStrokeDash(boolean isStrokeDash) {
        this.isStrokeDash = isStrokeDash;
        UserConfig.setBoolean(name + "IsStrokeDash", isStrokeDash);
        return this;
    }

    public ShapeStyle setStrokeDashed(boolean dashed) {
        setIsStrokeDash(dashed);
        if (dashed) {
            setStrokeDash(text2StrokeDash(strokeWidth + " " + strokeWidth * 3));
        }
        return this;
    }

    public ShapeStyle setStrokeDash(List<Double> strokeDash) {
        this.strokeDash = strokeDash;
        UserConfig.setString(name + "StrokeDash", strokeDash2Text(strokeDash));
        return this;
    }

    public ShapeStyle setStrokeDashText(String text) {
        setStrokeDash(text2StrokeDash(text));
        return this;
    }

    public ShapeStyle setLineCap(StrokeLineCap lineCap) {
        this.lineCap = lineCap;
        UserConfig.setString(name + "StrokeLineCap", lineCap != null ? lineCap.name() : null);
        return this;
    }

    public ShapeStyle setMore(String more) {
        this.more = more;
        return this;
    }

    /*
        get
     */
    public String getName() {
        return name;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public String getStrokeColorCss() {
        return FxColorTools.color2css(strokeColor);
    }

    public java.awt.Color getStrokeColorAwt() {
        return toAwtColor(getStrokeColor());
    }

    public Color getFillColor() {
        return fillColor;
    }

    public java.awt.Color getFillColorAwt() {
        return toAwtColor(getFillColor());
    }

    public String getFilleColorCss() {
        return FxColorTools.color2css(fillColor);
    }

    public Color getAnchorColor() {
        return anchorColor;
    }

    public java.awt.Color getAnchorColorAwt() {
        return toAwtColor(getAnchorColor());
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public float getStrokeOpacity() {
        return strokeOpacity;
    }

    public float getFillOpacity() {
        return fillOpacity;
    }

    public float getAnchorSize() {
        return anchorSize;
    }

    public boolean isIsFillColor() {
        return isFillColor;
    }

    public boolean isIsStrokeDash() {
        return isStrokeDash;
    }

    public List<Double> getStrokeDash() {
        return strokeDash;
    }

    public float[] getStrokeDashAwt() {
        return strokeDashAwt(strokeDash);
    }

    public String getStrokeDashText() {
        return strokeDash2Text(strokeDash);
    }

    public StrokeLineCap getLineCap() {
        return lineCap;
    }

    public int getLineCapAwt() {
        return lineCapAwt(lineCap);
    }

    public String getLineCapText() {
        return lineCap != null ? lineCap.name() : null;
    }

    public String getMore() {
        return more;
    }

}
