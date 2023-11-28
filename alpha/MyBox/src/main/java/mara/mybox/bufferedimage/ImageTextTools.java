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
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class ImageTextTools {

    public static BufferedImage addText(SingletonTask task,
            BufferedImage sourceImage, ControlImageText optionsController) {
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
            int imageType = BufferedImage.TYPE_INT_ARGB;
            Font font = optionsController.font();
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D fg = foreImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                fg.addRenderingHints(AppVariables.imageRenderHints);
            }
            FontMetrics metrics = fg.getFontMetrics(font);
            optionsController.countValues(fg, metrics, width, height);
            BufferedImage backImage = sourceImage;
            PixelsBlend blend = optionsController.getBlend();
            if (blend == null || task == null || !task.isWorking()) {
                return null;
            }

            if (optionsController.showBorders()) {
                int m = optionsController.getBordersMargin();
                DoubleRectangle border = DoubleRectangle.xywh(
                        optionsController.getBaseX() - m,
                        optionsController.getBaseY() - m,
                        optionsController.getTextWidth() + 2 * m,
                        optionsController.getTextHeight() + 2 * m);
                border.setRound(optionsController.getBordersArc());
                backImage = ShapeTools.drawShape(sourceImage, border,
                        optionsController.getBorderStyle(), blend);
            }

            if (backImage == null || task == null || !task.isWorking()) {
                return null;
            }
            Color textColor = optionsController.textColor();
            Color shadowColor = optionsController.shadowColor();
            int textPixel = textColor.getRGB();
            int shadowPixel = shadowColor.getRGB();
            int bgPixel = 0;
            if (textPixel == bgPixel) {
                bgPixel = Color.WHITE.getRGB();
                if (shadowPixel == bgPixel) {
                    fg.setBackground(Color.BLACK);
                    bgPixel = Color.BLACK.getRGB();
                } else {
                    fg.setBackground(Color.WHITE);
                }
            } else if (shadowPixel == bgPixel) {
                bgPixel = Color.WHITE.getRGB();
                if (textPixel == bgPixel) {
                    fg.setBackground(Color.BLACK);
                    bgPixel = Color.BLACK.getRGB();
                } else {
                    fg.setBackground(Color.WHITE);
                }
            } else {
                fg.setBackground(Colors.TRANSPARENT);
            }
            int textBaseX = optionsController.getBaseX();
            int textBaseY = optionsController.getTextY();
            int shadowSize = optionsController.getShadow();
            float textOpacity = opacity;
            fg.rotate(Math.toRadians(optionsController.getAngle()), textBaseX, textBaseY);
            fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textOpacity));
            fg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            fg.setFont(font);
            if (shadowSize > 0) {
                fg.setColor(shadowColor);
                drawText(fg, optionsController, text, shadowSize);
            }
            fg.setColor(textColor);
            drawText(fg, optionsController, text, 0);
            fg.dispose();

            if (blend == null || task == null || !task.isWorking()) {
                return null;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int backPixel = backImage.getRGB(i, j);
                    int forePixel = foreImage.getRGB(i, j);
                    if (forePixel == bgPixel) {
                        target.setRGB(i, j, backPixel);
                    } else {
                        target.setRGB(i, j, blend.blend(forePixel, backPixel));
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
