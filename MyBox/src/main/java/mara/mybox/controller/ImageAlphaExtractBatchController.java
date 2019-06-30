package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import mara.mybox.controller.base.ImageManufactureBatchController;
import javafx.beans.binding.Bindings;
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
        baseTitle = AppVaribles.getMessage("ImageAlphaExtract");

        operationType = VisitHistory.OperationType.Alpha;
        TipsLabelKey = "ImageAlphaExtractTips";

        fileExtensionFilter = CommonValues.AlphaImageExtensionFilter;

    }

    @Override
    public void initializeNext2() {
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
    protected String writeCurrentFile() {
        try {
            BufferedImage source = ImageFileReaders.readImage(currentParameters.sourceFile);
            BufferedImage[] targets = ImageManufacture.extractAlpha(source);
            if (targets == null) {
                return AppVaribles.getMessage("Failed");
            }
            targetFormat = fileType;
            if (targetFormat == null) {
                targetFormat = FileTools.getFileSuffix(currentParameters.sourceFile.getName());
            }
            String alphaFileName = FileTools.getFilePrefix(actualParameters.finalTargetName)
                    + "_noAlpha." + targetFormat;
            ImageFileWriters.writeImageFile(targets[0], targetFormat, alphaFileName);
            targetFiles.add(new File(alphaFileName));

            String noAlphaFileName = FileTools.getFilePrefix(actualParameters.finalTargetName)
                    + "_alpha." + targetFormat;
            ImageFileWriters.writeImageFile(targets[1], "png", noAlphaFileName);
            targetFiles.add(new File(noAlphaFileName));

            return AppVaribles.getMessage("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVaribles.getMessage("Failed");
        }
    }

}
