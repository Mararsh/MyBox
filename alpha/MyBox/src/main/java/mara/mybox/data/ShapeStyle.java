package mara.mybox.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import static javafx.scene.shape.StrokeLineCap.ROUND;
import static javafx.scene.shape.StrokeLineCap.SQUARE;
import javafx.scene.shape.StrokeLineJoin;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
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
    private float strokeWidth, strokeOpacity, fillOpacity, anchorSize, strokeLineLimit, dashOffset;
    private boolean isFillColor, isStrokeDash;
    private List<Double> strokeDash;
    private StrokeLineCap strokeLineCap;
    private StrokeLineJoin strokeLineJoin;

    public ShapeStyle() {
        init("");
    }

    public ShapeStyle(String name) {
        init(name);
    }

    public ShapeStyle(Connection conn, String name) {
        init(conn, name);
    }

    final public void init(String name) {
        try (Connection conn = DerbyBase.getConnection()) {
            init(conn, name);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    final public void init(Connection conn, String name) {
        try {
            this.name = name != null ? name : "";
            try {
                strokeColor = Color.web(UserConfig.getString(conn, name + "StrokeColor", DefaultStrokeColor));
            } catch (Exception e) {
                strokeColor = Color.web(DefaultStrokeColor);
            }
            try {
                fillColor = Color.web(UserConfig.getString(conn, name + "FillColor", "0xFFFFFFFF"));
            } catch (Exception e) {
                fillColor = Color.TRANSPARENT;
            }
            try {
                anchorColor = Color.web(UserConfig.getString(conn, name + "AnchorColor", DefaultAnchorColor));
            } catch (Exception e) {
                anchorColor = Color.web(DefaultAnchorColor);
            }
            strokeWidth = UserConfig.getFloat(conn, name + "StrokeWidth", 10);
            if (strokeWidth < 0) {
                strokeWidth = 10f;
            }
            strokeOpacity = UserConfig.getFloat(conn, name + "StrokeOpacity", 1);
            if (strokeOpacity < 0) {
                strokeOpacity = 1;
            }
            fillOpacity = UserConfig.getFloat(conn, name + "FillOpacity", 1f);
            if (fillOpacity < 0) {
                fillOpacity = 1f;
            }
            isFillColor = UserConfig.getBoolean(conn, name + "IsFillColor", false);
            isStrokeDash = UserConfig.getBoolean(conn, name + "IsStrokeDash", false);
            anchorSize = UserConfig.getFloat(conn, name + "AnchorSize", 10);
            if (anchorSize < 0) {
                anchorSize = 10;
            }
            String text = UserConfig.getString(conn, name + "StrokeDash", null);
            strokeDash = text2StrokeDash(text);
            text = UserConfig.getString(conn, name + "StrokeLineCap", "BUTT");
            strokeLineCap = strokeLineCap(text);
            text = UserConfig.getString(conn, name + "StrokeLineJoin", "MITER");
            strokeLineJoin = strokeLineJoin(text);
            strokeLineLimit = UserConfig.getFloat(conn, name + "StrokeLineLimit", 10f);
            dashOffset = UserConfig.getFloat(conn, name + "DashOffset", 0f);
            more = null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public ShapeStyle save() {
        try (Connection conn = DerbyBase.getConnection()) {
            UserConfig.setString(conn, name + "StrokeColor", strokeColor != null ? strokeColor.toString() : null);
            UserConfig.setString(conn, name + "AnchorColor", anchorColor != null ? anchorColor.toString() : null);
            UserConfig.setBoolean(conn, name + "IsFillColor", isFillColor);
            UserConfig.setString(conn, name + "FillColor", fillColor != null ? fillColor.toString() : null);
            UserConfig.setFloat(conn, name + "StrokeWidth", strokeWidth);
            UserConfig.setFloat(conn, name + "AnchorSize", anchorSize);
            UserConfig.setFloat(conn, name + "StrokeOpacity", strokeOpacity);
            UserConfig.setFloat(conn, name + "FillOpacity", fillOpacity);
            UserConfig.setBoolean(conn, name + "IsStrokeDash", isStrokeDash);
            UserConfig.setString(conn, name + "StrokeDash", strokeDash2Text(strokeDash));
            UserConfig.setFloat(conn, name + "DashOffset", dashOffset);
            UserConfig.setString(conn, name + "StrokeLineCap",
                    strokeLineCap != null ? strokeLineCap.name() : null);
            UserConfig.setFloat(conn, name + "StrokeLineLimit", strokeLineLimit);
            UserConfig.setString(conn, name + "StrokeLineJoin",
                    strokeLineJoin != null ? strokeLineJoin.name() : null);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return this;
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

    public static StrokeLineCap strokeLineCap(String text) {
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

    public static int strokeLineCapAwt(StrokeLineCap v) {
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

    public static StrokeLineJoin strokeLineJoin(String text) {
        try {
            if (text == null || text.isBlank()) {
                return StrokeLineJoin.MITER;
            }
            if ("ROUND".equalsIgnoreCase(text)) {
                return StrokeLineJoin.ROUND;
            } else if ("BEVEL".equalsIgnoreCase(text)) {
                return StrokeLineJoin.BEVEL;
            } else {
                return StrokeLineJoin.MITER;
            }
        } catch (Exception e) {
            return StrokeLineJoin.MITER;
        }
    }

    public static int strokeLineJoinAwt(StrokeLineJoin v) {
        try {
            if (null == v) {
                return java.awt.BasicStroke.JOIN_MITER;
            } else {
                switch (v) {
                    case ROUND:
                        return java.awt.BasicStroke.JOIN_ROUND;
                    case BEVEL:
                        return java.awt.BasicStroke.JOIN_BEVEL;
                    default:
                        return java.awt.BasicStroke.JOIN_MITER;
                }
            }
        } catch (Exception e) {
            return java.awt.BasicStroke.JOIN_MITER;
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
        return this;
    }

    public ShapeStyle setAnchorColor(Color anchorColor) {
        this.anchorColor = anchorColor;
        return this;
    }

    public ShapeStyle setIsFillColor(boolean isFillColor) {
        this.isFillColor = isFillColor;
        return this;
    }

    public ShapeStyle setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public ShapeStyle setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public ShapeStyle setAnchorSize(float anchorSize) {
        this.anchorSize = anchorSize;
        return this;
    }

    public ShapeStyle setStrokeOpacity(float strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
        return this;
    }

    public ShapeStyle setFillOpacity(float fillOpacity) {
        this.fillOpacity = fillOpacity;
        return this;
    }

    public ShapeStyle setIsStrokeDash(boolean isStrokeDash) {
        this.isStrokeDash = isStrokeDash;
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
        return this;
    }

    public ShapeStyle setStrokeDashText(String text) {
        setStrokeDash(text2StrokeDash(text));
        return this;
    }

    public ShapeStyle setDashOffset(float dashOffset) {
        this.dashOffset = dashOffset;
        return this;
    }

    public ShapeStyle setStrokeLineCap(StrokeLineCap strokeLineCap) {
        this.strokeLineCap = strokeLineCap;
        return this;
    }

    public ShapeStyle setStrokeLineLimit(float strokeLineLimit) {
        this.strokeLineLimit = strokeLineLimit;
        return this;
    }

    public ShapeStyle setStrokeLineJoin(StrokeLineJoin strokeLineJoin) {
        this.strokeLineJoin = strokeLineJoin;
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

    public float getDashOffset() {
        return dashOffset;
    }

    public StrokeLineCap getStrokeLineCap() {
        return strokeLineCap;
    }

    public int getStrokeLineCapAwt() {
        return strokeLineCapAwt(strokeLineCap);
    }

    public String getStrokeLineCapText() {
        return strokeLineCap != null ? strokeLineCap.name() : null;
    }

    public float getStrokeLineLimit() {
        return strokeLineLimit;
    }

    public StrokeLineJoin getStrokeLineJoin() {
        return strokeLineJoin;
    }

    public int getStrokeLineJoinAwt() {
        return strokeLineJoinAwt(strokeLineJoin);
    }

    public String getMore() {
        return more;
    }

}
