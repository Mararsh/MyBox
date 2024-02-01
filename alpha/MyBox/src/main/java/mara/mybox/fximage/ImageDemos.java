package mara.mybox.fximage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.TransformTools;
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
public class ImageDemos {

    public static void shear(FxTask demoTask, List<String> files,
            BufferedImage demoImage, File demoFile) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Shear");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            BufferedImage bufferedImage = TransformTools.shearImage(demoTask, demoImage, 1f, 0, true);
            if (!demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path, "radio_(1,0)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, -1f, 0, true);
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, "radio_(-1,0)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, 0, 1f, true);
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, "radio_(0,1)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, 0, -1f, true);
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, "radio_(0,-1)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, 1.5f, 2, true);
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, "radio_(1.5,2)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, 2, -1.5f, true);
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, "radio_(2,-1.5)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, -1.5f, 2, true);
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, "radio_(-1.5,2)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, -2, -1.5f, true);
            if (!demoTask.isWorking()) {
                return;
            }
            tmpFile = FileTmpTools.getPathTempFile(path, "radio_(-2,-1.5)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

        } catch (Exception e) {
            if (demoTask != null) {
                demoTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    public static void round(FxTask demoTask, List<String> files,
            BufferedImage demoImage, Color color, File demoFile) {
        if (demoTask == null || demoImage == null || files == null || color == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Round");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }

            int width = demoImage.getWidth();
            int height = demoImage.getHeight();
            List<Integer> values = Arrays.asList(1, 2, 4, 8, 10, 20, 30);
            BufferedImage bufferedImage;
            String tmpFile;
            for (int r : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                int v = Math.min(width / r, height / r);
                bufferedImage = BufferedImageTools.setRound(demoTask, demoImage, v, v, color);
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, message("Round") + v, ".png")
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

}
