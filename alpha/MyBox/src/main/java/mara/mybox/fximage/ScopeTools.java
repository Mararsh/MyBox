package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fximage.FxColorTools.toAwtColor;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class ScopeTools {
    
    public static Image scopeImage(Image srcImage, ImageScope scope, Color bgColor,
            boolean cutMargins, boolean exclude, boolean ignoreTransparent) {
        try {
            if (scope == null || scope.getScopeType() == null) {
                return srcImage;
            } else {
                PixelsOperation pixelsOperation = PixelsOperationFactory.create(srcImage, scope,
                        PixelsOperation.OperationType.Color, PixelsOperation.ColorActionType.Set)
                        .setColorPara1(ColorConvertTools.converColor(bgColor))
                        .setExcludeScope(!exclude)
                        .setSkipTransparent(ignoreTransparent);
                Image scopeImage = pixelsOperation.operateFxImage();
                if (cutMargins) {
                    return MarginTools.cutMarginsByColor(scopeImage, bgColor, 0, true, true, true, true);
                } else {
                    return scopeImage;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }
    
    public static Image indicateRectangle(Image image, Color color, int lineWidth, DoubleRectangle rect) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateRectangle(source, toAwtColor(color), lineWidth, rect);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }
    
    public static Image indicateCircle(Image image, Color color, int lineWidth, DoubleCircle circle) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateCircle(source, toAwtColor(color), lineWidth, circle);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }
    
    public static Image indicateEllipse(Image image, Color color, int lineWidth, DoubleEllipse ellipse) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateEllipse(source, toAwtColor(color), lineWidth, ellipse);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }
    
    public static Image indicateSplit(Image image, List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize, double scale) {
        if (rows == null || cols == null || rows.size() < 2 || cols.size() < 2) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateSplit(source, rows, cols, toAwtColor(lineColor), lineWidth, showSize, scale);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    // This way may fail for big image
    public static Image indicateSplitFx(Image image, List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize) {
        try {
            if (rows == null || cols == null) {
                return image;
            }
            Group group = new Group();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            int row;
            for (int i = 0; i < rows.size(); ++i) {
                row = rows.get(i);
                if (row <= 0 || row >= height - 1) {
                    continue;
                }
                Line rowLine = new Line(0, row, width, row);
                rowLine.setStroke(lineColor);
                rowLine.setStrokeWidth(lineWidth);
                group.getChildren().add(rowLine);
            }
            int col;
            for (int i = 0; i < cols.size(); ++i) {
                col = cols.get(i);
                if (col <= 0 || col >= width - 1) {
                    continue;
                }
                Line colLine = new Line(col, 0, col, height);
                colLine.setStroke(lineColor);
                colLine.setStrokeWidth(lineWidth);
                group.getChildren().add(colLine);
            }
            if (showSize) {
                for (int i = 0; i < rows.size() - 1; ++i) {
                    int h = rows.get(i + 1) - rows.get(i) + 1;
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        int w = cols.get(j + 1) - cols.get(j) + 1;
                        Text text = new Text();
                        text.setX(cols.get(j) + w / 3);
                        text.setY(rows.get(i) + h / 3);
                        text.setFill(lineColor);
                        text.setText(w + "x" + h);
                        text.setFont(new Font(lineWidth * 3.0));
                        group.getChildren().add(text);
                    }
                }
            }
            Blend blend = new Blend(BlendMode.SRC_OVER);
            blend.setBottomInput(new ImageInput(image));
            group.setEffect(blend);
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage newImage = group.snapshot(parameters, null);
            return newImage;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return null;
        }
    }
    
}
