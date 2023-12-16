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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileSplitTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FileCutController extends BaseBatchFileController {

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
        baseTitle = Languages.message("FileCut");

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

    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(listInput, new Tooltip(Languages.message("StartEndByteComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    private void checkSplitType() {
        filesNumberInput.setDisable(true);
        bytesNumberInput.setDisable(true);
        listInput.setDisable(true);
        filesNumberInput.setStyle(null);
        bytesNumberInput.setStyle(null);
        listInput.setStyle(null);

        RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
        if (Languages.message("SplitByFilesNumber").equals(selected.getText())) {
            splitType = FileSplitType.FilesNumber;
            filesNumberInput.setDisable(false);
            checkFilesNumber();

        } else if (Languages.message("SplitByBytesNumber").equals(selected.getText())) {
            splitType = FileSplitType.BytesNumber;
            bytesNumberInput.setDisable(false);
            checkBytesNumber();

        } else if (Languages.message("CutByStartEndByteList").equals(selected.getText())) {
            splitType = FileSplitType.StartEndList;
            listInput.setDisable(false);
            checkStartEndList();
        }
    }

    private void checkFilesNumber() {
        try {
            int v = Integer.parseInt(filesNumberInput.getText());
            if (v >= 0) {
                filesNumber = v;
                filesNumberInput.setStyle(null);
            } else {
                filesNumberInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            filesNumber = 0;
            filesNumberInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkBytesNumber() {
        try {
            long v = ByteTools.checkBytesValue(bytesNumberInput.getText());
            if (v >= 0) {
                bytesNumber = v;
                bytesNumberInput.setStyle(null);
            } else {
                bytesNumberInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            bytesNumberInput.setStyle(UserConfig.badStyle());
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
                listInput.setStyle(UserConfig.badStyle());
            } else {
                listInput.setStyle(null);
            }
        } catch (Exception e) {
            listInput.setStyle(UserConfig.badStyle());
        }
    }

    @Override
    public void initTargetSection() {

        startButton.disableProperty().bind(targetPathController.valid.not()
                .or(Bindings.isEmpty(tableData))
                .or(filesNumberInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(bytesNumberInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                .or(listInput.styleProperty().isEqualTo(UserConfig.badStyle()))
        );

    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return Languages.message("Skip");
        }
        String targetName = target.getAbsolutePath();
        List<File> files = null;
        switch (splitType) {
            case FilesNumber:
                files = FileSplitTools.splitFileByFilesNumber(currentTask, srcFile, targetName, filesNumber);
                break;
            case BytesNumber:
                files = FileSplitTools.splitFileByBytesNumber(currentTask, srcFile, targetName, bytesNumber);
                break;
            case StartEndList:
                files = FileSplitTools.splitFileByStartEndList(currentTask, srcFile, targetName, startEndList);
                break;
        }
        if (files == null || files.isEmpty()) {
            return Languages.message("Failed");
        } else {
            targetFileGenerated(files);
            return MessageFormat.format(Languages.message("FilesGenerated"), files.size());
        }
    }

}
