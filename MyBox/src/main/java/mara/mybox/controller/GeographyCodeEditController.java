package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class GeographyCodeEditController extends LocationBaseController {

    protected GeographyCodeController parent;

    @FXML
    protected TextField addressInput, fullInput, districtInput, townshipInput,
            neighbInput, buildingInput, acInput, streetInput, numberInput;

    @FXML
    protected Button locationButton;

    public GeographyCodeEditController() {
        baseTitle = AppVariables.message("GeographyCode");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            saveButton.disableProperty().bind(addressInput.textProperty().isEmpty()
                    .or(longitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(latitudeInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(locationButton, message("CoordinateOnMap"));
    }

    @Override
    public void loadCode(GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
            isSettingValues = true;

            if (code.getAddress() != null) {
                addressInput.setText(code.getAddress());
            }
            if (code.getFullAddress() != null) {
                fullInput.setText(code.getFullAddress());
            }
            if (code.getCountry() != null) {
                countrySelector.setValue(code.getCountry());
            }
            if (code.getProvince() != null) {
                provinceSelector.setValue(code.getProvince());
            }
            if (code.getCity() != null) {
                citySelector.setValue(code.getCity());
            }
            if (code.getDistrict() != null) {
                districtInput.setText(code.getDistrict());
            }
            if (code.getTownship() != null) {
                townshipInput.setText(code.getTownship());
            }
            if (code.getNeighborhood() != null) {
                neighbInput.setText(code.getNeighborhood());
            }
            if (code.getBuilding() != null) {
                buildingInput.setText(code.getBuilding());
            }
            if (code.getAdministrativeCode() != null) {
                acInput.setText(code.getAdministrativeCode());
            }
            if (code.getStreet() != null) {
                streetInput.setText(code.getStreet());
            }
            if (code.getNumber() != null) {
                numberInput.setText(code.getNumber());
            }
            if (code.getLevel() != null) {
                levelSelector.setValue(code.getLevel());
            }
            isSettingValues = false;

            if (code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                longitudeInput.setText(code.getLongitude() + "");
            }
            if (code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                latitudeInput.setText(code.getLatitude() + "");
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            if (longtitude < -180 || longtitude > 180
                    && latitude < -90 && latitude > 90) {
                return;
            }
            GeographyCode code = new GeographyCode();

            code.setAddress(addressInput.getText().trim());
            code.setLongitude(longtitude);
            code.setLatitude(latitude);
            code.setCountry(countrySelector.getValue());
            code.setProvince(provinceSelector.getValue());
            code.setCity(citySelector.getValue());
            code.setDistrict(districtInput.getText().trim());
            code.setTownship(townshipInput.getText().trim());
            code.setNeighborhood(neighbInput.getText().trim());
            code.setBuilding(buildingInput.getText().trim());
            code.setAdministrativeCode(acInput.getText().trim());
            code.setStreet(streetInput.getText().trim());
            code.setNumber(numberInput.getText().trim());
            code.setLevel(levelSelector.getValue());

            if (TableGeographyCode.write(code)) {
                popSuccessful();
            } else {
                popFailed();
            }

            if (parent != null) {
                parent.load();
                parent.getMyStage().toFront();
            }
            if (saveCloseCheck.isSelected()) {
                closeStage();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
