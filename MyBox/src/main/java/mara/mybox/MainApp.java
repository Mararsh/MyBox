package mara.mybox;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import mara.mybox.controller.base.BaseController;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.db.DerbyBase;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.image.ImageValue;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:02:28
 * @Description
 * @License Apache License Version 2.0
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {

            // https://pdfbox.apache.org/2.0/getting-started.html
//            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");
            // https://blog.csdn.net/weixin_42156742/article/details/81386226
            System.setProperty("java.awt.headless", "false");

            File userPath = new File(CommonValues.AppDataRoot);
            if (!userPath.exists()) {
                if (!userPath.mkdirs()) {
                    FxmlStage.alertError(stage,
                            MessageFormat.format(AppVaribles.getMessage("UserPathFail"), CommonValues.AppDataRoot));
                    return;
                }
            }
            if (CommonValues.AppTempPath.exists()) {
                FileTools.deleteDir(CommonValues.AppTempPath);
            }
            if (!CommonValues.AppTempPath.mkdirs()) {
                FxmlStage.alertError(stage,
                        MessageFormat.format(AppVaribles.getMessage("UserPathFail"), CommonValues.AppTempPath));
                return;
            }

            // The following 3 statements should be done in this order
            logger.info("Initialize data...");
            DerbyBase.initTables();
            AppVaribles.initAppVaribles();
            DerbyBase.checkUpdates();

            ImageValue.registrySupportedImageFormats();
            ImageIO.setUseCache(true);
            ImageIO.setCacheDirectory(CommonValues.AppTempPath);
//            ControlStyle.loadIcons(getClass());

            logger.info("Loading interface...");
//            if (getParameters().getRaw() != null && !getParameters().getRaw().isEmpty()) {
//                logger.debug(getParameters().getRaw().get(0));
//            }
            String inFile = null;
            List<String> paremeters = getParameters().getUnnamed();
            if (paremeters != null && !paremeters.isEmpty()) {
                for (String p : paremeters) {
                    try {
                        if (new File(p).exists()) {
                            inFile = p;
                            break;
                        }
                    } catch (Exception e) {
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

            //            logger.debug(Screen.getPrimary().getDpi());
        } catch (Exception e) {
            logger.error(e.toString());
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
