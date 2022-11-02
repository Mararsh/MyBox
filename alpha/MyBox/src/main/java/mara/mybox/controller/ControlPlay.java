package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.ScheduleTools;
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
    protected SimpleBooleanProperty stopNodify, timeNodify;
    protected boolean stopped;
    protected Thread playThread, targetThread;
    protected ScheduledFuture schedule;

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

    public ControlPlay() {
        stopNodify = new SimpleBooleanProperty();
        timeNodify = new SimpleBooleanProperty();
    }

    public void setParameters(BaseController parent, Thread targetThread) {
        try {
            this.parentController = parent;
            this.targetThread = targetThread;
            this.baseName = parent.baseName;
            clear();

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
                                timeNodify.set(!timeNodify.get());
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
            fromFrame = f;
            toFrame = t;
            currentIndex = 0;
            framesNumber = frames.size();
            stopped = false;
            if (reverseCheck.isSelected()) {
                currentIndex = toFrame;
            } else {
                currentIndex = fromFrame;
            }
            setPauseButton(false);
            isSettingValues = true;
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

            if (stopped) {
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

    protected boolean checkIndex(int index) {
        try {
            if (framesNumber < 1) {
                return false;
            }
            currentIndex = correctIndex(index);
            if (currentIndex < 0) {
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
            if (index < fromFrame && !loopCheck.isSelected()) {
                return -1;
            }
        } else {
            index = currentIndex + 1;
            if (index > toFrame && !loopCheck.isSelected()) {
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
            if (stopNodify != null) {
                stopNodify.set(!stopNodify.get());
            }
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
            if (stopped) {
                stopped = false;
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
            stopped = true;
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
        stopNodify.set(!stopNodify.get());
    }

    @Override
    public void cleanPane() {
        try {
            clear();
            stopNodify = null;
            timeNodify = null;

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
