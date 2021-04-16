package mara.mybox.controller;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import mara.mybox.MyBox;
import mara.mybox.db.DataMigration;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @License Apache License Version 2.0
 */
public class MyBoxLoadingController implements Initializable {

    protected String lang;
    protected Stage myStage;
    protected Scene myScene;

    @FXML
    protected Pane thisPane;
    @FXML
    protected ProgressIndicator progressIndicator;
    @FXML
    protected Label infoLabel, derbyLabel;
    @FXML
    protected ImageView imageView;
    @FXML
    protected HBox derbyBox;

    public MyBoxLoadingController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            lang = Locale.getDefault().getLanguage().toLowerCase();
            derbyBox.setVisible(true);
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
            infoLabel.setText(message(lang, "Initializing..."));
            MyBoxLog.console("MyBox Config file:" + AppVariables.MyboxConfigFile);
            Task task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        Platform.runLater(() -> {
                            infoLabel.setText(MessageFormat.format(message(lang,
                                    "InitializeDataUnder"), AppVariables.MyboxDataPath));
                        });
                        if (!initFiles(myStage)) {
                            return null;
                        }

                        Platform.runLater(() -> {
                            infoLabel.setText(MessageFormat.format(message(lang,
                                    "LoadingDatabase"), AppVariables.MyBoxDerbyPath));
                        });
                        DerbyBase.status = DerbyBase.DerbyStatus.NotConnected;
                        String initDB = DerbyBase.startDerby();
                        if (DerbyBase.status != DerbyBase.DerbyStatus.Embedded
                                && DerbyBase.status != DerbyBase.DerbyStatus.Nerwork) {
                            Platform.runLater(() -> {
                                FxmlStage.alertWarning(myStage, initDB);
                                MyBoxLog.console(initDB);
                            });
                            AppVariables.initAppVaribles();
                        } else {
                            // The following statements should be executed in this order
                            Platform.runLater(() -> {
                                infoLabel.setText(message(lang, "InitializingTables"));
                            });
                            DerbyBase.initTables();
                            Platform.runLater(() -> {
                                infoLabel.setText(message(lang, "InitializingVariables"));
                            });
                            AppVariables.initAppVaribles();
                            Platform.runLater(() -> {
                                infoLabel.setText(message(lang, "CheckingMigration"));
                            });
                            MyBoxLog.console(message(lang, "CheckingMigration"));
                            if (!DataMigration.checkUpdates()) {
                                cancel();
                                return null;
                            }
                            Platform.runLater(() -> {
                                infoLabel.setText(message(lang, "InitializingTableValues"));
                            });
                            DerbyBase.initTableValues();
                        }
                        ImageValue.registrySupportedImageFormats();
                        ImageIO.setUseCache(true);
                        ImageIO.setCacheDirectory(AppVariables.MyBoxTempPath);

                        MyBoxLog.info(message(lang, "Load") + " " + CommonValues.AppVersion);
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            infoLabel.setText(e.toString());
                            MyBoxLog.console(e.toString());
                        });
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        infoLabel.setText(message(lang, "LoadingInterface"));

                        String inFile = null;
                        if (AppVariables.appArgs != null) {
                            for (String arg : AppVariables.appArgs) {
                                if (MyBox.InternalRestartFlag.equals(arg) || arg.startsWith("config=")) {
                                    continue;
                                }
                                if (new File(arg).exists()) {
                                    inFile = arg;
                                    break;
                                } else {
                                    FxmlStage.alertError(myStage, MessageFormat.format(
                                            message("FilepathNonAscii"), arg));
                                }
                            }
                        }
                        if (inFile != null) {
                            BaseController controller = FxmlStage.openTarget(myStage, inFile, false);
                            if (controller == null) {
                                FxmlStage.openMyBox(myStage);
                            }
                        } else {
                            FxmlStage.openMyBox(myStage);
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
            MyBoxLog.error(e.toString());
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
                        FxmlStage.alertError(stage, MessageFormat.format(AppVariables.message(lang,
                                "UserPathFail"), AppVariables.MyboxDataPath));
                    });
                    return false;
                }
            }
            MyBoxLog.console("MyBox Data Path:" + AppVariables.MyboxDataPath);

            String oldPath = ConfigTools.readValue("MyBoxOldDataPath");
            if (oldPath != null) {
                if (oldPath.equals(ConfigTools.defaultDataPath())) {
                    FileTools.deleteDirExcept(new File(oldPath), ConfigTools.defaultConfigFile());
                } else {
                    FileTools.deleteDir(new File(oldPath));
                }
                ConfigTools.writeConfigValue("MyBoxOldDataPath", null);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                        FxmlStage.alertError(stage, MessageFormat.format(AppVariables.message(lang, "UserPathFail"),
                                AppVariables.MyBoxLogsPath));
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
                        FxmlStage.alertError(stage, MessageFormat.format(AppVariables.message(lang, "UserPathFail"),
                                AppVariables.MyBoxLanguagesPath));
                    });
                    return false;
                }
            }

            AppVariables.MyBoxDownloadsPath = new File(AppVariables.MyboxDataPath + File.separator + "downloads");
            if (!AppVariables.MyBoxDownloadsPath.exists()) {
                if (!AppVariables.MyBoxDownloadsPath.mkdirs()) {
                    Platform.runLater(() -> {
                        FxmlStage.alertError(stage, MessageFormat.format(AppVariables.message(lang, "UserPathFail"),
                                AppVariables.MyBoxDownloadsPath));
                    });
                    return false;
                }
            }

            AppVariables.MyBoxTempPath = new File(AppVariables.MyboxDataPath + File.separator + "AppTemp");
            if (AppVariables.MyBoxTempPath.exists()) {
                try {
                    FileTools.clearDir(AppVariables.MyBoxTempPath);
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            } else {
                if (!AppVariables.MyBoxTempPath.mkdirs()) {
                    Platform.runLater(() -> {
                        FxmlStage.alertError(stage, MessageFormat.format(AppVariables.message(lang, "UserPathFail"),
                                AppVariables.MyBoxTempPath));
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
                    add(AppVariables.MyBoxDownloadsPath);
                    add(AppVariables.MyBoxLogsPath);
                }
            };

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

}
