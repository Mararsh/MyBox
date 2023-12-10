package mara.mybox.fxml;

import java.awt.image.BufferedImage;
import java.util.List;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.PixelsOperation;
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
            BufferedImage bufferedImage = pixelsOperation
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Hue"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                if (!task.isWorking()) {
                    return;
                }
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false).operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Saturation"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                if (!task.isWorking()) {
                    return;
                }
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(false).setBoolPara3(true)
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Brightness"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                if (!task.isWorking()) {
                    return;
                }
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false)
                    .setTask(task)
                    .operate();
            if (!task.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("All"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(task, bufferedImage, "png", tmpFile)) {
                if (!task.isWorking()) {
                    return;
                }
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
