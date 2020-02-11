package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchController extends ImagesBatchController {

    protected String errorString, targetFormat;

    @FXML
    protected RadioButton pcxRadio;
    @FXML
    protected Button browseButton;

    public ImageManufactureBatchController() {

    }

    @Override
    public void initTargetSection() {
        super.initTargetSection();

        fileTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFileType();
            }
        });
        checkFileType();
        if (pcxRadio != null) {
            FxmlControl.setTooltip(pcxRadio, new Tooltip(message("PcxComments")));
        }

        browseButton.setDisable(true);
    }

    protected void checkFileType() {
        RadioButton selected = (RadioButton) fileTypeGroup.getSelectedToggle();
        if (message("OriginalType").equals(selected.getText())) {
            targetFileType = null;
        } else {
            targetFileType = selected.getText();
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            browseButton.setDisable(targetFiles == null || targetFiles.isEmpty());

            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            String targetName = target.getAbsolutePath();
            BufferedImage sourceImage = ImageFileReaders.readImage(srcFile);
            targetFormat = targetFileType;
            if (targetFormat == null) {
                targetFormat = FileTools.getFileSuffix(srcFile.getName());
            }
            BufferedImage targetImage = handleImage(sourceImage);
            if (targetImage == null) {
                if (errorString != null) {
                    return errorString;
                } else {
                    return AppVariables.message("Failed");
                }
            }
            ImageFileWriters.writeImageFile(targetImage, targetFormat, targetName);

            targetFileGenerated(target);
            browseButton.setDisable(false);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    // Methods should be implemented
    protected BufferedImage handleImage(BufferedImage source) {
        return null;
    }

}
