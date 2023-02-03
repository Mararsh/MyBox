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
import mara.mybox.fxml.style.StyleData.StyleColor;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.SystemTools;
import static mara.mybox.value.Colors.color;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-20
 * @License Apache License Version 2.0
 */
public class MyBoxIconsController extends BaseBatchFileController {

    protected File srcRoot;
    protected String resourcePath;

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
            String redPath = resourcePath + StyleTools.ButtonsSourcePath + "Red/";
            if (!new File(redPath).exists()) {
                popError(message("WrongSourceCodesPath"));
                return false;
            }
            synchronized (this) {
                if (task != null) {
                    task.cancel();
                }
                task = new SingletonTask<Void>(this) {

                    private List<ImageInformation> infos;

                    @Override
                    protected boolean handle() {
                        File[] icons = new File(redPath).listFiles();
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
                for (StyleColor style : StyleColor.values()) {
                    if (style == StyleColor.Red || style == StyleColor.Customize) {
                        continue;
                    }
                    FileDeleteTools.clearDir(new File(resourcePath + StyleTools.ButtonsSourcePath + style.name() + "/"));
                }
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

            for (StyleColor style : StyleColor.values()) {
                if (style == StyleColor.Red || style == StyleColor.Customize) {
                    continue;
                }
                BufferedImage image = StyleTools.makeIcon(srcImage, color(style, true), color(style, false));
                if (image == null) {
                    continue;
                }
                String tname = resourcePath + StyleTools.ButtonsSourcePath + style.name() + "/" + filename;
                ImageFileWriters.writeImageFile(image, "png", tname);
                targetFileGenerated(new File(tname));
            }
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
