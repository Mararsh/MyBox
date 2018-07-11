package mara.mybox;

import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.image.ImageValueTools;
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

    @Override
    public void start(Stage stage) throws Exception {
        try {
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            File userPath = new File(CommonValues.UserFilePath);
            if (!userPath.exists()) {
                userPath.mkdirs();
            }
            File configFile = new File(CommonValues.UserConfigFile);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            AppVaribles.CurrentBundle = CommonValues.BundleDefault;
            ImageValueTools.registrySupportedImageFormats();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.MyboxFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            BaseController controller = fxmlLoader.getController();
            controller.setMyStage(stage);

            stage.getIcons().add(CommonValues.AppIcon);
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.setScene(new Scene(pane));
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
//                    System.exit(0); // Close the background threads
                }
            });
            stage.show();
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
        launch(args);
    }

}
