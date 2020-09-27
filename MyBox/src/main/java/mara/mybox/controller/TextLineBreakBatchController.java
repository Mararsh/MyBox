package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class TextLineBreakBatchController extends TextEncodingBatchController {

    @FXML
    protected ToggleGroup lbGroup;

    public TextLineBreakBatchController() {
        baseTitle = AppVariables.message("TextLineBreakBatch");

    }

    @Override
    public void initOptionsSection() {
        super.initOptionsSection();

        lbGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkLineBreak();
            }
        });
        checkLineBreak();
    }

    protected void checkLineBreak() {
        RadioButton selected = (RadioButton) lbGroup.getSelectedToggle();
        if (AppVariables.message("LF").equals(selected.getText())) {
            targetInformation.setLineBreak(Line_Break.LF);
        } else if (AppVariables.message("CR").equals(selected.getText())) {
            targetInformation.setLineBreak(Line_Break.CR);
        } else if (AppVariables.message("CRLF").equals(selected.getText())) {
            targetInformation.setLineBreak(Line_Break.CRLF);
        }
        targetInformation.setLineBreakValue(TextTools.lineBreakValue(targetInformation.getLineBreak()));
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            sourceInformation.setFile(srcFile);
            sourceInformation.setLineBreak(TextTools.checkLineBreak(srcFile));
            sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
            if (autoDetermine) {
                boolean ok = TextTools.checkCharset(sourceInformation);
                if (!ok || sourceInformation == null) {
                    return AppVariables.message("Failed");
                }

            }
            targetInformation.setFile(target);
            targetInformation.setCharset(sourceInformation.getCharset());
            if (TextTools.convertLineBreak(sourceInformation, targetInformation)) {
                targetFileGenerated(target);
                return AppVariables.message("Successful");
            } else {
                return AppVariables.message("Failed");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

}
