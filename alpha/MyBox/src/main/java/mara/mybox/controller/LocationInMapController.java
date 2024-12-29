package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationInMapController extends BaseController {

    @FXML
    protected ControlCoordinatePicker mapController;

    public LocationInMapController() {
        baseTitle = message("LocationInMap");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            mapController.setParameter(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadCoordinate(double longitude, double latitude) {
        mapController.loadCoordinate(longitude, latitude);
    }

    /*
        static
     */
    public static LocationInMapController load(double longitude, double latitude) {
        try {
            LocationInMapController controller
                    = (LocationInMapController) WindowTools.openStage(Fxmls.LocationInMapFxml);
            controller.loadCoordinate(longitude, latitude);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
