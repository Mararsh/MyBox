package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.GeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-8
 * @License Apache License Version 2.0
 */
public class CoordinatePickerController extends BaseController {

    protected SimpleBooleanProperty notify;
    protected GeographyCode geographyCode;

    @FXML
    protected ControlCoordinatePicker mapController;
    @FXML
    protected CheckBox fillCheck;

    public CoordinatePickerController() {
        notify = new SimpleBooleanProperty();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            mapController.setParameter(false);

            if (fillCheck != null) {
                fillCheck.setSelected(UserConfig.getBoolean(baseName + "Fill", true));
                fillCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            UserConfig.setBoolean(baseName + "Fill", newv);
                        });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadCoordinate(double longitude, double latitude) {
        mapController.loadCoordinate(longitude, latitude);
    }

    @FXML
    @Override
    public void okAction() {
        geographyCode = mapController.geographyCode;
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

    /*
        static
     */
    public static CoordinatePickerController open(BaseController parent,
            double longitude, double latitude, boolean fill) {
        try {
            CoordinatePickerController controller
                    = (CoordinatePickerController) WindowTools.childStage(parent, Fxmls.CoordinatePickerFxml);
            controller.loadCoordinate(longitude, latitude);
            controller.fillCheck.setVisible(fill);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
