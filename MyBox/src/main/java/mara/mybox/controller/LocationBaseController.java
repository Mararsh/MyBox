package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.data.BaseTask;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.TableGeographyCode;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationBaseController extends BaseController {

    protected double longtitude, latitude;

    @FXML
    protected TextField longitudeInput, latitudeInput;
    @FXML
    protected ComboBox<String> countrySelector, provinceSelector, citySelector, levelSelector;

    public LocationBaseController() {
        baseTitle = AppVariables.message("Location");
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            if (countrySelector != null) {
                countrySelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            checkCountry();
                        });
            }

            if (provinceSelector != null) {
                provinceSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            checkProvince();
                        });
            }

            if (citySelector != null) {
                citySelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            checkCity();
                        });
            }

            longtitude = latitude = Double.MIN_VALUE;
            if (longitudeInput != null) {
                longitudeInput.textProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            checkLongtitude();
                        });
            }
            if (latitudeInput != null) {
                latitudeInput.textProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            checkLatitude();
                        });
            }

            loadAddresses();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void loadAddresses() {
        synchronized (this) {

            BaseTask addressesTask = new BaseTask<Void>() {

                private List<String> countries, provinces, cities, levels;

                @Override
                protected boolean handle() {
                    countries = TableGeographyCode.countries();
                    provinces = TableGeographyCode.provinces(countrySelector.getValue());
                    cities = TableGeographyCode.cities(
                            countrySelector.getValue(),
                            provinceSelector.getValue());
                    levels = TableGeographyCode.levels();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;

                    if (countrySelector != null) {
                        String v = countrySelector.getValue();
                        countrySelector.getItems().clear();
                        countrySelector.getItems().addAll(countries);
                        if (v != null) {
                            countrySelector.setValue(v);
                        }
                    }

                    if (provinceSelector != null) {
                        String v = provinceSelector.getValue();
                        provinceSelector.getItems().clear();
                        provinceSelector.getItems().addAll(provinces);
                        if (v != null) {
                            provinceSelector.setValue(v);
                        }
                    }

                    if (citySelector != null) {
                        String v = citySelector.getValue();
                        citySelector.getItems().clear();
                        citySelector.getItems().addAll(cities);
                        if (v != null) {
                            citySelector.setValue(v);
                        }
                    }

                    if (levelSelector != null) {
                        String v = levelSelector.getValue();
                        levelSelector.getItems().clear();
                        levelSelector.getItems().addAll(levels);
                        if (v != null) {
                            levelSelector.setValue(v);
                        }
                    }

                    isSettingValues = false;
                }

            };
            openHandlingStage(addressesTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(addressesTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void checkCountry() {
        String s = countrySelector.getValue();
        if (isSettingValues || s == null || s.trim().isBlank()) {
            return;
        }
        try {
            GeographyCode code = GeographyCode.query(s);
            if (code == null) {
                return;
            }
            longitudeInput.setText(code.getLongitude() + "");
            latitudeInput.setText(code.getLatitude() + "");
            loadAddresses();
        } catch (Exception e) {

        }

    }

    protected void checkProvince() {
        String s = provinceSelector.getValue();
        if (isSettingValues || s == null || s.trim().isBlank()) {
            return;
        }
        try {
            GeographyCode code = GeographyCode.query(s);
            if (code == null) {
                return;
            }
            longitudeInput.setText(code.getLongitude() + "");
            latitudeInput.setText(code.getLatitude() + "");
            loadAddresses();
        } catch (Exception e) {
        }
    }

    protected void checkCity() {
        String s = citySelector.getValue();
        if (isSettingValues || s == null || s.trim().isBlank()) {
            return;
        }
        try {
            GeographyCode code = GeographyCode.query(s);
            if (code == null) {
                return;
            }
            longitudeInput.setText(code.getLongitude() + "");
            latitudeInput.setText(code.getLatitude() + "");
            loadAddresses();
        } catch (Exception e) {
        }
    }

    protected void checkLongtitude() {
        try {
            double v = Double.valueOf(longitudeInput.getText().trim());
            if (v >= -180 && v <= 180) {
                longtitude = v;
                longitudeInput.setStyle(null);
            } else {
                longitudeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            longitudeInput.setStyle(badStyle);
        }
    }

    protected void checkLatitude() {
        try {
            double v = Double.valueOf(latitudeInput.getText().trim());
            if (v >= -90 && v <= 90) {
                latitude = v;
                latitudeInput.setStyle(null);
            } else {
                latitudeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            latitudeInput.setStyle(badStyle);
        }
    }

    @FXML
    public void locationAction() {
        try {
            LocationInMapController controller = (LocationInMapController) openStage(CommonValues.LocationInMapFxml);
            controller.locationController = this;
            if (longtitude >= -180 && latitude >= -180) {
                controller.load(longtitude, latitude, 3);
            }
            controller.getMyStage().setAlwaysOnTop(true);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadCode(GeographyCode code) {
        try {
            loadAddresses();
            if (code == null) {
                return;
            }
            isSettingValues = true;
            if (countrySelector != null && code.getCountry() != null) {
                countrySelector.setValue(code.getCountry());
            }
            if (provinceSelector != null && code.getProvince() != null) {
                provinceSelector.setValue(code.getProvince());
            }
            if (citySelector != null && code.getCity() != null) {
                citySelector.setValue(code.getCity());
            }
            if (levelSelector != null && code.getLevel() != null) {
                levelSelector.setValue(code.getLevel());
            }
            isSettingValues = false;

            if (longitudeInput != null
                    && code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                longitudeInput.setText(code.getLongitude() + "");
            }
            if (latitudeInput != null
                    && code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                latitudeInput.setText(code.getLatitude() + "");
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
