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
public class DoublePolylines implements DoubleShape {

    private List<List<DoublePoint>> points;

    public DoublePolylines() {
        points = new ArrayList<>();
    }

    @Override
    public Path2D.Double getShape() {
        if (points == null || points.isEmpty()) {
            return null;
        }
        Path2D.Double path = new Path2D.Double();
        for (List<DoublePoint> line : points) {
            DoublePoint p = line.get(0);
            path.moveTo(p.getX(), p.getY());
            for (int i = 1; i < line.size(); i++) {
                p = line.get(i);
                path.lineTo(p.getX(), p.getY());
            }
        }
        return path;
    }

    public boolean addLine(List<DoublePoint> line) {
        if (line == null) {
            return false;
        }
        points.add(line);
        return true;
    }

    public boolean setLine(int index, List<DoublePoint> line) {
        if (line == null) {
            return false;
        }
        points.set(index, line);
        return true;
    }

    @Override
    public boolean isValid() {
        return points != null;
    }

    @Override
    public DoublePolylines cloneValues() {
        DoublePolylines np = new DoublePolylines();
        for (List<DoublePoint> line : points) {
            List<DoublePoint> newline = new ArrayList<>();
            newline.addAll(line);
            np.addLine(newline);
        }
        return np;
    }

    public List<Line> getLineList() {
        List<Line> dlines = new ArrayList<>();
        int lastx, lasty = -1, thisx, thisy;
        for (List<DoublePoint> lineData : points) {
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

    public boolean contains(double x, double y) {
        if (!isValid()) {
            return false;
        }
        Point2D point = new Point2D(x, y);
        for (Line line : getLineList()) {
            if (line.contains(point)) {
                return true;
            }
        }
        return false;
    }

    public DoubleRectangle getBound() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (List<DoublePoint> lineData : points) {
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
        points.clear();
    }

    @Override
    public DoublePolylines translateRel(double offsetX, double offsetY) {
        DoublePolylines np = new DoublePolylines();
        for (List<DoublePoint> line : points) {
            List<DoublePoint> newline = new ArrayList<>();
            for (DoublePoint p : line) {
                newline.add(new DoublePoint(p.getX() + offsetX, p.getY() + offsetY));
            }
            np.addLine(newline);
        }
        return np;
    }

    @Override
    public DoublePolylines translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoublePolylines) moved : null;
    }

    public DoublePoint getCenter() {
        DoubleRectangle bound = getBound();
        return DoubleShape.getCenter(bound);
    }

    public int getLinesSize() {
        return points.size();
    }

    public List<List<DoublePoint>> getLines() {
        return points;
    }

    public void setLines(List<List<DoublePoint>> linePoints) {
        this.points = linePoints;
    }

    public boolean removeLastLine() {
        if (points.isEmpty()) {
            return false;
        }
        points.remove(points.size() - 1);
        return true;
    }

}
