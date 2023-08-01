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

    private final List<List<Line>> lines;

    public DoubleLines() {
        lines = new ArrayList<>();
    }

    @Override
    public Path2D.Double getShape() {
        return new Path2D.Double();
    }

    public boolean addLine(List<Line> line) {
        if (line == null) {
            return false;
        }
        lines.add(line);
        return true;
    }

    public boolean removeLastLine() {
        if (lines.isEmpty()) {
            return false;
        }
        lines.remove(lines.size() - 1);
        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public DoubleLines cloneValues() {
        DoubleLines np = new DoubleLines();
        for (List<Line> line : lines) {
            List<Line> newline = new ArrayList<>();
            newline.addAll(line);
            np.addLine(newline);
        }
        return np;
    }

    @Override
    public boolean contains(double x, double y) {
        if (!isValid()) {
            return false;
        }
        Point2D point = new Point2D(x, y);
        for (List<Line> line : lines) {
            for (Line item : line) {
                if (item.contains(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DoubleRectangle getBound() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (List<Line> line : lines) {
            for (Line item : line) {
                double x1 = item.getStartX();
                double y1 = item.getStartY();
                double x2 = item.getEndX();
                double y2 = item.getEndY();
                if (x1 < minX) {
                    minX = x1;
                }
                if (x1 > maxX) {
                    maxX = x1;
                }
                if (y1 < minY) {
                    minY = y1;
                }
                if (y1 > maxY) {
                    maxY = y1;
                }
                if (x2 < minX) {
                    minX = x2;
                }
                if (x2 > maxX) {
                    maxX = x2;
                }
                if (y2 < minY) {
                    minY = y2;
                }
                if (y2 > maxY) {
                    maxY = y2;
                }
            }
        }
        return new DoubleRectangle(minX, minY, maxX, maxY);
    }

    public void clear() {
        lines.clear();
    }

    @Override
    public DoubleLines translateRel(double offset) {
        return translateRel(offset, offset);
    }

    @Override
    public DoubleLines translateRel(double offsetX, double offsetY) {
        DoubleLines np = new DoubleLines();
        for (List<Line> line : lines) {
            List<Line> newline = new ArrayList<>();
            for (Line item : line) {
                newline.add(new Line(
                        item.getStartX() + offsetX,
                        item.getStartY() + offsetY,
                        item.getEndX() + offsetX,
                        item.getEndY() + offsetY));
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
        return lines.size();
    }

    public List<List<Line>> getLines() {
        return lines;
    }

    public List<List<DoublePoint>> getPoints() {
        List<List<DoublePoint>> points = new ArrayList<>();
        for (List<Line> line : lines) {
            List<DoublePoint> list = new ArrayList<>();
            for (Line item : line) {
                if (list.isEmpty()) {
                    list.add(new DoublePoint(item.getStartX(), item.getStartY()));
                }
                list.add(new DoublePoint(item.getEndX(), item.getEndY()));
            }
            points.add(list);
        }
        return points;
    }

    public void setPoints(List<List<DoublePoint>> points) {
        lines.clear();
        if (points == null) {
            return;
        }
        for (List<DoublePoint> linePoints : points) {
            List<Line> line = new ArrayList<>();
            for (int i = 1; i < linePoints.size(); i++) {
                DoublePoint lastPoint = linePoints.get(i - 1);
                DoublePoint thisPoint = linePoints.get(i);
                line.add(new Line(lastPoint.getX(), lastPoint.getY(),
                        thisPoint.getX(), thisPoint.getY()));
            }
            lines.add(line);
        }
    }

    public List<Line> getList() {
        List<Line> list = new ArrayList<>();
        for (List<Line> line : lines) {
            for (Line item : line) {
                list.add(item);
            }
        }
        return list;
    }

}
