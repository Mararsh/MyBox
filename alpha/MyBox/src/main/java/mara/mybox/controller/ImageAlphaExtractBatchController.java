package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
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
        TipsLabelKey = "ImageAlphaExtractTips";

        sourceExtensionFilter = FileFilters.AlphaImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
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
            String prefix = FileNameTools.getFilePrefix(target.getAbsolutePath());
            String alphaFileName = prefix + "_noAlpha." + targetFileSuffix;
            ImageFileWriters.writeImageFile(targets[0], attributes, alphaFileName);
            targetFileGenerated(new File(alphaFileName));

            String noAlphaFileName = prefix + "_alpha.png";
            ImageFileWriters.writeImageFile(targets[1], "png", noAlphaFileName);
            targetFileGenerated(new File(noAlphaFileName));

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
