package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javafx.stage.FileChooser;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEncodingBatchController extends FilesBatchController {

    protected boolean autoDetermine;
    protected FileEditInformation sourceInformation, targetInformation;

    @FXML
    protected ToggleGroup sourceGroup;
    @FXML
    protected ComboBox<String> sourceBox, targetBox;
    @FXML
    protected CheckBox targetBomCheck;

    public TextEncodingBatchController() {
        sourcePathKey = "TextFilePathKey";
        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*", "*.*"));
                add(new FileChooser.ExtensionFilter("txt", "*.txt"));
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };
    }

    @Override
    protected void initOptionsSection() {

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
        sourceBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceInformation.setCharset(Charset.forName(newValue));
            }
        });
        sourceBox.getSelectionModel().select(Charset.defaultCharset().name());
        checkSource();

        if (targetBox != null) {
            targetBox.getItems().addAll(setNames);
            targetBox.valueProperty().addListener(new ChangeListener<String>() {
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
            FxmlTools.quickTooltip(targetBomCheck, tips);
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
    protected String handleCurrentFile(FileInformation d) {
        File file = d.getFile();
        currentParameters.sourceFile = file;
        String filename = file.getName();
        currentParameters.finalTargetName = currentParameters.targetPath
                + File.separator + filename;
        boolean skip = false;
        if (targetExistType == TargetExistType.Rename) {
            while (new File(currentParameters.finalTargetName).exists()) {
                filename = FileTools.getFilePrefix(filename)
                        + targetSuffixInput.getText().trim() + "." + FileTools.getFileSuffix(filename);
                currentParameters.finalTargetName = currentParameters.targetPath
                        + File.separator + filename;
            }
        } else if (targetExistType == TargetExistType.Skip) {
            if (new File(currentParameters.finalTargetName).exists()) {
                skip = true;
            }
        }
        if (!skip) {
            return handleFile(currentParameters.sourceFile, new File(currentParameters.finalTargetName));
        } else {
            return AppVaribles.getMessage("Skip");
        }
    }

    @Override
    protected String handleCurrentDirectory(FileInformation d) {
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
                if (task.isCancelled()) {
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
                } else if (subDirCheck.isSelected()) {
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
