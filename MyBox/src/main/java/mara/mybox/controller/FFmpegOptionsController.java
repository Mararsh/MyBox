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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.data.StringTable;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-06-02
 * @License Apache License Version 2.0
 */
// http://trac.ffmpeg.org/wiki/Encode/H.264
// http://trac.ffmpeg.org/wiki/Capture/Desktop
// https://slhck.info/video/2017/02/24/crf-guide.html
// https://slhck.info/video/2017/03/01/rate-control.html
// https://www.cnblogs.com/sunny-li/p/9979796.html
// http://www.luyixian.cn/news_show_306225.aspx
public class FFmpegOptionsController extends BaseController {

    protected String executableName, executableDefault;
    protected File executable;
    protected FFprobeResult probeResult;
    protected List<String> dataTypes;
    protected Stream audioStream, videoStream;
    protected List<Stream> otherStreams;
    protected SingletonTask encoderTask, muxerTask, queryTask;
    protected String muxer, videoCodec, audioCodec, subtitleCodec, aspect, x264preset, volumn;
    protected boolean disableVideo, disbaleAudio, disbaleSubtitle;
    protected long mediaStart;
    protected int width, height, crf;
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
            crfSelector, x264presetSelector,
            subtitleEncoderSelector, aspectSelector, resolutionSelector, videoFrameRateSelector,
            videoBitrateSelector, audioBitrateSelector, audioSampleRateSelector, volumnSelector;
    @FXML
    protected TextField extensionInput, moreInput;
    @FXML
    protected CheckBox stereoCheck;

    public FFmpegOptionsController() {
        baseTitle = AppVariables.message("FFmpegOptions");

        TipsLabelKey = "FFmpegOptionsTips";

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
    public void initControls() {
        try {
            super.initControls();
            if (executableInput == null) {
                return;
            }

            FxmlControl.setTooltip(executableInput, message("FFmpegExeComments"));

            muxer = videoCodec = x264preset = audioCodec = subtitleCodec = aspect = null;
            disableVideo = disbaleAudio = disbaleSubtitle = false;

            executableInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue observable,
                        String oldValue, String newValue) {
                    checkExecutableInput();
                }
            });
            executableInput.setText(AppVariables.getUserConfigValue(executableName, executableDefault));

            if (functionBox != null) {
                functionBox.disableProperty().bind(executableInput.styleProperty().isEqualTo(badStyle));
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
                            if (newValue == null || newValue.isEmpty() || message("NotSetting").equals(newValue)) {
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
            setH264();
            setCRF();
            if (videoEncoderSelector != null) {
                videoEncoderSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            setVcodec(newValue);
                            setH264();
                            setCRF();
                        });
            }

            crf = -1;
            if (crfSelector != null) {
                FxmlControl.setTooltip(crfSelector, message("CRFComments"));
                crfSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                if (newValue == null || newValue.isEmpty() || message("NotSetting").equals(newValue)) {
                                    crf = -1;
                                    AppVariables.setUserConfigValue("ffmpegDefaultCRF", null);
                                    return;
                                }
                                int pos = newValue.indexOf(' ');
                                String s = newValue;
                                if (pos >= 0) {
                                    s = newValue.substring(0, pos);
                                }
                                int v = Integer.parseInt(s);
                                if (v > 0) {
                                    crf = v;
                                    AppVariables.setUserConfigValue("ffmpegDefaultCRF", v + "");
                                }
                            } catch (Exception e) {
                            }
                        });
            }

            if (x264presetSelector != null) {
                FxmlControl.setTooltip(x264presetSelector, message("X264PresetComments"));
                x264presetSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue == null || newValue.isEmpty() || message("NotSetting").equals(newValue)) {
                                x264preset = null;
                                AppVariables.setUserConfigValue("ffmpegx264preset", null);
                                return;
                            }
                            int pos = newValue.indexOf(' ');
                            if (pos < 0) {
                                x264preset = newValue;
                            } else {
                                x264preset = newValue.substring(0, pos);
                            }
                            AppVariables.setUserConfigValue("ffmpegx264preset", newValue);
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
                resolutionSelector.getItems().add(message("NotSetting"));
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

            volumn = null;
            if (volumnSelector != null) {
                FxmlControl.setTooltip(volumnSelector, message("ChangeVolumeComments"));
                volumnSelector.getItems().addAll(Arrays.asList(
                        message("NotSetting"),
                        message("10dB"), message("20dB"), message("5dB"), message("30dB"),
                        message("-10dB"), message("-20dB"), message("-5dB"), message("-30dB"),
                        message("1.5"), message("1.25"), message("2"), message("3"),
                        message("0.5"), message("0.8"), message("0.6")
                ));
                volumnSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                AppVariables.setUserConfigValue("ffmpegDefaultAudioVolumn", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSetting").equals(newValue)) {
                                volumn = null;
                                return;
                            }
                            volumn = newValue;
                        });
                volumnSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue("ffmpegDefaultAudioVolumn", message("NotSetting")));
            }

            if (moreInput != null) {
                FxmlControl.setTooltip(moreInput, message("SeparateBySpace"));
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

        readMuxers();
        readEncoders();
    }

    @FXML
    public void download() {
        try {
            browseURI(new URI("http://ffmpeg.org/download.html"));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void readMuxers() {
        if (muxerSelector == null) {
            return;
        }
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
        if (executable == null) {
            return;
        }
        if (audioEncoderSelector != null) {
            audioEncoderSelector.getItems().clear();
        }
        if (videoEncoderSelector != null) {
            videoEncoderSelector.getItems().clear();
        }
        if (subtitleEncoderSelector != null) {
            subtitleEncoderSelector.getItems().clear();
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
                        commonVideoNames.addAll(Arrays.asList("flv", "x264", "x265", "libvpx", "h264"));
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
                                        if (encoder.contains(common)) {
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
                        if (audioEncoderSelector != null) {
                            audioEncoderSelector.getItems().addAll(aEncoders);
                            audioEncoderSelector.getItems().add(0, AppVariables.message("DisableAudio"));
                            audioEncoderSelector.getItems().add(0, AppVariables.message("CopyAudio"));
                            audioEncoderSelector.getItems().add(0, AppVariables.message("NotSetting"));
                            audioEncoderSelector.getSelectionModel().select(
                                    AppVariables.getUserConfigValue("ffmpegDefaultAudioEncoder", "aac"));
                        }
                        if (videoEncoderSelector != null) {
                            videoEncoderSelector.getItems().addAll(videoCommons);
                            videoEncoderSelector.getItems().addAll(vEncoders);
                            videoEncoderSelector.getItems().add(0, AppVariables.message("DisableVideo"));
                            videoEncoderSelector.getItems().add(0, AppVariables.message("CopyVideo"));
                            videoEncoderSelector.getItems().add(0, AppVariables.message("NotSetting"));
                            videoEncoderSelector.getSelectionModel().select(
                                    AppVariables.getUserConfigValue("ffmpegDefaultVideoEncoder", "libx264"));
                        }
                        if (subtitleEncoderSelector != null) {
                            subtitleEncoderSelector.getItems().addAll(sEncoders);
                            subtitleEncoderSelector.getItems().add(0, AppVariables.message("DisableSubtitle"));
                            subtitleEncoderSelector.getItems().add(0, AppVariables.message("CopySubtitle"));
                            subtitleEncoderSelector.getItems().add(0, AppVariables.message("NotSetting"));
                            subtitleEncoderSelector.getSelectionModel().select(
                                    AppVariables.getUserConfigValue("ffmpegDefaultSubtitleEncoder", "srt"));
                        }
                    }

                    @Override
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

    protected void setVcodec(String value) {
        if (value != null && !value.isEmpty()) {
            AppVariables.setUserConfigValue("ffmpegDefaultVideoEncoder", value);
        }
        disableVideo = false;
        if (value == null || value.isEmpty() || message("NotSetting").equals(value)) {
            videoCodec = null;
            return;
        }
        if (message("DisableAudio").equals(value)) {
            disableVideo = true;
            videoCodec = null;
            return;
        }
        if (message("CopyVideo").equals(value)) {
            videoCodec = "copy";
            return;
        }
        int pos = value.indexOf(' ');
        if (pos < 0) {
            videoCodec = value;
        } else {
            videoCodec = value.substring(0, pos);
        }

    }

    protected void setH264() {
        x264preset = null;
        if (x264presetSelector == null) {
            return;
        }
        x264presetSelector.getItems().clear();
        if (videoCodec == null) {
            return;
        }
        if (videoCodec.contains("h264") && videoCodec.contains("nvenc")) {
            x264preset = null;
            x264presetSelector.getItems().addAll(Arrays.asList(
                    message("NotSetting"),
                    "slow  1  hq 2 passes",
                    "medium  2  hq 1 pass",
                    "fast  3  hp 1 pass",
                    "hp  4",
                    "hq  5",
                    "bd  6",
                    "ll  7  low latency",
                    "llhq  8  low latency hq",
                    "llhp  9  low latency hp",
                    "lossless  10  hq 2 passes",
                    "losslesshp  11"
            ));
            x264presetSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ffmpegx264preset", "medium"));
        } else if (videoCodec.contains("x264")) {
            x264presetSelector.getItems().addAll(Arrays.asList(
                    message("NotSetting"),
                    "ultrafast  " + message("Ultrafast"),
                    "superfast  " + message("Superfast"),
                    "veryfast  " + message("Veryfast"),
                    "faster  " + message("Faster"),
                    "fast  " + message("Fast"),
                    "medium  " + message("Medium"),
                    "slow  " + message("Slow"),
                    "slower  " + message("Slower"),
                    "veryslow  " + message("Veryslow")
            ));
            x264presetSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("ffmpegx264preset", "medium  " + message("Medium")));
        }
    }

    protected void setCRF() {
        crf = -1;
        if (crfSelector == null) {
            return;
        }
        crfSelector.getItems().clear();
        if (videoCodec == null) {
            return;
        }
        int max = 0, defaultValue;
        if (videoCodec.contains("x264")) {
            max = 51;
            defaultValue = 23;
        } else if (videoCodec.contains("x265")) {
            max = 51;
            defaultValue = 28;
        } else if (videoCodec.contains("libvpx")) {
            max = 63;
            defaultValue = 31;
        } else {
            return;
        }
        crfSelector.getItems().add(message("NotSetting"));
        for (int i = 0; i < max; ++i) {
            crfSelector.getItems().add(i + "");
        }
        crfSelector.getSelectionModel().select(AppVariables.getUserConfigValue("ffmpegDefaultCRF", defaultValue + ""));

    }

    @FXML
    public void defaultAction() {
        try {
            if (muxerSelector != null) {
                for (String item : muxerSelector.getItems()) {
                    if (item.toLowerCase().contains("mp4")) {
                        muxerSelector.getSelectionModel().select(item);
                        break;
                    }
                }
            }
            if (audioEncoderSelector != null) {
                for (String item : audioEncoderSelector.getItems()) {
                    if (item.toLowerCase().contains("aac")) {
                        audioEncoderSelector.getSelectionModel().select(item);
                        break;
                    }
                }
            }
            if (videoEncoderSelector != null) {
                boolean nvenc = false;
                for (String item : videoEncoderSelector.getItems()) {
                    if (item.contains("nvenc")) {
                        videoEncoderSelector.getSelectionModel().select(item);
                        nvenc = true;
                        break;
                    }
                }
                if (!nvenc) {
                    for (String item : videoEncoderSelector.getItems()) {
                        if (item.contains("x264")) {
                            videoEncoderSelector.getSelectionModel().select(item);
                            break;
                        }
                    }
                }
            }
            if (videoFrameRateSelector != null) {
                videoFrameRateSelector.getSelectionModel().select(null);
            }
            if (crfSelector != null) {
                crfSelector.getSelectionModel().select(null);
            }
            if (x264presetSelector != null) {
                x264presetSelector.getSelectionModel().select(null);
            }
            if (subtitleEncoderSelector != null) {
                subtitleEncoderSelector.getSelectionModel().select(null);
            }
            if (aspectSelector != null) {
                aspectSelector.getSelectionModel().select(null);
            }
            if (resolutionSelector != null) {
                resolutionSelector.getSelectionModel().select(null);
            }
            if (videoFrameRateSelector != null) {
                videoFrameRateSelector.getSelectionModel().select(null);
            }
            if (videoBitrateSelector != null) {
                videoBitrateSelector.getSelectionModel().select(null);
            }
            if (audioBitrateSelector != null) {
                audioBitrateSelector.getSelectionModel().select(null);
            }
            if (audioSampleRateSelector != null) {
                audioSampleRateSelector.getSelectionModel().select(null);
            }
            if (volumnSelector != null) {
                volumnSelector.getSelectionModel().select(null);
            }
            if (moreInput != null) {
                moreInput.setText("");
            }
            if (stereoCheck != null) {
                stereoCheck.setSelected(true);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void aboutMedia() {
        try {
            StringTable table = new StringTable(null, message("AboutMedia"));
            table.newLinkRow("FFmpegDocuments", "http://ffmpeg.org/documentation.html");
            table.newLinkRow("FFmpeg wiki", "https://trac.ffmpeg.org");
            table.newLinkRow("H264VideoEncodingGuide", "http://trac.ffmpeg.org/wiki/Encode/H.264");
            table.newLinkRow("AACEncodingGuide", "https://trac.ffmpeg.org/wiki/Encode/AAC");
            table.newLinkRow("UnderstandingRateControlModes", "https://slhck.info/video/2017/03/01/rate-control.html");
            table.newLinkRow("CRFGuide", "https://slhck.info/video/2017/02/24/crf-guide.html");
            table.newLinkRow("CapturingDesktopScreenRecording", "http://trac.ffmpeg.org/wiki/Capture/Desktop");

            File htmFile = HtmlTools.writeHtml(table.html());
            FxmlStage.browseURI(getMyStage(), htmFile.toURI());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
