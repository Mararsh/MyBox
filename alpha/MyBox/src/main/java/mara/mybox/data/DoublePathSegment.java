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
    protected DoublePoint startPoint, controlPoint1, controlPoint2, endPoint, arcRadius; // absoulte coordinate
    protected DoublePoint controlPoint1Rel, controlPoint2Rel, endPointRel; // relative coordinate
    protected double value, valueRel;
    protected boolean isAbsolute, flag1, flag2;
    protected int index, scale;

    public static enum PathSegmentType {
        Move, Line, Quadratic, Cubic, Arc, Close,
        LineHorizontal, LineVertical, QuadraticSmooth, CubicSmooth
    }

    public DoublePathSegment() {
        index = -1;
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

    public String getCommand() {
        try {
            if (type == null) {
                return null;
            }
            if (isAbsolute) {
                return absCommand();
            } else {
                return relCommand();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String getParameters() {
        try {
            if (type == null) {
                return null;
            }
            if (isAbsolute) {
                return absParameters();
            } else {
                return relParameters();
            }
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
            if (isAbsolute) {
                return abs();
            } else {
                return rel();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String abs() {
        try {
            if (type == null) {
                return null;
            }
            return absCommand() + " " + absParameters();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String rel() {
        try {
            if (type == null) {
                return null;
            }
            return relCommand() + " " + relParameters();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String absCommand() {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                    return "M ";
                case Line:
                    return "L ";
                case LineHorizontal:
                    return "H ";
                case LineVertical:
                    return "V ";
                case Quadratic:
                    return "Q ";
                case QuadraticSmooth:
                    return "T ";
                case Cubic:
                    return "C ";
                case CubicSmooth:
                    return "S ";
                case Arc:
                    return "A ";
                case Close:
                    return "Z ";
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String relCommand() {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                    return "m ";
                case Line:
                    return "l ";
                case LineHorizontal:
                    return "h ";
                case LineVertical:
                    return "v ";
                case Quadratic:
                    return "q ";
                case QuadraticSmooth:
                    return "t ";
                case Cubic:
                    return "c ";
                case CubicSmooth:
                    return "s ";
                case Arc:
                    return "a ";
                case Close:
                    return "z ";
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String absParameters() {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                case Line:
                case QuadraticSmooth:
                    return endPoint.text(scale);
                case LineHorizontal:
                case LineVertical:
                    return DoubleTools.scaleString(value, scale);
                case Quadratic:
                    return controlPoint1.text(scale) + " " + endPoint.text(scale);
                case Cubic:
                    return controlPoint1.text(scale)
                            + " " + controlPoint2.text(scale)
                            + " " + endPoint.text(scale);
                case CubicSmooth:
                    return controlPoint2.text(scale) + " " + endPoint.text(scale);
                case Arc:
                    return arcRadius.text(scale)
                            + " " + DoubleTools.scaleString(value, scale)
                            + " " + (flag1 ? 1 : 0)
                            + " " + (flag2 ? 1 : 0)
                            + " " + endPoint.text(scale);
                case Close:
                    return "";
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String relParameters() {
        try {
            if (type == null) {
                return null;
            }
            switch (type) {
                case Move:
                case Line:
                case QuadraticSmooth:
                    return endPointRel.text(scale);
                case LineHorizontal:
                case LineVertical:
                    return DoubleTools.scaleString(valueRel, scale);
                case Quadratic:
                    return controlPoint1Rel.text(scale) + " " + endPointRel.text(scale);
                case Cubic:
                    return controlPoint1Rel.text(scale)
                            + " " + controlPoint2Rel.text(scale)
                            + " " + endPointRel.text(scale);
                case CubicSmooth:
                    return controlPoint2Rel.text(scale) + " " + endPointRel.text(scale);
                case Arc:
                    return arcRadius.text(scale)
                            + " " + DoubleTools.scaleString(value, scale)
                            + " " + (flag1 ? 1 : 0)
                            + " " + (flag2 ? 1 : 0)
                            + " " + endPointRel.text(scale);
                case Close:
                    return "";
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DoublePathSegment copyTo() {
        try {
            DoublePathSegment seg = new DoublePathSegment()
                    .setType(type).setIsAbsolute(isAbsolute)
                    .setValue(value).setValueRel(valueRel)
                    .setFlag1(flag1).setFlag2(flag2)
                    .setScale(scale).setIndex(index);
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
            if (arcRadius != null) {
                seg.setArcRadius(arcRadius.copy());
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

    public DoublePathSegment translate(double offsetX, double offsetY) {
        try {
            if (type == null) {
                return this;
            }
            DoublePathSegment seg = copyTo();
            if (startPoint != null) {
                seg.setStartPoint(startPoint.translate(offsetX, offsetY));
            }
            if (controlPoint1 != null) {
                seg.setControlPoint1(controlPoint1.translate(offsetX, offsetY));
            }
            if (controlPoint2 != null) {
                seg.setControlPoint2(controlPoint2.translate(offsetX, offsetY));
            }
            if (endPoint != null) {
                seg.setEndPoint(endPoint.translate(offsetX, offsetY));
            }
            if (type == PathSegmentType.LineHorizontal) {
                seg.setValue(value + offsetX);
            }
            if (type == PathSegmentType.LineVertical) {
                seg.setValue(value + offsetY);
            }
            return seg;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DoublePathSegment scale(double scaleX, double scaleY) {
        try {
            if (type == null) {
                return this;
            }
            DoublePathSegment seg = copyTo();
            if (startPoint != null) {
                seg.setStartPoint(startPoint.scale(scaleX, scaleY));
            }
            if (controlPoint1 != null) {
                seg.setControlPoint1(controlPoint1.scale(scaleX, scaleY));
            }
            if (controlPoint2 != null) {
                seg.setControlPoint2(controlPoint2.scale(scaleX, scaleY));
            }
            if (endPoint != null) {
                seg.setEndPoint(endPoint.scale(scaleX, scaleY));
            }
            if (arcRadius != null) {
                seg.setArcRadius(arcRadius.scale(scaleX, scaleY));
            }
            if (controlPoint1Rel != null) {
                seg.setControlPoint1Rel(controlPoint1Rel.scale(scaleX, scaleY));
            }
            if (controlPoint2Rel != null) {
                seg.setControlPoint2Rel(controlPoint2Rel.scale(scaleX, scaleY));
            }
            if (endPointRel != null) {
                seg.setEndPointRel(endPointRel.scale(scaleX, scaleY));
            }
            if (type == PathSegmentType.LineHorizontal) {
                seg.setValue(value * scaleX);
                seg.setValueRel(valueRel * scaleX);
            }
            if (type == PathSegmentType.LineVertical) {
                seg.setValue(value * scaleY);
                seg.setValueRel(valueRel * scaleY);
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
                            segment.arcRadius.getX(), segment.arcRadius.getY(),
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

    public DoublePathSegment setArcRadius(DoublePoint arcRadius) {
        this.arcRadius = arcRadius;
        return this;
    }

    public DoublePathSegment setValueRel(double valueRel) {
        this.valueRel = valueRel;
        return this;
    }

    public DoublePathSegment setIndex(int index) {
        this.index = index;
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

    public DoublePoint getArcRadius() {
        return arcRadius;
    }

    public double getValueRel() {
        return valueRel;
    }

    public int getIndex() {
        return index;
    }

}
