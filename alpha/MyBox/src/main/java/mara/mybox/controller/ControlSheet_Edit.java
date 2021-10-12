package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-7
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Edit extends ControlSheet_Pages {

    protected boolean textChanged;

    @FXML
    protected ControlTextDelimiter editDelimiterController;
    @FXML
    protected TextArea textsEditArea;

    public void initEdit() {
        try {
            editDelimiterController.setControls(baseName + "Source", true);
            editDelimiterName = editDelimiterController.delimiterName;
            editDelimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                private boolean isAsking;

                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isAsking) {
                        return;
                    }
                    if (textChanged) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(getMyStage().getTitle());
                        alert.setHeaderText(getMyStage().getTitle());
                        alert.setContentText(message("SheetEditSureChangeDelimiter"));
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        ButtonType buttonChange = new ButtonType(message("Change"));
                        ButtonType buttonCancel = new ButtonType(message("Cancel"));
                        ButtonType buttonSynchronize = new ButtonType(message("Synchronize"));
                        alert.getButtonTypes().setAll(buttonSynchronize, buttonChange, buttonCancel);
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonSynchronize) {
                            String cname = editDelimiterName;
                            editDelimiterName = editDelimiterController.delimiterName;
                            synchronizeEdit(cname);
                        } else if (result.get() == buttonChange) {
                            editDelimiterName = editDelimiterController.delimiterName;
                            updateEdit();
                        } else {
                            isAsking = true;
                            editDelimiterController.setDelimiter(editDelimiterName);
                            isAsking = false;
                        }
                    } else {
                        editDelimiterName = editDelimiterController.delimiterName;
                        updateEdit();
                    }

                }
            });

            textsEditArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    textChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void updateEdit() {
        try {
            String text = TextTools.dataText(pageData, editDelimiterName);
            isSettingValues = true;
            textsEditArea.setText(text);
            isSettingValues = false;
            textChanged(false);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void textChanged(boolean changed) {
        try {
            textChanged = changed;
            editTab.setText(message("EditText") + (changed ? " *" : ""));
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    public void synchronizeEdit() {
        synchronizeEdit(editDelimiterName);
    }

    public void synchronizeEdit(String delimiterName) {
        try {
            if (isSettingValues) {
                return;
            }
            String s = textsEditArea.getText();
            String[] lines = s.split("\n");
            int colsSize = 0;
            List<List<String>> rows = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> row = TextTools.parseLine(line, delimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                int size = row.size();
                if (size > colsSize) {
                    colsSize = size;
                }
                rows.add(row);
            }
            int rowsSize = rows.size();
            if (pagesNumber > 1) {
                colsSize = columns == null ? 0 : columns.size();
            }
            if (rowsSize == 0 || colsSize == 0) {
                makeSheet(null);
                return;
            }
            String[][] data = new String[rowsSize][colsSize];
            for (int r = 0; r < rowsSize; r++) {
                List<String> row = rows.get(r);
                for (int c = 0; c < Math.min(colsSize, row.size()); c++) {
                    data[r][c] = row.get(c);
                }
            }
            makeSheet(data, true);
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
            synchronizeEdit();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
