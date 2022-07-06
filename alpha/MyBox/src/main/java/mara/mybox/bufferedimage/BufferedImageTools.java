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
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.MessageDigestTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileExtensions;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class BufferedImageTools {

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
    public static byte[] bytes(BufferedImage srcImage, String format) {
        byte[] bytes = null;
        try ( ByteArrayOutputStream stream = new ByteArrayOutputStream();) {
            BufferedImage tmpImage = srcImage;
            if (!FileExtensions.AlphaImages.contains(format)) {
                tmpImage = AlphaTools.removeAlpha(srcImage);
            }
            ImageIO.write(tmpImage, format, stream);
            bytes = stream.toByteArray();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return bytes;
    }

    public static String base64(BufferedImage image, String format) {
        try {
            if (image == null || format == null) {
                return null;
            }
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] bytes = bytes(image, format);
            if (bytes == null) {
                return null;
            }
            return encoder.encodeToString(bytes);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String base64(File file, String format) {
        try {
            if (file == null) {
                return null;
            }
            return base64(ImageFileReaders.readImage(file), format == null ? "jpg" : format);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static boolean sameByMD5(BufferedImage imageA, BufferedImage imageB) {
        if (imageA == null || imageB == null
                || imageA.getWidth() != imageB.getWidth()
                || imageA.getHeight() != imageB.getHeight()) {
            return false;
        }
        return Arrays.equals(MessageDigestTools.MD5(imageA), MessageDigestTools.MD5(imageB));
    }

    // This way may be more quicker than comparing digests
    public static boolean same(BufferedImage imageA, BufferedImage imageB) {
        try {
            if (imageA == null || imageB == null
                    || imageA.getWidth() != imageB.getWidth()
                    || imageA.getHeight() != imageB.getHeight()) {
                return false;
            }
            int width = imageA.getWidth(), height = imageA.getHeight();
            for (int y = 0; y < height / 2; y++) {
                for (int x = 0; x < width / 2; x++) {
                    if (imageA.getRGB(x, y) != imageA.getRGB(x, y)) {
                        return false;
                    }
                }
                for (int x = width - 1; x >= width / 2; x--) {
                    if (imageA.getRGB(x, y) != imageA.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            for (int y = height - 1; y >= height / 2; y--) {
                for (int x = 0; x < width / 2; x++) {
                    if (imageA.getRGB(x, y) != imageA.getRGB(x, y)) {
                        return false;
                    }
                }
                for (int x = width - 1; x >= width / 2; x--) {
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

    public static BufferedImage addArc(BufferedImage source, int arc, Color bgColor) {
        int width = source.getWidth();
        int height = source.getHeight();
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(width, height, imageType);
        Graphics2D g = target.createGraphics();
        if (AppVariables.imageRenderHints != null) {
            g.addRenderingHints(AppVariables.imageRenderHints);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);
        g.setClip(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return target;
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
