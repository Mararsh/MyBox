package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleLines implements DoubleShape {

    private List<List<DoublePoint>> lines;
    private List<DoublePoint> currentLine;

    public DoubleLines() {
        lines = new ArrayList<>();
    }

    public void setCurrentLine() {
        if (currentLine == null) {
            return;
        }
        if (!lines.contains(currentLine)) {
            lines.add(currentLine);
        }
    }

    public boolean startLine(DoublePoint p) {
        if (p == null) {
            return false;
        }
        if (currentLine != null) {
            if (!lines.contains(currentLine)) {
                lines.add(currentLine);
            }
        }
        currentLine = new ArrayList<>();
        currentLine.add(p);
        lines.add(currentLine);
        return true;
    }

    public boolean endLine(DoublePoint p) {
        if (p == null) {
            return false;
        }
        if (currentLine == null) {
            currentLine = new ArrayList<>();
        }
        currentLine.add(p);
        if (!lines.contains(currentLine)) {
            lines.add(currentLine);
        }
        currentLine = null;
        return true;
    }

    public boolean addPoint(DoublePoint p) {
        if (p == null) {
            return false;
        }
        if (currentLine == null) {
            currentLine = new ArrayList<>();
        }
        currentLine.add(p);
        if (!lines.contains(currentLine)) {
            lines.add(currentLine);
        }
        return true;
    }

    public boolean addLine(List<DoublePoint> line) {
        if (line == null) {
            return false;
        }
        if (!lines.contains(line)) {
            lines.add(line);
        }
        return true;
    }

    public boolean removeLastLine() {
        if (lines.isEmpty()) {
            return false;
        }
        lines.remove(lines.size() - 1);
        currentLine = null;
        return true;
    }

    @Override
    public boolean isValid() {
        return lines != null;
    }

    @Override
    public DoubleLines cloneValues() {
        DoubleLines np = new DoubleLines();
        for (List<DoublePoint> line : lines) {
            List<DoublePoint> newline = new ArrayList<>();
            newline.addAll(line);
            np.addLine(newline);
        }
        return np;
    }

    public List<Line> directLines() {
        List<Line> dlines = new ArrayList<>();
        int lastx, lasty = -1, thisx, thisy;
        for (List<DoublePoint> lineData : lines) {
            lastx = -1;
            for (DoublePoint linePoint : lineData) {
                thisx = (int) Math.round(linePoint.getX());
                thisy = (int) Math.round(linePoint.getY());
                if (lastx >= 0) {
                    Line line = new Line(lastx, lasty, thisx, thisy);
                    dlines.add(line);
                }
                lastx = thisx;
                lasty = thisy;
            }
        }
        return dlines;
    }

    @Override
    public boolean include(double x, double y) {
        if (!isValid()) {
            return false;
        }
        int lastx, lasty = -1, thisx, thisy;
        Point2D point = new Point2D(x, y);
        for (List<DoublePoint> lineData : lines) {
            lastx = -1;
            for (DoublePoint linePoint : lineData) {
                thisx = (int) Math.round(linePoint.getX());
                thisy = (int) Math.round(linePoint.getY());
                if (lastx >= 0) {
                    Line line = new Line(lastx, lasty, thisx, thisy);
                    if (line.contains(point)) {
                        return true;
                    }
                }
                lastx = thisx;
                lasty = thisy;
            }
        }
        return false;
    }

    public boolean include(Point2D point) {
        if (!isValid()) {
            return false;
        }
        int lastx, lasty = -1, thisx, thisy;
        for (List<DoublePoint> lineData : lines) {
            lastx = -1;
            for (DoublePoint linePoint : lineData) {
                thisx = (int) Math.round(linePoint.getX());
                thisy = (int) Math.round(linePoint.getY());
                if (lastx >= 0) {
                    Line line = new Line(lastx, lasty, thisx, thisy);
                    if (line.contains(point)) {
                        return true;
                    }
                }
                lastx = thisx;
                lasty = thisy;
            }
        }
        return false;
    }

    @Override
    public DoubleRectangle getBound() {
        return null;
    }

    public void clear() {
        lines.clear();
        currentLine = null;
    }

    @Override
    public DoubleLines move(double offset) {
        DoubleLines np = new DoubleLines();
        for (List<DoublePoint> line : lines) {
            List<DoublePoint> newline = new ArrayList<>();
            for (DoublePoint p : line) {
                newline.add(new DoublePoint(p.getX() + offset, p.getY() + offset));
            }
            np.addLine(newline);
        }
        return np;
    }

    @Override
    public DoubleLines move(double offsetX, double offsetY) {
        DoubleLines np = new DoubleLines();
        for (List<DoublePoint> line : lines) {
            List<DoublePoint> newline = new ArrayList<>();
            for (DoublePoint p : line) {
                newline.add(new DoublePoint(p.getX() + offsetX, p.getY() + offsetY));
            }
            np.addLine(newline);
        }
        return np;
    }

    public int getLinesSize() {
        return lines.size();
    }

    public int getPointsSize() {
        int count = 0;
        for (List<DoublePoint> line : lines) {
            count += line.size();
        }
        return count;
    }

    public DoublePoint getPoint(int index) {
        int count = 0;
        for (List<DoublePoint> line : lines) {
            for (DoublePoint p : line) {
                if (index == count) {
                    return p;
                }
                count++;
            }
        }
        return null;

    }

    public List<DoublePoint> getLine(int index) {
        try {
            return lines.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public List<DoublePoint> getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(List<DoublePoint> currentLine) {
        this.currentLine = currentLine;
    }

    public List<List<DoublePoint>> getLines() {
        return lines;
    }

    public void setLines(List<List<DoublePoint>> lines) {
        this.lines = lines;
    }

}
