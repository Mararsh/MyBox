package mara.mybox.data;

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
    protected DoublePoint startPoint, controlPoint1, controlPoint2, endPoint; // absoulte
    protected DoublePoint controlPoint1Rel, controlPoint2Rel, endPointRel; // relative
    protected double value, valueRel;
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
                case Line:
                case QuadraticSmooth:
                    return isAbsolute ? endPoint.text(scale) : endPointRel.text(scale);
                case LineHorizontal:
                case LineVertical:
                    return isAbsolute ? DoubleTools.scaleString(value, scale) : DoubleTools.scaleString(valueRel, scale);
                case Quadratic:
                    if (isAbsolute) {
                        return controlPoint1.text(scale) + " " + endPoint.text(scale);
                    } else {
                        return controlPoint1Rel.text(scale) + " " + endPointRel.text(scale);
                    }
                case Cubic:
                    if (isAbsolute) {
                        return controlPoint1.text(scale)
                                + " " + controlPoint2.text(scale)
                                + " " + endPoint.text(scale);
                    } else {
                        return controlPoint1Rel.text(scale)
                                + " " + controlPoint2Rel.text(scale)
                                + " " + endPointRel.text(scale);
                    }
                case CubicSmooth:
                    if (isAbsolute) {
                        return controlPoint2.text(scale) + " " + endPoint.text(scale);
                    } else {
                        return controlPoint2Rel.text(scale) + " " + endPointRel.text(scale);
                    }
                case Arc:
                    return controlPoint1.text(scale)
                            + " " + DoubleTools.scaleString(value, scale)
                            + " " + (flag1 ? 1 : 0)
                            + " " + (flag2 ? 1 : 0)
                            + " " + (isAbsolute ? endPoint.text(scale) : endPointRel.text(scale));
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
                    return isAbsolute ? "C " : "c ";
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

    public DoublePathSegment copy() {
        try {
            DoublePathSegment seg = new DoublePathSegment()
                    .setType(type).setIsAbsolute(isAbsolute)
                    .setFlag1(flag1).setFlag2(flag2)
                    .setScale(scale);
            if (startPoint != null) {
                seg.setStartPoint(startPoint.copy());
            }
            if (controlPoint1 != null) {
                seg.setControlPoint1(controlPoint1.copy());
            }
            if (controlPoint2 != null) {
                seg.setControlPoint2(controlPoint2.copy());
            }
            if (endPoint != null) {
                seg.setEndPoint(endPoint.copy());
            }
            if (controlPoint1Rel != null) {
                seg.setControlPoint1Rel(controlPoint1Rel.copy());
            }
            if (controlPoint2Rel != null) {
                seg.setControlPoint2Rel(controlPoint2Rel.copy());
            }
            if (endPointRel != null) {
                seg.setEndPointRel(endPointRel.copy());
            }
            return seg;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
    public static PathElement pathElement(DoublePathSegment segment) {
        try {
            if (segment.getType() == null) {
                return null;
            }
            switch (segment.getType()) {
                case Move:
                    return new MoveTo(segment.endPoint.getX(), segment.endPoint.getY());
                case Line:
                    return new LineTo(segment.endPoint.getX(), segment.endPoint.getY());
                case LineHorizontal:
                    return new HLineTo(segment.value);
                case LineVertical:
                    return new VLineTo(segment.value);
                case Quadratic:
                case QuadraticSmooth:
                    return new QuadCurveTo(
                            segment.controlPoint1.getX(), segment.controlPoint1.getY(),
                            segment.endPoint.getX(), segment.endPoint.getY());
                case Cubic:
                case CubicSmooth:
                    return new CubicCurveTo(
                            segment.controlPoint1.getX(), segment.controlPoint1.getY(),
                            segment.controlPoint2.getX(), segment.controlPoint2.getY(),
                            segment.endPoint.getX(), segment.endPoint.getY());
                case Arc:
                    return new ArcTo(
                            segment.controlPoint1.getX(), segment.controlPoint1.getY(),
                            segment.value,
                            segment.endPoint.getX(), segment.endPoint.getY(),
                            segment.flag1, segment.flag2);
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

    public DoublePathSegment setStartPoint(DoublePoint startPoint) {
        this.startPoint = startPoint;
        return this;
    }

    public DoublePathSegment setControlPoint1(DoublePoint controlPoint1) {
        this.controlPoint1 = controlPoint1;
        return this;
    }

    public DoublePathSegment setControlPoint2(DoublePoint controlPoint2) {
        this.controlPoint2 = controlPoint2;
        return this;
    }

    public DoublePathSegment setEndPoint(DoublePoint endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public DoublePathSegment setControlPoint1Rel(DoublePoint controlPoint1Rel) {
        this.controlPoint1Rel = controlPoint1Rel;
        return this;
    }

    public DoublePathSegment setControlPoint2Rel(DoublePoint controlPoint2Rel) {
        this.controlPoint2Rel = controlPoint2Rel;
        return this;
    }

    public DoublePathSegment setEndPointRel(DoublePoint endPointRel) {
        this.endPointRel = endPointRel;
        return this;
    }

    public DoublePathSegment setValueRel(double valueRel) {
        this.valueRel = valueRel;
        return this;
    }

    /*
        get
     */
    public PathSegmentType getType() {
        return type;
    }

    public DoublePoint getStartPoint() {
        return startPoint;
    }

    public DoublePoint getControlPoint1() {
        return controlPoint1;
    }

    public DoublePoint getControlPoint2() {
        return controlPoint2;
    }

    public DoublePoint getEndPoint() {
        return endPoint;
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

    public DoublePoint getControlPoint1Rel() {
        return controlPoint1Rel;
    }

    public DoublePoint getControlPoint2Rel() {
        return controlPoint2Rel;
    }

    public DoublePoint getEndPointRel() {
        return endPointRel;
    }

    public double getValueRel() {
        return valueRel;
    }

}
