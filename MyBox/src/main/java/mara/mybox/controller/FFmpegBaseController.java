package mara.mybox.controller;

import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegBaseController extends FilesBatchController {

    protected String executableName, executableDefault;
    protected File executable;
    protected FFprobeResult probeResult;
    protected List<String> dataTypes;
    protected Stream audioStream, videoStream;
    protected List<Stream> otherStreams;
    protected SingletonTask encoderTask, muxerTask, queryTask;
    protected String muxer, videoCodec, audioCodec, subtitleCodec, aspect;
    protected boolean disableVideo, disbaleAudio, disbaleSubtitle, mustSetResolution;
    protected long mediaStart;
    protected int width, height;
    protected float videoFrameRate, videoBitrate, audioBitrate, audioSampleRate;

    @FXML
    protected Label executableLabel;
    @FXML
    protected TextField executableInput;
    @FXML
    protected VBox functionBox;
    @FXML
    protected TextArea tipsArea;
    @FXML
    protected TitledPane ffmpegPane, optionsPane;
    @FXML
    protected ComboBox<String> muxerSelector, audioEncoderSelector, videoEncoderSelector,
            subtitleEncoderSelector, aspectSelector, resolutionSelector, videoFrameRateSelector,
            videoBitrateSelector, audioBitrateSelector, audioSampleRateSelector;
    @FXML
    protected TextField extensionInput, volumnInput, moreInput;

    // http://www.luyixian.cn/news_show_306225.aspx
    // http://trac.ffmpeg.org/wiki/Capture/Desktop
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
    public void initializeNext() {
        try {
            if (executableInput != null) {
                executableInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue observable,
                            String oldValue, String newValue) {
                        checkExecutableInput();
                    }
                });
                executableInput.setText(AppVariables.getUserConfigValue(executableName, executableDefault));
            }

            if (functionBox != null) {
                functionBox.disableProperty().bind(executableInput.styleProperty().isEqualTo(badStyle));
            }

            if (tipsArea != null) {
                tipsArea.setText(message("FFmpegArgumentsTips"));
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

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            muxer = videoCodec = audioCodec = subtitleCodec = aspect = null;
            disableVideo = disbaleAudio = disbaleSubtitle = false;

            if (executableInput != null) {
                optionsValid.bind(executableInput.styleProperty().isNotEqualTo(badStyle));
            }

            if (muxerSelector != null) {
                muxerSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
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
                        });
            }
            if (audioEncoderSelector != null) {
                audioEncoderSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
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
                        });
            }
            if (videoEncoderSelector != null) {
                videoEncoderSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
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
                        });
            }
            if (subtitleEncoderSelector != null) {
                subtitleEncoderSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
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
                        });
            }

            aspect = null;
            if (aspectSelector != null) {
                aspectSelector.getItems().addAll(Arrays.asList(
                        message("NotSetting"), "4:3", "16:9"
                ));
                aspectSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                AppVariables.setUserConfigValue("ffmpegDefaultAspect", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                                aspect = null;
                                return;
                            }
                            aspect = newValue;
                        });
                aspectSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue("ffmpegDefaultAspect", message("NotSetting")));
            }

            // http://ffmpeg.org/ffmpeg-utils.html
            width = height = -1;
            if (resolutionSelector != null) {
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
                resolutionSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
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
                        });
                String dres = AppVariables.getUserConfigValue("ffmpegDefaultResolution", "ntsc  720x480");
                if (mustSetResolution && message("NotSetting").equals(dres)) {
                    dres = "ntsc  720x480";
                }
                resolutionSelector.getSelectionModel().select(dres);
            }

            videoFrameRate = -1;
            if (videoFrameRateSelector != null) {
                videoFrameRateSelector.getItems().add(message("NotSetting"));
                videoFrameRateSelector.getItems().addAll(Arrays.asList(
                        "ntsc  30000/1001", "pal  25/1", "qntsc  30000/1001", "qpal  25/1",
                        "sntsc  30000/1001", "spal  25/1", "film  24/1", "ntsc-film  24000/1001"
                ));
                videoFrameRateSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
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
                        });
                videoFrameRateSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue("ffmpegDefaultFrameRate", message("NotSetting")));
            }

            videoBitrate = -1;
            if (videoBitrateSelector != null) {
                videoBitrateSelector.getItems().add(message("NotSetting"));
                videoBitrateSelector.getItems().addAll(Arrays.asList(
                        "1800kbps", "1600kbps", "1300kbps", "2400kbps", "1150kbps",
                        "5mbps", "6mbps", "8mbps", "16mbps", "4mbps",
                        "40mbps", "65mbps", "10mbps", "20mbps", "15mbps",
                        "3500kbps", "3000kbps", "2000kbps", "1000kbps", "800kbps", "500kbps",
                        "250kbps", "120kbps", "60kbps", "30kbps"
                ));
                videoBitrateSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                AppVariables.setUserConfigValue("ffmpegDefaultVideoBitrate", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                                videoBitrate = -1;
                                return;
                            }
                            try {
                                int pos = newValue.indexOf("kbps");
                                if (pos < 0) {
                                    pos = newValue.indexOf("mbps");
                                    if (pos < 0) {
                                        videoBitrateSelector.getEditor().setStyle(badStyle);
                                    } else {
                                        float f = Float.parseFloat(newValue.substring(0, pos).trim());
                                        if (f > 0) {
                                            videoBitrate = f * 1000;
                                            videoBitrateSelector.getEditor().setStyle(null);
                                        } else {
                                            videoBitrateSelector.getEditor().setStyle(badStyle);
                                        }
                                    }
                                } else {
                                    float f = Float.parseFloat(newValue.substring(0, pos).trim());
                                    if (f > 0) {
                                        videoBitrate = f;
                                        videoBitrateSelector.getEditor().setStyle(null);
                                    } else {
                                        videoBitrateSelector.getEditor().setStyle(badStyle);
                                    }
                                }
                            } catch (Exception e) {
                                videoBitrateSelector.getEditor().setStyle(badStyle);
                            }
                        });
                videoBitrateSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue("ffmpegDefaultVideoBitrate", message("NotSetting")));
            }

            audioBitrate = -1;
            if (audioBitrateSelector != null) {
                audioBitrateSelector.getItems().add(message("NotSetting"));
                audioBitrateSelector.getItems().addAll(Arrays.asList(
                        message("192kbps"), message("128kbps"), message("96kbps"), message("64kbps"),
                        message("256kbps"), message("320kbps"), message("32kbps"), message("1411.2kbps"),
                        message("328kbps")
                ));
                audioBitrateSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                AppVariables.setUserConfigValue("ffmpegDefaultAudioBitrate", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                                audioBitrate = -1;
                                return;
                            }
                            try {
                                int pos = newValue.indexOf("kbps");
                                String value;
                                if (pos < 0) {
                                    value = newValue;
                                } else {
                                    value = newValue.substring(0, pos);
                                }
                                float v = Float.valueOf(value.trim());
                                if (v > 0) {
                                    audioBitrate = v;
                                    audioBitrateSelector.getEditor().setStyle(null);
                                } else {
                                    audioBitrateSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                audioBitrateSelector.getEditor().setStyle(badStyle);
                            }
                        });
                audioBitrateSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue("ffmpegDefaultAudioBitrate", message("NotSetting")));
            }

            audioSampleRate = -1;
            if (audioSampleRateSelector != null) {
                audioSampleRateSelector.getItems().add(message("NotSetting"));
                audioSampleRateSelector.getItems().addAll(Arrays.asList(
                        message("48000Hz"), message("44100Hz"), message("96000Hz"), message("8000Hz"),
                        message("11025Hz"), message("22050Hz"), message("24000Hz"), message("32000Hz"),
                        message("50000Hz"), message("47250Hz"), message("192000Hz")
                ));
                audioSampleRateSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                AppVariables.setUserConfigValue("ffmpegDefaultAudioSampleRate", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                                audioSampleRate = -1;
                                return;
                            }
                            try {
                                int pos = newValue.indexOf("Hz");
                                String value;
                                if (pos < 0) {
                                    value = newValue;
                                } else {
                                    value = newValue.substring(0, pos);
                                }
                                int v = Integer.parseInt(value.trim());
                                if (v > 0) {
                                    audioSampleRate = v;
                                    audioSampleRateSelector.getEditor().setStyle(null);
                                } else {
                                    audioSampleRateSelector.getEditor().setStyle(badStyle);
                                }
                            } catch (Exception e) {
                                audioSampleRateSelector.getEditor().setStyle(badStyle);
                            }
                        });
                audioSampleRateSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue("ffmpegDefaultAudioSampleRate", message("NotSetting")));
            }

            if (volumnInput != null) {
                FxmlControl.setTooltip(volumnInput, message("ChangeVolumeComments"));
            }
            if (moreInput != null) {
                FxmlControl.setTooltip(moreInput, message("SeparateBySpace"));
            }

            if (progressValue != null) {
                progressValue.setPrefWidth(200);
            }

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

                    @Override
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

}
