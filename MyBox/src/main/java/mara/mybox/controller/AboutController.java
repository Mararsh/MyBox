package mara.mybox.controller;

import java.awt.Desktop;
import java.net.URI;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-13 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class AboutController extends BaseController {

    @FXML
    private Label version, date;
    @FXML
    private Hyperlink licenseLink, sourceLink, issuesLink, releasesLink, userGuideLink, cloudLink;

    @Override
    protected void initializeNext() {
        version.setText(CommonValues.AppVersion);
        date.setText(CommonValues.AppVersionDate);
        userGuideLink.setText("https://github.com/Mararsh/MyBox/releases/download/v"
                + CommonValues.AppVersion + "/MyBox-UserGuide-" + CommonValues.AppVersion
                + "-Overview-" + AppVaribles.getLanguage() + ".pdf");

        FxmlTools.miao8();

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

    @FXML
    private void linkIssues() {
        try {
            Desktop.getDesktop().browse(new URI(issuesLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkReleases() {
        try {
            Desktop.getDesktop().browse(new URI(releasesLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkUserGuide() {
        try {
            Desktop.getDesktop().browse(new URI(userGuideLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkCloud() {
        try {
            Desktop.getDesktop().browse(new URI(cloudLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
