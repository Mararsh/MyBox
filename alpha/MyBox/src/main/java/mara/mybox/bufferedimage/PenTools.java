package mara.mybox.bufferedimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.shape.Line;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class PenTools {

    public static BasicStroke stroke(DoubleShape shape, float strokeWidth) {
        return new BasicStroke(strokeWidth,
                ShapeStyle.lineCapAwt(shape),
                BasicStroke.JOIN_MITER, 1.0F,
                ShapeStyle.strokeDashAwt(shape), 0.0F);
    }

    public static BufferedImage drawRectangle(BufferedImage srcImage, DoubleRectangle rect, PixelsBlend blender) {
        try {
            if (rect == null || !rect.isValid()) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int x1 = (int) Math.round(rect.getSmallX());
            int y1 = (int) Math.round(rect.getSmallY());
            int x2 = (int) Math.round(rect.getBigX());
            int y2 = (int) Math.round(rect.getBigY());
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            Color strokeColor = ShapeStyle.strokeColorAwt(rect);
            float strokeWidth = ShapeStyle.strokeWidth(rect);
            float opacity = ShapeStyle.fillOpacity(rect);
            int arcWidth = ShapeStyle.roundArc(rect);
            if (arcWidth > 0) {
                arcWidth = Math.min(height - 1, arcWidth);
            }
            if (strokeWidth > 0) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                if (strokeColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(strokeColor);
                }
                g.setStroke(stroke(rect, strokeWidth));
                if (arcWidth > 0) {
                    g.drawRoundRect(x1, y1, x2 - x1, y2 - y1, arcWidth, arcWidth);
                } else {
                    g.drawRect(x1, y1, x2 - x1, y2 - y1);
                }
                g.dispose();
                if (strokeColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    target = PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
                }
            }
            if (ShapeStyle.isFillColor(rect)) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = ShapeStyle.fillColorAwt(rect);
                if (fillColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(fillColor);
                }
                if (arcWidth > 0) {
                    g.fillRoundRect(x1, y1, x2 - x1, y2 - y1, arcWidth, arcWidth);
                } else {
                    g.fillRect(x1, y1, x2 - x1, y2 - y1);
                }
                g.dispose();
                BufferedImage backImage = target;
                if (fillColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }

                } else {
                    target = PixelsBlend.blend(foreImage, backImage, 0, 0, blender);
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawEllipse(BufferedImage srcImage, DoubleEllipse ellipse, PixelsBlend blender) {
        try {
            if (ellipse == null || !ellipse.isValid()) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int x = (int) Math.round(ellipse.getCenterX());
            int y = (int) Math.round(ellipse.getCenterY());
            int rx = (int) Math.round(ellipse.getRadiusX());
            int ry = (int) Math.round(ellipse.getRadiusY());
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            Color strokeColor = ShapeStyle.strokeColorAwt(ellipse);
            float strokeWidth = ShapeStyle.strokeWidth(ellipse);
            float opacity = ShapeStyle.fillOpacity(ellipse);
            if (strokeWidth > 0) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                if (strokeColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(strokeColor);
                }
                g.setStroke(stroke(ellipse, strokeWidth));
                g.drawOval(x - rx, y - ry, 2 * rx, 2 * ry);
                g.dispose();
                if (strokeColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    target = PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
                }
            }
            if (ShapeStyle.isFillColor(ellipse)) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = ShapeStyle.fillColorAwt(ellipse);
                if (fillColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(fillColor);
                }
                g.fillOval(x - rx, y - ry, 2 * rx, 2 * ry);
                g.dispose();
                BufferedImage backImage = target;
                if (fillColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    target = PixelsBlend.blend(foreImage, backImage, 0, 0, blender);
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawCircle(BufferedImage srcImage, DoubleCircle circle, PixelsBlend blender) {
        try {
            if (circle == null || !circle.isValid()) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int x = (int) Math.round(circle.getCenterX());
            int y = (int) Math.round(circle.getCenterY());
            int r = (int) Math.round(circle.getRadius());
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            Color strokeColor = ShapeStyle.strokeColorAwt(circle);
            float strokeWidth = ShapeStyle.strokeWidth(circle);
            float opacity = ShapeStyle.fillOpacity(circle);
            if (strokeWidth > 0) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                if (strokeColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(strokeColor);
                }
                g.setStroke(stroke(circle, strokeWidth));
                g.drawOval(x - r, y - r, 2 * r, 2 * r);
                g.dispose();
                if (strokeColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    target = PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
                }
            }
            if (ShapeStyle.isFillColor(circle)) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = ShapeStyle.fillColorAwt(circle);
                if (fillColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(fillColor);
                }
                g.fillOval(x - r, y - r, 2 * r, 2 * r);
                g.dispose();
                BufferedImage backImage = target;
                if (fillColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    target = PixelsBlend.blend(foreImage, backImage, 0, 0, blender);
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawPolyline(BufferedImage srcImage, DoublePolyline polyline, PixelsBlend blender) {
        try {
            if (polyline == null || polyline.getSize() < 2) {
                return srcImage;
            }
            Map<String, int[]> xy = polyline.getIntXY();
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            Color strokeColor = ShapeStyle.strokeColorAwt(polyline);
            float strokeWidth = ShapeStyle.strokeWidth(polyline);
            float opacity = ShapeStyle.fillOpacity(polyline);
            if (strokeColor.getRGB() == 0) {
                g.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
            } else {
                g.setBackground(Colors.TRANSPARENT);
                g.setColor(strokeColor);
            }
            g.setStroke(stroke(polyline, strokeWidth));
            g.drawPolyline(xy.get("x"), xy.get("y"), polyline.getSize());
            g.dispose();
            if (strokeColor.getRGB() == 0) {
                BufferedImage target = new BufferedImage(width, height, imageType);
                int black = Color.BLACK.getRGB();
                int alpha = 255 - (int) (opacity * 255);
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        if (foreImage.getRGB(i, j) == black) {
                            target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                        } else {
                            target.setRGB(i, j, srcImage.getRGB(i, j));
                        }
                    }
                }
                return target;
            } else {
                return PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawPolyLines(BufferedImage srcImage, DoublePolyline polyline, PixelsBlend blender) {
        try {
            if (polyline == null || polyline.getSize() < 2) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            Color strokeColor = ShapeStyle.strokeColorAwt(polyline);
            float strokeWidth = ShapeStyle.strokeWidth(polyline);
            float opacity = ShapeStyle.fillOpacity(polyline);
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            if (strokeColor.getRGB() == 0) {
                g.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
            } else {
                g.setBackground(Colors.TRANSPARENT);
                g.setColor(strokeColor);
            }
            g.setStroke(stroke(polyline, strokeWidth));
            int lastx = -1;
            int lasty = -1;
            int thisx;
            int thisy;
            for (DoublePoint p : polyline.getPoints()) {
                thisx = (int) Math.round(p.getX());
                thisy = (int) Math.round(p.getY());
                if (lastx >= 0) {
                    g.drawLine(lastx, lasty, thisx, thisy);
                }
                lastx = thisx;
                lasty = thisy;
            }
            g.dispose();
            if (strokeColor.getRGB() == 0) {
                BufferedImage target = new BufferedImage(width, height, imageType);
                int black = Color.BLACK.getRGB();
                int alpha = 255 - (int) (opacity * 255);
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        if (foreImage.getRGB(i, j) == black) {
                            target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                        } else {
                            target.setRGB(i, j, srcImage.getRGB(i, j));
                        }
                    }
                }

                return target;
            } else {
                return PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawPolygon(BufferedImage srcImage, DoublePolygon polygon, PixelsBlend blender) {
        try {
            if (polygon == null || polygon.getSize() <= 2) {
                return srcImage;
            }
            Map<String, int[]> xy = polygon.getIntXY();
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            Color strokeColor = ShapeStyle.strokeColorAwt(polygon);
            float strokeWidth = ShapeStyle.strokeWidth(polygon);
            float opacity = ShapeStyle.fillOpacity(polygon);
            if (strokeWidth > 0) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                if (strokeColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(strokeColor);
                }
                g.setStroke(stroke(polygon, strokeWidth));
                g.drawPolygon(xy.get("x"), xy.get("y"), polygon.getSize());
                g.dispose();
                if (strokeColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }

                } else {
                    target = PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
                }
            }
            if (ShapeStyle.isFillColor(polygon)) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = ShapeStyle.fillColorAwt(polygon);
                if (fillColor.getRGB() == 0) {
                    g.setBackground(Color.WHITE);
                    g.setColor(Color.BLACK);
                } else {
                    g.setBackground(Colors.TRANSPARENT);
                    g.setColor(fillColor);
                }
                g.fillPolygon(xy.get("x"), xy.get("y"), polygon.getSize());
                g.dispose();
                BufferedImage backImage = target;
                if (fillColor.getRGB() == 0) {
                    target = new BufferedImage(width, height, imageType);
                    int black = Color.BLACK.getRGB();
                    int alpha = 255 - (int) (opacity * 255);
                    for (int j = 0; j < height; ++j) {
                        for (int i = 0; i < width; ++i) {
                            if (foreImage.getRGB(i, j) == black) {
                                target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                            } else {
                                target.setRGB(i, j, srcImage.getRGB(i, j));
                            }
                        }
                    }
                } else {
                    target = PixelsBlend.blend(foreImage, backImage, 0, 0, blender);
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawLines(BufferedImage srcImage, DoubleLines penData, PixelsBlend blender) {
        try {
            if (penData == null || penData.getPointsSize() == 0) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            Color strokeColor = ShapeStyle.strokeColorAwt(penData);
            float strokeWidth = ShapeStyle.strokeWidth(penData);
            float opacity = ShapeStyle.fillOpacity(penData);
            if (strokeColor.getRGB() == 0) {
                g.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
            } else {
                g.setBackground(Colors.TRANSPARENT);
                g.setColor(strokeColor);
            }
            g.setStroke(stroke(penData, strokeWidth));
            int lastx;
            int lasty = -1;
            int thisx;
            int thisy;
            for (List<DoublePoint> lineData : penData.getLines()) {
                lastx = -1;
                for (DoublePoint p : lineData) {
                    thisx = (int) Math.round(p.getX());
                    thisy = (int) Math.round(p.getY());
                    if (lastx >= 0) {
                        g.drawLine(lastx, lasty, thisx, thisy);
                    }
                    lastx = thisx;
                    lasty = thisy;
                }
            }
            g.dispose();
            if (strokeColor.getRGB() == 0) {
                BufferedImage target = new BufferedImage(width, height, imageType);
                int black = Color.BLACK.getRGB();
                int alpha = 255 - (int) (opacity * 255);
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        if (foreImage.getRGB(i, j) == black) {
                            target.setRGB(i, j, ColorConvertTools.setAlpha(srcImage.getRGB(i, j), alpha));
                        } else {
                            target.setRGB(i, j, srcImage.getRGB(i, j));
                        }
                    }
                }
                return target;
            } else {
                return PixelsBlend.blend(foreImage, srcImage, 0, 0, blender);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawErase(BufferedImage srcImage, DoubleLines penData, int strokeWidth) {
        try {
            if (penData == null || penData.getPointsSize() == 0 || strokeWidth < 1) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage linesImage = new BufferedImage(width, height, imageType);
            Graphics2D linesg = linesImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                linesg.addRenderingHints(AppVariables.imageRenderHints);
            }
            linesg.setBackground(Color.WHITE);
            linesg.setColor(Color.BLACK);
            linesg.setStroke(new BasicStroke(strokeWidth));
            int lastx;
            int lasty = -1;
            int thisx;
            int thisy;
            for (List<DoublePoint> lineData : penData.getLines()) {
                lastx = -1;
                for (DoublePoint p : lineData) {
                    thisx = (int) Math.round(p.getX());
                    thisy = (int) Math.round(p.getY());
                    if (lastx >= 0) {
                        linesg.drawLine(lastx, lasty, thisx, thisy);
                    }
                    lastx = thisx;
                    lasty = thisy;
                }
            }
            linesg.dispose();
            BufferedImage target = new BufferedImage(width, height, imageType);
            int black = Color.BLACK.getRGB();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    if (linesImage.getRGB(i, j) == black) {
                        target.setRGB(i, j, 0);
                    } else {
                        target.setRGB(i, j, srcImage.getRGB(i, j));
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage drawMosaic(BufferedImage source, DoubleLines penData,
            ImageMosaic.MosaicType mosaicType, int strokeWidth) {
        try {
            if (penData == null || mosaicType == null || penData.getPointsSize() == 0 || strokeWidth < 1) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D gt = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                gt.addRenderingHints(AppVariables.imageRenderHints);
            }
            gt.drawImage(source, 0, 0, width, height, null);
            gt.dispose();
            List<Line> dlines = penData.directLines();
            int pixel;
            for (Line line : dlines) {
                int x1 = Math.min(width, Math.max(0, (int) line.getStartX()));
                int y1 = Math.min(height, Math.max(0, (int) line.getStartY()));
                int x2 = Math.min(width, Math.max(0, (int) line.getEndX()));
                int y2 = Math.min(height, Math.max(0, (int) line.getEndY()));
                if (x1 == x2) {
                    if (y2 > y1) {
                        for (int x = Math.max(0, x1 - strokeWidth); x <= Math.min(width, x1 + strokeWidth); x++) {
                            for (int y = y1; y <= y2; y++) {
                                pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                                target.setRGB(x, y, pixel);
                            }
                        }
                    } else {
                        for (int x = Math.max(0, x1 - strokeWidth); x <= Math.min(width, x1 + strokeWidth); x++) {
                            for (int y = y2; y <= y1; y++) {
                                pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                                target.setRGB(x, y, pixel);
                            }
                        }
                    }
                } else if (x2 > x1) {
                    for (int x = x1; x <= x2; x++) {
                        int y0 = (x - x1) * (y2 - y1) / (x2 - x1) + y1;
                        int offset = (int) (x / (strokeWidth * Math.sqrt(x * x + y0 * y0)));
                        for (int y = Math.max(0, y0 - offset); y <= Math.min(height, y0 + offset); y++) {
                            pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                            target.setRGB(x, y, pixel);
                        }
                    }
                } else {
                    for (int x = x2; x <= x1; x++) {
                        int y0 = (x - x2) * (y1 - y2) / (x1 - x2) + y2;
                        int offset = (int) (x / (strokeWidth * Math.sqrt(x * x + y0 * y0)));
                        for (int y = Math.max(0, y0 - offset); y <= Math.min(height, y0 + offset); y++) {
                            pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                            target.setRGB(x, y, pixel);
                        }
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return source;
        }
    }

    protected static int mosaic(BufferedImage source, int imageWidth, int imageHeight, int x, int y,
            ImageMosaic.MosaicType type, int intensity) {
        int newColor;
        if (type == ImageMosaic.MosaicType.Mosaic) {
            int mx = Math.max(0, Math.min(imageWidth - 1, x - x % intensity));
            int my = Math.max(0, Math.min(imageHeight - 1, y - y % intensity));
            newColor = source.getRGB(mx, my);
        } else {
            int fx = Math.max(0, Math.min(imageWidth - 1, x - new Random().nextInt(intensity)));
            int fy = Math.max(0, Math.min(imageHeight - 1, y - new Random().nextInt(intensity)));
            newColor = source.getRGB(fx, fy);
        }
        return newColor;
    }

    public static boolean inLine(Line line, int x, int y) {
        double d = (x - line.getStartX()) * (line.getStartY() - line.getEndY()) - ((line.getStartX() - line.getEndX()) * (y - line.getStartY()));
        return Math.abs(d) < 1.0E-4 && (x >= Math.min(line.getStartX(), line.getEndX())
                && x <= Math.max(line.getStartX(), line.getEndX()))
                && (y >= Math.min(line.getStartY(), line.getEndY()))
                && (y <= Math.max(line.getStartY(), line.getEndY()));
    }

}
