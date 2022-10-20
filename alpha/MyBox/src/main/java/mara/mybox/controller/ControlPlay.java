package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-17
 * @License Apache License Version 2.0
 */
public class ControlPlay extends BaseController {

    protected int interval, framesNumber, currentIndex, fromFrame, toFrame;
    protected double speed;
    protected long currentDelay;
    protected SimpleBooleanProperty frameNodify, stopNodify, intervalNodify;

    @FXML
    protected ComboBox<String> speedSelector, intervalSelector, frameSelector;
    @FXML
    protected Button pauseButton;
    @FXML
    protected CheckBox loopCheck, reverseCheck;
    @FXML
    protected Label totalLabel;

    public ControlPlay() {
        frameNodify = new SimpleBooleanProperty();
        stopNodify = new SimpleBooleanProperty();
        intervalNodify = new SimpleBooleanProperty();
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            this.baseName = parent.baseName;
            clear();

            frameSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        pauseFrame(v - 1);
                    } catch (Exception e) {
                    }
                }
            });

            speed = 1.0;
            speedSelector.getItems().addAll(Arrays.asList(
                    "1", "1.5", "2", "0.5", "0.8", "1.2", "0.3", "3", "0.1", "5", "0.2", "8"
            ));
            speedSelector.setValue("1");
            speedSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.valueOf(newValue);
                        if (v <= 0) {
                            speedSelector.getEditor().setStyle(UserConfig.badStyle());
                        } else {
                            speed = v;
                            speedSelector.getEditor().setStyle(null);
                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            intervalSelector.getItems().addAll(Arrays.asList(
                    "500", "200", "100", "1000", "50", "2000", "300", "3000", "20", "10", "6000", "30000", "12000", "60000"
            ));
            interval = UserConfig.getInt(baseName + "Interval", 500);
            if (interval <= 0) {
                interval = 500;
            }
            intervalSelector.setValue(interval + "");
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v <= 0) {
                            intervalSelector.getEditor().setStyle(UserConfig.badStyle());
                        } else {
                            interval = v;
                            intervalSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "Interval", v);
                            intervalNodify.set(!intervalNodify.get());
                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(UserConfig.badStyle());
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // from, to, frameIndex are 0-based. Include to.
    // Displayed values are 1-based while internal values are 0-based
    public synchronized boolean play(int total, int from, int to) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            currentIndex = 0;
            frameSelector.getItems().clear();
            totalLabel.setText("");
            framesNumber = total;
            if (framesNumber < 1) {
                return false;
            }
            fromFrame = from;
            toFrame = to;
            List<String> frames = new ArrayList<>();
            if (fromFrame < 0 || fromFrame >= framesNumber) {
                fromFrame = 0;
            }
            if (toFrame < 0 || toFrame >= framesNumber) {
                toFrame = framesNumber - 1;
            }
            if (fromFrame > toFrame) {
                return false;
            }
            isSettingValues = true;
            for (int i = fromFrame + 1; i <= toFrame + 1; ++i) {
                frames.add(i + "");
            }
            frameSelector.getItems().addAll(frames);
            totalLabel.setText("/" + total);
            if (reverseCheck.isSelected()) {
                frameSelector.setValue((toFrame + 1) + "");
                isSettingValues = false;
                startFrame(toFrame);
            } else {
                frameSelector.setValue((fromFrame + 1) + "");
                isSettingValues = false;
                startFrame(fromFrame);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    // start is 0-based
    public synchronized void startFrame(int startIndex) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            setPauseButton(false);
            if (framesNumber < 1) {
                return;
            }
            currentDelay = (long) (interval / speed);
            displayFrame(startIndex);
            int nextIndex = nextIndex();
            if (nextIndex < 0) {
                setPauseButton(true);
                return;
            }
            if (currentDelay <= 0) {
                currentDelay = 200;
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        startFrame(nextIndex);
                    });
                }
            }, currentDelay);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    public synchronized int nextIndex() {
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

    // index is 0-based
    protected void displayFrame(int index) {
        try {
            currentIndex = correctIndex(index);
            if (currentIndex < 0) {
                setPauseButton(true);
                return;
            }
            isSettingValues = true;
            frameSelector.getSelectionModel().select((currentIndex + 1) + "");
            isSettingValues = false;
            speed = speed <= 0 ? 1 : speed;
            frameNodify.set(!frameNodify.get());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setPauseButton(boolean setAsPaused) {
        if (setAsPaused) {
            StyleTools.setNameIcon(pauseButton, Languages.message("Play"), "iconPlay.png");
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pauseButton.setUserData("Paused");
            stopNodify.set(!stopNodify.get());
        } else {
            StyleTools.setNameIcon(pauseButton, Languages.message("Pause"), "iconPause.png");
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            pauseButton.setUserData("Playing");
        }
        pauseButton.applyCss();
    }

    @FXML
    public void pauseAction() {
        try {
            if (pauseButton.getUserData().equals("Playing")) {
                pauseFrame(currentIndex);

            } else if (pauseButton.getUserData().equals("Paused")) {
                startFrame(currentIndex);

            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pauseFrame(int frame) {
        try {
            pause();
            displayFrame(frame);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pause() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            setPauseButton(true);
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
        if (timer != null) {
            timer.cancel();
        }
        if (task != null && !task.isQuit()) {
            task.cancel();
        }
        framesNumber = 0;
        currentIndex = 0;
        fromFrame = 0;
        toFrame = -1;
        stopNodify.set(!stopNodify.get());
    }

    @Override
    public void cleanPane() {
        try {
            clear();
            frameNodify = null;
            stopNodify = null;
            intervalNodify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
