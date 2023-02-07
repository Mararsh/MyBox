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
import mara.mybox.data.DoubleRectangle;
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
                backImage = PenTools.drawRectangle(sourceImage,
                        new DoubleRectangle(optionsController.getBaseX() - m,
                                optionsController.getBaseY() - m,
                                optionsController.getBaseX() + optionsController.getTextWidth() + m - 1,
                                optionsController.getBaseY() + optionsController.getTextHeight() + m - 1),
                        optionsController.bordersStrokeColor(),
                        optionsController.getBordersStrokeWidth(), optionsController.getBordersArc(),
                        optionsController.bordersDotted(), optionsController.bordersFilled(),
                        optionsController.bordersFillColor(), opacity,
                        ImageBlend.blender(PixelsBlend.ImagesBlendMode.NORMAL, opacity, false, true));
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
            int linex = textBaseX, liney = textBaseY, lineHeight = optionsController.getLineHeight();
            String[] lines = text.split("\n", -1);
            int lend = lines.length - 1;
            int shadow = optionsController.getShadow();
            boolean isOutline = optionsController.isOutline();
            boolean leftToRight = optionsController.isLeftToRight();
            float textOpacity = noBlend ? opacity : 1.0F;
            fg.rotate(Math.toRadians(optionsController.getAngle()), textBaseX, textBaseY);
//            fg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (optionsController.isVertical()) {
                for (int r = (leftToRight ? 0 : lend); (leftToRight ? r <= lend : r >= 0);) {
                    String line = lines[r];
                    liney = textBaseY;
                    double cWidthMax = 0;
                    for (int i = 0; i < line.length(); i++) {
                        String c = line.charAt(i) + "";
                        addText(fg, c, font, textColor, linex, liney, textOpacity, shadow, isOutline);
                        Rectangle2D cBound = metrics.getStringBounds(c, fg);
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
                    addText(fg, line, font, textColor, linex, liney, textOpacity, shadow, isOutline);
                    if (lineHeight > 0) {
                        liney += lineHeight;
                    } else {
                        liney += fg.getFontMetrics(font).getStringBounds(line, fg).getHeight();
                    }
                }
            }
            fg.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return ImageBlend.blend(foreImage, backImage, 0, 0, optionsController.blender());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void addText(Graphics2D g, String text,
            Font font, Color color, int x, int y, float opacity, int shadow, boolean isOutline) {
        try {
            if (text == null || text.isEmpty()) {
                return;
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            g.setColor(color.equals(Colors.TRANSPARENT) ? null : color);
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
            if (shadow > 0) {
                // Not blurred. Can improve
                g.setColor(Color.GRAY);
                g.drawString(text, x + shadow, y + shadow);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
