package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.IntPoint;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class PixelsOperation {

    protected BufferedImage image;
    protected boolean isDithering, boolPara;
    protected int intPara1, intPara2, intPara3;
    protected float floatPara1, floatPara2;
    protected Color colorPara1, colorPara2;
    protected ImageScope scope;
    protected OperationType operationType;
    protected ColorActionType colorActionType;

    protected Color[] thisLine, nextLine;
    protected int thisLineY;
    protected int imageWidth, imageHeight;

    public enum OperationType {
        Blur, Sharpen, Clarity, Emboss, EdgeDetect, Thresholding, Quantization, Gray, BlackOrWhite, Sepia,
        ReplaceColor, Invert, Red, Green, Blue, Yellow, Cyan, Magenta,
        Brightness, Sauration, Hue, Opacity, RGB, Color, Scope, Convolution, Contrast
    }

    public enum ColorActionType {
        Increase, Decrease, Set, Filter, Invert
    }

    public PixelsOperation() {

    }

    public PixelsOperation(BufferedImage image) {
        this.image = image;
    }

    public PixelsOperation(BufferedImage image, OperationType operationType) {
        this.image = image;
        this.operationType = operationType;
        this.scope = null;
    }

    public PixelsOperation(BufferedImage image, ImageScope scope, OperationType operationType) {
        this.image = image;
        this.operationType = operationType;
        this.scope = scope;
    }

    public PixelsOperation(BufferedImage image, ImageScope scope,
            OperationType operationType, ColorActionType colorActionType) {
        this.image = image;
        this.operationType = operationType;
        this.scope = scope;
        this.colorActionType = colorActionType;
    }

    public BufferedImage operate() {
        return operationImage();
    }

    public BufferedImage operationImage() {
        if (image == null || operationType == null) {
            return image;
        }
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        if (operationType != OperationType.BlackOrWhite
                && operationType != OperationType.Quantization) {
            isDithering = false;
        }
        if (scope != null && scope.getScopeType() == ImageScope.ScopeType.Matting) {
            isDithering = false;
            return operateMatting();
        } else {
            return operateScope();
        }
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
            boolean isScope = (operationType == OperationType.Scope);
            boolean isWhole = (scope == null || scope.getScopeType() == ImageScope.ScopeType.All);
            boolean inScope;
            if (isDithering) {
                thisLine = new Color[imageWidth];
                nextLine = new Color[imageWidth];
                thisLineY = 0;
                thisLine[0] = new Color(image.getRGB(0, 0), true);
            }
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = new Color(image.getRGB(x, y), true);
                    inScope = isWhole || scope.inScope(x, y, color);
                    if (isScope) {
                        if (inScope) {
                            if (isDithering && y == thisLineY) {
                                color = thisLine[x];
                            }
                            target.setRGB(x, y, color.getRGB());
                        } else {
                            operatePixel(target, color, x, y);
                        }
                    } else {
                        if (inScope) {
                            operatePixel(target, color, x, y);
                        } else {
                            if (isDithering && y == thisLineY) {
                                color = thisLine[x];
                            }
                            target.setRGB(x, y, color.getRGB());
                        }
                    }
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
            List<IntPoint> points = scope.getPoints();
            if (points == null || points.isEmpty()) {
                return image;
            }
            int imageType = image.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(imageWidth, imageHeight, imageType);
            boolean excluded = scope.isColorExcluded();
            boolean isScope = operationType == OperationType.Scope;
            if (isScope) {
                if (excluded) {
                    target = image.getSubimage(0, 0, imageWidth, imageHeight);
                } else {
                    for (int y = 0; y < imageHeight; y++) {
                        for (int x = 0; x < imageWidth; x++) {
                            operatePixel(target, x, y);
                        }
                    }
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

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<IntPoint> queue = new LinkedList<>();
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
                        if (isScope) {
                            if (excluded) {
                                operatePixel(target, color, x, y);
                            } else {
                                target.setRGB(x, y, pixel);
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
                    }
                }
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    protected void operatePixel(BufferedImage target, int x, int y) {
        operatePixel(target, image.getRGB(x, y), x, y);
    }

    protected void operatePixel(BufferedImage target, int pixel, int x, int y) {
        operatePixel(target, new Color(pixel, true), x, y);
    }

    protected void operatePixel(BufferedImage target, Color inColor, int x, int y) {
        Color color;
        if (isDithering && y == thisLineY) {
            color = thisLine[x];
        } else {
            color = inColor;
        }
        Color newColor = operateColor(color);
        target.setRGB(x, y, newColor.getRGB());

        dithering(target, color, newColor, x, y);

    }

    // https://en.wikipedia.org/wiki/Dither
    // https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
    protected void dithering(BufferedImage target, Color color, Color newColor, int x, int y) {
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

    protected Color operateColor(Color color) {
        if (operationType == null) {
            return color;
        }
        Color newColor = color;
        float f;
        int red, blue, green, opacity;
        float[] hsb;
        switch (operationType) {
            case Convolution:
                break;
            case Scope:
                newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (scope.getOpacity() * 255));
                break;
            case Sepia:
                newColor = ImageColor.pixel2Sepia(color, intPara1);
                break;
            case Thresholding:
                newColor = ImageColor.thresholdingColor(color, intPara1, intPara2, intPara3);
                break;
            case ReplaceColor:
                if (scope.inColorMatch(color, colorPara1)) {
                    newColor = colorPara2;
                }
                break;
            case Color:
                newColor = colorPara1;
                break;
            case Brightness:
                hsb = ImageColor.getHSB(color);
                switch (colorActionType) {
                    case Increase:
                        f = hsb[2] * (1.0f + floatPara1);
                        break;
                    case Decrease:
                        f = hsb[2] * (1.0f - floatPara1);
                        break;
                    case Set:
                    default:
                        f = floatPara1;
                        break;
                }
                f = Math.min(Math.max(f, 0.0f), 1.0f);
                newColor = ImageColor.HSB2RGB(hsb[0], hsb[1], f);
                break;
            case Sauration:
                hsb = ImageColor.getHSB(color);
                switch (colorActionType) {
                    case Increase:
                        f = hsb[1] * (1.0f + floatPara1);
                        break;
                    case Decrease:
                        f = hsb[1] * (1.0f - floatPara1);
                        break;
                    case Set:
                    default:
                        f = floatPara1;
                        break;
                }
                f = Math.min(Math.max(f, 0.0f), 1.0f);
                newColor = ImageColor.HSB2RGB(hsb[0], f, hsb[2]);
                break;
            case Hue:
                hsb = ImageColor.getHSB(color);
                switch (colorActionType) {
                    case Increase:
                        f = hsb[0] + floatPara1;
                        break;
                    case Decrease:
                        f = hsb[0] - floatPara1;
                        break;
                    case Set:
                    default:
                        f = floatPara1;
                        break;
                }
                if (f > 1.0f) {
                    f = f - 1.0f;
                }
                if (f < 0.0f) {
                    f = f + 1.0f;
                }
                f = Math.min(Math.max(f, 0.0f), 1.0f);
                newColor = ImageColor.HSB2RGB(f, hsb[1], hsb[2]);
                break;
            case Opacity:
                opacity = Math.min(Math.max(intPara1, 0), 255);
                newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                break;
            case Red:
                switch (colorActionType) {
                    case Set:
                        red = intPara1;
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, color.getGreen(), color.getBlue(), color.getAlpha());
                        break;
                    case Increase:
                        red = color.getRed() + intPara1;
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, color.getGreen(), color.getBlue(), color.getAlpha());
                        break;
                    case Decrease:
                        red = color.getRed() - intPara1;
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, color.getGreen(), color.getBlue(), color.getAlpha());
                        break;
                    case Filter:
                        newColor = new Color(color.getRed(), 0, 0, color.getAlpha());
                        break;
                    case Invert:
                        newColor = new Color(255 - color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                        break;
                }
                break;
            case Green:
                switch (colorActionType) {
                    case Set:
                        green = intPara1;
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(color.getRed(), green, color.getBlue(), color.getAlpha());
                        break;
                    case Increase:
                        green = color.getGreen() + intPara1;
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(color.getRed(), green, color.getBlue(), color.getAlpha());
                        break;
                    case Decrease:
                        green = color.getGreen() - intPara1;
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(color.getRed(), green, color.getBlue(), color.getAlpha());
                        break;
                    case Filter:
                        newColor = new Color(0, color.getGreen(), 0, color.getAlpha());
                        break;
                    case Invert:
                        newColor = new Color(color.getRed(), 255 - color.getGreen(), color.getBlue(), color.getAlpha());
                        break;
                }
                break;
            case Blue:
                switch (colorActionType) {
                    case Set:
                        blue = intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        newColor = new Color(color.getRed(), color.getGreen(), blue, color.getAlpha());
                        break;
                    case Increase:
                        blue = color.getBlue() + intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        newColor = new Color(color.getRed(), color.getGreen(), blue, color.getAlpha());
                        break;
                    case Decrease:
                        blue = color.getBlue() - intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        newColor = new Color(color.getRed(), color.getGreen(), blue, color.getAlpha());
                        break;
                    case Filter:
                        newColor = new Color(0, 0, color.getBlue(), color.getAlpha());
                        break;
                    case Invert:
                        newColor = new Color(color.getRed(), color.getGreen(), 255 - color.getBlue(), color.getAlpha());
                        break;
                }
                break;
            case Yellow:
                switch (colorActionType) {
                    case Set:
                        red = intPara1;
                        green = intPara1;
                        red = Math.min(Math.max(red, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(red, green, color.getBlue(), color.getAlpha());
                        break;
                    case Increase:
                        red = color.getRed() + intPara1;
                        green = color.getGreen() + intPara1;
                        red = Math.min(Math.max(red, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(red, green, color.getBlue(), color.getAlpha());
                        break;
                    case Decrease:
                        red = color.getRed() - intPara1;
                        green = color.getGreen() - intPara1;
                        red = Math.min(Math.max(red, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(red, green, color.getBlue(), color.getAlpha());
                        break;
                    case Filter:
                        newColor = new Color(color.getRed(), color.getGreen(), 0, color.getAlpha());
                        break;
                    case Invert:
                        newColor = new Color(255 - color.getRed(), 255 - color.getGreen(), color.getBlue(), color.getAlpha());
                        break;
                }
                break;
            case Cyan:
                switch (colorActionType) {
                    case Set:
                        blue = intPara1;
                        green = intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(color.getRed(), green, blue, color.getAlpha());
                        break;
                    case Increase:
                        blue = color.getBlue() + intPara1;
                        green = color.getGreen() + intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(color.getRed(), green, blue, color.getAlpha());
                        break;
                    case Decrease:
                        blue = color.getBlue() - intPara1;
                        green = color.getGreen() - intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        newColor = new Color(color.getRed(), green, blue, color.getAlpha());
                        break;
                    case Filter:
                        newColor = new Color(0, color.getGreen(), color.getBlue(), color.getAlpha());
                        break;
                    case Invert:
                        newColor = new Color(color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
                        break;
                }
                break;
            case Magenta:
                switch (colorActionType) {
                    case Set:
                        blue = intPara1;
                        red = intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, color.getGreen(), blue, color.getAlpha());
                        break;
                    case Increase:
                        blue = color.getBlue() + intPara1;
                        red = color.getRed() + intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, color.getGreen(), blue, color.getAlpha());
                        break;
                    case Decrease:
                        blue = color.getBlue() - intPara1;
                        red = color.getRed() - intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, color.getGreen(), blue, color.getAlpha());
                        break;
                    case Filter:
                        newColor = new Color(color.getRed(), 0, color.getBlue(), color.getAlpha());
                        break;
                    case Invert:
                        newColor = new Color(255 - color.getRed(), color.getGreen(), 255 - color.getBlue(), color.getAlpha());
                        break;
                }
                break;
            case RGB:
                switch (colorActionType) {
                    case Set:
                        blue = intPara1;
                        green = intPara1;
                        red = intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, green, blue, color.getAlpha());
                        break;
                    case Increase:
                        blue = color.getBlue() + intPara1;
                        green = color.getGreen() + intPara1;
                        red = color.getRed() + intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, green, blue, color.getAlpha());
                        break;
                    case Decrease:
                        blue = color.getBlue() - intPara1;
                        green = color.getGreen() - intPara1;
                        red = color.getRed() - intPara1;
                        blue = Math.min(Math.max(blue, 0), 255);
                        green = Math.min(Math.max(green, 0), 255);
                        red = Math.min(Math.max(red, 0), 255);
                        newColor = new Color(red, green, blue, color.getAlpha());
                        break;
                    case Invert:
                        newColor = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
                        break;
                }
                break;

            default:
                break;
        }
        return newColor;

    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public boolean isIsDithering() {
        return isDithering;
    }

    public void setIsDithering(boolean isDithering) {
        this.isDithering = isDithering;
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

    public ImageScope getScope() {
        return scope;
    }

    public void setScope(ImageScope scope) {
        this.scope = scope;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
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

}
