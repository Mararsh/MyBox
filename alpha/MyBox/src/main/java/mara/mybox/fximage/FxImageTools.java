package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageBlend;
import mara.mybox.bufferedimage.ImageTextTools;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.bufferedimage.ShadowTools;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fximage.FxColorTools.toAwtColor;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @Description
 * @License Apache License Version 2.0
 */
public class FxImageTools {

    public class ManufactureType {

        public static final int Brighter = 0;
        public static final int Darker = 1;
        public static final int Gray = 2;
        public static final int Invert = 3;
        public static final int Saturate = 4;
        public static final int Desaturate = 5;
    }

    public static Image createImage(int width, int height, Color color) {
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                pixelWriter.setColor(x, y, color);
            }
        }
        return newImage;
    }

    public static byte[] bytes(Image image) {
        return BufferedImageTools.bytes(SwingFXUtils.fromFXImage(image, null));
    }

    // This way may be more quicker than comparing digests
    public static boolean sameImage(Image imageA, Image imageB) {
        try {
            if (imageA == null || imageB == null
                    || imageA.getWidth() != imageB.getWidth()
                    || imageA.getHeight() != imageB.getHeight()) {
                return false;
            }
            int width = (int) imageA.getWidth(), height = (int) imageA.getHeight();
            PixelReader readA = imageA.getPixelReader();
            PixelReader readB = imageB.getPixelReader();
            for (int y = 0; y < height / 2; y++) {
                for (int x = 0; x < width / 2; x++) {
                    if (!readA.getColor(x, y).equals(readB.getColor(x, y))) {
                        return false;
                    }
                }
                for (int x = width - 1; x >= width / 2; x--) {
                    if (!readA.getColor(x, y).equals(readB.getColor(x, y))) {
                        return false;
                    }
                }
            }
            for (int y = height - 1; y >= height / 2; y--) {
                for (int x = 0; x < width / 2; x++) {
                    if (!readA.getColor(x, y).equals(readB.getColor(x, y))) {
                        return false;
                    }
                }
                for (int x = width - 1; x >= width / 2; x--) {
                    if (!readA.getColor(x, y).equals(readB.getColor(x, y))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // https://stackoverflow.com/questions/19548363/image-saved-in-javafx-as-jpg-is-pink-toned
    public static BufferedImage toBufferedImage(Image image) {
        if (image == null) {
            return null;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        return source;
    }

    public static BufferedImage checkAlpha(Image image, String format) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = AlphaTools.checkAlpha(source, format);
        return target;
    }

    public static Image clearAlpha(Image image) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = AlphaTools.removeAlpha(source);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image addText(Image image, String textString,
            java.awt.Font font, Color color, int x, int y,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent,
            int shadow, int angle, boolean isOutline, boolean isVertical) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageTextTools.addText(source, textString,
                font, toAwtColor(color), x, y, blendMode, opacity, orderReversed, ignoreTransparent,
                shadow, angle, isOutline, isVertical);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    // This way may fail for big image
    public static Image addTextFx(Image image, String textString,
            Font font, Color color, int x, int y, float transparent, int shadow) {
        try {
            Group group = new Group();

            Text text = new Text(x, y, textString);
            text.setFill(color);
            text.setFont(font);
            if (shadow > 0) {
                DropShadow dropShadow = new DropShadow();
                dropShadow.setOffsetX(shadow);
                dropShadow.setOffsetY(shadow);
                text.setEffect(dropShadow);
            }

            group.getChildren().add(text);

            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ImageInput(image));
            blend.setOpacity(1.0 - transparent);
            group.setEffect(blend);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Image addArc(Image image, int arc, Color bgColor) {
        if (image == null || arc <= 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = BufferedImageTools.addArc(source, arc, toAwtColor(bgColor));
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image addShadowAlpha(Image image, int shadow, Color color) {
        if (image == null || shadow <= 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ShadowTools.addShadowAlpha(source, shadow, toAwtColor(color));
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image addShadowNoAlpha(Image image, int shadow, Color color) {
        if (image == null || shadow <= 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ShadowTools.addShadowNoAlpha(source, shadow, toAwtColor(color));
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image blend(Image foreImage, Image backImage, int x, int y,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (foreImage == null || backImage == null || blendMode == null) {
            return null;
        }
        BufferedImage source1 = SwingFXUtils.fromFXImage(foreImage, null);
        BufferedImage source2 = SwingFXUtils.fromFXImage(backImage, null);
        BufferedImage target = ImageBlend.blend(source1, source2,
                x, y, blendMode, opacity, orderReversed, ignoreTransparent);
        if (target == null) {
            target = source1;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image makeMosaic(Image image, DoubleShape shape, int size,
            boolean isMosaic, boolean isExcluded) {
        if (!shape.isValid()) {
            return image;
        }
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(w, h);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (isExcluded) {
                    if (shape.include(x, y)) {
                        pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
                    } else {
                        if (isMosaic) {
                            int mx = Math.max(0, Math.min(w - 1, x - x % size));
                            int my = Math.max(0, Math.min(h - 1, y - y % size));
                            pixelWriter.setColor(x, y, pixelReader.getColor(mx, my));
                        } else {
                            int fx = Math.max(0, Math.min(w - 1, x - new Random().nextInt(size)));
                            int fy = Math.max(0, Math.min(h - 1, y - new Random().nextInt(size)));
                            pixelWriter.setColor(x, y, pixelReader.getColor(fx, fy));
                        }
                    }
                } else {
                    if (shape.include(x, y)) {
                        if (isMosaic) {
                            int mx = Math.max(0, Math.min(w - 1, x - x % size));
                            int my = Math.max(0, Math.min(h - 1, y - y % size));
                            pixelWriter.setColor(x, y, pixelReader.getColor(mx, my));
                        } else {
                            int fx = Math.max(0, Math.min(w - 1, x - new Random().nextInt(size)));
                            int fy = Math.max(0, Math.min(h - 1, y - new Random().nextInt(size)));
                            pixelWriter.setColor(x, y, pixelReader.getColor(fx, fy));
                        }
                    } else {
                        pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
                    }
                }
            }

        }
        return newImage;
    }

    public static Image replaceColor(Image image, Color oldColor, Color newColor, int distance) {
        if (image == null || oldColor == null || newColor == null || distance < 0) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = PixelsOperationFactory.replaceColor(source,
                ColorConvertTools.converColor(oldColor), ColorConvertTools.converColor(newColor), distance);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawHTML(Image backImage, Image html,
            DoubleRectangle bkRect, Color bkColor, float bkOpacity, int bkarc,
            int rotate, int margin) {
        if (html == null || backImage == null || bkRect == null) {
            return backImage;
        }
        BufferedImage backBfImage = SwingFXUtils.fromFXImage(backImage, null);
        BufferedImage htmlBfImage = SwingFXUtils.fromFXImage(html, null);
        BufferedImage target = ImageTextTools.drawHTML(backBfImage, htmlBfImage,
                bkRect, ColorConvertTools.converColor(bkColor), bkOpacity, bkarc, rotate, margin);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawHTML(Image backImage, BufferedImage html,
            int htmlX, int htmlY, int htmlWdith, int htmlHeight) {
        if (html == null || backImage == null) {
            return backImage;
        }
        BufferedImage backBfImage = SwingFXUtils.fromFXImage(backImage, null);
//        BufferedImage htmlBfImage = SwingFXUtils.fromFXImage(html, null);
        BufferedImage target = ImageTextTools.drawHTML(backBfImage, html,
                htmlX, htmlY, htmlWdith, htmlHeight);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static void blendDemoFx(BaseController parent, Button demoButton,
            Image foreImage, Image backImage, int x, int y,
            float opacity, boolean orderReversed, boolean ignoreTransparent) {
        BufferedImage foreBI = null;
        if (foreImage != null) {
            foreBI = SwingFXUtils.fromFXImage(foreImage, null);
        }
        BufferedImage backBI = null;
        if (backImage != null) {
            backBI = SwingFXUtils.fromFXImage(backImage, null);
        }
        blendDemo(parent, demoButton, foreBI, backBI, x, y, opacity, orderReversed, ignoreTransparent);
    }

    public static void blendDemo(BaseController parent, Button demoButton,
            BufferedImage foreImage, BufferedImage backImage, int x, int y,
            float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (parent != null) {
            parent.popInformation(Languages.message("WaitAndHandling"), 6000);
        }
        if (demoButton != null) {
            demoButton.setVisible(false);
        }
        Task demoTask = new Task<Void>() {
            private List<File> files;

            @Override
            protected Void call() {
                try {
                    BufferedImage foreBI = foreImage;
                    if (foreBI == null) {
                        foreBI = SwingFXUtils.fromFXImage(new Image("img/About.png"), null);
                    }
                    BufferedImage backBI = backImage;
                    if (backBI == null) {
                        backBI = SwingFXUtils.fromFXImage(new Image("img/ww8.png"), null);
                    }
                    files = new ArrayList<>();
                    for (String name : PixelsBlendFactory.blendModes()) {
                        PixelsBlend.ImagesBlendMode mode = PixelsBlendFactory.blendMode(name);
                        if (mode == PixelsBlend.ImagesBlendMode.NORMAL) {
                            BufferedImage blended = ImageBlend.blend(foreBI, backBI, x, y, mode, 1f, orderReversed, ignoreTransparent);
                            File tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                    + Languages.message("Opacity") + "-1.0f.png");
                            if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                files.add(tmpFile);
                            }
                            if (opacity < 1f) {
                                blended = ImageBlend.blend(foreBI, backBI, x, y, mode, opacity, orderReversed, ignoreTransparent);
                                tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                        + Languages.message("Opacity") + "-" + opacity + "f.png");
                                if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                    files.add(tmpFile);
                                }
                            }
                        } else {
                            BufferedImage blended = ImageBlend.blend(foreBI, backBI, x, y, mode, opacity, orderReversed, ignoreTransparent);
                            File tmpFile = new File(AppVariables.MyBoxTempPath + File.separator + name + "-"
                                    + Languages.message("Opacity") + "-" + opacity + "f.png");
                            if (ImageFileWriters.writeImageFile(blended, tmpFile)) {
                                files.add(tmpFile);
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (demoButton != null) {
                    demoButton.setVisible(true);
                }
                if (files.isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ImagesBrowserController controller
                                    = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                            controller.loadImages(files);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                });
            }

        };
        Thread thread = new Thread(demoTask);
        thread.setDaemon(false);
        thread.start();

    }

}
