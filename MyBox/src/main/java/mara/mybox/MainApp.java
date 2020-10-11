package mara.mybox;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.db.DataMigration;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.DerbyBase.DerbyStatus;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:02:28
 * @Description
 * @License Apache License Version 2.0
 */
public class MainApp extends Application {

    private MyBoxLoadingController loadController;
    private String lang;

    @Override
    public void init() throws Exception {
    }

    public void tmp() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    FxmlStage.class.getResource(CommonValues.MyBoxLoadingFxml));
            Pane pane = fxmlLoader.load();
            Scene scene = new Scene(pane);
            stage.getIcons().add(CommonFxValues.AppIcon);
            stage.setScene(scene);
            stage.show();
            loadController = (MyBoxLoadingController) fxmlLoader.getController();
            lang = Locale.getDefault().getLanguage().toLowerCase();

            Task task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        Platform.runLater(() -> {
                            loadController.setInfo(MessageFormat.format(message(lang,
                                    "InitializeDataUnder"), AppVariables.MyboxDataPath));
                        });
                        if (!initPaths(stage)) {
                            return null;
                        }

                        Platform.runLater(() -> {
                            loadController.pathReady();
                            loadController.setInfo(MessageFormat.format(message(lang,
                                    "LoadingDatabase"), AppVariables.MyBoxDerbyPath));
                        });
                        DerbyBase.status = DerbyStatus.NotConnected;
                        String initDB = DerbyBase.startDerby();
                        if (DerbyBase.status != DerbyStatus.Embedded
                                && DerbyBase.status != DerbyStatus.Nerwork) {
                            Platform.runLater(() -> {
                                FxmlStage.alertWarning(stage, initDB);
                            });
                            AppVariables.initAppVaribles();
                        } else {
                            // The following statements should be executed in this order
                            Platform.runLater(() -> {
                                loadController.setInfo(message(lang, "InitializingTables"));
                            });
                            DerbyBase.initTables();
                            Platform.runLater(() -> {
                                loadController.setInfo(message(lang, "InitializingVariables"));
                            });
                            AppVariables.initAppVaribles();
                            Platform.runLater(() -> {
                                loadController.setInfo(message(lang, "CheckingMigration"));
                            });
                            if (!DataMigration.checkUpdates()) {
                                cancel();
                                return null;
                            }
                            Platform.runLater(() -> {
                                loadController.setInfo(message(lang, "InitializingTableValues"));
                            });
                            DerbyBase.initTableValues();
                        }

                        ImageValue.registrySupportedImageFormats();
                        ImageIO.setUseCache(true);
                        ImageIO.setCacheDirectory(AppVariables.MyBoxTempPath);

                        tmp();

                    } catch (Exception e) {

                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();

                    Platform.runLater(() -> {
                        loadController.setInfo(message(lang, "LoadingInterface"));

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
                                    FxmlStage.alertError(stage, MessageFormat.format(
                                            message("FilepathNonAscii"), arg));
                                }
                            }
                        }

                        if (inFile != null) {
                            BaseController controller = FxmlStage.openTarget(stage, inFile, false);
                            if (controller == null) {
                                FxmlStage.openMyBox(stage);
                            }
                        } else {
                            FxmlStage.openMyBox(stage);
                        }
                    });

                }

                @Override
                protected void failed() {
                    super.failed();
                    stage.close();
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    stage.close();
                }
            };

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
            stage.close();
        }
    }

    public boolean initPaths(Stage stage) {
        try {
            if (!initRootPath(stage)) {
                return false;
            }
            AppVariables.MyBoxTempPath = new File(AppVariables.MyboxDataPath + File.separator + "AppTemp");
            if (AppVariables.MyBoxTempPath.exists()) {
                try {
                    FileTools.clearDir(AppVariables.MyBoxTempPath);
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            } else {
                if (!AppVariables.MyBoxTempPath.mkdirs()) {
                    FxmlStage.alertError(stage,
                            MessageFormat.format(AppVariables.message(lang, "UserPathFail"),
                                    AppVariables.MyBoxTempPath));
                    return false;
                }
            }

            AppVariables.MyBoxLanguagesPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_languages");
            if (!AppVariables.MyBoxLanguagesPath.exists()) {
                if (!AppVariables.MyBoxLanguagesPath.mkdirs()) {
                    FxmlStage.alertError(stage,
                            MessageFormat.format(AppVariables.message(lang, "UserPathFail"),
                                    AppVariables.MyBoxLanguagesPath));
                    return false;
                }
            }

            AppVariables.MyBoxDownloadsPath = new File(AppVariables.MyboxDataPath + File.separator + "downloads");
            if (!AppVariables.MyBoxDownloadsPath.exists()) {
                if (!AppVariables.MyBoxDownloadsPath.mkdirs()) {
                    FxmlStage.alertError(stage,
                            MessageFormat.format(AppVariables.message(lang, "UserPathFail"),
                                    AppVariables.MyBoxDownloadsPath));
                    return false;
                }
            }

            AppVariables.MyBoxDerbyPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_derby");
            AppVariables.MyBoxReservePaths = new ArrayList<File>() {
                {
                    add(AppVariables.MyBoxTempPath);
                    add(AppVariables.MyBoxDerbyPath);
                    add(AppVariables.MyBoxLanguagesPath);
                    add(AppVariables.MyBoxDownloadsPath);
                }
            };
            AppVariables.AlarmClocksFile = AppVariables.MyboxDataPath + File.separator + ".alarmClocks";
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public boolean initRootPath(Stage stage) {
        try {
            if (stage == null) {
                return false;
            }
            File currentDataPath = new File(AppVariables.MyboxDataPath);
            if (!currentDataPath.exists()) {
                if (!currentDataPath.mkdirs()) {
                    FxmlStage.alertError(stage,
                            MessageFormat.format(AppVariables.message(lang,
                                    "UserPathFail"), AppVariables.MyboxDataPath));
                    return false;
                }
            }
            File defaultPath = ConfigTools.defaultDataPathFile();
            File dbPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_derby");
            if (!dbPath.exists()) {
                if (defaultPath.exists() && !defaultPath.equals(currentDataPath)) {
                    Platform.runLater(() -> {
                        loadController.setInfo(MessageFormat.format(
                                AppVariables.message(lang, "CopyingAppData"),
                                defaultPath, AppVariables.MyboxDataPath));
                    });
                    if (FileTools.copyWholeDirectory(defaultPath, currentDataPath, null, false)) {
                        File lckFile = new File(dbPath.getAbsolutePath() + File.separator + "db.lck");
                        if (lckFile.exists()) {
                            try {
                                lckFile.delete();
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    }
                }
            }

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
            logger.error(e.toString());
            return false;
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

}
