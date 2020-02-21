package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.stage.Modality;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-13
 * @License Apache License Version 2.0
 */
public class GeographyRegionController extends BaseController {

    public GeographyRegionController() {
        baseTitle = AppVariables.message("GeographyRegion");
    }

    @Override
    public void initializeNext() {
        try {
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void examplesAction() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {

                    return true;
                }

                @Override
                protected void whenSucceeded() {

                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void clearAction() {

    }

    @FXML
    public void editAction() {
        try {
            GeographyRegionEditController controller
                    = (GeographyRegionEditController) openStage(CommonValues.GeographyRegionEditFxml);
            controller.parent = this;
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {

    }

    @FXML
    public void viewAction() {

    }

}
