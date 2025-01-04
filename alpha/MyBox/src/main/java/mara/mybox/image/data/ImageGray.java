package mara.mybox.image.data;

import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.image.tools.AlphaTools;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
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
    protected Color operateColor(Color color) {
        return ColorConvertTools.color2gray(color);
    }

    public static BufferedImage byteGray(FxTask task, BufferedImage srcImage) {
        try {
            int width = srcImage.getWidth();
            int height = srcImage.getHeight();
            BufferedImage naImage = AlphaTools.removeAlpha(task, srcImage, Color.WHITE);
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grayImage.createGraphics();
            if (AppVariables.ImageHints != null) {
                g.addRenderingHints(AppVariables.ImageHints);
            }
            g.drawImage(naImage, 0, 0, null);
            g.dispose();
            return grayImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return srcImage;
        }
    }

    public static BufferedImage intGray(FxTask task, BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int x = 0; x < width; x++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    int p = image.getRGB(x, y);
                    if (p == 0) {
                        grayImage.setRGB(x, y, 0);
                    } else {
                        grayImage.setRGB(x, y, ColorConvertTools.pixel2GrayPixel(p));
                    }
                }
            }
            return grayImage;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return image;
        }
    }

    public static Image gray(FxTask task, Image image) {
        try {
            BufferedImage bm = SwingFXUtils.fromFXImage(image, null);
            bm = intGray(task, bm);
            if (bm == null || (task != null && !task.isWorking())) {
                return null;
            }
            return SwingFXUtils.toFXImage(bm, null);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return image;
        }
    }

}
