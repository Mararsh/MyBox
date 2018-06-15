package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:09
 * @Description
 * @License Apache License Version 2.0
 */
public class TopBarController extends BaseController {

    @FXML
    private HBox CommonBar;

    public static class ColorConversion {

        public static int DEFAULT = 0;
        public static int OTSU = 1;
        public static int THRESHOLD = 9;
    }

    public TopBarController() {
    }

    @FXML
    private void mybox(MouseEvent event) {
        reloadInterface(CommonValues.MyboxInterface);
    }

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

    @FXML
    private void setEnglish(MouseEvent event) {
        AppVaribles.CurrentBundle = CommonValues.BundleZhCN;
        reloadInterface(thisFxml);
    }

    @FXML
    private void setChinese(MouseEvent event) {
        AppVaribles.CurrentBundle = CommonValues.BundleEnUS;
        reloadInterface(thisFxml);
    }

    @FXML
    private void about(MouseEvent event) {
        try {
            Pane aboutPane = FXMLLoader.load(getClass().getResource(CommonValues.AboutInterface), AppVaribles.CurrentBundle);
            Scene scene = new Scene(aboutPane);

            final Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(getThisStage());
            stage.setTitle(AppVaribles.getMessage("AppTitle"));
            stage.getIcons().add(new Image("img/mybox.png"));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
