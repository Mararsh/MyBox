package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.data.DoublePathSegment.PathSegmentType;
import org.apache.batik.parser.PathHandler;
import org.apache.batik.parser.PathParser;

/**
 * @Author Mara
 * @CreateDate 2023-7-30
 * @License Apache License Version 2.0
 */
public class DoublePathParser implements PathHandler {

    protected double currentX, currentY; // The point between previous segment and current segment
    protected double xCenter, yCenter; // for smooth curve

    protected int index, scale;
    protected List<DoublePathSegment> segments;

    public DoublePathParser parse(BaseController controller, String content, int scale) {
        try {
            segments = null;
            this.scale = scale;
            currentX = 0;
            currentY = 0;
            xCenter = 0;
            yCenter = 0;

            if (content == null || content.isBlank()) {
                return null;
            }
            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(this);
            pathParser.parse(content);

            return this;
        } catch (Exception e) {
            controller.displayError(e.toString());
            return null;
        }
    }

    public List<DoublePathSegment> getSegments() {
        return segments;
    }

    @Override
    public void startPath() {
        segments = new ArrayList<>();
        index = 0;
    }

    @Override
    public void endPath() {
    }

    @Override
    public void closePath() {
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Close)
                .setIsAbsolute(true)
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setEndPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
    }

    @Override
    public void movetoRel(float x, float y) {
        xCenter = currentX + x;
        yCenter = currentY + y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Move)
                .setIsAbsolute(false)
                .setScale(scale)
                .setEndPoint(new DoublePoint(xCenter, yCenter))
                .setEndPointRel(new DoublePoint(x, y))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
        currentY += y;
    }

    @Override
    public void movetoAbs(float x, float y) {
        xCenter = x;
        yCenter = y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Move)
                .setIsAbsolute(true)
                .setScale(scale)
                .setEndPoint(new DoublePoint(x, y))
                .setEndPointRel(new DoublePoint(x - currentX, y - currentY))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
        currentY = y;
    }

    @Override
    public void linetoRel(float x, float y) {
        xCenter = currentX + x;
        yCenter = currentY + y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Line)
                .setIsAbsolute(false)
                .setScale(scale)
                .setEndPoint(new DoublePoint(xCenter, yCenter))
                .setEndPointRel(new DoublePoint(x, y))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
        currentY += y;
    }

    @Override
    public void linetoAbs(float x, float y) {
        xCenter = x;
        yCenter = y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Line)
                .setIsAbsolute(true)
                .setScale(scale)
                .setEndPoint(new DoublePoint(x, y))
                .setEndPointRel(new DoublePoint(x - currentX, y - currentY))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
        currentY = y;
    }

    @Override
    public void linetoHorizontalRel(float x) {
        xCenter = currentX + x;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.LineHorizontal)
                .setIsAbsolute(false)
                .setScale(scale)
                .setValue(xCenter)
                .setValueRel(x)
                .setEndPoint(new DoublePoint(xCenter, currentY))
                .setEndPointRel(new DoublePoint(x, 0))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
    }

    @Override
    public void linetoHorizontalAbs(float x) {
        xCenter = x;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.LineVertical)
                .setIsAbsolute(true)
                .setScale(scale)
                .setValue(x)
                .setEndPoint(new DoublePoint(x, currentY))
                .setEndPointRel(new DoublePoint(x - currentX, 0))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
    }

    @Override
    public void linetoVerticalRel(float y) {
        yCenter = currentY + y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.LineVertical)
                .setIsAbsolute(false)
                .setScale(scale)
                .setValue(yCenter)
                .setValueRel(y)
                .setEndPoint(new DoublePoint(currentX, yCenter))
                .setEndPointRel(new DoublePoint(0, y))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentY += y;
    }

    @Override
    public void linetoVerticalAbs(float y) {
        yCenter = y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.LineVertical)
                .setIsAbsolute(true)
                .setScale(scale)
                .setValue(y)
                .setEndPoint(new DoublePoint(currentX, y))
                .setEndPointRel(new DoublePoint(0, y - currentY))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentY = y;
    }

    @Override
    public void curvetoCubicRel(float x1, float y1,
            float x2, float y2,
            float x, float y) {
        xCenter = currentX + x2;
        yCenter = currentY + y2;
        DoublePoint p = new DoublePoint(x, y);
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Cubic)
                .setIsAbsolute(false)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(currentX + x1, currentY + y1))
                .setControlPoint1Rel(new DoublePoint(x1, y1))
                .setControlPoint2(new DoublePoint(xCenter, yCenter))
                .setControlPoint2Rel(new DoublePoint(x2, y2))
                .setEndPoint(new DoublePoint(currentX + x, currentY + y))
                .setEndPointRel(p)
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
        currentY += y;
    }

    @Override
    public void curvetoCubicAbs(float x1, float y1,
            float x2, float y2,
            float x, float y) {
        xCenter = x2;
        yCenter = y2;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Cubic)
                .setIsAbsolute(true)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(x1, y1))
                .setControlPoint1Rel(new DoublePoint(x1 - currentX, y1 - currentY))
                .setControlPoint2(new DoublePoint(x2, y2))
                .setControlPoint2Rel(new DoublePoint(x2 - currentX, y2 - currentY))
                .setEndPoint(new DoublePoint(x, y))
                .setEndPointRel(new DoublePoint(x - currentX, y - currentY))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
        currentY = y;
    }

    // refer to "org.apache.batik.parser.curvetoCubicSmoothRel"
    @Override
    public void curvetoCubicSmoothRel(float x2, float y2,
            float x, float y) {
        xCenter = currentX + x2;
        yCenter = currentY + y2;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.CubicSmooth)
                .setIsAbsolute(false)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(currentX * 2 - xCenter, currentY * 2 - yCenter))
                .setControlPoint1Rel(new DoublePoint(currentX - xCenter, currentY - yCenter))
                .setControlPoint2(new DoublePoint(xCenter, yCenter))
                .setControlPoint2Rel(new DoublePoint(x2, y2))
                .setEndPoint(new DoublePoint(currentX + x, currentY + y))
                .setEndPointRel(new DoublePoint(x, y))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
        currentY += y;
    }

    // refer to "org.apache.batik.parser.curvetoCubicSmoothAbs"
    @Override
    public void curvetoCubicSmoothAbs(float x2, float y2,
            float x, float y) {
        xCenter = x2;
        yCenter = y2;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.CubicSmooth)
                .setIsAbsolute(true)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(currentX * 2 - xCenter, currentY * 2 - yCenter))
                .setControlPoint1Rel(new DoublePoint(currentX - xCenter, currentY - yCenter))
                .setControlPoint2(new DoublePoint(x2, y2))
                .setControlPoint2Rel(new DoublePoint(x2 - currentX, y2 - currentY))
                .setEndPoint(new DoublePoint(x, y))
                .setEndPointRel(new DoublePoint(x - currentX, y - currentY))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
        currentY = y;
    }

    @Override
    public void curvetoQuadraticRel(float x1, float y1,
            float x, float y) {
        xCenter = currentX + x1;
        yCenter = currentY + y1;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Quadratic)
                .setIsAbsolute(false)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(xCenter, yCenter))
                .setControlPoint1Rel(new DoublePoint(x1, y1))
                .setEndPoint(new DoublePoint(currentX + x, currentY + y))
                .setEndPointRel(new DoublePoint(x, y))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
        currentY += y;
    }

    @Override
    public void curvetoQuadraticAbs(float x1, float y1,
            float x, float y) {
        xCenter = x1;
        yCenter = y1;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Quadratic)
                .setIsAbsolute(true)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(x1, y1))
                .setControlPoint1Rel(new DoublePoint(x1 - currentX, y1 - currentY))
                .setEndPoint(new DoublePoint(x, y))
                .setEndPointRel(new DoublePoint(x - currentX, y - currentY))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
        currentY = y;
    }

    // refer to "org.apache.batik.parser.curvetoQuadraticSmoothRel"
    @Override
    public void curvetoQuadraticSmoothRel(float x, float y) {
        xCenter = currentX * 2 - xCenter;
        yCenter = currentY * 2 - yCenter;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.QuadraticSmooth)
                .setIsAbsolute(false)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(xCenter, yCenter))
                .setControlPoint1Rel(new DoublePoint(xCenter - currentX, yCenter - currentY))
                .setEndPoint(new DoublePoint(currentX + x, currentY + y))
                .setEndPointRel(new DoublePoint(x, y))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
        currentY += y;
    }

    // refer to "org.apache.batik.parser.curvetoQuadraticSmoothAbs"
    @Override
    public void curvetoQuadraticSmoothAbs(float x, float y) {
        xCenter = currentX * 2 - xCenter;
        yCenter = currentY * 2 - yCenter;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.QuadraticSmooth)
                .setIsAbsolute(true)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(xCenter, yCenter))
                .setControlPoint1Rel(new DoublePoint(xCenter - currentX, yCenter - currentY))
                .setEndPoint(new DoublePoint(x, y))
                .setEndPointRel(new DoublePoint(x - currentX, y - currentY))
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
        currentY = y;
    }

    @Override
    public void arcRel(float rx, float ry,
            float xAxisRotation,
            boolean largeArcFlag, boolean sweepFlag,
            float x, float y) {
        xCenter = currentX + x;
        yCenter = currentY + y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Arc)
                .setIsAbsolute(false)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(rx, ry))
                .setEndPoint(new DoublePoint(xCenter, yCenter))
                .setEndPointRel(new DoublePoint(x, y))
                .setValue(xAxisRotation)
                .setFlag1(largeArcFlag)
                .setFlag2(sweepFlag)
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX += x;
        currentY += y;
    }

    @Override
    public void arcAbs(float rx, float ry,
            float xAxisRotation,
            boolean largeArcFlag, boolean sweepFlag,
            float x, float y) {
        xCenter = x;
        yCenter = y;
        DoublePathSegment segment = new DoublePathSegment()
                .setType(PathSegmentType.Arc)
                .setIsAbsolute(true)
                .setScale(scale)
                .setControlPoint1(new DoublePoint(rx, ry))
                .setEndPoint(new DoublePoint(x, y))
                .setEndPointRel(new DoublePoint(x - currentX, y - currentY))
                .setValue(xAxisRotation)
                .setFlag1(largeArcFlag)
                .setFlag2(sweepFlag)
                .setStartPoint(new DoublePoint(currentX, currentY))
                .setIndex(index++);
        segments.add(segment);
        currentX = x;
        currentY = y;
    }

}
