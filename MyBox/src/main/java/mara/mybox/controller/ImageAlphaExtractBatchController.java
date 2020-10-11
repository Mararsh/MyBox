package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAlphaExtractBatchController extends ImageManufactureBatchController {

    public ImageAlphaExtractBatchController() {
        baseTitle = AppVariables.message("ImageAlphaExtract");

        operationType = VisitHistory.OperationType.Alpha;
        TipsLabelKey = "ImageAlphaExtractTips";

        sourceExtensionFilter = CommonFxValues.AlphaImageExtensionFilter;
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
            logger.debug(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            finalTargetName = target.getAbsolutePath();
            BufferedImage source = ImageFileReaders.readImage(srcFile);
            BufferedImage[] targets = ImageManufacture.extractAlpha(source);
            if (targets == null) {
                return AppVariables.message("Failed");
            }
            targetFormat = targetFileType;
            if (targetFormat == null) {
                targetFormat = FileTools.getFileSuffix(srcFile.getName());
            }
            if (targetFormat == null || !CommonValues.SupportedImages.contains(targetFormat)) {
                return AppVariables.message("Failed");
            }
            String alphaFileName = FileTools.getFilePrefix(finalTargetName)
                    + "_noAlpha." + targetFormat;
            ImageFileWriters.writeImageFile(targets[0], targetFormat, alphaFileName);
            targetFiles.add(new File(alphaFileName));

            String noAlphaFileName = FileTools.getFilePrefix(finalTargetName)
                    + "_alpha." + targetFormat;
            ImageFileWriters.writeImageFile(targets[1], "png", noAlphaFileName);
            targetFiles.add(new File(noAlphaFileName));

            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

}
