package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
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
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.TextEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @License Apache License Version 2.0
 */
public class TextFilesConvertController extends BaseBatchFileController {

    protected boolean sourceEncodingAutoDetermine, sameEncoding, sameBreak;
    protected TextEditInformation sourceInformation, targetInformation;
    protected int maxLines;

    @FXML
    protected ToggleGroup sourceEncodingGroup, targetEncodingGroup, lbGroup;
    @FXML
    protected ComboBox<String> sourceEncodingBox, targetEncodingBox, splitSelector;
    @FXML
    protected CheckBox targetBomCheck;

    public TextFilesConvertController() {
        baseTitle = message("TextConvertSplit");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(targetBomCheck, new Tooltip(message("BOMcomments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initOptionsSection() {

        sourceInformation = new TextEditInformation();
        targetInformation = new TextEditInformation();

        sourceEncodingGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSourceEncoding();
            }
        });

        List<String> setNames = TextTools.getCharsetNames();
        sourceEncodingBox.getItems().addAll(setNames);
        sourceEncodingBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceInformation.setCharset(Charset.forName(newValue));
            }
        });
        sourceEncodingBox.getSelectionModel().select(Charset.defaultCharset().name());
        checkSourceEncoding();

        targetEncodingGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkTargetEncoding();
            }
        });

        targetEncodingBox.getItems().addAll(setNames);
        targetEncodingBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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
        targetEncodingBox.getSelectionModel().select(Charset.defaultCharset().name());
        checkTargetEncoding();

        lbGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkLineBreak();
            }
        });
        checkLineBreak();

        maxLines = UserConfig.getInt(baseName + "LinesNumber", 1000);
        splitSelector.getItems().addAll(Arrays.asList(message("NotSplit"), "1000", "2000", "500", "1500", "3000", "5000", "10000"
        ));
        if (maxLines > 0) {
            splitSelector.setValue(maxLines + "");
        } else {
            splitSelector.setValue(message("NotSplit"));
        }
        splitSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                return;
            }
            if (message("NotSplit").equals(newValue)) {
                maxLines = -1;
                ValidationTools.setEditorNormal(splitSelector);
                UserConfig.setInt(baseName + "LinesNumber", -1);
                return;
            }
            try {
                int v = Integer.parseInt(newValue);
                if (v > 0) {
                    maxLines = v;
                    ValidationTools.setEditorNormal(splitSelector);
                    UserConfig.setInt(baseName + "LinesNumber", maxLines);
                } else {
                    ValidationTools.setEditorBadStyle(splitSelector);
                }
            } catch (Exception e) {
                ValidationTools.setEditorBadStyle(splitSelector);
            }
        });
    }

    protected void checkSourceEncoding() {
        RadioButton selected = (RadioButton) sourceEncodingGroup.getSelectedToggle();
        if (message("DetermineAutomatically").equals(selected.getText())) {
            sourceEncodingAutoDetermine = true;
            sourceEncodingBox.setDisable(true);
        } else {
            sourceEncodingAutoDetermine = false;
            sourceInformation.setCharset(Charset.forName(sourceEncodingBox.getSelectionModel().getSelectedItem()));
            sourceEncodingBox.setDisable(false);
        }
    }

    protected void checkTargetEncoding() {
        RadioButton selected = (RadioButton) targetEncodingGroup.getSelectedToggle();
        if (message("SameAsSourceFiles").equals(selected.getText())) {
            sameEncoding = true;
            targetEncodingBox.setDisable(true);
        } else {
            sameEncoding = false;
            targetInformation.setCharset(Charset.forName(targetEncodingBox.getSelectionModel().getSelectedItem()));
            targetEncodingBox.setDisable(false);
        }
    }

    protected void checkLineBreak() {
        RadioButton selected = (RadioButton) lbGroup.getSelectedToggle();
        sameBreak = false;
        if (message("SameAsSourceFiles").equals(selected.getText())) {
            sameBreak = true;;
        } else if (message("LF").equals(selected.getText())) {
            targetInformation.setLineBreak(FileEditInformation.Line_Break.LF);
        } else if (message("CR").equals(selected.getText())) {
            targetInformation.setLineBreak(FileEditInformation.Line_Break.CR);
        } else if (message("CRLF").equals(selected.getText())) {
            targetInformation.setLineBreak(FileEditInformation.Line_Break.CRLF);
        }
        if (!sameBreak) {
            targetInformation.setLineBreakValue(TextTools.lineBreakValue(targetInformation.getLineBreak()));
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            sourceInformation.setFile(srcFile);
            sourceInformation.setLineBreak(TextTools.checkLineBreak(currentTask, srcFile));
            sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (sourceEncodingAutoDetermine) {
                boolean ok = TextTools.checkCharset(sourceInformation);
                if (!ok || sourceInformation == null) {
                    return message("Failed");
                }
            }
            targetInformation.setFile(target);
            targetInformation.setWithBom(targetBomCheck.isSelected());
            if (sameEncoding) {
                targetInformation.setCharset(sourceInformation.getCharset());
            }
            if (sameBreak) {
                targetInformation.setLineBreak(sourceInformation.getLineBreak());
                targetInformation.setLineBreakValue(sourceInformation.getLineBreakValue());
            }

            List<File> files = TextTools.convert(currentTask,
                    sourceInformation, targetInformation, maxLines);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (files != null && !files.isEmpty()) {
                targetFileGenerated(files);
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            updateLogs(e.toString());
            return message("Failed");
        }
    }

}
