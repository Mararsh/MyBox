package mara.mybox.controller;

import mara.mybox.controller.base.BatchBaseController;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileInformation;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEncodingBatchController extends BatchBaseController {

    protected boolean autoDetermine;
    protected FileEditInformation sourceInformation, targetInformation;

    @FXML
    protected ToggleGroup sourceGroup;
    @FXML
    protected ComboBox<String> sourceBox, targetBox;
    @FXML
    protected CheckBox targetBomCheck;

    public TextEncodingBatchController() {
        baseTitle = AppVaribles.getMessage("TextEncodingBatch");

        sourcePathKey = "TextFilePathKey";
        fileExtensionFilter = CommonValues.TextExtensionFilter;
    }

    @Override
    public void initializeNext2() {
        try {
            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(filesTableView.getItems()))
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {

        sourceInformation = FileEditInformation.newEditInformation(Edit_Type.Text);
        targetInformation = FileEditInformation.newEditInformation(Edit_Type.Text);

        sourceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSource();
            }
        });

        List<String> setNames = TextTools.getCharsetNames();
        sourceBox.getItems().addAll(setNames);
        sourceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceInformation.setCharset(Charset.forName(newValue));
            }
        });
        sourceBox.getSelectionModel().select(Charset.defaultCharset().name());
        checkSource();

        if (targetBox != null) {
            targetBox.getItems().addAll(setNames);
            targetBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    targetInformation.setCharset(Charset.forName(newValue));
                    if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                            || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                            || "UTF-32LE".equals(newValue)) {
                        targetBomCheck.setDisable(false);
                    } else {
                        targetBomCheck.setDisable(true);
                        if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                            targetBomCheck.setSelected(true);
                        } else {
                            targetBomCheck.setSelected(false);
                        }
                    }
                }
            });
            targetBox.getSelectionModel().select(Charset.defaultCharset().name());

            Tooltip tips = new Tooltip(AppVaribles.getMessage("BOMcomments"));
            tips.setFont(new Font(16));
            FxmlControl.quickTooltip(targetBomCheck, tips);
        }
    }

    protected void checkSource() {
        RadioButton selected = (RadioButton) sourceGroup.getSelectedToggle();
        if (getMessage("DetermainAutomatically").equals(selected.getText())) {
            autoDetermine = true;
            sourceBox.setDisable(true);
        } else {
            autoDetermine = false;
            sourceInformation.setCharset(Charset.forName(sourceBox.getSelectionModel().getSelectedItem()));
            sourceBox.setDisable(false);
        }
    }

    @Override
    public void makeMoreParameters() {
        makeBatchParameters();
    }

    @Override
    public String handleCurrentFile(FileInformation d) {
        if (currentParameters.sourceFile == null) {
            return AppVaribles.getMessage("NotFound");
        }
        String sourceName = currentParameters.sourceFile.getName();
        String targetName = currentParameters.targetPath + File.separator + sourceName;
        if (targetExistType == TargetExistType.Rename) {
            while (new File(targetName).exists()) {
                sourceName = FileTools.getFilePrefix(sourceName)
                        + targetSuffixInput.getText().trim() + "." + FileTools.getFileSuffix(sourceName);
                targetName = currentParameters.targetPath + File.separator + sourceName;
            }
            return handleFile(currentParameters.sourceFile, new File(targetName));
        } else if (targetExistType == TargetExistType.Skip) {
            return AppVaribles.getMessage("Skip");
        }
        return "";
    }

    @Override
    public String handleCurrentDirectory(FileInformation d) {
        File sourceDir = d.getFile();
        File targetDir = new File(currentParameters.targetPath + File.separator + sourceDir.getName());
        boolean skip = false;
        if (targetExistType == TargetExistType.Rename) {
            while (targetDir.exists()) {
                targetDir = new File(targetDir.getAbsolutePath() + targetSuffixInput.getText().trim());
            }
        } else if (targetExistType == TargetExistType.Skip) {
            if (targetDir.exists()) {
                skip = true;
            }
        }
        if (!skip) {
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            dirTotal = dirOk = 0;
            handleDirectory(sourceDir, targetDir);
            return MessageFormat.format(AppVaribles.getMessage("DirHandledSummary"), dirTotal, dirOk);
        } else {
            return AppVaribles.getMessage("Skip");
        }
    }

    protected void handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
            return;
        }
        try {
            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(sourcePath.listFiles()));
            String[] names = filesNameInput.getText().trim().split("\\s+");
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                File targetFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    dirTotal++;
                    String originalName = srcFile.getAbsolutePath();
                    if (filesNameCheck.isSelected() && names.length > 0) {
                        boolean isValid = false;
                        for (String name : names) {
                            if (FileTools.getFileName(originalName).contains(name)) {
                                isValid = true;
                                break;
                            }
                        }
                        if (!isValid) {
                            continue;
                        }
                    }
                    if (!AppVaribles.getMessage("Failed").equals(handleFile(srcFile, targetFile))) {
                        dirOk++;
                    }
                } else if (subdirCheck.isSelected()) {
                    targetFile.mkdirs();
                    handleDirectory(srcFile, targetFile);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected String handleFile(File srcFile, File targetFile) {
        try {
            sourceInformation.setFile(srcFile);
            if (autoDetermine) {
                boolean ok = TextTools.checkCharset(sourceInformation);
                if (!ok || sourceInformation == null) {
                    return AppVaribles.getMessage("Failed");
                }
            }
            targetInformation.setFile(targetFile);
            targetInformation.setWithBom(targetBomCheck.isSelected());
            if (TextTools.convertCharset(sourceInformation, targetInformation)) {
                actualParameters.finalTargetName = targetFile.getAbsolutePath();
                targetFiles.add(targetFile);
                return AppVaribles.getMessage("Successful");
            } else {
                return AppVaribles.getMessage("Failed");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVaribles.getMessage("Failed");
        }
    }

}
