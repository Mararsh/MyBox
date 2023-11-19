package mara.mybox.fximage;

import java.awt.Color;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class ScopeTools {

    public static Image selectedScope(Image srcImage, ImageScope scope, Color bgColor,
            boolean cutMargins, boolean exclude, boolean ignoreTransparent) {
        try {
            if (scope == null) {
                return srcImage;
            } else {
                PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                        srcImage, scope,
                        PixelsOperation.OperationType.SelectScope)
                        .setColorPara1(bgColor)
                        .setExcludeScope(exclude)
                        .setSkipTransparent(ignoreTransparent);
                Image scopeImage = pixelsOperation.operateFxImage();
                if (cutMargins) {
                    return MarginTools.cutMarginsByColor(scopeImage,
                            ColorConvertTools.converColor(bgColor),
                            0, true, true, true, true);
                } else {
                    return scopeImage;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static Image maskScope(Image srcImage, ImageScope scope,
            boolean exclude, boolean ignoreTransparent) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    srcImage, scope,
                    PixelsOperation.OperationType.ShowScope)
                    .setExcludeScope(exclude)
                    .setSkipTransparent(ignoreTransparent);
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

}
