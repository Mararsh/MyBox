package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppPaths;
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
        initFileSelecter();
    }

    public final ControlTargetFile initFileSelecter() {
        initSelecter();
        isSource = false;
        isDirectory = false;
        checkQuit = false;
        permitNull = false;
        mustExist = false;
        return this;
    }

    @Override
    public ControlFileSelecter initFile() {
        super.initFile();
        initTargetExistType();
        return this;
    }

    public void initTargetExistType() {
        try {
            try {
                targetExistType = TargetExistType.valueOf(UserConfig.getString(baseName + "TargetExistType", "Rename"));
            } catch (Exception e) {
            }
            if (targetExistType == null) {
                targetExistType = TargetExistType.Rename;
            }
            switch (targetExistType) {
                case Replace:
                    targetReplaceRadio.setSelected(true);
                    break;
                case Skip:
                    targetSkipRadio.setSelected(true);
                    break;
                default:
                    targetRenameRadio.setSelected(true);
                    break;
            }
            targetExistGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkTargetExistType();
                }
            });

            targetNameAppend = UserConfig.getString(baseName + "TargetExistAppend", "_m");
            if (targetNameAppend == null || targetNameAppend.isEmpty()) {
                targetNameAppend = "_m";
            }
            targetAppendInput.setText(targetNameAppend);
            targetAppendInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkTargetExistType();
                }
            });

            appendTimestampCheck.setSelected(UserConfig.getBoolean(baseName + "AppendTimestamp", false));
            appendTimestampCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "AppendTimestamp", appendTimestampCheck.isSelected());
                }
            });

            checkTargetExistType();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public void checkTargetExistType() {
        targetAppendInput.setStyle(null);
        targetReplaceRadio.setStyle(null);
        if (targetReplaceRadio.isSelected()) {
            targetExistType = TargetExistType.Replace;
            targetReplaceRadio.setStyle(NodeStyleTools.darkRedTextStyle());

        } else if (targetRenameRadio.isSelected()) {
            targetExistType = TargetExistType.Rename;
            String a = targetAppendInput.getText();
            if (a == null || a.isEmpty()) {
                targetAppendInput.setStyle(UserConfig.badStyle());
            } else {
                targetNameAppend = a;
                UserConfig.setString(baseName + "TargetExistAppend", a);
            }

        } else if (targetSkipRadio.isSelected()) {
            targetExistType = TargetExistType.Skip;
        }
        UserConfig.setString(baseName + "TargetExistType", targetExistType.name());
    }

    public void setFile(int type, String name, String prefix, String ext) {
        defaultFile = null;
        setFileType(type);
        baseName = name;
        inputFile(FileTmpTools.generateFile(prefix, ext));
    }

    @Override
    public File makeTargetFile(String namePrefix, String nameSuffix, File targetPath) {
        try {
            if (targetExistType == null) {
                initTargetExistType();
            }
            if (namePrefix == null) {
                namePrefix = "mf";
            }
            if (nameSuffix == null) {
                nameSuffix = "";
            }
            if (targetPath == null) {
                targetPath = new File(AppPaths.getGeneratedPath());
            }
            String targetPrefix = targetPath.getAbsolutePath() + File.separator
                    + FileNameTools.filter(namePrefix);
            if (appendTimestampCheck == null || appendTimestampCheck.isSelected()) {
                targetPrefix += "_" + DateTools.nowFileString();
            }
            String targetSuffix = FileNameTools.filter(nameSuffix);
            File target = new File(targetPrefix + targetSuffix);
            if (target.exists()) {
                if (targetExistType == TargetExistType.Skip) {
                    target = null;
                } else if (targetExistType == TargetExistType.Rename) {
                    targetNameAppend = targetAppendInput.getText();
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
            MyBoxLog.debug(e);
            return null;
        }
    }

    public File makeTargetFile(File inFile) {
        if (inFile == null) {
            return FileTmpTools.getTempFile();
        }
        String filename = inFile.getAbsolutePath();
        return makeTargetFile(FileNameTools.prefix(filename),
                FileNameTools.suffix(filename),
                inFile.getParentFile());
    }

    @Override
    public File makeTargetFile() {
        return makeTargetFile(pickFile());
    }

    public boolean isSkip() {
        if (targetExistType == null) {
            initTargetExistType();
        }
        return targetExistType == TargetExistType.Skip;
    }

}
