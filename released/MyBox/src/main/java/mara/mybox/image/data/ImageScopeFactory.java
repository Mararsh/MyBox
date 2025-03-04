package mara.mybox.image.data;

import javafx.scene.image.Image;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoublePolygon;
import mara.mybox.image.data.ImageScope.ShapeType;
import mara.mybox.image.tools.ImageScopeTools;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScopeFactory {

    public static ImageScope create(ImageScope sourceScope) {
        try {
            ImageScope newScope;
            Image image = sourceScope.getImage();
            ShapeType shapeType = sourceScope.getShapeType();
            if (shapeType == null) {
                shapeType = ShapeType.Whole;
            }
            switch (shapeType) {
                case Whole:
                    newScope = new Whole(image);
                    break;
                case Rectangle:
                    newScope = new Rectangle(image);
                    break;
                case Circle:
                    newScope = new Circle(image);
                    break;
                case Ellipse:
                    newScope = new Ellipse(image);
                    break;
                case Polygon:
                    newScope = new Polygon(image);
                    break;
                case Matting4:
                    newScope = new Matting4(image);
                    break;
                case Matting8:
                    newScope = new Matting8(image);
                    break;
                case Outline:
                    newScope = new Outline(image);
                    break;
                default:
                    newScope = new Whole(image);
            }
            ImageScopeTools.cloneValues(newScope, sourceScope);
            return newScope;
        } catch (Exception e) {
            //            MyBoxLog.debug(e);
            return sourceScope;
        }
    }

    static class Whole extends ImageScope {

        public Whole(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Whole;
        }

    }

    static class Rectangle extends ImageScope {

        public Rectangle(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Rectangle;
        }

        @Override
        public boolean inShape(int x, int y) {
            return ImageScopeTools.inShape(rectangle, shapeExcluded, x, y);
        }
    }

    static class Circle extends ImageScope {

        public Circle(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Circle;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        public boolean inShape(int x, int y) {
            return ImageScopeTools.inShape(circle, shapeExcluded, x, y);
        }

    }

    static class Ellipse extends ImageScope {

        public Ellipse(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Ellipse;
        }

        @Override
        public boolean inShape(int x, int y) {
            return ImageScopeTools.inShape(ellipse, shapeExcluded, x, y);
        }

    }

    static class Polygon extends ImageScope {

        public Polygon(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Polygon;
            if (image != null) {
                polygon = new DoublePolygon();
            }
        }

        @Override
        public boolean inShape(int x, int y) {
            return ImageScopeTools.inShape(polygon, shapeExcluded, x, y);
        }

    }

    static class Outline extends ImageScope {

        public Outline(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Outline;
        }

        @Override
        public boolean inShape(int x, int y) {
            boolean in = outline != null && outline.getRGB(x, y) > 0;
            return shapeExcluded ? !in : in;
        }

    }

    static class Matting4 extends ImageScope {

        public Matting4(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Matting4;

        }

    }

    static class Matting8 extends ImageScope {

        public Matting8(Image image) {
            this.image = image;
            this.shapeType = ShapeType.Matting8;

        }

    }

}
