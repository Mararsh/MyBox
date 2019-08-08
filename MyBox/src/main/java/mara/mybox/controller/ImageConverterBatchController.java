package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.controller.base.ImagesBatchController;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.AppVaribles.setUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterBatchController extends ImagesBatchController {

    protected final String ImageConverterAppendColorKey, ImageConverterAppendCompressionKey, ImageConverterAppendQualityKey;
    protected ImageAttributes attributes;

    @FXML
    protected ImageConverterOptionsController optionsController;
    @FXML
    protected CheckBox appendColorCheck, appendCompressionCheck, appendQualityCheck;

    public ImageConverterBatchController() {
        baseTitle = AppVaribles.message("ImageConverterBatch");
        browseTargets = true;

        ImageConverterAppendColorKey = "ImageConverterDitherKey";
        ImageConverterAppendCompressionKey = "ImageConverterAppendCompressionKey";
        ImageConverterAppendQualityKey = "ImageConverterAppendQualityKey";

    }

    @Override
    public void initializeNext() {
        try {

            super.initializeNext();

            optionsController.initDpiBox(false);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.qualitySelector.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(optionsController.profileInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.thresholdInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            appendColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(ImageConverterAppendColorKey, appendColorCheck.isSelected());
                }
            });
            appendColorCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageConverterAppendColorKey));

            appendCompressionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(ImageConverterAppendCompressionKey, appendCompressionCheck.isSelected());
                }
            });
            appendCompressionCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageConverterAppendCompressionKey));

            appendQualityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    setUserConfigValue(ImageConverterAppendQualityKey, appendQualityCheck.isSelected());
                }
            });
            appendQualityCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageConverterAppendQualityKey));

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        attributes = optionsController.attributes;

        return true;
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVaribles.message("Skip");
            }
            if (ImageConvert.convertColorSpace(srcFile, attributes, target)) {
                actualParameters.finalTargetName = target.getAbsolutePath();
                targetFiles.add(target);
                return AppVaribles.message("Successful");
            } else {
                return AppVaribles.message("Failed");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVaribles.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(srcFile.getName());
            String nameSuffix = "";
            if (srcFile.isFile()) {
                if (appendColorCheck.isSelected()) {
                    if (message("IccProfile").equals(attributes.getColorSpaceName())) {
                        namePrefix += "_" + attributes.getProfileName();
                    } else {
                        namePrefix += "_" + attributes.getColorSpaceName();
                    }
                }
                if (attributes.getCompressionType() != null) {
                    if (appendCompressionCheck.isSelected()) {
                        namePrefix += "_" + attributes.getCompressionType();
                    }
                    if (appendQualityCheck.isSelected()) {
                        namePrefix += "_quality-" + attributes.getQuality() + "%";
                    }
                }
                namePrefix = namePrefix.replace(" ", "_");
                nameSuffix = "." + attributes.getImageFormat();
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
