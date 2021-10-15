package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-05-31
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderController extends BaseTaskController {

    protected String os;
    protected Process recorder;
    protected SimpleBooleanProperty stopping;

    @FXML
    protected FFmpegScreenRecorderOptionsController optionsController;
    @FXML
    protected CheckBox openCheck;
    @FXML
    protected TextField commandInput;

    public FFmpegScreenRecorderController() {
        baseTitle = Languages.message("FFmpegScreenRecorder");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            stopping = new SimpleBooleanProperty(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Media);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            os = SystemTools.os();

            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Open", newValue);
                }
            });
            openCheck.setSelected(UserConfig.getBoolean(baseName + "Open", true));

            optionsController.extensionInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkExt();
                    });
            checkExt();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    targetFileController.valid.not()
                            .or(optionsController.executableInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.titleInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.widthInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(optionsController.heightInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(stopping)
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkExt() {
        String ext = optionsController.extensionInput.getText();
        if (ext == null || ext.isBlank() || Languages.message("OriginalFormat").equals(ext)) {
            return;
        }
        String v = targetFileController.text();
        if (v == null || v.isBlank()) {
            targetFileController.input(AppVariables.MyBoxDownloadsPath.getAbsolutePath() + File.separator + DateTools.nowFileString() + "." + ext);
        } else if (!v.endsWith("." + ext)) {
            targetFileController.input(FileNameTools.getFilePrefix(v) + "." + ext);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!optionsController.audioCheck.isSelected() && !optionsController.videoCheck.isSelected()) {
            popError(Languages.message("NothingHandled"));
            return false;
        }
        if (targetFile == null) {
            popError(Languages.message("InvalidParameters"));
            return false;
        }
        targetFile.getParentFile().mkdirs();
        return true;
    }

    @FXML
    @Override
    public void startAction() {
        if (!checkOptions()) {
            return;
        }
        if (startButton.getUserData() != null) {
            StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            cancelAction();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            initLogs();
            StyleTools.setNameIcon(startButton, Languages.message("Stop"), "iconStop.png");
            startButton.applyCss();
            startButton.setUserData("started");
            tabPane.getSelectionModel().select(logsTab);
            if (optionsController.delayController.value > 0) {
                updateLogs(Languages.message("Delay") + ": " + optionsController.delayController.value + " " + Languages.message("Seconds"));
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startTask();
                    }
                }, optionsController.delayController.value * 1000);
            } else {
                startTask();
            }
        }
    }

    @Override
    protected boolean doTask() {
        if (optionsController.miaoCheck.isSelected()) {
            SoundTools.BenWu();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        updateLogs(Languages.message("Started"));
        List<String> parameters = makeParameters();
        if (parameters == null) {
            return false;
        }
        return startRecorder(parameters);
    }

    protected List<String> makeParameters() {
        List<String> parameters = new ArrayList();
        // http://trac.ffmpeg.org/wiki/Capture/Desktop
        parameters.add(optionsController.executable.getAbsolutePath());
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
        if (!moreParameters(parameters)) {
            return null;
        }
        return parameters;
    }

    protected String makeCommand(List<String> parameters) {
        return parameters.toString().replaceAll("[\\[|,|\\]]", " ");
    }

    protected boolean winParameters(List<String> parameters) {
        try {
            if (!"win".equals(os) || parameters == null) {
                return false;
            }
            // ffmpeg  -f gdigrab  -thread_queue_size 128 -probesize 200M  -i desktop -f dshow  -thread_queue_size 128 -i audio="立体声混音 (Realtek High Definition Audio)" -vcodec libx264 -acodec aac out.mp4   -y
            if (!optionsController.videoCheck.isSelected() || optionsController.disableVideo) {
                parameters.add("-vn");
            } else {
                parameters.add("-f");
                parameters.add("gdigrab");
                // https://stackoverflow.com/questions/57903639/why-getting-and-how-to-fix-the-warning-error-on-ffmpeg-not-enough-frames-to-es
                parameters.add("-probesize");
                parameters.add("100M");
                parameters.add("-thread_queue_size");
                parameters.add(optionsController.videoThreadQueueSize + "");

                if (optionsController.rectangleRadio.isSelected()
                        && optionsController.width > 0 && optionsController.height > 0) {
                    // -offset_x 10 -offset_y 20 -video_size 640x480 -show_region 1 -i desktop
                    parameters.add("-offset_x");
                    parameters.add(optionsController.x + "");
                    parameters.add("-offset_y");
                    parameters.add(optionsController.y + "");
                    parameters.add("-video_size");
                    parameters.add(optionsController.width + "x" + optionsController.height);
                    parameters.add("-show_region");
                    parameters.add("0");
                    parameters.add("-i");
                    parameters.add("desktop");

                } else if (optionsController.windowRadio.isSelected()) {
                    parameters.add("-i");
                    parameters.add("title=" + optionsController.titleInput.getText().trim());

                } else if (optionsController.fullscreenRadio.isSelected()) {
                    parameters.add("-i");
                    parameters.add("desktop");
                } else {
                    return false;
                }

            }

            if (!optionsController.audioCheck.isSelected()
                    || optionsController.audioDevice == null
                    || optionsController.disbaleAudio) {
                parameters.add("-an");
            } else {
                parameters.add("-f");
                parameters.add("dshow");
                parameters.add("-thread_queue_size");
                parameters.add(optionsController.audioThreadQueueSize + "");
                parameters.add("-i");
                parameters.add("audio=" + optionsController.audioDevice);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return true;
    }

    protected boolean linuxParameters(List<String> parameters) {
        try {
            if (!"linux".equals(os) || parameters == null) {
                return false;
            }
            // ffmpeg -video_size 1024x768 -framerate 25 -f x11grab -i :0.0+100,200 -f alsa -ac 2 -i hw:0 output.mkv
            if (!optionsController.videoCheck.isSelected() || optionsController.disableVideo) {
                parameters.add("-vn");
            } else {
                String offsets;
                if (optionsController.rectangleRadio.isSelected()
                        && optionsController.width > 0 && optionsController.height > 0) {
                    parameters.add("-video_size");
                    parameters.add(optionsController.width + "x" + optionsController.height);
                    offsets = optionsController.x + "," + optionsController.y;

                } else if (optionsController.fullscreenRadio.isSelected()) {
                    parameters.add("-video_size");
                    parameters.add(optionsController.screenWidth + "x" + optionsController.screenHeight);
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
                parameters.add(optionsController.videoThreadQueueSize + "");
                parameters.add("-i");
                parameters.add(":0.0+" + offsets);
            }

            if (!optionsController.audioCheck.isSelected()
                    || optionsController.disbaleAudio) {
                parameters.add("-an");
            } else {
                parameters.add("-f");
                parameters.add("alsa");
                parameters.add("-thread_queue_size");
                parameters.add(optionsController.audioThreadQueueSize + "");
                parameters.add("-i");
                parameters.add("hw:0");
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return true;
    }

    protected boolean macParameters(List<String> parameters) {
        try {
            if (!"mac".equals(os) || parameters == null) {
                return false;
            }
            // ffmpeg -f avfoundation -i "<screen device index>:<audio device index>" -r 30 -s 3360x2100 -pix_fmt uyvy422 output.yuv
            parameters.add("-f");
            parameters.add("avfoundation");
            if (optionsController.videoCheck.isSelected()) {
                parameters.add("-i");
                if (optionsController.audioCheck.isSelected()) {
                    parameters.add(optionsController.macVideo + ":" + optionsController.macAudio);
                } else {
                    parameters.add(optionsController.macVideo + "");
                }
            } else {
                if (optionsController.audioCheck.isSelected()) {
                    parameters.add(":" + optionsController.macAudio);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return true;
    }

    protected boolean moreParameters(List<String> parameters) {
        try {
            if (parameters == null) {
                return false;
            }
            if (optionsController.audioCheck.isSelected() && !optionsController.disbaleAudio) {
                if (optionsController.audioCodec != null) {
                    parameters.add("-acodec");
                    parameters.add(optionsController.audioCodec);
                }
                if (optionsController.audioBitrate > 0) {
                    parameters.add("-b:a");
                    parameters.add(optionsController.audioBitrate + "k");
                } else {
                    parameters.add("-b:a");
                    parameters.add("192k");
                }
                if (optionsController.audioSampleRate > 0) {
                    parameters.add("-ar");
                    parameters.add(optionsController.audioSampleRate + "");
                } else {
                    parameters.add("-ar");
                    parameters.add("44100");
                }
                parameters.add("-ac");
                parameters.add(optionsController.stereoCheck.isSelected() ? "2" : "1");
                if (optionsController.volumn != null) {
                    parameters.add("-af");
                    parameters.add("volume=" + optionsController.volumn);
                }

            }

            if (optionsController.videoCheck.isSelected() && !optionsController.disableVideo) {
                if (optionsController.videoCodec != null) {
                    parameters.add("-vcodec");
                    parameters.add(optionsController.videoCodec);
                }
                if (optionsController.videoFrameRate > 0) {
                    parameters.add("-r");
                    parameters.add(optionsController.videoFrameRate + "");
                } else {
                    parameters.add("-r");
                    parameters.add("30");
                }
                if (optionsController.videoBitrate > 0) {
                    parameters.add("-b:v");
                    parameters.add(optionsController.videoBitrate + "k");
                } else {
                    parameters.add("-b:v");
                    parameters.add("5000k");
                }
                if (optionsController.aspect != null) {
                    parameters.add("-aspect");
                    parameters.add(optionsController.aspect);
                }
            }

            String more = optionsController.moreInput.getText().trim();
            if (!more.isBlank()) {
                String[] args = StringTools.splitBySpace(more);
                if (args != null && args.length > 0) {
                    parameters.addAll(Arrays.asList(args));
                }
            }
            parameters.add(targetFile.getAbsolutePath());
            parameters.add("-y");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return true;
    }

    protected boolean startRecorder(List<String> parameters) {
        try {
            if (parameters == null) {
                return false;
            }
            ProcessBuilder pb = new ProcessBuilder(parameters)
                    .redirectErrorStream(true);
            warmUp(pb);
            FileDeleteTools.delete(targetFile);
            recorder = pb.start();
            String cmd = makeCommand(parameters);
            updateLogs(cmd);
            updateLogs("PID:" + recorder.pid());
            if (optionsController.durationController.value > 0) {
                updateLogs(Languages.message("Duration") + ": " + optionsController.durationController.value + " " + Languages.message("Seconds"));
            }
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            boolean started = false, recording;
            try ( BufferedReader inReader = new BufferedReader(
                    new InputStreamReader(recorder.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                long startTime = new Date().getTime();
                while ((line = inReader.readLine()) != null) {
                    recording = line.contains(" bitrate=");
                    if (recording) {
                        started = true;
                        if ((timer == null) && (optionsController.durationController.value > 0)) {
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    cancelAction();
                                }
                            }, optionsController.durationController.value * 1000);
                        }
                    }
                    if (verboseCheck.isSelected() || !recording) {
                        updateLogs(line + "\n");
                    }
                    if (!started && (new Date().getTime() - startTime > 15000)) {  // terminal process if too long blocking
                        recorder.destroyForcibly();
                        break;
                    }
                }
            }
            recorder.waitFor();
            if (recorder != null) {
                recorder.destroy();
                recorder = null;
            }
            stopping.set(false);
            if (optionsController.miaoCheck.isSelected()) {
                SoundTools.miao7();
            }
            if (started) {
                openTarget(null);
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        updateLogs(Languages.message("FFmpegScreenRecorderAbnormal"));
                        alertError(Languages.message("FFmpegScreenRecorderAbnormal"));
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return true;
    }

    // Looks the generated media is always invalid when command runs for the first time.
    // So let's skip its first time...
    protected void warmUp(ProcessBuilder pb) {
        if (pb == null) {
            return;
        }
        try {
            recorder = pb.start();
            cancelAction();
            stopping.set(false);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    public void cancelAction() {
        if (recorder == null) {
            stopping.set(false);
            return;
        }
        if (stopping.get()) {
            return;
        }
        stopping.set(true);
        if (recorder != null) {
            try ( BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(recorder.getOutputStream(), Charset.forName("UTF-8")));) {
                writer.append('q');
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        }
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (targetFile != null && targetFile.exists()) {
                    recordFileOpened(targetFile);
                    if (openCheck.isSelected()) {
                        MediaPlayerController controller
                                = (MediaPlayerController) WindowTools.openStage(Fxmls.MediaPlayerFxml);
                        controller.load(targetFile);
                    } else {
                        browseURI(targetFile.getParentFile().toURI());
                    }
                } else {
                    popInformation(Languages.message("NoFileGenerated"));
                }
            }
        });
    }

    @Override
    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            cancelAction();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
