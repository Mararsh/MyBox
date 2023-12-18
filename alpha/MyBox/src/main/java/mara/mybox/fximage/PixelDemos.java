package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
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

    public static void smooth(FxTask demoTask, List<String> files, ImageConvolution convolution) {
        if (demoTask == null || convolution == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Smooth");

            List<Integer> values = Arrays.asList(1, 2, 3, 4, 5);
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

    public static void contrast(FxTask demoTask, List<String> files, BufferedImage demoImage) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Contrast");

            ImageContrast contrast = new ImageContrast();
            contrast.setImage(demoImage).setTask(demoTask);

            BufferedImage bufferedImage = contrast
                    .setAlgorithm(ContrastAlgorithm.HSB_Histogram_Equalization)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()), ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            bufferedImage = contrast
                    .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Equalization)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()), ".png").getAbsolutePath();
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
                    .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Stretching)
                    .setIntPara1(100).setIntPara2(100)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()) + "_100-100", ".png").getAbsolutePath();
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
                    .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Stretching)
                    .setIntPara1(50).setIntPara2(50)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()) + "_50-50", ".png").getAbsolutePath();
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
                    .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Stretching)
                    .setIntPara1(30).setIntPara2(30)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()) + "_30-30", ".png").getAbsolutePath();
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
                    .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Shifting)
                    .setIntPara1(100)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()) + "_100", ".png").getAbsolutePath();
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
                    .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Shifting)
                    .setIntPara1(50)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()) + "_50", ".png").getAbsolutePath();
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
                    .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Shifting)
                    .setIntPara1(30)
                    .operate();
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path,
                    message(contrast.getAlgorithm().name()) + "_30", ".png").getAbsolutePath();
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
