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
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileSplitTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

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
            MyBoxLog.debug(e.toString());
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
            int v = Integer.valueOf(filesNumberInput.getText());
            if (v >= 0) {
                filesNumber = v;
                filesNumberInput.setStyle(null);
            } else {
                filesNumberInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            filesNumber = 0;
            filesNumberInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    private void checkBytesNumber() {
        try {
            long v = ByteTools.checkBytesValue(bytesNumberInput.getText());
            if (v >= 0) {
                bytesNumber = v;
                bytesNumberInput.setStyle(null);
            } else {
                bytesNumberInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            bytesNumberInput.setStyle(NodeStyleTools.badStyle);
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
                listInput.setStyle(NodeStyleTools.badStyle);
            } else {
                listInput.setStyle(null);
            }
        } catch (Exception e) {
            listInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    @Override
    public void initTargetSection() {

        startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                .or(targetPathInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                .or(Bindings.isEmpty(tableData))
                .or(filesNumberInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                .or(bytesNumberInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                .or(listInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
        );

    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return Languages.message("Skip");
        }
        String targetName = target.getAbsolutePath();
        List<File> files = null;
        switch (splitType) {
            case FilesNumber:
                files = FileSplitTools.splitFileByFilesNumber(srcFile, targetName, filesNumber);
                break;
            case BytesNumber:
                files = FileSplitTools.splitFileByBytesNumber(srcFile, targetName, bytesNumber);
                break;
            case StartEndList:
                files = FileSplitTools.splitFileByStartEndList(srcFile, targetName, startEndList);
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
