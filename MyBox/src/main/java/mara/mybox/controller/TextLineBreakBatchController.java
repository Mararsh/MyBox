package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.FileEditInformation.Line_Break;
import mara.mybox.tools.TextTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class TextLineBreakBatchController extends TextEncodingBatchController {

    @FXML
    protected ToggleGroup lbGroup;

    @Override
    protected void initOptionsSection() {
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
        if (AppVaribles.getMessage("LF").equals(selected.getText())) {
            targetInformation.setLineBreak(Line_Break.LF);
        } else if (AppVaribles.getMessage("CR").equals(selected.getText())) {
            targetInformation.setLineBreak(Line_Break.CR);
        } else if (AppVaribles.getMessage("CRLF").equals(selected.getText())) {
            targetInformation.setLineBreak(Line_Break.CRLF);
        }
        targetInformation.setLineBreakValue(TextTools.lineBreakValue(targetInformation.getLineBreak()));
    }

    @Override
    protected String handleFile(File srcFile, File targetFile) {
        try {
            sourceInformation.setFile(srcFile);
            sourceInformation.setLineBreak(TextTools.checkLineBreak(srcFile));
            sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
            if (autoDetermine) {
                boolean ok = TextTools.checkCharset(sourceInformation);
                if (!ok || sourceInformation == null) {
                    return AppVaribles.getMessage("Failed");
                }

            }
            targetInformation.setFile(targetFile);
            targetInformation.setCharset(sourceInformation.getCharset());
            if (TextTools.convertLineBreak(sourceInformation, targetInformation)) {
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
