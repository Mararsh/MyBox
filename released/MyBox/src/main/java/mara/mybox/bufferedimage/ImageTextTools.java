package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import mara.mybox.controller.ControlImageText;
import mara.mybox.data.DoubleText;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ImageTextTools {

    public static BufferedImage addText(BufferedImage sourceImage, ControlImageText optionsController) {
        try {
            String text = optionsController.text();
            if (text == null || text.isEmpty()) {
                return sourceImage;
            }
            float opacity = optionsController.getOpacity();
            if (opacity > 1.0F || opacity < 0) {
                opacity = 1.0F;
            }
            int width = sourceImage.getWidth();
            int height = sourceImage.getHeight();
            Font font = optionsController.font();
            BufferedImage foreImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = foreImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                fg.addRenderingHints(AppVariables.imageRenderHints);
            }
            FontMetrics metrics = fg.getFontMetrics(font);
            optionsController.countValues(fg, metrics, width, height);
            BufferedImage backImage = sourceImage;
            if (optionsController.showBorders()) {
                int m = optionsController.getBordersMargin();
                DoubleText textRect = new DoubleText(
                        optionsController.getBaseX() - m,
                        optionsController.getBaseY() - m,
                        optionsController.getBaseX() + optionsController.getTextWidth() + m - 1,
                        optionsController.getBaseY() + optionsController.getTextHeight() + m - 1);
                ShapeStyle style = new ShapeStyle("Text");
                style.setStrokeColor(optionsController.bordersStrokeColor());
                style.setStrokeWidth(optionsController.getBordersStrokeWidth());
                style.setIsFillColor(optionsController.bordersFilled());
                style.setFillColor(optionsController.bordersFillColor());
                style.setRoundArc(optionsController.getBordersArc());
                style.setFillOpacity(opacity);
                style.setStrokeDashed(optionsController.bordersDotted());
                backImage = ShapeTools.drawRectangle(sourceImage, textRect, style,
                        PixelsBlend.blender(PixelsBlend.ImagesBlendMode.NORMAL, opacity, false, true));
            }
            Color textColor = optionsController.textColor();
            boolean noBlend = textColor.equals(Colors.TRANSPARENT);
            if (noBlend) {
                fg.drawImage(backImage, 0, 0, width, height, null);
            } else {
                fg.setBackground(Colors.TRANSPARENT);
            }
            int textBaseX = optionsController.getBaseX();
            int textBaseY = optionsController.getTextY();
            int shadow = optionsController.getShadow();
            float textOpacity = noBlend ? opacity : 1.0F;
            fg.rotate(Math.toRadians(optionsController.getAngle()), textBaseX, textBaseY);
            fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textOpacity));
            fg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            fg.setFont(font);
            if (shadow > 0) {
                fg.setColor(optionsController.shadowColor());
                drawText(fg, optionsController, text, noBlend, shadow);
            }
            fg.setColor(textColor.equals(Colors.TRANSPARENT) ? null : textColor);
            drawText(fg, optionsController, text, noBlend, 0);

            fg.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return PixelsBlend.blend(foreImage, backImage, 0, 0, optionsController.blender());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean drawText(Graphics2D g, ControlImageText optionsController,
            String text, boolean noBlend, int shadow) {
        try {
            if (g == null) {
                return false;
            }
            int textBaseX = optionsController.getBaseX();
            int textBaseY = optionsController.getTextY();
            int linex = textBaseX, liney = textBaseY, lineHeight = optionsController.getLineHeight();
            String[] lines = text.split("\n", -1);
            int lend = lines.length - 1;
            boolean isOutline = optionsController.isOutline();
            boolean leftToRight = optionsController.isLeftToRight();
            Font font = optionsController.font();
            FontMetrics metrics = g.getFontMetrics(font);
            if (optionsController.isVertical()) {
                for (int r = (leftToRight ? 0 : lend); (leftToRight ? r <= lend : r >= 0);) {
                    String line = lines[r];
                    liney = textBaseY;
                    double cWidthMax = 0;
                    for (int i = 0; i < line.length(); i++) {
                        String c = line.charAt(i) + "";
                        drawText(g, c, font, linex, liney, shadow, isOutline);
                        Rectangle2D cBound = metrics.getStringBounds(c, g);
                        liney += cBound.getHeight();
                        if (lineHeight <= 0) {
                            double cWidth = cBound.getWidth();
                            if (cWidth > cWidthMax) {
                                cWidthMax = cWidth;
                            }
                        }
                    }
                    if (lineHeight > 0) {
                        linex += lineHeight;
                    } else {
                        linex += cWidthMax;
                    }
                    if (leftToRight) {
                        r++;
                    } else {
                        r--;
                    }
                }
            } else {
                for (String line : lines) {
                    drawText(g, line, font, linex, liney, shadow, isOutline);
                    if (lineHeight > 0) {
                        liney += lineHeight;
                    } else {
                        liney += g.getFontMetrics(font).getStringBounds(line, g).getHeight();
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
