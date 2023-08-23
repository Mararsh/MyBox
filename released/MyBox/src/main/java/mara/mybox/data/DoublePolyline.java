package mara.mybox.data;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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

    @Override
    public String name() {
        return message("Polyline");
    }

    @Override
    public Shape getShape() {
        Path2D.Double path = new Path2D.Double();
        DoublePoint p = points.get(0);
        path.moveTo(p.getX(), p.getY());
        for (int i = 1; i < points.size(); i++) {
            p = points.get(i);
            path.lineTo(p.getX(), p.getY());
        }
        return path;
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
        return addAll(DoublePoint.parseImageCoordinates(values));
    }

    public boolean setAll(List<DoublePoint> ps) {
        points.clear();
        return addAll(ps);
    }

    public boolean set(int index, DoublePoint p) {
        if (p == null || index < 0 || index >= points.size()) {
            return false;
        }
        points.set(index, p);
        return true;
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
        if (points == null || i < 0 || i >= points.size()) {
            return false;
        }
        points.remove(i);
        return true;
    }

    public boolean removeLast() {
        if (points == null || points.isEmpty()) {
            return false;
        }
        return remove(points.size() - 1);
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
    public DoublePolyline copy() {
        DoublePolyline np = new DoublePolyline();
        np.addAll(points);
        return np;
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
    public boolean translateRel(double offsetX, double offsetY) {
        List<DoublePoint> moved = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            moved.add(p.translate(offsetX, offsetY));
        }
        points.clear();
        points.addAll(moved);
        return true;
    }

    @Override
    public boolean scale(double scaleX, double scaleY) {
        List<DoublePoint> scaled = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            DoublePoint p = points.get(i);
            scaled.add(p.scale(scaleX, scaleY));
        }
        points.clear();
        points.addAll(scaled);
        return true;
    }

    @Override
    public String pathAbs() {
        String path = "";
        DoublePoint p = points.get(0);
        path += "M " + imageScale(p.getX()) + "," + imageScale(p.getY()) + "\n";
        for (int i = 1; i < points.size(); i++) {
            p = points.get(i);
            path += "L " + imageScale(p.getX()) + "," + imageScale(p.getY()) + "\n";
        }
        return path;
    }

    @Override
    public String pathRel() {
        String path = "";
        DoublePoint p = points.get(0);
        path += "m " + imageScale(p.getX()) + "," + imageScale(p.getY()) + "\n";
        double lastx = p.getX();
        double lasty = p.getY();
        for (int i = 1; i < points.size(); i++) {
            p = points.get(i);
            path += "l " + imageScale(p.getX() - lastx) + "," + imageScale(p.getY() - lasty) + "\n";
            lastx = p.getX();
            lasty = p.getY();
        }
        return path;
    }

    @Override
    public String elementAbs() {
        return "<polygon points=\""
                + DoublePoint.toText(points, UserConfig.imageScale(), " ") + "\"> ";
    }

    @Override
    public String elementRel() {
        return elementAbs();
    }

    public DoublePoint get(int i) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return points.get(i);
    }

}
