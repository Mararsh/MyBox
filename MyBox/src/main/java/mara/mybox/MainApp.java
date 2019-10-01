package mara.mybox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import mara.mybox.controller.BaseController;
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

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:02:28
 * @Description
 * @License Apache License Version 2.0
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            if (!initPaths(stage)) {
                return;
            }

//            makeIcons();  // Uncomment this line to generate Icons automatically in different color styles
            logger.info("Initialize data under " + AppVariables.MyboxDataPath + " ...");
            if (!DerbyBase.initTables()) {
                AppVariables.initAppVaribles();
                FxmlStage.alertWarning(stage,
                        MessageFormat.format(message(Locale.getDefault().getLanguage().toLowerCase(),
                                "DerbyNotAvalibale"), AppVariables.MyBoxDerbyPath));
            } else {
                // The following statements should be done in this order
                AppVariables.initAppVaribles();
                DerbyBase.checkUpdates();
            }

            ImageValue.registrySupportedImageFormats();
            ImageIO.setUseCache(true);
            ImageIO.setCacheDirectory(AppVariables.MyBoxTempPath);

            logger.info("Loading interface...");
            String inFile = null;
            if (getParameters() != null) {
//                logger.info(getParameters().getNamed());
//                logger.info(getParameters().getUnnamed());
//                logger.info(getParameters().getRaw());
                List<String> paremeters = getParameters().getUnnamed();
                if (paremeters != null && !paremeters.isEmpty()) {
                    for (String p : paremeters) {
                        try {
                            if (MyBox.InternalRestartFlag.equals(p) || p.startsWith("config=")) {
                                continue;
                            }
                            if (new File(p).exists()) {
                                inFile = p;
                                break;
                            }
                        } catch (Exception e) {
                        }
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

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static boolean initRootPath(Stage stage) {
        try {
            if (stage == null) {
                return false;
            }
            File currentDataPath = new File(AppVariables.MyboxDataPath);
            if (!currentDataPath.exists()) {
                if (!currentDataPath.mkdirs()) {
                    FxmlStage.alertError(stage,
                            MessageFormat.format(AppVariables.message(Locale.getDefault().getLanguage().toLowerCase(),
                                    "UserPathFail"), AppVariables.MyboxDataPath));
                    return false;
                }
            }
            File defaultPath = ConfigTools.defaultDataPathFile();
            File dbPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_derby");
            if (!dbPath.exists()) {
                if (defaultPath.exists() && !defaultPath.equals(currentDataPath)) {
                    logger.info("Copy app data from orginal path " + defaultPath
                            + " to new path " + AppVariables.MyboxDataPath + " ...");
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

    public static boolean initPaths(Stage stage) {
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
                            MessageFormat.format(AppVariables.message(Locale.getDefault().getLanguage().toLowerCase(), "UserPathFail"),
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
            PixelsOperation redOperation = PixelsOperation.newPixelsOperation(src, scope,
                    OperationType.Hue, ColorActionType.Decrease);
            redOperation.setFloatPara1(215 / 360.0f);
            PixelsOperation pinkOperation = PixelsOperation.newPixelsOperation(src, scope,
                    OperationType.Red, ColorActionType.Increase);
            pinkOperation.setIntPara1(151);
            PixelsOperation orangeOperation = PixelsOperation.newPixelsOperation(src, scope,
                    OperationType.Hue, ColorActionType.Increase);
            orangeOperation.setFloatPara1(171 / 360.0f);
            PixelsOperation blueOperation = PixelsOperation.newPixelsOperation(src, scope,
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
