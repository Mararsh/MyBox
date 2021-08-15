package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-17
 * @License Apache License Version 2.0
 */
public class ControlPlay extends BaseController {

    protected int interval, framesNumber, frameIndex, fromFrame, toFrame;
    protected double speed;
    protected long currentDelay;

    @FXML
    protected ComboBox<String> speedSelector, intervalSelector, frameSelector;
    @FXML
    protected Button pauseButton;
    @FXML
    protected CheckBox loopCheck, reverseCheck;
    @FXML
    protected Label totalLabel;

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
                            speedSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                        } else {
                            speed = v;
                            speedSelector.getEditor().setStyle(null);
                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(NodeStyleTools.badStyle);
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
                            intervalSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                        } else {
                            interval = v;
                            intervalSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "Interval", v);
                            if (parentController instanceof ImagesPlayController) {
                                ImagesPlayController imagesPlayController = (ImagesPlayController) parentController;
                                if (imagesPlayController.imageInfos != null) {
                                    for (ImageInformation info : imagesPlayController.imageInfos) {
                                        info.setDuration(interval);
                                    }
                                }
                            }

                        }
                    } catch (Exception e) {
                        speedSelector.getEditor().setStyle(NodeStyleTools.badStyle);
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
            frameIndex = 0;
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
    public synchronized void startFrame(int start) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            setPauseButton(false);
            if (framesNumber < 1) {
                return;
            }
            displayFrame(start);
            int index;
            long delay;
            if (reverseCheck.isSelected()) {
                index = frameIndex - 1;
                if (index < 0 && !loopCheck.isSelected()) {
                    setPauseButton(true);
                    return;
                }
            } else {
                index = frameIndex + 1;
                if (index >= framesNumber && !loopCheck.isSelected()) {
                    setPauseButton(true);
                    return;
                }
            }

            delay = currentDelay;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        startFrame(index);
                    });
                }
            }, delay);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // index is 0-based
    protected void displayFrame(int index) {
        try {
            if (framesNumber < 1) {
                return;
            }
            int end = toFrame;
            if (end < 0) {
                end = framesNumber - 1;
            }
            frameIndex = index;
            if (frameIndex > end) {
                frameIndex = fromFrame;
            }
            if (frameIndex < 0) {
                frameIndex = end;
            }
            isSettingValues = true;
            frameSelector.getSelectionModel().select((frameIndex + 1) + "");
            isSettingValues = false;
            if (this.parentController instanceof ImagesPlayController) {
                ImagesPlayController imagesPlayController = (ImagesPlayController) parentController;
                imagesPlayController.imageInformation = imagesPlayController.imageInfos.get(frameIndex);
                if (imagesPlayController.imageInformation != null) {
                    speed = speed <= 0 ? 1 : speed;
                    currentDelay = (int) (imagesPlayController.imageInformation.getDuration() / speed);
                } else {
                    currentDelay = (int) (interval / speed);
                }
                imagesPlayController.frameIndex = frameIndex;
                imagesPlayController.image = imagesPlayController.thumb(imagesPlayController.imageInformation);
                imagesPlayController.imageView.setImage(imagesPlayController.image);
                imagesPlayController.refinePane();
                imagesPlayController.updateLabelsTitle();
            }

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
                pauseFrame(frameIndex);

            } else if (pauseButton.getUserData().equals("Paused")) {
                startFrame(frameIndex);

            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void pauseFrame(int frame) {
        try {
            if (timer != null) {
                timer.cancel();
            }
            setPauseButton(true);
            displayFrame(frame);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void previousAction() {
        pauseFrame(--frameIndex);
    }

    @FXML
    @Override
    public void nextAction() {
        pauseFrame(++frameIndex);
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
        frameIndex = 0;
        fromFrame = 0;
        toFrame = -1;
    }

}
