package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-05-31
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderController extends BaseTaskController {

    protected String os;
    protected Process process;
    protected SimpleBooleanProperty stopping;

    @FXML
    protected FFmpegScreenRecorderOptionsController optionsController;
    @FXML
    protected TextField commandInput;
    @FXML
    protected CheckBox miaoRecordCheck;

    public FFmpegScreenRecorderController() {
        baseTitle = message("FFmpegScreenRecorder");
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

            optionsController.setParameters(this);

            miaoRecordCheck.setSelected(UserConfig.getBoolean("FFmpegScreenRecorderMiao", true));
            miaoRecordCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean("FFmpegScreenRecorderMiao", nv);
                }
            });

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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkExt() {
        String ext = optionsController.extensionInput.getText();
        if (ext == null || ext.isBlank() || message("OriginalFormat").equals(ext)) {
            return;
        }
        String v = targetFileController.text();
        if (v == null || v.isBlank()) {
            targetFileController.input(AppPaths.getGeneratedPath() + File.separator + DateTools.nowFileString() + "." + ext);
        } else if (!v.endsWith("." + ext)) {
            targetFileController.input(FileNameTools.replaceSuffix(v, ext));
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!optionsController.audioCheck.isSelected() && !optionsController.videoCheck.isSelected()) {
                popError(message("NothingHandled"));
                return false;
            }
            return pickTarget();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean pickTarget() {
        try {
            targetFile = targetFileController.file();
            if (targetFile == null) {
                popError(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            targetPath = targetFile.getParentFile();
            targetPath.mkdirs();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void startTask() {
        long delay = optionsController.delayController.value;
        if (delay > 0) {
            showLogs(message("Delay") + ": " + delay + " " + message("Seconds"));
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    superStartTask();
                }
            }, delay * 1000);
        } else {
            superStartTask();
        }
    }

    public void superStartTask() {
        super.startTask();
    }

    @Override
    public boolean doTask() {
        try {
            if (miaoRecordCheck.isSelected()) {
                SoundTools.BenWu();
            }
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            List<String> parameters = optionsController.makeParameters(null);
            process = optionsController.startProcess(this, parameters, targetFile);
            if (process == null) {
                cancelTask();
                return false;
            }
            showLogs(message("Started"));
            if (optionsController.durationController.value > 0) {
                showLogs(message("Duration") + ": "
                        + optionsController.durationController.value + " " + message("Seconds"));
            }
            boolean started = false, recording;
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;
                long start = new Date().getTime();
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
                    if (!started && (new Date().getTime() - start > 15000)) {  // terminal process if too long blocking
                        process.destroyForcibly();
                        break;
                    }
                }
            }
            process.waitFor();
            if (process != null) {
                process.destroy();
                process = null;
            }
            stopping.set(false);
            if (miaoRecordCheck.isSelected()) {
                SoundTools.miao7();
            }
            showLogs(message("Exit"));
            if (started) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (targetFile != null && targetFile.exists()) {
                            recordFileWritten(targetFile);
                            if (openCheck.isSelected()) {
                                ControllerTools.openTarget(targetFile.getAbsolutePath());
                            } else {
                                browseURI(targetFile.getParentFile().toURI());
                            }
                        } else {
                            popInformation(message("NoFileGenerated"));
                        }
                    }
                });
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showLogs(message("FFmpegScreenRecorderAbnormal"));
                        alertError(message("FFmpegScreenRecorderAbnormal"));
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return true;
    }

    @Override
    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        cancelAction();
    }

    @Override
    public void cancelAction() {
        if (process == null) {
            stopping.set(false);
            return;
        }
        if (stopping.get()) {
            return;
        }
        stopping.set(true);
        if (process != null) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), Charset.forName("UTF-8")));) {
                writer.append('q');
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        }
    }

    @Override
    public void cleanPane() {
        try {
            cancelTask();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
