package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.TextEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-2-8
 * @License Apache License Version 2.0
 */
public class TextFilesMergeController extends FilesMergeController {

    protected boolean sourceEncodingAutoDetermine;
    protected Charset sourceCharset, targetCharset;
    protected Line_Break targetLineBreak;
    protected String taregtLineBreakValue;
    protected OutputStreamWriter writer;

    @FXML
    protected ToggleGroup sourceEncodingGroup, lbGroup;
    @FXML
    protected ComboBox<String> sourceEncodingBox, targetEncodingBox;
    @FXML
    protected CheckBox targetBomCheck;

    public TextFilesMergeController() {
        baseTitle = message("TextFilesMerge");
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
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        sourceEncodingGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSourceEncoding();
            }
        });

        List<String> setNames = TextTools.getCharsetNames();
        sourceEncodingBox.getItems().addAll(setNames);
        sourceEncodingBox.getSelectionModel().select(Charset.defaultCharset().name());
        sourceEncodingBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceCharset = Charset.forName(newValue);
            }
        });
        checkSourceEncoding();

        targetEncodingBox.getItems().addAll(setNames);
        targetEncodingBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                targetCharset = Charset.forName(newValue);
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
        targetCharset = Charset.defaultCharset();
        targetEncodingBox.getSelectionModel().select(targetCharset.name());

        targetLineBreak = Line_Break.LF;
        lbGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkLineBreak();
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
            sourceCharset = Charset.forName(sourceEncodingBox.getSelectionModel().getSelectedItem());
            sourceEncodingBox.setDisable(false);
        }
    }

    protected void checkLineBreak() {
        RadioButton selected = (RadioButton) lbGroup.getSelectedToggle();
        if (message("LF").equals(selected.getText())) {
            targetLineBreak = Line_Break.LF;
        } else if (message("CR").equals(selected.getText())) {
            targetLineBreak = Line_Break.CR;
        } else if (message("CRLF").equals(selected.getText())) {
            targetLineBreak = Line_Break.CRLF;
        }
    }

    @Override
    protected boolean openWriter() {
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
            writer = new OutputStreamWriter(outputStream, targetCharset);
            if (targetBomCheck.isSelected()) {
                byte[] bytes = bomBytes(targetCharset.name());
                outputStream.write(bytes);
            }
            taregtLineBreakValue = TextTools.lineBreakValue(targetLineBreak);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String handleFile(File file) {
        try {
            TextEditInformation sourceInfo = new TextEditInformation(file);
            if (sourceEncodingAutoDetermine) {
                boolean ok = TextTools.checkCharset(sourceInfo);
                if (!ok) {
                    return message("Failed") + ": " + file;
                }
            } else {
                sourceInfo.setCharset(sourceCharset);
            }
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, sourceInfo.getCharset()))) {
                if (sourceInfo.isWithBom()) {
                    inputStream.skip(bomSize(sourceInfo.getCharset().name()));
                }
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    writer.write(line + taregtLineBreakValue);
                }
            } catch (Exception e) {
                return e.toString();
            }
            return message("Handled") + ": " + file;
        } catch (Exception e) {
            return file + " " + e.toString();
        }
    }

    @Override
    protected boolean closeWriter() {
        try {
            writer.flush();
            writer.close();
            outputStream.close();
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

}
