package mara.mybox.data;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.List;
import javafx.scene.shape.Path;
import mara.mybox.data.DoublePathSegment.PathSegmentType;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Close;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Cubic;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Line;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineHorizontal;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineVertical;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Move;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Quadratic;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-7-12
 * @License Apache License Version 2.0
 */
public class DoublePath implements DoubleShape {

    protected String content;
    protected List<DoublePathSegment> segments;
    protected int scale;

    public DoublePath() {
        init();
    }

    final public void init() {
        content = null;
        segments = null;
        scale = 3;
    }

    public DoublePath(String content) {
        init();
        parse(content);
    }

    public final List<DoublePathSegment> parse(String content) {
        this.content = content;

        DoublePathParser parser = new DoublePathParser().parse(content, scale);
        if (parser == null) {
            segments = null;
        } else {
            segments = parser.getSegments();
        }
        return segments;
    }

    public String typesetting(String separator) {
        return segmentsToString(segments, separator);
    }

    @Override
    public Path2D.Double getShape() {
        Path2D.Double path = new Path2D.Double();
        addToPath2D(path, segments);
        return path;
    }

    @Override
    public DoublePath cloneValues() {
        return new DoublePath(content);
    }

    @Override
    public boolean isValid() {
        return content != null && segments != null;
    }

    @Override
    public boolean isEmpty() {
        return !isValid() || segments.isEmpty();
    }

    @Override
    public boolean translateRel(double offsetX, double offsetY) {
        DoublePath nPath = new DoublePath(content);
        AffineTransform.getTranslateInstance(offsetX, offsetY);
        return true;
    }

    /*
        set
     */
    public void setContent(String content) {
        this.content = content;
        parse(content);
    }

    public void setSegments(List<DoublePathSegment> segments) {
        this.segments = segments;
        content = segmentsToString(segments, " ");
    }

    /*
        static
     */
    public static String segmentsToString(List<DoublePathSegment> segments, String separator) {
        try {
            if (segments == null || segments.isEmpty()) {
                return null;
            }
            String path = null;
            for (DoublePathSegment seg : segments) {
                if (path != null) {
                    path += separator + seg.text();
                } else {
                    path = seg.text();
                }
            }
            return path;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static boolean addToPath2D(Path2D.Double path, List<DoublePathSegment> segments) {
        try {
            if (path == null || segments == null) {
                return false;
            }
            for (DoublePathSegment seg : segments) {
                PathSegmentType type = seg.getType();
                if (type == null) {
                    continue;
                }
                switch (type) {
                    case Move:
                        path.moveTo(seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        break;
                    case Line:
                        path.lineTo(seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        break;
                    case LineHorizontal:
                        path.lineTo(seg.getValue(), seg.getStartPoint().getY());
                        break;
                    case LineVertical:
                        path.lineTo(seg.getStartPoint().getX(), seg.getValue());
                        break;
                    case Quadratic:
                        path.quadTo(seg.getControlPoint1().getX(), seg.getControlPoint1().getY(),
                                seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        break;
//                    case QuadraticSmooth:
//                        path.quadTo(seg.getInterPoint().getX(), seg.getValue());
//                        break;
                    case Cubic:
                        path.curveTo(seg.getControlPoint1().getX(), seg.getControlPoint1().getY(),
                                seg.getControlPoint2().getX(), seg.getControlPoint2().getY(),
                                seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        break;
//                    case CubicSmooth:
//                        path.quadTo(seg.getInterPoint().getX(), seg.getValue());
//                        break;
//                    case Arc:
//                        path.quadTo(seg.getInterPoint().getX(), seg.getValue());
//                        break;
                    case Close:
                        path.closePath();
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static boolean addToFxPath(Path path, List<DoublePathSegment> segments) {
        try {
            if (path == null || segments == null) {
                return false;
            }
            for (DoublePathSegment seg : segments) {
                path.getElements().add(seg.pathElement());
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static String typesetting(String content, String separator) {
        try {
            DoublePath path = new DoublePath(content);
            return segmentsToString(path.getSegments(), separator);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return content;
        }
    }

    /*
        get
     */
    public String getContent() {
        return content;
    }

    public List<DoublePathSegment> getSegments() {
        return segments;
    }

}
