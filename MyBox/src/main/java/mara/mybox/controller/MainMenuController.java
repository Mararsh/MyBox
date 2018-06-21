/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class MainMenuController extends BaseController {

    @FXML
    private Pane mainMenuPane;
    @FXML
    private Menu homeMenu;
    @FXML
    private ToggleGroup langGroup;
    @FXML
    private RadioMenuItem chineseMenuItem;
    @FXML
    private RadioMenuItem englishMenuItem;
    @FXML
    private Menu pdfMenu;
    @FXML
    private Menu imageMenu;
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu helpMenu;

    @Override
    protected void initializeNext() {

        if (AppVaribles.CurrentBundle == CommonValues.BundleZhCN) {
            chineseMenuItem.setSelected(true);
        } else {
            englishMenuItem.setSelected(true);
        }
    }

    @FXML
    private void showHome(ActionEvent event) {
        openStage(CommonValues.MyboxFxml, false);
    }

    @FXML
    private void setChinese(ActionEvent event) {
        AppVaribles.CurrentBundle = CommonValues.BundleZhCN;
        reloadStage(parentFxml);
    }

    @FXML
    private void setEnglish(ActionEvent event) {
        AppVaribles.CurrentBundle = CommonValues.BundleEnUS;
        reloadStage(parentFxml);
    }

    @FXML
    private void exit(ActionEvent event) {
        getMyStage().close();
    }

    @FXML
    private void openPdfConvertPictures(ActionEvent event) {
        reloadStage(CommonValues.PdfConvertPictureFxml, AppVaribles.getMessage("PdfConvertPictures"));
    }

    @FXML
    private void openImageViewer(ActionEvent event) {
        reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
    }

    @FXML
    private void showImageHelp(ActionEvent event) {
        try {
            File help = FxmlTools.getHelpFile(getClass(), "/docs/ImageHelp.html", "ImageHelp.html");
            Desktop.getDesktop().browse(help.toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void showAbout(ActionEvent event) {
        openStage(CommonValues.AboutFxml, true);
    }

    @Override
    public Stage getMyStage() {
        if (myStage == null) {
            if (mainMenuPane != null && mainMenuPane.getScene() != null) {
                myStage = (Stage) mainMenuPane.getScene().getWindow();
            }
        }
        return myStage;
    }

}
