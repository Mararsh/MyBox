package mara.mybox.fximage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-12-1
 * @License Apache License Version 2.0
 */
public class ColorDemos {

    public static void replaceColor(FxTask demoTask, List<String> files,
            PixelsOperation pixelsOperation, File demoFile) {
        if (demoTask == null || pixelsOperation == null || files == null) {
            return;
        }
        try {
            pixelsOperation.setTask(demoTask);
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("ReplaceColor");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            BufferedImage bufferedImage = pixelsOperation
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .start();
            if (!demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path, message("Hue"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false).start();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("Saturation"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(false).setBoolPara3(true)
                    .start();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("Brightness"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false)
                    .start();
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("All"), ".png").getAbsolutePath();
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
            BufferedImage demoImage, ImageScope scope, File demoFile) {
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("AdjustColor");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            PixelsOperation pixelsOperation;
            BufferedImage bufferedImage;
            String tmpFile;

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, PixelsOperation.OperationType.Color, PixelsOperation.ColorActionType.Set);
            pixelsOperation.setColorPara1(Color.PINK)
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .setTask(demoTask);
            bufferedImage = pixelsOperation.start();
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
            bufferedImage = pixelsOperation.start();
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
            bufferedImage = pixelsOperation.start();
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
            bufferedImage = pixelsOperation.start();
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
            bufferedImage = pixelsOperation.start();
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
            bufferedImage = pixelsOperation.start();
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
            bufferedImage = pixelsOperation.start();
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
            BufferedImage bufferedImage = pixelsOperation.start();
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

    public static void blendColor(FxTask currentTask, List<String> files, BufferedImage baseImage,
            javafx.scene.paint.Color color, File demoFile) {
        if (currentTask == null || baseImage == null || files == null) {
            return;
        }
        if (color == null) {
            color = javafx.scene.paint.Color.PINK;
        }
        Image overlay = FxImageTools.createImage(
                (int) (baseImage.getWidth() * 7 / 8), (int) (baseImage.getHeight() * 7 / 8),
                color);
        int x = (int) (baseImage.getWidth() - overlay.getWidth()) / 2;
        int y = (int) (baseImage.getHeight() - overlay.getHeight()) / 2;
        ShapeDemos.blendImage(currentTask, files, message("BlendColor"),
                baseImage, SwingFXUtils.fromFXImage(overlay, null), x, y, demoFile);
    }

    public static void blackWhite(FxTask demoTask, List<String> files,
            ImageBinary binary, File demoFile) {
        if (demoTask == null || binary == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("BlackOrWhite");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            binary.setTask(demoTask);
            int threshold = binary.getIntPara1();

            BufferedImage bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Default)
                    .setIsDithering(true)
                    .start();
            String tmpFile = FileTmpTools.getPathTempFile(path, message("Default")
                    + "_" + message("Dithering"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Default)
                    .setIsDithering(false)
                    .start();
            tmpFile = FileTmpTools.getPathTempFile(path, message("Default"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            List<Integer> inputs = new ArrayList<>();
            inputs.addAll(Arrays.asList(64, 96, 112, 128, 144, 160, 176, 198, 228));
            if (threshold > 0 && threshold < 255 && !inputs.contains(threshold)) {
                inputs.add(threshold);
            }
            for (int v : inputs) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = binary
                        .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                        .setIntPara1(v)
                        .setIsDithering(true)
                        .setTask(demoTask)
                        .start();
                tmpFile = FileTmpTools.getPathTempFile(path, message("Threshold") + v
                        + "_" + message("Dithering"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                bufferedImage = binary
                        .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                        .setIntPara1(v)
                        .setIsDithering(false)
                        .setTask(demoTask)
                        .start();
                tmpFile = FileTmpTools.getPathTempFile(path, message("Threshold") + v, ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }
            }

            int otsu = ImageBinary.threshold(demoTask, binary.getImage());
            bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                    .setIntPara1(otsu)
                    .setIsDithering(true)
                    .setTask(demoTask)
                    .start();
            tmpFile = FileTmpTools.getPathTempFile(path, message("OTSU")
                    + otsu + "_" + message("Dithering"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                    .setIntPara1(otsu)
                    .setIsDithering(false)
                    .setTask(demoTask)
                    .start();
            tmpFile = FileTmpTools.getPathTempFile(path, message("OTSU") + otsu, ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void sepia(FxTask demoTask, List<String> files,
            PixelsOperation pixelsOperation, File demoFile) {
        if (demoTask == null || pixelsOperation == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Sepia");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            pixelsOperation.setTask(demoTask);

            List<Integer> values = Arrays.asList(60, 80, 20, 50, 10, 5, 100, 15, 20);
            for (int v : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                BufferedImage bufferedImage = pixelsOperation.setIntPara1(v).start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                String tmpFile = FileTmpTools.getPathTempFile(path, message("Intensity") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void reduceColors(FxTask demoTask, List<String> files,
            BufferedImage demoImage, File demoFile) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("ReduceColors");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            ImageQuantization quantization;
            BufferedImage bufferedImage;
            String tmpFile;
            for (QuantizationAlgorithm a : QuantizationAlgorithm.values()) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                quantization = ImageQuantizationFactory.create(demoImage, null,
                        a, 8, 256, 1, 1, 1, false, true, true);
                bufferedImage = quantization.setTask(demoTask).start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message(quantization.getAlgorithm().name()) + "_" + message("Colors") + "8", ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                quantization = ImageQuantizationFactory.create(demoImage, null,
                        a, 27, 1024, 1, 1, 1, false, true, true);
                bufferedImage = quantization.setTask(demoTask).start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message(quantization.getAlgorithm().name()) + "_" + message("Colors") + "27", ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                quantization = ImageQuantizationFactory.create(demoImage, null,
                        a, 256, 1024, 2, 4, 3, false, true, true);
                bufferedImage = quantization.setTask(demoTask).start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message(quantization.getAlgorithm().name()) + "_" + message("Colors") + "256", ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void thresholding(FxTask demoTask, List<String> files,
            BufferedImage demoImage, File demoFile) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Thresholding");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            PixelsOperation op = PixelsOperationFactory.create(
                    demoImage, null, PixelsOperation.OperationType.Thresholding)
                    .setIsDithering(false).setTask(demoTask);

            BufferedImage bufferedImage = op.setIntPara1(128).setIntPara2(255).setIntPara3(0).start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path, message("Threshold") + "128_"
                    + message("BigValue") + "255_" + message("SmallValue") + "0", ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = op.setIntPara1(60).setIntPara2(190).setIntPara3(10).start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("Threshold") + "60_"
                    + message("BigValue") + "190_" + message("SmallValue") + "10", ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = op.setIntPara1(200).setIntPara2(255).setIntPara3(60).start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("Threshold") + "200_"
                    + message("BigValue") + "255_" + message("SmallValue") + "60", ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = op.setIntPara1(160).setIntPara2(225).setIntPara3(0).start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, message("Threshold") + "160_"
                    + message("BigValue") + "225_" + message("SmallValue") + "0", ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
