package mara.mybox.data;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Path;
import mara.mybox.controller.BaseController;
import mara.mybox.data.DoublePathSegment.PathSegmentType;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Arc;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Close;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Cubic;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.CubicSmooth;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Line;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineHorizontal;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.LineVertical;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Move;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.Quadratic;
import static mara.mybox.data.DoublePathSegment.PathSegmentType.QuadraticSmooth;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPath;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

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

    public DoublePath(List<DoublePathSegment> segments) {
        init();
        parseSegments(segments);
    }

    final public void init() {
        content = "";
        segments = null;
        scale = UserConfig.imageScale();
    }

    @Override
    public String name() {
        return message("SVGPath");
    }

    public final List<DoublePathSegment> parseContent(BaseController controller, String content) {
        this.content = content;
        segments = stringToSegments(controller, content, scale);
        return segments;
    }

    public final String parseSegments(List<DoublePathSegment> segments) {
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
    public DoublePath copy() {
        DoublePath path = new DoublePath();
        path.setContent(content);
        if (segments != null) {
            List<DoublePathSegment> list = new ArrayList<>();
            for (DoublePathSegment seg : segments) {
                list.add(seg.copy());
            }
            path.setSegments(list);
        }
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
        try {
            if (segments == null) {
                return true;
            }
            for (int i = 0; i < segments.size(); i++) {
                DoublePathSegment seg = segments.get(i);
                segments.set(i, seg.translate(offsetX, offsetY));
            }
            content = segmentsToString(segments, " ");
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean scale(double scaleX, double scaleY) {
        try {
            if (segments == null) {
                return true;
            }
            for (int i = 0; i < segments.size(); i++) {
                DoublePathSegment seg = segments.get(i);
                segments.set(i, seg.scale(scaleX, scaleY));
            }
            content = segmentsToString(segments, " ");
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public String pathAbs() {
        try {
            if (segments == null || segments.isEmpty()) {
                return null;
            }
            String path = null;
            for (DoublePathSegment seg : segments) {
                if (path != null) {
                    path += "\n" + seg.abs();
                } else {
                    path = seg.abs();
                }
            }
            return path;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    @Override
    public String pathRel() {
        try {
            if (segments == null || segments.isEmpty()) {
                return null;
            }
            String path = null;
            for (DoublePathSegment seg : segments) {
                if (path != null) {
                    path += "\n" + seg.rel();
                } else {
                    path = seg.rel();
                }
            }
            return path;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    @Override
    public String elementAbs() {
        return "<path d=\"\n" + pathAbs() + "\n\"> ";
    }

    @Override
    public String elementRel() {
        return "<path d=\"\n" + pathRel() + "\n\"> ";
    }

    public DoublePoint lastPoint() {
        if (isEmpty()) {
            return null;
        }
        return segments.get(segments.size() - 1).getEndPoint();
    }

    public boolean replace(int index, DoublePathSegment seg) {
        if (segments == null || index < 0 || index >= segments.size()) {
            return false;
        }
        segments.set(index, seg);
        content = segmentsToString(segments, " ");
        return true;
    }

    public boolean insert(int index, DoublePathSegment seg) {
        if (segments == null || index < 0 || index > segments.size()) {
            return false;
        }
        segments.add(index, seg);
        content = segmentsToString(segments, " ");
        return true;
    }

    public boolean add(DoublePathSegment seg) {
        if (segments == null) {
            return false;
        }
        segments.add(seg);
        content = segmentsToString(segments, " ");
        return true;
    }

    public boolean toAbs(BaseController controller) {
        try {
            parseContent(controller, pathAbs());
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public boolean toRel(BaseController controller) {
        try {
            parseContent(controller, pathRel());
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
    }

    public void clear() {
        content = "";
        segments = null;
    }

    /*
        static
     */
    public static List<DoublePathSegment> stringToSegments(BaseController controller, String content, int scale) {
        if (content == null || content.isBlank()) {
            return null;
        }
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
                        double angle = seg.getValue();
                        Arc2D arc = ExtendedGeneralPath.computeArc(
                                seg.getStartPoint().getX(), seg.getStartPoint().getY(),
                                seg.getArcRadius().getX(), seg.getArcRadius().getY(),
                                angle,
                                seg.isFlag1(), seg.isFlag2(),
                                seg.getEndPoint().getX(), seg.getEndPoint().getY());
                        AffineTransform t = AffineTransform.getRotateInstance(
                                Math.toRadians(angle), arc.getCenterX(), arc.getCenterY());
                        Shape s = t.createTransformedShape(arc);
                        path.append(s, true);
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

    public static List<DoublePathSegment> shapeToSegments(Shape shape) {
        try {
            if (shape == null) {
                return null;
            }
            PathIterator iterator = shape.getPathIterator(null);
            if (iterator == null) {
                return null;
            }
            List<DoublePathSegment> segments = new ArrayList<>();
            double[] coords = new double[6];
            int index = 0;
            int scale = UserConfig.imageScale();
            double currentX = 0, currentY = 0;
            DoublePathSegment segment;
            while (!iterator.isDone()) {
                int type = iterator.currentSegment(coords);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        segment = new DoublePathSegment()
                                .setType(PathSegmentType.Move)
                                .setIsAbsolute(true)
                                .setScale(scale)
                                .setEndPoint(new DoublePoint(coords[0], coords[1]))
                                .setEndPointRel(new DoublePoint(coords[0] - currentX, coords[1] - currentY))
                                .setStartPoint(new DoublePoint(currentX, currentY))
                                .setIndex(index++);
                        segments.add(segment);
                        currentX = coords[0];
                        currentY = coords[1];
                        break;
                    case PathIterator.SEG_LINETO:
                        segment = new DoublePathSegment()
                                .setType(PathSegmentType.Line)
                                .setIsAbsolute(true)
                                .setScale(scale)
                                .setEndPoint(new DoublePoint(coords[0], coords[1]))
                                .setEndPointRel(new DoublePoint(coords[0] - currentX, coords[1] - currentY))
                                .setStartPoint(new DoublePoint(currentX, currentY))
                                .setIndex(index++);
                        segments.add(segment);
                        currentX = coords[0];
                        currentY = coords[1];
                        break;
                    case PathIterator.SEG_QUADTO:
                        segment = new DoublePathSegment()
                                .setType(PathSegmentType.Quadratic)
                                .setIsAbsolute(true)
                                .setScale(scale)
                                .setControlPoint1(new DoublePoint(coords[0], coords[1]))
                                .setControlPoint1Rel(new DoublePoint(coords[0] - currentX, coords[1] - currentY))
                                .setEndPoint(new DoublePoint(coords[2], coords[3]))
                                .setEndPointRel(new DoublePoint(coords[2] - currentX, coords[3] - currentY))
                                .setStartPoint(new DoublePoint(currentX, currentY))
                                .setIndex(index++);
                        segments.add(segment);
                        currentX = coords[2];
                        currentY = coords[3];
                        break;
                    case PathIterator.SEG_CUBICTO:
                        segment = new DoublePathSegment()
                                .setType(PathSegmentType.Cubic)
                                .setIsAbsolute(true)
                                .setScale(scale)
                                .setControlPoint1(new DoublePoint(coords[0], coords[1]))
                                .setControlPoint1Rel(new DoublePoint(coords[0] - currentX, coords[1] - currentY))
                                .setControlPoint2(new DoublePoint(coords[2], coords[3]))
                                .setControlPoint2Rel(new DoublePoint(coords[2] - currentX, coords[3] - currentY))
                                .setEndPoint(new DoublePoint(coords[4], coords[5]))
                                .setEndPointRel(new DoublePoint(coords[4] - currentX, coords[5] - currentY))
                                .setStartPoint(new DoublePoint(currentX, currentY))
                                .setIndex(index++);
                        segments.add(segment);
                        currentX = coords[4];
                        currentY = coords[5];
                        break;
                    case PathIterator.SEG_CLOSE:
                        segment = new DoublePathSegment()
                                .setType(PathSegmentType.Close)
                                .setIsAbsolute(true)
                                .setScale(scale)
                                .setStartPoint(new DoublePoint(currentX, currentY))
                                .setIndex(index++);
                        segments.add(segment);
                        break;
                }
                iterator.next();
            }

            return segments;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static DoublePath shapeToPathData(Shape shape) {
        try {
            List<DoublePathSegment> segments = shapeToSegments(shape);
            if (segments == null || segments.isEmpty()) {
                return null;
            }
            DoublePath shapeData = new DoublePath();
            shapeData.parseSegments(segments);
            return shapeData;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static String shapeToStringByBatik(Shape shape) {
        try {
            if (shape == null) {
                return null;
            }
            DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
            Document myFactory = impl.createDocument("http://www.w3.org/2000/svg", "svg", null);
            SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(myFactory);
            return SVGPath.toSVGPathData(shape, ctx);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static DoublePath shapeToPathDataByBatik(Shape shape) {
        try {
            String s = shapeToStringByBatik(shape);
            if (s == null) {
                return null;
            }
            DoublePath shapeData = new DoublePath();
            shapeData.parseContent(null, s);
            return shapeData;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
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

    public static DoublePath scale(DoublePath path, double scaleX, double scaleY) {
        try {
            if (path == null) {
                return null;
            }
            DoublePath scaled = path.copy();
            scaled.scale(scaleX, scaleY);
            return scaled;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePath translateRel(DoublePath path, double offsetX, double offsetY) {
        try {
            if (path == null) {
                return null;
            }
            DoublePath trans = path.copy();
            trans.translateRel(offsetX, offsetY);
            return trans;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
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
