package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.FileFilters;
import mara.mybox.color.ColorBase;
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
        this.bkColor = ColorConvertTools.getAlphaColor();
        excludeScope = false;
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
        scope = ImageScopeFactory.create(scope);
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
            MyBoxLog.error(e.toString());
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
                    g2d.setColor(Colors.TRANSPARENT);
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
            MyBoxLog.error(e.toString());
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
