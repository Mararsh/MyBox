package mara.mybox.controller;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-06-26
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderOptionsController extends FFmpegOptionsController {

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
        baseTitle = AppVariables.message("FFmpegScreenRecorder");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            FxmlControl.setTooltip(tipsView, new Tooltip(message("FFmpegOptionsTips")
                    + "\n" + message("FFmpegScreenRecorderComments")));

            audioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("FFmpegScreenRecorderAudio", newValue);
                }
            });
            audioCheck.setSelected(AppVariables.getUserConfigBoolean("FFmpegScreenRecorderAudio", true));

            videoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("FFmpegScreenRecorderVideo", newValue);
                }
            });
            videoCheck.setSelected(AppVariables.getUserConfigBoolean("FFmpegScreenRecorderVideo", true));

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
                    fullscreenRadio.fire();
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
            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });
            xInput.setText(AppVariables.getUserConfigValue("FFmpegScreenRecorderX", "0"));

            y = 0;
            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });
            yInput.setText(AppVariables.getUserConfigValue("FFmpegScreenRecorderY", "0"));

            width = 0;
            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });
            widthInput.setText(AppVariables.getUserConfigValue("FFmpegScreenRecorderWidth", screenWidth + ""));

            height = 0;
            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRectangle();
                }
            });
            heightInput.setText(AppVariables.getUserConfigValue("FFmpegScreenRecorderHeight", screenHeight + ""));

            titleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkWindow();
                }
            });

            checkScope();

            delayController.permitInvalid(false).permitNotSetting(true)
                    .init(baseName + "Delay", 5);
            delayController.changed.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        delayController.checked();
                    }
                }
            });

            durationController.permitInvalid(false).permitNotSetting(true)
                    .init(baseName + "Duration", -1);
            durationController.changed.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        durationController.checked();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkAudioDeviceWin() {
        try {
            // ffmpeg -list_devices true -f dshow -i dummy
            ProcessBuilder pb = new ProcessBuilder(
                    executable.getAbsolutePath(),
                    "-list_devices", "true", "-f", "dshow", "-i", "dummy"
            ).redirectErrorStream(true);
            audioDevice = null;
            final Process process = pb.start();
            try ( BufferedReader inReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")))) {
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
                        audioComments.setText(message("AudioDevice") + ": " + audioDevice);
                        audioCheck.setDisable(false);
                        return;
                    }
                }
            }
            process.waitFor();
            if (audioDevice == null) {
                audioComments.setText(message("AudioDevice") + ": " + message("NotFound"));
                audioCheck.setDisable(true);
                audioCheck.setSelected(false);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkDevicesMac() {
        try {
            // ffmpeg -f avfoundation -list_devices true -i ""
            ProcessBuilder pb = new ProcessBuilder(
                    executable.getAbsolutePath(),
                    "-f", "avfoundation", "-list_devices", "true", "-i", "\"\""
            ).redirectErrorStream(true);
            macVideo = macAudio = -1;
            final Process process = pb.start();
            try ( BufferedReader inReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")))) {
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
                        } else if (line.contains("Microphone") || line.contains("麦克风")) {
                            macAudio = index;
                            audioComments.setText(message("AudioDevice") + ": " + name);
                            audioCheck.setDisable(false);
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
            }
            process.waitFor();
            if (macVideo < 0) {
                videoComments.setText(message("NotFound"));
                videoCheck.setDisable(true);
                videoCheck.setSelected(false);
            }
            if (macAudio < 0) {
                audioComments.setText(message("AudioDevice") + ": " + message("NotFound"));
                audioCheck.setDisable(true);
                audioCheck.setSelected(false);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkThreadQueueSizes() {
        try {
            audioThreadQueueSize = 128;
            audioThreadQueueSizeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            audioThreadQueueSize = v;
                            audioThreadQueueSizeInput.setStyle(null);
                            AppVariables.setUserConfigValue("FFmpegScreenRecorderAudio", newValue);
                        } else {
                            audioThreadQueueSizeInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                        audioThreadQueueSizeInput.setStyle(badStyle);
                    }

                }
            });
            audioThreadQueueSizeInput.setText(AppVariables.getUserConfigValue("FFmpegAudioThreadQueueSize", "128"));

            videoThreadQueueSize = 128;
            videoThreadQueueSizeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            videoThreadQueueSize = v;
                            videoThreadQueueSizeInput.setStyle(null);
                            AppVariables.setUserConfigValue("FFmpegScreenRecorderAudio", newValue);
                        } else {
                            videoThreadQueueSizeInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                        videoThreadQueueSizeInput.setStyle(badStyle);
                    }
                }
            });
            videoThreadQueueSizeInput.setText(AppVariables.getUserConfigValue("FFmpegVideoThreadQueueSize", "128"));

        } catch (Exception e) {
            logger.error(e.toString());
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
            logger.error(e.toString());
        }
    }

    protected void checkRectangle() {
        try {
            int v = Integer.parseInt(xInput.getText().trim());
            if (v >= 0 && v <= screenWidth) {
                x = v;
                xInput.setStyle(null);
                AppVariables.setUserConfigValue("FFmpegScreenRecorderX", v + "");
            } else {
                xInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            xInput.setStyle(badStyle);
        }
        try {
            int v = Integer.parseInt(yInput.getText().trim());
            if (v >= 0 && v <= screenWidth) {
                y = v;
                yInput.setStyle(null);
                AppVariables.setUserConfigValue("FFmpegScreenRecorderY", v + "");
            } else {
                yInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            yInput.setStyle(badStyle);
        }

        try {
            int v = Integer.parseInt(widthInput.getText().trim());
            if (v >= 0 && v <= screenWidth) {
                width = v;
                widthInput.setStyle(null);
                AppVariables.setUserConfigValue("FFmpegScreenRecorderWidth", v + "");
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }
        try {
            int v = Integer.parseInt(heightInput.getText().trim());
            if (v >= 0 && v <= screenHeight) {
                height = v;
                heightInput.setStyle(null);
                AppVariables.setUserConfigValue("FFmpegScreenRecorderHeight", v + "");
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
        }
    }

    protected void checkWindow() {
        if (titleInput.getText().trim().isBlank()) {
            titleInput.setStyle(badStyle);
        } else {
            titleInput.setStyle(null);
        }
    }

    @FXML
    @Override
    public void defaultAction() {
        super.defaultAction();
        fullscreenRadio.fire();
        audioThreadQueueSizeInput.setText("128");
        videoThreadQueueSizeInput.setText("128");
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
                if (item.contains("libx264rgb")) {
                    videoEncoderSelector.getSelectionModel().select(item);
                    x264presetSelector.getSelectionModel().select("ultrafast  " + message("Ultrafast"));
//                    crfSelector.getSelectionModel().select("18");
                    break;
                }
            }
        }
        delayController.select(5);
        durationController.select(-1);
    }

}
