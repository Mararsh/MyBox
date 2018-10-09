package mara.mybox.image;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import static mara.mybox.objects.CommonValues.AlphaColor;
import mara.mybox.objects.ImageCombine;
import mara.mybox.objects.ImageCombine.CombineSizeType;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.objects.ImageScope;
import mara.mybox.tools.FxmlImageTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageConvertTools {

    private static final Logger logger = LogManager.getLogger();

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
            return ImageConvertTools.clearAlpha(source);
        } else {
            return source;
        }
    }

    public static BufferedImage clearAlpha(BufferedImage source) {
        if (!hasAlpha(source)) {
            return source;
        }
        if (AppVaribles.alphaAsBlack) {
            return ImageConvertTools.replaceAlphaAsBlack(source);
        } else {
            return ImageConvertTools.replaceAlphaAsWhite(source);
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
            int alpha = AlphaColor.getRGB();
            int black = Color.BLACK.getRGB();
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = source.getRGB(i, j);
                    if (alpha == color) {
                        target.setRGB(i, j, black);
                    } else {
                        Color c = new Color(color);
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
            int alpha = AlphaColor.getRGB();
            int white = new Color(255, 255, 255).getRGB();
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = source.getRGB(i, j);
                    if (alpha == color) {
                        target.setRGB(i, j, white);
                    } else {
                        Color c = new Color(color);
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

    public static class KeepRatioType {

        public static final int BaseOnWidth = 0;
        public static final int BaseOnHeight = 1;
        public static final int BaseOnLarger = 2;
        public static final int BaseOnSmaller = 3;
        public static final int None = 9;

    }

    public static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        if (width == source.getWidth() && height == source.getHeight()) {
            return source;
        }
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        Graphics2D g = target.createGraphics();
        g.setBackground(AlphaColor);
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

        double ratioW = (double) targetW / source.getWidth();
        double ratioH = (double) targetH / source.getHeight();
        if (keepRatio && ratioW != ratioH) {
            switch (keepType) {
                case KeepRatioType.BaseOnWidth:
                    targetH = (int) (ratioW * source.getWidth());
                    break;
                case KeepRatioType.BaseOnHeight:
                    targetW = (int) (ratioH * source.getWidth());
                    break;
                case KeepRatioType.BaseOnLarger:
                    if (ratioW > ratioH) {
                        targetH = (int) (ratioW * source.getWidth());
                    } else {
                        targetW = (int) (ratioH * source.getWidth());
                    }
                    break;
                case KeepRatioType.BaseOnSmaller:
                    if (ratioW < ratioH) {
                        targetH = (int) (ratioW * source.getWidth());
                    } else {
                        targetW = (int) (ratioH * source.getWidth());
                    }
                    break;
            }
        }
        return scaleImage(source, targetW, targetH);
    }

    public static BufferedImage changeSaturate(BufferedImage source, float change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    float[] hsb = ImageColorTools.pixel2HSB(source.getRGB(i, j));
                    float v = hsb[1] + change;
                    if (v > 1.0f) {
                        v = 1.0f;
                    }
                    if (v < 0.0f) {
                        v = 0.0f;
                    }
                    Color newColor = Color.getHSBColor(hsb[0], v, hsb[2]);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeBrightness(BufferedImage source, float change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    float[] hsb = ImageColorTools.pixel2HSB(source.getRGB(i, j));
                    float v = hsb[2] + change;
                    if (v > 1.0f) {
                        v = 1.0f;
                    }
                    if (v < 0.0f) {
                        v = 0.0f;
                    }
                    Color newColor = Color.getHSBColor(hsb[0], hsb[1], v);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeHue(BufferedImage source, float change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    float[] hsb = ImageColorTools.pixel2HSB(source.getRGB(i, j));
                    float v = hsb[0] + change;
                    if (v > 1.0f) {
                        v = v - 1.0f;
                    }
                    if (v < 0.0f) {
                        v = v + 1.0f;
                    }
                    Color newColor = Color.getHSBColor(v, hsb[1], hsb[2]);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeRed(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j));
                    int v = color.getRed() + change;
                    if (v > 255) {
                        v = 255;
                    }
                    if (v < 0) {
                        v = 0;
                    }
                    newColor = new Color(v, color.getGreen(), color.getBlue(), color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeGreen(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j));
                    int v = color.getGreen() + change;
                    if (v > 255) {
                        v = 255;
                    }
                    if (v < 0) {
                        v = 0;
                    }
                    newColor = new Color(color.getRed(), v, color.getBlue(), color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage changeBlue(BufferedImage source, int change) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Color newColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j));
                    int v = color.getBlue() + change;
                    if (v > 255) {
                        v = 255;
                    }
                    if (v < 0) {
                        v = 0;
                    }
                    newColor = new Color(color.getRed(), color.getGreen(), v, color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addAlpha(BufferedImage src, int alpha) {
        try {
            int width = src.getWidth();
            int height = src.getHeight();
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int rgb = src.getRGB(i, j);
                    Color color = new Color(rgb);
                    Color newcolor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                    target.setRGB(i, j, newcolor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage makeInvert(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Color color = new Color(source.getRGB(i, j));
                    Color newColor = new Color(255 - color.getRed(), 255 - color.getGreen(),
                            255 - color.getBlue(), color.getAlpha());
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage makeBinary(BufferedImage source, int percent) {
        return ImageGrayTools.color2BinaryWithPercentage(source, percent);
    }

    public static BufferedImage keepRed(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    target.setRGB(i, j, source.getRGB(i, j) & 0xFFFF0000);
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage keepGreen(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    target.setRGB(i, j, source.getRGB(i, j) & 0xFF00FF00);
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage keepBlue(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    target.setRGB(i, j, source.getRGB(i, j) & 0xFF0000FF);
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage replaceColor(BufferedImage source,
            Color oldColor, Color newColor, int distance,
            boolean isColor, boolean excluded) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int newValue = newColor.getRGB();
            int imageType = source.getType();
            if (newColor.getRGB() == AlphaColor.getRGB()) {
                imageType = BufferedImage.TYPE_4BYTE_ABGR;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = source.getRGB(i, j);
                    if (matchColor(new Color(color), oldColor, distance, isColor, excluded)) {
                        target.setRGB(i, j, newValue);
                    } else {
                        target.setRGB(i, j, color);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static boolean matchColor(Color color1, Color color2,
            int distance, boolean isColor, boolean excluded) {
        boolean isMatch;
        if (isColor) {
            isMatch = ImageColorTools.isColorMatch(color1, color2, distance);
        } else {
            isMatch = ImageColorTools.isHueMatch(color1, color2, distance);
        }
        if (!excluded) {
            return isMatch;
        } else {
            return !isMatch;
        }
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
        Color bgColor = AlphaColor;
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

        target = cutMargins(target, bgColor, true, true, true, true);
        return target;
    }

    public static BufferedImage cutMargins(BufferedImage source, Color cutColor,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (cutColor.getRGB() == AlphaColor.getRGB()
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
            return cropImage(source, x1, y1, x2, y2);
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
            if (addColor.getRGB() == AlphaColor.getRGB()) {
                imageType = BufferedImage.TYPE_4BYTE_ABGR;
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
            Color bgColor = AlphaColor;
            g.setBackground(bgColor);
            if (shearX < 0) {
                g.translate(width / 2, 0);
            }
            g.shear(shearX, shearY);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            target = cutMargins(target, bgColor, true, true, true, true);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addWatermarkText(BufferedImage source, String text,
            Font font, Color color, int x, int y,
            float transparent, int shadow, int angle) {
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
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(Math.toRadians(angle), 0, 0);
            Font rotatedFont = font.deriveFont(affineTransform);
            if (shadow > 0) {  // Not blurred. Can improve
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g.setColor(Color.GRAY);
                g.setFont(rotatedFont);
                g.drawString(text, x + shadow, y + shadow);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent));
            g.setColor(color);
            g.setFont(rotatedFont);
            g.drawString(text, x, y);
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addWatermarkTexts(BufferedImage source,
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

    public static BufferedImage addWatermarkImage(BufferedImage source, BufferedImage water, int x, int y) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            g.drawImage(water, x, y, null);
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
        if (bgColor.getRGB() == AlphaColor.getRGB()) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.setBackground(bgColor);
        g.setClip(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
        g.drawImage(source, 0, 0, null);
        g.dispose();

        return target;
    }

    public static BufferedImage addShadow(BufferedImage source, int shadow, Color shadowColor) {
        if (shadow <= 0) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(width + shadow, height + shadow, imageType);
        Graphics2D g = target.createGraphics();
        Color bgColor = AlphaColor;
        g.setBackground(bgColor);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.setColor(shadowColor);
        g.fillRect(shadow, shadow, width, height);
        g.drawImage(source, 0, 0, null);
        g.dispose();

        return target;
    }

    public static BufferedImage cropImage(BufferedImage source,
            int x1, int y1, int x2, int y2) {
        try {

            int width = source.getWidth();
            int height = source.getHeight();
            if (x1 >= x2 || y1 >= y2
                    || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0
                    || x1 > width - 1 || y1 > height - 1
                    || x2 > width - 1 || y2 > height - 1) {
                return source;
            }
            int w = x2 - x1 + 1;
            int h = y2 - y1 + 1;
            BufferedImage target = source.getSubimage(x1, y1, w, h);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage showArea(BufferedImage source,
            Color color, int lineWidth,
            int x1, int y1, int x2, int y2) {
        try {

            int width = source.getWidth();
            int height = source.getHeight();
            if (x1 >= x2 || y1 >= y2
                    || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0
                    || x1 > width || x2 > width || y1 > height || y2 > height) {
                return source;
            }
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
            g.setStroke(stroke);
            g.drawLine(x1, y1, x2, y1);
            g.drawLine(x1, y1, x1, y2);
            g.drawLine(x2, y1, x2, y2);
            g.drawLine(x1, y2, x2, y2);
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage showScope(BufferedImage source, ImageScope scope) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int alpha = (int) Math.round(scope.getOpacity() * 255);
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int j = scope.getLeftY(); j <= scope.getRightY(); j++) {
                for (int i = scope.getLeftX(); i <= scope.getRightX(); i++) {
                    int rgb = source.getRGB(i, j);
                    Color color = new Color(rgb);
                    Color newcolor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                    target.setRGB(i, j, newcolor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage indicateSplit(BufferedImage source,
            List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize) {
        try {
            if (rows == null || cols == null) {
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
            g.setColor(lineColor);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
//            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
            g.setStroke(stroke);

            for (int i = 0; i < rows.size(); i++) {
                int row = rows.get(i);
                if (row <= 0 || row >= height - 1) {
                    continue;
                }
                g.drawLine(0, row, width, row);
            }
            for (int i = 0; i < cols.size(); i++) {
                int col = cols.get(i);
                if (col <= 0 || col >= width - 1) {
                    continue;
                }
                g.drawLine(col, 0, col, height);
            }

            if (showSize) {
                List<String> texts = new ArrayList<>();
                List<Integer> xs = new ArrayList<>();
                List<Integer> ys = new ArrayList<>();
                for (int i = 0; i < rows.size() - 1; i++) {
                    int h = rows.get(i + 1) - rows.get(i);
                    for (int j = 0; j < cols.size() - 1; j++) {
                        int w = cols.get(j + 1) - cols.get(j);
                        texts.add(w + "x" + h);
                        xs.add(cols.get(j) + w / 3);
                        ys.add(rows.get(i) + h / 3);
//                    logger.debug(w / 2 + ", " + h / 2 + "  " + w + "x" + h);
                    }
                }

                int fontSize = width / (cols.size() * 10);
                Font font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
                g.setFont(font);
                for (int i = 0; i < texts.size(); i++) {
                    g.drawString(texts.get(i), xs.get(i), ys.get(i));
                }
            }

            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
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
            List<ImageFileInformation> images, boolean isPart, boolean careTotal) {
        if (imageCombine == null || images == null) {
            return null;
        }
        try {
            int x = imageCombine.getMarginsValue(), y = imageCombine.getMarginsValue(), imageWidth, imageHeight;
            int totalWidth = 0, totalHeight = 0, maxWidth = 0, minWidth = Integer.MAX_VALUE;
            int sizeType = imageCombine.getSizeType();
            if (sizeType == CombineSizeType.AlignAsBigger) {
                for (ImageFileInformation image : images) {
                    imageWidth = (int) image.getImage().getWidth();
                    if (imageWidth > maxWidth) {
                        maxWidth = imageWidth;
                    }
                }
            }
            if (sizeType == CombineSizeType.AlignAsSmaller) {
                for (ImageFileInformation image : images) {
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
                ImageFileInformation imageInfo = images.get(i);
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
                    FxmlImageTools.colorConvert(imageCombine.getBgColor()),
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
            List<ImageFileInformation> images, boolean isPart, boolean careTotal) {
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
                for (ImageFileInformation image : images) {
                    imageHeight = (int) image.getImage().getHeight();
                    if (imageHeight > maxHeight) {
                        maxHeight = imageHeight;
                    }
                }
            }
            if (sizeType == CombineSizeType.AlignAsSmaller) {
                for (ImageFileInformation image : images) {
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
                ImageFileInformation imageInfo = images.get(i);
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
                    FxmlImageTools.colorConvert(imageCombine.getBgColor()),
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

    public static javafx.scene.image.Image combineImages(List<ImageFileInformation> images,
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
                ImageFileInformation imageInfo = images.get(i);
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

    public static BufferedImage applyFilter(BufferedImage source, Kernel filter) {
        if (source == null) {
            return null;
        }
        if (filter == null) {
            return source;
        }
        ConvolveOp imageOp = new ConvolveOp(filter, ConvolveOp.EDGE_ZERO_FILL, null);
        return applyConvolveOp(source, imageOp);
    }

    public static BufferedImage applyConvolveOp(BufferedImage source, ConvolveOp imageOp) {
        if (source == null) {
            return null;
        }
        if (imageOp == null) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        imageOp.filter(source, target);
        return target;
    }

    public static Kernel makeGaussFilter(int radius) {
        if (radius < 1) {
            return null;
        }
        float sum = 0.0f;
        int width = radius * 2 + 1;
        int size = (int) Math.pow((float) (width), 2);
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.PI * twoSigmaSquare;
        float[] data = new float[size];
        int index = 0;
        float x, y;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                x = i * i;
                y = j * j;
                data[index] = (float) Math.exp(-(x + y) / twoSigmaSquare) / sigmaRoot;
                sum += data[index];
                index++;
            }
        }
        String v = "";
        for (int k = 0; k < size; k++) {
            data[k] = data[k] / sum;
            v += "  " + data[k];
        }
//        logger.debug(v);
        return new Kernel(width, width, data);
    }

    public static BufferedImage blurImage(BufferedImage source, int radius) {
//        float ninth = 1.0f / 9.0f;
//        float[] blurKernel = {
//            ninth, ninth, ninth,
//            ninth, ninth, ninth,
//            ninth, ninth, ninth
//        };
//        Kernel k = new Kernel(3, 3, blurKernel);

        Kernel k = makeGaussFilter(radius);
        BufferedImage target = applyFilter(source, k);
        return target;
    }

// https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html
    public static BufferedImage sharpenImage(BufferedImage source) {
        float[] sharpenKernel = {
            0.0f, -1.0f, 0.0f,
            -1.0f, 5.0f, -1.0f,
            0.0f, -1.0f, 0.0f
        };
        Kernel k = new Kernel(3, 3, sharpenKernel);

        BufferedImage target = applyFilter(source, k);
        return target;
    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html
    public static BufferedImage edgeDetect(BufferedImage source) {
        float[] edgeDetectKernel = {
            0.0f, -1.0f, 0.0f,
            -1.0f, 4.0f, -1.0f,
            0.0f, -1.0f, 0.0f
        };
        Kernel k = new Kernel(3, 3, edgeDetectKernel);

        BufferedImage target = applyFilter(source, k);
        return target;
    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html?page=2
    public static BufferedImage thresholding(BufferedImage source, int threshold, int smallValue, int bigValue) {
        try {
            short[] thresholdArray = new short[256];
            for (int i = 0; i < 256; i++) {
                if (i < threshold) {
                    thresholdArray[i] = (short) smallValue;
                } else {
                    thresholdArray[i] = (short) bigValue;
                }
            }
            BufferedImageOp thresholdingOp = new LookupOp(new ShortLookupTable(0, thresholdArray), null);
            return thresholdingOp.filter(source, null);
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html?page=2
    public static BufferedImage posterizing(BufferedImage source, int size) {
        try {
            short[] posterize = new short[256];
            for (int i = 0; i < 256; i++) {
                posterize[i] = (short) (i - (i % size));
            }
            BufferedImageOp posterizeOp = new LookupOp(new ShortLookupTable(0, posterize), null);
            return posterizeOp.filter(source, null);
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

}
