package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @License Apache License Version 2.0
 */
public abstract class BaseImageEditBatchController extends BaseBatchImageController {

    protected ImageAttributes attributes;
    protected String errorString;

    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected Button browseButton;
    @FXML
    protected CheckBox handleTransparentCheck;

    protected abstract BufferedImage handleImage(BufferedImage source);

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (formatController != null) {
                formatController.setParameters(this, false);
            }
            if (browseButton != null) {
                browseButton.setDisable(true);
            }
            if (handleTransparentCheck != null) {
                handleTransparentCheck.setDisable(UserConfig.getBoolean(baseName + "HandleTransparent", false));
                handleTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(baseName + "HandleTransparent", nv);
                    }
                });
            }

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            if (browseButton != null) {
                browseButton.setDisable(targetFiles == null || targetFiles.isEmpty());
            }

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
            if (browseButton != null) {
                browseButton.setDisable(false);
            }
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return Languages.message("Failed");
        }
    }

}
