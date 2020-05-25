package mara.mybox.controller;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.Location;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationInMapController extends LocationsMapController {

    protected GeographyCodeEditController geographyCodeEditController;
    protected LocationEditBaseController locationController;

    @FXML
    protected TextField locateInput;
    @FXML
    protected TextArea dataArea;
    @FXML
    protected ToggleGroup locateGroup;
    @FXML
    protected RadioButton clickMapRadio, addressRadio, coordinateRadio;
    @FXML
    protected CheckBox multipleCheck;

    public LocationInMapController() {
        baseTitle = AppVariables.message("LocationInMap");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            locateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        checkLocate();
                    });
            checkLocate();

            fitViewCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
//                        mapSize = fitViewCheck.isSelected() ? 0 : 11;
                        AppVariables.setUserConfigValue("LocationInMapFitView", newv);
                    });
            fitViewCheck.setSelected(AppVariables.getUserConfigBoolean("LocationInMapFitView", true));

            multipleCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                        AppVariables.setUserConfigValue("LocationInMapMultiple", newv);
                    });
            multipleCheck.setSelected(AppVariables.getUserConfigBoolean("LocationInMapMultiple", true));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkLocate() {
        if (isSettingValues) {
            return;
        }
        if (coordinateRadio.isSelected()) {
            FxmlControl.setTooltip(locateInput, message("InputCoordinateComments"));
            locateInput.setText("120.629974, 31.324224");
            locateInput.setDisable(false);
            startButton.setDisable(false);
        } else if (addressRadio.isSelected()) {
            FxmlControl.setTooltip(locateInput, message("GeographyCodeComments"));
            locateInput.setText("泰山");
            locateInput.setDisable(false);
            startButton.setDisable(false);
        } else {
            FxmlControl.setTooltip(locateInput, message("InputCoordinateComments"));
            locateInput.setStyle(null);
            locateInput.setText("");
            locateInput.setDisable(true);
            startButton.setDisable(true);
        }
    }

    public String markerImage() {
        String path = "/" + ControlStyle.getIconPath();
        if (markerCircleRadio.isSelected()) {
            return FxmlControl.getInternalFile(path + "iconCircle.png", "map",
                    AppVariables.ControlColor.name() + "Circle.png").getAbsolutePath();

        } else if (markerImageRadio.isSelected()) {
            if (sourceFile != null && sourceFile.exists()) {
                return sourceFile.getAbsolutePath();
            }

        }
        return locationImage();
    }

    public String markerLabel(Location location) {
        if (textNoneRadio.isSelected()) {
            return "";

        } else if (textCoordinateRadio.isSelected()) {
            return location.getLongitude() + "," + location.getLatitude();

        } else if (textAddressRadio.isSelected()) {
            if (location != null && location.getAddress() != null) {
                return location.getAddress();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @FXML
    @Override
    public void startAction() {
        dataArea.clear();
        String value = locateInput.getText().trim();
        if (value.isBlank()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String geoCode;
                private GeographyCode geographyCode;

                @Override
                protected boolean handle() {
                    if (coordinateRadio.isSelected()) {
                        try {
                            String[] values = value.split(",");
                            double longitude = DoubleTools.scale6(Double.valueOf(values[0].trim()));
                            double latitude = DoubleTools.scale6(Double.valueOf(values[1].trim()));
                            geographyCode = GeographyCode.query(longitude, latitude, true);
                        } catch (Exception e) {
                            logger.error(e.toString());
                            return false;
                        }
                    } else {
                        geographyCode = GeographyCode.geoCode(value);
                    }
                    if (geographyCode == null) {
                        return false;
                    }
                    geoCode = geographyCode.info("\n");
                    return geoCode != null;
                }

                @Override
                protected void whenSucceeded() {
                    dataArea.setText(geoCode);
                    String image = markerImage();
                    String label = "";
                    if (textCoordinateRadio.isSelected()) {
                        label = geographyCode.getLongitude() + "," + geographyCode.getLatitude();
                    } else if (textAddressRadio.isSelected()) {
                        label = geographyCode.getName();
                    }
                    LocationTools.addMarkerInGaoDeMap(webEngine,
                            geographyCode.getLongitude(), geographyCode.getLatitude(),
                            label, geoCode.replaceAll("\n", "</br>"),
                            image, multipleCheck.isSelected(),
                            (fitViewCheck.isSelected() ? 0 : mapSize),
                            markerSize, 14);

                    if (isSettingValues) {
                        isSettingValues = false;
                        return;
                    }

                    if (locationController != null) {
                        locationController.loadCode(geographyCode);
//                        locationController.getMyStage().toFront();
                        if (saveCloseCheck.isSelected()) {
                            closeStage();
                        }
                    }
                    if (geographyCodeEditController != null) {
                        geographyCodeEditController.longitudeInput.setText(geographyCode.getLongitude() + "");
                        geographyCodeEditController.latitudeInput.setText(geographyCode.getLatitude() + "");
//                        locationController.getMyStage().toFront();
                        if (saveCloseCheck.isSelected()) {
                            closeStage();
                        }
                    }

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @Override
    protected void mapClicked(double longitude, double latitude) {
        coordinateRadio.setSelected(true);
        locateInput.setText(longitude + ", " + latitude);
        startAction();
    }

    protected void load(double longitude, double latitude, int zoom) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (mapLoaded) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        isSettingValues = true;
                        coordinateRadio.setSelected(true);
                        locateInput.setText(longitude + ", " + latitude);
                        mapSize = zoom;
                        startAction();
                    }
                });
            }
        }, 500, 500);
    }

}
