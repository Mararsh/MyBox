package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.fxml.FXML;
import javax.imageio.ImageIO;
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

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void goAction() {
        if (task != null) {
            task.cancel();
        }
        srcRoot = sourceCodesPathController.file();
        if (srcRoot == null) {
            popError(message("InvalidSourceCodesPath"));
            return;
        }
        resourcePath = srcRoot + "/src/main/resources/";
        String redPath = resourcePath + StyleTools.ButtonsSourcePath + "Red/";
        if (!new File(redPath).exists()) {
            popError(message("WrongSourceCodesPath"));
            return;
        }
        task = new SingletonTask<Void>(this) {
            private List<File> icons = null;

            @Override
            protected boolean handle() {
                try {
                    icons = Arrays.asList(new File(redPath).listFiles());
                    Collections.sort(icons, new Comparator<File>() {
                        @Override
                        public int compare(File v1, File v2) {
                            long diff = v2.lastModified() - v1.lastModified();
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
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void taskQuit() {
                tableController.addFiles(0, icons);
                task = null;
            }
        };
        start(task);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            if (tableData == null || tableData.isEmpty()) {
                actualParameters = null;
                return false;
            }
            updateLogs(resourcePath + StyleTools.ButtonsSourcePath);
            if (tableController.isNoneSelected()) {
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
                targetFileGenerated(new File(tname), false);
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
