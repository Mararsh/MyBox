package mara.mybox.data;

import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.List;
import javafx.scene.shape.Path;
import mara.mybox.controller.BaseController;
import mara.mybox.data.DoublePathSegment.PathSegmentType;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Close;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Cubic;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Line;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineHorizontal;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineVertical;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Move;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Quadratic;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SvgTools;
import static mara.mybox.value.Languages.message;

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

    public DoublePath(BaseController controller, String content) {
        init();
        parseContent(controller, content);
    }

    final public void init() {
        content = null;
        segments = null;
        scale = 3;
    }

    @Override
    public String name() {
        return message("SvgPath");
    }

    public final List<DoublePathSegment> parseContent(BaseController controller, String content) {
        this.content = content;
        segments = stringToSegments(controller, content, scale);
        return segments;
    }

    public String parseSegments(List<DoublePathSegment> segments) {
        this.segments = segments;
        content = segmentsToString(segments, " ");
        return content;
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
        DoublePath path = new DoublePath();
        path.setContent(content);
        path.setSegments(segments);
        return path;
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
//        DoublePath nPath = new DoublePath(content);
//        AffineTransform.getTranslateInstance(offsetX, offsetY);
        return true;
    }

    /*
        static
     */
    public static List<DoublePathSegment> stringToSegments(BaseController controller, String content, int scale) {
        DoublePathParser parser = new DoublePathParser().parse(controller, content, scale);
        if (parser == null) {
            return null;
        } else {
            return parser.getSegments();
        }
    }

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
                    case LineHorizontal:
                    case LineVertical:
                        path.lineTo(seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        break;
                    case Quadratic:
                    case QuadraticSmooth:
                        path.quadTo(seg.getControlPoint1().getX(), seg.getControlPoint1().getY(),
                                seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        break;
                    case Cubic:
                    case CubicSmooth:
                        path.curveTo(seg.getControlPoint1().getX(), seg.getControlPoint1().getY(),
                                seg.getControlPoint2().getX(), seg.getControlPoint2().getY(),
                                seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        break;
                    case Arc:
                        Arc2D arc = SvgTools.computeArc(
                                seg.getStartPoint().getX(), seg.getStartPoint().getY(),
                                seg.getControlPoint1().getX(), seg.getControlPoint1().getY(),
                                seg.getValue(),
                                seg.isFlag1(), seg.isFlag2(),
                                seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        path.append(arc, true);
                        break;
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
                path.getElements().add(DoublePathSegment.pathElement(seg));
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public static String typesetting(BaseController controller, String content, String separator, int scale) {
        try {
            List<DoublePathSegment> segments = stringToSegments(controller, content, scale);
            return segmentsToString(segments, separator);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return content;
        }
    }

    /*
        set
     */
    public void setContent(String content) {
        this.content = content;
    }

    public void setSegments(List<DoublePathSegment> segments) {
        this.segments = segments;
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
