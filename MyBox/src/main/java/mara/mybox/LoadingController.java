package mara.mybox;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class LoadingController {

    private static final Logger logger = LogManager.getLogger();

    @FXML
    private ProgressIndicator progressIndicator;

    public void init(final Task<?> task) {
        try {
            progressIndicator.setProgress(-1F);
            progressIndicator.progressProperty().bind(task.progressProperty());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
