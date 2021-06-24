package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import javax.imageio.ImageIO;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-20
 * @License Apache License Version 2.0
 */
public class MyBoxIconsController extends BaseTaskController {

    @FXML
    protected ControlFileSelecter sourceCodesPathController;

    public MyBoxIconsController() {
        baseTitle = message("MakeIcons");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            sourceCodesPathController.label(message("sourceCodesPath"))
                    .isDirectory(true).isSource(false).mustExist(true).permitNull(true)
                    .defaultValue("win".equals(SystemTools.os()) ? "D:\\MyBox" : "/home/mara/mybox")
                    .name("SourceCodesPath", true);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void startAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            startButton.setDisable(true);
            initLogs();
            tabPane.getSelectionModel().select(logsTab);
            startTask();
        }
    }

    @Override
    protected boolean doTask() {
        try {
            String saved = AppVariables.getUserConfigValue("SourceCodesPath", null);
            if (saved == null) {
                parentController.popError(message("MissSourceCodesPath"));
                return false;
            }
            String srcPath = saved + "/src/main/resources/";
            String lightBluePath = srcPath + ControlStyle.ButtonsPath + "LightBlue/";
            if (!new File(lightBluePath).exists()) {
                parentController.popError(message("WrongSourceCodesPath"));
                return false;
            }
            updateLogs(srcPath + ControlStyle.ButtonsPath);
            String redPath = srcPath + ControlStyle.ButtonsPath + "Red/";
            FileTools.clearDir(new File(redPath));
            String pinkPath = srcPath + ControlStyle.ButtonsPath + "Pink/";
            FileTools.clearDir(new File(pinkPath));
            String orangePath = srcPath + ControlStyle.ButtonsPath + "Orange/";
            FileTools.clearDir(new File(orangePath));
            String bluePath = srcPath + ControlStyle.ButtonsPath + "Blue/";
            FileTools.clearDir(new File(bluePath));

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
                updateLogs(message("SourceFile") + ": " + icon.getAbsolutePath());
                src = ImageIO.read(icon);
                redOperation.setImage(src);
                ImageFileWriters.writeImageFile(redOperation.operate(), "png", redPath + filename);
                updateLogs(MessageFormat.format(message("FilesGenerated"), redPath + filename));

                pinkOperation.setImage(src);
                ImageFileWriters.writeImageFile(pinkOperation.operate(), "png", pinkPath + filename);
                updateLogs(MessageFormat.format(message("FilesGenerated"), pinkPath + filename));

                orangeOperation.setImage(src);
                ImageFileWriters.writeImageFile(orangeOperation.operate(), "png", orangePath + filename);
                updateLogs(MessageFormat.format(message("FilesGenerated"), orangePath + filename));

                blueOperation.setImage(src);
                ImageFileWriters.writeImageFile(blueOperation.operate(), "png", bluePath + filename);
                updateLogs(MessageFormat.format(message("FilesGenerated"), bluePath + filename));
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void afterSuccess() {
        if (parentController != null) {
            parentController.popInformation(message("TakeEffectWhenReboot"));
        } else {
            popInformation(message("TakeEffectWhenReboot"));
        }
        startButton.setDisable(false);
    }

}
