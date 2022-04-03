package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class GeographyCodeEditController extends GeographyCodeUserController {

    protected double longitude, latitude, altitude, precision;
    protected long area, population;
    protected GeographyCode loadedCode;
    protected CoordinateSystem coordinateSystem;

    @FXML
    protected TextField gcidInput, subordinateInput, chineseInput, englishInput,
            longitudeInput, latitudeInput, altitudeInput, precisionInput,
            code1Input, code2Input, code3Input, code4Input, code5Input,
            alias1Input, alias2Input, alias3Input, alias4Input, alias5Input,
            areaInput, populationInput;
    @FXML
    protected ComboBox<String> coordinateSystemSelector;
    @FXML
    protected RadioButton globalRadio, continentRadio, countryRadio, provinceRadio, cityRadio,
            countyRadio, townRadio, villageRadio, buildingRadio, pointOfInterestRadio;
    @FXML
    protected Button locationButton;
    @FXML
    protected CheckBox predefinedCheck;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected VBox treeBox;
    @FXML
    protected FlowPane levelPane;

    public GeographyCodeEditController() {
        baseTitle = Languages.message("GeographyCode");
        TipsLabelKey = "GeographyCodeEditComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            longitude = latitude = -200;
            altitude = precision = 0;
            coordinateSystem = CoordinateSystem.defaultCode();

            longitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checklongitude();
                    });

            latitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkLatitude();
                    });

            altitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkAltitude();
                    });

            precisionInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkPrecision();
                    });

            for (CoordinateSystem.Value item : CoordinateSystem.Value.values()) {
                coordinateSystemSelector.getItems().add(Languages.message(item.name()));
            }
            coordinateSystemSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return;
                }
                coordinateSystem = new CoordinateSystem(newValue);
                UserConfig.setString("GeographyCodeCoordinateSystem", newValue);
            });
            coordinateSystemSelector.getSelectionModel().select(UserConfig.getString("GeographyCodeCoordinateSystem", Languages.message("CGCS2000")));

            areaInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkArea();
                    });

            populationInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkPopulation();
                    });

            saveButton.disableProperty().bind(longitudeInput.styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(latitudeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(areaInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(populationInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        locationController.loadTree(this);
        globalRadio.setDisable(true);
        continentRadio.setDisable(true);

    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(locationButton, Languages.message("CoordinateOnMap"));
            NodeStyleTools.setTooltip(gcidInput, Languages.message("AssignedByMyBox"));
            NodeStyleTools.setTooltip(subordinateInput, Languages.message("ClickNodePickValue"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checklongitude() {
        try {
            String s = longitudeInput.getText().trim();
            if (s.isBlank()) {
                longitudeInput.setStyle(null);
                longitude = -200;
                return;
            }
            double v = Double.valueOf(s);
            if (v >= -180 && v <= 180) {
                longitude = v;
                longitudeInput.setStyle(null);
            } else {
                longitudeInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            longitudeInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkLatitude() {
        try {
            String s = latitudeInput.getText().trim();
            if (s.isBlank()) {
                latitudeInput.setStyle(null);
                latitude = -200;
                return;
            }
            double v = Double.valueOf(s);
            if (v >= -90 && v <= 90) {
                latitude = v;
                latitudeInput.setStyle(null);
            } else {
                latitudeInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            latitudeInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkAltitude() {
        try {
            String s = altitudeInput.getText().trim();
            if (s.isBlank()) {
                altitudeInput.setStyle(null);
                altitude = AppValues.InvalidDouble;
                return;
            }
            altitude = Double.valueOf(s);
            altitudeInput.setStyle(null);
        } catch (Exception e) {
            altitudeInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkPrecision() {
        try {
            String s = precisionInput.getText().trim();
            if (s.isBlank()) {
                precisionInput.setStyle(null);
                precision = AppValues.InvalidDouble;
                return;
            }
            precision = Double.valueOf(s);
            precisionInput.setStyle(null);
        } catch (Exception e) {
            precisionInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkArea() {
        try {
            long v = Long.valueOf(areaInput.getText().trim());
            if (v > 0) {
                area = v;
                areaInput.setStyle(null);
            } else {
                areaInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            areaInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkPopulation() {
        try {
            long v = Long.valueOf(populationInput.getText().trim());
            if (v > 0) {
                population = v;
                populationInput.setStyle(null);
            } else {
                populationInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            populationInput.setStyle(UserConfig.badStyle());
        }
    }

    public void load(GeographyCodeController parent, GeographyCode code) {
        parentController = parent;
        try {
            loadedCode = (GeographyCode) (code.clone());
            setGeographyCode(code);
        } catch (Exception e) {
        }
    }

    @FXML
    public void locationAction(ActionEvent event) {
        try {
            LocationInMapController controller = (LocationInMapController) openStage(Fxmls.LocationInMapFxml);
            controller.loadCoordinate(this, longitude, latitude);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setGeographyCode(GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
            if (loadedCode != null && loadedCode.getGcid() > 0
                    && !gcidInput.getText().isBlank()) {
                if (code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                    longitudeInput.setText(code.getLongitude() + "");
                } else {
                    longitudeInput.clear();
                }
                if (code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                    latitudeInput.setText(code.getLatitude() + "");
                } else {
                    latitudeInput.clear();
                }
                return;
            }

            if (loadedCode != null && loadedCode.getGcid() > 0) {
                gcidInput.setText(loadedCode.getGcid() + "");
            } else if (code.getGcid() > 0) {
                gcidInput.setText(code.getGcid() + "");
            } else {
                gcidInput.clear();
            }
            predefinedCheck.setSelected(GeographyCode.isPredefined(code));
            // can not locate the code in the tree since the tree is loaded lazily
            int level = code.getLevel();
            switch (level) {
                case 1:
                    globalRadio.setSelected(true);
                    break;
                case 2:
                    continentRadio.setSelected(true);
                    break;
                case 3:
                    countryRadio.setSelected(true);
                    break;
                case 4:
                    provinceRadio.setSelected(true);
                    break;
                case 5:
                    cityRadio.setSelected(true);
                    break;
                case 6:
                    countyRadio.setSelected(true);
                    break;
                case 7:
                    townRadio.setSelected(true);
                    break;
                case 8:
                    villageRadio.setSelected(true);
                    break;
                case 9:
                    buildingRadio.setSelected(true);
                    break;
                case 10:
                default:
                    level = 10;
                    pointOfInterestRadio.setSelected(true);
            }
            if (level < 3) {
                predefinedCheck.setDisable(true);
                levelPane.setDisable(true);
                treeBox.setDisable(true);
            }
            if (level > 1) {
                codeSelected(TableGeographyCode.getOwner(code, true));
            }

            if (code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                longitudeInput.setText(code.getLongitude() + "");
            } else {
                longitudeInput.clear();
            }
            if (code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                latitudeInput.setText(code.getLatitude() + "");
            } else {
                latitudeInput.clear();
            }
            if (code.getAltitude() != AppValues.InvalidDouble) {
                altitudeInput.setText(code.getAltitude() + "");
            } else {
                altitudeInput.clear();
            }
            if (code.getPrecision() != AppValues.InvalidDouble) {
                precisionInput.setText(code.getPrecision() + "");
            } else {
                precisionInput.clear();
            }
            coordinateSystemSelector.getSelectionModel().select(Languages.message(code.getCoordinateSystem().name()));
            if (code.getChineseName() != null) {
                chineseInput.setText(code.getChineseName());
            }
            if (code.getEnglishName() != null) {
                englishInput.setText(code.getEnglishName());
            }
            if (code.getCode1() != null) {
                code1Input.setText(code.getCode1());
            }
            if (code.getCode2() != null) {
                code2Input.setText(code.getCode2());
            }
            if (code.getCode3() != null) {
                code3Input.setText(code.getCode3());
            }
            if (code.getCode4() != null) {
                code4Input.setText(code.getCode4());
            }
            if (code.getCode5() != null) {
                code5Input.setText(code.getCode5());
            }
            if (code.getAlias1() != null) {
                alias1Input.setText(code.getAlias1());
            }
            if (code.getAlias2() != null) {
                alias2Input.setText(code.getAlias2());
            }
            if (code.getAlias3() != null) {
                alias3Input.setText(code.getAlias3());
            }
            if (code.getAlias4() != null) {
                alias4Input.setText(code.getAlias4());
            }
            if (code.getAlias5() != null) {
                alias5Input.setText(code.getAlias5());
            }
            if (code.getArea() > 0) {
                areaInput.setText(code.getArea() + "");
            }
            if (code.getPopulation() > 0) {
                populationInput.setText(code.getPopulation() + "");
            }

            if (code.getComments() != null) {
                commentsArea.setText(code.getComments());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void codeSelected(GeographyCode code) {
        try {
            selectedCode = code;
            if (code != null) {
                subordinateInput.setText(selectedCode.getFullName());
            } else {
                subordinateInput.setText("");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            String c = chineseInput.getText().trim();
            String e = englishInput.getText().trim();
            if (c.isBlank() && e.isBlank()) {
                popError(Languages.message("GeographyCodeNameNeed"));
                return;
            }

            GeographyCodeLevel level;
            if (globalRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 1);
            } else if (continentRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 2);
            } else if (countryRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 3);
            } else if (provinceRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 4);
            } else if (cityRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 5);
            } else if (countyRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 6);
            } else if (townRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 7);
            } else if (villageRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 8);
            } else if (buildingRadio.isSelected()) {
                level = new GeographyCodeLevel((short) 9);
            } else {
                level = new GeographyCodeLevel((short) 10);
            }
            if (selectedCode == null) {
                if (!globalRadio.isSelected()) {
                    popError(Languages.message("SubordinateNeed"));
                    return;
                }
            } else if (selectedCode.getLevel() >= level.getLevel()) {
//                MyBoxLog.debug(selectedCode.getLevel() + " " + level.getLevel());
                popError(Languages.message("LevelSmallerThanParent"));
                return;
            }

            GeographyCode newCode = new GeographyCode();

            if (gcidInput.getText() == null || gcidInput.getText().isBlank()) {
                newCode.setGcid(-1);
            } else {
                newCode.setGcid(Long.valueOf(gcidInput.getText()));
            }
            if (predefinedCheck.isSelected()) {
                newCode.setSource(GeographyCode.AddressSource.PredefinedData);
            } else {
                newCode.setSource(GeographyCode.AddressSource.InputtedData);
            }
            newCode.setLevelCode(level);
            newCode.setLongitude(longitude);
            newCode.setLatitude(latitude);
            newCode.setAltitude(altitude);
            newCode.setPrecision(precision);
            newCode.setCoordinateSystem(coordinateSystem);
            newCode.setArea(area);
            newCode.setPopulation(population);

            if (!c.isBlank()) {
                newCode.setChineseName(c);
            }
            if (!e.isBlank()) {
                newCode.setEnglishName(e);
            }
            newCode.setCode1(code1Input.getText().trim());
            newCode.setCode2(code2Input.getText().trim());
            newCode.setCode3(code3Input.getText().trim());
            newCode.setCode4(code4Input.getText().trim());
            newCode.setCode5(code5Input.getText().trim());
            newCode.setAlias1(alias1Input.getText().trim());
            newCode.setAlias2(alias2Input.getText().trim());
            newCode.setAlias3(alias3Input.getText().trim());
            newCode.setAlias4(alias4Input.getText().trim());
            newCode.setAlias5(alias5Input.getText().trim());
            if (selectedCode != null) {
                newCode.setOwner(selectedCode.getGcid());
                newCode.setContinent(selectedCode.getContinent());
                newCode.setCountry(selectedCode.getCountry());
                newCode.setProvince(selectedCode.getProvince());
                newCode.setCity(selectedCode.getCity());
                newCode.setTown(selectedCode.getTown());
                newCode.setCounty(selectedCode.getCounty());
                newCode.setVillage(selectedCode.getVillage());
            }

            String comments = commentsArea.getText().trim();
            if (comments.isBlank()) {
                newCode.setComments(null);
            } else {
                newCode.setComments(comments);
            }

            if (!TableGeographyCode.write(newCode)) {
                popFailed();
                return;
            }

            if (parentController != null) {
                ((GeographyCodeController) parentController).refreshAction();
                parentController.getMyStage().requestFocus();
            } else {
                GeographyCodeController controller = (GeographyCodeController) openStage(Fxmls.GeographyCodeFxml);
                controller.getMyStage().requestFocus();
            }
            closeStage();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void copyAction() {
        gcidInput.setText("");
        popInformation(Languages.message("DataCopyComments"));
        if (loadedCode != null) {
            loadedCode.setGcid(-1);
        }
    }

    public void clearData() {
        try {
            gcidInput.clear();
            predefinedCheck.setSelected(false);
            longitudeInput.clear();
            latitudeInput.clear();
            altitudeInput.clear();
            precisionInput.clear();
            chineseInput.clear();
            englishInput.clear();
            code1Input.clear();
            code2Input.clear();
            code3Input.clear();
            code4Input.clear();
            code5Input.clear();
            alias1Input.clear();
            alias2Input.clear();
            alias3Input.clear();
            alias4Input.clear();
            alias5Input.clear();
            areaInput.clear();
            populationInput.clear();
            commentsArea.clear();
            codeSelected(null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        clearData();
        if (loadedCode != null) {
            setGeographyCode(loadedCode);
        }
    }

}
