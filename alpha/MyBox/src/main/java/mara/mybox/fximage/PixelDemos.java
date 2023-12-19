package mara.mybox.fximage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.bufferedimage.ImageContrast.ContrastAlgorithm;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.bufferedimage.ImageMosaic.MosaicType;
import mara.mybox.db.data.ConvolutionKernel;
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
public class PixelDemos {

    public static void mosaic(FxTask demoTask, List<String> files, BufferedImage demoImage, MosaicType type) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Mosaic");

            ImageMosaic mosaic = ImageMosaic.create().setType(type);
            mosaic.setImage(demoImage).setTask(demoTask);

            List<Integer> values = Arrays.asList(1, 3, 5, 8, 10, 15, 20, 25, 30, 50, 60, 80, 100);
            BufferedImage bufferedImage;
            String tmpFile;
            for (int v : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = mosaic.setIntensity(v).operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, message("Intensity") + "_" + v, ".png")
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

    public static void smooth(FxTask demoTask, List<String> files, ImageConvolution convolution) {
        if (demoTask == null || convolution == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Smooth");

            List<Integer> values = Arrays.asList(1, 2, 3, 4);
            BufferedImage bufferedImage;
            String tmpFile;
            for (int v : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = convolution
                        .setKernel(ConvolutionKernel.makeAverageBlur(v))
                        .setTask(demoTask)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("AverageBlur") + "_" + message("Intensity") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = convolution
                        .setKernel(ConvolutionKernel.makeGaussBlur(v))
                        .setTask(demoTask)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("GaussianBlur") + "_" + message("Intensity") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = convolution
                        .setKernel(ConvolutionKernel.makeMotionBlur(v))
                        .setTask(demoTask)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("MotionBlur") + "_" + message("Intensity") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

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

    public static void contrast(FxTask demoTask, List<String> files, BufferedImage demoImage, String prefix) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Contrast");
            if (prefix == null) {
                prefix = "";
            } else {
                prefix += "_";
            }

            ImageContrast contrast = new ImageContrast();
            contrast.setImage(demoImage).setTask(demoTask);

            BufferedImage bufferedImage = contrast
                    .setAlgorithm(ContrastAlgorithm.SaturationHistogramEqualization)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path,
                    prefix + message("Saturation") + "-" + message("HistogramEqualization"), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = contrast
                    .setAlgorithm(ContrastAlgorithm.BrightnessHistogramEqualization)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    prefix + message("Brightness") + "-" + message("HistogramEqualization"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = contrast
                    .setAlgorithm(ContrastAlgorithm.SaturationBrightnessHistogramEqualization)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    prefix + message("SaturationBrightness") + "-" + message("HistogramEqualization"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = contrast
                    .setAlgorithm(ContrastAlgorithm.GrayHistogramEqualization)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    prefix + message("Gray") + "-" + message("HistogramEqualization"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                if (!demoTask.isWorking()) {
                    return;
                }
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            long size = (long) (demoImage.getWidth() * demoImage.getHeight());
            List<Integer> pvalues = new ArrayList<>(Arrays.asList(1, 5, 10, 20, 30));
            long threshold = (long) (size * 0.05);
            for (int v : pvalues) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.SaturationHistogramStretching)
                        .setThreshold(threshold).setPercentage(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("Saturation") + "-" + message("HistogramStretching") + "_"
                        + message("Percentage") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.BrightnessHistogramStretching)
                        .setThreshold(threshold).setPercentage(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("Brightness") + "-" + message("HistogramStretching") + "_"
                        + message("Percentage") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.SaturationBrightnessHistogramStretching)
                        .setThreshold(threshold).setPercentage(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("SaturationBrightness") + "-" + message("HistogramStretching") + "_"
                        + message("Percentage") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.GrayHistogramStretching)
                        .setThreshold(threshold).setPercentage(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("Gray") + "-" + message("HistogramStretching") + "_"
                        + message("Percentage") + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

            }

            List<Integer> values = new ArrayList<>(Arrays.asList(5, 15, 30, 50, -5, -15, -30, -50));
            for (int v : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.SaturationHistogramShifting)
                        .setOffset(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("Saturation") + "-" + message("HistogramShifting") + "_" + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.BrightnessHistogramShifting)
                        .setOffset(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("Brightness") + "-" + message("HistogramShifting") + "_" + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.SaturationBrightnessHistogramShifting)
                        .setOffset(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("SaturationBrightness") + "-" + message("HistogramShifting") + "_" + v, ".png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = contrast
                        .setAlgorithm(ContrastAlgorithm.GrayHistogramShifting)
                        .setOffset(v)
                        .operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        prefix + message("Gray") + "-" + message("HistogramShifting") + "_" + v, ".png")
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

}
