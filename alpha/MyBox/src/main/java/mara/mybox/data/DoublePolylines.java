package mara.mybox.data;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;

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
    public String name() {
        return message("Polylines");
    }

    @Override
    public Path2D.Double getShape() {
        if (points == null) {
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
        if (line == null || index < 0 || index >= points.size()) {
            return false;
        }
        points.set(index, line);
        return true;
    }

    public boolean removeLine(int index) {
        if (points == null || index < 0 || index >= points.size()) {
            return false;
        }
        points.remove(index);
        return true;
    }

    @Override
    public boolean isValid() {
        return points != null;
    }

    @Override
    public boolean isEmpty() {
        return !isValid() || points.isEmpty();
    }

    @Override
    public DoublePolylines copy() {
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

    public void clear() {
        points.clear();
    }

    @Override
    public boolean translateRel(double offsetX, double offsetY) {
        List<List<DoublePoint>> npoints = new ArrayList<>();
        for (List<DoublePoint> line : points) {
            List<DoublePoint> newline = new ArrayList<>();
            for (DoublePoint p : line) {
                newline.add(new DoublePoint(p.getX() + offsetX, p.getY() + offsetY));
            }
            npoints.add(newline);
        }
        points.clear();
        points.addAll(npoints);
        return true;
    }

    public void translateLineRel(int index, double offsetX, double offsetY) {
        if (index < 0 || index >= points.size()) {
            return;
        }
        List<DoublePoint> newline = new ArrayList<>();
        List<DoublePoint> line = points.get(index);
        for (int i = 0; i < line.size(); i++) {
            DoublePoint p = line.get(i);
            newline.add(p.translate(offsetX, offsetY));
        }
        points.set(index, newline);
    }

    @Override
    public boolean scale(double scaleX, double scaleY) {
        List<List<DoublePoint>> npoints = new ArrayList<>();
        for (List<DoublePoint> line : points) {
            List<DoublePoint> newline = new ArrayList<>();
            for (DoublePoint p : line) {
                newline.add(p.scale(scaleX, scaleY));
            }
            npoints.add(newline);
        }
        points.clear();
        points.addAll(npoints);
        return true;
    }

    public void scale(int index, double scaleX, double scaleY) {
        if (index < 0 || index >= points.size()) {
            return;
        }
        List<DoublePoint> newline = new ArrayList<>();
        List<DoublePoint> line = points.get(index);
        for (int i = 0; i < line.size(); i++) {
            DoublePoint p = line.get(i);
            newline.add(p.scale(scaleX, scaleY));
        }
        points.set(index, newline);
    }

    @Override
    public String svgAbs() {
        String path = "";
        for (List<DoublePoint> line : points) {
            DoublePoint p = line.get(0);
            path += "M " + imageScale(p.getX()) + "," + imageScale(p.getY()) + "\n";
            for (int i = 1; i < line.size(); i++) {
                p = line.get(i);
                path += "L " + imageScale(p.getX()) + "," + imageScale(p.getY()) + "\n";
            }
        }
        return path;
    }

    @Override
    public String svgRel() {
        String path = "";
        double lastx = 0, lasty = 0;
        for (List<DoublePoint> line : points) {
            DoublePoint p = line.get(0);
            path += "m " + imageScale(p.getX() - lastx) + "," + imageScale(p.getY() - lasty) + "\n";
            lastx = p.getX();
            lasty = p.getY();
            for (int i = 1; i < line.size(); i++) {
                p = line.get(i);
                path += "l " + imageScale(p.getX() - lastx) + "," + imageScale(p.getY() - lasty) + "\n";
                lastx = p.getX();
                lasty = p.getY();
            }
        }
        return path;
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
