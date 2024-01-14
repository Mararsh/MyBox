package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class MarginTools {

    public static Image cutTransparentMargins(FxTask task, Image image) {
        return cutMarginsByColor(task, image, Color.TRANSPARENT, 10, true, true, true, true);
    }

    public static Image cutMarginsByWidth(FxTask task, Image image,
            int MarginWidth, boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            int imageWidth = (int) image.getWidth();
            int imageHeight = (int) image.getHeight();
            int top = 0;
            int bottom = imageHeight;
            int left = 0;
            int right = imageWidth;
            if (cutTop) {
                top = MarginWidth;
            }
            if (cutBottom) {
                bottom -= MarginWidth;
            }
            if (cutLeft) {
                left = MarginWidth;
            }
            if (cutRight) {
                right -= MarginWidth;
            }
            return CropTools.cropOutsideFx(task, image, left, top, right, bottom);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Image cutMarginsByColor(FxTask task, Image image, Color mColor, int colorDistance,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {
        try {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            PixelReader pixelReader = image.getPixelReader();
            int top = 0;
            int bottom = height;
            int left = 0;
            int right = width;
            int distance2 = colorDistance * colorDistance;
            if (cutTop) {
                for (int j = 0; j < height; ++j) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    boolean notMatch = false;
                    for (int i = 0; i < width; ++i) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        if (!FxColorTools.isColorMatchSquare(pixelReader.getColor(i, j), mColor, distance2)) {
                            notMatch = true;
                            break;
                        }
                    }
                    if (notMatch) {
                        top = j;
                        break;
                    }
                }
            }
            //            MyBoxLog.debug("top: " + top);
            if (top < 0) {
                return null;
            }
            if (cutBottom) {
                for (int j = height - 1; j >= 0; --j) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    boolean notMatch = false;
                    for (int i = 0; i < width; ++i) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        if (!FxColorTools.isColorMatchSquare(pixelReader.getColor(i, j), mColor, distance2)) {
                            notMatch = true;
                            break;
                        }
                    }
                    if (notMatch) {
                        bottom = j + 1;
                        break;
                    }
                }
            }
            //            MyBoxLog.debug("bottom: " + bottom);
            if (bottom < 0) {
                return null;
            }
            if (cutLeft) {
                for (int i = 0; i < width; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    boolean notMatch = false;
                    for (int j = 0; j < height; ++j) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        if (!FxColorTools.isColorMatchSquare(pixelReader.getColor(i, j), mColor, distance2)) {
                            notMatch = true;
                            break;
                        }
                    }
                    if (notMatch) {
                        left = i;
                        break;
                    }
                }
            }
            //            MyBoxLog.debug("left: " + left);
            if (left < 0) {
                return null;
            }
            if (cutRight) {
                for (int i = width - 1; i >= 0; --i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    boolean notMatch = false;
                    for (int j = 0; j < height; ++j) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        if (!FxColorTools.isColorMatchSquare(pixelReader.getColor(i, j), mColor, distance2)) {
                            notMatch = true;
                            break;
                        }
                    }
                    if (notMatch) {
                        right = i + 1;
                        break;
                    }
                }
            }
            //            MyBoxLog.debug("right: " + right);
            if (right < 0) {
                return null;
            }
            //            MyBoxLog.debug(left + " " + top + " " + right + " " + bottom);
            return CropTools.cropOutsideFx(task, image, left, top, right, bottom);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Image dragMarginsFx(FxTask task, Image image, Color color, DoubleRectangle rect) {
        try {
            if (image == null || rect == null) {
                return image;
            }
            int iwidth = (int) image.getWidth();
            int iheight = (int) image.getHeight();
            int rwidth = (int) rect.getWidth();
            int rheight = (int) rect.getHeight();
            int rx = (int) rect.getX();
            int ry = (int) rect.getY();
            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(rwidth, rheight);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            int ix;
            int iy;
            for (int j = 0; j < rheight; ++j) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                for (int i = 0; i < rwidth; ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    ix = i + rx;
                    iy = j + ry;
                    if (ix >= 0 && ix < iwidth && iy >= 0 && iy < iheight) {
                        pixelWriter.setColor(i, j, pixelReader.getColor(ix, iy));
                    } else {
                        pixelWriter.setColor(i, j, color);
                    }
                }
            }
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return image;
        }
    }

    public static Image addMarginsFx(FxTask task, Image image, Color color, int MarginWidth,
            boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        if (image == null || MarginWidth <= 0) {
            return image;
        }

        return addMarginsFx(task, image, color,
                addTop ? MarginWidth : -1,
                addBottom ? MarginWidth : -1,
                addLeft ? MarginWidth : -1,
                addRight ? MarginWidth : -1);
    }

    public static Image addMarginsFx(FxTask task, Image image, Color color,
            int top, int bottom, int left, int right) {
        try {
            if (image == null) {
                return image;
            }
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
//            MyBoxLog.debug(width + "  " + height);
            int totalWidth = width;
            int totalHegiht = height;
            int x1 = 0;
            int y1 = 0;

            if (left > 0) {
                totalWidth += left;
                x1 = left;
            }
            if (right > 0) {
                totalWidth += right;
            }
            if (top > 0) {
                totalHegiht += top;
                y1 = top;

            }
            if (bottom > 0) {
                totalHegiht += bottom;
            }
//            MyBoxLog.debug(totalWidth + "  " + totalHegiht);
            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(totalWidth, totalHegiht);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(x1, y1, width, height, pixelReader, 0, 0);
//            MyBoxLog.debug(x1 + "  " + y1);
            if (left > 0) {
                for (int x = 0; x < left; x++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    for (int y = 0; y < totalHegiht; y++) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (right > 0) {
                for (int x = totalWidth - 1; x > totalWidth - right - 1; x--) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    for (int y = 0; y < totalHegiht; y++) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (top > 0) {
                for (int y = 0; y < top; y++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    for (int x = 0; x < totalWidth; x++) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (bottom > 0) {
                for (int y = totalHegiht - 1; y > totalHegiht - bottom - 1; y--) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    for (int x = 0; x < totalWidth; x++) {
                        if (task != null && !task.isWorking()) {
                            return null;
                        }
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return image;
        }
    }

    // This way may fail for big image
    public static Image addMarginsFx2(Image image, Color color, int MarginWidth,
            boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (image == null || MarginWidth <= 0) {
                return image;
            }
            Group group = new Group();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double totalWidth = image.getWidth();
            double totalHeight = image.getHeight();
            ImageView view = new ImageView(image);
            view.setPreserveRatio(true);
            view.setFitWidth(imageWidth);
            view.setFitHeight(imageHeight);
            if (addLeft) {
                view.setX(MarginWidth);
                totalWidth += MarginWidth;
            } else {
                view.setX(0);
            }
            if (addTop) {
                view.setY(MarginWidth);
                totalHeight += MarginWidth;
            } else {
                view.setY(0);
            }
            if (addBottom) {
                totalHeight += MarginWidth;
            }
            if (addRight) {
                totalWidth += MarginWidth;
            }
            group.getChildren().add(view);
            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ColorInput(0, 0, totalWidth, totalHeight, color));
            group.setEffect(blend);
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return image;
        }
    }

    public static Image blurMarginsNoAlpha(FxTask task, Image image, int blurWidth,
            boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        if (image == null || blurWidth <= 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.MarginTools.blurMarginsNoAlpha(task, source,
                blurWidth, blurTop, blurBottom, blurLeft, blurRight);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image blurMarginsAlpha(FxTask task, Image image,
            int blurWidth, boolean blurTop, boolean blurBottom, boolean blurLeft, boolean blurRight) {
        if (image == null || blurWidth <= 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.MarginTools.blurMarginsAlpha(task, source,
                blurWidth, blurTop, blurBottom, blurLeft, blurRight);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
