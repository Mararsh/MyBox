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
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAlphaExtractBatchController extends BaseImageManufactureBatchController {

    public ImageAlphaExtractBatchController() {
        baseTitle = Languages.message("ImageAlphaExtract");

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
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }

            BufferedImage source = ImageFileReaders.readImage(srcFile);
            BufferedImage[] targets = AlphaTools.extractAlpha(source);
            if (targets == null) {
                return Languages.message("Failed");
            }
            String prefix = target.getParent() + File.separator + FileNameTools.prefix(target.getName());
            String noAlphaFileName = prefix + "_noAlpha." + targetFileSuffix;
            ImageFileWriters.writeImageFile(targets[0], attributes, noAlphaFileName);
            targetFileGenerated(new File(noAlphaFileName));

            String alphaFileName = prefix + "_alpha.png";
            ImageFileWriters.writeImageFile(targets[1], "png", alphaFileName);
            targetFileGenerated(new File(alphaFileName));

            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        return null;
    }

}
