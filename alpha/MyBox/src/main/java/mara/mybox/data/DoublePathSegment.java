package mara.mybox.data;

import java.util.List;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-30
 * @License Apache License Version 2.0
 */
public class DoublePathSegment {

    protected String text;
    protected PathSegmentType type;
    protected List<DoublePoint> points;
    protected boolean isAbsolute;

    public static enum PathSegmentType {
        Move, Line, Quadratic, Cubic, Arc, Close
    }

    public DoublePathSegment() {
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
            case Quadratic:
                return message("QuadraticCurve");
            case Cubic:
                return message("CubicCurve");
            case Arc:
                return message("ArcCurve");
            case Close:
                return message("Close");
        }
        return null;
    }

    public String getPointsText() {
        return DoublePoint.toText(points, 2);
    }

    public String text(int scale) {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                    return (isAbsolute ? "M " : "m ") + points.get(0).text(scale);
                case Line:
                    return (isAbsolute ? "L " : "l ") + points.get(0).text(scale);
                case Quadratic:
                    return (isAbsolute ? "Q " : "q ") + points.get(0).text(scale) + " " + points.get(1).text(scale);
                case Cubic:
                    return (isAbsolute ? "C " : "c ") + points.get(0).text(scale) + " " + points.get(1).text(scale) + " " + points.get(2).text(scale);
                case Arc:
                    return (isAbsolute ? "A " : "a ") + points.get(0).text(scale) + " " + points.get(1).text(scale) + " " + points.get(2).text(scale);
                case Close:
                    return (isAbsolute ? "Z " : "z");
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
    public DoublePathSegment setText(String text) {
        this.text = text;
        return this;
    }

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

    /*
        get
     */
    public String getText() {
        return text;
    }

    public PathSegmentType getType() {
        return type;
    }

    public List<DoublePoint> getPoints() {
        return points;
    }

    public boolean isIsAbsolute() {
        return isAbsolute;
    }

}
