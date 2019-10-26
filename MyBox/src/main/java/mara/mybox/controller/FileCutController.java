package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FileCutController extends FilesBatchController {

    private FileSplitType splitType;
    private long bytesNumber, filesNumber;
    private List<Long> startEndList;

    @FXML
    protected ToggleGroup splitGroup;
    @FXML
    protected TextField filesNumberInput, bytesNumberInput, listInput;

    public enum FileSplitType {
        FilesNumber, BytesNumber, StartEndList
    }

    public FileCutController() {
        baseTitle = AppVariables.message("FileCut");

    }

    @Override
    public void initOptionsSection() {
        splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSplitType();
            }
        });
        checkSplitType();

        filesNumberInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkFilesNumber();
            }
        });

        bytesNumberInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkBytesNumber();
            }
        });

        listInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkStartEndList();
            }
        });

        FxmlControl.setTooltip(listInput, new Tooltip(message("StartEndByteComments")));

    }

    private void checkSplitType() {
        filesNumberInput.setDisable(true);
        bytesNumberInput.setDisable(true);
        listInput.setDisable(true);
        filesNumberInput.setStyle(null);
        bytesNumberInput.setStyle(null);
        listInput.setStyle(null);

        RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
        if (AppVariables.message("SplitByFilesNumber").equals(selected.getText())) {
            splitType = FileSplitType.FilesNumber;
            filesNumberInput.setDisable(false);
            checkFilesNumber();

        } else if (AppVariables.message("SplitByBytesNumber").equals(selected.getText())) {
            splitType = FileSplitType.BytesNumber;
            bytesNumberInput.setDisable(false);
            checkBytesNumber();

        } else if (AppVariables.message("CutByStartEndByteList").equals(selected.getText())) {
            splitType = FileSplitType.StartEndList;
            listInput.setDisable(false);
            checkStartEndList();
        }
    }

    private void checkFilesNumber() {
        try {
            int v = Integer.valueOf(filesNumberInput.getText());
            if (v >= 0) {
                filesNumber = v;
                filesNumberInput.setStyle(null);
            } else {
                filesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            filesNumber = 0;
            filesNumberInput.setStyle(badStyle);
        }
    }

    private void checkBytesNumber() {
        try {
            long v = ByteTools.checkBytesValue(bytesNumberInput.getText());
            if (v >= 0) {
                bytesNumber = v;
                bytesNumberInput.setStyle(null);
            } else {
                bytesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            bytesNumberInput.setStyle(badStyle);
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
                    long start = ByteTools.checkBytesValue(values[0]);
                    long end = ByteTools.checkBytesValue(values[1]);
                    if (start > 0 && end > 0) {           // 1-based start
                        startEndList.add(start);
                        startEndList.add(end);
                    } else {
                        startEndList.clear();
                        break;
                    }
                } catch (Exception e) {
                }
            }
            if (startEndList.isEmpty()) {
                listInput.setStyle(badStyle);
            } else {
                listInput.setStyle(null);
            }
        } catch (Exception e) {
            listInput.setStyle(badStyle);
        }
    }

    @Override
    public void initTargetSection() {

        startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(tableData))
                .or(filesNumberInput.styleProperty().isEqualTo(badStyle))
                .or(bytesNumberInput.styleProperty().isEqualTo(badStyle))
                .or(listInput.styleProperty().isEqualTo(badStyle))
        );

    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return AppVariables.message("Skip");
        }
        String targetName = target.getAbsolutePath();
        List<File> files = null;
        switch (splitType) {
            case FilesNumber:
                files = FileTools.splitFileByFilesNumber(srcFile, targetName, filesNumber);
                break;
            case BytesNumber:
                files = FileTools.splitFileByBytesNumber(srcFile, targetName, bytesNumber);
                break;
            case StartEndList:
                files = FileTools.splitFileByStartEndList(srcFile, targetName, startEndList);
                break;
        }
        if (files == null || files.isEmpty()) {
            return AppVariables.message("Failed");
        } else {
            currentParameters.finalTargetName = files.get(0).getAbsolutePath();
            targetFiles.addAll(files);
            return MessageFormat.format(AppVariables.message("FilesGenerated"), files.size());
        }
    }

}
