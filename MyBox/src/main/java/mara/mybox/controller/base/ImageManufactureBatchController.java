package mara.mybox.controller.base;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import mara.mybox.data.FileInformation;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.file.ImageFileReaders;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchController extends ImageBatchBaseController {

    protected String fileType, errorString, targetFormat;

    @FXML
    protected RadioButton pcxRadio;
    @FXML
    protected Button browseButton;

    public ImageManufactureBatchController() {

    }

    @Override
    public void initTargetSection() {
        super.initTargetSection();

        FxmlControl.quickTooltip(pcxRadio, new Tooltip(getMessage("PcxComments")));

        fileTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFileType();
            }
        });
        checkFileType();

        startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(tableView.getItems()))
        );

        browseButton.setDisable(true);
    }

    protected void checkFileType() {
        RadioButton selected = (RadioButton) fileTypeGroup.getSelectedToggle();
        if (getMessage("OriginalType").equals(selected.getText())) {
            fileType = null;
        } else {
            fileType = selected.getText();
        }
    }

    @Override
    public void makeMoreParameters() {
        makeBatchParameters();

    }

    @Override
    public String handleCurrentFile(FileInformation file) {
        if (currentParameters.sourceFile == null) {
            return AppVaribles.getMessage("NotFound");
        }
        String sourceName = currentParameters.sourceFile.getName();
        if (fileType != null) {
            sourceName = FileTools.replaceFileSuffix(sourceName, fileType);
        }
        String targetName = currentParameters.targetPath + File.separator + sourceName;
        boolean skip = false;
        String result = "";
        if (targetExistType == TargetExistType.Rename) {
            while (new File(targetName).exists()) {
                sourceName = FileTools.getFilePrefix(sourceName)
                        + targetSuffixInput.getText().trim() + "." + FileTools.getFileSuffix(sourceName);
                targetName = currentParameters.targetPath + File.separator + sourceName;
            }
        } else if (targetExistType == TargetExistType.Skip) {
            if (new File(targetName).exists()) {
                skip = true;
                result = AppVaribles.getMessage("Skip");
            }
        }
        if (!skip) {
            actualParameters.finalTargetName = targetName;
            result = writeCurrentFile();
        }
        browseButton.setDisable(targetFiles.isEmpty());
        return result;
    }

    protected String writeCurrentFile() {
        try {
            BufferedImage source = ImageFileReaders.readImage(currentParameters.sourceFile);
            targetFormat = fileType;
            if (targetFormat == null) {
                targetFormat = FileTools.getFileSuffix(currentParameters.sourceFile.getName());
            }
            BufferedImage target = handleImage(source);
            if (target == null) {
                if (errorString != null) {
                    return errorString;
                } else {
                    return AppVaribles.getMessage("Failed");
                }
            }
            ImageFileWriters.writeImageFile(target, targetFormat, actualParameters.finalTargetName);
            targetFiles.add(new File(actualParameters.finalTargetName));
            return AppVaribles.getMessage("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVaribles.getMessage("Failed");
        }
    }

    // Methods should be implemented
    protected BufferedImage handleImage(BufferedImage source) {
        return null;
    }

}
