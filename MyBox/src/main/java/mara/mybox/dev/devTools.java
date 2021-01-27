/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.dev;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import mara.mybox.controller.BaseController;
import mara.mybox.data.BaseTask;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 *
 * @author mara
 */
public class DevTools {

    public static List<Integer> installedVersion(Connection conn) {
        List<Integer> versions = new ArrayList<>();
        try {
            List<String> installed = TableStringValues.read(conn, "InstalledVersions");
            for (String v : installed) {
                versions.add(myboxVersion(v));
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
        return versions;
    }

    public static int lastVersion(Connection conn) {
        try {
            List<Integer> versions = installedVersion(conn);
            if (!versions.isEmpty()) {
                Collections.sort(versions);
                return versions.get(versions.size() - 1);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
        return 0;
    }

    public static int myboxVersion(String string) {
        try {
            String[] vs = string.split("\\.");
            switch (vs.length) {
                case 1:
                    return Integer.parseInt(vs[0]) * 1000000;
                case 2:
                    return Integer.parseInt(vs[0]) * 1000000 + Integer.parseInt(vs[1]) * 1000;
                case 3:
                    return Integer.parseInt(vs[0]) * 1000000 + Integer.parseInt(vs[1]) * 1000 + Integer.parseInt(vs[2]);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
        return 0;
    }

    public static String myboxVersion(int i) {
        try {
            int v1 = i / 1000000;
            int ii = i % 1000000;
            int v2 = ii / 1000;
            int v3 = ii % 1000;
            if (v3 == 0) {
                return v1 + "." + v2;
            } else {
                return v1 + "." + v2 + "." + v3;
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
        return i + "";
    }

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

                    List<String> prefix = Arrays.asList(
                            "iconChina", "iconMyBox", "iconRGB", "iconSaveAs",
                            "iconHue", "iconColorWheel", "iconColor", "iconButterfly", "iconPalette",
                            "iconMosaic", "iconBlackWhite", "iconGrayscale", "iconMap", "iconSynchronize"
                    );
                    List<String> keeps = new ArrayList<>();
                    for (String name : prefix) {
                        keeps.add(name + ".png");
                        keeps.add(name + "_100.png");
                    }
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
                    MyBoxLog.error(e.toString());
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                parentController.popInformation(message("TakeEffectWhenReboot"));
            }

        };
        return task;
    }

    public static String getFileName() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getFileName();
    }

    public static String getMethodName() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getMethodName();
    }

    public static String getClassName() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getClassName();
    }

    public static int getLineNumber() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getLineNumber();
    }

}
