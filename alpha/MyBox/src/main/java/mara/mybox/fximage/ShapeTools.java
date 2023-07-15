package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.ShapeStyle;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class ShapeTools {

    public static Image drawRectangle(Image image, DoubleRectangle rect, ShapeStyle style, PixelsBlend blender) {
        if (rect == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawRectangle(source, rect, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawCircle(Image image, DoubleCircle circle, ShapeStyle style, PixelsBlend blender) {
        if (circle == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawCircle(source, circle, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawEllipse(Image image, DoubleEllipse ellipse, ShapeStyle style, PixelsBlend blender) {
        if (ellipse == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawEllipse(source, ellipse, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolyline(Image image, DoublePolyline polyline, ShapeStyle style, PixelsBlend blender) {
        if (polyline == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawPolyline(source, polyline, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolyLines(Image image, DoublePolyline polyline, ShapeStyle style, PixelsBlend blender) {
        if (polyline == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawPolyLines(source, polyline, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawLines(Image image, DoubleLines penData, ShapeStyle style, PixelsBlend blender) {
        if (penData == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawLines(source, penData, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolygon(Image image, DoublePolygon polygon, ShapeStyle style, PixelsBlend blender) {
        if (polygon == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawPolygon(source, polygon, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
