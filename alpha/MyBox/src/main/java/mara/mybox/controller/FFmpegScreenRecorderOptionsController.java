package mara.mybox.controller;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-06-26
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderOptionsController extends ControlFFmpegOptions {

    protected Rectangle snapArea;
    protected int audioThreadQueueSize, videoThreadQueueSize,
            screenWidth, screenHeight, x, y, macVideo, macAudio;
    protected String os, audioDevice, vcodec, title;

    @FXML
    protected CheckBox audioCheck, videoCheck, miaoCheck;
    @FXML
    protected VBox videoBox;
    @FXML
    protected HBox fullScreenBox, windowBox, rectBox;
    @FXML
    protected Label audioComments, videoComments, screenSizeLabel, infoLabel;
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

    // http://trac.ffmpeg.org/wiki/Capture/Desktop
    // http://trac.ffmpeg.org/wiki/Encode/H.264
    // https://slhck.info/video/2017/02/24/crf-guide.html
    // https://slhck.info/video/2017/03/01/rate-control.html
    // https://www.cnblogs.com/sunny-li/p/9979796.html
    public FFmpegScreenRecorderOptionsController() {
        baseTitle = Languages.message("FFmpegScreenRecorder");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            audioCheck.setSelected(UserConfig.getBoolean("FFmpegScreenRecorderAudio", true));
            audioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("FFmpegScreenRecorderAudio", newValue);

                }
            });

            videoCheck.setSelected(UserConfig.getBoolean("FFmpegScreenRecorderVideo", true));
            videoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("FFmpegScreenRecorderVideo", newValue);

                }
            });

            os = SystemTools.os();
            switch (os) {
                case "win":
                    checkAudioDeviceWin();
                    break;
                case "linux":
                    windowRadio.setDisable(true);
                    videoBox.getChildren().remove(windowBox);
                    audioComments.setText("alsa");
                    break;
                case "mac":
                    fullscreenRadio.setSelected(true);
                    videoBox.getChildren().removeAll(windowBox, rectBox);
                    checkDevicesMac();
                    break;
                default:
                    return;
            }

            checkThreadQueueSizes();

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
                    .init(baseName + "Delay", 5);

            durationController.permitInvalid(false).permitNotSet(true)
                    .init(baseName + "Duration", -1);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(tipsView, new Tooltip(Languages.message("FFmpegOptionsTips")
                    + "\n" + Languages.message("FFmpegScreenRecorderComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkAudioDeviceWin() {
        try {
            if (executable == null) {
                return;
            }
            // ffmpeg -list_devices true -f dshow -i dummy
            ProcessBuilder pb = new ProcessBuilder(
                    executable.getAbsolutePath(),
                    "-list_devices", "true", "-f", "dshow", "-i", "dummy"
            ).redirectErrorStream(true);
            audioDevice = null;
            final Process process = pb.start();

            try ( BufferedReader inReader = process.inputReader(Charset.forName("UTF-8"))) {
                String line;
                boolean audioNext = false;
                while ((line = inReader.readLine()) != null) {
                    if (line.contains("DirectShow audio devices")) {
                        audioNext = true;
                    } else if (audioNext) {
                        int pos = line.indexOf("\"");
                        if (pos < 0) {
                            continue;
                        }
                        line = line.substring(pos + 1);
                        pos = line.indexOf("\"");
                        if (pos < 0) {
                            continue;
                        }
                        audioDevice = line.substring(0, pos);
                        audioComments.setText(Languages.message("AudioDevice") + ": " + audioDevice);
                        audioCheck.setDisable(false);
                        audioCheck.setSelected(true);
                        return;
                    }
                }
            }
            process.waitFor();
            if (audioDevice == null) {
                audioComments.setText(Languages.message("AudioDevice") + ": " + Languages.message("NotFound"));
                audioCheck.setDisable(true);
                audioCheck.setSelected(false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkDevicesMac() {
        try {
            if (executable == null) {
                return;
            }
            // ffmpeg -f avfoundation -list_devices true -i ""
            ProcessBuilder pb = new ProcessBuilder(
                    executable.getAbsolutePath(),
                    "-f", "avfoundation", "-list_devices", "true", "-i", "\"\""
            ).redirectErrorStream(true);
            macVideo = macAudio = -1;
            final Process process = pb.start();
            try ( BufferedReader inReader = process.inputReader(Charset.forName("UTF-8"))) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    try {
                        int pos1 = line.indexOf("] [");
                        if (pos1 < 0) {
                            continue;
                        }
                        String s = line.substring(pos1 + 3);
                        pos1 = s.indexOf("]");
                        int index = Integer.parseInt(s.substring(0, pos1));
                        String name = s.substring(pos1 + 1);
                        if (line.contains("Capture screen")) {
                            macVideo = index;
                            videoComments.setText(name);
                            videoCheck.setDisable(false);
                            videoCheck.setSelected(true);
                        } else if (line.contains("Microphone") || line.contains("麦克风")) {
                            macAudio = index;
                            audioComments.setText(Languages.message("AudioDevice") + ": " + name);
                            audioCheck.setDisable(false);
                            audioCheck.setSelected(true);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
            }
            process.waitFor();
            if (macVideo < 0) {
                videoComments.setText(Languages.message("NotFound"));
                videoCheck.setDisable(true);
                videoCheck.setSelected(false);
            }
            if (macAudio < 0) {
                audioComments.setText(Languages.message("AudioDevice") + ": " + Languages.message("NotFound"));
                audioCheck.setDisable(true);
                audioCheck.setSelected(false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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

}
