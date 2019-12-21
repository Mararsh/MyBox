package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegBaseController extends FilesBatchController {

    protected String executableName, executableDefault;
    protected File executable;

    @FXML
    protected Label executableLabel;
    @FXML
    protected TextField executableInput;
    @FXML
    protected VBox functionBox;

    public FFmpegBaseController() {
        baseTitle = AppVariables.message("MediaInformation");

        SourceFileType = VisitHistory.FileType.Media;
        SourcePathType = VisitHistory.FileType.Media;
        TargetPathType = VisitHistory.FileType.Media;
        TargetFileType = VisitHistory.FileType.Media;

        targetPathKey = "MediaFilePath";
        sourcePathKey = "MediaFilePath";

        sourceExtensionFilter = CommonFxValues.FFmpegMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

        executableName = "FFmpegExecutable";
        executableDefault = "D:\\Programs\\ffmpeg\\bin\\ffmpeg.exe";
    }

    @Override
    public void initializeNext() {
        try {
            if (executableInput != null) {
                executableInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue observable, String oldValue, String newValue) {
                        checkExecutableInput();
                    }
                });
                executableInput.setText(AppVariables.getUserConfigValue(executableName, executableDefault));
            }

            if (functionBox != null) {
                functionBox.disableProperty().bind(executableInput.styleProperty().isEqualTo(badStyle));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void selectExecutable() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null || !file.exists()) {
                return;
            }
            recordFileOpened(file);
            executableInput.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void checkExecutableInput() {
        executable = null;
        String v = executableInput.getText();
        if (v == null || v.isEmpty()) {
            executableInput.setStyle(badStyle);
            return;
        }
        final File file = new File(v);
        if (!file.exists()) {
            executableInput.setStyle(badStyle);
            return;
        }
        executable = file;
        executableInput.setStyle(null);
        AppVariables.setUserConfigValue(executableName, file.getAbsolutePath());
    }

    @FXML
    public void download() {
        try {
            browseURI(new URI("http://ffmpeg.org/download.html"));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
