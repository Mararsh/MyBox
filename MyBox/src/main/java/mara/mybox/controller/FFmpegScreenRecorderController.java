package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-05-31
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderController extends DataTaskController {

    protected String os;
    protected Process recorder;
    protected SimpleBooleanProperty stopping;

    @FXML
    protected FFmpegScreenRecorderOptionsController optionsController;
    @FXML
    protected CheckBox openCheck;

    public FFmpegScreenRecorderController() {
        baseTitle = AppVariables.message("FFmpegScreenRecorder");
        cancelName = "Stop";

        SourceFileType = VisitHistory.FileType.Media;
        SourcePathType = VisitHistory.FileType.Media;
        TargetPathType = VisitHistory.FileType.Media;
        TargetFileType = VisitHistory.FileType.Media;
        AddFileType = VisitHistory.FileType.Media;
        AddPathType = VisitHistory.FileType.Media;

        targetPathKey = "MediaFilePath";
        sourcePathKey = "MediaFilePath";

        sourceExtensionFilter = CommonFxValues.FFmpegMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            stopping = new SimpleBooleanProperty(false);

            os = SystemTools.os();
//            switch (os) {
//                case "linux":
//                    videoDevice = "x11grab";
//                    break;
//                case "mac":
//                    videoDevice = "avfoundation";
//                    break;
//                case "win":
//                    videoDevice = "gdigrab";
//                    break;
//                default:
//                    videoDevice = null;
//            };

//            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
//                @Override
//                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                    AppVariables.setUserConfigValue("FFmpegScreenRecorderOpen", newValue);
//                }
//            });
//            openCheck.setSelected(AppVariables.getUserConfigBoolean("FFmpegScreenRecorderOpen", true));
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(targetFileInput.textProperty())
                            .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.executableInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.titleInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.xInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.yInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.widthInput.styleProperty().isEqualTo(badStyle))
                            .or(optionsController.heightInput.styleProperty().isEqualTo(badStyle))
                            .or(stopping)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void selectTargetFileFromPath(File path) {
        try {
            String name = optionsController.extensionInput.getText().trim();
            if (name.isBlank()) {
                name = null;
            } else {
                name = "." + name;
            }
            final File file = chooseSaveFile(path, name, targetExtensionFilter, true);
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        if (!optionsController.audioCheck.isSelected() && !optionsController.videoCheck.isSelected()) {
            popError(message("NothingHandled"));
            return false;
        }
        if (targetFile == null) {
            popError(message("InvalidParameters"));
            return false;
        }
        return true;
    }

    @Override
    protected boolean doTask() {
        if ("win".equals(os)) {
            return winTask();
        }
        return false;
    }

    protected boolean winTask() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            // http://trac.ffmpeg.org/wiki/Capture/Desktop
            // ffmpeg  -f gdigrab  -thread_queue_size 128 -probesize 200M  -i desktop -f dshow  -thread_queue_size 128 -i audio="立体声混音 (Realtek High Definition Audio)" -vcodec libx264 -acodec aac out.mp4   -y
            List<String> parameters = new ArrayList();
            parameters.add(optionsController.executable.getAbsolutePath());
            if ("win".equals(os)) {
                if (optionsController.videoCheck.isSelected()) {
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
                if (optionsController.audioCheck.isSelected() && optionsController.audioDevice != null) {
                    parameters.add("-f");
                    parameters.add("dshow");
                    parameters.add("-thread_queue_size");
                    parameters.add(optionsController.audioThreadQueueSize + "");
                    parameters.add("-i");
                    parameters.add("audio=" + optionsController.audioDevice);
                }
            }

            if (!optionsController.videoCheck.isSelected() || optionsController.disableVideo) {
                parameters.add("-vn");
            } else {
                if (optionsController.videoCodec != null) {
                    parameters.add("-vcodec");
                    parameters.add(optionsController.videoCodec);
                }
                if (optionsController.videoFrameRate > 0) {
                    parameters.add("-r");
                    parameters.add(optionsController.videoFrameRate + "");
                }
                if (optionsController.videoBitrate > 0) {
                    parameters.add("-b:v");
                    parameters.add(optionsController.videoBitrate + "k");
                }
                if (optionsController.aspect != null) {
                    parameters.add("-aspect");
                    parameters.add(optionsController.aspect);
                }
            }

            if (!optionsController.audioCheck.isSelected()
                    || optionsController.audioDevice == null
                    || optionsController.disbaleAudio) {
                parameters.add("-an");
            } else {
                if (optionsController.audioCodec != null) {
                    parameters.add("-acodec");
                    parameters.add(optionsController.audioCodec);
                }
                if (optionsController.audioBitrate > 0) {
                    parameters.add("-b:a");
                    parameters.add(optionsController.audioBitrate + "k");
                }
                if (optionsController.audioSampleRate > 0) {
                    parameters.add("-ar");
                    parameters.add(optionsController.audioSampleRate + "");
                }
                parameters.add("-ac");
                parameters.add(optionsController.stereoCheck.isSelected() ? "2" : "1");
                if (optionsController.volumn != null) {
                    parameters.add("-af");
                    parameters.add("volume=" + optionsController.volumn);
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
            ProcessBuilder pb = new ProcessBuilder(parameters)
                    .redirectErrorStream(true);
            recorder = pb.start();
            updateLogs("PID:" + recorder.pid(), true);
            if (optionsController.duration > 0) {
                updateLogs(message("Duarion") + ": " + optionsController.duration + " " + message("Seconds"), true);
            }

            try ( BufferedReader inReader = new BufferedReader(
                    new InputStreamReader(recorder.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    boolean recording = line.contains(" bitrate=");
                    if (verboseCheck.isSelected() || !recording) {
                        updateLogs(line + "\n", true);
                    }
                    if (recording && (timer == null) && (optionsController.duration > 0)) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                cancelAction();
                            }

                        }, optionsController.duration * 1000);
                    }
                }
                openTarget(null);
                Timer taskTimer = new Timer();
                taskTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        stopping.set(false);
                        recorder.destroy();
                        recorder = null;
                    }

                }, 5000);
            }
            recorder.waitFor();
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return true;
    }

    protected boolean linuxTask() {
        // To be implemented
        return false;
    }

    protected boolean macTask() {
        // To be implemented
        return false;
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
                logger.error(e.toString());
            }
        }
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {
        try {
            if (targetFile != null && targetFile.exists()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        browseURI(targetFile.getParentFile().toURI());
                        recordFileOpened(targetFile);

//                        if (optionsController.openCheck.isSelected()) {
//                            MediaPlayerController controller
//                                    = (MediaPlayerController) FxmlStage.openStage(CommonValues.MediaPlayerFxml);
//                            controller.load(targetFile.toURI());
//                        }
                    }
                });
            } else {
                popInformation(AppVariables.message("NoFileGenerated"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        cancelAction();
        return true;
    }

}
