package mara.mybox.tools;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import mara.mybox.image.ImageConverter;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import static mara.mybox.objects.CommonValues.UserFilePath;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.AreaScopeType;
import static mara.mybox.tools.FileTools.getFileSuffix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlTools {

    private static final Logger logger = LogManager.getLogger();

    public static String badStyle = "-fx-text-box-border: red;";

    public static void setScrollPane(ScrollPane scrollPane, double xOffset, double yOffset) {
        final Bounds visibleBounds = scrollPane.getViewportBounds();
        double scrollWidth = scrollPane.getContent().getBoundsInLocal().getWidth() - visibleBounds.getWidth();
        double scrollHeight = scrollPane.getContent().getBoundsInLocal().getHeight() - visibleBounds.getHeight();

        scrollPane.setHvalue(scrollPane.getHvalue() + xOffset / scrollWidth);
        scrollPane.setVvalue(scrollPane.getVvalue() + yOffset / scrollHeight);
    }

    public static boolean setRadioFirstSelected(ToggleGroup group) {
        if (group == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            radioButton.setSelected(true);
            return true;
        }
        return false;
    }

    public static boolean setRadioSelected(ToggleGroup group, String text) {
        if (group == null || text == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            if (text.equals(radioButton.getText())) {
                button.setSelected(true);
                return true;
            }
        }
        return false;
    }

    // https://stackoverflow.com/questions/26854301/how-to-control-the-javafx-tooltips-delay
    public static void quickTooltip(final Node node, final Tooltip tooltip) {
        node.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                tooltip.show(node, event.getScreenX(), event.getScreenY() + 15);
            }
        });
        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                tooltip.hide();
            }
        });
    }

    public static void setComments(final Node node, final Tooltip tooltip) {
        node.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (AppVaribles.showComments) {
                    tooltip.show(node, event.getScreenX(), event.getScreenY() + 15);
                }
            }
        });
        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (AppVaribles.showComments) {
                    tooltip.hide();
                }
            }
        });
    }

    public static void removeTooltip(final Node node) {
        node.setOnMouseMoved(null);
        node.setOnMouseExited(null);
    }

    public static String getFxmlPath(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        int pos = fullPath.lastIndexOf("jar!");
        if (pos < 0) {
            return fullPath;
        }
        return fullPath.substring(pos + "jar!".length());
    }

    public static int getInputInt(TextField input) {
        try {
            return Integer.parseInt(input.getText());
        } catch (Exception e) {
            return CommonValues.InvalidValue;
        }
    }

    public static void setNonnegativeValidation(final TextField input) {
        setNonnegativeValidation(input, Integer.MAX_VALUE);
    }

    public static void setNonnegativeValidation(final TextField input, final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setFloatValidation(final TextField input) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    float v = Float.valueOf(newValue);
                    input.setStyle(null);
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setFileValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isFile()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathExistedValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static File getUserFile(Class someClass, String resourceFile, String userFile) {
        if (someClass == null || resourceFile == null || userFile == null) {
            return null;
        }
        File file = new File(UserFilePath + "/" + userFile);
        if (file.exists()) {
            return file;
        }
        URL url = someClass.getResource(resourceFile);
        if (url.toString().startsWith("jar:")) {
            try {
                try (InputStream input = someClass.getResourceAsStream(resourceFile);
                        OutputStream out = new FileOutputStream(file)) {
                    int read;
                    byte[] bytes = new byte[1024];
                    while ((read = input.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                }
//                file.desleteOnExit();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(someClass.getResource(resourceFile).getFile());
        }
        return file;
    }

    // Solution from https://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
    public static File getResourceFile(Class someClass, String resourceFile) {
        if (someClass == null || resourceFile == null) {
            return null;
        }

        File file = null;
        URL url = someClass.getResource(resourceFile);
        if (url.toString().startsWith("jar:")) {
            try {
                InputStream input = someClass.getResourceAsStream(resourceFile);
                file = File.createTempFile("MyBox", "." + getFileSuffix(resourceFile));
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];
                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(someClass.getResource(resourceFile).getFile());
        }
        return file;
    }

    public class ImageManufactureType {

        public static final int Brighter = 0;
        public static final int Darker = 1;
        public static final int Gray = 2;
        public static final int Invert = 3;
        public static final int Saturate = 4;
        public static final int Desaturate = 5;
    }

    public static Image manufactureImage(Image image, int manuType) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                switch (manuType) {
                    case ImageManufactureType.Brighter:
                        color = color.brighter();
                        break;
                    case ImageManufactureType.Darker:
                        color = color.darker();
                        break;
                    case ImageManufactureType.Gray:
                        color = color.grayscale();
                        break;
                    case ImageManufactureType.Invert:
                        color = color.invert();
                        break;
                    case ImageManufactureType.Saturate:
                        color = color.saturate();
                        break;
                    case ImageManufactureType.Desaturate:
                        color = color.desaturate();
                        break;
                    default:
                        break;
                }
                pixelWriter.setColor(x, y, color);
            }
        }
        return newImage;
    }

    // https://stackoverflow.com/questions/19548363/image-saved-in-javafx-as-jpg-is-pink-toned
    public static BufferedImage getWritableData(Image image, String format) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        if (!CommonValues.NoAlphaImages.contains(format.toLowerCase()) || !bufferedImage.isAlphaPremultiplied()) {
            return bufferedImage;
        }
        if (AppVaribles.alphaAsBlack) {
            return ImageConverter.RemoveAlpha(bufferedImage);
        } else {
            return ImageConverter.ReplaceAlphaAsWhite(bufferedImage);
        }
    }

    public static Image changeSaturate(Image image, float change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    double v = color.getSaturation() + change;
                    if (v > 1.0) {
                        v = 1.0;
                    }
                    if (v < 0.0) {
                        v = 0.0;
                    }
                    Color newColor = Color.hsb(color.getHue(), v, color.getBrightness(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeBrightness(Image image, float change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    double v = color.getBrightness() + change;
                    if (v > 1.0) {
                        v = 1.0;
                    }
                    if (v < 0.0) {
                        v = 0.0;
                    }
                    Color newColor = Color.hsb(color.getHue(), color.getSaturation(), v, color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image changeHue(Image image, int change, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    double v = color.getHue() + change;
                    if (v > 360.0) {
                        v = v - 360.0;
                    }
                    if (v < 0.0) {
                        v = v + 360.0;
                    }
                    Color newColor = Color.hsb(v, color.getSaturation(), color.getBrightness(), color.getOpacity());
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image setOpacity(Image image, int opacity, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity / 100.0);
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image replaceColors(Image image, Color newColor, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeInvert(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, color.invert());
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeGray(Image image, ImageScope scope) {
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    pixelWriter.setColor(x, y, color.grayscale());
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    public static Image makeBinary(Image image, int precent, ImageScope scope) {
        double threshold = precent / 100.0;
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (scope.inScope(x, y, color)) {
                    double gray = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                    if (gray < threshold) {
                        pixelWriter.setColor(x, y, Color.BLACK);
                    } else {
                        pixelWriter.setColor(x, y, Color.WHITE);
                    }
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return newImage;
    }

    // https://en.wikipedia.org/wiki/Color_difference
    public static double calculateColorDistance2(Color color1, Color color2) {
        if (color1 == color2) {
            return 0;
        }
        double v = 2 * Math.pow(color1.getRed() * 255 - color2.getRed() * 255, 2)
                + 4 * Math.pow(color1.getGreen() * 255 - color2.getGreen() * 255, 2)
                + 3 * Math.pow(color1.getBlue() * 255 - color2.getBlue() * 255, 2);
        return v;
    }

    public static boolean isColorMatch(Color color1, Color color2, int distance) {
        if (color1 == color2) {
            return true;
        }
        return calculateColorDistance2(color1, color2) <= Math.pow(distance, 2);
    }

    public static boolean isHueMatch(Color color1, Color color2, int distance) {
        return Math.abs(color1.getHue() - color2.getHue()) <= distance;
    }

    public static Image scaleImage(Image image, String format, float scale) {
        int targetW = (int) Math.round(image.getWidth() * scale);
        int targetH = (int) Math.round(image.getHeight() * scale);
        return scaleImage(image, format, targetW, targetH);
    }

    public static Image scaleImage(Image image, String format, int width, int height) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConverter.scaleImage(source, width, height);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static String rgb2Hex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static String rgb2AlphaHex(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getOpacity() * 255),
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static Image rotateImage(Image image, int angle) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConverter.rotateImage(source, angle);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image horizontalImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int j = 0; j < height; j++) {
            int l = 0, r = width - 1;
            while (l <= r) {
                Color cl = pixelReader.getColor(l, j);
                Color cr = pixelReader.getColor(r, j);
                pixelWriter.setColor(l, j, cr);
                pixelWriter.setColor(r, j, cl);
                l++;
                r--;
            }
        }
        return newImage;
    }

    public static Image verticalImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        for (int i = 0; i < width; i++) {
            int t = 0, b = height - 1;
            while (t <= b) {
                Color ct = pixelReader.getColor(i, t);
                Color cb = pixelReader.getColor(i, b);
                pixelWriter.setColor(i, t, cb);
                pixelWriter.setColor(i, b, ct);
                t++;
                b--;
            }
        }
        return newImage;
    }

    public static Image shearImage(Image image, float shearX, float shearY) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConverter.shearImage(source, shearX, shearY);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image addWatermark(Image image, String text,
            Font font, Color color, int x, int y, float transparent) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConverter.addWatermarkText(source, text,
                font, FxmlTools.colorConvert(color), x, y, transparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static java.awt.Color colorConvert(javafx.scene.paint.Color color) {
        java.awt.Color newColor = new java.awt.Color((int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
        return newColor;
    }

    public static Image cutEdges(Image image, Color color,
            boolean cutTop, boolean cutBottom, boolean cutLeft, boolean cutRight) {

        try {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            PixelReader pixelReader = image.getPixelReader();

            int top = 0, bottom = height - 1, left = 0, right = width - 1;
            if (cutTop) {
                for (int j = 0; j < height; j++) {
                    boolean hasValue = false;
                    for (int i = 0; i < width; i++) {
                        if (!pixelReader.getColor(i, j).equals(color)) {
//                            logger.debug("hasValue: " + i + " " + j + " " + color);
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
                        if (!pixelReader.getColor(i, j).equals(color)) {
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
                        if (!pixelReader.getColor(i, j).equals(color)) {
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
                        if (!pixelReader.getColor(i, j).equals(color)) {
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

//            logger.debug(left + " " + top + " " + right + " " + bottom);
            return cropImage(image, left, top, right, bottom);

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static Image cropImage(Image image, int x1, int y1, int x2, int y2) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        if (x1 >= x2 || y1 >= y2
                || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0
                || x2 > width || y2 > height) {
            return image;
        }
        int w = x2 - x1 + 1;
        int h = y2 - y1 + 1;
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(w, h);
        PixelWriter pixelWriter = newImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, w, h, pixelReader, x1, y1);
        return newImage;
    }

    public static Image positionImage(Image image, File icon, int x, int y) {
        try {
            BufferedImage source = SwingFXUtils.fromFXImage(image, null);
            BufferedImage iconImage = ImageIO.read(icon);
            BufferedImage target = ImageConverter.addWatermarkImage(source, iconImage, x, y);
            Image newImage = SwingFXUtils.toFXImage(target, null);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    public static Image showArea(Image image, Color color, int lineWidth, int x1, int y1, int x2, int y2) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageConverter.showArea(source, FxmlTools.colorConvert(color), lineWidth, x1, y1, x2, y2);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image indicateScope(Image image, ImageScope scope) {
        if ((scope.getAreaScopeType() == AreaScopeType.AllArea) && scope.isAllColors()) {
            return image;
        }
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        double opacity = scope.getOpacity();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                Color opacityColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                if (scope.indicateOpacity(x, y, color)) {
                    pixelWriter.setColor(x, y, opacityColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }

        return newImage;
    }

    public static Image indicateSplit(Image image,
            List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth) {
        if (rows == null || cols == null) {
            return image;
        }
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        List<Integer> lineRows = new ArrayList<>();
        int row, top, bottom;
        for (int i = 0; i < rows.size(); i++) {
            row = rows.get(i);
            if (row <= 0 || row >= height - 1) {
                continue;
            }
            top = row - lineWidth / 2;
            if (top < 0) {
                top = 0;
            }
            bottom = row + lineWidth / 2;
            if (lineWidth % 2 == 0) {
                bottom--;
            }
            if (bottom >= height) {
                bottom = height - 1;
            }
            for (int j = top; j <= bottom; j++) {
                lineRows.add(j);
            }
        }
        List<Integer> lineCols = new ArrayList<>();
        int col, left, right;
        for (int i = 0; i < cols.size(); i++) {
            col = cols.get(i);
            if (col <= 0 || col >= width - 1) {
                continue;
            }
            left = col - lineWidth / 2;
            if (left < 0) {
                left = 0;
            }
            right = col + lineWidth / 2;
            if (lineWidth % 2 == 0) {
                right--;
            }
            if (right >= width) {
                right = width - 1;
            }
            for (int j = left; j <= right; j++) {
                lineCols.add(j);
            }
        }

        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                if (color == Color.TRANSPARENT) {
                    pixelWriter.setColor(x, y, color);
                    continue;
                }
                if (lineCols.contains(x) || lineRows.contains(y)) {
                    pixelWriter.setColor(x, y, lineColor);
                } else {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }

        return newImage;
    }

}
