package mara.mybox.fxml;

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
import static mara.mybox.fxml.FxmlImageTools.cropImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:38:23
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlMarginsTools {

    private static final Logger logger = LogManager.getLogger();

    public static Image cutMarginsByWidth(Image image, int MarginWidth,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {

        try {
            int imageWidth = (int) image.getWidth();
            int imageHeight = (int) image.getHeight();

            int top = 0, bottom = imageHeight - 1, left = 0, right = imageWidth - 1;
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
            return cropImage(image, left, top, right, bottom);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Image cutMarginsByColor(Image image, Color mColor, int colorDistance,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {

        try {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            PixelReader pixelReader = image.getPixelReader();

            int top = 0, bottom = height - 1, left = 0, right = width - 1;
            double d = colorDistance / 255.0;
            if (cutTop) {
                for (int j = 0; j < height; j++) {
                    boolean notMatch = false;
                    for (int i = 0; i < width; i++) {
                        if (!FxmlColorTools.isColorMatch(pixelReader.getColor(i, j), mColor, d)) {
//                            logger.debug("notMatch: " + i + " " + j + " " + color);
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
//            logger.debug("top: " + top);
            if (top < 0) {
                return null;
            }
            if (cutBottom) {
                for (int j = height - 1; j >= 0; j--) {
                    boolean notMatch = false;
                    for (int i = 0; i < width; i++) {
                        if (!FxmlColorTools.isColorMatch(pixelReader.getColor(i, j), mColor, d)) {
                            notMatch = true;
                            break;
                        }
                    }
                    if (notMatch) {
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
                    boolean notMatch = false;
                    for (int j = 0; j < height; j++) {
                        if (!FxmlColorTools.isColorMatch(pixelReader.getColor(i, j), mColor, d)) {
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
//            logger.debug("left: " + left);
            if (left < 0) {
                return null;
            }
            if (cutRight) {
                for (int i = width - 1; i >= 0; i--) {
                    boolean notMatch = false;
                    for (int j = 0; j < height; j++) {
                        if (!FxmlColorTools.isColorMatch(pixelReader.getColor(i, j), mColor, d)) {
                            notMatch = true;
                            break;
                        }
                    }
                    if (notMatch) {
                        right = i;
                        break;
                    }
                }
            }
//            logger.debug("right: " + right);
            if (right < 0) {
                return null;
            }

//            logger.debug(left + " " + top + " " + right + " " + bottom);
            return cropImage(image, left, top, right, bottom);

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Image addMarginsFx(Image image, Color color, int MarginWidth,
            boolean addTop, boolean addBottom, boolean addLeft, boolean addRight) {
        try {
            if (image == null || MarginWidth <= 0) {
                return image;
            }
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            int totalWidth = width, totalHegiht = height;
            int x1 = 0, y1 = 0, x2 = width, y2 = height;
            if (addLeft) {
                totalWidth += MarginWidth;
                x1 = MarginWidth;
                x2 = width + MarginWidth;
            }
            if (addRight) {
                totalWidth += MarginWidth;
            }
            if (addTop) {
                totalHegiht += MarginWidth;
                y1 = MarginWidth;
                y2 = height + MarginWidth;
            }
            if (addBottom) {
                totalHegiht += MarginWidth;
            }
//            logger.debug(width + "  " + totalWidth);

            PixelReader pixelReader = image.getPixelReader();
            WritableImage newImage = new WritableImage(totalWidth, totalHegiht);
            PixelWriter pixelWriter = newImage.getPixelWriter();
            pixelWriter.setPixels(x1, y1, width, height, pixelReader, 0, 0);

//            logger.debug(x1 + "  " + y1);
//            logger.debug(totalWidth + "  " + totalHegiht);
            if (addLeft) {
                for (int x = 0; x < MarginWidth; x++) {
                    for (int y = 0; y < totalHegiht; y++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addRight) {
                for (int x = totalWidth - 1; x > totalWidth - MarginWidth - 1; x--) {
                    for (int y = 0; y < totalHegiht; y++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addTop) {
                for (int y = 0; y < MarginWidth; y++) {
                    for (int x = 0; x < totalWidth; x++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
            if (addBottom) {
                for (int y = totalHegiht - 1; y > totalHegiht - MarginWidth - 1; y--) {
                    for (int x = 0; x < totalWidth; x++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }

            return newImage;

        } catch (Exception e) {
            logger.error(e.toString());
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
            double imageWidth = image.getWidth(), imageHeight = image.getHeight();
            double totalWidth = image.getWidth(), totalHeight = image.getHeight();
            ImageView view = new ImageView(image);
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
            logger.error(e.toString());
            return image;
        }

    }

}
