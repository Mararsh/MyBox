package mara.mybox;

import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ImageManufactureController;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.image.ImageValueTools;
import mara.mybox.tools.FileTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:02:28
 * @Description
 * @License Apache License Version 2.0
 */
public class MainApp extends Application {

    private static final Logger logger = LogManager.getLogger();
    private static String imageFile;

    @Override
    public void start(Stage stage) throws Exception {
        try {

            // https://bugs.openjdk.java.net/browse/JDK-8041125
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");

            File userPath = new File(CommonValues.UserFilePath);
            if (!userPath.exists()) {
                userPath.mkdirs();
            }
            File configFile = new File(CommonValues.UserConfigFile);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            File alarmFile = new File(CommonValues.AlarmClocksFile);
            if (!alarmFile.exists()) {
                alarmFile.createNewFile();
            }

            ImageValueTools.registrySupportedImageFormats();
            AppVaribles.initAppVaribles();

//            logger.debug(Screen.getPrimary().getDpi());
            FXMLLoader fxmlLoader;
            Pane pane;
            if (imageFile != null) {
                fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageManufactureFxml), AppVaribles.CurrentBundle);
                pane = fxmlLoader.load();
                final ImageManufactureController imageController = (ImageManufactureController) fxmlLoader.getController();
                imageController.loadImage(imageFile);
                imageController.setMyStage(stage);
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        if (!imageController.stageClosing()) {
                            event.consume();
                        }
                    }
                });

            } else {
                fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.MyboxFxml), AppVaribles.CurrentBundle);
                pane = fxmlLoader.load();
                final BaseController controller = fxmlLoader.getController();
                controller.setMyStage(stage);
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        if (!controller.stageClosing()) {
                            event.consume();
                        }
                    }
                });
            }
            try {
                pane.getStylesheets().add(getClass().getResource(AppVaribles.currentStyle).toExternalForm());
            } catch (Exception e) {
                logger.error(e.toString());
            }

            stage.getIcons().add(CommonValues.AppIcon);
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.setScene(new Scene(pane));
            stage.show();

            // https://stackoverflow.com/questions/23527679/trying-to-open-a-javafx-stage-after-calling-platform-exit
            Platform.setImplicitExit(false);
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
        if (args.length > 0) {
            if (CommonValues.SupportedImages.contains(FileTools.getFileSuffix(args[0]))) {
                imageFile = args[0];
            }
        }
        launch(args);
    }

}
