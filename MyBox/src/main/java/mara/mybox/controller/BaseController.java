package mara.mybox.controller;

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
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
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
 * @Description
 * @License Apache License Version 2.0
 */
public class BaseController implements Initializable {

    protected String parentFxml;

    protected static final Logger logger = LogManager.getLogger();
    protected String thisFxml;
    protected Stage thisStage, loadingStage;
    protected Alert loadingAlert;

    @FXML
    protected Pane thisPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        thisFxml = FxmlTools.getFxmlPath(url.getPath());
        initStage2();
    }

    @FXML
    private void pdfTools(MouseEvent event) {
        reloadInterface(CommonValues.PdfInterface);
    }

    @FXML
    private void newPdfTools(MouseEvent event) {
        try {
            Pane pane = FXMLLoader.load(getClass().getResource(CommonValues.PdfInterface), AppVaribles.CurrentBundle);
            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(null);
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.getIcons().add(new Image("img/mybox.png"));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void fileTools(MouseEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setHeaderText(null);
            alert.setContentText(AppVaribles.getMessage("Developing..."));
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void imageTools(MouseEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setHeaderText(null);
            alert.setContentText(AppVaribles.getMessage("Developing..."));
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initStage2() {

    }

    public String getParentFxml() {
        return parentFxml;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public String getThisFxml() {
        return thisFxml;
    }

    public void setThisFxml(String thisFxml) {
        this.thisFxml = thisFxml;
    }

    public Stage getThisStage() {
        if (thisStage == null) {
            if (thisPane != null && thisPane.getScene() != null) {
                thisStage = (Stage) thisPane.getScene().getWindow();
            }
        }
        return thisStage;
    }

    public void setThisStage(Stage thisStage) {
        this.thisStage = thisStage;
    }

    public Stage getLoadingStage() {
        return loadingStage;
    }

    public void setLoadingStage(Stage loadingStage) {
        this.loadingStage = loadingStage;
    }

    public Alert getLoadingAlert() {
        return loadingAlert;
    }

    public void setLoadingAlert(Alert loadingAlert) {
        this.loadingAlert = loadingAlert;
    }

    public Pane getThisPane() {
        return thisPane;
    }

    public void setThisPane(Pane thisPane) {
        this.thisPane = thisPane;
    }

    protected void openLoadingStage(final Task<?> task) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.LoadingInterface), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            LoadingController controller = fxmlLoader.getController();
            controller.init(task);

            loadingStage = new Stage();
            loadingStage.initModality(Modality.NONE);
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.initOwner(getThisStage());
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

    protected void reloadInterface(String newFxml) {
        thisStage = getThisStage();
        if (thisStage == null || newFxml == null) {
            return;
        }
        try {
            Pane pane = FXMLLoader.load(getClass().getResource(newFxml), AppVaribles.CurrentBundle);
            thisStage.setScene(new Scene(pane));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
