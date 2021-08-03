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
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
            pagesNumberInput.setText(UserConfig.getUserConfigString(baseName + "PagesNumber", "20"));

            filesNumberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkFilesNumber();
                }
            });
            filesNumberInput.setText(UserConfig.getUserConfigString(baseName + "FilesNumber", "3"));

            listInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkStartEndList();
                }
            });
            listInput.setText(UserConfig.getUserConfigString(baseName + "List", ""));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeTools.setTooltip(listInput, new Tooltip(Languages.message("StartEndComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
        if (Languages.message("PagesNumberOfEachFile").equals(selected.getText())) {
            splitType = SplitType.PagesNumber;
            pagesNumberInput.setDisable(false);
            checkPagesNumber();

        } else if (Languages.message("NumberOfFilesDividedEqually").equals(selected.getText())) {
            splitType = SplitType.FilesNumber;
            filesNumberInput.setDisable(false);
            checkFilesNumber();

        } else if (Languages.message("StartEndList").equals(selected.getText())) {
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
                UserConfig.setUserConfigString(baseName + "PagesNumber", pagesNumber + "");
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
                UserConfig.setUserConfigString(baseName + "FilesNumber", filesNumber + "");
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
                UserConfig.setUserConfigString(baseName + "List", listInput.getText());
            }
        } catch (Exception e) {
            listInput.setStyle(badStyle);
        }
    }

}
