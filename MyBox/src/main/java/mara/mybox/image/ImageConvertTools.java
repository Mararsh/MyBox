package mara.mybox.image;

import mara.mybox.fxml.FxmlImageTools;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import static mara.mybox.objects.CommonValues.AlphaColor;
import mara.mybox.objects.ImageCombine;
import mara.mybox.objects.ImageCombine.CombineSizeType;
import mara.mybox.objects.ImageInformation;
import static mara.mybox.objects.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageConvertTools {

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
        if (AppVaribles.isAlphaAsBlack()) {
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
            int alpha = AlphaColor.getRGB();
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

    public static BufferedImage addText(BufferedImage source, String text,
            Font font, Color color, int x, int y,
            float transparent, int shadow, int angle, boolean isOutline) {
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
            if (isOutline) {
                FontRenderContext frc = g.getFontRenderContext();
                TextLayout textTl = new TextLayout(text, rotatedFont, frc);
                Shape outline = textTl.getOutline(null);
                AffineTransform transform = g.getTransform();
                transform.translate(x, y);
                g.transform(transform);
                g.setColor(color);
                g.draw(outline);
            } else {
                g.setColor(color);
                g.setFont(rotatedFont);
                g.drawString(text, x, y);
            }
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
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
        if (bgColor.getRGB() == AlphaColor.getRGB()) {
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

}
