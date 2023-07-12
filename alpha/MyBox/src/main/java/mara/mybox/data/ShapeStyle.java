package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import static mara.mybox.fximage.FxColorTools.toAwtColor;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-11
 * @License Apache License Version 2.0
 */
public class ShapeStyle {

    public static final String DefaultStrokeColor = "#c94d58", DefaultAnchorColor = "#0066cc";


    /*
        get
     */
    public static String name(DoubleShape shape) {
        return shape != null ? shape.getClass().getSimpleName() : "";
    }

    public static float strokeWidth(DoubleShape shape) {
        float v = UserConfig.getFloat(name(shape) + "StrokeWidth", 2);
        if (v < 0) {
            v = 2f;
        }
        return v;
    }

    public static Color strokeColor(DoubleShape shape) {
        return Color.web(UserConfig.getString(name(shape) + "StrokeColor", DefaultStrokeColor));
    }

    public static java.awt.Color strokeColorAwt(DoubleShape shape) {
        return toAwtColor(strokeColor(shape));
    }

    public static float strokeOpacity(DoubleShape shape) {
        float v = UserConfig.getFloat(name(shape) + "StrokeOpacity", 1f);
        if (v < 0) {
            v = 1f;
        }
        return v;
    }

    public static boolean isFillColor(DoubleShape shape) {
        return UserConfig.getBoolean(name(shape) + "IsFillColor", false);
    }

    public static Color fillColor(DoubleShape shape) {
        return Color.web(UserConfig.getString(name(shape) + "FillColor", "0xFFFFFFFF"));
    }

    public static java.awt.Color fillColorAwt(DoubleShape shape) {
        return toAwtColor(fillColor(shape));
    }

    public static float fillOpacity(DoubleShape shape) {
        float v = UserConfig.getFloat(name(shape) + "FillOpacity", 0.3f);
        if (v < 0) {
            v = 0.3f;
        }
        return v;
    }

    public static Color anchorColor(DoubleShape shape) {
        return Color.web(UserConfig.getString(name(shape) + "AnchorColor", DefaultStrokeColor));
    }

    public static float anchorSize(DoubleShape shape) {
        float v = UserConfig.getFloat(name(shape) + "AnchorSize", 10);
        if (v < 0) {
            v = 10f;
        }
        return v;
    }

    public static List<Float> strokeDash(DoubleShape shape) {
        try {
            double strokeWidth = strokeWidth(shape);
            String text = UserConfig.getString(name(shape) + "StrokeDash", strokeWidth + " " + strokeWidth * 3);
            if (text == null || text.isBlank()) {
                return null;
            }
            String[] values = text.split("\\s+");
            if (values == null || values.length == 0) {
                return null;
            }
            List<Float> dash = new ArrayList<>();
            for (String v : values) {
                dash.add(Float.valueOf(v));
            }
            return dash;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[] strokeDashAwt(DoubleShape shape) {
        try {
            List<Float> vs = strokeDash(shape);
            if (vs == null || vs.isEmpty()) {
                return null;
            }
            float[] values = new float[vs.size()];
            for (int i = 0; i < vs.size(); i++) {
                values[i] = vs.get(i);
            }
            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public static String strokeDashString(DoubleShape shape) {
        try {
            List<Float> vs = strokeDash(shape);
            if (vs == null || vs.isEmpty()) {
                return null;
            }
            String s = "";
            for (int i = 0; i < vs.size(); i++) {
                s += vs.get(i) + " ";
            }
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    public static StrokeLineCap lineCap(DoubleShape shape) {
        try {
            String v = UserConfig.getString(name(shape) + "LineCap", "BUTT");
            if (v == null || v.isBlank()) {
                return StrokeLineCap.BUTT;
            }
            if ("BUTT".equalsIgnoreCase(v)) {
                return StrokeLineCap.ROUND;
            } else if ("SQUARE".equalsIgnoreCase(v)) {
                return StrokeLineCap.SQUARE;
            } else {
                return StrokeLineCap.BUTT;
            }
        } catch (Exception e) {
            return StrokeLineCap.BUTT;
        }
    }

    public static int lineCapAwt(DoubleShape shape) {
        try {
            StrokeLineCap v = lineCap(shape);
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

    public static int roundArc(DoubleShape shape) {
        return UserConfig.getInt(name(shape) + "RoundArc", 0);
    }

    /*
        set
     */
    public static void setStrokeColor(DoubleShape shape, Color strokeColor) {
        UserConfig.setString(name(shape) + "StrokeColor", strokeColor != null ? strokeColor.toString() : null);
    }

    public static void setAnchorColor(DoubleShape shape, Color anchorColor) {
        UserConfig.setString(name(shape) + "AnchorColor", anchorColor != null ? anchorColor.toString() : null);
    }

    public static void setIsFillColor(DoubleShape shape, boolean fillColor) {
        UserConfig.setBoolean(name(shape) + "IsFillColor", fillColor);
    }

    public static void setFillColor(DoubleShape shape, Color fillColor) {
        UserConfig.setString(name(shape) + "FillColor", fillColor != null ? fillColor.toString() : null);
    }

    public static void setStrokeWidth(DoubleShape shape, float strokeWidth) {
        UserConfig.setFloat(name(shape) + "StrokeWidth", strokeWidth);
    }

    public static void setAnchorSize(DoubleShape shape, float anchorSize) {
        UserConfig.setFloat(name(shape) + "AnchorSize", anchorSize);
    }

    public static void setStrokeOpacity(DoubleShape shape, float strokeOpacity) {
        UserConfig.setFloat(name(shape) + "StrokeOpacity", strokeOpacity);
    }

    public static void setFillOpacity(DoubleShape shape, float fillOpacity) {
        UserConfig.setFloat(name(shape) + "FillOpacity", fillOpacity);
    }

    public static void setStrokeDashed(DoubleShape shape, boolean strokeDashed) {
        if (strokeDashed) {
            double strokeWidth = strokeWidth(shape);
            UserConfig.setString(name(shape) + "StrokeDash", strokeWidth + " " + strokeWidth * 3);
        } else {
            UserConfig.setString(name(shape) + "StrokeDash", null);
        }
    }

    public static void setStrokeDashString(DoubleShape shape, String strokeDash) {
        if (strokeDash == null || strokeDash.isEmpty()) {
            UserConfig.setString(name(shape) + "StrokeDash", null);

        } else {
            UserConfig.setString(name(shape) + "StrokeDash", strokeDash);
        }
    }

    public static void setStrokeDash(DoubleShape shape, List<Float> strokeDash) {
        if (strokeDash == null || strokeDash.isEmpty()) {
            UserConfig.setString(name(shape) + "StrokeDash", null);
        } else {
            String s = "";
            for (Float v : strokeDash) {
                s += v + " ";
            }
            UserConfig.setString(name(shape) + "StrokeDash", s);
        }
    }

    public static void setLineCap(DoubleShape shape, StrokeLineCap lineCap) {
        UserConfig.setString(name(shape) + "StrokeLineCap", lineCap != null ? lineCap.name() : null);
    }

    public static void setRoundArc(DoubleShape shape, int arc) {
        UserConfig.setInt(name(shape) + "RoundArc", arc);
    }

}
