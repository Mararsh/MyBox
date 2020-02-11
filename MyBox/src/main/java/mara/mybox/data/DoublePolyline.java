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
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoublePolyline implements DoubleShape {

    private List<DoublePoint> points;
    private Polyline polyline;

    public DoublePolyline() {
        points = new ArrayList<>();
        polyline = new Polyline();
    }

    public boolean add(double x, double y) {
        if (x < 0 || y < 0) {
            return false;
        }
        return add(new DoublePoint(x, y));
    }

    public boolean add(DoublePoint p) {
        if (p == null) {
            return false;
        }
        points.add(p);
        getPolyline();
        return true;
    }

    public boolean addAll(List<DoublePoint> ps) {
        if (ps == null) {
            return false;
        }
        points.addAll(ps);
        getPolyline();
        return true;
    }

    public boolean setAll(List<DoublePoint> ps) {
        if (ps == null) {
            return false;
        }
        points.clear();
        points.addAll(ps);
        getPolyline();
        return true;
    }

    public boolean remove(double x, double y) {
        if (x < 0 || y < 0 || points == null || points.isEmpty()) {
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
        getPolyline();
        return true;
    }

    public boolean remove(int i) {
        if (i < 0 || points == null || points.isEmpty()) {
            return false;
        }
        points.remove(i);
        getPolyline();
        return true;
    }

    public boolean removeLast() {
        if (remove(points.size() - 1)) {
            getPolyline();
            return true;
        } else {
            return false;
        }
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
    public boolean include(double x, double y) {
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
        Bounds bound = polyline.getBoundsInLocal();
        return new DoubleRectangle(bound.getMinX(), bound.getMinY(), bound.getMaxX(), bound.getMaxY());
    }

    public Polyline getPolyline() {
        polyline = new Polyline();
        polyline.getPoints().addAll(getData());
        return polyline;
    }

    public void clear() {
        points.clear();
        polyline = new Polyline();
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
    public DoublePolyline move(double offset) {
        DoublePolyline np = new DoublePolyline();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            np.add(p.getX() + offset, p.getY() + offset);
        }
        return np;
    }

    @Override
    public DoublePolyline move(double offsetX, double offsetY) {
        DoublePolyline np = new DoublePolyline();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            np.add(p.getX() + offsetX, p.getY() + offsetY);
        }
        return np;
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

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

}
