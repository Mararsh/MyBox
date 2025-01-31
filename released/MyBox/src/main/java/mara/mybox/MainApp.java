package mara.mybox;

import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.sysDefaultLanguage;

/**
 * @Author Mara
 * @CreateDate 2020-11-7
 * @License Apache License Version 2.0
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            if (AppVariables.MyboxConfigFile == null
                    || !AppVariables.MyboxConfigFile.exists()
                    || !AppVariables.MyboxConfigFile.isFile()) {
                openStage(stage, Fxmls.MyBoxSetupFxml);
            } else {
                MyBoxLoading(stage);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            stage.close();
        }
    }

    public static void MyBoxLoading(Stage stage) throws Exception {
        try {
            FXMLLoader fxmlLoader = openStage(stage, Fxmls.MyBoxLoadingFxml);
            if (fxmlLoader != null) {
                MyBoxLoadingController loadController
                        = (MyBoxLoadingController) fxmlLoader.getController();
                loadController.run();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            stage.close();
        }
    }

    public static FXMLLoader openStage(Stage stage, String fxml) throws Exception {
        try {
            String lang = sysDefaultLanguage();
            ResourceBundle bundle;
            if (lang.startsWith("zh")) {
                bundle = Languages.BundleZhCN;
            } else {
                bundle = Languages.BundleEn;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(fxml), bundle);
            Pane pane = fxmlLoader.load();
            Scene scene = new Scene(pane);
            stage.setTitle("MyBox v" + AppValues.AppVersion);
            stage.getIcons().add(AppValues.AppIcon);
            stage.setScene(scene);
            stage.show();
            return fxmlLoader;
        } catch (Exception e) {
            MyBoxLog.error(e);
            stage.close();
            return null;
        }
    }

}
