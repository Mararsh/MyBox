package mara.mybox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoublePolygon implements DoubleShape {

    private List<DoublePoint> points;

    public DoublePolygon() {
        points = new ArrayList<>();
    }

    @Override
    public java.awt.Polygon getShape() {
        java.awt.Polygon polygon = new java.awt.Polygon();
        for (DoublePoint p : points) {
            polygon.addPoint((int) p.getX(), (int) p.getY());
        }
        return polygon;
    }

    public boolean add(double x, double y) {
        points.add(new DoublePoint(x, y));
        return true;
    }

    public boolean addAll(List<DoublePoint> ps) {
        if (ps == null) {
            return false;
        }
        points.addAll(ps);
        return true;
    }

    public boolean addAll(String values) {
        return addAll(DoublePoint.parseList(values));
    }

    public boolean setAll(List<DoublePoint> ps) {
        if (ps == null) {
            return false;
        }
        points.clear();
        return addAll(ps);
    }

    public boolean remove(double x, double y) {
        if (points == null || points.isEmpty()) {
            return false;
        }
        List<Double> d = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            if (p.getX() == x && p.getY() == y) {
                points.remove(i);
                break;
            }
        }
        return true;
    }

    public boolean remove(int i) {
        if (i < 0 || points == null || points.isEmpty()) {
            return false;
        }
        points.remove(i);
        return true;
    }

    public boolean removeLast() {
        if (remove(points.size() - 1)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return points != null && points.size() > 2;
    }

    public boolean same(DoublePolygon polygon) {
        if (polygon == null) {
            return false;
        }
        if (points == null || points.isEmpty()) {
            return polygon.getPoints() == null || polygon.getPoints().isEmpty();
        } else {
            if (polygon.getPoints() == null || points.size() != polygon.getPoints().size()) {
                return false;
            }
        }
        List<DoublePoint> bPoints = polygon.getPoints();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint point = points.get(i);
            if (!point.same(bPoints.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DoublePolygon cloneValues() {
        DoublePolygon np = new DoublePolygon();
        np.addAll(points);
        return np;
    }

    public void clear() {
        points.clear();
    }

    public List<DoublePoint> getPoints() {
        return points;
    }

    public int getSize() {
        if (points == null) {
            return 0;
        }
        return points.size();
    }

    public List<Double> getData() {
        List<Double> d = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            d.add(points.get(i).getX());
            d.add(points.get(i).getY());
        }
        return d;
    }

    public Map<String, int[]> getIntXY() {
        Map<String, int[]> xy = new HashMap<>();
        if (points == null || points.isEmpty()) {
            return xy;
        }
        int[] x = new int[points.size()];
        int[] y = new int[points.size()];
        for (int i = 0; i < points.size(); ++i) {
            x[i] = (int) Math.round(points.get(i).getX());
            y[i] = (int) Math.round(points.get(i).getY());
        }
        xy.put("x", x);
        xy.put("y", y);
        return xy;
    }

    @Override
    public DoublePolygon translateRel(double offsetX, double offsetY) {
        DoublePolygon np = new DoublePolygon();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            np.add(p.getX() + offsetX, p.getY() + offsetY);
        }
        return np;
    }

    @Override
    public DoublePolygon translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoublePolygon) moved : null;
    }

    public DoublePoint get(int i) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return points.get(i);
    }

    public void setPoints(List<DoublePoint> points) {
        this.points = points;
    }

}
