package mara.mybox.bufferedimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
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
public class ShapeTools {

    public static BasicStroke stroke(DoubleShape shape, ShapeStyle style) {
        return new BasicStroke(style.getStrokeWidth(),
                style.getLineCapAwt(),
                BasicStroke.JOIN_MITER, 1.0F,
                style.getStrokeDashAwt(), 0.0F);
    }

    public static BufferedImage drawRectangle(BufferedImage srcImage, DoubleRectangle rect,
            ShapeStyle style, PixelsBlend blender) {
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
            Color strokeColor = style.getStrokeColorAwt();
            float strokeWidth = style.getStrokeWidth();
            float opacity = blender.getOpacity();
            int arcWidth = style.getRoundArc();
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
                g.setStroke(stroke(rect, style));
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
            if (style.isIsFillColor()) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = style.getFillColorAwt();
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

    public static BufferedImage drawEllipse(BufferedImage srcImage, DoubleEllipse ellipse,
            ShapeStyle style, PixelsBlend blender) {
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
            Color strokeColor = style.getStrokeColorAwt();
            float strokeWidth = style.getStrokeWidth();
            float opacity = blender.getOpacity();
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
                g.setStroke(stroke(ellipse, style));
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
            if (style.isIsFillColor()) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = style.getFillColorAwt();
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

    public static BufferedImage drawCircle(BufferedImage srcImage, DoubleCircle circle,
            ShapeStyle style, PixelsBlend blender) {
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
            Color strokeColor = style.getStrokeColorAwt();
            float strokeWidth = style.getStrokeWidth();
            float opacity = blender.getOpacity();;
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
                g.setStroke(stroke(circle, style));
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
            if (style.isIsFillColor()) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = style.getFillColorAwt();
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

    public static BufferedImage drawPolyline(BufferedImage srcImage, DoublePolyline polyline,
            ShapeStyle style, PixelsBlend blender) {
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
            Color strokeColor = style.getStrokeColorAwt();
            float opacity = blender.getOpacity();;
            if (strokeColor.getRGB() == 0) {
                g.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
            } else {
                g.setBackground(Colors.TRANSPARENT);
                g.setColor(strokeColor);
            }
            g.setStroke(stroke(polyline, style));
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

    public static BufferedImage drawPolyLines(BufferedImage srcImage, DoublePolyline polyline,
            ShapeStyle style, PixelsBlend blender) {
        try {
            if (polyline == null || polyline.getSize() < 2) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            Color strokeColor = style.getStrokeColorAwt();
            float opacity = blender.getOpacity();;
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
            g.setStroke(stroke(polyline, style));
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

    public static BufferedImage drawPolygon(BufferedImage srcImage, DoublePolygon polygon,
            ShapeStyle style, PixelsBlend blender) {
        try {
            if (polygon == null || polygon.getSize() <= 2) {
                return srcImage;
            }
            Map<String, int[]> xy = polygon.getIntXY();
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = srcImage;
            Color strokeColor = style.getStrokeColorAwt();
            float strokeWidth = style.getStrokeWidth();
            float opacity = blender.getOpacity();;
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
                g.setStroke(stroke(polygon, style));
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
            if (style.isIsFillColor()) {
                BufferedImage foreImage = new BufferedImage(width, height, imageType);
                Graphics2D g = foreImage.createGraphics();
                if (AppVariables.imageRenderHints != null) {
                    g.addRenderingHints(AppVariables.imageRenderHints);
                }
                Color fillColor = style.getFillColorAwt();
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

    public static BufferedImage drawLines(BufferedImage srcImage, DoubleLines linesData,
            ShapeStyle style, PixelsBlend blender) {
        try {
            if (linesData == null || linesData.getPointsSize() == 0) {
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
            Color strokeColor = style.getStrokeColorAwt();
            float opacity = blender.getOpacity();;
            if (strokeColor.getRGB() == 0) {
                g.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
            } else {
                g.setBackground(Colors.TRANSPARENT);
                g.setColor(strokeColor);
            }
            g.setStroke(stroke(linesData, style));
            for (Line line : linesData.lines()) {
                int x1 = Math.min(width, Math.max(0, (int) line.getStartX()));
                int y1 = Math.min(height, Math.max(0, (int) line.getStartY()));
                int x2 = Math.min(width, Math.max(0, (int) line.getEndX()));
                int y2 = Math.min(height, Math.max(0, (int) line.getEndY()));
                g.drawLine(x1, y1, x2, y2);
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

    public static boolean inLine(Line line, int x, int y) {
        double d = (x - line.getStartX()) * (line.getStartY() - line.getEndY()) - ((line.getStartX() - line.getEndX()) * (y - line.getStartY()));
        return Math.abs(d) < 1.0E-4 && (x >= Math.min(line.getStartX(), line.getEndX())
                && x <= Math.max(line.getStartX(), line.getEndX()))
                && (y >= Math.min(line.getStartY(), line.getEndY()))
                && (y <= Math.max(line.getStartY(), line.getEndY()));
    }

}
