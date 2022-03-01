package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterBatchController extends BaseBatchImageController {

    protected ImageAttributes attributes;

    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected CheckBox appendColorCheck, appendCompressionCheck, appendQualityCheck;

    public ImageConverterBatchController() {
        baseTitle = Languages.message("ImageConverterBatch");
        browseTargets = true;

    }

    @Override
    public void initControls() {
        try {

            super.initControls();

            formatController.setParameters(this, false);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
                    .or(targetPathController.valid.not())
                    .or(formatController.qualitySelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(formatController.profileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(formatController.binaryController.thresholdInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(formatController.icoWidthSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
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
                    UserConfig.setBoolean("ImageConverterAppendColor", appendColorCheck.isSelected());
                }
            });
            appendColorCheck.setSelected(UserConfig.getBoolean("ImageConverterAppendColor"));

            appendCompressionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean("ImageConverterAppendCompression", appendCompressionCheck.isSelected());
                }
            });
            appendCompressionCheck.setSelected(UserConfig.getBoolean("ImageConverterAppendCompression"));

            appendQualityCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v,
                        Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean("ImageConverterAppendQuality", appendQualityCheck.isSelected());
                }
            });
            appendQualityCheck.setSelected(UserConfig.getBoolean("ImageConverterAppendQuality"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        attributes = formatController.attributes;

        return true;
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            if (ImageConvertTools.convertColorSpace(srcFile, attributes, target)) {
                targetFileGenerated(target);
                return Languages.message("Successful");
            } else {
                return Languages.message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.prefix(srcFile.getName());
            String nameSuffix = "";
            if (srcFile.isFile()) {
                if (!"ico".equals(attributes.getImageFormat())) {
                    if (appendColorCheck.isSelected()) {
                        if (Languages.message("IccProfile").equals(attributes.getColorSpaceName())) {
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
