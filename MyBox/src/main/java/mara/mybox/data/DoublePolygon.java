package mara.mybox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Bounds;
import javafx.scene.shape.Polygon;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoublePolygon implements DoubleShape {

    private List<DoublePoint> points;
    private Polygon polygon;

    public DoublePolygon() {
        points = new ArrayList<>();
        polygon = new Polygon();
    }

    public boolean add(double x, double y) {
        if (x < 0 || y < 0) {
            return false;
        }
        points.add(new DoublePoint(x, y));
        getPolygon();
        return true;
    }

    public boolean addAll(List<DoublePoint> ps) {
        if (ps == null) {
            return false;
        }
        points.addAll(ps);
        getPolygon();
        return true;
    }

    public boolean setAll(List<DoublePoint> ps) {
        if (ps == null) {
            return false;
        }
        points.clear();
        points.addAll(ps);
        getPolygon();
        return true;
    }

    public boolean remove(double x, double y) {
        if (x < 0 || y < 0 || points == null || points.isEmpty()) {
            return false;
        }
        List<Double> d = new ArrayList();
        for (int i = 0; i < points.size(); i++) {
            DoublePoint p = points.get(i);
            if (p.getX() == x && p.getY() == y) {
                points.remove(i);
                break;
            }
        }
        getPolygon();
        return true;
    }

    public boolean remove(int i) {
        if (i < 0 || points == null || points.isEmpty()) {
            return false;
        }
        points.remove(i);
        getPolygon();
        return true;
    }

    public boolean removeLast() {
        if (remove(points.size() - 1)) {
            getPolygon();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return points != null && points.size() > 2;
    }

    @Override
    public DoublePolygon cloneValues() {
        DoublePolygon np = new DoublePolygon();
        np.addAll(points);
        return np;
    }

    @Override
    public boolean include(double x, double y) {
        return isValid() && polygon.contains(x, y);
    }

    @Override
    public DoubleRectangle getBound() {
        Bounds bound = polygon.getBoundsInLocal();
        return new DoubleRectangle(bound.getMinX(), bound.getMinY(), bound.getMaxX(), bound.getMaxY());
    }

    public Polygon getPolygon() {
        polygon = new Polygon();
        polygon.getPoints().addAll(getData());
        return polygon;
    }

    public void clear() {
        points.clear();
        polygon = new Polygon();
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
        List<Double> d = new ArrayList();
        for (int i = 0; i < points.size(); i++) {
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
        for (int i = 0; i < points.size(); i++) {
            x[i] = (int) Math.round(points.get(i).getX());
            y[i] = (int) Math.round(points.get(i).getY());
        }
        xy.put("x", x);
        xy.put("y", y);
        return xy;
    }

    @Override
    public DoublePolygon move(double offset) {
        DoublePolygon np = new DoublePolygon();
        for (int i = 0; i < points.size(); i++) {
            DoublePoint p = points.get(i);
            np.add(p.getX() + offset, p.getY() + offset);
        }
        return np;
    }

    @Override
    public DoublePolygon move(double offsetX, double offsetY) {
        DoublePolygon np = new DoublePolygon();
        for (int i = 0; i < points.size(); i++) {
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

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

}
