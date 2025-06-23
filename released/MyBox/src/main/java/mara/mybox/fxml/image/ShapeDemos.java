package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import mara.mybox.image.tools.ImageTextTools;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.image.data.PixelsBlendFactory;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.controller.ControlImageText;
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
public class ShapeDemos {

    public static void blendImage(FxTask currentTask, List<String> files, String op,
            BufferedImage baseImage, BufferedImage overlay, int x, int y, File demoFile) {
        if (currentTask == null || baseImage == null || overlay == null || files == null) {
            return;
        }
        try {
            BufferedImage baseBI = ScaleTools.demoImage(baseImage);
            BufferedImage overlayBI = ScaleTools.demoImage(overlay);
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + op;
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }
            PixelsBlend.ImagesBlendMode mode;
            PixelsBlend blender;
            BufferedImage blended;
            String tmpFile;
            for (String name : PixelsBlendFactory.blendModes()) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                mode = PixelsBlendFactory.blendMode(name);
                blender = PixelsBlendFactory.create(mode).setBlendMode(mode);
                blender.setWeight(1.0F).setBaseAbove(false).setBaseTransparentAs(
                        PixelsBlend.TransparentAs.Another).setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = PixelsBlend.blend(currentTask, overlayBI, baseBI, x, y, blender);
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity")
                        + "1_" + message("Overlay") + "_" + message("BaseImage"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                blender.setWeight(0.5F).setBaseAbove(false).setBaseTransparentAs(PixelsBlend.TransparentAs.Another)
                        .setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = PixelsBlend.blend(currentTask, overlayBI, baseBI, x, y, blender);
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity") + "0.5_"
                        + message("Overlay") + "_" + message("BaseImage"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                blender.setWeight(0.5F).setBaseAbove(false).setBaseTransparentAs(PixelsBlend.TransparentAs.Transparent)
                        .setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = PixelsBlend.blend(currentTask, overlayBI, baseBI, x, y, blender);
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity") + "0.5_"
                        + message("Transparent") + "_" + message("BaseImage"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                blender.setWeight(0.5F).setBaseAbove(false).setBaseTransparentAs(PixelsBlend.TransparentAs.Transparent)
                        .setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = PixelsBlend.blend(currentTask, overlayBI, baseBI, x, y, blender);
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity") + "0.5_"
                        + message("Transparent") + "_" + message("Transparent"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
            }
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

    public static void text(FxTask currentTask, List<String> files,
            BufferedImage baseImage, ControlImageText optionsController, File demoFile) {
        if (currentTask == null || baseImage == null || optionsController == null || files == null) {
            return;
        }
        try {
            String path = AppPaths.getGeneratedPath() + File.separator + "imageDemo"
                    + File.separator + message("Text");
            if (demoFile != null) {
                path += File.separator + demoFile.getName();
            } else {
                path += File.separator + "x";
            }
            PixelsBlend.ImagesBlendMode mode;
            PixelsBlend blend;
            BufferedImage blended;
            String tmpFile;
            for (String name : PixelsBlendFactory.blendModes()) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                mode = PixelsBlendFactory.blendMode(name);
                blend = PixelsBlendFactory.create(mode).setBlendMode(mode);
                blend.setWeight(1.0F).setBaseAbove(false).setBaseTransparentAs(
                        PixelsBlend.TransparentAs.Another).setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = ImageTextTools.addText(currentTask, baseImage, optionsController.setBlend(blend));
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity")
                        + "1_" + message("Overlay") + "_" + message("BaseImage"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                blend.setWeight(0.5F).setBaseAbove(false).setBaseTransparentAs(PixelsBlend.TransparentAs.Another)
                        .setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = ImageTextTools.addText(currentTask, baseImage, optionsController.setBlend(blend));
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity") + "0.5_"
                        + message("Overlay") + "_" + message("BaseImage"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                blend.setWeight(0.5F).setBaseAbove(false).setBaseTransparentAs(PixelsBlend.TransparentAs.Transparent)
                        .setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = ImageTextTools.addText(currentTask, baseImage, optionsController.setBlend(blend));
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity") + "0.5_"
                        + message("Transparent") + "_" + message("BaseImage"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                blend.setWeight(0.5F).setBaseAbove(false).setBaseTransparentAs(PixelsBlend.TransparentAs.Transparent)
                        .setOverlayTransparentAs(PixelsBlend.TransparentAs.Another);
                blended = ImageTextTools.addText(currentTask, baseImage, optionsController.setBlend(blend));
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.getPathTempFile(path, name + "-" + message("Opacity") + "0.5_"
                        + message("Transparent") + "_" + message("Transparent"), ".png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, blended, tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
            }
        } catch (Exception e) {
            if (currentTask != null) {
                currentTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
        }
    }

}
