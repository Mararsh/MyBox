package mara.mybox.data;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @License Apache License Version 2.0
 */
public class DoubleLines implements DoubleShape {

    private List<List<DoublePoint>> linePoints;
    private List<DoublePoint> currentLine;

    public DoubleLines() {
        linePoints = new ArrayList<>();
    }

    @Override
    public Path2D.Double getShape() {
        return new Path2D.Double();
    }

    public boolean addPoint(DoublePoint p) {
        if (p == null) {
            return false;
        }
        if (currentLine == null) {
            currentLine = new ArrayList<>();
            linePoints.add(currentLine);
        }
        currentLine.add(p);
        return true;
    }

    public boolean endLine(DoublePoint p) {
        addPoint(p);
        currentLine = null;
        return true;
    }

    public boolean addLine(List<DoublePoint> line) {
        if (line == null) {
            return false;
        }
        linePoints.add(line);
        return true;
    }

    public boolean removeLastLine() {
        if (linePoints.isEmpty()) {
            return false;
        }
        linePoints.remove(linePoints.size() - 1);
        currentLine = null;
        return true;
    }

    @Override
    public boolean isValid() {
        return linePoints != null;
    }

    @Override
    public DoubleLines cloneValues() {
        DoubleLines np = new DoubleLines();
        for (List<DoublePoint> line : linePoints) {
            List<DoublePoint> newline = new ArrayList<>();
            newline.addAll(line);
            np.addLine(newline);
        }
        return np;
    }

    public List<Line> lines() {
        List<Line> dlines = new ArrayList<>();
        int lastx, lasty = -1, thisx, thisy;
        for (List<DoublePoint> lineData : linePoints) {
            if (lineData.size() == 1) {
                DoublePoint linePoint = lineData.get(0);
                thisx = (int) Math.round(linePoint.getX());
                thisy = (int) Math.round(linePoint.getY());
                Line line = new Line(thisx, thisy, thisx, thisy);
                dlines.add(line);
            } else {
                lastx = Integer.MAX_VALUE;
                for (DoublePoint linePoint : lineData) {
                    thisx = (int) Math.round(linePoint.getX());
                    thisy = (int) Math.round(linePoint.getY());
                    if (lastx != Integer.MAX_VALUE) {
                        Line line = new Line(lastx, lasty, thisx, thisy);
                        dlines.add(line);
                    }
                    lastx = thisx;
                    lasty = thisy;
                }
            }
        }
        return dlines;
    }

    @Override
    public boolean contains(double x, double y) {
        if (!isValid()) {
            return false;
        }
        Point2D point = new Point2D(x, y);
        for (Line line : lines()) {
            if (line.contains(point)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DoubleRectangle getBound() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (List<DoublePoint> lineData : linePoints) {
            for (DoublePoint p : lineData) {
                double x = p.getX();
                double y = p.getY();
                if (x < minX) {
                    minX = x;
                }
                if (x > maxX) {
                    maxX = x;
                }
                if (y < minY) {
                    minY = y;
                }
                if (y > maxY) {
                    maxY = y;
                }
            }
        }
        return new DoubleRectangle(minX, minY, maxX, maxY);
    }

    public void clear() {
        linePoints.clear();
        currentLine = null;
    }

    @Override
    public DoubleLines translateRel(double offset) {
        return translateRel(offset, offset);
    }

    @Override
    public DoubleLines translateRel(double offsetX, double offsetY) {
        DoubleLines np = new DoubleLines();
        for (List<DoublePoint> line : linePoints) {
            List<DoublePoint> newline = new ArrayList<>();
            for (DoublePoint p : line) {
                newline.add(new DoublePoint(p.getX() + offsetX, p.getY() + offsetY));
            }
            np.addLine(newline);
        }
        return np;
    }

    @Override
    public DoubleLines translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleLines) moved : null;
    }

    @Override
    public DoublePoint getCenter() {
        DoubleRectangle bound = getBound();
        return bound.getCenter();
    }

    public int getLinesSize() {
        return linePoints.size();
    }

    public int getPointsSize() {
        int count = 0;
        for (List<DoublePoint> line : linePoints) {
            count += line.size();
        }
        return count;
    }

    public DoublePoint getPoint(int index) {
        int count = 0;
        for (List<DoublePoint> line : linePoints) {
            for (DoublePoint p : line) {
                if (index == count) {
                    return p;
                }
                count++;
            }
        }
        return null;

    }

    public List<DoublePoint> getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(List<DoublePoint> currentLine) {
        this.currentLine = currentLine;
    }

    public List<List<DoublePoint>> getLinePoints() {
        return linePoints;
    }

    public void setLinePoints(List<List<DoublePoint>> linePoints) {
        this.linePoints = linePoints;
        currentLine = null;
    }

}
