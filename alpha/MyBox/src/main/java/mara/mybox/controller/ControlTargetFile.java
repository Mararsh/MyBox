package mara.mybox.controller;

import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlTargetFile extends ControlFileSelecter {

    protected TargetExistType targetExistType;
    protected String targetNameAppend;

    public static enum TargetExistType {
        Rename, Replace, Skip
    }

    @FXML
    protected ToggleGroup targetExistGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected TextField targetAppendInput;
    @FXML
    protected CheckBox appendTimestampCheck;

    public ControlTargetFile() {
        isSource = false;
        isDirectory = false;
        checkQuit = false;
        permitNull = false;
        mustExist = false;
        notify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        super.initControls();
        targetExistGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                checkTargetExistType();
            }
        });
        isSettingValues = true;
        NodeTools.setRadioSelected(targetExistGroup, UserConfig.getString(baseName + "TargetExistType", message("Replace")));
        if (targetAppendInput != null) {
            targetAppendInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkTargetExistType();
                }
            });
            targetAppendInput.setText(UserConfig.getString(baseName + "TargetExistAppend", "_m"));
        }
        isSettingValues = false;
        checkTargetExistType();
    }

    public void checkTargetExistType() {
        if (isSettingValues) {
            return;
        }
        if (targetAppendInput != null) {
            targetAppendInput.setStyle(null);
        }

        RadioButton selected = (RadioButton) targetExistGroup.getSelectedToggle();
        if (selected.equals(targetReplaceRadio)) {
            targetExistType = TargetExistType.Replace;

        } else if (selected.equals(targetRenameRadio)) {
            targetExistType = TargetExistType.Rename;
            if (targetAppendInput != null) {
                if (targetAppendInput.getText() == null || targetAppendInput.getText().trim().isEmpty()) {
                    targetAppendInput.setStyle(UserConfig.badStyle());
                } else {
                    UserConfig.setString(baseName + "TargetExistAppend", targetAppendInput.getText().trim());
                }
            }

        } else if (selected.equals(targetSkipRadio)) {
            targetExistType = TargetExistType.Skip;
        }
        UserConfig.setString(baseName + "TargetExistType", selected.getText());
    }

    @Override
    public ControlTargetFile init() {
        String v = null;
        if (savedName != null) {
            v = UserConfig.getString(savedName, null);
        }
        if (v == null || v.isBlank()) {
            v = defaultFile != null ? defaultFile.getAbsolutePath() : null;
        } else if (!isDirectory) {
//            v = FileNameTools.appendName(v, "_m");
        }
        fileInput.setText(v);
        return this;
    }

    @Override
    public File makeTargetFile(String namePrefix, String nameSuffix, File targetPath) {
        try {
            String targetPrefix = targetPath.getAbsolutePath() + File.separator
                    + FileNameTools.filter(namePrefix);
            if (appendTimestampCheck != null && appendTimestampCheck.isSelected()) {
                targetPrefix += "_" + DateTools.nowFileString();
            }
            String targetSuffix = FileNameTools.filter(nameSuffix);
            File target = new File(targetPrefix + targetSuffix);
            if (target.exists()) {
                if (targetExistType == TargetExistType.Skip) {
                    target = null;
                } else if (targetExistType == TargetExistType.Rename) {
                    if (targetAppendInput != null) {
                        targetNameAppend = targetAppendInput.getText().trim();
                    }
                    if (targetNameAppend == null || targetNameAppend.isEmpty()) {
                        targetNameAppend = "_m";
                    }
                    while (true) {
                        targetPrefix = targetPrefix + targetNameAppend;
                        target = new File(targetPrefix + targetSuffix);
                        if (!target.exists()) {
                            break;
                        }
                    }
                }
            }
            if (target != null) {
                target.getParentFile().mkdirs();
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public boolean isSkip() {
        return targetExistType == TargetExistType.Skip;
    }

}
