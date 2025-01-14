package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
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
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCode.CoordinateSystem;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.tools.GeographyCodeTools.coordinateSystemByName;
import static mara.mybox.tools.GeographyCodeTools.coordinateSystemName;
import mara.mybox.tools.LongTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class ControlDataGeographyCode extends BaseDataValuesController {

    protected TableNodeGeographyCode geoTable;

    @FXML
    protected TextField chineseInput, englishInput, longitudeInput, latitudeInput,
            continentInput, countryInput, provinceInput, cityInput,
            countyInput, townInput, villageInput, buildingInput, poiInput,
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
    protected CheckBox wrapCheck;
    @FXML
    protected TextArea descArea;
    @FXML
    protected VBox treeBox;
    @FXML
    protected FlowPane levelPane;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(locationButton, message("CoordinateOnMap"));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initEditor() {
        try {
            super.initEditor();

            geoTable = (TableNodeGeographyCode) nodeTable;

            listenerChanged(chineseInput);
            listenerChanged(englishInput);
            listenerChanged(longitudeInput);
            listenerChanged(latitudeInput);
            listenerChanged(continentInput);
            listenerChanged(countryInput);
            listenerChanged(provinceInput);
            listenerChanged(cityInput);
            listenerChanged(countyInput);
            listenerChanged(villageInput);
            listenerChanged(buildingInput);
            listenerChanged(poiInput);
            listenerChanged(code1Input);
            listenerChanged(code2Input);
            listenerChanged(code3Input);
            listenerChanged(code4Input);
            listenerChanged(code5Input);
            listenerChanged(alias1Input);
            listenerChanged(alias2Input);
            listenerChanged(alias3Input);
            listenerChanged(alias4Input);
            listenerChanged(alias5Input);
            listenerChanged(populationInput);
            listenerChanged(areaInput);
            listenerChanged(descArea);

            manageWrapped(wrapCheck, descArea);

            for (CoordinateSystem item : CoordinateSystem.values()) {
                coordinateSystemSelector.getItems().add(message(item.name()));
            }
            coordinateSystemSelector.getSelectionModel().select(
                    UserConfig.getString("GeographyCodeCoordinateSystem", message("CGCS2000")));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            if (nodeEditor.currentNode != null) {
                loadNode(nodeEditor.currentNode);
            } else {
                loadNull();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadNull() {
        try {
            isSettingValues = true;
            pointOfInterestRadio.setSelected(true);
            longitudeInput.clear();
            latitudeInput.clear();
            chineseInput.clear();
            englishInput.clear();
            continentInput.clear();
            countryInput.clear();
            provinceInput.clear();
            cityInput.clear();
            countyInput.clear();
            townInput.clear();
            villageInput.clear();
            buildingInput.clear();
            poiInput.clear();
            code1Input.clear();
            code1Input.clear();
            code1Input.clear();
            code1Input.clear();
            code1Input.clear();
            alias1Input.clear();
            alias1Input.clear();
            alias1Input.clear();
            alias1Input.clear();
            alias1Input.clear();
            areaInput.clear();
            populationInput.clear();
            descArea.clear();
            isSettingValues = false;
            valueChanged(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadNode(DataNode node) {
        try {
            if (isSettingValues) {
                return;
            }
            loadNull();
            if (node == null) {
                return;
            }
            isSettingValues = true;
            switch (node.getShortValue("level")) {
                case 0:
                    globalRadio.setSelected(true);
                    break;
                case 1:
                    continentRadio.setSelected(true);
                    break;
                case 2:
                    countryRadio.setSelected(true);
                    break;
                case 3:
                    provinceRadio.setSelected(true);
                    break;
                case 4:
                    cityRadio.setSelected(true);
                    break;
                case 5:
                    countyRadio.setSelected(true);
                    break;
                case 6:
                    townRadio.setSelected(true);
                    break;
                case 7:
                    villageRadio.setSelected(true);
                    break;
                case 8:
                    buildingRadio.setSelected(true);
                    break;
                case 9:
                default:
                    pointOfInterestRadio.setSelected(true);
            }
            double d = node.getDoubleValue("longitude");
            if (d >= -180 && d <= 180) {
                longitudeInput.setText(d + "");
            }
            d = node.getDoubleValue("latitude");
            if (d >= -90 && d <= 90) {
                latitudeInput.setText(d + "");
            }

            coordinateSystemSelector.getSelectionModel()
                    .select(coordinateSystemName(node.getShortValue("coordinate_system")));
            String cname = node.getStringValue("chinese_name");
            chineseInput.setText(cname != null && !cname.isBlank() ? cname : nodeEditor.titleInput.getText());
            String ename = node.getStringValue("english_name");
            englishInput.setText(ename != null && !ename.isBlank() ? ename : nodeEditor.titleInput.getText());
            continentInput.setText(node.getStringValue("continent"));
            countryInput.setText(node.getStringValue("country"));
            provinceInput.setText(node.getStringValue("province"));
            cityInput.setText(node.getStringValue("city"));
            countyInput.setText(node.getStringValue("county"));
            townInput.setText(node.getStringValue("town"));
            villageInput.setText(node.getStringValue("village"));
            buildingInput.setText(node.getStringValue("building"));
            poiInput.setText(node.getStringValue(" poi"));
            code1Input.setText(node.getStringValue("code1"));
            code2Input.setText(node.getStringValue("code2"));
            code3Input.setText(node.getStringValue("code3"));
            code4Input.setText(node.getStringValue("code4"));
            code5Input.setText(node.getStringValue("code5"));
            alias1Input.setText(node.getStringValue("alias1"));
            alias2Input.setText(node.getStringValue("alias2"));
            alias3Input.setText(node.getStringValue("alias3"));
            alias4Input.setText(node.getStringValue("alias4"));
            alias5Input.setText(node.getStringValue("alias5"));

            double area = node.getDoubleValue("area");
            if (area > 0) {
                areaInput.setText(DoubleTools.format(area, null, -1));
            }
            long population = node.getLongValue("population");
            if (population > 0) {
                populationInput.setText(LongTools.format(population, null, -1));
            }
            descArea.setText(node.getStringValue("description"));
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        try {
            String chinese_name = chineseInput.getText();
            String english_name = englishInput.getText();
//            if ((chinese_name == null || chinese_name.isBlank())
//                    && (english_name == null || english_name.isBlank())) {
//                popError(message("GeographyCodeNameNeed"));
//                return null;
//            }
            node.setValue("chinese_name", chinese_name != null ? chinese_name.trim() : null);
            node.setValue("english_name", english_name != null ? english_name.trim() : null);

            double longitude = pickLongitude();
//            if (longitude > 180 || longitude < -180) {
//                popError(message("InvalidValue") + ": " + message("Longitude"));
//                return null;
//            }

            double latitude = pickLatitude();
//            if (latitude > 90 || latitude < -90) {
//                popError(message("InvalidValue") + ": " + message("Latitude"));
//                return null;
//            }

            double area = pickArea();
//            if (area < 0) {
//                popError(message("InvalidValue") + ": " + message("SquareMeters"));
//                return null;
//            }

            long population = pickPopulation();
//            if (population < 0) {
//                popError(message("InvalidValue") + ": " + message("Population"));
//                return null;
//            }

            node.setValue("longitude", longitude);
            node.setValue("latitude", latitude);
            node.setValue("area", area);
            node.setValue("population", population);

            short level;
            if (globalRadio.isSelected()) {
                level = 0;
            } else if (continentRadio.isSelected()) {
                level = 1;
            } else if (countryRadio.isSelected()) {
                level = 2;
            } else if (provinceRadio.isSelected()) {
                level = 3;
            } else if (cityRadio.isSelected()) {
                level = 4;
            } else if (countyRadio.isSelected()) {
                level = 5;
            } else if (townRadio.isSelected()) {
                level = 6;
            } else if (villageRadio.isSelected()) {
                level = 7;
            } else if (buildingRadio.isSelected()) {
                level = 8;
            } else {
                level = 9;
            }
            node.setValue("level", level);

            CoordinateSystem cs = coordinateSystemByName(coordinateSystemSelector.getValue());
            node.setValue("coordinate_system", (short) cs.ordinal());

            String s = code1Input.getText();
            node.setValue("code1", s != null ? s.trim() : null);
            s = code2Input.getText();
            node.setValue("code2", s != null ? s.trim() : null);
            s = code3Input.getText();
            node.setValue("code3", s != null ? s.trim() : null);
            s = code4Input.getText();
            node.setValue("code4", s != null ? s.trim() : null);
            s = code5Input.getText();
            node.setValue("code5", s != null ? s.trim() : null);

            s = alias1Input.getText();
            node.setValue("alias1", s != null ? s.trim() : null);
            s = alias2Input.getText();
            node.setValue("alias2", s != null ? s.trim() : null);
            s = alias3Input.getText();
            node.setValue("alias3", s != null ? s.trim() : null);
            s = alias4Input.getText();
            node.setValue("alias4", s != null ? s.trim() : null);
            s = alias5Input.getText();
            node.setValue("alias5", s != null ? s.trim() : null);

            s = continentInput.getText();
            node.setValue("continent", s != null ? s.trim() : null);
            s = countryInput.getText();
            node.setValue("country", s != null ? s.trim() : null);
            s = provinceInput.getText();
            node.setValue("province", s != null ? s.trim() : null);
            s = cityInput.getText();
            node.setValue("city", s != null ? s.trim() : null);
            s = countyInput.getText();
            node.setValue("county", s != null ? s.trim() : null);
            s = townInput.getText();
            node.setValue("town", s != null ? s.trim() : null);
            s = villageInput.getText();
            node.setValue("village", s != null ? s.trim() : null);
            s = buildingInput.getText();
            node.setValue("building", s != null ? s.trim() : null);
            s = poiInput.getText();
            node.setValue("poi", s != null ? s.trim() : null);

            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected double pickLongitude() {
        try {
            double v = Double.parseDouble(longitudeInput.getText());
            if (v >= -180 && v <= 180) {
                return v;
            }
        } catch (Exception e) {
        }
        return -200;
    }

    protected double pickLatitude() {
        try {
            double v = Double.parseDouble(latitudeInput.getText());
            if (v >= -90 && v <= 90) {
                return v;
            }
        } catch (Exception e) {
        }
        return -200;
    }

    protected double pickArea() {
        try {
            Object area = geoTable.area(areaInput.getText());
            if (area != null) {
                return (double) area;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    protected long pickPopulation() {
        try {
            Object population = geoTable.population(populationInput.getText());
            if (population != null) {
                return (long) population;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    @FXML
    public void locationAction(ActionEvent event) {
        try {
            CoordinatePickerController controller
                    = CoordinatePickerController.open(this, pickLongitude(), pickLatitude(), true);
            controller.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    changeGeographyCode(controller.geographyCode, controller.fillCheck.isSelected());
                    controller.closeStage();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void changeGeographyCode(GeographyCode code, boolean fill) {
        if (code == null) {
            return;
        }
        longitudeInput.setText(code.getLongitude() + "");
        latitudeInput.setText(code.getLatitude() + "");
        if (!fill) {
            return;
        }
        String v = chineseInput.getText();
        if (v == null || v.isBlank()) {
            chineseInput.setText(code.getChineseName());
        }
        v = englishInput.getText();
        if (v == null || v.isBlank()) {
            englishInput.setText(code.getEnglishName());
        }
        v = continentInput.getText();
        if (v == null || v.isBlank()) {
            continentInput.setText(code.getContinent());
        }
        v = countryInput.getText();
        if (v == null || v.isBlank()) {
            countryInput.setText(code.getCountry());
        }
        v = provinceInput.getText();
        if (v == null || v.isBlank()) {
            provinceInput.setText(code.getProvince());
        }
        v = cityInput.getText();
        if (v == null || v.isBlank()) {
            cityInput.setText(code.getCity());
        }
        v = countyInput.getText();
        if (v == null || v.isBlank()) {
            countyInput.setText(code.getCounty());
        }
        v = townInput.getText();
        if (v == null || v.isBlank()) {
            townInput.setText(code.getTown());
        }
        v = villageInput.getText();
        if (v == null || v.isBlank()) {
            villageInput.setText(code.getVillage());
        }
        v = buildingInput.getText();
        if (v == null || v.isBlank()) {
            buildingInput.setText(code.getBuilding());
        }
        v = poiInput.getText();
        if (v == null || v.isBlank()) {
            poiInput.setText(code.getPoi());
        }

        v = code1Input.getText();
        if (v == null || v.isBlank()) {
            code1Input.setText(code.getCode1());
        }
        v = code2Input.getText();
        if (v == null || v.isBlank()) {
            code2Input.setText(code.getCode2());
        }
        v = code3Input.getText();
        if (v == null || v.isBlank()) {
            code3Input.setText(code.getCode3());
        }
        v = code4Input.getText();
        if (v == null || v.isBlank()) {
            code4Input.setText(code.getCode4());
        }
        v = code5Input.getText();
        if (v == null || v.isBlank()) {
            code5Input.setText(code.getCode5());
        }

        v = alias1Input.getText();
        if (v == null || v.isBlank()) {
            alias1Input.setText(code.getAlias1());
        }
        v = alias2Input.getText();
        if (v == null || v.isBlank()) {
            alias2Input.setText(code.getAlias2());
        }
        v = alias3Input.getText();
        if (v == null || v.isBlank()) {
            alias3Input.setText(code.getAlias3());
        }
        v = alias4Input.getText();
        if (v == null || v.isBlank()) {
            alias4Input.setText(code.getAlias4());
        }
        v = alias5Input.getText();
        if (v == null || v.isBlank()) {
            alias5Input.setText(code.getAlias5());
        }

        v = descArea.getText();
        if (v == null || v.isBlank()) {
            descArea.setText(code.getDescription());
        }
    }

    public void loadGeographyCode(GeographyCode code) {
        if (code == null) {
            return;
        }
        loadNode(geoTable.toNode(code));
        nodeEditor.titleInput.setText(code.getTitle());
    }

    /*
        static
     */
    public static DataTreeNodeEditorController editCode(BaseController parent, GeographyCode code) {
        try {
            DataTreeNodeEditorController controller = DataTreeNodeEditorController.open(parent);
            controller.setTable(new TableNodeGeographyCode());
            ((ControlDataGeographyCode) controller.dataController).loadGeographyCode(code);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
}
