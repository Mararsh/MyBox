package mara.mybox.controller;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.fxml.FXML;
import javax.imageio.ImageIO;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-05-31
 * @License Apache License Version 2.0
 */
public class FFmpegScreenRecorderController extends DataTaskController {

    protected Process recorder;

    protected List<SnapTask> snapTasks;
    protected List<WriteTask> writeTasks;
    protected Rectangle snapArea;
    protected int framesPerSecond, delayPerFrame;
    protected ConcurrentLinkedQueue<Snap> snaps;

    @FXML
    protected FFmpegOptionsController ffmpegOptionsController;

    public FFmpegScreenRecorderController() {
        baseTitle = AppVariables.message("RecordImagesInSystemClipBoard");
        cancelName = "Stop";
    }

    // https://github.com/bahusvel/JavaScreenCapture
    @FXML
    @Override
    public void startAction() {
        try {
            if (message("Stop").equals(startButton.getText())) {
                cancelAction();
                return;
            }
            initLogs();
            startTime = new Date().getTime();
            startButton.setText(message("Stop"));
            tabPane.getSelectionModel().select(logsTab);

            framesPerSecond = 25;
            delayPerFrame = 1000 / framesPerSecond;
            snapArea = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            snaps = new ConcurrentLinkedQueue<>();
            snapTasks = new ArrayList();
            writeTasks = new ArrayList();
            for (int i = 0; i < framesPerSecond; i++) {
//                int index = i + 1;
                Timer taskTimer = new Timer();
                taskTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        WriteTask writeTask = new WriteTask();
                        writeTasks.add(writeTask);
                        writeTask.run();

                        SnapTask snapTask = new SnapTask();
                        snapTasks.add(snapTask);
                        snapTask.run();
//                        updateLogs(message("Task") + " " + index + " starting...", true);
                    }

                }, delayPerFrame * i);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public class Snap {

        private Date time;
        private BufferedImage image;

        public Snap(Date time, BufferedImage image) {
            this.time = time;
            this.image = image;
        }

        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
            this.time = time;
        }

        public BufferedImage getImage() {
            return image;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }

    }

    public class SnapTask implements Runnable {

        private Timer taskTimer;
        private Robot robot;

        public SnapTask() {
        }

        public void stop() {
            if (taskTimer != null) {
                taskTimer.cancel();
                taskTimer = null;
            }
        }

        @Override
        public void run() {
            try {
                robot = new Robot();
                if (taskTimer != null) {
                    taskTimer.cancel();
                }
                taskTimer = new Timer();
                taskTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        BufferedImage image = robot.createScreenCapture(snapArea);
                        snaps.add(new Snap(new Date(), image));
                    }

                }, 0, 1000);

            } catch (Exception e) {
                logger.debug(e.toString());
            }
        }
    }

    public class WriteTask implements Runnable {

        private Timer taskTimer;
        private boolean stopSnapping;

        public WriteTask() {
        }

        public void stop() {
            stopSnapping = true;
        }

        @Override
        public void run() {
            try {
                if (taskTimer != null) {
                    taskTimer.cancel();
                }
                stopSnapping = false;
                taskTimer = new Timer();
                taskTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Snap snap = snaps.poll();
                        if (snap != null) {
                            try {
                                File file = new File("D:\\tmp\\3\\snap" + "-" + DateTools.datetimeToString(snap.getTime()).replaceAll(":", "-") + ".png");
                                ImageIO.write(snap.getImage(), "png", file);
//                updateLogs("Task:" + taskIndex + " image:" + imageIndex + " file:" + file.getAbsolutePath(), true);
                            } catch (Exception e) {
                                logger.debug(e.toString());
                            }
                        } else if (stopSnapping) {
                            taskTimer.cancel();
                            taskTimer = null;
                        }
                    }

                }, 0, delayPerFrame);

            } catch (Exception e) {
                logger.debug(e.toString());
            }
        }
    }

    @Override
    public void cancelAction() {
        if (snapTasks != null) {
            snapTasks.forEach((t) -> {
                t.stop();
            });
        }
        if (writeTasks != null) {
            writeTasks.forEach((t) -> {
                t.stop();
            });
        }

        startButton.setText(message("Start"));
    }

//    @Override
    protected boolean doTask2() {
        if (ffmpegOptionsController.executable == null) {
            return false;
        }
        String audioDevice = queryAudioDevice();
        if (audioDevice != null) {
            startRecording(audioDevice);
        }
        return true;
    }

    // http://trac.ffmpeg.org/wiki/Capture/Desktop
    protected String queryAudioDevice() {
        try {
            // ffmpeg -list_devices true -f dshow -i dummy
            List<String> command = new ArrayList<>();
            command.add(ffmpegOptionsController.executable.getAbsolutePath());
            command.add("-list_devices");
            command.add("true");
            command.add("-f");
            command.add("dshow");
            command.add("-i");
            command.add("dummy");
            ProcessBuilder pb = new ProcessBuilder(command)
                    .redirectErrorStream(true);
            final Process process = pb.start();
            try ( BufferedReader inReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                boolean audioNext = false;
                while ((line = inReader.readLine()) != null) {
                    updateLogs(line + "\n", true);
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
                        return line.substring(0, pos);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
        }
        return null;
    }

    protected void startRecording(String audioDevice) {
        if (audioDevice == null) {
            return;
        }
        try {
            logger.debug(audioDevice);
            // ffmpeg -f gdigrab -i desktop -f dshow -i audio="立体声混音 (Realtek High Definition Audio)"  try.mp4   -y
            // ffmpeg -f gdigrab -i desktop -f dshow -i audio="xxx" -vcodec libx264    out.mp4   -y
            // chcp 65001
            List<String> command = new ArrayList<>();
//            command.add("chcp");
//            command.add("65001");
            command.add(ffmpegOptionsController.executable.getAbsolutePath());
//            command.add("-f");
//            command.add("gdigrab");
//            command.add("-i");
//            command.add("desktop");
            command.add("-f");
            command.add("dshow");
            command.add("-i");
            command.add("audio=" + audioDevice);
//            command.add("-vcodec");
//            command.add("libx264");
            command.add("D:\\tmp\\2\\try.mp4");
            command.add("-y");

            ProcessBuilder pb = new ProcessBuilder(command)
                    .redirectErrorStream(true);
            recorder = pb.start();
            updateLogs("PID:" + recorder.pid(), true);

            try ( BufferedReader inReader = new BufferedReader(
                    new InputStreamReader(recorder.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    updateLogs(line + "\n", true);
                    logger.debug(line);
                }
            }
            recorder.waitFor();
        } catch (Exception e) {
        }
    }

    protected void record(String audioDevice) {
        try {
            FFmpeg ffmpeg = FFmpeg.atPath(ffmpegOptionsController.executable.toPath().getParent())
                    .addOutput(UrlOutput.toPath(targetFile.toPath()))
                    .setOverwriteOutput(true);
            ffmpeg.addArguments("-f", "gdigrab");
            ffmpeg.addArguments("-i", "desktop");
            ffmpeg.addArguments("-f", "dshow");
            ffmpeg.addArguments("-i", "audio=" + audioDevice);
            ffmpeg.addArguments("-vcodec", "libx264");
//        ffmpeg.addArgument("D:\\tmp\\2\\try.mp4");
            ffmpeg.addArgument("-y");

            FFmpegResult result = ffmpeg.execute();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

//    @Override
    public void cancelAction2() {
        if (task != null) {
            task.cancel();
        }
        if (recorder != null) {

            recorder.destroyForcibly();

        }
    }

}
