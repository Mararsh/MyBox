package mara.mybox.data;

import java.util.List;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.VLineTo;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-30
 * @License Apache License Version 2.0
 */
public class DoublePathSegment {

    protected PathSegmentType type;
    protected DoublePoint interPoint; // The point between previous segment and current segment
    protected List<DoublePoint> points;
    protected double value;
    protected boolean isAbsolute, flag1, flag2;
    protected int scale;

    public static enum PathSegmentType {
        Move, Line, Quadratic, Cubic, Arc, Close,
        LineHorizontal, LineVertical, QuadraticSmooth, CubicSmooth
    }

    public DoublePathSegment() {
        scale = 3;
    }

    public String getTypeName() {
        if (type == null) {
            return null;
        }
        switch (type) {
            case Move:
                return message("Move");
            case Line:
                return message("StraightLine");
            case LineHorizontal:
                return message("LineHorizontal");
            case LineVertical:
                return message("LineVertical");
            case Quadratic:
                return message("QuadraticCurve");
            case QuadraticSmooth:
                return message("QuadraticSmooth");
            case Cubic:
                return message("CubicCurve");
            case CubicSmooth:
                return message("CubicSmooth");
            case Arc:
                return message("ArcCurve");
            case Close:
                return message("Close");
        }
        return null;
    }

    public String getParameters() {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                    return points.get(0).text(scale);
                case Line:
                    return points.get(0).text(scale);
                case LineHorizontal:
                    return DoubleTools.scaleString(value, scale);
                case LineVertical:
                    return DoubleTools.scaleString(value, scale);
                case Quadratic:
                    return points.get(0).text(scale) + " " + points.get(1).text(scale);
                case QuadraticSmooth:
                    return points.get(0).text(scale);
                case Cubic:
                    return points.get(0).text(scale)
                            + " " + points.get(1).text(scale)
                            + " " + points.get(2).text(scale);
                case CubicSmooth:
                    return points.get(0).text(scale) + " " + points.get(1).text(scale);
                case Arc:
                    return points.get(0).text(scale)
                            + " " + DoubleTools.scaleString(value, scale)
                            + " " + (flag1 ? 1 : 0)
                            + " " + (flag2 ? 1 : 0)
                            + " " + points.get(1).text(scale);
                case Close:
                    return "";
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String getCommand() {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                    return isAbsolute ? "M " : "m ";
                case Line:
                    return isAbsolute ? "L " : "l ";
                case LineHorizontal:
                    return isAbsolute ? "H " : "h ";
                case LineVertical:
                    return isAbsolute ? "V " : "v ";
                case Quadratic:
                    return isAbsolute ? "Q " : "q ";
                case QuadraticSmooth:
                    return isAbsolute ? "T " : "t ";
                case Cubic:
                    return isAbsolute ? "Q " : "q ";
                case CubicSmooth:
                    return isAbsolute ? "S " : "s ";
                case Arc:
                    return isAbsolute ? "A " : "a ";
                case Close:
                    return isAbsolute ? "Z " : "z ";
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String text() {
        try {
            if (type == null) {
                return null;
            }
            return getCommand() + " " + getParameters();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public PathElement pathElement() {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                    return new MoveTo(points.get(0).getX(), points.get(0).getY());
                case Line:
                    return new LineTo(points.get(0).getX(), points.get(0).getY());
                case LineHorizontal:
                    return new HLineTo(value);
                case LineVertical:
                    return new VLineTo(value);
                case Quadratic:
                    return new QuadCurveTo(points.get(0).getX(), points.get(0).getY(),
                            points.get(1).getX(), points.get(1).getY());
                case QuadraticSmooth:
                    return new QuadCurveTo(points.get(0).getX(), points.get(0).getY(),
                            points.get(0).getX(), points.get(0).getY());
                case Cubic:
                    return new CubicCurveTo(points.get(0).getX(), points.get(0).getY(),
                            points.get(1).getX(), points.get(1).getY(),
                            points.get(2).getX(), points.get(2).getY());
                case CubicSmooth:
                    return new CubicCurveTo(points.get(0).getX(), points.get(0).getY(),
                            points.get(0).getX(), points.get(0).getY(),
                            points.get(1).getX(), points.get(1).getY());
                case Arc:
                    return new ArcTo(points.get(0).getX(), points.get(0).getY(),
                            value,
                            points.get(1).getX(), points.get(1).getY(),
                            flag1, flag2);
                case Close:
                    return new ClosePath();
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        set
     */
    public DoublePathSegment setType(PathSegmentType type) {
        this.type = type;
        return this;
    }

    public DoublePathSegment setPoints(List<DoublePoint> points) {
        this.points = points;
        return this;
    }

    public DoublePathSegment setIsAbsolute(boolean isAbsolute) {
        this.isAbsolute = isAbsolute;
        return this;
    }

    public DoublePathSegment setValue(double value) {
        this.value = value;
        return this;
    }

    public DoublePathSegment setFlag1(boolean flag1) {
        this.flag1 = flag1;
        return this;
    }

    public DoublePathSegment setFlag2(boolean flag2) {
        this.flag2 = flag2;
        return this;
    }

    public DoublePathSegment setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public DoublePathSegment setInterPoint(DoublePoint interPoint) {
        this.interPoint = interPoint;
        return this;
    }

    /*
        get
     */
    public PathSegmentType getType() {
        return type;
    }

    public List<DoublePoint> getPoints() {
        return points;
    }

    public boolean isIsAbsolute() {
        return isAbsolute;
    }

    public double getValue() {
        return value;
    }

    public boolean isFlag1() {
        return flag1;
    }

    public boolean isFlag2() {
        return flag2;
    }

    public int getScale() {
        return scale;
    }

    public DoublePoint getInterPoint() {
        return interPoint;
    }

}
