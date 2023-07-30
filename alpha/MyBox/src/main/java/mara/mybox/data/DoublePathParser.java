package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import org.apache.batik.parser.PathHandler;
import org.apache.batik.parser.PathParser;

/**
 * @Author Mara
 * @CreateDate 2023-7-30
 * @License Apache License Version 2.0
 */
public class DoublePathParser implements PathHandler {

    protected int scale;
    protected List<DoublePathSegment> segments;

    public DoublePathParser parse(String content, int scale) {
        try {
            segments = null;
            this.scale = scale;
            if (content == null || content.isBlank()) {
                return null;
            }

            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(this);
            pathParser.parse(content);

            return this;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<DoublePathSegment> getSegments() {
        return segments;
    }

    @Override
    public void startPath() {
        segments = new ArrayList<>();
    }

    @Override
    public void endPath() {
    }

    @Override
    public void closePath() {
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Close);
        segments.add(segment);
    }

    @Override
    public void movetoRel(float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Move)
                .setIsAbsolute(false)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void movetoAbs(float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Move)
                .setIsAbsolute(true)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void linetoRel(float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Line)
                .setIsAbsolute(false)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void linetoAbs(float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Line)
                .setIsAbsolute(true)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void linetoHorizontalRel(float x) {
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.LineHorizontal)
                .setIsAbsolute(false)
                .setScale(scale)
                .setValue(x);
        segments.add(segment);
    }

    @Override
    public void linetoHorizontalAbs(float x) {
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.LineVertical)
                .setIsAbsolute(true)
                .setScale(scale)
                .setValue(x);
        segments.add(segment);
    }

    @Override
    public void linetoVerticalRel(float y) {
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.LineVertical)
                .setIsAbsolute(false)
                .setScale(scale)
                .setValue(y);
        segments.add(segment);
    }

    @Override
    public void linetoVerticalAbs(float y) {
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.LineVertical)
                .setIsAbsolute(true)
                .setScale(scale)
                .setValue(y);
        segments.add(segment);
    }

    @Override
    public void curvetoCubicRel(float x1, float y1,
            float x2, float y2,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x1, y1));
        points.add(new DoublePoint(x2, y2));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Cubic)
                .setIsAbsolute(false)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void curvetoCubicAbs(float x1, float y1,
            float x2, float y2,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x1, y1));
        points.add(new DoublePoint(x2, y2));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Cubic)
                .setIsAbsolute(true)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void curvetoCubicSmoothRel(float x2, float y2,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x2, y2));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.CubicSmooth)
                .setIsAbsolute(false)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void curvetoCubicSmoothAbs(float x2, float y2,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x2, y2));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.CubicSmooth)
                .setIsAbsolute(true)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void curvetoQuadraticRel(float x1, float y1,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x1, y1));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Quadratic)
                .setIsAbsolute(false)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void curvetoQuadraticAbs(float x1, float y1,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x1, y1));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Quadratic)
                .setIsAbsolute(true)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void curvetoQuadraticSmoothRel(float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.QuadraticSmooth)
                .setIsAbsolute(false)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void curvetoQuadraticSmoothAbs(float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.QuadraticSmooth)
                .setIsAbsolute(true)
                .setScale(scale)
                .setPoints(points);
        segments.add(segment);
    }

    @Override
    public void arcRel(float rx, float ry,
            float xAxisRotation,
            boolean largeArcFlag, boolean sweepFlag,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(rx, ry));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Arc)
                .setIsAbsolute(false)
                .setScale(scale)
                .setPoints(points)
                .setValue(xAxisRotation)
                .setFlag1(largeArcFlag)
                .setFlag2(sweepFlag);
        segments.add(segment);
    }

    @Override
    public void arcAbs(float rx, float ry,
            float xAxisRotation,
            boolean largeArcFlag, boolean sweepFlag,
            float x, float y) {
        List<DoublePoint> points = new ArrayList<>();
        points.add(new DoublePoint(rx, ry));
        points.add(new DoublePoint(x, y));
        DoublePathSegment segment = new DoublePathSegment()
                .setType(DoublePathSegment.PathSegmentType.Arc)
                .setIsAbsolute(true)
                .setScale(scale)
                .setPoints(points)
                .setValue(xAxisRotation)
                .setFlag1(largeArcFlag)
                .setFlag2(sweepFlag);
        segments.add(segment);
    }
}
