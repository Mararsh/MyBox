package mara.mybox.controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.value.AppVaribles;

/**
 * @Author Mara
 * @CreateDate 2019-7-5
 * @License Apache License Version 2.0
 */
public class FilesDeleteController extends FilesBatchController {

    protected List<String> targetNames;
    protected int currentAccum, digit;

    protected List<File> includeFiles;
    protected int dirFiles, dirRenamed;
    protected File currentDir;
    protected Map<String, String> currentNewNames;
    protected Map<String, Map<String, String>> newNames;

    @FXML
    private CheckBox verboseCheck;
    @FXML
    protected TextField maxLinesinput;
    @FXML
    protected TextArea logsTextArea;

    public FilesDeleteController() {
        baseTitle = AppVaribles.message("FilesDelete");

        targetPathKey = "FileTargetPath";
        sourcePathKey = "FileSourcePath";

    }

    @FXML
    protected void clearLogsAction(ActionEvent event) {
        logsTextArea.setText("");
    }

}
