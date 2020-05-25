package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class GeographyCodeEditController extends GeographyCodeUserController {

    protected GeographyCodeController parent;
    protected double longitude, latitude;
    protected long area, population;
    protected GeographyCode parentCode;

    @FXML
    protected TextField gcidInput, subordinateInput, chineseInput, englishInput,
            longitudeInput, latitudeInput,
            code1Input, code2Input, code3Input, code4Input, code5Input,
            alias1Input, alias2Input, alias3Input, alias4Input, alias5Input,
            areaInput, populationInput;
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
        baseTitle = AppVariables.message("GeographyCode");
        TipsLabelKey = "GeographyCodeEditComments";
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            longitude = -200;
            latitude = -200;

            longitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checklongitude();
                    });

            latitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkLatitude();
                    });

            areaInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkArea();
                    });

            populationInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkPopulation();
                    });

            saveButton.disableProperty().bind(longitudeInput.styleProperty().isEqualTo(badStyle)
                    .or(latitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(areaInput.styleProperty().isEqualTo(badStyle))
                    .or(populationInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(locationButton, message("CoordinateOnMap"));
        FxmlControl.setTooltip(gcidInput, message("AssignedByMyBox"));
        FxmlControl.setTooltip(subordinateInput, message("ClickNodePickValue"));

        locationController.loadTree(this);
        globalRadio.setDisable(true);
        continentRadio.setDisable(true);
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
                longitudeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            longitudeInput.setStyle(badStyle);
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

    protected void checkArea() {
        try {
            long v = Long.valueOf(areaInput.getText().trim());
            if (v > 0) {
                area = v;
                areaInput.setStyle(null);
            } else {
                areaInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaInput.setStyle(badStyle);
        }
    }

    protected void checkPopulation() {
        try {
            long v = Long.valueOf(populationInput.getText().trim());
            if (v > 0) {
                population = v;
                populationInput.setStyle(null);
            } else {
                populationInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            populationInput.setStyle(badStyle);
        }
    }

    @FXML
    public void locationAction() {
        try {
            LocationInMapController controller = (LocationInMapController) openStage(CommonValues.LocationInMapFxml);
            controller.geographyCodeEditController = this;
            if (longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90) {
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
            if (code == null || code.getLevelCode() == null) {
                return;
            }
            // can not locate the code since the tree is loaded lazily

            isSettingValues = true;
            gcidInput.setText(code.getGcid() + "");
            predefinedCheck.setSelected(code.isPredefined());

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
//                    case 10:
//                        pointOfInterestRadio.setSelected(true);
//                        break;
                default:
                    cityRadio.setSelected(true);
                    break;
            }
            if (level < 3) {
                predefinedCheck.setDisable(true);
                levelPane.setDisable(true);
                treeBox.setDisable(true);
            }
            if (level > 1) {
                parentCode = TableGeographyCode.getParent(code, true);
                codeSelected(parentCode);
            }

            if (code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                longitudeInput.setText(code.getLongitude() + "");
            }
            if (code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                latitudeInput.setText(code.getLatitude() + "");
            }
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

            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
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
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            String c = chineseInput.getText().trim();
            String e = englishInput.getText().trim();
            if (c.isBlank() && e.isBlank()) {
                popError(message("GeographyCodeNameNeed"));
                return;
            }

            GeographyCodeLevel level;
            if (globalRadio.isSelected()) {
                level = new GeographyCodeLevel(1);
            } else if (continentRadio.isSelected()) {
                level = new GeographyCodeLevel(2);
            } else if (countryRadio.isSelected()) {
                level = new GeographyCodeLevel(3);
            } else if (provinceRadio.isSelected()) {
                level = new GeographyCodeLevel(4);
            } else if (cityRadio.isSelected()) {
                level = new GeographyCodeLevel(5);
            } else if (countyRadio.isSelected()) {
                level = new GeographyCodeLevel(6);
            } else if (townRadio.isSelected()) {
                level = new GeographyCodeLevel(7);
            } else if (villageRadio.isSelected()) {
                level = new GeographyCodeLevel(8);
            } else if (buildingRadio.isSelected()) {
                level = new GeographyCodeLevel(9);
//            } else if (pointOfInterestRadio.isSelected()) {
//                level = new GeographyCodeLevel(10);
            } else {
                popError(message("InvalidData"));
                return;
            }
            if (selectedCode == null) {
                if (!globalRadio.isSelected()) {
                    popError(message("SubordinateNeed"));
                    return;
                }
            } else if (selectedCode.getLevel() >= level.getLevel()) {
                popError(message("LevelSmallerThanParent"));
                return;
            }

            GeographyCode newCode = new GeographyCode();

            if (gcidInput.getText() == null || gcidInput.getText().isBlank()) {
                newCode.setGcid(-1);
            } else {
                newCode.setGcid(Long.valueOf(gcidInput.getText()));
            }
            newCode.setPredefined(predefinedCheck.isSelected());
            newCode.setLevelCode(level);
            newCode.setLongitude(longitude);
            newCode.setLatitude(latitude);
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

            if (parent != null) {
                parent.refreshAction();
                parent.getMyStage().toFront();
            }
            if (saveCloseCheck.isSelected()) {
                popSuccessful();
                closeStage();
                return;
            }
            if (selectedCode != null) {
                locationController.removeNode(newCode);
                locationController.addNode(selectedCode, newCode);
            }
            popSuccessful();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
