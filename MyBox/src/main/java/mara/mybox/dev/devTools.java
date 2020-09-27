/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.dev;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import mara.mybox.controller.BaseController;
import mara.mybox.data.BaseTask;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 *
 * @author mara
 */
public class devTools {

    public static BaseTask<Void> makeIconsTask(BaseController parentController) {
        String saved = AppVariables.getUserConfigValue("SourceCodesPath", null);
        if (saved == null) {
            parentController.popError(message("MissSourceCodesPath"));
            return null;
        }
        String srcPath = saved + "/src/main/resources/";
        if (!new File(srcPath + "buttonsLightBlue/").exists()) {
            parentController.popError(message("WrongSourceCodesPath"));
            return null;
        }
        if (parentController.getTask() != null) {
            return null;
        }
        BaseTask<Void> task = new BaseTask<Void>() {

            @Override
            protected boolean handle() {
                try {
                    String lightBluePath = srcPath + "buttonsLightBlue/";
                    String redPath = srcPath + "buttons/";
                    FileTools.clearDir(new File(redPath));
                    String pinkPath = srcPath + "buttonsPink/";
                    FileTools.clearDir(new File(pinkPath));
                    String orangePath = srcPath + "buttonsOrange/";
                    FileTools.clearDir(new File(orangePath));
                    String bluePath = srcPath + "buttonsBlue/";
                    FileTools.clearDir(new File(bluePath));

                    List<String> keeps = Arrays.asList(
                            "iconChina.png", "iconMyBox.png", "iconRGB.png", "iconWOW.png", "iconSaveAs.png",
                            "iconHue.png", "iconColorWheel.png", "iconColor.png", "iconButterfly.png", "iconPalette.png",
                            "iconMosaic.png", "iconBlackWhite.png", "iconGrayscale.png", "iconMap.png"
                    );
                    File[] icons = new File(lightBluePath).listFiles();
                    BufferedImage src = null;
                    ImageScope scope = new ImageScope();
                    PixelsOperation redOperation = PixelsOperation.create(src, scope,
                            PixelsOperation.OperationType.Hue, PixelsOperation.ColorActionType.Decrease);
                    redOperation.setFloatPara1(215 / 360.0f);
                    PixelsOperation pinkOperation = PixelsOperation.create(src, scope,
                            PixelsOperation.OperationType.Red, PixelsOperation.ColorActionType.Increase);
                    pinkOperation.setIntPara1(151);
                    PixelsOperation orangeOperation = PixelsOperation.create(src, scope,
                            PixelsOperation.OperationType.Hue, PixelsOperation.ColorActionType.Increase);
                    orangeOperation.setFloatPara1(171 / 360.0f);
                    PixelsOperation blueOperation = PixelsOperation.create(src, scope,
                            PixelsOperation.OperationType.Saturation, PixelsOperation.ColorActionType.Increase);
                    blueOperation.setFloatPara1(0.5f);
                    String filename;
                    for (File icon : icons) {
                        filename = icon.getName();
                        if (!filename.startsWith("icon") || !filename.endsWith(".png")) {
                            continue;
                        }
                        src = ImageIO.read(icon);
                        if (keeps.contains(filename)) {
                            FileTools.copyFile(icon, new File(redPath + filename));
                            FileTools.copyFile(icon, new File(pinkPath + filename));
                            FileTools.copyFile(icon, new File(orangePath + filename));
                            FileTools.copyFile(icon, new File(bluePath + filename));
                            continue;
                        }
                        redOperation.setImage(src);
                        ImageFileWriters.writeImageFile(redOperation.operate(), "png", redPath + filename);

                        pinkOperation.setImage(src);
                        ImageFileWriters.writeImageFile(pinkOperation.operate(), "png", pinkPath + filename);

                        orangeOperation.setImage(src);
                        ImageFileWriters.writeImageFile(orangeOperation.operate(), "png", orangePath + filename);

                        blueOperation.setImage(src);
                        ImageFileWriters.writeImageFile(blueOperation.operate(), "png", bluePath + filename);

                    }

//                        String targetPath = rootPath + "/target/classes/";
//                        if (new File(targetPath).exists()) {
//                            FileTools.copyWholeDirectory(new File(srcPath + "buttonsLightBlue"), new File(targetPath + "buttonsLightBlue"));
//                            FileTools.copyWholeDirectory(new File(srcPath + "buttons"), new File(targetPath + "buttons"));
//                            FileTools.copyWholeDirectory(new File(srcPath + "buttonsPink"), new File(targetPath + "buttonsPink"));
//                            FileTools.copyWholeDirectory(new File(srcPath + "buttonsOrange"), new File(targetPath + "buttonsOrange"));
//                            FileTools.copyWholeDirectory(new File(srcPath + "buttonsBlue"), new File(targetPath + "buttonsBlue"));
//                        }
                } catch (Exception e) {
                    logger.error(e.toString());
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                parentController.popInformation(message("TakeEffectNextTime"));
            }

            @Override
            protected void taskQuit() {
                parentController.setTask(null);
            }
        };
        return task;
    }

}
