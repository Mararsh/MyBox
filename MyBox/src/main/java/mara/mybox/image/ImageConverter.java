package mara.mybox.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import static mara.mybox.objects.CommonValues.AlphaColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageConverter {

    private static final Logger logger = LogManager.getLogger();

    public static BufferedImage toBufferedImage(Image img, int colorSpace) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), colorSpace);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    public static class KeepRatioType {

        public static final int BaseOnWidth = 0;
        public static final int BaseOnHeight = 1;
        public static final int BaseOnLarger = 2;
        public static final int BaseOnSmaller = 3;
        public static final int None = 9;

    }

    public static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        BufferedImage target = new BufferedImage(width, height, source.getType());
        Graphics2D g = target.createGraphics();
        if (source.isAlphaPremultiplied() || source.getType() == BufferedImage.TYPE_INT_ARGB) {
            g.setBackground(AlphaColor);
        } else {
            g.setBackground(Color.WHITE);
        }
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return target;
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

    public static BufferedImage RemoveAlpha(BufferedImage source) {
        try {
            if (!source.isAlphaPremultiplied()) {
                return source;
            }
            BufferedImage imageRGB = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.OPAQUE);
            Graphics2D graphics = imageRGB.createGraphics();
            graphics.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);
            return imageRGB;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage ReplaceAlphaAsBlack(BufferedImage source) {
        return RemoveAlpha(source);
    }

    public static BufferedImage ReplaceAlphaAsWhite(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage target = new BufferedImage(width, height, BufferedImage.OPAQUE);
            int alpha = AlphaColor.getRGB();
            int white = Color.WHITE.getRGB();
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int color = source.getRGB(i, j);
                    if (alpha == color) {
                        target.setRGB(i, j, white);
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
        int imageType = source.getType();
        BufferedImage target = new BufferedImage(newWidth, newHeight, source.getType());
        Graphics2D g = target.createGraphics();
        g.setBackground(AlphaColor);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (!isSkew) {
            g.rotate(Math.toRadians(angle), newWidth / 2, newHeight / 2);
            g.drawImage(source, 0, 0, null);
        } else {
            g.rotate(Math.toRadians(angle), width, height);
            g.drawImage(source, width / 2, height / 2, null);
        }
        g.dispose();
//        logger.debug("Rotated: " + newWidth + ", " + newHeight);
        Color alphaColor;
        if (source.isAlphaPremultiplied() || source.getType() == BufferedImage.TYPE_INT_ARGB) {
            alphaColor = AlphaColor;
        } else {
            alphaColor = Color.WHITE;
        }
        target = cutEdges(target, alphaColor, true, true, true, true);
        return target;
    }

    public static BufferedImage cutEdges(BufferedImage source, Color cutColor,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
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

            BufferedImage target = new BufferedImage(right - left + 1, bottom - top + 1, source.getType());
            int w, h = 0;
            for (int j = top; j <= bottom; j++, h++) {
                w = 0;
                for (int i = left; i <= right; i++, w++) {
                    int rgb = source.getRGB(i, j);
                    target.setRGB(w, h, rgb);
                }
            }

//            int w = right + 1 - left;
//            int h = bottom + 1 - top;
//            BufferedImage target = new BufferedImage(w, h, source.getType());
//            Graphics2D g = target.createGraphics();
//            g.drawImage(source, left, top, w, h, null);
//            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage horizontalImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            BufferedImage target = new BufferedImage(width, height, source.getType());
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

    public static BufferedImage verticalImage(BufferedImage source) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
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
            int imageType = source.getType();
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
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (shearX < 0) {
                g.translate(width / 2, 0);
            }
            g.shear(shearX, shearY);
            g.drawImage(source, 0, 0, null);
            g.dispose();
            Color alphaColor;
            if (source.isAlphaPremultiplied() || source.getType() == BufferedImage.TYPE_INT_ARGB) {
                alphaColor = AlphaColor;
            } else {
                alphaColor = Color.WHITE;
            }
            target = cutEdges(target, alphaColor, true, true, true, true);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage addWatermarkText(BufferedImage source, String text,
            Font font, Color color, int x, int y, float transparent) {
        try {
            if (transparent > 1.0f || transparent < 0) {
                transparent = 1.0f;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent);
            g.setComposite(ac);
            g.setColor(color);
            g.setFont(font);
            g.drawString(text, x, y);
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
}
