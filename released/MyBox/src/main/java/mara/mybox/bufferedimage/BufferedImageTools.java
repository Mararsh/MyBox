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
import java.util.Arrays;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.FileFilters;
import mara.mybox.color.ColorBase;
import mara.mybox.tools.MessageDigestTools;
import mara.mybox.value.Colors;

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
        return Arrays.equals(MessageDigestTools.MD5(imageA), MessageDigestTools.MD5(imageB));
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
        int imageType = source.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }
        if (bgColor.getRGB() == Colors.TRANSPARENT.getRGB()) {
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

}
