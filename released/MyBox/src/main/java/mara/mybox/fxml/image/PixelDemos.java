package mara.mybox.fxml.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.image.tools.BufferedImageTools;
import mara.mybox.image.tools.BufferedImageTools.Direction;
import mara.mybox.image.tools.ColorConvertTools;
import mara.mybox.image.data.ImageContrast;
import mara.mybox.image.data.ImageContrast.ContrastAlgorithm;
import mara.mybox.image.data.ImageConvolution;
import mara.mybox.image.data.ImageMosaic;
import mara.mybox.image.data.ImageMosaic.MosaicType;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-12-1
 * @License Apache License Version 2.0
 */
public class PixelDemos {

    public static void mosaic(FxTask demoTask, List<String> files, BufferedImage demoImage,
            MosaicType type, File demoFile) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Mosaic");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            ImageMosaic mosaic = ImageMosaic.create().setType(type);
            mosaic.setImage(demoImage).setTask(demoTask);

            List<Integer> values = Arrays.asList(1, 3, 5, 8, 10, 15, 20, 25, 30, 50, 60, 80, 100);
            BufferedImage bufferedImage;
            String tmpFile;
            for (int v : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = mosaic.setIntensity(v).start();
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

    public static void shadow(FxTask demoTask, List<String> files, BufferedImage demoImage,
            Color color, File demoFile) {
        if (demoTask == null || color == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Shadow");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

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

    public static void smooth(FxTask demoTask, List<String> files,
            ImageConvolution convolution, File demoFile) {
        if (demoTask == null || convolution == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Smooth");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

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
                        .start();
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
                        .start();
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
                        .start();
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

    public static void sharpen(FxTask demoTask, List<String> files,
            ImageConvolution convolution, File demoFile) {
        if (demoTask == null || convolution == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Sharpen");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            BufferedImage bufferedImage = convolution
                    .setKernel(ConvolutionKernel.makeUnsharpMasking(1))
                    .setTask(demoTask)
                    .start();
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
                    .start();
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
                    .start();
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
                    .start();
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
                    .start();
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
                    .start();
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

    public static void contrast(FxTask demoTask, List<String> files,
            BufferedImage demoImage, File demoFile) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Contrast");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            ImageContrast contrast = new ImageContrast();
            contrast.setImage(demoImage).setTask(demoTask);

            BufferedImage bufferedImage = contrast
                    .setAlgorithm(ContrastAlgorithm.SaturationHistogramEqualization)
                    .start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path,
                    message("Saturation") + "-" + message("HistogramEqualization"), ".png")
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
                    .start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message("Brightness") + "-" + message("HistogramEqualization"), ".png").getAbsolutePath();
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
                    .start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message("SaturationBrightness") + "-" + message("HistogramEqualization"), ".png").getAbsolutePath();
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
                    .start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message("Gray") + "-" + message("HistogramEqualization"), ".png").getAbsolutePath();
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("Saturation") + "-" + message("HistogramStretching") + "_"
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("Brightness") + "-" + message("HistogramStretching") + "_"
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("SaturationBrightness") + "-" + message("HistogramStretching") + "_"
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("Gray") + "-" + message("HistogramStretching") + "_"
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("Saturation") + "-" + message("HistogramShifting") + "_" + v, ".png")
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("Brightness") + "-" + message("HistogramShifting") + "_" + v, ".png")
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("SaturationBrightness") + "-" + message("HistogramShifting") + "_" + v, ".png")
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
                        .start();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path,
                        message("Gray") + "-" + message("HistogramShifting") + "_" + v, ".png")
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

    public static void edge(FxTask demoTask, List<String> files,
            BufferedImage demoImage, File demoFile) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("EdgeDetection");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(demoImage).setTask(demoTask);

            convolution(demoTask, files, path,
                    convolution.setKernel(ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace()));
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            convolution(demoTask, files, path,
                    convolution.setKernel(ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert()));
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            convolution(demoTask, files, path,
                    convolution.setKernel(ConvolutionKernel.makeEdgeDetectionFourNeighborLaplace()));
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            convolution(demoTask, files, path,
                    convolution.setKernel(ConvolutionKernel.makeEdgeDetectionFourNeighborLaplaceInvert()));
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void emboss(FxTask demoTask, List<String> files,
            BufferedImage demoImage, File demoFile) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Emboss");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(demoImage).setTask(demoTask);

            for (Direction d : Direction.values()) {
                convolution(demoTask, files, path,
                        convolution.setKernel(ConvolutionKernel.makeEmbossKernel(d, 3, true)));
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                convolution(demoTask, files, path,
                        convolution.setKernel(ConvolutionKernel.makeEmbossKernel(d, 5, true)));
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static void convolution(FxTask demoTask, List<String> files,
            String path, ImageConvolution convolution) {
        try {
            convolution.getKernel().setGray(false);
            BufferedImage bufferedImage = convolution.setIsGray(false).start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path, convolution.getKernel().getName(), ".png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            convolution.getKernel().setGray(true);
            bufferedImage = convolution.setIsGray(true).start();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, convolution.getKernel().getName()
                    + "_" + message("Grey"), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
