package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageConvert;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.setUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterBatchController extends BaseBatchImageController {

    protected ImageAttributes attributes;

    @FXML
    protected ControlImageConverterOptions optionsController;
    @FXML
    protected CheckBox appendColorCheck, appendCompressionCheck, appendQualityCheck;

    public ImageConverterBatchController() {
        baseTitle = AppVariables.message("ImageConverterBatch");
        browseTargets = true;

    }

    @Override
    public void initControls() {
        try {

            super.initControls();

            optionsController.setValues(false);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.qualitySelector.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(optionsController.profileInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.thresholdInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.icoWidthSelector.getEditor().styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            appendColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    setUserConfigValue("ImageConverterAppendColor", appendColorCheck.isSelected());
                }
            });
            appendColorCheck.setSelected(AppVariables.getUserConfigBoolean("ImageConverterAppendColor"));

            appendCompressionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    setUserConfigValue("ImageConverterAppendCompression", appendCompressionCheck.isSelected());
                }
            });
            appendCompressionCheck.setSelected(AppVariables.getUserConfigBoolean("ImageConverterAppendCompression"));

            appendQualityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    setUserConfigValue("ImageConverterAppendQuality", appendQualityCheck.isSelected());
                }
            });
            appendQualityCheck.setSelected(AppVariables.getUserConfigBoolean("ImageConverterAppendQuality"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                return AppVariables.message("Skip");
            }
            if (ImageConvert.convertColorSpace(srcFile, attributes, target)) {
                targetFileGenerated(target);
                return AppVariables.message("Successful");
            } else {
                return AppVariables.message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(srcFile.getName());
            String nameSuffix = "";
            if (srcFile.isFile()) {
                if (!"ico".equals(attributes.getImageFormat())) {
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
                }
                namePrefix = namePrefix.replace(" ", "_");
                nameSuffix = "." + attributes.getImageFormat();
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
