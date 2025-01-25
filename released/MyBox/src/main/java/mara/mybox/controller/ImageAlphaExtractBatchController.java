package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import mara.mybox.image.tools.AlphaTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
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
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems()));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }

            BufferedImage source = ImageFileReaders.readImage(currentTask, srcFile);
            if (source == null) {
                if (currentTask.isWorking()) {
                    return message("Failed");
                } else {
                    return message("Canceled");
                }
            }
            BufferedImage[] targets = AlphaTools.extractAlpha(currentTask, source);
            if (targets == null) {
                if (currentTask.isWorking()) {
                    return message("Failed");
                } else {
                    return message("Canceled");
                }
            }
            String prefix = target.getParent() + File.separator + FileNameTools.prefix(target.getName());
            String noAlphaFileName = prefix + "_noAlpha." + targetFileSuffix;
            if (ImageFileWriters.writeImageFile(currentTask, targets[0], attributes, noAlphaFileName)) {
                targetFileGenerated(new File(noAlphaFileName));
            } else if (currentTask.isWorking()) {
                return message("Failed");
            } else {
                return message("Canceled");
            }
            String alphaFileName = prefix + "_alpha.png";
            if (ImageFileWriters.writeImageFile(currentTask, targets[1], "png", alphaFileName)) {
                targetFileGenerated(new File(alphaFileName));
            } else if (currentTask.isWorking()) {
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
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return null;
    }

}
