package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Map;
import javax.imageio.ImageIO;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.MessageDigestTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;
import mara.mybox.value.FileExtensions;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class BufferedImageTools {

    public static enum Direction {
        Top, Bottom, Left, Right, LeftTop, RightBottom, LeftBottom, RightTop
    }

    public static class KeepRatioType {

        public static final int BaseOnWidth = 0;
        public static final int BaseOnHeight = 1;
        public static final int BaseOnLarger = 2;
        public static final int BaseOnSmaller = 3;
        public static final int None = 9;

    }

    public static BufferedImage addShadow(FxTask task, BufferedImage source,
            int shadowX, int shadowY, Color shadowColor, boolean isBlur) {
        try {
            if (source == null || shadowColor == null
                    || (shadowX == 0 && shadowY == 0)) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            boolean blend = !AlphaTools.hasAlpha(source);
            int imageType = blend ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
            BufferedImage shadowImage = new BufferedImage(width, height, imageType);
            float iOpocity;
            float jOpacity;
            float opocity;
            float shadowRed = shadowColor.getRed() / 255.0F;
            float shadowGreen = shadowColor.getGreen() / 255.0F;
            float shadowBlue = shadowColor.getBlue() / 255.0F;
            Color newColor;
            Color alphaColor = blend ? ColorConvertTools.alphaColor() : Colors.TRANSPARENT;
            int alphaPixel = alphaColor.getRGB();
            int offsetX = Math.abs(shadowX);
            int offsetY = Math.abs(shadowY);
            for (int j = 0; j < height; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < width; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    int pixel = source.getRGB(i, j);
                    if (pixel == 0) {
                        shadowImage.setRGB(i, j, alphaPixel);
                        continue;
                    }
                    if (isBlur) {
                        iOpocity = jOpacity = 1.0F;
                        if (i < offsetX) {
                            iOpocity = 1.0F * i / offsetX;
                        } else if (i > width - offsetX) {
                            iOpocity = 1.0F * (width - i) / offsetX;
                        }
                        if (j < offsetY) {
                            jOpacity = 1.0F * j / offsetY;
                        } else if (j > height - offsetY) {
                            jOpacity = 1.0F * (height - j) / offsetY;
                        }
                        opocity = iOpocity * jOpacity;
                        if (opocity == 1.0F) {
                            newColor = shadowColor;
                        } else if (blend) {
                            newColor = ColorBlendTools.blendColor(shadowColor, opocity, alphaColor);
                        } else {
                            newColor = new Color(shadowRed, shadowGreen, shadowBlue, opocity);
                        }
                    } else {
                        newColor = shadowColor;
                    }
                    shadowImage.setRGB(i, j, newColor.getRGB());
                }
            }
            if (task != null && !task.isWorking()) {
                return null;
            }
            BufferedImage target = new BufferedImage(width + offsetX, height + offsetY, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            if (task != null && !task.isWorking()) {
                return null;
            }
            int x = shadowX > 0 ? 0 : -shadowX;
            int y = shadowY > 0 ? 0 : -shadowY;
            int sx = shadowX > 0 ? shadowX : 0;
            int sy = shadowY > 0 ? shadowY : 0;
            g.drawImage(shadowImage, sx, sy, null);
            if (task != null && !task.isWorking()) {
                return null;
            }
            g.drawImage(source, x, y, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BufferedImage setRound(FxTask task, BufferedImage source,
            int roundX, int roundY, Color bgColor) {
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, width, height);
            if (task != null && !task.isWorking()) {
                return null;
            }
            g.setClip(new RoundRectangle2D.Double(0, 0, width, height, roundX, roundY));
            if (task != null && !task.isWorking()) {
                return null;
            }
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
            return null;
        }
    }

    // https://stackoverflow.com/questions/24038524/how-to-get-byte-from-javafx-imageview
    public static byte[] bytes(FxTask task, BufferedImage srcImage, String format) {
        byte[] bytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();) {
            BufferedImage tmpImage = srcImage;
            if (!FileExtensions.AlphaImages.contains(format)) {
                tmpImage = AlphaTools.removeAlpha(task, srcImage);
                if (tmpImage == null) {
                    return null;
                }
            }
            ImageIO.write(tmpImage, format, stream);
            bytes = stream.toByteArray();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return bytes;
    }

    public static String base64(FxTask task, BufferedImage image, String format) {
        try {
            if (image == null || format == null) {
                return null;
            }
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] bytes = bytes(task, image, format);
            if (bytes == null) {
                return null;
            }
            return encoder.encodeToString(bytes);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String base64(FxTask task, File file, String format) {
        try {
            if (file == null) {
                return null;
            }
            return base64(task, ImageFileReaders.readImage(task, file), format == null ? "jpg" : format);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean sameByMD5(FxTask task, BufferedImage imageA, BufferedImage imageB) {
        if (imageA == null || imageB == null
                || imageA.getWidth() != imageB.getWidth()
                || imageA.getHeight() != imageB.getHeight()) {
            return false;
        }
        byte[] bA = MessageDigestTools.MD5(task, imageA);
        if (bA == null || (task != null && !task.isWorking())) {
            return false;
        }
        byte[] bB = MessageDigestTools.MD5(task, imageB);
        if (bB == null || (task != null && !task.isWorking())) {
            return false;
        }
        return Arrays.equals(bA, bB);
    }

    // This way may be quicker than comparing digests
    public static boolean same(FxTask task, BufferedImage imageA, BufferedImage imageB) {
        try {
            if (imageA == null || imageB == null
                    || imageA.getWidth() != imageB.getWidth()
                    || imageA.getHeight() != imageB.getHeight()) {
                return false;
            }
            int width = imageA.getWidth(), height = imageA.getHeight();
            for (int y = 0; y < height / 2; y++) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                for (int x = 0; x < width / 2; x++) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    if (imageA.getRGB(x, y) != imageA.getRGB(x, y)) {
                        return false;
                    }
                }
                for (int x = width - 1; x >= width / 2; x--) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    if (imageA.getRGB(x, y) != imageA.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            for (int y = height - 1; y >= height / 2; y--) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                for (int x = 0; x < width / 2; x++) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    if (imageA.getRGB(x, y) != imageA.getRGB(x, y)) {
                        return false;
                    }
                }
                for (int x = width - 1; x >= width / 2; x--) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    if (imageA.getRGB(x, y) != imageA.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // This way may be  quicker than comparing digests
    public static boolean sameImage(FxTask task, BufferedImage imageA, BufferedImage imageB) {
        if (imageA == null || imageB == null
                || imageA.getWidth() != imageB.getWidth()
                || imageA.getHeight() != imageB.getHeight()) {
            return false;
        }
        for (int y = 0; y < imageA.getHeight(); y++) {
            if (task != null && !task.isWorking()) {
                return false;
            }
            for (int x = 0; x < imageA.getWidth(); x++) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                if (imageA.getRGB(x, y) != imageB.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
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

    public static BufferedImage applyRenderHints(BufferedImage srcImage, Map<RenderingHints.Key, Object> hints) {
        try {
            if (srcImage == null || hints == null) {
                return srcImage;
            }
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            g.addRenderingHints(hints);
            g.drawImage(srcImage, 0, 0, width, height, null);
            g.dispose();
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

}
