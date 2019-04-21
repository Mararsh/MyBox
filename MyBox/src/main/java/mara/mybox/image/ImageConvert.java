package mara.mybox.image;

import mara.mybox.fxml.ImageManufacture;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePenLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageCombine;
import mara.mybox.data.ImageCombine.CombineSizeType;
import mara.mybox.data.ImageInformation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.IntRectangle;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.CommonValues.TRANSPARENT;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageConvert {

    public static class Direction {

        public static int Top = 0;
        public static int Bottom = 1;
        public static int Left = 2;
        public static int Right = 3;
        public static int LeftTop = 4;
        public static int RightBottom = 5;
        public static int LeftBottom = 6;
        public static int RightTop = 7;

    }

    public static class KeepRatioType {

        public static final int BaseOnWidth = 0;
        public static final int BaseOnHeight = 1;
        public static final int BaseOnLarger = 2;
        public static final int BaseOnSmaller = 3;
        public static final int None = 9;

    }

    public static boolean hasAlpha(BufferedImage source) {
        switch (source.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_BYTE_INDEXED:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_GRAY:
                return false;
            default:
                return true;
        }
    }

    public static BufferedImage checkAlpha(BufferedImage source, String targetFormat) {
        if (targetFormat != null && CommonValues.NoAlphaImages.contains(targetFormat.toLowerCase())) {
            return ImageConvert.clearAlpha(source);
        } else {
            return source;
        }
    }

    public static BufferedImage clearAlpha(BufferedImage source) {
        if (!hasAlpha(source)) {
            return source;
        }
        if (AppVaribles.isAlphaAsWhite()) {
            return ImageConvert.replaceAlphaAsWhite(source);
        } else {
            return ImageConvert.replaceAlphaAsBlack(source);
        }
    }

    public static BufferedImage removeAlpha(BufferedImage source) {
        return replaceAlphaAsBlack(source);
    }

    public static BufferedImage replaceAlphaAsBlack(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            int alpha = 0;
            int black = Color.BLACK.getRGB();
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = source.getRGB(i, j);
                    if (alpha == color) {
                        target.setRGB(i, j, black);
                    } else {
                        Color c = new Color(color, true);
                        Color newColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
                        target.setRGB(i, j, newColor.getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage replaceAlphaAsWhite(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            int alpha = TRANSPARENT.getRGB();
            int white = new Color(255, 255, 255).getRGB();
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = source.getRGB(i, j);
                    if (alpha == color) {
                        target.setRGB(i, j, white);
                    } else {
                        Color c = new Color(color, true);
                        Color newColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
                        target.setRGB(i, j, newColor.getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        if (width == source.getWidth() && height == source.getHeight()) {
            return source;
        }
        if (width <= 0 || height <= 0) {
            return source;
        }
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        Graphics2D g = target.createGraphics();
        g.setBackground(TRANSPARENT);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return target;
    }

    public static BufferedImage scaleImageWidthKeep(BufferedImage source, int width) {
        int height = source.getHeight() * width / source.getWidth();
        return scaleImage(source, width, height);
    }

    public static BufferedImage scaleImageHeightKeep(BufferedImage source, int height) {
        int width = source.getWidth() * height / source.getHeight();
        return scaleImage(source, width, height);
    }

    public static BufferedImage scaleImage(BufferedImage source, float scale) {
        int width = (int) (source.getWidth() * scale);
        int height = (int) (source.getHeight() * scale);
        return scaleImage(source, width, height);
    }

    public static BufferedImage scaleImage(BufferedImage source,
            int targetW, int targetH,
            boolean keepRatio, int keepType) {

        int finalW = targetW;
        int finalH = targetH;
        double ratioW = (double) targetW / source.getWidth();
        double ratioH = (double) targetH / source.getHeight();
        if (keepRatio && ratioW != ratioH) {
            switch (keepType) {
                case KeepRatioType.BaseOnWidth:
                    finalH = (int) (ratioW * source.getHeight());
                    break;
                case KeepRatioType.BaseOnHeight:
                    finalW = (int) (ratioH * source.getWidth());
                    break;
                case KeepRatioType.BaseOnLarger:
                    if (ratioW > ratioH) {
                        finalH = (int) (ratioW * source.getHeight());
                    } else {
                        finalW = (int) (ratioH * source.getWidth());
                    }
                    break;
                case KeepRatioType.BaseOnSmaller:
                    if (ratioW < ratioH) {
                        finalH = (int) (ratioW * source.getHeight());
                    } else {
                        finalW = (int) (ratioH * source.getWidth());
                    }
                    break;
            }
        }

        return scaleImage(source, finalW, finalH);
    }

    public static BufferedImage addText(BufferedImage source, String text,
            Font font, Color color, int x, int y,
            float opacity, int shadow, int angle,
            boolean isOutline, boolean isVertical) {
        try {
            if (opacity > 1.0f || opacity < 0) {
                opacity = 1.0f;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            if (isVertical) {
                int ay = y;
                for (int i = 0; i < text.length(); i++) {
                    String c = String.valueOf(text.charAt(i));
                    addText(g, c, font, color, x, ay, opacity, shadow, angle, isOutline);
                    ay += g.getFontMetrics().getStringBounds(c, g).getHeight();
                }
            } else {
                addText(g, text, font, color, x, y, opacity, shadow, angle, isOutline);
            }

            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void addText(Graphics2D g, String text,
            Font font, Color color, int x, int y,
            float opacity, int shadow, int angle,
            boolean isOutline) {
        try {
            AffineTransform saveAT = g.getTransform();
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(Math.toRadians(angle), 0, 0);
            Font rotatedFont = font.deriveFont(affineTransform);
            if (shadow > 0) {  // Not blurred. Can improve
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g.setColor(Color.GRAY);
                g.setFont(rotatedFont);
                g.drawString(text, x + shadow, y + shadow);
            }

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.setColor(color);
            g.setFont(rotatedFont);
            if (isOutline) {
                FontRenderContext frc = g.getFontRenderContext();
                TextLayout textTl = new TextLayout(text, rotatedFont, frc);
                Shape outline = textTl.getOutline(null);
                AffineTransform transform = g.getTransform();
                transform.translate(x, y);
                g.transform(transform);
                g.draw(outline);
            } else {
                g.drawString(text, x, y);
            }
            g.setTransform(saveAT);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static BufferedImage addTexts(BufferedImage source,
            Font font, Color color, float transparent,
            List<String> texts, List<Integer> xs, List<Integer> ys) {
        try {
            if (transparent > 1.0f || transparent < 0) {
                transparent = 1.0f;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent));
            g.setColor(color);
            g.setFont(font);
            for (int i = 0; i < texts.size(); i++) {
                g.drawString(texts.get(i), xs.get(i), ys.get(i));
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addPicture(BufferedImage source,
            BufferedImage picture, int x, int y, int w, int h,
            boolean keepRatio, float transparent) {
        try {
            if (w <= 0 || h <= 0 || picture == null || picture.getWidth() == 0) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int ah = h, aw = w;
            if (keepRatio) {
                if (w * 1.0f / h > picture.getWidth() * 1.0f / picture.getHeight()) {
                    ah = (int) (picture.getHeight() * w / picture.getWidth());
                } else {
                    aw = (int) (picture.getWidth() * h / picture.getHeight());
                }
            }
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent));
            g.drawImage(picture, x, y, aw, ah, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addArc(BufferedImage source, int arc, Color bgColor) {
        int width = source.getWidth();
        int height = source.getHeight();
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        if (bgColor.getRGB() == TRANSPARENT.getRGB()) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);
        g.setClip(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
        g.drawImage(source, 0, 0, null);
        g.dispose();

        return target;
    }

    public static BufferedImage addShadow(BufferedImage source, int shadowWidth,
            Color shadowColor) {
        if (hasAlpha(source)) {
            return addShadowAlpha(source, shadowWidth, shadowColor);
        } else {
            return addShadowNoAlpha(source, shadowWidth, shadowColor);
        }
    }

    public static BufferedImage addShadowAlpha(BufferedImage source, int shadowWidth,
            Color shadowColor) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            if (shadowWidth <= 0 || 2 * shadowWidth > width || 2 * shadowWidth > height) {
                return source;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage shadowImage = new BufferedImage(width, height, imageType);
            Color newColor;
            float iOpocity, jOpacity, opocity;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    iOpocity = jOpacity = 1.0f;
                    if (i < shadowWidth) {
                        iOpocity = 1.0f * i / shadowWidth;
                    } else if (i > width - shadowWidth) {
                        iOpocity = 1.0f * (width - i) / shadowWidth;
                    }
                    if (j < shadowWidth) {
                        jOpacity = 1.0f * j / shadowWidth;
                    } else if (j > height - shadowWidth) {
                        jOpacity = 1.0f * (height - j) / shadowWidth;
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0f) {
                        newColor = shadowColor;
                    } else {
                        newColor = new Color(shadowColor.getRed() / 255.0f,
                                shadowColor.getGreen() / 255.0f, shadowColor.getBlue() / 255.0f, opocity);
                    }
                    shadowImage.setRGB(i, j, newColor.getRGB());
                }
            }

            BufferedImage target = new BufferedImage(width + shadowWidth, height + shadowWidth, imageType);
            Graphics2D g = target.createGraphics();
            Color bgColor = TRANSPARENT;
            g.setColor(bgColor);
            g.fillRect(0, 0, width + shadowWidth, height + shadowWidth);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.drawImage(shadowImage, shadowWidth, shadowWidth, null);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addShadowNoAlpha(BufferedImage source, int shadowWidth,
            Color shadowColor) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            if (shadowWidth <= 0 || 2 * shadowWidth > width || 2 * shadowWidth > height) {
                return source;
            }
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage shadowImage = new BufferedImage(width, height, imageType);
            float iOpocity, jOpacity, opocity;
            Color newColor;
            Color bgColor;
            if (AppVaribles.isAlphaAsWhite()) {
                bgColor = Color.WHITE;
            } else {
                bgColor = Color.BLACK;
            }
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    iOpocity = jOpacity = 1.0f;
                    if (i < shadowWidth) {
                        iOpocity = 1.0f * i / shadowWidth;
                    } else if (i > width - shadowWidth) {
                        iOpocity = 1.0f * (width - i) / shadowWidth;
                    }
                    if (j < shadowWidth) {
                        jOpacity = 1.0f * j / shadowWidth;
                    } else if (j > height - shadowWidth) {
                        jOpacity = 1.0f * (height - j) / shadowWidth;
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0f) {
                        newColor = shadowColor;
                    } else {
                        newColor = blendAlpha(shadowColor, opocity, bgColor);
                    }
                    shadowImage.setRGB(i, j, newColor.getRGB());
                }
            }

            BufferedImage target = new BufferedImage(width + shadowWidth, height + shadowWidth, imageType);
            Graphics2D g = target.createGraphics();
            g.setColor(bgColor);
            g.fillRect(0, 0, width + shadowWidth, height + shadowWidth);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.drawImage(shadowImage, shadowWidth, shadowWidth, null);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage cutMargins(BufferedImage source, Color cutColor,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (cutColor.getRGB() == TRANSPARENT.getRGB()
                    && !hasAlpha(source)) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int top = 0, bottom = height - 1, left = 0, right = width - 1;
            int cutValue = cutColor.getRGB();
            if (cutTop) {
                for (int j = 0; j < height; j++) {
                    boolean hasValue = false;
                    for (int i = 0; i < width; i++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        top = j;
                        break;
                    }
                }
            }
//            logger.debug("top: " + top);
            if (top < 0) {
                return null;
            }
            if (cutBottom) {
                for (int j = height - 1; j >= 0; j--) {
                    boolean hasValue = false;
                    for (int i = 0; i < width; i++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        bottom = j;
                        break;
                    }
                }
            }
//            logger.debug("bottom: " + bottom);
            if (bottom < 0) {
                return null;
            }
            if (cutLeft) {
                for (int i = 0; i < width; i++) {
                    boolean hasValue = false;
                    for (int j = 0; j < height; j++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        left = i;
                        break;
                    }
                }
            }
//            logger.debug("left: " + left);
            if (left < 0) {
                return null;
            }
            if (cutRight) {
                for (int i = width - 1; i >= 0; i--) {
                    boolean hasValue = false;
                    for (int j = 0; j < height; j++) {
                        if (source.getRGB(i, j) != cutValue) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (hasValue) {
                        right = i;
                        break;
                    }
                }
            }
//            logger.debug("right: " + right);
            if (right < 0) {
                return null;
            }

            int w = right + 1 - left;
            int h = bottom + 1 - top;
            BufferedImage target = source.getSubimage(left, top, w, h);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage cutMargins(BufferedImage source,
            int MarginWidth, boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (source == null || MarginWidth <= 0) {
                return source;
            }
            if (!cutTop && !cutBottom && !cutLeft && !cutRight) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int x1 = 0, y1 = 0, x2 = width - 1, y2 = height - 2;
            if (cutLeft) {
                x1 = MarginWidth;
            }
            if (cutRight) {
                x2 = width - 1 - MarginWidth;
            }
            if (cutTop) {
                y1 = MarginWidth;
            }
            if (cutBottom) {
                y2 = height - 1 - MarginWidth;
            }
            return cropOutside(source, x1, y1, x2, y2);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addMargins(BufferedImage source, Color addColor,
            int MarginWidth, boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (source == null || MarginWidth <= 0) {
                return source;
            }
            if (!addTop && !addBottom && !addLeft && !addRight) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int totalWidth = width, totalHegiht = height;
            int x = 0, y = 0;
            if (addLeft) {
                totalWidth += MarginWidth;
                x = MarginWidth;
            }
            if (addRight) {
                totalWidth += MarginWidth;
            }
            if (addTop) {
                totalHegiht += MarginWidth;
                y = MarginWidth;
            }
            if (addBottom) {
                totalHegiht += MarginWidth;
            }
            int imageType = source.getType();
            if (addColor.getRGB() == TRANSPARENT.getRGB()) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(totalWidth, totalHegiht, imageType);
            Graphics2D g = target.createGraphics();
            g.setColor(addColor);
            g.fillRect(0, 0, totalWidth, totalHegiht);
            g.drawImage(source, x, y, width, height, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blurMarginsAlpha(BufferedImage source, int blurWidth,
            boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            float iOpocity, jOpacity, opocity;
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int pixel = source.getRGB(i, j);
                    iOpocity = jOpacity = 1.0f;
                    if (i < blurWidth) {
                        if (blurLeft) {
                            iOpocity = 1.0f * i / blurWidth;
                        }
                    } else if (i > width - blurWidth) {
                        if (blurRight) {
                            iOpocity = 1.0f * (width - i) / blurWidth;
                        }
                    }
                    if (j < blurWidth) {
                        if (blurTop) {
                            jOpacity = 1.0f * j / blurWidth;
                        }
                    } else if (j > height - blurWidth) {
                        if (blurBottom) {
                            jOpacity = 1.0f * (height - j) / blurWidth;
                        }
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0f) {
                        target.setRGB(i, j, pixel);
                    } else {
                        newColor = new Color(pixel);
                        opocity = newColor.getAlpha() * opocity;
                        newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(),
                                (int) opocity);
                        target.setRGB(i, j, newColor.getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blurMarginsNoAlpha(BufferedImage source, int blurWidth,
            boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            float iOpocity, jOpacity, opocity;
            Color bgColor;
            if (AppVaribles.isAlphaAsWhite()) {
                bgColor = Color.WHITE;
            } else {
                bgColor = Color.BLACK;
            }
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int pixel = source.getRGB(i, j);
                    iOpocity = jOpacity = 1.0f;
                    if (i < blurWidth) {
                        if (blurLeft) {
                            iOpocity = 1.0f * i / blurWidth;
                        }
                    } else if (i > width - blurWidth) {
                        if (blurRight) {
                            iOpocity = 1.0f * (width - i) / blurWidth;
                        }
                    }
                    if (j < blurWidth) {
                        if (blurTop) {
                            jOpacity = 1.0f * j / blurWidth;
                        }
                    } else if (j > height - blurWidth) {
                        if (blurBottom) {
                            jOpacity = 1.0f * (height - j) / blurWidth;
                        }
                    }
                    opocity = iOpocity * jOpacity;
                    if (opocity == 1.0f) {
                        target.setRGB(i, j, pixel);
                    } else {
                        Color color = blendAlpha(new Color(pixel), opocity, bgColor);
                        target.setRGB(i, j, color.getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    // https://www.cnblogs.com/xiaonanxia/p/9448444.html
    public static Color blendAlpha(Color color, float opocity, Color bgColor) {
        int red = (int) (color.getRed() * opocity + bgColor.getRed() * (1 - opocity));
        int green = (int) (color.getGreen() * opocity + bgColor.getGreen() * (1 - opocity));
        int blue = (int) (color.getBlue() * opocity + bgColor.getBlue() * (1 - opocity));
        return new Color(red, green, blue);
    }

    public static BufferedImage rotateImage(BufferedImage source, int angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle = 360 + angle;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth, newHeight;
        boolean isSkew = false;
        switch (angle) {
            case 180:
            case 0:
            case 360:
                newWidth = width;
                newHeight = height;
                break;
            case 90:
            case 270:
                if (width > height) {
                    newWidth = width;
                    newHeight = width;
                } else {
                    newWidth = height;
                    newHeight = height;
                }
                break;
            default:
                if (width > height) {
                    newWidth = 2 * width;
                    newHeight = 2 * width;
                } else {
                    newWidth = 2 * height;
                    newHeight = 2 * height;
                }
                isSkew = true;
                break;
        }
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D g = target.createGraphics();
        Color bgColor = TRANSPARENT;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setBackground(bgColor);
        if (!isSkew) {
            g.rotate(Math.toRadians(angle), newWidth / 2, newHeight / 2);
            g.drawImage(source, 0, 0, null);
        } else {
            g.rotate(Math.toRadians(angle), width, height);
            g.drawImage(source, width / 2, height / 2, null);
        }
        g.dispose();
//        logger.debug("Rotated: " + newWidth + ", " + newHeight);

        target = ImageConvert.cutMargins(target, bgColor, true, true, true, true);
        return target;
    }

    public static BufferedImage horizontalMirrorImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                int l = 0, r = width - 1;
                while (l < r) {
                    int pl = source.getRGB(l, j);
                    int pr = source.getRGB(r, j);
                    target.setRGB(l, j, pr);
                    target.setRGB(r, j, pl);
                    l++;
                    r--;
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage verticalMirrorImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int i = 0; i < width; i++) {
                int t = 0, b = height - 1;
                while (t < b) {
                    int pt = source.getRGB(i, t);
                    int pb = source.getRGB(i, b);
                    target.setRGB(i, t, pb);
                    target.setRGB(i, b, pt);
                    t++;
                    b--;
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage shearImage(BufferedImage source, float shearX, float shearY) {
        try {
            int scale = Math.round(Math.abs(shearX));
            if (scale <= 1) {
                scale = 2;
            }
            scale = scale * scale;
//            if (scale > 64) {
//                scale = 64;
//            }
            int width = source.getWidth() * scale;
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            Color bgColor = TRANSPARENT;
            g.setBackground(bgColor);
            if (shearX < 0) {
                g.translate(width / 2, 0);
            }
            g.shear(shearX, shearY);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            target = ImageConvert.cutMargins(target, bgColor, true, true, true, true);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static void applyQualityProperties(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    public static BufferedImage createCompatibleImage(int width, int height) {
        return createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    public static BufferedImage createCompatibleImage(int width, int height, int transparency) {
        BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
        image.coerceData(true);
        return image;
    }

    public static BufferedImage cropOutside(BufferedImage source,
            DoubleShape shape, Color bgColor) {
        try {
            if (shape == null || !shape.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            DoubleRectangle bound = shape.getBound();

            int x1 = (int) Math.round(Math.max(0, bound.getSmallX()));
            int y1 = (int) Math.round(Math.max(0, bound.getSmallY()));
            if (x1 >= width || y1 >= height) {
                return source;
            }
            int x2 = (int) Math.round(Math.min(width - 1, bound.getBigX()));
            int y2 = (int) Math.round(Math.min(height - 1, bound.getBigY()));
            int w = x2 - x1 + 1;
            int h = y2 - y1 + 1;

            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(w, h, imageType);
            int bgPixel = bgColor.getRGB();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (shape.include(x1 + x, y1 + y)) {
                        target.setRGB(x, y, source.getRGB(x1 + x, y1 + y));
                    } else {
                        target.setRGB(x, y, bgPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage cropInside(BufferedImage source,
            DoubleShape shape, Color bgColor) {
        try {
            if (shape == null || !shape.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            int bgPixel = bgColor.getRGB();
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    if (shape.include(i, j)) {
                        target.setRGB(i, j, bgPixel);
                    } else {
                        target.setRGB(i, j, source.getRGB(i, j));
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage cropOutside(BufferedImage source,
            IntRectangle rectangle) {
        return cropOutside(source, rectangle.getSmallX(), rectangle.getSmallY(),
                rectangle.getBigX(), rectangle.getBigY());
    }

    public static BufferedImage cropOutside(BufferedImage source,
            double x1, double y1, double x2, double y2) {
        return cropOutside(source, new DoubleRectangle(x1, y1, x2, y2), Color.WHITE);
    }

    public static BufferedImage combineSingleColumn(List<javafx.scene.image.Image> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            int imageWidth, imageHeight, totalWidth = 0, totalHeight = 0;
            for (javafx.scene.image.Image image : images) {
                imageWidth = (int) image.getWidth();
                if (totalWidth < imageWidth) {
                    totalWidth = imageWidth;
                }
                totalHeight += (int) image.getHeight();
            }

            BufferedImage target = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();

            int x = 0, y = 0;
            for (javafx.scene.image.Image image : images) {
                BufferedImage source = SwingFXUtils.fromFXImage(image, null);
                imageWidth = (int) image.getWidth();
                imageHeight = (int) image.getHeight();
                g.drawImage(source, x, y, imageWidth, imageHeight, null);
                y += imageHeight;
            }

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static javafx.scene.image.Image combineSingleColumn(ImageCombine imageCombine,
            List<ImageInformation> images, boolean isPart, boolean careTotal) {
        if (imageCombine == null || images == null) {
            return null;
        }
        try {
            int x = imageCombine.getMarginsValue(), y = imageCombine.getMarginsValue(), imageWidth, imageHeight;
            int totalWidth = 0, totalHeight = 0, maxWidth = 0, minWidth = Integer.MAX_VALUE;
            int sizeType = imageCombine.getSizeType();
            if (sizeType == CombineSizeType.AlignAsBigger) {
                for (ImageInformation image : images) {
                    imageWidth = (int) image.getImage().getWidth();
                    if (imageWidth > maxWidth) {
                        maxWidth = imageWidth;
                    }
                }
            }
            if (sizeType == CombineSizeType.AlignAsSmaller) {
                for (ImageInformation image : images) {
                    imageWidth = (int) image.getImage().getWidth();
                    if (imageWidth < minWidth) {
                        minWidth = imageWidth;
                    }
                }
            }
            List<Integer> xs = new ArrayList();
            List<Integer> ys = new ArrayList();
            List<Integer> widths = new ArrayList();
            List<Integer> heights = new ArrayList();
            for (int i = 0; i < images.size(); i++) {
                ImageInformation imageInfo = images.get(i);
                javafx.scene.image.Image image = imageInfo.getImage();
                imageWidth = (int) image.getWidth();
                imageHeight = (int) image.getHeight();
                if (sizeType == CombineSizeType.KeepSize
                        || sizeType == CombineSizeType.TotalWidth
                        || sizeType == CombineSizeType.TotalHeight) {

                } else if (sizeType == CombineSizeType.EachWidth) {
                    if (!isPart) {
                        imageHeight = (imageHeight * imageCombine.getEachWidthValue()) / imageWidth;
                        imageWidth = imageCombine.getEachWidthValue();
                    }
                } else if (sizeType == CombineSizeType.EachHeight) {
                    if (!isPart) {
                        imageWidth = (imageWidth * imageCombine.getEachHeightValue()) / imageHeight;
                        imageHeight = imageCombine.getEachHeightValue();
                    }
                } else if (sizeType == CombineSizeType.AlignAsBigger) {
                    imageHeight = (imageHeight * maxWidth) / imageWidth;
                    imageWidth = maxWidth;
                } else if (sizeType == CombineSizeType.AlignAsSmaller) {
                    imageHeight = (imageHeight * minWidth) / imageWidth;
                    imageWidth = minWidth;
                }

                xs.add(x);
                ys.add(y);
                widths.add(imageWidth);
                heights.add(imageHeight);

                x = imageCombine.getMarginsValue();
                y += imageHeight + imageCombine.getIntervalValue();

                if (imageWidth > totalWidth) {
                    totalWidth = imageWidth;
                }
            }

            totalWidth += 2 * imageCombine.getMarginsValue();
            totalHeight = y + imageCombine.getMarginsValue() - imageCombine.getIntervalValue();

            javafx.scene.image.Image newImage = combineImages(images, totalWidth, totalHeight,
                    ImageManufacture.toAwtColor(imageCombine.getBgColor()),
                    xs, ys, widths, heights,
                    imageCombine.getTotalWidthValue(), imageCombine.getTotalHeightValue(),
                    careTotal && (sizeType == CombineSizeType.TotalWidth),
                    careTotal && (sizeType == CombineSizeType.TotalHeight));

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static javafx.scene.image.Image combineSingleRow(ImageCombine imageCombine,
            List<ImageInformation> images, boolean isPart, boolean careTotal) {
        if (imageCombine == null || images == null) {
            return null;
        }
        try {
            int x = imageCombine.getMarginsValue(), y = imageCombine.getMarginsValue(), imageWidth, imageHeight;
            int totalWidth = 0, totalHeight = 0, maxHeight = 0, minHeight = Integer.MAX_VALUE;
            int sizeType = imageCombine.getSizeType();

            if (isPart) {
                y = 0;
            }
            if (sizeType == CombineSizeType.AlignAsBigger) {
                for (ImageInformation image : images) {
                    imageHeight = (int) image.getImage().getHeight();
                    if (imageHeight > maxHeight) {
                        maxHeight = imageHeight;
                    }
                }
            }
            if (sizeType == CombineSizeType.AlignAsSmaller) {
                for (ImageInformation image : images) {
                    imageHeight = (int) image.getImage().getHeight();
                    if (imageHeight < minHeight) {
                        minHeight = imageHeight;
                    }
                }
            }
            List<Integer> xs = new ArrayList();
            List<Integer> ys = new ArrayList();
            List<Integer> widths = new ArrayList();
            List<Integer> heights = new ArrayList();
            for (int i = 0; i < images.size(); i++) {
                ImageInformation imageInfo = images.get(i);
                javafx.scene.image.Image image = imageInfo.getImage();
                imageWidth = (int) image.getWidth();
                imageHeight = (int) image.getHeight();
                if (sizeType == CombineSizeType.KeepSize
                        || sizeType == CombineSizeType.TotalWidth
                        || sizeType == CombineSizeType.TotalHeight) {

                } else if (sizeType == CombineSizeType.EachWidth) {
                    imageHeight = (imageHeight * imageCombine.getEachWidthValue()) / imageWidth;
                    imageWidth = imageCombine.getEachWidthValue();
                } else if (sizeType == CombineSizeType.EachHeight) {
                    imageWidth = (imageWidth * imageCombine.getEachHeightValue()) / imageHeight;
                    imageHeight = imageCombine.getEachHeightValue();
                } else if (sizeType == CombineSizeType.AlignAsBigger) {
                    imageWidth = (imageWidth * maxHeight) / imageHeight;
                    imageHeight = maxHeight;
                } else if (sizeType == CombineSizeType.AlignAsSmaller) {
                    imageWidth = (imageWidth * minHeight) / imageHeight;
                    imageHeight = minHeight;
                }

                xs.add(x);
                ys.add(y);
                widths.add(imageWidth);
                heights.add(imageHeight);

                x += imageWidth + imageCombine.getIntervalValue();

                if (imageHeight > totalHeight) {
                    totalHeight = imageHeight;
                }
            }

            totalWidth = x + imageCombine.getMarginsValue() - imageCombine.getIntervalValue();
            if (!isPart) {
                totalHeight += 2 * imageCombine.getMarginsValue();
            }

            javafx.scene.image.Image newImage = combineImages(images, totalWidth, totalHeight,
                    ImageManufacture.toAwtColor(imageCombine.getBgColor()),
                    xs, ys, widths, heights,
                    imageCombine.getTotalWidthValue(), imageCombine.getTotalHeightValue(),
                    careTotal && (sizeType == CombineSizeType.TotalWidth),
                    careTotal && (sizeType == CombineSizeType.TotalHeight));

            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static javafx.scene.image.Image combineImages(List<ImageInformation> images,
            int totalWidth, int totalHeight, Color bgColor,
            List<Integer> xs, List<Integer> ys, List<Integer> widths, List<Integer> heights,
            int trueTotalWidth, int trueTotalHeight,
            boolean isTotalWidth, boolean isTotalHeight) {
        if (images == null || xs == null || ys == null || widths == null || heights == null) {
            return null;
        }
        try {
            BufferedImage target = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, totalWidth, totalHeight);

            for (int i = 0; i < images.size(); i++) {
                ImageInformation imageInfo = images.get(i);
                javafx.scene.image.Image image = imageInfo.getImage();
                BufferedImage source = SwingFXUtils.fromFXImage(image, null);
                g.drawImage(source, xs.get(i), ys.get(i), widths.get(i), heights.get(i), null);
            }

            if (isTotalWidth) {
                target = scaleImage(target, trueTotalWidth, (trueTotalWidth * totalHeight) / totalWidth);
            } else if (isTotalHeight) {
                target = scaleImage(target, (trueTotalHeight * totalWidth) / totalHeight, trueTotalHeight);
            }

            javafx.scene.image.Image newImage = SwingFXUtils.toFXImage(target, null);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage drawRectangle(BufferedImage source,
            DoubleRectangle rect, Color strokeColor, int strokeWidth,
            int arcWidth, boolean dotted, boolean isFill, Color fillColor) {
        try {
            if (rect == null || strokeColor == null || !rect.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int x1 = (int) Math.round(rect.getSmallX());
            int y1 = (int) Math.round(rect.getSmallY());
            int x2 = (int) Math.round(rect.getBigX());
            int y2 = (int) Math.round(rect.getBigY());
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            if (strokeWidth > 0) {
                g.setColor(strokeColor);
                BasicStroke stroke;
                if (dotted) {
                    stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            1f, new float[]{strokeWidth, strokeWidth}, 0f);
                } else {
                    stroke = new BasicStroke(strokeWidth);
                }
                g.setStroke(stroke);
                if (arcWidth > 0) {
                    int a = Math.max(0, Math.min(height - 1, (int) Math.round(arcWidth)));
                    g.drawRoundRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1, a, a);
                } else {
                    g.drawRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
                }
            }
            if (isFill) {
                g.setColor(fillColor);
                if (arcWidth > 0) {
                    int a = Math.max(0, Math.min(height - 1, (int) Math.round(arcWidth)));
                    g.fillRoundRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1, a, a);
                } else {
                    g.fillRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
                }
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage drawCircle(BufferedImage source,
            DoubleCircle circle, Color strokeColor, int strokeWidth,
            boolean dotted, boolean isFill, Color fillColor) {
        try {
            if (circle == null || strokeColor == null || !circle.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int x = (int) Math.round(circle.getCenterX());
            int y = (int) Math.round(circle.getCenterY());
            int r = (int) Math.round(circle.getRadius());
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            if (strokeWidth > 0) {
                g.setColor(strokeColor);

                BasicStroke stroke;
                if (dotted) {
                    stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            1f, new float[]{strokeWidth, strokeWidth}, 0f);
                } else {
                    stroke = new BasicStroke(strokeWidth);
                }
                g.setStroke(stroke);

                g.drawOval(x - r, y - r, 2 * r, 2 * r);
            }
            if (isFill) {
                g.setColor(fillColor);
                g.fillOval(x - r, y - r, 2 * r, 2 * r);
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage drawEllipse(BufferedImage source,
            DoubleEllipse ellipse, Color strokeColor, int strokeWidth,
            boolean dotted, boolean isFill, Color fillColor) {
        try {
            if (ellipse == null || strokeColor == null || !ellipse.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int x = (int) Math.round(ellipse.getCenterX());
            int y = (int) Math.round(ellipse.getCenterY());
            int rx = (int) Math.round(ellipse.getRadiusX());
            int ry = (int) Math.round(ellipse.getRadiusY());
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            if (strokeWidth > 0) {
                g.setColor(strokeColor);
                BasicStroke stroke;
                if (dotted) {
                    stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            1f, new float[]{strokeWidth, strokeWidth}, 0f);
                } else {
                    stroke = new BasicStroke(strokeWidth);
                }
                g.setStroke(stroke);

                g.drawOval(x - rx, y - ry, 2 * rx, 2 * ry);
            }
            if (isFill) {
                g.setColor(fillColor);
                g.fillOval(x - rx, y - ry, 2 * rx, 2 * ry);
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage drawPolygon(BufferedImage source,
            DoublePolygon polygonData, Color strokeColor, int strokeWidth,
            boolean dotted, boolean isFill, Color fillColor) {
        try {
            if (polygonData == null || strokeColor == null || polygonData.getSize() < 3) {
                return source;
            }
            Map<String, int[]> xy = polygonData.getIntXY();
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            if (strokeWidth > 0) {
                g.setColor(strokeColor);
                BasicStroke stroke;
                if (dotted) {
                    stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            1f, new float[]{strokeWidth, strokeWidth}, 0f);
                } else {
                    stroke = new BasicStroke(strokeWidth);
                }
                g.setStroke(stroke);
                g.drawPolygon(xy.get("x"), xy.get("y"), polygonData.getSize());
            }
            if (isFill) {
                g.setColor(fillColor);
                g.fillPolygon(xy.get("x"), xy.get("y"), polygonData.getSize());
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage drawPolyline(BufferedImage source,
            DoublePolyline polylineData, Color strokeColor, int strokeWidth,
            boolean dotted) {
        try {
            if (polylineData == null || strokeColor == null
                    || polylineData.getSize() < 2 || strokeWidth < 1) {
                return source;
            }
            Map<String, int[]> xy = polylineData.getIntXY();
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(strokeColor);
            BasicStroke stroke;
            if (dotted) {
                stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        1f, new float[]{strokeWidth, strokeWidth}, 0f);
            } else {
                stroke = new BasicStroke(strokeWidth);
            }
            g.setStroke(stroke);
            g.drawPolyline(xy.get("x"), xy.get("y"), polylineData.getSize());

            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage drawLines(BufferedImage source,
            DoublePolyline polylineData, Color strokeColor, int strokeWidth,
            boolean dotted) {
        try {
            if (polylineData == null || strokeColor == null
                    || polylineData.getSize() < 2 || strokeWidth < 1) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);

            g.setColor(strokeColor);
            BasicStroke stroke;
            if (dotted) {
                stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        1f, new float[]{strokeWidth, strokeWidth}, 0f);
            } else {
                stroke = new BasicStroke(strokeWidth);
            }
            g.setStroke(stroke);
            int lastx = -1, lasty = -1, thisx, thisy;
            for (DoublePoint p : polylineData.getPoints()) {
                thisx = (int) Math.round(p.getX());
                thisy = (int) Math.round(p.getY());
                if (lastx >= 0) {
                    g.drawLine(lastx, lasty, thisx, thisy);
                }
                lastx = thisx;
                lasty = thisy;
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage drawLines(BufferedImage source,
            DoublePenLines penData, Color strokeColor, int strokeWidth,
            boolean dotted) {
        try {
            if (penData == null || strokeColor == null
                    || penData.getPointsSize() == 0 || strokeWidth < 1) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);

            g.setColor(strokeColor);
            BasicStroke stroke;
            if (dotted) {
                stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        1f, new float[]{strokeWidth, strokeWidth}, 0f);
            } else {
                stroke = new BasicStroke(strokeWidth);
            }
            g.setStroke(stroke);
            int lastx, lasty = -1, thisx, thisy;
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
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

}
