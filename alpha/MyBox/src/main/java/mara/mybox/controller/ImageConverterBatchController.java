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
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.value.Languages.message;
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
        baseTitle = message("ImageConverterBatch");
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
            MyBoxLog.error(e);
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
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeActualParameters() {
        attributes = formatController.attributes;
        return super.makeActualParameters();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            if (ImageConvertTools.convertColorSpace(currentTask, srcFile, attributes, target)) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                if (currentTask.isWorking()) {
                    return message("Failed");
                } else {
                    return message("Canceled");
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
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
            MyBoxLog.error(e);
            return null;
        }
    }

}
