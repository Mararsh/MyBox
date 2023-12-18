package mara.mybox.fximage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ColorConvertTools;
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

    public static void shear(FxTask demoTask, List<String> files, BufferedImage demoImage) {
        if (demoTask == null || demoImage == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Shear");

            BufferedImage bufferedImage = TransformTools.shearImage(demoTask, demoImage, 1.5f, 2);
            if (!demoTask.isWorking()) {
                return;
            }
            String tmpFile = FileTmpTools.getPathTempFile(path, "radio_(1.5,2)", ".png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (!demoTask.isWorking()) {
                return;
            }

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, 2, -1.5f);
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

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, -1.5f, 2);
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

            bufferedImage = TransformTools.shearImage(demoTask, demoImage, -2, -1.5f);
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

}
