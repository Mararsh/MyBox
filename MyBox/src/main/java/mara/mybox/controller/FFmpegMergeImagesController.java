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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
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
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegMergeImagesController extends FFmpegConvertMediaStreamsController {

    protected ObservableList<MediaInformation> audiosData;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab imagesTab, audiosTab;
    @FXML
    protected FFmpegAudiosTableController audiosTableController;
    @FXML
    protected CheckBox stopCheck;
    @FXML
    protected TextField audioBitrateInput;

    public FFmpegMergeImagesController() {
        baseTitle = AppVariables.message("FFmpegMergeImages");
        mustSetResolution = true;

        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Media;
        TargetFileType = VisitHistory.FileType.Media;

        targetPathKey = "MediaFilePath";
        sourcePathKey = "ImageFilePath";

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = CommonFxValues.FFmpegMediaExtensionFilter;

        executableName = "FFmpegExecutable";
        executableDefault = "D:\\Programs\\ffmpeg\\bin\\ffmpeg.exe";

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            audiosTableController.parentController = this;
            audiosTableController.parentFxml = myFxml;

            audiosData = audiosTableController.tableData;

            audioBitrateInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultAudioBitrate", newValue);
                    }
                }
            });
            audioBitrateInput.setText(AppVariables.getUserConfigValue("ffmpegDefaultAudioBitrate", "192k"));

            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetFileInput.textProperty()))
                            .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsValid.not())
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void selectTargetFileFromPath(File path) {
        try {
            String name = null;
            String ext = extensionInput.getText().trim();
            if (!ext.isEmpty() && !message("OriginalFormat").equals(ext)) {
                name = "." + ext;
            }
            final File file = chooseSaveFile(path, name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void doCurrentProcess() {
        try {
            if (currentParameters == null || tableData.isEmpty()
                    || targetFile == null || width <= 0 || height <= 0) {
                popError(message("InvalidParameters"));
                return;
            }
            String ext = extensionInput.getText().trim();
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
            for (int i = 0; i < tableData.size(); i++) {
                if (task == null || task.isCancelled()) {
                    updateLogs(message("TaskCancelled"), true);
                    return null;
                }
                ImageInformation info = (ImageInformation) tableData.get(i);
                File file = info.getFile();
                if (file == null) {
                    continue;
                }
                totalFilesHandled++;
                updateLogs(message("Handling") + ": " + file, true);
                try {
                    BufferedImage image = ImageFileReaders.readImage(file);
                    if (image == null) {
                        continue;
                    }
                    BufferedImage fitImage = ImageManufacture.fitSize(image, width, height);
                    File tmpFile = FileTools.getTempFile(".png");
                    if (ImageFileWriters.writeImageFile(fitImage, tmpFile) && tmpFile.exists()) {
                        lastFile = tmpFile;
                        s.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
                        s.append("duration  ").append(info.getDuration() / 1000).append("\n");
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
            for (int i = 0; i < audiosData.size(); i++) {
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
            File audiosListFile = FileTools.getTempFile(".txt");
            FileTools.writeFile(audiosListFile, s.toString(), Charset.forName("utf-8"));
            return audiosListFile;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    protected void merge(File imagesListFile, File audiosListFile, File videoFile)
            throws Exception {
        ProgressListener listener = new ProgressListener() {
            private long lastProgress = System.currentTimeMillis();
            private long lastStatus = System.currentTimeMillis();

            @Override
            public void onProgress(FFmpegProgress progress) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        long now = System.currentTimeMillis();
                        if (now > lastProgress + 500) {
                            updateLogs(message("Handled") + ":"
                                    + DateTools.showSeconds(progress.getTimeMillis() / 1000), true);
                            progressValue.setText(DateTools.showSeconds(progress.getTimeMillis() / 1000));
                            lastProgress = now;
                        }
                        if (now > lastStatus + 3000) {
                            long cost = now - mediaStart;
                            String s = message("Cost") + ": " + DateTools.showTime(cost);
                            statusLabel.setText(s);
                            lastStatus = now;
                        }
                    }
                });

            }
        };
        mediaStart = System.currentTimeMillis();
        FFmpeg ffmpeg = FFmpeg.atPath(executable.toPath().getParent())
                .addArguments("-f", "concat")
                .addArguments("-safe", "0")
                .addArguments("-i", imagesListFile.getAbsolutePath())
                .addArguments("-f", "concat")
                .addArguments("-safe", "0")
                .addArguments("-i", audiosListFile.getAbsolutePath())
                .addArguments("-s", width + "x" + height)
                .addArguments("-pix_fmt", "yuv420p")
                .addArguments("-b:a", audioBitrateInput.getText())
                .addOutput(UrlOutput.toPath(videoFile.toPath()))
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
        String volumn = volumnInput.getText().trim();
        if (!volumn.isBlank()) {
            ffmpeg.addArguments("-af", "volume=" + volumn);
        }
        if (stopCheck.isSelected()) {
            logger.debug("here");
            ffmpeg.addArgument("-shortest");
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

        updateLogs(message("ConvertingMedia") + "  " + message("TargetFile") + ":" + videoFile, true);
        FFmpegResult result = ffmpeg.execute();

        targetFileGenerated(videoFile);
        updateLogs(message("Size") + ": " + FileTools.showFileSize(result.getVideoSize()), true);
    }

}
