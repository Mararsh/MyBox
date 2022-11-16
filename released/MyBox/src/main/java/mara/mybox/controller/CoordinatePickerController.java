package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-8
 * @License Apache License Version 2.0
 */
public class CoordinatePickerController extends LocationInMapController {

    protected SimpleBooleanProperty notify;

    public CoordinatePickerController() {
        notify = new SimpleBooleanProperty();
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @FXML
    @Override
    public void okAction() {
        if (geographyCode == null) {
            popError(message("NoData"));
            return;
        }
        notify.set(!notify.get());
    }

    @Override
    public void cleanPane() {
        try {
            notify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    public static CoordinatePickerController open(BaseController parent, double longitude, double latitude) {
        try {
            CoordinatePickerController controller
                    = (CoordinatePickerController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.CoordinatePickerFxml, true);
            controller.loadCoordinate(longitude, latitude);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
