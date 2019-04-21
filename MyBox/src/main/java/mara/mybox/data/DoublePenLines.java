package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoublePenLines implements DoubleShape {

    private List<List<DoublePoint>> lines;
    private List<DoublePoint> currentLine;

    public DoublePenLines() {
        lines = new ArrayList();
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
        currentLine = new ArrayList();
        currentLine.add(p);
        lines.add(currentLine);
        return true;
    }

    public boolean endLine(DoublePoint p) {
        if (p == null) {
            return false;
        }
        if (currentLine == null) {
            currentLine = new ArrayList();
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
            currentLine = new ArrayList();
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
    public DoublePenLines cloneValues() {
        DoublePenLines np = new DoublePenLines();
        for (List<DoublePoint> line : lines) {
            List<DoublePoint> newline = new ArrayList();
            newline.addAll(line);
            np.addLine(newline);
        }
        return np;
    }

    @Override
    public boolean include(double x, double y) {
        if (!isValid()) {
            return false;
        }
        for (List<DoublePoint> line : lines) {
            for (DoublePoint p : line) {
                if (p.getX() == x && p.getY() == y) {
                    return true;
                }
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
    public DoublePenLines move(double offset) {
        DoublePenLines np = new DoublePenLines();
        for (List<DoublePoint> line : lines) {
            List<DoublePoint> newline = new ArrayList();
            for (DoublePoint p : line) {
                newline.add(new DoublePoint(p.getX() + offset, p.getY() + offset));
            }
            np.addLine(newline);
        }
        return np;
    }

    @Override
    public DoublePenLines move(double offsetX, double offsetY) {
        DoublePenLines np = new DoublePenLines();
        for (List<DoublePoint> line : lines) {
            List<DoublePoint> newline = new ArrayList();
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
