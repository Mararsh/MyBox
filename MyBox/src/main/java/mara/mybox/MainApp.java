package mara.mybox;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:02:28
 * @Description
 * @License Apache License Version 2.0
 */
public class MainApp extends Application {

    private static final String fxml = CommonValues.MyboxInterface;

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage stage) throws Exception {
        try {
            AppVaribles.CurrentBundle = CommonValues.BundleDefault;

//            ImageTools.checkImageFormats();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            Pane pane = FXMLLoader.load(getClass().getResource(fxml), AppVaribles.CurrentBundle);
            Scene scene = new Scene(pane);
//        scene.getStylesheets().add("/styles/Styles.css");

            stage.getIcons().add(new Image("img/mybox.png"));
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.setScene(scene);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.exit(0); // Close the background threads
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
