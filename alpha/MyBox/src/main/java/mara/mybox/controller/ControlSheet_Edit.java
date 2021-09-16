package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-7
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Edit extends ControlSheet_Pages {

    @FXML
    protected ControlTextDelimiter editDelimiterController;
    @FXML
    protected TextArea textsEditArea;

    public void initEdit() {
        try {
            editDelimiterController.setControls(baseName + "Source", true);
            editDelimiterName = editDelimiterController.delimiterName;
            editDelimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    editDelimiterName = editDelimiterController.delimiterName;
                }
            });

            textsEditArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    editTab.setText(message("EditText") + " *");
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void textApplyAction() {
        try {
            String text = TextTools.dataText(pageData, editDelimiterName);
            isSettingValues = true;
            textsEditArea.setText(text);
            isSettingValues = false;
            editTab.setText(message("EditText"));
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    public void synchronizeEdit() {
        try {
            if (isSettingValues) {
                return;
            }
            String s = textsEditArea.getText();
            String[] lines = s.split("\n");
            int colsSize = 0;
            List<List<String>> data = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> row = TextTools.parseLine(line, editDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                int size = row.size();
                if (size > colsSize) {
                    colsSize = size;
                }
                data.add(row);
            }
            int rowsSize = data.size();
            if (pagesNumber > 1) {
                colsSize = columns == null ? 0 : columns.size();
            }
            if (rowsSize == 0 || colsSize == 0) {
                makeSheet(null);
                return;
            }
            pageData = new String[rowsSize][colsSize];
            for (int r = 0; r < rowsSize; r++) {
                List<String> row = data.get(r);
                for (int c = 0; c < Math.min(colsSize, row.size()); c++) {
                    pageData[r][c] = row.get(c);
                }
            }
            makeSheet(pageData, true);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void loadText(String text) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            tabPane.getSelectionModel().select(editTab);
            isSettingValues = true;
            textsEditArea.setText(text);
            isSettingValues = false;
            editTab.setText(message("EditText") + " *");
            synchronizeEdit();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
