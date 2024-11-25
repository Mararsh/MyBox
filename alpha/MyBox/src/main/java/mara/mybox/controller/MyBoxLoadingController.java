package mara.mybox.controller;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import javax.imageio.ImageIO;
import mara.mybox.MyBox;
import mara.mybox.bufferedimage.ImageColorSpace;
import mara.mybox.db.DataMigration;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
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
                    try {
                        info(MessageFormat.format(message(lang, "InitializeDataUnder"), AppVariables.MyboxDataPath));
                        if (!initFiles(myStage)) {
                            return null;
                        }
                        info(MessageFormat.format(message(lang, "LoadingDatabase"), AppVariables.MyBoxDerbyPath));
                        DerbyBase.status = DerbyBase.DerbyStatus.NotConnected;
                        String initDB = DerbyBase.startDerby();
                        if (!DerbyBase.isStarted()) {
                            Platform.runLater(() -> {
                                PopTools.alertWarning(null, initDB);
                                MyBoxLog.console(initDB);
                            });
                            AppVariables.initAppVaribles();
                        } else {
                            // The following statements should be executed in this order
                            info(message(lang, "InitializingTables"));
                            DerbyBase.initTables(loadingController);

                            info(message(lang, "InitializingVariables"));
                            AppVariables.initAppVaribles();

                            info(message(lang, "CheckingMigration"));
                            MyBoxLog.console(message(lang, "CheckingMigration"));
                            if (!DataMigration.checkUpdates(loadingController)) {
                                cancel();
                                return null;
                            }
                            info(message(lang, "InitializingTableValues"));
                            DerbyBase.initTableValues();
                        }

                        initEnv();

                        MyBoxLog.info(message(lang, "Load") + " " + AppValues.AppVersion);
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            infoLabel.setText(e.toString());
                            MyBoxLog.console(e.toString());
                        });
                    }
                    return null;
                }

                protected void initEnv() {
                    try {
                        info(message(lang, "InitializingEnv"));

                        ImageColorSpace.registrySupportedImageFormats();
                        ImageIO.setUseCache(true);
                        ImageIO.setCacheDirectory(AppVariables.MyBoxTempPath);

                        MicrosoftDocumentTools.registryFactories();
//                        AlarmClock.scheduleAll();

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            infoLabel.setText(e.toString());
                            MyBoxLog.console(e.toString());
                        });
                    }
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

    public boolean initRootPath(Stage stage) {
        try {
            if (stage == null) {
                return false;
            }
            File currentDataPath = new File(AppVariables.MyboxDataPath);
            if (!currentDataPath.exists()) {
                if (!currentDataPath.mkdirs()) {
                    Platform.runLater(() -> {
                        PopTools.alertError(null, MessageFormat.format(message(lang,
                                "UserPathFail"), AppVariables.MyboxDataPath));
                    });
                    return false;
                }
            }
            MyBoxLog.console("MyBox Data Path:" + AppVariables.MyboxDataPath);

            String oldPath = ConfigTools.readValue("MyBoxOldDataPath");
            if (oldPath != null) {
                if (oldPath.equals(ConfigTools.defaultDataPath())) {
                    FileDeleteTools.deleteDirExcept(null,
                            new File(oldPath), ConfigTools.defaultConfigFile());
                } else {
                    FileDeleteTools.deleteDir(new File(oldPath));
                }
                ConfigTools.writeConfigValue("MyBoxOldDataPath", null);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean initFiles(Stage stage) {
        try {
            if (!initRootPath(stage)) {
                return false;
            }

            AppVariables.MyBoxLogsPath = new File(AppVariables.MyboxDataPath + File.separator + "logs");
            if (!AppVariables.MyBoxLogsPath.exists()) {
                if (!AppVariables.MyBoxLogsPath.mkdirs()) {
                    Platform.runLater(() -> {
                        PopTools.alertError(null, MessageFormat.format(message(lang, "UserPathFail"), AppVariables.MyBoxLogsPath));
                    });
                    return false;
                }
            }

            AppVariables.MyBoxDerbyPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_derby");
            System.setProperty("derby.stream.error.file", AppVariables.MyBoxLogsPath + File.separator + "derby.log");

            AppVariables.MyBoxLanguagesPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_languages");
            if (!AppVariables.MyBoxLanguagesPath.exists()) {
                if (!AppVariables.MyBoxLanguagesPath.mkdirs()) {
                    Platform.runLater(() -> {
                        PopTools.alertError(null, MessageFormat.format(message(lang, "UserPathFail"), AppVariables.MyBoxLanguagesPath));
                    });
                    return false;
                }
            }

            AppVariables.MyBoxTempPath = new File(AppVariables.MyboxDataPath + File.separator + "AppTemp");
            if (!AppVariables.MyBoxTempPath.exists()) {
                if (!AppVariables.MyBoxTempPath.mkdirs()) {
                    Platform.runLater(() -> {
                        PopTools.alertError(null, MessageFormat.format(message(lang, "UserPathFail"), AppVariables.MyBoxTempPath));
                    });
                    return false;
                }
            }

            AppVariables.AlarmClocksFile = AppVariables.MyboxDataPath + File.separator + ".alarmClocks";

            AppVariables.MyBoxReservePaths = new ArrayList<File>() {
                {
                    add(AppVariables.MyBoxTempPath);
                    add(AppVariables.MyBoxDerbyPath);
                    add(AppVariables.MyBoxLanguagesPath);
                    add(new File(AppPaths.getDownloadsPath()));
                    add(AppVariables.MyBoxLogsPath);
                }
            };

            String prefix = AppPaths.getGeneratedPath() + File.separator;
            new File(prefix + "png").mkdirs();
            new File(prefix + "jpg").mkdirs();
            new File(prefix + "pdf").mkdirs();
            new File(prefix + "htm").mkdirs();
            new File(prefix + "xml").mkdirs();
            new File(prefix + "json").mkdirs();
            new File(prefix + "txt").mkdirs();
            new File(prefix + "csv").mkdirs();
            new File(prefix + "md").mkdirs();
            new File(prefix + "xlsx").mkdirs();
            new File(prefix + "docx").mkdirs();
            new File(prefix + "pptx").mkdirs();
            new File(prefix + "svg").mkdirs();
            new File(prefix + "js").mkdirs();
            new File(prefix + "mp4").mkdirs();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void info(String info) {
        Platform.runLater(() -> {
            infoLabel.setText(info);
        });
        Platform.requestNextPulse();
    }

}
