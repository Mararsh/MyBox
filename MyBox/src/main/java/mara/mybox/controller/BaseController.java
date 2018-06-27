package mara.mybox.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
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

    protected static final Logger logger = LogManager.getLogger();

    protected List<FileChooser.ExtensionFilter> fileExtensionFilter;

    protected String myFxml, parentFxml;
    protected Stage myStage, loadingStage;
    protected Alert loadingAlert;
    protected Task<Void> task;

    @FXML
    protected Pane thisPane;
    @FXML
    protected Pane mainMenu;
    @FXML
    protected MainMenuController mainMenuController;

    public BaseController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            myFxml = FxmlTools.getFxmlPath(url.getPath());
            if (mainMenuController != null) {
                mainMenuController.setParentFxml(myFxml);
            }

            initializeNext();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initializeNext() {

    }

    public void reloadStage(String newFxml) {
        reloadStage(newFxml, null);
    }

    public void reloadStage(String newFxml, String title) {
        try {
            if (task != null && task.isRunning()) {
                openStage(newFxml, title, false);
                return;
            }
            getMyStage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            BaseController controller = fxmlLoader.getController();
            controller.setMyStage(myStage);
            myStage.setScene(new Scene(pane));
            if (title != null) {
                myStage.setTitle(title);
            } else if (getMyStage().getTitle() == null) {
                myStage.setTitle(AppVaribles.getMessage("AppTitle"));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void openStage(String newFxml, boolean isOwned) {
        openStage(newFxml, AppVaribles.getMessage("AppTitle"), isOwned);
    }

    public void openStage(String newFxml, String title, boolean isOwned) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(newFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            BaseController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);

            Scene scene = new Scene(pane);
            stage.initModality(Modality.NONE);
            if (isOwned) {
                stage.initOwner(getMyStage());
            } else {
                stage.initOwner(null);
            }
            if (title == null) {
                stage.setTitle(AppVaribles.getMessage("AppTitle"));
            } else {
                stage.setTitle(title);
            }
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void popInformation(String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public Stage getMyStage() {
        if (myStage == null) {
            if (thisPane != null && thisPane.getScene() != null) {
                myStage = (Stage) thisPane.getScene().getWindow();
            }
        }
        return myStage;
    }

    public void setMyStage(Stage myStage) {
        this.myStage = myStage;

    }

    public void showImage(String filename) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageViewerFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            ImageViewerController controller = fxmlLoader.getController();
            controller.loadImage(filename);

            Stage stage = new Stage();
            controller.setMyStage(stage);
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.initModality(Modality.NONE);
            stage.initStyle(StageStyle.DECORATED);
            stage.initOwner(null);
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void openLoadingStage(final Task<?> task, Modality block) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.LoadingFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            LoadingController controller = fxmlLoader.getController();
            controller.init(task);

            loadingStage = new Stage();
            loadingStage.initModality(block);
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.initOwner(getMyStage());
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

    public String getParentFxml() {
        return parentFxml;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public String getThisFxml() {
        return myFxml;
    }

    public void setThisFxml(String thisFxml) {
        this.myFxml = thisFxml;
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

}
