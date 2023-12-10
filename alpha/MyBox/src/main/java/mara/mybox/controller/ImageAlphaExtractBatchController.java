package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAlphaExtractBatchController extends BaseImageEditBatchController {

    public ImageAlphaExtractBatchController() {
        baseTitle = message("ImageAlphaExtract");

        operationType = VisitHistory.OperationType.Alpha;

        sourceExtensionFilter = FileFilters.AlphaImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }

            BufferedImage source = ImageFileReaders.readImage(task, srcFile);
            if (source == null) {
                if (task.isWorking()) {
                    return message("Failed");
                } else {
                    return message("Canceled");
                }
            }
            BufferedImage[] targets = AlphaTools.extractAlpha(task, source);
            if (targets == null) {
                if (task.isWorking()) {
                    return message("Failed");
                } else {
                    return message("Canceled");
                }
            }
            String prefix = target.getParent() + File.separator + FileNameTools.prefix(target.getName());
            String noAlphaFileName = prefix + "_noAlpha." + targetFileSuffix;
            if (ImageFileWriters.writeImageFile(task, targets[0], attributes, noAlphaFileName)) {
                targetFileGenerated(new File(noAlphaFileName));
            } else if (task.isWorking()) {
                return message("Failed");
            } else {
                return message("Canceled");
            }
            String alphaFileName = prefix + "_alpha.png";
            if (ImageFileWriters.writeImageFile(task, targets[1], "png", alphaFileName)) {
                targetFileGenerated(new File(alphaFileName));
            } else if (task.isWorking()) {
                return message("Failed");
            } else {
                return message("Canceled");
            }
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        return null;
    }

}
