package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-2-15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGray extends PixelsOperation {

    public ImageGray() {
        operationType = OperationType.Gray;
    }

    public ImageGray(BufferedImage image) {
        this.image = image;
        this.operationType = OperationType.Gray;
    }

    public ImageGray(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Gray;
    }

    public ImageGray(Image image, ImageScope scope) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        this.operationType = OperationType.Gray;
        this.scope = scope;
    }

    public ImageGray(BufferedImage image, ImageScope scope) {
        this.image = image;
        this.operationType = OperationType.Gray;
        this.scope = scope;
    }

    @Override
    public BufferedImage operate() {
        if (image == null || operationType == null
                || operationType != OperationType.Gray) {
            return image;
        }

        if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All) {
            return byteGray(image);
        }
        return operateImage();
    }

    @Override
    protected Color operateColor(Color color) {
        return ColorConvertTools.color2gray(color);
    }

    public static BufferedImage byteGray(BufferedImage srcImage) {
        try {
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grayImage.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.drawImage(srcImage, 0, 0, null);
            g.dispose();
            return grayImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return srcImage;
        }
    }

    public static BufferedImage intGray(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int p = image.getRGB(x, y);
                    grayImage.setRGB(x, y, ColorConvertTools.pixel2GrayPixel(p));
                }
            }
            return grayImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return image;
        }
    }

    public static Image gray(Image image) {
        try {
            BufferedImage bm = SwingFXUtils.fromFXImage(image, null);
            bm = intGray(bm);
            return SwingFXUtils.toFXImage(bm, null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return image;
        }
    }

}
