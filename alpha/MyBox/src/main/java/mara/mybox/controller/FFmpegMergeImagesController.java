package mara.mybox.controller;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.MediaInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegMergeImagesController extends BaseBatchFFmpegController {

    protected ObservableList<MediaInformation> audiosData;

    @FXML
    protected Tab imagesTab, audiosTab;
    @FXML
    protected ControlFFmpegAudiosTable audiosTableController;
    @FXML
    protected CheckBox stopCheck;

    public FFmpegMergeImagesController() {
        baseTitle = Languages.message("FFmpegMergeImagesInformation");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.Media);
        targetExtensionFilter = FileFilters.FFmpegMediaExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            audiosTableController.parentController = this;
            audiosTableController.parentFxml = myFxml;

            audiosData = audiosTableController.tableData;

            ffmpegOptionsController.extensionInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkExt();
                    });
            checkExt();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    targetFileController.valid.not()
                            .or(Bindings.isEmpty(tableView.getItems()))
                            .or(ffmpegOptionsController.extensionInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void checkExt() {
        String ext = ffmpegOptionsController.extensionInput.getText();
        if (ext == null || ext.isBlank() || Languages.message("OriginalFormat").equals(ext)) {
            return;
        }
        String v = targetFileController.text();
        if (v == null || v.isBlank()) {
            targetFileController.input(AppPaths.getGeneratedPath() + File.separator + DateTools.nowFileString() + "." + ext);
        } else if (!v.endsWith("." + ext)) {
            targetFileController.input(FileNameTools.getFilePrefix(v) + "." + ext);
        }
    }

    @Override
    public void doCurrentProcess() {
        try {
            if (currentParameters == null || tableData.isEmpty() || targetFile == null) {
                popError(Languages.message("InvalidParameters"));
                return;
            }
            if (ffmpegOptionsController.width <= 0) {
                ffmpegOptionsController.width = 720;
            }
            if (ffmpegOptionsController.height <= 0) {
                ffmpegOptionsController.height = 480;
            }
            String ext = ffmpegOptionsController.extensionInput.getText().trim();
            if (ext.isEmpty() || Languages.message("OriginalFormat").equals(ext)) {
                ext = FileNameTools.getFileSuffix(targetFile.getName());
            }
            final File videoFile = makeTargetFile(FileNameTools.getFilePrefix(targetFile.getName()),
                    "." + ext, targetFile.getParentFile());
            if (videoFile == null) {
                return;
            }
            updateLogs(Languages.message("TargetFile") + ": " + videoFile, true);
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                processStartTime = new Date();
                totalFilesHandled = 0;
                updateInterface("Started");
                task = new SingletonTask<Void>(this) {

                    @Override
                    public Void call() {
                        try {
                            File imagesListFile = handleImages();
                            if (imagesListFile == null) {
                                return null;
                            }
                            File audiosListFile = handleAudios();
                            merge(imagesListFile, audiosListFile, videoFile);

                        } catch (Exception e) {
                            updateLogs(e.toString(), true);
                        }
                        ok = true;
                        return null;
                    }

                    @Override
                    public void succeeded() {
                        super.succeeded();
                        updateInterface("Done");
                    }

                    @Override
                    public void cancelled() {
                        super.cancelled();
                        updateInterface("Canceled");
                    }

                    @Override
                    public void failed() {
                        super.failed();
                        updateInterface("Failed");
                    }

                    @Override
                    protected void taskQuit() {
                        super.taskQuit();
                        quitProcess();
                        task = null;
                    }

                };
                start(task, false);
            }

        } catch (Exception e) {
            updateInterface("Failed");
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void disableControls(boolean disable) {
        super.disableControls(disable);
        audiosTableController.thisPane.setDisable(disable);
    }

    //  https://trac.ffmpeg.org/wiki/Slideshow
    protected File handleImages() {
        try {
            StringBuilder s = new StringBuilder();
            File lastFile = null;
            for (int i = 0; i < tableData.size(); ++i) {
                if (task == null || task.isCancelled()) {
                    updateLogs(Languages.message("TaskCancelled"), true);
                    return null;
                }
                ImageInformation info = (ImageInformation) tableData.get(i);
                totalFilesHandled++;
                if (info.getFile() != null) {
                    if (info.getIndex() >= 0) {
                        updateLogs(Languages.message("Reading") + ": " + info.getFile() + "-" + info.getIndex(), true);
                    } else {
                        updateLogs(Languages.message("Reading") + ": " + info.getFile(), true);
                    }
                }
                try {
                    BufferedImage bufferedImage = ImageInformation.readBufferedImage(info);
                    if (bufferedImage == null) {
                        continue;
                    }
                    BufferedImage fitImage = ScaleTools.fitSize(bufferedImage,
                            ffmpegOptionsController.width, ffmpegOptionsController.height);
                    File tmpFile = TmpFileTools.getTempFile(".png");
                    if (ImageFileWriters.writeImageFile(fitImage, tmpFile) && tmpFile.exists()) {
                        lastFile = tmpFile;
                        s.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
                        s.append("duration  ").append(info.getDuration() / 1000.00f).append("\n");
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
            if (lastFile == null) {
                updateLogs(Languages.message("InvalidData"), true);
                return null;
            }
            s.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
            File imagesListFile = TmpFileTools.getTempFile(".txt");
            TextFileTools.writeFile(imagesListFile, s.toString(), Charset.forName("utf-8"));
            return imagesListFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected File handleAudios() {
        try {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < audiosData.size(); ++i) {
                if (task == null || task.isCancelled()) {
                    updateLogs(Languages.message("TaskCancelled"), true);
                    return null;
                }
                MediaInformation info = audiosData.get(i);
                File file = info.getFile();
                if (file == null) {
                    continue;
                }
                totalFilesHandled++;
                updateLogs(Languages.message("Handling") + ": " + file, true);
                s.append("file '").append(file.getAbsolutePath()).append("'\n");
            }
            String ss = s.toString();
            if (ss.isEmpty()) {
                return null;
            }
            File audiosListFile = TmpFileTools.getTempFile(".txt");
            TextFileTools.writeFile(audiosListFile, s.toString(), Charset.forName("utf-8"));
            return audiosListFile;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected void merge(File imagesListFile, File audiosListFile, File videoFile) {
        if (imagesListFile == null || videoFile == null) {
            return;
        }
        try {
            ProgressListener listener = new ProgressListener() {
                private long lastProgress = System.currentTimeMillis();
                private long lastStatus = System.currentTimeMillis();

                @Override
                public void onProgress(FFmpegProgress progress) {
                    Platform.runLater(() -> {
                        long now = System.currentTimeMillis();
                        if (now > lastProgress + 500) {
                            if (verboseCheck == null || verboseCheck.isSelected()) {
                                updateLogs(Languages.message("Handled") + ":"
                                        + DateTools.timeDuration(progress.getTimeMillis()), true);
                            }
                            progressValue.setText(DateTools.timeDuration(progress.getTimeMillis()));
                            lastProgress = now;
                        }
                        if (now > lastStatus + 3000) {
                            long cost = now - ffmpegOptionsController.mediaStart;
                            String s = Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
                            statusLabel.setText(s);
                            lastStatus = now;
                        }
                    });

                }
            };
            ffmpegOptionsController.mediaStart = System.currentTimeMillis();
            FFmpeg ffmpeg = FFmpeg.atPath(ffmpegOptionsController.executable.toPath().getParent())
                    .addArguments("-f", "concat")
                    .addArguments("-safe", "0")
                    .addArguments("-i", imagesListFile.getAbsolutePath());
            if (audiosListFile != null) {
                ffmpeg.addArguments("-f", "concat")
                        .addArguments("-safe", "0")
                        .addArguments("-i", audiosListFile.getAbsolutePath());
            }
            ffmpeg.addArguments("-s", ffmpegOptionsController.width + "x" + ffmpegOptionsController.height)
                    .addArguments("-pix_fmt", "yuv420p");

            if (ffmpegOptionsController.audioBitrate > 0) {
                ffmpeg.addArguments("-b:a", ffmpegOptionsController.audioBitrate + "k");
            } else {
                ffmpeg.addArguments("-b:a", "192k");
            }
            if (ffmpegOptionsController.audioSampleRate > 0) {
                ffmpeg.addArguments("-ar", ffmpegOptionsController.audioSampleRate + "");
            } else {
                ffmpeg.addArguments("-ar", "44100");
            }
            ffmpeg.addOutput(UrlOutput.toPath(videoFile.toPath()))
                    .setProgressListener(listener)
                    .setOverwriteOutput(true);
            if (ffmpegOptionsController.disableVideo) {
                ffmpeg.addArgument("-vn");
            } else if (ffmpegOptionsController.videoCodec != null) {
                ffmpeg.addArguments("-vcodec", ffmpegOptionsController.videoCodec);
            }
            if (ffmpegOptionsController.aspect != null) {
                ffmpeg.addArguments("-aspect", ffmpegOptionsController.aspect);
            }
            if (ffmpegOptionsController.videoFrameRate > 0) {
                ffmpeg.addArguments("-r", ffmpegOptionsController.videoFrameRate + "");
            } else {
                ffmpeg.addArguments("-r", "30");
            }
            if (ffmpegOptionsController.videoBitrate > 0) {
                ffmpeg.addArguments("-b:v", ffmpegOptionsController.videoBitrate + "k");
            } else {
                ffmpeg.addArguments("-b:v", "5000k");
            }

            if (ffmpegOptionsController.disbaleAudio) {
                ffmpeg.addArgument("-an");
            } else if (ffmpegOptionsController.audioCodec != null) {
                ffmpeg.addArguments("-acodec", ffmpegOptionsController.audioCodec);
            }
            if (ffmpegOptionsController.volumn != null) {
                ffmpeg.addArguments("-af", "volume=" + ffmpegOptionsController.volumn);
            }
            if (ffmpegOptionsController.stereoCheck != null) {
                ffmpeg.addArguments("-ac", ffmpegOptionsController.stereoCheck.isSelected() ? "2" : "1");
            }
            if (stopCheck.isSelected()) {
                ffmpeg.addArgument("-shortest");
            }

            if (ffmpegOptionsController.disbaleSubtitle) {
                ffmpeg.addArgument("-sn");
            } else if (ffmpegOptionsController.subtitleCodec != null) {
                ffmpeg.addArguments("-scodec", ffmpegOptionsController.subtitleCodec);
            }

            String more = ffmpegOptionsController.moreInput.getText().trim();
            if (!more.isBlank()) {
                String[] args = StringTools.splitBySpace(more);
                if (args != null && args.length > 0) {
                    for (String arg : args) {
                        ffmpeg.addArgument(arg);
                    }
                }
            }
            updateLogs(Languages.message("ConvertingMedia") + "  " + Languages.message("TargetFile") + ":" + videoFile, true);
            FFmpegResult result = ffmpeg.execute();
            targetFileGenerated(videoFile);
            updateLogs(Languages.message("Size") + ": " + FileTools.showFileSize(result.getVideoSize()), true);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

}
