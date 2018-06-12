package mara.mybox;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:50:43
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxBaseController implements Initializable {

    private static final Logger logger = LogManager.getLogger();
    protected String fxml;
    protected Stage currentStage, loadingStage;
    protected Alert loadingAlert;

    @FXML
    private Pane topPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxml = FxmlTools.getFxmlPath(url.getPath());
        initStage2();
    }

    protected void initStage2() {

    }

    public String getFxml() {
        return fxml;
    }

    public Pane getTopPane() {
        return topPane;
    }

    protected Stage getStage() {
        if (currentStage == null && topPane != null && topPane.getScene() != null) {
            currentStage = (Stage) topPane.getScene().getWindow();
        }
        return currentStage;
    }

    protected void changeInterface(String newFxml) {
        try {
            currentStage = getStage();
            topPane = null;
            Pane pane = FXMLLoader.load(getClass().getResource(newFxml), AppVaribles.CurrentBundle);
            currentStage.setScene(new Scene(pane));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void mybox() {
        changeInterface(CommonValues.MyboxInterface);
    }

    @FXML
    public void pdfTools() {
        changeInterface(CommonValues.PdfInterface);
    }

    @FXML
    protected void setChinese() {
        AppVaribles.CurrentBundle = CommonValues.BundleZhCN;
        changeInterface(fxml);
    }

    @FXML
    protected void setEnglish() {
        AppVaribles.CurrentBundle = CommonValues.BundleEnUS;
        changeInterface(fxml);
    }

    @FXML
    protected void about() {
        try {
            Pane aboutPane = FXMLLoader.load(getClass().getResource(CommonValues.AboutInterface), AppVaribles.CurrentBundle);
            Scene scene = new Scene(aboutPane);

            final Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(getStage());
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void newPdfTools() {
        try {
            Pane pane = FXMLLoader.load(getClass().getResource(CommonValues.PdfInterface), AppVaribles.CurrentBundle);
            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(null);
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void imageTools() {
        try {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setHeaderText(null);
            alert.setContentText(AppVaribles.getMessage("Developing..."));
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void fileTools() {
        try {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setHeaderText(null);
            alert.setContentText(AppVaribles.getMessage("Developing..."));
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void openLoadingStage(final Task<?> task) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.LoadingInterface), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            LoadingController controller = fxmlLoader.getController();
            controller.init(task);

            loadingStage = new Stage();
            loadingStage.initModality(Modality.NONE);
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.initOwner(getStage());
            loadingStage.setScene(new Scene(pane));
            loadingStage.show();

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loadingStage.close();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
