package mara.mybox.controller;

import java.awt.Desktop;
import java.net.URI;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-13 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class AboutController extends BaseController {

    @FXML
    private Label version;
    @FXML
    private Label date;
    @FXML
    private Hyperlink licenseLink;
    @FXML
    private Hyperlink sourceLink;

    @Override
    protected void initStage2() {
        version.setText(CommonValues.AppVersion + "");
        date.setText(CommonValues.AppVersionDate);
    }

    @FXML
    private void closeStage() {
        try {
            getThisStage().close();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkLicense() {
        try {
            Desktop.getDesktop().browse(new URI(licenseLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkSource() {
        try {
            Desktop.getDesktop().browse(new URI(sourceLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
