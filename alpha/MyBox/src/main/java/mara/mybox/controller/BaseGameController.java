package mara.mybox.controller;

import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-30
 * @License Apache License Version 2.0
 */
public class BaseGameController extends BaseWebViewController {

    protected Random random;

    protected File soundFile;

    @FXML
    protected Tab playTab, settingsTab;
    @FXML
    protected Label scoreLabel, soundFileLabel;
    @FXML
    protected RadioButton guaiRadio, benRadio, guaiBenRadio, muteRadio, customizedSoundRadio;
    @FXML
    protected HBox selectSoundBox;
    @FXML
    protected CheckBox scoreCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            random = new Random();

            if (scoreCheck != null) {
                scoreCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                        UserConfig.setBoolean(baseName + "PopScores", scoreCheck.isSelected());
                    }
                });
                scoreCheck.setSelected(UserConfig.getBoolean(baseName + "PopScores", true));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void initSound() {
        try {
            if (guaiRadio == null) {
                return;
            }
            isSettingValues = true;

            String sound = UserConfig.getString(baseName + "Sound", "Guai");
            switch (sound) {
                case "Ben":
                    benRadio.setSelected(true);
                    break;
                case "GuaiBen":
                    guaiBenRadio.setSelected(true);
                    break;
                case "Mute":
                    muteRadio.setSelected(true);
                    break;
                case "Customized":
                    String sfile = UserConfig.getString("GameEliminationSoundFile", null);
                    if (sfile != null) {
                        soundFile = new File(sfile);
                        if (soundFile.exists()) {
                            soundFileLabel.setText(soundFile.getAbsolutePath());
                            customizedSoundRadio.setSelected(true);
                        } else {
                            soundFile = null;
                            guaiRadio.setSelected(true);
                            soundFileLabel.setText(null);
                        }
                    }
                    break;
                default:
                    guaiRadio.setSelected(true);
                    break;
            }

            guaiRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    if (!isSettingValues && newVal) {
                        UserConfig.setString(baseName + "Sound", "Guai");
                    }
                }
            });
            benRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (!isSettingValues && newVal) {
                        UserConfig.setString(baseName + "Sound", "Ben");
                    }
                }
            });
            guaiBenRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (!isSettingValues && newVal) {
                        UserConfig.setString(baseName + "Sound", "GuaiBen");
                    }
                }
            });
            muteRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (!isSettingValues && newVal) {
                        UserConfig.setString(baseName + "Sound", "Mute");
                    }
                }
            });
            customizedSoundRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal,
                        Boolean newVal) {
                    if (!isSettingValues && newVal) {
                        UserConfig.setString(baseName + "Sound", "Customized");
                    }
                }
            });

            selectSoundBox.disableProperty().bind(customizedSoundRadio.selectedProperty().not());

            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void playSound(boolean good) {
        if (guaiRadio == null) {
            return;
        }
        if (guaiRadio.isSelected()) {
            SoundTools.GuaiAO();
        } else if (benRadio.isSelected()) {
            SoundTools.BenWu();
        } else if (guaiBenRadio.isSelected()) {
            if (good) {
                SoundTools.GuaiAO();
            } else {
                SoundTools.BenWu();
            }
        } else if (customizedSoundRadio.isSelected()) {
            if (soundFile != null && soundFile.exists()) {
                SoundTools.mp3(soundFile);
            } else {
                soundFile = null;
                soundFileLabel.setText(null);
                guaiRadio.setSelected(true);
                SoundTools.GuaiAO();
            }
        }
    }

    @FXML
    public void selectSoundFile() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            File file = mara.mybox.fxml.FxFileTools.selectFile(this, UserConfig.getPath("MusicPath"),
                    FileFilters.Mp3WavExtensionFilter);
            if (file == null) {
                return;
            }
            selectSoundFile(file);
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    public void selectSoundFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        recordFileOpened(file);
        String suffix = FileNameTools.ext(file.getName());
        if (suffix == null
                || (!"mp3".equals(suffix.toLowerCase()) && !"wav".equals(suffix.toLowerCase()))) {
            alertError(message("OnlySupportMp3Wav"));
            return;
        }
        soundFile = file;
        UserConfig.setString("MusicPath", file.getParent());
        UserConfig.setString(baseName + "SoundFile", file.getAbsolutePath());
        soundFileLabel.setText(file.getAbsolutePath());
        SoundTools.mp3(soundFile);
    }

    @FXML
    public void showSoundFileMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        RecentVisitMenu menu = new RecentVisitMenu(this, event, false) {

            @Override
            public void handleSelect() {
                selectSoundFile();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectSoundFile();
                    return;
                }
                selectSoundFile(file);
            }

        };
        menu.setSourceFileType(VisitHistory.FileType.Media)
                .setSourcePathType(VisitHistory.FileType.Media)
                .setSourceExtensionFilter(FileFilters.Mp3WavExtensionFilter);
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            menu.setDefaultPath("C:\\Windows\\media");
        }
        menu.pop();
    }

    @FXML
    public void pickSoundFile(Event event) {
        if (MenuTools.isPopMenu("RecentVisit") || AppVariables.fileRecentNumber <= 0) {
            selectSoundFile();
        } else {
            showSoundFileMenu(event);
        }
    }

    @FXML
    public void popSoundFile(Event event) {
        if (MenuTools.isPopMenu("RecentVisit")) {
            showSoundFileMenu(event);
        }
    }

    public void popScore(Node node, int added) {
        if (added <= 0 || scoreCheck == null || !scoreCheck.isSelected()) {
            return;
        }
        Label popupLabel = new Label("+" + added);
        popupLabel.setStyle("-fx-background-color:black;"
                + " -fx-text-fill: gold;"
                + " -fx-font-size: 3em;"
                + " -fx-padding: 10px;"
                + " -fx-background-radius: 10;");
        final Popup scorePopup = new Popup();
        scorePopup.setAutoFix(true);
        scorePopup.setAutoHide(true);
        scorePopup.getContent().add(popupLabel);
        LocateTools.locateUp(node, scorePopup);

        Timer stimer = new Timer();
        stimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        scorePopup.hide();
                    }
                });
            }
        }, 1000);
    }

}
