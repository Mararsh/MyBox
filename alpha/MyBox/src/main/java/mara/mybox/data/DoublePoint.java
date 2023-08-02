package mara.mybox.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.tools.DoubleTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:02
 * @License Apache License Version 2.0
 */
public class DoublePoint {

    public final static String Separator = "\\s+|\\,";
    private double x, y;

    public DoublePoint() {
        x = Double.NaN;
        y = Double.NaN;
    }

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DoublePoint(Point2D p) {
        this.x = p.getX();
        this.y = p.getY();
    }

    public boolean same(DoublePoint p) {
        if (p == null || !p.valid()) {
            return !this.valid();
        } else if (!this.valid()) {
            return false;
        } else {
            return x == p.getX() && y == p.getY();
        }
    }

    public boolean valid() {
        return !DoubleTools.invalidDouble(x) && !DoubleTools.invalidDouble(y);
    }

    public DoublePoint move(double offsetX, double offsetY) {
        return new DoublePoint(x + offsetX, y + offsetY);
    }

    public String text(int scale) {
        return DoubleTools.scale(x, scale) + "," + DoubleTools.scale(y, scale);
    }

    /*
        static
     */
    public static DoublePoint create() {
        return new DoublePoint();
    }

    public static double distanceSquare(double x1, double y1, double x2, double y2) {
        double distanceX = x1 - x2;
        double distanceY = y1 - y2;
        return distanceX * distanceX + distanceY * distanceY;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(DoublePoint.distanceSquare(x1, y1, x2, y2));
    }

    public static double distanceSquare(DoublePoint A, DoublePoint B) {
        double distanceX = A.getX() - B.getX();
        double distanceY = A.getY() - B.getY();
        return distanceX * distanceX + distanceY * distanceY;
    }

    public static double distance(DoublePoint A, DoublePoint B) {
        return Math.sqrt(distanceSquare(A, B));
    }

    public static int compare(String string1, String string2, String separator, boolean desc) {
        return compare(parse(string1, separator), parse(string2, separator), desc);
    }

    public static int compare(DoublePoint p1, DoublePoint p2, boolean desc) {
        try {
            if (p1 == null || !p1.valid()) {
                if (p2 == null || !p2.valid()) {
                    return 0;
                } else {
                    return desc ? 1 : -1;
                }
            } else {
                if (p2 == null || !p2.valid()) {
                    return desc ? -1 : 1;
                } else {
                    int p1c = DoubleTools.compare(p1.x, p2.x, desc);
                    if (p1c == 0) {
                        return DoubleTools.compare(p1.y, p2.y, desc);
                    } else {
                        return p1c;
                    }
                }
            }
        } catch (Exception e) {
            return 1;
        }
    }

    public static List<DoublePoint> parseList(String string) {
        return DoublePoint.parseList(string, DoublePoint.Separator);
    }

    public static List<DoublePoint> parseList(String string, String separator) {
        try {
            if (string == null || string.isBlank()) {
                return null;
            }
            String[] vs = string.split(separator);
            if (vs == null || vs.length < 2) {
                return null;
            }
            List<DoublePoint> list = new ArrayList<>();
            for (int i = 0; i < vs.length - 1; i += 2) {
                list.add(new DoublePoint(Double.parseDouble(vs[i]), Double.parseDouble(vs[i + 1])));
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    public static DoublePoint parse(String string, String separator) {
        try {
            if (string == null || string.isBlank()) {
                return null;
            }
            String[] vs = string.split(separator);
            if (vs == null || vs.length < 2) {
                return null;
            }
            return new DoublePoint(Double.parseDouble(vs[0]), Double.parseDouble(vs[1]));
        } catch (Exception e) {
            return null;
        }
    }

    public static String toText(List<DoublePoint> points, int scale, String separator) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        String s = null;
        for (DoublePoint p : points) {
            if (s != null) {
                s += separator;
            } else {
                s = "";
            }
            s += DoubleTools.scale(p.getX(), scale) + ","
                    + DoubleTools.scale(p.getY(), scale);
        }
        return s;
    }

    public static DoublePoint scale(DoublePoint p, int scale) {
        try {
            if (p == null || scale < 0) {
                return p;
            }
            return new DoublePoint(DoubleTools.scale(p.getX(), scale), DoubleTools.scale(p.getY(), scale));
        } catch (Exception e) {
            return p;
        }
    }

    public static List<DoublePoint> scaleList(List<DoublePoint> points, int scale) {
        if (points == null || points.isEmpty()) {
            return points;
        }
        List<DoublePoint> scaled = new ArrayList<>();
        for (DoublePoint p : points) {
            scaled.add(scale(p, scale));
        }
        return scaled;
    }

    public static List<List<DoublePoint>> scaleLists(List<List<DoublePoint>> list, int scale) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        List<List<DoublePoint>> scaled = new ArrayList<>();
        for (List<DoublePoint> points : list) {
            scaled.add(scaleList(points, scale));
        }
        return scaled;
    }

    /*
        get/set
     */
    public double getX() {
        return x;
    }

    public DoublePoint setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public DoublePoint setY(double y) {
        this.y = y;
        return this;
    }

}
