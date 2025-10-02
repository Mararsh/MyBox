package mara.mybox.controller;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.MyBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @License Apache License Version 2.0
 */
public class MyBoxLoadingController implements Initializable {

    protected String lang;
    protected Stage myStage;
    protected Scene myScene;
    protected MyBoxLoadingController loadingController;

    @FXML
    protected Pane thisPane;
    @FXML
    protected ProgressIndicator progressIndicator;
    @FXML
    protected Label infoLabel;
    @FXML
    protected ImageView imageView;

    public MyBoxLoadingController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            lang = Languages.embedLangName();
            infoLabel.requestFocus();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public boolean run() {
        try {
            myScene = thisPane.getScene();
            if (myScene == null) {
                return false;
            }
            myStage = (Stage) myScene.getWindow();
            myStage.setUserData(this);
            loadingController = this;
            infoLabel.setText(message(lang, "Initializing..."));
            MyBoxLog.console("MyBox Config file:" + AppVariables.MyboxConfigFile);
            Task task = new Task<Void>() {
                @Override
                protected Void call() {
                    MyBox.initEnv(loadingController, lang);
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        infoLabel.setText(message(lang, "LoadingInterface"));

                        if (TextClipboardTools.isStartWhenBoot()) {
                            TextClipboardTools.setCopy(true);
                            TextClipboardTools.startTextClipboardMonitor();
                        } else {
                            TextClipboardTools.setCopy(false);
                        }

                        String inFile = null;
                        if (AppVariables.AppArgs != null) {
                            for (String arg : AppVariables.AppArgs) {
                                if (MyBox.InternalRestartFlag.equals(arg) || arg.startsWith("config=")) {
                                    continue;
                                }
                                if (new File(arg).exists()) {
                                    inFile = arg;
                                    break;
                                } else {
                                    PopTools.alertError(null, MessageFormat.format(message("FilepathNonAscii"), arg));
                                }
                            }
                        }
                        if (inFile != null) {
                            BaseController controller = ControllerTools.openTarget(inFile, false);
                            if (controller == null) {
                                ControllerTools.openMyBox(myStage);
                            }
                        } else {
                            ControllerTools.openMyBox(myStage);
                        }
                        if (myStage != null) {
                            myStage.close();
                        }
                    });

                }

                @Override
                protected void failed() {
                    super.failed();
                    myStage.close();
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    myStage.close();
                }
            };
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public Stage getMyStage() {
        if (myStage == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
                if (myScene != null) {
                    myStage = (Stage) myScene.getWindow();
                    myStage.setUserData(this);
                }
            }
        }
        return myStage;
    }

    public void info(String info) {
        Platform.runLater(() -> {
            infoLabel.setText(info);
        });
        Platform.requestNextPulse();
    }

}
