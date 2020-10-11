package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data.GeographyCode;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2020-4-25
 * @License Apache License Version 2.0
 */
public class GeographyCodeUserController extends BaseController {

    protected GeographyCode selectedCode;

    @FXML
    protected GeographyCodeSelectorController locationController;

    @Override
    public void initControls() {
        try {
            super.initControls();
            selectedCode = null;

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            locationController.loadTree(this);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void codeSelected(GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
            selectedCode = code;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean leavingScene() {
        locationController.leavingScene();
        return super.leavingScene();
    }

}
