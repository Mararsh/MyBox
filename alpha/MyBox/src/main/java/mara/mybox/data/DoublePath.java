package mara.mybox.data;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.DoublePathSegment.PathSegmentType;
import mara.mybox.dev.MyBoxLog;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;

/**
 * @Author Mara
 * @CreateDate 2023-7-12
 * @License Apache License Version 2.0
 */
public class DoublePath extends DoubleRectangle {
    
    protected String content;
    protected DoublePoint startPoint;
    protected List<DoublePathSegment> segments;
    
    public DoublePath() {
        content = null;
    }
    
    public DoublePath(String content) {
        this.content = content;
        startPoint = null;
        segments = null;
        if (content == null || content.isBlank()) {
            return;
        }
        try {
            AWTPathProducer pathProducer = new AWTPathProducer();
            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(pathProducer);
            pathParser.parse(content);
            
            ExtendedGeneralPath exPath = (ExtendedGeneralPath) pathProducer.getShape();
            startPoint = new DoublePoint(exPath.getCurrentPoint());
            
            ExtendedPathIterator exPathIterator = exPath.getExtendedPathIterator();
            float[] values = new float[6];
            segments = new ArrayList<>();
            List<DoublePoint> points;
            while (!exPathIterator.isDone()) {
                try {
                    int type = exPathIterator.currentSegment(values);
                    DoublePathSegment segment = new DoublePathSegment().setIsAbsolute(true);
                    switch (type) {
                        case PathIterator.SEG_MOVETO:
                            points = new ArrayList<>();
                            points.add(new DoublePoint(values[0], values[1]));
                            segment.setType(PathSegmentType.Move).setPoints(points);
                            break;
                        case PathIterator.SEG_LINETO:
                            points = new ArrayList<>();
                            points.add(new DoublePoint(values[0], values[1]));
                            segment.setType(PathSegmentType.Line).setPoints(points);
                            break;
                        case PathIterator.SEG_QUADTO:
                            points = new ArrayList<>();
                            points.add(new DoublePoint(values[0], values[1]));
                            points.add(new DoublePoint(values[2], values[3]));
                            segment.setType(PathSegmentType.Quadratic).setPoints(points);
                            break;
                        case PathIterator.SEG_CUBICTO:
                            points = new ArrayList<>();
                            points.add(new DoublePoint(values[0], values[1]));
                            points.add(new DoublePoint(values[2], values[3]));
                            points.add(new DoublePoint(values[4], values[5]));
                            segment.setType(PathSegmentType.Cubic).setPoints(points);
                            break;
                        case PathIterator.SEG_CLOSE:
                            segment.setType(PathSegmentType.Close).setPoints(null);
                            break;
                        default:
                            segment = null;
                            break;
                    }
                    if (segment != null) {
                        segments.add(segment);
                    }
                    exPathIterator.next();
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }
    
    public String typesetting(String separator, int scale) {
        return segmentsToPath(segments, separator, scale);
    }

    /*
        static
     */
    public static String segmentsToPath(List<DoublePathSegment> segments, String separator, int scale) {
        try {
            if (segments == null || segments.isEmpty()) {
                return null;
            }
            String path = null;
            for (DoublePathSegment seg : segments) {
                if (path != null) {
                    path += separator + seg.text(scale);
                } else {
                    path = seg.text(scale);
                }
            }
            return path;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }
    
    public static String typesetting(String content, String separator, int scale) {
        try {
            DoublePath path = new DoublePath(content);
            return segmentsToPath(path.getSegments(), separator, scale);
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
    
    public void setStartPoint(DoublePoint startPoint) {
        this.startPoint = startPoint;
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
