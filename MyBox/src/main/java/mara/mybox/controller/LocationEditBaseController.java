package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.data.BaseTask;
import mara.mybox.data.GeographyCode;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationEditBaseController extends BaseController {

    protected double longitude, latitude;

    @FXML
    protected TextField longitudeInput, latitudeInput;
    @FXML
    protected ComboBox<String> countrySelector, provinceSelector, citySelector, levelSelector;

    public LocationEditBaseController() {
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

            longitude = latitude = Double.MIN_VALUE;
            if (longitudeInput != null) {
                longitudeInput.textProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            checklongitude();
                        });
            }
            if (latitudeInput != null) {
                latitudeInput.textProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            checkLatitude();
                        });
            }

            loadLevels();
            loadCountries();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void loadLevels() {
        if (levelSelector == null) {
            return;
        }
        levelSelector.getItems().clear();
        synchronized (this) {

            BaseTask addressesTask = new BaseTask<Void>() {

                private List<String> levels;

                @Override
                protected boolean handle() {
//                    levels = TableGeographyCode.levels();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    String v = levelSelector.getValue();
                    isSettingValues = true;
                    levelSelector.getItems().addAll(levels);
                    isSettingValues = false;
                    levelSelector.setValue(v);
                }

            };
            openHandlingStage(addressesTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(addressesTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void loadCountries() {
        if (countrySelector == null) {
            return;
        }
        countrySelector.getItems().clear();
        synchronized (this) {

            BaseTask addressesTask = new BaseTask<Void>() {

                private List<String> countries;

                @Override
                protected boolean handle() {
//                    countries = GeographyCode.countries();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    String sc = countrySelector.getValue();
                    countrySelector.getItems().addAll(countries);
                    isSettingValues = false;
                    if (sc != null) {
                        countrySelector.setValue(sc);
                    }
                }
            };
            openHandlingStage(addressesTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(addressesTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void loadProvinces() {
        if (provinceSelector == null) {
            return;
        }
        provinceSelector.getItems().clear();
        String country = countrySelector.getValue();
        if (country == null || country.trim().isBlank()) {
            return;
        }
        synchronized (this) {

            BaseTask addressesTask = new BaseTask<Void>() {

                private List<String> provinces;

                @Override
                protected boolean handle() {
//                    provinces = TableGeographyCode.provinces(country);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    String v = provinceSelector.getValue();
                    isSettingValues = true;
                    provinceSelector.getItems().addAll(provinces);
                    if (v != null) {
                        provinceSelector.setValue(v);
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

    protected void loadCities() {
        if (citySelector == null) {
            return;
        }
        citySelector.getItems().clear();
        String country = countrySelector.getValue();
        String province = provinceSelector.getValue();
        if (country == null || country.trim().isBlank()) {
            return;
        }
        synchronized (this) {

            BaseTask addressesTask = new BaseTask<Void>() {

                private List<String> cities;

                @Override
                protected boolean handle() {
//                    cities = TableGeographyCode.cities(country, province);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    String v = citySelector.getValue();
                    isSettingValues = true;
                    citySelector.getItems().addAll(cities);
                    if (v != null) {
                        citySelector.setValue(v);
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
//            GeographyCode code = GeographyCode.query(message("Country"), s);
//            if (code == null) {
//                return;
//            }
//            longitudeInput.setText(code.getLongitude() + "");
//            latitudeInput.setText(code.getLatitude() + "");
            loadProvinces();
            loadCities();
        } catch (Exception e) {
        }
    }

    protected void checkProvince() {
        String s = provinceSelector.getValue();
        if (isSettingValues || s == null || s.trim().isBlank()) {
            return;
        }
        try {
//            GeographyCode code = GeographyCode.query(message("Province"), s);
//            if (code == null) {
//                return;
//            }
//            longitudeInput.setText(code.getLongitude() + "");
//            latitudeInput.setText(code.getLatitude() + "");
            loadCities();
        } catch (Exception e) {
        }
    }

    protected void checkCity() {
        String s = citySelector.getValue();
        if (isSettingValues || s == null || s.trim().isBlank()) {
            return;
        }
        try {
//            GeographyCode code = GeographyCode.query(message("City"), s);
//            if (code == null) {
//                return;
//            }
//            longitudeInput.setText(code.getLongitude() + "");
//            latitudeInput.setText(code.getLatitude() + "");
        } catch (Exception e) {
        }
    }

    protected void checklongitude() {
        try {
            double v = Double.valueOf(longitudeInput.getText().trim());
            if (v >= -180 && v <= 180) {
                longitude = v;
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
            if (longitude >= -180 && latitude >= -180) {
                controller.load(longitude, latitude, 3);
            }
            controller.getMyStage().setAlwaysOnTop(true);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadCode(GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
//            if (countrySelector != null && code.getCountry() != null) {
//                countrySelector.setValue(code.getCountry());
//            }
//            if (provinceSelector != null && code.getProvince() != null) {
//                provinceSelector.setValue(code.getProvince());
//            }
//            if (citySelector != null && code.getCity() != null) {
//                citySelector.setValue(code.getCity());
//            }
//            if (levelSelector != null && code.getLevel() != null) {
//                levelSelector.setValue(code.getLevel());
//            }

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
