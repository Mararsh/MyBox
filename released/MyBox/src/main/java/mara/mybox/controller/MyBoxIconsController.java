package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import javax.imageio.ImageIO;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.StyleTools;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Colors;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-5-20
 * @License Apache License Version 2.0
 */
public class MyBoxIconsController extends BaseTaskController {

    @FXML
    protected ControlFileSelecter sourceCodesPathController;

    public MyBoxIconsController() {
        baseTitle = Languages.message("MakeIcons");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            sourceCodesPathController.label(Languages.message("sourceCodesPath"))
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
            String saved = UserConfig.getString("SourceCodesPath", null);
            if (saved == null) {
                parentController.popError(Languages.message("MissSourceCodesPath"));
                return false;
            }
            String srcPath = saved + "/src/main/resources/";
            String lightBluePath = srcPath + StyleTools.ButtonsPath + "LightBlue/";
            if (!new File(lightBluePath).exists()) {
                parentController.popError(Languages.message("WrongSourceCodesPath"));
                return false;
            }
            updateLogs(srcPath + StyleTools.ButtonsPath);
            String redPath = srcPath + StyleTools.ButtonsPath + "Red/";
            FileDeleteTools.clearDir(new File(redPath));
            String pinkPath = srcPath + StyleTools.ButtonsPath + "Pink/";
            FileDeleteTools.clearDir(new File(pinkPath));
            String orangePath = srcPath + StyleTools.ButtonsPath + "Orange/";
            FileDeleteTools.clearDir(new File(orangePath));
            String bluePath = srcPath + StyleTools.ButtonsPath + "Blue/";
            FileDeleteTools.clearDir(new File(bluePath));
            String darkGreenPath = srcPath + StyleTools.ButtonsPath + "DarkGreen/";
            FileDeleteTools.clearDir(new File(darkGreenPath));

            File[] icons = new File(lightBluePath).listFiles();
            BufferedImage src = null;

            PixelsOperation operation1 = PixelsOperationFactory.replaceColorOperation(src,
                    Colors.MyBoxDarkGreyBlue, Colors.MyBoxDarkGreyBlue, 20);
            PixelsOperation operation2 = PixelsOperationFactory.replaceColorOperation(src,
                    Colors.MyBoxGreyBlue, Colors.MyBoxGreyBlue, 20);

            String filename;
            for (File icon : icons) {
                filename = icon.getName();
                if (!filename.startsWith("icon") || !filename.endsWith(".png")) {
                    continue;
                }
                updateLogs(Languages.message("SourceFile") + ": " + icon.getAbsolutePath());
                src = ImageIO.read(icon);

                operation1.setImage(src).setColorPara2(Colors.MyBoxDarkBlue);
                operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightBlue);
                ImageFileWriters.writeImageFile(operation2.operate(), "png", bluePath + filename);
                updateLogs(MessageFormat.format(Languages.message("FilesGenerated"), bluePath + filename));

                operation1.setImage(src).setColorPara2(Colors.MyBoxDarkPink);
                operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightPink);
                ImageFileWriters.writeImageFile(operation2.operate(), "png", pinkPath + filename);
                updateLogs(MessageFormat.format(Languages.message("FilesGenerated"), pinkPath + filename));

                operation1.setImage(src).setColorPara2(Colors.MyBoxDarkRed);
                operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightRed);
                ImageFileWriters.writeImageFile(operation2.operate(), "png", redPath + filename);
                updateLogs(MessageFormat.format(Languages.message("FilesGenerated"), redPath + filename));

                operation1.setImage(src).setColorPara2(Colors.MyBoxOrange);
                operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightOrange);
                ImageFileWriters.writeImageFile(operation2.operate(), "png", orangePath + filename);
                updateLogs(MessageFormat.format(Languages.message("FilesGenerated"), orangePath + filename));

                operation1.setImage(src).setColorPara2(Colors.MyBoxDarkGreen);
                operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightGreen);
                ImageFileWriters.writeImageFile(operation2.operate(), "png", darkGreenPath + filename);
                updateLogs(MessageFormat.format(Languages.message("FilesGenerated"), darkGreenPath + filename));

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
            parentController.popInformation(Languages.message("TakeEffectWhenReboot"));
        } else {
            popInformation(Languages.message("TakeEffectWhenReboot"));
        }
        startButton.setDisable(false);
    }

}
