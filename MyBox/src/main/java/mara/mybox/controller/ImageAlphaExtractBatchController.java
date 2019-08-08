package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import mara.mybox.controller.base.ImageManufactureBatchController;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAlphaExtractBatchController extends ImageManufactureBatchController {

    public ImageAlphaExtractBatchController() {
        baseTitle = AppVaribles.message("ImageAlphaExtract");

        operationType = VisitHistory.OperationType.Alpha;
        TipsLabelKey = "ImageAlphaExtractTips";

        sourceExtensionFilter = CommonValues.AlphaImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initializeNext() {
        try {
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
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVaribles.message("Skip");
            }
            actualParameters.finalTargetName = target.getAbsolutePath();
            BufferedImage source = ImageFileReaders.readImage(srcFile);
            BufferedImage[] targets = ImageManufacture.extractAlpha(source);
            if (targets == null) {
                return AppVaribles.message("Failed");
            }
            targetFormat = fileType;
            if (targetFormat == null) {
                targetFormat = FileTools.getFileSuffix(srcFile.getName());
            }
            if (targetFormat == null || !CommonValues.SupportedImages.contains(targetFormat)) {
                return AppVaribles.message("Failed");
            }
            String alphaFileName = FileTools.getFilePrefix(actualParameters.finalTargetName)
                    + "_noAlpha." + targetFormat;
            ImageFileWriters.writeImageFile(targets[0], targetFormat, alphaFileName);
            targetFiles.add(new File(alphaFileName));

            String noAlphaFileName = FileTools.getFilePrefix(actualParameters.finalTargetName)
                    + "_alpha." + targetFormat;
            ImageFileWriters.writeImageFile(targets[1], "png", noAlphaFileName);
            targetFiles.add(new File(noAlphaFileName));

            return AppVaribles.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVaribles.message("Failed");
        }
    }

}
