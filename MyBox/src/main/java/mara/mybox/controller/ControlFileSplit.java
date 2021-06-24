package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-20
 * @License Apache License Version 2.0
 */
public class ControlFileSplit extends BaseController {

    protected int pagesNumber, filesNumber;
    protected List<Integer> startEndList;
    protected SplitType splitType;
    protected SimpleBooleanProperty valid;

    @FXML
    protected ToggleGroup splitGroup;
    @FXML
    protected TextField pagesNumberInput, filesNumberInput, listInput;

    public enum SplitType {
        PagesNumber, FilesNumber, StartEndList
    }

    public ControlFileSplit() {
        splitType = SplitType.PagesNumber;
        valid = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            valid.bind(pagesNumberInput.styleProperty().isEqualTo(badStyle)
                    .or(filesNumberInput.styleProperty().isEqualTo(badStyle))
                    .or(listInput.styleProperty().isEqualTo(badStyle)));

            splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSplitType();
                }
            });
            checkSplitType();

            pagesNumberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkPagesNumber();
                }
            });
            pagesNumberInput.setText(AppVariables.getUserConfigValue(baseName + "PagesNumber", "20"));

            filesNumberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkFilesNumber();
                }
            });
            filesNumberInput.setText(AppVariables.getUserConfigValue(baseName + "FilesNumber", "3"));

            listInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkStartEndList();
                }
            });
            listInput.setText(AppVariables.getUserConfigValue(baseName + "List", ""));

            FxmlControl.setTooltip(listInput, new Tooltip(message("StartEndComments")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void checkSplitType() {
        pagesNumberInput.setDisable(true);
        filesNumberInput.setDisable(true);
        listInput.setDisable(true);
        pagesNumberInput.setStyle(null);
        filesNumberInput.setStyle(null);
        listInput.setStyle(null);

        RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
        if (AppVariables.message("PagesNumberOfEachFile").equals(selected.getText())) {
            splitType = SplitType.PagesNumber;
            pagesNumberInput.setDisable(false);
            checkPagesNumber();

        } else if (AppVariables.message("NumberOfFilesDividedEqually").equals(selected.getText())) {
            splitType = SplitType.FilesNumber;
            filesNumberInput.setDisable(false);
            checkFilesNumber();

        } else if (AppVariables.message("StartEndList").equals(selected.getText())) {
            splitType = SplitType.StartEndList;
            listInput.setDisable(false);
            checkStartEndList();
        }
    }

    private void checkPagesNumber() {
        try {
            int v = Integer.valueOf(pagesNumberInput.getText());
            if (v > 0) {
                pagesNumberInput.setStyle(null);
                pagesNumber = v;
                AppVariables.setUserConfigValue(baseName + "PagesNumber", pagesNumber + "");
            } else {
                pagesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pagesNumberInput.setStyle(badStyle);
        }
    }

    private void checkFilesNumber() {
        try {
            int v = Integer.valueOf(filesNumberInput.getText());
            if (v > 0) {
                filesNumberInput.setStyle(null);
                filesNumber = v;
                AppVariables.setUserConfigValue(baseName + "FilesNumber", filesNumber + "");
            } else {
                filesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            filesNumberInput.setStyle(badStyle);
        }
    }

    private void checkStartEndList() {
        startEndList = new ArrayList<>();
        try {
            String[] list = listInput.getText().split(",");
            for (String item : list) {
                String[] values = item.split("-");
                if (values.length != 2) {
                    continue;
                }
                try {
                    int start = Integer.valueOf(values[0].trim());
                    int end = Integer.valueOf(values[1].trim());
                    if (start > 0 && end >= start) {  // 1-based start
                        startEndList.add(start);
                        startEndList.add(end);
                    }
                } catch (Exception e) {
                }
            }
            if (startEndList.isEmpty()) {
                listInput.setStyle(badStyle);
            } else {
                listInput.setStyle(null);
                AppVariables.setUserConfigValue(baseName + "List", listInput.getText());
            }
        } catch (Exception e) {
            listInput.setStyle(badStyle);
        }
    }

}
