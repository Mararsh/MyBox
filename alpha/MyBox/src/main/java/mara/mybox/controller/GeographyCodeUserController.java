package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.dev.MyBoxLog;

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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            locationController.loadTree(this);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void codeSelected(GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
            selectedCode = code;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            locationController.cleanPane();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
