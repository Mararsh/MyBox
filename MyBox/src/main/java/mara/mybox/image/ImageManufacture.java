package mara.mybox.image;

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
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.shape.Line;
import javax.imageio.ImageIO;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageCombine.CombineSizeType;
import mara.mybox.image.ImageMosaic.MosaicType;
import mara.mybox.image.PixelBlend.ImagesBlendMode;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageManufacture {

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
                if (source.getColorModel() != null) {
                    return source.getColorModel().hasAlpha();
                } else {
                    return true;
                }
        }
    }

    // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4836466
    public static BufferedImage checkAlpha(BufferedImage source, String targetFormat) {
        if (targetFormat == null) {
            return source;
        }
        BufferedImage checked = source;
        if (CommonValues.NoAlphaImages.contains(targetFormat.toLowerCase())) {
            checked = ImageManufacture.removeAlpha(source);
        }
        return checked;
    }

    public static BufferedImage removeAlpha(BufferedImage source) {
        if (!hasAlpha(source)) {
            return source;
        }
        return ImageManufacture.removeAlpha(source, ImageColor.getAlphaColor());
    }

    public static BufferedImage removeAlpha(BufferedImage source, Color color) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            int colorPixel = color.getRGB();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, colorPixel);
                    } else {
                        target.setRGB(i, j, new Color(pixel, false).getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage#
    public static BufferedImage clone(BufferedImage source) {
        if (source == null) {
            return null;
        }
        try {
            ColorModel cm = source.getColorModel();
            Hashtable<String, Object> properties = null;
            String[] keys = source.getPropertyNames();
            if (keys != null) {
                properties = new Hashtable<>();
                for (String key : keys) {
                    properties.put(key, source.getProperty(key));
                }
            }
            return new BufferedImage(cm, source.copyData(null), cm.isAlphaPremultiplied(), properties)
                    .getSubimage(0, 0, source.getWidth(), source.getHeight());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // https://stackoverflow.com/questions/24038524/how-to-get-byte-from-javafx-imageview
    public static byte[] bytes(BufferedImage image) {
        byte[] bytes = null;
        try ( ByteArrayOutputStream stream = new ByteArrayOutputStream();) {
            ImageIO.write(image, "png", stream);
            bytes = stream.toByteArray();
        } catch (Exception e) {
        }
        return bytes;
    }

    public static boolean same(BufferedImage imageA, BufferedImage imageB) {
        if (imageA == null || imageB == null
                || imageA.getWidth() != imageB.getWidth()
                || imageA.getHeight() != imageB.getHeight()) {
            return false;
        }
        return Arrays.equals(SystemTools.MD5(imageA), SystemTools.MD5(imageB));
    }

    // This way may be more quicker than comparing digests
    public static boolean sameImage(BufferedImage imageA, BufferedImage imageB) {
        if (imageA == null || imageB == null
                || imageA.getWidth() != imageB.getWidth()
                || imageA.getHeight() != imageB.getHeight()) {
            return false;
        }
        for (int y = 0; y < imageA.getHeight(); y++) {
            for (int x = 0; x < imageA.getWidth(); x++) {
                if (imageA.getRGB(x, y) != imageB.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BufferedImage scaleImage(BufferedImage source, int width, int height,
            int dither, int antiAlias, int quality, int interpolation) {
        if (width <= 0 || height <= 0
                || (width == source.getWidth() && height == source.getHeight())) {
            return source;
        }
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage target = new BufferedImage(width, height, imageType);
        Graphics2D g = target.createGraphics();
        if (antiAlias == 1) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else if (antiAlias == 0) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        if (quality == 1) {
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        } else if (quality == 0) {
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        if (dither == 1) {
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        } else if (dither == 0) {
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        }
        switch (interpolation) {
            case 1:
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                break;
            case 4:
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                break;
            case 9:
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                break;
        }
        g.setBackground(CommonFxValues.TRANSPARENT);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return target;
    }

    public static BufferedImage scaleImageBySize(BufferedImage source, int width, int height) {
        return scaleImage(source, width, height, -1, -1, -1, -1);
    }

    public static BufferedImage scaleImageWidthKeep(BufferedImage source, int width) {
        return scaleImageWidthKeep(source, width, -1, -1, -1, -1);
    }

    public static BufferedImage scaleImageWidthKeep(BufferedImage source, int width,
            int dither, int antiAlias, int quality, int interpolation) {
        if (width <= 0 || width == source.getWidth()) {
            return source;
        }
        int height = source.getHeight() * width / source.getWidth();
        return scaleImage(source, width, height, dither, antiAlias, quality, interpolation);
    }

    public static BufferedImage scaleImageHeightKeep(BufferedImage source, int height) {
        return scaleImageHeightKeep(source, height, -1, -1, -1, -1);
    }

    public static BufferedImage scaleImageHeightKeep(BufferedImage source, int height,
            int dither, int antiAlias, int quality, int interpolation) {
        int width = source.getWidth() * height / source.getHeight();
        return scaleImage(source, width, height, dither, antiAlias, quality, interpolation);
    }

    public static BufferedImage scaleImageByScale(BufferedImage source, float scale) {
        return scaleImageByScale(source, scale, scale);
    }

    public static BufferedImage scaleImageByScale(BufferedImage source, float xscale, float yscale) {
        return scaleImageByScale(source, xscale, yscale, -1, -1, -1, -1);
    }

    public static BufferedImage scaleImageByScale(BufferedImage source, float scale,
            int dither, int antiAlias, int quality, int interpolation) {
        return scaleImageByScale(source, scale, scale, dither, antiAlias, quality, interpolation);
    }

    public static BufferedImage scaleImageByScale(BufferedImage source, float xscale, float yscale,
            int dither, int antiAlias, int quality, int interpolation) {
        int width = (int) (source.getWidth() * xscale);
        int height = (int) (source.getHeight() * yscale);
        return scaleImage(source, width, height, dither, antiAlias, quality, interpolation);
    }

    public static BufferedImage scaleImageLess(BufferedImage source, int size) {
        if (size <= 0) {
            return source;
        }
        float scale = size / (source.getWidth() * source.getHeight());
        if (scale >= 1) {
            return source;
        }
        return scaleImageByScale(source, scale);
    }

    public static int[] scaleValues(int sourceX, int sourceY, int newWidth, int newHeight, int keepRatioType) {
        int finalW = newWidth;
        int finalH = newHeight;
        if (keepRatioType != KeepRatioType.None) {
            double ratioW = (double) newWidth / sourceX;
            double ratioH = (double) newHeight / sourceY;
            if (ratioW != ratioH) {
                switch (keepRatioType) {
                    case KeepRatioType.BaseOnWidth:
                        finalH = (int) (ratioW * sourceY);
                        break;
                    case KeepRatioType.BaseOnHeight:
                        finalW = (int) (ratioH * sourceX);
                        break;
                    case KeepRatioType.BaseOnLarger:
                        if (ratioW > ratioH) {
                            finalH = (int) (ratioW * sourceY);
                        } else {
                            finalW = (int) (ratioH * sourceX);
                        }
                        break;
                    case KeepRatioType.BaseOnSmaller:
                        if (ratioW < ratioH) {
                            finalH = (int) (ratioW * sourceY);
                        } else {
                            finalW = (int) (ratioH * sourceX);
                        }
                        break;
                }
            }
        }
        int[] d = new int[2];
        d[0] = finalW;
        d[1] = finalH;
        return d;
    }

    public static BufferedImage scaleImage(BufferedImage source,
            int targetW, int targetH, boolean keepRatio, int keepType) {
        int finalW = targetW;
        int finalH = targetH;
        if (keepRatio) {
            int[] wh = ImageManufacture.scaleValues(source.getWidth(), source.getHeight(),
                    targetW, targetH, keepType);
            finalW = wh[0];
            finalH = wh[1];
        }
        return scaleImageBySize(source, finalW, finalH);
    }

    public static BufferedImage fitSize(BufferedImage source, int targetW, int targetH) {
        try {
            int[] wh = ImageManufacture.scaleValues(source.getWidth(), source.getHeight(),
                    targetW, targetH, KeepRatioType.BaseOnSmaller);
            int finalW = wh[0];
            int finalH = wh[1];
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(targetW, targetH, imageType);
            Graphics2D g = target.createGraphics();
            g.setBackground(CommonFxValues.TRANSPARENT);
            g.drawImage(source, (targetW - finalW) / 2, (targetH - finalH) / 2, finalW, finalH, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage addText(BufferedImage backImage, String text,
            Font font, Color color, int x, int y,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed,
            int shadow, int angle, boolean isOutline, boolean isVertical) {
        try {
            if (opacity > 1.0f || opacity < 0) {
                opacity = 1.0f;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            boolean noBlend = color.equals(CommonFxValues.TRANSPARENT);
            if (noBlend) {
                g.drawImage(backImage, 0, 0, width, height, null);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
            }
            float textOpacity = noBlend ? opacity : 1.0f;
            if (isVertical) {
                int ay = y;
                for (int i = 0; i < text.length(); ++i) {
                    String c = String.valueOf(text.charAt(i));
                    addText(g, c, font, color, x, ay, textOpacity, shadow, angle, isOutline);
                    ay += g.getFontMetrics().getStringBounds(c, g).getHeight();
                }
            } else {
                addText(g, text, font, color, x, y, textOpacity, shadow, angle, isOutline);
            }
            g.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void addText(Graphics2D g, String text,
            Font font, Color color, int x, int y,
            float opacity, int shadow, int angle, boolean isOutline) {
        try {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            g.rotate(Math.toRadians(angle), x, y);
            g.setColor(color.equals(CommonFxValues.TRANSPARENT) ? null : color);
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
            if (shadow > 0) {  // Not blurred. Can improve
                g.setColor(Color.GRAY);
                g.drawString(text, x + shadow, y + shadow);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(color);
            g.setFont(font);
            for (int i = 0; i < texts.size(); ++i) {
                g.drawString(texts.get(i), xs.get(i), ys.get(i));
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                    ah = picture.getHeight() * w / picture.getWidth();
                } else {
                    aw = picture.getWidth() * h / picture.getHeight();
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
            MyBoxLog.error(e.toString());
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
        if (bgColor.getRGB() == CommonFxValues.TRANSPARENT.getRGB()) {
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

    public static BufferedImage addShadowAlpha(BufferedImage source,
            int shadowWidth, Color shadowColor) {
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
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        shadowImage.setRGB(i, j, 0);
                        continue;
                    }
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
            Color bgColor = CommonFxValues.TRANSPARENT;
            g.setColor(bgColor);
            g.fillRect(0, 0, width + shadowWidth, height + shadowWidth);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.drawImage(shadowImage, shadowWidth, shadowWidth, null);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addShadowNoAlpha(BufferedImage source, int shadowWidth, Color shadowColor) {
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
            Color alphaColor = ImageColor.getAlphaColor();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        shadowImage.setRGB(i, j, alphaColor.getRGB());
                        continue;
                    }
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
                        newColor = ImageColor.blendAlpha(shadowColor, opocity, alphaColor);
                    }
                    shadowImage.setRGB(i, j, newColor.getRGB());
                }
            }

            BufferedImage target = new BufferedImage(width + shadowWidth, height + shadowWidth, imageType);
            Graphics2D g = target.createGraphics();
            g.setColor(alphaColor);
            g.fillRect(0, 0, width + shadowWidth, height + shadowWidth);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.drawImage(shadowImage, shadowWidth, shadowWidth, null);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage cutMargins(BufferedImage source, Color cutColor,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            if (cutColor.getRGB() == CommonFxValues.TRANSPARENT.getRGB()
                    && !hasAlpha(source)) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int top, bottom, left, right;
            int cutValue = cutColor.getRGB();
            if (cutTop) {
                top = -1;
                toploop:
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        if (source.getRGB(i, j) != cutValue) {
                            top = j;
                            break toploop;
                        }
                    }
                }
                if (top < 0) {
                    return null;
                }
            } else {
                top = 0;
            }
            if (cutBottom) {
                bottom = - 1;
                bottomploop:
                for (int j = height - 1; j >= 0; --j) {
                    for (int i = 0; i < width; ++i) {
                        if (source.getRGB(i, j) != cutValue) {
                            bottom = j;
                            break bottomploop;
                        }
                    }
                }
                if (bottom < 0) {
                    return null;
                }
            } else {
                bottom = height - 1;
            }
            if (cutLeft) {
                left = -1;
                leftloop:
                for (int i = 0; i < width; ++i) {
                    for (int j = 0; j < height; ++j) {
                        if (source.getRGB(i, j) != cutValue) {
                            left = i;
                            break leftloop;
                        }
                    }
                }
                if (left < 0) {
                    return null;
                }
            } else {
                left = 0;
            }
            if (cutRight) {
                right = - 1;
                rightloop:
                for (int i = width - 1; i >= 0; --i) {
                    for (int j = 0; j < height; ++j) {
                        if (source.getRGB(i, j) != cutValue) {
                            right = i;
                            break rightloop;
                        }
                    }
                }
                if (right < 0) {
                    return null;
                }
            } else {
                right = width - 1;
            }
//            int w = right + 1 - left;
//            int h = bottom + 1 - top;
//            BufferedImage target = source.getSubimage(left, top, w, h); // This way works on Java8 but messes on Java 12
            BufferedImage target = cropOutside(source, left, top, right, bottom);
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
            if (addColor.getRGB() == CommonFxValues.TRANSPARENT.getRGB()) {
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blurMarginsAlpha(BufferedImage source,
            int blurWidth, boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            float iOpocity, jOpacity, opocity;
            Color newColor;
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, 0);
                        continue;
                    }
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage blurMarginsNoAlpha(BufferedImage source,
            int blurWidth, boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_RGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            float iOpocity, jOpacity, opocity;
            Color alphaColor = ImageColor.getAlphaColor();
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, alphaColor.getRGB());
                        continue;
                    }
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
                        Color color = ImageColor.blendAlpha(new Color(pixel), opocity, alphaColor);
                        target.setRGB(i, j, color.getRGB());
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
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
                newWidth = newHeight = Math.max(width, height);
                break;
            default:
                newWidth = newHeight = 2 * Math.max(width, height);
                isSkew = true;
                break;
        }
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D g = target.createGraphics();
        Color bgColor = CommonFxValues.TRANSPARENT;
        g.setBackground(bgColor);
        if (!isSkew) {
            g.rotate(Math.toRadians(angle), newWidth / 2, newHeight / 2);
            g.drawImage(source, 0, 0, null);
        } else {
            g.rotate(Math.toRadians(angle), width, height);
            g.drawImage(source, width / 2, height / 2, null);
        }
        g.dispose();
        target = ImageManufacture.cutMargins(target, bgColor, true, true, true, true);
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
            for (int j = 0; j < height; ++j) {
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
            MyBoxLog.error(e.toString());
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
            for (int i = 0; i < width; ++i) {
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
            MyBoxLog.error(e.toString());
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
            Color bgColor = CommonFxValues.TRANSPARENT;
            g.setBackground(bgColor);
            if (shearX < 0) {
                g.translate(width / 2, 0);
            }
            g.shear(shearX, shearY);
            g.drawImage(source, 0, 0, null);
            g.dispose();

            target = ImageManufacture.cutMargins(target, bgColor, true, true, true, true);
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    public static BufferedImage cropOutside(BufferedImage source, DoubleShape shape, Color bgColor) {
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
            MyBoxLog.error(e.toString());
            return source;
        }
    }

    public static BufferedImage cropInside(BufferedImage source, DoubleShape shape, Color bgColor) {
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
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    if (shape.include(i, j)) {
                        target.setRGB(i, j, bgPixel);
                    } else {
                        target.setRGB(i, j, source.getRGB(i, j));
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return source;
        }
    }

    public static BufferedImage cropOutside(BufferedImage source, DoubleRectangle rectangle) {
        if (rectangle == null) {
            return source;
        }
        return cropOutside(source, rectangle.getSmallX(), rectangle.getSmallY(),
                rectangle.getBigX(), rectangle.getBigY());
    }

    public static BufferedImage cropOutside(BufferedImage source, double x1, double y1, double x2, double y2) {
        return cropOutside(source, new DoubleRectangle(x1, y1, x2, y2), Color.WHITE);
    }

    public static BufferedImage mergeImagesVertical(List<File> files, int width, int height) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        try {
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            int y = 0;
            for (File file : files) {
                BufferedImage image = ImageFileReaders.readImage(file);
                if (image == null) {
                    continue;
                }
                int imageWidth = (int) image.getWidth();
                int imageHeight = (int) image.getHeight();
                g.drawImage(image, 0, y, imageWidth, imageHeight, null);
                y += imageHeight;
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage sample(BufferedImage source, DoubleRectangle rectangle, int xscale, int yscale) {
        try {
            if (rectangle == null) {
                return scaleImageByScale(source, xscale, yscale);
            }
            int realXScale = xscale > 0 ? xscale : 1;
            int realYScale = yscale > 0 ? yscale : 1;
            BufferedImage bufferedImage = cropOutside(source, rectangle);
            int width = bufferedImage.getWidth() / realXScale;
            int height = bufferedImage.getHeight() / realYScale;
            bufferedImage = scaleImageBySize(bufferedImage, width, height);
            return bufferedImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return source;
        }
    }

    public static BufferedImage sample(BufferedImage source,
            int x1, int y1, int x2, int y2, int xscale, int yscale) {
        if (x1 >= x2 || y1 >= y2 || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
            return null;
        }
        return sample(source, new DoubleRectangle(x1, y1, x2, y2), xscale, yscale);
    }

    public static javafx.scene.image.Image combineSingleColumn(ImageCombine imageCombine,
            List<ImageInformation> imageInfos, boolean isPart, boolean careTotal) {
        if (imageCombine == null || imageInfos == null) {
            return null;
        }
        try {
            int x = imageCombine.getMarginsValue(), y = x, imageWidth, imageHeight;
            int totalWidth = 0, totalHeight = 0, maxWidth = 0, minWidth = Integer.MAX_VALUE;
            int sizeType = imageCombine.getSizeType();
            if (sizeType == CombineSizeType.AlignAsBigger) {
                for (ImageInformation imageInfo : imageInfos) {
                    imageWidth = imageInfo.getWidth();
                    if (imageWidth > maxWidth) {
                        maxWidth = imageWidth;
                    }
                }
            } else if (sizeType == CombineSizeType.AlignAsSmaller) {
                for (ImageInformation imageInfo : imageInfos) {
                    imageWidth = imageInfo.getWidth();
                    if (imageWidth < minWidth) {
                        minWidth = imageWidth;
                    }
                }
            }
            List<Integer> xs = new ArrayList<>();
            List<Integer> ys = new ArrayList<>();
            List<Integer> widths = new ArrayList<>();
            List<Integer> heights = new ArrayList<>();
            for (ImageInformation imageInfo : imageInfos) {
                imageWidth = imageInfo.getWidth();
                imageHeight = imageInfo.getHeight();
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

            javafx.scene.image.Image newImage = combineImages(imageInfos, totalWidth, totalHeight,
                    FxmlImageManufacture.toAwtColor(imageCombine.getBgColor()),
                    xs, ys, widths, heights,
                    imageCombine.getTotalWidthValue(), imageCombine.getTotalHeightValue(),
                    careTotal && (sizeType == CombineSizeType.TotalWidth),
                    careTotal && (sizeType == CombineSizeType.TotalHeight));

            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                for (ImageInformation imageInfo : images) {
                    imageHeight = imageInfo.getHeight();
                    if (imageHeight > maxHeight) {
                        maxHeight = imageHeight;
                    }
                }
            } else if (sizeType == CombineSizeType.AlignAsSmaller) {
                for (ImageInformation imageInfo : images) {
                    imageHeight = imageInfo.getHeight();
                    if (imageHeight < minHeight) {
                        minHeight = imageHeight;
                    }
                }
            }
            List<Integer> xs = new ArrayList<>();
            List<Integer> ys = new ArrayList<>();
            List<Integer> widths = new ArrayList<>();
            List<Integer> heights = new ArrayList<>();
            for (ImageInformation imageInfo : images) {
                imageWidth = imageInfo.getWidth();
                imageHeight = imageInfo.getHeight();
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
                    FxmlImageManufacture.toAwtColor(imageCombine.getBgColor()),
                    xs, ys, widths, heights,
                    imageCombine.getTotalWidthValue(), imageCombine.getTotalHeightValue(),
                    careTotal && (sizeType == CombineSizeType.TotalWidth),
                    careTotal && (sizeType == CombineSizeType.TotalHeight));

            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static javafx.scene.image.Image combineImages(
            List<ImageInformation> imageInfos,
            int totalWidth, int totalHeight, Color bgColor,
            List<Integer> xs, List<Integer> ys, List<Integer> widths, List<Integer> heights,
            int trueTotalWidth, int trueTotalHeight,
            boolean isTotalWidth, boolean isTotalHeight) {
        if (imageInfos == null || xs == null || ys == null || widths == null || heights == null) {
            return null;
        }
        try {
            BufferedImage target = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, totalWidth, totalHeight);

            for (int i = 0; i < imageInfos.size(); ++i) {
                ImageInformation imageInfo = imageInfos.get(i);
                javafx.scene.image.Image image = imageInfo.loadImage();
                BufferedImage source = SwingFXUtils.fromFXImage(image, null);
                g.drawImage(source, xs.get(i), ys.get(i), widths.get(i), heights.get(i), null);
            }

            if (isTotalWidth) {
                target = scaleImageBySize(target, trueTotalWidth, (trueTotalWidth * totalHeight) / totalWidth);
            } else if (isTotalHeight) {
                target = scaleImageBySize(target, (trueTotalHeight * totalWidth) / totalHeight, trueTotalHeight);
            }

            javafx.scene.image.Image newImage = SwingFXUtils.toFXImage(target, null);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage drawRectangle(BufferedImage backImage,
            DoubleRectangle rect, Color strokeColor, int strokeWidth,
            int arcWidth, boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (rect == null || strokeColor == null || !rect.isValid()) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int x1 = (int) Math.round(rect.getSmallX());
            int y1 = (int) Math.round(rect.getSmallY());
            int x2 = (int) Math.round(rect.getBigX());
            int y2 = (int) Math.round(rect.getBigY());
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            boolean noBlend = (!isFill && strokeColor.equals(CommonFxValues.TRANSPARENT))
                    || (isFill && fillColor.equals(CommonFxValues.TRANSPARENT));
            if (noBlend) {
                g.drawImage(backImage, 0, 0, width, height, null);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                g.setComposite(ac);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
            }
            if (strokeWidth > 0) {
                if (strokeColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(strokeColor);
                }
                BasicStroke stroke;
                if (dotted) {
                    stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            1f, new float[]{strokeWidth, strokeWidth}, 0f);
                } else {
                    stroke = new BasicStroke(strokeWidth);
                }
                g.setStroke(stroke);
                if (arcWidth > 0) {
                    int a = Math.max(0, Math.min(height - 1, Math.round(arcWidth)));
                    g.drawRoundRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1, a, a);
                } else {
                    g.drawRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
                }
            }
            if (isFill) {
                if (fillColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(fillColor);
                }
                if (arcWidth > 0) {
                    int a = Math.max(0, Math.min(height - 1, Math.round(arcWidth)));
                    g.fillRoundRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1, a, a);
                } else {
                    g.fillRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
                }
            }
            g.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawCircle(BufferedImage backImage,
            DoubleCircle circle, Color strokeColor, int strokeWidth,
            boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (circle == null || strokeColor == null || !circle.isValid()) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int x = (int) Math.round(circle.getCenterX());
            int y = (int) Math.round(circle.getCenterY());
            int r = (int) Math.round(circle.getRadius());
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            boolean noBlend = (!isFill && strokeColor.equals(CommonFxValues.TRANSPARENT))
                    || (isFill && fillColor.equals(CommonFxValues.TRANSPARENT));
            if (noBlend) {
                g.drawImage(backImage, 0, 0, width, height, null);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                g.setComposite(ac);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
            }
            if (strokeWidth > 0) {
                if (strokeColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(strokeColor);
                }
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
                if (fillColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(fillColor);
                }
                g.fillOval(x - r, y - r, 2 * r, 2 * r);
            }
            g.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawEllipse(BufferedImage backImage,
            DoubleEllipse ellipse, Color strokeColor, int strokeWidth,
            boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (ellipse == null || strokeColor == null || !ellipse.isValid()) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int x = (int) Math.round(ellipse.getCenterX());
            int y = (int) Math.round(ellipse.getCenterY());
            int rx = (int) Math.round(ellipse.getRadiusX());
            int ry = (int) Math.round(ellipse.getRadiusY());
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            boolean noBlend = (!isFill && strokeColor.equals(CommonFxValues.TRANSPARENT))
                    || (isFill && fillColor.equals(CommonFxValues.TRANSPARENT));
            if (noBlend) {
                g.drawImage(backImage, 0, 0, width, height, null);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                g.setComposite(ac);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
            }
            if (strokeWidth > 0) {
                if (strokeColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(strokeColor);
                }
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
                if (fillColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(fillColor);
                }
                g.fillOval(x - rx, y - ry, 2 * rx, 2 * ry);
            }
            g.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawPolygon(BufferedImage backImage,
            DoublePolygon polygonData, Color strokeColor, int strokeWidth,
            boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (polygonData == null || strokeColor == null || polygonData.getSize() <= 2) {
                return backImage;
            }
            Map<String, int[]> xy = polygonData.getIntXY();
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            boolean noBlend = (!isFill && strokeColor.equals(CommonFxValues.TRANSPARENT))
                    || (isFill && fillColor.equals(CommonFxValues.TRANSPARENT));
            if (noBlend) {
                g.drawImage(backImage, 0, 0, width, height, null);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                g.setComposite(ac);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
            }
            if (strokeWidth > 0) {
                if (strokeColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(strokeColor);
                }
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
                if (fillColor.getRGB() == 0) {
                    g.setColor(null);
                } else {
                    g.setColor(fillColor);
                }
                g.fillPolygon(xy.get("x"), xy.get("y"), polygonData.getSize());
            }
            g.dispose();
            if (noBlend) {
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawPolyline(BufferedImage backImage,
            DoublePolyline polylineData, Color strokeColor, int strokeWidth,
            boolean dotted, ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (polylineData == null || strokeColor == null
                    || polylineData.getSize() < 2 || strokeWidth < 1) {
                return backImage;
            }
            Map<String, int[]> xy = polylineData.getIntXY();
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            if (strokeColor.equals(CommonFxValues.TRANSPARENT)) {
                g.drawImage(backImage, 0, 0, width, height, null);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                g.setComposite(ac);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
            }
            if (strokeColor.getRGB() == 0) {
                g.setColor(null);
            } else {
                g.setColor(strokeColor);
            }
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
            if (strokeColor.equals(CommonFxValues.TRANSPARENT)) {
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage blendImages(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        if (foreImage == null || backImage == null || blendMode == null) {
            return null;
        }
        BufferedImage target = ImageBlend.blendImages(foreImage, backImage,
                x, y, blendMode, opacity, orderReversed);
        if (target == null) {
            target = foreImage;
        }
        return target;
    }

    public static BufferedImage drawLines(BufferedImage backImage,
            DoublePolyline polylineData, Color strokeColor, int strokeWidth,
            boolean dotted, ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (polylineData == null || strokeColor == null
                    || polylineData.getSize() < 2 || strokeWidth < 1) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            if (strokeColor.equals(CommonFxValues.TRANSPARENT)) {
                g.drawImage(backImage, 0, 0, width, height, null);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                g.setComposite(ac);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
            }
            if (strokeColor.getRGB() == 0) {
                g.setColor(null);
            } else {
                g.setColor(strokeColor);
            }
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
            if (strokeColor.equals(CommonFxValues.TRANSPARENT)) {
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawLines(BufferedImage backImage,
            DoubleLines penData, Color strokeColor, int strokeWidth,
            boolean dotted, ImagesBlendMode blendMode, float opacity, boolean orderReversed) {
        try {
            if (penData == null || strokeColor == null
                    || penData.getPointsSize() == 0 || strokeWidth < 1) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = backImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage foreImage = new BufferedImage(width, height, imageType);
            Graphics2D g = foreImage.createGraphics();
            if (strokeColor.getRGB() == 0) {
                g.drawImage(backImage, 0, 0, width, height, null);
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
                g.setComposite(ac);
                g.setColor(null);
            } else {
                g.setBackground(CommonFxValues.TRANSPARENT);
                g.setColor(strokeColor);
            }
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
            if (strokeColor.getRGB() == 0) {
                MyBoxLog.console(strokeColor.getRGB());
                return foreImage;
            } else {
                return blendImages(foreImage, backImage, 0, 0, blendMode, opacity, orderReversed);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawErase(BufferedImage srcImage, DoubleLines penData, int strokeWidth) {
        try {
            if (penData == null || penData.getPointsSize() == 0 || strokeWidth < 1) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            int imageType = srcImage.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage newImage = new BufferedImage(width, height, imageType);
            Graphics2D g = newImage.createGraphics();
            g.drawImage(srcImage, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(null);
            BasicStroke stroke = new BasicStroke(strokeWidth);
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
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return srcImage;
        }
    }

    protected static int mosaic(BufferedImage source, int imageWidth,
            int imageHeight,
            int x, int y, MosaicType type, int intensity) {
        int newColor;
        if (type == MosaicType.Mosaic) {
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
        double d = (x - line.getStartX()) * (line.getStartY() - line.getEndY())
                - ((line.getStartX() - line.getEndX()) * (y - line.getStartY()));
        return Math.abs(d) < 0.0001
                && (x >= Math.min(line.getStartX(), line.getEndX())
                && x <= Math.max(line.getStartX(), line.getEndX()))
                && (y >= Math.min(line.getStartY(), line.getEndY()))
                && (y <= Math.max(line.getStartY(), line.getEndY()));
    }

    public static BufferedImage drawMosaic(BufferedImage source,
            DoubleLines penData, MosaicType mosaicType, int strokeWidth) {
        try {
            if (penData == null || mosaicType == null
                    || penData.getPointsSize() == 0 || strokeWidth < 1) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
//            BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D g = mask.createGraphics();
//            g.setColor(CommonImageValues.TRANSPARENT);
//            g.fillRect(0, 0, width, height);
//            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
//            g.setComposite(ac);
//            g.setColor(Color.BLACK);
//            g.setStroke(new BasicStroke(strokeWidth));
//            int lastx, lasty = -1, thisx, thisy;
//            for (List<DoublePoint> lineData : penData.getLines()) {
//                lastx = -1;
//                for (DoublePoint p : lineData) {
//                    thisx = (int) Math.round(p.getX());
//                    thisy = (int) Math.round(p.getY());
//                    if (lastx >= 0) {
//                        g.drawLine(lastx, lasty, thisx, thisy);
//                    }
//                    lastx = thisx;
//                    lasty = thisy;
//                }
//            }
//            g.dispose();

            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D gt = target.createGraphics();
            gt.drawImage(source, 0, 0, width, height, null);
            gt.dispose();
            List<Line> dlines = penData.directLines();
            int pixel;
            for (Line line : dlines) {
                int x1 = Math.min(width, Math.max(0, (int) line.getStartX()));
                int y1 = Math.min(height, Math.max(0, (int) line.getStartY()));
                int x2 = Math.min(width, Math.max(0, (int) line.getEndX()));
                int y2 = Math.min(height, Math.max(0, (int) line.getEndY()));
//                MyBoxLog.debug(x1 + "," + y1 + "    " + x2 + "," + y2);
                if (x1 == x2) {
                    if (y2 > y1) {
//                        MyBoxLog.debug(Math.max(0, x1 - strokeWidth) + "," + Math.min(width, x1 + strokeWidth));
                        for (int x = Math.max(0, x1 - strokeWidth);
                                x <= Math.min(width, x1 + strokeWidth); x++) {

                            for (int y = y1; y <= y2; y++) {

                                pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                                target.setRGB(x, y, pixel);
                            }
                        }
                    } else {
//                        MyBoxLog.debug(Math.max(0, x1 - strokeWidth) + "," + Math.min(width, x1 + strokeWidth));
                        for (int x = Math.max(0, x1 - strokeWidth);
                                x <= Math.min(width, x1 + strokeWidth); x++) {
                            for (int y = y2; y <= y1; y++) {
                                pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                                target.setRGB(x, y, pixel);
                            }
                        }
                    }

                } else if (x2 > x1) {
//                    MyBoxLog.debug(x1 + "," + x2);
                    for (int x = x1; x <= x2; x++) {
                        int y0 = (x - x1) * (y2 - y1) / (x2 - x1) + y1;
                        int offset = (int) (x / (strokeWidth * Math.sqrt(x * x + y0 * y0)));
//                        MyBoxLog.debug(y0 + "," + offset);
                        for (int y = Math.max(0, y0 - offset);
                                y <= Math.min(height, y0 + offset); y++) {
                            pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                            target.setRGB(x, y, pixel);
                        }
                    }

                } else {
//                    MyBoxLog.debug(x2 + "," + x1);
                    for (int x = x2; x <= x1; x++) {
                        int y0 = (x - x2) * (y1 - y2) / (x1 - x2) + y2;
                        int offset = (int) (x / (strokeWidth * Math.sqrt(x * x + y0 * y0)));
//                        MyBoxLog.debug(y0 + "," + offset);
                        for (int y = Math.max(0, y0 - offset);
                                y <= Math.min(height, y0 + offset); y++) {
                            pixel = mosaic(source, width, height, x, y, mosaicType, strokeWidth);
                            target.setRGB(x, y, pixel);
                        }
                    }
                }

            }

//            int pixel, white = Color.BLACK.getRGB();
//            for (int j = 0; j < height; ++j) {
//                for (int i = 0; i < width; ++i) {
//                    pixel = source.getRGB(i, j);
//                    if (pixel == 0 || mask.getRGB(i, j) == 0) {
//                        continue;
//                    }
////                    pixel = mosaic(source, width, height, i, j, mosaicType, strokeWidth);
//                    target.setRGB(i, j, pixel);
//                }
//            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return source;
        }
    }

    public static BufferedImage[] extractAlpha(BufferedImage source) {
        try {
            if (source == null) {
                return null;
            }
            BufferedImage[] bfs = new BufferedImage[2];
            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage noAlphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Color color, newColor;
            int pixel;
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    pixel = source.getRGB(i, j);
                    color = new Color(pixel, true);

                    newColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
                    noAlphaImage.setRGB(i, j, newColor.getRGB());

                    newColor = new Color(0, 0, 0, color.getAlpha());
                    alphaImage.setRGB(i, j, newColor.getRGB());
                }
            }
            bfs[0] = noAlphaImage;
            bfs[1] = alphaImage;
            return bfs;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage extractAlphaOnly(BufferedImage source) {
        try {
            if (source == null) {
                return null;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Color color, newColor;
            int pixel;
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i) {
                    pixel = source.getRGB(i, j);
                    color = new Color(pixel, true);
                    newColor = new Color(0, 0, 0, color.getAlpha());
                    alphaImage.setRGB(i, j, newColor.getRGB());
                }
            }
            return alphaImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage[] outline(BufferedImage srcImage,
            DoubleRectangle rect, int targetWidth, int targetHeight,
            boolean keepRatio,
            Color bgColor, boolean exclude) {
        try {
            if (srcImage == null) {
                return null;
            }
            BufferedImage scaledImage = scaleImage(srcImage, (int) rect.getWidth(), (int) rect.getHeight(),
                    keepRatio, ImageManufacture.KeepRatioType.BaseOnWidth);
            int offsetX = (int) rect.getSmallX();
            int offsetY = (int) rect.getSmallY();
            int scaledWidth = scaledImage.getWidth();
            int scaledHeight = scaledImage.getHeight();
            int width = offsetX >= 0
                    ? Math.max(targetWidth, scaledWidth + offsetX)
                    : Math.max(targetWidth - offsetX, scaledWidth);
            int height = offsetY >= 0
                    ? Math.max(targetHeight, scaledHeight + offsetY)
                    : Math.max(targetHeight - offsetY, scaledHeight);
            int startX = offsetX >= 0 ? offsetX : 0;
            int startY = offsetY >= 0 ? offsetY : 0;
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            if (!exclude) {
                g.setColor(bgColor);
            } else {
                g.setColor(CommonFxValues.TRANSPARENT);
            }
            g.fillRect(0, 0, width, height);
            int pixel, bgPixel = bgColor.getRGB();
            for (int j = 0; j < scaledHeight; ++j) {
                for (int i = 0; i < scaledWidth; ++i) {
                    pixel = scaledImage.getRGB(i, j);
                    if (!exclude) {
                        if (pixel == 0) {
                            target.setRGB(i + startX, j + startY, bgPixel);
                        } else {
                            target.setRGB(i + startX, j + startY, 0);
                        }
                    } else {
                        if (pixel == 0) {
                            target.setRGB(i + startX, j + startY, 0);
                        } else {
                            target.setRGB(i + startX, j + startY, bgPixel);
                        }
                    }
                }
            }
            g.dispose();
            BufferedImage[] ret = new BufferedImage[2];
            ret[0] = scaledImage;
            ret[1] = target;
            return ret;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addAlpha(BufferedImage source,
            BufferedImage alpha, boolean isPlus) {
        try {
            if (source == null || alpha == null
                    || !alpha.getColorModel().hasAlpha()) {
                return source;
            }
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            int alphaWidth = alpha.getWidth();
            int alphaHeight = alpha.getHeight();
            boolean addAlpha = isPlus && source.getColorModel().hasAlpha();
            BufferedImage target = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);
            Color sourceColor, alphaColor, newColor;
            int alphaValue;
            for (int j = 0; j < sourceHeight; ++j) {
                for (int i = 0; i < sourceWidth; ++i) {

                    if (i < alphaWidth && j < alphaHeight) {
                        sourceColor = new Color(source.getRGB(i, j), addAlpha);
                        alphaColor = new Color(alpha.getRGB(i, j), true);
                        alphaValue = alphaColor.getAlpha();
                        if (addAlpha) {
                            alphaValue = Math.min(255, alphaValue + sourceColor.getAlpha());
                        }
                        newColor = new Color(sourceColor.getRed(),
                                sourceColor.getGreen(), sourceColor.getBlue(), alphaValue);
                        target.setRGB(i, j, newColor.getRGB());

                    } else {
                        target.setRGB(i, j, source.getRGB(i, j));
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addAlpha(BufferedImage source,
            float opacity, boolean isPlus) {
        try {
            if (source == null || opacity < 0) {
                return source;
            }
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            boolean addAlpha = isPlus && source.getColorModel().hasAlpha();
            BufferedImage target = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);
            Color sourceColor, newColor;
            int opacityValue = Math.min(255, Math.round(opacity * 255));
            for (int j = 0; j < sourceHeight; ++j) {
                for (int i = 0; i < sourceWidth; ++i) {
                    sourceColor = new Color(source.getRGB(i, j), addAlpha);
                    if (addAlpha) {
                        opacityValue = Math.min(255, opacityValue + sourceColor.getAlpha());
                    }
                    newColor = new Color(sourceColor.getRed(),
                            sourceColor.getGreen(), sourceColor.getBlue(), opacityValue);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static BufferedImage premultipliedAlpha(BufferedImage source,
            boolean removeAlpha) {
        try {
            if (source == null || !hasAlpha(source)
                    || (source.isAlphaPremultiplied() && !removeAlpha)) {
                return source;
            }
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();
            int imageType;
            if (removeAlpha) {
                imageType = BufferedImage.TYPE_INT_RGB;
            } else {
                imageType = BufferedImage.TYPE_INT_ARGB_PRE;
            }
            BufferedImage target = new BufferedImage(sourceWidth, sourceHeight, imageType);
            Color sourceColor, newColor, bkColor = ImageColor.getAlphaColor();
            int bkPixel = bkColor.getRGB();
            for (int j = 0; j < sourceHeight; ++j) {
                for (int i = 0; i < sourceWidth; ++i) {
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        target.setRGB(i, j, bkPixel);
                        continue;
                    }
                    sourceColor = new Color(pixel, true);
                    newColor = ImageColor.blendAlpha(sourceColor, sourceColor.getAlpha(), bkColor, !removeAlpha);
                    target.setRGB(i, j, newColor.getRGB());
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return source;
        }
    }

    public static BufferedImage premultipliedAlpha2(BufferedImage source,
            boolean removeAlpha) {
        try {
            if (source == null || !hasAlpha(source)
                    || (source.isAlphaPremultiplied() && !removeAlpha)) {
                return source;
            }
            BufferedImage target = clone(source);
            target.coerceData(true);
            if (removeAlpha) {
                target = removeAlpha(target);
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return source;
        }
    }

    public static BufferedImage drawHTML(BufferedImage backImage,
            BufferedImage html, DoubleRectangle bkRect, Color bkColor, float bkOpacity, int bkarc,
            int rotate, int margin) {
        try {
            if (html == null || backImage == null || bkRect == null) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.setBackground(CommonFxValues.TRANSPARENT);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g.drawImage(backImage, 0, 0, null);

            g.rotate(Math.toRadians(rotate),
                    bkRect.getSmallX() + bkRect.getWidth() / 2,
                    bkRect.getSmallY() + bkRect.getHeight() / 2);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bkOpacity));
            g.setColor(bkColor);
            g.fillRoundRect((int) bkRect.getSmallX(), (int) bkRect.getSmallY(),
                    (int) bkRect.getWidth(), (int) bkRect.getHeight(), bkarc, bkarc);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.setColor(CommonFxValues.TRANSPARENT);
            g.drawImage(html, (int) bkRect.getSmallX() + margin, (int) bkRect.getSmallY() + margin,
                    (int) bkRect.getWidth() - 2 * margin, (int) bkRect.getHeight() - 2 * margin,
                    null);

            g.dispose();

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawHTML2(BufferedImage backImage,
            BufferedImage html,
            DoubleRectangle bkRect, Color bkColor, float bkOpacity, int bkarc,
            int rotate, int margin) {
        try {
            if (html == null || backImage == null || bkRect == null) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.setBackground(CommonFxValues.TRANSPARENT);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g.drawImage(backImage, 0, 0, null);

            g.rotate(Math.toRadians(rotate),
                    bkRect.getSmallX() + bkRect.getWidth() / 2,
                    bkRect.getSmallY() + bkRect.getHeight() / 2);
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bkOpacity));
//            g.setColor(bkColor);
//            g.fillRoundRect((int) bkRect.getSmallX(), (int) bkRect.getSmallY(),
//                    (int) bkRect.getWidth(), (int) bkRect.getHeight(), bkarc, bkarc);
//
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.setColor(CommonFxValues.TRANSPARENT);
//            g.drawImage(html, (int) bkRect.getSmallX() + margin, (int) bkRect.getSmallY() + margin,
//                    (int) bkRect.getWidth() - 2 * margin, (int) bkRect.getHeight() - 2 * margin,
//                    null);

            g.drawImage(html, (int) bkRect.getSmallX(), (int) bkRect.getSmallY(),
                    (int) bkRect.getWidth(), (int) bkRect.getHeight(),
                    null);

            g.dispose();

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

    public static BufferedImage drawHTML(BufferedImage backImage,
            BufferedImage html,
            int htmlX, int htmlY, int htmlWdith, int htmlHeight) {
        try {
            if (html == null || backImage == null) {
                return backImage;
            }
            int width = backImage.getWidth();
            int height = backImage.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.setBackground(CommonFxValues.TRANSPARENT);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g.drawImage(backImage, 0, 0, null);

//            g.rotate(Math.toRadians(rotate),
//                    bkRect.getSmallX() + bkRect.getWidth() / 2,
//                    bkRect.getSmallY() + bkRect.getHeight() / 2);
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bkOpacity));
//            g.setColor(bkColor);
//            g.fillRoundRect((int) bkRect.getSmallX(), (int) bkRect.getSmallY(),
//                    (int) bkRect.getWidth(), (int) bkRect.getHeight(), bkarc, bkarc);
//
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.setColor(CommonFxValues.TRANSPARENT);
//            g.drawImage(html, (int) bkRect.getSmallX() + margin, (int) bkRect.getSmallY() + margin,
//                    (int) bkRect.getWidth() - 2 * margin, (int) bkRect.getHeight() - 2 * margin,
//                    null);

            g.drawImage(html, htmlX, htmlY, htmlWdith, htmlHeight, null);

            g.dispose();

            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return backImage;
        }
    }

}
