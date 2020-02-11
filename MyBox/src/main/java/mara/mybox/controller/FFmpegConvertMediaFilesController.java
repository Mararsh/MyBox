package mara.mybox.controller;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
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

    protected FFprobeResult probeResult;
    protected List<String> dataTypes;
    protected Stream audioStream, videoStream;
    protected List<Stream> otherStreams;
    protected SingletonTask encoderTask, muxerTask, queryTask;
    protected String muxer, videoCodec, audioCodec, subtitleCodec, aspect;
    protected boolean disableVideo, disbaleAudio, disbaleSubtitle, mustSetResolution;
    protected long mediaStart;
    protected int width, height;
    protected float videoFrameRate;

    @FXML
    protected TitledPane ffmpegPane, optionsPane;
    @FXML
    protected ComboBox<String> muxerSelector, audioEncoderSelector, videoEncoderSelector,
            subtitleEncoderSelector, aspectSelector, resolutionSelector, videoFrameRateSelector;
    @FXML
    protected TextField extensionInput, volumnInput, moreInput;
    @FXML
    protected ImageView argumentTipsView;

    public FFmpegConvertMediaFilesController() {
        baseTitle = AppVariables.message("FFmpegConvertMediaFiles");
        mustSetResolution = false;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            executableName = "FFmpegExecutable";
            executableDefault = "D:\\Programs\\ffmpeg\\bin\\ffmpeg.exe";

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            muxer = videoCodec = audioCodec = subtitleCodec = aspect = null;
            disableVideo = disbaleAudio = disbaleSubtitle = false;

            optionsValid.bind(executableInput.styleProperty().isNotEqualTo(badStyle));

            muxerSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultMuter", newValue);
                    }
                    if (newValue == null || newValue.isEmpty()
                            || message("OriginalFormat").equals(newValue)
                            || message("NotSetting").equals(newValue)) {
                        extensionInput.setText(message("OriginalFormat"));
                        muxer = null;
                        return;
                    }
                    int pos = newValue.indexOf(' ');
                    if (pos < 0) {
                        muxer = newValue;
                    } else {
                        muxer = newValue.substring(0, pos);
                    }
                    if (muxer.equals("hls")) {
                        extensionInput.setText("m3u8");
                    } else {
                        extensionInput.setText(muxer);
                    }
                }
            });
            audioEncoderSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultAudioEncoder", newValue);
                    }
                    disbaleAudio = false;
                    if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                        audioCodec = null;
                        return;
                    }
                    if (message("DisableAudio").equals(newValue)) {
                        disbaleAudio = true;
                        audioCodec = null;
                        return;
                    }
                    if (message("CopyAudio").equals(newValue)) {
                        audioCodec = "copy";
                        return;
                    }
                    int pos = newValue.indexOf(' ');
                    if (pos < 0) {
                        audioCodec = newValue;
                    } else {
                        audioCodec = newValue.substring(0, pos);
                    }
                }
            });
            videoEncoderSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultVideoEncoder", newValue);
                    }
                    disableVideo = false;
                    if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                        videoCodec = null;
                        return;
                    }
                    if (message("DisableAudio").equals(newValue)) {
                        disableVideo = true;
                        videoCodec = null;
                        return;
                    }
                    if (message("CopyVideo").equals(newValue)) {
                        videoCodec = "copy";
                        return;
                    }
                    int pos = newValue.indexOf(' ');
                    if (pos < 0) {
                        videoCodec = newValue;
                    } else {
                        videoCodec = newValue.substring(0, pos);
                    }
                }
            });
            subtitleEncoderSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultSubtitleEncoder", newValue);
                    }
                    disbaleSubtitle = false;
                    if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                        subtitleCodec = null;
                        return;
                    }
                    if (message("DisableAudio").equals(newValue)) {
                        disbaleSubtitle = true;
                        subtitleCodec = null;
                        return;
                    }
                    if (message("CopySubtitle").equals(newValue)) {
                        subtitleCodec = "copy";
                        return;
                    }
                    int pos = newValue.indexOf(' ');
                    if (pos < 0) {
                        subtitleCodec = newValue;
                    } else {
                        subtitleCodec = newValue.substring(0, pos);
                    }
                }
            });

            aspect = null;
            aspectSelector.getItems().addAll(Arrays.asList(
                    message("NotSetting"), "4:3", "16:9"
            ));
            aspectSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultAspect", newValue);
                    }
                    if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                        aspect = null;
                        return;
                    }
                    aspect = newValue;
                }
            });
            aspectSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ffmpegDefaultAspect", message("NotSetting")));

            // http://ffmpeg.org/ffmpeg-utils.html
            width = height = -1;
            if (!mustSetResolution) {
                resolutionSelector.getItems().add(message("NotSetting"));
            }
            resolutionSelector.getItems().addAll(Arrays.asList(
                    "ntsc  720x480", "pal  720x576", "qntsc  352x240", "qpal  352x288", "sntsc  640x480",
                    "spal  768x576", "film  352x240", "ntsc-film  352x240", "sqcif  128x96", "qcif  176x144",
                    "cif  352x288", "4cif  704x576", "16cif  1408x1152", "qqvga  160x120", "qvga  320x240",
                    "vga  640x480", "svga  800x600", "xga  1024x768", "uxga  1600x1200", "qxga  2048x1536",
                    "sxga  1280x1024", "qsxga  2560x2048", "hsxga  5120x4096", "wvga  852x480", "wxga  1366x768",
                    "wsxga  1600x1024", "wuxga  1920x1200", "woxga  2560x1600", "wqsxga  3200x2048",
                    "wquxga  3840x2400", "whsxga  6400x4096", "whuxga  7680x4800", "cga  320x200",
                    "ega  640x350", "hd480  852x480", "hd720  1280x720", "hd1080  1920x1080", "2k  2048x1080",
                    "2kflat  1998x1080", "2kscope  2048x858", "4k  4096x2160", "4kflat  3996x2160", "4kscope  4096x1716",
                    "nhd  640x360", "hqvga  240x160", "wqvga  400x240", "fwqvga  432x240", "hvga  480x320",
                    "qhd  960x540", "2kdci  2048x1080", "4kdci  4096x2160", "uhd2160  3840x2160", "uhd4320  7680x4320"
            ));
            resolutionSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultResolution", newValue);
                    }
                    if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                        width = height = -1;
                        return;
                    }
                    try {
                        String value = newValue.substring(newValue.lastIndexOf(' ') + 1);
                        int pos = value.indexOf('x');
                        width = Integer.parseInt(value.substring(0, pos));
                        height = Integer.parseInt(value.substring(pos + 1));
                    } catch (Exception e) {
                    }
                }
            });
            String dres = AppVariables.getUserConfigValue("ffmpegDefaultResolution", "ntsc  720x480");
            if (mustSetResolution && message("NotSetting").equals(dres)) {
                dres = "ntsc  720x480";
            }
            resolutionSelector.getSelectionModel().select(dres);

            videoFrameRate = -1;
            videoFrameRateSelector.getItems().add(message("NotSetting"));
            videoFrameRateSelector.getItems().addAll(Arrays.asList(
                    "ntsc  30000/1001", "pal  25/1", "qntsc  30000/1001", "qpal  25/1",
                    "sntsc  30000/1001", "spal  25/1", "film  24/1", "ntsc-film  24000/1001"
            ));
            videoFrameRateSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        AppVariables.setUserConfigValue("ffmpegDefaultFrameRate", newValue);
                    }
                    if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                        videoFrameRate = -1;
                        return;
                    }
                    try {
                        String value = newValue.substring(newValue.lastIndexOf(' ') + 1);
                        int pos = value.indexOf('/');
                        videoFrameRate = Integer.parseInt(value.substring(0, pos)) * 1f
                                / Integer.parseInt(value.substring(pos + 1));
                    } catch (Exception e) {
                    }
                }
            });
            videoFrameRateSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ffmpegDefaultFrameRate", message("NotSetting")));

            FxmlControl.setTooltip(volumnInput, message("ChangeVolumeComments"));
            FxmlControl.setTooltip(moreInput, message("SeparateBySpace"));

            progressValue.setPrefWidth(200);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(argumentTipsView, message("FFmpegArgumentsTips"));
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

    public void readMuxers() {
        muxerSelector.getItems().clear();
        if (executable == null) {
            return;
        }
        synchronized (this) {
            if (muxerTask != null) {
                return;
            }
            try {
                List<String> command = new ArrayList<>();
                command.add(executable.getAbsolutePath());
                command.add("-hide_banner");
                command.add("-muxers");
                ProcessBuilder pb = new ProcessBuilder(command)
                        .redirectErrorStream(true);
                pb.redirectErrorStream(true);
                final Process process = pb.start();

                muxerTask = new SingletonTask<Void>() {
                    private List<String> muxers, commons;

                    @Override
                    protected boolean handle() {
                        error = null;
                        muxers = new ArrayList();
                        commons = new ArrayList();
                        List<String> commonNames = new ArrayList();
                        commonNames.addAll(Arrays.asList("mp4", "mp3", "aiff", "au", "avi", "flv", "mov", "wav", "m4v", "hls", "rtsp"));
                        try ( BufferedReader inReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String line;
                            int count = 0;
                            while ((line = inReader.readLine()) != null) {
                                count++;
                                if (count < 4 || line.length() < 5) {
                                    continue;
                                }
                                String muxer = line.substring(4);
                                for (String common : commonNames) {
                                    if (muxer.startsWith(common + " ")) {
                                        commons.add(muxer);
                                        break;
                                    }
                                }
                                muxers.add(muxer);
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error != null) {
                            popError(error);
                        }
                        muxerSelector.getItems().addAll(commons);
                        muxerSelector.getItems().addAll(muxers);
                        muxerSelector.getItems().add(0, AppVariables.message("OriginalFormat"));
                        muxerSelector.getSelectionModel().select(
                                AppVariables.getUserConfigValue("ffmpegDefaultMuter", "mp4"));
                    }

                    protected void taskQuit() {
                        muxerTask = null;
                    }
                };

                openHandlingStage(muxerTask, Modality.WINDOW_MODAL);
                Thread thread = new Thread(muxerTask);
                thread.setDaemon(true);
                thread.start();

                process.waitFor();

            } catch (Exception e) {
                logger.debug(e.toString());
                popError(e.toString());
            } finally {
                muxerTask = null;
            }
        }
    }

    public void readEncoders() {
        audioEncoderSelector.getItems().clear();
        videoEncoderSelector.getItems().clear();
        subtitleEncoderSelector.getItems().clear();
        if (executable == null) {
            return;
        }
        synchronized (this) {
            if (encoderTask != null) {
                return;
            }
            try {
                List<String> command = new ArrayList<>();
                command.add(executable.getAbsolutePath());
                command.add("-hide_banner");
                command.add("-encoders");
                ProcessBuilder pb = new ProcessBuilder(command)
                        .redirectErrorStream(true);
                pb.redirectErrorStream(true);
                final Process process = pb.start();

                encoderTask = new SingletonTask<Void>() {
                    private List<String> aEncoders, vEncoders, sEncoders, videoCommons;

                    @Override
                    protected boolean handle() {
                        error = null;
                        aEncoders = new ArrayList();
                        vEncoders = new ArrayList();
                        sEncoders = new ArrayList();
                        videoCommons = new ArrayList();
                        List<String> commonVideoNames = new ArrayList();
                        commonVideoNames.addAll(Arrays.asList("flv", "libx264", "libx265"));
                        try ( BufferedReader inReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {
                            String line;
                            int count = 0;
                            while ((line = inReader.readLine()) != null) {
                                count++;
                                if (count < 10 || line.length() < 9) {
                                    continue;
                                }
                                String type = line.substring(0, 8);
                                String encoder = line.substring(8);
                                if (type.contains("V")) {
                                    for (String common : commonVideoNames) {
                                        if (encoder.startsWith(common + " ")) {
                                            videoCommons.add(encoder);
                                            break;
                                        }
                                    }
                                    vEncoders.add(encoder);
                                } else if (type.contains("A")) {
                                    aEncoders.add(encoder);
                                } else if (type.contains("S")) {
                                    sEncoders.add(encoder);
                                }
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error != null) {
                            popError(error);
                        }

                        audioEncoderSelector.getItems().addAll(aEncoders);
                        audioEncoderSelector.getItems().add(0, AppVariables.message("DisableAudio"));
                        audioEncoderSelector.getItems().add(0, AppVariables.message("CopyAudio"));
                        audioEncoderSelector.getItems().add(0, AppVariables.message("NotSetting"));
                        audioEncoderSelector.getSelectionModel().select(
                                AppVariables.getUserConfigValue("ffmpegDefaultAudioEncoder", "aac"));

                        videoEncoderSelector.getItems().addAll(videoCommons);
                        videoEncoderSelector.getItems().addAll(vEncoders);
                        videoEncoderSelector.getItems().add(0, AppVariables.message("DisableVideo"));
                        videoEncoderSelector.getItems().add(0, AppVariables.message("CopyVideo"));
                        videoEncoderSelector.getItems().add(0, AppVariables.message("NotSetting"));
                        videoEncoderSelector.getSelectionModel().select(
                                AppVariables.getUserConfigValue("ffmpegDefaultVideoEncoder", "libx264"));

                        subtitleEncoderSelector.getItems().addAll(sEncoders);
                        subtitleEncoderSelector.getItems().add(0, AppVariables.message("DisableSubtitle"));
                        subtitleEncoderSelector.getItems().add(0, AppVariables.message("CopySubtitle"));
                        subtitleEncoderSelector.getItems().add(0, AppVariables.message("NotSetting"));
                        subtitleEncoderSelector.getSelectionModel().select(
                                AppVariables.getUserConfigValue("ffmpegDefaultSubtitleEncoder", "srt"));

                    }

                    protected void taskQuit() {
                        encoderTask = null;
                    }
                };

                openHandlingStage(encoderTask, Modality.WINDOW_MODAL);
                Thread thread = new Thread(encoderTask);
                thread.setDaemon(true);
                thread.start();

                process.waitFor();

            } catch (Exception e) {
                logger.debug(e.toString());
                popError(e.toString());
            } finally {
                encoderTask = null;
            }
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
                    .setProgressListener(new ProgressListener() {
                        @Override
                        public void onProgress(FFmpegProgress progress) {
                            countedDuration.set(progress.getTimeMillis());
                        }
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
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
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
