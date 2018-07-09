package mara.mybox.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.objects.CommonValues;
import static mara.mybox.objects.CommonValues.UserFilePath;
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
                // +15 moves the tooltip 15 pixels below the mouse cursor;
                // if you don't change the y coordinate of the tooltip, you
                // will see constant screen flicker
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

    public static File getHelpFile(Class someClass, String resourceFile, String helpFile) {
        if (someClass == null || resourceFile == null || helpFile == null) {
            return null;
        }
        File file = new File(UserFilePath + "/" + helpFile);
        if (file.exists()) {
            return file;
        }
        URL url = someClass.getResource(resourceFile);
        if (url.toString().startsWith("jar:")) {
            try {
                InputStream input = someClass.getResourceAsStream(resourceFile);
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

    public static void manufactureImage(ImageView imageView, int manuType) {
        Image image = imageView.getImage();
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
        imageView.setImage(newImage);
    }

    // https://stackoverflow.com/questions/19548363/image-saved-in-javafx-as-jpg-is-pink-toned
    public static BufferedImage readImage(ImageView imageView) {
        BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
        // Remove alpha-channel from buffered image:
        BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE);
        Graphics2D graphics = imageRGB.createGraphics();
        graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        return imageRGB;
    }

    public static void changeSaturate(ImageView imageView, float change) {
        Image image = imageView.getImage();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                double v = color.getSaturation() + change;
                if (v > 1.0) {
                    v = 1.0;
                }
                if (v < 0.0) {
                    v = 0.0;
                }
                Color newColor = Color.hsb(color.getHue(), v, color.getBrightness());
                pixelWriter.setColor(x, y, newColor);
            }
        }
        imageView.setImage(newImage);
    }

    public static void changeBrightness(ImageView imageView, float change) {
        Image image = imageView.getImage();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                double v = color.getBrightness() + change;
                if (v > 1.0) {
                    v = 1.0;
                }
                if (v < 0.0) {
                    v = 0.0;
                }
                Color newColor = Color.hsb(color.getHue(), color.getSaturation(), v);
                pixelWriter.setColor(x, y, newColor);
            }
        }
        imageView.setImage(newImage);
    }

    public static void changeHue(ImageView imageView, int change) {
        Image image = imageView.getImage();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                double v = color.getHue() + change;
                if (v > 360.0) {
                    v = v - 360.0;
                }
                if (v < 0.0) {
                    v = v + 360.0;
                }
                Color newColor = Color.hsb(v, color.getSaturation(), color.getBrightness());
                pixelWriter.setColor(x, y, newColor);
            }
        }
        imageView.setImage(newImage);
    }

    public static void makeBinary(ImageView imageView, int precent) {
        int threshold = 256 * precent / 100;
        Image image = imageView.getImage();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = newImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                double gray = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                if (gray * 255 < threshold) {
                    pixelWriter.setColor(x, y, Color.BLACK);
                } else {
                    pixelWriter.setColor(x, y, Color.WHITE);
                }
            }
        }
        imageView.setImage(newImage);
    }
}
