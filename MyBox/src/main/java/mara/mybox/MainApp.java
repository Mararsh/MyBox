package mara.mybox;

import java.io.File;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import mara.mybox.controller.OpenFile;
import mara.mybox.db.DerbyBase;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.image.ImageValueTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.objects.AppVaribles.logger;

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
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");

            ImageValueTools.registrySupportedImageFormats();

            File userPath = new File(CommonValues.UserFilePath);
            if (!userPath.exists()) {
                userPath.mkdirs();
            }
            DerbyBase.initTables();
            AppVaribles.initAppVaribles();
            DerbyBase.checkUpdates();

            File tempPath = new File(CommonValues.TempPath);
            if (tempPath.exists()) {
                FileTools.deleteDir(tempPath);
            }
            tempPath.mkdirs();

            ImageIO.setUseCache(true);
            ImageIO.setCacheDirectory(new File(CommonValues.TempPath));

            String inFile = null;
            List<String> paremeters = getParameters().getUnnamed();
            if (paremeters != null && !paremeters.isEmpty()) {
                if (new File(paremeters.get(0)).exists()) {
                    inFile = paremeters.get(0);
                }
            }
            if (inFile != null) {
                if (!OpenFile.openTarget(getClass(), stage, inFile)) {
                    OpenFile.openMyBox(getClass(), stage);
                }
            } else {
                OpenFile.openMyBox(getClass(), stage);
            }

            // https://stackoverflow.com/questions/23527679/trying-to-open-a-javafx-stage-after-calling-platform-exit
            Platform.setImplicitExit(false);

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
