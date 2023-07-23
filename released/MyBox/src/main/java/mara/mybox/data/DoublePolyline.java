package mara.mybox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Bounds;
import javafx.scene.shape.Polyline;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @License Apache License Version 2.0
 */
public class DoublePolyline implements DoubleShape {

    private final List<DoublePoint> points;

    public DoublePolyline() {
        points = new ArrayList<>();
    }

    public boolean add(double x, double y) {
        return add(new DoublePoint(x, y));
    }

    public boolean add(DoublePoint p) {
        if (p == null) {
            return false;
        }
        points.add(p);
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
        points.clear();
        return addAll(ps);
    }

    public boolean remove(double x, double y) {
        if (points == null || points.isEmpty()) {
            return false;
        }
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            if (p.getX() == x && p.getY() == y) {
                remove(i);
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
        return remove(points.size() - 1);
    }

    @Override
    public boolean isValid() {
        return points != null && points.size() > 1;
    }

    @Override
    public DoublePolyline cloneValues() {
        DoublePolyline np = new DoublePolyline();
        np.addAll(points);
        return np;
    }

    @Override
    public boolean contains(double x, double y) {
        if (!isValid()) {
            return false;
        }
        for (DoublePoint p : points) {
            if (p.getX() == x && p.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public boolean same(DoublePolyline polyline) {
        if (polyline == null) {
            return false;
        }
        if (points == null || points.isEmpty()) {
            return polyline.getPoints() == null || polyline.getPoints().isEmpty();
        } else {
            if (polyline.getPoints() == null || points.size() != polyline.getPoints().size()) {
                return false;
            }
        }
        List<DoublePoint> bPoints = polyline.getPoints();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint point = points.get(i);
            if (!point.same(bPoints.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DoubleRectangle getBound() {
        if (points == null || points.isEmpty()) {
            return null;
        }
        Polyline polyline = new Polyline();
        for (DoublePoint p : points) {
            polyline.getPoints().add(p.getX());
            polyline.getPoints().add(p.getY());
        }
        Bounds bound = polyline.getBoundsInLocal();
        return new DoubleRectangle(bound.getMinX(), bound.getMinY(), bound.getMaxX(), bound.getMaxY());
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
    public DoublePoint getCenter() {
        DoubleRectangle bound = getBound();
        return bound != null ? bound.getCenter() : null;
    }

    @Override
    public DoublePolyline move(double offset) {
        return move(offset, offset);
    }

    @Override
    public DoublePolyline move(double offsetX, double offsetY) {
        List<DoublePoint> moved = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            moved.add(p.move(offsetX, offsetY));
        }
        DoublePolyline np = new DoublePolyline();
        np.addAll(moved);
        return np;
    }

    @Override
    public DoublePolyline moveTo(double x, double y) {
        DoubleShape moved = DoubleShape.moveTo(this, x, y);
        return moved != null ? (DoublePolyline) moved : null;
    }

    public DoublePoint get(int i) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return points.get(i);
    }

}
