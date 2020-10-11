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
import mara.mybox.data.MediaInformation;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegMergeImagesController extends FFmpegBatchController {

    protected ObservableList<MediaInformation> audiosData;

    @FXML
    protected Tab imagesTab, audiosTab;
    @FXML
    protected ImagesTableController imagesTableController;
    @FXML
    protected FFmpegAudiosTableController audiosTableController;
    @FXML
    protected CheckBox stopCheck;
    @FXML
    protected ControlFileSelecter targetFileController;

    public FFmpegMergeImagesController() {
        baseTitle = AppVariables.message("FFmpegMergeImagesInformation");

        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Media;
        TargetFileType = VisitHistory.FileType.Media;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Media);
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = CommonFxValues.FFmpegMediaExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            audiosTableController.parentController = this;
            audiosTableController.parentFxml = myFxml;

            audiosData = audiosTableController.tableData;

            targetFileController.label(message("TargetFile"))
                    .isDirectory(false).isSource(false).mustExist(false).permitNull(false)
                    .name(baseName + "TargetFile", false).type(VisitHistory.FileType.Media);

            ffmpegOptionsController.extensionInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        String ext = newValue.trim();
                        if (!ext.isEmpty() && !message("OriginalFormat").equals(ext)) {
                            targetFileController.defaultValue("." + ext);
                        }
                    });

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(targetFileController.fileInput.textProperty())
                            .or(targetFileController.fileInput.styleProperty().isEqualTo(badStyle))
                            .or(ffmpegOptionsController.extensionInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void doCurrentProcess() {
        try {
            if (currentParameters == null || tableData.isEmpty() || targetFile == null) {
                popError(message("InvalidParameters"));
                return;
            }
            if (ffmpegOptionsController.width <= 0) {
                ffmpegOptionsController.width = 720;
            }
            if (ffmpegOptionsController.height <= 0) {
                ffmpegOptionsController.height = 480;
            }
            String ext = ffmpegOptionsController.extensionInput.getText().trim();
            if (ext.isEmpty() || message("OriginalFormat").equals(ext)) {
                ext = FileTools.getFileSuffix(targetFile.getName());
            }
            final File videoFile = makeTargetFile(FileTools.getFilePrefix(targetFile.getName()),
                    "." + ext, targetFile.getParentFile());
            if (videoFile == null) {
                return;
            }
            updateLogs(message("TargetFile") + ": " + videoFile, true);
            synchronized (this) {
                if (task != null) {
                    return;
                }
                processStartTime = new Date();
                totalFilesHandled = 0;
                updateInterface("Started");
                task = new SingletonTask<Void>() {

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
                        quitProcess();
                        task = null;
                    }

                };
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
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
                    updateLogs(message("TaskCancelled"), true);
                    return null;
                }
                ImageInformation info = (ImageInformation) tableData.get(i);
                totalFilesHandled++;
                if (info.getFile() != null) {
                    if (info.getIndex() >= 0) {
                        updateLogs(message("Handling") + ": " + info.getFile() + "-" + info.getIndex(), true);
                    } else {
                        updateLogs(message("Handling") + ": " + info.getFile(), true);
                    }
                }
                try {
                    BufferedImage image = ImageFileReaders.getBufferedImage(info);
                    if (image == null) {
                        continue;
                    }
                    BufferedImage fitImage = ImageManufacture.fitSize(image,
                            ffmpegOptionsController.width, ffmpegOptionsController.height);
                    File tmpFile = FileTools.getTempFile(".png");
                    if (ImageFileWriters.writeImageFile(fitImage, tmpFile) && tmpFile.exists()) {
                        lastFile = tmpFile;
                        s.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
                        s.append("duration  ").append(info.getDuration() / 1000.00f).append("\n");
                    }
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
            }
            if (lastFile == null) {
                updateLogs(message("InvalidData"), true);
                return null;
            }
            s.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
            File imagesListFile = FileTools.getTempFile(".txt");
            FileTools.writeFile(imagesListFile, s.toString(), Charset.forName("utf-8"));
            return imagesListFile;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    protected File handleAudios() {
        try {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < audiosData.size(); ++i) {
                if (task == null || task.isCancelled()) {
                    updateLogs(message("TaskCancelled"), true);
                    return null;
                }
                MediaInformation info = audiosData.get(i);
                File file = info.getFile();
                if (file == null) {
                    continue;
                }
                totalFilesHandled++;
                updateLogs(message("Handling") + ": " + file, true);
                s.append("file '").append(file.getAbsolutePath()).append("'\n");
            }
            String ss = s.toString();
            if (ss.isEmpty()) {
                return null;
            }
            File audiosListFile = FileTools.getTempFile(".txt");
            FileTools.writeFile(audiosListFile, s.toString(), Charset.forName("utf-8"));
            return audiosListFile;
        } catch (Exception e) {
            logger.error(e.toString());
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
                                updateLogs(message("Handled") + ":"
                                        + DateTools.timeDuration(progress.getTimeMillis()), true);
                            }
                            progressValue.setText(DateTools.timeDuration(progress.getTimeMillis()));
                            lastProgress = now;
                        }
                        if (now > lastStatus + 3000) {
                            long cost = now - ffmpegOptionsController.mediaStart;
                            String s = message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
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
            updateLogs(message("ConvertingMedia") + "  " + message("TargetFile") + ":" + videoFile, true);
            FFmpegResult result = ffmpeg.execute();
            targetFileGenerated(videoFile);
            updateLogs(message("Size") + ": " + FileTools.showFileSize(result.getVideoSize()), true);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

}
