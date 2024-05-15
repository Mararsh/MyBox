package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegConvertMediaFilesController extends BaseBatchFFmpegController {

    public FFmpegConvertMediaFilesController() {
        baseTitle = message("FFmpegConvertMediaFiles");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(ffmpegOptionsController.extensionInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            String ext = ffmpegOptionsController.extensionInput.getText().trim();
            if (ext.isEmpty() || message("OriginalFormat").equals(ext)) {
                ext = FileNameTools.ext(srcFile.getName());
            }
            File target = makeTargetFile(FileNameTools.prefix(srcFile.getName()), "." + ext, targetPath);
            if (target == null) {
                return message("Skip");
            }
            convert(currentTask, srcFile.getAbsolutePath(), target);
            return message("Successful");
        } catch (Exception e) {
            showLogs(e.toString());
            return message("Failed");
        }
    }

    @Override
    public void updateFileProgress(long number, long total) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                double p = (number * 1d) / total;
                String s = DateTools.timeDuration(number) + "/"
                        + DateTools.timeDuration(total);
                progressBar.setProgress(p);
                progressValue.setText(s);
            }
        });
    }

    protected void convert(FxTask currentTask, String sourceMedia, File targetFile) {
        try {
            if (sourceMedia == null || targetFile == null) {
                return;
            }
            showLogs(message("ConvertingMedia") + ": " + sourceMedia + " -> " + targetFile);
            List<String> parameters = new ArrayList<>();
            parameters.add("-i");
            parameters.add(sourceMedia);
            parameters = ffmpegOptionsController.makeParameters(parameters);
            Process process = ffmpegOptionsController.startProcess(this, parameters, targetFile);
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        break;
                    }
                    if (verboseCheck.isSelected()) {
                        updateLogs(line + "\n");
                    }
                }
            }
            process.waitFor();
            targetFileGenerated(targetFile);
            showLogs(message("Size") + ": " + FileTools.showFileSize(targetFile.length()));
        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if ((ffmpegOptionsController.encoderTask != null && !ffmpegOptionsController.encoderTask.isQuit())
                || (ffmpegOptionsController.muxerTask != null && !ffmpegOptionsController.muxerTask.isQuit())
                || (ffmpegOptionsController.queryTask != null && !ffmpegOptionsController.queryTask.isQuit())) {
            if (!PopTools.askSure(getTitle(), message("TaskRunning"))) {
                return false;
            }
            if (ffmpegOptionsController.encoderTask != null) {
                ffmpegOptionsController.encoderTask.cancel();
                ffmpegOptionsController.encoderTask = null;
            }
            if (ffmpegOptionsController.muxerTask != null) {
                ffmpegOptionsController.muxerTask.cancel();
                ffmpegOptionsController.muxerTask = null;
            }
            if (ffmpegOptionsController.queryTask != null) {
                ffmpegOptionsController.queryTask.cancel();
                ffmpegOptionsController.queryTask = null;
            }
        }
        return true;
    }

}
