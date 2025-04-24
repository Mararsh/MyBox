package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class GeographyCodeController extends DataTreeController {

    @FXML
    protected ControlGeographyCodeTree codesController;
    @FXML
    protected ControlGeographyCodeView mapController;

    public GeographyCodeController() {
        baseTitle = message("GeographyCode");
    }

    @Override
    public void initValues() {
        try {
            treeController = codesController;

            super.initValues();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            codesController.setPatrameters(this);
            mapController.setPatrameters(codesController);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void cleanPane() {
        try {
            mapController.cleanPane();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
