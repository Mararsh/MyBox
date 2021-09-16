package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_TextsDisplay extends ControlSheet_Html {

    public void initTextControls() {
        try {
            displayDelimiterController.setControls(baseName + "Display", false);
            displayDelimiterName = displayDelimiterController.delimiterName;
            displayDelimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    displayDelimiterName = displayDelimiterController.delimiterName;
                    updateText();
                }
            });

            textTitleCheck.setSelected(UserConfig.getBoolean(baseName + "TextTitle", true));
            textTitleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateText();
                UserConfig.setBoolean(baseName + "TextTitle", newValue);
            });
            textColumnCheck.setSelected(UserConfig.getBoolean(baseName + "TextColumn", false));
            textColumnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateText();
                UserConfig.setBoolean(baseName + "TextColumn", newValue);
            });
            textRowCheck.setSelected(UserConfig.getBoolean(baseName + "TextRow", false));
            textRowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateText();
                UserConfig.setBoolean(baseName + "TextRow", newValue);
            });

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void updateText() {
        List<String> colsNames = null;
        List<String> rowsNames = null;
        String title = null;
        if (textTitleCheck.isSelected()) {
            title = titleName();
        }
        if (textColumnCheck.isSelected()) {
            colsNames = columnNames();
        }
        if (textRowCheck.isSelected()) {
            rowsNames = pageData == null ? null : rowNames(pageData.length);
        }
        String text = TextTools.dataText(pageData, displayDelimiterName, colsNames, rowsNames);
        if (title != null && !title.isBlank()) {
            textsDisplayArea.setText(title + "\n\n" + text);
        } else {
            textsDisplayArea.setText(text);
        }
    }

    @FXML
    public void editText() {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textsDisplayArea.getText());
    }
}
