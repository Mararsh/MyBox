package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class PenTools {

    public static Image drawRectangle(Image image, DoubleRectangle rect, PixelsBlend blender) {
        if (rect == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawRectangle(source, rect, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawCircle(Image image, DoubleCircle circle, PixelsBlend blender) {
        if (circle == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawCircle(source, circle, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawEllipse(Image image, DoubleEllipse ellipse, PixelsBlend blender) {
        if (ellipse == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawEllipse(source, ellipse, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolyline(Image image, DoublePolyline polyline, PixelsBlend blender) {
        if (polyline == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawPolyline(source, polyline, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolyLines(Image image, DoublePolyline polyline, PixelsBlend blender) {
        if (polyline == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawPolyLines(source, polyline, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawLines(Image image, DoubleLines penData, PixelsBlend blender) {
        if (penData == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawLines(source, penData, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolygon(Image image, DoublePolygon polygon, PixelsBlend blender) {
        if (polygon == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawPolygon(source, polygon, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawErase(Image image, DoubleLines penData, int strokeWidth) {
        if (penData == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawErase(source, penData, strokeWidth);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawMosaic(Image image, DoubleLines penData,
            ImageMosaic.MosaicType mosaicType, int strokeWidth) {
        if (penData == null || mosaicType == null || strokeWidth < 1) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawMosaic(source, penData, mosaicType, strokeWidth);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
