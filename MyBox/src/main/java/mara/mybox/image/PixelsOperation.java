package mara.mybox.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.IntPoint;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @Version 1.0
 * @Description Pixel operations whose calculation only involves pixel itself.
 * Pixel operations who involve other pixels need defined separatedly.
 * @License Apache License Version 2.0
 */
public class PixelsOperation {

    protected BufferedImage image;
    protected boolean isDithering, boolPara, skipTransparent = true, excludeScope;
    protected int intPara1, intPara2, intPara3, scopeColor = 0;
    protected float floatPara1, floatPara2;
    protected Color colorPara1, colorPara2, bkColor;
    protected ImageScope scope;
    protected OperationType operationType;
    protected ColorActionType colorActionType;

    protected Color[] thisLine, nextLine;
    protected int thisLineY;
    protected int imageWidth, imageHeight;

    public enum OperationType {
        Smooth, Denoise, Blur, Sharpen, Clarity, Emboss, EdgeDetect,
        Thresholding, Quantization, Gray, BlackOrWhite, Sepia,
        ReplaceColor, Invert, Red, Green, Blue, Yellow, Cyan, Magenta, Mosaic,
        FrostedGlass,
        Brightness, Saturation, Hue, Opacity, PreOpacity, RGB, Color, ShowScope,
        Convolution, Contrast
    }

    public enum ColorActionType {
        Increase, Decrease, Set, Filter, Invert
    }

    public PixelsOperation() {
        this.bkColor = ImageColor.getAlphaColor();
        excludeScope = false;
    }

    public PixelsOperation(BufferedImage image, ImageScope scope, OperationType operationType) {
        this.image = image;
        this.operationType = operationType;
        this.scope = scope;
    }

    public static PixelsOperation create(Image image,
            ImageScope scope, OperationType operationType) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        return PixelsOperation.create(bufferedImage, scope, operationType);
    }

    public static PixelsOperation create(Image image,
            ImageScope scope, OperationType operationType, ColorActionType colorActionType) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        return PixelsOperation.create(bufferedImage, scope, operationType, colorActionType);
    }

    public static PixelsOperation create(BufferedImage image,
            ImageScope scope, OperationType operationType) {
        switch (operationType) {
            case ShowScope:
                return new ShowScope(image, scope);
            case Sepia:
                return new Sepia(image, scope);
            case Thresholding:
                return new Thresholding(image, scope);
            default:
                return new PixelsOperation(image, scope, operationType);
        }
    }

    public static PixelsOperation create(BufferedImage image,
            ImageScope scope, OperationType operationType, ColorActionType colorActionType) {
        switch (operationType) {
            case ReplaceColor:
                return new ReplaceColor(image, scope);
            case Color:
                return new SetColor(image, scope);
            case Opacity:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseOpacity(image, scope);
                    case Decrease:
                        return new DecreaseOpacity(image, scope);
                    case Set:
                    default:
                        return new SetOpacity(image, scope);
                }
            case PreOpacity:
                switch (colorActionType) {
                    case Increase:
                        return new IncreasePreOpacity(image, scope);
                    case Decrease:
                        return new DecreasePreOpacity(image, scope);
                    case Set:
                    default:
                        return new SetPreOpacity(image, scope);
                }
            case Brightness:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseBrightness(image, scope);
                    case Decrease:
                        return new DecreaseBrightness(image, scope);
                    case Set:
                    default:
                        return new SetBrightness(image, scope);
                }
            case Saturation:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseSaturation(image, scope);
                    case Decrease:
                        return new DecreaseSaturation(image, scope);
                    case Set:
                    default:
                        return new SetSaturation(image, scope);
                }
            case Hue:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseHue(image, scope);
                    case Decrease:
                        return new DecreaseHue(image, scope);
                    case Set:
                    default:
                        return new SetHue(image, scope);
                }
            case Red:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseRed(image, scope);
                    case Decrease:
                        return new DecreaseRed(image, scope);
                    case Filter:
                        return new FilterRed(image, scope);
                    case Invert:
                        return new InvertRed(image, scope);
                    case Set:
                    default:
                        return new SetRed(image, scope);
                }
            case Green:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseGreen(image, scope);
                    case Decrease:
                        return new DecreaseGreen(image, scope);
                    case Filter:
                        return new FilterGreen(image, scope);
                    case Invert:
                        return new InvertGreen(image, scope);
                    case Set:
                    default:
                        return new SetGreen(image, scope);
                }
            case Blue:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseBlue(image, scope);
                    case Decrease:
                        return new DecreaseBlue(image, scope);
                    case Filter:
                        return new FilterBlue(image, scope);
                    case Invert:
                        return new InvertBlue(image, scope);
                    case Set:
                    default:
                        return new SetBlue(image, scope);
                }
            case Yellow:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseYellow(image, scope);
                    case Decrease:
                        return new DecreaseYellow(image, scope);
                    case Filter:
                        return new FilterYellow(image, scope);
                    case Invert:
                        return new InvertYellow(image, scope);
                    case Set:
                    default:
                        return new SetYellow(image, scope);
                }
            case Cyan:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseCyan(image, scope);
                    case Decrease:
                        return new DecreaseCyan(image, scope);
                    case Filter:
                        return new FilterCyan(image, scope);
                    case Invert:
                        return new InvertCyan(image, scope);
                    case Set:
                    default:
                        return new SetCyan(image, scope);
                }
            case Magenta:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseMagenta(image, scope);
                    case Decrease:
                        return new DecreaseMagenta(image, scope);
                    case Filter:
                        return new FilterMagenta(image, scope);
                    case Invert:
                        return new InvertMagenta(image, scope);
                    case Set:
                    default:
                        return new SetMagenta(image, scope);
                }
            case RGB:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseRGB(image, scope);
                    case Decrease:
                        return new DecreaseRGB(image, scope);
                    case Invert:
                        return new InvertRGB(image, scope);
//                    case Set:
//                    default:
//                        return new SetRGB(image, scope);
                }
            default:
                return new PixelsOperation(image, scope, operationType);
        }
    }

    public BufferedImage operate() {
        return operateImage();
    }

    public BufferedImage operateImage() {
        if (image == null || operationType == null) {
            return image;
        }
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        if (operationType != OperationType.BlackOrWhite
                && operationType != OperationType.Quantization) {
            isDithering = false;
        }
        scope = ImageScope.fineImageScope(scope);
        skipTransparent = operationType != OperationType.ReplaceColor
                || colorPara1.getRGB() != 0;

        if (scope != null && scope.getScopeType() == ImageScope.ScopeType.Matting) {
            isDithering = false;
            return operateMatting();
        } else {
            return operateScope();
        }
    }

    public Image operateFxImage() {
        BufferedImage target = operate();
        if (target == null) {
            return null;
        }
        return SwingFXUtils.toFXImage(target, null);
    }

    protected BufferedImage operateScope() {
        if (image == null) {
            return image;
        }
        try {
            int imageType = image.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(imageWidth, imageHeight, imageType);
            boolean isShowScope = (operationType == OperationType.ShowScope);
            boolean isWhole = (scope == null || scope.getScopeType() == ImageScope.ScopeType.All);
            boolean inScope;
            if (isDithering) {
                thisLine = new Color[imageWidth];
                nextLine = new Color[imageWidth];
                thisLineY = 0;
                thisLine[0] = new Color(image.getRGB(0, 0), true);
            }
            Color newColor;
            int pixel, white = Color.WHITE.getRGB();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    pixel = image.getRGB(x, y);
                    Color color = new Color(pixel, true);
                    if (pixel == 0 && skipTransparent) {  // pass transparency
                        newColor = color;

                    } else {

                        inScope = isWhole || scope.inScope(x, y, color);
                        if (excludeScope) {
                            inScope = !inScope;
                        }
                        if (isDithering && y == thisLineY) {
                            color = thisLine[x];
                        }
                        if (isShowScope) {
                            newColor = color;
                            if (inScope) {
                                target.setRGB(x, y, scopeColor);
                            } else {
                                target.setRGB(x, y, white);
                            }
                        } else {
                            if (inScope) {
                                newColor = operatePixel(target, color, x, y);

                            } else {
                                newColor = color;
                                target.setRGB(x, y, color.getRGB());
                            }
                        }
                    }
                    dithering(color, newColor, x, y);
                }
                if (isDithering) {
                    thisLine = nextLine;
                    thisLineY = y + 1;
                    nextLine = new Color[imageWidth];
                }
            }
            thisLine = nextLine = null;
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return image;
        }
    }

    // https://en.wikipedia.org/wiki/Flood_fill
    // https://www.codeproject.com/Articles/6017/QuickFill-An-Efficient-Flood-Fill-Algorithm
    protected BufferedImage operateMatting() {
        try {
            if (image == null || scope == null) {
                return image;
            }
            boolean isShowScope = operationType == OperationType.ShowScope;
            int imageType = image.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(imageWidth, imageHeight, imageType);
            boolean excluded = scope.isColorExcluded();
            if (excludeScope) {
                excluded = !excluded;
            }
            if (isShowScope) {
                if (excluded) {
                    Graphics2D g2d = target.createGraphics();
                    g2d.setColor(CommonFxValues.TRANSPARENT);
                    g2d.fillRect(0, 0, imageWidth, imageHeight);
                    g2d.dispose();
                } else {
                    Graphics2D g2d = target.createGraphics();
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, imageWidth, imageHeight);
                    g2d.dispose();
                }

            } else {
                if (excluded) {
                    for (int y = 0; y < imageHeight; y++) {
                        for (int x = 0; x < imageWidth; x++) {
                            operatePixel(target, x, y);
                        }
                    }
                } else {
                    target = image.getSubimage(0, 0, imageWidth, imageHeight);
                }
            }
            List<IntPoint> points = scope.getPoints();
            if (points == null || points.isEmpty()) {
                return target;
            }

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<IntPoint> queue = new LinkedList<>();
            int white = Color.WHITE.getRGB();
            boolean eightNeighbor = scope.isEightNeighbor();
            for (IntPoint point : points) {
                Color startColor = new Color(image.getRGB(point.getX(), point.getY()), true);
                queue.add(point);
                while (!queue.isEmpty()) {
                    IntPoint p = queue.remove();
                    int x = p.getX(), y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    int pixel = image.getRGB(x, y);
                    Color color = new Color(pixel, true);
                    if (scope.inColorMatch(startColor, color)) {
                        if (pixel == 0 && skipTransparent) {
                            target.setRGB(x, y, pixel);
                        } else if (isShowScope) {
                            if (excluded) {
                                target.setRGB(x, y, white);
                            } else {
                                target.setRGB(x, y, scopeColor);
                            }
                        } else {
                            if (excluded) {
                                target.setRGB(x, y, pixel);
                            } else {
                                operatePixel(target, color, x, y);
                            }
                        }

                        queue.add(new IntPoint(x + 1, y));
                        queue.add(new IntPoint(x - 1, y));
                        queue.add(new IntPoint(x, y + 1));
                        queue.add(new IntPoint(x, y - 1));

                        if (eightNeighbor) {
                            queue.add(new IntPoint(x + 1, y + 1));
                            queue.add(new IntPoint(x + 1, y - 1));
                            queue.add(new IntPoint(x - 1, y + 1));
                            queue.add(new IntPoint(x - 1, y - 1));
                        }
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    protected Color operatePixel(BufferedImage target, int x, int y) {
        int pixel = image.getRGB(x, y);
        Color color = new Color(pixel, true);
        if (pixel == 0 && skipTransparent) {
            return color;
        } else {
            return operatePixel(target, color, x, y);
        }
    }

    protected Color operatePixel(BufferedImage target, Color color, int x, int y) {
        Color newColor = operateColor(color);
        target.setRGB(x, y, newColor.getRGB());
        return newColor;
    }

    // https://en.wikipedia.org/wiki/Dither
    // https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
    protected void dithering(Color color, Color newColor, int x, int y) {
        if (!isDithering || y != thisLineY) {
            return;
        }
        int red_error, green_error, blue_error;
        int new_red, new_green, new_blue;
        red_error = color.getRed() - newColor.getRed();
        green_error = color.getGreen() - newColor.getGreen();
        blue_error = color.getBlue() - newColor.getBlue();

        if (x + 1 < imageWidth) {
            color = new Color(image.getRGB(x + 1, y), true);
            new_red = Math.max(Math.min(color.getRed() + red_error * 7 / 16, 255), 0);
            new_green = Math.max(Math.min(color.getGreen() + green_error * 7 / 16, 255), 0);
            new_blue = Math.max(Math.min(color.getBlue() + blue_error * 7 / 16, 255), 0);
            newColor = new Color(new_red, new_green, new_blue, color.getAlpha());
            thisLine[x + 1] = newColor;
        }

        if (x - 1 >= 0 && y + 1 < imageHeight) {
            color = new Color(image.getRGB(x - 1, y + 1), true);
            new_red = Math.max(Math.min(color.getRed() + red_error * 3 / 16, 255), 0);
            new_green = Math.max(Math.min(color.getGreen() + green_error * 3 / 16, 255), 0);
            new_blue = Math.max(Math.min(color.getBlue() + blue_error * 3 / 16, 255), 0);
            newColor = new Color(new_red, new_green, new_blue, color.getAlpha());
            nextLine[x - 1] = newColor;
        }

        if (y + 1 < imageHeight) {
            color = new Color(image.getRGB(x, y + 1), true);
            new_red = Math.max(Math.min(color.getRed() + red_error * 5 / 16, 255), 0);
            new_green = Math.max(Math.min(color.getGreen() + green_error * 5 / 16, 255), 0);
            new_blue = Math.max(Math.min(color.getBlue() + blue_error * 5 / 16, 255), 0);
            newColor = new Color(new_red, new_green, new_blue, color.getAlpha());
            nextLine[x] = newColor;
        }

        if (x + 1 < imageWidth && y + 1 < imageHeight) {
            color = new Color(image.getRGB(x + 1, y + 1), true);
            new_red = Math.max(Math.min(color.getRed() + red_error * 1 / 16, 255), 0);
            new_green = Math.max(Math.min(color.getGreen() + green_error * 1 / 16, 255), 0);
            new_blue = Math.max(Math.min(color.getBlue() + blue_error * 1 / 16, 255), 0);
            newColor = new Color(new_red, new_green, new_blue, color.getAlpha());
            nextLine[x + 1] = newColor;
        }

    }

    // SubClass should implement this
    protected Color operateColor(Color color) {
        return color;
    }

    public static class ShowScope extends PixelsOperation {

        public ShowScope(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.ShowScope;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    (int) (scope.getOpacity() * 255));
        }
    }

    public static class Sepia extends PixelsOperation {

        public Sepia(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Sepia;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return ImageColor.pixel2Sepia(color, intPara1);
        }
    }

    public static class Thresholding extends PixelsOperation {

        public Thresholding(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Thresholding;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return ImageColor.thresholdingColor(color, intPara1, intPara2, intPara3);
        }
    }

    public static class ReplaceColor extends PixelsOperation {

        public ReplaceColor(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.ReplaceColor;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return colorPara2;
        }
    }

    public static class SetColor extends PixelsOperation {

        public SetColor(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Color;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return colorPara1;
        }
    }

    public static class SetOpacity extends PixelsOperation {

        public SetOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Opacity;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(Math.max(intPara1, 0), 255));
        }
    }

    public static class IncreaseOpacity extends PixelsOperation {

        public IncreaseOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Opacity;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(Math.max(color.getAlpha() + intPara1, 0), 255));
        }
    }

    public static class DecreaseOpacity extends PixelsOperation {

        public DecreaseOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Opacity;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(Math.max(color.getAlpha() - intPara1, 0), 255));
        }
    }

    public static class SetPreOpacity extends PixelsOperation {

        public SetPreOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.PreOpacity;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int opacity = Math.min(Math.max(intPara1, 0), 255);
            float f = opacity / 255.0f;
            return ImageColor.blendAlpha(color, f, bkColor);
        }
    }

    public static class IncreasePreOpacity extends PixelsOperation {

        public IncreasePreOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.PreOpacity;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int opacity = Math.min(Math.max(color.getAlpha() + intPara1, 0), 255);
            float f = opacity / 255.0f;
            return ImageColor.blendAlpha(color, f, bkColor);
        }
    }

    public static class DecreasePreOpacity extends PixelsOperation {

        public DecreasePreOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.PreOpacity;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int opacity = Math.min(Math.max(color.getAlpha() - intPara1, 0), 255);
            float f = opacity / 255.0f;
            return ImageColor.blendAlpha(color, f, bkColor);
        }
    }

    public static class SetBrightness extends PixelsOperation {

        public SetBrightness(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Brightness;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = floatPara1;
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(hsb[0], hsb[1], f);
        }
    }

    public static class IncreaseBrightness extends PixelsOperation {

        public IncreaseBrightness(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Brightness;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = hsb[2] * (1.0f + floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(hsb[0], hsb[1], f);
        }
    }

    public static class DecreaseBrightness extends PixelsOperation {

        public DecreaseBrightness(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Brightness;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = hsb[2] * (1.0f - floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(hsb[0], hsb[1], f);
        }
    }

    public static class SetSaturation extends PixelsOperation {

        public SetSaturation(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Saturation;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = floatPara1;
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(hsb[0], f, hsb[2]);
        }
    }

    public static class IncreaseSaturation extends PixelsOperation {

        public IncreaseSaturation(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Saturation;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = hsb[1] * (1.0f + floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(hsb[0], f, hsb[2]);
        }
    }

    public static class DecreaseSaturation extends PixelsOperation {

        public DecreaseSaturation(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Saturation;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = hsb[1] * (1.0f - floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(hsb[0], f, hsb[2]);
        }
    }

    public static class SetHue extends PixelsOperation {

        public SetHue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Hue;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = floatPara1;
            if (f > 1.0f) {
                f = f - 1.0f;
            }
            if (f < 0.0f) {
                f = f + 1.0f;
            }
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(f, hsb[1], hsb[2]);
        }
    }

    public static class IncreaseHue extends PixelsOperation {

        public IncreaseHue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Hue;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = hsb[0] + floatPara1;
            if (f > 1.0f) {
                f = f - 1.0f;
            }
            if (f < 0.0f) {
                f = f + 1.0f;
            }
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(f, hsb[1], hsb[2]);
        }
    }

    public static class DecreaseHue extends PixelsOperation {

        public DecreaseHue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Hue;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ImageColor.getHSB(color);
            float f = hsb[0] - floatPara1;
            if (f > 1.0f) {
                f = f - 1.0f;
            }
            if (f < 0.0f) {
                f = f + 1.0f;
            }
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ImageColor.HSB2RGB(f, hsb[1], hsb[2]);
        }
    }

    public static class SetRed extends PixelsOperation {

        public SetRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(intPara1, 0), 255),
                    color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class IncreaseRed extends PixelsOperation {

        public IncreaseRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class DecreaseRed extends PixelsOperation {

        public DecreaseRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class FilterRed extends PixelsOperation {

        public FilterRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 0, 0, color.getAlpha());
        }
    }

    public static class InvertRed extends PixelsOperation {

        public InvertRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class SetGreen extends PixelsOperation {

        public SetGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), Math.min(Math.max(intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class IncreaseGreen extends PixelsOperation {

        public IncreaseGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class DecreaseGreen extends PixelsOperation {

        public DecreaseGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class FilterGreen extends PixelsOperation {

        public FilterGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(0, color.getGreen(), 0, color.getAlpha());
        }
    }

    public static class InvertGreen extends PixelsOperation {

        public InvertGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 255 - color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class SetBlue extends PixelsOperation {

        public SetBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(),
                    Math.min(Math.max(intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class IncreaseBlue extends PixelsOperation {

        public IncreaseBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class DecreaseBlue extends PixelsOperation {

        public DecreaseBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class FilterBlue extends PixelsOperation {

        public FilterBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(0, 0, color.getBlue(), color.getAlpha());
        }
    }

    public static class InvertBlue extends PixelsOperation {

        public InvertBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }

    public static class SetYellow extends PixelsOperation {

        public SetYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(v, v, color.getBlue(), color.getAlpha());
        }
    }

    public static class IncreaseYellow extends PixelsOperation {

        public IncreaseYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class DecreaseYellow extends PixelsOperation {

        public DecreaseYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class FilterYellow extends PixelsOperation {

        public FilterYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), 0, color.getAlpha());
        }
    }

    public static class InvertYellow extends PixelsOperation {

        public InvertYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), 255 - color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class SetCyan extends PixelsOperation {

        public SetCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(color.getRed(), v, v, color.getAlpha());
        }
    }

    public static class IncreaseCyan extends PixelsOperation {

        public IncreaseCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(),
                    Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255),
                    color.getAlpha());
        }
    }

    public static class DecreaseCyan extends PixelsOperation {

        public DecreaseCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(),
                    Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255),
                    color.getAlpha());
        }
    }

    public static class FilterCyan extends PixelsOperation {

        public FilterCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(0, color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class InvertCyan extends PixelsOperation {

        public InvertCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }

    public static class SetMagenta extends PixelsOperation {

        public SetMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(v, color.getGreen(), v, color.getAlpha());
        }
    }

    public static class IncreaseMagenta extends PixelsOperation {

        public IncreaseMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    color.getGreen(),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class DecreaseMagenta extends PixelsOperation {

        public DecreaseMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    color.getGreen(),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class FilterMagenta extends PixelsOperation {

        public FilterMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 0, color.getBlue(), color.getAlpha());
        }
    }

    public static class InvertMagenta extends PixelsOperation {

        public InvertMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }

    public static class SetRGB extends PixelsOperation {

        public SetRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(v, v, v, color.getAlpha());
        }
    }

    public static class IncreaseRGB extends PixelsOperation {

        public IncreaseRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class DecreaseRGB extends PixelsOperation {

        public DecreaseRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class InvertRGB extends PixelsOperation {

        public InvertRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }

    /*
        get/set
     */
    public OperationType getOperationType() {
        return operationType;
    }

    public PixelsOperation setOperationType(OperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public BufferedImage getImage() {
        return image;
    }

    public PixelsOperation setImage(BufferedImage image) {
        this.image = image;
        return this;
    }

    public PixelsOperation setImage(Image image) {
        this.image = SwingFXUtils.fromFXImage(image, null);
        return this;
    }

    public boolean isIsDithering() {
        return isDithering;
    }

    public PixelsOperation setIsDithering(boolean isDithering) {
        this.isDithering = isDithering;
        return this;
    }

    public ImageScope getScope() {
        return scope;
    }

    public PixelsOperation setScope(ImageScope scope) {
        this.scope = scope;
        return this;
    }

    public boolean isBoolPara() {
        return boolPara;
    }

    public void setBoolPara(boolean boolPara) {
        this.boolPara = boolPara;
    }

    public int getIntPara1() {
        return intPara1;
    }

    public void setIntPara1(int intPara1) {
        this.intPara1 = intPara1;
    }

    public int getIntPara2() {
        return intPara2;
    }

    public void setIntPara2(int intPara2) {
        this.intPara2 = intPara2;
    }

    public int getIntPara3() {
        return intPara3;
    }

    public void setIntPara3(int intPara3) {
        this.intPara3 = intPara3;
    }

    public float getFloatPara1() {
        return floatPara1;
    }

    public void setFloatPara1(float floatPara1) {
        this.floatPara1 = floatPara1;
    }

    public float getFloatPara2() {
        return floatPara2;
    }

    public void setFloatPara2(float floatPara2) {
        this.floatPara2 = floatPara2;
    }

    public Color[] getThisLine() {
        return thisLine;
    }

    public void setThisLine(Color[] thisLine) {
        this.thisLine = thisLine;
    }

    public Color[] getNextLine() {
        return nextLine;
    }

    public void setNextLine(Color[] nextLine) {
        this.nextLine = nextLine;
    }

    public int getThisLineY() {
        return thisLineY;
    }

    public void setThisLineY(int thisLineY) {
        this.thisLineY = thisLineY;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Color getColorPara1() {
        return colorPara1;
    }

    public void setColorPara1(Color colorPara1) {
        this.colorPara1 = colorPara1;
    }

    public Color getColorPara2() {
        return colorPara2;
    }

    public void setColorPara2(Color colorPara2) {
        this.colorPara2 = colorPara2;
    }

    public ColorActionType getColorActionType() {
        return colorActionType;
    }

    public void setColorActionType(ColorActionType colorActionType) {
        this.colorActionType = colorActionType;
    }

    public int getScopeColor() {
        return scopeColor;
    }

    public void setScopeColor(int scopeColor) {
        this.scopeColor = scopeColor;
    }

    public boolean isSkipTransparent() {
        return skipTransparent;
    }

    public void setSkipTransparent(boolean skipTransparent) {
        this.skipTransparent = skipTransparent;
    }

    public boolean isExcludeScope() {
        return excludeScope;
    }

    public void setExcludeScope(boolean excludeScope) {
        this.excludeScope = excludeScope;
    }

    public Color getBkColor() {
        return bkColor;
    }

    public void setBkColor(Color bkColor) {
        this.bkColor = bkColor;
    }

}
