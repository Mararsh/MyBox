package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import static mara.mybox.fximage.FxColorTools.toAwtColor;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class PenTools {

    public static Image drawErase(Image image, DoubleLines penData, int strokeWidth) {
        if (penData == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawErase(source, penData, strokeWidth);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawCircle(Image image, DoubleCircle circle,
            Color strokeColor, int strokeWidth, boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (circle == null || strokeColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawCircle(
                source, circle, toAwtColor(strokeColor), strokeWidth, dotted, isFill, toAwtColor(fillColor),
                blendMode, opacity, orderReversed, ignoreTransparent);
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

    public static Image drawLines(Image image, DoublePolyline polyline,
            Color strokeColor, int strokeWidth, boolean dotted,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (polyline == null || strokeColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawLines(source, polyline,
                toAwtColor(strokeColor), strokeWidth, dotted, blendMode, opacity, orderReversed, ignoreTransparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawLines(Image image, DoubleLines penData,
            Color strokeColor, int strokeWidth, boolean dotted,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (penData == null || strokeColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawLines(source, penData,
                toAwtColor(strokeColor), strokeWidth, dotted, blendMode, opacity, orderReversed, ignoreTransparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolyline(Image image, DoublePolyline polyline,
            Color strokeColor, int strokeWidth, boolean dotted,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (polyline == null || strokeColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawPolyline(source, polyline,
                toAwtColor(strokeColor), strokeWidth, dotted, blendMode, opacity, orderReversed, ignoreTransparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawRectangle(Image image, DoubleRectangle rect,
            Color strokeColor, int strokeWidth, int arcWidth, boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (rect == null || strokeColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawRectangle(source, rect,
                toAwtColor(strokeColor), strokeWidth, arcWidth, dotted, isFill, toAwtColor(fillColor),
                blendMode, opacity, orderReversed, ignoreTransparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawEllipse(Image image, DoubleEllipse ellipse,
            Color strokeColor, int strokeWidth, boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (ellipse == null || strokeColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawEllipse(source, ellipse,
                toAwtColor(strokeColor), strokeWidth, dotted, isFill, toAwtColor(fillColor), blendMode, opacity, orderReversed, ignoreTransparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawPolygon(Image image, DoublePolygon polygon,
            Color strokeColor, int strokeWidth, boolean dotted, boolean isFill, Color fillColor,
            ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        if (polygon == null || strokeColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.PenTools.drawPolygon(source, polygon,
                toAwtColor(strokeColor), strokeWidth, dotted, isFill, toAwtColor(fillColor), blendMode, opacity, orderReversed, ignoreTransparent);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
