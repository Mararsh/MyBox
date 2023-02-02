package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javax.imageio.ImageIO;
import mara.mybox.bufferedimage.ImageFileInformation;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.SystemTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-20
 * @License Apache License Version 2.0
 */
public class MyBoxIconsController extends BaseBatchFileController {

    protected File srcRoot;
    protected String resourcePath, lightBluePath, redPath, pinkPath, orangePath, bluePath, darkGreenPath;
    protected PixelsOperation operation1, operation2;

    @FXML
    protected ControlPathInput sourceCodesPathController;

    public MyBoxIconsController() {
        baseTitle = message("MakeIcons");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            sourceCodesPathController.label(message("sourceCodesPath"))
                    .isDirectory(true).isSource(false).mustExist(true).permitNull(false)
                    .defaultFile("win".equals(SystemTools.os()) ? new File("D:\\MyBox") : new File("/home/mara/mybox"))
                    .baseName(baseName).savedName(baseName + "SourceCodesPath").init();

            sourceCodesPathController.notify.addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                checkPath();
            });
            checkPath();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public boolean checkPath() {
        try {
            tableData.clear();
            srcRoot = sourceCodesPathController.file();
            if (srcRoot == null) {
                popError(message("InvalidSourceCodesPath"));
                return false;
            }
            resourcePath = srcRoot + "/src/main/resources/";
            lightBluePath = resourcePath + StyleTools.ButtonsSourcePath + "LightBlue/";
            if (!new File(lightBluePath).exists()) {
                popError(message("WrongSourceCodesPath"));
                return false;
            }
            redPath = resourcePath + StyleTools.ButtonsSourcePath + "Red/";
            pinkPath = resourcePath + StyleTools.ButtonsSourcePath + "Pink/";
            orangePath = resourcePath + StyleTools.ButtonsSourcePath + "Orange/";
            bluePath = resourcePath + StyleTools.ButtonsSourcePath + "Blue/";
            darkGreenPath = resourcePath + StyleTools.ButtonsSourcePath + "DarkGreen/";
            synchronized (this) {
                if (task != null) {
                    task.cancel();
                }
                task = new SingletonTask<Void>(this) {

                    private List<ImageInformation> infos;

                    @Override
                    protected boolean handle() {
                        File[] icons = new File(lightBluePath).listFiles();
                        infos = new ArrayList<>();
                        for (File file : icons) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            ImageFileInformation finfo = ImageFileInformation.create(file);
                            infos.addAll(finfo.getImagesInformation());
                        }
                        Collections.sort(infos, new Comparator<ImageInformation>() {
                            @Override
                            public int compare(ImageInformation v1, ImageInformation v2) {
                                long diff = v2.getModifyTime() - v1.getModifyTime();
                                if (diff == 0) {
                                    return 0;
                                } else if (diff > 0) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            }
                        });
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (!infos.isEmpty()) {
                            tableData.addAll(infos);
                            tableView.refresh();
                            popDone();
                        }
                    }

                };
                start(task);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            if (tableData == null || tableData.isEmpty()) {
                actualParameters = null;
                return false;
            }
            updateLogs(resourcePath + StyleTools.ButtonsSourcePath);
            if (tableView.getSelectionModel().getSelectedItem() == null) {
                FileDeleteTools.clearDir(new File(redPath));
                FileDeleteTools.clearDir(new File(pinkPath));
                FileDeleteTools.clearDir(new File(orangePath));
                FileDeleteTools.clearDir(new File(bluePath));
                FileDeleteTools.clearDir(new File(darkGreenPath));
            }
            return super.makeMoreParameters();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public String handleFile(File file) {
        try {
            if (task == null || task.isCancelled()) {
                return message("Canceled");
            }
            String filename = file.getName();
            if (!filename.startsWith("icon") || !filename.endsWith(".png")) {
                return message("Skip");
            }
            updateLogs(message("SourceFile") + ": " + file.getAbsolutePath());
            BufferedImage srcImage = ImageIO.read(file);

//            operation1 = PixelsOperationFactory.replaceColorOperation(srcImage,
//                    Colors.MyBoxDarkGreyBlue, Colors.MyBoxDarkGreyBlue, 20);
//            operation2 = PixelsOperationFactory.replaceColorOperation(srcImage,
//                    Colors.MyBoxGreyBlue, Colors.MyBoxGreyBlue, 20);
//
//            operation1.setImage(srcImage).setColorPara2(Colors.MyBoxDarkBlue);
//            operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightBlue);
//            ImageFileWriters.writeImageFile(operation2.operate(), "png", bluePath + filename);
//            targetFileGenerated(new File(bluePath + filename));
//
//            operation1.setImage(srcImage).setColorPara2(Colors.MyBoxDarkPink);
//            operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightPink);
//            ImageFileWriters.writeImageFile(operation2.operate(), "png", pinkPath + filename);
//            targetFileGenerated(new File(pinkPath + filename));
//
//            operation1.setImage(srcImage).setColorPara2(Colors.MyBoxDarkRed);
//            operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightRed);
//            ImageFileWriters.writeImageFile(operation2.operate(), "png", redPath + filename);
//            targetFileGenerated(new File(redPath + filename));
//
//            operation1.setImage(srcImage).setColorPara2(Colors.MyBoxOrange);
//            operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightOrange);
//            ImageFileWriters.writeImageFile(operation2.operate(), "png", orangePath + filename);
//            targetFileGenerated(new File(orangePath + filename));
//
//            operation1.setImage(srcImage).setColorPara2(Colors.MyBoxDarkGreen);
//            operation2.setImage(operation1.operate()).setColorPara2(Colors.MyBoxLightGreen);
//            ImageFileWriters.writeImageFile(operation2.operate(), "png", darkGreenPath + filename);
//            targetFileGenerated(new File(darkGreenPath + filename));
            return message("Successful");
        } catch (Exception e) {
            return file + " " + e.toString();
        }
    }

    public void makeIcon(BufferedImage srcImage, PixelsOperation p1, PixelsOperation p2) {
        SoundTools.miao3();
        popInformation(message("TakeEffectWhenReboot"));
    }

    @Override
    public void donePost() {
        SoundTools.miao3();
        popInformation(message("TakeEffectWhenReboot"));
    }

}
