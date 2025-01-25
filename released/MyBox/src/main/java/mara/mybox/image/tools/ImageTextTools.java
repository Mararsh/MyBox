package mara.mybox.image.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import mara.mybox.image.data.PixelsBlend;
import static mara.mybox.image.tools.ShapeTools.stroke;
import mara.mybox.controller.ControlImageText;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ImageTextTools {

    public static BufferedImage addText(FxTask task,
            BufferedImage sourceImage, ControlImageText optionsController) {
        try {
            String text = optionsController.text();
            if (text == null || text.isEmpty()) {
                return sourceImage;
            }
            int width = sourceImage.getWidth();
            int height = sourceImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            Font font = optionsController.font();
            BufferedImage shapeImage = new BufferedImage(width, height, imageType);
            Graphics2D g = shapeImage.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            FontMetrics metrics = g.getFontMetrics(font);
            optionsController.countValues(g, metrics, width, height);
            PixelsBlend blend = optionsController.getBlend();
            if (blend == null || (task != null && !task.isWorking())) {
                return null;
            }
            int textBaseX = optionsController.getBaseX();
            int textBaseY = optionsController.getTextY();
            int shadowSize = optionsController.getShadow();
            g.rotate(Math.toRadians(optionsController.getAngle()), textBaseX, textBaseY);

            Color textColor = Color.BLACK;
            Color shadowColor = Color.GRAY;
            Color borderColor = Color.GREEN;
            Color fillColor = Color.RED;
            Color backgroundColor = Color.BLUE;
            g.setBackground(backgroundColor);

            if (optionsController.showBorders()) {
                ShapeStyle style = optionsController.getBorderStyle();
                if (style == null || (task != null && !task.isWorking())) {
                    return null;
                }
                g.setStroke(stroke(style));
                int m = optionsController.getBordersMargin();
                DoubleRectangle border = DoubleRectangle.xywh(
                        optionsController.getBaseX() - m,
                        optionsController.getBaseY() - m,
                        optionsController.getTextWidth() + 2 * m,
                        optionsController.getTextHeight() + 2 * m);
                border.setRoundx(optionsController.getBordersArc());
                border.setRoundy(optionsController.getBordersArc());
                Shape shape = border.getShape();
                if (style == null || (task != null && !task.isWorking())) {
                    return null;
                }
                if (optionsController.bordersFilled()) {
                    g.setColor(fillColor);
                    g.fill(shape);
                }
                if (optionsController.getBordersStrokeWidth() > 0) {
                    g.setColor(borderColor);
                    g.draw(shape);
                }
            }
            if (blend == null || (task != null && !task.isWorking())) {
                return null;
            }

            g.setStroke(new BasicStroke());
            g.setFont(font);
            if (shadowSize > 0) {
                g.setColor(shadowColor);
                drawText(g, optionsController, text, shadowSize);
            }
            g.setColor(textColor);
            drawText(g, optionsController, text, 0);
            g.dispose();

            if (blend == null || (task != null && !task.isWorking())) {
                return null;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            int textPixel = textColor.getRGB();
            int shadowPixel = shadowColor.getRGB();
            int borderPixel = borderColor.getRGB();
            int fillPixel = fillColor.getRGB();
            int realTextPixel = optionsController.textColor().getRGB();
            int realShadowPixel = optionsController.shadowColor().getRGB();
            int realBorderPixel = optionsController.bordersStrokeColor().getRGB();
            int realFillPixel = optionsController.bordersFillColor().getRGB();
            for (int j = 0; j < height; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < width; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    int srcPixel = sourceImage.getRGB(i, j);
                    int shapePixel = shapeImage.getRGB(i, j);
                    if (shapePixel == textPixel) {
                        target.setRGB(i, j, blend.blend(realTextPixel, srcPixel));
                    } else if (shapePixel == shadowPixel) {
                        target.setRGB(i, j, blend.blend(realShadowPixel, srcPixel));
                    } else if (shapePixel == borderPixel) {
                        target.setRGB(i, j, blend.blend(realBorderPixel, srcPixel));
                    } else if (shapePixel == fillPixel) {
                        target.setRGB(i, j, blend.blend(realFillPixel, srcPixel));
                    } else {
                        target.setRGB(i, j, srcPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean drawText(Graphics2D g, ControlImageText optionsController,
            String text, int shadow) {
        try {
            if (g == null) {
                return false;
            }
            int textBaseX = optionsController.getBaseX();
            int textBaseY = optionsController.getTextY();
            int rowx = textBaseX, rowy = textBaseY, rowHeight = optionsController.getRowHeight();
            String[] lines = text.split("\n", -1);
            int lend = lines.length - 1;
            boolean isOutline = optionsController.isOutline();
            boolean leftToRight = optionsController.isLeftToRight();
            Font font = optionsController.font();
            FontMetrics metrics = g.getFontMetrics(font);
            if (optionsController.isVertical()) {
                for (int r = (leftToRight ? 0 : lend); (leftToRight ? r <= lend : r >= 0);) {
                    String line = lines[r];
                    rowy = textBaseY;
                    double cWidthMax = 0;
                    for (int i = 0; i < line.length(); i++) {
                        String c = line.charAt(i) + "";
                        drawText(g, c, font, rowx, rowy, shadow, isOutline);
                        Rectangle2D cBound = metrics.getStringBounds(c, g);
                        rowy += cBound.getHeight();
                        if (rowHeight <= 0) {
                            double cWidth = cBound.getWidth();
                            if (cWidth > cWidthMax) {
                                cWidthMax = cWidth;
                            }
                        }
                    }
                    if (rowHeight > 0) {
                        rowx += rowHeight;
                    } else {
                        rowx += cWidthMax;
                    }
                    if (leftToRight) {
                        r++;
                    } else {
                        r--;
                    }
                }
            } else {
                for (String line : lines) {
                    drawText(g, line, font, rowx, rowy, shadow, isOutline);
                    if (rowHeight > 0) {
                        rowy += rowHeight;
                    } else {
                        rowy += g.getFontMetrics(font).getStringBounds(line, g).getHeight();
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static void drawText(Graphics2D g, String text,
            Font font, int x, int y, int shadow, boolean isOutline) {
        try {
            if (text == null || text.isEmpty()) {
                return;
            }
            if (shadow > 0) {
                // Not blurred. Can improve
                g.drawString(text, x + shadow, y + shadow);
            } else {
                if (isOutline) {
                    FontRenderContext frc = g.getFontRenderContext();
                    TextLayout textTl = new TextLayout(text, font, frc);
                    Shape outline = textTl.getOutline(null);
                    g.translate(x, y);
                    g.draw(outline);
                    g.translate(-x, -y);
                } else {
                    g.drawString(text, x, y);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
