package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScopeTools {

    public static ImageScope.ScopeType scopeType(String type) {
        if (type == null) {
            return ImageScope.ScopeType.Invalid;
        }
        if ("All".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.All;
        }
        if ("Matting".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Matting;
        }
        if ("Rectangle".equalsIgnoreCase(type) || "RectangleColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Rectangle;
        }
        if ("Circle".equalsIgnoreCase(type) || "CircleColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Circle;
        }
        if ("Ellipse".equalsIgnoreCase(type) || "EllipseColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Ellipse;
        }
        if ("Polygon".equalsIgnoreCase(type) || "PolygonColor".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Polygon;
        }
        if ("Color".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Color;
        }
        if ("Outline".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Outline;
        }
        if ("Operate".equalsIgnoreCase(type)) {
            return ImageScope.ScopeType.Operate;
        }
        return ImageScope.ScopeType.Invalid;
    }

    public static void cloneValues(ImageScope targetScope, ImageScope sourceScope) {
        try {
            List<IntPoint> npoints = new ArrayList<>();
            if (sourceScope.getPoints() != null) {
                npoints.addAll(sourceScope.getPoints());
            }
            targetScope.setPoints(npoints);
            List<Color> ncolors = new ArrayList<>();
            if (sourceScope.getColors() != null) {
                ncolors.addAll(sourceScope.getColors());
            }
            targetScope.setColors(ncolors);
            targetScope.setRectangle(sourceScope.getRectangle().cloneValues());
            targetScope.setCircle(sourceScope.getCircle().cloneValues());
            targetScope.setEllipse(sourceScope.getEllipse().cloneValues());
            targetScope.setPolygon(sourceScope.getPolygon().cloneValues());
            targetScope.setColorDistance(sourceScope.getColorDistance());
            targetScope.setColorDistanceSquare(sourceScope.getColorDistanceSquare());
            targetScope.setHsbDistance(sourceScope.getHsbDistance());
            targetScope.setColorExcluded(sourceScope.isColorExcluded());
            targetScope.setDistanceSquareRoot(sourceScope.isDistanceSquareRoot());
            targetScope.setAreaExcluded(sourceScope.isAreaExcluded());
            targetScope.setOpacity(sourceScope.getOpacity());
            targetScope.setCreateTime(sourceScope.getCreateTime());
            targetScope.setOutline(sourceScope.getOutline());
            targetScope.setEightNeighbor(sourceScope.isEightNeighbor());
        } catch (Exception e) {
            //            MyBoxLog.debug(e);
        }
    }

    public static ImageScope cloneAll(ImageScope sourceScope) {
        ImageScope targetScope = new ImageScope();
        ImageScopeTools.cloneAll(targetScope, sourceScope);
        return targetScope;
    }

    public static void cloneAll(ImageScope targetScope, ImageScope sourceScope) {
        try {
            targetScope.setImage(sourceScope.getImage());
            targetScope.setScopeType(sourceScope.getScopeType());
            targetScope.setColorScopeType(sourceScope.getColorScopeType());
            cloneValues(targetScope, sourceScope);
        } catch (Exception e) {
        }
    }

    public static boolean inShape(DoubleShape shape, boolean areaExcluded, int x, int y) {
        if (areaExcluded) {
            return !DoubleShape.contains(shape, x, y);
        } else {
            return DoubleShape.contains(shape, x, y);
        }
    }

    public static boolean isColorMatchSquare(List<Color> colors, boolean colorExcluded, int colorDistanceSqure, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isColorMatchSquare(color, oColor, colorDistanceSqure)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isColorMatchSquare(color, oColor, colorDistanceSqure)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isRedMatch(List<Color> colors, boolean colorExcluded, int colorDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isRedMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isRedMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isGreenMatch(List<Color> colors, boolean colorExcluded, int colorDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isGreenMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isGreenMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isBlueMatch(List<Color> colors, boolean colorExcluded, int colorDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isBlueMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isBlueMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isHueMatch(List<Color> colors, boolean colorExcluded, float hsbDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isHueMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isHueMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isSaturationMatch(List<Color> colors, boolean colorExcluded, float hsbDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isSaturationMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ColorMatchTools.isSaturationMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isBrightnessMatch(List<Color> colors, boolean colorExcluded, float hsbDistance, Color color) {
        if (colors == null || colors.isEmpty()) {
            return true;
        }
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ColorMatchTools.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {

            for (Color oColor : colors) {
                if (ColorMatchTools.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static BufferedImage indicateEllipse(BufferedImage source, Color color, int lineWidth, DoubleEllipse ellipse) {
        try {
            if (!ellipse.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            g.setStroke(stroke);
            g.drawOval((int) Math.round(ellipse.getX1()), (int) Math.round(ellipse.getY1()),
                    (int) Math.round(ellipse.getRadiusX() * 2), (int) Math.round(ellipse.getRadiusY() * 2));
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage indicateRectangle(BufferedImage source, Color color, int lineWidth, DoubleRectangle rect) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            g.setStroke(stroke);
            g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage indicateCircle(BufferedImage source, Color color, int lineWidth, DoubleCircle circle) {
        try {
            if (!circle.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            g.setStroke(stroke);
            g.drawOval((int) circle.getCenterX() - (int) circle.getRadius(), (int) circle.getCenterY() - (int) circle.getRadius(), 2 * (int) circle.getRadius(), 2 * (int) circle.getRadius());
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    public static BufferedImage indicateSplit(BufferedImage source,
            List<Integer> rows, List<Integer> cols, Color lineColor, int lineWidth, boolean showSize, double scale) {
        try {
            if (rows == null || cols == null) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
            g.setColor(lineColor);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[]{
                lineWidth, lineWidth}, 0.0F);
            //            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
            g.setStroke(stroke);
            for (int i = 0; i < rows.size(); ++i) {
                int row = (int) (rows.get(i) / scale);
                if (row <= 0 || row >= height - 1) {
                    continue;
                }
                g.drawLine(0, row, width, row);
            }
            for (int i = 0; i < cols.size(); ++i) {
                int col = (int) (cols.get(i) / scale);
                if (col <= 0 || col >= width - 1) {
                    continue;
                }
                g.drawLine(col, 0, col, height);
            }
            if (showSize) {
                int fontSize = width / (cols.size() * 10);
                Font font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
                g.setFont(font);
                FontMetrics metrics = g.getFontMetrics(font);
                int yOffset = metrics.getAscent();
                for (int i = 0; i < rows.size() - 1; ++i) {
                    int h = rows.get(i + 1) - rows.get(i);
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        int w = cols.get(j + 1) - cols.get(j);
                        int x = cols.get(j) + w / 3;
                        int y = rows.get(i) + h / 3 + yOffset;
                        g.drawString(w + "x" + h, (int) (x / scale), (int) (y / scale) + yOffset);
                    }
                }
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

}
