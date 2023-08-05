package mara.mybox.bufferedimage;

import java.awt.Color;
import javafx.scene.image.Image;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScopeFactory {

    public static ImageScope create(ImageScope sourceScope) {
        try {

            ImageScope newScope = ImageScopeFactory.create(
                    sourceScope.getImage(),
                    sourceScope.getScopeType(),
                    sourceScope.getColorScopeType());
            ImageScopeTools.cloneValues(newScope, sourceScope);
            return newScope;
        } catch (Exception e) {
            //            MyBoxLog.debug(e);
            return sourceScope;
        }
    }

    public static ImageScope create(Image image,
            ImageScope.ScopeType scopeType,
            ImageScope.ColorScopeType colorScopeType) {
        switch (scopeType) {
            case All:
                return new All(image);
            case Color:
                switch (colorScopeType) {
                    case AllColor:
                        return new All(image);
                    case Color:
                        return new ColorDistance(image);
                    case Red:
                        return new RedDistance(image);
                    case Green:
                        return new GreenDistance(image);
                    case Blue:
                        return new BlueDistance(image);
                    case Brightness:
                        return new BrightnessDistance(image);
                    case Saturation:
                        return new SaturationDistance(image);
                    case Hue:
                        return new HueDistance(image);
                    default:
                        return new All(image);
                }
            case Rectangle:
                switch (colorScopeType) {
                    case AllColor:
                        return new Rectangle(image);
                    case Color:
                        return new RectangleColor(image);
                    case Red:
                        return new RectangleRed(image);
                    case Green:
                        return new RectangleGreen(image);
                    case Blue:
                        return new RectangleBlue(image);
                    case Brightness:
                        return new RectangleBrightness(image);
                    case Saturation:
                        return new RectangleSaturation(image);
                    case Hue:
                        return new RectangleHue(image);
                    default:
                        return new Rectangle(image);
                }
            case Circle:
                switch (colorScopeType) {
                    case AllColor:
                        return new Circle(image);
                    case Color:
                        return new CircleColor(image);
                    case Red:
                        return new CircleRed(image);
                    case Green:
                        return new CircleGreen(image);
                    case Blue:
                        return new CircleBlue(image);
                    case Brightness:
                        return new CircleBrightness(image);
                    case Saturation:
                        return new CircleSaturation(image);
                    case Hue:
                        return new CircleHue(image);
                    default:
                        return new Circle(image);
                }
            case Ellipse:
                switch (colorScopeType) {
                    case AllColor:
                        return new Ellipse(image);
                    case Color:
                        return new EllipseColor(image);
                    case Red:
                        return new EllipseRed(image);
                    case Green:
                        return new EllipseGreen(image);
                    case Blue:
                        return new EllipseBlue(image);
                    case Brightness:
                        return new EllipseBrightness(image);
                    case Saturation:
                        return new EllipseSaturation(image);
                    case Hue:
                        return new EllipseHue(image);
                    default:
                        return new ImageScope(image, scopeType);
                }
            case Polygon:
                switch (colorScopeType) {
                    case AllColor:
                        return new Polygon(image);
                    case Color:
                        return new PolygonColor(image);
                    case Red:
                        return new PolygonRed(image);
                    case Green:
                        return new PolygonGreen(image);
                    case Blue:
                        return new PolygonBlue(image);
                    case Brightness:
                        return new PolygonBrightness(image);
                    case Saturation:
                        return new PolygonSaturation(image);
                    case Hue:
                        return new PolygonHue(image);
                    default:
                        return new ImageScope(image, scopeType);
                }
            case Matting:
                switch (colorScopeType) {
                    case AllColor:
                        return new Matting(image);
                    case Color:
                        return new MattingColor(image);
                    case Red:
                        return new MattingRed(image);
                    case Green:
                        return new MattingGreen(image);
                    case Blue:
                        return new MattingBlue(image);
                    case Brightness:
                        return new MattingBrightness(image);
                    case Saturation:
                        return new MattingSaturation(image);
                    case Hue:
                        return new MattingHue(image);
                    default:
                        return new Matting(image);
                }
            case Outline:
                return new Outline(image);
            default:
                return new ImageScope(image, scopeType);
        }
    }

    static class All extends ImageScope {

        public All(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.All;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return true;
        }
    }

    static class Rectangle extends ImageScope {

        public Rectangle(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.inShape(rectangle, areaExcluded, x, y);
        }
    }

    static class Circle extends ImageScope {

        public Circle(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.inShape(circle, areaExcluded, x, y);
        }
    }

    static class Ellipse extends ImageScope {

        public Ellipse(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.inShape(ellipse, areaExcluded, x, y);
        }
    }

    static class Polygon extends ImageScope {

        public Polygon(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            if (image != null) {
                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.inShape(polygon, areaExcluded, x, y);
        }

    }

    static class AllColor extends ImageScope {

        public AllColor(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.AllColor;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return true;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return true;
        }
    }

    static class ColorDistance extends ImageScope {

        public ColorDistance(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.Color;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.isColorMatchSquare(colors, colorExcluded, colorDistanceSquare, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isColorMatchSquare(color1, color2, colorDistanceSquare);
        }
    }

    static class RedDistance extends ImageScope {

        public RedDistance(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.Red;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class GreenDistance extends ImageScope {

        public GreenDistance(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.Green;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class BlueDistance extends ImageScope {

        public BlueDistance(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.Blue;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class BrightnessDistance extends ImageScope {

        public BrightnessDistance(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.Brightness;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class SaturationDistance extends ImageScope {

        public SaturationDistance(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.Saturation;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class HueDistance extends ImageScope {

        public HueDistance(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Color;
            this.colorScopeType = ImageScope.ColorScopeType.Hue;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return ImageScopeTools.isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class RectangleColor extends ImageScope {

        public RectangleColor(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            this.colorScopeType = ImageScope.ColorScopeType.Color;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isColorMatchSquare(colors, colorExcluded, colorDistanceSquare, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isColorMatchSquare(color1, color2, colorDistanceSquare);
        }
    }

    static class RectangleRed extends ImageScope {

        public RectangleRed(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            this.colorScopeType = ImageScope.ColorScopeType.Red;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class RectangleGreen extends ImageScope {

        public RectangleGreen(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            this.colorScopeType = ImageScope.ColorScopeType.Green;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class RectangleBlue extends ImageScope {

        public RectangleBlue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            this.colorScopeType = ImageScope.ColorScopeType.Blue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class RectangleBrightness extends ImageScope {

        public RectangleBrightness(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            this.colorScopeType = ImageScope.ColorScopeType.Brightness;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class RectangleSaturation extends ImageScope {

        public RectangleSaturation(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            this.colorScopeType = ImageScope.ColorScopeType.Saturation;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class RectangleHue extends ImageScope {

        public RectangleHue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Rectangle;
            this.colorScopeType = ImageScope.ColorScopeType.Hue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class CircleColor extends ImageScope {

        public CircleColor(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            this.colorScopeType = ImageScope.ColorScopeType.Color;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isColorMatchSquare(colors, colorExcluded, colorDistanceSquare, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isColorMatchSquare(color1, color2, colorDistanceSquare);
        }
    }

    static class CircleRed extends ImageScope {

        public CircleRed(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            this.colorScopeType = ImageScope.ColorScopeType.Red;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class CircleGreen extends ImageScope {

        public CircleGreen(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            this.colorScopeType = ImageScope.ColorScopeType.Green;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class CircleBlue extends ImageScope {

        public CircleBlue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            this.colorScopeType = ImageScope.ColorScopeType.Blue;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class CircleBrightness extends ImageScope {

        public CircleBrightness(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            this.colorScopeType = ImageScope.ColorScopeType.Brightness;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class CircleSaturation extends ImageScope {

        public CircleSaturation(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            this.colorScopeType = ImageScope.ColorScopeType.Saturation;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class CircleHue extends ImageScope {

        public CircleHue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Circle;
            this.colorScopeType = ImageScope.ColorScopeType.Hue;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class EllipseColor extends ImageScope {

        public EllipseColor(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            this.colorScopeType = ImageScope.ColorScopeType.Color;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isColorMatchSquare(colors, colorExcluded, colorDistanceSquare, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isColorMatchSquare(color1, color2, colorDistanceSquare);
        }
    }

    static class EllipseRed extends ImageScope {

        public EllipseRed(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            this.colorScopeType = ImageScope.ColorScopeType.Red;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class EllipseGreen extends ImageScope {

        public EllipseGreen(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            this.colorScopeType = ImageScope.ColorScopeType.Green;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class EllipseBlue extends ImageScope {

        public EllipseBlue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            this.colorScopeType = ImageScope.ColorScopeType.Blue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class EllipseBrightness extends ImageScope {

        public EllipseBrightness(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            this.colorScopeType = ImageScope.ColorScopeType.Brightness;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class EllipseSaturation extends ImageScope {

        public EllipseSaturation(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            this.colorScopeType = ImageScope.ColorScopeType.Saturation;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class EllipseHue extends ImageScope {

        public EllipseHue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Ellipse;
            this.colorScopeType = ImageScope.ColorScopeType.Hue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = DoubleEllipse.rect(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class PolygonColor extends ImageScope {

        public PolygonColor(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            this.colorScopeType = ImageScope.ColorScopeType.Color;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isColorMatchSquare(colors, colorExcluded, colorDistanceSquare, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isColorMatchSquare(color1, color2, colorDistanceSquare);
        }
    }

    static class PolygonRed extends ImageScope {

        public PolygonRed(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            this.colorScopeType = ImageScope.ColorScopeType.Red;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class PolygonGreen extends ImageScope {

        public PolygonGreen(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            this.colorScopeType = ImageScope.ColorScopeType.Green;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class PolygonBlue extends ImageScope {

        public PolygonBlue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            this.colorScopeType = ImageScope.ColorScopeType.Blue;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class PolygonBrightness extends ImageScope {

        public PolygonBrightness(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            this.colorScopeType = ImageScope.ColorScopeType.Brightness;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class PolygonSaturation extends ImageScope {

        public PolygonSaturation(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            this.colorScopeType = ImageScope.ColorScopeType.Saturation;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class PolygonHue extends ImageScope {

        public PolygonHue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Polygon;
            this.colorScopeType = ImageScope.ColorScopeType.Hue;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!ImageScopeTools.inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return ImageScopeTools.isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class Outline extends ImageScope {

        public Outline(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Outline;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (outline == null) {
                return false;
            }
            return outline.getRGB(x, y) == 0;
        }
    }

    static class Matting extends ImageScope {

        public Matting(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Color;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isColorMatchSquare(color1, color2, colorDistanceSquare);
        }
    }

    static class MattingColor extends ImageScope {

        public MattingColor(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Color;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isColorMatchSquare(color1, color2, colorDistanceSquare);
        }
    }

    static class MattingRed extends ImageScope {

        public MattingRed(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Red;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class MattingGreen extends ImageScope {

        public MattingGreen(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Green;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class MattingBlue extends ImageScope {

        public MattingBlue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Blue;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class MattingBrightness extends ImageScope {

        public MattingBrightness(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Brightness;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class MattingSaturation extends ImageScope {

        public MattingSaturation(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Saturation;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class MattingHue extends ImageScope {

        public MattingHue(Image image) {
            this.image = image;
            this.scopeType = ImageScope.ScopeType.Matting;
            this.colorScopeType = ImageScope.ColorScopeType.Hue;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ColorMatchTools.isHueMatch(color1, color2, hsbDistance);
        }
    }

}
