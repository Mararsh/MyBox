package mara.mybox.controller;

//import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
//import com.github.kokorin.jaffree.ffprobe.Stream;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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
public class ControlFFmpegOptions extends BaseController {

    protected BaseTaskController ffmpegController;
    protected String executableName, executableDefault;
    protected File executable;
    protected List<String> dataTypes;
    protected SingletonTask encoderTask, muxerTask, queryTask;
    protected String muxer, videoCodec, audioCodec, subtitleCodec, aspect, x264preset, volumn;
    protected boolean disableVideo, disableAudio, disableSubtitle;
    protected long mediaStart;
    protected int width, height, crf;
    protected float videoFrameRate, videoBitrate, audioBitrate, audioSampleRate;

    @FXML
    protected Label executableLabel;
    @FXML
    protected TextField executableInput, extensionInput, moreInput;
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
    protected CheckBox stereoCheck;
    @FXML
    protected Button helpMeButton;
    @FXML
    protected HBox durationBox;
    @FXML
    protected CheckBox shortestCheck;

    public ControlFFmpegOptions() {
        baseTitle = message("FFmpegOptions");
        TipsLabelKey = "FFmpegOptionsTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            executableName = "FFmpegExecutable";
            executableDefault = "win".equals(SystemTools.os()) ? "D:\\Programs\\ffmpeg\\bin\\ffmpeg.exe" : "/home/ffmpeg";

            disableVideo = disableAudio = disableSubtitle = false;
            width = height = -1;
            videoFrameRate = 24f;
            videoBitrate = 5000 * 1000;
            audioBitrate = 192;
            audioSampleRate = 44100;
            crf = -1;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (executableInput == null) {
                return;
            }

            executableInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue observable, String oldValue, String newValue) {
                    checkExecutableInput();
                }
            });

            if (functionBox != null) {
                functionBox.disableProperty().bind(executableInput.styleProperty().isEqualTo(UserConfig.badStyle()));
            }

            if (durationBox != null) {
                durationBox.setVisible(false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(executableInput, message("FFmpegExeComments"));
            if (moreInput != null) {
                NodeStyleTools.setTooltip(moreInput, message("SeparateBySpace"));
            }
            if (tipsArea != null) {
                tipsArea.setText(message("FFmpegArgumentsTips"));
            }
            if (crfSelector != null) {
                NodeStyleTools.setTooltip(crfSelector, message("CRFComments"));
            }
            if (x264presetSelector != null) {
                NodeStyleTools.setTooltip(x264presetSelector, message("X264PresetComments"));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(BaseTaskController ffmpegController) {
        try {
            this.ffmpegController = ffmpegController;
            executableInput.setText(UserConfig.getString(executableName, executableDefault));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void selectExecutable() {
        try {
            File file = FxFileTools.selectFile(this);
            if (file == null) {
                return;
            }
            executableInput.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    public void checkExecutableInput() {
        executable = null;
        if (helpMeButton != null) {
            helpMeButton.setDisable(true);
        }
        String v = executableInput.getText();
        if (v == null || v.isEmpty()) {
            executableInput.setStyle(UserConfig.badStyle());
            return;
        }
        final File file = new File(v);
        if (!file.exists()) {
            executableInput.setStyle(UserConfig.badStyle());
            return;
        }
        executable = file;
        executableInput.setStyle(null);
        UserConfig.setString(executableName, file.getAbsolutePath());

        readMuxers();
        readEncoders();
        if (helpMeButton != null) {
            helpMeButton.setDisable(false);
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
//        ffmpegController.tabPane.getSelectionModel().select(ffmpegController.logsTab);
        if (muxerTask != null) {
            muxerTask.cancel();
        }
        try {
            List<String> command = new ArrayList<>();
            command.add(executable.getAbsolutePath());
            command.add("-muxers");
            showCmd(command);
            ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
            final Process process = pb.start();

            muxerTask = new SingletonTask<Void>(this) {
                private List<String> muxers, commons;

                @Override
                protected boolean handle() {
                    error = null;
                    muxers = new ArrayList();
                    commons = new ArrayList();
                    List<String> commonNames = new ArrayList();
                    commonNames.addAll(Arrays.asList("mp4", "mp3", "aiff", "au", "avi", "flv", "mov", "wav", "m4v", "hls", "rtsp"));
                    try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                        String line;
                        int count = 0;
                        while ((line = inReader.readLine()) != null) {
                            ffmpegController.showLogs(line);
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
                    muxerSelector.getItems().add(0, message("OriginalFormat"));
                    muxerSelector.getSelectionModel().selectedItemProperty().addListener(
                            (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                                if (newValue != null && !newValue.isEmpty()) {
                                    UserConfig.setString("ffmpegDefaultMuter", newValue);
                                }
                                if (newValue == null || newValue.isEmpty()
                                || message("OriginalFormat").equals(newValue)
                                || message("NotSet").equals(newValue)) {
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
                    muxerSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultMuter", "mp4"));
                }

                @Override
                protected void finalAction() {
                    super.finalAction();
                    muxerTask = null;
                }
            };
            start(muxerTask);

            process.waitFor();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            popError(e.toString());
        } finally {
            muxerTask = null;
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
        if (encoderTask != null) {
            encoderTask.cancel();
        }
//            ffmpegController.tabPane.getSelectionModel().select(ffmpegController.logsTab);
        try {
            List<String> command = new ArrayList<>();
            command.add(executable.getAbsolutePath());
            command.add("-hide_banner");
            command.add("-encoders");
            showCmd(command);
            ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
            final Process process = pb.start();

            encoderTask = new SingletonTask<Void>(this) {
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
                    try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                        String line;
                        int count = 0;
                        while ((line = inReader.readLine()) != null) {
                            ffmpegController.showLogs(line);
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
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (audioEncoderSelector != null) {
                        audioEncoderSelector.getItems().addAll(aEncoders);
                        audioEncoderSelector.getItems().add(0, message("DisableAudio"));
                        audioEncoderSelector.getItems().add(0, message("CopyAudio"));
                        audioEncoderSelector.getItems().add(0, message("NotSet"));
                        initAudioControls();
                    }
                    if (videoEncoderSelector != null) {
                        videoEncoderSelector.getItems().addAll(videoCommons);
                        videoEncoderSelector.getItems().addAll(vEncoders);
                        videoEncoderSelector.getItems().add(0, message("DisableVideo"));
                        videoEncoderSelector.getItems().add(0, message("CopyVideo"));
                        videoEncoderSelector.getItems().add(0, message("NotSet"));
                        initVideoControls();
                    }
                    if (subtitleEncoderSelector != null) {
                        subtitleEncoderSelector.getItems().addAll(sEncoders);
                        subtitleEncoderSelector.getItems().add(0, message("DisableSubtitle"));
                        subtitleEncoderSelector.getItems().add(0, message("CopySubtitle"));
                        subtitleEncoderSelector.getItems().add(0, message("NotSet"));
                        initSubtitleControls();
                    }

                }

                @Override
                protected void finalAction() {
                    super.finalAction();
                    encoderTask = null;
                }
            };
            start(encoderTask);

            process.waitFor();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            popError(e.toString());
        } finally {
            encoderTask = null;
        }
    }

    public void initAudioControls() {
        try {
            if (audioEncoderSelector != null) {
                audioEncoderSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                UserConfig.setString("ffmpegDefaultAudioEncoder", newValue);
                            }
                            disableAudio = false;
                            if (newValue == null || newValue.isEmpty() || message("NotSet").equals(newValue)) {
                                audioCodec = null;
                                return;
                            }
                            if (message("DisableAudio").equals(newValue)) {
                                disableAudio = true;
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
                audioEncoderSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultAudioEncoder", "aac"));
            }

            if (audioBitrateSelector != null) {
                audioBitrateSelector.getItems().add(message("NotSet"));
                audioBitrateSelector.getItems().addAll(Arrays.asList(
                        "192kbps", "128kbps", "96kbps", "64kbps", "256kbps", "320kbps",
                        "32kbps", "1411.2kbps", "328kbps"
                ));
                audioBitrateSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                UserConfig.setString("ffmpegDefaultAudioBitrate", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
                                audioBitrate = 192;
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
                                float v = Float.parseFloat(value.trim());
                                if (v > 0) {
                                    audioBitrate = v;
                                    audioBitrateSelector.getEditor().setStyle(null);
                                } else {
                                    audioBitrateSelector.getEditor().setStyle(UserConfig.badStyle());
                                }
                            } catch (Exception e) {
                                audioBitrateSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        });
                audioBitrateSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultAudioBitrate", "192kbps"));
            }

            if (audioSampleRateSelector != null) {
                audioSampleRateSelector.getItems().add(message("NotSet"));
                audioSampleRateSelector.getItems().addAll(Arrays.asList(message("48000Hz"), message("44100Hz"), message("96000Hz"), message("8000Hz"),
                        message("11025Hz"), message("22050Hz"), message("24000Hz"), message("32000Hz"),
                        message("50000Hz"), message("47250Hz"), message("192000Hz")
                ));
                audioSampleRateSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                UserConfig.setString("ffmpegDefaultAudioSampleRate", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
                                audioSampleRate = 44100;
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
                                    audioSampleRateSelector.getEditor().setStyle(UserConfig.badStyle());
                                }
                            } catch (Exception e) {
                                audioSampleRateSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        });
                audioSampleRateSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultAudioSampleRate", message("44100Hz")));
            }

            if (volumnSelector != null) {
                volumnSelector.getItems().addAll(Arrays.asList(message("NotSet"),
                        message("10dB"), message("20dB"), message("5dB"), message("30dB"),
                        message("-10dB"), message("-20dB"), message("-5dB"), message("-30dB"),
                        message("1.5"), message("1.25"), message("2"), message("3"),
                        message("0.5"), message("0.8"), message("0.6")
                ));
                volumnSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                UserConfig.setString("ffmpegDefaultAudioVolumn", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
                                volumn = null;
                                return;
                            }
                            volumn = newValue;
                        });
                volumnSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultAudioVolumn", message("NotSet")));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initVideoControls() {
        try {
            setH264();
            setCRF();
            if (videoEncoderSelector != null) {
                videoEncoderSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            setVcodec(newValue);
                            setH264();
                            setCRF();
                        });
                videoEncoderSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultVideoEncoder", defaultVideoEcodec()));
            }

            if (aspectSelector != null) {
                aspectSelector.getItems().addAll(Arrays.asList(message("NotSet"), "4:3", "16:9"
                ));
                aspectSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                UserConfig.setString("ffmpegDefaultAspect", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
                                aspect = null;
                                return;
                            }
                            aspect = newValue;
                        });
                aspectSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultAspect", message("NotSet")));
            }

            // http://ffmpeg.org/ffmpeg-utils.html
            if (resolutionSelector != null) {
                resolutionSelector.getItems().add(message("NotSet"));
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
                                UserConfig.setString("ffmpegDefaultResolution", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
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
                String dres = UserConfig.getString("ffmpegDefaultResolution", "ntsc  720x480");
                resolutionSelector.getSelectionModel().select(dres);
            }

            if (videoFrameRateSelector != null) {
                videoFrameRateSelector.getItems().add(message("NotSet"));
                videoFrameRateSelector.getItems().addAll(Arrays.asList(
                        "ntsc  30000/1001", "pal  25/1", "qntsc  30000/1001", "qpal  25/1",
                        "sntsc  30000/1001", "spal  25/1", "film  24/1", "ntsc-film  24000/1001"
                ));
                videoFrameRateSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                UserConfig.setString("ffmpegDefaultFrameRate", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
                                videoFrameRate = 24f;
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
                videoFrameRateSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultFrameRate", "ntsc  30000/1001"));
            }

            if (videoBitrateSelector != null) {
                videoBitrateSelector.getItems().add(message("NotSet"));
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
                                UserConfig.setString("ffmpegDefaultVideoBitrate", newValue);
                            }
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
                                videoBitrate = 1800;
                                return;
                            }
                            try {
                                int pos = newValue.indexOf("kbps");
                                if (pos < 0) {
                                    pos = newValue.indexOf("mbps");
                                    if (pos < 0) {
                                        videoBitrateSelector.getEditor().setStyle(UserConfig.badStyle());
                                    } else {
                                        float f = Float.parseFloat(newValue.substring(0, pos).trim());
                                        if (f > 0) {
                                            videoBitrate = f * 1000;
                                            videoBitrateSelector.getEditor().setStyle(null);
                                        } else {
                                            videoBitrateSelector.getEditor().setStyle(UserConfig.badStyle());
                                        }
                                    }
                                } else {
                                    float f = Float.parseFloat(newValue.substring(0, pos).trim());
                                    if (f > 0) {
                                        videoBitrate = f;
                                        videoBitrateSelector.getEditor().setStyle(null);
                                    } else {
                                        videoBitrateSelector.getEditor().setStyle(UserConfig.badStyle());
                                    }
                                }
                            } catch (Exception e) {
                                videoBitrateSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        });
                videoBitrateSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultVideoBitrate", "1800kbps"));
            }

            if (crfSelector != null) {
                crfSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                if (newValue == null || newValue.isEmpty() || message("NotSet").equals(newValue)) {
                                    crf = -1;
                                    UserConfig.setString("ffmpegDefaultCRF", null);
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
                                    UserConfig.setString("ffmpegDefaultCRF", v + "");
                                }
                            } catch (Exception e) {
                            }
                        });
            }

            if (x264presetSelector != null) {
                x264presetSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue == null || newValue.isEmpty() || message("NotSet").equals(newValue)) {
                                x264preset = null;
                                UserConfig.setString("ffmpegx264preset", null);
                                return;
                            }
                            int pos = newValue.indexOf(' ');
                            if (pos < 0) {
                                x264preset = newValue;
                            } else {
                                x264preset = newValue.substring(0, pos);
                            }
                            UserConfig.setString("ffmpegx264preset", newValue);
                        });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initSubtitleControls() {
        try {
            if (subtitleEncoderSelector != null) {
                subtitleEncoderSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue != null && !newValue.isEmpty()) {
                                UserConfig.setString("ffmpegDefaultSubtitleEncoder", newValue);
                            }
                            disableSubtitle = false;
                            if (newValue == null || newValue.isEmpty()
                            || message("NotSet").equals(newValue)) {
                                subtitleCodec = null;
                                return;
                            }
                            if (message("DisableAudio").equals(newValue)) {
                                disableSubtitle = true;
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
                subtitleEncoderSelector.getSelectionModel().select(
                        UserConfig.getString("ffmpegDefaultSubtitleEncoder", message("NotSet")));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setVcodec(String value) {
        if (value != null && !value.isEmpty()) {
            UserConfig.setString("ffmpegDefaultVideoEncoder", value);
        }
        disableVideo = false;
        if (value == null || value.isEmpty() || message("NotSet").equals(value)) {
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
            x264presetSelector.getItems().addAll(Arrays.asList(message("NotSet"),
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
            x264presetSelector.getSelectionModel().select(UserConfig.getString("ffmpegx264preset", "medium"));
        } else if (videoCodec.contains("x264")) {
            x264presetSelector.getItems().addAll(Arrays.asList(message("NotSet"),
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
            x264presetSelector.getSelectionModel().select(UserConfig.getString("ffmpegx264preset", "medium  " + message("Medium")));
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
        crfSelector.getItems().add(message("NotSet"));
        for (int i = 0; i < max; ++i) {
            crfSelector.getItems().add(i + "");
        }
        crfSelector.getSelectionModel().select(UserConfig.getString("ffmpegDefaultCRF", defaultValue + ""));

    }

    public String defaultVideoEcodec() {
        if (videoEncoderSelector != null) {
            for (String item : videoEncoderSelector.getItems()) {
                if (item.contains("nvenc")) {
                    return item;
                }
            }
            for (String item : videoEncoderSelector.getItems()) {
                if (item.toLowerCase().contains("videotoolbox")) {
                    return item;
                }
            }
            for (String item : videoEncoderSelector.getItems()) {
                if (item.contains("x264") || item.contains("h264")) {
                    return item;
                }
            }
        }
        return null;
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
                videoEncoderSelector.getSelectionModel().select(defaultVideoEcodec());
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
                videoFrameRateSelector.getSelectionModel().select("ntsc  30000/1001");
            }
            if (videoBitrateSelector != null) {
                videoBitrateSelector.getSelectionModel().select("1800kbps");
            }
            if (audioBitrateSelector != null) {
                audioBitrateSelector.getSelectionModel().select("192kbps");
            }
            if (audioSampleRateSelector != null) {
                audioSampleRateSelector.getSelectionModel().select(message("44100Hz"));
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void aboutMedia() {
        openHtml(HelpTools.aboutMedia());
    }

    @FXML
    public void helpMe() {
        try {
            FFmpegInformationController controller
                    = (FFmpegInformationController) openStage(Fxmls.FFmpegInformationFxml);
            controller.queryInput.setText("-h");
            controller.tabPane.getSelectionModel().select(controller.queryTab);
            controller.goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void download() {
        openLink("http://ffmpeg.org/download.html");
    }

    protected boolean disableAudio() {
        return disableAudio;
    }

    protected boolean disableVideo() {
        return disableVideo;
    }

    protected boolean disableSubtitle() {
        return disableSubtitle;
    }

    protected List<String> makeParameters(List<String> inParameters) {
        try {
            List<String> parameters = new ArrayList<>();
            if (inParameters != null) {
                parameters.addAll(inParameters);
            }
            makeSpecialParameters(parameters);
            if (disableVideo()) {
                parameters.add("-vn");
            } else {
                makeVideoParameters(parameters);
            }
            if (disableAudio()) {
                parameters.add("-an");
            } else {
                makeAudioParameters(parameters);
            }
            if (disableSubtitle()) {
                parameters.add("-sn");
            } else {
                makeSubtitleParameters(parameters);
            }

            String more = moreInput.getText().trim();
            if (!more.isBlank()) {
                String[] args = StringTools.splitBySpace(more);
                if (args != null && args.length > 0) {
                    parameters.addAll(Arrays.asList(args));
                }
            }
            return parameters;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    protected List<String> makeSpecialParameters(List<String> parameters) {
        return parameters;
    }

    protected List<String> makeVideoParameters(List<String> parameters) {
        try {
            if (videoCodec != null) {
                parameters.add("-vcodec");
                parameters.add(videoCodec);
            }
            if (aspect != null) {
                parameters.add("-aspect");
                parameters.add(aspect);
            }
            parameters.add("-r");
            if (videoFrameRate > 0) {
                parameters.add(videoFrameRate + "");
            } else {
                parameters.add("30");
            }
            parameters.add("-b:v");
            if (videoBitrate > 0) {
                parameters.add(videoBitrate + "k");
            } else {
                parameters.add("5000k");
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return parameters;
    }

    protected List<String> makeAudioParameters(List<String> parameters) {
        try {
            if (audioCodec != null) {
                parameters.add("-acodec");
                parameters.add(audioCodec);
            }
            parameters.add("-b:a");
            if (audioBitrate > 0) {
                parameters.add(audioBitrate + "k");
            } else {
                parameters.add("192k");
            }
            parameters.add("-ar");
            if (audioSampleRate > 0) {
                parameters.add(audioSampleRate + "");
            } else {
                parameters.add("44100");
            }
            if (volumn != null) {
                parameters.add("-af");
                parameters.add("volume=" + volumn);
            }
            if (stereoCheck != null) {
                parameters.add("-ac");
                parameters.add(stereoCheck.isSelected() ? "2" : "1");
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return parameters;
    }

    protected List<String> makeSubtitleParameters(List<String> parameters) {
        try {
            if (subtitleCodec != null) {
                parameters.add("-scodec");
                parameters.add(subtitleCodec);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return parameters;
    }

    protected Process startProcess(BaseTaskController controller, List<String> parameters, File targetFile) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(executable.getAbsolutePath());
            cmd.addAll(parameters);
            if (targetFile != null) {
                cmd.add(targetFile.getAbsolutePath());
                FileDeleteTools.delete(targetFile);
            }
            parameters.add("-y");
            showCmd(cmd);

            ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
            Process process = pb.start();
            controller.showLogs("PID:" + process.pid());
            return process;
        } catch (Exception e) {
            controller.showLogs(e.toString());
            MyBoxLog.console(e);
            return null;
        }
    }

    protected void showCmd(List<String> command) {
        if (command == null || ffmpegController == null) {
            return;
        }
        ffmpegController.showLogs("\n\n" + command.toString().replaceAll("[\\[|,|\\]]", " "));
    }

}
