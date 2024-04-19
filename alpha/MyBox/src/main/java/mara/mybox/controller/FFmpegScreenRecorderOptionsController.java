package mara.mybox.controller;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.SystemTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-06-26
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderOptionsController extends ControlFFmpegOptions {

    protected Rectangle snapArea;
    protected int audioThreadQueueSize, videoThreadQueueSize,
            screenWidth, screenHeight, x, y;
    protected String os, vcodec, title;
    protected List<String> videoDevices, audioDevices;

    @FXML
    protected CheckBox audioCheck, videoCheck;
    @FXML
    protected VBox videoBox;
    @FXML
    protected ComboBox<String> videoDeviceSelector, audioDeviceSelector;
    @FXML
    protected HBox fullScreenBox, windowBox, rectBox;
    @FXML
    protected Label screenSizeLabel, infoLabel;
    @FXML
    protected TextField audioThreadQueueSizeInput, videoThreadQueueSizeInput,
            titleInput, xInput, yInput, widthInput, heightInput, vcodecInput;
    @FXML
    protected ToggleGroup scopeGroup, vcodecGroup;
    @FXML
    protected RadioButton fullscreenRadio, windowRadio, rectangleRadio,
            libx264rgbRadio, h264nvencRadio, libx264Radio, vcodecRadio;
    @FXML
    protected ControlTimeLength durationController, delayController;
    @FXML
    protected Button queryVideoDeviceButton, queryAudioDeviceButton;

    // http://trac.ffmpeg.org/wiki/Capture/Desktop
    // http://trac.ffmpeg.org/wiki/Encode/H.264
    // https://slhck.info/video/2017/02/24/crf-guide.html
    // https://slhck.info/video/2017/03/01/rate-control.html
    // https://www.cnblogs.com/sunny-li/p/9979796.html
    public FFmpegScreenRecorderOptionsController() {
        baseTitle = message("FFmpegScreenRecorder");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            audioCheck.setSelected(UserConfig.getBoolean("FFmpegScreenRecorderAudio", true));
            audioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean("FFmpegScreenRecorderAudio", nv);
                }
            });

            videoCheck.setSelected(UserConfig.getBoolean("FFmpegScreenRecorderVideo", true));
            videoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean("FFmpegScreenRecorderVideo", nv);
                }
            });

            DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
            screenWidth = dm.getWidth();
            screenHeight = dm.getHeight();
            screenSizeLabel.setText(screenWidth + "x" + screenHeight);

            scopeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkScope();
                }
            });

            x = 0;
            xInput.setText(UserConfig.getString("FFmpegScreenRecorderX", "0"));
            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });

            y = 0;
            yInput.setText(UserConfig.getString("FFmpegScreenRecorderY", "0"));
            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });

            width = 0;
            widthInput.setText(UserConfig.getString("FFmpegScreenRecorderWidth", screenWidth + ""));
            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });

            height = 0;
            heightInput.setText(UserConfig.getString("FFmpegScreenRecorderHeight", screenHeight + ""));
            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });

            titleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkWindow();
                }
            });

            checkScope();

            delayController.permitInvalid(false).permitNotSet(true)
                    .init(baseName + "Delay", 0);

            durationController.permitInvalid(false).permitNotSet(true)
                    .init(baseName + "Duration", 5);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(tipsView, new Tooltip(message("FFmpegOptionsTips")
                    + "\n" + message("FFmpegScreenRecorderComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setParameters(BaseTaskController ffmpegController) {
        try {
            super.setParameters(ffmpegController);
            if (executable == null) {
                return;
            }
            videoDevices = new ArrayList<>();
            audioDevices = new ArrayList<>();
            os = SystemTools.os();
            switch (os) {
                case "win":
                    videoDevices.add(message("Screen"));
                    queryVideoDeviceButton.setDisable(false);
                    queryAudioDeviceButton.setDisable(false);
                    checkDevicesWin();
                    break;
                case "linux":
                    windowRadio.setDisable(true);
                    videoDevices.add(message("Screen"));
                    videoBox.getChildren().remove(windowBox);
                    audioDevices.add("alsa");
                    queryVideoDeviceButton.setDisable(true);
                    queryAudioDeviceButton.setDisable(true);
                    break;
                case "mac":
                    fullscreenRadio.setSelected(true);
                    videoBox.getChildren().removeAll(windowBox, rectBox);
                    queryVideoDeviceButton.setDisable(false);
                    queryAudioDeviceButton.setDisable(false);
                    checkDevicesMac();
                    break;
                default:
                    return;
            }

            ffmpegController.showLogs(message("VideoDevice") + ": " + videoDevices.toString());
            videoDeviceSelector.getItems().setAll(videoDevices);
            videoDeviceSelector.getSelectionModel().select(0);

            audioDeviceSelector.getItems().setAll(audioDevices);
            if (audioDevices.isEmpty()) {
                ffmpegController.showLogs(message("AudioDevice") + ": " + message("NotFound"));
                audioCheck.setDisable(true);
                audioCheck.setSelected(false);
            } else {
                ffmpegController.showLogs(message("AudioDevice") + ": " + audioDevices.toString());
                audioCheck.setDisable(false);
                audioCheck.setSelected(true);
                audioDeviceSelector.getSelectionModel().select(0);
            }

            checkThreadQueueSizes();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkDevicesWin() {
        try {
            String s = queryDevicesWin();
            if (s == null || s.isBlank()) {
                return;
            }
            String[] lines = s.split("\n");
            String name, prefix = "]  \"";
            int pos, prefixlen = prefix.length();
            boolean videoNext = false, audioNext = false;
            for (String line : lines) {
                ffmpegController.showLogs(line);
                if (line.endsWith("\" (audio)")) {
                    pos = line.indexOf(prefix);
                    if (pos < 0) {
                        continue;
                    }
                    name = line.substring(pos + prefixlen);
                    pos = name.indexOf("\"");
                    if (pos < 0) {
                        continue;
                    }
                    name = name.substring(0, pos);
                    audioDevices.add(name);
                } else if (line.endsWith("\" (video)")) {
                    pos = line.indexOf(prefix);
                    if (pos < 0) {
                        continue;
                    }
                    name = line.substring(pos + prefixlen);
                    pos = name.indexOf("\"");
                    if (pos < 0) {
                        continue;
                    }
                    name = name.substring(0, pos);
//                        videoDevices.add(name);
                } else if (line.contains("DirectShow video devices")) {
                    videoNext = true;
                    audioNext = false;
                } else if (line.contains("DirectShow audio devices")) {
                    videoNext = false;
                    audioNext = true;
                } else if (videoNext) {
                    pos = line.indexOf(prefix);
                    if (pos < 0) {
                        continue;
                    }
                    name = line.substring(pos + prefixlen);
                    pos = name.indexOf("\"");
                    if (pos < 0) {
                        continue;
                    }
                    name = name.substring(0, pos);
//                        videoDevices.add(name);
                } else if (audioNext) {
                    pos = line.indexOf(prefix);
                    if (pos < 0) {
                        continue;
                    }
                    name = line.substring(pos + prefixlen);
                    pos = name.indexOf("\"");
                    if (pos < 0) {
                        continue;
                    }
                    name = name.substring(0, pos);
                    audioDevices.add(name);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected String queryDevicesWin() {
        try {
            if (executable == null) {
                return null;
            }
            // ffmpeg -list_devices true -f dshow -i dummy
            List<String> command = new ArrayList<>();
            command.add(executable.getAbsolutePath());
            command.add("-hide_banner");
            command.add("-list_devices");
            command.add("true");
            command.add("-f");
            command.add("dshow");
            command.add("-i");
            command.add("dummy");
            showCmd(command);
            StringBuilder s = new StringBuilder();
            ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
            final Process process = pb.start();
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;

                while ((line = inReader.readLine()) != null) {
                    s.append(line).append("\n");
                }
            }
            process.waitFor();
            return s.toString();

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void checkDevicesMac() {
        try {
            if (executable == null) {
                return;
            }
            String s = queryDevicesMac();
            if (s == null || s.isBlank()) {
                return;
            }
            String[] lines = s.split("\n");
            boolean videoNext = false, audioNext = false;
            for (String line : lines) {
                ffmpegController.showLogs(line);
                if (line.contains("AVFoundation video devices")) {
                    videoNext = true;
                    audioNext = false;
                } else if (videoNext) {
                    int pos1 = line.indexOf("] [");
                    if (pos1 < 0) {
                        continue;
                    }
                    String name = line.substring(pos1 + 2);
                    videoDevices.add(name);
                } else if (line.contains("AVFoundation audio devices")) {
                    videoNext = false;
                    audioNext = true;
                } else if (audioNext) {
                    int pos1 = line.indexOf("] [");
                    if (pos1 < 0) {
                        continue;
                    }
                    String name = line.substring(pos1 + 2);
                    audioDevices.add(name);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected String queryDevicesMac() {
        try {
            if (executable == null) {
                return null;
            }
            // ffmpeg -f avfoundation -list_devices true -i ""
            List<String> command = new ArrayList<>();
            command.add(executable.getAbsolutePath());
            command.add("-hide_banner");
            command.add("-f");
            command.add("avfoundation");
            command.add("-list_devices");
            command.add("true");
            command.add("-i");
            command.add("\"\"");
            showCmd(command);
            StringBuilder s = new StringBuilder();
            ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
            final Process process = pb.start();
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    s.append(line).append("\n");
                }
            }
            process.waitFor();
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void checkThreadQueueSizes() {
        try {
            audioThreadQueueSize = 128;
            audioThreadQueueSizeInput.setText(UserConfig.getString("FFmpegAudioThreadQueueSize", "128"));
            audioThreadQueueSizeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            audioThreadQueueSize = v;
                            audioThreadQueueSizeInput.setStyle(null);
                            UserConfig.setString("FFmpegScreenRecorderAudio", newValue);

                        } else {
                            audioThreadQueueSizeInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        audioThreadQueueSizeInput.setStyle(UserConfig.badStyle());
                    }

                }
            });

            videoThreadQueueSize = 128;
            videoThreadQueueSizeInput.setText(UserConfig.getString("FFmpegVideoThreadQueueSize", "128"));
            videoThreadQueueSizeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            videoThreadQueueSize = v;
                            videoThreadQueueSizeInput.setStyle(null);
                            UserConfig.setString("FFmpegScreenRecorderAudio", newValue);

                        } else {
                            videoThreadQueueSizeInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        videoThreadQueueSizeInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkScope() {
        try {
            titleInput.setStyle(null);
            xInput.setStyle(null);
            yInput.setStyle(null);
            widthInput.setStyle(null);
            heightInput.setStyle(null);
            if (rectangleRadio.isSelected()) {
                checkRectangle();
            } else if (windowRadio.isSelected()) {
                checkWindow();
            } else if (fullscreenRadio.isSelected()) {

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkRectangle() {
        try {
            int v = Integer.parseInt(xInput.getText().trim());
            if (v >= 0 && v <= screenWidth) {
                x = v;
                xInput.setStyle(null);
                UserConfig.setString("FFmpegScreenRecorderX", v + "");
            } else {
                xInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            xInput.setStyle(UserConfig.badStyle());
        }
        try {
            int v = Integer.parseInt(yInput.getText().trim());
            if (v >= 0 && v <= screenWidth) {
                y = v;
                yInput.setStyle(null);
                UserConfig.setString("FFmpegScreenRecorderY", v + "");
            } else {
                yInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            yInput.setStyle(UserConfig.badStyle());
        }

        try {
            int v = Integer.parseInt(widthInput.getText().trim());
            if (v >= 0 && v <= screenWidth) {
                width = v;
                widthInput.setStyle(null);
                UserConfig.setString("FFmpegScreenRecorderWidth", v + "");
            } else {
                widthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            widthInput.setStyle(UserConfig.badStyle());
        }
        try {
            int v = Integer.parseInt(heightInput.getText().trim());
            if (v >= 0 && v <= screenHeight) {
                height = v;
                heightInput.setStyle(null);
                UserConfig.setString("FFmpegScreenRecorderHeight", v + "");
            } else {
                heightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            heightInput.setStyle(UserConfig.badStyle());
        }

    }

    protected void checkWindow() {
        if (titleInput.getText().trim().isBlank()) {
            titleInput.setStyle(UserConfig.badStyle());
        } else {
            titleInput.setStyle(null);
        }

    }

    @FXML
    @Override
    public void defaultAction() {
        super.defaultAction();
        fullscreenRadio.setSelected(true);
        audioThreadQueueSizeInput.setText("128");
        videoThreadQueueSizeInput.setText("128");
        delayController.select(5);
        durationController.select(-1);

    }

    @FXML
    public void queryDevice() {
        try {
            String s = null;
            switch (os) {
                case "win":
                    s = queryDevicesWin();
                    break;
                case "linux":
                    break;
                case "mac":
                    s = queryDevicesMac();
                    break;
            }
            if (s == null || s.isBlank()) {
                popError(message("Failed"));
            }
            TextPopController.loadText(s);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @Override
    protected boolean disableAudio() {
        return !audioCheck.isSelected() || disableAudio;
    }

    @Override
    protected boolean disableVideo() {
        return !videoCheck.isSelected() || disableVideo;
    }

    // http://trac.ffmpeg.org/wiki/Capture/Desktop
    @Override
    protected List<String> makeSpecialParameters(List<String> parameters) {
        switch (os) {
            case "win":
                if (!winParameters(parameters)) {
                    return null;
                }
                break;
            case "linux":
                if (!linuxParameters(parameters)) {
                    return null;
                }
                break;
            case "mac":
                if (!macParameters(parameters)) {
                    return null;
                }
                break;
            default:
                return null;
        }
        return parameters;
    }

    protected boolean winParameters(List<String> parameters) {
        try {
            if (!"win".equals(os) || parameters == null) {
                return false;
            }
            // ffmpeg  -f gdigrab  -thread_queue_size 128 -probesize 200M  -i desktop -f dshow  -thread_queue_size 128 -i audio="立体声混音 (Realtek High Definition Audio)" -vcodec libx264 -acodec aac out.mp4   -y
            if (disableVideo()) {
                parameters.add("-vn");
            } else {
                parameters.add("-f");
                parameters.add("gdigrab");
                // https://stackoverflow.com/questions/57903639/why-getting-and-how-to-fix-the-warning-error-on-ffmpeg-not-enough-frames-to-es
                parameters.add("-probesize");
                parameters.add("100M");
                parameters.add("-thread_queue_size");
                parameters.add(videoThreadQueueSize + "");

                if (rectangleRadio.isSelected() && width > 0 && height > 0) {
                    // -offset_x 10 -offset_y 20 -video_size 640x480 -show_region 1 -i desktop
                    parameters.add("-offset_x");
                    parameters.add(x + "");
                    parameters.add("-offset_y");
                    parameters.add(y + "");
                    parameters.add("-video_size");
                    parameters.add(width + "x" + height);
                    parameters.add("-show_region");
                    parameters.add("0");
                    parameters.add("-i");
                    parameters.add("desktop");

                } else if (windowRadio.isSelected()) {
                    parameters.add("-i");
                    parameters.add("title=" + titleInput.getText().trim());

                } else if (fullscreenRadio.isSelected()) {
                    parameters.add("-i");
                    parameters.add("desktop");
                } else {
                    return false;
                }
            }

            if (!audioCheck.isSelected() || audioDevices.isEmpty() || disableAudio) {
                parameters.add("-an");
            } else {
                parameters.add("-f");
                parameters.add("dshow");
                parameters.add("-thread_queue_size");
                parameters.add(audioThreadQueueSize + "");
                parameters.add("-i");
                parameters.add("audio=" + audioDeviceSelector.getValue());

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return true;
    }

    protected boolean linuxParameters(List<String> parameters) {
        try {
            if (!"linux".equals(os) || parameters == null) {
                return false;
            }
            // ffmpeg -video_size 1024x768 -framerate 25 -f x11grab -i :0.0+100,200 -f alsa -ac 2 -i hw:0 output.mkv
            if (!videoCheck.isSelected() || disableVideo) {
                parameters.add("-vn");
            } else {
                String offsets;
                if (rectangleRadio.isSelected() && width > 0 && height > 0) {
                    parameters.add("-video_size");
                    parameters.add(width + "x" + height);
                    offsets = x + "," + y;

                } else if (fullscreenRadio.isSelected()) {
                    parameters.add("-video_size");
                    parameters.add(screenWidth + "x" + screenHeight);
                    offsets = "0,0";
                } else {
                    return false;
                }

                parameters.add("-f");
                parameters.add("x11grab");
                // https://stackoverflow.com/questions/57903639/why-getting-and-how-to-fix-the-warning-error-on-ffmpeg-not-enough-frames-to-es
                parameters.add("-probesize");
                parameters.add("100M");
                parameters.add("-thread_queue_size");
                parameters.add(videoThreadQueueSize + "");
                parameters.add("-i");
                parameters.add(":0.0+" + offsets);
            }

            if (!audioCheck.isSelected() || disableAudio) {
                parameters.add("-an");
            } else {
                parameters.add("-f");
                parameters.add("alsa");
                parameters.add("-thread_queue_size");
                parameters.add(audioThreadQueueSize + "");
                parameters.add("-i");
                parameters.add("hw:0");
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return true;
    }

    protected int macDeviceIndex(String name) {
        try {
            if (name == null) {
                return -1;
            }
            int pos = name.indexOf("[");
            if (pos < 0) {
                return -1;
            }
            name = name.substring(pos + 1);
            pos = name.indexOf("]");
            if (pos < 0) {
                return -1;
            }
            name = name.substring(0, pos);
            return Integer.parseInt(name);
        } catch (Exception e) {
            return -1;
        }
    }

    protected boolean macParameters(List<String> parameters) {
        try {
            if (!"mac".equals(os) || parameters == null) {
                return false;
            }
            // ffmpeg -f avfoundation -i "<screen device index>:<audio device index>" -r 30 -s 3360x2100 -pix_fmt uyvy422 output.yuv
            parameters.add("-f");
            parameters.add("avfoundation");

            int v = macDeviceIndex(videoDeviceSelector.getValue());
            int a = macDeviceIndex(audioDeviceSelector.getValue());

            if (videoCheck.isSelected()) {
                if (v < 0) {
//                    popError(message("InvalidParameter") + ": " + message("VideoDevice"));
//                    return false;                    
                    v = 0;
                }
                parameters.add("-i");
                if (audioCheck.isSelected()) {
                    if (a < 0) {
//                        popError(message("InvalidParameter") + ": " + message("AudioDevice"));
//                        return false;
                        a = 0;
                    }
                    parameters.add(v + ":" + a);
                } else {
                    parameters.add(v + "");
                }
            } else {
                if (a < 0) {
//                    popError(message("InvalidParameter") + ": " + message("AudioDevice"));
//                    return false;
                    a = 0;
                }
                if (audioCheck.isSelected()) {
                    parameters.add(":" + a);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return true;
    }

}
