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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ImageTextTools {

    public static BufferedImage addText(BufferedImage backImage, ControlImageText optionsController) {
        try {
            String text = optionsController.getText();
            if (text == null || text.isEmpty()) {
                return backImage;
            }
            float opacity = optionsController.getOpacity();
            if (opacity > 1.0F || opacity < 0) {
                opacity = 1.0F;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            BufferedImage foreImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = foreImage.createGraphics();
            Color color = optionsController.getAwtColor();
            boolean noBlend = color.equals(Colors.TRANSPARENT);
            if (noBlend) {
                g.drawImage(backImage, 0, 0, width, height, null);
            } else {
                g.setBackground(Colors.TRANSPARENT);
            }
            Font font = optionsController.getFont();
            FontMetrics metrics = g.getFontMetrics(font);
            optionsController.countBaseXY(g, metrics, width, height);
            int baseX = optionsController.getBaseX();
            int baseY = optionsController.getBaseY();
            int linex = baseX, liney = baseY, lineHeight = optionsController.getLineHeight();
            String[] lines = text.split("\n", -1);
            int lend = lines.length - 1;
            int shadow = optionsController.getShadow();
            boolean isOutline = optionsController.isOutline();
            boolean leftToRight = optionsController.isLeftToRight();
            float textOpacity = noBlend ? opacity : 1.0F;
            g.rotate(Math.toRadians(optionsController.getAngle()), baseX, baseY);
            if (optionsController.isVertical()) {
                for (int r = (leftToRight ? 0 : lend); (leftToRight ? r <= lend : r >= 0);) {
                    String line = lines[r];
                    liney = baseY;
                    double cWidthMax = 0;
                    for (int i = 0; i < line.length(); i++) {
                        String c = line.charAt(i) + "";
                        addText(g, c, font, color, linex, liney, textOpacity, shadow, isOutline);
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
                    addText(g, line, font, color, linex, liney, textOpacity, shadow, isOutline);
                    if (lineHeight > 0) {
                        liney += lineHeight;
                    } else {
                        liney += g.getFontMetrics(font).getStringBounds(line, g).getHeight();
                    }
                }
            }
            g.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return ImageBlend.blend(foreImage, backImage, 0, 0,
                        optionsController.getBlendMode(), opacity,
                        optionsController.orderReversed(), optionsController.ignoreTransparent());
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
