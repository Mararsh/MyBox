package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.ScheduleTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-17
 * @License Apache License Version 2.0
 */
public class ControlPlay extends BaseController {

    protected int framesNumber, currentIndex, fromFrame, toFrame;
    protected long timeValue;
    protected SimpleBooleanProperty stopped;
    protected Thread playThread, targetThread;
    protected ScheduledFuture schedule;
    protected boolean snapping;
    protected Node snapNode;
    protected double snapScale;
    protected LoadingController loadingController;
    protected SnapshotParameters snapParameters;
    protected List<File> snaps;

    @FXML
    protected ToggleGroup fixGroup;
    @FXML
    protected RadioButton delayRadio, intervalRadio;
    @FXML
    protected ComboBox<String> timeSelector, frameSelector;
    @FXML
    protected Button pauseButton;
    @FXML
    protected CheckBox loopCheck, reverseCheck;
    @FXML
    protected Label totalLabel;
    @FXML
    protected VBox snapBox;
    @FXML
    protected ColorSet colorController;

    public ControlPlay() {
        stopped = new SimpleBooleanProperty();
    }

    public void setParameters(BaseController parent, Thread targetThread, Node snapNode) {
        try {
            this.parentController = parent;
            this.targetThread = targetThread;
            this.snapNode = snapNode;
            this.baseName = parent.baseName;
            clear();

            if (snapNode == null) {
                snapBox.setVisible(false);
            }

            frameSelector.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    pauseFrame(newValue.intValue());
                }
            });

            fixGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (!isSettingValues) {
                        startFrame(currentIndex);
                    }
                }
            });

            timeSelector.getItems().addAll(Arrays.asList(
                    "500", "200", "100", "1000", "50", "2000", "300", "3000", "20", "10",
                    "5", "2", "1", "6000", "30000", "12000", "60000"
            ));
            timeValue = UserConfig.getInt(baseName + "Interval", 500);
            if (timeValue <= 0) {
                timeValue = 500;
            }
            timeSelector.setValue(timeValue + "");
            timeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v <= 0) {
                            timeSelector.getEditor().setStyle(UserConfig.badStyle());
                        } else {
                            timeValue = v;
                            timeSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "Interval", v);
                            if (!isSettingValues) {
                                startFrame(currentIndex);
                            }
                        }
                    } catch (Exception e) {
                        timeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            loopCheck.setSelected(UserConfig.getBoolean(baseName + "Loop", true));
            loopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Loop", loopCheck.isSelected());
                }
            });

            reverseCheck.setSelected(UserConfig.getBoolean(baseName + "Reverse", false));
            reverseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Reverse", reverseCheck.isSelected());
                }
            });

            colorController.init(this, baseName + "SnapColor", Color.WHITE);

            setPauseButton(false);

            playThread = new Thread() {
                @Override
                public void run() {
                    displayFrameTask(currentIndex);
                }
            };

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // from, to, frameIndex are 0-based. Include to.
    // Displayed values are 1-based while internal values are 0-based
    public boolean play(List<String> frames, int from, int to) {
        try {
            if (frames == null || frames.isEmpty()) {
                return false;
            }
            clear();
            int s = frames.size();
            if (s < 1) {
                return false;
            }
            int f = from;
            int t = to;
            if (f < 0 || f >= s) {
                f = 0;
            }
            if (t < 0 || t >= s) {
                t = s - 1;
            }
            if (f > t) {
                return false;
            }
            stopped.set(false);
            isSettingValues = true;
            fromFrame = f;
            toFrame = t;
            currentIndex = 0;
            framesNumber = frames.size();
            if (reverseCheck.isSelected()) {
                currentIndex = toFrame;
            } else {
                currentIndex = fromFrame;
            }
            setPauseButton(false);
            List<String> names = new ArrayList<>();
            for (String frame : frames) {
                String v = frame.replaceAll("\n", " ");
                int len = v.length();
                if (len > 100) {
                    v = v.substring(0, 100);
                }
                names.add(v);
            }
            frameSelector.getItems().setAll(names);
            frameSelector.getSelectionModel().select(currentIndex);
            totalLabel.setText("/" + framesNumber);
            isSettingValues = false;
            startFrame(currentIndex);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean play(int total, int from, int to) {
        try {
            if (total < 1) {
                return false;
            }
            int f = from;
            int t = to;
            if (f < 0 || f >= total) {
                f = 0;
            }
            if (t < 0 || t >= total) {
                t = total - 1;
            }
            if (f > t) {
                return false;
            }
            List<String> frames = new ArrayList<>();
            for (int i = f + 1; i <= t + 1; ++i) {
                frames.add(i + "");
            }
            return play(frames, f, t);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean play(List<String> frames) {
        try {
            if (frames == null || frames.isEmpty()) {
                return false;
            }
            int size = frames.size();
            if (size == 0) {
                return false;
            }
            List<String> names = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                names.add((i + 1) + "  " + frames.get(i));
            }
            return play(names, 0, size - 1);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected void startFrame(int index) {
        try {
            if (schedule != null) {
                schedule.cancel(true);
            }
            if (playThread != null) {
                playThread.interrupt();
            }
            if (targetThread != null) {
                targetThread.interrupt();
            }
            if (!checkIndex(index)) {
                return;
            }
            if (delayRadio.isSelected()) {
                schedule = ScheduleTools.service.scheduleWithFixedDelay(playThread,
                        0, timeValue, TimeUnit.MILLISECONDS);

            } else {
                schedule = ScheduleTools.service.scheduleAtFixedRate(playThread,
                        0, timeValue, TimeUnit.MILLISECONDS);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void displayFrameTask(int index) {
        try {
            if (!checkIndex(index)) {
                return;
            }
            if (targetThread != null) {
                targetThread.interrupt();
            }
            targetThread.run();

            if (snapNode != null && snapping) {
                Platform.runLater(() -> {
                    snap();
                });
                synchronized (snapNode) {
                    snapNode.wait();
                }
            }
            if (stopped.get()) {
                pause();
            } else {
                int next = nextIndex();
                if (next >= 0) {
                    currentIndex = next;
                } else {
                    pause();
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void snap() {
        try {
            if (loadingController != null) {
                loadingController.setInfo(message("Snapshot") + ": " + frameSelector.getValue());
            }
            Image snapshot = snapNode.snapshot(snapParameters, null);
            File tmpfile = TmpFileTools.getTempFile(".png");
            ImageFileWriters.writeImageFile(SwingFXUtils.fromFXImage(snapshot, null), "png", tmpfile.getAbsolutePath());
            snaps.add(tmpfile);
            synchronized (snapNode) {
                snapNode.notifyAll();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean checkIndex(int index) {
        try {
            if (framesNumber < 1) {
                return false;
            }
            currentIndex = correctIndex(index);
            if (currentIndex < 0) {
                MyBoxLog.console(currentIndex);
                pause();
                return false;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    isSettingValues = true;
                    frameSelector.getSelectionModel().select(currentIndex);
                    isSettingValues = false;
                }
            });
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public int correctIndex(int index) {
        if (framesNumber < 1) {
            return -1;
        }
        int end = toFrame;
        if (end < 0 || end >= framesNumber) {
            end = framesNumber - 1;
        }
        int start = fromFrame;
        if (start < 0 || start >= framesNumber) {
            start = 0;
        }
        if (index > end) {
            return start;
        }
        if (index < start) {
            return end;
        }
        return index;
    }

    public int nextIndex() {
        int index;
        if (reverseCheck.isSelected()) {
            index = currentIndex - 1;
            if (index < fromFrame && (snapping || !loopCheck.isSelected())) {
                return -1;
            }
        } else {
            index = currentIndex + 1;
            if (index > toFrame && (snapping || !loopCheck.isSelected())) {
                return -1;
            }
        }
        return correctIndex(index);
    }

    protected void setPauseButton(boolean setAsPaused) {
        if (setAsPaused) {
            StyleTools.setNameIcon(pauseButton, message("Play"), "iconPlay.png");
            previousButton.setDisable(false);
            nextButton.setDisable(false);
        } else {
            StyleTools.setNameIcon(pauseButton, message("Pause"), "iconPause.png");
            previousButton.setDisable(true);
            nextButton.setDisable(true);
        }
        pauseButton.applyCss();
    }

    @FXML
    public void pauseAction() {
        try {
            if (stopped.get()) {
                stopped.set(false);
                startFrame(currentIndex);
            } else {
                pause();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pauseFrame(int frame) {
        try {
            pause();
            displayFrameTask(frame);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pause() {
        try {
            stopped.set(true);
            if (snapNode != null) {
                synchronized (snapNode) {
                    snapNode.notifyAll();
                }
            }
            if (snapping) {
                outSnaps();
            }
            snapping = false;
            if (schedule != null) {
                schedule.cancel(true);
            }
            schedule = null;
            if (playThread != null) {
                playThread.interrupt();
            }
            if (targetThread != null) {
                targetThread.interrupt();
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setPauseButton(true);
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void previousAction() {
        pauseFrame(--currentIndex);
    }

    @FXML
    @Override
    public void nextAction() {
        pauseFrame(++currentIndex);
    }

    @FXML
    @Override
    public void firstAction() {
        pauseFrame(fromFrame);
    }

    @FXML
    @Override
    public void lastAction() {
        pauseFrame(toFrame);
    }

    public void clear() {
        pause();
        framesNumber = 0;
        currentIndex = 0;
        fromFrame = 0;
        toFrame = -1;
        isSettingValues = true;
        frameSelector.getItems().clear();
        totalLabel.setText("");
        isSettingValues = false;
    }

    @FXML
    public void snapAction() {
        if (snapNode == null) {
            return;
        }
        pause();
        if (loadingController != null) {
            loadingController.closeStage();
            loadingController = null;
        }
        loadingController = parentController.handling();
        loadingController.canceled.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pause();
                }
            }
        });
        stopped.set(false);
        snapping = true;
        snaps = new ArrayList<>();
        snapScale = NodeTools.dpiScale(dpi);
        snapParameters = new SnapshotParameters();
        snapParameters.setFill(colorController.color());
        snapParameters.setTransform(javafx.scene.transform.Transform.scale(snapScale, snapScale));
        if (reverseCheck.isSelected()) {
            currentIndex = framesNumber - 1;
        } else {
            currentIndex = 0;
        }
        schedule = ScheduleTools.service.scheduleWithFixedDelay(playThread,
                0, 200, TimeUnit.MILLISECONDS);
    }

    public void outSnaps() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (loadingController != null) {
                    loadingController.closeStage();
                    loadingController = null;
                }
                if (snaps != null && !snaps.isEmpty()) {
                    ImagesEditorController.open(snaps);
                }
            }
        });
    }

    @Override
    public void cleanPane() {
        try {
            clear();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
