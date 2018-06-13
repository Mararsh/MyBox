package mara.mybox;

import java.awt.Desktop;
import java.net.URI;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-13 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class AboutController extends MyBoxBaseController {

    private static final Logger logger = LogManager.getLogger();

    @FXML
    private Hyperlink licenseLink;
    @FXML
    private Hyperlink sourceLink;

    @FXML
    private void closeStage() {
        try {
            getStage().close();
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
