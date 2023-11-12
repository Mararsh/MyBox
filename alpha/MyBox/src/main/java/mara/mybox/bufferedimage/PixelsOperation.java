package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @Description Pixel operations whose calculation only involves pixel itself.
 * Pixel operations who involve other pixels need defined separatedly.
 * @License Apache License Version 2.0
 */
public abstract class PixelsOperation {

    protected BufferedImage image;
    protected boolean isDithering, skipTransparent, excludeScope,
            boolPara1, boolPara2, boolPara3;
    protected int intPara1, intPara2, intPara3, scopeColor = 0;
    protected float floatPara1, floatPara2;
    protected Color colorPara1, colorPara2;
    protected ImageScope scope;
    protected OperationType operationType;
    protected ColorActionType colorActionType;
    protected int currentX, currentY;

    protected Color[] thisLine, nextLine;
    protected int thisLineY;
    protected int imageWidth, imageHeight;

    public enum OperationType {
        Smooth, Denoise, Blur, Sharpen, Clarity, Emboss, EdgeDetect,
        Thresholding, Quantization, Gray, BlackOrWhite, Sepia,
        ReplaceColor, Invert, Red, Green, Blue, Yellow, Cyan, Magenta, Mosaic, FrostedGlass,
        Brightness, Saturation, Hue, Opacity, PreOpacity, RGB, Color, Blend,
        ShowScope, SelectScope, Convolution, Contrast
    }

    public enum ColorActionType {
        Set, Increase, Decrease, Filter, Invert
    }

    public PixelsOperation() {
        excludeScope = false;
        skipTransparent = true;
    }

    public PixelsOperation(BufferedImage image, ImageScope scope, OperationType operationType) {
        this.image = image;
        this.operationType = operationType;
        this.scope = scope;
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
        if (scope != null) {
            scope = ImageScopeFactory.create(scope);
        }
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
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(imageWidth, imageHeight, imageType);
            boolean isWhole = (scope == null || scope.getScopeType() == null);
            boolean inScope;
            if (isDithering) {
                thisLine = new Color[imageWidth];
                nextLine = new Color[imageWidth];
                thisLineY = 0;
                thisLine[0] = new Color(image.getRGB(0, 0), true);
            }
            Color newColor;
            int pixel;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    pixel = image.getRGB(x, y);
                    Color color = new Color(pixel, true);
                    if (pixel == 0 && skipTransparent) {
                        // transparency  need write dithering lines while they affect nothing
                        newColor = skipTransparent(target, x, y);

                    } else {

                        inScope = inScope(isWhole, x, y, color);
                        if (isDithering && y == thisLineY) {
                            color = thisLine[x];
                        }
                        if (inScope) {
                            newColor = operatePixel(target, color, x, y);
                        } else {
                            newColor = color;
                            target.setRGB(x, y, color.getRGB());
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
            MyBoxLog.error(e);
            return image;
        }
    }

    // https://en.wikipedia.org/wiki/Flood_fill
    // https://www.codeproject.com/Articles/6017/QuickFill-An-Efficient-Flood-Fill-Algorithm
    protected BufferedImage operateMatting() {
        try {
            if (image == null || scope == null || scope.getScopeType() == null) {
                return image;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            BufferedImage target = new BufferedImage(imageWidth, imageHeight, imageType);
            boolean excluded = scope.isColorExcluded();
            if (excludeScope) {
                excluded = !excluded;
            }
            if (operationType == OperationType.ShowScope) {
                excluded = !excluded;
            }
            if (excluded) {
                for (int y = 0; y < imageHeight; y++) {
                    for (int x = 0; x < imageWidth; x++) {
                        operatePixel(target, x, y);
                    }
                }
            } else {
                target = image.getSubimage(0, 0, imageWidth, imageHeight);
            }
            List<IntPoint> points = scope.getPoints();
            if (points == null || points.isEmpty()) {
                return target;
            }

            boolean[][] visited = new boolean[imageHeight][imageWidth];
            Queue<IntPoint> queue = new LinkedList<>();
            boolean eightNeighbor = scope.isEightNeighbor();
            int x, y;
            for (IntPoint point : points) {
                x = point.getX();
                y = point.getY();
                if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight) {
                    continue;
                }
                Color startColor = new Color(image.getRGB(x, y), true);
                queue.add(point);
                while (!queue.isEmpty()) {
                    IntPoint p = queue.remove();
                    x = p.getX();
                    y = p.getY();
                    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight
                            || visited[y][x]) {
                        continue;
                    }
                    visited[y][x] = true;
                    int pixel = image.getRGB(x, y);
                    Color color = new Color(pixel, true);
                    if (scope.inColorMatch(startColor, color)) {
                        if (pixel == 0 && skipTransparent) {
                            skipTransparent(target, x, y);
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
            MyBoxLog.error(e);
            return null;
        }
    }

    protected boolean inScope(boolean isWhole, int x, int y, Color color) {
        try {
            boolean inScope = isWhole || scope.inScope(x, y, color);
            if (excludeScope) {
                inScope = !inScope;
            }
            return inScope;
        } catch (Exception e) {
            return false;
        }
    }

    protected Color skipTransparent(BufferedImage target, int x, int y) {
        try {
            target.setRGB(x, y, 0);
            return Colors.TRANSPARENT;
        } catch (Exception e) {
            return null;
        }
    }

    protected Color operatePixel(BufferedImage target, int x, int y) {
        try {
            int pixel = image.getRGB(x, y);
            Color color = new Color(pixel, true);
            if (pixel == 0 && skipTransparent) {
                return color;
            } else {
                return operatePixel(target, color, x, y);
            }
        } catch (Exception e) {
            return null;
        }
    }

    protected Color operatePixel(BufferedImage target, Color color, int x, int y) {
        currentX = x;
        currentY = y;
        Color newColor = operateColor(color);
        if (newColor == null) {
            newColor = color;
        }
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

    public boolean isBoolPara1() {
        return boolPara1;
    }

    public PixelsOperation setBoolPara1(boolean boolPara1) {
        this.boolPara1 = boolPara1;
        return this;
    }

    public boolean isBoolPara2() {
        return boolPara2;
    }

    public PixelsOperation setBoolPara2(boolean boolPara2) {
        this.boolPara2 = boolPara2;
        return this;
    }

    public boolean isBoolPara3() {
        return boolPara3;
    }

    public PixelsOperation setBoolPara3(boolean boolPara3) {
        this.boolPara3 = boolPara3;
        return this;
    }

    public int getIntPara1() {
        return intPara1;
    }

    public PixelsOperation setIntPara1(int intPara1) {
        this.intPara1 = intPara1;
        return this;
    }

    public int getIntPara2() {
        return intPara2;
    }

    public PixelsOperation setIntPara2(int intPara2) {
        this.intPara2 = intPara2;
        return this;
    }

    public int getIntPara3() {
        return intPara3;
    }

    public PixelsOperation setIntPara3(int intPara3) {
        this.intPara3 = intPara3;
        return this;
    }

    public float getFloatPara1() {
        return floatPara1;
    }

    public PixelsOperation setFloatPara1(float floatPara1) {
        this.floatPara1 = floatPara1;
        return this;
    }

    public float getFloatPara2() {
        return floatPara2;
    }

    public PixelsOperation setFloatPara2(float floatPara2) {
        this.floatPara2 = floatPara2;
        return this;
    }

    public Color[] getThisLine() {
        return thisLine;
    }

    public PixelsOperation setThisLine(Color[] thisLine) {
        this.thisLine = thisLine;
        return this;
    }

    public Color[] getNextLine() {
        return nextLine;
    }

    public PixelsOperation setNextLine(Color[] nextLine) {
        this.nextLine = nextLine;
        return this;
    }

    public int getThisLineY() {
        return thisLineY;
    }

    public PixelsOperation setThisLineY(int thisLineY) {
        this.thisLineY = thisLineY;
        return this;
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

    public PixelsOperation setColorPara1(Color colorPara1) {
        this.colorPara1 = colorPara1;
        return this;
    }

    public Color getColorPara2() {
        return colorPara2;
    }

    public PixelsOperation setColorPara2(Color colorPara2) {
        this.colorPara2 = colorPara2;
        return this;
    }

    public ColorActionType getColorActionType() {
        return colorActionType;
    }

    public PixelsOperation setColorActionType(ColorActionType colorActionType) {
        this.colorActionType = colorActionType;
        return this;
    }

    public int getScopeColor() {
        return scopeColor;
    }

    public PixelsOperation setScopeColor(int scopeColor) {
        this.scopeColor = scopeColor;
        return this;
    }

    public boolean isSkipTransparent() {
        return skipTransparent;
    }

    public PixelsOperation setSkipTransparent(boolean skipTransparent) {
        this.skipTransparent = skipTransparent;
        return this;
    }

    public boolean isExcludeScope() {
        return excludeScope;
    }

    public PixelsOperation setExcludeScope(boolean excludeScope) {
        this.excludeScope = excludeScope;
        return this;
    }

}
