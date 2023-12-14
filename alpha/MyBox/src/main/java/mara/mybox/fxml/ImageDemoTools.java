package mara.mybox.fxml;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-12-1
 * @License Apache License Version 2.0
 */
public class ImageDemoTools {

    public static void replaceColor(FxTask task, List<String> files,
            PixelsOperation pixelsOperation, String prefix) {
        if (task == null || pixelsOperation == null || prefix == null || files == null) {
            return;
        }
        try {
            pixelsOperation.setTask(task);

            BufferedImage bufferedImage = pixelsOperation
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Hue"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false).operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Saturation"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(false).setBoolPara3(true)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Brightness"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("All"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    public static void adjustColor(FxTask task, List<String> files,
            BufferedImage demoImage, ImageScope scope) {
        try {
            PixelsOperation pixelsOperation;
            BufferedImage bufferedImage;
            String tmpFile;

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Color, PixelsOperation.ColorActionType.Set);
            pixelsOperation.setColorPara1(ColorConvertTools.converColor(Color.LIGHTPINK))
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .setTask(task);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Color") + "_" + message("Filter"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Brightness, PixelsOperation.ColorActionType.Increase)
                    .setFloatPara1(0.5f).setTask(task);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Brightness") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Hue, PixelsOperation.ColorActionType.Decrease)
                    .setFloatPara1(0.3f).setTask(task);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Hue") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Saturation, PixelsOperation.ColorActionType.Increase)
                    .setFloatPara1(0.5f).setTask(task);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Saturation") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Opacity, PixelsOperation.ColorActionType.Decrease)
                    .setIntPara1(128).setTask(task);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Opacity") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Yellow, PixelsOperation.ColorActionType.Increase)
                    .setIntPara1(60).setTask(task);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Yellow") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Magenta, PixelsOperation.ColorActionType.Decrease)
                    .setIntPara1(60).setTask(task);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Magenta") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert,
                    message("RGB") + "_" + message("Invert"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Red, PixelsOperation.ColorActionType.Invert,
                    message("Red") + "_" + message("Invert"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Green, PixelsOperation.ColorActionType.Invert,
                    message("Green") + "_" + message("Invert"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Blue, PixelsOperation.ColorActionType.Invert,
                    message("Blue") + "_" + message("Invert"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Yellow, PixelsOperation.ColorActionType.Invert,
                    message("Yellow") + "_" + message("Invert"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Magenta, PixelsOperation.ColorActionType.Invert,
                    message("Magenta") + "_" + message("Invert"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Cyan, PixelsOperation.ColorActionType.Invert,
                    message("Cyan") + "_" + message("Invert"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Red, PixelsOperation.ColorActionType.Filter,
                    message("Red") + "_" + message("Filter"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Green, PixelsOperation.ColorActionType.Filter,
                    message("Green") + "_" + message("Filter"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Blue, PixelsOperation.ColorActionType.Filter,
                    message("Blue") + "_" + message("Filter"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Yellow, PixelsOperation.ColorActionType.Filter,
                    message("Yellow") + "_" + message("Filter"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Magenta, PixelsOperation.ColorActionType.Filter,
                    message("Magenta") + "_" + message("Filter"));
            if (!task.isWorking()) {
                return;
            }

            adjustColor(task, files, demoImage, scope, PixelsOperation.OperationType.Cyan, PixelsOperation.ColorActionType.Filter,
                    message("Cyan") + "_" + message("Filter"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void adjustColor(FxTask task, List<String> files,
            BufferedImage demoImage, ImageScope scope,
            PixelsOperation.OperationType type,
            PixelsOperation.ColorActionType action, String name) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                    demoImage, scope, type, action)
                    .setTask(task);
            BufferedImage bufferedImage = pixelsOperation.operate();
            String tmpFile = FileTmpTools.generateFile(name, "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void sharpen(FxTask task, List<String> files, ImageConvolution convolution) {
        if (task == null || convolution == null || files == null) {
            return;
        }
        try {
            String prefix = message("Sharpen");

            BufferedImage bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(1))
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.generateFile(prefix + "_" + message("UnsharpMasking") + "_1", "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!task.isWorking()) {
                    return;
                }
                task.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(2))
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("UnsharpMasking") + "_2", "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!task.isWorking()) {
                    return;
                }
                task.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(2))
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("UnsharpMasking") + "_3", "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!task.isWorking()) {
                    return;
                }
                task.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(2))
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("UnsharpMasking") + "_4", "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!task.isWorking()) {
                    return;
                }
                task.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.MakeSharpenEightNeighborLaplace())
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("EightNeighborLaplace"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!task.isWorking()) {
                    return;
                }
                task.setInfo(tmpFile);
            }

            bufferedImage = convolution
                    .setKernel(ConvolutionKernel.MakeSharpenFourNeighborLaplace())
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("FourNeighborLaplace"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!task.isWorking()) {
                    return;
                }
                task.setInfo(tmpFile);
            }

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

}
