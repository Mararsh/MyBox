package mara.mybox.controller;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegConvertMediaFilesController extends FFmpegBaseController {

    public FFmpegConvertMediaFilesController() {
        baseTitle = AppVariables.message("FFmpegConvertMediaFiles");
        mustSetResolution = false;
    }

    @Override
    public void checkExecutableInput() {
        try {
            super.checkExecutableInput();
            readMuxers();
            readEncoders();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            String ext = extensionInput.getText().trim();
            if (ext.isEmpty() || message("OriginalFormat").equals(ext)) {
                ext = FileTools.getFileSuffix(srcFile.getName());
            }
            File target = makeTargetFile(FileTools.getFilePrefix(srcFile.getName()),
                    "." + ext, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            convert(srcFile, target, -1);
            targetFileGenerated(target);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            return AppVariables.message("Failed");
        }
    }

    @Override
    public void updateFileProgress(long number, long total) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                double p = (number * 1d) / total;
                String s = DateTools.showSeconds(number / 1000) + "/"
                        + DateTools.showSeconds(total / 1000);
                progressBar.setProgress(p);
                progressValue.setText(s);
            }
        });
    }

    protected void convert(File sourceFile, File targetFile, long duration)
            throws Exception {
        convert(UrlInput.fromPath(Paths.get(sourceFile.getAbsolutePath())), targetFile, duration);
    }

    protected void convert(UrlInput input, File targetFile, long inDuration)
            throws Exception {
        final long duration;
        if (inDuration < 0) {
            final AtomicLong countedDuration = new AtomicLong();
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(message("CountingMediaDuration"), true);
            }
            FFmpeg.atPath(executable.toPath().getParent())
                    .addInput(input)
                    .addOutput(new NullOutput())
                    .setOverwriteOutput(true)
                    .setProgressListener((FFmpegProgress progress) -> {
                        countedDuration.set(progress.getTimeMillis());
                    })
                    .execute();
            duration = countedDuration.get();
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(message("Duration") + ": " + DateTools.showDuration(duration), true);
            }
        } else {
            duration = inDuration;
        }

        ProgressListener listener = new ProgressListener() {
            private long lastProgress = System.currentTimeMillis();
            private long lastStatus = System.currentTimeMillis();

            @Override
            public void onProgress(FFmpegProgress progress) {
                Platform.runLater(() -> {
                    long now = System.currentTimeMillis();
                    if (now > lastProgress + 500) {
                        if (duration > 0) {
                            updateFileProgress(progress.getTimeMillis(), duration);
                        } else {
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(message("Handled") + ":"
                                        + DateTools.showSeconds(progress.getTimeMillis() / 1000), true);
                            }
                            progressValue.setText(DateTools.showSeconds(progress.getTimeMillis() / 1000));
                        }
                        lastProgress = now;
                    }
                    if (now > lastStatus + 3000) {
                        long cost = now - mediaStart;
                        if (duration > 0) {
                            double p = DoubleTools.scale2(progress.getTimeMillis() * 100.0d / duration);
                            long left = Math.round(cost * (100 - p) / p);
                            String s = message("Percentage") + ": " + p + "%  "
                                    + message("Cost") + ": " + DateTools.showTime(cost) + "   "
                                    + message("EstimatedLeft") + ": " + DateTools.showTime(left);
                            statusLabel.setText(s);
                        } else {
                            String s = message("Cost") + ": " + DateTools.showTime(cost);
                            statusLabel.setText(s);
                        }
                        lastStatus = now;
                    }
                });

            }
        };
        mediaStart = System.currentTimeMillis();
        FFmpeg ffmpeg = FFmpeg.atPath(executable.toPath().getParent())
                .addInput(input)
                .addOutput(UrlOutput.toPath(targetFile.toPath()))
                .setProgressListener(listener)
                .setOverwriteOutput(true);
        if (disableVideo) {
            ffmpeg.addArgument("-vn");
        } else if (videoCodec != null) {
            ffmpeg.addArguments("-vcodec", videoCodec);
        }
        if (disbaleAudio) {
            ffmpeg.addArgument("-an");
        } else if (audioCodec != null) {
            ffmpeg.addArguments("-acodec", audioCodec);
        }
        if (disbaleSubtitle) {
            ffmpeg.addArgument("-sn");
        } else if (subtitleCodec != null) {
            ffmpeg.addArguments("-scodec", subtitleCodec);
        }
        if (aspect != null) {
            ffmpeg.addArguments("-aspect", aspect);
        }
        if (videoFrameRate > 0) {
            ffmpeg.addArguments("-r", videoFrameRate + "");
        }
        if (width > 0 && height > 0) {
            ffmpeg.addArguments("-s", width + "x" + height);
        }
        if (videoBitrate > 0) {
            ffmpeg.addArguments("-b:v", videoBitrate + "k");
        }
        if (audioBitrate > 0) {
            ffmpeg.addArguments("-b:a", audioBitrate + "k");
        }
        if (audioSampleRate > 0) {
            ffmpeg.addArguments("-ar", audioSampleRate + "");
        }
        String volumn = volumnInput.getText().trim();
        if (!volumn.isBlank()) {
            ffmpeg.addArguments("-af", "volume=" + volumn);
        }

        String more = moreInput.getText().trim();
        if (!more.isBlank()) {
            String[] args = StringTools.splitBySpace(more);
            if (args != null && args.length > 0) {
                for (String arg : args) {
                    ffmpeg.addArgument(arg);
                }
            }
        }

        if (verboseCheck == null || verboseCheck.isSelected()) {
            updateLogs(message("ConvertingMedia") + "  " + message("TargetFile") + ":" + targetFile, true);
        }
        FFmpegResult result = ffmpeg.execute();
    }

    @Override
    public void viewTarget(File file) {
        if (file == null) {
            return;
        }
        if (Arrays.asList(CommonValues.MediaPlayerSupports).contains(FileTools.getFileSuffix(file))) {
            FxmlStage.openMediaPlayer(null, file);

        } else {
            openTarget(null);
        }

    }

    @Override
    public boolean checkBeforeNextAction() {
        if ((encoderTask != null && encoderTask.isRunning())
                || (muxerTask != null && muxerTask.isRunning())
                || (queryTask != null && queryTask.isRunning())) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVariables.message("TaskRunning"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSure) {
                if (encoderTask != null) {
                    encoderTask.cancel();
                    encoderTask = null;
                }
                if (muxerTask != null) {
                    muxerTask.cancel();
                    muxerTask = null;
                }
                if (queryTask != null) {
                    queryTask.cancel();
                    queryTask = null;
                }
            } else {
                return false;
            }
        }
        return true;
    }

}
