package mara.mybox.fxml;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-12-1
 * @License Apache License Version 2.0
 */
public class ImageDemoTools {

    public static void replaceColor(FxTask demoTask, List<String> files,
            PixelsOperation pixelsOperation, String prefix) {
        if (demoTask == null || pixelsOperation == null || prefix == null || files == null) {
            return;
        }
        try {
            pixelsOperation.setTask(demoTask);
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("ReplaceColor");

            BufferedImage bufferedImage = pixelsOperation
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path, prefix + "_" + message("Hue"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false).operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, prefix + "_" + message("Saturation"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(false).setBoolPara3(true)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, prefix + "_" + message("Brightness"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, prefix + "_" + message("All"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
        } catch (Exception e) {
            if (demoTask != null) {
                demoTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    public static void adjustColor(FxTask demoTask, List<String> files,
            BufferedImage demoImage, ImageScope scope) {
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("AdjustColor");

            PixelsOperation pixelsOperation;
            BufferedImage bufferedImage;
            String tmpFile;

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Color, PixelsOperation.ColorActionType.Set);
            pixelsOperation.setColorPara1(Color.PINK)
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Color") + "_" + message("Filter"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Brightness, PixelsOperation.ColorActionType.Increase)
                    .setFloatPara1(0.5f).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Brightness") + "_" + message("Increase"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Hue, PixelsOperation.ColorActionType.Decrease)
                    .setFloatPara1(0.3f).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Hue") + "_" + message("Decrease"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Saturation, PixelsOperation.ColorActionType.Increase)
                    .setFloatPara1(0.5f).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Saturation") + "_" + message("Increase"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Opacity, PixelsOperation.ColorActionType.Decrease)
                    .setIntPara1(128).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Opacity") + "_" + message("Decrease"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Yellow, PixelsOperation.ColorActionType.Increase)
                    .setIntPara1(60).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Yellow") + "_" + message("Increase"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Magenta, PixelsOperation.ColorActionType.Decrease)
                    .setIntPara1(60).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Magenta") + "_" + message("Decrease"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert,
                    message("RGB") + "_" + message("Invert"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Red, PixelsOperation.ColorActionType.Invert,
                    message("Red") + "_" + message("Invert"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Green, PixelsOperation.ColorActionType.Invert,
                    message("Green") + "_" + message("Invert"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Blue, PixelsOperation.ColorActionType.Invert,
                    message("Blue") + "_" + message("Invert"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Yellow, PixelsOperation.ColorActionType.Invert,
                    message("Yellow") + "_" + message("Invert"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Magenta, PixelsOperation.ColorActionType.Invert,
                    message("Magenta") + "_" + message("Invert"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Cyan, PixelsOperation.ColorActionType.Invert,
                    message("Cyan") + "_" + message("Invert"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Red, PixelsOperation.ColorActionType.Filter,
                    message("Red") + "_" + message("Filter"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Green, PixelsOperation.ColorActionType.Filter,
                    message("Green") + "_" + message("Filter"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Blue, PixelsOperation.ColorActionType.Filter,
                    message("Blue") + "_" + message("Filter"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Yellow, PixelsOperation.ColorActionType.Filter,
                    message("Yellow") + "_" + message("Filter"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Magenta, PixelsOperation.ColorActionType.Filter,
                    message("Magenta") + "_" + message("Filter"));
            if (!demoTask.isWorking()) {
                return;
            }

            adjustColor(demoTask, files, demoImage, scope, path,
                    PixelsOperation.OperationType.Cyan, PixelsOperation.ColorActionType.Filter,
                    message("Cyan") + "_" + message("Filter"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void adjustColor(FxTask demoTask, List<String> files,
            BufferedImage demoImage, ImageScope scope, String path,
            PixelsOperation.OperationType type,
            PixelsOperation.ColorActionType action, String name) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                    demoImage, scope, type, action)
                    .setTask(demoTask);
            BufferedImage bufferedImage = pixelsOperation.operate();
            String tmpFile = FileTmpTools.getPathTempFile(path, name, ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
        } catch (Exception e) {
            if (demoTask != null) {
                demoTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    public static void sharpen(FxTask demoTask, List<String> files, ImageConvolution convolution) {
        if (demoTask == null || convolution == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Sharpen");

            BufferedImage bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(1))
                    .setTask(demoTask)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path, message("UnsharpMasking") + "_1", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(2))
                    .setTask(demoTask)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("UnsharpMasking") + "_2", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(2))
                    .setTask(demoTask)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("UnsharpMasking") + "_3", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(2))
                    .setTask(demoTask)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("UnsharpMasking") + "_4", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.MakeSharpenEightNeighborLaplace())
                    .setTask(demoTask)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("EightNeighborLaplace"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.MakeSharpenFourNeighborLaplace())
                    .setTask(demoTask)
                    .operate();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("FourNeighborLaplace"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }

        } catch (Exception e) {
            if (demoTask != null) {
                demoTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    public static void shadow(FxTask demoTask, List<String> files, BufferedImage demoImage, Color color) {
        if (demoTask == null || color == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Shadow");

            int offsetX = Math.max(30, demoImage.getWidth() / 20);
            int offsetY = Math.max(30, demoImage.getHeight() / 20);

            shadow(demoTask, files, demoImage, path, color, offsetX, offsetY, true);
            shadow(demoTask, files, demoImage, path, color, offsetX, -offsetY, true);
            shadow(demoTask, files, demoImage, path, color, -offsetX, offsetY, true);
            shadow(demoTask, files, demoImage, path, color, -offsetX, -offsetY, true);
            shadow(demoTask, files, demoImage, path, color, offsetX, offsetY, false);
            shadow(demoTask, files, demoImage, path, color, offsetX, -offsetY, false);
            shadow(demoTask, files, demoImage, path, color, -offsetX, offsetY, false);
            shadow(demoTask, files, demoImage, path, color, -offsetX, -offsetY, false);
        } catch (Exception e) {
            if (demoTask != null) {
                demoTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    public static void shadow(FxTask demoTask, List<String> files, BufferedImage demoImage,
            String path, Color color, int offsetX, int offsetY, boolean blur) {
        try {
            BufferedImage bufferedImage = BufferedImageTools.addShadow(demoTask, demoImage,
                    -offsetX, -offsetY, color, blur);
            String tmpFile = FileTmpTools.getPathTempFile(path,
                    ColorConvertTools.color2css(color)
                    + "_x-" + offsetX + "_y-" + offsetY + (blur ? ("_" + message("Blur")) : ""),
                    ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
        } catch (Exception e) {
            if (demoTask != null) {
                demoTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

}
