package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
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
            if (textAllCheck != null) {
//            textAllCheck.setSelected(UserConfig.getBoolean(baseName + "TextAll", false));
                textAllCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateText();
//                UserConfig.setBoolean(baseName + "TextAll", newValue);
                });
            }

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void updateText() {
        if (pagesNumber > 1 && textAllCheck != null && textAllCheck.isSelected()) {
            displayAllText();
        } else {
            displayPageText();
        }
    }

    protected void displayPageText() {
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

    protected void displayAllText() {
        displayPageText();
    }

    protected void rowText(StringBuilder s, int index, List<String> values, String delimiter) {
        try {
            if (textRowCheck.isSelected()) {
                if (index == -1) {
                    s.append(delimiter);
                } else if (index >= 0) {
                    s.append(message("Row")).append(index + 1).append(delimiter);
                }
            }
            int end = values.size() - 1;
            for (int c = 0; c <= end; c++) {
                s.append(values.get(c));
                if (c < end) {
                    s.append(delimiter);
                }
            }
            s.append("\n");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected int pageText(StringBuilder s, int inIndex, String delimiter) {
        int index = inIndex;
        try {
            if (sheetInputs != null) {
                for (int r = 0; r < sheetInputs.length; r++) {
                    rowText(s, index++, row(r), delimiter);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return index;
    }

    @FXML
    public void editText() {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textsDisplayArea.getText());
    }
}
