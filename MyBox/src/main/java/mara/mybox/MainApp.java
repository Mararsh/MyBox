package mara.mybox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import mara.mybox.controller.InformationController;
import mara.mybox.db.DerbyBase;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageScope;
import mara.mybox.image.ImageValue;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.PixelsOperation.ColorActionType;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:02:28
 * @Description
 * @License Apache License Version 2.0
 */
public class MainApp extends Application {

    private InformationController infoController;
    private String lang;

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    FxmlStage.class.getResource(CommonValues.InformationFxml));
            Pane pane = fxmlLoader.load();
            Scene scene = new Scene(pane);
            stage.getIcons().add(CommonImageValues.AppIcon);
            stage.setScene(scene);
            stage.show();
            infoController = (InformationController) fxmlLoader.getController();
            lang = Locale.getDefault().getLanguage().toLowerCase();
            infoController.setInfo(message(lang, "Initializing..."));

            Task task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                infoController.setInfo(MessageFormat.format(message(lang,
                                        "InitializeDataUnder"), AppVariables.MyboxDataPath));
                            }
                        });
                        if (!initPaths(stage)) {
                            return null;
                        }

                        // Uncomment this line to generate Icons automatically in different color styles
//                        makeIcons();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                infoController.setInfo(MessageFormat.format(message(lang,
                                        "LoadingDatabase"), AppVariables.MyBoxDerbyPath));
                            }
                        });
                        String initDB = DerbyBase.initDatabase();
                        if (initDB == null) {
                            FxmlStage.alertWarning(stage,
                                    MessageFormat.format(message(lang,
                                            "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath));
                        } else {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    infoController.setInfo(initDB);
                                }
                            });
                            // The following statements should be done in this order
                            AppVariables.initAppVaribles();
                            DerbyBase.checkUpdates();
                        }

                        ImageValue.registrySupportedImageFormats();
                        ImageIO.setUseCache(true);
                        ImageIO.setCacheDirectory(AppVariables.MyBoxTempPath);

                    } catch (Exception e) {

                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            infoController.setInfo(message(lang, "LoadingInterface"));

                            String inFile = null;
                            if (AppVariables.appArgs != null) {
                                for (String arg : AppVariables.appArgs) {
                                    if (MyBox.InternalRestartFlag.equals(arg) || arg.startsWith("config=")) {
                                        continue;
                                    }
                                    if (new File(arg).exists()) {
                                        inFile = arg;
                                        break;
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

            AppVariables.MyBoxDerbyPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_derby");
            AppVariables.MyBoxReservePaths = new ArrayList<File>() {
                {
                    add(AppVariables.MyBoxTempPath);
                    add(AppVariables.MyBoxDerbyPath);
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            infoController.setInfo(MessageFormat.format(
                                    AppVariables.message(lang, "CopyingAppData"),
                                    defaultPath, AppVariables.MyboxDataPath));
                        }
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

            String oldPath = ConfigTools.readConfigValue("MyBoxOldDataPath");
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

    // This is for developement to generate Icons automatically in different color style
    public static void makeIcons() {
        try {
            List<String> keeps = Arrays.asList(
                    "iconRGB.png", "iconSaveAs.png", "iconWOW.png", "iconPDF.png",
                    "iconHue.png", "iconColorWheel.png", "iconColor.png", "iconButterfly.png", "iconPalette.png",
                    "iconMosaic.png", "iconBlackWhite.png", "iconGrayscale.png"
            );
            String srcPath = "D:\\MyBox\\src\\main\\resources\\";
            String redPath = srcPath + "buttons\\";
            FileTools.clearDir(new File(redPath));
            String pinkPath = srcPath + "buttonsPink\\";
            FileTools.clearDir(new File(pinkPath));
            String orangePath = srcPath + "buttonsOrange\\";
            FileTools.clearDir(new File(orangePath));
            String bluePath = srcPath + "buttonsBlue\\";
            FileTools.clearDir(new File(bluePath));

            File[] icons = new File(srcPath + "buttonsLightBlue").listFiles();
            BufferedImage src = null;
            ImageScope scope = new ImageScope();
            PixelsOperation redOperation = PixelsOperation.create(src, scope,
                    OperationType.Hue, ColorActionType.Decrease);
            redOperation.setFloatPara1(215 / 360.0f);
            PixelsOperation pinkOperation = PixelsOperation.create(src, scope,
                    OperationType.Red, ColorActionType.Increase);
            pinkOperation.setIntPara1(151);
            PixelsOperation orangeOperation = PixelsOperation.create(src, scope,
                    OperationType.Hue, ColorActionType.Increase);
            orangeOperation.setFloatPara1(171 / 360.0f);
            PixelsOperation blueOperation = PixelsOperation.create(src, scope,
                    OperationType.Saturation, ColorActionType.Increase);
            blueOperation.setFloatPara1(0.5f);
            String filename;
            for (File icon : icons) {
                filename = icon.getName();
                if (!filename.startsWith("icon") || !filename.endsWith(".png")) {
                    continue;
                }
                src = ImageIO.read(icon);
                if (keeps.contains(filename)) {
                    FileTools.copyFile(icon, new File(redPath + filename));
                    FileTools.copyFile(icon, new File(pinkPath + filename));
                    FileTools.copyFile(icon, new File(orangePath + filename));
                    FileTools.copyFile(icon, new File(bluePath + filename));
                    continue;
                }
                redOperation.setImage(src);
                ImageFileWriters.writeImageFile(redOperation.operate(), "png", redPath + filename);

                pinkOperation.setImage(src);
                ImageFileWriters.writeImageFile(pinkOperation.operate(), "png", pinkPath + filename);

                orangeOperation.setImage(src);
                ImageFileWriters.writeImageFile(orangeOperation.operate(), "png", orangePath + filename);

                blueOperation.setImage(src);
                ImageFileWriters.writeImageFile(blueOperation.operate(), "png", bluePath + filename);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
