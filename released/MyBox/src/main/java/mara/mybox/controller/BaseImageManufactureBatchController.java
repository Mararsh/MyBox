package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @License Apache License Version 2.0
 */
public abstract class BaseImageManufactureBatchController extends BaseBatchImageController {

    protected ImageAttributes attributes;
    protected String errorString;

    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected Button browseButton;

    protected abstract BufferedImage handleImage(BufferedImage source);

    @Override
    public void initTargetSection() {
        super.initTargetSection();

        if (formatController != null) {
            formatController.setParameters(this, false);
        }
        if (browseButton != null) {
            browseButton.setDisable(true);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (formatController != null) {
            attributes = formatController.getAttributes();
            targetFileSuffix = attributes.getImageFormat();
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            browseButton.setDisable(targetFiles == null || targetFiles.isEmpty());

            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            BufferedImage sourceImage = ImageFileReaders.readImage(srcFile);
            BufferedImage targetImage = handleImage(sourceImage);
            if (targetImage == null) {
                if (errorString != null) {
                    return errorString;
                } else {
                    return Languages.message("Failed");
                }
            }
            ImageFileWriters.writeImageFile(targetImage, attributes, target.getAbsolutePath());

            targetFileGenerated(target);
            browseButton.setDisable(false);
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

}
